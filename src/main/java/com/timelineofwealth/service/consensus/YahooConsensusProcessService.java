package com.timelineofwealth.service.consensus;

import com.timelineofwealth.service.ConfigFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

import static com.timelineofwealth.service.consensus.YcMath.NaN;
import static com.timelineofwealth.service.consensus.YcMath.ok;

/**
 * Weekly Yahoo consensus processing (Admin > Process Yahoo Consensus): the Java port of the Python step 4
 * (weekly_lib.consensus_db.load + weekly_lib.dashboard_db.load).
 *
 * Inputs: the WeeklyConsensus folder (yyyymmdd_YahooConsensus.csv files and YahooConsensusHistory.csv, written by
 * 1_Download_YahooConsensus.bat), nse_price_history / index_valuation / stock_split_probability / stock_universe,
 * 11Property\website\watchlists.properties and the AnalystsReco sheet of AnalystViewTracker.xlsx.
 * Outputs (one transaction): yahoo_consensus, consensus_weekly_score, consensus_weekly_group, consensus_scoreboard,
 * consensus_broker_view.
 *
 * "Compare only" computes everything and reports every difference with what is in the database, writing nothing.
 */
@Service
public class YahooConsensusProcessService {
    private static final Logger logger = LoggerFactory.getLogger(YahooConsensusProcessService.class);
    static final int MIN_ANALYSTS = 5, SCORE_WEEKS = 4, FORWARD_WEEKS = 4, TOP_N = 15;
    static final LocalDate PRICE_HISTORY_START = LocalDate.of(2025, 10, 1);
    static final String MAIN_INDEX = "NIFTY", OTHER = "Other";

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public YahooConsensusProcessService(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    // =====================================================================================================
    // table layouts
    // =====================================================================================================
    enum T { INT, DEC, STR, DATE }

    static final class Col {
        final String name; final T type; final int scale;
        Col(String name, T type, int scale) { this.name = name; this.type = type; this.scale = scale; }
    }

    static Col i(String n) { return new Col(n, T.INT, 0); }
    static Col d(String n, int s) { return new Col(n, T.DEC, s); }
    static Col s(String n) { return new Col(n, T.STR, 0); }
    static Col dt(String n) { return new Col(n, T.DATE, 0); }

    static final class Table {
        final String name; final List<Col> keys; final List<Col> cols;
        Table(String name, List<Col> keys, List<Col> cols) { this.name = name; this.keys = keys; this.cols = cols; }
        List<Col> all() { List<Col> a = new ArrayList<>(keys); a.addAll(cols); return a; }
    }

    static final Table YAHOO = new Table("yahoo_consensus", Arrays.asList(s("ticker"), dt("date"), s("fin_year")),
            Arrays.asList(s("yahoo_ticker"), d("tp_avg", 3), d("tp_min", 3), d("tp_max", 3), d("rev_avg", 3), d("rev_min", 3),
                    d("rev_max", 3), d("eps_avg", 3), d("eps_min", 3), d("eps_max", 3), i("analyst_count"), d("year_ago_rev", 3),
                    d("year_ago_eps", 3), d("current_price", 3)));
    static final Table SCORE = new Table("consensus_weekly_score", Arrays.asList(dt("week_date"), s("ticker")),
            Arrays.asList(i("analyst_count"), s("watchlist_group"), d("tp_rev_1w", 6), d("tp_rev_4w", 6), d("eps_fy1_rev_4w", 6),
                    d("eps_fy2_rev_4w", 6), d("rev_fy1_rev_4w", 6), i("persistence_13w"), d("mom_4w", 6), d("mom_12_1", 6),
                    d("score", 6), i("score_rank"), i("is_long"), i("is_short"), i("is_divergence"), i("is_emerging"),
                    d("price_at_score", 3), d("eps_rev_4w", 6), d("eps_rev_13w", 6), d("score_pct", 4), d("z_eps", 6),
                    d("z_tp", 6), d("z_rev", 6), d("z_mom12", 6), i("count_chg_13w"), i("consensus_age_weeks")));
    static final Table GROUP = new Table("consensus_weekly_group", Arrays.asList(dt("week_date"), s("watchlist_group")),
            Arrays.asList(i("stock_count"), d("avg_eps_rev_4w", 6), i("up_count"), i("down_count"), d("avg_return_1m", 6),
                    d("rel_return_1m_vs_nifty", 6)));
    static final Table BOARD = new Table("consensus_scoreboard", Collections.singletonList(dt("week_date")),
            Arrays.asList(d("ic_spearman", 6), d("long_fwd_ret", 6), d("short_fwd_ret", 6), d("spread_fwd_ret", 6),
                    d("hit_rate", 6), d("breadth_up", 6), d("breadth_down", 6)));
    static final Table BROKER = new Table("consensus_broker_view", Arrays.asList(s("ticker"), s("broker")),
            Arrays.asList(s("reco"), d("target", 3), dt("report_date"), s("quarter"), s("fwd_multiple")));

    // =====================================================================================================
    // result of one run
    // =====================================================================================================
    public static final class Result {
        public final List<String> steps = new ArrayList<>();
        public final List<String> notes = new ArrayList<>();
        public final Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        public final List<Map<String, Object>> comparison = new ArrayList<>();
        public boolean brokerViewsRead;
        public String error;
        public boolean written;
        public LocalDate firstWeek, lastWeek;
        public int weeks;

        void step(String s) { steps.add(s); logger.info("Yahoo consensus: {}", s); }
    }

    /** compareOnly = true: compute and compare with the database; false: compute and replace the tables. */
    public Result run(boolean compareOnly) {
        Result res = new Result();
        long t0 = System.currentTimeMillis();
        try {
            compute(res);
            res.step("Computed in " + (System.currentTimeMillis() - t0) / 1000 + " s");
            compare(res);
            if (!compareOnly) write(res);
        } catch (Exception e) {
            logger.error("Yahoo consensus processing failed", e);
            res.error = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        return res;
    }

    // =====================================================================================================
    // compute (model.load_core + dashboard_db.week_rows + consensus_db + broker_rows)
    // =====================================================================================================
    void compute(Result res) throws Exception {
        LocalDate today = LocalDate.now();
        YcInputs in = new YcInputs(jdbc);
        File folder = new File(ConfigFiles.yahooConsensusFolder());

        // ---- consensus snapshots and prices
        List<YcConsensus.Row> rows = YcInputs.weeklyConsensus(folder, res.notes);
        YcConsensus cons = new YcConsensus(rows);
        res.step("Read " + cons.dates.size() + " weekly consensus files (" + cons.dates.get(0) + " to "
                + cons.dates.get(cons.dates.size() - 1) + "), " + cons.tickers.length + " tickers");
        Set<String> universe = new TreeSet<>(Arrays.asList(cons.tickers));
        Map<String, String> renames = in.tickerRenames();
        LocalDate first = cons.dates.get(0).minusMonths(14);
        LocalDate priceStart = first.isBefore(PRICE_HISTORY_START) ? first : PRICE_HISTORY_START;
        Map<String, String> renamedFrom = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : renames.entrySet())
            if (universe.contains(e.getValue()) && !universe.contains(e.getKey())) renamedFrom.put(e.getValue(), e.getKey());
        Set<String> withOld = new TreeSet<>(universe);
        withOld.addAll(renamedFrom.values());
        YcPrices prices = in.prices(withOld, priceStart);
        joinRenamed(prices, renamedFrom, res.notes);
        List<YcConsensus.Event> events = in.splitEvents(withOld, priceStart);
        Map<String, String> oldToNew = new HashMap<>();
        for (Map.Entry<String, String> e : renamedFrom.entrySet()) oldToNew.put(e.getValue(), e.getKey());
        for (YcConsensus.Event ev : events) ev.ticker = oldToNew.getOrDefault(ev.ticker, ev.ticker);
        adjustUnprocessedSplits(prices, events, res.notes);
        Map<String, List<LocalDate>> priceBreaks = priceBreaks(events);
        prices.fill();
        YcPrices nifty = in.indexLevels(MAIN_INDEX, priceStart);
        res.step("Prices from " + priceStart + " to " + prices.lastDate() + " for " + prices.raw.size() + " stocks; "
                + events.size() + " split / corporate-action rows");

        cons.applyCorporateActions(events);
        cons.flagSuspectJumps();
        res.notes.addAll(cons.notes);

        // ---- weekly scores
        Map<Integer, Score> scores = new LinkedHashMap<>();
        for (int t = 0; t < cons.dates.size(); t++) {
            Score sc = score(cons, prices, priceBreaks, t);
            if (sc != null) scores.put(t, sc);
        }
        Map<String, String> groups = YcInputs.watchlistGroups(new File(ConfigFiles.watchlists()));
        if (groups.isEmpty()) res.notes.add("watchlists.properties not found or has no 101+ sections: every stock goes to 'Other'");
        Set<String> fno = in.fnoTickers(universe);
        Map<String, String> mapping = in.tickerMap(universe);

        List<Map<String, Object>> scoreRows = new ArrayList<>(), groupRows = new ArrayList<>(), boardRows = new ArrayList<>();
        for (int t : scores.keySet()) weekRows(cons, prices, nifty, scores, t, fno, groups, mapping, scoreRows, groupRows, boardRows);
        res.weeks = scores.size();
        List<Integer> ws = new ArrayList<>(scores.keySet());
        res.firstWeek = cons.dates.get(ws.get(0));
        res.lastWeek = cons.dates.get(ws.get(ws.size() - 1));
        res.step("Scored " + res.weeks + " weeks (" + res.firstWeek + " to " + res.lastWeek + "): " + scoreRows.size()
                + " stock rows, " + groupRows.size() + " group rows, " + boardRows.size() + " scoreboard rows");

        // ---- yahoo_consensus (the whole history file)
        res.tables.put(YAHOO.name, yahooRows(in, new File(folder, "YahooConsensusHistory.csv"), res));
        res.tables.put(SCORE.name, scoreRows);
        res.tables.put(GROUP.name, groupRows);
        res.tables.put(BOARD.name, boardRows);

        // ---- broker views (AnalystViewTracker.xlsx)
        try {
            YcBrokerViews bv = new YcBrokerViews();
            List<YcBrokerViews.Reco> recos = bv.read(new File(ConfigFiles.analystViewTracker()), today);
            Set<String> tks = new TreeSet<>();
            for (YcBrokerViews.Reco r : recos) tks.add(r.ticker);
            List<YcBrokerViews.View> views = YcBrokerViews.latest(recos, today, in.tickerMap(tks));
            List<Map<String, Object>> out = new ArrayList<>();
            for (YcBrokerViews.View v : views) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("ticker", v.ticker); m.put("broker", v.broker); m.put("reco", v.reco);
                m.put("target", v.target); m.put("report_date", v.reportDate); m.put("quarter", v.quarter);
                m.put("fwd_multiple", v.fwdMultiple);
                out.add(m);
            }
            res.tables.put(BROKER.name, out);
            res.brokerViewsRead = true;
            res.step("AnalystsReco: " + bv.stats.get("rows") + " rows, " + bv.stats.get("verified") + " verified (yellow), "
                    + bv.stats.get("datesRepaired") + " report dates repaired; " + out.size() + " latest broker views");
        } catch (Exception e) {
            res.notes.add("consensus_broker_view not updated: could not read " + ConfigFiles.analystViewTracker() + " (" + e.getMessage() + ")");
        }
    }

