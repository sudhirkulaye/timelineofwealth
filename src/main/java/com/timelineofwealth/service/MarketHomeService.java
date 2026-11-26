package com.timelineofwealth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Home page data: world indices, commodities, currencies and rates (Yahoo Finance public spark endpoint, delayed),
 * today's NIFTY 50 movers (via {@link LiveQuoteService}) and market headlines from public RSS feeds.
 * Everything is cached (quotes 2 minutes, headlines 10 minutes) and fetched with short timeouts, so the page
 * stays fast and a source that is down only leaves its own card empty.
 */
@Service
public class MarketHomeService {
    private static final Logger logger = LoggerFactory.getLogger(MarketHomeService.class);

    private static final String SPARK = "https://query1.finance.yahoo.com/v7/finance/spark?range=1d&interval=5m&symbols=";
    private static final long QUOTE_TTL_MS = 2 * 60 * 1000L;
    private static final long NEWS_TTL_MS = 10 * 60 * 1000L;
    private static final long BACKOFF_MS = 5 * 60 * 1000L;
    private static final int TIMEOUT_MS = 5000;

    /** group -> {Yahoo symbol, display name, unit shown after the value} */
    private static final String[][][] GROUPS = {
            {   // India
                    {"^NSEI", "NIFTY 50", ""}, {"^BSESN", "SENSEX", ""}, {"^NSEBANK", "NIFTY Bank", ""},
                    {"NIFTY_MIDCAP_100.NS", "NIFTY Midcap 100", ""}, {"^CNXSC", "NIFTY Smallcap 100", ""},
                    {"^CNXIT", "NIFTY IT", ""}, {"^INDIAVIX", "India VIX", ""}},
            {   // Global
                    {"^GSPC", "S&P 500", ""}, {"^IXIC", "Nasdaq", ""}, {"^DJI", "Dow Jones", ""},
                    {"^FTSE", "FTSE 100", ""}, {"^GDAXI", "DAX", ""}, {"^N225", "Nikkei 225", ""},
                    {"^HSI", "Hang Seng", ""}, {"000001.SS", "Shanghai", ""}},
            {   // Commodities
                    {"GC=F", "Gold", "$/oz"}, {"SI=F", "Silver", "$/oz"}, {"BZ=F", "Brent crude", "$/bbl"},
                    {"CL=F", "WTI crude", "$/bbl"}, {"NG=F", "Natural gas", "$/MMBtu"}, {"HG=F", "Copper", "$/lb"}},
            {   // Currencies and rates
                    {"INR=X", "USD / INR", "₹"}, {"EURINR=X", "EUR / INR", "₹"}, {"GBPINR=X", "GBP / INR", "₹"},
                    {"JPYINR=X", "JPY / INR", "₹"}, {"DX-Y.NYB", "Dollar index", ""}, {"^TNX", "US 10Y yield", "%"},
                    {"BTC-USD", "Bitcoin", "$"}},
    };
    private static final String[] GROUP_KEYS = {"india", "global", "commodities", "currencies"};

    /** {source name, feed url, region} — headlines and links only */
    private static final String[][] FEEDS = {
            {"Economic Times", "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms", "india"},
            {"Mint", "https://www.livemint.com/rss/markets", "india"},
            {"Business Standard", "https://www.business-standard.com/rss/markets-106.rss", "india"},
            {"BusinessLine", "https://www.thehindubusinessline.com/markets/feeder/default.rss", "india"},
            {"CNBC", "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=100727362", "global"},
            {"Financial Times", "https://www.ft.com/markets?format=rss", "global"},
            {"Investing.com", "https://www.investing.com/rss/news_25.rss", "global"},
    };

    private final JdbcTemplate jdbc;
    private final LiveQuoteService liveQuotes;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService pool = Executors.newFixedThreadPool(6, r -> {
        Thread t = new Thread(r, "market-home");
        t.setDaemon(true);
        return t;
    });

    private volatile Map<String, Object> markets;
    private volatile long marketsAt, backoffUntil;
    private volatile Map<String, Object> news;
    private volatile long newsAt;
    private volatile List<String[]> nifty50;          // {ticker, short name}
    private volatile long nifty50At;

    public MarketHomeService(JdbcTemplate jdbc, LiveQuoteService liveQuotes) {
        this.jdbc = jdbc;
        this.liveQuotes = liveQuotes;
    }

    // =====================================================================================================
    // markets
    // =====================================================================================================

