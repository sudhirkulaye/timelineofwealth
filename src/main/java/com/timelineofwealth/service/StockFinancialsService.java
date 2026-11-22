package com.timelineofwealth.service;

import com.timelineofwealth.entities.StockPnl;
import com.timelineofwealth.entities.StockQuarter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Annual and quarterly revenue / EBITDA / net profit for the stock page.
 *
 * Source 1 (preferred): the ticker's latest result workbook
 *   QuarterResultsScreenerExcels/{yyyyQn}/{TICKER}_FY{yyQn}.xlsx, sheets "AnnualResults" and "QuarterP&L"
 *   (Screener.in layout: row 3 dates, 4 sales, 6 operating profit, 12 net profit; quarter row 15 result date).
 * Source 2 (fallback): MySQL stock_pnl / stock_quarter, which are loaded less often.
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
     * {source: "excel"|"db"|"none", file, annual: {d:[], sales:[], ebitda:[], np:[]}, quarterly: {d:[], sales:[], ebitda:[], np:[], resultDate:[]}}
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
        return fromDb(ticker);
    }

    private Map<String, Object> fromExcel(File file) throws Exception {
        try (FileInputStream in = new FileInputStream(file); Workbook wb = new XSSFWorkbook(in)) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("source", "excel");
            out.put("file", file.getName());
            out.put("annual", readSheet(wb.getSheet("AnnualResults"), false));
            out.put("quarterly", readSheet(wb.getSheet("QuarterP&L"), true));
            return out;
        }
    }

    private Map<String, List<Object>> readSheet(Sheet sheet, boolean quarterly) {
        Map<String, List<Object>> s = new LinkedHashMap<>();
        for (String k : quarterly ? new String[]{"d", "sales", "ebitda", "np", "resultDate"} : new String[]{"d", "sales", "ebitda", "np"}) {
            s.put(k, new ArrayList<>());
        }
        if (sheet == null) return s;
        Row dates = sheet.getRow(ROW_DATE);
        if (dates == null) return s;
        Date today = new Date();
        for (int col = 1; col <= dates.getLastCellNum(); col++) {
            Date d = date(dates.getCell(col));
            if (d == null || d.after(today)) continue;     // skips "TTM", blank and projected columns
            Double sales = num(cell(sheet, ROW_SALES, col));
            if (sales == null || sales == 0) continue;     // year/quarter without data
            Date r = quarterly ? date(cell(sheet, ROW_RESULT_DATE, col)) : null;
            if (quarterly && r != null && r.after(today)) continue;   // quarter ended but results not declared yet
            s.get("d").add(iso(d));
            s.get("sales").add(sales);
            s.get("ebitda").add(num(cell(sheet, ROW_EBITDA, col)));
            s.get("np").add(num(cell(sheet, ROW_NET_PROFIT, col)));
            if (quarterly) s.get("resultDate").add(r == null ? null : iso(r));
        }
        return s;
    }

    private Map<String, Object> fromDb(String ticker) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<StockPnl> pnl = commonService.getStockPnl(ticker);
        List<StockQuarter> qtr = commonService.getStockQuarter(ticker);
        out.put("source", pnl.isEmpty() && qtr.isEmpty() ? "none" : "db");
        Map<String, List<Object>> a = new LinkedHashMap<>(), q = new LinkedHashMap<>();
        for (String k : new String[]{"d", "sales", "ebitda", "np"}) { a.put(k, new ArrayList<>()); q.put(k, new ArrayList<>()); }
        q.put("resultDate", new ArrayList<>());
        pnl.sort(Comparator.comparing(p -> p.getKey().getDate()));
        for (StockPnl p : pnl) {
            a.get("d").add(p.getKey().getDate().toString());
            a.get("sales").add(dbl(p.getSales()));
            a.get("ebitda").add(dbl(p.getOperatingProfit()));
            a.get("np").add(dbl(p.getNetProfit()));
        }
        qtr.sort(Comparator.comparing(p -> p.getKey().getDate()));
        for (StockQuarter p : qtr) {
            q.get("d").add(p.getKey().getDate().toString());
            q.get("sales").add(dbl(p.getSales()));
            q.get("ebitda").add(dbl(p.getOperatingProfit()));
            q.get("np").add(dbl(p.getNetProfit()));
            q.get("resultDate").add(p.getResultDate() == null ? null : p.getResultDate().toString());
        }
        out.put("annual", a);
        out.put("quarterly", q);
        return out;
    }

    // ---------- cell helpers ----------
    private static Cell cell(Sheet sheet, int row, int col) {
        Row r = sheet.getRow(row);
        return r == null ? null : r.getCell(col);
    }

    private static int type(Cell c) {
        return c.getCellType() == Cell.CELL_TYPE_FORMULA ? c.getCachedFormulaResultType() : c.getCellType();
    }

    private static Double num(Cell c) {
        if (c == null) return null;
        try {
            int t = type(c);
            if (t == Cell.CELL_TYPE_NUMERIC) return c.getNumericCellValue();
            if (t == Cell.CELL_TYPE_STRING) {
                String s = c.getStringCellValue().trim().replace(",", "");
                return s.isEmpty() ? null : Double.parseDouble(s);
            }
        } catch (Exception ignored) { /* text such as "NA" */ }
        return null;
    }

    private static Date date(Cell c) {
        if (c == null) return null;
        try {
            int t = type(c);
            if (t == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(c)) return c.getDateCellValue();
            if (t == Cell.CELL_TYPE_NUMERIC) {
                double v = c.getNumericCellValue();
                if (v > 20000 && v < 80000) return DateUtil.getJavaDate(v);   // a date serial without date format
            }
            if (t == Cell.CELL_TYPE_STRING) return java.sql.Date.valueOf(c.getStringCellValue().trim());
        } catch (Exception ignored) { /* not a date */ }
        return null;
    }

    private static String iso(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    private static Double dbl(BigDecimal b) { return b == null ? null : b.doubleValue(); }
}
