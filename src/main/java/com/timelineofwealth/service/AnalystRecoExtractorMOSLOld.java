package com.timelineofwealth.service;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.timelineofwealth.dto.ReportDataExtractConfig;
import com.timelineofwealth.dto.ReportParameters;


public class AnalystRecoExtractorMOSLOld extends AnalystRecoExtractor {

    public ReportParameters getReportParameters(String reportFilePath, ReportDataExtractConfig rdec) {

        String BROKER = "MOSL";
        String CMP = rdec.getCMP();
        String TP = rdec.getTP();
        String MCAP = rdec.getMCAP();
        String CMPPATTERN = rdec.getCMPPATTERN();
        String TPPATTERN = rdec.getTPPATTERN();
        String MCAPPATTERN = rdec.getMCAPPATTERN();
        String RATINGPATTERN = rdec.getRATINGPATTERN();

        String INCOME_STATEMENT_PAGE = rdec.getINCOME_STATEMENT_PAGE();
        String RATIO_PAGE = rdec.getRATIO_PAGE();
        String VALUATION_PAGE = rdec.getVALUATION_PAGE();

        String HEADER_ROW_NAME = rdec.getHEADER_ROW_NAME();
        String REVENUE_ROW_NAME = rdec.getREVENUE_ROW_NAME();
        String EBITDA_ROW_NAME = rdec.getEBITDA_ROW_NAME();
        String DEPRECIATION_ROW_NAME = rdec.getDEPRECIATION_ROW_NAME();

        String EBITDAMARGIN_ROW_NAME = rdec.getEBITDAMARGIN_ROW_NAME();
        String ROCE_ROW_NAME = rdec.getROCE_ROW_NAME();

        String AUM_ROW_NAME = rdec.getAUM_ROW_NAME();
        String CREDITCOSTS_ROW_NAME = rdec.getCREDITCOSTS_ROW_NAME();
        String GNPA_ROW_NAME = rdec.getGNPA_ROW_NAME();
        String NNPA_ROW_NAME = rdec.getNNPA_ROW_NAME();

        String EVBYEBITDA_ROW_NAME = rdec.getEVBYEBITDA_ROW_NAME();

        String Y0 = rdec.getY0();
        String Y1 = rdec.getY1();
        String Y2 = rdec.getY2();

        String MILLIONS_OR_BILLIONS = rdec.getMILLIONS_OR_BILLIONS();
        String MILLIONS_OR_BILLIONS_FLAG = "M";

        String RESEARCHANALYST1 = rdec.getRESEARCHANALYST1();
        String RESEARCHANALYST2 = rdec.getRESEARCHANALYST2();

        String QUARTER = rdec.getQUARTER();

        String DATEPATTERN = "\\d{1,2}\\s*+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s*+\\d{2,4}";
        String DATEFORMAT = "dd MMMMM yyyy";

        ReportParameters reportParameters = new ReportParameters();


        String outPutString;
        PdfReader pdfReader = null;

        try {
            // Open PDF document
            pdfReader = new PdfReader(reportFilePath);

            //Get the number of pages in pdf.
            int noOfPages = pdfReader.getNumberOfPages();

            //Load the first page
            String pageContentFirst = PdfTextExtractor.getTextFromPage(pdfReader, 1);
//            System.out.println("Content on Page 1 : \n" + pageContent);

            // Extract report date
            String[] linesFirstPage = pageContentFirst.split("\n");
            int lineNumber = 0;
            // Extracted Date
            String dateString = "";
            long dateLastModified = new File(reportFilePath).lastModified();
            while (lineNumber < linesFirstPage.length && dateString.isEmpty()) {
                dateString = getReportDate(linesFirstPage[lineNumber], dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
                lineNumber++;
            }
            // if date not found then set it to last modified date
            if (dateString.isEmpty()){
                dateString = getReportDate(null, dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
            }
            System.out.print("Date : " + dateString + " ");

            // get the line no. matching CMP and MCAP
            boolean isCMPLineFound = false, isTPLineFound = false, isMCapLineFound = false, isAnalyst1LineFound = false;
            int cmpLineNumber = -1, tpLineNumber = -1, mcapLineNumber = -1, analyst1LineNumber = -1;
            for (int i = 0; i < linesFirstPage.length; i++) {
                if (!isCMPLineFound && linesFirstPage[i].contains(CMP)){
                    cmpLineNumber = i;
                    isCMPLineFound = true;
                    if (linesFirstPage[i].contains(TP)) {
                        tpLineNumber = cmpLineNumber;
                        isTPLineFound = true;
                    }
                }
                if (!isTPLineFound && linesFirstPage[i].contains(TP)){
                    tpLineNumber = i;
                    isTPLineFound = true;
                }
                if(!isMCapLineFound && linesFirstPage[i].startsWith(MCAP)) {
                    mcapLineNumber = i;
                    isMCapLineFound = true;
                }
                if (!isAnalyst1LineFound && linesFirstPage[i].toLowerCase().contains(RESEARCHANALYST1.toLowerCase())){
                    analyst1LineNumber = i;
                    isAnalyst1LineFound = true;
                }
                if(isCMPLineFound && isMCapLineFound && isAnalyst1LineFound  && isTPLineFound)
                    break;
            }

            // Extract MCAP
            String mcap = "";
            double mcapNumber = 0;
            if (mcapLineNumber != -1) {
                mcapNumber = getMCapFromBillion(linesFirstPage[mcapLineNumber], rdec, 1, BROKER);
                System.out.print("MCap : " + mcapNumber + " ");
            } else {
                mcapNumber = 0;
                System.out.println("\n\n ********** Exception ********* \n\n Market Cap was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Extract CMP
            String cmp = "";
            if (cmpLineNumber != -1) {
                cmp = "" + getCMP(linesFirstPage[cmpLineNumber], rdec, 2, BROKER);
                System.out.print("CMP : " + cmp + " ");
            } else {
                cmp = "0";
                System.out.println("\n\n ********** Exception ********* \n\n CMP was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Extract TP
            String tp = "";
            if (tpLineNumber != -1) {
                tp = "" + getTP(linesFirstPage[tpLineNumber], rdec, 1, BROKER);
                System.out.print("TP : " + tp + " ");
            } else {
                tp = "0";
                System.out.println("\n\n ********** Exception ********* \n\n TP was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Extract rating
            String rating = "";
            if (cmpLineNumber!= -1) {
                String ratingLine = linesFirstPage[cmpLineNumber];
                // Extract the last word
                String[] ratingLineWords = ratingLine.split(" ");
                rating = capitalizeFirstChar(ratingLineWords[ratingLineWords.length - 1]);
                System.out.print("Ratings : " + rating + "\n");
            } else {
                rating = "";
                System.out.println("\n\n ********** Exception ********* \n\n Rating was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }


            // Extract Analyst Names
            String analystNames = "";
            analystNames = getAnalyst(analystNames, pageContentFirst, rdec, analyst1LineNumber);
            if(!analystNames.isEmpty())
                System.out.println("Analysts Names : " + analystNames);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Analyst Names were not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);


            // Find Income Statement Page No.
            int incomeStatementPageNumber = -1;
            incomeStatementPageNumber = getPageNumberForMatchingPattern(pdfReader, 2, noOfPages, INCOME_STATEMENT_PAGE, rdec, BROKER);
            if (incomeStatementPageNumber > 1)
                System.out.print("Inc. Statement Page No. : " + incomeStatementPageNumber + " / ");
            else
                System.out.println("\n\n ********** Exception ********* \n\n Income Statement Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Find Ratio Page No.
            int ratioPageNumber = -1;
            ratioPageNumber = getPageNumberForMatchingPattern(pdfReader,  incomeStatementPageNumber, noOfPages, RATIO_PAGE, rdec, BROKER);
            if (ratioPageNumber > 1)
                System.out.print("Ratio Page No. : " + ratioPageNumber + " / ");
            else
                System.out.println("\n\n ********** Exception ********* \n\n Ratio Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Find Valuation Page No.
            int valuationPageNumber = -1;
            valuationPageNumber = getPageNumberForMatchingPattern(pdfReader,  incomeStatementPageNumber, noOfPages, rdec.getVALUATION_PAGE(), rdec, BROKER);
            if (valuationPageNumber > 1)
                System.out.println("Valuation Page No. : " + valuationPageNumber);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Valuation Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Load the Inc. Statement Page
            String pageContentIncomeStmt = PdfTextExtractor.getTextFromPage(pdfReader, incomeStatementPageNumber);
//            System.out.println("\n\n*************************\n\nContent on Page :" + INCOME_STATEMENT_PAGE + " No. - " + incomeStatementPageNumber + "\n" + pageContent);

            // Get lines from from the Inc. Statement Page
            String[] linesIncomeStmt = pageContentIncomeStmt.split("\n");

            // Get Header Line No. from the Inc. Statement Page
            int headerLineNumber = -1;
            headerLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, 0, HEADER_ROW_NAME,rdec, BROKER);
            if (headerLineNumber > 1)
                System.out.println("Inc. Statement Header Line No. : " + headerLineNumber + " Value - " + linesIncomeStmt[headerLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Income Statement Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Revenue Line No. form the Inc. Statement Page
            int revenueLineNumber = -1;
            revenueLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, headerLineNumber, REVENUE_ROW_NAME,rdec, BROKER);
            if (revenueLineNumber > 1)
                System.out.println("Revenue Line No. : " + revenueLineNumber + " Value - " + linesIncomeStmt[revenueLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Revenue Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get EBITDA Line No. form the Inc. Statement Page
            int ebitdaLineNumber = -1;
            ebitdaLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, revenueLineNumber, EBITDA_ROW_NAME,rdec, BROKER);
            if (ebitdaLineNumber > 1)
                System.out.println("EBITDA Line No. : " + ebitdaLineNumber + " Value - " + linesIncomeStmt[ebitdaLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n EBITDA Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Depreciation Line No. form the Inc. Statement Page
            int depreciationLineNumber = -1;
            boolean isFinancialReport = false;
            String gnpa = "", y0GNPA = "", y1GNPA = "", y2GNPA = "";
            double y0GNPANumber = 0, y1GNPANumber = 0, y2GNPANumber = 0;
            String nnpa = "", y0NNPA = "", y1NNPA = "", y2NNPA = "";
            double y0NNPANumber = 0, y1NNPANumber = 0, y2NNPANumber = 0;
            String creditCosts = "", y0CreditCosts = "", y1CreditCosts = "", y2CreditCosts = "";
            double y0CreditCostNumber = 0, y1CreditCostNumber = 0, y2CreditCostNumber = 0;
            String aum = "", y0AUM = "", y1AUM = "", y2AUM = "";
            double y0AUMNumber = 0, y1AUMNumber = 0, y2AUMNumber = 0;
            if (EBITDA_ROW_NAME.contains("PAT")|| EBITDA_ROW_NAME.toLowerCase().contains("profit after tax") ) {
                isFinancialReport = true;
            } else {
                depreciationLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, ebitdaLineNumber, DEPRECIATION_ROW_NAME, rdec, BROKER);
                if (depreciationLineNumber > 1)
                    System.out.println("Depreciation Line No. : " + depreciationLineNumber + " Value - " + linesIncomeStmt[depreciationLineNumber]);
                else
                    System.out.println("\n\n ********** Exception ********* \n\n Depreciation Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Get OPM Lines Line No. form the Inc. Statement Page
            int ebitdaMarginLineNumber = -1;
            ebitdaMarginLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, ebitdaLineNumber, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
            if (ebitdaMarginLineNumber > 1)
                System.out.println("OPM Line No. : " + ebitdaMarginLineNumber + " Value - " + linesIncomeStmt[ebitdaMarginLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n OPM Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Corresponding Line
            String header = linesIncomeStmt[headerLineNumber];
            String revenue = linesIncomeStmt[revenueLineNumber];
            String ebitda = linesIncomeStmt[ebitdaLineNumber];
            String depreciation = "";
            if(!isFinancialReport)
                depreciation = linesIncomeStmt[depreciationLineNumber];
            // if OPM Margin not found on Inc. Statement Load from the Ratio page
            String ebitdaMargin = null;
            if (ebitdaMarginLineNumber == -1) {
                System.out.println("Checking if Margin presents on the ratio page");
                String ratioPage = PdfTextExtractor.getTextFromPage(pdfReader, ratioPageNumber);
                String[] ratioPageLines = ratioPage.split("\n");
                ebitdaMarginLineNumber = getLineNumberForMatchingPattern(ratioPageLines, 0, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
                if(ebitdaMarginLineNumber>0)
                    ebitdaMargin = ratioPageLines[ebitdaMarginLineNumber];
            } else {
                ebitdaMargin = linesIncomeStmt[ebitdaMarginLineNumber];
            }
            //patch to remove '%' from ebitdaMargin
            if(ebitdaMargin!= null)
                ebitdaMargin = ebitdaMargin.replaceAll("%", "");

            // Convert rest of the line into Array
            String[] headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
            String[] revenueColumns = getDataColumnsForHeader(revenue, REVENUE_ROW_NAME);
            String[] ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME);
            if(ebitdaColumns.length != headerColumns.length && ebitdaColumns.length == 0){
                if(!linesIncomeStmt[ebitdaLineNumber - 1].trim().equals(""))
                    ebitda = linesIncomeStmt[ebitdaLineNumber] + linesIncomeStmt[ebitdaLineNumber - 1];
                else if(!linesIncomeStmt[ebitdaLineNumber + 1].trim().equals(""))
                    ebitda = linesIncomeStmt[ebitdaLineNumber] + linesIncomeStmt[ebitdaLineNumber + 1];

                ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME);
            }
            String[] depreciationColumns = null;
            if (!isFinancialReport) {
                depreciationColumns = getDataColumnsForHeader(depreciation, DEPRECIATION_ROW_NAME);
                if(depreciationColumns.length != headerColumns.length && depreciationColumns.length == 0){
                    if(!linesIncomeStmt[depreciationLineNumber - 1].trim().equals(""))
                        depreciation = linesIncomeStmt[depreciationLineNumber] + linesIncomeStmt[depreciationLineNumber - 1];
                    else if(!linesIncomeStmt[depreciationLineNumber + 1].trim().equals(""))
                        depreciation = linesIncomeStmt[depreciationLineNumber] + linesIncomeStmt[depreciationLineNumber + 1];

                    depreciationColumns = getDataColumnsForHeader(depreciation, DEPRECIATION_ROW_NAME);
                }
            }
            // code if depreciation numbers on previous line
//            if (depreciationColumns.length == 0) {
//                //in case depreciation numbers are on new line
//                System.out.println(" depreciation+lines[depreciationLineNumber-1] " + depreciation+lines[depreciationLineNumber-1].trim());
//                depreciationColumns = getDataColumnsForHeader(depreciation+lines[depreciationLineNumber-1].trim(), DEPRECIATION_ROW_NAME);
//            }
            String[] ebitdaMarginColumns = null;
            if(ebitdaMargin != null)
                ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);

            // Set million or billion flag
            MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find()? "B" : "M";

            // Find Y0, Y1 and Y2 Index position
            int y0Column = getIndexOfTheYear(headerColumns, Y0);
            int y1Column = getIndexOfTheYear(headerColumns, Y1);
            int y2Column = getIndexOfTheYear(headerColumns, Y2);

            System.out.print(" Y0 Index " + y0Column);
            System.out.print(" Y1 Index " + y1Column);
            System.out.print(" Y2 Index " + y2Column + "\n");
            System.out.println("Header Columns  " + headerColumns.length);
            System.out.println("Revenue Columns " + revenueColumns.length);
            System.out.println("EBITDA Columns  " + ebitdaColumns.length);
            if(!isFinancialReport)
                System.out.println("Dep. Columns    " + depreciationColumns.length);
            if (ebitdaMarginColumns!=null)
                System.out.println("OPM Columns     " + ebitdaMarginColumns.length);
            else
                System.out.println("OPM Line Missing  " );

            String y0Revenue = "", y1Revenue = "", y2Revenue = "";
            String y0EBITDA = "", y1EBITDA = "", y2EBITDA = "";
            String y0Depreciation = "", y1Depreciation = "", y2Depreciation = "";
            String y0EBITDAMargin = "", y1EBITDAMargin = "", y2EBITDAMargin = "";
            double y0RevenueNumber = 0, y1RevenueNumber = 0, y2RevenueNumber = 0;
            double y0EBITDANumber = 0, y1EBITDANumber = 0, y2EBITDANumber = 0;
            double y0DepreciationNumber = 0, y1DepreciationNumber = 0, y2DepreciationNumber = 0;

            if (y0Column == -1) {
                System.out.println(Y0 + " column not found in the header on " + INCOME_STATEMENT_PAGE + " page");
            } else {
                y0Revenue = revenueColumns[y0Column];
                y0EBITDA = ebitdaColumns[y0Column];
                if(!isFinancialReport) {
                    y0Depreciation = depreciationColumns[y0Column];
                    if (ebitdaMarginColumns != null)
                        y0EBITDAMargin = ebitdaMarginColumns[y0Column];
                    else
                        y0EBITDAMargin = "0";
                }

                y0RevenueNumber = Double.parseDouble(y0Revenue.replace(",", ""));
                y0EBITDANumber = Double.parseDouble(y0EBITDA.replace(",", ""));
                if(!isFinancialReport)
                    y0DepreciationNumber = Double.parseDouble(y0Depreciation.replace(",", ""));

                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                    y0RevenueNumber = y0RevenueNumber / 10;
                    y0EBITDANumber = y0EBITDANumber / 10;
                    if(!isFinancialReport)
                        y0DepreciationNumber = y0DepreciationNumber / 10;
                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                    y0RevenueNumber = y0RevenueNumber * 100;
                    y0EBITDANumber = y0EBITDANumber * 100;
                    if(!isFinancialReport)
                        y0DepreciationNumber = y0DepreciationNumber * 100;
                }
            }
            if (y1Column == -1) {
                System.out.println(Y1 + " column not found in the header on " + INCOME_STATEMENT_PAGE + " page");
            } else {
                y1Revenue = revenueColumns[y1Column];
                y1EBITDA = ebitdaColumns[y1Column];
                if(!isFinancialReport) {
                    y1Depreciation = depreciationColumns[y1Column];
                    if (ebitdaMarginColumns != null)
                        y1EBITDAMargin = ebitdaMarginColumns[y1Column];
                    else
                        y1EBITDAMargin = "0";
                }

                y1RevenueNumber = Double.parseDouble(y1Revenue.replace(",", ""));
                y1EBITDANumber = Double.parseDouble(y1EBITDA.replace(",", ""));
                if(!isFinancialReport)
                    y1DepreciationNumber = Double.parseDouble(y1Depreciation.replace(",", ""));

                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                    y1RevenueNumber = y1RevenueNumber / 10;
                    y1EBITDANumber = y1EBITDANumber / 10;
                    if(!isFinancialReport)
                        y1DepreciationNumber = y1DepreciationNumber / 10;
                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                    y1RevenueNumber = y1RevenueNumber * 100;
                    y1EBITDANumber = y1EBITDANumber * 100;
                    if(!isFinancialReport)
                        y1DepreciationNumber = y1DepreciationNumber * 100;
                }
            }
            if (y2Column == -1) {
                System.out.println(Y2 + " column not found in the header on " + INCOME_STATEMENT_PAGE + " page");
            } else {
                y2Revenue = revenueColumns[y2Column];
                y2EBITDA = ebitdaColumns[y2Column];
                if(!isFinancialReport) {
                    y2Depreciation = depreciationColumns[y2Column];
                    if (ebitdaMarginColumns != null)
                        y2EBITDAMargin = ebitdaMarginColumns[y2Column];
                    else
                        y2EBITDAMargin = "0";
                }

                y2RevenueNumber = Double.parseDouble(y2Revenue.replace(",", ""));
                y2EBITDANumber = Double.parseDouble(y2EBITDA.replace(",", ""));
                if(!isFinancialReport)
                    y2DepreciationNumber = Double.parseDouble(y2Depreciation.replace(",", ""));

                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                    y2RevenueNumber = y2RevenueNumber / 10;
                    y2EBITDANumber = y2EBITDANumber / 10;
                    if(!isFinancialReport)
                        y2DepreciationNumber = y2DepreciationNumber / 10;
                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                    y2RevenueNumber = y2RevenueNumber * 100;
                    y2EBITDANumber = y2EBITDANumber * 100;
                    if(!isFinancialReport)
                        y2DepreciationNumber = y2DepreciationNumber * 100;
                }
            }

            //Load the Ratio Page
            String pageContentRatio = PdfTextExtractor.getTextFromPage(pdfReader, ratioPageNumber);
//            System.out.println("\n\n*************************\n\n  Content on Ratio Page " + RATIO_PAGE + " No :" + ratioPageNumber + "\n" + pageContent);

            // Get lines on the Ratio Page
            String[] linesRatio = pageContentRatio.split("\n");

            // Get Header Line No. from the Inc. Ratio Page
            headerLineNumber = -1;
            headerLineNumber = getLineNumberForMatchingPattern(linesRatio, 0, HEADER_ROW_NAME,rdec, BROKER);
            if (headerLineNumber > 1)
                System.out.println("Ratio Header Line No. : " + headerLineNumber + " Value - " + linesRatio[headerLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Ratio Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get ROCE Line No. from the Inc. Ratio Page
            int roceLineNumber = -1;
            roceLineNumber = getLineNumberForMatchingPattern(linesRatio, headerLineNumber, ROCE_ROW_NAME,rdec, BROKER);
            if (roceLineNumber > 1)
                System.out.println("ROCE Line No. : " + roceLineNumber + " Value - " + linesRatio[roceLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n ROCE Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get EVBYEBITDA Line No. from the Inc. Ratio Page
            int evbyebitdaLineNumber = -1;
            evbyebitdaLineNumber = getLineNumberForMatchingPattern(linesRatio, headerLineNumber, EVBYEBITDA_ROW_NAME,rdec, BROKER);
            if (evbyebitdaLineNumber > 1)
                System.out.println("EVBYEBITDA Line No. : " + evbyebitdaLineNumber + "  Value - " + linesRatio[evbyebitdaLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n EVBYEBITDA Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            if(isFinancialReport){
                ebitdaMarginLineNumber = getLineNumberForMatchingPattern(linesRatio, 0, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
                if(ebitdaMarginLineNumber>0) {
                    ebitdaMargin = linesRatio[ebitdaMarginLineNumber];
                    if (ebitdaMargin != null)
                        ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                    if (ebitdaMarginColumns.length != headerColumns.length && ebitdaMarginColumns.length == 0) {
                        if (!linesRatio[ebitdaMarginLineNumber + 1].trim().equals(""))
                            ebitdaMargin = linesRatio[ebitdaMarginLineNumber] + linesRatio[ebitdaMarginLineNumber + 1];
                        else if (!linesRatio[ebitdaMarginLineNumber - 1].trim().equals(""))
                            ebitdaMargin = linesRatio[ebitdaMarginLineNumber] + linesRatio[ebitdaMarginLineNumber - 1];

                        ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                    }
                } else {
                    ebitdaMarginLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, 0, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
                    ebitdaMargin = linesIncomeStmt[ebitdaMarginLineNumber];
                    if (ebitdaMargin != null)
                        ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                    if (ebitdaMarginColumns.length != headerColumns.length && ebitdaMarginColumns.length == 0) {
                        if (!linesIncomeStmt[ebitdaMarginLineNumber + 1].trim().equals(""))
                            ebitdaMargin = linesIncomeStmt[ebitdaMarginLineNumber] + linesIncomeStmt[ebitdaMarginLineNumber + 1];
                        else if (!linesIncomeStmt[ebitdaMarginLineNumber - 1].trim().equals(""))
                            ebitdaMargin = linesIncomeStmt[ebitdaMarginLineNumber] + linesIncomeStmt[ebitdaMarginLineNumber - 1];

                        ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                    }
                }
                if(ebitdaMarginColumns.length == headerColumns.length) {
                    if (ebitdaMarginColumns != null)
                        y0EBITDAMargin = ebitdaMarginColumns[y0Column];
                    else
                        y0EBITDAMargin = "0";

                    if (ebitdaMarginColumns != null)
                        y1EBITDAMargin = ebitdaMarginColumns[y1Column];
                    else
                        y1EBITDAMargin = "0";

                    if (ebitdaMarginColumns != null)
                        y2EBITDAMargin = ebitdaMarginColumns[y2Column];
                    else
                        y2EBITDAMargin = "0";
                }

                int gnpaLineNumber = getLineNumberForMatchingPattern(linesRatio, 0, GNPA_ROW_NAME,rdec, BROKER);
                String[] gnpaColumns = null;
                if(gnpaLineNumber>0) {
                    gnpa = linesRatio[gnpaLineNumber];
                    if (gnpa != null)
                        gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);
                    if (gnpaColumns.length != headerColumns.length && gnpaColumns.length == 0) {
                        if (!linesRatio[gnpaLineNumber + 1].trim().equals(""))
                            gnpa = linesRatio[gnpaLineNumber] + linesRatio[gnpaLineNumber + 1];
                        else if (!linesRatio[gnpaLineNumber - 1].trim().equals(""))
                            gnpa = linesRatio[gnpaLineNumber] + linesRatio[gnpaLineNumber - 1];

                        gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);
                    }
                } else {
                    gnpaLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, 0, GNPA_ROW_NAME,rdec, BROKER);
                    gnpa = linesIncomeStmt[gnpaLineNumber];
                    if (gnpa != null)
                        gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);
                    if (gnpaColumns.length != headerColumns.length && gnpaColumns.length == 0) {
                        if (!linesIncomeStmt[gnpaLineNumber + 1].trim().equals(""))
                            gnpa = linesIncomeStmt[gnpaLineNumber] + linesIncomeStmt[gnpaLineNumber + 1];
                        else if (!linesIncomeStmt[gnpaLineNumber - 1].trim().equals(""))
                            gnpa = linesIncomeStmt[gnpaLineNumber] + linesIncomeStmt[gnpaLineNumber - 1];

                        gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);
                    }
                }
                if(gnpaColumns.length == headerColumns.length) {
                    if (gnpaColumns != null) {
                        y0GNPA = gnpaColumns[y0Column];
                        try {
                            y0GNPANumber = Double.parseDouble(y0GNPA);
                        }catch (Exception e){
                            y0GNPANumber = 0;
                        }
                    }
                    else
                        y0GNPANumber = 0;

                    if (gnpaColumns != null) {
                        y1GNPA = gnpaColumns[y1Column];
                        try {
                            y1GNPANumber = Double.parseDouble(y1GNPA);
                        }catch (Exception e){
                            y1GNPANumber = 0;
                        }
                    }
                    else
                        y1GNPANumber = 0;

                    if (gnpaColumns != null) {
                        y2GNPA = gnpaColumns[y2Column];
                        try {
                            y2GNPANumber = Double.parseDouble(y2GNPA);
                        }catch (Exception e){
                            y2GNPANumber = 0;
                        }
                    }
                    else
                        y2GNPANumber = 0;
                }

                int nnpaLineNumber = getLineNumberForMatchingPattern(linesRatio, 0, NNPA_ROW_NAME,rdec, BROKER);
                String[] nnpaColumns = null;
                if(nnpaLineNumber>0) {
                    nnpa = linesRatio[nnpaLineNumber];
                    if (nnpa != null)
                        nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);
                    if (nnpaColumns.length != headerColumns.length && nnpaColumns.length == 0) {
                        if (!linesRatio[nnpaLineNumber + 1].trim().equals(""))
                            nnpa = linesRatio[nnpaLineNumber] + linesRatio[nnpaLineNumber + 1];
                        else if (!linesRatio[nnpaLineNumber - 1].trim().equals(""))
                            nnpa = linesRatio[nnpaLineNumber] + linesRatio[nnpaLineNumber - 1];

                        nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);
                    }
                } else {
                    nnpaLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, 0, NNPA_ROW_NAME,rdec, BROKER);
                    nnpa = linesIncomeStmt[nnpaLineNumber];
                    if (nnpa != null)
                        nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);
                    if (nnpaColumns.length != headerColumns.length && nnpaColumns.length == 0) {
                        if (!linesIncomeStmt[nnpaLineNumber + 1].trim().equals(""))
                            nnpa = linesIncomeStmt[nnpaLineNumber] + linesIncomeStmt[nnpaLineNumber + 1];
                        else if (!linesIncomeStmt[nnpaLineNumber - 1].trim().equals(""))
                            nnpa = linesIncomeStmt[nnpaLineNumber] + linesIncomeStmt[nnpaLineNumber - 1];

                        nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);
                    }
                }
                if(nnpaColumns.length == headerColumns.length) {
                    if (nnpaColumns != null) {
                        y0NNPA = nnpaColumns[y0Column];
                        try {
                            y0NNPANumber = Double.parseDouble(y0NNPA);
                        }catch (Exception e){
                            y0NNPANumber = 0;
                        }
                    }
                    else
                        y0NNPANumber = 0;

                    if (nnpaColumns != null) {
                        y1NNPA = nnpaColumns[y1Column];
                        try {
                            y1NNPANumber = Double.parseDouble(y1NNPA);
                        }catch (Exception e){
                            y1NNPANumber = 0;
                        }
                    }
                    else
                        y1NNPANumber = 0;

                    if (nnpaColumns != null) {
                        y2NNPA = nnpaColumns[y2Column];
                        try {
                            y2NNPANumber = Double.parseDouble(y2NNPA);
                        }catch (Exception e){
                            y2NNPANumber = 0;
                        }
                    }
                    else
                        y2NNPANumber = 0;
                }

                int aumLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, 0, AUM_ROW_NAME,rdec, BROKER);

                String[] aumColumns = null;
                String MILLIONS_OR_BILLIONS_FLAG_OLD = MILLIONS_OR_BILLIONS_FLAG;

                if(aumLineNumber>0) {
                    aum = linesIncomeStmt[aumLineNumber];
                    if (aum != null)
                        aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);

                    if (AUM_ROW_NAME.contains("INR b"))
                        MILLIONS_OR_BILLIONS_FLAG = "B";
                    if (AUM_ROW_NAME.contains("INR m"))
                        MILLIONS_OR_BILLIONS_FLAG = "M";
                    if (aumColumns.length != headerColumns.length && aumColumns.length == 0) {
                        if (!linesIncomeStmt[aumLineNumber + 1].trim().equals(""))
                            aum = linesIncomeStmt[aumLineNumber] + linesIncomeStmt[aumLineNumber + 1];
                        else if (!linesIncomeStmt[aumLineNumber - 1].trim().equals(""))
                            aum = linesIncomeStmt[aumLineNumber] + linesIncomeStmt[aumLineNumber - 1];

                        aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);
                    }
                } else {
                    aumLineNumber = getLineNumberForMatchingPattern(linesRatio, 0, AUM_ROW_NAME,rdec, BROKER);
                    aum = linesRatio[aumLineNumber];
                    if (aum != null)
                        aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);
                    if (AUM_ROW_NAME.contains("INR b"))
                        MILLIONS_OR_BILLIONS_FLAG = "B";
                    if (AUM_ROW_NAME.contains("INR m"))
                        MILLIONS_OR_BILLIONS_FLAG = "M";
                    if (aumColumns.length != headerColumns.length && aumColumns.length == 0) {
                        if (!linesRatio[aumLineNumber + 1].trim().equals(""))
                            aum = linesRatio[aumLineNumber] + linesRatio[aumLineNumber + 1];
                        else if (!linesRatio[aumLineNumber - 1].trim().equals(""))
                            aum = linesRatio[aumLineNumber] + linesRatio[aumLineNumber - 1];

                        aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);
                    }
                }
                if(aumColumns.length == headerColumns.length) {
                    if (aumColumns != null) {
                        y0AUM = aumColumns[y0Column];
                        try {
                            y0AUMNumber = Double.parseDouble(y0AUM.replaceAll(",", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y0AUMNumber = y0AUMNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y0AUMNumber = y0AUMNumber * 100;
                            }
                        }catch (Exception e){
                            y0AUMNumber = 0;
                        }
                    }
                    else
                        y0AUMNumber = 0;

                    if (aumColumns != null) {
                        y1AUM = aumColumns[y1Column];
                        try {
                            y1AUMNumber = Double.parseDouble(y1AUM.replaceAll(",", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y1AUMNumber = y1AUMNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y1AUMNumber = y1AUMNumber * 100;
                            }
                        }catch (Exception e){
                            y1AUMNumber = 0;
                        }
                    }
                    else
                        y1AUMNumber = 0;

                    if (aumColumns != null) {
                        y2AUM = aumColumns[y2Column];
                        try {
                            y2AUMNumber = Double.parseDouble(y2AUM.replaceAll(",", ""));
                            if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y2AUMNumber = y2AUMNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y2AUMNumber = y2AUMNumber * 100;
                            }
                        }catch (Exception e){
                            y2AUMNumber = 0;
                        }
                    }
                    else
                        y2AUMNumber = 0;
                }
                MILLIONS_OR_BILLIONS_FLAG = MILLIONS_OR_BILLIONS_FLAG_OLD;

                int creditCostsLineNumber = getLineNumberForMatchingPattern(linesRatio, 0, CREDITCOSTS_ROW_NAME,rdec, BROKER);
                String[] creditCostsColumns = null;
                if(creditCostsLineNumber>0) {
                    creditCosts = linesRatio[creditCostsLineNumber];
                    if (creditCosts != null)
                        creditCostsColumns = getDataColumnsForHeader(creditCosts, CREDITCOSTS_ROW_NAME);
                    if (creditCostsColumns.length != headerColumns.length && creditCostsColumns.length == 0) {
                        if (!linesRatio[creditCostsLineNumber + 1].trim().equals(""))
                            creditCosts = linesRatio[creditCostsLineNumber] + linesRatio[creditCostsLineNumber + 1];
                        else if (!linesRatio[creditCostsLineNumber - 1].trim().equals(""))
                            creditCosts = linesRatio[creditCostsLineNumber] + linesRatio[creditCostsLineNumber - 1];

                        creditCostsColumns = getDataColumnsForHeader(creditCosts, CREDITCOSTS_ROW_NAME);
                    }
                } else {
                    creditCostsLineNumber = getLineNumberForMatchingPattern(linesIncomeStmt, 0, CREDITCOSTS_ROW_NAME,rdec, BROKER);
                    creditCosts = linesIncomeStmt[creditCostsLineNumber];
                    if (creditCosts != null)
                        creditCostsColumns = getDataColumnsForHeader(creditCosts, CREDITCOSTS_ROW_NAME);
                    if (creditCostsColumns.length != headerColumns.length && creditCostsColumns.length == 0) {
                        if (!linesIncomeStmt[creditCostsLineNumber + 1].trim().equals(""))
                            creditCosts = linesIncomeStmt[creditCostsLineNumber] + linesIncomeStmt[creditCostsLineNumber + 1];
                        else if (!linesIncomeStmt[creditCostsLineNumber - 1].trim().equals(""))
                            creditCosts = linesIncomeStmt[creditCostsLineNumber] + linesIncomeStmt[creditCostsLineNumber - 1];

                        creditCostsColumns = getDataColumnsForHeader(creditCosts, CREDITCOSTS_ROW_NAME);
                    }
                }
                if(creditCostsColumns.length == headerColumns.length) {
                    if (creditCostsColumns != null) {
                        y0CreditCosts = creditCostsColumns[y0Column];
                        try {
                            y0CreditCostNumber = Double.parseDouble(y0CreditCosts);
                        }catch (Exception e){
                            y0CreditCostNumber = 0;
                        }
                    }
                    else
                        y0CreditCostNumber = 0;

                    if (creditCostsColumns != null) {
                        y1CreditCosts = creditCostsColumns[y1Column];
                        try {
                            y1CreditCostNumber = Double.parseDouble(y1CreditCosts);
                        }catch (Exception e){
                            y1CreditCostNumber = 0;
                        }
                    }
                    else
                        y1CreditCostNumber = 0;

                    if (creditCostsColumns != null) {
                        y2CreditCosts = creditCostsColumns[y2Column];
                        try {
                            y2CreditCostNumber = Double.parseDouble(y2CreditCosts);
                        }catch (Exception e){
                            y2CreditCostNumber = 0;
                        }
                    }
                    else
                        y2CreditCostNumber = 0;
                }

            }


            header = linesRatio[headerLineNumber];
            String roce = linesRatio[roceLineNumber];

            String evbyebitda = linesRatio[evbyebitdaLineNumber];

            headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
            String[] roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME);
            String[] evbyebitdaColumns = getDataColumnsForHeader(evbyebitda, EVBYEBITDA_ROW_NAME);

            // Handle exceptional case if Coumnn array is null
            if (headerColumns == null || headerColumns.length == 0) {
                headerColumns = appendPreviousRow(linesRatio, headerLineNumber, HEADER_ROW_NAME, true);
                //evenif header columns are null thne add next line
                if(headerColumns == null || headerColumns.length == 0) {
                    header = linesRatio[headerLineNumber] + linesRatio[headerLineNumber+1];
                    headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
                }
                // case when header "Y/E March" is on the next line and corresponding data on the previous line
                if(headerColumns == null || headerColumns.length == 0) {
                    header = linesRatio[headerLineNumber] + linesRatio[headerLineNumber-1];
                    headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
                    if (roceColumns == null || roceColumns.length == 0){
                        roceColumns = getDataColumnsForHeader(roce + linesRatio[roceLineNumber-1], ROCE_ROW_NAME);
                    }
                    if (evbyebitdaColumns == null || evbyebitdaColumns.length == 0){
                        evbyebitdaColumns = getDataColumnsForHeader(evbyebitda + linesRatio[evbyebitdaLineNumber-1], EVBYEBITDA_ROW_NAME);
                    }
                }
            }

            if(roceColumns.length != headerColumns.length && roceColumns.length == 0){
                if(!linesRatio[roceLineNumber - 1].trim().equals(""))
                    roce = linesRatio[roceLineNumber] + linesRatio[roceLineNumber - 1];
                else if(!linesRatio[roceLineNumber + 1].trim().equals(""))
                    roce = linesRatio[roceLineNumber] + linesRatio[roceLineNumber + 1];

                roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME);
            }


            // Find Y0, Y1 and Y2 Index position
            y0Column = getIndexOfTheYear(headerColumns, Y0);
            y1Column = getIndexOfTheYear(headerColumns, Y1);
            y2Column = getIndexOfTheYear(headerColumns, Y2);

            String y0ROCE = "", y1ROCE = "", y2ROCE = "";
            String y0EVBYEBITDA = "", y1EVBYEBITDA = "", y2EVBYEBITDA = "";
            double y0EVBYEBITDANumber = 0, y1EVBYEBITDANumber = 0, y2EVBYEBITDANumber = 0;
            double y0EVBYEBITNumber = 0, y1EVBYEBITNumber = 0, y2EVBYEBITNumber = 0;

            if (y0Column == -1) {
                System.out.println(Y0 + " column not found in the header on " + RATIO_PAGE + " page");
            } else {
                if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length && roceColumns.length != 0) {
                    System.out.println("Header mismatch for " +  QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    System.out.print(" Header Length " +  headerColumns.length);
                    System.out.print(" roce Length " +  roceColumns.length);
                    System.out.print(" evbyebitda Length " +  evbyebitdaColumns.length);

                    y0ROCE = roceColumns[y0Column-(headerColumns.length-roceColumns.length)].replace("(", "-").replace(")", "").replace("NA", "0");
                } else if (roceColumns.length != 0) {
                    y0ROCE = roceColumns[y0Column].replace("(", "-").replace(")", "").replace("NA", "0");
                } else {
                    y0ROCE = "0";
                }
                try {
                    if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                        y0EVBYEBITDA = evbyebitdaColumns[y0Column - (headerColumns.length - evbyebitdaColumns.length)].replace("NA", "0").replace("n/m", "0");
                    } else {
                        y0EVBYEBITDA = evbyebitdaColumns[y0Column].replace("NA", "0").replace("n/m", "0");
                    }
                } catch (Exception e) {
                    y0EVBYEBITDA = "0";
                }

                y0EVBYEBITDANumber = Double.parseDouble(y0EVBYEBITDA);

            }
            if (y1Column == -1) {
                System.out.println(Y1 + " column not found in the header on " + RATIO_PAGE + " page");
            } else {
                try {
                    if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length && roceColumns.length != 0) {
                        y1ROCE = roceColumns[y1Column - (headerColumns.length - roceColumns.length)].replace("(", "-").replace(")", "").replace("NA", "0");
                    } else if (roceColumns.length != 0) {
                        y1ROCE = roceColumns[y1Column].replace("(", "-").replace(")", "").replace("NA", "0");
                    } else {
                        y1ROCE = "0";
                    }
                    if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                        y1EVBYEBITDA = evbyebitdaColumns[y1Column - (headerColumns.length - evbyebitdaColumns.length)].replace("NA", "0").replace("n/m", "0");
                    } else {
                        y1EVBYEBITDA = evbyebitdaColumns[y1Column].replace("NA", "0").replace("n/m", "0");
                    }
                } catch (Exception e) {
                    y1EVBYEBITDA = "0";
                }

                y1EVBYEBITDANumber = Double.parseDouble(y1EVBYEBITDA);
            }
            if (y2Column == -1) {
                System.out.println(Y2 + " column not found in the header on " + RATIO_PAGE + " page");
            } else {
                try {
                    if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length && roceColumns.length != 0) {
                        y2ROCE = roceColumns[y2Column - (headerColumns.length - roceColumns.length)].replace("(", "-").replace(")", "").replace("NA", "0");
                    } else if (roceColumns.length != 0) {
                        y2ROCE = roceColumns[y2Column].replace("(", "-").replace(")", "").replace("NA", "0");
                    } else {
                        y2ROCE = "0";
                    }
                    if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                        y2EVBYEBITDA = evbyebitdaColumns[y2Column - (headerColumns.length - evbyebitdaColumns.length)].replace("NA", "0").replace("n/m", "0");
                    } else {
                        y2EVBYEBITDA = evbyebitdaColumns[y2Column].replace("NA", "0").replace("n/m", "0");
                    }
                } catch (Exception e) {
                    y2EVBYEBITDA = "0";
                }
                y2EVBYEBITDANumber = Double.parseDouble(y2EVBYEBITDA);
            }

