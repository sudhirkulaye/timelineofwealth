package com.timelineofwealth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellReference;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenerFileVerification {
    public static void main(String[] args) {
        String excelPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2023Q4";
        String latestDateString = "2023-05-20 05:52:00 PM";

        try {
            // Parse the latestDate string to a Date object
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            Date latestDate = dateFormat.parse(latestDateString);

            // Find Excel files created or modified on and after the latestDate
            Path excelFolderPath = Paths.get(excelPath);
            Files.walkFileTree(excelFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Check if the file is an Excel file (XLSX)
                    if (Files.isRegularFile(file) && file.toString().toLowerCase().endsWith(".xlsx") && !file.toString().contains("$")) {
                        // Check if the file was created or modified on or after the latestDate
                        Date fileModifiedDateTime = new Date(attrs.lastModifiedTime().toMillis());
                        if (fileModifiedDateTime.after(latestDate) || fileModifiedDateTime.equals(latestDate)) {
                            // Open the Excel file and read cell B1 of the "Data Sheet"
                            String cellValue = readCellValue(file.toString(), "Data Sheet", "B1");

                            // Print the filename and cell value
                            System.out.println(file.getFileName() + "\t" + cellValue);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readCellValue(String filePath, String sheetName, String cellAddress) throws IOException {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
        Sheet sheet = workbook.getSheet(sheetName);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(1);

        String cellValue = "";
        if (cell != null) {
            CellType cellType = cell.getCellTypeEnum();
            if (cellType == CellType.STRING) {
                cellValue = cell.getStringCellValue();
            } else if (cellType == CellType.NUMERIC) {
                cellValue = String.valueOf(cell.getNumericCellValue());
            }
        }

        workbook.close();
        return cellValue;
    }
}
