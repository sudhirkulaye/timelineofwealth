package com.timelineofwealth.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class HeaderFooterPageEvent extends PdfPageEventHelper {
    private static final Logger logger = LoggerFactory.getLogger(HeaderFooterPageEvent.class);
    private String logoFilePath = "Logo.png";
    private Font paraLinkFont = FontFactory.getFont("Calibri", 10, BaseColor.BLUE);
    private Font paraTextFont = FontFactory.getFont("Calibri", 10, BaseColor.BLACK);

    public void onStartPage(PdfWriter writer, Document document) {
        logger.debug(String.format("In GeneratePDFReport.HeaderFooterPageEvent.onStartPage "));
        try {
            File file = ResourceUtils.getFile("classpath:static/images/Logo.png");
            logoFilePath = file.getAbsolutePath();
        }catch (Exception e){
            logger.error("Unable to get logo file");
        }
        Image image;
        try {
            image = Image.getInstance(logoFilePath);
            image.setAlignment(Element.ALIGN_LEFT);
            //image.setAbsolutePosition(document.left() + 10, document.top() + 1);
            image.setAbsolutePosition(20, 790);
            image.scalePercent(20f, 20f);
            writer.getDirectContent().addImage(image, true);
        } catch (IOException | DocumentException e) {
            logger.error("Erro in loading image GeneratePDFReport.HeaderFooterPageEvent.onStartPage", e);
        }

        //ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("PMS Report"), 30, 800, 0);
        //ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("https://timelineofwealth.com"), 550, 800, 0);
    }

        public void onEndPage(PdfWriter writer, Document document) {
            //ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("" + document.getPageNumber(),paraTextFont), 550, 30, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("" + document.getPageNumber(),paraTextFont), (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 10, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("https://timelineofwealth.com/", paraLinkFont), document.left(), document.bottom() - 10, 0);

        }

}
