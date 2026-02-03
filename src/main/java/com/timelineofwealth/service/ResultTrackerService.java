package com.timelineofwealth.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ResultTrackerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String FILE_PATH =
            "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert\\ResultTracker.xlsx";

    public void updateResultTracker() {
        // Query 1: returns 3 rows
        String sql1 =
                "select 'INDEX', " +
                        "b.ticker, b.short_name, c.sector_name_display, c.industry_name_display, c.sub_industry_name_display, " +
                        "b.latest_price, " +
                        "0 AS last_result, " +
                        "0 AS dummy_col, " +
                        "(return_1D / 100), (return_1W / 100), (return_2W / 100), (return_1M / 100), (return_2M / 100), " +
                        "(return_3M / 100), (return_6M / 100), (return_9M / 100), (return_YTD / 100), (return_1Y / 100), " +
                        "(up_52w_min / 100), (down_52w_max / 100), (return_2Y / 100), (return_3Y / 100), " +
                        "`1w_min`, `1w_max`, `2w_min`, `2w_max`, `1m_min`, `1m_max`, `2m_min`, `2m_max`, " +
                        "`3m_min`, `3m_max`, `6m_min`, `6m_max` " +
                        "from stock_universe b, subindustry c, stock_price_movement d " +
                        "where b.ticker in ('NIFTYBEES', 'JUNIORBEES', 'BANKBEES') " +
                        "and b.subindustryid = c.subindustryid " +
                        "and (b.is_bse500 = 1 or b.is_nse500 = 1) " +
                        "and b.ticker = d.ticker";

        List<Map<String, Object>> top3Rows = jdbcTemplate.queryForList(sql1);

        // Query 2: returns 700+ rows
        String sql2 =
                "SELECT " +
                        "IF(is_sensex = 1, 'SENSEX', " +
                        "IF(is_nifty50 = 1, 'NIFTY', " +
                        "IF(is_nse100 = 1 OR is_bse100 = 1, 'NSE-BSE100', " +
                        "IF(is_nse200 = 1 OR is_bse200 = 1, 'NSE-BSE200', " +
                        "IF(is_nse500 = 1 OR is_bse500 = 1, 'NSE-BSE500', 'Other')" +
                        ")" +
                        ")" +
                        ")" +
                        ") AS index1, " +
                        "b.ticker, b.short_name, c.sector_name_display, c.industry_name_display, c.sub_industry_name_display, " +
                        "a.cmp, a.market_cap, last_result_date, " +
                        "(d.return_1D / 100), (d.return_1W / 100), (d.return_2W / 100), (d.return_1M / 100), (d.return_2M / 100), " +
                        "(d.return_3M / 100), (d.return_6M / 100), (d.return_9M / 100), (d.return_YTD / 100), (d.return_1Y / 100), " +
                        "(d.up_52w_min / 100), (d.down_52w_max / 100), (d.return_2Y / 100), (d.return_3Y / 100), " +
                        "`1w_min`, `1w_max`, `2w_min`, `2w_max`, `1m_min`, `1m_max`, `2m_min`, `2m_max`, " +
                        "`3m_min`, `3m_max`, `6m_min`, `6m_max` " +
                        "FROM daily_data_s a, stock_universe b, subindustry c, stock_price_movement d " +
                        "WHERE a.date = (SELECT date_today FROM setup_dates) " +
                        "AND a.name = b.ticker " +
                        "AND b.subindustryid = c.subindustryid " +
                        "AND b.ticker = d.ticker " +
                        "ORDER BY sector_name_display, industry_name_display, sub_industry_name_display, market_cap DESC";
        List<Map<String, Object>> universeRows = jdbcTemplate.queryForList(sql2);

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet("Valuation");

            // -----------------------------
            // Write Query 1 → A2 to AI4
            // -----------------------------
            int rowIndex = 1; // A2 = row index 1 (0‑based)
            for (Map<String, Object> row : top3Rows) {
                Row excelRow = sheet.getRow(rowIndex);
                if (excelRow == null) excelRow = sheet.createRow(rowIndex);

                int colIndex = 0;
                for (Object value : row.values()) {
                    Cell cell = excelRow.getCell(colIndex);
                    if (cell == null) cell = excelRow.createCell(colIndex);
                    setCellValuePreserveFormat(cell, value);
                    colIndex++;
                }
                rowIndex++;
            }
            System.out.println("Index Query Updated in ResultTracker Valuation Sheet");

            // -----------------------------
            // Write Query 2 → A6 downward
            // -----------------------------
            rowIndex = 5; // A6 = row index 5
            for (Map<String, Object> row : universeRows) {
                Row excelRow = sheet.getRow(rowIndex);
                if (excelRow == null) excelRow = sheet.createRow(rowIndex);

                int colIndex = 0;
                for (Object value : row.values()) {
                    Cell cell = excelRow.getCell(colIndex);
                    if (cell == null) cell = excelRow.createCell(colIndex);
                    setCellValuePreserveFormat(cell, value);
                    colIndex++;
                }
                rowIndex++;
            }

            System.out.println("Stock Query Updated in ResultTracker Valuation Sheet");

            // Save file
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCellValuePreserveFormat(Cell cell, Object value) {
        try {
            if (value == null) {
                cell.setCellValue(Cell.CELL_TYPE_BLANK);
                return;
            }

            String s = value.toString().trim();

            // Remove % sign and convert to number
            if (s.endsWith("%")) {
                String num = s.substring(0, s.length() - 1);
                double d = Double.parseDouble(num) / 100.0;
                cell.setCellValue(d);
                return;
            }

            // Try numeric
            try {
                double d = Double.parseDouble(s.replace(",", ""));
                cell.setCellValue(d);
                return;
            } catch (NumberFormatException ignore) {
                // Not numeric → fall through to text
            }

            // Default: write as text
            cell.setCellValue(s);

        } catch (Exception ignore) {
            // Ignore any error and continue
        }
    }
}