    // ---------------------------------------------------- prices (calc.join_renamed, adjust_unprocessed_splits, price_breaks)
    static void joinRenamed(YcPrices p, Map<String, String> renamedFrom, List<String> notes) {
        List<String> joined = new ArrayList<>();
        for (Map.Entry<String, String> e : renamedFrom.entrySet()) {
            String nw = e.getKey(), old = e.getValue();
            double[] sOld = p.raw.get(old);
            if (sOld == null) continue;
            double[] sNew = p.raw.get(nw);
            int first = -1;
            if (sNew != null) for (int i = 0; i < sNew.length; i++) if (ok(sNew[i])) { first = i; break; }
            if (sNew == null || first < 0) {
                p.raw.put(nw, sOld.clone());
                notes.add(nw + ": no prices under the new symbol yet; using the old symbol " + old);
                continue;
            }
            double before = NaN;
            for (int i = first - 1; i >= 0; i--) if (ok(sOld[i])) { before = sOld[i]; break; }
            if (!ok(before)) continue;
            double jump = sNew[first] / before - 1;
            if (Math.abs(jump) > 0.25) {
                notes.add(nw + ": price history not joined with the old symbol " + old + " (" + Math.round(jump * 100) + "% jump at the rename)");
                continue;
            }
            for (int i = 0; i < first; i++) sNew[i] = sOld[i];
            joined.add(nw + " from " + old + " before " + p.dates.get(first));
        }
        for (String old : renamedFrom.values()) p.raw.remove(old);
        if (!joined.isEmpty()) notes.add("Renamed stocks: earlier prices taken from the old symbol: " + String.join("; ", joined));
    }

