package com.timelineofwealth.service.consensus;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Latest verified broker view per ticker and broker from the AnalystsReco sheet of AnalystViewTracker.xlsx.
 * Port of weekly_lib.load.load_analyst_reco + calc.broker_views(...)["latest"] + dashboard_db.broker_rows.
 * Only rows whose Ticker cell has the yellow (FFFFFF00) solid fill count: they are entered and cross-checked.
 */
final class YcBrokerViews {
    static final List<String> BROKERS = Arrays.asList("MOSL", "AMBIT", "PL", "AXIS");
    static final int MAX_AGE_DAYS = 270;
    private static final String VERIFIED = "FFFFFF00";
    private static final Pattern QUARTER = Pattern.compile("^FY(\\d{2})Q([1-4])");
    private static final Pattern MONTH_DAY = Pattern.compile("(\\d{1,2})[-./](\\d{1,2})\\s*(?:00:00:00)?\\s*$");

    /** one verified AnalystsReco row */
    static final class Reco {
        String ticker, broker, reco, quarter, fwdMultiple;
        double target;
        LocalDateTime date;     // repaired report date; null when it could not be read
        int sheetRow;
    }

    /** a consensus_broker_view row */
    static final class View {
        String ticker, broker, reco, quarter, fwdMultiple;
        Double target;
        LocalDate reportDate;
    }

    /** Stats shown on the page: rows read, verified, repaired dates. */
    final Map<String, Integer> stats = new LinkedHashMap<>();
    final List<String> notes = new ArrayList<>();

    List<Reco> read(File xlsx, LocalDate today) throws Exception {
        Path tmp = Files.createTempFile("yc_", ".xlsx");      // Excel may have the file open
        Files.copy(xlsx.toPath(), tmp, StandardCopyOption.REPLACE_EXISTING);
        List<Reco> out = new ArrayList<>();
        try (InputStream in = Files.newInputStream(tmp); Workbook wb = new XSSFWorkbook(in)) {
            Sheet ws = wb.getSheet("AnalystsReco");
            if (ws == null) throw new IllegalStateException("No AnalystsReco sheet in " + xlsx);
            Row hr = ws.getRow(0);
            Map<String, Integer> col = new HashMap<>();
            for (int c = 0; c < hr.getLastCellNum(); c++) {
                Object v = value(hr.getCell(c));
                if (v != null && !col.containsKey(v.toString())) col.put(v.toString(), c);
            }
            int cT = col.getOrDefault("Ticker", -1), cQ = col.getOrDefault("Quarter", -1), cD = col.getOrDefault("Date", -1),
                    cB = col.getOrDefault("Broker", -1), cR = col.getOrDefault("Reco", -1), cTg = col.getOrDefault("Target", -1),
                    cF = col.getOrDefault("Fwd Multiple", -1);
            int rows = 0, verified = 0, fixed = 0, unresolved = 0;
            for (int r = 1; r <= ws.getLastRowNum(); r++) {
                Row row = ws.getRow(r);
                if (row == null) continue;
                Cell first = row.getCell(0);
                if (value(first) == null) continue;
                rows++;
                if (!isVerified(first)) continue;
                verified++;
                Object tk = at(row, cT), br = at(row, cB);
                if (tk == null || br == null) continue;
                Reco x = new Reco();
                x.sheetRow = r + 1;
                x.ticker = pyStr(tk).trim().toUpperCase(Locale.ROOT);
                x.broker = pyStr(br).trim().toUpperCase(Locale.ROOT);
                String reco = title(pyStr(at(row, cR)).trim());
                x.reco = "Nan".equals(reco) ? null : reco;
                x.target = targetValue(at(row, cTg));
                x.fwdMultiple = fwdMultiple(at(row, cF));
                Object q = at(row, cQ);
                x.quarter = q == null ? null : pyStr(q).trim();
                Object[] fix = fixReportDate(at(row, cD), q == null ? "None" : pyStr(q), today);
                x.date = (LocalDateTime) fix[0];
                if ("fixed".equals(fix[1])) fixed++;
                if ("unresolved".equals(fix[1])) unresolved++;
                out.add(x);
            }
            stats.put("rows", rows);
            stats.put("verified", verified);
            stats.put("datesRepaired", fixed);
            stats.put("datesUnresolved", unresolved);
        } finally {
            Files.deleteIfExists(tmp);
        }
        return out;
    }

