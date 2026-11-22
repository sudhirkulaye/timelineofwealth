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
 * Annual and quarterly revenue / EBITDA / net profit for the stock page.
 *
 * Source 1 (preferred): the ticker's latest result workbook
 *   QuarterResultsScreenerExcels/{yyyyQn}/{TICKER}_FY{yyQn}.xlsx, sheets "AnnualResults" and "QuarterP&L"
 *   (Screener.in layout: row 3 dates, 4 sales, 6 operating profit, 12 net profit; quarter row 15 result date).
 * The workbooks are the only source: the stock_pnl / stock_quarter tables are no longer maintained.
 */
@Service
public class StockFinancialsService {
    private static final Logger logger = LoggerFactory.getLogger(StockFinancialsService.class);
    private static final int ROW_DATE = 2, ROW_SALES = 3, ROW_EBITDA = 5, ROW_NET_PROFIT = 11, ROW_RESULT_DATE = 14;

    private final CommonService commonService;
    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    public StockFinancialsService(CommonService commonService) {
        this.commonService = commonService;
    }

    /**
     * {source: "excel"|"none", file, annual: {d:[], sales:[], ebitda:[], np:[]}, quarterly: {d:[], sales:[], ebitda:[], np:[], resultDate:[]}}
     * Dates are period ends (yyyy-MM-dd); values in Rs crore.
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
            // without recalculated values, so the cached results in AnnualResults / QuarterP&L can be stale or empty.
            FormulaEvaluator ev = wb.getCreationHelper().createFormulaEvaluator();
            ev.setIgnoreMissingWorkbooks(true);
            out.put("annual", readSheet(wb.getSheet("AnnualResults"), false, ev));
            out.put("quarterly", readSheet(wb.getSheet("QuarterP&L"), true, ev));
            List<Map<String, Object>> bfsi = readBfsi(wb.getSheet("ModelBFSI"), ev);
            if (bfsi != null) out.put("bfsi", bfsi);
            return out;
        }
    }

    // ---------- ModelBFSI (banks, NBFCs, AMCs...) ----------
    private static final String[] BFSI_BLOCKS = {
            "Annual Income Expense History", "Annual Banking History", "Annual Loan Distribution History",
            "Quarterly Income Expense History", "Quarterly Banking History", "Quarterly Loan Distribution History"};

    /**
     * History blocks of the ModelBFSI sheet, actual periods only (period ended, Net Interest Income reported):
     * [{block, period: "A"|"Q", labels: ["FY25", ...], ends: ["2025-03-31", ...], rows: [{label, values: [...]}, ...]}].
     * Rows under "Contribution" / "Growth" sub-headers get that prefix. Returns null when the sheet holds no history
     * (the template exists in every workbook, but only lenders and other financials fill it in).
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

            // actual periods = those where the income block reports Net Interest Income
            if (b.contains("Income Expense")) {
                Set<Integer> keep = new HashSet<>();
                for (Map<String, Object> row : rows) {
                    if (String.valueOf(row.get("label")).startsWith("Net Interest Income")) {
                        @SuppressWarnings("unchecked") List<Double> v = (List<Double>) row.get("values");
                        for (int i = 0; i < v.size(); i++) if (v.get(i) != null && v.get(i) != 0) keep.add(i);
                    }
                }
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
        }
        return blocks;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> pick(Object list, Set<Integer> keep) {
        List<T> in = (List<T>) list, out = new ArrayList<>();
        for (int i = 0; i < in.size(); i++) if (keep.contains(i)) out.add(in.get(i));
        return out;
    }

    /** FY26 -> 31 Mar 2026; FY27Q2 -> 30 Sep 2026 (Indian fiscal year April-March). */
    private static Date periodEnd(String label) {
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

    private Map<String, List<Object>> readSheet(Sheet sheet, boolean quarterly, FormulaEvaluator ev) {
        Map<String, List<Object>> s = new LinkedHashMap<>();
        for (String k : quarterly ? new String[]{"d", "sales", "ebitda", "np", "resultDate"} : new String[]{"d", "sales", "ebitda", "np"}) {
            s.put(k, new ArrayList<>());
        }
        if (sheet == null) return s;
        Row dates = sheet.getRow(ROW_DATE);
        if (dates == null) return s;
        Date today = new Date();
        for (int col = 1; col <= dates.getLastCellNum(); col++) {
            Date d = date(dates.getCell(col), ev);
            if (d == null || d.after(today)) continue;     // skips "TTM", blank and projected columns
            Double sales = num(cell(sheet, ROW_SALES, col), ev);
            if (sales == null || sales == 0) continue;     // year/quarter without data
            Date r = quarterly ? date(cell(sheet, ROW_RESULT_DATE, col), ev) : null;
            if (quarterly && r != null && r.after(today)) continue;   // quarter ended but results not declared yet
            s.get("d").add(iso(d));
            s.get("sales").add(sales);
            s.get("ebitda").add(num(cell(sheet, ROW_EBITDA, col), ev));
            s.get("np").add(num(cell(sheet, ROW_NET_PROFIT, col), ev));
            if (quarterly) s.get("resultDate").add(r == null ? null : iso(r));
        }
        return s;
    }

    private static Map<String, Object> none() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", "none");
        Map<String, List<Object>> a = new LinkedHashMap<>(), q = new LinkedHashMap<>();
        for (String k : new String[]{"d", "sales", "ebitda", "np"}) { a.put(k, new ArrayList<>()); q.put(k, new ArrayList<>()); }
        q.put("resultDate", new ArrayList<>());
        out.put("annual", a);
        out.put("quarterly", q);
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
