package com.timelineofwealth.service.consensus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.timelineofwealth.service.consensus.YcMath.NaN;
import static com.timelineofwealth.service.consensus.YcMath.ok;

/**
 * Port of weekly_lib.calc.Consensus: the weekly Yahoo consensus as date x ticker matrices (stored here as
 * [ticker][date]), with split / bonus rescaling, suspect-jump breaks and roll-safe revisions.
 */
final class YcConsensus {
    /** one row of a weekly *_YahooConsensus.csv file (after load.load_consensus clean-up) */
    static final class Row {
        LocalDate date;
        String ticker;          // YAHOO Ticker, trimmed, upper case
        String finYr;           // null when blank
        double tp, tpMin, tpMax, eps, epsMin, epsMax, rev, count;   // 0 -> NaN
    }

    /** a stock_split_probability row */
    static final class Event {
        String ticker;
        LocalDate date;
        double close, previousClose, dayPercentChange;
        String isProcessed;     // str(value) as pandas would print it ("None" for NULL)
        String note;            // null for NULL
    }

    final List<LocalDate> dates;
    final String[] tickers;
    final Map<String, Integer> tickerIndex = new HashMap<>();
    final double[][] tp, tpMin, tpMax, count;
    final TreeMap<String, double[][]> eps = new TreeMap<>(), epsMin = new TreeMap<>(), epsMax = new TreeMap<>(), rev = new TreeMap<>();
    final Map<String, List<LocalDate>> breaks = new HashMap<>();
    final List<String> notes = new ArrayList<>();
    private final Map<Long, double[][]> revCache = new HashMap<>();

    /** rows must already be de-duplicated on (date, ticker, finYr) and sorted on (date, ticker, finYr nulls last) */
    YcConsensus(List<Row> rows) {
        TreeSet<LocalDate> ds = new TreeSet<>();
        TreeSet<String> ts = new TreeSet<>();
        TreeSet<String> labels = new TreeSet<>();
        for (Row r : rows) { ds.add(r.date); ts.add(r.ticker); if (r.finYr != null) labels.add(r.finYr); }
        dates = new ArrayList<>(ds);
        tickers = ts.toArray(new String[0]);
        for (int i = 0; i < tickers.length; i++) tickerIndex.put(tickers[i], i);
        Map<LocalDate, Integer> di = new HashMap<>();
        for (int i = 0; i < dates.size(); i++) di.put(dates.get(i), i);
        int nt = tickers.length, nd = dates.size();
        tp = matrix(nt, nd); tpMin = matrix(nt, nd); tpMax = matrix(nt, nd); count = matrix(nt, nd);
        for (String l : labels) { eps.put(l, matrix(nt, nd)); epsMin.put(l, matrix(nt, nd)); epsMax.put(l, matrix(nt, nd)); rev.put(l, matrix(nt, nd)); }
        // base = first row per (date, ticker) in (date, ticker, finYr) order: TP and analyst count come from it
        Set<String> seen = new HashSet<>();
        for (Row r : rows) {
            int t = tickerIndex.get(r.ticker), d = di.get(r.date);
            if (seen.add(d + "|" + r.ticker)) {
                tp[t][d] = r.tp; tpMin[t][d] = r.tpMin; tpMax[t][d] = r.tpMax; count[t][d] = r.count;
            }
            if (r.finYr != null) {
                eps.get(r.finYr)[t][d] = r.eps; epsMin.get(r.finYr)[t][d] = r.epsMin;
                epsMax.get(r.finYr)[t][d] = r.epsMax; rev.get(r.finYr)[t][d] = r.rev;
            }
        }
    }

    private static double[][] matrix(int nt, int nd) {
        double[][] m = new double[nt][];
        for (int i = 0; i < nt; i++) m[i] = YcMath.nans(nd);
        return m;
    }

    // ---------------------------------------------------- corporate actions
    /** _per_share order: TP, TP min, TP max, EPS avg / min / max by fiscal-year label */
    private List<double[][]> perShare() {
        List<double[][]> out = new ArrayList<>(Arrays.asList(tp, tpMin, tpMax));
        out.addAll(eps.values());
        out.addAll(epsMin.values());
        out.addAll(epsMax.values());
        return out;
    }

