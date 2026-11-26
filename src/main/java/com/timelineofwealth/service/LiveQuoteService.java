package com.timelineofwealth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;

/**
 * Display-only current market price (CMP) and today's change, from Yahoo Finance's public spark endpoint
 * (NSE symbol + ".NS", BSE-only stocks as BSE code + ".BO"). Quotes are delayed and best effort:
 * every quote is cached for a couple of minutes, failures are remembered for a while, and Yahoo is never
 * called on the page's main loading path, so a slow or unavailable Yahoo cannot slow the site down.
 */
@Service
public class LiveQuoteService {
    private static final Logger logger = LoggerFactory.getLogger(LiveQuoteService.class);

    private static final String SPARK = "https://query1.finance.yahoo.com/v7/finance/spark?range=1d&interval=1d&symbols=";
    private static final int BATCH = 20;                        // Yahoo spark accepts at most 20 symbols per call
    private static final int MAX_TICKERS = 120;                 // per request to our endpoint
    private static final long TTL_MS = 2 * 60 * 1000L;          // fresh quote reused for 2 minutes
    private static final long FAIL_TTL_MS = 10 * 60 * 1000L;    // symbol Yahoo did not return: retry after 10 minutes
    private static final long BACKOFF_MS = 5 * 60 * 1000L;      // Yahoo unreachable: stop calling it for 5 minutes
    private static final long SYMBOLS_TTL_MS = 6 * 60 * 60 * 1000L;
    private static final int TIMEOUT_MS = 4000;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService pool = Executors.newFixedThreadPool(5,r -> {
        Thread t = new Thread(r, "live-quote");
        t.setDaemon(true);
        return t;
    });

    private static final class Quote {
        final Map<String, Object> data;     // null = Yahoo had no quote for the symbol
        final long fetchedAt;
        Quote(Map<String, Object> data, long fetchedAt) { this.data = data; this.fetchedAt = fetchedAt; }
        boolean fresh(long now) { return now - fetchedAt < (data == null ? FAIL_TTL_MS : TTL_MS); }
    }

    private final ConcurrentHashMap<String, Quote> cache = new ConcurrentHashMap<>();
    private volatile Map<String, String> symbols = Collections.emptyMap();   // stock_universe ticker -> Yahoo symbol
    private volatile long symbolsLoadedAt = 0;
    private volatile long backoffUntil = 0;

    public LiveQuoteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Quotes for the given stock_universe tickers: {ticker: {p: price, pc: previous close, ch: change,
     * r: change ratio (0.012 = +1.2%), t: quote time epoch seconds}}. Tickers without a quote are left out.
     */
    public Map<String, Object> getQuotes(Collection<String> tickers) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (tickers == null || tickers.isEmpty()) return out;
        Map<String, String> sym = symbols();
        Map<String, String> wanted = new LinkedHashMap<>();       // ticker -> symbol
        for (String t : tickers) {
            if (t == null) continue;
            String k = t.trim();
            if (k.isEmpty() || k.length() > 30 || wanted.containsKey(k)) continue;
            String s = sym.get(k.toUpperCase(Locale.ROOT));
            if (s == null) s = defaultSymbol(k);
            wanted.put(k, s);
            if (wanted.size() >= MAX_TICKERS) break;
        }

        long now = System.currentTimeMillis();
        List<String> stale = new ArrayList<>();
        for (String s : new LinkedHashSet<>(wanted.values())) {
            Quote q = cache.get(s);
            if (q == null || !q.fresh(now)) stale.add(s);
        }
        if (!stale.isEmpty() && now >= backoffUntil) fetch(stale);

