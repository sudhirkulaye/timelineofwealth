package com.timelineofwealth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.regex.*;

public class ConsolidateQuarterData {

    public static void main(String[] args) throws Exception {
        String baseDir = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\";
        String quarterFolder = "2025Q4";
        String resultFolder = baseDir + "CriticalFactors\\CriticalFactors\\EquityInvestment\\";
        String bfsFilePath = resultFolder + "Technology\\BFSI.xlsx";

        FileInputStream fis = new FileInputStream(bfsFilePath);
        Workbook bfsWorkbook = new XSSFWorkbook(fis);
        Sheet bfsSheet = bfsWorkbook.getSheet("QuarterP&L");

        for (int rowIndex = 4; rowIndex <= 12; rowIndex++) { // Excel rows A5 to A13
            Row row = bfsSheet.getRow(rowIndex);
            if (row == null) continue;
            Cell cell = row.getCell(0);
            if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING) continue;

            String value = cell.getStringCellValue().trim();
            Matcher matcher = Pattern.compile("([A-Z0-9]+)\\[(\\d+)]").matcher(value);
            if (!matcher.matches()) continue;

            String ticker = matcher.group(1);
            int sourceRowNum = Integer.parseInt(matcher.group(2)) - 1; // zero-based row

            String sourceFilePath = baseDir + quarterFolder + "\\" + ticker + "_FY25Q4.xlsx";
            try (FileInputStream srcFis = new FileInputStream(sourceFilePath)) {
                Workbook srcWorkbook = new XSSFWorkbook(srcFis);
                FormulaEvaluator evaluator = srcWorkbook.getCreationHelper().createFormulaEvaluator();
                Sheet srcSheet = srcWorkbook.getSheet("QuarterP&L");

                Row sourceRow = srcSheet.getRow(sourceRowNum);
                if (sourceRow == null) continue;

                Row destRow = bfsSheet.getRow(rowIndex);
                if (destRow == null) destRow = bfsSheet.createRow(rowIndex);

                for (int col = 1; col <= 17; col++) { // B to R → col 1 to 17
                    Cell sourceCell = sourceRow.getCell(col);
                    Cell destCell = destRow.getCell(col);
                    if (destCell == null) destCell = destRow.createCell(col);

                    if (sourceCell != null) {
                        CellValue cellValue = evaluator.evaluate(sourceCell);

                        if (cellValue != null) {
                            switch (cellValue.getCellType()) {
                                case Cell.CELL_TYPE_NUMERIC:
                                    destCell.setCellValue(cellValue.getNumberValue());
                                    break;
                                case Cell.CELL_TYPE_STRING:
                                    destCell.setCellValue(cellValue.getStringValue());
                                    break;
                                case Cell.CELL_TYPE_BOOLEAN:
                                    destCell.setCellValue(cellValue.getBooleanValue());
                                    break;
                                case Cell.CELL_TYPE_BLANK:
                                    destCell.setCellType(Cell.CELL_TYPE_BLANK);
                                    break;
                                default:
                                    destCell.setCellType(Cell.CELL_TYPE_BLANK);
                            }
                        } else {
                            destCell.setCellType(Cell.CELL_TYPE_BLANK);
                        }
                    } else {
                        destCell.setCellType(Cell.CELL_TYPE_BLANK);
                    }
                }

                srcWorkbook.close();
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + sourceFilePath);
            } catch (Exception e) {
                System.err.println("Error processing: " + ticker + " → " + e.getMessage());
            }
        }

        fis.close();

        // Save back the updated BFSI file
        try (FileOutputStream fos = new FileOutputStream(bfsFilePath)) {
            bfsWorkbook.write(fos);
        }
        bfsWorkbook.close();

        System.out.println("✅ BFSI.xlsx updated successfully.");
    }
}