    /** {asOf: epoch ms, india: [...], global: [...], commodities: [...], currencies: [...]}; item = {s, n, u, p, pc, r, t, spark} */
    public synchronized Map<String, Object> markets() {
        long now = System.currentTimeMillis();
        if (markets != null && (now - marketsAt < QUOTE_TTL_MS || now < backoffUntil)) return markets;
        List<String> all = new ArrayList<>();
        for (String[][] g : GROUPS) for (String[] s : g) all.add(s[0]);
        Map<String, Map<String, Object>> got = new HashMap<>();
        List<Future<Map<String, Map<String, Object>>>> jobs = new ArrayList<>();
        for (int i = 0; i < all.size(); i += 20) {
            List<String> chunk = all.subList(i, Math.min(i + 20, all.size()));
            jobs.add(pool.submit(() -> spark(chunk)));
        }
        boolean failed = false;
        for (Future<Map<String, Map<String, Object>>> f : jobs) {
            try { got.putAll(f.get(TIMEOUT_MS * 2L, TimeUnit.MILLISECONDS)); } catch (Exception e) { failed = true; }
        }
        if (got.isEmpty()) {
            backoffUntil = now + BACKOFF_MS;
            if (markets != null) return markets;           // keep showing the last good set
        }
        if (failed) logger.warn("Home markets: some Yahoo spark calls failed");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("asOf", now);
        for (int g = 0; g < GROUPS.length; g++) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (String[] s : GROUPS[g]) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("s", s[0]);
                m.put("n", s[1]);
                m.put("u", s[2]);
                Map<String, Object> q = got.get(s[0]);
                if (q == null && markets != null) q = previous(s[0]);
                if (q != null) m.putAll(q);
                items.add(m);
            }
            out.put(GROUP_KEYS[g], items);
        }
        markets = out;
        marketsAt = now;
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> previous(String sym) {
        for (String k : GROUP_KEYS) {
            for (Map<String, Object> m : (List<Map<String, Object>>) markets.get(k)) {
                if (sym.equals(m.get("s")) && m.get("p") != null) {
                    Map<String, Object> q = new LinkedHashMap<>(m);
                    q.remove("s"); q.remove("n"); q.remove("u");
                    return q;
                }
            }
        }
        return null;
    }

    private Map<String, Map<String, Object>> spark(List<String> symbols) throws Exception {
        StringBuilder q = new StringBuilder();
        for (String s : symbols) { if (q.length() > 0) q.append(','); q.append(URLEncoder.encode(s, "UTF-8")); }
        HttpURLConnection conn = open(SPARK + q, "application/json");
        Map<String, Map<String, Object>> out = new HashMap<>();
        try {
            if (conn.getResponseCode() != 200) {
                logger.warn("Home markets: Yahoo spark HTTP " + conn.getResponseCode());
                return out;
            }
            JsonNode root;
            try (InputStream in = conn.getInputStream()) { root = mapper.readTree(in); }
            for (JsonNode r : root.path("spark").path("result")) {
                String s = r.path("symbol").asText("");
                JsonNode resp = r.path("response").path(0);
                JsonNode meta = resp.path("meta");
                double p = meta.path("regularMarketPrice").asDouble(0);
                double pc = meta.path("chartPreviousClose").asDouble(meta.path("previousClose").asDouble(0));
                if (s.isEmpty() || p <= 0 || pc <= 0) continue;
                List<Double> line = new ArrayList<>();
                for (JsonNode c : resp.path("indicators").path("quote").path(0).path("close")) {
                    if (c.isNumber()) line.add(Math.round(c.asDouble() * 10000) / 10000.0);
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("p", p);
                m.put("pc", pc);
                m.put("r", p / pc - 1);
                m.put("t", meta.path("regularMarketTime").asLong(0));
                m.put("spark", thin(line, 80));
                out.put(s, m);
            }
        } finally {
            conn.disconnect();
        }
        return out;
    }

    /** keep at most n points (evenly spaced, always the last) so the payload stays small */
    private static List<Double> thin(List<Double> v, int n) {
        if (v.size() <= n) return v;
        List<Double> out = new ArrayList<>(n);
        double step = (v.size() - 1) / (double) (n - 1);
        for (int i = 0; i < n; i++) out.add(v.get((int) Math.round(i * step)));
        return out;
    }

    // =====================================================================================================
    // NIFTY 50 movers
    // =====================================================================================================

    /** {up, down, flat, gainers: [{t, n, p, r}], losers: [...]} from live quotes of the NIFTY 50 members */
    public Map<String, Object> movers() {
        List<String[]> members = nifty50();
        Map<String, String> names = new HashMap<>();
        List<String> tickers = new ArrayList<>();
        for (String[] m : members) { tickers.add(m[0]); names.put(m[0], m[1]); }
        Map<String, Object> q = liveQuotes.getQuotes(tickers);
        List<Map<String, Object>> rows = new ArrayList<>();
        int up = 0, down = 0, flat = 0;
        for (Map.Entry<String, Object> e : q.entrySet()) {
            @SuppressWarnings("unchecked") Map<String, Object> v = (Map<String, Object>) e.getValue();
            double r = ((Number) v.get("r")).doubleValue();
            if (r > 0.0005) up++; else if (r < -0.0005) down++; else flat++;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("t", e.getKey());
            m.put("n", names.get(e.getKey()));
            m.put("p", v.get("p"));
            m.put("r", r);
            rows.add(m);
        }
        rows.sort(Comparator.comparingDouble((Map<String, Object> m) -> ((Number) m.get("r")).doubleValue()).reversed());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("up", up);
        out.put("down", down);
        out.put("flat", flat);
        out.put("gainers", new ArrayList<>(rows.subList(0, Math.min(6, rows.size()))));
        List<Map<String, Object>> losers = new ArrayList<>(rows.subList(Math.max(0, rows.size() - 6), rows.size()));
        Collections.reverse(losers);
        out.put("losers", losers);
        return out;
    }

    private List<String[]> nifty50() {
        long now = System.currentTimeMillis();
        if (nifty50 != null && now - nifty50At < 6 * 60 * 60 * 1000L) return nifty50;
        List<String[]> out = new ArrayList<>();
        try {
            jdbc.query("SELECT ticker, short_name FROM stock_universe WHERE is_nifty50 = 1", rs -> {
                String t = rs.getString(1);
                if (t != null) out.add(new String[]{t.trim(), rs.getString(2) == null ? t.trim() : rs.getString(2).trim()});
            });
        } catch (Exception e) {
            logger.warn("Home movers: could not read NIFTY 50 members: " + e.getMessage());
        }
        nifty50 = out;
        nifty50At = now;
        return out;
    }

    // =====================================================================================================
    // headlines
    // =====================================================================================================

    /** {india: [{title, link, source, at}], global: [...]}, newest first; headlines and links only */
    public synchronized Map<String, Object> news() {
        long now = System.currentTimeMillis();
        if (news != null && now - newsAt < NEWS_TTL_MS) return news;
        List<Future<List<Map<String, Object>>>> jobs = new ArrayList<>();
        for (String[] f : FEEDS) jobs.add(pool.submit(() -> feed(f[0], f[1], f[2])));
        List<Map<String, Object>> all = new ArrayList<>();
        for (Future<List<Map<String, Object>>> j : jobs) {
            try { all.addAll(j.get(TIMEOUT_MS * 2L, TimeUnit.MILLISECONDS)); } catch (Exception ignored) { }
        }
        if (all.isEmpty() && news != null) { newsAt = now - NEWS_TTL_MS / 2; return news; }  // retry sooner
        all.sort(Comparator.comparingLong((Map<String, Object> m) -> (Long) m.get("at")).reversed());
        Map<String, Object> out = new LinkedHashMap<>();
        for (String region : new String[]{"india", "global"}) {
            List<Map<String, Object>> list = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (Map<String, Object> m : all) {
                if (!region.equals(m.get("_r"))) continue;
                String key = String.valueOf(m.get("title")).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
                if (key.length() > 60) key = key.substring(0, 60);
                if (!seen.add(key)) continue;
                Map<String, Object> x = new LinkedHashMap<>(m);
                x.remove("_r");
                list.add(x);
                if (list.size() >= 30) break;
            }
            out.put(region, list);
        }
        news = out;
        newsAt = now;
        return out;
    }

    private List<Map<String, Object>> feed(String source, String url, String region) {
        List<Map<String, Object>> out = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = open(url, "application/rss+xml, application/xml, text/xml");
            if (conn.getResponseCode() != 200) {
                logger.warn("Home news: " + source + " HTTP " + conn.getResponseCode());
                return out;
            }
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);   // no DTDs / external entities
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setExpandEntityReferences(false);
            DocumentBuilder b = f.newDocumentBuilder();
            Document doc;
            try (InputStream in = conn.getInputStream()) { doc = b.parse(in); }
            NodeList items = doc.getElementsByTagName("item");
            long cutoff = System.currentTimeMillis() - 4L * 24 * 60 * 60 * 1000;
            for (int i = 0; i < items.getLength() && out.size() < 25; i++) {
                Element it = (Element) items.item(i);
                String title = clean(child(it, "title"));
                String link = child(it, "link");
                if (title.isEmpty() || link == null) continue;
                link = link.trim();
                if (!link.startsWith("https://") && !link.startsWith("http://")) continue;
                Long at = parseDate(child(it, "pubDate"));
                if (at == null || at < cutoff) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("title", title.length() > 220 ? title.substring(0, 220) + "…" : title);
                m.put("link", link);
                m.put("source", source);
                m.put("at", at);
                m.put("_r", region);
                out.add(m);
            }
        } catch (Exception e) {
            logger.warn("Home news: " + source + " failed: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return out;
    }

    private static String child(Element e, String tag) {
        NodeList l = e.getElementsByTagName(tag);
        if (l.getLength() == 0) return null;
        Node n = l.item(0);
        return n.getTextContent();
    }

    /** strip any markup and entities left in a headline, collapse whitespace */
    private static String clean(String s) {
        if (s == null) return "";
        s = s.replaceAll("<[^>]*>", " ")
                .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ");
        return s.replaceAll("\\s+", " ").trim();
    }

    private static final String[] DATE_FORMATS = {
            "EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, d MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm Z", "EEE, dd MMM yyyy HH:mm zzz", "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss"};      // the last one (Investing.com) carries no zone: UTC

    private static Long parseDate(String s) {
        if (s == null) return null;
        s = s.trim();
        for (String p : DATE_FORMATS) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(p, Locale.ENGLISH);
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                f.setLenient(false);
                return f.parse(s).getTime();
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static HttpURLConnection open(String url, String accept) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(TIMEOUT_MS);
        c.setReadTimeout(TIMEOUT_MS);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "Mozilla/5.0");
        c.setRequestProperty("Accept", accept);
        return c;
    }
}
