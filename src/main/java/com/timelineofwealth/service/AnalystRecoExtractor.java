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

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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

    protected String QUARTER;
    protected String RECO_PAGE;
    protected String CMP;
    protected String TP;
    protected String MCAP;
    protected String CMPPATTERN;
    protected String TPPATTERN;
    protected String MCAPPATTERN;
    protected String RATINGPATTERN;

    protected String INCOME_STATEMENT_PAGE;
    protected String MILLIONS_OR_BILLIONS_FLAG;
    protected String MILLIONS_OR_BILLIONS;
    protected String HEADER_ROW_NAME;
    protected String REVENUE_ROW_NAME;
    protected String EBITDA_ROW_NAME;
    protected String DEPRECIATION_ROW_NAME;
    protected String PAT_ROW_NAME;
    protected String EPS_ROW_NAME;

    protected String MARGIN_PAGE;
    protected String MARGIN_HEADER_ROW_NAME;
    protected String EBITDAMARGIN_ROW_NAME;

    protected String RATIO_PAGE;
    protected String RATIO_HEADER_ROW_NAME;
    protected String ROCE_ROW_NAME;

    protected String VALUATION_PAGE;
    protected String VALUATION_HEADER_ROW_NAME;
    protected String EVBYEBITDA_ROW_NAME;

    protected String Y0;
    protected String Y1;
    protected String Y2;
    protected String Y3;

    protected String RESEARCHANALYST1;
    protected String RESEARCHANALYST2;

    protected String AUM_PAGE;
    protected String AUM_HEADER_ROW_NAME;
    protected String AUM_ROW_NAME;
    protected String AUM_MILLIONS_OR_BILLIONS_FLAG;
    protected String AUM_MILLIONS_OR_BILLIONS;

    protected String CREDITCOSTS_PAGE;
    protected String CREDITCOSTS_HEADER_ROW_NAME;
    protected String CREDITCOSTS_ROW_NAME;

    protected String NPA_PAGE;
    protected String NPA_HEADER_ROW_NAME;
    protected String GNPA_ROW_NAME;
    protected String NNPA_ROW_NAME;

    protected String pageContentReco = null;
    protected String[] linesRecoPage = null;

    protected String pageContentIncomeStmt = null;
    protected String[] linesIncomeStmt = null;

    protected String pageContentMargin = null;
    protected String[] linesMargin = null;

    protected String pageContentRatio = null;
    protected String[] linesRatio = null;

    protected String pageContentValuation = null;
    protected String[] linesValuation = null;

    protected String pageContentAUM = null;
    protected String[] linesAUM = null;

    protected String pageContentCreditCost = null;
    protected String[] linesCreditCost = null;

    protected String pageContentNPA = null;
    protected String[] linesNPA = null;

    protected String dateString = "";
    protected BigDecimal mcap = new BigDecimal("0");
    protected String cmp = "0";
    protected String rating = "";
    protected String targetPrice = "0";
    protected String analystNames = "";

    protected String headerRecoPage = "";
    protected String[] headerColumnsRecoPage = null;
    protected String headerIncomeStmt = "";
    protected String[] headerColumnsIncomeStmt = null;
    protected String headerMargin = "";
    protected String[] headerColumnsMargin = null;
    protected String headerRatio = "";
    protected String[] headerColumnsRatio = null;
    protected String headerValuation = "";
    protected String[] headerColumnsValuation = null;
    protected String headerAUM = "";
    protected String[] headerColumnsAUM = null;
    protected String headerCreditCosts = "";
    protected String[] headerColumnsCreditCosts = null;
    protected String headerNPA = "";
    protected String[] headerColumnsNPA = null;


    protected Integer headerRecoPageLineNumber = null;
    protected Integer valuationPageNumber = null;
    protected Integer headerFirstPageLineNumber = null;
    protected Integer headerIncomeStmtLineNumber = null;
    protected Integer headerMarginLineNumber = null;
    protected Integer headerRatioLineNumber = null;
    protected Integer headerValuationLineNumber = null;
    protected Integer headerAUMLineNumber = null;
    protected Integer headerCreditCostLineNumber = null;
    protected Integer headerNPALineNumber = null;
    protected Integer revenueLineNumber = null;
    protected Integer ebitdaLineNumber = null;
    protected Integer y0ColumnNumberOnIncStmt = null, y1ColumnNumberOnIncStmt = null, y2ColumnNumberOnIncStmt = null, y3ColumnNumberOnIncStmt = null;
    protected Integer y0ColumnNumberOnRatio = null, y1ColumnNumberOnRatio = null, y2ColumnNumberOnRatio = null, y3ColumnNumberOnRatio = null;

    protected BigDecimal y0EBITDANumber = null, y1EBITDANumber = null, y2EBITDANumber = null, y3EBITDANumber = null;
    protected BigDecimal y0DepreciationNumber = null, y1DepreciationNumber = null, y2DepreciationNumber = null, y3DepreciationNumber = null;

    protected ReportParameters reportParameters = new ReportParameters();

    protected PdfReader pdfReader = null;

    private Map<String, ReportDataExtractConfig> configMap = new HashMap<>();

    public static void main(String[] args) {
        boolean isFinancialReport = false;

        try {
            // Load config file
            Properties prop = new Properties();
            String reportConfigFileVersion = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\reportconfig.properties";
            // Define the output file path
            String reportExtractFile = "";

            FileInputStream input = new FileInputStream(reportConfigFileVersion);
            prop.load(input);
            input.close();

            // Get reportDataExtractConfigFilePath
            String reportDataExtractConfigFilePath = prop.getProperty("ReportDataExtractConfigFilePath");
            String analystNamesFilePath = prop.getProperty("AnalystNamesFilePath");
            AnalystRecoExtractor analystRecoExtractor = new AnalystRecoExtractor();
            analystRecoExtractor.loadAnalystNames(analystNamesFilePath);
//            analystRecoExtractor.loadReportDataExtractConfig(reportDataExtractConfigFilePath);

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
                    if(reportFilePath.contains("Financials-"))
                        isFinancialReport = true;
                    else
                        isFinancialReport = false;


                    // Load ReportDataConfig Parameters for the ticker
                    // ReportDataExtractConfig reportDataExtractConfig = analystRecoExtractor.getReportDataExtractConfig(reportDataExtractConfigFilePath, quarter, ticker, brokerName);
                    //Fix: Dont load all parameters but load only one that is required
                    ReportDataExtractConfig reportDataExtractConfig = analystRecoExtractor.loadReportDataExtractConfig(reportDataExtractConfigFilePath, quarter, ticker, brokerName);

                    // get Subclass based on broker
                    AnalystRecoExtractor extractor = getExtractor(brokerName);

                    // Call getReportParameters
                    int currentFile = i + 1;
                    System.out.println("\n\n ********** New File ********* \n");
                    System.out.println("Begin Updating File No. " + currentFile + " --> " +quarter + "_" + ticker + "_" + brokerName);
                    ReportParameters reportParameters = extractor.getReportParameters(reportFilePath, reportDataExtractConfig, isFinancialReport);
                    // Call saveReportParameters
                    // Define the content to append
                    String tickerValue = (ticker != null) ? ticker : "";
                    String quarterValue = (quarter != null) ? quarter : "";
                    String reportDateValue = (reportParameters.getReportDate() != null) ? reportParameters.getReportDate() : "";
                    String mcapValue = (reportParameters.getMcap() != null) ? reportParameters.getMcap().toString()  : "";
                    String priceValue = (reportParameters.getPrice() != null) ? reportParameters.getPrice().toString()  : "";
                    String targetValue = (reportParameters.getTarget() != null) ? reportParameters.getTarget().toString()  : "";

                    String contentToAppend = tickerValue + "\t" + quarterValue + "\t" + reportDateValue + "\t" +
                            mcapValue + "\t" + priceValue + "\t" + "0.0\t" + brokerName + "\t" +
                            reportParameters.getRating() + "\t" + targetValue + "\t0.0\t" +
                            ((reportParameters.getY0Revenue() != null) ? reportParameters.getY0Revenue() : "") + "\t" +
                            ((reportParameters.getY1Revenue() != null) ? reportParameters.getY1Revenue() : "") + "\t" +
                            ((reportParameters.getY2Revenue() != null) ? reportParameters.getY2Revenue() : "") + "\t" +
                            ((reportParameters.getRevenueChange() != null) ? reportParameters.getRevenueChange() : "") + "\t" +
                            "=" + ((reportParameters.getY0EBIT() != null) ? reportParameters.getY0EBIT() : "") + "\t=" +
                            ((reportParameters.getY1EBIT() != null) ? reportParameters.getY1EBIT() : "") + "\t=" +
                            ((reportParameters.getY2EBIT() != null) ? reportParameters.getY2EBIT() : "") + "\t" +
                            ((reportParameters.getEbitChange() != null) ? reportParameters.getEbitChange() : "") + "\t" +
                            ((reportParameters.getY0OPM() != null) ? reportParameters.getY0OPM() : "") + "\t" +
                            ((reportParameters.getY1OPM() != null) ? reportParameters.getY1OPM() : "") + "\t" +
                            ((reportParameters.getY2OPM() != null) ? reportParameters.getY2OPM() : "") + "\t" +
                            ((reportParameters.getY0ROCE() != null) ? reportParameters.getY0ROCE() : "") + "\t" +
                            ((reportParameters.getY1ROCE() != null) ? reportParameters.getY1ROCE() : "") + "\t" +
                            ((reportParameters.getY2ROCE() != null) ? reportParameters.getY2ROCE() : "") + "\t" +
                            ((reportParameters.getY0EVBYEBIT() != null) ? reportParameters.getY0EVBYEBIT() : "") + "\t" +
                            ((reportParameters.getY1EVBYEBIT() != null) ? reportParameters.getY1EVBYEBIT() : "") + "\t" +
                            ((reportParameters.getY2EVBYEBIT() != null) ? reportParameters.getY2EVBYEBIT() : "") + "\t" +
                            ((reportParameters.getY0AUM() != null) ? reportParameters.getY0AUM() : "") + "\t" +
                            ((reportParameters.getY1AUM() != null) ? reportParameters.getY1AUM() : "") + "\t" +
                            ((reportParameters.getY2AUM() != null) ? reportParameters.getY2AUM() : "") + "\t0.0\t" +
                            ((reportParameters.getY0CreditCost() != null) ? reportParameters.getY0CreditCost() : "") + "\t" +
                            ((reportParameters.getY1CreditCost() != null) ? reportParameters.getY1CreditCost() : "") + "\t" +
                            ((reportParameters.getY2CreditCost() != null) ? reportParameters.getY2CreditCost() : "") + "\t" +
                            ((reportParameters.getY0GNPA() != null) ? reportParameters.getY0GNPA() : "") + "\t" +
                            ((reportParameters.getY1GNPA() != null) ? reportParameters.getY1GNPA() : "") + "\t" +
                            ((reportParameters.getY2GNPA() != null) ? reportParameters.getY2GNPA() : "") + "\t" +
                            ((reportParameters.getY0NNPA() != null) ? reportParameters.getY0NNPA() : "") + "\t" +
                            ((reportParameters.getY1NNPA() != null) ? reportParameters.getY1NNPA() : "") + "\t" +
                            ((reportParameters.getY2NNPA() != null) ? reportParameters.getY2NNPA() : "") + "\t" +
                            ((reportParameters.getAnalystsNames() != null) ? reportParameters.getAnalystsNames() : "");

                    System.out.println(contentToAppend);
                    // Append content to file
                    if(reportExtractFile.isEmpty()){
                        Path path = Paths.get(excelFilePath);
                        String basePath = path.getParent().toString();
                        reportExtractFile = basePath + "\\ReportExtract.txt";
                    }

                    extractor.saveReportParametersNewFormat(tickerValue, reportParameters, Paths.get(excelFilePath).getParent().toString());

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportExtractFile, true))) {
                        writer.newLine(); // Start on a new line
                        writer.write(contentToAppend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    extractor.saveReportParameters(reportParameters, excelFilePath);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in AnalystRecoExtractor.main " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected ReportParameters getReportParameters(String reportFilePath, ReportDataExtractConfig reportDataExtractConfig, boolean isFinancialReport) {
        return null;
    }

    protected void setConstants(ReportDataExtractConfig rdec) {
        QUARTER = rdec.getQUARTER();
        RECO_PAGE = rdec.getRECO_PAGE();
        CMP = rdec.getCMP();
        TP = rdec.getTP();
        MCAP = rdec.getMCAP();
        CMPPATTERN = rdec.getCMPPATTERN();
        TPPATTERN = rdec.getTPPATTERN();
        MCAPPATTERN = rdec.getMCAPPATTERN();
        RATINGPATTERN = rdec.getRATINGPATTERN();

        INCOME_STATEMENT_PAGE = rdec.getINCOME_STATEMENT_PAGE();
        MILLIONS_OR_BILLIONS = rdec.getMILLIONS_OR_BILLIONS();
        MILLIONS_OR_BILLIONS_FLAG = "M";
        HEADER_ROW_NAME = rdec.getHEADER_ROW_NAME();
        REVENUE_ROW_NAME = rdec.getREVENUE_ROW_NAME();
        EBITDA_ROW_NAME = rdec.getEBITDA_ROW_NAME();
        DEPRECIATION_ROW_NAME = rdec.getDEPRECIATION_ROW_NAME();
        PAT_ROW_NAME = rdec.getPAT_ROW_NAME();
        EPS_ROW_NAME = rdec.getEPS_ROW_NAME();

        MARGIN_PAGE = rdec.getMARGIN_PAGE();
        MARGIN_HEADER_ROW_NAME = rdec.getMARGIN_HEADER_ROW_NAME();
        EBITDAMARGIN_ROW_NAME = rdec.getEBITDAMARGIN_ROW_NAME();

        RATIO_PAGE = rdec.getRATIO_PAGE();
        RATIO_HEADER_ROW_NAME = rdec.getRATIO_HEADER_ROW_NAME();
        ROCE_ROW_NAME = rdec.getROCE_ROW_NAME();

        VALUATION_PAGE = rdec.getVALUATION_PAGE();
        VALUATION_HEADER_ROW_NAME = rdec.getVALUATION_HEADER_ROW_NAME();
        EVBYEBITDA_ROW_NAME = rdec.getEVBYEBITDA_ROW_NAME();

        Y0 = rdec.getY0();
        Y1 = rdec.getY1();
        Y2 = rdec.getY2();
        Y3 = rdec.getY3();

        RESEARCHANALYST1 = rdec.getRESEARCHANALYST1();
        RESEARCHANALYST2 = rdec.getRESEARCHANALYST2();

        AUM_PAGE = rdec.getAUM_PAGE();
        AUM_HEADER_ROW_NAME = rdec.getAUM_HEADER_ROW_NAME();
        AUM_ROW_NAME = rdec.getAUM_ROW_NAME();
        AUM_MILLIONS_OR_BILLIONS = rdec.getAUM_MILLIONS_OR_BILLIONS();
        AUM_MILLIONS_OR_BILLIONS_FLAG = "M";

        CREDITCOSTS_PAGE = rdec.getCREDITCOSTS_PAGE();
        CREDITCOSTS_HEADER_ROW_NAME = rdec.getCREDITCOSTS_HEADER_ROW_NAME();
        CREDITCOSTS_ROW_NAME = rdec.getCREDITCOSTS_ROW_NAME();

        NPA_PAGE = rdec.getNPA_PAGE();
        NPA_HEADER_ROW_NAME = rdec.getNPA_HEADER_ROW_NAME();
        GNPA_ROW_NAME = rdec.getGNPA_ROW_NAME();
        NNPA_ROW_NAME = rdec.getNNPA_ROW_NAME();

        reportParameters.setQuarter(QUARTER);

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

                        if (reportParameters.getY0AUM() != null) {
                            XSSFCell y0AUMCell = row.getCell(26);
                            y0AUMCell.setCellValue(reportParameters.getY0AUM().doubleValue());
                        }

                        if (reportParameters.getY1AUM() != null) {
                            XSSFCell y1AUMCell = row.getCell(27);
                            y1AUMCell.setCellValue(reportParameters.getY1AUM().doubleValue());
                        }

                        if (reportParameters.getY2AUM() != null) {
                            XSSFCell y2AUMCell = row.getCell(28);
                            y2AUMCell.setCellValue(reportParameters.getY2AUM().doubleValue());
                        }

                        if(reportParameters.getY0CreditCost() != null) {
                            XSSFCell y0CreditCostCell = row.getCell(30);
                            y0CreditCostCell.setCellValue(reportParameters.getY0CreditCost().doubleValue());
                        }

                        if(reportParameters.getY1CreditCost() != null) {
                            XSSFCell y1CreditCostCell = row.getCell(31);
                            y1CreditCostCell.setCellValue(reportParameters.getY1CreditCost().doubleValue());
                        }

                        if(reportParameters.getY2CreditCost() != null) {
                            XSSFCell y2CreditCostCell = row.getCell(32);
                            y2CreditCostCell.setCellValue(reportParameters.getY2CreditCost().doubleValue());
                        }

                        if(reportParameters.getY0GNPA() != null) {
                            XSSFCell y0GNPACell = row.getCell(33);
                            y0GNPACell.setCellValue(reportParameters.getY0GNPA().doubleValue());
                        }

                        if(reportParameters.getY1GNPA() != null) {
                            XSSFCell y1GNPACell = row.getCell(34);
                            y1GNPACell.setCellValue(reportParameters.getY1GNPA().doubleValue());
                        }

                        if(reportParameters.getY2GNPA() != null) {
                            XSSFCell y2GNPACell = row.getCell(35);
                            y2GNPACell.setCellValue(reportParameters.getY2GNPA().doubleValue());
                        }

                        if(reportParameters.getY0NNPA() != null) {
                            XSSFCell y0NNPACell = row.getCell(36);
                            y0NNPACell.setCellValue(reportParameters.getY0NNPA().doubleValue());
                        }

                        if(reportParameters.getY1NNPA() != null) {
                            XSSFCell y1NNPACell = row.getCell(37);
                            y1NNPACell.setCellValue(reportParameters.getY1NNPA().doubleValue());
                        }

                        if(reportParameters.getY2NNPA() != null) {
                            XSSFCell y2NNPACell = row.getCell(38);
                            y2NNPACell.setCellValue(reportParameters.getY2NNPA().doubleValue());
                        }

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
            int RECO_PAGEColumnPosition = -1;
            int CMPColumnPosition = -1;
            int TPColumnPosition = -1;
            int MCAPColumnPosition = -1;
            int CMPPATTERNColumnPosition = -1;
            int TPPATTERNColumnPosition = -1;
            int MCAPPATTERNColumnPosition = -1;
            int RATINGPATTERNPostion = -1;

            int INCOME_STATEMENT_PAGEColumnPosition = -1;
            int MILLIONS_OR_BILLIONSColumnPosition = -1;
            int HEADER_ROW_NAMEColumnPosition = -1;
            int REVENUE_ROW_NAMEColumnPosition = -1;
            int EBITDA_ROW_NAMEColumnPosition = -1;
            int DEPRECIATION_ROW_NAMEColumnPosition = -1;
            int PAT_ROW_NAMEColumnPosition = -1;
            int EPS_ROW_NAMEColumnPosition = -1;

            int MARGIN_PAGEColumnPosition = -1;
            int MARGIN_HEADER_ROW_NAMEColumnPosition = -1;
            int EBITDAMARGIN_ROW_NAMEColumnPosition = -1;

            int RATIO_PAGEColumnPosition = -1;
            int RATIO_HEADER_ROW_NAMEColumnPosition = -1;
            int ROCE_ROW_NAMEColumnPosition = -1;

            int VALUATION_PAGEColumnPosition = -1;
            int VALUATION_HEADER_ROW_NAMEColumnPosition = -1;
            int EVBYEBITDA_ROW_NAMEColumnPosition = -1;

            int Y0ColumnPosition = -1;
            int Y1ColumnPosition = -1;
            int Y2ColumnPosition = -1;
            int Y3ColumnPosition = -1;

            int RESEARCHANALYST1ColumnPosition = -1;
            int RESEARCHANALYST2ColumnPosition = -1;

            int AUM_PAGEColumnPosition = -1;
            int AUM_HEADER_ROW_NAMEColumnPosition = -1;
            int AUMColumnPosition  = -1;
            int AUM_MILLIONS_OR_BILLIONSColumnPosition = -1;

            int CREDITCOSTS_PAGEColumnPosition = -1;
            int CREDITCOSTS_HEADER_ROW_NAMEColumnPosition = -1;
            int CREDITCOSTSColumnPosition = -1;

            int NPA_PAGEColumnPosition = -1;
            int NPA_HEADER_ROW_NAMEColumnPosition = -1;
            int GNPAColumnPosition = -1;
            int NNPAColumnPosition = -1;

            // Read header row to get the column index of MCAP header
            for (int i = 2; i < headerRow.getLastCellNum(); i++) {
                XSSFCell headerCell = headerRow.getCell(i);
                if (headerCell.getStringCellValue().equals("RECO_PAGE")) {
                    RECO_PAGEColumnPosition = i;
                }
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
                if (headerCell.getStringCellValue().equals("MILLIONS_OR_BILLIONS")) {
                    MILLIONS_OR_BILLIONSColumnPosition = i;
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
                if (headerCell.getStringCellValue().equals("PROFIT_ROW_NAME")) {
                    PAT_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("EPS_ROW_NAME")) {
                    EPS_ROW_NAMEColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("MARGIN_PAGE")) {
                    MARGIN_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("MARGIN_HEADER_ROW_NAME")){
                    MARGIN_HEADER_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("EBITDAMARGIN_ROW_NAME")) {
                    EBITDAMARGIN_ROW_NAMEColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("RATIO_PAGE")) {
                    RATIO_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("RATIO_HEADER_ROW_NAME")){
                    RATIO_HEADER_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("ROCE_ROW_NAME")) {
                    ROCE_ROW_NAMEColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("VALUATION_PAGE")) {
                    VALUATION_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("VALUATION_HEADER_ROW_NAME")){
                    VALUATION_HEADER_ROW_NAMEColumnPosition = i;
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
                if (headerCell.getStringCellValue().equals("Y3")) {
                    Y3ColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("RESEARCHANALYST1")) {
                    RESEARCHANALYST1ColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("RESEARCHANALYST2")) {
                    RESEARCHANALYST2ColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("AUM_PAGE")) {
                    AUM_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("AUM_HEADER_ROW_NAME")){
                    AUM_HEADER_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("AUM_ROW_NAME")) {
                    AUMColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("AUM_MILLIONS_OR_BILLIONS")) {
                    AUM_MILLIONS_OR_BILLIONSColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("CREDITCOSTS_PAGE")) {
                    CREDITCOSTS_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("CREDITCOSTS_HEADER_ROW_NAME")){
                    CREDITCOSTS_HEADER_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("CREDITCOSTS_ROW_NAME")) {
                    CREDITCOSTSColumnPosition = i;
                }

                if (headerCell.getStringCellValue().equals("NPA_PAGE")) {
                    NPA_PAGEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("NPA_HEADER_ROW_NAME")){
                    NPA_HEADER_ROW_NAMEColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("GNPA_ROW_NAME")) {
                    GNPAColumnPosition = i;
                }
                if (headerCell.getStringCellValue().equals("NNPA_ROW_NAME")) {
                    NNPAColumnPosition = i;
                }
            }

            XSSFRow dataRow = ws.getRow(quarterTickerRowNumber);
            if (dataRow != null) {
                reportDataExtractConfig = new ReportDataExtractConfig();
                if (RECO_PAGEColumnPosition != -1 && dataRow.getCell(RECO_PAGEColumnPosition) != null) {
                    reportDataExtractConfig.setRECO_PAGE(dataRow.getCell(RECO_PAGEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setRECO_PAGE("");
                }
                if (CMPColumnPosition != -1 && dataRow.getCell(CMPColumnPosition) != null) {
                    reportDataExtractConfig.setCMP(dataRow.getCell(CMPColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setCMP("");
                }
                if (TPColumnPosition != -1 && dataRow.getCell(TPColumnPosition) != null) {
                    reportDataExtractConfig.setTP(dataRow.getCell(TPColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setTP("");
                }
                if (MCAPColumnPosition != -1 && dataRow.getCell(MCAPColumnPosition) != null) {
                    reportDataExtractConfig.setMCAP(dataRow.getCell(MCAPColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setMCAP("");
                }
                if (CMPPATTERNColumnPosition != -1 && dataRow.getCell(CMPPATTERNColumnPosition) != null) {
                    reportDataExtractConfig.setCMPPATTERN(dataRow.getCell(CMPPATTERNColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setCMPPATTERN("");
                }
                if (TPPATTERNColumnPosition != -1 && dataRow.getCell(TPPATTERNColumnPosition) != null) {
                    reportDataExtractConfig.setTPPATTERN(dataRow.getCell(TPPATTERNColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setTPPATTERN("");
                }
                if (MCAPPATTERNColumnPosition != -1 && dataRow.getCell(MCAPPATTERNColumnPosition) != null) {
                    reportDataExtractConfig.setMCAPPATTERN(dataRow.getCell(MCAPPATTERNColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setMCAPPATTERN("");
                }
                if (RATINGPATTERNPostion != -1 && dataRow.getCell(RATINGPATTERNPostion) != null) {
                    reportDataExtractConfig.setRATINGPATTERN(dataRow.getCell(RATINGPATTERNPostion).getStringCellValue());
                } else {
                    reportDataExtractConfig.setRATINGPATTERN("");
                }

                if (INCOME_STATEMENT_PAGEColumnPosition != -1 && dataRow.getCell(INCOME_STATEMENT_PAGEColumnPosition) != null) {
                    reportDataExtractConfig.setINCOME_STATEMENT_PAGE(dataRow.getCell(INCOME_STATEMENT_PAGEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setINCOME_STATEMENT_PAGE("");
                }
                if (MILLIONS_OR_BILLIONSColumnPosition != -1 && dataRow.getCell(MILLIONS_OR_BILLIONSColumnPosition) != null) {
                    reportDataExtractConfig.setMILLIONS_OR_BILLIONS(dataRow.getCell(MILLIONS_OR_BILLIONSColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setMILLIONS_OR_BILLIONS("");
                }
                if (HEADER_ROW_NAMEColumnPosition != -1 && dataRow.getCell(HEADER_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setHEADER_ROW_NAME(dataRow.getCell(HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setHEADER_ROW_NAME("");
                }
                if (REVENUE_ROW_NAMEColumnPosition != -1 && dataRow.getCell(REVENUE_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setREVENUE_ROW_NAME(dataRow.getCell(REVENUE_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setREVENUE_ROW_NAME("");
                }
                if (EBITDA_ROW_NAMEColumnPosition != -1 && dataRow.getCell(EBITDA_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setEBITDA_ROW_NAME(dataRow.getCell(EBITDA_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setEBITDA_ROW_NAME("");
                }
                if (DEPRECIATION_ROW_NAMEColumnPosition != -1) {
                    if(dataRow.getCell(DEPRECIATION_ROW_NAMEColumnPosition) != null)
                        reportDataExtractConfig.setDEPRECIATION_ROW_NAME(dataRow.getCell(DEPRECIATION_ROW_NAMEColumnPosition).getStringCellValue());
                    else
                        reportDataExtractConfig.setDEPRECIATION_ROW_NAME ("");
                }
                if (PAT_ROW_NAMEColumnPosition != -1 && dataRow.getCell(PAT_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setPAT_ROW_NAME(dataRow.getCell(PAT_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setPAT_ROW_NAME("");
                }
                if (EPS_ROW_NAMEColumnPosition != -1 && dataRow.getCell(EPS_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setEPS_ROW_NAME(dataRow.getCell(EPS_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setEPS_ROW_NAME("");
                }

                if (MARGIN_PAGEColumnPosition != -1 && dataRow.getCell(MARGIN_PAGEColumnPosition) != null) {
                    reportDataExtractConfig.setMARGIN_PAGE(dataRow.getCell(MARGIN_PAGEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setMARGIN_PAGE("");
                }
                if (MARGIN_HEADER_ROW_NAMEColumnPosition != -1 && dataRow.getCell(MARGIN_HEADER_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setMARGIN_HEADER_ROW_NAME(dataRow.getCell(MARGIN_HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setMARGIN_HEADER_ROW_NAME("");
                }
                if (EBITDAMARGIN_ROW_NAMEColumnPosition != -1 && dataRow.getCell(EBITDAMARGIN_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setEBITDAMARGIN_ROW_NAME(dataRow.getCell(EBITDAMARGIN_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setEBITDAMARGIN_ROW_NAME("");
                }

                if (RATIO_PAGEColumnPosition != -1 && dataRow.getCell(RATIO_PAGEColumnPosition) != null) {
                    reportDataExtractConfig.setRATIO_PAGE(dataRow.getCell(RATIO_PAGEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setRATIO_PAGE("");
                }
                if (RATIO_HEADER_ROW_NAMEColumnPosition != -1 && dataRow.getCell(RATIO_HEADER_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setRATIO_HEADER_ROW_NAME(dataRow.getCell(RATIO_HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setRATIO_HEADER_ROW_NAME("");
                }
                if (ROCE_ROW_NAMEColumnPosition != -1 && dataRow.getCell(ROCE_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setROCE_ROW_NAME(dataRow.getCell(ROCE_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setROCE_ROW_NAME("");
                }

                if (VALUATION_PAGEColumnPosition != -1 && dataRow.getCell(VALUATION_PAGEColumnPosition) != null) {
                    reportDataExtractConfig.setVALUATION_PAGE(dataRow.getCell(VALUATION_PAGEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setVALUATION_PAGE("");
                }
                if (VALUATION_HEADER_ROW_NAMEColumnPosition != -1 && dataRow.getCell(VALUATION_HEADER_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setVALUATION_HEADER_ROW_NAME(dataRow.getCell(VALUATION_HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setVALUATION_HEADER_ROW_NAME("");
                }
                if (EVBYEBITDA_ROW_NAMEColumnPosition != -1 && dataRow.getCell(EVBYEBITDA_ROW_NAMEColumnPosition) != null) {
                    reportDataExtractConfig.setEVBYEBITDA_ROW_NAME(dataRow.getCell(EVBYEBITDA_ROW_NAMEColumnPosition).getStringCellValue());
                } else {
                    reportDataExtractConfig.setEVBYEBITDA_ROW_NAME("");
                }

                if (Y0ColumnPosition != -1 && dataRow.getCell(Y0ColumnPosition) != null) {
                    reportDataExtractConfig.setY0(dataRow.getCell(Y0ColumnPosition).getStringCellValue().replaceAll("\"", ""));
                } else {
                    reportDataExtractConfig.setY0("");
                }
                if (Y1ColumnPosition != -1 && dataRow.getCell(Y1ColumnPosition) != null) {
                    reportDataExtractConfig.setY1(dataRow.getCell(Y1ColumnPosition).getStringCellValue().replaceAll("\"", ""));
                } else {
                    reportDataExtractConfig.setY1("");
                }
                if (Y2ColumnPosition != -1 && dataRow.getCell(Y2ColumnPosition) != null) {
                    reportDataExtractConfig.setY2(dataRow.getCell(Y2ColumnPosition).getStringCellValue().replaceAll("\"", ""));
                } else {
                    reportDataExtractConfig.setY2("");
                }
                if (Y3ColumnPosition != -1 && dataRow.getCell(Y3ColumnPosition) != null) {
                    reportDataExtractConfig.setY3(dataRow.getCell(Y3ColumnPosition).getStringCellValue().replaceAll("\"", ""));
                } else {
                    reportDataExtractConfig.setY3("");
                }

                if (AUM_PAGEColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setAUM_PAGE(dataRow.getCell(AUM_PAGEColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setAUM_PAGE("");
                    }
                }
                if (AUM_HEADER_ROW_NAMEColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setAUM_HEADER_ROW_NAME(dataRow.getCell(AUM_HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setAUM_HEADER_ROW_NAME("");
                    }
                }
                if (AUMColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setAUM_ROW_NAME(dataRow.getCell(AUMColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setAUM_ROW_NAME("");
                    }
                }
                if (AUM_MILLIONS_OR_BILLIONSColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setAUM_MILLIONS_OR_BILLIONS(dataRow.getCell(AUM_MILLIONS_OR_BILLIONSColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setAUM_MILLIONS_OR_BILLIONS("");
                    }
                }

                if (CREDITCOSTS_PAGEColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setCREDITCOSTS_PAGE(dataRow.getCell(CREDITCOSTS_PAGEColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setCREDITCOSTS_PAGE("");
                    }
                }
                if (CREDITCOSTS_HEADER_ROW_NAMEColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setCREDITCOSTS_HEADER_ROW_NAME(dataRow.getCell(CREDITCOSTS_HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setCREDITCOSTS_HEADER_ROW_NAME("");
                    }
                }
                if (CREDITCOSTSColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setCREDITCOSTS_ROW_NAME(dataRow.getCell(CREDITCOSTSColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setCREDITCOSTS_ROW_NAME("");
                    }
                }

                if (NPA_PAGEColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setNPA_PAGE(dataRow.getCell(NPA_PAGEColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setNPA_PAGE("");
                    }
                }
                if (NPA_HEADER_ROW_NAMEColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setNPA_HEADER_ROW_NAME(dataRow.getCell(NPA_HEADER_ROW_NAMEColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setNPA_HEADER_ROW_NAME("");
                    }
                }
                if (GNPAColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setGNPA_ROW_NAME(dataRow.getCell(GNPAColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setGNPA_ROW_NAME("");
                    }
                }
                if (NNPAColumnPosition != -1) {
                    try {
                        reportDataExtractConfig.setNNPA_ROW_NAME(dataRow.getCell(NNPAColumnPosition).getStringCellValue());
                    } catch (Exception e) {
                        reportDataExtractConfig.setNNPA_ROW_NAME("");
                    }
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

        /*if (this.configMap.isEmpty()) {
            // load configMap
            loadReportDataExtractConfig(reportDataExtractConfigFilePath);
        }*/
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
            case "AXIS":
                return new AnalystRecoExtractorAXIS();
            /*case "ICICIDIRECT":
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
        return getMCapFromBillion(mcapLine, rdec, group, broker,false);
    }

    protected double getMCapFromBillion(String mcapLine, ReportDataExtractConfig rdec, int group, String broker, boolean isMCapInCr) {
        String mcap = "";
        double mcapNumber = 0;
        if (mcapLine != null && !mcapLine.isEmpty()) {
            // Extracting cmp
            Pattern mcapPattern = Pattern.compile(rdec.getMCAPPATTERN());
            Matcher mcapMatcher = mcapPattern.matcher(mcapLine);
            if (mcapMatcher.find()) {
                mcap = mcapMatcher.group(group).replace(",", "");
                if(!isMCapInCr)
                    mcapNumber = Double.parseDouble(mcap) * 100;
                else
                    mcapNumber = Double.parseDouble(mcap);
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

    protected String[] getDataColumnsForHeaderPL(String headerLine, String rowHeaderPattern){
        String[] dataColumns = null;
        Pattern pattern = Pattern.compile(rowHeaderPattern);
        Matcher matcher = pattern.matcher(headerLine.trim());
        if(!rowHeaderPattern.isEmpty()) {
            if (matcher.find()) {
                String rowHeader = matcher.group(1);
                int strDataColumnsIndex = headerLine.trim().indexOf(rowHeader)+rowHeader.length();
                String strDataColumns = headerLine.trim().substring(strDataColumnsIndex, headerLine.trim().length()); // headerLine.replace(rowHeader, "");
                Pattern pattern1 = Pattern.compile("[a-zA-Z]");
                Matcher matcher1 = pattern1.matcher(strDataColumns);
                if (matcher1.find()) {
                    int endIndex = matcher1.start();
                    strDataColumns = strDataColumns.substring(0, endIndex).trim();
                }
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

    protected void saveReportParametersNewFormat(String ticker, ReportParameters rp, String outputFolderPath) {

        try {
            String newFile = outputFolderPath + File.separator + "ReportextractNew.txt";
            File file = new File(newFile);

            boolean writeHeader = !file.exists();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

                if (writeHeader) {
                    writer.write("TICKER\tType\tQuarter\tDate\tMcap(Cr)\tPrice\tFirm\tReco\tTP\tFwd PE\tFIN Yr\tEPS\tRev.\tEBIT%\tProfit\tA1\tA2\tA3\tLead Analyst");
                    writer.newLine();
                }

                // Lead analyst (first name)
                String leadAnalyst = "";
                if (rp.getAnalystsNames() != null && rp.getAnalystsNames().contains(";")) {
                    leadAnalyst = rp.getAnalystsNames().split(";")[0].trim();
                } else {
                    leadAnalyst = rp.getAnalystsNames();
                }

                String type = "RE";
                String firm = rp.getBroker();

                // derive FY1, FY2, FY3
                String[] fys = deriveFinancialYears(rp.getQuarter());
                String fy1 = fys[0];
                String fy2 = fys[1];
                String fy3 = fys[2];

                // three rows: FY1, FY2, FY3
                String[][] rows = {
                        { fy1, safe(rp.getY1EPS()), safe(rp.getY1Revenue()), safe(rp.getY1OPM()), safe(rp.getY1PAT()) },
                        { fy2, safe(rp.getY2EPS()), safe(rp.getY2Revenue()), safe(rp.getY2OPM()), safe(rp.getY2PAT()) },
                        { fy3, safe(rp.getY3EPS()), safe(rp.getY3Revenue()), safe(rp.getY3OPM()), safe(rp.getY3PAT()) }
                };

                for (String[] row : rows) {
                    String fy = row[0];
                    String eps = row[1];
                    String rev = row[2];
                    String ebitMargin = row[3];
                    String profit = row[4];

                    if (fy.isEmpty()) continue;

                    /*boolean hasData = true;

                    try {
                        if (!rev.isEmpty() && Double.parseDouble(rev) > 0) hasData = true;
                    } catch (Exception ignored) {}

                    try {
                        if (!eps.isEmpty() && Double.parseDouble(eps) > 0) hasData = true;
                    } catch (Exception ignored) {}

                    if (!hasData) continue;*/

                    writer.write(
                            ticker + "\t" +
                                    type + "\t" +
                                    rp.getQuarter() + "\t" +
                                    rp.getReportDate() + "\t" +
                                    safe(rp.getMcap()) + "\t" +
                                    safe(rp.getPrice()) + "\t" +
                                    firm + "\t" +
                                    rp.getRating() + "\t" +
                                    safe(rp.getTarget()) + "\t" +
                                    "" + "\t" +      // Fwd PE
                                    fy + "\t" +
                                    eps + "\t" +
                                    rev + "\t" +
                                    ebitMargin + "\t" +
                                    profit + "\t" +
                                    "" + "\t" +      // A1
                                    "" + "\t" +      // A2
                                    "" + "\t" +      // A3
                                    leadAnalyst
                    );
                    writer.newLine();
                }
            }

            System.out.println("New formatted extract saved to: " + newFile);

        } catch (Exception e) {
            System.out.println("Exception in saveReportParametersNewFormat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String[] deriveFinancialYears(String quarter) {

        if (quarter == null) return new String[] { "", "", "" };

        try {
            // Clean unexpected characters
            quarter = quarter.trim().replaceAll("[^A-Za-z0-9]", "");

            // Expect exactly FY26Q3
            Pattern p = Pattern.compile("FY(\\d{2})Q(\\d)");
            Matcher m = p.matcher(quarter);

            if (!m.find()) {
                System.out.println("Quarter parse failed for: [" + quarter + "]");
                return new String[] { "", "", "" };
            }

            int fy = Integer.parseInt(m.group(1));  // 26
            int q = Integer.parseInt(m.group(2));   // 3

            int y1 = (q == 4) ? fy + 1 : fy;
            int y2 = y1 + 1;
            int y3 = y1 + 2;

            return new String[] {
                    "FY" + y1,
                    "FY" + y2,
                    "FY" + y3
            };

        } catch (Exception e) {
            System.out.println("Error parsing quarter: [" + quarter + "]");
            return new String[] { "", "", "" };
        }
    }

    // Utility to avoid printing "null"
    private String safe(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

}
