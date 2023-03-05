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


public class AnalystRecoExtractorKOTAK extends AnalystRecoExtractor {

    public ReportParameters getReportParameters(String reportFilePath, ReportDataExtractConfig rdec) {

        String BROKER = "KOTAK";
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

        String DATEPATTERN = "(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s*\\d{1,2},?\\s*\\d{2,4}";
        String DATEFORMAT = "MMMMM dd yyyy";

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
            int lineNumber = 1;
            // Extracted Date
            String dateString = "";
            long dateLastModified = new File(reportFilePath).lastModified();
            while (lineNumber < 10 && dateString.isEmpty()) {
                dateString = getReportDate(lines[lineNumber], dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
                lineNumber++;
            }
            // if date not found then set it to last modified date
            if (dateString.isEmpty()){
                dateString = getReportDate(null, dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
            }
            System.out.print("Date : " + dateString + " ");

            // get the line no. matching CMP, TP and MCAP
            boolean isCMPLineFound = false, isTPLineFound = false, isMCapLineFound = false, isRatingsFound = false;
            int cmpLineNumber = -1, tpLineNumber = -1, mcapLineNumber = -1, analyst1LineNumber = -1, analyst2LineNumber = -1;
            Pattern ratingPattern = Pattern.compile(RATINGPATTERN);
            String rating = "";
            for (int i = 0; i < lines.length; i++) {
                if (!isCMPLineFound && lines[i].contains(CMP)){
                    cmpLineNumber = i;
                    isCMPLineFound = true;
                }
                if (!isTPLineFound && lines[i].contains(TP)){
                    tpLineNumber = i;
                    isTPLineFound = true;
                }
                if(!isMCapLineFound && lines[i].contains(MCAP)) {
                    mcapLineNumber = i;
                    isMCapLineFound = true;
                }
                Matcher m = ratingPattern.matcher(lines[i].trim());
                if(!isRatingsFound && m.find()) {
                    rating = capitalizeFirstChar(m.group(1));
                    isRatingsFound = true;
                }
                if(isCMPLineFound && isMCapLineFound && isTPLineFound)
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
                cmp = "" + getCMP(lines[cmpLineNumber], rdec, 1, BROKER);
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
            if (!rating.isEmpty())
                System.out.print("Ratings : " + rating + "\n");
            else
                System.out.println("\n\n ********** Exception ********* \n\n Rating was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Extract Analyst Names
            String analystNames = "";
            analystNames = getAnalyst(analystNames, pageContent, rdec, -1);
            if(!analystNames.isEmpty())
                System.out.println("Analysts Names : " + analystNames);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Analyst Names were not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);


            // Find Income Statement Page No.
            int incomeStatementPageNumber = -1;
            incomeStatementPageNumber = getPageNumberForMatchingPattern(pdfReader,  2, noOfPages, INCOME_STATEMENT_PAGE, rdec, true, BROKER);
            if (incomeStatementPageNumber > 1)
                System.out.print("Inc. Statement Page No. : " + incomeStatementPageNumber + " / ");
            else
                System.out.println("\n\n ********** Exception ********* \n\n Income Statement Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Find Ratio Page No.
            int ratioPageNumber = -1;
            ratioPageNumber = getPageNumberForMatchingPattern(pdfReader,  2, incomeStatementPageNumber, RATIO_PAGE, rdec, true, BROKER);
            if (incomeStatementPageNumber > 1)
                System.out.print("Ratio Page No. : " + ratioPageNumber + " / ");
            else
                System.out.println("\n\n ********** Exception ********* \n\n Ratio Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Find Valuation Page No.
            int valuationPageNumber = -1;
            valuationPageNumber = getPageNumberForMatchingPattern(pdfReader,  1, ratioPageNumber, rdec.getVALUATION_PAGE(), rdec, BROKER);
            if (incomeStatementPageNumber > 1)
                System.out.println("Valuation Page No. : " + valuationPageNumber);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Valuation Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Load the Inc. Statement Page
            pageContent = PdfTextExtractor.getTextFromPage(pdfReader, incomeStatementPageNumber);
//            System.out.println("\n\n*************************\n\nContent on Page :" + INCOME_STATEMENT_PAGE + " No. - " + incomeStatementPageNumber + "\n" + pageContent);

            // Get lines from from the Inc. Statement Page
            lines = pageContent.split("\n");

            // Get Header Line No. from the Inc. Statement Page in KOTAK header is not there so search for Y0 and pass "" to get the headerColumns
            int headerLineNumber = -1;
            headerLineNumber = getLineNumberForMatchingPattern(lines, 1, HEADER_ROW_NAME,rdec, true, BROKER);
//            headerLineNumber = headerLineNumber - 1;
            if (headerLineNumber > 1)
                System.out.println("Inc. Statement Header Line : " + lines[headerLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Income Statement Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Revenue Line No. form the Inc. Statement Page
            int revenueLineNumber = -1;
            revenueLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, REVENUE_ROW_NAME,rdec, BROKER);
            if (revenueLineNumber > 1)
                System.out.println("Revenue Line : " + lines[revenueLineNumber]);
            else {
                revenueLineNumber = getLineNumberForMatchingPattern(lines, 0, REVENUE_ROW_NAME,rdec, BROKER);
                if (revenueLineNumber > 1)
                    System.out.println("Revenue Line : " + lines[revenueLineNumber]);
                else
                    System.out.println("\n\n ********** Exception ********* \n\n Revenue Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Get EBITDA Line No. form the Inc. Statement Page
            int ebitdaLineNumber = -1;
            ebitdaLineNumber = getLineNumberForMatchingPattern(lines, revenueLineNumber, EBITDA_ROW_NAME,rdec, BROKER);
            if (ebitdaLineNumber > 1)
                System.out.println("EBITDA Line : " + lines[ebitdaLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n EBITDA Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Depreciation Line No. form the Inc. Statement Page
            int depreciationLineNumber = -1;
            depreciationLineNumber = getLineNumberForMatchingPattern(lines, ebitdaLineNumber, DEPRECIATION_ROW_NAME,rdec, BROKER);
            if (depreciationLineNumber > 1)
                System.out.println("Depreciation Line : " + lines[depreciationLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Depreciation Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Corresponding Line
            String header = lines[headerLineNumber].replace("`", ""); // patch for FY23Q3_BRIGADE_KOTAK
            String revenue = lines[revenueLineNumber];
            String ebitda = lines[ebitdaLineNumber].replace("(", "-").replace(")", "");
            boolean depreciationMissingFlag = false;
            String depreciation = null;
            String ebit = null;
            if (depreciationLineNumber == -1) {
                System.out.println("\n\n ********** Exception ********* \n\n Handling a case when depreciation is not present");
                int ebitLineNumber = getLineNumberForMatchingPattern(lines, ebitdaLineNumber, "(?i)^((EBIT ))",rdec, BROKER);
                ebit = lines[ebitLineNumber];
                depreciationMissingFlag = true;
            } else
                depreciation = lines[depreciationLineNumber];

            // Convert rest of the line into Array
            String[] headerColumns = getDataColumnsForHeader(header, "");
            String[] revenueColumns = getDataColumnsForHeader(revenue, REVENUE_ROW_NAME, headerColumns.length);

            if(revenueColumns == null || revenueColumns.length == 0) {
                // handling a case especially ITC where Segment Revenues header is not followed by numbers
                int newrevenueLineNumber  = revenueLineNumber;
                while (newrevenueLineNumber != -1 || revenueColumns.length == 0) {
                    newrevenueLineNumber = getLineNumberForMatchingPattern(lines, revenueLineNumber + 1, REVENUE_ROW_NAME, rdec, BROKER);
                    if (newrevenueLineNumber != -1 ) {
                        revenue = lines[newrevenueLineNumber];
                        revenueColumns = getDataColumnsForHeader(revenue, REVENUE_ROW_NAME, headerColumns.length);
                        revenueLineNumber = newrevenueLineNumber;
                    }
                }
            }

            // Handling a case when Revenue number is "1  ,040"
            if (revenueColumns.length != headerColumns.length) {
                String modifiedRevenue = revenue.replaceAll("(?<=\\d)\\s\\s(?=,)", "").trim();
                revenueColumns = getDataColumnsForHeader(modifiedRevenue, REVENUE_ROW_NAME, headerColumns.length);
            }

            // evenif columns are not matching then call function getCorrectNumbers
            if(revenueColumns.length != headerColumns.length) {
                revenueColumns = getDataColumnsForHeader(revenue.replace(",", "").trim(), REVENUE_ROW_NAME);
                revenueColumns = getCorrectNumbers(revenueColumns);
            }

            String[] ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME, headerColumns.length);

            // Handling a case when ebitda number is "1  ,040"
            if (ebitdaColumns.length != revenueColumns.length) {
                String modifiedEbitda = ebitda.replaceAll("(?<=\\d)\\s\\s(?=,)", "").trim();
                ebitdaColumns = getDataColumnsForHeader(modifiedEbitda, EBITDA_ROW_NAME, headerColumns.length);
            }

            // evenif columns are not matching then call function getCorrectNumbers
            if(ebitdaColumns.length != revenueColumns.length) {
                ebitdaColumns = getDataColumnsForHeader(ebitda.replace(",", "").trim(), EBITDA_ROW_NAME);
                ebitdaColumns = getCorrectNumbers(ebitdaColumns);
            }

            String[] depreciationColumns = null;
            String[] ebitColumns = null;
            if (depreciationMissingFlag == true) {
                ebitColumns = getDataColumnsForHeader(ebit, "(?i)^((EBIT ))");
                // evenif columns are not matching then call function getCorrectNumbers
                if(ebitColumns.length != revenueColumns.length) {
                    ebitColumns = getDataColumnsForHeader(ebit.replace(",", "").trim(), "(?i)^((EBIT ))");
                    ebitColumns = getCorrectNumbers(ebitColumns);
                }
            } else {
                depreciationColumns = getDataColumnsForHeader(depreciation.replace("(", "").replace(")", ""), DEPRECIATION_ROW_NAME, headerColumns.length);
                // evenif columns are not matching then call function getCorrectNumbers
                if(depreciationColumns.length != revenueColumns.length) {
                    depreciationColumns = getDataColumnsForHeader(depreciation.replace(",", "").trim(), DEPRECIATION_ROW_NAME);
                    depreciationColumns = getCorrectNumbers(depreciationColumns);
                }
            }

            // Set million or billion flag
            MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContent).find()? "B" : "M";

            //handling a case of VBL where headers are at two places
            if(revenueColumns.length == ebitdaColumns.length && revenueColumns.length < headerColumns.length) {
                headerColumns = getDataColumnsForHeader(header, "(?i)^((\\s*Key metrics/assumptions))");
            }

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

            if(!depreciationMissingFlag)
                System.out.println("Dep. Columns    " + depreciationColumns.length);
            else
                System.out.println("EBIT Columns    " + ebitColumns.length);

            String y0Revenue = "", y1Revenue = "", y2Revenue = "";
            String y0EBITDA = "", y1EBITDA = "", y2EBITDA = "";
            String y0Depreciation = "", y1Depreciation = "", y2Depreciation = "";
            String y0EBIT = "", y1EBIT = "", y2EBIT = "";
            String y0EBITDAMargin = "0", y1EBITDAMargin = "0", y2EBITDAMargin = "0";
            double y0RevenueNumber = 0, y1RevenueNumber = 0, y2RevenueNumber = 0;
            double y0EBITDANumber = 0, y1EBITDANumber = 0, y2EBITDANumber = 0;
            double y0DepreciationNumber = 0, y1DepreciationNumber = 0, y2DepreciationNumber = 0;
            double y0EBITNumber = 0, y1EBITNumber = 0, y2EBITNumber = 0;
            if (y0Column == -1) {
                System.out.println(Y0 + " column not found in the header on " + INCOME_STATEMENT_PAGE + " page");
            } else {
                y0Revenue = revenueColumns[y0Column];
                y0EBITDA = ebitdaColumns[y0Column];
                if(!depreciationMissingFlag) {
                    y0Depreciation = depreciationColumns[y0Column];
                } else {
                    y0EBIT = ebitColumns[y0Column];
                }

                y0RevenueNumber = Double.parseDouble(y0Revenue.replace(",", ""));
                y0EBITDANumber = Double.parseDouble(y0EBITDA.replace(",", "").replace("(", "-").replace(")", ""));

                if(!depreciationMissingFlag) {
                    y0DepreciationNumber = Double.parseDouble(y0Depreciation.replace(",", "").replace("(", "").replace(")", ""));
                } else {
                    y0EBITNumber = Double.parseDouble(y0EBIT.replace(",", "").replace("(", "-").replace(")", ""));
                    y0DepreciationNumber = y0EBITDANumber - y0EBITNumber;
                }

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
                if(!depreciationMissingFlag) {
                    y1Depreciation = depreciationColumns[y1Column];
                } else {
                    y1EBIT = ebitColumns[y1Column];
                }

                y1RevenueNumber = Double.parseDouble(y1Revenue.replace(",", ""));
                y1EBITDANumber = Double.parseDouble(y1EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                if(!depreciationMissingFlag) {
                    y1DepreciationNumber = Double.parseDouble(y1Depreciation.replace(",", "").replace("(", "").replace(")", ""));
                } else {
                    y1EBITNumber = Double.parseDouble(y1EBIT.replace(",", "").replace("(", "-").replace(")", ""));
                    y1DepreciationNumber = y1EBITDANumber - y1EBITNumber;
                }

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
                if(!depreciationMissingFlag) {
                    y2Depreciation = depreciationColumns[y2Column];
                } else {
                    y2EBIT = ebitColumns[y2Column];
                }

                y2RevenueNumber = Double.parseDouble(y2Revenue.replace(",", ""));
                y2EBITDANumber = Double.parseDouble(y2EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
                if(!depreciationMissingFlag) {
                    y2DepreciationNumber = Double.parseDouble(y2Depreciation.replace(",", "").replace("(", "").replace(")", ""));
                } else {
                    y2EBITNumber = Double.parseDouble(y2EBIT.replace(",", "").replace("(", "-").replace(")", ""));
                    y2DepreciationNumber = y2EBITDANumber - y2EBITNumber;
                }

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
            String y0ROCE = "0", y1ROCE = "0", y2ROCE = "0";
            if (ratioPageNumber > 0) {
                pageContent = PdfTextExtractor.getTextFromPage(pdfReader, ratioPageNumber);
//            System.out.println("\n\n*************************\n\n  Content on Page " + RATIO_PAGE + " No :" + ratioPageNumber + "\n" + pageContent);

                // Get lines on the Ratio Page
                lines = pageContent.split("\n");

                // Get Header Line No. from the Inc. Ratio Page
                headerLineNumber = -1;
                headerLineNumber = getLineNumberForMatchingPattern(lines, 0, HEADER_ROW_NAME, rdec, true, BROKER);
                if (headerLineNumber > 1) {
                    header = lines[headerLineNumber].replace("`", ""); // patch for FY23Q3_BRIGADE_KOTAK
                    System.out.println("Ratio Header Line No. : " + headerLineNumber);
                } else {
                    // in case header is not avialble fetch the header from income statement
                    String incStatementPage = PdfTextExtractor.getTextFromPage(pdfReader, incomeStatementPageNumber);
                    String[] incomeStatementLines = incStatementPage.split("\n");
                    headerLineNumber = getLineNumberForMatchingPattern(incomeStatementLines, 0, HEADER_ROW_NAME, rdec, BROKER);
                    if (headerLineNumber > 1) {
                        header = lines[headerLineNumber].replace("`", ""); // patch for FY23Q3_BRIGADE_KOTAK
                        System.out.println("Ratio Header Line : " + header);
                    } else
                        System.out.println("\n\n ********** Exception ********* \n\n Ratio Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }

                headerColumns = getDataColumnsForHeader(header, "", headerColumns.length);

                // Get OPM Lines form the Ratio Page
                int ebitdaMarginLineNumber = -1;
                String ebitdaMargin = null;
                ebitdaMarginLineNumber = getLineNumberForMatchingPattern(lines, 0, EBITDAMARGIN_ROW_NAME, rdec, BROKER);
                if (ebitdaMarginLineNumber > 1) {
                    ebitdaMargin = lines[ebitdaMarginLineNumber];
                    System.out.println("OPM Line : " + ebitdaMargin);
                } else
                    System.out.println("\n\n ********** Exception ********* \n\n OPM Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

                // Get ROCE Line No. from the Inc. Ratio Page
                int roceLineNumber = -1, roceLineNumberSecond = -1;
                roceLineNumber = getLineNumberForMatchingPattern(lines, 0, ROCE_ROW_NAME, rdec, BROKER);
                roceLineNumberSecond = getLineNumberForMatchingPattern(lines, roceLineNumber + 1, ROCE_ROW_NAME, rdec, BROKER);
                if (roceLineNumber > 1 && roceLineNumberSecond == -1)
                    System.out.println("ROCE Line : " + lines[roceLineNumber]);
                else if (roceLineNumber > 1 && roceLineNumberSecond > 1 && lines[roceLineNumberSecond].toLowerCase().contains("post"))
                    roceLineNumber = roceLineNumberSecond;
                else if (roceLineNumber == -1)
                    System.out.println("\n\n ********** Exception ********* \n\n ROCE Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);


                String roce = null;
                String[] roceColumns = null;
                boolean roceMissingFlag = false;
                if (roceLineNumber!= -1) {
                    roce = lines[roceLineNumber].trim();
                    Pattern pattern = Pattern.compile(ROCE_ROW_NAME);
                    Matcher matcher = pattern.matcher(roce);
                    if (matcher.find()) {
                        String rowHeader = matcher.group(1);
                        roce = rowHeader + roce.substring(rowHeader.length(), roce.length()).replace("(", "-").replace(")", "");
                    }
                    roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME, headerColumns.length);
                }
                else {
                    roce = "ROCE ";
                    roceMissingFlag = true;
                    for (int i = 0; i < headerColumns.length; i++) {
                        roce = roce + " 0";
                    }
                    roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME);
                }


                String[] ebitdaMarginColumns = null;
                boolean opmMissingFlag = false;
                if (ebitdaMarginLineNumber!= -1) {
                    ebitdaMargin = lines[ebitdaMarginLineNumber].trim();
                    Pattern pattern = Pattern.compile(EBITDAMARGIN_ROW_NAME);
                    Matcher matcher = pattern.matcher(ebitdaMargin);
                    if (matcher.find()) {
                        String rowHeader = matcher.group(1);
                        ebitdaMargin = rowHeader + ebitdaMargin.substring(rowHeader.length(), ebitdaMargin.length()).replace("(", "-").replace(")", "");
                    }
                    ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME, headerColumns.length);
                }
                else {
                    ebitdaMargin = "EBITDA Margin ";
                    opmMissingFlag = true;
                    for (int i = 0; i < headerColumns.length; i++) {
                        ebitdaMargin = ebitdaMargin + " 0";
                    }
                    ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                }


                // Find Y0, Y1 and Y2 Index position
                y0Column = getIndexOfTheYear(headerColumns, Y0);
                y1Column = getIndexOfTheYear(headerColumns, Y1);
                y2Column = getIndexOfTheYear(headerColumns, Y2);

                System.out.print(" Y0 Index " + y0Column);
                System.out.print(" Y1 Index " + y1Column);
                System.out.print(" Y2 Index " + y2Column + "\n");
                System.out.println("Header Columns  " + headerColumns.length);
                System.out.println("OPM Columns " + ebitdaMarginColumns.length);
                System.out.println("ROCE Columns  " + roceColumns.length);

                if (y0Column == -1) {
                    System.out.println(Y0 + " column not found in the header on " + RATIO_PAGE + " page");
                } else {
                    if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length) {
                        System.out.println("Header mismatch for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        System.out.print(" Header Length " + headerColumns.length);
                        System.out.print(" roce Length " + roceColumns.length);

                        y0ROCE = roceColumns[y0Column - (headerColumns.length - roceColumns.length)].replace("%", "");
                    } else {
                        y0ROCE = roceColumns[y0Column].replace("%", "");
                    }
                    if (headerColumns.length != ebitdaMarginColumns.length && headerColumns.length > ebitdaMarginColumns.length) {
                        y0EBITDAMargin = ebitdaMarginColumns[y0Column - (headerColumns.length - ebitdaMarginColumns.length)].replace("%", "");
                    } else {
                        y0EBITDAMargin = ebitdaMarginColumns[y0Column].replace("%", "");
                    }
                }
                if (y1Column == -1) {
                    System.out.println(Y1 + " column not found in the header on " + RATIO_PAGE + " page");
                } else {
                    if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length) {
                        y1ROCE = roceColumns[y1Column - (headerColumns.length - roceColumns.length)].replace("%", "");
                    } else {
                        y1ROCE = roceColumns[y1Column].replace("%", "");
                    }
                    if (headerColumns.length != ebitdaMarginColumns.length && headerColumns.length > ebitdaMarginColumns.length) {
                        y1EBITDAMargin = ebitdaMarginColumns[y1Column - (headerColumns.length - ebitdaMarginColumns.length)].replace("%", "");
                    } else {
                        y1EBITDAMargin = ebitdaMarginColumns[y1Column].replace("%", "");
                    }
                }
                if (y2Column == -1) {
                    System.out.println(Y2 + " column not found in the header on " + RATIO_PAGE + " page");
                } else {
                    if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length) {
                        y2ROCE = roceColumns[y2Column - (headerColumns.length - roceColumns.length)].replace("%", "");
                    } else {
                        y2ROCE = roceColumns[y2Column].replace("%", "");
                    }
                    if (headerColumns.length != ebitdaMarginColumns.length && headerColumns.length > ebitdaMarginColumns.length) {
                        y2EBITDAMargin = ebitdaMarginColumns[y2Column - (headerColumns.length - ebitdaMarginColumns.length)].replace("%", "");
                    } else {
                        y2EBITDAMargin = ebitdaMarginColumns[y2Column].replace("%", "");
                    }
                }
            } else {
                    System.out.println("\\n\\n*************************\\n\\n Exception Ratio Page is missing - Ratio Page No. -" + valuationPageNumber);
            }

            //**********************************//
            //Load the Valuation Page
            String y0EVBYEBITDA = "0", y1EVBYEBITDA = "0", y2EVBYEBITDA = "0";
            double y0EVBYEBITDANumber = 0, y1EVBYEBITDANumber = 0, y2EVBYEBITDANumber = 0;
            double y0EVBYEBITNumber = 0, y1EVBYEBITNumber = 0, y2EVBYEBITNumber = 0;

            if (valuationPageNumber>0 && ratioPageNumber>0) {
                pageContent = PdfTextExtractor.getTextFromPage(pdfReader, valuationPageNumber);
//            System.out.println("\n\n*************************\n\n  Content on Page " + VALUATION_PAGE + " No :" + valuationPageNumber + "\n" + pageContent);

                // Get lines on the Ratio Page
                lines = pageContent.split("\n");

                // Get Header Line No. from the Inc. Ratio Page
                headerLineNumber = -1;
                headerLineNumber = getLineNumberForMatchingPattern(lines, 1, HEADER_ROW_NAME, rdec, true, BROKER);
                if (headerLineNumber > 1) {
                    header = lines[headerLineNumber];
                    System.out.println("Ratio Header Line : " + lines[headerLineNumber]);
                } else {
                    //Search from the top of the page
                    headerLineNumber = getLineNumberForMatchingPattern(lines, 0, HEADER_ROW_NAME, rdec, BROKER);
                    if (headerLineNumber > 1) {
                        header = lines[headerLineNumber];
                        System.out.println("Ratio Header Line No. : " + headerLineNumber);
                    } else {
                        // in case header is not avialble fetch the header from income statement
                        String incStatementPage = PdfTextExtractor.getTextFromPage(pdfReader, incomeStatementPageNumber);
                        String[] incomeStatementLines = incStatementPage.split("\n");
                        headerLineNumber = getLineNumberForMatchingPattern(incomeStatementLines, 0, HEADER_ROW_NAME, rdec, BROKER);
                        if (headerLineNumber > 1) {
                            header = lines[headerLineNumber];
                            System.out.println("Ratio Header Line No. : " + headerLineNumber);
                        } else
                            System.out.println("\n\n ********** Exception ********* \n\n Ratio Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                }

                // Get EVBYEBITDA Line No. from the Inc. Ratio Page
                int evbyebitdaLineNumber = -1;
                evbyebitdaLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, EVBYEBITDA_ROW_NAME, rdec, BROKER);
                if (evbyebitdaLineNumber > 1)
                    System.out.println("EVBYEBITDA Line : " + lines[evbyebitdaLineNumber]);
                else
                    System.out.println("\n\n ********** Exception ********* \n\n EVBYEBITDA Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

                if (evbyebitdaLineNumber != -1) {
                    String evbyebitda = lines[evbyebitdaLineNumber];

                    headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
                    String[] evbyebitdaColumns = getDataColumnsForHeader(evbyebitda, EVBYEBITDA_ROW_NAME);

                    // Handling a case when headerline is not on a new line.
                    if(headerColumns.length != evbyebitdaColumns.length) {
                        Pattern pattern = Pattern.compile(HEADER_ROW_NAME);
                        Matcher matcher = pattern.matcher(header.trim());
                        if (matcher.find()) {
                            String rowHeader = matcher.group(1);
                            int rowHeaderlocation = header.trim().indexOf(rowHeader);
                            String strDataColumns = header.trim().substring(rowHeaderlocation+rowHeader.length(), header.trim().length());
                            headerColumns = strDataColumns.trim().split(" ");
                            headerColumns = Arrays.stream(headerColumns)
                                    .filter(s -> !s.isEmpty())
                                    .toArray(String[]::new);
                        }
                    }

                    // Find Y0, Y1 and Y2 Index position
                    y0Column = getIndexOfTheYear(headerColumns, Y0);
                    y1Column = getIndexOfTheYear(headerColumns, Y1);
                    y2Column = getIndexOfTheYear(headerColumns, Y2);

                    System.out.print(" Y0 Index " + y0Column);
                    System.out.print(" Y1 Index " + y1Column);
                    System.out.print(" Y2 Index " + y2Column + "\n");

                    System.out.println("EV/EBITDA Columns  " + evbyebitdaColumns.length);

                    // Find Y0, Y1 and Y2 Index position
                    if (headerColumns != null ) {
                        y0Column = getIndexOfTheYear(headerColumns, Y0);
                    } else
                        y0Column = -1;
                    y1Column = getIndexOfTheYear(headerColumns, Y1);
                    y2Column = getIndexOfTheYear(headerColumns, Y2);

                    if (y0Column == -1) {
                        System.out.println(Y0 + " column not found in the header on " + RATIO_PAGE + " page");
                    } else {

                        if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                            y0EVBYEBITDA = evbyebitdaColumns[y0Column - (headerColumns.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                        } else {
                            y0EVBYEBITDA = evbyebitdaColumns[y0Column].replace("nmf", "").replace("nm", "");
                        }
                        if (y0EVBYEBITDA.isEmpty())
                            y0EVBYEBITDA = "0";
                        if (y0EVBYEBITDA.contains("(") && y0EVBYEBITDA.contains(")") && !y0EVBYEBITDA.contains("-"))
                            y0EVBYEBITDA = "-" + y0EVBYEBITDA.replace("(", "").replace(")", "");
                        y0EVBYEBITDANumber = Double.parseDouble(y0EVBYEBITDA);

                    }
                    if (y1Column == -1) {
                        System.out.println(Y1 + " column not found in the header on " + RATIO_PAGE + " page");
                    } else {
                        if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                            y1EVBYEBITDA = evbyebitdaColumns[y1Column - (headerColumns.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                        } else {
                            y1EVBYEBITDA = evbyebitdaColumns[y1Column].replace("nmf", "").replace("nm", "");
                        }
                        if (y1EVBYEBITDA.isEmpty())
                            y1EVBYEBITDA = "0";
                        if (y1EVBYEBITDA.contains("(") && y1EVBYEBITDA.contains(")") && !y1EVBYEBITDA.contains("-"))
                            y1EVBYEBITDA = "-" + y1EVBYEBITDA.replace("(", "").replace(")", "");
                        y1EVBYEBITDANumber = Double.parseDouble(y1EVBYEBITDA);
                    }
                    if (y2Column == -1) {
                        System.out.println(Y2 + " column not found in the header on " + RATIO_PAGE + " page");
                    } else {
                        if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                            y2EVBYEBITDA = evbyebitdaColumns[y2Column - (headerColumns.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                        } else {
                            y2EVBYEBITDA = evbyebitdaColumns[y2Column].replace("nmf", "").replace("nm", "");
                        }
                        if (y2EVBYEBITDA.isEmpty())
                            y2EVBYEBITDA = "0";
                        if (y2EVBYEBITDA.contains("(") && y2EVBYEBITDA.contains(")") && !y2EVBYEBITDA.contains("-"))
                            y2EVBYEBITDA = "-" + y2EVBYEBITDA.replace("(", "").replace(")", "");
                        y2EVBYEBITDANumber = Double.parseDouble(y2EVBYEBITDA);
                    }

                    // Converting EVBYEBITDA to EVBYEBIT
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
                }
            } else {
                System.out.println("\\n\\n*************************\\n\\n Exception Valuation Page is missing - Valuation Page No. -" + valuationPageNumber);
            }

            //**********************************//

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
            System.out.println("Excecption in AnalystREcoExtractorKOTAK for report " + QUARTER + "_" + rdec.getTICKER() + "_KOTAK.pdf  " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            pdfReader.close();
        }
        return reportParameters;
    }


}


