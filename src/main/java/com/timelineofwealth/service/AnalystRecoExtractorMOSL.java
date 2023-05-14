package com.timelineofwealth.service;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.timelineofwealth.dto.ReportDataExtractConfig;
import com.timelineofwealth.dto.ReportParameters;


public class AnalystRecoExtractorMOSL extends AnalystRecoExtractor {

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
            String pageContent = PdfTextExtractor.getTextFromPage(pdfReader, 1);
//            System.out.println("Content on Page 1 : \n" + pageContent);

            // Extract report date
            String[] lines = pageContent.split("\n");
            int lineNumber = 0;
            // Extracted Date
            String dateString = "";
            long dateLastModified = new File(reportFilePath).lastModified();
            while (lineNumber < lines.length && dateString.isEmpty()) {
                dateString = getReportDate(lines[lineNumber], dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
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
            for (int i = 0; i < lines.length; i++) {
                if (!isCMPLineFound && lines[i].contains(CMP)){
                    cmpLineNumber = i;
                    isCMPLineFound = true;
                    if (lines[i].contains(TP)) {
                        tpLineNumber = cmpLineNumber;
                        isTPLineFound = true;
                    }
                }
                if (!isTPLineFound && lines[i].contains(TP)){
                    tpLineNumber = i;
                    isTPLineFound = true;
                }
                if(!isMCapLineFound && lines[i].startsWith(MCAP)) {
                    mcapLineNumber = i;
                    isMCapLineFound = true;
                }
                if (!isAnalyst1LineFound && lines[i].toLowerCase().contains(RESEARCHANALYST1.toLowerCase())){
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
                mcapNumber = getMCapFromBillion(lines[mcapLineNumber], rdec, 1, BROKER);
                System.out.print("MCap : " + mcapNumber + " ");
            } else {
                mcapNumber = 0;
                System.out.println("\n\n ********** Exception ********* \n\n Market Cap was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Extract CMP
            String cmp = "";
            if (cmpLineNumber != -1) {
                cmp = "" + getCMP(lines[cmpLineNumber], rdec, 2, BROKER);
                System.out.print("CMP : " + cmp + " ");
            } else {
                cmp = "0";
                System.out.println("\n\n ********** Exception ********* \n\n CMP was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Extract TP
            String tp = "";
            if (tpLineNumber != -1) {
                tp = "" + getTP(lines[tpLineNumber], rdec, 1, BROKER);
                System.out.print("TP : " + tp + " ");
            } else {
                tp = "0";
                System.out.println("\n\n ********** Exception ********* \n\n TP was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Extract rating
            String rating = "";
            if (cmpLineNumber!= -1) {
                String ratingLine = lines[cmpLineNumber];
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
            analystNames = getAnalyst(analystNames, pageContent, rdec, analyst1LineNumber);
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
            pageContent = PdfTextExtractor.getTextFromPage(pdfReader, incomeStatementPageNumber);
//            System.out.println("\n\n*************************\n\nContent on Page :" + INCOME_STATEMENT_PAGE + " No. - " + incomeStatementPageNumber + "\n" + pageContent);

            // Get lines from from the Inc. Statement Page
            lines = pageContent.split("\n");

            // Get Header Line No. from the Inc. Statement Page
            int headerLineNumber = -1;
            headerLineNumber = getLineNumberForMatchingPattern(lines, 0, HEADER_ROW_NAME,rdec, BROKER);
            if (headerLineNumber > 1)
                System.out.println("Inc. Statement Header Line No. : " + headerLineNumber);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Income Statement Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Revenue Line No. form the Inc. Statement Page
            int revenueLineNumber = -1;
            revenueLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, REVENUE_ROW_NAME,rdec, BROKER);
            if (revenueLineNumber > 1)
                System.out.println("Revenue Line No. : " + revenueLineNumber + " Value - " + lines[revenueLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Revenue Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get EBITDA Line No. form the Inc. Statement Page
            int ebitdaLineNumber = -1;
            ebitdaLineNumber = getLineNumberForMatchingPattern(lines, revenueLineNumber, EBITDA_ROW_NAME,rdec, BROKER);
            if (ebitdaLineNumber > 1)
                System.out.println("EBITDA Line No. : " + ebitdaLineNumber + " Value - " + lines[ebitdaLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n EBITDA Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Depreciation Line No. form the Inc. Statement Page
            int depreciationLineNumber = -1;
            depreciationLineNumber = getLineNumberForMatchingPattern(lines, ebitdaLineNumber, DEPRECIATION_ROW_NAME,rdec, BROKER);
            if (depreciationLineNumber > 1)
                System.out.println("Depreciation Line No. : " + depreciationLineNumber + " Value - " + lines[depreciationLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Depreciation Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get OPM Lines Line No. form the Inc. Statement Page
            int ebitdaMarginLineNumber = -1;
            ebitdaMarginLineNumber = getLineNumberForMatchingPattern(lines, ebitdaLineNumber, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
            if (ebitdaMarginLineNumber > 1)
                System.out.println("OPM Line No. : " + ebitdaMarginLineNumber + " Value - " + lines[ebitdaMarginLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n OPM Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Corresponding Line
            String header = lines[headerLineNumber];
            String revenue = lines[revenueLineNumber];
            String ebitda = lines[ebitdaLineNumber];
            String depreciation = lines[depreciationLineNumber];
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
                ebitdaMargin = lines[ebitdaMarginLineNumber];
            }

            // Convert rest of the line into Array
            String[] headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
            String[] revenueColumns = getDataColumnsForHeader(revenue, REVENUE_ROW_NAME);
            String[] ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME);
            String[] depreciationColumns = getDataColumnsForHeader(depreciation, DEPRECIATION_ROW_NAME);
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
            MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContent).find()? "B" : "M";

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
                y0Depreciation = depreciationColumns[y0Column];
                if(ebitdaMarginColumns!=null)
                    y0EBITDAMargin = ebitdaMarginColumns[y0Column];
                else
                    y0EBITDAMargin = "0";

                y0RevenueNumber = Double.parseDouble(y0Revenue.replace(",", ""));
                y0EBITDANumber = Double.parseDouble(y0EBITDA.replace(",", ""));
                y0DepreciationNumber = Double.parseDouble(y0Depreciation.replace(",", ""));

                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                    y0RevenueNumber = y0RevenueNumber / 10;
                    y0EBITDANumber = y0EBITDANumber / 10;
                    y0DepreciationNumber = y0DepreciationNumber / 10;
                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                    y0RevenueNumber = y0RevenueNumber * 100;
                    y0EBITDANumber = y0EBITDANumber * 100;
                    y0DepreciationNumber = y0DepreciationNumber * 100;
                }
            }
            if (y1Column == -1) {
                System.out.println(Y1 + " column not found in the header on " + INCOME_STATEMENT_PAGE + " page");
            } else {
                y1Revenue = revenueColumns[y1Column];
                y1EBITDA = ebitdaColumns[y1Column];
                y1Depreciation = depreciationColumns[y1Column];
                if(ebitdaMarginColumns!=null)
                    y1EBITDAMargin = ebitdaMarginColumns[y1Column];
                else
                    y1EBITDAMargin = "0";

                y1RevenueNumber = Double.parseDouble(y1Revenue.replace(",", ""));
                y1EBITDANumber = Double.parseDouble(y1EBITDA.replace(",", ""));
                y1DepreciationNumber = Double.parseDouble(y1Depreciation.replace(",", ""));

                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                    y1RevenueNumber = y1RevenueNumber / 10;
                    y1EBITDANumber = y1EBITDANumber / 10;
                    y1DepreciationNumber = y1DepreciationNumber / 10;
                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                    y1RevenueNumber = y1RevenueNumber * 100;
                    y1EBITDANumber = y1EBITDANumber * 100;
                    y1DepreciationNumber = y1DepreciationNumber * 100;
                }
            }
            if (y2Column == -1) {
                System.out.println(Y2 + " column not found in the header on " + INCOME_STATEMENT_PAGE + " page");
            } else {
                y2Revenue = revenueColumns[y2Column];
                y2EBITDA = ebitdaColumns[y2Column];
                y2Depreciation = depreciationColumns[y2Column];
                if(ebitdaMarginColumns!=null)
                    y2EBITDAMargin = ebitdaMarginColumns[y2Column];
                else
                    y2EBITDAMargin = "0";

                y2RevenueNumber = Double.parseDouble(y2Revenue.replace(",", ""));
                y2EBITDANumber = Double.parseDouble(y2EBITDA.replace(",", ""));
                y2DepreciationNumber = Double.parseDouble(y2Depreciation.replace(",", ""));

                if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                    y2RevenueNumber = y2RevenueNumber / 10;
                    y2EBITDANumber = y2EBITDANumber / 10;
                    y2DepreciationNumber = y2DepreciationNumber / 10;
                } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                    y2RevenueNumber = y2RevenueNumber * 100;
                    y2EBITDANumber = y2EBITDANumber * 100;
                    y2DepreciationNumber = y2DepreciationNumber * 100;
                }
            }

            //Load the Ratio Page
            pageContent = PdfTextExtractor.getTextFromPage(pdfReader, ratioPageNumber);
//            System.out.println("\n\n*************************\n\n  Content on Ratio Page " + RATIO_PAGE + " No :" + ratioPageNumber + "\n" + pageContent);

            // Get lines on the Ratio Page
            lines = pageContent.split("\n");

            // Get Header Line No. from the Inc. Ratio Page
            headerLineNumber = -1;
            headerLineNumber = getLineNumberForMatchingPattern(lines, 0, HEADER_ROW_NAME,rdec, BROKER);
            if (headerLineNumber > 1)
                System.out.println("Ratio Header Line No. : " + headerLineNumber);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Ratio Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get ROCE Line No. from the Inc. Ratio Page
            int roceLineNumber = -1;
            roceLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, ROCE_ROW_NAME,rdec, BROKER);
            if (roceLineNumber > 1)
                System.out.println("ROCE Line No. : " + roceLineNumber);
            else
                System.out.println("\n\n ********** Exception ********* \n\n ROCE Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get EVBYEBITDA Line No. from the Inc. Ratio Page
            int evbyebitdaLineNumber = -1;
            evbyebitdaLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, EVBYEBITDA_ROW_NAME,rdec, BROKER);
            if (evbyebitdaLineNumber > 1)
                System.out.println("EVBYEBITDA Line No. : " + evbyebitdaLineNumber + "  Value -" + lines[evbyebitdaLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n EVBYEBITDA Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);


            header = lines[headerLineNumber];
            String roce = lines[roceLineNumber];
            String evbyebitda = lines[evbyebitdaLineNumber];

            headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
            String[] roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME);
            String[] evbyebitdaColumns = getDataColumnsForHeader(evbyebitda, EVBYEBITDA_ROW_NAME);

            // Handle exceptional case if Coumnn array is null
            if (headerColumns == null || headerColumns.length == 0) {
                headerColumns = appendPreviousRow(lines, headerLineNumber, HEADER_ROW_NAME, true);
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

                    y0ROCE = roceColumns[y0Column-(headerColumns.length-roceColumns.length)];
                } else if (roceColumns.length != 0) {
                    y0ROCE = roceColumns[y0Column];
                } else {
                    y0ROCE = "0";
                }
                try {
                    if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                        y0EVBYEBITDA = evbyebitdaColumns[y0Column - (headerColumns.length - evbyebitdaColumns.length)];
                    } else {
                        y0EVBYEBITDA = evbyebitdaColumns[y0Column];
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
                        y1ROCE = roceColumns[y1Column - (headerColumns.length - roceColumns.length)];
                    } else if (roceColumns.length != 0) {
                        y1ROCE = roceColumns[y1Column];
                    } else {
                        y1ROCE = "0";
                    }
                    if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                        y1EVBYEBITDA = evbyebitdaColumns[y1Column - (headerColumns.length - evbyebitdaColumns.length)];
                    } else {
                        y1EVBYEBITDA = evbyebitdaColumns[y1Column];
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
                        y2ROCE = roceColumns[y2Column - (headerColumns.length - roceColumns.length)];
                    } else if (roceColumns.length != 0) {
                        y2ROCE = roceColumns[y2Column];
                    } else {
                        y2ROCE = "0";
                    }
                    if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                        y2EVBYEBITDA = evbyebitdaColumns[y2Column - (headerColumns.length - evbyebitdaColumns.length)];
                    } else {
                        y2EVBYEBITDA = evbyebitdaColumns[y2Column];
                    }
                } catch (Exception e) {
                    y2EVBYEBITDA = "0";
                }
                y2EVBYEBITDANumber = Double.parseDouble(y2EVBYEBITDA);
            }

            // Converting EVBYEBITDA to EVBYEBIT
            if (y0EBITDANumber > 0 && Math.abs(y0DepreciationNumber) > 0 && y0EVBYEBITDANumber > 0) {
                y0EVBYEBITNumber = (y0EVBYEBITDANumber*y0EBITDANumber) / (y0EBITDANumber - Math.abs(y0DepreciationNumber));
                BigDecimal bd = new BigDecimal(y0EVBYEBITNumber);
                bd = bd.setScale(4, RoundingMode.HALF_UP);
                y0EVBYEBITNumber = bd.doubleValue();
            }
            if (y1EBITDANumber > 0 && Math.abs(y1DepreciationNumber) > 0 && y1EVBYEBITDANumber > 0) {
                y1EVBYEBITNumber = (y1EVBYEBITDANumber*y1EBITDANumber) / (y1EBITDANumber - Math.abs(y1DepreciationNumber));
                BigDecimal bd = new BigDecimal(y1EVBYEBITNumber);
                bd = bd.setScale(4, RoundingMode.HALF_UP);
                y1EVBYEBITNumber = bd.doubleValue();
            }
            if (y2EBITDANumber > 0 && Math.abs(y2DepreciationNumber) > 0 && y2EVBYEBITDANumber > 0) {
                y2EVBYEBITNumber = (y2EVBYEBITDANumber*y2EBITDANumber) / (y2EBITDANumber - Math.abs(y2DepreciationNumber));
                BigDecimal bd = new BigDecimal(y2EVBYEBITNumber);
                bd = bd.setScale(4, RoundingMode.HALF_UP);
                y2EVBYEBITNumber = bd.doubleValue();
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
            reportParameters.setY0EBIT(y0EBITDANumber + "-" + y0DepreciationNumber);
            reportParameters.setY1EBIT(y1EBITDANumber + "-" + y1DepreciationNumber);
            reportParameters.setY2EBIT(y2EBITDANumber + "-" + y2DepreciationNumber);
            reportParameters.setY0OPM(new BigDecimal(Double.parseDouble(y0EBITDAMargin)/100).setScale(4, RoundingMode.HALF_UP));
            reportParameters.setY1OPM(new BigDecimal(Double.parseDouble(y1EBITDAMargin)/100).setScale(4, RoundingMode.HALF_UP));
            reportParameters.setY2OPM(new BigDecimal(Double.parseDouble(y2EBITDAMargin)/100).setScale(4, RoundingMode.HALF_UP));
            reportParameters.setY0ROCE(new BigDecimal(Double.parseDouble(y0ROCE)/100).setScale(4, RoundingMode.HALF_UP));
            reportParameters.setY1ROCE(new BigDecimal(Double.parseDouble(y1ROCE)/100).setScale(4, RoundingMode.HALF_UP));
            reportParameters.setY2ROCE(new BigDecimal(Double.parseDouble(y2ROCE)/100).setScale(4, RoundingMode.HALF_UP));
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


