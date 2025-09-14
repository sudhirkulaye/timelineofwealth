package com.timelineofwealth.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

public class MOSLPDFExtractor {
    public static void main(String[] args) throws IOException {
        // Load config file
        Properties config = new Properties();
        config.load(new FileInputStream("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia\\config.properties"));

        // Get source file name from config
        String sourceFile = config.getProperty("sourceFile");

        // Open PDF document
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile));

        // Iterate through configured links
        for (int i = 1; config.containsKey("link" + i); i++) {
            try {
                int pageNum = Integer.parseInt(config.getProperty("link" + i));
                for (PdfAnnotation annotation : pdfDoc.getPage(pageNum).getAnnotations()) {
                    String annotationTag = annotation.getPdfObject().get(PdfName.A).toString();
                    if (!annotationTag.contains("http")) continue;

                    String pdfLink = annotationTag.substring(annotationTag.indexOf("http"), annotationTag.indexOf(" >>")).replaceAll(" ", "%20");

                    // Download the PDF document from the link
                    URL website = new URL(pdfLink);
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());

                    // Prepare destination path
                    String destFolder = config.getProperty("destFolder" + i);
                    String fileName = config.getProperty("fileName" + i);
                    int fileCount = 1;
                    String newFileName = destFolder + fileName + ".pdf";
                    while (new File(newFileName).exists()) {
                        newFileName = destFolder + fileName + "_" + fileCount + ".pdf";
                        fileCount++;
                    }

                    // Save the file
                    FileOutputStream fos = new FileOutputStream(newFileName);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();

                    System.out.println("✅ Downloaded: " + fileName);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error processing link" + i + ": " + e.getMessage());
                // Optionally log stack trace or continue silently
            }
        }

        pdfDoc.close();
    }
}