    /** calc.broker_views(...)["latest"] then dashboard_db.broker_rows (ticker mapping, MOSL/AMBIT/PL/AXIS only). */
    static List<View> latest(List<Reco> all, LocalDate today, Map<String, String> tickerMap) {
        LocalDateTime limit = today.plusDays(1).atStartOfDay();
        LocalDateTime oldest = today.minusDays(MAX_AGE_DAYS).atStartOfDay();
        List<Reco> r = new ArrayList<>();
        for (Reco x : all) if (x.date != null && !x.date.isAfter(limit)) r.add(x);
        // stable sort on (Ticker, Broker, Date); the last row of each (Ticker, Broker) is its latest view
        r.sort(Comparator.comparing((Reco x) -> x.ticker).thenComparing(x -> x.broker).thenComparing(x -> x.date));
        Map<String, Reco> last = new LinkedHashMap<>();
        for (Reco x : r) last.put(x.ticker + "|" + x.broker, x);
        List<Reco> kept = new ArrayList<>();
        for (Reco x : last.values()) if (!x.date.isBefore(oldest) && BROKERS.contains(x.broker)) kept.add(x);
        // map tickers, then keep the later report when two sheet tickers map to the same stock
        kept.sort(Comparator.comparing((Reco x) -> x.date));
        Map<String, View> out = new LinkedHashMap<>();
        for (Reco x : kept) {
            View v = new View();
            v.ticker = tickerMap.getOrDefault(x.ticker, x.ticker);
            v.broker = x.broker;
            v.reco = x.reco == null ? null : x.reco.trim();
            v.target = Double.isNaN(x.target) ? null : x.target;
            v.reportDate = x.date.toLocalDate();
            v.quarter = x.quarter;
            v.fwdMultiple = x.fwdMultiple;
            out.put(v.ticker + "|" + v.broker, v);
        }
        return new ArrayList<>(out.values());
    }

    // ---------------------------------------------------- cell helpers (openpyxl, data_only=True)
    private static Object at(Row row, int c) { return c < 0 ? null : value(row.getCell(c)); }

    /** the value openpyxl would return: str, int, float, datetime, bool or None (formula cells: cached result) */
    static Object value(Cell c) {
        if (c == null) return null;
        CellType t = c.getCellTypeEnum() == CellType.FORMULA ? c.getCachedFormulaResultTypeEnum() : c.getCellTypeEnum();
        switch (t) {
            case STRING: {
                String s = c.getStringCellValue();
                return s == null || s.isEmpty() ? null : s;
            }
            case NUMERIC: {
                if (DateUtil.isCellDateFormatted(c)) {
                    Date d = c.getDateCellValue();
                    return d == null ? null : LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
                }
                double v = c.getNumericCellValue();
                if (v == Math.rint(v) && Math.abs(v) < 1e15) return (long) v;
                return v;
            }
            case BOOLEAN: return c.getBooleanCellValue();
            case ERROR: return FormulaError.forInt(c.getErrorCellValue()).getString();
            default: return null;
        }
    }

    /** yellow solid fill given as an explicit RGB (as openpyxl's fgColor.rgb) */
    static boolean isVerified(Cell c) {
        CellStyle st = c.getCellStyle();
        if (!(st instanceof XSSFCellStyle) || st.getFillPatternEnum() != FillPatternType.SOLID_FOREGROUND) return false;
        XSSFColor fg = ((XSSFCellStyle) st).getFillForegroundXSSFColor();
        if (fg == null || !fg.getCTColor().isSetRgb()) return false;
        byte[] rgb = fg.getCTColor().getRgb();
        StringBuilder sb = new StringBuilder(rgb.length == 3 ? "00" : "");
        for (byte b : rgb) sb.append(String.format("%02X", b & 0xff));
        return VERIFIED.equals(sb.toString());
    }

