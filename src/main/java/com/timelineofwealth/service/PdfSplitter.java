package com.timelineofwealth.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfSplitter {
    public static void main(String[] args) {
        try {
            // Load properties file
            Properties properties = new Properties();
            properties.load(new FileInputStream("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\KotakDaily\\config.properties"));

            // Get source file name
            String sourceFile = properties.getProperty("sourceFile");

            // Open the original PDF
            PDDocument document = PDDocument.load(new File(sourceFile));

            // Get number of split files
            int numSplits = Integer.parseInt(properties.getProperty("numSplits"));

            // Lists to store start and end page, and destination folders and file names
            List<Integer> startPages = new ArrayList<>();
            List<Integer> endPages = new ArrayList<>();
            List<String> destFolders = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();

            // Loop through each split file
            for (int i = 1; i <= numSplits; i++) {
                // Get start and end pages, destination folder, and file name for current file
                startPages.add(Integer.parseInt(properties.getProperty("startPage" + i)));
                endPages.add(Integer.parseInt(properties.getProperty("endPage" + i)));
                destFolders.add(properties.getProperty("destFolder" + i));
                fileNames.add(properties.getProperty("fileName" + i));
            }

            // Split the PDF
            for (int i = 0; i < numSplits; i++) {
                // Create a new document for the split PDF
                PDDocument splitDoc = new PDDocument();

                // Add pages between the start and end pages
                for (int j = startPages.get(i)-1; j < endPages.get(i); j++) {
                    splitDoc.addPage(document.getPage(j));
                }

                /*// Save the split PDF
                splitDoc.save(destFolders.get(i) + fileNames.get(i) + ".pdf");
                splitDoc.close();*/
                int fileCount = 1;
                String newFileName = destFolders.get(i) + fileNames.get(i) + ".pdf";

                while (new File(newFileName).exists()) {
                    newFileName = destFolders.get(i) + fileNames.get(i) + "_" + fileCount + ".pdf";
                    fileCount++;
                }

                splitDoc.save(newFileName);
                splitDoc.close();
            }

            // Close the original PDF
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
