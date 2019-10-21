package com.timelineofwealth.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.timelineofwealth.dto.BenchmarkTwrrMonthlyDTO;
import com.timelineofwealth.dto.BenchmarkTwrrSummaryDTO;
import com.timelineofwealth.dto.ConsolidatedPortfolioHoldings;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("GeneratePDFReport")
public class GeneratePDFReport {
    private static final Logger logger = LoggerFactory.getLogger(GeneratePDFReport.class);
    //private static Font topicFont = FontFactory.getFont("Cambria", 12, new BaseColor(21,37,57));
    private static Font topicFont = FontFactory.getFont("Cambria", 12, BaseColor.BLUE);
    private static Font paraTextFont = FontFactory.getFont("Calibri", 8, BaseColor.BLACK);
    private static Font tableHeaderFont = FontFactory.getFont("Calibri", 10, BaseColor.WHITE);
    private static Font tableBodyFont = FontFactory.getFont("Calibri", 8, BaseColor.BLACK);
    private static Font benchmarkTableBodyFont = FontFactory.getFont("Calibri", 6, BaseColor.BLACK);
    private static BaseColor tableHeaderBaseColor = new BaseColor(155, 187, 89);
    private static BaseColor tableBodyBaseColor = new BaseColor(205, 221, 117);
    private static BaseColor tableBodyBaseColorAlt = new BaseColor(230, 238, 213);
    private static DecimalFormat dfForPercent = new DecimalFormat("##0.00");
    private static DecimalFormat dfForAmt = new DecimalFormat("#,###");

    @Autowired
    private static UserMembersRepository userMembersRepository;
    @Autowired
    public void setUserMembersRepository(UserMembersRepository userMembersRepository){
        GeneratePDFReport.userMembersRepository = userMembersRepository;
    }

    @Autowired
    private static MemberRepository memberRepository;
    @Autowired
    public void setMemberRepository(MemberRepository memberRepository){
        GeneratePDFReport.memberRepository = memberRepository;
    }

    @Autowired
    private static AdviserUserMappingRepository adviserUserMappingRepository;
    @Autowired
    public void setAdviserUserMappingRepository(AdviserUserMappingRepository adviserUserMappingRepository){
        GeneratePDFReport.adviserUserMappingRepository = adviserUserMappingRepository;
    }

    @Autowired
    private static PortfolioRepository portfolioRepository;
    @Autowired
    public void setPortfolioRepository(PortfolioRepository portfolioRepository){
        GeneratePDFReport.portfolioRepository = portfolioRepository;
    }
    @Autowired
    private static PortfolioHoldingsRepository portfolioHoldingsRepository;
    @Autowired
    public void setPortfolioHoldingsRepository(PortfolioHoldingsRepository portfolioHoldingsRepository){
        GeneratePDFReport.portfolioHoldingsRepository = portfolioHoldingsRepository;
    }
    @Autowired
    private static PortfolioHistoricalHoldingsRepository portfolioHistoricalHoldingsRepository;
    @Autowired
    public void setPortfolioHistoricalHoldingsRepository(PortfolioHistoricalHoldingsRepository portfolioHistoricalHoldingsRepository){
        GeneratePDFReport.portfolioHistoricalHoldingsRepository = portfolioHistoricalHoldingsRepository;
    }
    @Autowired
    private static PortfolioCashflowRepository portfolioCashflowRepository;
    @Autowired
    public void setPortfolioCashflowRepository(PortfolioCashflowRepository portfolioCashflowRepository){
        GeneratePDFReport.portfolioCashflowRepository = portfolioCashflowRepository;
    }
    @Autowired
    private static PortfolioReturnsCalculationSupportRepository portfolioReturnsCalculationSupportRepository;
    @Autowired
    public void setPortfolioReturnsCalculationSupportRepository(PortfolioReturnsCalculationSupportRepository portfolioReturnsCalculationSupportRepository){
        GeneratePDFReport.portfolioReturnsCalculationSupportRepository = portfolioReturnsCalculationSupportRepository;
    }
    @Autowired
    private static PortfolioTwrrMonthlyRepository portfolioTwrrMonthlyRepository;
    @Autowired
    public void setPortfolioTwrrMonthlyRepository(PortfolioTwrrMonthlyRepository portfolioTwrrMonthlyRepository) {
        GeneratePDFReport.portfolioTwrrMonthlyRepository = portfolioTwrrMonthlyRepository;
    }
    @Autowired
    private static PortfolioTwrrSummaryRepository portfolioTwrrSummaryRepository;
    @Autowired
    public void setPortfolioTwrrSummaryRepository(PortfolioTwrrSummaryRepository portfolioTwrrSummaryRepository){
        GeneratePDFReport.portfolioTwrrSummaryRepository = portfolioTwrrSummaryRepository;
    }
    @Autowired
    private static BenchmarkTwrrSummaryRepository benchmarkTwrrSummaryRepository;
    @Autowired
    public void setBenchmarkTwrrSummaryRepository(BenchmarkTwrrSummaryRepository benchmarkTwrrSummaryRepository){
        GeneratePDFReport.benchmarkTwrrSummaryRepository = benchmarkTwrrSummaryRepository;
    }
    @Autowired
    private static BenchmarkTwrrMonthlyRepository benchmarkTwrrMonthlyRepository;
    @Autowired
    public void setBenchmarkTwrrMonthlyRepository(BenchmarkTwrrMonthlyRepository benchmarkTwrrMonthlyRepository){
        GeneratePDFReport.benchmarkTwrrMonthlyRepository = benchmarkTwrrMonthlyRepository;
    }
    @Autowired
    private static CompositeRepository compositeRepository;
    @Autowired
    public void setCompositeRepository(CompositeRepository compositeRepository){
        GeneratePDFReport.compositeRepository = compositeRepository;
    }

