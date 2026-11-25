package com.timelineofwealth.service.consensus;

import java.time.LocalDate;
import java.util.*;

/**
 * A date x ticker price panel (pandas DataFrame from pivot_table), with the forward-filled copy and the "as of"
 * lookups of weekly_lib.calc (asof_row, fresh_mask, momentum_12_1).
 */
final class YcPrices {
    final List<LocalDate> dates;                    // sorted, union of every ticker's trading days
    final Map<String, double[]> raw;                // ticker -> close per date (NaN = no row)
    Map<String, double[]> ff;                       // forward-filled copy, built by fill()

    YcPrices(List<LocalDate> dates, Map<String, double[]> raw) {
        this.dates = dates;
        this.raw = raw;
    }

    /** prices.ffill() */
    void fill() {
        ff = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> e : raw.entrySet()) {
            double[] s = e.getValue().clone();
            for (int i = 1; i < s.length; i++) if (Double.isNaN(s[i])) s[i] = s[i - 1];
            ff.put(e.getKey(), s);
        }
    }

    LocalDate lastDate() { return dates.isEmpty() ? null : dates.get(dates.size() - 1); }

    /** position of the last date <= when, or -1 */
    int asofIndex(LocalDate when) {
        int lo = 0, hi = dates.size() - 1, ans = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (!dates.get(mid).isAfter(when)) { ans = mid; lo = mid + 1; } else hi = mid - 1;
        }
        return ans;
    }

    /** calc.asof_row(prices_ff, when)[ticker] */
    double asof(String ticker, LocalDate when) {
        double[] s = ff.get(ticker);
        int i = asofIndex(when);
        return s == null || i < 0 ? YcMath.NaN : s[i];
    }

    /** calc.fresh_mask(prices, when, 6)[ticker]: traded (unfilled price present) in [when - 6 days, when] */
    boolean fresh(String ticker, LocalDate when) {
        double[] s = raw.get(ticker);
        if (s == null) return false;
        LocalDate from = when.minusDays(6);
        for (int i = asofIndex(when); i >= 0 && !dates.get(i).isBefore(from); i--) if (!Double.isNaN(s[i])) return true;
        return false;
    }

    /**
     * calc.momentum_12_1 for every ticker: price 22 trading days back over price 253 trading days back, minus 1;
     * NaN when fewer than 253 rows or when a price break lies inside the window.
     */
    Map<String, Double> momentum12m1(LocalDate t, Map<String, List<LocalDate>> breaks) {
        Map<String, Double> out = new HashMap<>();
        int last = asofIndex(t);
        int n = last + 1, look = 252, skip = 21;
        if (n <= look) return out;
        int iStart = last - look, iEnd = last - skip;
        LocalDate start = dates.get(iStart), end = dates.get(iEnd);
        for (Map.Entry<String, double[]> e : ff.entrySet()) {
            double[] s = e.getValue();
            double v = s[iEnd] / s[iStart] - 1;
            List<LocalDate> bs = breaks.get(e.getKey());
            if (bs != null) for (LocalDate x : bs) if (x.isAfter(start) && !x.isAfter(end)) { v = YcMath.NaN; break; }
            out.put(e.getKey(), v);
        }
        return out;
    }
}
