package com.timelineofwealth.service.consensus;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.timelineofwealth.service.consensus.YcMath.NaN;

/**
 * File and database inputs of the weekly consensus process, ported from weekly_lib.load, weekly_lib.db and
 * weekly_lib.consensus_db.ticker_map. Each method notes the Python function it mirrors.
 */
final class YcInputs {
    static final Pattern WEEKLY_FILE = Pattern.compile("^(\\d{8})_YahooConsensus\\.csv$", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JdbcTemplate jdbc;

    YcInputs(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    // =====================================================================================================
    // CSV files
    // =====================================================================================================

    /** load.load_consensus: every yyyymmdd_YahooConsensus.csv, zeros -> NaN, de-duplicated and sorted. */
    static List<YcConsensus.Row> weeklyConsensus(File folder, List<String> notes) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) throw new IOException("Folder not found: " + folder);
        TreeMap<String, File> byKey = new TreeMap<>();               // (date, path) order
        for (File f : files) {
            Matcher m = WEEKLY_FILE.matcher(f.getName());
            if (m.matches()) byKey.put(m.group(1) + "|" + f.getPath(), f);
        }
        if (byKey.isEmpty()) throw new IOException("No *_YahooConsensus.csv files in " + folder);
        List<YcConsensus.Row> rows = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Set<String> caseDups = new TreeSet<>();
        int dropped = 0;
        for (Map.Entry<String, File> e : byKey.entrySet()) {
            LocalDate date = LocalDate.parse(e.getKey().substring(0, 8), YMD);
            for (Map<String, String> rec : readCsv(e.getValue())) {
                YcConsensus.Row r = new YcConsensus.Row();
                r.date = date;
                String raw = str(rec.get("YAHOO Ticker")).trim();
                r.ticker = raw.toUpperCase(Locale.ROOT);
                if (!raw.equals(r.ticker)) caseDups.add(raw);
                String fy = str(rec.get("FIN Yr")).trim();
                r.finYr = fy.isEmpty() || fy.equals("nan") ? null : fy;
                r.tp = zeroNaN(num(rec.get("TP(Avg)")));
                r.tpMin = zeroNaN(num(rec.get("TP(Min)")));
                r.tpMax = zeroNaN(num(rec.get("TP(Max)")));
                r.eps = zeroNaN(num(rec.get("EPS(Avg)")));
                r.epsMin = zeroNaN(num(rec.get("EPS(Min)")));
                r.epsMax = zeroNaN(num(rec.get("EPS(Max)")));
                r.rev = zeroNaN(num(rec.get("Rev.(Avg)")));
                r.count = zeroNaN(num(rec.get("#Count")));
                if (seen.add(date + "|" + r.ticker + "|" + r.finYr)) rows.add(r); else dropped++;
            }
        }
        if (!caseDups.isEmpty()) notes.add("Ticker list has lower-case duplicates, merged: " + String.join(", ", caseDups));
        if (dropped > 0) notes.add("Dropped " + dropped + " duplicate consensus rows");
        rows.sort(Comparator.comparing((YcConsensus.Row r) -> r.date).thenComparing(r -> r.ticker)
                .thenComparing(r -> r.finYr, Comparator.nullsLast(Comparator.naturalOrder())));
        return rows;
    }