    private static HeaderFooterPageEvent headerFooterPageEvent = new HeaderFooterPageEvent();

    private static boolean isAuthenticatedRequest(User loggedInUser, long memberid){
        logger.debug(String.format("In GeneratePDFReport.isAuthenticatedRequest "));
        // Check for model portfolio
        UserMembers memberUser = GeneratePDFReport.userMembersRepository.findByMemberid(memberid);
        //Login user wants to generate PDF
        if(loggedInUser.getEmail().equals(memberUser.getEmail())) {
            return true;
        }
        //Login adviser wants to generate PDF
        int count = adviserUserMappingRepository.countByKeyAdviseridAndKeyUserid(loggedInUser.getEmail(), memberUser.getEmail());
        if (count > 0){
            return true;
        }
        return false;
    }

    private static boolean isModelPortfolio(User loggedInUser, long memberid){
        logger.debug(String.format("In GeneratePDFReport.isAuthenticatedRequest "));
        // Check for model portfolio
        int count = compositeRepository.countByFundManagerEmailAndAdviserMemberid(loggedInUser.getEmail(), memberid);
        if (count > 0){
            return true;
        }
        return false;
    }

    private static void addFirstPage(Document document, String reportHeader, long memberid, boolean isModelPortfolio) throws Exception{

        Paragraph paragraph;
        paragraph = new Paragraph("", topicFont);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        paragraph = new Paragraph(reportHeader, topicFont);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        PdfPTable table = new PdfPTable(2);
        float[] columnWidth = {2f, 4f};
        table.setWidths(columnWidth);
        table.setWidthPercentage(50);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        Member member = memberRepository.findByMemberid(memberid);

        PdfPCell leftCell = new PdfPCell(new Phrase("Statement Date: ",paraTextFont));
        PdfPCell rightCell = new PdfPCell(new Phrase(""+CommonService.getSetupDates().getDateToday().toString(),paraTextFont));

        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(BaseColor.WHITE);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setBackgroundColor(BaseColor.WHITE);
        rightCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(leftCell);
        table.addCell(rightCell);

        if(isModelPortfolio == false) {
            leftCell = new PdfPCell(new Phrase("Client Name: ",paraTextFont));
            String prefix = " ";
            if(member.getGender().equalsIgnoreCase("M")){
                prefix = "Mr. ";
            } else if (member.getGender().equalsIgnoreCase("F")){
                prefix = "Ms. ";
            } else {
                prefix = "M/s ";
            }
            rightCell = new PdfPCell(new Phrase(prefix + member.getFirstName() + " " + member.getLastName(),paraTextFont));
        } else {
            leftCell = new PdfPCell(new Phrase("Model Portfolio Performance",paraTextFont));
            rightCell = new PdfPCell(new Phrase("",paraTextFont));
        }

        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(BaseColor.WHITE);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setBackgroundColor(BaseColor.WHITE);
        rightCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(leftCell);
        table.addCell(rightCell);
        document.add(table);

        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        String disclaimer = "DISCLAIMER: This statement is system generated report and does not require any stamp and signature. " +
                "While efforts have been made to ensure the accuracy of the information provided, " +
                "we shall not in any way be held liable or responsible for any inaccuracy and / or error in the statement " +
                "that may occur due to any malfunction in the software or otherwise. " +
                "Any errors must be brought to the notice within a period of 7 days after delivery of the reports, " +
                "failing which it shall be deemed to be accurate and binding. " +
                "Further you are requested to please contact us if you have any queries on this statement or any discrepancies in the statement. " +
                "This statement contains details of all your investments reported/made through us " +
                "and so reflected in our PMS & Wealth Management System as of the date of issuance hereof. " +
                "Certain transactions may be pending settlement and hence could affect your gains. " +
                "The valuations are only estimates and have been made by us based upon sources regarded by us as appropriate reference. " +
                "The indicative valuations are subject to change without notice and " +
                "we make no representations, warranty, express or implied as to its accuracy, completeness or reliability. " +
                "The valuations may not correspond with valuations given by another market participant. " +
                "We shall have no liability in respect of any error or omission arising from the valuation given. " +
                "This statement is for your private use only and the information herein contained is generated through a software program. " +
                "These statements are being supplied to you solely for your information. " +
                "Statements or part thereof may not be reprinted, sold or redistributed without the written consent. " +
                "Any distribution, use or reproduction of this statement without the prior permission is strictly prohibited. " +
                "This statement is preliminary and tentative and it should not form part of your tax returns.";
        paragraph = new Paragraph(disclaimer, paraTextFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);
    }

