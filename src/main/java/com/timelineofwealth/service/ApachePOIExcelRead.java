package com.timelineofwealth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static java.lang.Math.max;

public class ApachePOIExcelRead {
    private static final String FILE_NAME = "C://MyDocuments//MyFirstExcel.xlsx";

    public static void main(String[] args) {

        try {

            FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
            String data[][] = getSheetInStringArray(excelFile, 0);
            for (int i = 0; i < data.length; i++){
                for (int j = 0; j < data[0].length; j ++) {
                    System.out.println(data[i][j]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String[][] getSheetInStringArray(FileInputStream excelFile, int sheetPosition){
        String returnObject[][] = null;
        try {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(sheetPosition);

            int rowSize = datatypeSheet.getLastRowNum()+1;
            int columnSize = 0;

            if (datatypeSheet.getRow(0) == null || datatypeSheet.getRow(0).getLastCellNum() < 0) {
                for(int i = 0; i < datatypeSheet.getLastRowNum(); i++) {
                    if (datatypeSheet.getRow(i) != null && datatypeSheet.getRow(i).getLastCellNum() >= 0) {
                        columnSize = datatypeSheet.getRow(i).getLastCellNum();
                    }
                }
            } else {
                columnSize = datatypeSheet.getRow(0).getLastCellNum();
            }

            System.out.println("rowSize" + rowSize);
            System.out.println("columnSize" + columnSize);

            returnObject = new String[datatypeSheet.getLastRowNum()+1][columnSize];

            for (Row currentRow : datatypeSheet) {
                for (Cell currentCell : currentRow) {
                    returnObject[currentCell.getRowIndex()][currentCell.getColumnIndex()] = getCellValue(currentCell);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnObject;
    }

    private static String getCellValue(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case BOOLEAN:
                return  "" + cell.getBooleanCellValue();
            case STRING:
                return  "" + cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return  "" + cell.getDateCellValue();
                } else {
                    return  "" + cell.getNumericCellValue();
                }
            case FORMULA:
                return  "" + cell.getNumericCellValue();
            case BLANK:
                return  "";
            default:
                return  "";
        }
    }

}
