package com.timelineofwealth.service;


import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.timelineofwealth.dto.ReportDataExtractConfig;
import com.timelineofwealth.dto.ReportParameters;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AnalystRecoExtractorAXIS extends AnalystRecoExtractor {

    protected String BROKER = "AXIS";

    protected String DATEPATTERN = "\\d{1,2}\\s*+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s*+\\d{2,4}";
    protected String DATEFORMAT = "dd MMMMM yyyy";

    public ReportParameters getReportParameters(String reportFilePath, ReportDataExtractConfig rdec, boolean isFinancialReport) {

        try {
            //Set Constants
            setConstants(rdec);
            reportParameters.setBroker(BROKER);

            // Open PDF document
            pdfReader = new PdfReader(reportFilePath);

            loadPages(pdfReader, rdec, isFinancialReport);
            setReportDate(reportFilePath, rdec);
            setMarketCap(rdec);
            setCMP(rdec);
            setRatings(rdec);
            setTargetPrice(rdec);
            setAnalystNames(rdec);

            setHeaderColumns(rdec, isFinancialReport);
            setRevenue(rdec, isFinancialReport);
            setProfit(rdec);
            if(!isFinancialReport)
                setProfitForNonFinancials(rdec);
            else {
                reportParameters.setY0EBIT(""+reportParameters.getY0PAT());
                reportParameters.setY1EBIT(""+reportParameters.getY1PAT());
                reportParameters.setY2EBIT(""+reportParameters.getY2PAT());
                reportParameters.setY3EBIT(""+reportParameters.getY3PAT());
            }
            setEPS(rdec);

            setOPMOrNIM(rdec, isFinancialReport);
            setROCEOrROE(rdec, isFinancialReport);
            setEVBYEBITRatio(rdec, isFinancialReport);

            if(isFinancialReport) {
                if(AUM_ROW_NAME != null && !AUM_ROW_NAME.isEmpty())
                    setAUM(rdec);
                if(CREDITCOSTS_ROW_NAME != null && !CREDITCOSTS_ROW_NAME.isEmpty())
                    setCreditCost(rdec);
                if(GNPA_ROW_NAME != null && !GNPA_ROW_NAME.isEmpty())
                    setGNPA(rdec);
                if(NNPA_ROW_NAME != null && !NNPA_ROW_NAME.isEmpty())
                    setNNPA(rdec);
            }

        } catch (IOException e) {
            System.out.println("########## Excecption in AnalystREcoExtractorAMBIT for report " + QUARTER + "_" + rdec.getTICKER() + "_AMBIT.pdf  " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            pdfReader.close();
        }
        return reportParameters;
    }

    protected void loadPages(PdfReader pdfReader, ReportDataExtractConfig rdec, boolean isFinancialReport){
        //Get the number of pages in pdf.
        int noOfPages = pdfReader.getNumberOfPages();

        try {
            // Load Reco Page
            pageContentReco = PdfTextExtractor.getTextFromPage(pdfReader, 1);
            linesRecoPage = pageContentReco.split("\n");

            // Load Income Statement Page
            int incomeStatementPageNo = -1;
            incomeStatementPageNo = getPageNumberForMatchingPattern(pdfReader, 2, noOfPages, INCOME_STATEMENT_PAGE, rdec, BROKER);

            if (incomeStatementPageNo > 1) {
                System.out.print("Inc. Statement Page No. : " + incomeStatementPageNo + " / ");
                pageContentIncomeStmt = PdfTextExtractor.getTextFromPage(pdfReader, incomeStatementPageNo);
                linesIncomeStmt = pageContentIncomeStmt.split("\n");
            } else
                System.out.println("########## Income Statement Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Load Margin Page
            int marginPageNo = -1;
            marginPageNo = getPageNumberForMatchingPattern(pdfReader, incomeStatementPageNo, noOfPages, MARGIN_PAGE, rdec, BROKER);
            if (marginPageNo > 1) {
                System.out.print("Margin Page No. : " + marginPageNo + " / ");
                pageContentMargin = PdfTextExtractor.getTextFromPage(pdfReader, marginPageNo);
                linesMargin = pageContentMargin.split("\n");
            } else
                System.out.println("##########Margin Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

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
            valuationPageNo = getPageNumberForMatchingPattern(pdfReader,  incomeStatementPageNo, noOfPages, VALUATION_PAGE, rdec, BROKER);
            if (valuationPageNo > 0) {
                System.out.println("Valuation Page No. : " + valuationPageNo);
                valuationPageNumber = new Integer(valuationPageNo);
                pageContentValuation = PdfTextExtractor.getTextFromPage(pdfReader, valuationPageNo);
                linesValuation = pageContentValuation.split("\n");
            } else
                System.out.println("########## Valuation Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

            // Find AUM Page No.
            if(isFinancialReport && AUM_PAGE != null && !AUM_PAGE.isEmpty()) {
                int aumPageNo = -1;
                aumPageNo = getPageNumberForMatchingPattern(pdfReader, incomeStatementPageNo, noOfPages, AUM_PAGE, rdec, BROKER);
                if (aumPageNo > 1) {
                    System.out.print("AUM Page No. : " + aumPageNo + " / ");
                    pageContentAUM = PdfTextExtractor.getTextFromPage(pdfReader, aumPageNo);
                    linesAUM = pageContentAUM.split("\n");
                } else
                    System.out.println("########## AUM Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Find Credit Cost Page No.
            if(isFinancialReport && CREDITCOSTS_PAGE != null && !CREDITCOSTS_PAGE.isEmpty()) {
                int creditCostPageNo = -1;
                creditCostPageNo = getPageNumberForMatchingPattern(pdfReader, incomeStatementPageNo, noOfPages, CREDITCOSTS_PAGE, rdec, BROKER);
                if (creditCostPageNo > 1) {
                    System.out.print("Credit Cost Page No. : " + creditCostPageNo + " / ");
                    pageContentCreditCost = PdfTextExtractor.getTextFromPage(pdfReader, creditCostPageNo);
                    linesCreditCost = pageContentCreditCost.split("\n");
                } else
                    System.out.println("########## Credit Cost Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }

            // Find NPA Page No.
            int npaPageNo = -1;
            if(isFinancialReport && NPA_PAGE != null && !NPA_PAGE.isEmpty()) {
                npaPageNo = getPageNumberForMatchingPattern(pdfReader, incomeStatementPageNo, noOfPages, NPA_PAGE, rdec, BROKER);
                if (npaPageNo > 1) {
                    System.out.println("NPA Page No. : " + npaPageNo + " / ");
                    pageContentNPA = PdfTextExtractor.getTextFromPage(pdfReader, npaPageNo);
                    linesNPA = pageContentNPA.split("\n");
                } else
                    System.out.println("########## NPA Page not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (IOException e){
            System.out.println("########## Exception in loadPages " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    private String normalizeDate(String line) {
        if (line == null) return null;
        return line.replaceAll("(\\d+)(st|nd|rd|th)", "$1")  // remove ordinal suffix
                .replace(",", "")                        // remove comma
                .trim();
    }

    public void setReportDate(String reportFilePath, ReportDataExtractConfig rdec){
        // Extract report date
        int lineNumber = 0;
        try {
            long dateLastModified = new File(reportFilePath).lastModified();
            String dateString_1 = "";

            while (lineNumber < linesRecoPage.length && dateString.isEmpty()) {
                String cleanedLine = normalizeDate(linesRecoPage[lineNumber]);
                dateString = getReportDate(cleanedLine, dateLastModified, DATEPATTERN, DATEFORMAT, rdec, BROKER);
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
        for (int i = 0; i < linesRecoPage.length; i++) {
            if(!isMCapLineFound && linesRecoPage[i].contains(MCAP)) {
                mcapLineNumber = i;
                isMCapLineFound = true;
            }
            if(isMCapLineFound)
                break;
        }
        try {
            if (mcapLineNumber != -1) {
                mcapNumber = getMCapFromBillion(linesRecoPage[mcapLineNumber], rdec, 1, BROKER, true);
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
        for (int i = 0; i < linesRecoPage.length; i++) {
            if (!isCMPLineFound && linesRecoPage[i].contains(CMP)){
                cmpLineNumber = i;
                isCMPLineFound = true;
            }
            if(isCMPLineFound)
                break;
        }
        try {
            if (cmpLineNumber != -1) {
                cmp = "" + getCMP(linesRecoPage[cmpLineNumber], rdec, 1, BROKER);
            } else {
                cmp = "0";
                System.out.println("\n########## CMP line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            cmp = "0";
            System.out.println("\n########## Exception in setting CMP, setting to 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
        reportParameters.setPrice(new BigDecimal(cmp.replaceAll(",", "")).setScale(0, RoundingMode.HALF_UP) );
        System.out.print("CMP : " + cmp + " ");
    }

    protected void setTargetPrice(ReportDataExtractConfig rdec){
        int targetPriceLineNumber = -1;
        boolean isTPLineFound = false;
        for (int i = 0; i < linesRecoPage.length; i++) {
            if (!isTPLineFound && linesRecoPage[i].contains(TP)){
                targetPriceLineNumber = i;
                isTPLineFound = true;
            }
            if(isTPLineFound)
                break;
        }
        try {
            // Extract Target Price
            if (targetPriceLineNumber != -1) {
                targetPrice = "0";
                for (int i = targetPriceLineNumber + 1;
                     i < targetPriceLineNumber + 10 && targetPrice.equals("0");
                     i++) {

                    String line = linesRecoPage[i].trim();
                    if (line.isEmpty()) continue;

                    // Skip lines that contain month names (date lines)
                    if (line.matches(".*(January|February|March|April|May|June|July|August|September|October|November|December).*"))
                        continue;

                    // Skip lines that contain alphabets (mixed text)
                    if (line.matches(".*[A-Za-z].*"))
                        continue;

                    // Now extract pure numeric values (with commas)
                    Matcher m = Pattern.compile("^(\\d[\\d,]*)$").matcher(line);
                    if (m.find()) {
                        targetPrice = m.group(1).replaceAll(",", "");
                        break;
                    }
                }
            } else {
                targetPrice = "0";
                System.out.println("\n########## Target Price line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            targetPrice = "0";
            System.out.println("\n########## Exception in setting Target Price, setting to 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
        reportParameters.setTarget(new BigDecimal(targetPrice.replaceAll(",", "")).setScale(0, RoundingMode.HALF_UP));
        System.out.print("Target Price : " + targetPrice + " ");
    }

    protected void setRatings(ReportDataExtractConfig rdec){
        boolean isRatingsFound = false;
        Pattern ratingPattern = Pattern.compile(RATINGPATTERN);
        for (int i = 0; i < linesRecoPage.length; i++) {
            try {
                Matcher m = ratingPattern.matcher(linesRecoPage[i].trim());
                if(!isRatingsFound && m.find()) {
                    rating = capitalizeFirstChar(m.group(1));
                    isRatingsFound = true;
                }
            } catch (Exception e) {
                System.out.println("\n########## Exception in setting Ratings for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                rating = "";
            }
            if(isRatingsFound)
                break;
        }
        reportParameters.setRating(rating);
        System.out.print("Ratings : " + rating + " ");
    }

    protected void setAnalystNames(ReportDataExtractConfig rdec) {

        Set<String> names = new LinkedHashSet<>();

        // 1) Find all AXIS emails on the reco page
        Pattern emailPattern = Pattern.compile("([A-Za-z0-9._%+-]+)@axissecurities\\.in",
                Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(pageContentReco);

        while (emailMatcher.find()) {

            String localPart = emailMatcher.group(1);
            if (localPart == null || localPart.isEmpty()) continue;

            // 2) Derive base name from local-part
            String[] tokens = localPart.split("[._]");
            List<String> cleanTokens = new ArrayList<>();
            for (String t : tokens) {
                t = t.trim();
                if (t.isEmpty()) continue;
                cleanTokens.add(t);
            }
            if (cleanTokens.isEmpty()) continue;

            String firstToken = cleanTokens.get(0);
            String lastToken  = cleanTokens.get(cleanTokens.size() - 1);

            String baseName =
                    capitalizeFirstChar(firstToken) + " " +
                            capitalizeFirstChar(lastToken);

            // 3) Try to find richer display name (e.g., "Kuber Chauhan")
            String displayName = null;

            String baseNameRegex = Pattern.quote(baseName) +
                    "(?:\\s*,?\\s*[A-Z]{2,5})?"; // allow CFA/FRM if present
            Pattern displayNamePattern = Pattern.compile(baseNameRegex);

            Matcher displayMatcher = displayNamePattern.matcher(pageContentReco);
            if (displayMatcher.find()) {
                displayName = displayMatcher.group().trim();
            }

            // 4) Fallback
            if (displayName == null || displayName.isEmpty()) {
                displayName = baseName;
            }

            // 5) Final cleaning
            displayName = displayName
                    .replaceAll("(?i)research analyst[s]?", "")
                    .replaceAll("(?i)research associate[s]?", "")
                    .trim();

            if (displayName.length() < 3) continue;
            if (!displayName.contains(" ")) continue;
            if (displayName.matches("^[A-Z]{1,4}$")) continue;
            if (displayName.matches("^[A-Z]{1,4}\\)$")) continue;

            names.add(displayName);
        }

        analystNames = String.join("; ", names);
        reportParameters.setAnalystsNames(analystNames);

        System.out.println("Analysts Names : " + analystNames);
    }

    protected void setHeaderColumns(ReportDataExtractConfig rdec, boolean isFinancialReport){
        // Get Header Line No. from the Inc. Stmt. page
        int headerLineNo = -1;
        try {
            headerLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, HEADER_ROW_NAME,rdec, BROKER);
            if(headerLineNo < 0) {
                headerLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, HEADER_ROW_NAME,rdec, BROKER);
            }
            if (headerLineNo > 1) {
                headerIncomeStmt = linesIncomeStmt[headerLineNo];
                headerColumnsIncomeStmt = getDataColumnsForHeader(headerIncomeStmt, HEADER_ROW_NAME);
                headerIncomeStmtLineNumber = new Integer(headerLineNo);
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
            headerLineNo = getLineNumberForMatchingPattern(linesRatio, 0, RATIO_HEADER_ROW_NAME, rdec, BROKER);
            if(headerLineNo < 0) {
                headerLineNo = getLineNumberForMatchingPattern(linesRatio, 0, RATIO_HEADER_ROW_NAME, rdec, BROKER);
            }
            if (headerLineNo > 1) {
                headerRatio = linesRatio[headerLineNo];
                headerColumnsRatio = getDataColumnsForHeader(headerRatio, RATIO_HEADER_ROW_NAME);
                headerRatioLineNumber = new Integer(headerLineNo);
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

        // Get Header Line No. from the Margin Page
        headerLineNo = -1;
        try {
            headerLineNo = getLineNumberForMatchingPattern(linesMargin, 0, MARGIN_HEADER_ROW_NAME,rdec, BROKER);
            if(headerLineNo < 0) {
                headerLineNo = getLineNumberForMatchingPattern(linesMargin, 0, MARGIN_HEADER_ROW_NAME,rdec, BROKER);
            }
            if (headerLineNo > 1) {
                headerMargin = linesMargin[headerLineNo];
                headerColumnsMargin = getDataColumnsForHeader(headerMargin, MARGIN_HEADER_ROW_NAME);
                headerMarginLineNumber = new Integer(headerLineNo);
                System.out.println("Margin Header Count : " + headerColumnsMargin.length + " / Line : " + linesMargin[headerLineNo]);
            } else {
                // in case header is not available fetch the header from income statement
                headerMargin = headerRatio;
                headerColumnsMargin = headerColumnsRatio;
                headerMarginLineNumber = null;
                System.out.println("$$$$$$$$$$ Setting Margin Header same as Ratio Header ");
            }
        } catch (Exception e) {
            System.out.println("########## Exception in setting Margin Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }

        // Get Header Line No. from the Inc. Valuation Page
        headerLineNo = -1;
        try {
            headerLineNo = getLineNumberForMatchingPattern(linesValuation, 0, VALUATION_HEADER_ROW_NAME, rdec, true, BROKER);
            if(headerLineNo < 0) {
                headerLineNo = getLineNumberForMatchingPattern(linesValuation, 0, VALUATION_HEADER_ROW_NAME, rdec, BROKER);
            }

            if (headerLineNo > 1) {
                headerValuation = linesValuation[headerLineNo];
                headerColumnsValuation = getDataColumnsForHeader(headerValuation, VALUATION_HEADER_ROW_NAME
                );
                headerValuationLineNumber = new Integer(headerLineNo);
                System.out.println("Valuation Header Count : " + headerColumnsValuation.length + " / Line : " + linesValuation[headerLineNo]);
            } else {
                // in case header is not avialble fetch the header from income statement
                headerValuation = headerRatio;
                headerColumnsValuation = headerColumnsRatio;
                headerValuationLineNumber = null;
                System.out.println("$$$$$$$$$$ Setting Valuation Header same as Ratio Header ");
            }

        } catch (Exception e){
            System.out.println("########## Exception in setting Valuation Page Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }

        // Get Header Line No. for the first page
        headerLineNo = -1;
        try {
            headerLineNo = getLineNumberForMatchingPattern(linesRecoPage, 1, "(?i).*((Forecasts\\/Valuations))",rdec, true, BROKER);
            if (headerLineNo > 1) {
                headerRecoPage = linesRecoPage[headerLineNo].replace("`", "");
                headerColumnsRecoPage = getDataColumnsForHeader(headerRecoPage, "(?i).*((Forecasts\\/Valuations))");
                headerFirstPageLineNumber = new Integer(headerLineNo);
                System.out.println("Reco Page Header Count : " + headerColumnsIncomeStmt.length + " / Line : " + linesRecoPage[headerLineNo]);
            } else {
                System.out.println("$$$$$$$$$$ Reco Page Header Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            System.out.println("########## Exception in setting Reco Page Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }

        // Get Header Line No. for AUM Page
        if(isFinancialReport && AUM_PAGE != null && !AUM_PAGE.isEmpty()) {
            headerLineNo = -1;
            try {
                headerLineNo = getLineNumberForMatchingPattern(linesAUM, 0, AUM_HEADER_ROW_NAME, rdec, true, BROKER);
                if(headerLineNo < 0) {
                    headerLineNo = getLineNumberForMatchingPattern(linesAUM, 0, AUM_HEADER_ROW_NAME, rdec, BROKER);
                }

                if (headerLineNo > 1) {
                    headerAUM = linesAUM[headerLineNo];
                    headerColumnsAUM = getDataColumnsForHeader(headerAUM, AUM_HEADER_ROW_NAME);
                    headerAUMLineNumber = new Integer(headerLineNo);
                    System.out.println("AUM Header Count : " + headerColumnsAUM.length + " / Line : " + linesAUM[headerLineNo]);
                } else {
                    // in case header is not avialble fetch the header from income statement
                    headerAUM = headerIncomeStmt;
                    headerColumnsAUM = headerColumnsIncomeStmt;
                    headerAUMLineNumber = null;
                    System.out.println("$$$$$$$$$$ Setting AUM Header same as Inc. Stmt. Header ");
                }

            } catch (Exception e) {
                System.out.println("########## Exception in setting AUM Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        }

        // Get Header Line No. for CreditCost Page
        if(isFinancialReport && CREDITCOSTS_PAGE != null && !CREDITCOSTS_PAGE.isEmpty()) {
            headerLineNo = -1;
            try {
                headerLineNo = getLineNumberForMatchingPattern(linesCreditCost, 0, CREDITCOSTS_HEADER_ROW_NAME, rdec, true, BROKER);
                if(headerLineNo < 0) {
                    headerLineNo = getLineNumberForMatchingPattern(linesCreditCost, 0, CREDITCOSTS_HEADER_ROW_NAME, rdec, BROKER);
                }
                if (headerLineNo > 1) {
                    headerCreditCosts = linesCreditCost[headerLineNo];
                    headerColumnsCreditCosts = getDataColumnsForHeader(headerCreditCosts, CREDITCOSTS_HEADER_ROW_NAME);
                    headerCreditCostLineNumber = new Integer(headerLineNo);
                    System.out.println("CreditCost Header Count : " + headerColumnsCreditCosts.length + " / Line : " + linesCreditCost[headerLineNo]);
                } else {
                    // in case header is not avialble fetch the header from income statement
                    headerCreditCosts = headerRatio;
                    headerColumnsCreditCosts = headerColumnsRatio;
                    headerCreditCostLineNumber = null;
                    System.out.println("$$$$$$$$$$ Setting Credit Cost Header same as Ratio Header ");
                }
            } catch (Exception e) {
                System.out.println("########## Exception in setting CreditCost Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        }

        // Get Header Line No. for NPA Page
        if(isFinancialReport && NPA_PAGE != null && !NPA_PAGE.isEmpty()) {
            headerLineNo = -1;
            try {
                headerLineNo = getLineNumberForMatchingPattern(linesNPA, 0, NPA_HEADER_ROW_NAME, rdec, true, BROKER);
                if(headerLineNo < 0) {
                    headerLineNo = getLineNumberForMatchingPattern(linesNPA, 0, NPA_HEADER_ROW_NAME, rdec, BROKER);
                }
                if (headerLineNo > 1) {
                    headerNPA = linesCreditCost[headerLineNo];
                    headerColumnsNPA = getDataColumnsForHeader(headerNPA, NPA_HEADER_ROW_NAME);
                    headerNPALineNumber = new Integer(headerLineNo);
                    System.out.println("CreditCost Header Count : " + headerColumnsNPA.length + " / Line : " + linesNPA[headerLineNo]);
                } else {
                    // in case header is not avialble fetch the header from income statement
                    headerNPA = headerRatio;
                    headerColumnsNPA = headerColumnsRatio;
                    headerNPALineNumber = null;
                    System.out.println("$$$$$$$$$$ Setting NPA Header same as Ratio Header ");
                }

            } catch (Exception e) {
                System.out.println("########## Exception in setting NPA Header for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        }
    }

    protected void setRevenue(ReportDataExtractConfig rdec, boolean isFinancialReport){
        int headerLineNo = -1;
        int revenueLineNo = -1;
        int y0Column = -1, y1Column = -1, y2Column = -1, y3Column = -1;
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
                revenueColumns = getDataColumnsForHeader(revenue, REVENUE_ROW_NAME);

                if(revenueColumns != null && revenueColumns.length != 0 ) {
                    if(revenueColumns.length == headerColumnsIncomeStmt.length) {
                        // Set million or billion flag
//                        MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find()? "B" : "M";
                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                        y3Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y3);
                        System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y1Column);
                        System.out.print(" Y3 Index " + y3Column + "\n");
                        System.out.println("Revenue Columns : " + revenue + " Columns: " + Arrays.toString(revenueColumns));

                        String y0Revenue = "", y1Revenue = "", y2Revenue = "", y3Revenue = "";
                        double y0RevenueNumber = 0, y1RevenueNumber = 0, y2RevenueNumber = 0, y3RevenueNumber = 0;

                        if (y0Column >= 0) {
                            y0Revenue = revenueColumns[y0Column];
                            y0RevenueNumber = Double.parseDouble(y0Revenue.replaceAll(",", ""));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y0RevenueNumber = y0RevenueNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y0RevenueNumber = y0RevenueNumber * 100;
                            }*/
                            reportParameters.setY0Revenue(new BigDecimal(y0RevenueNumber).setScale(2, RoundingMode.HALF_UP));
                            y0ColumnNumberOnIncStmt = new Integer(y0Column);
                        } else {
                            reportParameters.setY0Revenue(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 Revenue = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            y1Revenue = revenueColumns[y1Column];
                            y1RevenueNumber = Double.parseDouble(y1Revenue.replaceAll(",", ""));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y1RevenueNumber = y1RevenueNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y1RevenueNumber = y1RevenueNumber * 100;
                            }*/
                            reportParameters.setY1Revenue(new BigDecimal(y1RevenueNumber).setScale(2, RoundingMode.HALF_UP));
                            y1ColumnNumberOnIncStmt = new Integer(y1Column);
                        } else {
                            reportParameters.setY1Revenue(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 Revenue = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            y2Revenue = revenueColumns[y2Column];
                            y2RevenueNumber = Double.parseDouble(y2Revenue.replaceAll(",", ""));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y2RevenueNumber = y2RevenueNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y2RevenueNumber = y2RevenueNumber * 100;
                            }*/
                            reportParameters.setY2Revenue(new BigDecimal(y2RevenueNumber).setScale(2, RoundingMode.HALF_UP));
                            y2ColumnNumberOnIncStmt = new Integer(y2Column);
                        } else {
                            reportParameters.setY2Revenue(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 Revenue = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y3Column >= 0) {
                            y3Revenue = revenueColumns[y3Column];
                            y3RevenueNumber = Double.parseDouble(y3Revenue.replace(",", ""));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y3RevenueNumber = y3RevenueNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y3RevenueNumber = y3RevenueNumber * 100;
                            }*/
                            reportParameters.setY3Revenue(new BigDecimal(y3RevenueNumber).setScale(2, RoundingMode.HALF_UP));
                            y3ColumnNumberOnIncStmt = new Integer(y3Column);
                        } else {
                            reportParameters.setY3Revenue(new BigDecimal("0"));
                            System.out.println("########## Y3 Column Index not found Setting Y3 Revenue = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0Revenue(new BigDecimal("0"));
                        reportParameters.setY1Revenue(new BigDecimal("0"));
                        reportParameters.setY2Revenue(new BigDecimal("0"));
                        reportParameters.setY3Revenue(new BigDecimal("0"));
                        System.out.println("########## Revenue Row Columns (" + revenueColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0Revenue(new BigDecimal("0"));
                    reportParameters.setY1Revenue(new BigDecimal("0"));
                    reportParameters.setY2Revenue(new BigDecimal("0"));
                    reportParameters.setY3Revenue(new BigDecimal("0"));
                    System.out.println("########## Revenue Row Header and Revenue Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            }
            else {
                revenueLineNumber = null;
                reportParameters.setY0Revenue(new BigDecimal("0"));
                reportParameters.setY1Revenue(new BigDecimal("0"));
                reportParameters.setY2Revenue(new BigDecimal("0"));
                reportParameters.setY3Revenue(new BigDecimal("0"));
                System.out.println("########## Revenue Line not found for for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e){
            reportParameters.setY0Revenue(new BigDecimal("0"));
            reportParameters.setY1Revenue(new BigDecimal("0"));
            reportParameters.setY2Revenue(new BigDecimal("0"));
            reportParameters.setY3Revenue(new BigDecimal("0"));
            System.out.println("########## Exception in setting Revenue for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setProfitForNonFinancials(ReportDataExtractConfig rdec){
        try {
            int revenueLineNo = -1;
            int depreciationLineNo = -1;
            boolean isToSetEBIT = false, isEBITDAPresent = false, canImpliedDepreciationBeFound = false;
            String ebitda = null, ebit = null, depreciation = null, ebitdaPattern = EBITDA_ROW_NAME;
            String[] ebitdaColumns = null, depreciationColumns = null, ebitColumns = null;
            int y0Column = -1, y1Column = -1, y2Column = -1, y3Column = -1;
            String y0EBITDA = "", y1EBITDA = "", y2EBITDA = "", y3EBITDA = "";
            String y0Depreciation = "", y1Depreciation = "", y2Depreciation = "", y3Depreciation = "";
            double y0EBITDANo = 0, y1EBITDANo = 0, y2EBITDANo = 0, y3EBITDANo = 0;
            double y0DepreciationNo = 0, y1DepreciationNo = 0, y2DepreciationNo = 0, y3DepreciationNo = 0;

            String y0EBIT = "", y1EBIT = "", y2EBIT = "", y3EBIT = "";
            double y0EBITNumber = 0, y1EBITNumber = 0, y2EBITNumber = 0, y3EBITNumber = 0;

            ebitdaPattern = ebitdaPattern.replace("|\\s*EBITDA \\(underlying\\) |\\s*EBITDA|\\s*Adj\\. EBITDA", "");

            if (revenueLineNumber != null)
                revenueLineNo = revenueLineNumber.intValue();
            // Get EBITDA Line No. form the Inc. Statement Page
            int ebitdaLineNo = -1;
            ebitdaLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, revenueLineNo, ebitdaPattern, rdec, BROKER);

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
                    ebitda = linesIncomeStmt[ebitdaLineNo].replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
                    ebitdaColumns = getDataColumnsForHeader(ebitda, ebitdaPattern);

                    if(ebitdaColumns == null || (ebitdaColumns != null && ebitdaColumns.length != headerColumnsIncomeStmt.length && ebitdaColumns.length == 0)){
                        if(!linesIncomeStmt[ebitdaLineNo - 1].trim().equals(""))
                            ebitda = linesIncomeStmt[ebitdaLineNo] + linesIncomeStmt[ebitdaLineNo - 1];
                        else if(!linesIncomeStmt[ebitdaLineNo + 1].trim().equals(""))
                            ebitda = linesIncomeStmt[ebitdaLineNo] + linesIncomeStmt[ebitdaLineNo + 1];

                        ebitdaColumns = getDataColumnsForHeader(ebitda, ebitdaPattern);
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
//                                    MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find() ? "B" : "M";

                                    if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null && y3ColumnNumberOnIncStmt != null) {
                                        y0Column = y0ColumnNumberOnIncStmt.intValue();
                                        y1Column = y1ColumnNumberOnIncStmt.intValue();
                                        y2Column = y2ColumnNumberOnIncStmt.intValue();
                                        y3Column = y3ColumnNumberOnIncStmt.intValue();

                                    } else {
                                        y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                        y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                        y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                                        y3Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y3);
                                    }
                                    System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                                    System.out.print(" Y1 Index " + y1Column);
                                    System.out.print(" Y2 Index " + y2Column);
                                    System.out.print(" Y3 Index " + y3Column + "\n");
                                    System.out.println("Profit Line : " + ebitda + " Depreciation: " + depreciation + " Columns: " + Arrays.toString(ebitdaColumns));

                                    if (y0Column >= 0) {
                                        y0EBITDA = ebitdaColumns[y0Column];
                                        y0Depreciation = depreciationColumns[y0Column];

                                        y0EBITDANo = Double.parseDouble(y0EBITDA.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        y0DepreciationNo = Math.abs(Double.parseDouble(y0Depreciation.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1")));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y0EBITDANo = y0EBITDANo / 10;
                                            y0DepreciationNo = y0DepreciationNo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y0EBITDANo = y0EBITDANo * 100;
                                            y0DepreciationNo = y0DepreciationNo * 100;
                                        }*/
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

                                        y1EBITDANo = Double.parseDouble(y1EBITDA.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        y1DepreciationNo = Math.abs(Double.parseDouble(y1Depreciation.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1")));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y1EBITDANo = y1EBITDANo / 10;
                                            y1DepreciationNo = y1DepreciationNo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y1EBITDANo = y1EBITDANo * 100;
                                            y1DepreciationNo = y1DepreciationNo * 100;
                                        }*/
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

                                        y2EBITDANo = Double.parseDouble(y2EBITDA.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        y2DepreciationNo = Math.abs(Double.parseDouble(y2Depreciation.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1")));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y2EBITDANo = y2EBITDANo / 10;
                                            y2DepreciationNo = y2DepreciationNo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y2EBITDANo = y2EBITDANo * 100;
                                            y2DepreciationNo = y2DepreciationNo * 100;
                                        }*/
                                        y2EBITDANumber = new BigDecimal(y2EBITDANo);
                                        y2DepreciationNumber = new BigDecimal(y2DepreciationNo);
                                        reportParameters.setY2EBIT(y2EBITDANo + "-" + y2DepreciationNo);
                                    } else {
                                        reportParameters.setY2EBIT(null);
                                        System.out.println("########## Y2 Column Index not found Setting Y2 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                    }
                                    if (y3Column >= 0) {
                                        y3EBITDA = ebitdaColumns[y3Column];
                                        y3Depreciation = depreciationColumns[y3Column];

                                        y3EBITDANo = Double.parseDouble(y3EBITDA.replaceAll(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        y3DepreciationNo = Double.parseDouble(y3Depreciation.replaceAll(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y3EBITDANo = y3EBITDANo / 10;
                                            y3DepreciationNo = y3DepreciationNo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y3EBITDANo = y3EBITDANo * 100;
                                            y3DepreciationNo = y3DepreciationNo * 100;
                                        }*/
                                        y3EBITDANumber = new BigDecimal(y3EBITDANo);
                                        y3DepreciationNumber = new BigDecimal(y3DepreciationNo);
                                        reportParameters.setY3EBIT(y3EBITDANo + "-" + y3DepreciationNo);
                                    } else {
                                        reportParameters.setY3EBIT(null);
                                        System.out.println("########## Y3 Column Index not found Setting Y3 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
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
                    System.out.println("########## Setting EBIT directly");
                    int ebitLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, ebitdaLineNo, "(?i)^((EBIT ))", rdec, BROKER);
                    if(ebitLineNo > 0) {
                        ebit = linesIncomeStmt[ebitLineNo];
                        ebitColumns = getDataColumnsForHeader(ebit, "(?i)^((EBIT ))");
                        if (ebitColumns != null && ebitColumns.length != 0) {
                            // evenif columns are not matching then call function getCorrectNumbers
                            if (ebitColumns.length != headerColumnsIncomeStmt.length) {
                                ebitColumns = getDataColumnsForHeader(ebit.replaceAll(",", "").trim(), "(?i)^((EBIT ))");
                                ebitColumns = getCorrectNumbers(ebitColumns);
                            }
                            if (ebitColumns.length == headerColumnsIncomeStmt.length) {
                                if(isEBITDAPresent == true){
                                    ebitda = linesIncomeStmt[ebitdaLineNo].replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
                                    ebitdaColumns = getDataColumnsForHeader(ebitda, ebitdaPattern, headerColumnsIncomeStmt.length);

                                    if (ebitdaColumns != null && ebitdaColumns.length != 0) {
                                        if(ebitdaColumns.length == headerColumnsIncomeStmt.length) {
                                            canImpliedDepreciationBeFound = true;
                                        }
                                    }
                                }

//                                MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find() ? "B" : "M";
                                //handling a case of VBL where headers are at two places
                                    /*if(revenueColumns.length == ebitdaColumns.length && revenueColumns.length < headerColumns.length) {
                                        headerColumns = getDataColumnsForHeader(header, "(?i)^((\\s*Key metrics/assumptions))");
                                    }*/
                                if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null && y3ColumnNumberOnIncStmt != null) {
                                    y0Column = y0ColumnNumberOnIncStmt.intValue();
                                    y1Column = y1ColumnNumberOnIncStmt.intValue();
                                    y2Column = y2ColumnNumberOnIncStmt.intValue();
                                    y3Column = y3ColumnNumberOnIncStmt.intValue();
                                } else {
                                    y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                                    y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                                    y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                                    y3Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y3);
                                }
                                System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                                System.out.print(" Y1 Index " + y1Column);
                                System.out.print(" Y2 Index " + y2Column);
                                System.out.print(" Y3 Index " + y3Column + "\n");
                                System.out.println("EBIT Line : " + ebit + " Columns: " + Arrays.toString(ebitColumns));

                                if (y0Column >= 0) {
                                    y0EBIT = ebitColumns[y0Column];
                                    y0EBITNumber = Double.parseDouble(y0EBIT.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                    /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                        y0EBITNumber = y0EBITNumber / 10;
                                    } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                        y0EBITNumber = y0EBITNumber * 100;
                                    }*/
                                    if (canImpliedDepreciationBeFound == true) {
                                        y0EBITDA = ebitdaColumns[y0Column];
                                        y0EBITDANo = Double.parseDouble(y0EBITDA.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y0EBITDANo = y0EBITDANo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y0EBITDANo = y0EBITDANo * 100;
                                        }*/
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
                                    y1EBITNumber = Double.parseDouble(y1EBIT.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                    /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                        y1EBITNumber = y1EBITNumber / 10;
                                    } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                        y1EBITNumber = y1EBITNumber * 100;
                                    }*/
                                    if (canImpliedDepreciationBeFound == true) {
                                        y1EBITDA = ebitdaColumns[y1Column];
                                        y1EBITDANo = Double.parseDouble(y1EBITDA.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y1EBITDANo = y1EBITDANo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y1EBITDANo = y1EBITDANo * 100;
                                        }*/
                                        y1DepreciationNo = y1EBITDANo - y1EBITNumber;
                                        reportParameters.setY1EBIT(y1EBITDANo + "-" + y1DepreciationNo);
                                    } else {
                                        reportParameters.setY1EBIT("" + y1EBITNumber);
                                    }
                                } else {
                                    reportParameters.setY1EBIT(null);
                                    System.out.println("########## Y1 Column Index not found Setting y1 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                                if (y2Column >= 0) {
                                    y2EBIT = ebitColumns[y2Column];
                                    y2EBITNumber = Double.parseDouble(y2EBIT.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                    /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                        y2EBITNumber = y2EBITNumber / 10;
                                    } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                        y2EBITNumber = y2EBITNumber * 100;
                                    }*/
                                    if (canImpliedDepreciationBeFound == true) {
                                        y2EBITDA = ebitdaColumns[y2Column];
                                        y2EBITDANo = Double.parseDouble(y2EBITDA.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y2EBITDANo = y2EBITDANo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y2EBITDANo = y2EBITDANo * 100;
                                        }*/
                                        y2DepreciationNo = y2EBITDANo - y2EBITNumber;
                                        reportParameters.setY2EBIT(y2EBITDANo + "-" + y2DepreciationNo);
                                    } else {
                                        reportParameters.setY2EBIT("" + y2EBITNumber);
                                    }
                                } else {
                                    reportParameters.setY2EBIT(null);
                                    System.out.println("########## Y2 Column Index not found Setting y2 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                                if (y3Column >= 0) {
                                    y3EBIT = ebitColumns[y3Column];
                                    y3EBITNumber = Double.parseDouble(y3EBIT.replaceAll(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                    if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                        y3EBITNumber = y3EBITNumber / 10;
                                    } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                        y3EBITNumber = y3EBITNumber * 100;
                                    }
                                    if (canImpliedDepreciationBeFound == true) {
                                        y3EBITDA = ebitdaColumns[y3Column];
                                        y3EBITDANo = Double.parseDouble(y3EBITDA.replaceAll(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                                        /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                            y3EBITDANo = y3EBITDANo / 10;
                                        } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                            y3EBITDANo = y3EBITDANo * 100;
                                        }*/
                                        y3DepreciationNo = y3EBITDANo - y3EBITNumber;
                                        reportParameters.setY3EBIT(y3EBITDANo + "-" + y3DepreciationNo);
                                    } else {
                                        reportParameters.setY3EBIT("" + y3EBITNumber);
                                    }
                                } else {
                                    reportParameters.setY3EBIT(null);
                                    System.out.println("########## Y3 Column Index not found Setting y3 EBIT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                }
                            } else {
                                reportParameters.setY0EBIT(null);
                                reportParameters.setY1EBIT(null);
                                reportParameters.setY2EBIT(null);
                                reportParameters.setY3EBIT(null);
                                System.out.println("########## EBIT Row Columns (" + ebitColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            }
                        } else {
                            reportParameters.setY0EBIT(null);
                            reportParameters.setY1EBIT(null);
                            reportParameters.setY2EBIT(null);
                            reportParameters.setY3EBIT(null);
                            System.out.println("########## EBIT Row Header and EBIT Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0EBIT(null);
                        reportParameters.setY1EBIT(null);
                        reportParameters.setY2EBIT(null);
                        reportParameters.setY3EBIT(null);
                        System.out.println("########## EBIT line also not present for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                }
            }

        } catch (Exception e) {
            reportParameters.setY0EBIT(null);
            reportParameters.setY1EBIT(null);
            reportParameters.setY2EBIT(null);
            reportParameters.setY3EBIT(null);
            System.out.println("########## Exception in setting EBIT for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setProfit(ReportDataExtractConfig rdec){
        try {
            int revenueLineNo = -1;
            String pat = null, patPattern = PAT_ROW_NAME;
            String[] patColumns = null;
            int y0Column = -1, y1Column = -1, y2Column = -1, y3Column = -1;
            String y0PAT = "", y1PAT = "", y2PAT = "", y3PAT = "";
            double y0PATNumber = 0, y1PATNumber = 0, y2PATNumber = 0, y3PATNumber = 0;

//            patPattern = patPattern.replace("|\\s*EBITDA \\(underlying\\) |\\s*EBITDA|\\s*Adj\\. EBITDA", "");

            if (revenueLineNumber != null)
                revenueLineNo = revenueLineNumber.intValue();
            // Get EBITDA Line No. form the Inc. Statement Page
            int patLineNo = -1;
            patLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, revenueLineNo, patPattern, rdec, BROKER);

            if(patLineNo > 0) {
                pat = linesIncomeStmt[patLineNo].replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
                patColumns = getDataColumnsForHeader(pat, patPattern);


                if(patColumns == null || (patColumns != null && patColumns.length != headerColumnsIncomeStmt.length && patColumns.length == 0)){
                    if(!linesIncomeStmt[patLineNo - 1].trim().equals(""))
                        pat = linesIncomeStmt[patLineNo] + linesIncomeStmt[patLineNo - 1];
                    else if(!linesIncomeStmt[patLineNo + 1].trim().equals(""))
                        pat = linesIncomeStmt[patLineNo] + linesIncomeStmt[patLineNo + 1];

                    patColumns = getDataColumnsForHeader(pat, patPattern);
                }

                if (patColumns != null && patColumns.length != 0) {
                    if(patColumns.length == headerColumnsIncomeStmt.length) {
                        // Set million or billion flag
//                        MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(MILLIONS_OR_BILLIONS).matcher(pageContentIncomeStmt).find()? "B" : "M";

                        if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null && y3ColumnNumberOnIncStmt != null) {
                            y0Column = y0ColumnNumberOnIncStmt.intValue();
                            y1Column = y1ColumnNumberOnIncStmt.intValue();
                            y2Column = y2ColumnNumberOnIncStmt.intValue();
                            y3Column = y3ColumnNumberOnIncStmt.intValue();
                        } else {
                            y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                            y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                            y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            y3Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y3);
                        }
                        System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column);
                        System.out.print(" Y3 Index " + y3Column + "\n");
                        System.out.println("PAT Line : " + pat + " Columns: " + Arrays.toString(patColumns));

                        if (y0Column >= 0) {
                            y0PAT = patColumns[y0Column];
                            y0PATNumber = Double.parseDouble(y0PAT.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y0PATNumber = y0PATNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y0PATNumber = y0PATNumber * 100;
                            }*/
                            reportParameters.setY0PAT(new BigDecimal(y0PATNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY0PAT(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 PAT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            y1PAT = patColumns[y1Column];

                            y1PATNumber = Double.parseDouble(y1PAT.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y1PATNumber = y1PATNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y1PATNumber = y1PATNumber * 100;
                            }*/
                            reportParameters.setY1PAT(new BigDecimal(y1PATNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY1PAT(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 PAT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            y2PAT = patColumns[y2Column];

                            y2PATNumber = Double.parseDouble(y2PAT.replaceAll(",", "").replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y2PATNumber = y2PATNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y2PATNumber = y2PATNumber * 100;
                            }*/
                            reportParameters.setY2PAT(new BigDecimal(y2PATNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY2PAT(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 PAT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y3Column >= 0) {
                            y3PAT = patColumns[y3Column];

                            y3PATNumber = Double.parseDouble(y3PAT.replaceAll(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            /*if (MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                y3PATNumber = y3PATNumber / 10;
                            } else if (MILLIONS_OR_BILLIONS_FLAG.equals("B")){
                                y3PATNumber = y3PATNumber * 100;
                            }*/
                            reportParameters.setY3PAT(new BigDecimal(y3PATNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY3PAT(new BigDecimal("0"));
                            System.out.println("########## Y3 Column Index not found Setting Y3 PAT = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0PAT(new BigDecimal("0"));
                        reportParameters.setY1PAT(new BigDecimal("0"));
                        reportParameters.setY2PAT(new BigDecimal("0"));
                        reportParameters.setY3PAT(new BigDecimal("0"));
                        System.out.println("########## Net Profit Row Columns (" + patColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0PAT(new BigDecimal("0"));
                    reportParameters.setY1PAT(new BigDecimal("0"));
                    reportParameters.setY2PAT(new BigDecimal("0"));
                    reportParameters.setY3PAT(new BigDecimal("0"));
                    System.out.println("########## PAT Row Header and PAT Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0PAT(new BigDecimal("0"));
                reportParameters.setY1PAT(new BigDecimal("0"));
                reportParameters.setY2PAT(new BigDecimal("0"));
                reportParameters.setY3PAT(new BigDecimal("0"));
                System.out.println("########## PAT line is not present for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0PAT(new BigDecimal("0"));
            reportParameters.setY1PAT(new BigDecimal("0"));
            reportParameters.setY2PAT(new BigDecimal("0"));
            reportParameters.setY3PAT(new BigDecimal("0"));
            System.out.println("########## Exception in setting EBIT for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setEPS(ReportDataExtractConfig rdec){
        try {
            String eps = null;
            String[] epsColumns = null;
            int y0Column = -1, y1Column = -1, y2Column = -1, y3Column = -1;
            String y0EPS = "", y1EPS = "", y2EPS = "", y3EPS = "";
            double y0EPSNumber = 0, y1EPSNumber = 0, y2EPSNumber = 0, y3EPSNumber = 0;
            boolean epsOnRatio = false;

            int epsLineNo = -1;
            epsLineNo = getLineNumberForMatchingPattern(linesIncomeStmt, 0, EPS_ROW_NAME, rdec, BROKER);
            if(epsLineNo < 0) {
                epsLineNo = getLineNumberForMatchingPattern(linesRatio, 0, EPS_ROW_NAME, rdec, BROKER);
                epsOnRatio = true;
            }

            if(epsLineNo > 0) {
                String line = "";
                if (!epsOnRatio)
                    line = linesIncomeStmt[epsLineNo];
                else
                    line = linesRatio[epsLineNo];

                // Replace only (number) patterns
                line = line.replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
                eps = line;
                epsColumns = getDataColumnsForHeader(eps, EPS_ROW_NAME);


                if(epsColumns == null || (epsColumns != null && epsColumns.length != headerColumnsIncomeStmt.length && epsColumns.length == 0)){
                    if(!linesIncomeStmt[epsLineNo - 1].trim().equals(""))
                        eps = linesIncomeStmt[epsLineNo] + linesIncomeStmt[epsLineNo - 1];
                    else if(!linesIncomeStmt[epsLineNo + 1].trim().equals(""))
                        eps = linesIncomeStmt[epsLineNo] + linesIncomeStmt[epsLineNo + 1];

                    epsColumns = getDataColumnsForHeader(eps, EPS_ROW_NAME);
                }

                if (epsColumns != null && epsColumns.length != 0) {
                    if(epsColumns.length == headerColumnsIncomeStmt.length || (epsOnRatio && headerColumnsIncomeStmt.length == headerColumnsRatio.length)) {
                        if(y0ColumnNumberOnIncStmt != null && y1ColumnNumberOnIncStmt != null && y2ColumnNumberOnIncStmt != null ) {
                            y0Column = y0ColumnNumberOnIncStmt.intValue();
                            y1Column = y1ColumnNumberOnIncStmt.intValue();
                            y2Column = y2ColumnNumberOnIncStmt.intValue();
                            y3Column = y3ColumnNumberOnIncStmt.intValue();
                        } else {
                            y0Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y0);
                            y1Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y1);
                            y2Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y2);
                            y3Column = getIndexOfTheYear(headerColumnsIncomeStmt, Y3);
                        }
                        System.out.print("Inc. Stmt. Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column);
                        System.out.print(" Y3 Index " + y3Column + "\n");
                        System.out.println("EPS Line : " + eps + " Columns: " + Arrays.toString(epsColumns));

                        if (y0Column >= 0) {
                            y0EPS = epsColumns[y0Column];
                            y0EPSNumber = Double.parseDouble(y0EPS.replace(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            reportParameters.setY0EPS(new BigDecimal(y0EPSNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY0EPS(new BigDecimal("0"));
                            System.out.println("########## Y0 Column Index not found Setting Y0 EPS = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y1Column >= 0) {
                            y1EPS = epsColumns[y1Column];
                            y1EPSNumber = Double.parseDouble(y1EPS.replace(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            reportParameters.setY1EPS(new BigDecimal(y1EPSNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY1EPS(new BigDecimal("0"));
                            System.out.println("########## Y1 Column Index not found Setting Y1 EPS = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y2Column >= 0) {
                            y2EPS = epsColumns[y2Column];
                            y2EPSNumber = Double.parseDouble(y2EPS.replace(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            reportParameters.setY2EPS(new BigDecimal(y2EPSNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY2EPS(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y2 EPS = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        if (y3Column >= 0) {
                            y3EPS = epsColumns[y3Column];
                            y3EPSNumber = Double.parseDouble(y3EPS.replace(",", "").replaceAll("\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1"));
                            reportParameters.setY3EPS(new BigDecimal(y3EPSNumber).setScale(2, RoundingMode.HALF_UP));
                        } else {
                            reportParameters.setY3EPS(new BigDecimal("0"));
                            System.out.println("########## Y2 Column Index not found Setting Y3 EPS = null for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                    } else {
                        reportParameters.setY0EPS(new BigDecimal("0"));
                        reportParameters.setY1EPS(new BigDecimal("0"));
                        reportParameters.setY2EPS(new BigDecimal("0"));
                        reportParameters.setY3EPS(new BigDecimal("0"));
                        System.out.println("########## EPS Columns (" + epsColumns.length + ") and Header Row Columns ("+ headerColumnsIncomeStmt.length + ") are not same for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0EPS(new BigDecimal("0"));
                    reportParameters.setY1EPS(new BigDecimal("0"));
                    reportParameters.setY2EPS(new BigDecimal("0"));
                    reportParameters.setY3EPS(new BigDecimal("0"));
                    System.out.println("########## EPS Row Header and EPS Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0EPS(new BigDecimal("0"));
                reportParameters.setY1EPS(new BigDecimal("0"));
                reportParameters.setY2EPS(new BigDecimal("0"));
                reportParameters.setY3EPS(new BigDecimal("0"));
                System.out.println("########## EPS line is not present for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0EPS(new BigDecimal("0"));
            reportParameters.setY1EPS(new BigDecimal("0"));
            reportParameters.setY2EPS(new BigDecimal("0"));
            reportParameters.setY3EPS(new BigDecimal("0"));
            System.out.println("########## Exception in setting EPS for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setOPMOrNIM(ReportDataExtractConfig rdec, boolean isFinancialReport){
        int ebitdaLineNo = -1, ebitdaMarginLineNo = -1;
        String ebitdaMargin = null;
        String[] ebitdaMarginColumns = null;
        String y0EBITDAMargin = "0", y1EBITDAMargin = "0", y2EBITDAMargin = "0", y3EBITDAMargin = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1, y3Column = -1;
//        boolean isMarginOnRatio = false;
        try {
            if (ebitdaLineNumber != null)
                ebitdaLineNo = ebitdaLineNumber.intValue();
            else
                ebitdaLineNo = revenueLineNumber.intValue();

            if(!isFinancialReport)
                ebitdaMarginLineNo = getLineNumberForMatchingPattern(linesMargin, ebitdaLineNo, EBITDAMARGIN_ROW_NAME, rdec, BROKER);
            else
                ebitdaMarginLineNo = getLineNumberForMatchingPattern(linesMargin, 0, EBITDAMARGIN_ROW_NAME, rdec, BROKER);

            if (ebitdaMarginLineNo > 0) {
                ebitdaMargin = linesMargin[ebitdaMarginLineNo].replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
            }

            if (ebitdaMargin != null) {
                ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);

                if (ebitdaMarginColumns.length != headerColumnsMargin.length && ebitdaMarginColumns.length == 0) {
                    if (!headerColumnsMargin[ebitdaMarginLineNo + 1].trim().equals(""))
                        ebitdaMargin = headerColumnsMargin[ebitdaMarginLineNo] + headerColumnsMargin[ebitdaMarginLineNo + 1];
                    else if (!headerColumnsMargin[ebitdaMarginLineNo - 1].trim().equals(""))
                        ebitdaMargin = headerColumnsMargin[ebitdaMarginLineNo] + headerColumnsMargin[ebitdaMarginLineNo - 1];

                    ebitdaMarginColumns = getDataColumnsForHeader(ebitdaMargin, EBITDAMARGIN_ROW_NAME);
                }

                // Find Y0, Y1 and Y2 Index position
                y0Column = getIndexOfTheYear(headerColumnsMargin, Y0);
                y1Column = getIndexOfTheYear(headerColumnsMargin, Y1);
                y2Column = getIndexOfTheYear(headerColumnsMargin, Y2);
                y3Column = getIndexOfTheYear(headerColumnsMargin, Y3);

                System.out.print("OPM Header : Y0 Index " + y0Column);
                System.out.print(" Y1 Index " + y1Column);
                System.out.print(" Y2 Index " + y2Column);
                System.out.print(" Y3 Index " + y3Column + "\n");
                System.out.println("Profit Margin Line : " + ebitdaMargin + " Columns: " + Arrays.toString(ebitdaMarginColumns));

                if (ebitdaMarginColumns.length != 0) {
                    if (y0Column >= 0) {
                        try {
                            if (headerColumnsMargin.length != ebitdaMarginColumns.length && headerColumnsMargin.length > ebitdaMarginColumns.length) {
                                System.out.println("########## Header mismatch for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                                System.out.print(" Header Length " + headerColumnsMargin.length);
                                System.out.print(" OPM/NIM Length " + ebitdaMarginColumns.length + "\n");
                                y0EBITDAMargin = ebitdaMarginColumns[y0Column - (headerColumnsMargin.length - ebitdaMarginColumns.length)].replace("%", "");
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
                            if (headerColumnsMargin.length != ebitdaMarginColumns.length && headerColumnsMargin.length > ebitdaMarginColumns.length) {
                                y1EBITDAMargin = ebitdaMarginColumns[y1Column - (headerColumnsMargin.length - ebitdaMarginColumns.length)].replace("%", "");
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
                            if (headerColumnsMargin.length != ebitdaMarginColumns.length && headerColumnsMargin.length > ebitdaMarginColumns.length) {
                                y2EBITDAMargin = ebitdaMarginColumns[y2Column - (headerColumnsMargin.length - ebitdaMarginColumns.length)].replace("%", "");
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
                    if (y3Column >= 0) {
                        try {
                            if (headerColumnsMargin.length != ebitdaMarginColumns.length && headerColumnsMargin.length > ebitdaMarginColumns.length) {
                                y3EBITDAMargin = ebitdaMarginColumns[y3Column - (headerColumnsMargin.length - ebitdaMarginColumns.length)].replace("%", "");
                            } else {
                                y3EBITDAMargin = ebitdaMarginColumns[y3Column].replace("%", "");
                            }
                        } catch (Exception e) {
                            y3EBITDAMargin = "0";
                            System.out.println("########## Exception in setting Y3 OPM/NIM for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        }
                        reportParameters.setY3OPM(new BigDecimal(Double.parseDouble(y3EBITDAMargin) / 100).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        reportParameters.setY3OPM(new BigDecimal("0"));
                        System.out.println("########## Y3 Column Index not found. Setting Y3 OPM/NIM = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                    }
                } else {
                    reportParameters.setY0OPM(new BigDecimal("0"));
                    reportParameters.setY1OPM(new BigDecimal("0"));
                    reportParameters.setY2OPM(new BigDecimal("0"));
                    reportParameters.setY3OPM(new BigDecimal("0"));
                    System.out.println("########## OPM/NIM Line Row Header and Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0OPM(new BigDecimal("0"));
                reportParameters.setY1OPM(new BigDecimal("0"));
                reportParameters.setY2OPM(new BigDecimal("0"));
                reportParameters.setY3OPM(new BigDecimal("0"));
                System.out.println("########## OPM/NIM Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
            }
        } catch (Exception e) {
            reportParameters.setY0OPM(new BigDecimal("0"));
            reportParameters.setY1OPM(new BigDecimal("0"));
            reportParameters.setY2OPM(new BigDecimal("0"));
            reportParameters.setY3OPM(new BigDecimal("0"));
            System.out.println("########## Exception in setting OPM/NIM for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setROCEOrROE(ReportDataExtractConfig rdec, boolean isFinancialReport){
        int roceLineNo = -1, roceLineNoSecond = -1, headerRatioLineNo = -1;
        String roce = null, rocePattern = ROCE_ROW_NAME;
        String[] roceColumns = null;
        String y0ROCE = "0", y1ROCE = "0", y2ROCE = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1;

        try {
            if (isFinancialReport)
                rocePattern = rocePattern.replaceAll("(?i)ROCE", "RoE");
            else
                rocePattern = rocePattern.replaceAll("(?i)ROAE", "ROCE").replaceAll("(?i)ROE", "ROCE");

            if (headerRatioLineNumber != null)
                headerRatioLineNo = headerRatioLineNumber.intValue();
            else
                headerRatioLineNo = 0;

            roceLineNo = getLineNumberForMatchingPattern(linesRatio, 0, rocePattern, rdec, BROKER);
            roceLineNoSecond = getLineNumberForMatchingPattern(linesRatio, roceLineNo + 1, rocePattern, rdec, BROKER);

            if (roceLineNo > 1 && roceLineNoSecond > 1 && linesRatio[roceLineNoSecond].toLowerCase().contains("post"))
                roceLineNo = roceLineNoSecond;

            if (roceLineNo > 1) {
                roce = linesRatio[roceLineNo].trim().replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
                roceColumns = getDataColumnsForHeader(roce, rocePattern);

                // Find Y0, Y1 and Y2 Index position
                if (y0ColumnNumberOnRatio != null && y1ColumnNumberOnRatio != null && y2ColumnNumberOnRatio != null) {
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
                System.out.println("ROCE/ROE Columns : " + roce + " Columns: " + Arrays.toString(roceColumns));

                if(roceColumns != null && roceColumns.length != 0) {
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
                    System.out.println("########## RoCE/RoE Line Row Header and Data not on the same line for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                }
            } else {
                reportParameters.setY0ROCE(new BigDecimal("0"));
                reportParameters.setY1ROCE(new BigDecimal("0"));
                reportParameters.setY2ROCE(new BigDecimal("0"));
                System.out.println("########## RoCE/RoE Line not found for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
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
        String evbyebitda = "", evbyebitdaPattern = EVBYEBITDA_ROW_NAME;
        String y0EVBYEBITDA = "", y1EVBYEBITDA = "", y2EVBYEBITDA = "";
        double y0EVBYEBITDANumber = 0, y1EVBYEBITDANumber = 0, y2EVBYEBITDANumber = 0;
        double y0EVBYEBITNumber = 0, y1EVBYEBITNumber = 0, y2EVBYEBITNumber = 0;
        String[] evbyebitdaColumns = null;
        int y0Column = -1, y1Column = -1, y2Column = -1;
        boolean isValuationOnRecoPage = false;
        try {

            if (isFinancialReport)
                evbyebitdaPattern = evbyebitdaPattern.replace("|\\s*EV\\/EBITDA \\(x\\)|\\s*EV\\/EBITDA", "");
            else
                evbyebitdaPattern = evbyebitdaPattern.replace("\\s*Consolidated P\\/B|\\s*P\\/BV \\(x\\)|\\s*P\\/BV|\\s*P\\/B Consol\\.|\\s*P\\/B|", "");


            if(valuationPageNumber != null && valuationPageNumber.intValue() == 1)
                isValuationOnRecoPage = true;

            if(!isValuationOnRecoPage)
                evbyebitdaLineNumber = getLineNumberForMatchingPattern(linesValuation, 0, evbyebitdaPattern, rdec, BROKER);
            else
                evbyebitdaLineNumber = getLineNumberForMatchingPattern(linesRecoPage, 0, evbyebitdaPattern, rdec, BROKER);

            if (evbyebitdaLineNumber >= 0) {
                if(!isValuationOnRecoPage)
                    evbyebitda = linesValuation[evbyebitdaLineNumber].replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
                else
                    evbyebitda = linesRecoPage[evbyebitdaLineNumber].replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");

                evbyebitdaColumns = getDataColumnsForHeader(evbyebitda, evbyebitdaPattern);

                if(evbyebitdaColumns != null) {
                    if ((!isValuationOnRecoPage && headerColumnsValuation.length != evbyebitdaColumns.length) || (isValuationOnRecoPage && headerColumnsRecoPage.length != evbyebitdaColumns.length)) {
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
                    if(!isValuationOnRecoPage) {
                        y0Column = getIndexOfTheYear(headerColumnsValuation, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsValuation, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsValuation, Y2);
                    } else {
                        y0Column = getIndexOfTheYear(headerColumnsRecoPage, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsRecoPage, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsRecoPage, Y2);
                    }
                    System.out.print("Valuation Header : Y0 Index " + y0Column);
                    System.out.print(" Y1 Index " + y1Column);
                    System.out.print(" Y2 Index " + y2Column + "\n");
                    System.out.println("Valuation Columns : " + evbyebitda + " Columns: " + Arrays.toString(evbyebitdaColumns));

                    if (y0Column >= 0) {
                        try {
                            if (!isValuationOnRecoPage && headerColumnsValuation.length != evbyebitdaColumns.length && headerColumnsValuation.length > evbyebitdaColumns.length) {
                                y0EVBYEBITDA = evbyebitdaColumns[y0Column - (headerColumnsValuation.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                            } else {
                                y0EVBYEBITDA = evbyebitdaColumns[y0Column].replace("nmf", "").replace("nm", "");
                            }
                            if (y0EVBYEBITDA.isEmpty())
                                y0EVBYEBITDA = "0";
                            if (y0EVBYEBITDA.contains("(") && y0EVBYEBITDA.contains(")") && !y0EVBYEBITDA.contains("-"))
                                y0EVBYEBITDA = "-" + y0EVBYEBITDA.replace("(", "").replace(")", "");

                            y0EVBYEBITDANumber = Double.parseDouble(y0EVBYEBITDA);
                            if(!isFinancialReport) {
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
                            System.out.println("########## Y0 Column Index not found Setting Y0 EV/EBITDA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY0EVBYEBIT(new BigDecimal("0"));
                        }
                    }
                    if (y1Column >= 0) {
                        try {
                            if (!isValuationOnRecoPage && headerColumnsValuation.length != evbyebitdaColumns.length && headerColumnsValuation.length > evbyebitdaColumns.length) {
                                y1EVBYEBITDA = evbyebitdaColumns[y1Column - (headerColumnsValuation.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                            } else {
                                y1EVBYEBITDA = evbyebitdaColumns[y1Column].replace("nmf", "").replace("nm", "");
                            }
                            if (y1EVBYEBITDA.isEmpty())
                                y1EVBYEBITDA = "0";
                            if (y1EVBYEBITDA.contains("(") && y1EVBYEBITDA.contains(")") && !y1EVBYEBITDA.contains("-"))
                                y1EVBYEBITDA = "-" + y1EVBYEBITDA.replace("(", "").replace(")", "");

                            y1EVBYEBITDANumber = Double.parseDouble(y1EVBYEBITDA);
                            if(!isFinancialReport) {
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
                            System.out.println("########## Y1 Column Index not found Setting Y1 EV/EBITDA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                            reportParameters.setY1EVBYEBIT(new BigDecimal("0"));
                        }
                    }
                    if (y2Column >= 0) {
                        try {
                            if (!isValuationOnRecoPage && headerColumnsValuation.length != evbyebitdaColumns.length && headerColumnsValuation.length > evbyebitdaColumns.length) {
                                y2EVBYEBITDA = evbyebitdaColumns[y2Column - (headerColumnsValuation.length - evbyebitdaColumns.length)].replace("nmf", "").replace("nm", "");
                            } else {
                                y2EVBYEBITDA = evbyebitdaColumns[y2Column].replace("nmf", "").replace("nm", "");
                            }
                            if (y2EVBYEBITDA.isEmpty())
                                y2EVBYEBITDA = "0";
                            if (y2EVBYEBITDA.contains("(") && y2EVBYEBITDA.contains(")") && !y2EVBYEBITDA.contains("-"))
                                y2EVBYEBITDA = "-" + y2EVBYEBITDA.replace("(", "").replace(")", "");

                            y2EVBYEBITDANumber = Double.parseDouble(y2EVBYEBITDA);
                            if(!isFinancialReport) {
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
                            System.out.println("########## Y2 Column Index not found Setting y2 EV/EBITDA = 0 for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
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
//        String MILLIONS_OR_BILLIONS_FLAG_OLD = MILLIONS_OR_BILLIONS_FLAG;

        try {
            // Get AUM Line No. form the Inc. Statement Page
            aumLineNo = getLineNumberForMatchingPattern(linesAUM, 0, AUM_ROW_NAME, rdec, BROKER);

            if (aumLineNo > 1) {
                aum = linesAUM[aumLineNo];
                aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);

                if (aumColumns != null && aumColumns.length != headerColumnsAUM.length && aumColumns.length == 0) {
                    if (!linesAUM[aumLineNo - 1].trim().equals(""))
                        aum = linesAUM[aumLineNo] + linesAUM[aumLineNo - 1];
                    else if (!linesAUM[aumLineNo + 1].trim().equals(""))
                        aum = linesAUM[aumLineNo] + linesAUM[aumLineNo + 1];

                    aumColumns = getDataColumnsForHeader(aum, AUM_ROW_NAME);
                }

//                AUM_MILLIONS_OR_BILLIONS_FLAG = Pattern.compile(AUM_MILLIONS_OR_BILLIONS).matcher(pageContentAUM).find()? "B" : "M";
                /*if (aum.contains("INR b"))
                    AUM_MILLIONS_OR_BILLIONS_FLAG = "B";
                if (aum.contains("INR m"))
                    AUM_MILLIONS_OR_BILLIONS_FLAG = "M";*/

                if(aumColumns != null && aumColumns.length != 0 ) {
                    if((aumColumns.length == headerColumnsAUM.length) || (aumColumns.length == headerColumnsAUM.length)) {
                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsAUM, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsAUM, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsAUM, Y2);

                        System.out.print("AUM Header : Y0 Index " + y0Column);
                        System.out.print(" Y1 Index " + y1Column);
                        System.out.print(" Y2 Index " + y2Column + "\n");
                        System.out.println("AUM Columns : " + Arrays.toString(aumColumns));

                        if (y0Column >= 0) {
                            try {
                                y0AUM = aumColumns[y0Column];
                                y0AUMNumber = Double.parseDouble(y0AUM.replaceAll(",", ""));
                                /*if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y0AUMNumber = y0AUMNumber / 10;
                                } else if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y0AUMNumber = y0AUMNumber * 100;
                                }*/
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
                                y1AUMNumber = Double.parseDouble(y1AUM.replaceAll(",", ""));
                                /*if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y1AUMNumber = y1AUMNumber / 10;
                                } else if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y1AUMNumber = y1AUMNumber * 100;
                                }*/
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
                                y2AUMNumber = Double.parseDouble(y2AUM.replaceAll(",", ""));
                                /*if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y2AUMNumber = y2AUMNumber / 10;
                                } else if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y2AUMNumber = y2AUMNumber * 100;
                                }*/
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
                        System.out.println("########## AUM Row Columns (" + aumColumns.length + ") and Header Row Columns ("+ headerColumnsAUM.length + ") are not same, so made the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);

                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsAUM, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsAUM, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsAUM, Y2);

                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;
                        adjustedY0Column = aumColumns.length - (headerColumnsAUM.length - y0Column);
                        adjustedY1Column = aumColumns.length - (headerColumnsAUM.length - y1Column);
                        adjustedY2Column = aumColumns.length - (headerColumnsAUM.length - y2Column);

                        System.out.print("AUM Header : Y0 Index " + adjustedY0Column);
                        System.out.print(" Y1 Index " + adjustedY1Column);
                        System.out.print(" Y2 Index " + adjustedY2Column + "\n");
                        System.out.println("AUM Columns : " + Arrays.toString(aumColumns));

                        if(adjustedY0Column >=0) {
                            try {
                                y0AUM = aumColumns[adjustedY0Column];
                                y0AUMNumber = Double.parseDouble(y0AUM.replaceAll(",", ""));
                                /*if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y0AUMNumber = y0AUMNumber / 10;
                                } else if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y0AUMNumber = y0AUMNumber * 100;
                                }*/
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
                                y1AUMNumber = Double.parseDouble(y1AUM.replaceAll(",", ""));
                                /*if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y1AUMNumber = y1AUMNumber / 10;
                                } else if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y1AUMNumber = y1AUMNumber * 100;
                                }*/
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
                                y2AUMNumber = Double.parseDouble(y2AUM.replaceAll(",", ""));
                                /*if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("M")) {
                                    y2AUMNumber = y2AUMNumber / 10;
                                } else if (AUM_MILLIONS_OR_BILLIONS_FLAG.equals("B")) {
                                    y2AUMNumber = y2AUMNumber * 100;
                                }*/
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
//            MILLIONS_OR_BILLIONS_FLAG = MILLIONS_OR_BILLIONS_FLAG_OLD;
        } catch (Exception e){
//            MILLIONS_OR_BILLIONS_FLAG = MILLIONS_OR_BILLIONS_FLAG_OLD;
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

        try {
            creditCostLineNo = getLineNumberForMatchingPattern(linesCreditCost, 0, CREDITCOSTS_ROW_NAME, rdec, BROKER);

            if (creditCostLineNo > 1) {
                creditCost = linesCreditCost[creditCostLineNo].replaceAll( "\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\)", "-$1");
                creditCostColumns = getDataColumnsForHeader(creditCost, CREDITCOSTS_ROW_NAME);

                if (creditCostColumns != null && creditCostColumns.length != headerColumnsCreditCosts.length && creditCostColumns.length == 0) {
                    if (!linesCreditCost[creditCostLineNo - 1].trim().equals(""))
                        creditCost = linesCreditCost[creditCostLineNo] + linesCreditCost[creditCostLineNo - 1];
                    else if (!linesCreditCost[creditCostLineNo + 1].trim().equals(""))
                        creditCost = linesCreditCost[creditCostLineNo] + linesCreditCost[creditCostLineNo + 1];

                    creditCostColumns = getDataColumnsForHeader(creditCost, CREDITCOSTS_ROW_NAME);
                }

                if(creditCostColumns!= null && creditCostColumns.length != 0) {
                    if(creditCostColumns.length == headerColumnsCreditCosts.length) {

                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsCreditCosts, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsCreditCosts, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsCreditCosts, Y2);

                        System.out.print("Credit Cost Header : Y0 Index " + y0Column);
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
                        System.out.println("########## Credit Cost Header column count (" + headerColumnsCreditCosts.length + ") not matching with Credit Cost column count(" + creditCostColumns.length + "), used the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsCreditCosts, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsCreditCosts, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsCreditCosts, Y2);

                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;

                        adjustedY0Column = creditCostColumns.length - (headerColumnsCreditCosts.length - y0Column);
                        adjustedY1Column = creditCostColumns.length - (headerColumnsCreditCosts.length - y1Column);
                        adjustedY2Column = creditCostColumns.length - (headerColumnsCreditCosts.length - y2Column);

                        System.out.print("Credit Cost Header : Y0 Index " + adjustedY0Column);
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
            System.out.println("E########## Exception in setting Credit Cost for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
        }
    }

    protected void setGNPA(ReportDataExtractConfig rdec){
        int gnpaLineNo = -1;
        String gnpa = null;
        String[] gnpaColumns = null;
        String y0GNPA = "0", y1GNPA = "0", y2GNPA = "0";
        int y0Column = -1, y1Column = -1, y2Column = -1;

        try {
            gnpaLineNo = getLineNumberForMatchingPattern(linesNPA, 0, GNPA_ROW_NAME, rdec, BROKER);

            if (gnpaLineNo > 1) {
                gnpa = linesNPA[gnpaLineNo];

                gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);

                if (gnpaColumns != null && gnpaColumns.length != headerColumnsNPA.length && gnpaColumns.length == 0) {
                    if (!linesNPA[gnpaLineNo - 1].trim().equals(""))
                        gnpa = linesNPA[gnpaLineNo] + linesNPA[gnpaLineNo - 1];
                    else if (!linesNPA[gnpaLineNo + 1].trim().equals(""))
                        gnpa = linesNPA[gnpaLineNo] + linesNPA[gnpaLineNo + 1];

                    gnpaColumns = getDataColumnsForHeader(gnpa, GNPA_ROW_NAME);
                }

                if(gnpaColumns!= null && gnpaColumns.length != 0) {
                    if(gnpaColumns.length == headerColumnsNPA.length) {

                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsNPA, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsNPA, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsNPA, Y2);

                        System.out.print("NPA Header : Y0 Index " + y0Column);
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
                        System.out.println("########## NPA Header column count (" + headerColumnsNPA.length + ") not matching with GNPA column count(" + gnpaColumns.length + "), used the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsNPA, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsNPA, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsNPA, Y2);

                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;

                        adjustedY0Column = gnpaColumns.length - (headerColumnsNPA.length - y0Column);
                        adjustedY1Column = gnpaColumns.length - (headerColumnsNPA.length - y1Column);
                        adjustedY2Column = gnpaColumns.length - (headerColumnsNPA.length - y2Column);

                        System.out.print("NPA Header : Y0 Index " + adjustedY0Column);
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

        try {
            nnpaLineNo = getLineNumberForMatchingPattern(linesNPA, 0, NNPA_ROW_NAME, rdec, BROKER);

            if (nnpaLineNo > 1) {
                nnpa = linesNPA[nnpaLineNo];

                nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);

                if (nnpaColumns != null && nnpaColumns.length != headerColumnsNPA.length && nnpaColumns.length == 0) {
                    if (!linesNPA[nnpaLineNo - 1].trim().equals(""))
                        nnpa = linesNPA[nnpaLineNo] + linesNPA[nnpaLineNo - 1];
                    else if (!linesNPA[nnpaLineNo + 1].trim().equals(""))
                        nnpa = linesNPA[nnpaLineNo] + linesNPA[nnpaLineNo + 1];

                    nnpaColumns = getDataColumnsForHeader(nnpa, NNPA_ROW_NAME);
                }

                if(nnpaColumns!= null && nnpaColumns.length != 0) {
                    if(nnpaColumns.length == headerColumnsNPA.length) {

                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsNPA, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsNPA, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsNPA, Y2);

                        System.out.print("NPA Header : Y0 Index " + y0Column);
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
                        System.out.println("########## NPA Header column count (" + headerColumnsNPA.length + ") not matching with NNPA column count(" + nnpaColumns.length + "), used the best judgement for " + QUARTER + "_" + rdec.getTICKER() + "_" + BROKER);
                        // Find Y0, Y1 and Y2 Index position
                        y0Column = getIndexOfTheYear(headerColumnsNPA, Y0);
                        y1Column = getIndexOfTheYear(headerColumnsNPA, Y1);
                        y2Column = getIndexOfTheYear(headerColumnsNPA, Y2);

                        int adjustedY0Column = -1, adjustedY1Column = -1, adjustedY2Column = -1;
                        adjustedY0Column = nnpaColumns.length - (headerColumnsNPA.length - y0Column);
                        adjustedY1Column = nnpaColumns.length - (headerColumnsNPA.length - y1Column);
                        adjustedY2Column = nnpaColumns.length - (headerColumnsNPA.length - y2Column);

                        System.out.print("NPA Header : Y0 Index " + adjustedY0Column);
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
}


