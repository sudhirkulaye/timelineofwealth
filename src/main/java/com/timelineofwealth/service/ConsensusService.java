package com.timelineofwealth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Yahoo consensus views for the website.
 *
 * Weekly inputs (loaded by the YahooFinance step 4 batch file on both laptops):
 *   yahoo_consensus          raw weekly consensus (target price, EPS, revenue, analysts per fiscal year)
 *   consensus_weekly_score   split-safe revisions, composite score and the idea flags per stock and week
 *   consensus_weekly_group   revisions by watchlist group (sections 101+ of watchlists.properties)
 *   consensus_scoreboard     how the score's long and short lists did over the next four weeks
 * Daily inputs: nse_price_history / stock_universe / stock_price_movement / index_valuation, so the price
 * columns (upside to target, returns, price since the score) are as of the latest trading day.
 *
 * The public views never carry broker names: broker targets are labelled 1 (Ambit), 2 (MOSL), 3 (Axis) and 4 (PL).
 * The Focus Five view (weights, named brokers) is for the adviser pages only.
 */
@Service
public class ConsensusService {
    private static final Logger logger = LoggerFactory.getLogger(ConsensusService.class);
    private static final String[] INDICES = {"NIFTY", "NIFTY200", "BSEMidCap", "BSESmallCap"};
    private static final String[][] BROKER_NAMES = {{"1", "AMBIT"}, {"2", "MOSL"}, {"3", "AXIS"}, {"4", "PL"}};
    static final int TOP_N = 15;
    static final int FOCUS_MEMBER_ID = 1;
    static final int FOCUS_PORTFOLIO_ID = 2;
    private static final Set<String> CASH_TICKERS = new HashSet<>(Collections.singletonList("MOSL_CASH"));

    private final JdbcTemplate jdbc;
    private final AnalystViewService analystViewService;

    private volatile String cacheKey = "";
    private volatile Map<String, Map<String, Object>> latestCache = Collections.emptyMap();

    public ConsensusService(JdbcTemplate jdbc, AnalystViewService analystViewService) {
        this.jdbc = jdbc;
        this.analystViewService = analystViewService;
    }

    // =====================================================================================================
    // latest consensus per stock (stock list, idea lists, Focus Five)
    // =====================================================================================================