    static boolean pending(YcConsensus.Event ev) { return !"YES".equals(ev.isProcessed.toUpperCase(Locale.ROOT)); }

    /** `prev / close if close else nan`: NaN when the close is 0 */
    static double ratio(YcConsensus.Event ev) { return ev.close == 0 ? NaN : ev.previousClose / ev.close; }

    static void adjustUnprocessedSplits(YcPrices p, List<YcConsensus.Event> events, List<String> notes) {
        for (YcConsensus.Event ev : events) {
            if (!pending(ev)) continue;
            double[] s = p.raw.get(ev.ticker);
            if (s == null || ev.close == 0) continue;
            double before = NaN, after = NaN;
            for (int i = 0; i < s.length; i++) {
                LocalDate d = p.dates.get(i);
                if (!d.isAfter(ev.date.minusDays(1)) && ok(s[i])) before = s[i];
                if (!d.isBefore(ev.date) && ok(s[i])) { after = s[i]; break; }
            }
            if (!ok(before) || !ok(after)) continue;
            Double factor = YcMath.snapRatio(ratio(ev), 0.08);
            if (factor == null) {
                notes.add(ev.ticker + ": move on " + ev.date + " is pending in stock_split_probability but is not a clean split ratio; prices left unchanged");
                continue;
            }
            if (Math.abs(after / before * factor - 1) > 0.12) continue;         // history already adjusted
            for (int i = 0; i < s.length && p.dates.get(i).isBefore(ev.date); i++) s[i] = s[i] / factor;
            notes.add(ev.ticker + ": pending split on " + ev.date + " (factor " + factor + ") adjusted for this run");
        }
    }

    static Map<String, List<LocalDate>> priceBreaks(List<YcConsensus.Event> events) {
        Map<String, List<LocalDate>> out = new HashMap<>();
        for (YcConsensus.Event ev : events)
            if (pending(ev) && YcMath.snapRatio(ratio(ev), 0.08) == null) out.computeIfAbsent(ev.ticker, k -> new ArrayList<>()).add(ev.date);
        return out;
    }