    /** Consensus._rescale: divide the history before the first later update of about 1/factor by factor. */
    private boolean rescale(double[] s, LocalDate evDate, double factor) {
        LocalDate from = evDate.minusDays(14);
        int tStar = -1;
        double prevFilled = NaN;
        for (int i = 0; i < s.length; i++) {
            double r = i == 0 ? NaN : s[i] / prevFilled;
            if (!dates.get(i).isBefore(from) && ok(r) && Math.abs(r - 1) > 0.001) {
                double x = r * factor;
                if (x >= 0.6 && x <= 1.4) { tStar = i; break; }
            }
            if (ok(s[i])) prevFilled = s[i];
        }
        if (tStar < 0) return false;
        for (int i = 0; i < tStar; i++) s[i] = s[i] / factor;
        return true;
    }

    void applyCorporateActions(List<Event> events) {
        LocalDate first = dates.get(0).minusDays(45);
        for (Event ev : events) {
            if (ev.date.isBefore(first)) continue;
            Integer t = tickerIndex.get(ev.ticker);
            if (t == null) continue;
            String note = ev.note == null ? "" : ev.note.trim();
            double ratio = ev.close != 0 && !Double.isNaN(ev.close) ? ev.previousClose / ev.close
                    : (Double.isNaN(ev.close) ? ev.previousClose / ev.close : NaN);
            String low = note.toLowerCase(Locale.ROOT);
            boolean namedSplit = low.contains("bonus") || low.contains("split");
            boolean pending = !"YES".equals(ev.isProcessed.toUpperCase(Locale.ROOT));
            Double factor = namedSplit ? YcMath.snapRatio(ratio, 0.10) : (pending ? YcMath.snapRatio(ratio, 0.06) : null);
            if (factor != null) {
                boolean touched = false;
                for (double[][] m : perShare()) touched |= rescale(m[t], ev.date, factor);
                if (touched) notes.add(ev.ticker + ": " + (note.isEmpty() ? "split" : note) + " on " + ev.date
                        + " - consensus TP/EPS history before it divided by " + fmt(factor));
            } else {
                breaks.computeIfAbsent(ev.ticker, k -> new ArrayList<>()).add(ev.date);
                notes.add(ev.ticker + ": corporate action on " + ev.date + " (note: " + (note.isEmpty() ? "none" : note)
                        + ") - revisions across it ignored");
            }
        }
    }

    /** Consensus.flag_suspect_jumps: a TP move below -40% or above +67% with no recorded split breaks revisions. */
    void flagSuspectJumps() {
        for (int d = 1; d < dates.size(); d++) {
            for (int t = 0; t < tickers.length; t++) {
                double prev = NaN;
                for (int k = d - 1; k >= 0; k--) if (ok(tp[t][k])) { prev = tp[t][k]; break; }
                double r = tp[t][d] / prev;
                if (ok(r) && (r < 0.6 || r > 1.67)) {
                    breaks.computeIfAbsent(tickers[t], k -> new ArrayList<>()).add(dates.get(d).minusDays(1));
                    notes.add(tickers[t] + ": consensus TP moved " + Math.round((r - 1) * 100) + "% in the week to "
                            + dates.get(d) + " with no recorded split - revisions across it ignored");
                }
            }
        }
    }

    // ---------------------------------------------------- revisions
    /** Consensus.past_date: the snapshot nearest to `weeks` before t (within 4 days and earlier than t), or -1. */
    int pastDate(int t, int weeks) {
        LocalDate target = dates.get(t).minusWeeks(weeks);
        int best = 0;
        long bestGap = Long.MAX_VALUE;
        for (int i = 0; i < dates.size(); i++) {
            long g = Math.abs(ChronoUnit.DAYS.between(target, dates.get(i)));
            if (g < bestGap) { bestGap = g; best = i; }
        }
        return bestGap <= 4 && dates.get(best).isBefore(dates.get(t)) ? best : -1;
    }

    /** Consensus._no_break: false when a break date lies in (p, t]. */
    boolean noBreak(int ticker, int p, int t) {
        List<LocalDate> bs = breaks.get(tickers[ticker]);
        if (bs == null) return true;
        LocalDate dp = dates.get(p), dt = dates.get(t);
        for (LocalDate d : bs) if (d.isAfter(dp) && !d.isAfter(dt)) return false;
        return true;
    }

