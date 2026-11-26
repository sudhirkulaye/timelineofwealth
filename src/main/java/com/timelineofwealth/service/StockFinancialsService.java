package com.timelineofwealth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Annual and quarterly revenue / EBITDA / net profit / EPS for the stock page.
 *
 * Source: the ticker's latest result workbook QuarterResultsScreenerExcels/{yyyyQn}/{TICKER}_FY{yyQn}.xlsx,
 * sheet "HistoricalData", blocks "Annual Income Statement" and "Quarterly Income Statement". Blocks are found by
 * their title in column B, the period header by "FIN Years" / "Quarters" in column C (labels FY15.. / FY24Q3..),
 * and rows by their label in column C (or D for the margin sub-rows), so the layout may move.
 * Only actual periods are returned: the period has ended, Revenue is non-zero and the result date (if any) is not
 * in the future; projected columns (FY27, FY27Q3...) are dropped.
 * The workbooks are the only source: the stock_pnl / stock_quarter tables are no longer maintained.
 */
@Service
public class StockFinancialsService {
    private static final Logger logger = LoggerFactory.getLogger(StockFinancialsService.class);
    private static final String SHEET = "HistoricalData";
    private static final String ANNUAL_BLOCK = "Annual Income Statement", QUARTER_BLOCK = "Quarterly Income Statement";
    /** JSON key -> row label in the income statement blocks */
    private static final String[][] ROWS = {
            {"ebitda", "EBITDA"}, {"ebitdaMargin", "Operating Profit Margin%"},
            {"np", "Net Income"}, {"patMargin", "PAT Margin%"}, {"shares", "Wt. Avg. Shares"},
            {"eps", "EPS (Diluted)"}};
    private static final String[] KEYS = {"d", "label", "sales", "ebitda", "ebitdaMargin", "np", "patMargin", "eps", "resultDate"};

    private final CommonService commonService;
    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    public StockFinancialsService(CommonService commonService) {
        this.commonService = commonService;
    }

    /**
     * {source: "excel"|"none", file, annual: {d, label, sales, ebitda, ebitdaMargin, np, patMargin, eps, resultDate},
     *  quarterly: {same keys}, bfsi?: [...], bfsiHasNii?: true|false}
     * d = period end (yyyy-MM-dd), label = FY26 / FY27Q1; values in Rs crore, margins as fractions, EPS in Rs.
     * eps is the workbook's "EPS (Diluted)", or null where it is 0 or differs from Net Income / Wt. Avg. Shares
     * by more than 25% (the template's annual EPS row divides the wrong Data Sheet row in some workbooks).
     */
    public Map<String, Object> getFinancials(String ticker) {
        File file = commonService.getLatestExcelFileForTicker(ticker);
        if (file != null) {
            String key = file.getAbsolutePath() + "@" + file.lastModified();
            Map<String, Object> hit = cache.get(key);
            if (hit != null) return hit;
            try {
                Map<String, Object> out = fromExcel(file);
                cache.put(key, out);
                return out;
            } catch (Exception e) {
                logger.error("Could not read financials from {}", file, e);
            }
        }
        return none();
    }