        for (Map.Entry<String, String> e : wanted.entrySet()) {
            Quote q = cache.get(e.getValue());
            if (q != null && q.data != null) out.put(e.getKey(), q.data);
        }
        return out;
    }

    private void fetch(List<String> stale) {
        List<Future<?>> jobs = new ArrayList<>();
        for (int i = 0; i < stale.size(); i += BATCH) {
            final List<String> chunk = new ArrayList<>(stale.subList(i, Math.min(i + BATCH, stale.size())));
            jobs.add(pool.submit(() -> fetchChunk(chunk)));
        }
        // wait at most ~10 s overall; a chunk still running keeps going and fills the cache for the next call
        long deadline = System.currentTimeMillis() + TIMEOUT_MS * 5L / 2;
        for (Future<?> f : jobs) {
            long left = deadline - System.currentTimeMillis();
            if (left <= 0) break;
            try { f.get(left, TimeUnit.MILLISECONDS); } catch (Exception ignored) { }
        }
    }

    private void fetchChunk(List<String> chunk) {
        long now = System.currentTimeMillis();
        HttpURLConnection conn = null;
        try {
            StringBuilder q = new StringBuilder();
            for (String s : chunk) { if (q.length() > 0) q.append(','); q.append(URLEncoder.encode(s, "UTF-8")); }
            conn = (HttpURLConnection) new URL(SPARK + q).openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            if (code != 200) {
                logger.warn("Live quotes: Yahoo spark returned HTTP " + code);
                if (code == 429 || code >= 500) backoffUntil = now + BACKOFF_MS;
                return;
            }
            JsonNode root;
            try (InputStream in = conn.getInputStream()) { root = mapper.readTree(in); }
            Set<String> seen = new HashSet<>();
            for (JsonNode r : root.path("spark").path("result")) {
                String s = r.path("symbol").asText("");
                JsonNode meta = r.path("response").path(0).path("meta");
                double p = meta.path("regularMarketPrice").asDouble(0);
                double pc = meta.path("chartPreviousClose").asDouble(meta.path("previousClose").asDouble(0));
                if (s.isEmpty()) continue;
                seen.add(s);
                if (p <= 0 || pc <= 0) { cache.put(s, new Quote(null, now)); continue; }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("p", p);
                m.put("pc", pc);
                m.put("ch", p - pc);
                m.put("r", p / pc - 1);
                m.put("t", meta.path("regularMarketTime").asLong(0));
                cache.put(s, new Quote(Collections.unmodifiableMap(m), now));
            }
            for (String s : chunk) if (!seen.contains(s)) cache.put(s, new Quote(null, now));
        } catch (Exception ex) {
            logger.warn("Live quotes: Yahoo spark failed: " + ex.getMessage());
            backoffUntil = now + BACKOFF_MS;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** stock_universe ticker -> Yahoo symbol: NSE code + ".NS", else BSE code + ".BO". Reloaded every few hours. */
    private Map<String, String> symbols() {
        long now = System.currentTimeMillis();
        if (now - symbolsLoadedAt < SYMBOLS_TTL_MS && !symbols.isEmpty()) return symbols;
        synchronized (this) {
            if (now - symbolsLoadedAt < SYMBOLS_TTL_MS && !symbols.isEmpty()) return symbols;
            Map<String, String> m = new HashMap<>();
            try {
                jdbcTemplate.query("SELECT ticker, nse_code, bse_code FROM stock_universe", rs -> {
                    String t = rs.getString(1), nse = rs.getString(2), bse = rs.getString(3);
                    if (t == null) return;
                    String s = null;
                    if (nse != null && !nse.trim().isEmpty()) s = nse.trim().toUpperCase(Locale.ROOT) + ".NS";
                    else if (bse != null && !bse.trim().isEmpty()) s = bse.trim() + ".BO";
                    if (s != null) m.put(t.trim().toUpperCase(Locale.ROOT), s);
                });
            } catch (Exception ex) {
                logger.warn("Live quotes: could not load stock_universe symbols: " + ex.getMessage());
            }
            symbols = m;
            symbolsLoadedAt = now;
            return m;
        }
    }

    private static String defaultSymbol(String ticker) {
        String t = ticker.toUpperCase(Locale.ROOT);
        return t.matches("\\d+") ? t + ".BO" : t + ".NS";
    }
}