    private static void addListOfPMS(Document document, List<Portfolio> portfolios) throws DocumentException{

        document.newPage();
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        Paragraph paragraph;
        paragraph = new Paragraph("1. List of PMS", topicFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        float[] columnWidth = {1f, 3f, 4f, 3f, 3f, 2f, 2f};
        table.setWidths(columnWidth);

        PdfPCell headerCell = new PdfPCell(new Paragraph("ID", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Inception Dt.", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Strategy Name", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Net Investment", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Valuation", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Absolute Return", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Time Wt. Return", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        // for loop to add data
        PdfPCell pdfPCell;
        if(portfolios != null){
            for (int i = 0; i < portfolios.size(); i++){
                if(i%2 == 1){
                    pdfPCell = new PdfPCell(new Paragraph(""+portfolios.get(i).getKey().getPortfolioid(), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColor);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+portfolios.get(i).getStartDate(), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColor);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+portfolios.get(i).getDescription(), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColor);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(portfolios.get(i).getNetInvestment()), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColor);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(portfolios.get(i).getMarketValue()), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColor);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+ dfForPercent.format(new BigDecimal(portfolios.get(i).getAbsoluteReturn().floatValue()*100))+"%", tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColor);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+ dfForPercent.format(new BigDecimal(portfolios.get(i).getAnnualizedReturn().floatValue()*100))+"%", tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColor);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                } else {
                    pdfPCell = new PdfPCell(new Paragraph(""+portfolios.get(i).getKey().getPortfolioid(), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+portfolios.get(i).getStartDate(), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+portfolios.get(i).getDescription(), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(portfolios.get(i).getNetInvestment()), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(portfolios.get(i).getMarketValue()), tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+ dfForPercent.format(new BigDecimal(portfolios.get(i).getAbsoluteReturn().floatValue()*100))+"%", tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                    pdfPCell = new PdfPCell(new Paragraph(""+ dfForPercent.format(new BigDecimal(portfolios.get(i).getAnnualizedReturn().floatValue()*100))+"%", tableBodyFont));
                    pdfPCell.setPaddingLeft(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                    pdfPCell.setExtraParagraphSpace(5f);
                    table.addCell(pdfPCell);
                }
            }
        }
        document.add(table);
    }

    private static void addPortfolioHoldings(Document document, List<Portfolio> portfolios, List<ConsolidatedPortfolioHoldings> consolidatedPortfolioHoldings) throws DocumentException{

        document.newPage();
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        Paragraph paragraph;
        paragraph = new Paragraph("2. Current Holdings ", topicFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        for(int k = 0; k < portfolios.size(); k++) {
            int portfolioid = portfolios.get(k).getKey().getPortfolioid();
            List<ConsolidatedPortfolioHoldings> folioHoldings = consolidatedPortfolioHoldings.stream()
                    .filter(p -> p.getPortfolioid() == portfolioid).collect(Collectors.toList());

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidth = {1f, 4f, 2f, 3f, 3f, 3f, 2f};
            table.setWidths(columnWidth);

            PdfPCell headerCell = new PdfPCell(new Paragraph("ID", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Security Name", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Quantity", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Purchase Cost", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Market Value", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Net Profit", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Weight", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            // for loop to add data
            PdfPCell pdfPCell;
            if(folioHoldings != null){
                for (int i = 0; i < folioHoldings.size(); i++){
                    if(i%2 == 1){
                        pdfPCell = new PdfPCell(new Paragraph(""+folioHoldings.get(i).getPortfolioid(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+folioHoldings.get(i).getName(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(folioHoldings.get(i).getQuantity()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(folioHoldings.get(i).getTotalCost()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(folioHoldings.get(i).getMarketValue()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+ dfForAmt.format(folioHoldings.get(i).getNetProfit()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+ dfForPercent.format(new BigDecimal(folioHoldings.get(i).getWeight().floatValue()))+"%", tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    } else {
                        pdfPCell = new PdfPCell(new Paragraph(""+folioHoldings.get(i).getPortfolioid(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+folioHoldings.get(i).getName(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(folioHoldings.get(i).getQuantity()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(folioHoldings.get(i).getTotalCost()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(folioHoldings.get(i).getMarketValue()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+ dfForAmt.format(folioHoldings.get(i).getNetProfit()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+ dfForPercent.format(new BigDecimal(folioHoldings.get(i).getWeight().floatValue()))+"%", tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    }
                }
            }
            document.add(table);

        }
    }

    private static void addPortfolioCashflows(Document document, List<Portfolio> portfolios, List<PortfolioReturnsCalculationSupport> cashflows) throws DocumentException{

        document.newPage();
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        Paragraph paragraph;
        paragraph = new Paragraph("3. Portfolio Cashflows and EOM Valuations ", topicFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        for(int k = 0; k < portfolios.size(); k++) {
            int portfolioid = portfolios.get(k).getKey().getPortfolioid();
            List<PortfolioReturnsCalculationSupport> cashflowsTemp = cashflows.stream()
                    .filter(p -> p.getKey().getPortfolioid() == portfolioid).collect(Collectors.toList());

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidth = {1f, 3f, 3f, 3f, 4f};
            table.setWidths(columnWidth);

            PdfPCell headerCell = new PdfPCell(new Paragraph("ID", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Date", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Cashflow", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Valuation", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Description", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            // for loop to add data
            PdfPCell pdfPCell;
            if(cashflowsTemp != null){
                for (int i = 0; i < cashflowsTemp.size(); i++){
                    if(i%2 == 1){
                        pdfPCell = new PdfPCell(new Paragraph(""+cashflowsTemp.get(i).getKey().getPortfolioid(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+cashflowsTemp.get(i).getKey().getDate(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(cashflowsTemp.get(i).getCashflow()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(cashflowsTemp.get(i).getValue()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+cashflowsTemp.get(i).getDescription(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    } else {
                        pdfPCell = new PdfPCell(new Paragraph(""+cashflowsTemp.get(i).getKey().getPortfolioid(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+cashflowsTemp.get(i).getKey().getDate(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(cashflowsTemp.get(i).getCashflow()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForAmt.format(cashflowsTemp.get(i).getValue()), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+cashflowsTemp.get(i).getDescription(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    }
                }
            }
            document.add(table);

        }
    }

    private static void addPortfolioReturns(Document document, List<Portfolio> portfolios, List<PortfolioTwrrSummary> portfolioTwrrSummaries, List<PortfolioTwrrMonthly> portfolioTwrrMonthlyList) throws DocumentException{

        document.newPage();
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        Paragraph paragraph;
        paragraph = new Paragraph("4. Portfolio Returns", topicFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        for(int k = 0; k < portfolios.size(); k++) {
            if (k > 0){
                document.newPage();
                document.add( Chunk.NEWLINE );
                document.add( Chunk.NEWLINE );
            }
            int portfolioid = portfolios.get(k).getKey().getPortfolioid();

            paragraph = new Paragraph("Trailing Returns of Portfolio "+ portfolioid, topicFont);
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(50);
            paragraph.setIndentationRight(50);
            paragraph.setSpacingAfter(10);
            document.add(paragraph);

            List<PortfolioTwrrSummary> portfolioTwrrSummariesTemp = portfolioTwrrSummaries.stream()
                    .filter(p -> p.getKey().getPortfolioid() == portfolioid).collect(Collectors.toList());

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidth = {2f, 2f, 1f, 1f, 1f, 1f, 1f, 1f, 2f};
            table.setWidths(columnWidth);

            PdfPCell headerCell = new PdfPCell(new Paragraph("Current Month %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Current Quarter %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("3M %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("6M %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("1Yr %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("2Yr %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("3Yr %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("5Yr %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Inception %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            // for loop to add data
            PdfPCell pdfPCell;
            if(portfolioTwrrSummariesTemp != null){
                for (int i = 0; i < portfolioTwrrSummariesTemp.size(); i++){
                    if(i%2 == 1){
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrSinceCurrentMonth().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrSinceCurrentQuarter().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrThreeMonths().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrHalfYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrOneYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrTwoYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrThreeYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrFiveYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrSinceInception().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    } else {
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrSinceCurrentMonth().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrSinceCurrentQuarter().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrThreeMonths().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrHalfYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrOneYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrTwoYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrThreeYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrFiveYear().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph(""+dfForPercent.format(new BigDecimal(portfolioTwrrSummariesTemp.get(i).getReturnsTwrrSinceInception().floatValue()*100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    }
                }
            }
            document.add(table);

            paragraph = new Paragraph("Quarterly Returns of Portfolio "+ portfolioid, topicFont);
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(50);
            paragraph.setIndentationRight(50);
            paragraph.setSpacingAfter(10);
            document.add(paragraph);

            List<PortfolioTwrrMonthly> portfolioTwrrMonthlyListTemp = portfolioTwrrMonthlyList.stream()
                    .filter(p -> p.getKey().getPortfolioid() == portfolioid).collect(Collectors.toList());

            table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidth1 = {1f, 1f, 1f, 1f, 1f, 1f};
            table.setWidths(columnWidth1);

            headerCell = new PdfPCell(new Paragraph("Year", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Mar-Q %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Jun-Q %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Sep-Q %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Dec-Q %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Annual %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            // for loop to add data
            if(portfolioTwrrMonthlyListTemp != null) {
                for (int i = 0; i < portfolioTwrrMonthlyListTemp.size(); i++) {
                    if (i % 2 == 1) {
                        pdfPCell = new PdfPCell(new Paragraph("" + portfolioTwrrMonthlyListTemp.get(i).getKey().getReturnsYear(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsMarEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJunEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsSepEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsDecEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsCalendarYear().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    } else {
                        pdfPCell = new PdfPCell(new Paragraph("" + portfolioTwrrMonthlyListTemp.get(i).getKey().getReturnsYear(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsMarEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJunEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsSepEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsDecEndingQuarter().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsCalendarYear().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    }
                }
            }
            document.add(table);

            paragraph = new Paragraph("Monthly Returns of Portfolio "+ portfolioid, topicFont);
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(50);
            paragraph.setIndentationRight(50);
            paragraph.setSpacingAfter(10);
            document.add(paragraph);

            table = new PdfPTable(13);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidth2 = {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f};
            table.setWidths(columnWidth2);

            headerCell = new PdfPCell(new Paragraph("Year", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Jan %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Feb %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Mar %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Apr %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("May %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Jun %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Jul %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Aug %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Sep %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Oct %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Nov %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Dec %", tableHeaderFont));
            headerCell.setPaddingLeft(10);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(tableHeaderBaseColor);
            headerCell.setExtraParagraphSpace(5f);
            table.addCell(headerCell);

            // for loop to add data
            if(portfolioTwrrMonthlyListTemp != null) {
                for (int i = 0; i < portfolioTwrrMonthlyListTemp.size(); i++) {
                    if (i % 2 == 1) {
                        pdfPCell = new PdfPCell(new Paragraph("" + portfolioTwrrMonthlyListTemp.get(i).getKey().getReturnsYear(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJan().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJan().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsFeb().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsMar().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsApr().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJun().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJul().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsAug().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsSep().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsOct().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsNov().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsDec().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColor);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    } else {
                        pdfPCell = new PdfPCell(new Paragraph("" + portfolioTwrrMonthlyListTemp.get(i).getKey().getReturnsYear(), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJan().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsFeb().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsMar().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsApr().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsMay().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJun().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsJul().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsAug().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsSep().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsOct().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsNov().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                        pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(portfolioTwrrMonthlyListTemp.get(i).getReturnsDec().floatValue() * 100)), tableBodyFont));
                        pdfPCell.setPaddingLeft(10);
                        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                        pdfPCell.setBackgroundColor(tableBodyBaseColorAlt);
                        pdfPCell.setExtraParagraphSpace(5f);
                        table.addCell(pdfPCell);
                    }
                }
            }
            document.add(table);
        }
    }

    private static void addBenchmarkReturns(Document document, List<Portfolio> portfolios, List<BenchmarkTwrrSummaryDTO> benchmarkTwrrSummaries, List<BenchmarkTwrrMonthlyDTO> benchmarkTwrrMonthlyList) throws DocumentException{

        document.newPage();
        document.add( Chunk.NEWLINE );
        document.add( Chunk.NEWLINE );

        Paragraph paragraph;
        paragraph = new Paragraph("5. Benchmark Returns", topicFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        paragraph = new Paragraph("Trailing Returns of Benchmarks", topicFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
        paragraph.setSpacingAfter(10);
        document.add(paragraph);

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        float[] columnWidth = {2f, 5f, 2f, 2f, 1f, 1f, 1f, 1f, 1f, 1f};
        table.setWidths(columnWidth);

        PdfPCell headerCell = new PdfPCell(new Paragraph("Type", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Name", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Current Month %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Current Quarter %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("3M %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("6M %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("1Yr %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("2Yr %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("3Yr %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("5Yr %", tableHeaderFont));
        headerCell.setPaddingLeft(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_CENTER);
        headerCell.setBackgroundColor(tableHeaderBaseColor);
        headerCell.setExtraParagraphSpace(5f);
        table.addCell(headerCell);

        BaseColor tempColor = tableBodyBaseColor;

        // for loop to add data
        PdfPCell pdfPCell;
        if (benchmarkTwrrSummaries != null) {
            for (int i = 0; i < benchmarkTwrrSummaries.size(); i++) {
                if(i % 2 == 1){
                    tempColor = tableBodyBaseColor;
                } else {
                    tempColor = tableBodyBaseColorAlt;
                }
                pdfPCell = new PdfPCell(new Paragraph("" + benchmarkTwrrSummaries.get(i).getBenchmarkType(), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + benchmarkTwrrSummaries.get(i).getBenchmarkName(), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrSinceCurrentMonth().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrSinceCurrentQuarter().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrThreeMonths().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrHalfYear().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrOneYear().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrTwoYear().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrThreeYear().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
                pdfPCell = new PdfPCell(new Paragraph("" + dfForPercent.format(new BigDecimal(benchmarkTwrrSummaries.get(i).getReturnsTwrrFiveYear().floatValue() * 100)), benchmarkTableBodyFont));
                pdfPCell.setPaddingLeft(10);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(tempColor);
                pdfPCell.setExtraParagraphSpace(5f);
                table.addCell(pdfPCell);
            }
        }
        document.add(table);
    }

    public static boolean generatePMSPDFForMember(User user, long memberid, ServletContext context, HttpServletRequest request, HttpServletResponse response){
        logger.debug(String.format("In GeneratePDFReport.generatePMSPDFForMember "));
        //Authentication for user PDF Generation
        boolean isModelPortfolio = isModelPortfolio(user, memberid);

        if (isModelPortfolio == false){
            boolean isAuthenticatedRequest = isAuthenticatedRequest(user, memberid);
            if (isAuthenticatedRequest == false){
                logger.error("Not authorized to generate the report for User email %s and memberid %d", user.getEmail(), memberid);
                throw new InsufficientAuthenticationException("Not authorized to generate the report");
            }
        }

        List<Long> members = new ArrayList<>();
        members.add(memberid);
        List<Portfolio> portfolios = portfolioRepository.findAllByKeyMemberidInAndStatusOrderByKeyPortfolioid(members,"ACTIVE");
        if(portfolios.size()==0){
            logger.error("There is no portfolio for User email %s and memberid %d", user.getEmail(), memberid);
            return false;
        }
        List<Object[]> objects = portfolioHoldingsRepository.getConsolidatedPortfolioHoldings(members);
        List<ConsolidatedPortfolioHoldings> consolidatedPortfolioHoldings = new ArrayList<>();
        for(Object[] object : objects) {
            ConsolidatedPortfolioHoldings holding = new ConsolidatedPortfolioHoldings();
            holding.setMemberid(Integer.parseInt(""+ (int)object[0]));
            holding.setPortfolioid((int) object[1]);
            holding.setName((String) object[2]);
            holding.setQuantity((BigDecimal) object[3]);
            holding.setTotalCost((BigDecimal) object[4]);
            holding.setMarketValue((BigDecimal) object[5]);
            holding.setNetProfit((BigDecimal) object[6]);
            holding.setWeight((BigDecimal) object[7]);
            consolidatedPortfolioHoldings.add(holding);
        }
        List<PortfolioReturnsCalculationSupport> cashflows = portfolioReturnsCalculationSupportRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyDateDesc(members);
        List<PortfolioTwrrSummary> portfolioTwrrSummaries = portfolioTwrrSummaryRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyBenchmarkidAsc(members);
        List<PortfolioTwrrMonthly> portfolioTwrrMonthlyList = portfolioTwrrMonthlyRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyReturnsYearDesc(members);
        objects = benchmarkTwrrSummaryRepository.findAllBenchmarks();
        List<BenchmarkTwrrSummaryDTO> benchmarkTwrrSummaries = new ArrayList<>();
        for (Object[] object : objects) {
            BenchmarkTwrrSummaryDTO dto = new BenchmarkTwrrSummaryDTO();
            dto.setBenchmarkType(""+object[0]);
            dto.setBenchmarkName(""+object[1]);
            dto.setBenchmarkid(Long.valueOf(""+object[2]));
            dto.setReturnsDate(java.sql.Date.valueOf(""+object[3]));
            dto.setReturnsTwrrSinceCurrentMonth(BigDecimal.valueOf(Double.valueOf(""+object[4])));
            dto.setReturnsTwrrSinceCurrentQuarter(BigDecimal.valueOf(Double.valueOf(""+object[5])));
            dto.setReturnsTwrrSinceFinYear(BigDecimal.valueOf(Double.valueOf(""+object[6])));
            dto.setReturnsTwrrYtd(BigDecimal.valueOf(Double.valueOf(""+object[7])));
            dto.setReturnsTwrrThreeMonths(BigDecimal.valueOf(Double.valueOf(""+object[8])));
            dto.setReturnsTwrrHalfYear(BigDecimal.valueOf(Double.valueOf(""+object[9])));
            dto.setReturnsTwrrOneYear(BigDecimal.valueOf(Double.valueOf(""+object[10])));
            dto.setReturnsTwrrTwoYear(BigDecimal.valueOf(Double.valueOf(""+object[11])));
            try {
                dto.setReturnsTwrrThreeYear(BigDecimal.valueOf(Double.valueOf("" + object[12])));
            } catch (Exception e){
                dto.setReturnsTwrrThreeYear(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsTwrrFiveYear(BigDecimal.valueOf(Double.valueOf(""+object[13])));
            } catch (Exception e) {
                dto.setReturnsTwrrFiveYear(BigDecimal.valueOf(0));
            }

            try {
                dto.setReturnsTwrrTenYear(BigDecimal.valueOf(Double.valueOf(""+object[14])));
            } catch (Exception e){
                dto.setReturnsTwrrTenYear(BigDecimal.valueOf(0));
            }
            dto.setReturnsTwrrSinceInception(BigDecimal.valueOf(0));

            benchmarkTwrrSummaries.add(dto);
        }
        objects = benchmarkTwrrMonthlyRepository.findAllBenchmarks();
        List<BenchmarkTwrrMonthlyDTO> benchmarkTwrrMonthlyList = new ArrayList<>();
        for (Object[] object : objects) {
            BenchmarkTwrrMonthlyDTO dto = new BenchmarkTwrrMonthlyDTO();
            dto.setBenchmarkType(""+object[0]);
            dto.setBenchmarkName(""+object[1]);
            dto.setBenchmarkid(Long.valueOf(""+object[2]));
            dto.setYear(Integer.valueOf(""+object[3]));
            try {
                dto.setReturnsCalendarYear(BigDecimal.valueOf(Double.valueOf(""+object[4])));
            } catch (Exception e){
                dto.setReturnsCalendarYear(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsFinYear(BigDecimal.valueOf(Double.valueOf("" + object[5])));
            }catch (Exception e){
                dto.setReturnsFinYear(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsMarEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[6])));
            } catch (Exception e) {
                dto.setReturnsMarEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJunEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[7])));
            } catch (Exception e) {
                dto.setReturnsJunEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsSepEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[8])));
            } catch (Exception e) {
                dto.setReturnsSepEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsDecEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[9])));
            } catch (Exception e) {
                dto.setReturnsDecEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJan(BigDecimal.valueOf(Double.valueOf(""+object[10])));
            } catch (Exception e) {
                dto.setReturnsJan(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsFeb(BigDecimal.valueOf(Double.valueOf(""+object[11])));
            } catch (Exception e) {
                dto.setReturnsFeb(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsMar(BigDecimal.valueOf(Double.valueOf(""+object[12])));
            } catch (Exception e) {
                dto.setReturnsMar(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsApr(BigDecimal.valueOf(Double.valueOf(""+object[13])));
            } catch (Exception e) {
                dto.setReturnsApr(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsMay(BigDecimal.valueOf(Double.valueOf(""+object[14])));
            } catch (Exception e) {
                dto.setReturnsMay(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJun(BigDecimal.valueOf(Double.valueOf(""+object[15])));
            } catch (Exception e) {
                dto.setReturnsJun(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJul(BigDecimal.valueOf(Double.valueOf(""+object[16])));
            } catch (Exception e) {
                dto.setReturnsJul(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsAug(BigDecimal.valueOf(Double.valueOf(""+object[17])));
            } catch (Exception e) {
                dto.setReturnsAug(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsSep(BigDecimal.valueOf(Double.valueOf(""+object[18])));
            } catch (Exception e) {
                dto.setReturnsSep(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsOct(BigDecimal.valueOf(Double.valueOf(""+object[19])));
            } catch (Exception e) {
                dto.setReturnsOct(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsNov(BigDecimal.valueOf(Double.valueOf(""+object[20])));
            } catch (Exception e) {
                dto.setReturnsNov(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsDec(BigDecimal.valueOf(Double.valueOf(""+object[21])));
            } catch (Exception e) {
                dto.setReturnsDec(BigDecimal.valueOf(0));
            }
            benchmarkTwrrMonthlyList.add(dto);
        }

        try {
            Document document;

            //document = new Document(PageSize.A4, 10, 10, 10, 10);
            document = new Document(PageSize.A4);

            String filePath = context.getRealPath("/resources/reports");
            File file = new File(filePath);
            boolean exists = new File(filePath).exists();
            if(!exists){
                new File(filePath).mkdirs();
            }

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file + "/" +memberid+".pdf"));
            writer.setPageEvent(GeneratePDFReport.headerFooterPageEvent);
            document.open();

            addFirstPage(document,"PMS Performance and Wealth Distribution Summary", memberid, isModelPortfolio);
            addListOfPMS(document, portfolios);
            addPortfolioHoldings(document, portfolios, consolidatedPortfolioHoldings);
            addPortfolioCashflows(document, portfolios, cashflows);
            addPortfolioReturns(document, portfolios,portfolioTwrrSummaries, portfolioTwrrMonthlyList);
            addBenchmarkReturns(document, portfolios,benchmarkTwrrSummaries, benchmarkTwrrMonthlyList);

            document.close();
            writer.close();
            return true;

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
