package com.timelineofwealth.service;


import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.timelineofwealth.dto.ReportDataExtractConfig;
import com.timelineofwealth.dto.ReportParameters;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalystRecoExtractor {

    private String ambitAnalystNames = "";
    private String moslAnalystNames = "";
    private String axisAnalystNames = "";
    private String icicidirectAnalystNames = "";
    private String plAnalystNames = "";
    private String kotakAanalystNames = "";
    private String[] brokers = {"MOSL", "AMBIT", "AXIS","ICICIDIRECT", "PL", "KOTAK"};

    private Map<String, ReportDataExtractConfig> configMap = new HashMap<>();

    public static void main(String[] args) {

        try {
            // Load config file
            Properties prop = new Properties();
            FileInputStream input = new FileInputStream("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\reportconfig.properties");
            prop.load(input);
            input.close();

            // Get reportDataExtractConfigFilePath
            String reportDataExtractConfigFilePath = prop.getProperty("ReportDataExtractConfigFilePath");
            String analystNamesFilePath = prop.getProperty("AnalystNamesFilePath");
            AnalystRecoExtractor analystRecoExtractor = new AnalystRecoExtractor();
            analystRecoExtractor.loadAnalystNames(analystNamesFilePath);
            analystRecoExtractor.loadReportDataExtractConfig(reportDataExtractConfigFilePath);

            // Load list of reportPaths and corresponding excelPaths
            ArrayList<String> reportFilePaths = new ArrayList<String>();
            ArrayList<String> excelFilePaths = new ArrayList<String>();
            ArrayList<String> processFlags = new ArrayList<String>();
            int count = 1;
            while (true) {
                String reportFilePath = prop.getProperty("reportFile" + count);
                if (reportFilePath == null) {
                    break;
                }
                reportFilePaths.add(reportFilePath);
                excelFilePaths.add(prop.getProperty("excelFile" + count));
                processFlags.add(prop.getProperty("processFlag" + count));
                count++;
            }

            //For each report file
            for (int i = 0; i < reportFilePaths.size(); i++) {
                String reportFilePath = reportFilePaths.get(i);
                String excelFilePath = excelFilePaths.get(i);
                String processFlag = processFlags.get(i);

                if (processFlag.equalsIgnoreCase("Y")) {

                    File file = new File(reportFilePath);
                    String fileName = file.getName().substring(0, file.getName().indexOf(".pdf"));

                    String quarter = fileName.split("_")[0];
                    String ticker = fileName.split("_")[1];
                    String brokerName = fileName.split("_")[2];
                    Date dateLastModfied = new Date(file.lastModified());

                    file.getClass();

                    // Load ReportDataConfig Parameters for the ticker
                    ReportDataExtractConfig reportDataExtractConfig = analystRecoExtractor.getReportDataExtractConfig(reportDataExtractConfigFilePath, quarter, ticker, brokerName);

                    // get Subclass based on broker
                    AnalystRecoExtractor extractor = getExtractor(brokerName);

                    // Call getReportParameters
                    int currentFile = i + 1;
                    System.out.println("\n\n ********** New File ********* \n\n");
                    System.out.println("Begin Updating File No. " + currentFile + " --> " +quarter + "_" + ticker + "_" + brokerName);
                    ReportParameters reportParameters = extractor.getReportParameters(reportFilePath, reportDataExtractConfig);
                    // Call saveReportParameters
                    extractor.saveReportParameters(reportParameters, excelFilePath);
                }

            }


        } catch (Exception e) {
            System.out.println("Exception in AnalystRecoExtractor.main " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected ReportParameters getReportParameters(String reportFilePath, ReportDataExtractConfig reportDataExtractConfig) {
        return null;
    }


    protected void saveReportParameters(ReportParameters reportParameters, String excelFileToBeUpdated) {
        try {
            FileInputStream file = new FileInputStream(new File(excelFileToBeUpdated));
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet ws = wb.getSheet("AnalystReco");
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            // Loop through each row in the workbook
            for (int i = 1; i <= ws.getLastRowNum(); i++) {
                XSSFRow row = ws.getRow(i);
                XSSFCell firstColumnCell = row.getCell(0); // Quarter
                XSSFCell sixthColumnCell = row.getCell(5); // Broker
                if (firstColumnCell != null && sixthColumnCell != null) {
                    CellValue firstColumnValue = evaluator.evaluate(firstColumnCell);
                    CellValue sixthColumnValue = evaluator.evaluate(sixthColumnCell);
                    // if Quarter & Broker matches then copy the result at that row
                    if (firstColumnValue != null && sixthColumnValue != null &&
                            firstColumnValue.getStringValue().equals(reportParameters.getQuarter()) &&
                            sixthColumnValue.getStringValue().equals(reportParameters.getBroker()) ) {
                        XSSFCell dateCell = row.getCell(1);
                        dateCell.setCellValue(reportParameters.getReportDate());

                        XSSFCell mcapCell = row.getCell(2);
                        mcapCell.setCellValue(reportParameters.getMcap().doubleValue());

                        XSSFCell priceCell = row.getCell(3);
                        priceCell.setCellValue(reportParameters.getPrice().doubleValue());

                        XSSFCell recoCell = row.getCell(6);
                        recoCell.setCellValue(reportParameters.getRating());

                        XSSFCell targetCell = row.getCell(7);
                        targetCell.setCellValue(reportParameters.getTarget().doubleValue());

                        XSSFCell y0RevCell = row.getCell(9);
                        y0RevCell.setCellValue(reportParameters.getY0Revenue().doubleValue());

                        XSSFCell y1RevCell = row.getCell(10);
                        y1RevCell.setCellValue(reportParameters.getY1Revenue().doubleValue());

                        XSSFCell y2RevCell = row.getCell(11);
                        y2RevCell.setCellValue(reportParameters.getY2Revenue().doubleValue());

                        XSSFCell y0EBITCell = row.getCell(13);
                        y0EBITCell.setCellFormula(reportParameters.getY0EBIT());

                        XSSFCell y1EBITCell = row.getCell(14);
                        y1EBITCell.setCellFormula(reportParameters.getY1EBIT());

                        XSSFCell y2EBITCell = row.getCell(15);
                        y2EBITCell.setCellFormula(reportParameters.getY2EBIT());

                        XSSFCell y0OPMCell = row.getCell(17);
                        y0OPMCell.setCellValue(reportParameters.getY0OPM().doubleValue());

                        XSSFCell y1OPMCell = row.getCell(18);
                        y1OPMCell.setCellValue(reportParameters.getY1OPM().doubleValue());

                        XSSFCell y2OPMCell = row.getCell(19);
                        y2OPMCell.setCellValue(reportParameters.getY2OPM().doubleValue());

                        XSSFCell y0ROCECell = row.getCell(20);
                        y0ROCECell.setCellValue(reportParameters.getY0ROCE().doubleValue());

                        XSSFCell y1ROCECell = row.getCell(21);
                        y1ROCECell.setCellValue(reportParameters.getY1ROCE().doubleValue());

                        XSSFCell y2ROCECell = row.getCell(22);
                        y2ROCECell.setCellValue(reportParameters.getY2ROCE().doubleValue());

                        XSSFCell y0EVBYEBITCell = row.getCell(23);
                        y0EVBYEBITCell.setCellValue(reportParameters.getY0EVBYEBIT().doubleValue());

                        XSSFCell y1EVBYEBITCell = row.getCell(24);
                        y1EVBYEBITCell.setCellValue(reportParameters.getY1EVBYEBIT().doubleValue());

                        XSSFCell y2EVBYEBITCell = row.getCell(25);
                        y2EVBYEBITCell.setCellValue(reportParameters.getY2EVBYEBIT().doubleValue());

                        XSSFCell analystNamesCell = row.getCell(39);
                        if (analystNamesCell == null) {
                            analystNamesCell = row.createCell(39);
                        }
                        analystNamesCell.setCellValue(reportParameters.getAnalystsNames());

                        XSSFCell summaryCell = row.getCell(40);
                        evaluator.evaluate(summaryCell);
                    }

                }
            }
            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(new File(excelFileToBeUpdated));
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            String fileName = new File(excelFileToBeUpdated).getAbsoluteFile().toString();
            System.out.println("********** Copied AnalystReco Sheet for " + fileName.substring(fileName.lastIndexOf("\\")) + "*********\n\n");
        } catch (Exception e) {
            System.out.println("Exception in saveReportParameters " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadAnalystNames(String analystNamesFilePath){
        if (kotakAanalystNames.isEmpty() && moslAnalystNames.isEmpty() && ambitAnalystNames.isEmpty() && plAnalystNames.isEmpty()) {
            try {
                FileInputStream file = new FileInputStream(new File(analystNamesFilePath));
                XSSFWorkbook wb = new XSSFWorkbook(file);
                moslAnalystNames = "(";
                ambitAnalystNames = "(";
                axisAnalystNames = "(";
                icicidirectAnalystNames = "(";
                plAnalystNames = "(";
                kotakAanalystNames = "(";

                for (String broker :  brokers) {
                    System.out.println("Loading Analyst Names for "+ broker);
                    XSSFSheet ws = wb.getSheet(broker);
                    boolean isFirstAnalyst = true;
                    for (int i = 0; i <= ws.getLastRowNum(); i++) {
                        XSSFRow row = ws.getRow(i);
                        XSSFCell firstColumnCell = row.getCell(0);
                        String analystName = firstColumnCell.getStringCellValue();
                        if (isFirstAnalyst) {
                            isFirstAnalyst = false;
                            switch (broker) {
                                case "MOSL":
                                    moslAnalystNames = moslAnalystNames + "(" + analystName + ")";
                                    break;
                                case "AMBIT":
                                    ambitAnalystNames = ambitAnalystNames + "(" + analystName + ")";
                                    break;
                                case "AXIS":
                                    axisAnalystNames = axisAnalystNames + "(" + analystName + ")";
                                    break;
                                case "ICICIDIRECT":
                                    icicidirectAnalystNames = icicidirectAnalystNames + "(" + analystName + ")";
                                    break;
                                case "PL":
                                    plAnalystNames = plAnalystNames + "(" + analystName + ")";
                                    break;
                                case "KOTAK":
                                    kotakAanalystNames = kotakAanalystNames + "(" + analystName + ")";
                                    break;
                            }
                        }
                        else {
                            switch (broker) {
                                case "MOSL":
                                    moslAnalystNames = moslAnalystNames + "|("  + analystName + ")";
                                    break;
                                case "AMBIT":
                                    ambitAnalystNames = ambitAnalystNames + "|("  + analystName + ")";
                                    break;
                                case "AXIS":
                                    axisAnalystNames = axisAnalystNames + "|("  + analystName + ")";
                                    break;
                                case "ICICIDIRECT":
                                    icicidirectAnalystNames = icicidirectAnalystNames + "|("  + analystName + ")";
                                    break;
                                case "PL":
                                    plAnalystNames = plAnalystNames + "|("  + analystName + ")";
                                    break;
                                case "KOTAK":
                                    kotakAanalystNames = kotakAanalystNames + "|("  + analystName + ")";
                                    break;
                            }
                        }
                    }
                }
                moslAnalystNames = moslAnalystNames + ")";
                ambitAnalystNames = ambitAnalystNames + ")";
                axisAnalystNames = axisAnalystNames + ")";
                icicidirectAnalystNames = icicidirectAnalystNames + ")";
                plAnalystNames = plAnalystNames + ")";
                kotakAanalystNames = kotakAanalystNames + ")";
                file.close();
            } catch (Exception e) {
                System.out.println("Exception in loadAnalystNames " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadReportDataExtractConfig(String reportDataExtractConfigFilePath){
        try {
            for (String broker : brokers){
                ReportDataExtractConfig reportDataExtractConfig = null;
                // Open the new workbook
                FileInputStream file = new FileInputStream(new File(reportDataExtractConfigFilePath));
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet ws = wb.getSheet(broker);

                XSSFRow headerRow = ws.getRow(0);

                for(int i = 1; i <= ws.getLastRowNum(); i++) {
                    XSSFRow dataRow = ws.getRow(i);
                    if (dataRow != null) {
                        String ticker = dataRow.getCell(0).getStringCellValue();
                        String quarter = dataRow.getCell(1).getStringCellValue();
                        reportDataExtractConfig = loadReportDataExtractConfig(reportDataExtractConfigFilePath, quarter, ticker, broker);
                        configMap.put(quarter + "_" + ticker + "_" + broker, reportDataExtractConfig);
                    }
                }

                wb.close();
                file.close();
            }

        } catch (Exception e) {
            System.out.println("Exception in loadReportDataExtractConfig " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ReportDataExtractConfig loadReportDataExtractConfig(String reportDataExtractConfigFilePath, String quarter, String ticker, String brokerName) {
        ReportDataExtractConfig reportDataExtractConfig = null;
        try {
            // Open the new workbook
            FileInputStream file = new FileInputStream(new File(reportDataExtractConfigFilePath));
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet ws = wb.getSheet(brokerName);
            XSSFRow headerRow = ws.getRow(0);

            // First row is header, Second row is default parameter
            int quarterTickerRowNumber = -1;
            for (int i = 1; i <= ws.getLastRowNum(); i++) {
                XSSFRow row = ws.getRow(i);
                XSSFCell firstColumnCell = row.getCell(0);
                String cellZero = firstColumnCell.getStringCellValue();
                XSSFCell secondColumnCell = row.getCell(1);
                String cellOne = secondColumnCell.getStringCellValue();
                if (cellZero.equals(ticker) && cellOne.equals(quarter)) {
                    quarterTickerRowNumber = i;
                    break;
                }
            }
            if (quarterTickerRowNumber == -1) {
                for (int i = 1; i <= ws.getLastRowNum(); i++) {
                    XSSFRow row = ws.getRow(i);
                    XSSFCell firstColumnCell = row.getCell(0);
                    String cellZero = firstColumnCell.getStringCellValue();
                    XSSFCell secondColumnCell = row.getCell(1);
                    String cellOne = secondColumnCell.getStringCellValue();
                    if (cellZero.equals("DEFAULT") && cellOne.equals(quarter)) {
                        quarterTickerRowNumber = i;
                        break;
                    }
                }
            }

            int tickerColumnPosition = 0;
            int quarterColumnPosition = 1;
            int CMPColumnPosition = -1;
            int TPColumnPosition = -1;
            int MCAPColumnPosition = -1;
            int CMPPATTERNColumnPosition = -1;
            int TPPATTERNColumnPosition = -1;
            int MCAPPATTERNColumnPosition = -1;
            int RATINGPATTERNPostion = -1;

            int INCOME_STATEMENT_PAGEColumnPosition = -1;
            int RATIO_PAGEColumnPosition = -1;
            int VALUATION_PAGEColumnPosition = -1;

            int HEADER_ROW_NAMEColumnPosition = -1;
            int REVENUE_ROW_NAMEColumnPosition = -1;
            int EBITDA_ROW_NAMEColumnPosition = -1;
            int DEPRECIATION_ROW_NAMEColumnPosition = -1;

            int EBITDAMARGIN_ROW_NAMEColumnPosition = -1;
            int ROCE_ROW_NAMEColumnPosition = -1;

            int EVBYEBITDA_ROW_NAMEColumnPosition = -1;

            int Y0ColumnPosition = -1;
            int Y1ColumnPosition = -1;
            int Y2ColumnPosition = -1;

            int MILLIONS_OR_BILLIONSColumnPosition = -1;
            int RESEARCHANALYST1ColumnPosition = -1;
            int RESEARCHANALYST2ColumnPosition = -1;

            // Read header row to get the column index of MCAP header
            for (int i = 2; i < headerRow.getLastCellNum(); i++) {
                XSSFCell headerCell = headerRow.getCell(i);
                if (headerCell.getStringCellValue().equals("CMP")) {
                    CMPColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("TP")) {
                    TPColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("MCAP")) {
                    MCAPColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("CMPPATTERN")) {
                    CMPPATTERNColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("TPPATTERN")) {
                    TPPATTERNColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("MCAPPATTERN")) {
                    MCAPPATTERNColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("RATINGPATTERN")) {
                    RATINGPATTERNPostion = i;
                }

                if (headerCell.getStringCellValue().equals("INCOME_STATEMENT_PAGE")) {
                    INCOME_STATEMENT_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("RATIO_PAGE")) {
                    RATIO_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("VALUATION_PAGE")) {
                    VALUATION_PAGEColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("HEADER_ROW_NAME")) {
                    HEADER_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("REVENUE_ROW_NAME")) {
                    REVENUE_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("EBITDA_ROW_NAME")) {
                    EBITDA_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("DEPRECIATION_ROW_NAME")) {
                    DEPRECIATION_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("EBITDAMARGIN_ROW_NAME")) {
                    EBITDAMARGIN_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("ROCE_ROW_NAME")) {
                    ROCE_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("EVBYEBITDA_ROW_NAME")) {
                    EVBYEBITDA_ROW_NAMEColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("Y0")) {
                    Y0ColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("Y1")) {
                    Y1ColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("Y2")) {
                    Y2ColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("MILLIONS_OR_BILLIONS")) {
                    MILLIONS_OR_BILLIONSColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("RESEARCHANALYST1")) {
                    RESEARCHANALYST1ColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("RESEARCHANALYST2")) {
                    RESEARCHANALYST2ColumnPosition = i;
                }
            }


            XSSFRow dataRow = ws.getRow(quarterTickerRowNumber);
            if (dataRow != null) {
                reportDataExtractConfig = new ReportDataExtractConfig();
                if (CMPColumnPosition != -1) {
                    reportDataExtractConfig.setCMP(dataRow.getCell(CMPColumnPosition).getStringCellValue());
                }
                if (TPColumnPosition != -1) {
                    reportDataExtractConfig.setTP(dataRow.getCell(TPColumnPosition).getStringCellValue());
                }
                if (MCAPColumnPosition != -1) {
                    reportDataExtractConfig.setMCAP(dataRow.getCell(MCAPColumnPosition).getStringCellValue());
                }
                if (CMPPATTERNColumnPosition != -1) {
                    reportDataExtractConfig.setCMPPATTERN(dataRow.getCell(CMPPATTERNColumnPosition).getStringCellValue());
                }
                if (TPPATTERNColumnPosition != -1) {
                    reportDataExtractConfig.setTPPATTERN(dataRow.getCell(TPPATTERNColumnPosition).getStringCellValue());
                }
                if (MCAPPATTERNColumnPosition != -1) {
                    reportDataExtractConfig.setMCAPPATTERN(dataRow.getCell(MCAPPATTERNColumnPosition).getStringCellValue());
                }
                if (RATINGPATTERNPostion != -1) {
                    reportDataExtractConfig.setRATINGPATTERN(dataRow.getCell(RATINGPATTERNPostion).getStringCellValue());
                }

                if (INCOME_STATEMENT_PAGEColumnPosition != -1) {
                    reportDataExtractConfig.setINCOME_STATEMENT_PAGE(dataRow.getCell(INCOME_STATEMENT_PAGEColumnPosition).getStringCellValue());
                }
                if (RATIO_PAGEColumnPosition != -1) {
                    reportDataExtractConfig.setRATIO_PAGE(dataRow.getCell(RATIO_PAGEColumnPosition).getStringCellValue());
                }
                if (VALUATION_PAGEColumnPosition != -1) {
                    reportDataExtractConfig.setVALUATION_PAGE(dataRow.getCell(VALUATION_PAGEColumnPosition).getStringCellValue());
                }

                if (HEADER_ROW_NAMEColumnPosition != -1) {
                    reportDataExtractConfig.setHEADER_ROW_NAME(dataRow.getCell(HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                }
                if (REVENUE_ROW_NAMEColumnPosition != -1) {
                    reportDataExtractConfig.setREVENUE_ROW_NAME(dataRow.getCell(REVENUE_ROW_NAMEColumnPosition).getStringCellValue());
                }
                if (EBITDA_ROW_NAMEColumnPosition != -1) {
                    reportDataExtractConfig.setEBITDA_ROW_NAME(dataRow.getCell(EBITDA_ROW_NAMEColumnPosition).getStringCellValue());
                }
                if (DEPRECIATION_ROW_NAMEColumnPosition != -1) {
                    reportDataExtractConfig.setDEPRECIATION_ROW_NAME(dataRow.getCell(DEPRECIATION_ROW_NAMEColumnPosition).getStringCellValue());
                }

                if (EBITDAMARGIN_ROW_NAMEColumnPosition != -1) {
                    reportDataExtractConfig.setEBITDAMARGIN_ROW_NAME(dataRow.getCell(EBITDAMARGIN_ROW_NAMEColumnPosition).getStringCellValue());
                }
                if (ROCE_ROW_NAMEColumnPosition != -1) {
                    reportDataExtractConfig.setROCE_ROW_NAME(dataRow.getCell(ROCE_ROW_NAMEColumnPosition).getStringCellValue());
                }

                if (EVBYEBITDA_ROW_NAMEColumnPosition != -1) {
                    reportDataExtractConfig.setEVBYEBITDA_ROW_NAME(dataRow.getCell(EVBYEBITDA_ROW_NAMEColumnPosition).getStringCellValue());
                }

                if (Y0ColumnPosition != -1) {
                    reportDataExtractConfig.setY0(dataRow.getCell(Y0ColumnPosition).getStringCellValue().replaceAll("\"", ""));
                }
                if (Y1ColumnPosition != -1) {
                    reportDataExtractConfig.setY1(dataRow.getCell(Y1ColumnPosition).getStringCellValue().replaceAll("\"", ""));
                }
                if (Y2ColumnPosition != -1) {
                    reportDataExtractConfig.setY2(dataRow.getCell(Y2ColumnPosition).getStringCellValue().replaceAll("\"", ""));
                }

                if (MILLIONS_OR_BILLIONSColumnPosition != -1) {
                    reportDataExtractConfig.setMILLIONS_OR_BILLIONS(dataRow.getCell(MILLIONS_OR_BILLIONSColumnPosition).getStringCellValue());
                }

                switch (brokerName) {
                    case "MOSL" :
                        reportDataExtractConfig.setRESEARCHANALYST1(this.moslAnalystNames);
                        break;
                    case "AMBIT" :
                        reportDataExtractConfig.setRESEARCHANALYST1(this.ambitAnalystNames);
                        break;
                    case "AXIS" :
                        reportDataExtractConfig.setRESEARCHANALYST1(this.axisAnalystNames);
                        break;
                    case "ICICIDIRECT" :
                        reportDataExtractConfig.setRESEARCHANALYST1(this.icicidirectAnalystNames);
                        break;
                    case "PL" :
                        reportDataExtractConfig.setRESEARCHANALYST1(this.plAnalystNames);
                        break;
                    case "KOTAK" :
                        reportDataExtractConfig.setRESEARCHANALYST1(this.kotakAanalystNames);
                        break;
                }


                if (RESEARCHANALYST2ColumnPosition != -1) {
                    reportDataExtractConfig.setRESEARCHANALYST2(dataRow.getCell(RESEARCHANALYST2ColumnPosition).getStringCellValue());
                }

                reportDataExtractConfig.setQUARTER(quarter);
                reportDataExtractConfig.setTICKER(ticker);
                reportDataExtractConfig.setBROKER(brokerName);
            }

            wb.close();
            file.close();

        } catch (Exception e) {
            System.out.println("Exception in loadReportDataExtractConfig " + e.getMessage());
            e.printStackTrace();
        }
        return reportDataExtractConfig;
    }

    private ReportDataExtractConfig getReportDataExtractConfig(String reportDataExtractConfigFilePath, String quarter, String ticker, String brokerName) {
        ReportDataExtractConfig reportDataExtractConfig = null;

        if (this.configMap.isEmpty()) {
            // load configMap
            loadReportDataExtractConfig(reportDataExtractConfigFilePath);
        }
        if (this.configMap.containsKey(quarter + "_" + ticker + "_" + brokerName)) {
            reportDataExtractConfig = configMap.get(quarter + "_" + ticker + "_" + brokerName);
            return reportDataExtractConfig;
        } else if (this.configMap.containsKey(quarter + "_" + "DEFAULT" + "_" + brokerName)) {
            reportDataExtractConfig = configMap.get(quarter + "_" + "DEFAULT" + "_" + brokerName);
            reportDataExtractConfig.setTICKER(ticker);
            return reportDataExtractConfig;
        }

        return reportDataExtractConfig;
    }

    private static AnalystRecoExtractor getExtractor(String brokerName) {
        switch (brokerName) {
            case "MOSL":
                return new AnalystRecoExtractorMOSL();
            case "AMBIT":
                return new AnalystRecoExtractorAMBIT();
            case "KOTAK":
                return new AnalystRecoExtractorKOTAK();
            case "PL":
                return new AnalystRecoExtractorPL();
            /*case "AXIS":
                return new AnalystRecoExtractorAXIS();
            case "ICICIDIRECT":
                return new AnalystRecoExtractorICICIDIRECT();
             */
            default:
                throw new IllegalArgumentException("Invalid broker name: " + brokerName);
        }
    }

    protected String getReportDate(String dateLine, long lastModifiedDate, String pattern, String inputDateformat, ReportDataExtractConfig rdec, String broker) {

        if (dateLine == null) {
            System.out.println("\n\n ********** Exception ********* \n\nSetting Date as a last modified date for Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + rdec.getBROKER() + " from line - " + dateLine + "\n\n ********** Exception ********* \n\n");
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastModifiedDate));
        }

        // The regular expression pattern
        // Pattern will match date in the form 01 January 2023/01 Jan 2023/1 January 2023/01 Jan 23/
//        String pattern = "\\d{1,2}\\s*+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s*+\\d{2,4}";

        // Create a Pattern object
        Pattern p = Pattern.compile(pattern);

        // Create a Matcher object for the input string
        Matcher m = p.matcher(dateLine);

        // Date String to be return
        String dateString = "";

        // Check if the pattern matches the input string and print the matched string
        if (m.find()) {
            //Date object
            Date date = null;
            // Create a SimpleDateFormat object with the pattern "dd MMMMM yyyy"
            SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputDateformat);
            // Targeted Date Format
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {

                // Parse the matched string to a Date object
                date = inputDateFormat.parse(m.group().replace(",", ""));
                // Format the Date object to a String
                String formatted = inputDateFormat.format(date);

                dateString = targetFormat.format(date);
            } catch (Exception e) {
                System.out.println("\n\n ********** Exception ********* \n\nSetting returning date as File Last Modified Date. Error in parsing report date from  Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + rdec.getBROKER() + " from line - " + dateLine + "\n\n ********** Exception ********* \n\n");
                date = new Date (lastModifiedDate);
                dateString = targetFormat.format(date);
            }
        }
        return dateString;
    }

    protected double getMCapFromBillion(String mcapLine, ReportDataExtractConfig rdec, int group, String broker) {
        String mcap = "";
        double mcapNumber = 0;
        if (mcapLine != null && !mcapLine.isEmpty()) {
            // Extracting cmp
            Pattern mcapPattern = Pattern.compile(rdec.getMCAPPATTERN());
            Matcher mcapMatcher = mcapPattern.matcher(mcapLine);
            if (mcapMatcher.find()) {
                mcap = mcapMatcher.group(group).replace(",", "");
                mcapNumber = Double.parseDouble(mcap) * 100;
            }
        } else {
            System.out.println("\n\n ********** Exception ********* \n\nSetting MCap as zero. Error in parsing MCap from  Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + rdec.getBROKER() + "\n\n ********** Exception ********* \n\n" );
            mcapNumber = 0;
        }
        return mcapNumber;
    }

    protected int getCMP(String cmpLine, ReportDataExtractConfig rdec, int group, String broker) {
        int cmp = 0;
        if (cmpLine != null && !cmpLine.isEmpty()) {
            cmpLine = cmpLine.replaceAll("IN\\s+R", "INR");
            Matcher m = Pattern.compile(rdec.getCMPPATTERN()).matcher(cmpLine);
            if (m.find()) {
                String strCMP = m.group(group).replace(",", "");
                cmp = Integer.parseInt(strCMP);
            }
        } else {
            System.out.println("\n\n ********** Exception ********* \n\nSetting CMP as zero. Error in parsing CMP from  Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + broker + "\n\n ********** Exception ********* \n\n" );
            cmp = 0;
        }
        return cmp;
    }

    protected int getTP(String tpLine, ReportDataExtractConfig rdec, int group, String broker) {
        int tp = 0;
        if (tpLine != null && !tpLine.isEmpty()) {
            Matcher m = Pattern.compile(rdec.getTPPATTERN()).matcher(tpLine.substring(tpLine.indexOf(rdec.getTP()),tpLine.length()));
            if (m.find()) {
                String strNumber = m.group(group).replace(",", "");
                tp = Integer.parseInt(strNumber);
            }
        } else {
            System.out.println("\n\n ********** Exception ********* \n\nSetting TP as zero. Error in parsing TP from  Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + rdec.getBROKER() + "\n\n ********** Exception ********* \n\n" );
            tp = 0;
        }
        return tp;
    }

    protected String getValueFromRE(String content, String pattern, int group, ReportDataExtractConfig rdec) {
        String value = "";
        if (content != null && !content.isEmpty() && pattern != null && !pattern.isEmpty()) {
            Matcher m = Pattern.compile(pattern).matcher(content);
            if (m.find()) {
                value = m.group(group);
            }
        } else {
            System.out.println("\n\n ********** Exception ********* \n\nSetting TP as zero. Error in parsing TP from  Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + rdec.getBROKER() + "\n\n ********** Exception ********* \n\n" );
            value = "";
        }
        return value;
    }

    protected String getAnalyst(String analystNames, String pageContent, ReportDataExtractConfig rdec, int analyst1LineNumber){
        Pattern listOfAnalystPattern = Pattern.compile(rdec.getRESEARCHANALYST1(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = listOfAnalystPattern.matcher(pageContent);
        StringBuilder sb = new StringBuilder();
        sb.append(analystNames);

        while (matcher.find()) {
            String analystName = matcher.group().trim();
            if (!analystName.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(analystName);
            }
        }
        analystNames = sb.toString();
        analystNames = capitalizeFirstChar(analystNames).
                replace(", Cfa", ", CFA").
                replace(", Ca", ", CA").
                trim();
        if (analystNames.trim().endsWith(";"))
            analystNames = analystNames.substring(0,analystNames.length()-1);
        return analystNames;
    }

    protected String getAnalyst(String analystNames, String pageContent, ReportDataExtractConfig rdec){
        Pattern listOfAnalystPattern = Pattern.compile(rdec.getRESEARCHANALYST1(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = listOfAnalystPattern.matcher(pageContent);
        StringBuilder sb = new StringBuilder();
        sb.append(analystNames);

        while (matcher.find()) {
            String analystName = matcher.group().trim();
            if (!analystName.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(analystName);
            }
        }
        analystNames = sb.toString();
        analystNames = capitalizeFirstChar(analystNames).
                replace(", Cfa", ", CFA").
                replace(", Ca", ", CA").
                trim();
        if (analystNames.trim().endsWith(";"))
            analystNames = analystNames.substring(0,analystNames.length()-1);
        return analystNames;
    }

    protected int getPageNumberForMatchingPattern(PdfReader pdfReader, int fromPage, int noOfPages, String strPattern, ReportDataExtractConfig rdec, boolean isReverseOrder, String broker){
        int incomeStatementPageNumber = -1;
        Pattern pattern = Pattern.compile(strPattern);
        int startPage = fromPage;
        int endPage = noOfPages;
        int step = 1;
        if (isReverseOrder) {
            startPage = noOfPages;
            endPage = fromPage;
            step = -1;
        }
        try {
            for (int i = startPage; i != endPage + step; i += step) {
                String pageContent = PdfTextExtractor.getTextFromPage(pdfReader, i);
                Matcher matcher = pattern.matcher(pageContent);
                if (matcher.find()) {
                    incomeStatementPageNumber = i;
                    break;
                }
            }
        } catch (Exception e){
            System.out.println("\n\n ********** Exception ********* \n\nError in finding Page No. for pattern : " + strPattern + " from  Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + rdec.getBROKER() + "\n\n ********** Exception ********* \n\n" );
            incomeStatementPageNumber = -1;
        }
        return incomeStatementPageNumber;
    }

    protected int getPageNumberForMatchingPattern(PdfReader pdfReader, int fromPage, int noOfPages, String strPattern, ReportDataExtractConfig rdec, String broker){

        return getPageNumberForMatchingPattern(pdfReader, fromPage,noOfPages, strPattern, rdec, false, rdec.getBROKER());
    }

    protected int getLineNumberForMatchingPattern(String[] lines, int fromLine, String strPattern, ReportDataExtractConfig rdec, boolean isReverseOrder, String broker){

        int lineNumber = -1;
        Pattern pattern = Pattern.compile(strPattern);
        int startLine = fromLine;
        int endLine = lines.length-1;
        int step = 1;
        if (isReverseOrder) {
            startLine = lines.length-1;
            endLine = fromLine;
            step = -1;
        }

        try {
            for (int i = startLine; i != endLine + step; i += step) {
                Matcher matcher = pattern.matcher(lines[i]);
                if (matcher.find()) {
                    lineNumber = i;
                    break;
                }
            }
        } catch (Exception e){
            System.out.println("\n\n ********** Exception ********* \n\nError in finding Line No. for pattern : " + strPattern + " from  Source file " + rdec.getQUARTER() + "_" + rdec.getTICKER() + "_" + rdec.getBROKER() + "\n\n ********** Exception ********* \n\n" );
            lineNumber = -1;
        }
        return lineNumber;
    }

    protected int getLineNumberForMatchingPattern(String[] lines, int fromLine, String strPattern, ReportDataExtractConfig rdec, String broker){

        return getLineNumberForMatchingPattern(lines,fromLine,strPattern,rdec,false, broker);
    }

    protected String[] getDataColumnsForHeader(String headerLine, String rowHeaderPattern, int headerColumnLength){
        String[] dataColumns = getDataColumnsForHeader(headerLine, rowHeaderPattern);
        if (dataColumns.length > headerColumnLength) {
            // there are additional spaces between two digits then first remove single space and check
            String modfiedHeaderLine = headerLine.replaceAll("(?<=\\d)\\s(?=\\d)", "").trim();
            dataColumns = getDataColumnsForHeader(modfiedHeaderLine, rowHeaderPattern);
            // check if columns are still mor then perhaps there is still one more addtional space remove that too
            if (dataColumns.length > headerColumnLength) {
                modfiedHeaderLine = modfiedHeaderLine.replaceAll("(?<=\\d)\\s\\s(?=\\d)", "").trim();
                dataColumns = getDataColumnsForHeader(modfiedHeaderLine, rowHeaderPattern);
            }
        }
        if (dataColumns.length > headerColumnLength) {
            // there are additional spaces between minus sign and digit then first remove single space and check
            String modfiedHeaderLine = headerLine.replaceAll("(-)\\s(?=\\d)", "").trim();
            dataColumns = getDataColumnsForHeader(modfiedHeaderLine, rowHeaderPattern);
            // check if columns are still mor then perhaps there is still one more addtional space remove that too
            if (dataColumns.length > headerColumnLength) {
                modfiedHeaderLine = modfiedHeaderLine.replaceAll("(-)\\s\\s(?=\\d)", "").trim();
                dataColumns = getDataColumnsForHeader(modfiedHeaderLine, rowHeaderPattern);
            }
        }
        return dataColumns;
    }

    protected String[] getCorrectNumbers(String[] dataColumns) {
        int len = dataColumns.length;
        String modifiedDataColumnsString = dataColumns[0];
        int prevLen = modifiedDataColumnsString.length();

        for (int i = 1; i < len; i++) {
            String cur = dataColumns[i];

            int curLen = cur.length();
            if (prevLen != 0 && Math.abs(curLen - prevLen) > 2) {
                i++;
                if(i < len-1) {
                    modifiedDataColumnsString += cur + " " + dataColumns[i];
                    prevLen = dataColumns[i].length();
//                    System.out.println(" modifiedDataColumnsString_1 - "+ modifiedDataColumnsString);
                }
                if (i == len-2 && curLen > prevLen) {
                    modifiedDataColumnsString += dataColumns[i] + dataColumns[dataColumns.length-1];
//                    System.out.println(" modifiedDataColumnsString_3 - "+ modifiedDataColumnsString);
                }
                if (i == len-2 && curLen < prevLen) {
                    modifiedDataColumnsString += dataColumns[dataColumns.length-1];
//                    System.out.println(" modifiedDataColumnsString_4 - "+ modifiedDataColumnsString);
                }
            } else {
                modifiedDataColumnsString += " " + cur;
                prevLen = curLen;
//                System.out.println(" modifiedDataColumnsString_2 - "+ modifiedDataColumnsString);
            }
        }
        dataColumns = modifiedDataColumnsString.split(" ");
        dataColumns = Arrays.stream(dataColumns)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        return dataColumns;
    }

    protected String[] getDataColumnsForHeader(String headerLine, String rowHeaderPattern){
        String[] dataColumns = null;
        Pattern pattern = Pattern.compile(rowHeaderPattern);
        Matcher matcher = pattern.matcher(headerLine.trim());
        if(!rowHeaderPattern.isEmpty()) {
            if (matcher.find()) {
                String rowHeader = matcher.group(1);
                int strDataColumnsIndex = headerLine.trim().indexOf(rowHeader)+rowHeader.length();
                String strDataColumns = headerLine.trim().substring(strDataColumnsIndex, headerLine.trim().length()); // headerLine.replace(rowHeader, "");
                dataColumns = strDataColumns.trim().split(" ");
                dataColumns = Arrays.stream(dataColumns)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);
            }
        } else {
            dataColumns = headerLine.trim().split(" ");
            dataColumns = Arrays.stream(dataColumns)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
        return dataColumns;
    }

    protected int getIndexOfTheYear(String[] dataColumns, String yearPattern) {
        int index = -1;
        Pattern pattern = Pattern.compile(yearPattern);
        for (int i = 0; i < dataColumns.length; i++) {
            Matcher matcher = pattern.matcher(dataColumns[i].trim());
            if (matcher.find()) {
                index = i;
                break;
            }
        }
        return index;
    }

    protected String capitalizeFirstChar(String s) {
        // Split the string by spaces
        String[] words = s.trim().split(" ");

        // Capitalize the first letter of each word and lowercase the rest
        StringBuilder output = new StringBuilder();
        for (String word : words) {
            if(word.length()>1) {
                output.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        String strOutput = output.toString().trim();
        if (strOutput.equalsIgnoreCase("NA") || strOutput.equalsIgnoreCase("NR") || strOutput.equalsIgnoreCase("UR"))
            strOutput = "Not Rated";

        return strOutput;
    }

}