    /** Python str() of a cell value */
    static String pyStr(Object v) {
        if (v == null) return "None";
        if (v instanceof Double) {
            double d = (Double) v;
            return d == Math.rint(d) && Math.abs(d) < 1e16 ? String.valueOf((long) d) + ".0" : String.valueOf(d);
        }
        if (v instanceof LocalDateTime) return ((LocalDateTime) v).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (v instanceof Boolean) return ((Boolean) v) ? "True" : "False";
        return v.toString();
    }

    /** Python str.title() */
    static String title(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean prevCased = false;
        for (char ch : s.toCharArray()) {
            if (Character.isLetter(ch)) {
                sb.append(prevCased ? Character.toLowerCase(ch) : Character.toUpperCase(ch));
                prevCased = true;
            } else {
                sb.append(ch);
                prevCased = false;
            }
        }
        return sb.toString();
    }

    /** pd.to_numeric(errors="coerce"), 0 -> NaN (not filled in yet) */
    static double targetValue(Object v) {
        double d;
        if (v instanceof Number) d = ((Number) v).doubleValue();
        else if (v instanceof String) d = YcInputs.num((String) v);
        else d = Double.NaN;
        return d == 0 ? Double.NaN : d;
    }

    /** the valuation basis as typed ("18x Sep'27 EPS", "DCF", "SoTP"), whitespace collapsed; null when blank */
    static String fwdMultiple(Object v) {
        if (v == null) return null;
        String s = (v instanceof String ? (String) v : pyStr(v)).replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) return null;
        return s.length() > 100 ? s.substring(0, 100) : s;
    }

    static Integer expectedReportYear(String quarter) {
        Matcher m = QUARTER.matcher(quarter);
        if (!m.find()) return null;
        int fy = 2000 + Integer.parseInt(m.group(1)), q = Integer.parseInt(m.group(2));
        return q == 1 || q == 2 ? fy - 1 : fy;          // Q1/Q2 results come out in the previous calendar year
    }

    /** load._fix_report_date: {date or null, "ok" | "fixed" | "unresolved"} */
    static Object[] fixReportDate(Object raw, String quarter, LocalDate today) {
        Integer expected = expectedReportYear(quarter);
        LocalDateTime parsed = raw instanceof LocalDateTime ? (LocalDateTime) raw : parseDate(raw);
        LocalDateTime todayTs = today.atStartOfDay();
        if (parsed != null && (expected == null || parsed.getYear() == expected) && !parsed.isAfter(todayTs.plusDays(1)))
            return new Object[]{parsed, "ok"};
        int[] md = null;
        if (parsed != null) md = new int[]{parsed.getMonthValue(), parsed.getDayOfMonth()};
        else {
            Matcher m = MONTH_DAY.matcher(pyStr(raw));
            if (m.find()) md = new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        }
        if (md == null || expected == null)
            return new Object[]{parsed, parsed == null || parsed.isAfter(todayTs) ? "unresolved" : "ok"};
        LocalDateTime fixed;
        try {
            fixed = LocalDate.of(expected, md[0], md[1]).atStartOfDay();
        } catch (java.time.DateTimeException e) {
            return new Object[]{parsed, "unresolved"};
        }
        if (fixed.isAfter(todayTs)) return new Object[]{parsed, "unresolved"};
        return new Object[]{fixed, "fixed"};
    }

    /** pd.to_datetime(text, errors="coerce") for the plain date forms; anything else is NaT (null) */
    static LocalDateTime parseDate(Object raw) {
        if (!(raw instanceof String)) return null;
        String s = ((String) raw).trim();
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd"};
        for (String p : patterns) {
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern(p);
                return p.contains("HH") ? LocalDateTime.parse(s, f) : LocalDate.parse(s, f).atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
