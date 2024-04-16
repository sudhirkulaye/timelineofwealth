package com.timelineofwealth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConsolidatedResultTracker {

    static String QUARTER = "Q4"; // Update this for each new quarter
    static String YEAR = "24"; // Update this for each new year
    static String TRACKER_FILE_PATH = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert\\ResultTracker.xlsx";
    static String QUARTE_REXCEL_Folder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\20" + YEAR + QUARTER;

    static Boolean IS_OVERRIDE_PRICE_DATA = true;
    static Boolean IS_OVERRIDE_RECO_DATA = true;
    static Boolean IS_OVERRIDE_OTHER_DATA = true;

    // Tracker workbook cell locations
    static Integer TICKER_LOCATION = 1;
    static Integer FOLDER_LOCATION = 2;
    static Integer MOSL_REPORT_STATUS_LOCATION = 6;
    static Integer AMBIT_REPORT_STATUS_LOCATION = 7;
    static Integer AXIS_REPORT_STATUS_LOCATION = 8;
    static Integer ICICIDIRECT_REPORT_STATUS_LOCATION = 9;
    static Integer PL_REPORT_STATUS_LOCATION = 10;
    static Integer KOTAK_REPORT_STATUS_LOCATION = 11;

    static Integer RESULT_PRICE_LOCATION = 13;

    static Integer TTM_REV_TREND_LOCATION = 16;
    static Integer TTM_NOPLAT_TREND_LOCATION = 17;
    static Integer TTM_SUBSIDIARY_TREND_LOCATION = 18;
    static Integer REV_G_Q0_RATIO_LOCATION = 19;
    static Integer REV_G_Q1_RATIO_LOCATION = 20;
    static Integer REV_G_Q2_RATIO_LOCATION = 21;
    static Integer REV_G_Q3_RATIO_LOCATION = 22;
    static Integer REV_G_Q4_RATIO_LOCATION = 23;
    static Integer NOPLAT_G_Q0_RATIO_LOCATION = 24;
    static Integer NOPLAT_G_Q1_RATIO_LOCATION = 25;
    static Integer NOPLAT_G_Q2_RATIO_LOCATION = 26;
    static Integer NOPLAT_G_Q3_RATIO_LOCATION = 27;
    static Integer NOPLAT_G_Q4_RATIO_LOCATION = 28;
    static Integer OPM_Q0_RATIO_LOCATION = 29;
    static Integer OPM_Q1_RATIO_LOCATION = 30;
    static Integer OPM_Q2_RATIO_LOCATION = 31;
    static Integer OPM_Q3_RATIO_LOCATION = 32;
    static Integer OPM_Q4_RATIO_LOCATION = 33;
    static Integer MOSL_RECO_LOCATION = 34;
    static Integer AMBIT_RECO_LOCATION = 35;
    static Integer AXIS_RECO_LOCATION = 36;
    static Integer ICICIDIRECT_RECO_LOCATION = 37;
    static Integer PL_RECO_LOCATION = 38;
    static Integer KOTAK_RECO_LOCATION = 39;
    static Integer AUM0_LOCATION = 40;
    static Integer AUM_G_Q0_RATIO_LOCATION = 41;
    static Integer AUM_G_Q1_RATIO_LOCATION = 42;
    static Integer AUM_G_Q2_RATIO_LOCATION = 43;
    static Integer AUM_G_Q3_RATIO_LOCATION = 44;
    static Integer AUM_G_Q4_RATIO_LOCATION = 45;
    static Integer GNPA_TREND_LOCATION = 46;
    static Integer NNPA_TREND_LOCATION = 47;
    static Integer GNPA_RATIO_TREND_LOCATION = 48;
    static Integer NNPA_RATIO_TREND_LOCATION = 49;
    static Integer SLIPPAGES_TREND_LOCATION = 50;

    //Ticker workbook QuarterP&L sheet cell locations
    static Integer LATEST_QUARTER_RESULT_PRICE_ROW = 13;
    static Integer LATEST_QUARTER_RESULT_PRICE_COLUMN = 17;

    //Ticker workbook AnalystReco sheet cell locations
    static Integer RECO_COLUMN = 6;
    static Integer TARGET_PRICE_COLUMN = 7;
    static Integer LATEST_QUARTER_COLUMN = 17;

    static Integer NET_PROFIT_ROW = 11;
    static Integer OPM_ROW = 15;
    static Integer QUARTER_SALES_GROWTH_ROW = 18;
    static Integer TTM_SALES_ROW = 19;
    static Integer NOPLAT_ROW = 22;
    static Integer TTM_NOPLAT_ROW = 23;
    static Integer TTM_AFTER_TAX_OTHER_INC_ROW = 33;
    static Integer TTM_PAT_ROW = 34;
    static Integer SLIPPAGES_ROW = 238;
    static Integer AUM_ROW = 240;
    static Integer GNPA_ROW = 243;
    static Integer NNPA_ROW = 244;
    static Integer GNPA_RATIO_ROW = 245;
    static Integer NNPA_RATIO_ROW = 246;
    static Integer NIM_ROW = 248;
    static Integer TTM_NII_ROW = 265;
    static Integer TTM_ST_PAT_ROW = 270;
    static Integer TTM_SUBSIDIARY_PAT_ROW = 272;
    static Integer QUARTER_NII_GROWTH_ROW = 280;
    static Integer ST_PAT_GROWTH_ROW = 285;
    static Integer AUM_GROWTH_ROW = 309;


    public static void updateResultTrackerExcel() {

        try {
            FileInputStream trackerFileInputStream;
            Workbook trackerWorkbook;
            Sheet fySheet;

            try {
                trackerFileInputStream = new FileInputStream(new File(TRACKER_FILE_PATH));
                trackerWorkbook = new XSSFWorkbook(trackerFileInputStream);
            } catch (Exception e){
                throw new Exception("Tracker file not found. " + e.getMessage());
            }

            try {
                fySheet = trackerWorkbook.getSheet("FY" + YEAR + QUARTER);
            } catch (Exception e){
                throw new Exception("FY" + YEAR + QUARTER + " sheet not found in the Tracker file. " + e.getMessage());
            }
            if (fySheet == null) {
                throw new Exception("FY" + YEAR + QUARTER + " sheet is null.");
            }

            //processing rows in FY sheet
            for (int rowIndex = 2; rowIndex < fySheet.getLastRowNum() + 1; rowIndex++) {
                // get ticker Row
                Row row = fySheet.getRow(rowIndex);

                // get corresponding Cells
                Cell tickerCell = row.getCell(TICKER_LOCATION);
                Cell folderCell = row.getCell(FOLDER_LOCATION);
                Cell moslReportStatusCell = row.getCell(MOSL_REPORT_STATUS_LOCATION);
                Cell ambitReportStatusCell = row.getCell(AMBIT_REPORT_STATUS_LOCATION);
                Cell axisReportStatusCell = row.getCell(AXIS_REPORT_STATUS_LOCATION);
                Cell icicidirectReportStatusCell = row.getCell(ICICIDIRECT_REPORT_STATUS_LOCATION);
                Cell plReportStatusCell = row.getCell(PL_REPORT_STATUS_LOCATION);
                Cell kotakReportStatusCell = row.getCell(KOTAK_REPORT_STATUS_LOCATION);
                Cell priceCell = row.getCell(RESULT_PRICE_LOCATION);
                Cell ttmRevTrendCell = row.getCell(TTM_REV_TREND_LOCATION);
                Cell ttmNoplatTrendCell = row.getCell(TTM_NOPLAT_TREND_LOCATION);
                Cell revGQ0RatioCell = row.getCell(REV_G_Q0_RATIO_LOCATION);
                Cell revGQ1RatioCell = row.getCell(REV_G_Q1_RATIO_LOCATION);
                Cell revGQ2RatioCell = row.getCell(REV_G_Q2_RATIO_LOCATION);
                Cell revGQ3RatioCell = row.getCell(REV_G_Q3_RATIO_LOCATION);
                Cell revGQ4RatioCell = row.getCell(REV_G_Q4_RATIO_LOCATION);
                Cell noplatGQ0RatioCell = row.getCell(NOPLAT_G_Q0_RATIO_LOCATION);
                Cell noplatGQ1RatioCell = row.getCell(NOPLAT_G_Q1_RATIO_LOCATION);
                Cell noplatGQ2RatioCell = row.getCell(NOPLAT_G_Q2_RATIO_LOCATION);
                Cell noplatGQ3RatioCell = row.getCell(NOPLAT_G_Q3_RATIO_LOCATION);
                Cell noplatGQ4RatioCell = row.getCell(NOPLAT_G_Q4_RATIO_LOCATION);
                Cell opmQ0RatioCell = row.getCell(OPM_Q0_RATIO_LOCATION);
                Cell opmQ1RatioCell = row.getCell(OPM_Q1_RATIO_LOCATION);
                Cell opmQ2RatioCell = row.getCell(OPM_Q2_RATIO_LOCATION);
                Cell opmQ3RatioCell = row.getCell(OPM_Q3_RATIO_LOCATION);
                Cell opmQ4RatioCell = row.getCell(OPM_Q4_RATIO_LOCATION);
                Cell moslRecoCell = row.getCell(MOSL_RECO_LOCATION);
                Cell ambitRecoCell = row.getCell(AMBIT_RECO_LOCATION);
                Cell axisRecoCell = row.getCell(AXIS_RECO_LOCATION);
                Cell icicidirectRecoCell = row.getCell(ICICIDIRECT_RECO_LOCATION);
                Cell plRecoCell = row.getCell(PL_RECO_LOCATION);
                Cell kotakRecoCell = row.getCell(KOTAK_RECO_LOCATION);
                Cell aum0Cell = row.getCell(AUM0_LOCATION);
                Cell aumGQ0RatioCell = row.getCell(AUM_G_Q0_RATIO_LOCATION);
                Cell aumGQ1RatioCell = row.getCell(AUM_G_Q1_RATIO_LOCATION);
                Cell aumGQ2RatioCell = row.getCell(AUM_G_Q2_RATIO_LOCATION);
                Cell aumGQ3RatioCell = row.getCell(AUM_G_Q3_RATIO_LOCATION);
                Cell aumGQ4RatioCell = row.getCell(AUM_G_Q4_RATIO_LOCATION);
                Cell gnpaTrendCell = row.getCell(GNPA_TREND_LOCATION);
                Cell nnpaTrendCell = row.getCell(NNPA_TREND_LOCATION);
                Cell gnpaRatioTrendCell = row.getCell(GNPA_RATIO_TREND_LOCATION);
                Cell nnpaRatioTrendCell = row.getCell(NNPA_RATIO_TREND_LOCATION);
                Cell slippagesTrendCell = row.getCell(SLIPPAGES_TREND_LOCATION);


                if (tickerCell != null && tickerCell.getCellTypeEnum() == CellType.STRING && tickerCell.getCellTypeEnum() != CellType.BLANK) {

                    String ticker = tickerCell.getStringCellValue();
                    try {
                        //opern ticker quarter result file
                        String tickerFilePath = QUARTE_REXCEL_Folder + "\\" + ticker + "_FY" + YEAR + QUARTER + ".xlsx";
                        File tickerFile = null;
                        FileInputStream tickerFileInputStream = null;
                        Workbook tickerWorkbook = null;
                        FormulaEvaluator trackerFileEvaluator;
                        FormulaEvaluator tickerFileEvaluator;


                        try {
                            tickerFile = new File(tickerFilePath);
                            tickerFileInputStream = new FileInputStream(tickerFile);
                            tickerWorkbook = new XSSFWorkbook(tickerFileInputStream);
                        } catch (Exception e) {
                            System.out.println("Could not find file for " + ticker + " Exception - " + e.getMessage());
                            continue;
                        }

                        if (tickerWorkbook == null) {
                            throw new Exception("tickerWorbook is null for " + ticker);
                        }

                        Sheet quarterPnLSheet = tickerWorkbook.getSheet("QuarterP&L");
                        if (quarterPnLSheet == null) {
                            throw new Exception("QuarterP&L not found for " + ticker);
                        }

                        Sheet analystRecoSheet = tickerWorkbook.getSheet("AnalystReco");
                        if (analystRecoSheet == null) {
                            throw new Exception("AnalystReco not found for " + ticker);
                        }

                        trackerFileEvaluator = trackerWorkbook.getCreationHelper().createFormulaEvaluator();
                        tickerFileEvaluator = tickerWorkbook.getCreationHelper().createFormulaEvaluator();

                        // update price data
                        if (priceCell == null ||
                                (priceCell != null && (priceCell.getCellTypeEnum() == CellType.BLANK || priceCell.getNumericCellValue() == 0)) ||
                                IS_OVERRIDE_PRICE_DATA == true) {

                            if (priceCell == null) {
                                priceCell = row.createCell(RESULT_PRICE_LOCATION, CellType.NUMERIC);
                            }
                            Cell priceDataCell = null;

                            try {
                                priceDataCell = quarterPnLSheet.getRow(LATEST_QUARTER_RESULT_PRICE_ROW).getCell(LATEST_QUARTER_RESULT_PRICE_COLUMN);
                            } catch (Exception e) {
                                System.out.println("Exception in getting the latest quarter result price CELL for " + ticker);
                            }
                            if (priceDataCell != null) {
                                double price = getNumberValueFromCell(priceDataCell, tickerFileEvaluator);
                                if (price > 0)
                                    priceCell.setCellValue(price);
                            } else {
                                System.out.println("priceDataCell null for " + ticker);
                            }
                        }

                        // update MOSL reco
                        if (moslReportStatusCell != null && !moslReportStatusCell.getStringCellValue().isEmpty()) {
                            if (moslRecoCell == null || moslRecoCell.getStringCellValue().isEmpty()) {
                                row.createCell(MOSL_RECO_LOCATION, CellType.STRING);
                                setReco(ticker, trackerWorkbook, analystRecoSheet, moslRecoCell, 0);
                            } else if (IS_OVERRIDE_RECO_DATA) {
                                setReco(ticker, trackerWorkbook, analystRecoSheet, moslRecoCell, 0);
                            }
                        }

                        // update AMBIT reco
                        if (ambitReportStatusCell != null && !ambitReportStatusCell.getStringCellValue().isEmpty()) {
                            if (ambitRecoCell == null || ambitRecoCell.getStringCellValue().isEmpty()) {
                                row.createCell(AMBIT_RECO_LOCATION, CellType.STRING);
                                setReco(ticker, trackerWorkbook, analystRecoSheet, ambitRecoCell, 20);
                            } else if (IS_OVERRIDE_RECO_DATA) {
                                setReco(ticker, trackerWorkbook, analystRecoSheet, ambitRecoCell, 20);
                            }
                        }

                        // update AXIS reco
                        if (axisReportStatusCell != null && !axisReportStatusCell.getStringCellValue().isEmpty()) {
                            if (axisRecoCell == null || axisRecoCell.getStringCellValue().isEmpty()) {
                                row.createCell(AXIS_RECO_LOCATION, CellType.STRING);
                                setReco(ticker, trackerWorkbook, analystRecoSheet, axisRecoCell, 40);
                            } else if (IS_OVERRIDE_RECO_DATA) {
                                setReco(ticker, trackerWorkbook, analystRecoSheet, axisRecoCell, 40);
                            }
                        }

                        // update ICICIDIRECT reco
                        if (icicidirectReportStatusCell != null && !icicidirectReportStatusCell.getStringCellValue().isEmpty()) {
                            if (icicidirectRecoCell == null || icicidirectRecoCell.getStringCellValue().isEmpty()) {
                                row.createCell(ICICIDIRECT_RECO_LOCATION, CellType.STRING);
                                setReco(ticker, trackerWorkbook, analystRecoSheet, icicidirectRecoCell, 60);
                            } else if (IS_OVERRIDE_RECO_DATA) {
                                setReco(ticker, trackerWorkbook, analystRecoSheet, icicidirectRecoCell, 60);
                            }
                        }

                        // update PL reco
                        if (plReportStatusCell != null && !plReportStatusCell.getStringCellValue().isEmpty()) {
                            if (plRecoCell == null || plRecoCell.getStringCellValue().isEmpty()) {
                                row.createCell(PL_RECO_LOCATION, CellType.STRING);
                                setReco(ticker, trackerWorkbook, analystRecoSheet, plRecoCell, 80);
                            } else if (IS_OVERRIDE_RECO_DATA) {
                                setReco(ticker, trackerWorkbook, analystRecoSheet, plRecoCell, 80);
                            }
                        }

                        // update KOTAK reco
                        if (kotakReportStatusCell != null && !kotakReportStatusCell.getStringCellValue().isEmpty()) {
                            if (kotakRecoCell == null || kotakRecoCell.getStringCellValue().isEmpty()) {
                                row.createCell(KOTAK_RECO_LOCATION, CellType.STRING);
                                setReco(ticker, trackerWorkbook, analystRecoSheet, kotakRecoCell, 100);
                            } else if (IS_OVERRIDE_RECO_DATA) {
                                setReco(ticker, trackerWorkbook, analystRecoSheet, kotakRecoCell, 100);
                            }
                        }

                        // update Revenue Trend, NOPLAT and OPM and Revenue growth
                        CellValue cellValue = trackerFileEvaluator.evaluate(folderCell);
                        String folder = null;
                        boolean isFinancialTicker = false;

                        try {
                            folder = cellValue.getStringValue();
                            if (folder.startsWith("Financials"))
                                isFinancialTicker = true;
                        } catch (Exception e) {
                            folder = null;
                        }

                        if (!isFinancialTicker) {
                            // set TTM Sales Trend
                            setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_SALES_ROW, LATEST_QUARTER_COLUMN, TTM_REV_TREND_LOCATION);
                            // set TTM NOPLAT Trend
                            setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_NOPLAT_ROW, LATEST_QUARTER_COLUMN, TTM_NOPLAT_TREND_LOCATION);
                            // set TTM After Tax Non. Op. Inc. Trend
                            setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_AFTER_TAX_OTHER_INC_ROW, LATEST_QUARTER_COLUMN, TTM_SUBSIDIARY_TREND_LOCATION);
                            // set Quarterly Sales Growth
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q0_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q1_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 1);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q2_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 2);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q3_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 3);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q4_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 4);
                            // set Quarter NOPLAT growth rate
                            computeAndSetGrowth(row, quarterPnLSheet, tickerFileEvaluator, NOPLAT_ROW, NOPLAT_G_Q0_RATIO_LOCATION);
                            // set QPM
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q0_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q1_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 1);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q2_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 2);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q3_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 3);
                            setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q4_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 4);
                        } else {
                            // set TTM NII as TTM Sales for Financials if not exists then set TTM Sales
                            if (quarterPnLSheet.getRow(TTM_NII_ROW) != null && quarterPnLSheet.getRow(TTM_NII_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("TTM NII")) {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_NII_ROW, LATEST_QUARTER_COLUMN, TTM_REV_TREND_LOCATION);
                            } else {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_SALES_ROW, LATEST_QUARTER_COLUMN, TTM_REV_TREND_LOCATION);
                            }
                            // set TTM Standalone PAT TREND as NOPLAT trend
                            if (quarterPnLSheet.getRow(TTM_ST_PAT_ROW) != null && quarterPnLSheet.getRow(TTM_ST_PAT_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("TTM St. PAT")) {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_ST_PAT_ROW, LATEST_QUARTER_COLUMN, TTM_NOPLAT_TREND_LOCATION);
                            } else {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_PAT_ROW, LATEST_QUARTER_COLUMN, TTM_NOPLAT_TREND_LOCATION);
                            }
                            // set subsiiary PAT trend
                            if (quarterPnLSheet.getRow(TTM_SUBSIDIARY_PAT_ROW) != null && quarterPnLSheet.getRow(TTM_SUBSIDIARY_PAT_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("TTM Subsidiaries")) {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, TTM_SUBSIDIARY_PAT_ROW, LATEST_QUARTER_COLUMN, TTM_SUBSIDIARY_TREND_LOCATION);
                            }
                            // set NII growth
                            if (quarterPnLSheet.getRow(QUARTER_NII_GROWTH_ROW) != null && quarterPnLSheet.getRow(QUARTER_NII_GROWTH_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("NII g%")) {
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q0_RATIO_LOCATION, QUARTER_NII_GROWTH_ROW, LATEST_QUARTER_COLUMN);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q1_RATIO_LOCATION, QUARTER_NII_GROWTH_ROW, LATEST_QUARTER_COLUMN - 1);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q2_RATIO_LOCATION, QUARTER_NII_GROWTH_ROW, LATEST_QUARTER_COLUMN - 2);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q3_RATIO_LOCATION, QUARTER_NII_GROWTH_ROW, LATEST_QUARTER_COLUMN - 3);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q4_RATIO_LOCATION, QUARTER_NII_GROWTH_ROW, LATEST_QUARTER_COLUMN - 4);
                            } else {
                                // else set sales growth
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q0_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q1_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 1);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q2_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 2);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q3_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 3);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, REV_G_Q4_RATIO_LOCATION, QUARTER_SALES_GROWTH_ROW, LATEST_QUARTER_COLUMN - 4);
                            }
                            // set Standalone PAT growth
                            if (quarterPnLSheet.getRow(ST_PAT_GROWTH_ROW) != null && quarterPnLSheet.getRow(ST_PAT_GROWTH_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("St. PAT g%")) {
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, NOPLAT_G_Q0_RATIO_LOCATION, ST_PAT_GROWTH_ROW, LATEST_QUARTER_COLUMN);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, NOPLAT_G_Q1_RATIO_LOCATION, ST_PAT_GROWTH_ROW, LATEST_QUARTER_COLUMN - 1);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, NOPLAT_G_Q2_RATIO_LOCATION, ST_PAT_GROWTH_ROW, LATEST_QUARTER_COLUMN - 2);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, NOPLAT_G_Q3_RATIO_LOCATION, ST_PAT_GROWTH_ROW, LATEST_QUARTER_COLUMN - 3);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, NOPLAT_G_Q4_RATIO_LOCATION, ST_PAT_GROWTH_ROW, LATEST_QUARTER_COLUMN - 4);
                            } else {
                                // else set consolidated PAT growth
                                computeAndSetGrowth(row, quarterPnLSheet, tickerFileEvaluator, NET_PROFIT_ROW, NOPLAT_G_Q0_RATIO_LOCATION);
                            }
                            // set NIM
                            if (quarterPnLSheet.getRow(NIM_ROW) != null && quarterPnLSheet.getRow(NIM_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("NIM%")) {
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q0_RATIO_LOCATION, NIM_ROW, LATEST_QUARTER_COLUMN);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q1_RATIO_LOCATION, NIM_ROW, LATEST_QUARTER_COLUMN - 1);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q2_RATIO_LOCATION, NIM_ROW, LATEST_QUARTER_COLUMN - 2);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q3_RATIO_LOCATION, NIM_ROW, LATEST_QUARTER_COLUMN - 3);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q4_RATIO_LOCATION, NIM_ROW, LATEST_QUARTER_COLUMN - 4);
                            } else {
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q0_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q1_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 1);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q2_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 2);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q3_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 3);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, OPM_Q4_RATIO_LOCATION, OPM_ROW, LATEST_QUARTER_COLUMN - 4);
                            }
                            // set AUM
                            if (quarterPnLSheet.getRow(AUM_ROW) != null && quarterPnLSheet.getRow(AUM_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("Loans / AUM")) {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, AUM_ROW, LATEST_QUARTER_COLUMN, AUM0_LOCATION, 100000, false);
                            }
                            // set AUM growth
                            if (quarterPnLSheet.getRow(AUM_GROWTH_ROW) != null && quarterPnLSheet.getRow(AUM_GROWTH_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("Loans / AUM g%")) {
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, AUM_G_Q0_RATIO_LOCATION, AUM_GROWTH_ROW, LATEST_QUARTER_COLUMN);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, AUM_G_Q1_RATIO_LOCATION, AUM_GROWTH_ROW, LATEST_QUARTER_COLUMN - 1);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, AUM_G_Q2_RATIO_LOCATION, AUM_GROWTH_ROW, LATEST_QUARTER_COLUMN - 2);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, AUM_G_Q3_RATIO_LOCATION, AUM_GROWTH_ROW, LATEST_QUARTER_COLUMN - 3);
                                setNumericValue(row, quarterPnLSheet, tickerFileEvaluator, AUM_G_Q4_RATIO_LOCATION, AUM_GROWTH_ROW, LATEST_QUARTER_COLUMN - 4);
                            }
                            // set GNPA Trend
                            if (quarterPnLSheet.getRow(GNPA_ROW) != null && quarterPnLSheet.getRow(GNPA_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("GNPA")) {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, GNPA_ROW, LATEST_QUARTER_COLUMN, GNPA_TREND_LOCATION);
                            }
                            // set NNPA Trend
                            if (quarterPnLSheet.getRow(NNPA_ROW) != null && quarterPnLSheet.getRow(NNPA_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("NNPA")) {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, NNPA_ROW, LATEST_QUARTER_COLUMN, NNPA_TREND_LOCATION);
                            }
                            // set GNPA% Trend
                            if (quarterPnLSheet.getRow(GNPA_RATIO_ROW) != null && quarterPnLSheet.getRow(GNPA_RATIO_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("GNPA%")) {
                                setTrend(row, quarterPnLSheet, tickerFileEvaluator, GNPA_RATIO_ROW, LATEST_QUARTER_COLUMN, GNPA_RATIO_TREND_LOCATION, 1000, true);
                            }
                            // set NNPA% Trend
                            try {
                                if (quarterPnLSheet.getRow(NNPA_RATIO_ROW) != null && quarterPnLSheet.getRow(NNPA_RATIO_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("NNPA%")) {
                                    setTrend(row, quarterPnLSheet, tickerFileEvaluator, NNPA_RATIO_ROW, LATEST_QUARTER_COLUMN, NNPA_RATIO_TREND_LOCATION, 100, true);
                                }
                                // set Slippages Trend
                                if (quarterPnLSheet.getRow(SLIPPAGES_ROW) != null && quarterPnLSheet.getRow(SLIPPAGES_ROW).getCell(0).getStringCellValue().trim().equalsIgnoreCase("Slippages")) {
                                    setTrend(row, quarterPnLSheet, tickerFileEvaluator, SLIPPAGES_ROW, LATEST_QUARTER_COLUMN, SLIPPAGES_TREND_LOCATION);
                                }
                            } catch (Exception e) {
                                System.out.println("Exception in setting NNPA% Trend for ticker " + ticker);
                                e.printStackTrace();
                            }
                        }
                        tickerFileInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exception in processing ticker " + ticker);
                    }
                }
            }

            trackerFileInputStream.close();
            // Save the updated tracker file
            saveWorkbook(trackerWorkbook, TRACKER_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void saveWorkbook(Workbook workbook, String filePath) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setReco(String ticker, Workbook trackerWorkbook, Sheet analystRecoSheet, Cell trackerCell, int startPosition){

        XSSFWorkbook workbook = (XSSFWorkbook) trackerWorkbook;

        // Create a cell style with the desired font color
        XSSFFont greenFont = workbook.createFont();
        greenFont.setColor(IndexedColors.GREEN.getIndex()); // Green color

        XSSFFont redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.getIndex()); // Red color

        // Create a font for black color
        XSSFFont blackFont = workbook.createFont();
        blackFont.setColor(IndexedColors.BLACK.getIndex());

        String recommendation = "";
        String[] recoArray = new String[4]; // Array to store recommendations
        String[] targetArray = new String[4]; // Array to store target prices

        //fill values in the array
        for (int i = 1; i < 5; i ++) {
            Cell recoCell = analystRecoSheet.getRow(i + startPosition).getCell(RECO_COLUMN);
            Cell targetCell = analystRecoSheet.getRow(i + startPosition).getCell(TARGET_PRICE_COLUMN);

            if (recoCell != null && recoCell.getCellTypeEnum() == CellType.STRING
                    && targetCell != null && targetCell.getCellTypeEnum() == CellType.NUMERIC) {
                String moslReco = recoCell.getStringCellValue();
                try {
                    int moslTarget = (int) targetCell.getNumericCellValue();
                    recoArray[i - 1] = moslReco;
                    targetArray[i - 1] = "" + moslTarget;
                } catch (Exception e) {
                    recoArray[i - 1] = moslReco;
                    targetArray[i - 1] = "" + 0;
                }
            } else {
                recoArray[i - 1] = "";
                targetArray[i - 1] = "";
            }
        }

        int firstTargetStart = 0, firstTargetEnd = 0, secondTargetStart = 0, secondTargetEnd = 0, thirdTargetStart = 0, thirdTargetEnd = 0;
        XSSFFont firstTargetColor = blackFont, secondTargetColor = blackFont, thirdTargetColor = blackFont;
        int firstRecoStart = 0, firstRecoEnd = 0, secondRecoStart = 0, secondRecoEnd = 0, thirdRecoStart = 0, thirdRecoEnd = 0;
        XSSFFont firstRecoColor = blackFont, secondRecoColor = blackFont, thirdRecoColor = blackFont;

        for (int i=0; i<4; i++){
            if(!recoArray[i].equals("")) {
                if(recommendation.equals("")) {
                    // first item
                    if(!targetArray[i + 1].equals("")){
                        int nextTargetNo = Integer.parseInt(targetArray[i + 1]);
                        int targetNo = Integer.parseInt(targetArray[i]);
                        String nextReco = recoArray[i + 1];
                        String reco = recoArray[i];
                        if(targetNo > nextTargetNo) {
                            firstTargetColor = greenFont;
                        }
                        if(targetNo < nextTargetNo){
                            firstTargetColor = redFont;
                        }
                        if(isUpgrade(nextReco, reco)){
                            firstRecoColor = greenFont;
                            System.out.println("1st Upgrade for ticker - " + ticker + " " + reco + " / " + nextReco);
                        }
                        if(isDowngrade(nextReco, reco)){
                            firstRecoColor = redFont;
                            System.out.println("1st Downgrade for ticker - " + ticker + " " + reco + " / " + nextReco);
                        }
                    }
                    firstRecoStart = 0;
                    firstRecoEnd = recoArray[i].length();
                    firstTargetStart = recoArray[i].length() + 1;
                    firstTargetEnd = firstTargetStart + targetArray[i].length() + 1;
                    recommendation = recoArray[i] + " " + targetArray[i];
                }
                else {
                    if(i==1){
                        if(!targetArray[i + 1].equals("")) {
                            int nextTargetNo = Integer.parseInt(targetArray[i + 1]);
                            int targetNo = Integer.parseInt(targetArray[i]);
                            String nextReco = recoArray[i + 1];
                            String reco = recoArray[i];
                            if (targetNo > nextTargetNo) {
                                secondTargetColor = greenFont;
                            }
                            if (targetNo < nextTargetNo) {
                                secondTargetColor = redFont;
                            }
                            if(isUpgrade(nextReco, reco)){
                                secondRecoColor = greenFont;
//                                System.out.println("2nd Upgrade for ticker - " + ticker + " " + reco + " / " + nextReco);
                            }
                            if(isDowngrade(nextReco, reco)){
                                secondRecoColor = redFont;
//                                System.out.println("2nd Downgrade for ticker - " + ticker + " " + reco + " / " + nextReco);
                            }
                        }
                        secondRecoStart = recommendation.length() + 2;
                        secondRecoEnd = recommendation.length() + recoArray[i].length() + 3;
                        secondTargetStart = recommendation.length() + recoArray[i].length() + 4;
                        secondTargetEnd = secondTargetStart + targetArray[i].length() + 1;
                    }
                    if(i==2){
                        if(!targetArray[i + 1].equals("")) {
                            int nextTargetNo = Integer.parseInt(targetArray[i + 1]);
                            int targetNo = Integer.parseInt(targetArray[i]);
                            String nextReco = recoArray[i + 1];
                            String reco = recoArray[i];
                            if (targetNo > nextTargetNo) {
                                thirdTargetColor = greenFont;
                            }
                            if (targetNo < nextTargetNo) {
                                thirdTargetColor = redFont;
                            }
                            if(isUpgrade(nextReco, reco)){
                                thirdRecoColor = greenFont;
//                                System.out.println("3rd Upgrade for ticker - " + ticker + " " + reco + " / " + nextReco);
                            }
                            if(isDowngrade(nextReco, reco)){
                                thirdRecoColor = redFont;
//                                System.out.println("3rd Downgrade for ticker - " + ticker + " " + reco + " / " + nextReco);
                            }
                        }
                        thirdRecoStart = recommendation.length() + 2;
                        thirdRecoEnd = recommendation.length() + recoArray[i].length() + 3;
                        thirdTargetStart = recommendation.length() + recoArray[i].length() + 4;
                        thirdTargetEnd = thirdTargetStart + targetArray[i].length() + 1;
                    }
                    recommendation = recommendation + " / " + recoArray[i] + " " + targetArray[i];
                }
            }
            else {
                if(i < 3) {
                    if(i==1){
                        secondRecoStart = recommendation.length();
                        secondRecoEnd = secondRecoStart + 2;
                        secondTargetStart = recommendation.length();
                        secondTargetEnd = secondTargetStart + 3;
                    }
                    if(i==2){
                        thirdRecoStart = recommendation.length();
                        thirdRecoEnd = thirdRecoStart + 2;
                        thirdTargetStart = recommendation.length();
                        thirdTargetEnd = thirdTargetStart + 3;
                    }
                    recommendation = recommendation + " / ";
                }
                else {
                    recommendation = recommendation + "  ";
                }
            }
        }
        /*System.out.print(ticker + " - recommendation: " + recommendation + " length - " + recommendation.length());
        System.out.print(" firstRecoStart - firstRecoEnd : " + firstRecoStart + "-" + firstRecoEnd);
        System.out.print(" firstTargetStart - firstTargetEnd : " + firstTargetStart + "-" + firstTargetEnd);
        System.out.print(" secondRecoStart - firstRecoEnd : " + secondRecoStart + "-" + secondRecoEnd);
        System.out.print(" secondTargetStart - firstTargetEnd : " + secondTargetStart + "-" + secondTargetEnd);
        System.out.println(" thirdRecoStart - firstRecoEnd : " + thirdRecoStart + "-" + thirdRecoEnd);
        System.out.println(" thirdTargetStart - firstTargetEnd : " + thirdTargetStart + "-" + thirdTargetEnd);*/

        try {
            trackerCell.setCellValue(recommendation);
            trackerCell.getRichStringCellValue().applyFont(firstRecoStart, firstRecoEnd, firstRecoColor);
            trackerCell.getRichStringCellValue().applyFont(firstTargetStart, firstTargetEnd, firstTargetColor);
            trackerCell.getRichStringCellValue().applyFont(secondRecoStart, secondRecoEnd, secondRecoColor);
            trackerCell.getRichStringCellValue().applyFont(secondTargetStart, secondTargetEnd, secondTargetColor);
            trackerCell.getRichStringCellValue().applyFont(thirdRecoStart, thirdRecoEnd, thirdRecoColor);
            trackerCell.getRichStringCellValue().applyFont(thirdTargetStart, thirdTargetEnd, thirdTargetColor);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Exception in setting Rich Text for " + ticker + ", AnalystReco start position - " + startPosition + " Exception: " + e.getMessage());
        }
    }

    public static boolean isUpgrade(String lastReco, String currentReco) {
        int lastRecoRank = getRecommendationRank(lastReco);
        int currentRecoRank = getRecommendationRank(currentReco);
        return currentRecoRank < lastRecoRank;
    }

    public static boolean isDowngrade(String lastReco, String currentReco) {
        int lastRecoRank = getRecommendationRank(lastReco);
        int currentRecoRank = getRecommendationRank(currentReco);
        return currentRecoRank > lastRecoRank;
    }

    public static int getRecommendationRank(String reco) {
        switch (reco.trim().toLowerCase()) {
            case "buy":
                return 1;
            case "accumulate":
            case "add":
                return 2;
            case "hold":
                return 3;
            case "neutral":
                return 4;
            case "reduce":
            case "sell":
                return 5;
            case "ur":
            case "na":
            case "nr":
                return 6;
            default:
                return 0; // Return 0 for unknown or invalid recommendations
        }
    }

    public static void setTrend(Row trackerRow, Sheet quarterPnLSheet, FormulaEvaluator tickerFileEvaluator,  int rownumber, int y0CellNo, int trackerCellNumber) {
        setTrend(trackerRow, quarterPnLSheet, tickerFileEvaluator, rownumber, y0CellNo, trackerCellNumber, 1000, false);
        /*Row tickerRow = quarterPnLSheet.getRow(rownumber);//19 for Revenue
        String[] tickerRowValuesArray = new String[4];
        if (tickerRow != null) {
            for (int i = y0CellNo, j = 0; i >= (y0CellNo-3) || j <= 3; i--, j++) { //17 for Y0 and 14 for Y4
                Cell ttmRevenueCell = tickerRow.getCell(i);
                if (ttmRevenueCell != null) {
                    CellValue cellValue = tickerFileEvaluator.evaluate(ttmRevenueCell);
                    try {
                        double tickerRowValue = cellValue.getNumberValue() / 1000; // in thousand cr.
                        tickerRowValue = Math.round(tickerRowValue * 100);
                        tickerRowValue = tickerRowValue / 100;
                        tickerRowValuesArray[j] = "" + tickerRowValue;
                    } catch (Exception e) {
                        tickerRowValuesArray[j] = "";
                    }
                }
            }
        }
        String trackerRowTrend = "";
        for (int i = 0; i <= 3; i++) {
            if (i < 3)
                trackerRowTrend = trackerRowTrend + tickerRowValuesArray[i] + " / ";
            else
                trackerRowTrend = trackerRowTrend + tickerRowValuesArray[i];
        }
        Cell trackerRowTrendCell = trackerRow.getCell(trackerCellNumber);
        if (trackerRowTrendCell == null)
            trackerRowTrendCell = trackerRow.createCell(trackerCellNumber, CellType.STRING);
        trackerRowTrendCell.setCellValue(trackerRowTrend);*/
    }

    public static void setTrend(Row trackerRow, Sheet quarterPnLSheet, FormulaEvaluator tickerFileEvaluator,  int rownumber, int y0CellNo, int trackerCellNumber, int unit, boolean isPercent) {

        Row tickerRow = quarterPnLSheet.getRow(rownumber);//19 for Revenue
        String[] tickerRowValuesArray = new String[4];
        if (tickerRow != null) {
            for (int i = y0CellNo, j = 0; i >= (y0CellNo-3) || j <= 3; i--, j++) { //17 for Y0 and 14 for Y4
                Cell ttmRevenueCell = tickerRow.getCell(i);
                if (ttmRevenueCell != null) {
                    CellValue cellValue = tickerFileEvaluator.evaluate(ttmRevenueCell);
                    try {
                        double tickerRowValue = 0; // in thousand cr. or lac cr.
                        if (!isPercent) {
                            tickerRowValue = cellValue.getNumberValue() / unit; // in thousand cr.
                            tickerRowValue = Math.round(tickerRowValue * 100);
                            tickerRowValue = tickerRowValue / 100;
                        } else {
                            tickerRowValue = cellValue.getNumberValue() * 100; // in thousand cr.
                            tickerRowValue = Math.round(tickerRowValue * 100);
                            tickerRowValue = tickerRowValue / 100;
                        }
                        tickerRowValuesArray[j] = "" + tickerRowValue;
                    } catch (Exception e) {
                        tickerRowValuesArray[j] = "";
                    }
                }
            }
        }
        String trackerRowTrend = "";
        for (int i = 0; i <= 3; i++) {
            if (i < 3) {
                if(!isPercent) {
                    trackerRowTrend = trackerRowTrend + tickerRowValuesArray[i] + " / ";
                } else {
                    trackerRowTrend = trackerRowTrend + tickerRowValuesArray[i] + "% / ";
                }
            }
            else {
                if(!isPercent) {
                    trackerRowTrend = trackerRowTrend + tickerRowValuesArray[i];
                } else {
                    trackerRowTrend = trackerRowTrend + tickerRowValuesArray[i] + "%";
                }
            }
        }
        Cell trackerRowTrendCell = trackerRow.getCell(trackerCellNumber);
        if (trackerRowTrendCell == null)
            trackerRowTrendCell = trackerRow.createCell(trackerCellNumber, CellType.STRING);
        trackerRowTrendCell.setCellValue(trackerRowTrend);
    }

    public static void setNumericValue(Row row, Sheet quarterPnLSheet, FormulaEvaluator tickerFileEvaluator, int trackerCellLocation, int tickerRowNumber, int tickerCellNumber){
        if (quarterPnLSheet.getRow(tickerRowNumber) != null) {
            if (row.getCell(trackerCellLocation) == null || row.getCell(trackerCellLocation).getCellType() == Cell.CELL_TYPE_BLANK || IS_OVERRIDE_OTHER_DATA) {
                if (row.getCell(trackerCellLocation) == null)
                    row.createCell(trackerCellLocation, CellType.NUMERIC);
                row.getCell(trackerCellLocation).setCellValue(getNumberValueFromCell(quarterPnLSheet.getRow(tickerRowNumber).getCell(tickerCellNumber), tickerFileEvaluator));
            }
        }
    }

    private static double getNumberValueFromCell(Cell dataCell, FormulaEvaluator evaluator) {
        double value = 0;
        if(dataCell == null) {
            return value;
        }
        if (dataCell.getCellTypeEnum() == CellType.NUMERIC) {
            value = dataCell.getNumericCellValue();
        } else if (dataCell.getCellTypeEnum() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(dataCell);
            try {
                value = cellValue.getNumberValue();
            } catch (Exception e) {
                value = 0;
            }
        }
        return value;
    }

    private static void computeAndSetGrowth(Row row, Sheet quarterPnLSheet, FormulaEvaluator tickerFileEvaluator,  int tickerRowNumber, int trackerFirstCellLocation){
        if (quarterPnLSheet.getRow(tickerRowNumber) != null) {
            Cell currentProfitCell, lastYrProfitCell;
            for (int i = LATEST_QUARTER_COLUMN; i >= LATEST_QUARTER_COLUMN - 4; i--){
                currentProfitCell = quarterPnLSheet.getRow(tickerRowNumber).getCell(i);
                lastYrProfitCell =  quarterPnLSheet.getRow(tickerRowNumber).getCell(i-4);
                if(currentProfitCell!=null && lastYrProfitCell!=null){
                    CellValue currentProfitValue = tickerFileEvaluator.evaluate(currentProfitCell);
                    CellValue lastYrProfitValue = tickerFileEvaluator.evaluate(lastYrProfitCell);
                    double currentProfit = currentProfitValue.getNumberValue();
                    double lastYrProfit = lastYrProfitValue.getNumberValue();
                    if(lastYrProfit>0 && currentProfit > 0){
                        double profitGrowth = (currentProfit/lastYrProfit)-1;
                        profitGrowth = Math.round(profitGrowth * 10000);
                        profitGrowth = profitGrowth / 10000;
                        row.getCell(trackerFirstCellLocation).setCellValue(profitGrowth);
                    } else {
                        row.getCell(trackerFirstCellLocation).setCellValue(0);
                    }
                } else {
                    row.getCell(trackerFirstCellLocation).setCellValue(0);
                }
                trackerFirstCellLocation++;
            }
        }
    }
}
