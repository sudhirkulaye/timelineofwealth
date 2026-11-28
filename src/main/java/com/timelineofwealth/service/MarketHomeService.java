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
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * Home page data: Indian and world indices, NSE sector indices, commodities, currencies and rates (Yahoo Finance
 * public spark endpoint, delayed) with a year of daily closes for 1M to 1Y returns, today's NIFTY 50 movers
 * (via {@link LiveQuoteService}), the NIFTY 50 valuation (this site's index_valuation table) and headlines from the
 * RSS feeds of financial newspapers. Everything is cached (quotes 2 minutes, history 1 hour, headlines 10 minutes,
 * valuation 6 hours) and fetched with short timeouts, so the page stays fast and a source that is down only leaves
 * its own card empty.
 */
@Service
public class MarketHomeService {
    private static final Logger logger = LoggerFactory.getLogger(MarketHomeService.class);

    private static final String SPARK_1D = "https://query1.finance.yahoo.com/v7/finance/spark?range=1d&interval=5m&symbols=";
    private static final String SPARK_2Y = "https://query1.finance.yahoo.com/v7/finance/spark?range=2y&interval=1d&symbols=";
    private static final long QUOTE_TTL_MS = 2 * 60 * 1000L;
    private static final long HISTORY_TTL_MS = 60 * 60 * 1000L;
    private static final long NEWS_TTL_MS = 10 * 60 * 1000L;
    private static final long VALUATION_TTL_MS = 6 * 60 * 60 * 1000L;
    private static final long BACKOFF_MS = 5 * 60 * 1000L;
    private static final int TIMEOUT_MS = 5000;
    private static final int CHUNK = 20;                      // Yahoo spark accepts at most 20 symbols per call
    /** return periods in months: 1M, 3M, 6M, 1Y */
    private static final int[] PERIODS = {1, 3, 6, 12};

    /** group -> {Yahoo symbol, display name, unit shown after the value} */
    private static final String[][][] GROUPS = {
            {   // India (Yahoo has no daily history for NIFTY Midcap 100 / Smallcap 100; 150 and 250 are the usual benchmarks)
                    {"^NSEI", "NIFTY 50", ""}, {"^BSESN", "SENSEX", ""}, {"^NSEBANK", "NIFTY Bank", ""},
                    {"NIFTYMIDCAP150.NS", "NIFTY Midcap 150", ""}, {"NIFTYSMLCAP250.NS", "NIFTY Smallcap 250", ""},
                    {"^INDIAVIX", "India VIX", ""}},
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
            {   // NSE sector indices for the heat map: only those with daily history on Yahoo (not Private Bank,
                // Healthcare, Oil & Gas, Consumer Durables, Media, Defence or Capital Markets)
                    {"^NSEBANK", "Bank", ""}, {"^CNXPSUBANK", "PSU Bank", ""}, {"NIFTY_FIN_SERVICE.NS", "Financial Services", ""},
                    {"^CNXIT", "IT", ""}, {"^CNXAUTO", "Auto", ""}, {"^CNXPHARMA", "Pharma", ""}, {"^CNXFMCG", "FMCG", ""},
                    {"^CNXCONSUM", "Consumption", ""}, {"^CNXMETAL", "Metal", ""}, {"^CNXENERGY", "Energy", ""},
                    {"^CNXREALTY", "Realty", ""}, {"^CNXINFRA", "Infrastructure", ""}, {"^CNXPSE", "PSU enterprises", ""},
                    {"^CNXMNC", "MNC", ""}},
    };
    private static final String[] GROUP_KEYS = {"india", "global", "commodities", "currencies", "sectors"};
    /** symbols whose daily closes are sent to the page for the charts (the sector heat map needs only returns) */
    private static final Set<String> CHARTED = new HashSet<>();
    static {
        for (int g = 0; g < 4; g++) for (String[] s : GROUPS[g]) CHARTED.add(s[0]);
    }

    /**
     * A newspaper: its e-paper, its feeds and how many of its latest headlines to show. print = the feeds carry the
     * print edition (today's paper / this week's issue); otherwise they are the paper's latest online stories.
     */
    private static final class Paper {
        final String name, home, epaper, region;
        final int show;
        final boolean print;
        final String[] feeds;
        Paper(String name, String home, String epaper, String region, int show, boolean print, String... feeds) {
            this.name = name; this.home = home; this.epaper = epaper; this.region = region; this.show = show;
            this.print = print; this.feeds = feeds;
        }
    }

    /**
     * Headlines and links only, in order of preference within each region, each paper with a link to its e-paper.
     * Print-edition feeds exist only for Business Standard (today's paper) and The Economist (this week's issue);
     * BusinessLine's today's-paper feed lags by days, and Mint, The Economic Times, the FT and the WSJ publish none,
     * so those show their latest online stories. Financial Express has no feed at all (its /feed/ answers 410 Gone):
     * it is listed with its e-paper link only.
     */
    private static final Paper[] PAPERS = {
            new Paper("Business Standard", "https://www.business-standard.com/todays-paper", "https://epaper.business-standard.com/",
                    "india", 5, true,
                    "https://www.business-standard.com/rss/todays-paper.rss"),
            new Paper("BusinessLine", "https://www.thehindubusinessline.com/markets/", "https://epaper.thehindubusinessline.com/",
                    "india", 4, false,
                    "https://www.thehindubusinessline.com/markets/feeder/default.rss",
                    "https://www.thehindubusinessline.com/economy/feeder/default.rss",
                    "https://www.thehindubusinessline.com/companies/feeder/default.rss"),
            new Paper("Mint", "https://www.livemint.com/market", "https://epaper.livemint.com/",
                    "india", 4, false,
                    "https://www.livemint.com/rss/markets",
                    "https://www.livemint.com/rss/economy",
                    "https://www.livemint.com/rss/companies"),
            new Paper("Financial Express", "https://www.financialexpress.com/", "https://epaper.financialexpress.com/",
                    "india", 0, false),
            new Paper("The Economic Times", "https://economictimes.indiatimes.com/markets", "https://epaper.indiatimes.com/",
                    "india", 3, false,
                    "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms",
                    "https://economictimes.indiatimes.com/news/economy/rssfeeds/1373380680.cms"),
            new Paper("Financial Times", "https://www.ft.com/markets", "https://www.ft.com/todaysnewspaper",
                    "global", 5, false,
                    "https://www.ft.com/markets?format=rss",
                    "https://www.ft.com/global-economy?format=rss",
                    "https://www.ft.com/companies?format=rss"),
            new Paper("The Wall Street Journal", "https://www.wsj.com/finance", "https://www.wsj.com/digital-print-edition",
                    "global", 5, false,
                    "https://feeds.content.dowjones.io/public/rss/RSSMarketsMain",
                    "https://feeds.content.dowjones.io/public/rss/socialeconomyfeed",
                    "https://feeds.content.dowjones.io/public/rss/WSJcomUSBusiness"),
            new Paper("The Economist", "https://www.economist.com/weeklyedition", "https://www.economist.com/weeklyedition",
                    "global", 5, true,
                    "https://www.economist.com/printedition/rss.xml"),
    };

    /** stock-tip headlines (multibaggers, "stocks to buy", targets and stop losses) are left out */
    private static final Pattern TIPS = Pattern.compile(
            "multibagger|penny stock|\\b(stocks?|shares?) to (buy|sell)\\b|\\bbuy or sell\\b|\\bbuy,? sell or hold\\b"
                    + "|\\btarget,? (and |& )?sl\\b|\\bstop[ -]?loss\\b|\\bintraday (trading )?(guide|picks|tips)\\b|\\bjackpot\\b"
                    + "|\\bshould you buy\\b|\\bstocks? under (rs\\.? ?|₹ ?)?\\d",
            Pattern.CASE_INSENSITIVE);
    /** print-edition items that are not stories: cartoons, letters and The Economist's one-word weekly digests */
    private static final Pattern NOT_STORY = Pattern.compile(
            "^(?:(?:kal's )?cartoon\\b|letters\\b)|^(?:politics|business|indicators|the world this week)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final long NEWS_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000;

    private final JdbcTemplate jdbc;
    private final LiveQuoteService liveQuotes;
    private final AnalystViewService analystViews;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService pool = daemons(4, "market-home");          // Yahoo calls
    private final ExecutorService newsPool = daemons(6, "market-news");      // RSS feeds

    // separate locks: a slow feed must not hold up the quotes
    private final Object marketsLock = new Object(), historyLock = new Object(), newsLock = new Object(), valuationLock = new Object(),
            longTermLock = new Object();
    private volatile Map<String, Object> markets;
    private volatile long marketsAt, marketsBackoffUntil;
    private volatile Map<String, Object> history;
    private volatile long historyAt, historyBackoffUntil;
    private volatile Map<String, Object> news;
    private volatile long newsAt;
    private volatile Map<String, Object> valuation;
    private volatile long valuationAt;
    private volatile Map<String, Object> longTerm;
    private volatile long longTermAt;
    private volatile List<String[]> nifty50;          // {ticker, short name}
    private volatile long nifty50At;

    public MarketHomeService(JdbcTemplate jdbc, LiveQuoteService liveQuotes, AnalystViewService analystViews) {
        this.jdbc = jdbc;
        this.liveQuotes = liveQuotes;
        this.analystViews = analystViews;
    }

    private static ExecutorService daemons(int n, String name) {
        return Executors.newFixedThreadPool(n, r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }

    private static List<String> allSymbols() {
        Set<String> all = new LinkedHashSet<>();
        for (String[][] g : GROUPS) for (String[] s : g) all.add(s[0]);
        return new ArrayList<>(all);
    }

    // =====================================================================================================
    // markets (live, delayed)
    // =====================================================================================================

    /**
     * {asOf: epoch ms, india: [...], global: [...], commodities: [...], currencies: [...], sectors: [...]};
     * item = {s, n, u, p, pc, r, t, d, spark}: price, previous close, 1D change ratio, quote time (epoch s),
     * session date (exchange time zone) and the intraday line.
     */
    public Map<String, Object> markets() {
        synchronized (marketsLock) {
            long now = System.currentTimeMillis();
            if (markets != null && (now - marketsAt < QUOTE_TTL_MS || now < marketsBackoffUntil)) return markets;
            Map<String, Map<String, Object>> got = fetchAll(allSymbols(), SPARK_1D, false);
            if (got.isEmpty()) {
                marketsBackoffUntil = now + BACKOFF_MS;
                if (markets != null) return markets;           // keep showing the last good set
            }
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
                    if (g == 4) m.remove("spark");             // the heat map shows numbers only
                    items.add(m);
                }
                out.put(GROUP_KEYS[g], items);
            }
            markets = out;
            marketsAt = now;
            return out;
        }
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

    /** one spark call per 20 symbols, in parallel; symbols that failed are simply missing from the result */
    private Map<String, Map<String, Object>> fetchAll(List<String> symbols, String base, boolean daily) {
        List<Future<Map<String, Map<String, Object>>>> jobs = new ArrayList<>();
        for (int i = 0; i < symbols.size(); i += CHUNK) {
            List<String> chunk = new ArrayList<>(symbols.subList(i, Math.min(i + CHUNK, symbols.size())));
            jobs.add(pool.submit(() -> spark(chunk, base, daily)));
        }
        Map<String, Map<String, Object>> got = new HashMap<>();
        boolean failed = false;
        for (Future<Map<String, Map<String, Object>>> f : jobs) {
            try { got.putAll(f.get(TIMEOUT_MS * 2L, TimeUnit.MILLISECONDS)); } catch (Exception e) { failed = true; }
        }
        if (failed) logger.warn("Home markets: some Yahoo spark calls failed (" + (daily ? "daily" : "intraday") + ")");
        return got;
    }

    private Map<String, Map<String, Object>> spark(List<String> symbols, String base, boolean daily) throws Exception {
        StringBuilder q = new StringBuilder();
        for (String s : symbols) { if (q.length() > 0) q.append(','); q.append(URLEncoder.encode(s, "UTF-8")); }
        HttpURLConnection conn = open(base + q, "application/json");
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
                Map<String, Object> m = daily ? dailyItem(resp) : liveItem(resp);
                if (!s.isEmpty() && m != null) out.put(s, m);
            }
        } finally {
            conn.disconnect();
        }
        return out;
    }

    private static Map<String, Object> liveItem(JsonNode resp) {
        JsonNode meta = resp.path("meta");
        double p = meta.path("regularMarketPrice").asDouble(0);
        double pc = meta.path("chartPreviousClose").asDouble(meta.path("previousClose").asDouble(0));
        if (p <= 0 || pc <= 0) return null;
        List<Double> line = new ArrayList<>();
        for (JsonNode c : resp.path("indicators").path("quote").path(0).path("close")) {
            if (c.isNumber()) line.add(round(c.asDouble()));
        }
        long t = meta.path("regularMarketTime").asLong(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("p", p);
        m.put("pc", pc);
        m.put("r", p / pc - 1);
        m.put("t", t);
        if (t > 0) m.put("d", day(t, zone(meta)).toString());
        m.put("spark", thin(line, 80));
        return m;
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
    // history: a year of daily closes for 1M, 3M, 6M and 1Y returns and charts
    // =====================================================================================================

    /**
     * {asOf, items: {symbol: {d, b, i, c, hi, lo}}}: date of the last daily close (exchange time zone), the close at
     * the start of each period (1M, 3M, 6M, 1Y; null when the history is shorter), where each period starts in c,
     * the daily closes of the last year (charted symbols only) and the 52-week high and low.
     * The page sets today's live price against these.
     */
    public Map<String, Object> history() {
        synchronized (historyLock) {
            long now = System.currentTimeMillis();
            if (history != null && (now - historyAt < HISTORY_TTL_MS || now < historyBackoffUntil)) return history;
            Map<String, Map<String, Object>> got = fetchAll(allSymbols(), SPARK_2Y, true);
            if (got.isEmpty()) {
                historyBackoffUntil = now + BACKOFF_MS;
                if (history != null) return history;
            }
            Map<String, Object> items = new LinkedHashMap<>();
            if (history != null) items.putAll(castItems(history.get("items")));   // keep what failed this time
            for (Map.Entry<String, Map<String, Object>> e : got.entrySet()) {
                Map<String, Object> m = e.getValue();
                if (!CHARTED.contains(e.getKey())) { m.remove("c"); m.remove("i"); }
                items.put(e.getKey(), m);
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("asOf", now);
            out.put("items", items);
            history = out;
            historyAt = got.isEmpty() ? now - HISTORY_TTL_MS + BACKOFF_MS : now;   // empty: retry after the back-off
            return out;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castItems(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : Collections.<String, Object>emptyMap();
    }

    private static Map<String, Object> dailyItem(JsonNode resp) {
        JsonNode meta = resp.path("meta");
        ZoneId zone = zone(meta);
        JsonNode ts = resp.path("timestamp");
        JsonNode cl = resp.path("indicators").path("quote").path(0).path("close");
        List<LocalDate> days = new ArrayList<>();
        List<Double> closes = new ArrayList<>();
        for (int i = 0; i < ts.size() && i < cl.size(); i++) {
            JsonNode c = cl.get(i);
            if (c == null || !c.isNumber() || c.asDouble() <= 0) continue;
            LocalDate d = day(ts.get(i).asLong(), zone);
            if (!days.isEmpty() && !d.isAfter(days.get(days.size() - 1))) {      // a repeated last bar: keep the newer value
                closes.set(closes.size() - 1, c.asDouble());
                continue;
            }
            days.add(d);
            closes.add(c.asDouble());
        }
        if (closes.size() < 2) return null;

        LocalDate today = LocalDate.now(zone);
        int[] at = new int[PERIODS.length];
        for (int k = 0; k < PERIODS.length; k++) at[k] = onOrBefore(days, today.minusMonths(PERIODS[k]));
        int from = Math.max(0, at[PERIODS.length - 1]);
        List<Double> base = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();
        for (int k = 0; k < PERIODS.length; k++) {
            base.add(at[k] < 0 ? null : round(closes.get(at[k])));
            idx.add(at[k] < 0 ? -1 : at[k] - from);
        }
        List<Double> series = new ArrayList<>(closes.size() - from);
        for (int i = from; i < closes.size(); i++) series.add(round(closes.get(i)));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("d", days.get(days.size() - 1).toString());
        m.put("b", base);
        m.put("i", idx);
        m.put("c", series);
        double hi = meta.path("fiftyTwoWeekHigh").asDouble(0), lo = meta.path("fiftyTwoWeekLow").asDouble(0);
        if (hi <= 0 || lo <= 0 || hi < lo) {                     // fall back to the closes of the last year
            hi = 0; lo = Double.MAX_VALUE;
            for (Double v : series) { hi = Math.max(hi, v); lo = Math.min(lo, v); }
        }
        m.put("hi", round(hi));
        m.put("lo", round(lo));
        return m;
    }

    /** index of the last day on or before the target, -1 if the history starts after it */
    private static int onOrBefore(List<LocalDate> days, LocalDate target) {
        int lo = 0, hi = days.size() - 1, k = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (!days.get(mid).isAfter(target)) { k = mid; lo = mid + 1; } else hi = mid - 1;
        }
        return k;
    }

    private static ZoneId zone(JsonNode meta) {
        String tz = meta.path("exchangeTimezoneName").asText("");
        if (!tz.isEmpty()) {
            try { return ZoneId.of(tz); } catch (Exception ignored) { }
        }
        return ZoneOffset.ofTotalSeconds(meta.path("gmtoffset").asInt(0));
    }

    private static LocalDate day(long epochSeconds, ZoneId zone) {
        return Instant.ofEpochSecond(epochSeconds).atZone(zone).toLocalDate();
    }

    /** six significant digits: plenty for a chart or a return, and a third of the bytes */
    private static double round(double v) {
        if (v == 0 || Double.isNaN(v) || Double.isInfinite(v)) return v;
        return new BigDecimal(v).round(new MathContext(6)).doubleValue();
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
        out.put("gainers", new ArrayList<>(rows.subList(0, Math.min(5, rows.size()))));
        List<Map<String, Object>> losers = new ArrayList<>(rows.subList(Math.max(0, rows.size() - 5), rows.size()));
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
    // NIFTY 50 valuation (this site's data)
    // =====================================================================================================

    /** NSE has computed the index P/E from consolidated earnings since 1 April 2021; earlier values are not comparable */
    private static final String PE_FROM = "2021-04-01";

    /**
     * {asOf, pe, pb, dy, median, min, max, below, from}: the latest trailing P/E, P/B and dividend yield of the
     * NIFTY 50 and how the P/E compares with its history since {@link #PE_FROM} (median, range, share of days with a
     * lower P/E). Empty when the table has no data.
     */
    public Map<String, Object> valuation() {
        synchronized (valuationLock) {
            long now = System.currentTimeMillis();
            if (valuation != null && now - valuationAt < VALUATION_TTL_MS) return valuation;
            List<String> dates = new ArrayList<>();
            List<double[]> rows = new ArrayList<>();
            try {
                jdbc.query("SELECT date, pe, pb, div_yield FROM index_valuation WHERE ticker = 'NIFTY' AND pe > 0 "
                        + "AND date >= ? ORDER BY date", new Object[]{PE_FROM}, rs -> {
                    dates.add(rs.getDate(1).toString());
                    rows.add(new double[]{rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)});
                });
            } catch (Exception e) {
                logger.warn("Home valuation: could not read index_valuation: " + e.getMessage());
            }
            Map<String, Object> out = new LinkedHashMap<>();
            if (!rows.isEmpty()) {
                double[] last = rows.get(rows.size() - 1);
                double[] pe = new double[rows.size()];
                int below = 0;
                for (int i = 0; i < pe.length; i++) {
                    pe[i] = rows.get(i)[0];
                    if (pe[i] < last[0]) below++;
                }
                Arrays.sort(pe);
                int n = pe.length;
                double median = n % 2 == 1 ? pe[n / 2] : (pe[n / 2 - 1] + pe[n / 2]) / 2;
                out.put("asOf", dates.get(dates.size() - 1));
                out.put("from", dates.get(0));
                out.put("pe", last[0]);
                out.put("pb", last[1] > 0 ? last[1] : null);
                out.put("dy", last[2] > 0 ? last[2] : null);
                out.put("median", Math.round(median * 100) / 100.0);
                out.put("min", pe[0]);
                out.put("max", pe[n - 1]);
                out.put("below", Math.round(below * 1000.0 / n) / 1000.0);
            }
            valuation = out;
            valuationAt = rows.isEmpty() ? now - VALUATION_TTL_MS + BACKOFF_MS : now;
            return out;
        }
    }

    // =====================================================================================================
    // long-term index returns and research teasers (this site's data, summarised so the page stays light)
    // =====================================================================================================

    private static final String[][] LONG_INDICES = {
            {"NIFTY", "NIFTY 50"}, {"NIFTY200", "NIFTY 200"}, {"BSEMidCap", "BSE Midcap"}, {"BSESmallCap", "BSE Smallcap"}};
    private static final int[] LONG_MONTHS = {12, 36, 60, 120, 240};
    private static final long LONG_TERM_TTL_MS = 60 * 60 * 1000L;

    /**
     * {months: [12, 36, 60, 120, 240], rows: [{name, close, asOf, r: [...]}]}: price returns (dividends excluded) from
     * the index_valuation table, measured from the close on or before the same date n months earlier; 3Y and longer
     * annualised, null when the history is shorter.
     */
    public Map<String, Object> longTerm() {
        synchronized (longTermLock) {
            long now = System.currentTimeMillis();
            if (longTerm != null && now - longTermAt < LONG_TERM_TTL_MS) return longTerm;
            List<Map<String, Object>> rows = new ArrayList<>();
            for (String[] ix : LONG_INDICES) {
                List<LocalDate> d = new ArrayList<>();
                List<Double> v = new ArrayList<>();
                try {
                    jdbc.query("SELECT date, value FROM index_valuation WHERE ticker = ? AND value > 0 ORDER BY date",
                            new Object[]{ix[0]}, rs -> { d.add(rs.getDate(1).toLocalDate()); v.add(rs.getDouble(2)); });
                } catch (Exception e) {
                    logger.warn("Home long-term returns: could not read " + ix[0] + ": " + e.getMessage());
                }
                if (d.size() < 2) continue;
                LocalDate last = d.get(d.size() - 1);
                double close = v.get(v.size() - 1);
                List<Double> r = new ArrayList<>();
                for (int months : LONG_MONTHS) {
                    int k = onOrBefore(d, last.minusMonths(months));
                    if (k < 0) { r.add(null); continue; }
                    double x = months >= 36 ? Math.pow(close / v.get(k), 12.0 / months) - 1 : close / v.get(k) - 1;
                    r.add(Math.round(x * 100000) / 100000.0);
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", ix[1]);
                m.put("close", close);
                m.put("asOf", last.toString());
                m.put("r", r);
                rows.add(m);
            }
            List<Integer> months = new ArrayList<>();
            for (int mo : LONG_MONTHS) months.add(mo);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("months", months);
            out.put("rows", rows);
            longTerm = out;
            longTermAt = rows.isEmpty() ? now - LONG_TERM_TTL_MS + BACKOFF_MS : now;
            return out;
        }
    }

    /**
     * Teasers for the research desk: {economy: {asOf, themes, critical, top: [three headlines]},
     * sectors: {asOf, total, counts: {trend: n}, improving: [sector], weakening: [sector]}}. The notes themselves are
     * cached by {@link AnalystViewService}; a part is left out when its sheet is empty.
     */
    public Map<String, Object> research() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Map<String, Object> e = analystViews.getEconomy();
            List<?> nut = asList(e.get("nutshell")), points = asList(e.get("points"));
            if (!nut.isEmpty() || !points.isEmpty()) {
                List<String> top = new ArrayList<>();
                for (Object n : nut) {
                    String h = n instanceof Map ? str(((Map<?, ?>) n).get("headline")) : str(n);
                    if (!h.isEmpty()) top.add(h);
                    if (top.size() == 3) break;
                }
                int critical = 0;
                for (Object p : points) {
                    if (p instanceof Map && "critical".equalsIgnoreCase(str(((Map<?, ?>) p).get("importance")))) critical++;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("asOf", e.get("asOf"));
                m.put("themes", nut.size());
                m.put("critical", critical);
                m.put("top", top);
                out.put("economy", m);
            }
        } catch (Exception ex) {
            logger.warn("Home research: economy summary failed: " + ex.getMessage());
        }
        try {
            Map<String, Object> s = analystViews.getSectors();
            List<?> list = asList(s.get("sectors"));
            if (!list.isEmpty()) {
                Map<String, Integer> counts = new LinkedHashMap<>();
                List<String> improving = new ArrayList<>(), weakening = new ArrayList<>();
                for (Object o : list) {
                    if (!(o instanceof Map)) continue;
                    Map<?, ?> x = (Map<?, ?>) o;
                    String trend = str(x.get("trend"));
                    if (trend.isEmpty()) continue;
                    counts.merge(trend, 1, Integer::sum);
                    if ("Improving".equalsIgnoreCase(trend)) improving.add(str(x.get("sector")));
                    else if ("Weakening".equalsIgnoreCase(trend)) weakening.add(str(x.get("sector")));
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("asOf", s.get("asOf"));
                m.put("total", list.size());
                m.put("counts", counts);
                m.put("improving", improving);
                m.put("weakening", weakening);
                out.put("sectors", m);
            }
        } catch (Exception ex) {
            logger.warn("Home research: sector summary failed: " + ex.getMessage());
        }
        return out;
    }

    private static List<?> asList(Object o) {
        return o instanceof List ? (List<?>) o : Collections.emptyList();
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    // =====================================================================================================
    // headlines
    // =====================================================================================================

    /**
     * {india: [paper], global: [paper]}; paper = {name, home, epaper, edition: print|online, items: [{title, link, at}]},
     * papers in order of preference, each paper's latest headlines first. Headlines and links only.
     */
    public Map<String, Object> news() {
        synchronized (newsLock) {
            long now = System.currentTimeMillis();
            if (news != null && now - newsAt < NEWS_TTL_MS) return news;
            List<List<Future<List<Map<String, Object>>>>> jobs = new ArrayList<>();
            for (Paper p : PAPERS) {
                List<Future<List<Map<String, Object>>>> mine = new ArrayList<>();
                for (String url : p.feeds) mine.add(newsPool.submit(() -> feed(p.name, url)));
                jobs.add(mine);
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("india", new ArrayList<Map<String, Object>>());
            out.put("global", new ArrayList<Map<String, Object>>());
            Set<String> seen = new HashSet<>();            // the same story in two feeds or two papers: keep the first
            int total = 0;
            for (int i = 0; i < PAPERS.length; i++) {
                Paper p = PAPERS[i];
                List<Map<String, Object>> all = new ArrayList<>();
                for (Future<List<Map<String, Object>>> j : jobs.get(i)) {
                    try { all.addAll(j.get(TIMEOUT_MS * 2L, TimeUnit.MILLISECONDS)); } catch (Exception ignored) { }
                }
                all.sort(Comparator.comparingLong((Map<String, Object> m) -> (Long) m.get("at")).reversed());
                List<Map<String, Object>> items = new ArrayList<>();
                for (Map<String, Object> m : all) {
                    String key = String.valueOf(m.get("title")).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
                    if (key.length() > 60) key = key.substring(0, 60);
                    String link = String.valueOf(m.get("link")).replaceAll("[?#].*$", "");
                    if (!seen.add("t:" + key) | !seen.add("l:" + link)) continue;
                    items.add(m);
                    if (items.size() >= p.show) break;
                }
                total += items.size();
                Map<String, Object> paper = new LinkedHashMap<>();
                paper.put("name", p.name);
                paper.put("home", p.home);
                paper.put("epaper", p.epaper);
                paper.put("edition", p.print ? "print" : "online");
                paper.put("items", items);
                @SuppressWarnings("unchecked") List<Map<String, Object>> region = (List<Map<String, Object>>) out.get(p.region);
                region.add(paper);
            }
            if (total == 0 && news != null) { newsAt = now - NEWS_TTL_MS / 2; return news; }  // retry sooner
            news = out;
            newsAt = now;
            return out;
        }
    }

    private List<Map<String, Object>> feed(String source, String url) {
        List<Map<String, Object>> out = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = open(url, "application/rss+xml, application/xml, text/xml");
            if (conn.getResponseCode() != 200) {
                logger.warn("Home news: " + source + " HTTP " + conn.getResponseCode() + " " + url);
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
            long cutoff = System.currentTimeMillis() - NEWS_MAX_AGE_MS;
            for (int i = 0; i < items.getLength() && out.size() < 30; i++) {
                Element it = (Element) items.item(i);
                String title = clean(child(it, "title"));
                String link = child(it, "link");
                if (title.isEmpty() || link == null || TIPS.matcher(title).find() || NOT_STORY.matcher(title).find()) continue;
                link = link.trim();
                if (!link.startsWith("https://") && !link.startsWith("http://")) continue;
                Long at = parseDate(child(it, "pubDate"));
                if (at == null || at < cutoff) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("title", title.length() > 220 ? title.substring(0, 220) + "…" : title);
                m.put("link", link);
                m.put("at", at);
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
            "yyyy-MM-dd HH:mm:ss"};      // the last one carries no zone: UTC

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
