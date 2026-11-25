package com.timelineofwealth.service.consensus;

import java.util.Arrays;

/**
 * Numeric helpers that behave like the pandas / numpy / scipy calls used by the Python weekly_lib, so the Java
 * port gives the same numbers: NaN means "missing" and is skipped the way pandas skips it.
 */
final class YcMath {
    static final double NaN = Double.NaN;
    private static final double[] NICE_RATIOS = {1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0, 20.0};

    private YcMath() {}

    /** pandas notna: only NaN counts as missing (infinity is a value, as in pandas). */
    static boolean ok(double v) { return !Double.isNaN(v); }

    /** numpy isfinite (snap_ratio, writing to MySQL) */
    static boolean finite(double v) { return !Double.isNaN(v) && !Double.isInfinite(v); }

    static double[] nans(int n) {
        double[] a = new double[n];
        Arrays.fill(a, NaN);
        return a;
    }

    /** pandas Series.rank() (method="average", 1-based); NaN stays NaN. */
    static double[] rankAverage(double[] v) {
        int n = v.length;
        double[] out = nans(n);
        Integer[] idx = new Integer[n];
        int k = 0;
        for (int i = 0; i < n; i++) if (ok(v[i])) idx[k++] = i;
        Integer[] ix = Arrays.copyOf(idx, k);
        Arrays.sort(ix, (a, b) -> Double.compare(v[a], v[b]));
        for (int i = 0; i < k; ) {
            int j = i;
            while (j + 1 < k && v[ix[j + 1]] == v[ix[i]]) j++;
            double r = (i + j) / 2.0 + 1;
            for (int q = i; q <= j; q++) out[ix[q]] = r;
            i = j + 1;
        }
        return out;
    }

    /** pandas mean (skipna). */
    static double mean(double[] v) {
        double s = 0;
        int n = 0;
        for (double x : v) if (ok(x)) { s += x; n++; }
        return n == 0 ? NaN : s / n;
    }

    /** pandas std (ddof=1, skipna). */
    static double std(double[] v) {
        double m = mean(v);
        int n = 0;
        double ss = 0;
        for (double x : v) if (ok(x)) { ss += (x - m) * (x - m); n++; }
        return n < 2 ? NaN : Math.sqrt(ss / (n - 1));
    }

    /** weekly_lib.calc.rank_z: (rank - mean(rank)) / std(rank). */
    static double[] rankZ(double[] v) {
        double[] r = rankAverage(v);
        double m = mean(r), s = std(r);
        double[] out = nans(v.length);
        for (int i = 0; i < v.length; i++) if (ok(r[i])) out[i] = (r[i] - m) / s;
        return out;
    }

    /** scipy.stats.spearmanr(a, b).correlation for arrays without NaN: Pearson correlation of average ranks. */
    static double spearman(double[] a, double[] b) {
        double[] ra = rankAverage(a), rb = rankAverage(b);
        double ma = mean(ra), mb = mean(rb);
        double sab = 0, saa = 0, sbb = 0;
        for (int i = 0; i < a.length; i++) {
            double x = ra[i] - ma, y = rb[i] - mb;
            sab += x * y;
            saa += x * x;
            sbb += y * y;
        }
        return saa == 0 || sbb == 0 ? NaN : sab / Math.sqrt(saa * sbb);
    }

    /** weekly_lib.calc.snap_ratio: nearest clean split / bonus ratio, or null. */
    static Double snapRatio(double ratio, double tol) {
        if (!finite(ratio) || ratio < 1.3) return null;
        int best = 0;
        double bestErr = Double.MAX_VALUE;
        for (int i = 0; i < NICE_RATIOS.length; i++) {
            double err = Math.abs(NICE_RATIOS[i] / ratio - 1);
            if (err < bestErr) { bestErr = err; best = i; }      // first minimum, like numpy argmin
        }
        return Math.abs(NICE_RATIOS[best] / ratio - 1) <= tol ? NICE_RATIOS[best] : null;
    }
}
