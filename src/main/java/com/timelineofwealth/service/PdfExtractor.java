package com.timelineofwealth.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import tech.tablesaw.api.Table;

public class PdfExtractor {
    /*public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        prop.load(PdfExtractor.class.getClassLoader().getResourceAsStream("config.properties"));

        String sourceFile = prop.getProperty("sourceFile");
        String tableHeading = prop.getProperty("tableHeading");
        String[] rowNames = prop.getProperty("rowNames").replace("\"", "").split(",");
        String[] columnNames = prop.getProperty("columnNames").replace("\"", "").split(",");
        String destinationFile = prop.getProperty("destinationFile");

        File file = new File(sourceFile);
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        // Extract data from the table
        int startIndex = text.indexOf(tableHeading);
        int endIndex = text.indexOf("\n\n", startIndex);
        String tableText = text.substring(startIndex, endIndex);
        Table table = Table.read().csv(tableText);

        Table extractedData = table.where(col -> {
            for (String columnName : columnNames) {
                if (col.name().equals(columnName)) {
                    return true;
                }
            }
            return false;
        }).where(row -> {
            for (String rowName : rowNames) {
                if (row.getString(0).startsWith(rowName)) {
                    return true;
                }
            }
            return false;
        });


        // Write extracted data to a file
        FileWriter writer = new FileWriter(destinationFile);
        writer.write("Row\t" + String.join("\t", columnNames) + "\n");
        for (int i = 0; i < extractedData.rowCount(); i++) {
            String rowName = extractedData.row(i).getString(0);
            String rowData = rowName;
            for (String columnName : columnNames) {
                rowData += "\t" + extractedData.column(columnName).getString(i);
            }
            writer.write(rowData + "\n");
        }
        writer.close();
    }*/
}


