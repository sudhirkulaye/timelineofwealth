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
            return out;
        }
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
