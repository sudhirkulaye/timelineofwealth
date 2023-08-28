package com.timelineofwealth.service;


import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.timelineofwealth.dto.ReportDataExtractConfig;
import com.timelineofwealth.dto.ReportParameters;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AnalystRecoExtractorMOSL extends AnalystRecoExtractor {

    protected String BROKER = "MOSL";
    protected String CMP;
    protected String TP;
    protected String MCAP;
    protected String CMPPATTERN;
    protected String TPPATTERN;
    protected String MCAPPATTERN;
    protected String RATINGPATTERN;

    protected String INCOME_STATEMENT_PAGE;
    protected String RATIO_PAGE;
    protected String VALUATION_PAGE;

    protected String HEADER_ROW_NAME;
    protected String REVENUE_ROW_NAME;
    protected String EBITDA_ROW_NAME;
    protected String DEPRECIATION_ROW_NAME;

    protected String EBITDAMARGIN_ROW_NAME;
    protected String ROCE_ROW_NAME;

    protected String AUM_ROW_NAME;
    protected String CREDITCOSTS_ROW_NAME;
    protected String GNPA_ROW_NAME;
    protected String NNPA_ROW_NAME;

    protected String EVBYEBITDA_ROW_NAME;

    protected String Y0;
    protected String Y1;
    protected String Y2;

    protected String MILLIONS_OR_BILLIONS;
    protected String MILLIONS_OR_BILLIONS_FLAG;

    protected String RESEARCHANALYST1;
    protected String RESEARCHANALYST2;

    protected String QUARTER;

    protected String DATEPATTERN = "\\d{1,2}\\s*+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s*+\\d{2,4}";

    protected String DATEFORMAT = "dd MMMMM yyyy";

    protected String pageContentFirst = null;
    protected String[] linesFirstPage = null;

    protected String pageContentIncomeStmt = null;
    protected String[] linesIncomeStmt = null;

    protected String pageContentRatio = null;
    protected String[] linesRatio = null;

    protected String pageContentValuation = null;
    protected String[] linesValuation = null;

    protected String dateString = "";
    protected BigDecimal mcap = new BigDecimal("0");
    protected String cmp = "0";
    protected String rating = "";
    protected String targetPrice = "0";
    protected String analystNames = "";

    protected String headerFirstPage = "";
    protected String[] headerColumnsFirstPage = null;
    protected String headerIncomeStmt = "";
    protected String[] headerColumnsIncomeStmt = null;
    protected String headerRatio = "";
    protected String[] headerColumnsRatio = null;
    protected String headerValuation = "";
    protected String[] headerColumnsValuation = null;

    protected Integer headerFirstPageLineNumber = null;
    protected Integer valuationPageNumber = null;
    protected Integer headerIncomeStmtLineNumber = null;
    protected Integer headerRatioLineNumber = null;
    protected Integer headerValuationLineNumber = null;
    protected Integer revenueLineNumber = null;
    protected Integer ebitdaLineNumber = null;
    protected Integer y0ColumnNumberOnIncStmt = null, y1ColumnNumberOnIncStmt = null, y2ColumnNumberOnIncStmt = null;
    protected Integer y0ColumnNumberOnRatio = null, y1ColumnNumberOnRatio = null, y2ColumnNumberOnRatio = null;

    protected BigDecimal y0EBITDANumber = null, y1EBITDANumber = null, y2EBITDANumber = null;
    protected BigDecimal y0DepreciationNumber = null, y1DepreciationNumber = null, y2DepreciationNumber = null;

    protected ReportParameters reportParameters = new ReportParameters();

    protected PdfReader pdfReader = null;

    protected void setConstants(ReportDataExtractConfig rdec) {
        CMP = rdec.getCMP();
        TP = rdec.getTP();
        MCAP = rdec.getMCAP();
        CMPPATTERN = rdec.getCMPPATTERN();
        TPPATTERN = rdec.getTPPATTERN();
        MCAPPATTERN = rdec.getMCAPPATTERN();
        RATINGPATTERN = rdec.getRATINGPATTERN();

        INCOME_STATEMENT_PAGE = rdec.getINCOME_STATEMENT_PAGE();
        RATIO_PAGE = rdec.getRATIO_PAGE();
        VALUATION_PAGE = rdec.getVALUATION_PAGE();

        HEADER_ROW_NAME = rdec.getHEADER_ROW_NAME();
        REVENUE_ROW_NAME = rdec.getREVENUE_ROW_NAME();
        EBITDA_ROW_NAME = rdec.getEBITDA_ROW_NAME();
        DEPRECIATION_ROW_NAME = rdec.getDEPRECIATION_ROW_NAME();

        EBITDAMARGIN_ROW_NAME = rdec.getEBITDAMARGIN_ROW_NAME();
        ROCE_ROW_NAME = rdec.getROCE_ROW_NAME();

        AUM_ROW_NAME = rdec.getAUM_ROW_NAME();
        CREDITCOSTS_ROW_NAME = rdec.getCREDITCOSTS_ROW_NAME();
        GNPA_ROW_NAME = rdec.getGNPA_ROW_NAME();
        NNPA_ROW_NAME = rdec.getNNPA_ROW_NAME();

        EVBYEBITDA_ROW_NAME = rdec.getEVBYEBITDA_ROW_NAME();

        Y0 = rdec.getY0();
        Y1 = rdec.getY1();
        Y2 = rdec.getY2();

        MILLIONS_OR_BILLIONS = rdec.getMILLIONS_OR_BILLIONS();
        MILLIONS_OR_BILLIONS_FLAG = "M";

        RESEARCHANALYST1 = rdec.getRESEARCHANALYST1();
        RESEARCHANALYST2 = rdec.getRESEARCHANALYST2();

        QUARTER = rdec.getQUARTER();

        reportParameters.setQuarter(QUARTER);
        reportParameters.setBroker(BROKER);
    }

    public ReportParameters getReportParameters(String reportFilePath, ReportDataExtractConfig rdec) {
        String outPutString;
        boolean isFinancialReport = false;
        try {
            //Set Constants
            setConstants(rdec);

            // Open PDF document
            pdfReader = new PdfReader(reportFilePath);

            loadPages(pdfReader, rdec);
            setReportDate(reportFilePath, rdec);
            setMarketCap(rdec);
            setCMP(rdec);
            setRatings(rdec);
            setTargetPrice(rdec);
            setAnalystNames(rdec);
            // set isFinancialReport
            if (EBITDA_ROW_NAME.contains("PAT")|| EBITDA_ROW_NAME.toLowerCase().contains("profit after tax") ) {
                isFinancialReport = true;
            } else {
                isFinancialReport = false;
            }
            setHeaderColumns(rdec, isFinancialReport);
            setRevenue(rdec, isFinancialReport);
            if(!isFinancialReport)
                setProfitForNonFinancials(rdec);
            else
                setProfitForFinancials(rdec);

            setOPMOrNIM(rdec, isFinancialReport);
            setROCEOrROE(rdec, isFinancialReport);
            setEVBYEBITRatio(rdec, isFinancialReport);

            if(isFinancialReport) {
                setAUM(rdec);
                setCreditCost(rdec);
                setGNPA(rdec);
                setNNPA(rdec);
            }

        } catch (IOException e) {
            System.out.println("########## Excecption in AnalystREcoExtractorMOSL for report " + QUARTER + "_" + rdec.getTICKER() + "_MOSL.pdf  " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            pdfReader.close();
        }
        return reportParameters;
    }

    protected void loadPages(PdfReader pdfReader, ReportDataExtractConfig rdec){
        //Get the number of pages in pdf.
        int noOfPages = pdfReader.getNumberOfPages();

        try {

            // Load First Page
            pageContentFirst = PdfTextExtractor.getTextFromPage(pdfReader, 1);
            linesFirstPage = pageContentFirst.split("\n");

            // Load Income Statement Page
            int incomeStatementPageNo = -1;
            incomeStatementPageNo = getPageNumberForMatchingPattern(pdfReader, 2, noOfPages, INCOME_STATEMENT_PAGE, rdec, BROKER);
            if (incomeStatementPageNo > 1) {
                System.out.print("Inc. Statement Page No. : " + incomeStatementPageNo + " / ");
                pageContentIncomeStmt = PdfTextExtractor.getTextFromPage(pdfReader, incomeStatementPageNo);
                linesIncomeStmt = pageContentIncomeStmt.split("\n");
            } else
                System.out.println("########## Income Statement Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Find Ratio Page No.
            int ratioPageNo = -1;
            ratioPageNo = getPageNumberForMatchingPattern(pdfReader,  incomeStatementPageNo, noOfPages, RATIO_PAGE, rdec, BROKER);
            if (ratioPageNo > 1) {
                System.out.print("Ratio Page No. : " + ratioPageNo + " / ");
                pageContentRatio = PdfTextExtractor.getTextFromPage(pdfReader, ratioPageNo);
                linesRatio = pageContentRatio.split("\n");
            } else
                System.out.println("########## Ratio Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Find Valuation Page No.
            int valuationPageNo = -1;
            valuationPageNo = getPageNumberForMatchingPattern(pdfReader,  incomeStatementPageNo, noOfPages, rdec.getVALUATION_PAGE(), rdec, BROKER);
            if (valuationPageNo > 0) {
                System.out.println("Valuation Page No. : " + valuationPageNo);
                valuationPageNumber = new Integer(valuationPageNo);
                pageContentValuation = PdfTextExtractor.getTextFromPage(pdfReader, valuationPageNo);
                linesValuation = pageContentValuation.split("\n");
            } else
                System.out.println("########## Valuation Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        } catch (IOException e){
            System.out.println("########## Exception in loadPages " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    public void setReportDate(String reportFilePath, ReportDataExtractConfig rdec){
        // Extract report date
        int lineNumber = 0;

        try {
            long dateLastModified = new File(reportFilePath).lastModified();
            while (lineNumber < linesFirstPage.length && dateString.isEmpty()) {
                dateString = getReportDate(linesFirstPage[lineNumber], dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
                lineNumber++;
            }
            // if date not found then set it to last modified date
            if (dateString.isEmpty()){
                dateString = getReportDate(null, dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
            }
        } catch (Exception e) {
            System.out.println("\n########## Exception in setting Report Date, setting to blank for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            dateString = "";
        }
        reportParameters.setReportDate(dateString);
        System.out.print("Date : " + dateString + " ");
    }

    protected void setMarketCap(ReportDataExtractConfig rdec){
        double mcapNumber = 0;
        int mcapLineNumber = -1;
        boolean isMCapLineFound = false;
        for (int i = 0; i < linesFirstPage.length; i++) {
            if(!isMCapLineFound && linesFirstPage[i].startsWith(MCAP)) {
                mcapLineNumber = i;
                isMCapLineFound = true;
            }
            if(isMCapLineFound)
                break;
        }
        try {
            if (mcapLineNumber != -1) {
                mcapNumber = getMCapFromBillion(linesFirstPage[mcapLineNumber], rdec, 1, BROKER);
                mcap = new BigDecimal(mcapNumber).setScale(0, RoundingMode.HALF_UP);
            } else {
                mcapNumber = 0;
                System.out.println("\n########## Market Cap line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            mcap = new BigDecimal("0");
            System.out.println("\n########## Exception in setting Market Cap, setting to 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
        reportParameters.setMcap(mcap);
        System.out.print("MCap : " + mcapNumber + " ");
    }

    protected void setCMP(ReportDataExtractConfig rdec){
        int cmpLineNumber = -1;
        boolean isCMPLineFound = false;
        for (int i = 0; i < linesFirstPage.length; i++) {
            if (!isCMPLineFound && linesFirstPage[i].contains(CMP)){
                cmpLineNumber = i;
                isCMPLineFound = true;
            }
            if(isCMPLineFound)
                break;
        }
        try {
            if (cmpLineNumber != -1) {
                cmp = "" + getCMP(linesFirstPage[cmpLineNumber], rdec, 2, BROKER);
            } else {
                cmp = "0";
                System.out.println("\n########## CMP line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            cmp = "0";
            System.out.println("\n########## Exception in setting CMP, setting to 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
        reportParameters.setPrice(new BigDecimal(cmp.replace(",", "")).setScale(0, RoundingMode.HALF_UP) );
        System.out.print("CMP : " + cmp + " ");
    }

    protected void setTargetPrice(ReportDataExtractConfig rdec){
        int cmpLineNumber = -1, targetPriceLineNumber = -1;
        boolean isCMPLineFound = false, isTPLineFound = false;
        for (int i = 0; i < linesFirstPage.length; i++) {
            if (!isCMPLineFound && linesFirstPage[i].contains(CMP)){
                cmpLineNumber = i;
                isCMPLineFound = true;
                if (linesFirstPage[i].contains(TP)) {
                    targetPriceLineNumber = cmpLineNumber;
                    isTPLineFound = true;
                }
            }
            if(isTPLineFound)
                break;
        }
        try {
            // Extract Target Price
            if (targetPriceLineNumber != -1) {
                targetPrice = "" + getTP(linesFirstPage[targetPriceLineNumber], rdec, 1, BROKER);
            } else {
                targetPrice = "0";
                System.out.println("\n########## Target Price line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            targetPrice = "0";
            System.out.println("\n########## Exception in setting Target Price, setting to 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
        reportParameters.setTarget(new BigDecimal(targetPrice.replace(",", "")).setScale(0, RoundingMode.HALF_UP));
        System.out.print("Target Price : " + targetPrice + " ");
    }

    protected void setRatings(ReportDataExtractConfig rdec){
        int cmpLineNumber = -1;
        boolean isCMPLineFound = false;
        for (int i = 0; i < linesFirstPage.length; i++) {
            if (!isCMPLineFound && linesFirstPage[i].contains(CMP)){
                cmpLineNumber = i;
                isCMPLineFound = true;
            }
            if(isCMPLineFound)
                break;
        }
        try {
            if (cmpLineNumber!= -1) {
                String ratingLine = linesFirstPage[cmpLineNumber];
                // Extract the last word
                String[] ratingLineWords = ratingLine.split(" ");
                rating = capitalizeFirstChar(ratingLineWords[ratingLineWords.length - 1]);
            } else {
                System.out.println("\n########## Exception in setting Ratings for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                rating = "";
            }
        } catch (Exception e) {
            System.out.println("\n########## Exception in setting Ratings for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            rating = "";
        }
        reportParameters.setRating(rating);
        System.out.print("Ratings : " + rating + " ");
    }

    protected void setAnalystNames(ReportDataExtractConfig rdec){
        int analyst1LineNumber = -1;
        boolean isAnalyst1LineFound = false;
        for (int i = 0; i < linesFirstPage.length; i++) {
            if(!isAnalyst1LineFound && linesFirstPage[i].toLowerCase().contains(RESEARCHANALYST1.toLowerCase())) {
                analyst1LineNumber = i;
                isAnalyst1LineFound = true;
            }
            if(isAnalyst1LineFound)
                break;
        }
        // Extract Analyst Names
        analystNames = getAnalyst(analystNames, pageContentFirst, rdec, analyst1LineNumber);
        reportParameters.setAnalystsNames(analystNames);
        System.out.println("Analysts Names : " + analystNames);
    }

    protected void setHeaderColumns(ReportDataExtractConfig rdec, boolean isFinancialReport){
        // Get Header Line No. from the Inc.
        int headerLineNo = -1;
        try {
            headerLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, HEADER_ROW_NAME,rdec, BROKER);
            if (headerLineNo > 1) {
                headerIncomeStmt = linesIncomeStmt[headerLineNo];
                headerColumnsIncomeStmt = getDataColumnsForHeader(headerIncomeStmt, HEADER_ROW_NAME);
                headerIncomeStmtLineNumber = new Integer(headerLineNo);
                // case when header "Y/E March" is on the next line and corresponding data on the previous line
                if(headerColumnsIncomeStmt == null || headerColumnsIncomeStmt.length == 0) {
                    headerIncomeStmt = linesIncomeStmt[headerLineNo] + linesIncomeStmt[headerLineNo-1];
                    headerColumnsIncomeStmt = getDataColumnsForHeader(headerIncomeStmt, HEADER_ROW_NAME);
                }
                System.out.println("Inc. Statement Header Count : " + headerColumnsIncomeStmt.length + " / Line : " + linesIncomeStmt[headerLineNo]);
            } else {
                System.out.println("########## Income Statement Header Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            System.out.println("########## Exception in setting Income Statement Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }

        // Get Header Line No. from the Inc. Ratio Page
        headerLineNo = -1;
        try {
            headerLineNo = getLineNumberForMatchingPattern(linesRatio, 0, HEADER_ROW_NAME,rdec, BROKER);
            if (headerLineNo > 1) {
                headerRatio = linesRatio[headerLineNo];
                headerColumnsRatio = getDataColumnsForHeader(headerRatio, HEADER_ROW_NAME);
                headerRatioLineNumber = new Integer(headerLineNo);
                // case when header "Y/E March" is on the next line and corresponding data on the previous line
                if(headerColumnsRatio == null || headerColumnsRatio.length == 0) {
                    headerRatio = linesRatio[headerLineNo] + linesRatio[headerLineNo-1];
                    headerColumnsRatio = getDataColumnsForHeader(headerRatio, HEADER_ROW_NAME);
                }
                System.out.println("Ratio Header Count : " + headerColumnsRatio.length + " / Line : " + linesRatio[headerLineNo]);
            } else {
                // in case header is not available fetch the header from income statement
                headerRatio = headerIncomeStmt;
                headerColumnsRatio = headerColumnsIncomeStmt;
                headerRatioLineNumber = null;
                System.out.println("$$$$$$$$$$ Setting Ratio Header same as Income Statement Header ");
            }
        } catch (Exception e){
            System.out.println("########## Exception in setting Ratio Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }

        // Get Header Line No. from the Inc. Valuation Page
        headerLineNo = -1;
        try {
            headerLineNo = getLineNumberForMatchingPattern(linesValuation, 1, HEADER_ROW_NAME, rdec, true, BROKER);
            if (headerLineNo > 1) {
                headerValuation = linesValuation[headerLineNo];
                headerColumnsValuation = getDataColumnsForHeader(headerValuation, HEADER_ROW_NAME);
                headerValuationLineNumber = new Integer(headerLineNo);
                System.out.println("Valuation Header Count : " + headerColumnsValuation.length + " / Line : " + linesValuation[headerLineNo]);
            } else {
                //Search from the top of the page
                headerLineNo = getLineNumberForMatchingPattern(linesValuation, 0, HEADER_ROW_NAME, rdec, BROKER);
                if (headerLineNo > 1) {
                    headerValuation = linesValuation[headerLineNo];
                    headerColumnsValuation = getDataColumnsForHeader(headerValuation, HEADER_ROW_NAME);
                    headerValuationLineNumber = new Integer(headerLineNo);
                    System.out.println("Valuation Header Count : " + headerColumnsValuation.length + " / Line : " + linesValuation[headerLineNo]);
                } else {
                    // in case header is not avialble fetch the header from income statement
                    headerValuation = headerRatio;
                    headerColumnsValuation = headerColumnsRatio;
                    headerValuationLineNumber = null;
                    System.out.println("$$$$$$$$$$ Setting Valuation Header same as Ratio Header ");
                }
            }

            // Get Header Line No. for the first page
            headerLineNo = -1;
            try {
                headerLineNo = getLineNumberForMatchingPattern(linesFirstPage, 1, "(?i).*((Forecasts\\/Valuations))",rdec, true, BROKER);
                if (headerLineNo > 1) {
                    headerFirstPage = linesFirstPage[headerLineNo].replace("`", "");
                    headerColumnsFirstPage = getDataColumnsForHeader(headerFirstPage, "(?i).*((Forecasts\\/Valuations))");
                    headerFirstPageLineNumber = new Integer(headerLineNo);
                    System.out.println("First Page Header Count : " + headerColumnsIncomeStmt.length + " / Line : " + linesFirstPage[headerLineNo]);
                } else {
                    System.out.println("$$$$$$$$$$ First Page Header Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } catch (Exception e) {
                System.out.println("########## Exception in setting First Page Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

        } catch (Exception e){
            System.out.println("########## Exception in setting First Page Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setRevenue(ReportDataExtractConfig rdec, boolean isFinancialReport){
        int headerLineNo = -1;
        int revenueLineNo = -1;
        int y0Column = -1, y1Column = -1, y2Column = -1;
        String revenue = null;
        String[] revenueColumns = null;
        try {
            // first find header line
            if (headerIncomeStmtLineNumber != null)
                headerLineNo = headerIncomeStmtLineNumber.intValue();
            // Get Revenue Line No. form the Inc. Statement Page
            if(headerLineNo>0)
                revenueLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, headerLineNo, REVENUE_ROW_NAME, rdec, BROKER);

            if (revenueLineNo > 1) {
                revenueLineNumber = new Integer(revenueLineNo);
                revenue = linesIncomeStmt[revenueLineNo];
                revenueColumns = getDataColumnsForHeader(revenue, REVENUE_ROW_NAME, headerColumnsIncomeStmt.length);

                if(revenueColumns != null && revenueColumns.length != 0 ) {
                    if(revenueColumns.length == headerColumnsIncomeStmt.length) {
                        // Set million or billion flag
                        MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find()? "B" : "M";
                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                        System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column + "\n");
                        System.out.println("Revenue Columns : " + Arrays.toString(revenueColumns));

                        String y0Revenue = "", y1Revenue = "", y2Revenue = "";
                        double y0RevenueNumber = 0, y1RevenueNumber = 0, y2RevenueNumber = 0;

                        if (y0Column >= 0) {
                            y0Revenue = revenueColumns[y0Column];
                            y0RevenueNumber = Double.parseDouble(y0Revenue.replace(",", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y0RevenueNumber = y0RevenueNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y0RevenueNumber = y0RevenueNumber * 100;
                            }
                            reportParameters.setY0Revenue(new BigDecimal(y0RevenueNumber).setScale(2, RoundingMode.HALF_UP));
                            y0ColumnNumberOnIncStmt = new Integer(y0Column);
                        } else {
                            reportParameters.setY0Revenue(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 Revenue = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            y1Revenue = revenueColumns[y1Column];
                            y1RevenueNumber = Double.parseDouble(y1Revenue.replace(",", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y1RevenueNumber = y1RevenueNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y1RevenueNumber = y1RevenueNumber * 100;
                            }
                            reportParameters.setY1Revenue(new BigDecimal(y1RevenueNumber).setScale(2, RoundingMode.HALF_UP));
                            y1ColumnNumberOnIncStmt = new Integer(y1Column);
                        } else {
                            reportParameters.setY0Revenue(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 Revenue = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            y2Revenue = revenueColumns[y2Column];
                            y2RevenueNumber = Double.parseDouble(y2Revenue.replace(",", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y2RevenueNumber = y2RevenueNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y2RevenueNumber = y2RevenueNumber * 100;
                            }
                            reportParameters.setY2Revenue(new BigDecimal(y2RevenueNumber).setScale(2, RoundingMode.HALF_UP));
                            y2ColumnNumberOnIncStmt = new Integer(y2Column);
                        } else {
                            reportParameters.setY0Revenue(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 Revenue = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0Revenue(new BigDecimal("0"));
                        reportParameters.setY1Revenue(new BigDecimal("0"));
                        reportParameters.setY2Revenue(new BigDecimal("0"));
                        System.out.println("########## Revenue Row Columns (" + revenueColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0Revenue(new BigDecimal("0"));
                    reportParameters.setY1Revenue(new BigDecimal("0"));
                    reportParameters.setY2Revenue(new BigDecimal("0"));
                    System.out.println("########## Revenue Row Header and Revenue Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            }
            else {
                revenueLineNumber = null;
                reportParameters.setY0Revenue(new BigDecimal("0"));
                reportParameters.setY1Revenue(new BigDecimal("0"));
                reportParameters.setY2Revenue(new BigDecimal("0"));
                System.out.println("########## Revenue Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e){
            reportParameters.setY0Revenue(new BigDecimal("0"));
            reportParameters.setY1Revenue(new BigDecimal("0"));
            reportParameters.setY2Revenue(new BigDecimal("0"));
            System.out.println("########## Exception in setting Revenue for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setProfitForNonFinancials(ReportDataExtractConfig rdec){
        try {
            int revenueLineNo = -1;
            int depreciationLineNo = -1;
            boolean isToSetEBIT = false, isEBITDAPresent = false, canImpliedDepreciationBeFound = false;
            String ebitda = null, ebit = null, depreciation = null;
            String[] ebitdaColumns = null, depreciationColumns = null, ebitColumns = null;
            int y0Column = -1, y1Column = -1, y2Column = -1;
            String y0EBITDA = "", y1EBITDA = "", y2EBITDA = "";
            String y0Depreciation = "", y1Depreciation = "", y2Depreciation = "";
            double y0EBITDANo = 0, y1EBITDANo = 0, y2EBITDANo = 0;
            double y0DepreciationNo = 0, y1DepreciationNo = 0, y2DepreciationNo = 0;

            String y0EBIT = "", y1EBIT = "", y2EBIT = "";
            double y0EBITNumber = 0, y1EBITNumber = 0, y2EBITNumber = 0;

            if (revenueLineNumber != null)
                revenueLineNo = revenueLineNumber.intValue();
            // Get EBITDA Line No. form the Inc. Statement Page
            int ebitdaLineNo = -1;
            ebitdaLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, revenueLineNo, EBITDA_ROW_NAME, rdec, BROKER);

            if(ebitdaLineNo < 0) {
                System.out.println("EBITDA line is not present for" + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                isToSetEBIT = true;
            } else  {
                ebitdaLineNumber = new Integer(ebitdaLineNo);
                isEBITDAPresent = true;
                depreciationLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, ebitdaLineNo, DEPRECIATION_ROW_NAME, rdec, BROKER);
                if(depreciationLineNo < 0) {
                    System.out.println("########## Depreciation line is not present for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    isToSetEBIT = true;
                } else {
                    ebitda = linesIncomeStmt[ebitdaLineNo].replace("(", "-").replace(")", "");
                    ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME);

                    if(ebitdaColumns == null || (ebitdaColumns != null && ebitdaColumns.length != headerColumnsIncomeStmt.length && ebitdaColumns.length == 0)){
                        if(!linesIncomeStmt[ebitdaLineNo - 1].trim().equals(""))
                            ebitda = linesIncomeStmt[ebitdaLineNo] + linesIncomeStmt[ebitdaLineNo - 1];
                        else if(!linesIncomeStmt[ebitdaLineNo + 1].trim().equals(""))
                            ebitda = linesIncomeStmt[ebitdaLineNo] + linesIncomeStmt[ebitdaLineNo + 1];

                        ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME);
                    }

                    depreciation = linesIncomeStmt[depreciationLineNo];
                    depreciationColumns = getDataColumnsForHeader(depreciation, DEPRECIATION_ROW_NAME);

                    if(depreciationColumns == null || (depreciationColumns != null && depreciationColumns.length != headerColumnsIncomeStmt.length && depreciationColumns.length == 0)){
                        if(!linesIncomeStmt[depreciationLineNo - 1].trim().equals(""))
                            depreciation = linesIncomeStmt[depreciationLineNo] + linesIncomeStmt[depreciationLineNo - 1];
                        else if(!linesIncomeStmt[depreciationLineNo + 1].trim().equals(""))
                            depreciation = linesIncomeStmt[depreciationLineNo] + linesIncomeStmt[depreciationLineNo + 1];

                        depreciationColumns = getDataColumnsForHeader(depreciation, DEPRECIATION_ROW_NAME);
                    }

                    if (ebitdaColumns != null && ebitdaColumns.length != 0) {
                        if (depreciationColumns != null && depreciationColumns.length != 0) {
                            if(ebitdaColumns.length == headerColumnsIncomeStmt.length) {
                                if (depreciationColumns.length == headerColumnsIncomeStmt.length) {
                                    MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find() ? "B" : "M";

                                    if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                        y0Column = y0ColumnNumberOnIncStmt.intValue();
                                        y1Column = y1ColumnNumberOnIncStmt.intValue();
                                        y2Column = y2ColumnNumberOnIncStmt.intValue();
                                    } else {
                                        y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                        y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                        y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                                    }
                                    System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                                    System.out.print(" Y1 Index " + y1Column);
                                    System.out.print(" Y2 Index " + y2Column + "\n");
                                    System.out.println("Profit Line : " + Arrays.toString(ebitdaColumns));

                                    if (y0Column >= 0) {
                                        y0EBITDA = ebitdaColumns[y0Column];
                                        y0Depreciation = depreciationColumns[y0Column];

                                        y0EBITDANo = Double.parseDouble(y0EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                                        y0DepreciationNo = Double.parseDouble(y0Depreciation.replace(",", "").replace("(", "").replace(")", ""));
                                        if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y0EBITDANo = y0EBITDANo / 10;
                                            y0DepreciationNo = y0DepreciationNo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y0EBITDANo = y0EBITDANo * 100;
                                            y0DepreciationNo = y0DepreciationNo * 100;
                                        }
                                        y0EBITDANumber = new BigDecimal(y0EBITDANo);
                                        y0DepreciationNumber = new BigDecimal(y0DepreciationNo);
                                        reportParameters.setY0EBIT(y0EBITDANo + "-" + y0DepreciationNo);
                                    } else {
                                        reportParameters.setY0EBIT(null);
                                        System.out.println("########## Y0 Column Index not found Setting Y0 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    }
                                    if (y1Column >= 0) {
                                        y1EBITDA = ebitdaColumns[y1Column];
                                        y1Depreciation = depreciationColumns[y1Column];

                                        y1EBITDANo = Double.parseDouble(y1EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                                        y1DepreciationNo = Double.parseDouble(y1Depreciation.replace(",", "").replace("(", "").replace(")", ""));
                                        if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y1EBITDANo = y1EBITDANo / 10;
                                            y1DepreciationNo = y1DepreciationNo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y1EBITDANo = y1EBITDANo * 100;
                                            y1DepreciationNo = y1DepreciationNo * 100;
                                        }
                                        y1EBITDANumber = new BigDecimal(y1EBITDANo);
                                        y1DepreciationNumber = new BigDecimal(y1DepreciationNo);
                                        reportParameters.setY1EBIT(y1EBITDANo + "-" + y1DepreciationNo);
                                    } else {
                                        reportParameters.setY1EBIT(null);
                                        System.out.println("########## Y1 Column Index not found Setting Y1 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    }
                                    if (y2Column >= 0) {
                                        y2EBITDA = ebitdaColumns[y2Column];
                                        y2Depreciation = depreciationColumns[y2Column];

                                        y2EBITDANo = Double.parseDouble(y2EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                                        y2DepreciationNo = Double.parseDouble(y2Depreciation.replace(",", "").replace("(", "").replace(")", ""));
                                        if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y2EBITDANo = y2EBITDANo / 10;
                                            y2DepreciationNo = y2DepreciationNo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y2EBITDANo = y2EBITDANo * 100;
                                            y2DepreciationNo = y2DepreciationNo * 100;
                                        }
                                        y2EBITDANumber = new BigDecimal(y2EBITDANo);
                                        y2DepreciationNumber = new BigDecimal(y2DepreciationNo);
                                        reportParameters.setY2EBIT(y2EBITDANo + "-" + y2DepreciationNo);
                                    } else {
                                        reportParameters.setY2EBIT(null);
                                        System.out.println("########## Y2 Column Index not found Setting Y2 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    }
                                } else {
                                    System.out.println("########## Depreciation Row Columns (" + depreciationColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    isToSetEBIT = true;
                                }
                            } else {
                                System.out.println("########## Operating Profit Row Columns (" + ebitdaColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                isToSetEBIT = true;
                            }
                        } else {
                            System.out.println("########## Depreciation Row Header and Depreciation Data not on the same line so, setting EBIT directly for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            isToSetEBIT = true;
                        }
                    } else {
                        System.out.println("########## EBITDA Row Header and EBITDA Data not on the same line so, setting EBIT directly for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        isToSetEBIT = true;
                    }
                }
                if(isToSetEBIT == true) {
                    System.out.println("##########  Setting EBIT directly");
                    int ebitLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, ebitdaLineNo, "(?i)^((EBIT ))", rdec, BROKER);
                    if(ebitLineNo > 0) {
                        ebit = linesIncomeStmt[ebitLineNo];
                        ebitColumns = getDataColumnsForHeader(ebit, "(?i)^((EBIT ))");
                        if (ebitColumns != null && ebitColumns.length != 0) {
                            // evenif columns are not matching then call function getCorrectNumbers
                            if (ebitColumns.length != headerColumnsIncomeStmt.length) {
                                ebitColumns = getDataColumnsForHeader(ebit.replace(",", "").trim(), "(?i)^((EBIT ))");
                                ebitColumns = getCorrectNumbers(ebitColumns);
                            }
                            if (ebitColumns.length == headerColumnsIncomeStmt.length) {
                                if(isEBITDAPresent == true){
                                    ebitda = linesIncomeStmt[ebitdaLineNo].replace("(", "-").replace(")", "");
                                    ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME, headerColumnsIncomeStmt.length);

                                    if (ebitdaColumns != null && ebitdaColumns.length != 0) {
                                        if(ebitdaColumns.length == headerColumnsIncomeStmt.length) {
                                            canImpliedDepreciationBeFound = true;
                                        }
                                    }
                                }

                                MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find() ? "B" : "M";
                                //handling a case of VBL where headers are at two places
                                    /*if(revenueColumns.length == ebitdaColumns.length && revenueColumns.length < headerColumns.length) {
                                        headerColumns = getDataColumnsForHeader(header, "(?i)^((\\s*Key metrics/assumptions))");
                                    }*/
                                if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                    y0Column = y0ColumnNumberOnIncStmt.intValue();
                                    y1Column = y1ColumnNumberOnIncStmt.intValue();
                                    y2Column = y2ColumnNumberOnIncStmt.intValue();
                                } else {
                                    y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                    y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                    y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                                }
                                System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                                System.out.print(" Y1 Index " + y1Column);
                                System.out.print(" Y2 Index " + y2Column + "\n");
                                System.out.println("EBIT Line : " + Arrays.toString(ebitColumns));

                                if (y0Column >= 0) {
                                    y0EBIT = ebitColumns[y0Column];
                                    y0EBITNumber = Double.parseDouble(y0EBIT.replace(",", "").replace("(", "-").replace(")", ""));
                                    if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                        y0EBITNumber = y0EBITNumber / 10;
                                    } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                        y0EBITNumber = y0EBITNumber * 100;
                                    }
                                    if (canImpliedDepreciationBeFound == true) {
                                        y0EBITDA = ebitdaColumns[y0Column];
                                        y0EBITDANo = Double.parseDouble(y0EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                                        if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y0EBITDANo = y0EBITDANo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y0EBITDANo = y0EBITDANo * 100;
                                        }
                                        y0DepreciationNo = y0EBITDANo - y0EBITNumber;
                                        reportParameters.setY0EBIT(y0EBITDANo + "-" + y0DepreciationNo);
                                    } else {
                                        reportParameters.setY0EBIT("" + y0EBITNumber);
                                    }
                                } else {
                                    reportParameters.setY0EBIT(null);
                                    System.out.println("########## Y0 Column Index not found Setting Y0 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                                if (y1Column >= 0) {
                                    y1EBIT = ebitColumns[y1Column];
                                    y1EBITNumber = Double.parseDouble(y1EBIT.replace(",", "").replace("(", "-").replace(")", ""));
                                    if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                        y1EBITNumber = y1EBITNumber / 10;
                                    } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                        y1EBITNumber = y1EBITNumber * 100;
                                    }
                                    if (canImpliedDepreciationBeFound == true) {
                                        y1EBITDA = ebitdaColumns[y1Column];
                                        y1EBITDANo = Double.parseDouble(y1EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                                        if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y1EBITDANo = y1EBITDANo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y1EBITDANo = y1EBITDANo * 100;
                                        }
                                        y1DepreciationNo = y1EBITDANo - y1EBITNumber;
                                        reportParameters.setY1EBIT(y1EBITDANo + "-" + y1DepreciationNo);
                                    } else {
                                        reportParameters.setY1EBIT("" + y1EBITNumber);
                                    }
                                } else {
                                    reportParameters.setY1EBIT(null);
                                    System.out.println("##########  Y1 Column Index not found Setting y1 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                                if (y2Column >= 0) {
                                    y2EBIT = ebitColumns[y2Column];
                                    y2EBITNumber = Double.parseDouble(y2EBIT.replace(",", "").replace("(", "-").replace(")", ""));
                                    if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                        y2EBITNumber = y2EBITNumber / 10;
                                    } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                        y2EBITNumber = y2EBITNumber * 100;
                                    }
                                    if (canImpliedDepreciationBeFound == true) {
                                        y2EBITDA = ebitdaColumns[y2Column];
                                        y2EBITDANo = Double.parseDouble(y2EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                                        if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y2EBITDANo = y2EBITDANo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y2EBITDANo = y2EBITDANo * 100;
                                        }
                                        y2DepreciationNo = y2EBITDANo - y2EBITNumber;
                                        reportParameters.setY2EBIT(y2EBITDANo + "-" + y2DepreciationNo);
                                    } else {
                                        reportParameters.setY2EBIT("" + y2EBITNumber);
                                    }
                                } else {
                                    reportParameters.setY2EBIT(null);
                                    System.out.println("########## Y2 Column Index not found Setting y2 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                            } else {
                                reportParameters.setY0EBIT(null);
                                reportParameters.setY0EBIT(null);
                                reportParameters.setY0EBIT(null);
                                System.out.println("########## EBIT Row Columns (" + ebitColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY0EBIT(null);
                            reportParameters.setY0EBIT(null);
                            reportParameters.setY0EBIT(null);
                            System.out.println("########## EBIT Row Header and EBIT Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0EBIT(null);
                        reportParameters.setY0EBIT(null);
                        reportParameters.setY0EBIT(null);
                        System.out.println("########## EBIT line also not present for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                }
            }

        } catch (Exception e) {
            reportParameters.setY0EBIT(null);
            reportParameters.setY0EBIT(null);
            reportParameters.setY0EBIT(null);
            System.out.println("########## Exception in setting EBIT for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setProfitForFinancials(ReportDataExtractConfig rdec){
        try {
            int revenueLineNo = -1;
            String pat = null;
            String[] patColumns = null;
            int y0Column = -1, y1Column = -1, y2Column = -1;
            String y0PAT = "", y1PAT = "", y2PAT = "";
            double y0PATNumber = 0, y1PATNumber = 0, y2PATNumber = 0;

            if (revenueLineNumber != null)
                revenueLineNo = revenueLineNumber.intValue();
            // Get EBITDA Line No. form the Inc. Statement Page
            int patLineNo = -1;
            patLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, revenueLineNo, EBITDA_ROW_NAME, rdec, BROKER);

            if(patLineNo > 0) {
                pat = linesIncomeStmt[patLineNo].replace("(", "-").replace(")", "");
                patColumns = getDataColumnsForHeader(pat, EBITDA_ROW_NAME);


                if(patColumns == null || (patColumns != null && patColumns.length != headerColumnsIncomeStmt.length && patColumns.length == 0)){
                    if(!linesIncomeStmt[patLineNo - 1].trim().equals(""))
                        pat = linesIncomeStmt[patLineNo] + linesIncomeStmt[patLineNo - 1];
                    else if(!linesIncomeStmt[patLineNo + 1].trim().equals(""))
                        pat = linesIncomeStmt[patLineNo] + linesIncomeStmt[patLineNo + 1];

                    patColumns = getDataColumnsForHeader(pat, EBITDA_ROW_NAME);
                }

                if (patColumns != null && patColumns.length != 0) {
                    if(patColumns.length == headerColumnsIncomeStmt.length) {
                        // Set million or billion flag
                        MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find()? "B" : "M";

                        if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                            y0Column = y0ColumnNumberOnIncStmt.intValue();
                            y1Column = y1ColumnNumberOnIncStmt.intValue();
                            y2Column = y2ColumnNumberOnIncStmt.intValue();
                        } else {
                            y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                            y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                            y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                        }
                        System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column + "\n");
                        System.out.println("PAT Line : " + Arrays.toString(patColumns));

                        if (y0Column >= 0) {
                            y0PAT = patColumns[y0Column];
                            y0PATNumber = Double.parseDouble(y0PAT.replace(",", "").replace("(", "-").replace(")", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y0PATNumber = y0PATNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y0PATNumber = y0PATNumber * 100;
                            }
                            reportParameters.setY0EBIT("" + y0PATNumber);
                        } else {
                            reportParameters.setY0EBIT(null);
                            System.out.println("########## Y0 Column Index not found Setting Y0 PAT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            y1PAT = patColumns[y1Column];

                            y1PATNumber = Double.parseDouble(y1PAT.replace(",", "").replace("(", "-").replace(")", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y1PATNumber = y1PATNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y1PATNumber = y1PATNumber * 100;
                            }
                            reportParameters.setY1EBIT("" + y1PATNumber);
                        } else {
                            reportParameters.setY1EBIT(null);
                            System.out.println("########## Y1 Column Index not found Setting Y1 PAT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            y2PAT = patColumns[y2Column];

                            y2PATNumber = Double.parseDouble(y2PAT.replace(",", "").replace("(", "-").replace(")", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y2PATNumber = y2PATNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y2PATNumber = y2PATNumber * 100;
                            }
                            reportParameters.setY2EBIT("" + y2PATNumber);
                        } else {
                            reportParameters.setY2EBIT(null);
                            System.out.println("########## Y2 Column Index not found Setting Y2 PAT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0EBIT(null);
                        reportParameters.setY0EBIT(null);
                        reportParameters.setY0EBIT(null);
                        System.out.println("########## Net Profit Row Columns (" + patColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0EBIT(null);
                    reportParameters.setY0EBIT(null);
                    reportParameters.setY0EBIT(null);
                    System.out.println("########## PAT Row Header and PAT Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0EBIT(null);
                reportParameters.setY0EBIT(null);
                reportParameters.setY0EBIT(null);
                System.out.println("########## PAT line is not present for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0EBIT(null);
            reportParameters.setY0EBIT(null);
            reportParameters.setY0EBIT(null);
            System.out.println("########## Exception in setting EBIT for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setOPMOrNIM(ReportDataExtractConfig rdec, boolean isFinancialReport){
        int ebitdaLineNo = -1, ebitdaMarginLineNo = -1;
        String ebitdaMargin = null;
        String[] ebitdaMarginColumns = null;
        String y0EBITDAMargin = "0", y1EBITDAMargin = "0", y2EBITDAMargin = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1;
        boolean isMarginOnRatio = false;
        try {
            if (ebitdaLineNumber != null)
                ebitdaLineNo = ebitdaLineNumber.intValue();
            else
                ebitdaLineNo = revenueLineNumber.intValue();

            if(!isFinancialReport)
                ebitdaMarginLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, ebitdaLineNo, EBITDAMARGIN_ROW_NAME, rdec, BROKER);
            else
                ebitdaMarginLineNo = getLineNumberForMatchingPattern(linesRatio, 0, EBITDAMARGIN_ROW_NAME, rdec, BROKER);

            if (ebitdaMarginLineNo == -1 && !isFinancialReport) {
                ebitdaMarginLineNo = getLineNumberForMatchingPattern(linesRatio, 0, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
                isMarginOnRatio = true;
                if(ebitdaMarginLineNo>0)
                    ebitdaMargin = linesRatio[ebitdaMarginLineNo];
            } else if (!isFinancialReport) {
                ebitdaMargin = linesIncomeStmt[ebitdaMarginLineNo].trim();
            }

            if (ebitdaMarginLineNo == -1 && isFinancialReport) {
                ebitdaMarginLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
                isMarginOnRatio = false;
                if(ebitdaMarginLineNo>0)
                    ebitdaMargin = linesIncomeStmt[ebitdaMarginLineNo];
            } else if (isFinancialReport) {
                ebitdaMargin = linesRatio[ebitdaMarginLineNo];
            }

            if (ebitdaMargin != null) {

                ebitdaMargin = ebitdaMargin;
                ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);

                if(isMarginOnRatio) {
                    if (ebitdaMarginColumns.length != headerColumnsRatio.length && ebitdaMarginColumns.length == 0) {
                        if (!linesRatio[ebitdaMarginLineNo + 1].trim().equals(""))
                            ebitdaMargin = linesRatio[ebitdaMarginLineNo] + linesRatio[ebitdaMarginLineNo + 1];
                        else if (!linesRatio[ebitdaMarginLineNo - 1].trim().equals(""))
                            ebitdaMargin = linesRatio[ebitdaMarginLineNo] + linesRatio[ebitdaMarginLineNo - 1];

                        ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                    }
                } else {
                    if (ebitdaMarginColumns.length != headerColumnsIncomeStmt.length && ebitdaMarginColumns.length == 0) {
                        if (!headerColumnsIncomeStmt[ebitdaMarginLineNo + 1].trim().equals(""))
                            ebitdaMargin = headerColumnsIncomeStmt[ebitdaMarginLineNo] + headerColumnsIncomeStmt[ebitdaMarginLineNo + 1];
                        else if (!headerColumnsIncomeStmt[ebitdaMarginLineNo - 1].trim().equals(""))
                            ebitdaMargin = headerColumnsIncomeStmt[ebitdaMarginLineNo] + headerColumnsIncomeStmt[ebitdaMarginLineNo - 1];

                        ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                    }
                }

                // Find Y0, Y1 and Y2 Index position
                if(!isMarginOnRatio) {
                    if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                        y0Column = y0ColumnNumberOnIncStmt.intValue();
                        y1Column = y1ColumnNumberOnIncStmt.intValue();
                        y2Column = y2ColumnNumberOnIncStmt.intValue();
                    } else {
                        y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                    }
                } else {
                    y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                    y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                    y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                    y0ColumnNumberOnRatio = new Integer(y0Column);
                    y1ColumnNumberOnRatio = new Integer(y1Column);
                    y2ColumnNumberOnRatio = new Integer(y2Column);
                }

                System.out.print("Ratio Header : Y0 Index " + y0Column);
                System.out.print(" Y1 Index " + y1Column);
                System.out.print(" Y2 Index " + y2Column + "\n");
                System.out.println("Profit Margin Line : " + Arrays.toString(ebitdaMarginColumns));

                if (ebitdaMarginColumns.length != 0) {
                    if (y0Column >= 0) {
                        try {
                            if (headerColumnsRatio.length != ebitdaMarginColumns.length && headerColumnsRatio.length > ebitdaMarginColumns.length) {
                                System.out.println("########## Header mismatch for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                System.out.print(" Header Length " + headerColumnsRatio.length);
                                System.out.print(" OPM/NIM Length " + ebitdaMarginColumns.length + "\n");
                                y0EBITDAMargin = ebitdaMarginColumns[y0Column - (headerColumnsRatio.length - ebitdaMarginColumns.length)].replace("%", "");
                            } else {
                                y0EBITDAMargin = ebitdaMarginColumns[y0Column].replace("%", "");
                            }
                        } catch (Exception e) {
                            y0EBITDAMargin = "0";
                            System.out.println("########## Exception in setting Y0 OPM/NIM for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        reportParameters.setY0OPM(new BigDecimal(Double.parseDouble(y0EBITDAMargin) / 100).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        reportParameters.setY0OPM(new BigDecimal("0"));
                        System.out.println("########## Y0 Column Index not found. Setting Y0 OPM/NIM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                    if (y1Column >= 0) {
                        try {
                            if (headerColumnsRatio.length != ebitdaMarginColumns.length && headerColumnsRatio.length > ebitdaMarginColumns.length) {
                                y1EBITDAMargin = ebitdaMarginColumns[y1Column - (headerColumnsRatio.length - ebitdaMarginColumns.length)].replace("%", "");
                            } else {
                                y1EBITDAMargin = ebitdaMarginColumns[y1Column].replace("%", "");
                            }
                        } catch (Exception e) {
                            y1EBITDAMargin = "0";
                            System.out.println("########## Exception in setting Y1 OPM/NIM for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        reportParameters.setY1OPM(new BigDecimal(Double.parseDouble(y1EBITDAMargin) / 100).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        reportParameters.setY1OPM(new BigDecimal("0"));
                        System.out.println("########## Y1 Column Index not found. Setting Y1 OPM/NIM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                    if (y2Column >= 0) {
                        try {
                            if (headerColumnsRatio.length != ebitdaMarginColumns.length && headerColumnsRatio.length > ebitdaMarginColumns.length) {
                                y2EBITDAMargin = ebitdaMarginColumns[y2Column - (headerColumnsRatio.length - ebitdaMarginColumns.length)].replace("%", "");
                            } else {
                                y2EBITDAMargin = ebitdaMarginColumns[y2Column].replace("%", "");
                            }
                        } catch (Exception e) {
                            y2EBITDAMargin = "0";
                            System.out.println("########## Exception in setting Y2 OPM/NIM for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        reportParameters.setY2OPM(new BigDecimal(Double.parseDouble(y2EBITDAMargin) / 100).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        reportParameters.setY2OPM(new BigDecimal("0"));
                        System.out.println("########## Y2 Column Index not found. Setting Y2 OPM/NIM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0OPM(new BigDecimal("0"));
                    reportParameters.setY1OPM(new BigDecimal("0"));
                    reportParameters.setY2OPM(new BigDecimal("0"));
                    System.out.println("########## OPM/NIM Line Row Header and Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0OPM(new BigDecimal("0"));
                reportParameters.setY1OPM(new BigDecimal("0"));
                reportParameters.setY2OPM(new BigDecimal("0"));
                System.out.println("########## OPM/NIM Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0OPM(new BigDecimal("0"));
            reportParameters.setY1OPM(new BigDecimal("0"));
            reportParameters.setY2OPM(new BigDecimal("0"));
            System.out.println("########## Exception in setting OPM/NIM for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setROCEOrROE(ReportDataExtractConfig rdec, boolean isFinancialReport){
        int roceLineNo = -1, headerRatioLineNo = -1;
        String roce = null;
        String[] roceColumns = null;
        String y0ROCE = "0", y1ROCE = "0", y2ROCE = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1;
        boolean isRoCEFound = false;

        try {
            if (headerRatioLineNumber != null)
                headerRatioLineNo = headerRatioLineNumber.intValue();
            else
                headerRatioLineNo = 0;

            roceLineNo = getLineNumberForMatchingPattern(linesRatio, headerRatioLineNo, ROCE_ROW_NAME, rdec, BROKER);

            if (roceLineNo > 1) {

                roce = linesRatio[roceLineNo].trim();
                roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME);

                if (roceColumns == null || roceColumns.length == 0){
                    roceColumns = getDataColumnsForHeader(roce + linesRatio[roceLineNo-1], ROCE_ROW_NAME);
                }

                // Find Y0, Y1 and Y2 Index position
                if(y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                    y0Column = y0ColumnNumberOnRatio.intValue();
                    y1Column = y1ColumnNumberOnRatio.intValue();
                    y2Column = y2ColumnNumberOnRatio.intValue();
                } else {
                    y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                    y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                    y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                }

                System.out.print("Ratio Header : Y0 Index " + y0Column);
                System.out.print(" Y1 Index " + y1Column);
                System.out.print(" Y2 Index " + y2Column + "\n");
                System.out.println("ROCE/ROE Columns : " + Arrays.toString(roceColumns));

                if(roceColumns != null && roceColumns.length != 0) {
                    isRoCEFound = true;
                    if (y0Column >= 0) {
                        try {
                            if (headerColumnsRatio.length != roceColumns.length && headerColumnsRatio.length > roceColumns.length) {
                                System.out.println("########## Header mismatch for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                System.out.print(" Header Length " + headerColumnsRatio.length);
                                System.out.print(" ROCE Length " + roceColumns.length + "\n");

                                y0ROCE = roceColumns[y0Column - (headerColumnsRatio.length - roceColumns.length)].replace("%", "").replace("NM", "0");
                            } else {
                                y0ROCE = roceColumns[y0Column].replace("%", "").replace("NM", "0");
                            }
                        } catch (Exception e) {
                            y0ROCE = "0";
                            System.out.println("########## Exception in setting RoCE/RoE Y0 Column Index for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        reportParameters.setY0ROCE(new BigDecimal(Double.parseDouble(y0ROCE) / 100).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        reportParameters.setY0ROCE(new BigDecimal("0"));
                        System.out.println("########## Y0 Column Index not found Setting Y0 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                    if (y1Column >= 0) {
                        try {
                            if (headerColumnsRatio.length != roceColumns.length && headerColumnsRatio.length > roceColumns.length) {
                                y1ROCE = roceColumns[y1Column - (headerColumnsRatio.length - roceColumns.length)].replace("%", "").replace("NM", "0");
                            } else {
                                y1ROCE = roceColumns[y1Column].replace("%", "").replace("NM", "0");
                            }
                        } catch (Exception e) {
                            y1ROCE = "0";
                            System.out.println("########## Exception in setting RoCE/RoE Y1 Column Index for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        reportParameters.setY1ROCE(new BigDecimal(Double.parseDouble(y1ROCE) / 100).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        reportParameters.setY1ROCE(new BigDecimal("0"));
                        System.out.println("########## Y1 Column Index not found Setting Y1 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                    if (y2Column >= 0) {
                        try {
                            if (headerColumnsRatio.length != roceColumns.length && headerColumnsRatio.length > roceColumns.length) {
                                y2ROCE = roceColumns[y2Column - (headerColumnsRatio.length - roceColumns.length)].replace("%", "").replace("NM", "0");
                            } else {
                                y2ROCE = roceColumns[y2Column].replace("%", "").replace("NM", "0");
                            }
                        } catch (Exception e) {
                            y2ROCE = "0";
                            System.out.println("########## Exception in setting RoCE/RoE Y2 Column Index for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        reportParameters.setY2ROCE(new BigDecimal(Double.parseDouble(y2ROCE) / 100).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        reportParameters.setY2ROCE(new BigDecimal("0"));
                        System.out.println("########## Y2 Column Index not found Setting Y2 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0ROCE(new BigDecimal("0"));
                    reportParameters.setY1ROCE(new BigDecimal("0"));
                    reportParameters.setY2ROCE(new BigDecimal("0"));
                    isRoCEFound = false;
                    System.out.println("########## RoCE/RoE Line Row Header and Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0ROCE(new BigDecimal("0"));
                reportParameters.setY1ROCE(new BigDecimal("0"));
                reportParameters.setY2ROCE(new BigDecimal("0"));
                isRoCEFound = false;
                System.out.println("########## RoCE/RoE Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
            if (!isRoCEFound) {
                roceLineNo = getLineNumberForMatchingPattern(linesFirstPage, 0, ROCE_ROW_NAME, rdec, BROKER);
                if(roceLineNo > 0) {
                    roce = linesFirstPage[roceLineNo];
                    roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME, headerColumnsFirstPage.length);

                    if (roceColumns != null && roceColumns.length != 0) {
                        if (headerColumnsFirstPage.length == roceColumns.length) {
                            // Find Y0, Y1 and Y2 Index position
                            y0Column = getIndexOfTheYear(headerColumnsFirstPage, Y0);
                            y1Column = getIndexOfTheYear(headerColumnsFirstPage, Y1);
                            y2Column = getIndexOfTheYear(headerColumnsFirstPage, Y2);

                            System.out.print("First Page Header : Y0 Index " + y0Column);
                            System.out.print(" Y1 Index " + y1Column);
                            System.out.print(" Y2 Index " + y2Column + "\n");
                            System.out.println("ROCE/ROE Columns : " + Arrays.toString(roceColumns));

                            if (y0Column >= 0) {
                                try {
                                    y0ROCE = roceColumns[y0Column].replace("%", "").replace("NM", "0");
                                } catch (Exception e) {
                                    y0ROCE = "0";
                                    System.out.println("########## Exception in setting RoCE/RoE Y0 Column Index for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                                reportParameters.setY0ROCE(new BigDecimal(Double.parseDouble(y0ROCE) / 100).setScale(4, RoundingMode.HALF_UP));
                            } else {
                                reportParameters.setY0ROCE(new BigDecimal("0"));
                                System.out.println("########## Y0 Column Index not found Setting Y0 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            if (y1Column >= 0) {
                                try {
                                    y1ROCE = roceColumns[y1Column].replace("%", "").replace("NM", "0");
                                } catch (Exception e) {
                                    y1ROCE = "0";
                                    System.out.println("########## Exception in setting RoCE/RoE Y1 Column Index for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                                reportParameters.setY1ROCE(new BigDecimal(Double.parseDouble(y1ROCE) / 100).setScale(4, RoundingMode.HALF_UP));
                            } else {
                                reportParameters.setY1ROCE(new BigDecimal("0"));
                                System.out.println("########## Y1 Column Index not found Setting Y1 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            if (y2Column >= 0) {
                                try {
                                    y2ROCE = roceColumns[y2Column].replace("%", "").replace("NM", "0");
                                } catch (Exception e) {
                                    y2ROCE = "0";
                                    System.out.println("########## Exception in setting RoCE/RoE Y0 Column Index for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                                reportParameters.setY2ROCE(new BigDecimal(Double.parseDouble(y2ROCE) / 100).setScale(4, RoundingMode.HALF_UP));
                            } else {
                                reportParameters.setY2ROCE(new BigDecimal("0"));
                                System.out.println("########## Y2 Column Index not found Setting Y2 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY0ROCE(new BigDecimal("0"));
                            reportParameters.setY1ROCE(new BigDecimal("0"));
                            reportParameters.setY2ROCE(new BigDecimal("0"));
                            System.out.println("########## RoCE/ROE Row Columns (" + roceColumns.length + ") and Header Row Columns (" + headerColumnsFirstPage.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0ROCE(new BigDecimal("0"));
                        reportParameters.setY1ROCE(new BigDecimal("0"));
                        reportParameters.setY2ROCE(new BigDecimal("0"));
                        System.out.println("########## RoCE/ROE Row Header and ROCE/ROE Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0ROCE(new BigDecimal("0"));
                    reportParameters.setY1ROCE(new BigDecimal("0"));
                    reportParameters.setY2ROCE(new BigDecimal("0"));
                    System.out.println("########## RoCE/ROE Row not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            }
        } catch (Exception e) {
            reportParameters.setY0ROCE(new BigDecimal("0"));
            reportParameters.setY1ROCE(new BigDecimal("0"));
            reportParameters.setY2ROCE(new BigDecimal("0"));
            System.out.println("########## Exception in setting RoCE/RoE for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setEVBYEBITRatio(ReportDataExtractConfig rdec, boolean isFinancialReport){
        int evbyebitdaLineNumber = -1;
        String evbyebitda = "";
        String y0EVBYEBITDA = "", y1EVBYEBITDA = "", y2EVBYEBITDA = "";
        double y0EVBYEBITDANumber = 0, y1EVBYEBITDANumber = 0, y2EVBYEBITDANumber = 0;
        double y0EVBYEBITNumber = 0, y1EVBYEBITNumber = 0, y2EVBYEBITNumber = 0;
        String[] evbyebitdaColumns = null;
        int y0Column = -1, y1Column = -1, y2Column = -1;
        boolean isValuationOnFirstPage = false, isPriceToBookValue = false;
        try {
            if(valuationPageNumber != null && valuationPageNumber.intValue() == 1)
                isValuationOnFirstPage = true;

            if(EVBYEBITDA_ROW_NAME.contains("EBIT"))
                isPriceToBookValue = false;
            else
                isPriceToBookValue = true;

            if(!isValuationOnFirstPage)
                evbyebitdaLineNumber = getLineNumberForMatchingPattern(linesValuation, 0, EVBYEBITDA_ROW_NAME, rdec, BROKER);
            else
                evbyebitdaLineNumber = getLineNumberForMatchingPattern(linesFirstPage, 0, EVBYEBITDA_ROW_NAME, rdec, BROKER);

            if (evbyebitdaLineNumber >= 0) {
                if(!isValuationOnFirstPage)
                    evbyebitda = linesValuation[evbyebitdaLineNumber];
                else
                    evbyebitda = linesFirstPage[evbyebitdaLineNumber];

                evbyebitdaColumns = getDataColumnsForHeader(evbyebitda, EVBYEBITDA_ROW_NAME);

                if(evbyebitdaColumns != null) {
                    if ((!isValuationOnFirstPage && headerColumnsValuation.length != evbyebitdaColumns.length) || (isValuationOnFirstPage && headerColumnsFirstPage.length != evbyebitdaColumns.length)) {
                        Pattern pattern = Pattern.compile(HEADER_ROW_NAME);
                        Matcher matcher = pattern.matcher(headerValuation.trim());
                        if (matcher.find()) {
                            String rowHeader = matcher.group(1);
                            int rowHeaderlocation = headerValuation.trim().indexOf(rowHeader);
                            String strDataColumns = headerValuation.trim().substring(rowHeaderlocation + rowHeader.length(), headerValuation.trim().length());
                            headerColumnsValuation = strDataColumns.trim().split(" ");
                            headerColumnsValuation = Arrays.stream(headerColumnsValuation)
                                    .filter(s -> !s.isEmpty())
                                    .toArray(String[]::new);
                        }
                    }
                    if(!isValuationOnFirstPage) {
                        y0Column = getIndexOfTheYear(headerColumnsValuation, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsValuation, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsValuation, Y2);
                    } else {
                        y0Column = getIndexOfTheYear(headerColumnsFirstPage, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsFirstPage, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsFirstPage, Y2);
                    }
                    System.out.print("Valuation Header : Y0 Index " + y0Column);
                    System.out.print(" Y1 Index " + y1Column);
                    System.out.print(" Y2 Index " + y2Column + "\n");
                    System.out.println("Valuation Columns : " + Arrays.toString(evbyebitdaColumns));

                    if (y0Column >= 0) {
                        try {
                            if (!isValuationOnFirstPage && headerColumnsValuation.length != evbyebitdaColumns.length && headerColumnsValuation.length > evbyebitdaColumns.length) {
                                y0EVBYEBITDA = evbyebitdaColumns[y0Column - (headerColumnsValuation.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                            } else {
                                y0EVBYEBITDA = evbyebitdaColumns[y0Column].replace("nmf", "").replace("nm", "");
                            }
                            if (y0EVBYEBITDA.isEmpty())
                                y0EVBYEBITDA = "0";
                            if (y0EVBYEBITDA.contains("(") && y0EVBYEBITDA.contains(")") && !y0EVBYEBITDA.contains("-"))
                                y0EVBYEBITDA = "-" + y0EVBYEBITDA.replace("(", "").replace(")", "");

                            y0EVBYEBITDANumber = Double.parseDouble(y0EVBYEBITDA);
                            if(!isPriceToBookValue) {
                                if (y0EBITDANumber != null && y0DepreciationNumber != null) {
                                    if (y0EBITDANumber.doubleValue() > 0 && Math.abs(y0DepreciationNumber.doubleValue()) > 0 && y0EVBYEBITDANumber > 0) {
                                        y0EVBYEBITNumber = (y0EVBYEBITDANumber * y0EBITDANumber.doubleValue()) / (y0EBITDANumber.doubleValue() - Math.abs(y0DepreciationNumber.doubleValue()));
                                        BigDecimal bd = new BigDecimal(y0EVBYEBITNumber);
                                        bd = bd.setScale(4, RoundingMode.HALF_UP);
                                        y0EVBYEBITNumber = bd.doubleValue();
                                        reportParameters.setY0EVBYEBIT(new BigDecimal(y0EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
                                    } else {
                                        reportParameters.setY0EVBYEBIT(new BigDecimal("0"));
                                        System.out.println("########## Either EBITDA, Dep. or EV/EBITDA not found. Setting Y0 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    }
                                }
                            } else {
                                reportParameters.setY0EVBYEBIT(new BigDecimal(y0EVBYEBITDANumber).setScale(2, RoundingMode.HALF_UP));
                            }
                        } catch (Exception e) {
                            System.out.println("########## Y0 Column Index not found Setting Y0 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY0EVBYEBIT(new BigDecimal("0"));
                        }
                    }
                    if (y1Column >= 0) {
                        try {
                            if (!isValuationOnFirstPage && headerColumnsValuation.length != evbyebitdaColumns.length && headerColumnsValuation.length > evbyebitdaColumns.length) {
                                y1EVBYEBITDA = evbyebitdaColumns[y1Column - (headerColumnsValuation.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                            } else {
                                y1EVBYEBITDA = evbyebitdaColumns[y1Column].replace("nmf", "").replace("nm", "");
                            }
                            if (y1EVBYEBITDA.isEmpty())
                                y1EVBYEBITDA = "0";
                            if (y1EVBYEBITDA.contains("(") && y1EVBYEBITDA.contains(")") && !y1EVBYEBITDA.contains("-"))
                                y1EVBYEBITDA = "-" + y1EVBYEBITDA.replace("(", "").replace(")", "");

                            y1EVBYEBITDANumber = Double.parseDouble(y1EVBYEBITDA);
                            if(!isPriceToBookValue) {
                                if (y1EBITDANumber != null && y1DepreciationNumber != null) {
                                    if (y1EBITDANumber.doubleValue() > 0 && Math.abs(y1DepreciationNumber.doubleValue()) > 0 && y1EVBYEBITDANumber > 0) {
                                        y1EVBYEBITNumber = (y1EVBYEBITDANumber * y1EBITDANumber.doubleValue()) / (y1EBITDANumber.doubleValue() - Math.abs(y1DepreciationNumber.doubleValue()));
                                        BigDecimal bd = new BigDecimal(y1EVBYEBITNumber);
                                        bd = bd.setScale(4, RoundingMode.HALF_UP);
                                        y1EVBYEBITNumber = bd.doubleValue();
                                        reportParameters.setY1EVBYEBIT(new BigDecimal(y1EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
                                    } else {
                                        reportParameters.setY1EVBYEBIT(new BigDecimal("0"));
                                        System.out.println("########## Either EBITDA, Dep. or EV/EBITDA not found. Setting Y1 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    }
                                }
                            } else {
                                reportParameters.setY1EVBYEBIT(new BigDecimal(y1EVBYEBITDANumber).setScale(2, RoundingMode.HALF_UP));
                            }
                        } catch (Exception e) {
                            System.out.println("########## Y1 Column Index not found Setting Y1 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY1EVBYEBIT(new BigDecimal("0"));
                        }
                    }
                    if (y2Column >= 0) {
                        try {
                            if (!isValuationOnFirstPage && headerColumnsValuation.length != evbyebitdaColumns.length && headerColumnsValuation.length > evbyebitdaColumns.length) {
                                y2EVBYEBITDA = evbyebitdaColumns[y2Column - (headerColumnsValuation.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                            } else {
                                y2EVBYEBITDA = evbyebitdaColumns[y2Column].replace("nmf", "").replace("nm", "");
                            }
                            if (y2EVBYEBITDA.isEmpty())
                                y2EVBYEBITDA = "0";
                            if (y2EVBYEBITDA.contains("(") && y2EVBYEBITDA.contains(")") && !y2EVBYEBITDA.contains("-"))
                                y2EVBYEBITDA = "-" + y2EVBYEBITDA.replace("(", "").replace(")", "");

                            y2EVBYEBITDANumber = Double.parseDouble(y2EVBYEBITDA);
                            if(!isPriceToBookValue) {
                                if (y2EBITDANumber != null && y2DepreciationNumber != null) {
                                    if (y2EBITDANumber.doubleValue() > 0 && Math.abs(y2DepreciationNumber.doubleValue()) > 0 && y2EVBYEBITDANumber > 0) {
                                        y2EVBYEBITNumber = (y2EVBYEBITDANumber * y2EBITDANumber.doubleValue()) / (y2EBITDANumber.doubleValue() - Math.abs(y2DepreciationNumber.doubleValue()));
                                        BigDecimal bd = new BigDecimal(y2EVBYEBITNumber);
                                        bd = bd.setScale(4, RoundingMode.HALF_UP);
                                        y2EVBYEBITNumber = bd.doubleValue();
                                        reportParameters.setY2EVBYEBIT(new BigDecimal(y2EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
                                    } else {
                                        reportParameters.setY2EVBYEBIT(new BigDecimal("0"));
                                        System.out.println("########## Either EBITDA, Dep. or EV/EBITDA not found. Setting Y2 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    }
                                }
                            } else {
                                reportParameters.setY2EVBYEBIT(new BigDecimal(y2EVBYEBITDANumber).setScale(2, RoundingMode.HALF_UP));
                            }
                        } catch (Exception e) {
                            System.out.println("########## Y2 Column Index not found Setting y2 RoCE/RoE = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY2EVBYEBIT(new BigDecimal("0"));
                        }
                    }
                }
            } else {
                reportParameters.setY0EVBYEBIT(new BigDecimal("0"));
                reportParameters.setY1EVBYEBIT(new BigDecimal("0"));
                reportParameters.setY2EVBYEBIT(new BigDecimal("0"));
                System.out.println("########## Line EV/EBITDA or P/B not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e){
            reportParameters.setY0EVBYEBIT(new BigDecimal("0"));
            reportParameters.setY1EVBYEBIT(new BigDecimal("0"));
            reportParameters.setY2EVBYEBIT(new BigDecimal("0"));
            System.out.println("########## Exception in setting EV/EBIT or P/B for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setAUM(ReportDataExtractConfig rdec){
        int aumLineNo = -1;
        int y0Column = -1, y1Column = -1, y2Column = -1;
        String aum = null;
        String[] aumColumns = null;
        String y0AUM = "", y1AUM = "", y2AUM = "";
        double y0AUMNumber = 0, y1AUMNumber = 0, y2AUMNumber = 0;
        String MILLIONS_OR_BILLIONS_FLAG_OLD = MILLIONS_OR_BILLIONS_FLAG;
        boolean isAUMOnRatioPage = false;
        try {
            // Get AUM Line No. form the Inc. Statement Page
            aumLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, AUM_ROW_NAME, rdec, BROKER);
            if (aumLineNo < 0) {
                aumLineNo = getLineNumberForMatchingPattern(linesRatio, 0, AUM_ROW_NAME, rdec, BROKER);
                isAUMOnRatioPage = true;
            }

            if (aumLineNo > 1) {
                if(!isAUMOnRatioPage)
                    aum = linesIncomeStmt[aumLineNo];
                else
                    aum = linesRatio[aumLineNo];

                aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);

                if(!isAUMOnRatioPage) {
                    if (aumColumns != null && aumColumns.length != headerColumnsIncomeStmt.length && aumColumns.length == 0) {
                        if (!linesIncomeStmt[aumLineNo - 1].trim().equals(""))
                            aum = linesIncomeStmt[aumLineNo] + linesIncomeStmt[aumLineNo - 1];
                        else if (!linesIncomeStmt[aumLineNo + 1].trim().equals(""))
                            aum = linesIncomeStmt[aumLineNo] + linesIncomeStmt[aumLineNo + 1];

                        aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);
                    }
                } else {
                    if (aumColumns != null && aumColumns.length != headerColumnsRatio.length && aumColumns.length == 0) {
                        if (!headerColumnsRatio[aumLineNo - 1].trim().equals(""))
                            aum = headerColumnsRatio[aumLineNo] + headerColumnsRatio[aumLineNo - 1];
                        else if (!headerColumnsRatio[aumLineNo + 1].trim().equals(""))
                            aum = headerColumnsRatio[aumLineNo] + headerColumnsRatio[aumLineNo + 1];

                        aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);
                    }
                }

                if(aumColumns != null && aumColumns.length != 0 ) {
                    if((!isAUMOnRatioPage && aumColumns.length == headerColumnsIncomeStmt.length) || (isAUMOnRatioPage && aumColumns.length == headerColumnsRatio.length)) {

                        if (aum.contains("INR b"))
                            MILLIONS_OR_BILLIONS_FLAG = "B";
                        if (aum.contains("INR m"))
                            MILLIONS_OR_BILLIONS_FLAG = "M";

                        // Find Y0, Y1 and Y2 Index position
                        if (!isAUMOnRatioPage) {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        }

                        System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column + "\n");
                        System.out.println("AUM Columns : " + Arrays.toString(aumColumns));

                        if (y0Column >= 0) {
                            try {
                                y0AUM = aumColumns[y0Column];
                                y0AUMNumber = Double.parseDouble(y0AUM.replace(",", ""));
                                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y0AUMNumber = y0AUMNumber / 10;
                                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y0AUMNumber = y0AUMNumber * 100;
                                }
                                reportParameters.setY0AUM(new BigDecimal(y0AUMNumber).setScale(2, RoundingMode.HALF_UP));
                            } catch (Exception e) {
                                reportParameters.setY0AUM(new BigDecimal("0"));
                                System.out.println("########## Exception in setting Y0 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY0AUM(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            try {
                                y1AUM = aumColumns[y1Column];
                                y1AUMNumber = Double.parseDouble(y1AUM.replace(",", ""));
                                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y1AUMNumber = y1AUMNumber / 10;
                                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y1AUMNumber = y1AUMNumber * 100;
                                }
                                reportParameters.setY1AUM(new BigDecimal(y1AUMNumber).setScale(2, RoundingMode.HALF_UP));
                            } catch (Exception e) {
                                reportParameters.setY1AUM(new BigDecimal("0"));
                                System.out.println("########## Exception in setting Y1 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY1AUM(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            try {
                                y2AUM = aumColumns[y2Column];
                                y2AUMNumber = Double.parseDouble(y2AUM.replace(",", ""));
                                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y2AUMNumber = y2AUMNumber / 10;
                                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y2AUMNumber = y2AUMNumber * 100;
                                }
                                reportParameters.setY2AUM(new BigDecimal(y2AUMNumber).setScale(2, RoundingMode.HALF_UP));
                            } catch (Exception e) {
                                reportParameters.setY2AUM(new BigDecimal("0"));
                                System.out.println("########## Exception in setting Y2 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY2AUM(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        System.out.println("########## AUM Row Columns (" + aumColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same, so made the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        // Set million or billion flag
                        if (aum.contains("INR b"))
                            MILLIONS_OR_BILLIONS_FLAG = "B";
                        if (aum.contains("INR m"))
                            MILLIONS_OR_BILLIONS_FLAG = "M";

                        // Find Y0, Y1 and Y2 Index position
                        if (!isAUMOnRatioPage) {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        }

                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;
                        if (!isAUMOnRatioPage) {
                            adjustedY0Column = aumColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY1Column = aumColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY2Column = aumColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                        } else {
                            adjustedY0Column = aumColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY1Column = aumColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY2Column = aumColumns.length - (headerColumnsRatio.length - y0Column);
                        }

                        System.out.print("Inc. Stmt. Header : Y0 Index " + adjustedY0Column);
                        System.out.print(" Y1 Index " + adjustedY1Column);
                        System.out.print(" Y2 Index " + adjustedY2Column + "\n");
                        System.out.println("AUM Columns : " + Arrays.toString(aumColumns));

                        if(adjustedY0Column >=0) {
                            try {
                                y0AUM = aumColumns[adjustedY0Column];
                                y0AUMNumber = Double.parseDouble(y0AUM.replace(",", ""));
                                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y0AUMNumber = y0AUMNumber / 10;
                                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y0AUMNumber = y0AUMNumber * 100;
                                }
                                reportParameters.setY0AUM(new BigDecimal(y0AUMNumber).setScale(2, RoundingMode.HALF_UP));
                            } catch (Exception e) {
                                reportParameters.setY0AUM(new BigDecimal("0"));
                                System.out.println("########## Exception in setting Y0 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY0AUM(new BigDecimal("0"));
                            System.out.println("########## Adj. Y0 Column Index not found Setting Y2 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if(adjustedY1Column >= 0) {
                            try {
                                y1AUM = aumColumns[adjustedY1Column];
                                y1AUMNumber = Double.parseDouble(y1AUM.replace(",", ""));
                                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y1AUMNumber = y1AUMNumber / 10;
                                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y1AUMNumber = y1AUMNumber * 100;
                                }
                                reportParameters.setY1AUM(new BigDecimal(y1AUMNumber).setScale(2, RoundingMode.HALF_UP));
                            } catch (Exception e) {
                                reportParameters.setY1AUM(new BigDecimal("0"));
                                System.out.println("########## Exception in setting Y1 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY1AUM(new BigDecimal("0"));
                            System.out.println("########## Adj. Y1 Column Index not found Setting Y2 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if(adjustedY2Column >= 0) {
                            try {
                                y2AUM = aumColumns[adjustedY2Column];
                                y2AUMNumber = Double.parseDouble(y2AUM.replace(",", ""));
                                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y2AUMNumber = y2AUMNumber / 10;
                                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y2AUMNumber = y2AUMNumber * 100;
                                }
                                reportParameters.setY2AUM(new BigDecimal(y2AUMNumber).setScale(2, RoundingMode.HALF_UP));
                            } catch (Exception e) {
                                reportParameters.setY2AUM(new BigDecimal("0"));
                                System.out.println("########## Exception in setting Y2 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY2AUM(new BigDecimal("0"));
                            System.out.println("########## Adj. Y2 Column Index not found Setting Y2 AUM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    }
                } else {
                    reportParameters.setY0AUM(new BigDecimal("0"));
                    reportParameters.setY1AUM(new BigDecimal("0"));
                    reportParameters.setY2AUM(new BigDecimal("0"));
                    System.out.println("########## AUM Row Header and Revenue Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            }
            else {
                reportParameters.setY0AUM(new BigDecimal("0"));
                reportParameters.setY1AUM(new BigDecimal("0"));
                reportParameters.setY2AUM(new BigDecimal("0"));
                System.out.println("########## AUM Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
            MILLIONS_OR_BILLIONS_FLAG = MILLIONS_OR_BILLIONS_FLAG_OLD;
        } catch (Exception e){
            MILLIONS_OR_BILLIONS_FLAG = MILLIONS_OR_BILLIONS_FLAG_OLD;
            reportParameters.setY0AUM(new BigDecimal("0"));
            reportParameters.setY1AUM(new BigDecimal("0"));
            reportParameters.setY2AUM(new BigDecimal("0"));
            System.out.println("########## Exception in setting AUM for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setCreditCost(ReportDataExtractConfig rdec){
        int creditCostLineNo = -1;
        String creditCost = null;
        String[] creditCostColumns = null;
        String y0CreditCost = "0", y1CreditCost = "0", y2CreditCost = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1;
        boolean isCreditCostOnIncStmt = false;

        try {
            creditCostLineNo = getLineNumberForMatchingPattern(linesRatio, 0, CREDITCOSTS_ROW_NAME, rdec, BROKER);
            if(creditCostLineNo < 0) {
                creditCostLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, CREDITCOSTS_ROW_NAME, rdec, BROKER);
                isCreditCostOnIncStmt = true;
            }

            if (creditCostLineNo > 1) {
                if(!isCreditCostOnIncStmt)
                    creditCost = linesRatio[creditCostLineNo];
                else
                    creditCost = linesIncomeStmt[creditCostLineNo];

                creditCostColumns = getDataColumnsForHeader(creditCost, CREDITCOSTS_ROW_NAME);

                if(!isCreditCostOnIncStmt) {
                    if (creditCostColumns != null && creditCostColumns.length != headerColumnsRatio.length && creditCostColumns.length == 0) {
                        if (!linesRatio[creditCostLineNo - 1].trim().equals(""))
                            creditCost = linesRatio[creditCostLineNo] + linesRatio[creditCostLineNo - 1];
                        else if (!linesRatio[creditCostLineNo + 1].trim().equals(""))
                            creditCost = linesRatio[creditCostLineNo] + linesRatio[creditCostLineNo + 1];

                        creditCostColumns = getDataColumnsForHeader(creditCost, CREDITCOSTS_ROW_NAME);
                    }
                } else {
                    if (creditCostColumns != null && creditCostColumns.length != headerColumnsIncomeStmt.length && creditCostColumns.length == 0) {
                        if (!linesIncomeStmt[creditCostLineNo - 1].trim().equals(""))
                            creditCost = linesIncomeStmt[creditCostLineNo] + linesIncomeStmt[creditCostLineNo - 1];
                        else if (!linesIncomeStmt[creditCostLineNo + 1].trim().equals(""))
                            creditCost = linesIncomeStmt[creditCostLineNo] + linesIncomeStmt[creditCostLineNo + 1];

                        creditCostColumns = getDataColumnsForHeader(creditCost, CREDITCOSTS_ROW_NAME);
                    }
                }

                if(creditCostColumns!= null && creditCostColumns.length != 0) {
                    if((!isCreditCostOnIncStmt && creditCostColumns.length == headerColumnsRatio.length) || (isCreditCostOnIncStmt && creditCostColumns.length == headerColumnsIncomeStmt.length)) {

                        // Find Y0, Y1 and Y2 Index position
                        if(!isCreditCostOnIncStmt) {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        }

                        System.out.print("Ratio Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column + "\n");
                        System.out.println("Credit Cost Line : " + Arrays.toString(creditCostColumns));

                        if (y0Column >= 0) {
                            try {
                                y0CreditCost = creditCostColumns[y0Column].replace("%", "");
                            } catch (Exception e) {
                                y0CreditCost = "0";
                                System.out.println("########## Exception in setting Y0 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY0CreditCost(new BigDecimal(Double.parseDouble(y0CreditCost) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY0CreditCost(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            try {
                                y1CreditCost = creditCostColumns[y1Column].replace("%", "");
                            } catch (Exception e) {
                                y1CreditCost = "0";
                                System.out.println("########## Exception in setting Y1 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY1CreditCost(new BigDecimal(Double.parseDouble(y1CreditCost) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY1CreditCost(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            try {
                                y2CreditCost = creditCostColumns[y2Column].replace("%", "");
                            } catch (Exception e) {
                                y2CreditCost = "0";
                                System.out.println("########## Exception in setting Y2 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY2CreditCost(new BigDecimal(Double.parseDouble(y2CreditCost) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY2CreditCost(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        System.out.println("########## Inc. Stmt Header column count (" + headerColumnsIncomeStmt.length + ") not matching with Credit Cost column count(" + creditCostColumns.length + "), used the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        // Find Y0, Y1 and Y2 Index position
                        if(!isCreditCostOnIncStmt) {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        }
                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;
                        if(!isCreditCostOnIncStmt) {
                            adjustedY0Column = creditCostColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY1Column = creditCostColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY2Column = creditCostColumns.length - (headerColumnsRatio.length - y0Column);
                        } else {
                            adjustedY0Column = creditCostColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY1Column = creditCostColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY2Column = creditCostColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                        }

                        System.out.print("Inc. Stmt. Header : Y0 Index " + adjustedY0Column);
                        System.out.print(" Y1 Index " + adjustedY1Column);
                        System.out.print(" Y2 Index " + adjustedY2Column + "\n");
                        System.out.println("Credit Cost Columns : " + Arrays.toString(creditCostColumns));
                        if (adjustedY0Column >= 0) {
                            try {
                                y0CreditCost = creditCostColumns[adjustedY0Column].replace("%", "");
                            } catch (Exception e) {
                                y0CreditCost = "0";
                                System.out.println("########## Exception in setting Y0 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY0CreditCost(new BigDecimal(Double.parseDouble(y0CreditCost) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y0 Column Index not found Setting Y0 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY0CreditCost(new BigDecimal("0"));
                        }
                        if (adjustedY1Column >= 0) {
                            try {
                                y1CreditCost = creditCostColumns[adjustedY1Column].replace("%", "");
                            } catch (Exception e) {
                                y1CreditCost = "0";
                                System.out.println("########## Exception in setting Y1 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY1CreditCost(new BigDecimal(Double.parseDouble(y1CreditCost) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y1 Column Index not found Setting Y0 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY1CreditCost(new BigDecimal("0"));
                        }
                        if (adjustedY2Column >= 0) {
                            try {
                                y2CreditCost = creditCostColumns[adjustedY2Column].replace("%", "");
                            } catch (Exception e) {
                                y2CreditCost = "0";
                                System.out.println("########## Exception in setting Y2 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY2CreditCost(new BigDecimal(Double.parseDouble(y2CreditCost) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y2 Column Index not found Setting Y0 Credit Cost = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY2CreditCost(new BigDecimal("0"));
                        }
                    }
                } else {
                    reportParameters.setY0CreditCost(new BigDecimal("0"));
                    reportParameters.setY1CreditCost(new BigDecimal("0"));
                    reportParameters.setY2CreditCost(new BigDecimal("0"));
                    System.out.println("########## Credit Cost Row Header and data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0CreditCost(new BigDecimal("0"));
                reportParameters.setY1CreditCost(new BigDecimal("0"));
                reportParameters.setY2CreditCost(new BigDecimal("0"));
                System.out.println("########## Credit Cost Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0CreditCost(new BigDecimal("0"));
            reportParameters.setY1CreditCost(new BigDecimal("0"));
            reportParameters.setY2CreditCost(new BigDecimal("0"));
            System.out.println("########## Exception in setting Credit Cost for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setGNPA(ReportDataExtractConfig rdec){
        int gnpaLineNo = -1;
        String gnpa = null;
        String[] gnpaColumns = null;
        String y0GNPA = "0", y1GNPA = "0", y2GNPA = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1;
        boolean isGNPAOnIncStmt = false;

        try {
            gnpaLineNo = getLineNumberForMatchingPattern(linesRatio, 0, GNPA_ROW_NAME, rdec, BROKER);
            if(gnpaLineNo < 0) {
                gnpaLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, GNPA_ROW_NAME, rdec, BROKER);
                isGNPAOnIncStmt = true;
            }

            if (gnpaLineNo > 1) {
                if(!isGNPAOnIncStmt)
                    gnpa = linesRatio[gnpaLineNo];
                else
                    gnpa = linesIncomeStmt[gnpaLineNo];

                gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);

                if(!isGNPAOnIncStmt) {
                    if (gnpaColumns != null && gnpaColumns.length != headerColumnsRatio.length && gnpaColumns.length == 0) {
                        if (!linesRatio[gnpaLineNo - 1].trim().equals(""))
                            gnpa = linesRatio[gnpaLineNo] + linesRatio[gnpaLineNo - 1];
                        else if (!linesRatio[gnpaLineNo + 1].trim().equals(""))
                            gnpa = linesRatio[gnpaLineNo] + linesRatio[gnpaLineNo + 1];

                        gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);
                    }
                } else {
                    if (gnpaColumns != null && gnpaColumns.length != headerColumnsIncomeStmt.length && gnpaColumns.length == 0) {
                        if (!linesIncomeStmt[gnpaLineNo - 1].trim().equals(""))
                            gnpa = linesIncomeStmt[gnpaLineNo] + linesIncomeStmt[gnpaLineNo - 1];
                        else if (!linesIncomeStmt[gnpaLineNo + 1].trim().equals(""))
                            gnpa = linesIncomeStmt[gnpaLineNo] + linesIncomeStmt[gnpaLineNo + 1];

                        gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);
                    }
                }

                if(gnpaColumns!= null && gnpaColumns.length != 0) {
                    if((!isGNPAOnIncStmt && gnpaColumns.length == headerColumnsRatio.length) || (isGNPAOnIncStmt && gnpaColumns.length == headerColumnsIncomeStmt.length)) {

                        // Find Y0, Y1 and Y2 Index position
                        if(!isGNPAOnIncStmt) {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        }

                        System.out.print("Ratio Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column + "\n");
                        System.out.println("GNPA Line : " + Arrays.toString(gnpaColumns));

                        if (y0Column >= 0) {
                            try {
                                y0GNPA = gnpaColumns[y0Column].replace("%", "");
                            } catch (Exception e) {
                                y0GNPA = "0";
                                System.out.println("########## Exception in setting Y0 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY0GNPA(new BigDecimal(Double.parseDouble(y0GNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY0GNPA(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            try {
                                y1GNPA = gnpaColumns[y1Column].replace("%", "");
                            } catch (Exception e) {
                                y1GNPA = "0";
                                System.out.println("########## Exception in setting Y1 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY1GNPA(new BigDecimal(Double.parseDouble(y1GNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY1GNPA(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            try {
                                y2GNPA = gnpaColumns[y2Column].replace("%", "");
                            } catch (Exception e) {
                                y2GNPA = "0";
                                System.out.println("########## Exception in setting Y2 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY2GNPA(new BigDecimal(Double.parseDouble(y2GNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY2GNPA(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        System.out.println("########## Inc. Stmt Header column count (" + headerColumnsIncomeStmt.length + ") not matching with GNPA column count(" + gnpaColumns.length + "), used the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        // Find Y0, Y1 and Y2 Index position
                        if(!isGNPAOnIncStmt) {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        }
                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;
                        if(!isGNPAOnIncStmt) {
                            adjustedY0Column = gnpaColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY1Column = gnpaColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY2Column = gnpaColumns.length - (headerColumnsRatio.length - y0Column);
                        } else {
                            adjustedY0Column = gnpaColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY1Column = gnpaColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY2Column = gnpaColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                        }

                        System.out.print("Inc. Stmt. Header : Y0 Index " + adjustedY0Column);
                        System.out.print(" Y1 Index " + adjustedY1Column);
                        System.out.print(" Y2 Index " + adjustedY2Column + "\n");
                        System.out.println("GNPA Columns : " + Arrays.toString(gnpaColumns));
                        if (adjustedY0Column >= 0) {
                            try {
                                y0GNPA = gnpaColumns[adjustedY0Column].replace("%", "");
                            } catch (Exception e) {
                                y0GNPA = "0";
                                System.out.println("########## Exception in setting Y0 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY0GNPA(new BigDecimal(Double.parseDouble(y0GNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y0 Column Index not found Setting Y0 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY0GNPA(new BigDecimal("0"));
                        }
                        if (adjustedY1Column >= 0) {
                            try {
                                y1GNPA = gnpaColumns[adjustedY1Column].replace("%", "");
                            } catch (Exception e) {
                                y1GNPA = "0";
                                System.out.println("########## Exception in setting Y1 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY1GNPA(new BigDecimal(Double.parseDouble(y1GNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y1 Column Index not found Setting Y0 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY1GNPA(new BigDecimal("0"));
                        }
                        if (adjustedY2Column >= 0) {
                            try {
                                y2GNPA = gnpaColumns[adjustedY2Column].replace("%", "");
                            } catch (Exception e) {
                                y2GNPA = "0";
                                System.out.println("########## Exception in setting Y2 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY2GNPA(new BigDecimal(Double.parseDouble(y2GNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y2 Column Index not found Setting Y0 GNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY2GNPA(new BigDecimal("0"));
                        }
                    }
                } else {
                    reportParameters.setY0GNPA(new BigDecimal("0"));
                    reportParameters.setY1GNPA(new BigDecimal("0"));
                    reportParameters.setY2GNPA(new BigDecimal("0"));
                    System.out.println("########## GNPA Row Header and data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0GNPA(new BigDecimal("0"));
                reportParameters.setY1GNPA(new BigDecimal("0"));
                reportParameters.setY2GNPA(new BigDecimal("0"));
                System.out.println("########## GNPA Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0GNPA(new BigDecimal("0"));
            reportParameters.setY1GNPA(new BigDecimal("0"));
            reportParameters.setY2GNPA(new BigDecimal("0"));
            System.out.println("########## Exception in setting GNPA for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setNNPA(ReportDataExtractConfig rdec){
        int nnpaLineNo = -1;
        String nnpa = null;
        String[] nnpaColumns = null;
        String y0NNPA = "0", y1NNPA = "0", y2NNPA = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1;
        boolean isNNPAOnIncStmt = false;

        try {
            nnpaLineNo = getLineNumberForMatchingPattern(linesRatio, 0, NNPA_ROW_NAME, rdec, BROKER);
            if(nnpaLineNo < 0) {
                nnpaLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, NNPA_ROW_NAME, rdec, BROKER);
                isNNPAOnIncStmt = true;
            }

            if (nnpaLineNo > 1) {
                if(!isNNPAOnIncStmt)
                    nnpa = linesRatio[nnpaLineNo];
                else
                    nnpa = linesIncomeStmt[nnpaLineNo];

                nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);

                if(!isNNPAOnIncStmt) {
                    if (nnpaColumns != null && nnpaColumns.length != headerColumnsRatio.length && nnpaColumns.length == 0) {
                        if (!linesRatio[nnpaLineNo - 1].trim().equals(""))
                            nnpa = linesRatio[nnpaLineNo] + linesRatio[nnpaLineNo - 1];
                        else if (!linesRatio[nnpaLineNo + 1].trim().equals(""))
                            nnpa = linesRatio[nnpaLineNo] + linesRatio[nnpaLineNo + 1];

                        nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);
                    }
                } else {
                    if (nnpaColumns != null && nnpaColumns.length != headerColumnsIncomeStmt.length && nnpaColumns.length == 0) {
                        if (!linesIncomeStmt[nnpaLineNo - 1].trim().equals(""))
                            nnpa = linesIncomeStmt[nnpaLineNo] + linesIncomeStmt[nnpaLineNo - 1];
                        else if (!linesIncomeStmt[nnpaLineNo + 1].trim().equals(""))
                            nnpa = linesIncomeStmt[nnpaLineNo] + linesIncomeStmt[nnpaLineNo + 1];

                        nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);
                    }
                }

                if(nnpaColumns!= null && nnpaColumns.length != 0) {
                    if((!isNNPAOnIncStmt && nnpaColumns.length == headerColumnsRatio.length) || (isNNPAOnIncStmt && nnpaColumns.length == headerColumnsIncomeStmt.length)) {

                        // Find Y0, Y1 and Y2 Index position
                        if(!isNNPAOnIncStmt) {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        }

                        System.out.print("Ratio Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column + "\n");
                        System.out.println("NNPA Line : " + Arrays.toString(nnpaColumns));

                        if (y0Column >= 0) {
                            try {
                                y0NNPA = nnpaColumns[y0Column].replace("%", "");
                            } catch (Exception e) {
                                y0NNPA = "0";
                                System.out.println("########## Exception in setting Y0 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY0NNPA(new BigDecimal(Double.parseDouble(y0NNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY0NNPA(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            try {
                                y1NNPA = nnpaColumns[y1Column].replace("%", "");
                            } catch (Exception e) {
                                y1NNPA = "0";
                                System.out.println("########## Exception in setting Y1 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY1NNPA(new BigDecimal(Double.parseDouble(y1NNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY1NNPA(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            try {
                                y2NNPA = nnpaColumns[y2Column].replace("%", "");
                            } catch (Exception e) {
                                y2NNPA = "0";
                                System.out.println("########## Exception in setting Y2 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY2NNPA(new BigDecimal(Double.parseDouble(y2NNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY2NNPA(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        System.out.println("########## Inc. Stmt Header column count (" + headerColumnsIncomeStmt.length + ") not matching with NNPA column count(" + nnpaColumns.length + "), used the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        // Find Y0, Y1 and Y2 Index position
                        if(!isNNPAOnIncStmt) {
                            if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
                                y0Column = y0ColumnNumberOnRatio.intValue();
                                y1Column = y1ColumnNumberOnRatio.intValue();
                                y2Column = y2ColumnNumberOnRatio.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsRatio, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsRatio, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsRatio, Y2);
                            }
                        } else {
                            if (y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null) {
                                y0Column = y0ColumnNumberOnIncStmt.intValue();
                                y1Column = y1ColumnNumberOnIncStmt.intValue();
                                y2Column = y2ColumnNumberOnIncStmt.intValue();
                            } else {
                                y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            }
                        }
                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;
                        if(!isNNPAOnIncStmt) {
                            adjustedY0Column = nnpaColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY1Column = nnpaColumns.length - (headerColumnsRatio.length - y0Column);
                            adjustedY2Column = nnpaColumns.length - (headerColumnsRatio.length - y0Column);
                        } else {
                            adjustedY0Column = nnpaColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY1Column = nnpaColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                            adjustedY2Column = nnpaColumns.length - (headerColumnsIncomeStmt.length - y0Column);
                        }

                        System.out.print("Inc. Stmt. Header : Y0 Index " + adjustedY0Column);
                        System.out.print(" Y1 Index " + adjustedY1Column);
                        System.out.print(" Y2 Index " + adjustedY2Column + "\n");
                        System.out.println("NNPA Columns : " + Arrays.toString(nnpaColumns));
                        if (adjustedY0Column >= 0) {
                            try {
                                y0NNPA = nnpaColumns[adjustedY0Column].replace("%", "");
                            } catch (Exception e) {
                                y0NNPA = "0";
                                System.out.println("########## Exception in setting Y0 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY0NNPA(new BigDecimal(Double.parseDouble(y0NNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y0 Column Index not found Setting Y0 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY0NNPA(new BigDecimal("0"));
                        }
                        if (adjustedY1Column >= 0) {
                            try {
                                y1NNPA = nnpaColumns[adjustedY1Column].replace("%", "");
                            } catch (Exception e) {
                                y1NNPA = "0";
                                System.out.println("########## Exception in setting Y1 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY1NNPA(new BigDecimal(Double.parseDouble(y1NNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y1 Column Index not found Setting Y0 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY1NNPA(new BigDecimal("0"));
                        }
                        if (adjustedY2Column >= 0) {
                            try {
                                y2NNPA = nnpaColumns[adjustedY2Column].replace("%", "");
                            } catch (Exception e) {
                                y2NNPA = "0";
                                System.out.println("########## Exception in setting Y2 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                            reportParameters.setY2NNPA(new BigDecimal(Double.parseDouble(y2NNPA) / 100).setScale(4, RoundingMode.HALF_UP));
                        } else {
                            System.out.println("########## Adj. Y2 Column Index not found Setting Y0 NNPA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY2NNPA(new BigDecimal("0"));
                        }
                    }
                } else {
                    reportParameters.setY0NNPA(new BigDecimal("0"));
                    reportParameters.setY1NNPA(new BigDecimal("0"));
                    reportParameters.setY2NNPA(new BigDecimal("0"));
                    System.out.println("########## NNPA Row Header and data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0NNPA(new BigDecimal("0"));
                reportParameters.setY1NNPA(new BigDecimal("0"));
                reportParameters.setY2NNPA(new BigDecimal("0"));
                System.out.println("########## NNPA Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0NNPA(new BigDecimal("0"));
            reportParameters.setY1NNPA(new BigDecimal("0"));
            reportParameters.setY2NNPA(new BigDecimal("0"));
            System.out.println("########## Exception in setting NNPA for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }


    private String[] appendPreviousRow(String[] lines, int lineNumber, String pattern, boolean headreFlag){
        // if it is a header flag then check if previous row begins with "FY", if yes append
        String[] rowColumns = null;
        if(headreFlag == true){
            String onlyRowHeading = lines[lineNumber];
            if (lines[lineNumber-1].trim().toUpperCase().startsWith("FY") ||
                    lines[lineNumber-1].trim().toUpperCase().startsWith("CY") ||
                    lines[lineNumber-1].trim().toUpperCase().startsWith("20")) {
                String mergedRow = onlyRowHeading.trim() + " " + lines[lineNumber-1].trim();
                rowColumns = getDataColumnsForHeader(mergedRow, pattern);
            }
        }
        return rowColumns;
    }
}