            // Converting EVBYEBITDA to EVBYEBIT
            if(!isFinancialReport) {
                if (y0EBITDANumber > 0 && Math.abs(y0DepreciationNumber) > 0 && y0EVBYEBITDANumber > 0) {
                    y0EVBYEBITNumber = (y0EVBYEBITDANumber * y0EBITDANumber) / (y0EBITDANumber - Math.abs(y0DepreciationNumber));
                    BigDecimal bd = new BigDecimal(y0EVBYEBITNumber);
                    bd = bd.setScale(4, RoundingMode.HALF_UP);
                    y0EVBYEBITNumber = bd.doubleValue();
                }
                if (y1EBITDANumber > 0 && Math.abs(y1DepreciationNumber) > 0 && y1EVBYEBITDANumber > 0) {
                    y1EVBYEBITNumber = (y1EVBYEBITDANumber * y1EBITDANumber) / (y1EBITDANumber - Math.abs(y1DepreciationNumber));
                    BigDecimal bd = new BigDecimal(y1EVBYEBITNumber);
                    bd = bd.setScale(4, RoundingMode.HALF_UP);
                    y1EVBYEBITNumber = bd.doubleValue();
                }
                if (y2EBITDANumber > 0 && Math.abs(y2DepreciationNumber) > 0 && y2EVBYEBITDANumber > 0) {
                    y2EVBYEBITNumber = (y2EVBYEBITDANumber * y2EBITDANumber) / (y2EBITDANumber - Math.abs(y2DepreciationNumber));
                    BigDecimal bd = new BigDecimal(y2EVBYEBITNumber);
                    bd = bd.setScale(4, RoundingMode.HALF_UP);
                    y2EVBYEBITNumber = bd.doubleValue();
                }
            } else {
                y0EVBYEBITNumber = y0EVBYEBITDANumber;
                y1EVBYEBITNumber = y1EVBYEBITDANumber;
                y2EVBYEBITNumber = y2EVBYEBITDANumber;

            }