    static double relChange(double a, double b) {
        return Math.abs(b) > 0.01 ? (a - b) / Math.abs(b) : NaN;
    }

    /** Consensus.revisions(t, weeks, "avg"): {tp, eps, rev, countChg} per ticker. */
    double[][] revisions(int t, int weeks) {
        long key = t * 1000L + weeks;
        double[][] cached = revCache.get(key);
        if (cached != null) return cached;
        int nt = tickers.length;
        double[] tpChg = YcMath.nans(nt), epsChg = YcMath.nans(nt), revChg = YcMath.nans(nt), countChg = YcMath.nans(nt);
        int p = pastDate(t, weeks);
        if (p >= 0) {
            for (int i = 0; i < nt; i++) {
                tpChg[i] = tp[i][t] / tp[i][p] - 1;
                epsChg[i] = meanRel(eps, i, t, p);
                revChg[i] = meanRel(rev, i, t, p);
                countChg[i] = count[i][t] - count[i][p];
                if (!noBreak(i, p, t)) { tpChg[i] = NaN; epsChg[i] = NaN; revChg[i] = NaN; }
            }
        }
        double[][] out = {tpChg, epsChg, revChg, countChg};
        revCache.put(key, out);
        return out;
    }

    private static double meanRel(TreeMap<String, double[][]> mats, int i, int t, int p) {
        double s = 0;
        int n = 0;
        for (double[][] m : mats.values()) {
            double c = relChange(m[i][t], m[i][p]);
            if (ok(c)) { s += c; n++; }
        }
        return n == 0 ? NaN : s / n;
    }

    /** Consensus.persistence: weeks with an EPS upgrade minus weeks with a downgrade over the last 13 weekly changes. */
    int[] persistence(int t, int weeks) {
        int[] net = new int[tickers.length];
        for (int k = 0; k < weeks; k++) {
            if (t - k < 1) break;
            double[] r = revisions(t - k, 1)[1];
            for (int i = 0; i < net.length; i++) {
                if (r[i] > 0.001) net[i]++;
                else if (r[i] < -0.001) net[i]--;
            }
        }
        return net;
    }

    /** Consensus.change_stats(t)["age"]: weeks since the TP or any EPS estimate last moved by more than 0.1%. */
    double[] age(int t) {
        double[] out = new double[tickers.length];
        for (int i = 0; i < tickers.length; i++) {
            int last = -1;
            for (int d = t; d >= 1 && last < 0; d--) {
                if (moved(tp[i][d], tp[i][d - 1])) { last = d; break; }
                for (double[][] m : eps.values()) if (moved(m[i][d], m[i][d - 1])) { last = d; break; }
            }
            LocalDate lastDate = last < 0 ? dates.get(0) : dates.get(last);
            out[i] = Math.rint(ChronoUnit.DAYS.between(lastDate, dates.get(t)) / 7.0);
        }
        return out;
    }

    private static boolean moved(double now, double before) {
        double c = now / before - 1;              // pct_change(fill_method=None): NaN when either side is missing
        return ok(c) && Math.abs(c) > 0.001;
    }

    /** dashboard_db.fy_revisions: change of the nearest (FY1) and next (FY2) fiscal year shown at t, same label. */
    double[][] fyRevisions(TreeMap<String, double[][]> mats, int t, int weeks) {
        int nt = tickers.length;
        double[] fy1 = YcMath.nans(nt), fy2 = YcMath.nans(nt);
        int p = pastDate(t, weeks);
        if (p < 0 || mats.isEmpty()) return new double[][]{fy1, fy2};
        List<double[][]> ms = new ArrayList<>(mats.values());
        for (int i = 0; i < nt; i++) {
            int first = -1, second = -1;
            for (int l = 0; l < ms.size(); l++) {
                if (!ok(ms.get(l)[i][t])) continue;
                if (first < 0) first = l; else { second = l; break; }
            }
            boolean nb = noBreak(i, p, t);
            if (first >= 0 && nb) fy1[i] = relChange(ms.get(first)[i][t], ms.get(first)[i][p]);
            if (second >= 0 && nb) fy2[i] = relChange(ms.get(second)[i][t], ms.get(second)[i][p]);
        }
        return new double[][]{fy1, fy2};
    }

    private static String fmt(double v) { return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v); }
}