    private Map<String, Object> fromExcel(File file) throws Exception {
        try (FileInputStream in = new FileInputStream(file); Workbook wb = new XSSFWorkbook(in)) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("source", "excel");
            out.put("file", file.getName());
            // Evaluate formulas here: workbooks refreshed by pasting a new Screener "Data Sheet" are often saved
            // without recalculated values, so the cached results in HistoricalData can be stale or empty.
            FormulaEvaluator ev = wb.getCreationHelper().createFormulaEvaluator();
            ev.setIgnoreMissingWorkbooks(true);
            Sheet hd = wb.getSheet(SHEET);
            if (hd == null) logger.warn("{} has no {} sheet", file.getName(), SHEET);
            out.put("annual", readIncomeBlock(hd, ANNUAL_BLOCK, ev));
            out.put("quarterly", readIncomeBlock(hd, QUARTER_BLOCK, ev));
            List<Map<String, Object>> bfsi = readBfsi(wb.getSheet("ModelBFSI"), ev);
            if (bfsi != null) {
                out.put("bfsi", bfsi);
                out.put("bfsiHasNii", hasNii(bfsi));
            }
            return out;
        }
    }

    // ---------- HistoricalData income statement blocks ----------
    private static Map<String, List<Object>> emptySeries() {
        Map<String, List<Object>> s = new LinkedHashMap<>();
        for (String k : KEYS) s.put(k, new ArrayList<>());
        return s;
    }

    private Map<String, List<Object>> readIncomeBlock(Sheet sheet, String title, FormulaEvaluator ev) {
        Map<String, List<Object>> s = emptySeries();
        if (sheet == null) return s;
        int last = sheet.getLastRowNum();
        // block title in column B
        int r0 = -1;
        for (int r = 0; r <= last && r0 < 0; r++) {
            String t = text(cell(sheet, r, 1), ev);
            if (t != null && t.trim().startsWith(title)) r0 = r;
        }
        if (r0 < 0) { logger.warn("{}: block '{}' not found", SHEET, title); return s; }
        int hdr = -1;
        for (int r = r0; r <= Math.min(last, r0 + 6) && hdr < 0; r++) {
            String t = text(cell(sheet, r, 2), ev);
            if (t != null && (t.trim().startsWith("FIN Year") || t.trim().startsWith("Quarter"))) hdr = r;
        }
        if (hdr < 0) { logger.warn("{}: no period header under '{}'", SHEET, title); return s; }
        // the block ends at the next text in column B (the company-name row above the next block)
        int end = last + 1;
        for (int r = hdr + 1; r <= last; r++) {
            String t = text(cell(sheet, r, 1), ev);
            if (t != null && !t.trim().isEmpty()) { end = r; break; }
        }
        // row label -> row index (first match; main rows in column C, margin sub-rows in column D)
        Map<String, Integer> rowOf = new HashMap<>();
        for (int r = hdr + 1; r < end; r++) {
            for (int c = 2; c <= 3; c++) {
                String t = text(cell(sheet, r, c), ev);
                if (t != null && !t.trim().isEmpty()) { rowOf.putIfAbsent(t.trim(), r); break; }
            }
        }
        Integer revRow = rowOf.get("Revenue"), dateRow = rowOf.get("Result Date");
        if (revRow == null) { logger.warn("{}: no Revenue row under '{}'", SHEET, title); return s; }

        Date today = new Date();
        Row hr = sheet.getRow(hdr);
        for (int col = 3; hr != null && col < hr.getLastCellNum(); col++) {
            String label = text(hr.getCell(col), ev);
            Date pe = periodEnd(label);
            if (pe == null || pe.after(today)) continue;                    // blank, "TTM" or projected column
            Double sales = num(cell(sheet, revRow, col), ev);
            if (sales == null || sales == 0) continue;                       // period without data
            Date rd = dateRow == null ? null : date(cell(sheet, dateRow, col), ev);
            if (rd != null && rd.after(today)) continue;                     // results not declared yet
            Map<String, Double> v = new HashMap<>();
            for (String[] kr : ROWS) {
                Integer r = rowOf.get(kr[1]);
                v.put(kr[0], r == null ? null : num(cell(sheet, r, col), ev));
            }
            s.get("d").add(iso(pe));
            s.get("label").add(label.trim());
            s.get("sales").add(sales);
            s.get("ebitda").add(v.get("ebitda"));
            s.get("ebitdaMargin").add(v.get("ebitdaMargin"));
            s.get("np").add(v.get("np"));
            s.get("patMargin").add(v.get("patMargin"));
            s.get("eps").add(saneEps(v.get("eps"), v.get("np"), v.get("shares")));
            s.get("resultDate").add(rd == null ? null : iso(rd));
        }
        return s;
    }

    /** Reported EPS, or null when it is missing / 0 or far from Net Income / Wt. Avg. Shares. */
    private static Double saneEps(Double eps, Double np, Double shares) {
        if (eps == null || eps == 0 || Double.isNaN(eps) || Double.isInfinite(eps)) return null;
        if (np != null && shares != null && shares > 0 && np != 0) {
            double calc = np / shares;
            if (Math.abs(eps - calc) > 0.25 * Math.abs(calc)) return null;
        }
        return eps;
    }

    // ---------- ModelBFSI (banks, NBFCs, AMCs...) ----------
    private static final String[] BFSI_BLOCKS = {
            "Annual Income Expense History", "Annual Banking History", "Annual Loan Distribution History",
            "Quarterly Income Expense History", "Quarterly Banking History", "Quarterly Loan Distribution History"};

    /**
     * History blocks of the ModelBFSI sheet, actual periods only (period ended, Net Interest Income reported, or for
     * financials without NII a reported consolidated profit); rows with nothing reported are dropped:
     * [{block, period: "A"|"Q", labels: ["FY25", ...], ends: ["2025-03-31", ...], rows: [{label, values: [...]}, ...]}].
     * Rows under "Contribution" / "Growth" sub-headers get that prefix. Returns null when the sheet holds no history
     * The profit / EPS / price rows are formulas filled for every company, so the page decides by sector
     * (financials) or bfsiHasNii whether to use this view; non-financials keep the HistoricalData P&L.
     */
    private List<Map<String, Object>> readBfsi(Sheet sheet, FormulaEvaluator ev) {
        if (sheet == null) return null;
        // locate blocks by their title (column B)
        Map<String, Integer> start = new LinkedHashMap<>();
        for (int r = 0; r <= Math.min(sheet.getLastRowNum(), 400); r++) {
            String t = text(cell(sheet, r, 1), ev);
            if (t == null) continue;
            for (String b : BFSI_BLOCKS) if (t.trim().startsWith(b.substring(0, Math.min(b.length(), 28))) && !start.containsKey(b)) start.put(b, r);
        }
        if (start.isEmpty()) return null;
        List<Integer> starts = new ArrayList<>(start.values());
        Collections.sort(starts);

        Date today = new Date();
        Map<String, Set<Integer>> keepByPeriod = new HashMap<>();
        List<Map<String, Object>> blocks = new ArrayList<>();
        for (String b : BFSI_BLOCKS) {
            Integer r0 = start.get(b);
            if (r0 == null) continue;
            int next = sheet.getLastRowNum() + 1;
            for (int s : starts) if (s > r0) { next = s; break; }
            String period = b.startsWith("Annual") ? "A" : "Q";

            // header row: "FIN Years" / "Quarters" in column C, period labels from column D onwards
            int hdr = -1;
            for (int r = r0; r < Math.min(next, r0 + 6); r++) {
                String t = text(cell(sheet, r, 2), ev);
                if (t != null && (t.startsWith("FIN Year") || t.startsWith("Quarter"))) { hdr = r; break; }
            }
            if (hdr < 0) continue;
            // the block also ends at the next text in column B (company-name row above the next block), so the
            // last history block does not run into the projection blocks further down the sheet
            for (int r = hdr + 1; r < next; r++) {
                String t = text(cell(sheet, r, 1), ev);
                if (t != null && !t.trim().isEmpty()) { next = r; break; }
            }
            List<Integer> cols = new ArrayList<>();
            List<String> labels = new ArrayList<>(), ends = new ArrayList<>();
            Row hr = sheet.getRow(hdr);
            for (int c = 5; hr != null && c < hr.getLastCellNum(); c++) {
                String l = text(hr.getCell(c), ev);
                Date end = periodEnd(l);
                if (end == null || end.after(today)) continue;
                cols.add(c); labels.add(l); ends.add(iso(end));
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            String prefix = "";
            for (int r = hdr + 1; r < next; r++) {
                String label = text(cell(sheet, r, 2), ev);      // main rows in column C, sub-rows in column D
                if (label == null || label.trim().isEmpty()) label = text(cell(sheet, r, 3), ev);
                if (label == null || label.trim().isEmpty()) continue;
                label = label.trim();
                List<Double> vals = new ArrayList<>();
                boolean any = false;
                for (int c : cols) { Double v = num(cell(sheet, r, c), ev); vals.add(v); any |= v != null; }
                if (!any) {   // a sub-header such as "Contribution" or "Growth"
                    if (label.equals("Contribution") || label.equals("Growth")) prefix = label + ": ";
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("label", prefix + label);
                row.put("values", vals);
                rows.add(row);
            }

            // actual periods = those where the income block reports Net Interest Income; financials without NII
            // (insurers, AMCs, brokers) fall back to the periods with a reported consolidated / standalone profit
            if (b.contains("Income Expense")) {
                Set<Integer> keep = nonZero(rows, "Net Interest Income");
                if (keep.isEmpty()) keep = nonZero(rows, "Consolidated Profit");
                if (keep.isEmpty()) keep = nonZero(rows, "Standalone Profit");
                keepByPeriod.put(period, keep);
            }
            Map<String, Object> blk = new LinkedHashMap<>();
            blk.put("block", b);
            blk.put("period", period);
            blk.put("labels", labels);
            blk.put("ends", ends);
            blk.put("rows", rows);
            blocks.add(blk);
        }

        Set<Integer> annualKeep = keepByPeriod.getOrDefault("A", Collections.emptySet());
        if (annualKeep.size() < 2) return null;
        // trim every block to its actual periods
        for (Map<String, Object> blk : blocks) {
            Set<Integer> keep = keepByPeriod.getOrDefault(blk.get("period"), Collections.emptySet());
            blk.put("labels", pick(blk.get("labels"), keep));
            blk.put("ends", pick(blk.get("ends"), keep));
            @SuppressWarnings("unchecked") List<Map<String, Object>> rows = (List<Map<String, Object>>) blk.get("rows");
            for (Map<String, Object> row : rows) row.put("values", pick(row.get("values"), keep));
            // drop rows with nothing reported (template rows left at 0 / blank)
            rows.removeIf(row -> {
                @SuppressWarnings("unchecked") List<Double> v = (List<Double>) row.get("values");
                return v.stream().noneMatch(x -> x != null && x != 0);
            });
        }
        return blocks;
    }

    /** indices of the periods where the first row whose label starts with {@code label} is non-zero */
    @SuppressWarnings("unchecked")
    private static Set<Integer> nonZero(List<Map<String, Object>> rows, String label) {
        Set<Integer> keep = new HashSet<>();
        for (Map<String, Object> row : rows) {
            if (!String.valueOf(row.get("label")).startsWith(label)) continue;
            List<Double> v = (List<Double>) row.get("values");
            for (int i = 0; i < v.size(); i++) if (v.get(i) != null && v.get(i) != 0) keep.add(i);
            break;
        }
        return keep;
    }

    /** true when the annual income block of ModelBFSI reports Net Interest Income (lenders) */
    @SuppressWarnings("unchecked")
    private static boolean hasNii(List<Map<String, Object>> bfsi) {
        for (Map<String, Object> blk : bfsi) {
            if (!"A".equals(blk.get("period")) || !String.valueOf(blk.get("block")).contains("Income Expense")) continue;
            for (Map<String, Object> row : (List<Map<String, Object>>) blk.get("rows"))
                if (String.valueOf(row.get("label")).startsWith("Net Interest Income")) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> pick(Object list, Set<Integer> keep) {
        List<T> in = (List<T>) list, out = new ArrayList<>();
        for (int i = 0; i < in.size(); i++) if (keep.contains(i)) out.add(in.get(i));
        return out;
    }

    /** FY26 -> 31 Mar 2026; FY27Q2 -> 30 Sep 2026 (Indian fiscal year April-March). */
    static Date periodEnd(String label) {
        if (label == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^FY(\\d{2})(?:Q([1-4]))?$").matcher(label.trim());
        if (!m.matches()) return null;
        int fy = 2000 + Integer.parseInt(m.group(1));
        Calendar cal = Calendar.getInstance();
        cal.clear();
        if (m.group(2) == null) cal.set(fy, Calendar.MARCH, 31);
        else {
            int q = Integer.parseInt(m.group(2));
            int[] month = {0, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.DECEMBER, Calendar.MARCH};
            int[] day = {0, 30, 30, 31, 31};
            cal.set(q == 4 ? fy : fy - 1, month[q], day[q]);
        }
        return cal.getTime();
    }

    private static String text(Cell c, FormulaEvaluator ev) {
        Object v = value(c, ev);
        return v instanceof String ? (String) v : null;
    }

    private static Map<String, Object> none() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", "none");
        out.put("annual", emptySeries());
        out.put("quarterly", emptySeries());
        return out;
    }

    // ---------- cell helpers ----------
    private static Cell cell(Sheet sheet, int row, int col) {
        Row r = sheet.getRow(row);
        return r == null ? null : r.getCell(col);
    }

    /** Cell value as a number or string, evaluating formulas; falls back to the cached result if evaluation fails. */
    private static Object value(Cell c, FormulaEvaluator ev) {
        if (c == null) return null;
        int t = c.getCellType();
        if (t == Cell.CELL_TYPE_FORMULA) {
            try {
                CellValue v = ev.evaluate(c);
                if (v == null) return null;
                if (v.getCellType() == Cell.CELL_TYPE_NUMERIC) return v.getNumberValue();
                if (v.getCellType() == Cell.CELL_TYPE_STRING) return v.getStringValue();
                return null;
            } catch (Exception e) {
                t = c.getCachedFormulaResultType();
            }
        }
        try {
            if (t == Cell.CELL_TYPE_NUMERIC) return c.getNumericCellValue();
            if (t == Cell.CELL_TYPE_STRING) return c.getStringCellValue();
        } catch (Exception ignored) { /* unreadable cell */ }
        return null;
    }

    private static Double num(Cell c, FormulaEvaluator ev) {
        Object v = value(c, ev);
        if (v instanceof Double) return (Double) v;
        if (v instanceof String) {
            String s = ((String) v).trim().replace(",", "");
            try { return s.isEmpty() ? null : Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    /** Date cells: a date serial (formatted or not) or yyyy-MM-dd text. */
    private static Date date(Cell c, FormulaEvaluator ev) {
        Object v = value(c, ev);
        if (v instanceof Double) {
            double d = (Double) v;
            if (d > 20000 && d < 80000) return DateUtil.getJavaDate(d);
        } else if (v instanceof String) {
            try { return java.sql.Date.valueOf(((String) v).trim()); } catch (Exception ignored) { /* "TTM" etc. */ }
        }
        return null;
    }

    private static String iso(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

}