    /**
     * Latest consensus per ticker: {asOf, analysts, tpAvg, tpMin, tpMax, fy1, eps1Avg/Min/Max, fy2, eps2Avg/Min/Max,
     * tpRev4w, eps1Rev4w, eps2Rev4w, score, scoreRank, brokers:{"1":{reco,tp}, "2":..}}.
     * A stock whose latest snapshot is more than five weeks older than the newest week is left out.
     */
    public Map<String, Map<String, Object>> latestByTicker() {
        String key = jdbc.queryForObject(
                "SELECT CONCAT(IFNULL(MAX(date),''), '|', COUNT(*)) FROM yahoo_consensus", String.class);
        Map<String, Map<String, Object>> targets = analystViewService.getBrokerTargets();
        key = key + "|" + System.identityHashCode(targets) + "|" + jdbc.queryForObject(
                "SELECT CONCAT(IFNULL(MAX(week_date),''), '|', COUNT(*)) FROM consensus_weekly_score", String.class);
        if (tableExists("consensus_broker_view"))
            key = key + "|" + jdbc.queryForObject("SELECT CONCAT(COUNT(*), '|', IFNULL(MAX(report_date),'')) FROM consensus_broker_view", String.class);
        if (key.equals(cacheKey)) return latestCache;

        Map<String, Map<String, Object>> out = new HashMap<>();
        jdbc.query("SELECT y.ticker, y.date, y.fin_year, y.tp_avg, y.tp_min, y.tp_max, y.eps_avg, y.eps_min, y.eps_max, " +
                        "y.analyst_count FROM yahoo_consensus y " +
                        "JOIN (SELECT ticker, MAX(date) d FROM yahoo_consensus GROUP BY ticker) m " +
                        "  ON m.ticker = y.ticker AND m.d = y.date " +
                        "WHERE y.date >= (SELECT DATE_SUB(MAX(date), INTERVAL 35 DAY) FROM yahoo_consensus) " +
                        "ORDER BY y.ticker, y.fin_year",
                (ResultSet rs) -> {
                    String t = rs.getString("ticker");
                    Map<String, Object> m = out.get(t);
                    if (m == null) {
                        m = new LinkedHashMap<>();
                        m.put("asOf", str(rs.getDate("date")));
                        m.put("analysts", intOrNull(rs, "analyst_count"));
                        m.put("tpAvg", dbl(rs, "tp_avg"));
                        m.put("tpMin", dbl(rs, "tp_min"));
                        m.put("tpMax", dbl(rs, "tp_max"));
                        out.put(t, m);
                    }
                    String slot = m.containsKey("fy1") ? (m.containsKey("fy2") ? null : "2") : "1";
                    if (slot == null) return;
                    m.put("fy" + slot, rs.getString("fin_year"));
                    m.put("eps" + slot + "Avg", dbl(rs, "eps_avg"));
                    m.put("eps" + slot + "Min", dbl(rs, "eps_min"));
                    m.put("eps" + slot + "Max", dbl(rs, "eps_max"));
                });

        jdbc.query("SELECT ticker, tp_rev_4w, eps_fy1_rev_4w, eps_fy2_rev_4w, score, score_rank " +
                        "FROM consensus_weekly_score WHERE week_date = (SELECT MAX(week_date) FROM consensus_weekly_score)",
                (ResultSet rs) -> {
                    Map<String, Object> m = out.get(rs.getString("ticker"));
                    if (m == null) return;
                    m.put("tpRev4w", dbl(rs, "tp_rev_4w"));
                    m.put("eps1Rev4w", dbl(rs, "eps_fy1_rev_4w"));
                    m.put("eps2Rev4w", dbl(rs, "eps_fy2_rev_4w"));
                    m.put("score", dbl(rs, "score"));
                    m.put("scoreRank", intOrNull(rs, "score_rank"));
                });

        for (Map.Entry<String, Map<String, Map<String, Object>>> e : brokerViews().entrySet()) {
            Map<String, Object> m = out.computeIfAbsent(e.getKey(), k -> new LinkedHashMap<>());
            Map<String, Object> b = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Object>> v : e.getValue().entrySet()) {
                Map<String, Object> x = new LinkedHashMap<>();
                x.put("reco", v.getValue().get("reco"));
                x.put("tp", v.getValue().get("tp"));
                x.put("date", v.getValue().get("date"));
                b.put(v.getKey(), x);
            }
            if (!b.isEmpty()) m.put("brokers", b);
        }
        latestCache = out;
        cacheKey = key;
        return out;
    }

    // =====================================================================================================
    // public dashboard
    // =====================================================================================================

    public Map<String, Object> dashboard() {
        Map<String, Object> out = new LinkedHashMap<>();
        Date week = jdbc.queryForObject("SELECT MAX(week_date) FROM consensus_weekly_score", Date.class);
        Date priceDate = jdbc.queryForObject("SELECT MAX(date) FROM nse_price_history", Date.class);
        out.put("asOf", str(week));
        out.put("priceDate", str(priceDate));
        if (week == null) return out;

        Map<String, Map<String, Object>> latest = latestByTicker();
        Map<String, Object[]> meta = stockMeta();
        Map<String, Map<String, Double>> moves = priceMoves();
        Map<String, Map<String, Double>> idx = indexReturns();

        // ---------------- one row per stock with consensus (latest week)
        // Columns added to consensus_weekly_score later (eps_rev_4w, score_pct, z_*, ...) are read when present;
        // until the Python step writes them, close stand-ins are worked out here.
        Double nifty1m = idx.containsKey("NIFTY") ? idx.get("NIFTY").get("1M") : null;
        Double nifty3m = idx.containsKey("NIFTY") ? idx.get("NIFTY").get("3M") : null;
        List<Map<String, Object>> rows = new ArrayList<>();
        jdbc.query("SELECT * FROM consensus_weekly_score WHERE week_date = ?", (ResultSet rs) -> {
            Set<String> cols = columns(rs);
            Map<String, Object> r = new LinkedHashMap<>();
            String t = rs.getString("ticker");
            r.put("ticker", t);
            String g = rs.getString("watchlist_group");
            r.put("groupCode", g);
            r.put("sector", WatchlistNames.group(g));
            r.put("group", WatchlistNames.shortName(g));
            r.put("analysts", intOrNull(rs, "analyst_count"));
            r.put("score", dbl(rs, "score"));
            r.put("rank", intOrNull(rs, "score_rank"));
            r.put("tp4", dbl(rs, "tp_rev_4w"));
            Double eps4 = opt(rs, cols, "eps_rev_4w");
            r.put("eps4", eps4 != null || cols.contains("eps_rev_4w") ? eps4 : dbl(rs, "eps_fy1_rev_4w"));
            r.put("eps13", opt(rs, cols, "eps_rev_13w"));
            r.put("persist", intOrNull(rs, "persistence_13w"));
            r.put("mom", dbl(rs, "mom_4w"));
            r.put("mom12", dbl(rs, "mom_12_1"));
            r.put("pct", opt(rs, cols, "score_pct"));
            for (String z : new String[]{"z_eps", "z_tp", "z_rev", "z_mom12"}) r.put(z, opt(rs, cols, z));
            Double cc = opt(rs, cols, "count_chg_13w");
            r.put("countChg13", cc);
            Double age = opt(rs, cols, "consensus_age_weeks");
            r.put("age", age);
            r.put("isShort", rs.getInt("is_short") == 1);
            r.put("hasNew", cols.contains("score_pct"));
            rows.add(r);
        }, week);
        boolean fromPython = !rows.isEmpty() && Boolean.TRUE.equals(rows.get(0).get("hasNew"));
        if (!fromPython) {                       // stand-ins until the new columns are loaded
            Map<String, double[]> c13 = change13w();
            Map<String, Double> pct = pctOf(rows);
            for (Map<String, Object> r : rows) {
                double[] v = c13.get((String) r.get("ticker"));
                if (v != null) { r.put("eps13", Double.isNaN(v[0]) ? null : v[0]); r.put("countChg13", Double.isNaN(v[1]) ? null : v[1]); }
                r.put("pct", pct.get((String) r.get("ticker")));
            }
        }
        Map<String, Double> pct4w = pct4WeeksAgo(week, fromPython);
        Map<String, Map<String, Map<String, Object>>> views = brokerViews();
        for (Map<String, Object> r : rows) {
            enrich(r, latest, meta, moves);
            String t = (String) r.get("ticker");
            r.remove("hasNew");
            r.put("pct4w", pct4w.get(t));
            Double r1 = num(r.get("r1m"));
            r.put("rel1m", r1 != null && nifty1m != null ? r1 - nifty1m : null);
            r.put("industry", industryOf(r));
            r.put("brokers", brokerMix(views.get(t)));
            r.put("drivers", drivers(r, false));
            r.put("driversShort", drivers(r, true));
        }

        // ---------------- idea lists, same rules as the weekly dashboard (weekly_lib/model.idea_lists)
        List<Map<String, Object>> el = new ArrayList<>();
        for (Map<String, Object> r : rows) if (r.get("score") != null) el.add(r);
        Comparator<Map<String, Object>> byScore = Comparator.comparing((Map<String, Object> r) -> nz(r.get("score")));
        List<Map<String, Object>> longList = top(el, x -> true, byScore.reversed(), 60);
        List<Map<String, Object>> shortList = top(el, x -> Boolean.TRUE.equals(x.get("isShort")), byScore, TOP_N);
        Set<Object> shownLong = new HashSet<>(), shownShort = new HashSet<>();
        for (int i = 0; i < Math.min(TOP_N, longList.size()); i++) shownLong.add(longList.get(i).get("ticker"));
        for (Map<String, Object> r : shortList) shownShort.add(r.get("ticker"));
        out.put("nScored", el.size());
        out.put("topN", TOP_N);
        out.put("long", longList);
        out.put("short", shortList);
        out.put("divCut", top(el, x -> nz(x.get("eps4")) < -0.01 && nz(x.get("mom")) > 0.03 && x.get("eps4") != null && x.get("mom") != null,
                Comparator.comparing((Map<String, Object> x) -> nz(x.get("mom")) - nz(x.get("eps4"))).reversed(), 40));
        out.put("divRaised", top(el, x -> nz(x.get("eps4")) > 0.01 && nz(x.get("mom")) < -0.03 && x.get("eps4") != null && x.get("mom") != null,
                Comparator.comparing((Map<String, Object> x) -> nz(x.get("eps4")) - nz(x.get("mom"))).reversed(), 40));
        Map<String, Object> em = new LinkedHashMap<>();
        em.put("newTop", top(el, x -> nz(x.get("pct")) >= 80 && !(nz(x.get("pct4w")) >= 80) && !shownLong.contains(x.get("ticker")),
                Comparator.comparing((Map<String, Object> x) -> jump(x)).reversed(), 12));
        em.put("newBottom", top(el, x -> x.get("pct") != null && nz(x.get("pct")) <= 20 && !(x.get("pct4w") != null && nz(x.get("pct4w")) <= 20)
                        && !shownShort.contains(x.get("ticker")),
                Comparator.comparing((Map<String, Object> x) -> jump(x)), 12));
        em.put("coverage", top(rows, x -> nz(x.get("countChg13")) >= 3 && x.get("price") != null,
                Comparator.comparing((Map<String, Object> x) -> nz(x.get("countChg13")))
                        .thenComparing(x -> nz(x.get("analysts"))).reversed(), 12));
        em.put("persistent", top(rows, x -> nz(x.get("persist")) >= 6 && nz(x.get("analysts")) >= 5,
                Comparator.comparing((Map<String, Object> x) -> nz(x.get("persist")))
                        .thenComparing(x -> x.get("eps13") == null ? Double.NEGATIVE_INFINITY : nz(x.get("eps13"))).reversed(), 12));
        out.put("emerging", em);
        out.put("complete", fromPython);

        // ---------------- industries: medians over the scored stocks of each group, and of each sector
        Map<String, List<Map<String, Object>>> byGroup = new TreeMap<>();
        Map<String, List<Map<String, Object>>> bySector = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            if (r.get("score") == null) continue;
            String g = (String) r.get("groupCode");
            byGroup.computeIfAbsent(g == null ? WatchlistNames.OTHER : g, k -> new ArrayList<>()).add(r);
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : byGroup.entrySet()) {
            String g = e.getKey();
            Map<String, Object> m = industryStats(e.getValue(), nifty1m, nifty3m);
            m.put("code", g);
            m.put("sector", WatchlistNames.group(g));
            m.put("name", WatchlistNames.shortName(g));
            groups.add(m);
            bySector.computeIfAbsent((String) m.get("sector"), k -> new ArrayList<>()).addAll(e.getValue());
        }
        List<Map<String, Object>> sectors = new ArrayList<>();
        int order = 0;
        for (Map.Entry<String, List<Map<String, Object>>> e : bySector.entrySet()) {
            Map<String, Object> m = industryStats(e.getValue(), nifty1m, nifty3m);
            m.put("name", e.getKey());
            m.put("order", order++);
            m.put("groups", groups.stream().filter(x -> e.getKey().equals(x.get("sector"))).count());
            sectors.add(m);
        }
        out.put("groups", groups);
        out.put("sectors", sectors);

        // ---------------- market
        Map<String, Object> market = new LinkedHashMap<>();
        int scored = 0, up = 0, down = 0;
        for (Map<String, Object> r : rows) {
            if (r.get("score") == null) continue;
            scored++;
            double e = nz(r.get("eps4"));
            if (e > 0.005) up++;
            if (e < -0.005) down++;
        }
        market.put("covered", rows.size());
        market.put("scored", scored);
        market.put("up", up);
        market.put("down", down);
        List<Map<String, Object>> board = new ArrayList<>();
        jdbc.query("SELECT * FROM consensus_scoreboard ORDER BY week_date", (ResultSet rs) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("d", str(rs.getDate("week_date")));
            m.put("ic", dbl(rs, "ic_spearman"));
            m.put("long", dbl(rs, "long_fwd_ret"));
            m.put("short", dbl(rs, "short_fwd_ret"));
            m.put("spread", dbl(rs, "spread_fwd_ret"));
            m.put("hit", dbl(rs, "hit_rate"));
            m.put("up", dbl(rs, "breadth_up"));
            m.put("down", dbl(rs, "breadth_down"));
            board.add(m);
        });
        market.put("indices", indexTable(idx));
        market.put("indexChart", indexChart());
        out.put("market", market);
        out.put("scoreboard", board);
        out.put("scoreboardSummary", boardSummary(board));
        return out;
    }

    private static List<Map<String, Object>> top(List<Map<String, Object>> rows, java.util.function.Predicate<Map<String, Object>> keep,
                                                 Comparator<Map<String, Object>> order, int n) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) if (keep.test(r)) out.add(r);
        out.sort(order);
        return out.size() > n ? new ArrayList<>(out.subList(0, n)) : out;
    }

    /** score percentile gain over 4 weeks; a stock not scored 4 weeks ago counts from the middle (50) */
    private static double jump(Map<String, Object> r) {
        Double p = num(r.get("pct")), p4 = num(r.get("pct4w"));
        return (p == null ? 0 : p) - (p4 == null ? 50 : p4);
    }

    /** "Estimate upgrades + Target raised": the two largest score components above 0.25 (sign flipped for shorts). */
    static String drivers(Map<String, Object> r, boolean shortSide) {
        String[][] names = shortSide
                ? new String[][]{{"z_eps", "Estimate cuts"}, {"z_tp", "Target cut"}, {"z_rev", "Price run-up"}, {"z_mom12", "12-month downtrend"}}
                : new String[][]{{"z_eps", "Estimate upgrades"}, {"z_tp", "Target raised"}, {"z_rev", "Price pullback"}, {"z_mom12", "12-month uptrend"}};
        boolean any = false;
        List<Object[]> ranked = new ArrayList<>();
        for (String[] k : names) {
            Double z = num(r.get(k[0]));
            if (z == null) continue;
            any = true;
            double v = shortSide ? -z : z;
            if (v > 0.25) ranked.add(new Object[]{v, k[1]});
        }
        if (!any) return null;
        ranked.sort((a, b) -> Double.compare((Double) b[0], (Double) a[0]));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, ranked.size()); i++) sb.append(i > 0 ? " + " : "").append(ranked.get(i)[1]);
        return sb.length() > 0 ? sb.toString() : "Mixed";
    }

    /** Sector from the watchlist group; for stocks outside the 101+ groups, the research tracker's industry. */
    private String industryOf(Map<String, Object> r) {
        String code = (String) r.get("groupCode");
        if (WatchlistNames.code(code) >= 101) return (String) r.get("sector");
        Object ind = analystViewService.getViews((String) r.get("ticker")).get("industry");
        return ind == null || ind.toString().trim().isEmpty() ? "Unclassified" : ind.toString();
    }

    /** {n, pos, neu, neg} of the latest broker ratings (no names), or null when no broker covers the stock. */
    private static Map<String, Object> brokerMix(Map<String, Map<String, Object>> v) {
        if (v == null || v.isEmpty()) return null;
        int pos = 0, neu = 0, neg = 0, n = 0;
        for (Map<String, Object> b : v.values()) {
            Integer s = (Integer) b.get("score");
            if (s == null) continue;
            n++;
            if (s > 0) pos++; else if (s < 0) neg++; else neu++;
        }
        if (n == 0) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("n", n);
        m.put("pos", pos);
        m.put("neu", neu);
        m.put("neg", neg);
        return m;
    }

    static Integer recoScore(Object reco) {
        if (reco == null) return null;
        switch (reco.toString().trim().toUpperCase(Locale.ROOT)) {
            case "BUY": case "STRONG BUY": return 2;
            case "ADD": case "ACCUMULATE": return 1;
            case "HOLD": case "NEUTRAL": return 0;
            case "REDUCE": return -1;
            case "SELL": return -2;
            default: return null;
        }
    }

    private boolean tableExists(String name) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class, name);
        return n != null && n > 0;
    }

    /**
     * ticker -> label ("1" Ambit, "2" MOSL, "3" Axis, "4" PL) -> {reco, tp, date, score}. From consensus_broker_view
     * (AnalystsReco, loaded by the weekly Python step) when it has rows; otherwise Ambit and MOSL from the research
     * tracker.
     */
    private Map<String, Map<String, Map<String, Object>>> brokerViews() {
        Map<String, String> label = new HashMap<>();
        for (String[] b : BROKER_NAMES) label.put(b[1], b[0]);
        Map<String, Map<String, Map<String, Object>>> out = new HashMap<>();
        if (tableExists("consensus_broker_view")) {
            jdbc.query("SELECT ticker, broker, reco, target, report_date FROM consensus_broker_view", (ResultSet rs) -> {
                String lb = label.get(String.valueOf(rs.getString("broker")).trim().toUpperCase(Locale.ROOT));
                if (lb == null) return;
                Map<String, Object> v = new LinkedHashMap<>();
                v.put("reco", rs.getString("reco"));
                Double tp = dbl(rs, "target");
                v.put("tp", tp != null && tp > 0 ? tp : null);
                v.put("date", str(rs.getDate("report_date")));
                v.put("score", recoScore(v.get("reco")));
                out.computeIfAbsent(rs.getString("ticker").toUpperCase(Locale.ROOT), k -> new TreeMap<>()).put(lb, v);
            });
        }
        if (!out.isEmpty()) return out;          // AnalystsReco is loaded: one source for every page, as on the weekly dashboard
        for (Map.Entry<String, Map<String, Object>> e : analystViewService.getBrokerTargets().entrySet()) {
            for (String[] b : BROKER_NAMES) {
                @SuppressWarnings("unchecked") Map<String, Object> t = (Map<String, Object>) e.getValue().get(b[0]);
                if (t == null) continue;
                Map<String, Map<String, Object>> m = out.computeIfAbsent(e.getKey(), k -> new TreeMap<>());
                if (m.containsKey(b[0])) continue;
                Map<String, Object> v = new LinkedHashMap<>(t);
                v.put("date", e.getValue().get("date"));
                v.put("score", recoScore(t.get("reco")));
                m.put(b[0], v);
            }
        }
        return out;
    }

    private static Set<String> columns(ResultSet rs) throws SQLException {
        Set<String> s = new HashSet<>();
        java.sql.ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) s.add(md.getColumnLabel(i).toLowerCase(Locale.ROOT));
        return s;
    }

    private static Double opt(ResultSet rs, Set<String> cols, String c) throws SQLException {
        return cols.contains(c) ? dbl(rs, c) : null;
    }

    /** score percentile (0-100, 100 best) among the scored stocks, like pandas rank(pct=True) * 100 */
    private static Map<String, Double> pctOf(List<Map<String, Object>> rows) {
        List<Map<String, Object>> s = new ArrayList<>();
        for (Map<String, Object> r : rows) if (r.get("score") != null) s.add(r);
        s.sort(Comparator.comparing((Map<String, Object> r) -> nz(r.get("score"))));
        Map<String, Double> out = new HashMap<>();
        int n = s.size();
        for (int i = 0; i < n; ) {
            int j = i;
            while (j + 1 < n && nz(s.get(j + 1).get("score")) == nz(s.get(i).get("score"))) j++;
            double rank = (i + j) / 2.0 + 1;
            for (int k = i; k <= j; k++) out.put((String) s.get(k).get("ticker"), rank / n * 100);
            i = j + 1;
        }
        return out;
    }

    /** score percentile four weekly snapshots before `week` */
    private Map<String, Double> pct4WeeksAgo(Date week, boolean fromPython) {
        List<Date> weeks = jdbc.queryForList("SELECT DISTINCT week_date FROM consensus_weekly_score WHERE week_date <= ? ORDER BY week_date DESC LIMIT 5",
                Date.class, week);
        if (weeks.size() < 5) return new HashMap<>();
        Date w4 = weeks.get(4);
        List<Map<String, Object>> rows = new ArrayList<>();
        jdbc.query("SELECT * FROM consensus_weekly_score WHERE week_date = ? AND score IS NOT NULL", (ResultSet rs) -> {
            Set<String> cols = columns(rs);
            Map<String, Object> r = new HashMap<>();
            r.put("ticker", rs.getString("ticker"));
            r.put("score", dbl(rs, "score"));
            r.put("pct", opt(rs, cols, "score_pct"));
            rows.add(r);
        }, w4);
        if (fromPython) {
            Map<String, Double> out = new HashMap<>();
            for (Map<String, Object> r : rows) out.put((String) r.get("ticker"), num(r.get("pct")));
            if (!out.values().stream().allMatch(Objects::isNull)) return out;
        }
        return pctOf(rows);
    }

    /** name, market cap, today's price, upside to the consensus target, returns and price since the score. */
    private void enrich(Map<String, Object> r, Map<String, Map<String, Object>> latest, Map<String, Object[]> meta,
                        Map<String, Map<String, Double>> moves) {
        String t = (String) r.get("ticker");
        Object[] m = meta.get(t);
        Double price = null;
        if (m != null) {
            r.put("name", m[0]);
            r.put("mcap", m[1]);
            r.put("fno", m[3]);
            price = (Double) m[2];
        }
        r.put("price", price);
        Double at = (Double) r.get("priceAtScore");
        r.put("sinceScore", price != null && at != null && at > 0 ? price / at - 1 : null);
        Map<String, Object> c = latest.get(t);
        Double tp = c == null ? null : (Double) c.get("tpAvg");
        r.put("tpAvg", tp);
        r.put("upside", price != null && tp != null && price > 0 ? tp / price - 1 : null);
        Map<String, Double> mv = moves.get(t);
        r.put("r1d", mv == null ? null : mv.get("1D"));
        r.put("r1w", mv == null ? null : mv.get("1W"));
        r.put("r1m", mv == null ? null : mv.get("1M"));
        r.put("r3m", mv == null ? null : mv.get("3M"));
    }

    /**
     * Industry columns over a set of scored stocks: n, net EPS upgrades (share with EPS 4W above +0.5% minus share
     * below -0.5%), median EPS 4W, EPS 13W, TP 4W, and median 1M / 3M return relative to NIFTY.
     */
    private static Map<String, Object> industryStats(List<Map<String, Object>> rs, Double nifty1m, Double nifty3m) {
        Map<String, Object> m = new LinkedHashMap<>();
        int up = 0, down = 0;
        List<Double> e4 = new ArrayList<>(), e13 = new ArrayList<>(), tp = new ArrayList<>(), rel1 = new ArrayList<>(), rel3 = new ArrayList<>();
        for (Map<String, Object> r : rs) {
            Double e = num(r.get("eps4"));
            if (e != null) { e4.add(e); if (e > 0.005) up++; if (e < -0.005) down++; }
            addIf(e13, num(r.get("eps13")));
            addIf(tp, num(r.get("tp4")));
            Double r1 = num(r.get("r1m")), r3 = num(r.get("r3m"));
            if (r1 != null && nifty1m != null) rel1.add(r1 - nifty1m);
            if (r3 != null && nifty3m != null) rel3.add(r3 - nifty3m);
        }
        m.put("n", e4.size());
        m.put("up", up);
        m.put("down", down);
        m.put("net", e4.isEmpty() ? null : (double) (up - down) / e4.size());
        m.put("eps4w", median(e4));
        m.put("eps13w", median(e13));
        m.put("tp4w", median(tp));
        m.put("rel1m", median(rel1));
        m.put("rel3m", median(rel3));
        return m;
    }

    private static void addIf(List<Double> xs, Double v) { if (v != null) xs.add(v); }

    private static Double median(List<Double> xs) {
        if (xs.isEmpty()) return null;
        List<Double> s = new ArrayList<>(xs);
        Collections.sort(s);
        int n = s.size();
        return n % 2 == 1 ? s.get(n / 2) : (s.get(n / 2 - 1) + s.get(n / 2)) / 2;
    }

    /**
     * Stand-in until consensus_weekly_score has eps_rev_13w / count_chg_13w: ticker -> {13-week change in consensus
     * average EPS (both fiscal years shown, same year at both ends, averaged), change in analyst count}.
     */
    private Map<String, double[]> change13w() {
        Map<String, double[]> out = new HashMap<>();
        Date last = jdbc.queryForObject("SELECT MAX(date) FROM yahoo_consensus", Date.class);
        if (last == null) return out;
        Date base = jdbc.queryForObject("SELECT MAX(date) FROM yahoo_consensus WHERE date <= DATE_SUB(?, INTERVAL 88 DAY)", Date.class, last);
        if (base == null) return out;
        Map<String, TreeMap<String, Double>> now = new HashMap<>(), then = new HashMap<>();
        Map<String, Integer> cNow = new HashMap<>(), cThen = new HashMap<>();
        jdbc.query("SELECT date, ticker, fin_year, eps_avg, analyst_count FROM yahoo_consensus WHERE date IN (?, ?)",
                (ResultSet rs) -> {
                    boolean isNow = rs.getDate(1).toString().equals(last.toString());
                    String t = rs.getString(2);
                    int c = rs.getInt(5);
                    if (!rs.wasNull()) (isNow ? cNow : cThen).put(t, c);
                    double v = rs.getDouble(4);
                    if (!rs.wasNull()) (isNow ? now : then).computeIfAbsent(t, k -> new TreeMap<>()).put(rs.getString(3), v);
                }, last, base);
        Set<String> all = new HashSet<>(now.keySet());
        all.addAll(cNow.keySet());
        for (String t : all) {
            TreeMap<String, Double> a = now.get(t), b = then.get(t);
            double sum = 0;
            int k = 0;
            if (a != null && b != null) {
                for (Map.Entry<String, Double> e : a.entrySet()) {
                    Double v0 = b.get(e.getKey());
                    if (v0 != null && Math.abs(v0) > 0.01) { sum += (e.getValue() - v0) / Math.abs(v0); k++; }
                }
            }
            Integer c1 = cNow.get(t), c0 = cThen.get(t);
            out.put(t, new double[]{k == 0 ? Double.NaN : sum / k, c1 != null && c0 != null ? c1 - c0 : Double.NaN});
        }
        return out;
    }

    /** {dates, series:{NIFTY:[cumulative return], ...}} over the last 12 months, on NIFTY's trading days. */
    private Map<String, Object> indexChart() {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, TreeMap<LocalDate, Double>> all = new LinkedHashMap<>();
        for (String ix : INDICES) all.put(ix, indexSeries(ix, LocalDate.now().minusYears(1).minusDays(7)));
        TreeMap<LocalDate, Double> nifty = all.get("NIFTY");
        if (nifty == null || nifty.isEmpty()) return out;
        LocalDate start = nifty.lastKey().minusYears(1);
        List<String> dates = new ArrayList<>();
        Map<String, List<Double>> series = new LinkedHashMap<>();
        Map<String, Double> base = new HashMap<>();
        for (Map.Entry<String, TreeMap<LocalDate, Double>> s : all.entrySet()) {
            Map.Entry<LocalDate, Double> b = s.getValue().floorEntry(start);
            if (b == null) b = s.getValue().ceilingEntry(start);
            if (b != null) { base.put(s.getKey(), b.getValue()); series.put(s.getKey(), new ArrayList<>()); }
        }
        for (LocalDate d : nifty.tailMap(start, true).keySet()) {
            dates.add(d.toString());
            for (Map.Entry<String, List<Double>> s : series.entrySet()) {
                Map.Entry<LocalDate, Double> v = all.get(s.getKey()).floorEntry(d);
                s.getValue().add(v == null ? null : v.getValue() / base.get(s.getKey()) - 1);
            }
        }
        out.put("start", start.toString());
        out.put("dates", dates);
        out.put("series", series);
        return out;
    }

    private Map<String, Object> boardSummary(List<Map<String, Object>> board) {
        int n = 0, pos = 0;
        double ic = 0, spread = 0, hit = 0;
        int nSpread = 0, nHit = 0;
        for (Map<String, Object> b : board) {
            Double v = (Double) b.get("ic");
            if (v != null) { n++; ic += v; if (v > 0) pos++; }
            Double s = (Double) b.get("spread");
            if (s != null) { nSpread++; spread += s; }
            Double h = (Double) b.get("hit");
            if (h != null) { nHit++; hit += h; }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("weeks", n);
        m.put("avgIc", n == 0 ? null : ic / n);
        m.put("positiveIc", n == 0 ? null : (double) pos / n);
        m.put("avgSpread", nSpread == 0 ? null : spread / nSpread);
        m.put("avgHit", nHit == 0 ? null : hit / nHit);
        return m;
    }

    // =====================================================================================================
    // Focus Five (adviser only)
    // =====================================================================================================

    public Map<String, Object> focusFive() {
        Map<String, Object> out = new LinkedHashMap<>();
        Date priceDate = jdbc.queryForObject("SELECT MAX(date) FROM nse_price_history", Date.class);
        out.put("priceDate", str(priceDate));
        out.put("asOf", str(jdbc.queryForObject("SELECT MAX(week_date) FROM consensus_weekly_score", Date.class)));

        // ---------------- holdings, aggregated by ticker
        Double portfolioValue = jdbc.query("SELECT market_value FROM portfolio WHERE memberid = ? AND portfolioid = ?",
                rs -> rs.next() ? dbl(rs, "market_value") : null, FOCUS_MEMBER_ID, FOCUS_PORTFOLIO_ID);
        out.put("portfolioValue", portfolioValue);
        Map<String, Map<String, Object>> hold = new LinkedHashMap<>();
        jdbc.query("SELECT ticker, short_name, SUM(quantity) q, SUM(total_cost) cost, SUM(market_value) mv, MIN(buy_date) first_buy " +
                        "FROM portfolio_holdings WHERE memberid = ? AND portfolioid = ? GROUP BY ticker, short_name ORDER BY SUM(market_value) DESC",
                (ResultSet rs) -> {
                    Map<String, Object> h = new LinkedHashMap<>();
                    String t = rs.getString("ticker");
                    h.put("ticker", t);
                    h.put("name", rs.getString("short_name"));
                    h.put("quantity", dbl(rs, "q"));
                    h.put("cost", dbl(rs, "cost"));
                    h.put("value", dbl(rs, "mv"));
                    h.put("firstBuy", str(rs.getDate("first_buy")));
                    h.put("cash", CASH_TICKERS.contains(t));
                    hold.put(t, h);
                }, FOCUS_MEMBER_ID, FOCUS_PORTFOLIO_ID);
        double total = 0;
        for (Map<String, Object> h : hold.values()) total += nz(h.get("value"));
        double denom = portfolioValue != null && portfolioValue > 0 ? portfolioValue : total;

        Map<String, Map<String, Object>> latest = latestByTicker();
        Map<String, Map<String, Double>> moves = priceMoves();
        Map<String, Object[]> meta = stockMeta();
        Map<String, Map<String, Map<String, Object>>> views = brokerViews();
        List<Map<String, Object>> holdings = new ArrayList<>();
        for (Map<String, Object> h : hold.values()) {
            String t = (String) h.get("ticker");
            Double v = (Double) h.get("value"), cost = (Double) h.get("cost");
            h.put("weight", denom > 0 && v != null ? v / denom : null);
            h.put("gain", v != null && cost != null && cost > 0 ? v / cost - 1 : null);
            if (!Boolean.TRUE.equals(h.get("cash"))) {
                Object[] m = meta.get(t);
                Double price = m == null ? null : (Double) m[2];
                h.put("price", price);
                Map<String, Double> mv = moves.get(t);
                for (String w : new String[]{"1D", "1W", "1M", "3M", "6M", "YTD", "1Y"}) h.put("r" + w, mv == null ? null : mv.get(w));
                Map<String, Object> c = latest.get(t);
                if (c != null) {
                    Map<String, Object> cc = new LinkedHashMap<>(c);
                    cc.remove("brokers");
                    Double tp = (Double) c.get("tpAvg");
                    cc.put("upside", tp != null && price != null && price > 0 ? tp / price - 1 : null);
                    h.put("consensus", cc);
                }
                Map<String, Map<String, Object>> bt = views.get(t.toUpperCase(Locale.ROOT));
                if (bt != null) {
                    Map<String, Object> named = new LinkedHashMap<>();
                    for (String[] lb : BROKER_NAMES) {
                        Map<String, Object> b = bt.get(lb[0]);
                        if (b == null) continue;
                        Map<String, Object> x = new LinkedHashMap<>(b);
                        Double tp = (Double) b.get("tp");
                        x.put("upside", tp != null && price != null && price > 0 ? tp / price - 1 : null);
                        named.put(lb[1], x);
                    }
                    h.put("brokers", named);
                }
            }
            holdings.add(h);
        }
        out.put("holdings", holdings);

        // ---------------- daily performance vs indices
        TreeMap<LocalDate, Double> nav = portfolioIndex();
        out.put("performance", performance(nav));
        Map<String, Object> twrr = jdbc.query("SELECT * FROM portfolio_twrr_summary WHERE memberid = ? AND portfolioid = ?",
                rs -> {
                    if (!rs.next()) return null;
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("date", str(rs.getDate("returns_date")));
                    m.put("MTD", dbl(rs, "returns_twrr_since_current_month"));
                    m.put("FYTD", dbl(rs, "returns_twrr_since_fin_year"));
                    m.put("3Y", dbl(rs, "returns_twrr_three_year"));
                    m.put("5Y", dbl(rs, "returns_twrr_five_year"));
                    m.put("inception", dbl(rs, "returns_twrr_since_inception"));
                    return m;
                }, FOCUS_MEMBER_ID, FOCUS_PORTFOLIO_ID);
        out.put("twrr", twrr);
        return out;
    }

    /** Daily unit value of the Focus Five model portfolio, cash flows taken out (a flow is assumed at the day's close). */
    private TreeMap<LocalDate, Double> portfolioIndex() {
        Map<LocalDate, Double> flows = new HashMap<>();
        jdbc.query("SELECT date, cashflow FROM portfolio_cashflow WHERE memberid = ? AND portfolioid = ?",
                (ResultSet rs) -> { flows.merge(rs.getDate(1).toLocalDate(), -rs.getDouble(2), Double::sum); },
                FOCUS_MEMBER_ID, FOCUS_PORTFOLIO_ID);
        TreeMap<LocalDate, Double> out = new TreeMap<>();
        double[] state = {Double.NaN, Double.NaN};   // previous value, unit value
        jdbc.query("SELECT date, value FROM portfolio_value_history WHERE memberid = ? AND portfolioid = ? AND value > 0 ORDER BY date",
                (ResultSet rs) -> {
                    LocalDate d = rs.getDate(1).toLocalDate();
                    double v = rs.getDouble(2);
                    if (Double.isNaN(state[0])) {
                        state[1] = 100;
                    } else {
                        // cashflow rows store money put in as negative numbers; flows map holds money in as positive
                        double inflow = flows.getOrDefault(d, 0.0);
                        if (state[0] > 0) state[1] *= (v - inflow) / state[0];
                    }
                    state[0] = v;
                    out.put(d, state[1]);
                }, FOCUS_MEMBER_ID, FOCUS_PORTFOLIO_ID);
        return out;
    }

    /** {dates, series:{FocusFive:[], NIFTY:[], ...} (unit values on common dates, last 3 years), returns:[{name, 1D, 1W, ...}]} */
    private Map<String, Object> performance(TreeMap<LocalDate, Double> nav) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (nav.isEmpty()) return out;
        LocalDate end = nav.lastKey();
        LocalDate from = end.minusYears(3);
        Map<String, TreeMap<LocalDate, Double>> all = new LinkedHashMap<>();
        all.put("Focus Five", nav);
        for (String ix : INDICES) all.put(ix, indexSeries(ix, nav.firstKey()));

        List<String> dates = new ArrayList<>();
        Map<String, List<Double>> series = new LinkedHashMap<>();
        for (String k : all.keySet()) series.put(k, new ArrayList<>());
        for (Map.Entry<LocalDate, Double> e : nav.tailMap(from, true).entrySet()) {
            dates.add(e.getKey().toString());
            for (Map.Entry<String, TreeMap<LocalDate, Double>> s : all.entrySet()) {
                Map.Entry<LocalDate, Double> v = s.getValue().floorEntry(e.getKey());
                series.get(s.getKey()).add(v == null ? null : v.getValue());
            }
        }
        out.put("dates", dates);
        out.put("series", series);

        String[] windows = {"1D", "1W", "1M", "3M", "6M", "YTD", "1Y", "3Y", "Inception"};
        List<Map<String, Object>> table = new ArrayList<>();
        for (Map.Entry<String, TreeMap<LocalDate, Double>> s : all.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", s.getKey());
            for (String w : windows) row.put(w, windowReturn(s.getValue(), end, w, nav.firstKey()));
            table.add(row);
        }
        out.put("returns", table);
        out.put("windows", windows);
        out.put("end", end.toString());
        return out;
    }

    private static Double windowReturn(TreeMap<LocalDate, Double> s, LocalDate end, String w, LocalDate inception) {
        Map.Entry<LocalDate, Double> e = s.floorEntry(end);
        if (e == null) return null;
        LocalDate start;
        switch (w) {
            case "1D": { Map.Entry<LocalDate, Double> p = s.lowerEntry(e.getKey()); return p == null ? null : e.getValue() / p.getValue() - 1; }
            case "1W": start = end.minusWeeks(1); break;
            case "1M": start = end.minusMonths(1); break;
            case "3M": start = end.minusMonths(3); break;
            case "6M": start = end.minusMonths(6); break;
            case "YTD": start = LocalDate.of(end.getYear() - 1, 12, 31); break;
            case "1Y": start = end.minusYears(1); break;
            case "3Y": start = end.minusYears(3); break;
            default: start = inception;
        }
        Map.Entry<LocalDate, Double> b = s.floorEntry(start);
        if (b == null) b = s.ceilingEntry(start);
        if (b == null || b.getValue() == null || b.getValue() == 0) return null;
        double r = e.getValue() / b.getValue() - 1;
        long days = java.time.temporal.ChronoUnit.DAYS.between(b.getKey(), e.getKey());
        if (("3Y".equals(w) || "Inception".equals(w)) && days > 366) r = Math.pow(1 + r, 365.25 / days) - 1;   // annualised
        return r;
    }

    // =====================================================================================================
    // shared lookups
    // =====================================================================================================

    /** ticker -> {short name, market cap, latest price, is F&O} from stock_universe. */
    private Map<String, Object[]> stockMeta() {
        Map<String, Object[]> out = new HashMap<>();
        jdbc.query("SELECT ticker, short_name, marketcap, latest_price, is_fno FROM stock_universe", (ResultSet rs) -> {
            out.put(rs.getString(1), new Object[]{rs.getString(2), dbl(rs, "marketcap"), dbl(rs, "latest_price"), rs.getInt(5) == 1});
        });
        return out;
    }

    /** ticker -> daily returns (as fractions) from stock_price_movement, which stores percentages. */
    private Map<String, Map<String, Double>> priceMoves() {
        Map<String, Map<String, Double>> out = new HashMap<>();
        jdbc.query("SELECT ticker, return_1d, return_1w, return_1m, return_3m, return_6m, return_ytd, return_1y FROM stock_price_movement",
                (ResultSet rs) -> {
                    Map<String, Double> m = new HashMap<>();
                    String[] keys = {"1D", "1W", "1M", "3M", "6M", "YTD", "1Y"};
                    for (int i = 0; i < keys.length; i++) {
                        Double v = dbl(rs, i + 2);
                        m.put(keys[i], v == null ? null : v / 100.0);
                    }
                    out.put(rs.getString(1), m);
                });
        return out;
    }

    private TreeMap<LocalDate, Double> indexSeries(String ticker, LocalDate from) {
        TreeMap<LocalDate, Double> s = new TreeMap<>();
        jdbc.query("SELECT date, value FROM index_valuation WHERE ticker = ? AND date >= ? AND value > 0 ORDER BY date",
                (ResultSet rs) -> { s.put(rs.getDate(1).toLocalDate(), rs.getDouble(2)); }, ticker, Date.valueOf(from.minusDays(10)));
        return s;
    }

    /** index -> {1D, 1W, 1M, 3M, YTD, 1Y} as of each index's latest level. */
    private Map<String, Map<String, Double>> indexReturns() {
        Map<String, Map<String, Double>> out = new LinkedHashMap<>();
        LocalDate from = LocalDate.now().minusYears(1).minusMonths(1);
        for (String ix : INDICES) {
            TreeMap<LocalDate, Double> s = indexSeries(ix, from);
            if (s.isEmpty()) continue;
            Map<String, Double> m = new LinkedHashMap<>();
            for (String w : new String[]{"1D", "1W", "1M", "3M", "YTD", "1Y"}) m.put(w, windowReturn(s, s.lastKey(), w, s.firstKey()));
            out.put(ix, m);
        }
        return out;
    }

    private static List<Map<String, Object>> indexTable(Map<String, Map<String, Double>> idx) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> e : idx.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", e.getKey());
            m.putAll(e.getValue());
            out.add(m);
        }
        return out;
    }

    // ---------------- small helpers
    private static String str(Date d) { return d == null ? null : d.toString(); }
    private static Double dbl(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }
    private static Double dbl(ResultSet rs, int col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }
    private static Integer intOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
    private static Double num(Object o) { return o instanceof Number ? ((Number) o).doubleValue() : null; }
    private static double nz(Object o) { Double d = num(o); return d == null ? 0 : d; }
    /** unranked stocks sort after every ranked one */
    private static double nzRank(Object o) { Double d = num(o); return d == null ? Double.MAX_VALUE : d; }
}
