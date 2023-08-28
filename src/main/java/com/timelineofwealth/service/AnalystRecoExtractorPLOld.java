package com.timelineofwealth.service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.timelineofwealth.dto.ReportDataExtractConfig;
import com.timelineofwealth.dto.ReportParameters;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalystRecoExtractorPLOld extends AnalystRecoExtractor {

    public ReportParameters getReportParameters(String reportFilePath, ReportDataExtractConfig rdec) {
        String BROKER = "PL";
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
            String dateString = "";
            long dateLastModified = new File(reportFilePath).lastModified();

            dateString = getReportDate(pageContent, dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
            // if date not found then set it to last modified date
            if (dateString.isEmpty()){
                dateString = getReportDate(null, dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
                System.out.print("Date set as last modified date: " + dateString + " ");
            } else
                System.out.print("Date : " + dateString + " ");

            // Extract MCAP
            String mcap = "";
            double mcapNumber = 0;
            mcapNumber = getMCapFromBillion(pageContent, rdec, 2, BROKER);
            if (mcapNumber > 0) {
                System.out.print("MCap : " + mcapNumber + " / ");
            } else {
                mcapNumber = 0;
                System.out.println("\n\n ********** Exception ********* \n\n Market Cap was not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Extract CMP
            String cmp = "";
            cmp = "" + getCMP(pageContent, rdec, 2, BROKER);
            if(!cmp.isEmpty())
                System.out.print("CMP : " + cmp + " / ");
            else
                System.out.print("\n\n ********** Exception ********* \n\n CMP not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Extract TP
            String tp = "";
            tp = getValueFromRE(pageContent, rdec.getTPPATTERN(),2, rdec).replace(",", "");
            if(!tp.isEmpty())
                System.out.print("TP : " + tp + " / ");
            else
                System.out.print("\n\n ********** Exception ********* \n\n TP not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);


            // Extract Ratings
            String rating = "";
            Pattern ratingPattern = Pattern.compile(RATINGPATTERN);
            Matcher m = ratingPattern.matcher(pageContent);
            if(m.find()) {
                rating = capitalizeFirstChar(m.group(1));
            }
            if(!rating.isEmpty())
                System.out.print("Rating : " + rating + " / ");
            else
                System.out.print("\n\n ********** Exception ********* \n\n Rating not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Extract Analyst Names
            String analystNames = "";
            analystNames = getAnalyst(analystNames, pageContent, rdec);
            if(!analystNames.isEmpty())
                System.out.println("Analysts Names : " + analystNames + " / ");
            else
                System.out.print("\n\n ********** Exception ********* \n\n Analyst Names were not set for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

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
            String[] lines = pageContent.split("\n");

            // Get Header Line No. from the Inc. Statement Page
            int headerLineNumber = -1;
            headerLineNumber = getLineNumberForMatchingPattern(lines, 0, HEADER_ROW_NAME,rdec, BROKER);
            if (headerLineNumber > 1)
                System.out.println("Inc. Statement Header Line : " + lines[headerLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Income Statement Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Revenue Line No. form the Inc. Statement Page
            int revenueLineNumber = -1;
            revenueLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, REVENUE_ROW_NAME,rdec, BROKER);
            if (revenueLineNumber > 1)
                System.out.println("Revenue Line : " + lines[revenueLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Revenue Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

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

            // Get OPM Lines Line No. form the Inc. Statement Page
            int ebitdaMarginLineNumber = -1;
            ebitdaMarginLineNumber = getLineNumberForMatchingPattern(lines, ebitdaLineNumber, EBITDAMARGIN_ROW_NAME,rdec, BROKER);
            if (ebitdaMarginLineNumber > 1)
                System.out.println("OPM Line : " + lines[ebitdaMarginLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n OPM Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get Corresponding Line
            String header = lines[headerLineNumber];
            String headerRowName = getValueFromRE(header,rdec.getHEADER_ROW_NAME(),1,rdec);
            int secondYEInex = header.indexOf(headerRowName, headerRowName.length());
            if(secondYEInex > 0) {
                header = header.substring(0, secondYEInex-1);
            }
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
                ebitdaMargin = ratioPageLines[ebitdaMarginLineNumber];
            } else {
                ebitdaMargin = lines[ebitdaMarginLineNumber];
            }

            // Convert rest of the line into Array
            String[] headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
            String[] revenueColumns = getDataColumnsForHeader(revenue, REVENUE_ROW_NAME);
            String[] ebitdaColumns = getDataColumnsForHeader(ebitda, EBITDA_ROW_NAME);
            String[] depreciationColumns = getDataColumnsForHeader(depreciation, DEPRECIATION_ROW_NAME);
            String[] ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);

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
            System.out.println("OPM Columns     " + ebitdaMarginColumns.length);

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
                y0EBITDAMargin = ebitdaMarginColumns[y0Column];

                y0RevenueNumber = Double.parseDouble(y0Revenue.replace(",", ""));
                y0EBITDANumber = Double.parseDouble(y0EBITDA.replace(",", "").replace("(", "-").replace(")", ""));
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
                y1EBITDAMargin = ebitdaMarginColumns[y1Column];

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
                y2EBITDAMargin = ebitdaMarginColumns[y2Column];

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
                System.out.println("Ratio Header Line : " + lines[headerLineNumber]);
            else
                System.out.println("\n\n ********** Exception ********* \n\n Ratio Header Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Get ROCE Line No. from the Inc. Ratio Page
            int roceLineNumber = -1;
            roceLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, ROCE_ROW_NAME,rdec, BROKER);
            if (roceLineNumber > 1)
                System.out.println("ROCE Line : " + lines[roceLineNumber].replace("NA", "0"));
            else{
                // in case ROCE is not on a new line
                String modifiedPattern = ROCE_ROW_NAME.replace("^", ".*");
                roceLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, modifiedPattern,rdec, BROKER);
                if(roceLineNumber > -1) {
                    ROCE_ROW_NAME = modifiedPattern;
                    System.out.println("ROCE Line : " + lines[roceLineNumber].replace("NA", "0"));
                } else
                    System.out.println("\n\n ********** Exception ********* \n\n ROCE Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Get EVBYEBITDA Line No. from the Inc. Ratio Page
            int evbyebitdaLineNumber = -1;
            evbyebitdaLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, EVBYEBITDA_ROW_NAME,rdec, BROKER);
            if (evbyebitdaLineNumber > 1)
                System.out.println("EVBYEBITDA Line : " + lines[evbyebitdaLineNumber].replace("NA", "0"));
            else {
                // in case EV/EBITADA is not on a new line
                String modifiedPattern = EVBYEBITDA_ROW_NAME.replace("^", ".*");
                evbyebitdaLineNumber = getLineNumberForMatchingPattern(lines, headerLineNumber, modifiedPattern,rdec, BROKER);
                if(evbyebitdaLineNumber > -1) {
                    EVBYEBITDA_ROW_NAME = modifiedPattern;
                    System.out.println("EVBYEBITDA Line : " + lines[evbyebitdaLineNumber].replace("NA", "0"));
                } else
                    System.out.println("\n\n ********** Exception ********* \n\n EVBYEBITDA Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }


            header = lines[headerLineNumber];
            //handle case where there are two headers on one line
            headerRowName = getValueFromRE(header,rdec.getHEADER_ROW_NAME(),1,rdec);
            secondYEInex = header.indexOf(headerRowName, headerRowName.length());
            if(secondYEInex > 0) {
                header = header.substring(secondYEInex);
            }
            String roce = lines[roceLineNumber].replace("NA", "0");
            String evbyebitda = lines[evbyebitdaLineNumber].replace("NA", "0");

            headerColumns = getDataColumnsForHeader(header, HEADER_ROW_NAME);
            String[] roceColumns = getDataColumnsForHeader(roce, ROCE_ROW_NAME);
            String[] evbyebitdaColumns = getDataColumnsForHeader(evbyebitda, EVBYEBITDA_ROW_NAME);

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
                if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length) {
                    System.out.println("Header mismatch for " +  QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    System.out.print(" Header Length " +  headerColumns.length);
                    System.out.print(" roce Length " +  roceColumns.length);
                    System.out.print(" evbyebitda Length " +  evbyebitdaColumns.length);

                    y0ROCE = roceColumns[y0Column-(headerColumns.length-roceColumns.length)].replace("(", "-").replace(")", "");
                } else {
                    y0ROCE = roceColumns[y0Column].replace("(", "-").replace(")", "");
                }
                if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                    y0EVBYEBITDA = evbyebitdaColumns[y0Column-(headerColumns.length-evbyebitdaColumns.length)];
                } else {
                    y0EVBYEBITDA = evbyebitdaColumns[y0Column];
                }

                y0EVBYEBITDANumber = Double.parseDouble(y0EVBYEBITDA);

            }
            if (y1Column == -1) {
                System.out.println(Y1 + " column not found in the header on " + RATIO_PAGE + " page");
            } else {
                if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length) {
                    y1ROCE = roceColumns[y1Column-(headerColumns.length-roceColumns.length)].replace("(", "-").replace(")", "");
                } else {
                    y1ROCE = roceColumns[y1Column].replace("(", "-").replace(")", "");
                }
                if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                    y1EVBYEBITDA = evbyebitdaColumns[y1Column-(headerColumns.length-evbyebitdaColumns.length)];
                } else {
                    y1EVBYEBITDA = evbyebitdaColumns[y1Column];
                }

                y1EVBYEBITDANumber = Double.parseDouble(y1EVBYEBITDA);
            }
            if (y2Column == -1) {
                System.out.println(Y2 + " column not found in the header on " + RATIO_PAGE + " page");
            } else {
                if (headerColumns.length != roceColumns.length && headerColumns.length > roceColumns.length) {
                    y2ROCE = roceColumns[y2Column-(headerColumns.length-roceColumns.length)].replace("(", "-").replace(")", "");
                } else {
                    y2ROCE = roceColumns[y2Column].replace("(", "-").replace(")", "");
                }
                if (headerColumns.length != evbyebitdaColumns.length && headerColumns.length > evbyebitdaColumns.length) {
                    y2EVBYEBITDA = evbyebitdaColumns[y2Column-(headerColumns.length-evbyebitdaColumns.length)];
                } else {
                    y2EVBYEBITDA = evbyebitdaColumns[y2Column];
                }
                y2EVBYEBITDANumber = Double.parseDouble(y2EVBYEBITDA.replace("(", "-").replace(")", ""));
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
            /*outPutString = outPutString + "\t" + y0RevenueNumber + "\t" + y1RevenueNumber + "\t" + y2RevenueNumber + "\t";
            outPutString = outPutString + "\t" + "=" + y0EBITDANumber + "-" + y0DepreciationNumber + "\t" + "=" + y1EBITDANumber + "-" + y1DepreciationNumber + "\t" + "=" + y2EBITDANumber + "-" + y2DepreciationNumber + "\t";
            outPutString = outPutString + "\t" + y0EBITDAMargin + "%\t" + y1EBITDAMargin + "%\t" + y2EBITDAMargin + "%";
            outPutString = outPutString + "\t" + y0ROCE + "%\t" + y1ROCE + "%\t" + y2ROCE + "%";
            outPutString = outPutString + "\t" + y0EVBYEBITNumber + "\t" + y1EVBYEBITNumber + "\t" + y2EVBYEBITNumber;
            outPutString = outPutString + "\t" + analystNames;*/

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
                reportParameters.setY0ROCE(new BigDecimal(0));
            if(!y1ROCE.isEmpty())
                reportParameters.setY1ROCE(new BigDecimal(Double.parseDouble(y1ROCE)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY1ROCE(new BigDecimal(0));
            if(!y2ROCE.isEmpty())
                reportParameters.setY2ROCE(new BigDecimal(Double.parseDouble(y2ROCE)/100).setScale(4, RoundingMode.HALF_UP));
            else
                reportParameters.setY2ROCE(new BigDecimal(0));
            reportParameters.setY0EVBYEBIT(new BigDecimal(y0EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setY1EVBYEBIT(new BigDecimal(y1EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setY2EVBYEBIT(new BigDecimal(y2EVBYEBITNumber).setScale(2, RoundingMode.HALF_UP));
            reportParameters.setAnalystsNames(analystNames);

            System.out.println("\n\nExtrated data for ticker " + rdec.getTICKER() + " : " + outPutString);

        } catch (IOException e) {
            System.out.println("Excecption in AnalystREcoExtractorMOSL for report " + QUARTER + "_" + rdec.getTICKER() + "_MOSL.pdf  " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            pdfReader.close();
        }
        return reportParameters;
    }

}