            outPutString = QUARTER + "\t" + dateString + "\t" + mcapNumber + "\t" + cmp + "\t\t" + BROKER + "\t" + rating + "\t" + tp + "\t";
            outPutString = outPutString + "\t" + y0RevenueNumber + "\t" + y1RevenueNumber + "\t" + y2RevenueNumber + "\t";
            outPutString = outPutString + "\t" + "=" + y0EBITDANumber + "-" + y0DepreciationNumber + "\t" + "=" + y1EBITDANumber + "-" + y1DepreciationNumber + "\t" + "=" + y2EBITDANumber + "-" + y2DepreciationNumber + "\t";
            outPutString = outPutString + "\t" + y0EBITDAMargin + "%\t" + y1EBITDAMargin + "%\t" + y2EBITDAMargin + "%";
            outPutString = outPutString + "\t" + y0ROCE + "%\t" + y1ROCE + "%\t" + y2ROCE + "%";
            outPutString = outPutString + "\t" + y0EVBYEBITNumber + "\t" + y1EVBYEBITNumber + "\t" + y2EVBYEBITNumber;
            outPutString = outPutString + "\t" + analystNames;

            reportParameters.setQuarter(QUARTER);
            reportParameters.setReportDate(dateString);
            reportParameters.setMcap(new BigDecimal(mcapNumber).setScale(0, RoundingMode.HALF_UP));
            reportParameters.setPrice(new BigDecimal(cmp.replace(",", "")).setScale(0, RoundingMode.HALF_UP) );
            reportParameters.setBroker(BROKER);
            reportParameters.setRating(rating);
            reportParameters.setTarget(new BigDecimal(tp.replace(",", "")).setScale(0, RoundingMode.HALF_UP));
            reportParameters.setY0Revenue(new BigDecimal(y0RevenueNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setY1Revenue(new BigDecimal(y1RevenueNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setY2Revenue(new BigDecimal(y2RevenueNumber).setScale(2, RoundingMode.HALF_UP));
            if(!isFinancialReport) {
                reportParameters.setY0EBIT(y0EBITDANumber + "-" + y0DepreciationNumber);
                reportParameters.setY1EBIT(y1EBITDANumber + "-" + y1DepreciationNumber);
                reportParameters.setY2EBIT(y2EBITDANumber + "-" + y2DepreciationNumber);
            } else {
                reportParameters.setY0EBIT(""+ y0EBITDANumber);
                reportParameters.setY1EBIT(""+ y1EBITDANumber);
                reportParameters.setY2EBIT(""+ y2EBITDANumber);
                reportParameters.setY0GNPA(new BigDecimal(y0GNPANumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY1GNPA(new BigDecimal(y1GNPANumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY2GNPA(new BigDecimal(y2GNPANumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY0NNPA(new BigDecimal(y0NNPANumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY1NNPA(new BigDecimal(y1NNPANumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY2NNPA(new BigDecimal(y2NNPANumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY0AUM(new BigDecimal(y0AUMNumber));
                reportParameters.setY1AUM(new BigDecimal(y1AUMNumber));
                reportParameters.setY2AUM(new BigDecimal(y2AUMNumber));
                reportParameters.setY0CreditCost(new BigDecimal(y0CreditCostNumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY1CreditCost(new BigDecimal(y1CreditCostNumber/100).setScale(4, RoundingMode.HALF_UP));
                reportParameters.setY2CreditCost(new BigDecimal(y2CreditCostNumber/100).setScale(4, RoundingMode.HALF_UP));
            }
            if(!y0EBITDAMargin.isEmpty())
                reportParameters.setY0OPM(new BigDecimal(Double.parseDouble(y0EBITDAMargin)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY0OPM(new BigDecimal(0));
            if(!y1EBITDAMargin.isEmpty())
                reportParameters.setY1OPM(new BigDecimal(Double.parseDouble(y1EBITDAMargin)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY1OPM(new BigDecimal(0));
            if(!y2EBITDAMargin.isEmpty())
                reportParameters.setY2OPM(new BigDecimal(Double.parseDouble(y2EBITDAMargin)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY2OPM(new BigDecimal(0));
            if(!y0ROCE.isEmpty())
                reportParameters.setY0ROCE(new BigDecimal(Double.parseDouble(y0ROCE)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY0ROCE(new BigDecimal("0"));
            if(!y1ROCE.isEmpty())
                reportParameters.setY1ROCE(new BigDecimal(Double.parseDouble(y1ROCE)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY1ROCE(new BigDecimal("0"));
            if(!y2ROCE.isEmpty())
                reportParameters.setY2ROCE(new BigDecimal(Double.parseDouble(y2ROCE)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY2ROCE(new BigDecimal("0"));
            reportParameters.setY0EVBYEBIT(new BigDecimal(y0EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setY1EVBYEBIT(new BigDecimal(y1EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setY2EVBYEBIT(new BigDecimal(y2EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setAnalystsNames(analystNames);

            System.out.println("Extrated data for ticker " + rdec.getTICKER() + " : " + outPutString);

        } catch (IOException e) {
            System.out.println("Excecption in AnalystREcoExtractorMOSL for report " + QUARTER + "_" + rdec.getTICKER() + "_MOSL.pdf  " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            pdfReader.close();
        }
        return reportParameters;
    }



    /*protected String getAnalyst(String analystNames, String pageContent, ReportDataExtractConfig rdec, int analyst1LineNumber){

        String[] lines = pageContent.split("\n");
        if (analyst1LineNumber != -1) {
            String tmpanalystNames = lines[analyst1LineNumber].replace("Research", "").
                    replace("research", "").
                    replace("Analysts", "").
                    replace("analysts", "").
                    replace("Analyst", "").
                    replace("analyst", "").
                    replace("–", "").
                    replace("-", "").
                    replace(":", "");
            // remove email ID which is in the bracket
            int i1 = -1, i2 = -1;
            String analystSeperator = "\\|";
            boolean isMoreThanOneAnalyst = false;
            if(tmpanalystNames.indexOf("|")>0){
                isMoreThanOneAnalyst = true;
            } else if (tmpanalystNames.indexOf("/")>0) {
                analystSeperator = "/";
                isMoreThanOneAnalyst = true;
            }
            if (isMoreThanOneAnalyst) {
                String[] parts = tmpanalystNames.split(analystSeperator);
                for (int i = 0; i < parts.length; i++) {
                    i1 = parts[i].indexOf("(");
                    i2 = parts[i].indexOf("(");
                    if (i1> 0 && i2 > 0) {
                        analystNames = analystNames + parts[i].replace(parts[i].substring(i1, i2+1), "").trim() + "; ";
                    }
                }
            } else {
                i1 = tmpanalystNames.indexOf("(");
                i2 = tmpanalystNames.indexOf(")");
                if (i1> 0 && i2 > 0)
                    tmpanalystNames = tmpanalystNames.replace(tmpanalystNames.substring(i1, i2+1), "").trim();
                analystNames = analystNames + tmpanalystNames + "; ";
            }

            // find the index of 2nd Analysts Row
            int analyst2LineNumber = -1;
            for (int i = analyst1LineNumber+1; i < lines.length; i++) {
                if (lines[i].toLowerCase().contains(rdec.getRESEARCHANALYST2().toLowerCase())) {
                    analyst2LineNumber = i;
                    tmpanalystNames = lines[i].replace("Research", "").
                            replace("research", "").
                            replace("Analysts", "").
                            replace("analysts", "").
                            replace("Analyst", "").
                            replace("analyst", "").
                            replace("–", "").
                            replace("-", "").
                            replace(":", "");
                    analystSeperator = "\\|";
                    isMoreThanOneAnalyst = false;
                    if(tmpanalystNames.indexOf("|")>0){
                        isMoreThanOneAnalyst = true;
                    } else if (tmpanalystNames.indexOf("/")>0) {
                        analystSeperator = "/";
                        isMoreThanOneAnalyst = true;
                    }
                    if (isMoreThanOneAnalyst) {
                        String[] parts = tmpanalystNames.split(analystSeperator);
                        for (int j = 0; j < parts.length; j++) {
                            // remove email ID which is in the bracket
                            i1 = parts[j].indexOf("(");
                            i2 = parts[j].indexOf(")");
                            if (i1> 0 && i2 > 0) {
                                analystNames = analystNames + " " + parts[j].replace(parts[j].substring(i1, i2+1), "").trim() + ";";
                            }
                        }
                    } else {
                        i1 = tmpanalystNames.indexOf("(");
                        i2 = tmpanalystNames.indexOf(")");
                        if (i1> 0 && i2 > 0)
                            tmpanalystNames = tmpanalystNames.replace(tmpanalystNames.substring(i1, i2+1), "").trim();
                        analystNames = analystNames + tmpanalystNames + "; ";
                    }
                    break;
                }
                if (analyst2LineNumber > 0)
                    break;
            }
                *//*analystNames = analystNames.replace("–  Analyst )", "");
                analystNames = analystNames.replace("–  analyst )", "");
                analystNames = analystNames.replace("–", "");
                analystNames = analystNames.replace("   ;", ";");
                analystNames = analystNames.replace("Analyst", "");
                analystNames = analystNames.replace("analyst", "");
                analystNames = analystNames.replace(")", "");
                analystNames = analystNames.replace("(", "");*//*
        }
        analystNames = capitalizeFirstChar(analystNames).
                replace(", Cfa", ", CFA").
                replace(", Ca", ", CA").
                trim();
        analystNames = analystNames.substring(0,analystNames.length()-1);

        return analystNames;
    }*/

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