    // ---------------------------------------------------- composite score (calc.score_history + composite_score)
    static final class Score {
        double[] score, pct, zEps, zTp, zRev, zMom12, mom, mom12;
        boolean[] eligible;
    }

    static Score score(YcConsensus cons, YcPrices prices, Map<String, List<LocalDate>> breaks, int t) {
        double[][] rev = cons.revisions(t, SCORE_WEEKS);
        boolean any = false;
        for (int i = 0; i < cons.tickers.length && !any; i++) any = ok(rev[0][i]) || ok(rev[1][i]);
        if (!any) return null;
        LocalDate td = cons.dates.get(t);
        int n = cons.tickers.length;
        double[] mom = YcMath.nans(n), mom12 = YcMath.nans(n);
        Map<String, Double> m12 = prices.momentum12m1(td, breaks);
        for (int i = 0; i < n; i++) {
            String tk = cons.tickers[i];
            if (!prices.ff.containsKey(tk) || !prices.fresh(tk, td)) continue;
            mom[i] = prices.asof(tk, td) / prices.asof(tk, td.minusWeeks(SCORE_WEEKS)) - 1;
            mom12[i] = m12.getOrDefault(tk, NaN);
        }
        double[] eps = rev[1].clone(), tp = rev[0].clone();
        for (int i = 0; i < n; i++) {
            if (Math.abs(eps[i]) > 1.0) eps[i] = NaN;
            if (Math.abs(tp[i]) > 0.5) tp[i] = NaN;
        }
        List<Integer> el = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double c = cons.count[i][t];
            if (c >= MIN_ANALYSTS && ok(mom[i]) && (ok(eps[i]) || ok(tp[i]))) el.add(i);
        }
        Score sc = new Score();
        sc.mom = mom; sc.mom12 = mom12;
        sc.zEps = scatter(n, el, YcMath.rankZ(gather(eps, el)), 1);
        sc.zTp = scatter(n, el, YcMath.rankZ(gather(tp, el)), 1);
        sc.zRev = scatter(n, el, YcMath.rankZ(gather(mom, el)), -1);
        sc.zMom12 = scatter(n, el, YcMath.rankZ(gather(mom12, el)), 1);
        sc.score = YcMath.nans(n);
        for (int i = 0; i < n; i++) {
            double s = 0;
            int k = 0;
            for (double z : new double[]{sc.zEps[i], sc.zTp[i], sc.zRev[i], sc.zMom12[i]}) if (ok(z)) { s += z; k++; }
            if (k >= 2) sc.score[i] = s / k;
        }
        double[] r = YcMath.rankAverage(sc.score);
        int nv = 0;
        for (double v : sc.score) if (ok(v)) nv++;
        sc.pct = YcMath.nans(n);
        sc.eligible = new boolean[n];
        for (int i = 0; i < n; i++) if (ok(r[i])) { sc.pct[i] = r[i] / nv * 100; sc.eligible[i] = true; }
        return sc;
    }

    private static double[] gather(double[] v, List<Integer> idx) {
        double[] out = new double[idx.size()];
        for (int k = 0; k < idx.size(); k++) out[k] = v[idx.get(k)];
        return out;
    }

    private static double[] scatter(int n, List<Integer> idx, double[] v, int sign) {
        double[] out = YcMath.nans(n);
        for (int k = 0; k < idx.size(); k++) out[idx.get(k)] = ok(v[k]) ? sign * v[k] : NaN;
        return out;
    }

    // ---------------------------------------------------- one week (dashboard_db.week_rows + model.idea_lists)
    static final class M {
        int i;                    // index in cons.tickers
        String tk;
        double count, score, pct, pct4w, mom, mom12, zEps, zTp, zRev, zMom12, eps4, eps13, countChg13, price, ret1m;
        double tp1, tp4, fy1, fy2, rv1, age;
        int persist;
        boolean eligible, fno, isLong, isShort, isDiv, isEm;
        String group;
        int rank;
    }

    void weekRows(YcConsensus cons, YcPrices prices, YcPrices nifty, Map<Integer, Score> scores, int t, Set<String> fno,
                  Map<String, String> groups, Map<String, String> mapping,
                  List<Map<String, Object>> scoreRows, List<Map<String, Object>> groupRows, List<Map<String, Object>> boardRows) {
        LocalDate td = cons.dates.get(t);
        Score sc = scores.get(t);
        double[][] rev1 = cons.revisions(t, 1), rev4 = cons.revisions(t, 4), rev13 = cons.revisions(t, 13);
        double[][] fyEps = cons.fyRevisions(cons.eps, t, 4);
        double[] rv1 = cons.fyRevisions(cons.rev, t, 4)[0];
        int p4 = cons.pastDate(t, 4);
        Score sc4 = p4 >= 0 ? scores.get(p4) : null;
        double[] age = cons.age(t);
        int[] persist = cons.persistence(t, 13);
        LocalDate monthAgo = td.minusMonths(1);

        List<M> m = new ArrayList<>();
        for (int i = 0; i < cons.tickers.length; i++) {
            if (!ok(cons.count[i][t])) continue;
            M x = new M();
            String tk = cons.tickers[i];
            x.i = i; x.tk = tk;
            x.count = cons.count[i][t];
            x.eligible = sc.eligible[i];
            x.score = sc.score[i]; x.pct = sc.pct[i]; x.mom = sc.mom[i]; x.mom12 = sc.mom12[i];
            x.zEps = sc.zEps[i]; x.zTp = sc.zTp[i]; x.zRev = sc.zRev[i]; x.zMom12 = sc.zMom12[i];
            x.age = age[i];
            x.pct4w = sc4 == null ? NaN : sc4.pct[i];
            x.fno = fno.contains(tk);
            x.eps4 = rev4[1][i]; x.eps13 = rev13[1][i]; x.countChg13 = rev13[3][i];
            x.persist = persist[i];
            x.price = prices.ff.containsKey(tk) && prices.fresh(tk, td) ? prices.asof(tk, td) : NaN;
            x.tp1 = rev1[0][i]; x.tp4 = rev4[0][i];
            x.fy1 = Math.abs(fyEps[0][i]) <= 1.0 ? fyEps[0][i] : NaN;
            x.fy2 = Math.abs(fyEps[1][i]) <= 1.0 ? fyEps[1][i] : NaN;
            x.rv1 = rv1[i];
            x.group = groups.getOrDefault(tk, OTHER);
            x.ret1m = x.price / prices.asof(tk, monthAgo) - 1;
            m.add(x);
        }

        // ---- idea lists
        List<M> el = new ArrayList<>();
        for (M x : m) if (x.eligible) el.add(x);
        Comparator<M> scoreDesc = Comparator.comparingDouble((M x) -> x.score).reversed();
        List<M> longList = top(el, x -> true, scoreDesc, 60);
        Set<M> shownLong = new HashSet<>(longList.subList(0, Math.min(TOP_N, longList.size())));
        List<M> shortList = top(el, x -> x.fno, Comparator.comparingDouble((M x) -> x.score), TOP_N);
        Set<M> shownShort = new HashSet<>(shortList);
        List<M> divShort = top(el, x -> x.eps4 < -0.01 && x.mom > 0.03, Comparator.comparingDouble((M x) -> x.mom - x.eps4).reversed(), 40);
        List<M> divLong = top(el, x -> x.eps4 > 0.01 && x.mom < -0.03, Comparator.comparingDouble((M x) -> x.eps4 - x.mom).reversed(), 40);
        List<M> newTop = top(el, x -> x.pct >= 80 && !(x.pct4w >= 80) && !shownLong.contains(x),
                Comparator.comparingDouble((M x) -> jump(x)).reversed(), 12);
        List<M> newBottom = top(el, x -> x.pct <= 20 && !(x.pct4w <= 20) && !shownShort.contains(x),
                Comparator.comparingDouble((M x) -> jump(x)), 12);
        List<M> coverage = top(m, x -> x.countChg13 >= 3 && ok(x.price),
                Comparator.comparingDouble((M x) -> -x.countChg13).thenComparingDouble(x -> -x.count), 12);
        List<M> persistent = top(m, x -> x.persist >= 6 && x.count >= MIN_ANALYSTS,
                Comparator.comparingInt((M x) -> -x.persist).thenComparing(x -> x.eps13, YahooConsensusProcessService::descNanLast), 12);
        for (M x : shownLong) x.isLong = true;
        for (M x : shortList) x.isShort = true;
        for (M x : divShort) x.isDiv = true;
        for (M x : divLong) x.isDiv = true;
        for (List<M> l : Arrays.asList(newTop, newBottom, coverage, persistent)) for (M x : l) x.isEm = true;

        // ---- score rank: 1 = best; ties in ticker order (pandas rank method="first")
        List<M> ranked = new ArrayList<>();
        for (M x : m) if (ok(x.score)) ranked.add(x);
        ranked.sort(scoreDesc);                                          // stable: equal scores keep ticker order
        for (int k = 0; k < ranked.size(); k++) ranked.get(k).rank = k + 1;

        // ---- group rows
        double nifty1m = nifty.asof(MAIN_INDEX, td) / nifty.asof(MAIN_INDEX, monthAgo) - 1;
        TreeMap<String, List<M>> byGroup = new TreeMap<>();
        for (M x : m) byGroup.computeIfAbsent(x.group, k -> new ArrayList<>()).add(x);
        for (Map.Entry<String, List<M>> e : byGroup.entrySet()) {
            List<Double> eps = new ArrayList<>();
            double rs = 0;
            int rn = 0, up = 0, down = 0;
            for (M x : e.getValue()) {
                if (ok(x.eps4)) { eps.add(x.eps4); if (x.eps4 > 0.005) up++; if (x.eps4 < -0.005) down++; }
                if (ok(x.ret1m)) { rs += x.ret1m; rn++; }
            }
            double r = rn == 0 ? NaN : rs / rn;
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("week_date", td);
            g.put("watchlist_group", e.getKey());
            g.put("stock_count", e.getValue().size());
            g.put("avg_eps_rev_4w", eps.isEmpty() ? NaN : eps.stream().mapToDouble(Double::doubleValue).sum() / eps.size());
            g.put("up_count", up);
            g.put("down_count", down);
            g.put("avg_return_1m", r);
            g.put("rel_return_1m_vs_nifty", r - nifty1m);
            groupRows.add(g);
        }

        // ---- scoreboard row
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("week_date", td);
        for (String k : new String[]{"ic_spearman", "long_fwd_ret", "short_fwd_ret", "spread_fwd_ret", "hit_rate"}) b.put(k, NaN);
        int ne = 0, nu = 0, nd = 0;
        for (M x : m) if (x.eligible && ok(x.eps4)) { ne++; if (x.eps4 > 0.005) nu++; if (x.eps4 < -0.005) nd++; }
        b.put("breadth_up", ne == 0 ? NaN : (double) nu / ne);
        b.put("breadth_down", ne == 0 ? NaN : (double) nd / ne);
        LocalDate end = td.plusWeeks(FORWARD_WEEKS);
        if (!end.isAfter(prices.lastDate())) {
            List<M> dm = new ArrayList<>();
            List<Double> fwd = new ArrayList<>();
            for (M x : m) {
                double f = prices.asof(x.tk, end) / prices.asof(x.tk, td) - 1;
                if (ok(x.score) && ok(f) && Math.abs(f) < 1.0) { dm.add(x); fwd.add(f); }
            }
            if (dm.size() >= 30) {
                double mean = fwd.stream().mapToDouble(Double::doubleValue).sum() / fwd.size();
                double[] sArr = new double[dm.size()], fArr = new double[dm.size()];
                List<Double> lo = new ArrayList<>(), sh = new ArrayList<>();
                for (int k = 0; k < dm.size(); k++) {
                    sArr[k] = dm.get(k).score;
                    fArr[k] = fwd.get(k);
                    double xs = fwd.get(k) - mean;
                    if (dm.get(k).isLong) lo.add(xs);
                    if (dm.get(k).isShort) sh.add(xs);
                }
                double lm = lo.isEmpty() ? NaN : lo.stream().mapToDouble(Double::doubleValue).sum() / lo.size();
                double sm = sh.isEmpty() ? NaN : sh.stream().mapToDouble(Double::doubleValue).sum() / sh.size();
                b.put("ic_spearman", YcMath.spearman(sArr, fArr));
                b.put("long_fwd_ret", lm);
                b.put("short_fwd_ret", sm);
                b.put("spread_fwd_ret", lm - sm);
                int picks = lo.size() + sh.size();
                long hits = lo.stream().filter(v -> v > 0).count() + sh.stream().filter(v -> v < 0).count();
                b.put("hit_rate", picks == 0 ? NaN : (double) hits / picks);
            }
        }
        boardRows.add(b);

        // ---- stock rows, keyed by the stock_universe ticker (first one wins when two map to the same)
        Set<String> seen = new HashSet<>();
        for (M x : m) {
            String ticker = mapping.getOrDefault(x.tk, x.tk);
            if (!seen.add(ticker)) continue;
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("week_date", td);
            r.put("ticker", ticker);
            r.put("analyst_count", x.count);
            r.put("watchlist_group", x.group);
            r.put("tp_rev_1w", x.tp1);
            r.put("tp_rev_4w", x.tp4);
            r.put("eps_fy1_rev_4w", x.fy1);
            r.put("eps_fy2_rev_4w", x.fy2);
            r.put("rev_fy1_rev_4w", x.rv1);
            r.put("persistence_13w", (double) x.persist);
            r.put("mom_4w", x.mom);
            r.put("mom_12_1", x.mom12);
            r.put("score", x.score);
            r.put("score_rank", x.rank == 0 ? NaN : (double) x.rank);
            r.put("is_long", x.isLong ? 1 : 0);
            r.put("is_short", x.isShort ? 1 : 0);
            r.put("is_divergence", x.isDiv ? 1 : 0);
            r.put("is_emerging", x.isEm ? 1 : 0);
            r.put("price_at_score", x.price);
            r.put("eps_rev_4w", x.eps4);
            r.put("eps_rev_13w", x.eps13);
            r.put("score_pct", x.pct);
            r.put("z_eps", x.zEps);
            r.put("z_tp", x.zTp);
            r.put("z_rev", x.zRev);
            r.put("z_mom12", x.zMom12);
            r.put("count_chg_13w", x.countChg13);
            r.put("consensus_age_weeks", x.age);
            scoreRows.add(r);
        }
    }

    private static double jump(M x) { return x.pct - (ok(x.pct4w) ? x.pct4w : 50); }

    /** descending, NaN last */
    private static int descNanLast(Double a, Double b) {
        boolean na = !ok(a), nb = !ok(b);
        if (na || nb) return na == nb ? 0 : (na ? 1 : -1);
        return Double.compare(b, a);
    }

    private static <X> List<X> top(List<X> rows, Predicate<X> keep, Comparator<X> order, int n) {
        List<X> out = new ArrayList<>();
        for (X r : rows) if (keep.test(r)) out.add(r);
        out.sort(order);
        return out.size() > n ? new ArrayList<>(out.subList(0, n)) : out;
    }

    // ---------------------------------------------------- yahoo_consensus (consensus_db.load)
    private List<Map<String, Object>> yahooRows(YcInputs in, File history, Result res) throws Exception {
        List<Map<String, String>> recs = YcInputs.readCsv(history);
        Set<String> syms = new TreeSet<>();
        for (Map<String, String> r : recs) if (!YcInputs.str(r.get("YAHOO Ticker")).isEmpty()) syms.add(r.get("YAHOO Ticker"));
        Map<String, String> mapping = in.tickerMap(syms);
        String[][] cols = {{"TP(Avg)", "tp_avg"}, {"TP(Min)", "tp_min"}, {"TP(Max)", "tp_max"}, {"Rev.(Avg)", "rev_avg"},
                {"Rev.(Min)", "rev_min"}, {"Rev.(Max)", "rev_max"}, {"EPS(Avg)", "eps_avg"}, {"EPS(Min)", "eps_min"},
                {"EPS(Max)", "eps_max"}, {"#Count", "analyst_count"}, {"Year Ago Rev.", "year_ago_rev"},
                {"Year Ago EPS", "year_ago_eps"}, {"Current Price", "current_price"}};
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        Set<LocalDate> weeks = new HashSet<>();
        for (Map<String, String> r : recs) {
            String raw = YcInputs.str(r.get("YAHOO Ticker"));
            Map<String, Object> m = new LinkedHashMap<>();
            LocalDate date = LocalDate.parse(r.get("Date").trim().substring(0, 10));
            weeks.add(date);
            String fy = YcInputs.str(r.get("FIN Yr")).trim();
            m.put("ticker", mapping.getOrDefault(raw, raw.trim().toUpperCase(Locale.ROOT)));
            m.put("date", date);
            m.put("fin_year", fy.isEmpty() ? "nan" : fy);
            m.put("yahoo_ticker", raw.trim().toUpperCase(Locale.ROOT));
            for (String[] c : cols) m.put(c[1], YcInputs.num(r.get(c[0])));
            out.put(m.get("ticker") + "|" + date + "|" + m.get("fin_year"), m);       // keep="last"
        }
        Integer dbWeeks = jdbc.queryForObject("SELECT COUNT(DISTINCT date) FROM yahoo_consensus", Integer.class);
        if (dbWeeks != null && weeks.size() < dbWeeks)
            throw new IllegalStateException(history + " has " + weeks.size() + " weeks but the database already has " + dbWeeks
                    + ". Copy the complete YahooConsensusHistory.csv and run again.");
        res.step("YahooConsensusHistory.csv: " + out.size() + " rows for " + weeks.size() + " weeks");
        return new ArrayList<>(out.values());
    }

    // =====================================================================================================
    // compare with the database
    // =====================================================================================================
    void compare(Result res) {
        for (Table tb : Arrays.asList(YAHOO, SCORE, GROUP, BOARD, BROKER)) {
            List<Map<String, Object>> rows = res.tables.get(tb.name);
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("table", tb.name);
            c.put("skipped", rows == null);
            c.put("match", false);
            if (rows == null) { res.comparison.add(c); continue; }
            Map<String, Map<String, Object>> db = new HashMap<>();
            if (exists(tb.name)) {
                jdbc.query("SELECT * FROM " + tb.name, (ResultSet rs) -> {
                    Set<String> have = columns(rs);
                    Map<String, Object> m = new HashMap<>();
                    for (Col col : tb.all()) m.put(col.name, have.contains(col.name) ? read(rs, col) : "(missing column)");
                    db.put(key(tb, m), m);
                });
            }
            int same = 0, onlyJava = 0, rounding = 0;
            Map<String, Integer> diffCols = new TreeMap<>();
            List<String> examples = new ArrayList<>();
            Set<String> matched = new HashSet<>();
            for (Map<String, Object> r : rows) {
                Map<String, Object> norm = new HashMap<>();
                for (Col col : tb.all()) norm.put(col.name, normalise(r.get(col.name), col));
                String k = key(tb, norm);
                Map<String, Object> dr = db.get(k);
                if (dr == null) { onlyJava++; if (examples.size() < 15) examples.add("only in Java: " + k); continue; }
                matched.add(k);
                boolean rowSame = true;
                for (Col col : tb.cols) {
                    Object a = norm.get(col.name), b2 = dr.get(col.name);
                    if (Objects.equals(a, b2)) continue;
                    if (col.type == T.DEC && a != null && b2 instanceof BigDecimal
                            && ((BigDecimal) a).subtract((BigDecimal) b2).abs().compareTo(BigDecimal.ONE.movePointLeft(col.scale)) <= 0) {
                        rounding++;
                        continue;
                    }
                    rowSame = false;
                    diffCols.merge(col.name, 1, Integer::sum);
                    if (examples.size() < 15) examples.add(k + " " + col.name + ": Java " + a + " / database " + b2);
                }
                if (rowSame) same++;
            }
            int onlyDb = 0;
            for (String k : db.keySet()) if (!matched.contains(k)) { onlyDb++; if (examples.size() < 15) examples.add("only in database: " + k); }
            c.put("javaRows", rows.size());
            c.put("dbRows", db.size());
            c.put("identical", same);
            c.put("onlyJava", onlyJava);
            c.put("onlyDb", onlyDb);
            c.put("lastDigit", rounding);
            c.put("diffColumns", diffCols);
            c.put("examples", examples);
            c.put("match", onlyJava == 0 && onlyDb == 0 && diffCols.isEmpty());
            res.comparison.add(c);
        }
    }

    private static String key(Table tb, Map<String, Object> m) {
        StringBuilder sb = new StringBuilder();
        for (Col k : tb.keys) sb.append(sb.length() == 0 ? "" : " | ").append(m.get(k.name));
        return sb.toString();
    }

    private static Set<String> columns(ResultSet rs) throws SQLException {
        Set<String> out = new HashSet<>();
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) out.add(md.getColumnLabel(i).toLowerCase(Locale.ROOT));
        return out;
    }

    private static Object read(ResultSet rs, Col c) throws SQLException {
        switch (c.type) {
            case INT: { long v = rs.getLong(c.name); return rs.wasNull() ? null : v; }
            case DEC: { BigDecimal v = rs.getBigDecimal(c.name); return v == null ? null : v.setScale(c.scale, RoundingMode.HALF_UP); }
            case DATE: { java.sql.Date v = rs.getDate(c.name); return v == null ? null : v.toLocalDate(); }
            default: return rs.getString(c.name);
        }
    }

    /** a computed value as MySQL would store it: NaN / infinity -> NULL, decimals rounded to the column scale */
    static Object normalise(Object v, Col c) {
        if (v == null) return null;
        if (v instanceof Double && !YcMath.finite((Double) v)) return null;
        switch (c.type) {
            case INT: return ((Number) v).longValue();
            case DEC: return new BigDecimal(Double.toString(((Number) v).doubleValue())).setScale(c.scale, RoundingMode.HALF_UP);
            case DATE: return v;
            default: return v.toString();
        }
    }

    private boolean exists(String table) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class, table);
        return n != null && n > 0;
    }

    // =====================================================================================================
    // write (one transaction)
    // =====================================================================================================
    /** ALTER TABLE ... ADD COLUMN for a column a table created by an older version does not have yet */
    private static void addMissingColumn(Connection con, Statement st, String table, String[] col) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                + "AND table_name = ? AND column_name = ?")) {
            ps.setString(1, table);
            ps.setString(2, col[0]);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) st.execute("ALTER TABLE " + table + " ADD COLUMN " + col[0] + " " + col[1]);
            }
        }
    }

    void write(Result res) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            try (Statement st = con.createStatement()) {                 // DDL commits on its own in MySQL
                for (String ddl : YcSchema.DDL) st.execute(ddl);
                for (String[] col : YcSchema.NEW_SCORE_COLS) addMissingColumn(con, st, "consensus_weekly_score", col);
                for (String[] col : YcSchema.NEW_BROKER_COLS) addMissingColumn(con, st, "consensus_broker_view", col);
            }
            boolean auto = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                for (Table tb : Arrays.asList(YAHOO, SCORE, GROUP, BOARD, BROKER)) {
                    List<Map<String, Object>> rows = res.tables.get(tb.name);
                    if (rows == null) continue;                            // broker views not read: table left as it is
                    try (Statement st = con.createStatement()) { st.executeUpdate("DELETE FROM " + tb.name); }
                    insert(con, tb, rows);
                }
                con.commit();
                res.written = true;
                res.step("Database updated: " + res.tables.keySet());
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(auto);
            }
        }
    }

    private static void insert(Connection con, Table tb, List<Map<String, Object>> rows) throws SQLException {
        List<Col> cols = tb.all();
        StringBuilder names = new StringBuilder(), one = new StringBuilder("(");
        for (int k = 0; k < cols.size(); k++) {
            names.append(k == 0 ? "" : ", ").append(cols.get(k).name);
            one.append(k == 0 ? "?" : ",?");
        }
        one.append(")");
        int per = Math.max(1, 30000 / cols.size());
        for (int from = 0; from < rows.size(); from += per) {
            List<Map<String, Object>> chunk = rows.subList(from, Math.min(rows.size(), from + per));
            StringBuilder sql = new StringBuilder("INSERT INTO " + tb.name + " (" + names + ") VALUES ");
            for (int k = 0; k < chunk.size(); k++) sql.append(k == 0 ? "" : ",").append(one);
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int p = 1;
                for (Map<String, Object> r : chunk) {
                    for (Col c : cols) {
                        Object v = normalise(r.get(c.name), c);
                        if (v == null) ps.setNull(p++, Types.NULL);
                        else if (v instanceof LocalDate) ps.setDate(p++, java.sql.Date.valueOf((LocalDate) v));
                        else ps.setObject(p++, v);
                    }
                }
                ps.executeUpdate();
            }
        }
    }
}