    /** CSV rows as column -> raw text ("" for an empty cell). */
    static List<Map<String, String>> readCsv(File f) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        try (Reader in = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8);
             CSVParser p = CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreSurroundingSpaces(false).parse(in)) {
            List<String> header = new ArrayList<>(p.getHeaderMap().keySet());
            for (CSVRecord rec : p) {
                Map<String, String> m = new HashMap<>();
                for (String h : header) m.put(h.startsWith("﻿") ? h.substring(1) : h, rec.isSet(h) ? rec.get(h) : "");
                out.add(m);
            }
        }
        return out;
    }

    static String str(String s) { return s == null ? "" : s; }

    /** pd.to_numeric(errors="coerce") */
    static double num(String s) {
        if (s == null) return NaN;
        String t = s.trim();
        if (t.isEmpty()) return NaN;
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return NaN;
        }
    }

    static double zeroNaN(double v) { return v == 0 ? NaN : v; }

    // =====================================================================================================
    // database
    // =====================================================================================================

    /** db.load_known_tickers */
    Set<String> knownTickers() {
        Set<String> out = new HashSet<>();
        jdbc.query("SELECT ticker FROM stock_universe", (ResultSet rs) -> {
            String t = rs.getString(1);
            if (t != null) out.add(t.trim().toUpperCase(Locale.ROOT));
        });
        return out;
    }

    /** db.load_ticker_renames: old NSE symbol -> current symbol (numeric ticker_old are BSE codes, skipped). */
    Map<String, String> tickerRenames() {
        Map<String, String> out = new LinkedHashMap<>();
        jdbc.query("SELECT ticker, ticker_old FROM stock_universe WHERE ticker_old IS NOT NULL AND ticker_old <> '' AND ticker_old <> ticker",
                (ResultSet rs) -> {
                    String t = rs.getString(1).trim().toUpperCase(Locale.ROOT);
                    String o = rs.getString(2).trim().toUpperCase(Locale.ROOT);
                    if (!o.matches("\\d+")) out.put(o, t);
                });
        return out;
    }

    /** db.load_bse_symbols reversed as in ticker_map: SYMBOL -> BSE code. */
    Map<String, String> bseBySymbol() {
        Map<String, String> codeToSym = new LinkedHashMap<>();
        jdbc.query("SELECT ticker, short_name FROM stock_universe WHERE ticker REGEXP '^[0-9]+$'", (ResultSet rs) -> {
            String sn = rs.getString(2) == null ? "" : rs.getString(2).trim();
            if (sn.matches("[A-Z0-9&\\-]+")) codeToSym.put(rs.getString(1).trim(), sn);
        });
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : codeToSym.entrySet()) out.put(e.getValue().toUpperCase(Locale.ROOT), e.getKey());
        return out;
    }

    /** consensus_db.ticker_map: Yahoo symbol -> stock_universe ticker (itself, its renamed symbol, its BSE code). */
    Map<String, String> tickerMap(Collection<String> symbols) {
        Set<String> known = knownTickers();
        Map<String, String> renames = tickerRenames();
        Map<String, String> bse = bseBySymbol();
        Map<String, String> out = new HashMap<>();
        for (String s : symbols) {
            String u = s.trim().toUpperCase(Locale.ROOT);
            if (known.contains(u)) out.put(s, u);
            else if (renames.containsKey(u) && known.contains(renames.get(u))) out.put(s, renames.get(u));
            else if (bse.containsKey(u)) out.put(s, bse.get(u));
            else out.put(s, u);
        }
        return out;
    }

    /** db.load_prices: close per date for the given tickers since `start` (pivot_table: NULL closes ignored). */
    YcPrices prices(Collection<String> tickers, LocalDate start) {
        Map<String, TreeMap<LocalDate, Double>> byTicker = new HashMap<>();
        TreeSet<LocalDate> dates = new TreeSet<>();
        List<String> sorted = new ArrayList<>(new TreeSet<>(tickers));
        for (List<String> chunk : chunks(sorted, 900)) {
            jdbc.query("SELECT nse_ticker, date, close_price FROM nse_price_history WHERE nse_ticker IN (" + marks(chunk.size()) + ") AND date >= ?",
                    (ResultSet rs) -> {
                        double v = rs.getDouble(3);
                        if (rs.wasNull()) return;
                        LocalDate d = rs.getDate(2).toLocalDate();
                        byTicker.computeIfAbsent(rs.getString(1).toUpperCase(Locale.ROOT), k -> new TreeMap<>()).put(d, v);
                        dates.add(d);
                    }, params(chunk, java.sql.Date.valueOf(start)));
        }
        List<LocalDate> ds = new ArrayList<>(dates);
        Map<LocalDate, Integer> di = new HashMap<>();
        for (int i = 0; i < ds.size(); i++) di.put(ds.get(i), i);
        Map<String, double[]> raw = new TreeMap<>();
        for (Map.Entry<String, TreeMap<LocalDate, Double>> e : byTicker.entrySet()) {
            double[] s = YcMath.nans(ds.size());
            for (Map.Entry<LocalDate, Double> p : e.getValue().entrySet()) s[di.get(p.getKey())] = p.getValue();
            raw.put(e.getKey(), s);
        }
        return new YcPrices(ds, raw);
    }

    /** db.load_index_levels for one index, forward-filled (only NIFTY is needed). */
    YcPrices indexLevels(String index, LocalDate start) {
        TreeMap<LocalDate, Double> s = new TreeMap<>();
        jdbc.query("SELECT date, value FROM index_valuation WHERE ticker = ? AND date >= ?", (ResultSet rs) -> {
            double v = rs.getDouble(2);
            if (!rs.wasNull()) s.put(rs.getDate(1).toLocalDate(), v);
        }, index, java.sql.Date.valueOf(start));
        List<LocalDate> ds = new ArrayList<>(s.keySet());
        double[] v = new double[ds.size()];
        for (int i = 0; i < ds.size(); i++) v[i] = s.get(ds.get(i));
        Map<String, double[]> raw = new HashMap<>();
        raw.put(index, v);
        YcPrices p = new YcPrices(ds, raw);
        p.fill();
        return p;
    }

    /** db.load_split_events */
    List<YcConsensus.Event> splitEvents(Collection<String> tickers, LocalDate start) {
        List<YcConsensus.Event> out = new ArrayList<>();
        List<String> sorted = new ArrayList<>(new TreeSet<>(tickers));
        // one query (as in Python) so the order of same-day events is the database's
        jdbc.query("SELECT ticker, date, close_price, previous_close_price, day_percent_change, is_processed, note " +
                        "FROM stock_split_probability WHERE ticker IN (" + marks(sorted.size()) + ") AND date >= ? ORDER BY date",
                (ResultSet rs) -> {
                    YcConsensus.Event e = new YcConsensus.Event();
                    e.ticker = rs.getString(1).toUpperCase(Locale.ROOT);
                    e.date = rs.getDate(2).toLocalDate();
                    e.close = dbl(rs, 3);
                    e.previousClose = dbl(rs, 4);
                    e.dayPercentChange = dbl(rs, 5);
                    String p = rs.getString(6);
                    e.isProcessed = p == null ? "None" : p;
                    e.note = rs.getString(7);
                    out.add(e);
                }, params(sorted, java.sql.Date.valueOf(start)));
        return out;
    }

    /** db.load_stock_meta(...)["is_fno"] == 1 */
    Set<String> fnoTickers(Collection<String> tickers) {
        Set<String> out = new HashSet<>();
        Set<String> seen = new HashSet<>();
        List<String> sorted = new ArrayList<>(new TreeSet<>(tickers));
        for (List<String> chunk : chunks(sorted, 900)) {
            jdbc.query("SELECT ticker, is_fno FROM stock_universe WHERE ticker IN (" + marks(chunk.size()) + ")", (ResultSet rs) -> {
                String t = rs.getString(1).toUpperCase(Locale.ROOT);
                if (!seen.add(t)) return;                        // drop_duplicates("ticker"), first wins
                int f = rs.getInt(2);
                if (!rs.wasNull() && f == 1) out.add(t);
            }, chunk.toArray());
        }
        return out;
    }

    static double dbl(ResultSet rs, int col) throws java.sql.SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? NaN : v;
    }

    static String marks(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(i == 0 ? "?" : ",?");
        return sb.toString();
    }

    static Object[] params(List<String> list, Object last) {
        Object[] out = new Object[list.size() + 1];
        for (int i = 0; i < list.size(); i++) out[i] = list.get(i);
        out[list.size()] = last;
        return out;
    }

    static <T> List<List<T>> chunks(List<T> list, int size) {
        List<List<T>> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) out.add(list.subList(i, Math.min(list.size(), i + size)));
        return out;
    }

    // =====================================================================================================
    // watchlists.properties
    // =====================================================================================================

    /** dashboard_db.watchlist_groups: ticker -> first 101+ section (sections in name order). */
    static Map<String, String> watchlistGroups(File file) throws IOException {
        Map<String, String> out = new HashMap<>();
        if (!file.isFile()) return out;
        TreeMap<String, List<String>> sections = new TreeMap<>();
        String current = null;
        Pattern head = Pattern.compile("^\\[([^\\]]+)\\]");
        for (String line : new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).split("\\r?\\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            Matcher m = head.matcher(line);
            if (m.find()) {
                current = m.group(1).trim();
                sections.computeIfAbsent(current, k -> new ArrayList<>());
                continue;
            }
            if (current != null && line.toLowerCase(Locale.ROOT).startsWith("tickers")) {
                int eq = line.indexOf('=');
                String value = eq < 0 ? "" : line.substring(eq + 1);
                for (String t : value.split(",")) if (!t.trim().isEmpty()) sections.get(current).add(t.trim().toUpperCase(Locale.ROOT));
            }
        }
        Pattern code = Pattern.compile("^(\\d+)_");
        for (Map.Entry<String, List<String>> e : sections.entrySet()) {
            Matcher m = code.matcher(e.getKey());
            if (m.find() && Integer.parseInt(m.group(1)) >= 101)
                for (String t : e.getValue()) out.putIfAbsent(t, e.getKey());
        }
        return out;
    }
}
