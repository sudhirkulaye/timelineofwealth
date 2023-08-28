package com.timelineofwealth.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

public class UpdateQuarterlyExcels {

    private static final String OLD_FOLDER = "Old";
    private static final int HEADER_ROW = 3;
    private static final int START_ROW = 4;
    private static final int END_ROW = 12;
    private static final int NUM_COLUMNS = 7;

    public static void main(String[] args) throws IOException, EncryptedDocumentException {
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis\\config.properties");
        prop.load(input);
        input.close();

        String sourcePath = prop.getProperty("SourcePath");
        boolean fileCopyFlag = prop.getProperty("FileCopyFlag").equalsIgnoreCase("true") ? true : false;
        boolean fileContentCopyFlag = prop.getProperty("FileContentCopyFlag").equalsIgnoreCase("true") ? true : false;
        ArrayList<String> sourceFiles = new ArrayList<String>();
        ArrayList<String> destinationPaths = new ArrayList<String>();
        int count = 1;
        while (true) {
            String sourceFile = prop.getProperty("SourceFile" + count);
            if (sourceFile == null) {
                break;
            }
            sourceFiles.add(sourceFile);
            destinationPaths.add(prop.getProperty("DestinationPath" + count));
            count++;
        }

        for (int i = 0; i < sourceFiles.size(); i++) {
            String sourceFile = sourceFiles.get(i);
            String destinationPath = destinationPaths.get(i);
            int fileNumber = i+1;
            File oldFileRenamed = null, newFile = null;
            if(fileCopyFlag){
                oldFileRenamed = renameAndMoveToOld(destinationPath, sourceFile);
                newFile = copyLatestFileToDestination(sourcePath, destinationPath, sourceFile);
            }
            if(oldFileRenamed == null) {
                String strOldFileName = prop.getProperty("OldFileName" + fileNumber);
                oldFileRenamed = new File(strOldFileName);
            }
            if (fileContentCopyFlag == true) {
                if(newFile == null) {
                    String strOldFileName = prop.getProperty("NewFileName" + fileNumber);
                    newFile = new File(strOldFileName);
                }
                copyQuarterPAndLSheet(oldFileRenamed, newFile);
                copyAnnualResultsSheet(oldFileRenamed, newFile);
                copySegmentAnalysisSheet(oldFileRenamed, newFile);
                copyValuationHistorySheet(oldFileRenamed, newFile);
                copyAnalystRecoSheet(oldFileRenamed, newFile);
                copyHistoryAndRatioSheet(oldFileRenamed, newFile);
                changeDataSheet(oldFileRenamed, newFile);
            }
        }
    }

    private static File renameAndMoveToOld(String destinationPath, String sourceFile) {
        String fileName = sourceFile.split("_")[0];
        File oldFile = new File(destinationPath + File.separator + fileName + ".xlsx");
        File oldFileRenamed = null;
        if (oldFile.exists()) {
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date(oldFile.lastModified()));
            oldFileRenamed = new File(destinationPath + File.separator + OLD_FOLDER + File.separator + fileName + "_" + date + ".xlsx");
            oldFile.renameTo(oldFileRenamed);
        }
        return oldFileRenamed;
    }

    private static File copyLatestFileToDestination(String sourcePath, String destinationPath, String sourceFile) {
        Path sourceFilePath = Paths.get(sourcePath, sourceFile);
        String destFile = sourceFile.substring(0, sourceFile.lastIndexOf("_"))+ ".xlsx";
        Path destFilePath = Paths.get(destinationPath, destFile);
        File exisitngFile = null;
        try {
            exisitngFile = new File(destinationPath + File.separator + destFile);
            if (!exisitngFile.exists())
                Files.copy(sourceFilePath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error copying file: " + e.getMessage());
        }
        return exisitngFile;
    }

    private static void copyQuarterPAndLSheet(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("QuarterP&L");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("QuarterP&L");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator evaluatorOld = oldWb.getCreationHelper().createFormulaEvaluator();

            Row row = ws.getRow(2);
            Row rowOld = oldWs.getRow(2);

            // flag to copy zero'th column
            boolean isFirstColumnCopied = false;

            // Copy data if header match
            for (int col = 1; col <= 17; col++) {

                Cell cell = row.getCell(col);
                evaluator.evaluateFormulaCell(cell);
                String date = "" + cell.getNumericCellValue();

                int oldCol = 0;
                for (int i = 1; i <= 17; i++) {
                    Cell cellOld = rowOld.getCell(i);
                    evaluatorOld.evaluateFormulaCell(cellOld);
                    if ((cellOld.getNumericCellValue() + "").equals(date)) {
                        oldCol = i;
                        break;
                    }
                }
                Row oldDatarow = null;
                Cell oldDataCell = null;
                double data = 0.0;
                String strData = "";
                if (oldCol != 0) {
                    if(!isFirstColumnCopied) {
                        for (int rownum = 51; rownum <= 219; rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(0);
                                evaluatorOld.evaluateFormulaCell(oldDataCell);
                                strData = oldDataCell.getStringCellValue();
                                ws.getRow(rownum).getCell(0).setCellValue(strData);
                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                ws.getRow(rownum).getCell(0).setCellValue(strData);
                                ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);

                                // Get the comment from the old sheet cell
                                Comment comment = oldDataCell.getCellComment();
                                if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                    // Create a drawing object in the destination sheet
                                    XSSFDrawing drawing = ws.createDrawingPatriarch();
                                    // Create a comment object using the drawing object
                                    XSSFComment newComment = drawing.createCellComment(drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5));
                                    // Set the comment text
                                    newComment.setString(comment.getString());
                                    // Set the author of the comment
                                    newComment.setAuthor(comment.getAuthor());
                                    // Set the cell reference of the comment
                                    newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                    // Set the comment to the destination sheet cell
                                    ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                }
                            }
                        }
                        isFirstColumnCopied = true;
                    }
                    if (col <= 7) {
                        // Copy row 4 to 12
                        for (int rownum = 3; rownum <= 11; rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            oldDataCell = oldDatarow.getCell(oldCol);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            if(oldDataCell != null && (oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
                                data = oldDataCell.getNumericCellValue();
                                ws.getRow(rownum).getCell(col).setCellValue(data);
                            }
                            if(oldDataCell != null && (oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA)) {
                                data = evaluatorOld.evaluate(oldDataCell).getNumberValue();
                                ws.getRow(rownum).getCell(col).setCellValue(data);
                                evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                            }
                        }
                    }
                    // Copy row 13
                    oldDatarow = oldWs.getRow(12);
                    oldDataCell = oldDatarow.getCell(oldCol);
                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                    if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK && oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
                        data = oldDataCell.getNumericCellValue();
                        ws.getRow(12).getCell(col).setCellValue(data);
                    }

                    // Copy row 14
                    oldDatarow = oldWs.getRow(13);
                    oldDataCell = oldDatarow.getCell(oldCol);
                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                    if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK && oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
                        data = oldDataCell.getNumericCellValue();
                        ws.getRow(13).getCell(col).setCellValue(data);
                    }

                    // Copy row 15
                    oldDatarow = oldWs.getRow(14);
                    if(oldDatarow != null) {
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        try {
                            if (oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                                if (oldDataCell.getCellType() != Cell.CELL_TYPE_FORMULA) {
                                    if (DateUtil.isCellDateFormatted(oldDataCell)) {
                                        Date d1 = oldDataCell.getDateCellValue();
                                        ws.getRow(14).getCell(col).setCellType(Cell.CELL_TYPE_BLANK);
                                        ws.getRow(14).getCell(col).setCellValue(d1);
                                    }
                                } else {
                                    if (oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC && ws.getRow(14) != null && ws.getRow(14).getCell(col) != null) {
                                        ws.getRow(14).getCell(col).setCellValue(oldDataCell.getNumericCellValue());
                                    } else if (oldDataCell.getCellTypeEnum() == CellType.STRING && ws.getRow(14) != null && ws.getRow(14).getCell(col) != null) {
                                        ws.getRow(14).getCell(col).setCellValue(oldDataCell.getStringCellValue());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Exception while copying date field in QuarterP&L for file " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")) + " for column " + col);
                        }
                    }

                    // Copy row 52
                    oldDatarow = oldWs.getRow(51);
                    if (oldDatarow != null) {
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if (oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(51).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 69
                    oldDatarow = oldWs.getRow(68);
                    if(oldDatarow != null) {
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if (oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(68).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 71 to 77
                    for (int rownum = 70; rownum <= 76; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        if (oldDatarow != null) {
                            oldDataCell = oldDatarow.getCell(oldCol);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            if (oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                                data = oldDataCell.getNumericCellValue();
                                ws.getRow(rownum).getCell(col).setCellValue(data);
                            }
                        }
                    }

                    // Copy row 79 to 85
                    for (int rownum = 78; rownum <= 84; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        if (oldDatarow != null) {
                            oldDataCell = oldDatarow.getCell(oldCol);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            if (oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                                data = oldDataCell.getNumericCellValue();
                                ws.getRow(rownum).getCell(col).setCellValue(data);
                            }
                        }
                    }

                    // Copy row 150 to 156
                    for (int rownum = 149; rownum <= 155; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        if (oldDatarow != null) {
                            oldDataCell = oldDatarow.getCell(oldCol);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            if (oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                                data = oldDataCell.getNumericCellValue();
                                ws.getRow(rownum).getCell(col).setCellValue(data);
                            }
                        }
                    }

                    // Copy row 203 to 220
                    for (int rownum = 202; rownum <= 219; rownum++) { // recent change
                        oldDatarow = oldWs.getRow(rownum);
                        if (oldDatarow != null) {
                            oldDataCell = oldDatarow.getCell(oldCol);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            if (oldDataCell != null && (oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
                                data = oldDataCell.getNumericCellValue();
                                ws.getRow(rownum).getCell(col).setCellValue(data);
                            }
                            if(oldDataCell != null && (oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA)) {
                                String formula = oldDataCell.getCellFormula();
                                ws.getRow(rownum).getCell(col).setCellFormula(formula);
                                evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                            }
                        }
                    }
                }
            }

            // Copy Additional rows if any
            row = ws.getRow(2);
            rowOld = oldWs.getRow(2);
            Row additionalRow = oldWs.getRow(220);
            isFirstColumnCopied = false;

            if(additionalRow != null) {
                for (int col = 1; col <= row.getLastCellNum(); col++) {
                    int oldCol = 0;
                    if(col <= 17) {
                        Cell cell = row.getCell(col);
                        evaluator.evaluateFormulaCell(cell);
                        String date = "" + cell.getNumericCellValue();


                        for (int i = 1; i <= 17; i++) {
                            Cell cellOld = rowOld.getCell(i);
                            evaluatorOld.evaluateFormulaCell(cellOld);
                            if ((cellOld.getNumericCellValue() + "").equals(date)) {
                                oldCol = i;
                                break;
                            }
                        }
                    } else {
                        oldCol = col;
                    }
                    Row oldDatarow = null;
                    Cell oldDataCell = null;
                    double data = 0.0;
                    String strData = "";
                    if (!isFirstColumnCopied) {
                        for (int rownum = 220; rownum <= oldWs.getLastRowNum(); rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(0);
                                if (oldDataCell != null) {
                                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                                    strData = oldDataCell.getStringCellValue();
                                    if(ws.getRow(rownum) == null) {
                                        ws.createRow(rownum);
                                    }
                                    if(ws.getRow(rownum).getCell(0) == null) {
                                        ws.getRow(rownum).createCell(0);
                                    }
                                    ws.getRow(rownum).getCell(0).setCellValue(strData);
                                    int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                    XSSFCellStyle newCellStyle = wb.createCellStyle();
                                    newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                    ws.getRow(rownum).getCell(0).setCellValue(strData);
                                    ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);

                                    // Get the comment from the old sheet cell
                                    /*Comment comment = oldDataCell.getCellComment();
                                    if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                        // Create a drawing object in the destination sheet
                                        XSSFDrawing drawing = ws.createDrawingPatriarch();
                                        // Create a comment object using the drawing object
                                        XSSFComment newComment = drawing.createCellComment(drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5));
                                        // Set the comment text
                                        newComment.setString(comment.getString());
                                        // Set the author of the comment
                                        newComment.setAuthor(comment.getAuthor());
                                        // Set the cell reference of the comment
                                        if (ws.getRow(rownum).getCell(0).getAddress() != null) {
                                            newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                            // Set the comment to the destination sheet cell
                                            ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                        }
                                    }*/
                                }
                            }
                        }
                        isFirstColumnCopied = true;
                    }
                    if (oldCol != 0) {
                        // Copy row 162 to last row
                        for (int rownum = 220; rownum <= oldWs.getLastRowNum(); rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(oldCol);
                                if (oldDataCell != null) {
                                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                                    if (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK) {
                                        if(oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                            data = oldDataCell.getNumericCellValue();
                                            if (ws.getRow(rownum) == null) {
                                                ws.createRow(rownum);
                                            }
                                            if (ws.getRow(rownum).getCell(col) == null) {
                                                ws.getRow(rownum).createCell(col);
                                            }
                                            ws.getRow(rownum).getCell(col).setCellValue(data);
                                            int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                            ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);

                                        }
                                        //if it is a formula then copy formula at the old position only
                                        if(oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
//                                            System.out.println("row - " + rownum + " col - " + col);
                                            String formula = oldDataCell.getCellFormula();
                                            String regex1 = "^\\d+(?:\\.\\d+)?\\s*[+\\-*\\/]\\s*\\d+(?:\\.\\d+)?$";
                                            String regex2 = "[A-Z]+\\$\\d+|[A-Z]+\\d+\\s*[+\\-*\\/]\\s*\\d+(?:\\.\\d+)?$";
                                            boolean isMathamatical = formula.matches(regex1) || formula.matches(regex2);
//                                            System.out.println("\tisMathamatical - " + isMathamatical + " for Formula - " + formula);
                                            if (ws.getRow(rownum) == null) {
                                                ws.createRow(rownum);
                                            }
                                            if (ws.getRow(rownum).getCell(oldCol) == null) {
                                                ws.getRow(rownum).createCell(oldCol);
                                            }
                                            if(!formula.isEmpty() && isMathamatical == true) {
                                                if (ws.getRow(rownum).getCell(col) == null) {
                                                    ws.getRow(rownum).createCell(col);
                                                }
                                                ws.getRow(rownum).getCell(col).setCellFormula(formula);
                                                evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                                                evaluator.evaluate(ws.getRow(rownum).getCell(col));
                                            }
                                            if(!formula.isEmpty() && isMathamatical == false) {
                                                ws.getRow(rownum).getCell(oldCol).setCellFormula(formula);
                                                evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(oldCol));
                                                evaluator.evaluate(ws.getRow(rownum).getCell(oldCol));
                                                if (col > 0 && oldDatarow.getCell(col) != null) {
                                                    if (ws.getRow(rownum).getCell(col) == null )
                                                        ws.getRow(rownum).createCell(col);
                                                    if (ws.getRow(rownum).getCell(col).getCellType() == Cell.CELL_TYPE_BLANK && oldDatarow.getCell(col).getCellType() == Cell.CELL_TYPE_FORMULA) {
                                                        ws.getRow(rownum).getCell(col).setCellFormula(oldDatarow.getCell(col).getCellFormula());
                                                        evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                                                        evaluator.evaluate(ws.getRow(rownum).getCell(col));
                                                    }
                                                }
                                            }
                                            int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                            if (isMathamatical == true)
                                                ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                            else
                                                ws.getRow(rownum).getCell(oldCol).setCellStyle(newCellStyle);

//                                            copyConditionalFormattingRules(oldWb, wb, oldWs, ws, rownum, oldCol, col);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Retain last year growth rate prediction
            Row oldDatarow = oldWs.getRow(18);
            if (oldDatarow != null) {
                Cell oldDataCell = oldDatarow.getCell(22);
                if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                    if (oldDataCell.getCellType() != Cell.CELL_TYPE_FORMULA) {
                        double data = oldDataCell.getNumericCellValue();
                        ws.getRow(18).getCell(22).setCellValue(data);
                    }
                }
            }

            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            oldWb.close();
            String fileName = newFile.getAbsoluteFile().toString();
            System.out.println("Copied QuarterPAndL Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (Exception e) {
            System.out.println("Exception while copying QuarterPAndL Sheet for " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")));
            e.printStackTrace();
        }
    }

    private static void copyAnnualResultsSheet(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("AnnualResults");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("AnnualResults");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator evaluatorOld = oldWb.getCreationHelper().createFormulaEvaluator();

            Row row = ws.getRow(2);
            Row rowOld = oldWs.getRow(2);

            // Copy data if header match
            for (int col = 1; col <= 4; col++) {
                Cell cell = row.getCell(col);
                evaluator.evaluateFormulaCell(cell);
                String date = "" + cell.getNumericCellValue();

                int oldCol = 0;
                for (int i = 1; i <= 5; i++) {
                    if(rowOld != null) {
                        Cell cellOld = rowOld.getCell(i);
                        if (cellOld != null) {
                            evaluatorOld.evaluateFormulaCell(cellOld);
                            if ((cellOld.getNumericCellValue() + "").equals(date)) {
                                oldCol = i;
                                break;
                            }
                        }
                    }
                }
                Row oldDatarow = null;
                Cell oldDataCell = null;
                double data = 0.0;
                if (oldCol != 0) {
                    // Copy row 30 to 68
                    for (int rownum = 29; rownum <= 67; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            if(oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                data = oldDataCell.getNumericCellValue();
                            else if(oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA)
                                data = evaluatorOld.evaluate(oldDataCell).getNumberValue();
                            else
                                data = 0.0;
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }
                }
            }

            // Copy Additional rows if any
            row = ws.getRow(2);
            rowOld = oldWs.getRow(2);
            Row additionalRow = oldWs.getRow(102);
            boolean isFirstColumnCopied = false;
            if(additionalRow != null) {
                for (int col = 1; col <= row.getLastCellNum(); col++) {
                    int oldCol = 0;
                    if (col <=14) {
                        Cell cell = row.getCell(col);
                        evaluator.evaluateFormulaCell(cell);
                        String date = "" + cell.getNumericCellValue();
                        for (int i = 1; i <= 14; i++) {
                            if(rowOld != null) {
                                Cell cellOld = rowOld.getCell(i);
                                evaluatorOld.evaluateFormulaCell(cellOld);
                                if ((cellOld.getNumericCellValue() + "").equals(date)) {
                                    oldCol = i;
                                    break;
                                }
                            }
                        }
                    } else  {
                        oldCol = col;
                    }
                    Row oldDatarow = null;
                    Cell oldDataCell = null;
                    double data = 0.0;
                    String strData = "";
                    if (!isFirstColumnCopied) {
                        for (int rownum = 102; rownum <= oldWs.getLastRowNum(); rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(0);
                                if (oldDataCell != null) {
                                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                                    strData = oldDataCell.getStringCellValue();
                                    if(ws.getRow(rownum) == null) {
                                        ws.createRow(rownum);
                                    }
                                    if(ws.getRow(rownum).getCell(0) == null) {
                                        ws.getRow(rownum).createCell(0);
                                    }
                                    ws.getRow(rownum).getCell(0).setCellValue(strData);
                                    int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                    XSSFCellStyle newCellStyle = wb.createCellStyle();
                                    newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                    ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);

                                    // Get the comment from the old sheet cell
                                        Comment comment = oldDataCell.getCellComment();
                                        if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                            // Create a drawing object in the destination sheet
                                            XSSFDrawing drawing = ws.createDrawingPatriarch();
                                            // Create a comment object using the drawing object
                                            XSSFComment newComment = drawing.createCellComment(drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5));
                                            // Set the comment text
                                            newComment.setString(comment.getString());
                                            // Set the author of the comment
                                            newComment.setAuthor(comment.getAuthor());
                                            // Set the cell reference of the comment
                                            newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                            // Set the comment to the destination sheet cell
                                            ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                        }
                                }
                            }
                        }
                        isFirstColumnCopied = true;
                    }
                    if (oldCol != 0) {
                        // Copy row 162 to last row
                        for (int rownum = 102; rownum <= oldWs.getLastRowNum(); rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(oldCol);
                                if (oldDataCell != null) {
                                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                                    if (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK) {
                                        if(oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                            data = oldDataCell.getNumericCellValue();
                                            if (ws.getRow(rownum) == null) {
                                                ws.createRow(rownum);
                                            }
                                            if (ws.getRow(rownum).getCell(col) == null) {
                                                ws.getRow(rownum).createCell(col);
                                            }
                                            ws.getRow(rownum).getCell(col).setCellValue(data);
                                            int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                            ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                        }
                                        // if it sa formula then keep the formula where it should be
                                        if(oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                            String formula = oldDataCell.getCellFormula();
                                            String regex1 = "^\\d+(?:\\.\\d+)?\\s*[+\\-*\\/]\\s*\\d+(?:\\.\\d+)?$";
                                            String regex2 = "[A-Z]+\\$\\d+|[A-Z]+\\d+\\s*[+\\-*\\/]\\s*\\d+(?:\\.\\d+)?$";
                                            boolean isMathamatical = formula.matches(regex1) || formula.matches(regex2);
                                            if (ws.getRow(rownum) == null) {
                                                ws.createRow(rownum);
                                            }
                                            if (ws.getRow(rownum).getCell(oldCol) == null) {
                                                ws.getRow(rownum).createCell(oldCol);
                                            }
                                            if (!formula.isEmpty() && isMathamatical == true) {
                                                if (ws.getRow(rownum).getCell(col) == null) {
                                                    ws.getRow(rownum).createCell(col);
                                                }
                                                ws.getRow(rownum).getCell(col).setCellFormula(formula);
                                                evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                                                evaluator.evaluate(ws.getRow(rownum).getCell(col));
                                            }
                                            if (!formula.isEmpty() && isMathamatical == false) {
                                                ws.getRow(rownum).getCell(oldCol).setCellFormula(formula);
                                                evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(oldCol));
                                                evaluator.evaluate(ws.getRow(rownum).getCell(oldCol));
                                                if (col > 0 && oldDatarow.getCell(col) != null) {
                                                    if (ws.getRow(rownum).getCell(col) == null )
                                                        ws.getRow(rownum).createCell(col);
                                                    if (ws.getRow(rownum).getCell(col).getCellType() == Cell.CELL_TYPE_BLANK && oldDatarow.getCell(col).getCellType() == Cell.CELL_TYPE_FORMULA) {
                                                        ws.getRow(rownum).getCell(col).setCellFormula(oldDatarow.getCell(col).getCellFormula());
                                                        evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                                                        evaluator.evaluate(ws.getRow(rownum).getCell(col));
                                                    }
                                                }
                                            }
                                            int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                            if (isMathamatical == true)
                                                ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                            else
                                                ws.getRow(rownum).getCell(oldCol).setCellStyle(newCellStyle);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            oldWb.close();
            String fileName = newFile.getAbsoluteFile().toString();
            System.out.println("Copied AnnualResult Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (Exception e) {
            System.out.println("Exception while copying AnnualResult Sheet for " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")));
            e.printStackTrace();
        }
    }

    private static void copySegmentAnalysisSheet(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("SegmentAnalysis");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("SegmentAnalysis");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator evaluatorOld = oldWb.getCreationHelper().createFormulaEvaluator();

            Row row = ws.getRow(1);
            Row rowOld = oldWs.getRow(1);

            // flag to copy zero'th column
            boolean isFirstColumnCopied = false;

            // Copy data if header match
            for (int col = 1; col <= 12; col++) {
                Cell cell = row.getCell(col);
                evaluator.evaluateFormulaCell(cell);
                String date = "" + cell.getNumericCellValue();

                int oldCol = 0;
                for (int i = 1; i <= 12; i++) {
                    if(rowOld != null) {
                        if (rowOld != null) {
                            Cell cellOld = rowOld.getCell(i);
                            if (cellOld != null && evaluatorOld != null) {
                                evaluatorOld.evaluateFormulaCell(cellOld);
                                if (cellOld.getCellType() == Cell.CELL_TYPE_FORMULA && (cellOld.getNumericCellValue() + "").equals(date)) {
                                    oldCol = i;
                                    break;
                                }
                            }
                        }
                    }
                }
                Row oldDatarow = null;
                Cell oldDataCell = null;
                double data = 0.0;
                String strData = "";
                if (oldCol != 0) {
                    if(!isFirstColumnCopied) {
                        for (int rownum = 2; rownum <= 169; rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            oldDataCell = oldDatarow.getCell(0);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            strData = oldDataCell.getStringCellValue();
                            ws.getRow(rownum).getCell(0).setCellValue(strData);
                            int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                            ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);

                            // Get the comment from the old sheet cell
                            /*Comment comment = oldDataCell.getCellComment();
                            if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                // Create a drawing object in the destination sheet
                                XSSFDrawing drawing = ws.createDrawingPatriarch();
                                // Create a comment object using the drawing object
                                XSSFComment newComment = drawing.createCellComment(drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5));
                                // Set the comment text
                                newComment.setString(comment.getString());
                                // Set the author of the comment
                                newComment.setAuthor(comment.getAuthor());
                                // Set the cell reference of the comment
                                newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                // Set the comment to the destination sheet cell
                                ws.getRow(rownum).getCell(0).setCellComment(newComment);
                            }*/
                        }
                        isFirstColumnCopied = true;
                    }

                    // Copy row 3 to 14
                    for (int rownum = 2; rownum <= 13; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 25 to 33
                    for (int rownum = 24; rownum <= 32; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 45 to 52
                    for (int rownum = 44; rownum <= 51; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 63 to 70
                    for (int rownum = 62; rownum <= 69; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 81
                    oldDatarow = oldWs.getRow(80);
                    oldDataCell = oldDatarow.getCell(oldCol);
                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                    if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                        data = oldDataCell.getNumericCellValue();
                        ws.getRow(80).getCell(col).setCellValue(data);
                    }

                    // Copy row 85 to 91
                    for (int rownum = 84; rownum <= 90; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 101
                    oldDatarow = oldWs.getRow(100);
                    oldDataCell = oldDatarow.getCell(oldCol);
                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                    if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                        data = oldDataCell.getNumericCellValue();
                        ws.getRow(100).getCell(col).setCellValue(data);
                    }

                    // Copy row 103 to 110
                    for (int rownum = 102; rownum <= 109; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 137 to 154
                    for (int rownum = 136; rownum <= 153; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                    }

                    // Copy row 156 to 162
                    for (int rownum = 155; rownum <= 161; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        oldDataCell = oldDatarow.getCell(oldCol);
                        evaluatorOld.evaluateFormulaCell(oldDataCell);
                        if(oldDataCell != null && (oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
                            data = oldDataCell.getNumericCellValue();
                            ws.getRow(rownum).getCell(col).setCellValue(data);
                        }
                        if(oldDataCell != null && (oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA)) {
                            String formula = oldDataCell.getCellFormula();
                            ws.getRow(rownum).getCell(col).setCellFormula(formula);
                            evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                        }
                    }

                }
            }

            // Copy Additional rows if any
            row = ws.getRow(1);
            rowOld = oldWs.getRow(1);
            Row additionalRow = oldWs.getRow(170);
            isFirstColumnCopied = false;
            if(additionalRow != null) {
                for (int col = 1; col <= row.getLastCellNum(); col++) {
                    int oldCol = 0;
                    if (col <=12) {
                        Cell cell = row.getCell(col);
                        evaluator.evaluateFormulaCell(cell);
                        String date = "" + cell.getNumericCellValue();
                        for (int i = 1; i <= 12; i++) {
                            if(rowOld != null) {
                                Cell cellOld = rowOld.getCell(i);
                                evaluatorOld.evaluateFormulaCell(cellOld);
                                if ((cellOld.getNumericCellValue() + "").equals(date)) {
                                    oldCol = i;
                                    break;
                                }
                            }
                        }
                    } else  {
                        oldCol = col;
                    }
                    Row oldDatarow = null;
                    Cell oldDataCell = null;
                    double data = 0.0;
                    String strData = "";
                    if (!isFirstColumnCopied) {
                        for (int rownum = 170; rownum <= oldWs.getLastRowNum(); rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(0);
                                if (oldDataCell != null) {
                                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                                    strData = oldDataCell.getStringCellValue();
                                    if(ws.getRow(rownum) == null) {
                                        ws.createRow(rownum);
                                    }
                                    if(ws.getRow(rownum).getCell(0) == null) {
                                        ws.getRow(rownum).createCell(0);
                                    }
                                    ws.getRow(rownum).getCell(0).setCellValue(strData);
                                    int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                    XSSFCellStyle newCellStyle = wb.createCellStyle();
                                    newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                    ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);

                                    // Get the comment from the old sheet cell
                                       /* Comment comment = oldDataCell.getCellComment();
                                        if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                            // Create a drawing object in the destination sheet
                                            XSSFDrawing drawing = ws.createDrawingPatriarch();
                                            // Create a comment object using the drawing object
                                            XSSFComment newComment = drawing.createCellComment(drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5));
                                            // Set the comment text
                                            newComment.setString(comment.getString());
                                            // Set the author of the comment
                                            newComment.setAuthor(comment.getAuthor());
                                            // Set the cell reference of the comment
                                            newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                            // Set the comment to the destination sheet cell
                                            ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                        }*/
                                }
                            }
                        }
                        isFirstColumnCopied = true;
                    }
                    if (oldCol != 0) {
                        // Copy row 162 to last row
                        for (int rownum = 170; rownum <= oldWs.getLastRowNum(); rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(oldCol);
                                if (oldDataCell != null) {
                                    evaluatorOld.evaluateFormulaCell(oldDataCell);
                                    if (oldDataCell.getCellType() != Cell.CELL_TYPE_BLANK) {
                                        if(oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                            data = oldDataCell.getNumericCellValue();
                                            if (ws.getRow(rownum) == null) {
                                                ws.createRow(rownum);
                                            }
                                            if (ws.getRow(rownum).getCell(col) == null) {
                                                ws.getRow(rownum).createCell(col);
                                            }
                                            ws.getRow(rownum).getCell(col).setCellValue(data);
                                            int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                            ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                        }
                                        if(oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                            String formula = oldDataCell.getCellFormula();
                                            if (ws.getRow(rownum) == null) {
                                                ws.createRow(rownum);
                                            }
                                            if (ws.getRow(rownum).getCell(col) == null) {
                                                ws.getRow(rownum).createCell(col);
                                            }
                                            if (!formula.isEmpty()) {
                                                ws.getRow(rownum).getCell(col).setCellFormula(formula);
                                                evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                                            }
                                            int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                            ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            oldWb.close();
            String fileName = newFile.getAbsoluteFile().toString();
            System.out.println("Copied SegmentAnalysis Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (Exception e) {
            System.out.println("Exception while copying SegmentAnalysis Sheet for " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")));
            e.printStackTrace();
        }
    }

    private static void copyValuationHistorySheet(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("ValuationHistory");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("ValuationHistory");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator evaluatorOld = oldWb.getCreationHelper().createFormulaEvaluator();

            //if it is an old Excel
            int startRowNum = -1, endRowNumOld = -1;

            if (oldWs.getLastRowNum() < 30) {
                startRowNum = 1;
                endRowNumOld = oldWs.getLastRowNum();
                copyrowsForValuationHistoryModel(wb, ws, oldWb, oldWs, evaluator, evaluatorOld, startRowNum, endRowNumOld);
            } else {
                // Copy first Model history
                startRowNum = 1;
                endRowNumOld = 29;
                copyrowsForValuationHistoryModel(wb, ws, oldWb, oldWs, evaluator, evaluatorOld, startRowNum, endRowNumOld);
                // Copy Second Model History
                startRowNum = 31;
                endRowNumOld = oldWs.getLastRowNum();
                copyrowsForValuationHistoryModel(wb, ws, oldWb, oldWs, evaluator, evaluatorOld, startRowNum, endRowNumOld);
            }



            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            oldWb.close();
            String fileName = newFile.getAbsoluteFile().toString();
            System.out.println("Copied ValuationHistory Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (IOException e) {
            System.out.println("Exception while copying ValuationHistory Sheet for " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")));
            e.printStackTrace();
        }
    }

    private static void copyrowsForValuationHistoryModel(XSSFWorkbook wb, XSSFSheet ws,  XSSFWorkbook oldWb, XSSFSheet oldWs, FormulaEvaluator evaluator, FormulaEvaluator evaluatorOld, int startRowNum, int endRowNumOld){

        int lastMatchingRowNo = 0;
        // Loop through each row in the new workbook
        // Copy default 7 rows
        for (int i = startRowNum; i <= startRowNum+7; i++) {
            XSSFRow row = ws.getRow(i);
            if (row != null) {
                XSSFCell firstColumnCell = row.getCell(0);

                // Check if the first column cell is not null
                try {
                    if (firstColumnCell != null) {
                        CellValue firstColumnValue = evaluator.evaluate(firstColumnCell);

                        // Loop through each row in the old workbook
                        for (int j = startRowNum; j <= endRowNumOld; j++) {
                            XSSFRow oldRow = oldWs.getRow(j);
                            if (oldRow != null) {
                                XSSFCell oldFirstColumnCell = oldRow.getCell(0);

                                // Check if the first column cell in the old workbook is not null
                                if (oldFirstColumnCell != null) {
                                    CellValue oldFirstColumnValue = evaluatorOld.evaluate(oldFirstColumnCell);

                                    if (firstColumnValue != null && oldFirstColumnValue != null && oldFirstColumnValue.getStringValue() != null) {
                                        // Check if the first column values match
                                        if (firstColumnValue.getStringValue().equals(oldFirstColumnValue.getStringValue())) {
                                            lastMatchingRowNo = j;
                                            // Copy the row from the old workbook to the new workbook
                                            for (int k = 0; k < row.getLastCellNum(); k++) {
                                                XSSFCell oldCell = oldRow.getCell(k);
                                                XSSFCell newCell = row.getCell(k);
                                                if (oldCell != null) {
                                                    if (oldCell.getCellType() == Cell.CELL_TYPE_STRING) {
                                                        newCell.setCellType(Cell.CELL_TYPE_BLANK);
                                                        newCell.setCellValue(evaluatorOld.evaluate(oldCell).getStringValue());
                                                    } else if (oldCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                                        newCell.setCellType(Cell.CELL_TYPE_BLANK);
                                                        newCell.setCellValue(evaluatorOld.evaluate(oldCell).getNumberValue());
                                                    }

                                                    int oldCellStyleIndex = oldCell.getCellStyle().getIndex();
                                                    XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                    newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                    newCell.setCellStyle(newCellStyle);
                                                }
                                            }
                                            break;
                                        }

                                    }

                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error copying file: " + e.getMessage());
                }
            }
        }
        // Copy additional rows beyond default 7 rows
        int j = startRowNum+6;
//        System.out.println("2.0 lastMatchingRowNo - "+ lastMatchingRowNo + " & endRowNumOld - " + endRowNumOld);
        if (lastMatchingRowNo > 0) {
            for (int i = lastMatchingRowNo + 1; i <= endRowNumOld; i++) {
                XSSFRow oldRow = oldWs.getRow(i);
                if (oldRow == null) {
                    continue;
                }
                j++;
                XSSFRow row = ws.createRow(j);

                for (int k = 0; k < oldRow.getLastCellNum(); k++) {
                    XSSFCell oldCell = oldRow.getCell(k);
                    XSSFCell newCell = row.createCell(k);

                    if (oldCell != null) {
                        if (oldCell.getCellType() == Cell.CELL_TYPE_STRING) {
                            newCell.setCellValue(evaluatorOld.evaluate(oldCell).getStringValue());
                            newCell.setCellValue(evaluator.evaluate(newCell).getStringValue());
                        } else if (oldCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            newCell.setCellValue(evaluatorOld.evaluate(oldCell).getNumberValue());
                            newCell.setCellValue(evaluator.evaluate(newCell).getNumberValue());
                        }

                        int oldCellStyleIndex = oldCell.getCellStyle().getIndex();
                        XSSFCellStyle newCellStyle = wb.createCellStyle();
                        newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                        newCell.setCellStyle(newCellStyle);
                    }
                }
            }
        }
    }

    private static void copyAnalystRecoSheet(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("AnalystReco");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("AnalystReco");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator evaluatorOld = oldWb.getCreationHelper().createFormulaEvaluator();

            //Copy Header from the old file
            XSSFRow newHeaderRow = ws.getRow(0);
            XSSFRow oldHeaderRow = ws.getRow(0);
            if (newHeaderRow.getLastCellNum() == oldHeaderRow.getLastCellNum()) {
                for (int i = 0; i <= newHeaderRow.getLastCellNum(); i++) {
                    XSSFCell oldCell = oldHeaderRow.getCell(i);
                    XSSFCell newCell = newHeaderRow.getCell(i);
                    if (oldCell!= null) {
                        if(newCell == null)
                            newHeaderRow.createCell(i);
                        newCell.setCellValue(oldCell.getStringCellValue());
                    }
                }
            }

            // Loop through each row in the new workbook
            for (int i = 1; i <= ws.getLastRowNum(); i++) {
                XSSFRow row = ws.getRow(i);
                XSSFCell firstColumnCell = row.getCell(0);
                XSSFCell sixthColumnCell = row.getCell(5);

                // Check if the first column cell is not null
                if (firstColumnCell != null && sixthColumnCell != null) {
                    CellValue firstColumnValue = evaluator.evaluate(firstColumnCell);
                    CellValue sixthColumnValue = evaluator.evaluate(sixthColumnCell);

                    // Loop through each row in the old workbook
                    for (int j = 1; j <= oldWs.getLastRowNum(); j++) {
                        XSSFRow oldRow = oldWs.getRow(j);
                        XSSFCell oldFirstColumnCell = oldRow.getCell(0);
                        XSSFCell oldSixthColumnCell = oldRow.getCell(5);

                        // Check if the first column cell in the old workbook is not null
                        if (oldFirstColumnCell != null && oldSixthColumnCell != null) {
                            CellValue oldFirstColumnValue = evaluatorOld.evaluate(oldFirstColumnCell);
                            CellValue oldSixthColumnValue = evaluatorOld.evaluate(oldSixthColumnCell);

                            // Check if the first column values match
                            if (firstColumnValue != null && oldFirstColumnValue != null &&
                                    sixthColumnValue != null && oldSixthColumnValue != null &&
                                    oldFirstColumnValue.getStringValue() != null && oldSixthColumnValue.getStringValue() != null &&
                                    firstColumnValue.getStringValue().equals(oldFirstColumnValue.getStringValue()) &&
                                    sixthColumnValue.getStringValue().equals(oldSixthColumnValue.getStringValue())) {
                                // Copy the row from the old workbook to the new workbook
                                for (int k = 1; k < row.getLastCellNum(); k++) {
                                    if (k == 4 || k ==8 || k ==12 || k == 16 || k == 29)
                                        continue;
                                    // if old excel is having less columns then copy Analyst Name seperatly which is at position 27
                                    if (oldWs.getRow(0).getLastCellNum() < ws.getRow(0).getLastCellNum() && k == 26) {
                                        XSSFCell oldCell = oldRow.getCell(26);
                                        XSSFCell newCell = row.getCell(39);
                                        if (newCell == null)
                                            newCell = row.createCell(39);
                                        if (oldCell == null)
                                            continue;
                                        if (oldCell != null) {
                                            // Copy Analyst Names
                                            if (oldCell.getCellType() == Cell.CELL_TYPE_STRING && evaluatorOld.evaluate(oldCell) != null && evaluatorOld.evaluate(oldCell).getStringValue() != null) {
                                                newCell.setCellValue(evaluatorOld.evaluate(oldCell).getStringValue());
                                            }
                                        }
                                    } else if (oldWs.getRow(0).getLastCellNum() < ws.getRow(0).getLastCellNum() && k == 27) {
                                        continue;
                                    }
                                    else {
                                        XSSFCell oldCell = oldRow.getCell(k);
                                        XSSFCell newCell = row.getCell(k);
                                        if (newCell == null)
                                            newCell = row.createCell(k);
                                        if (oldCell == null)
                                            continue;

                                        if (oldCell != null) {
                                            if (oldCell.getCellType() == Cell.CELL_TYPE_STRING && evaluatorOld.evaluate(oldCell) != null && evaluatorOld.evaluate(oldCell).getStringValue() != null) {
                                                newCell.setCellValue(evaluatorOld.evaluate(oldCell).getStringValue());
                                            } else if (oldCell.getCellType() == Cell.CELL_TYPE_NUMERIC && evaluatorOld.evaluate(oldCell) != null) {
                                                newCell.setCellValue(evaluatorOld.evaluate(oldCell).getNumberValue());
                                            }
                                            if ((k == 13 || k == 14 || k == 15) && oldCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                                String forumula = oldCell.getCellFormula();
                                                newCell.setCellFormula(forumula);
                                                evaluator.evaluate(newCell);
                                            }
                                            int oldCellStyleIndex = oldCell.getCellStyle().getIndex();
                                            XSSFCellStyle newCellStyle = wb.createCellStyle();
                                            newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                            newCell.setCellStyle(newCellStyle);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            // For Tech companies
            XSSFCell oldCell_Row1_Col17 = oldWs.getRow(1).getCell(17);

            if (oldCell_Row1_Col17.getCellType() == Cell.CELL_TYPE_FORMULA) {
                for (int i = 1;  i <= 120; i++ ) {

                    XSSFCell oldCell17 = oldWs.getRow(i).getCell(17);
                    XSSFCell oldCell18 = oldWs.getRow(i).getCell(18);
                    XSSFCell oldCell19 = oldWs.getRow(i).getCell(19);

                    XSSFCell cell17 = ws.getRow(i).getCell(17);
                    XSSFCell cell18 = ws.getRow(i).getCell(18);
                    XSSFCell cell19 = ws.getRow(i).getCell(19);

                    if (cell17 != null && oldCell17 != null && oldCell17.getCellType()==Cell.CELL_TYPE_FORMULA && oldCell17.getCellFormula() != null) {
                        cell17.setCellFormula(oldCell17.getCellFormula());
                        evaluator.evaluate(cell17);
                    }
                    if (cell18 != null && oldCell18 != null && oldCell18.getCellType()==Cell.CELL_TYPE_FORMULA && oldCell18.getCellFormula() != null) {
                        cell18.setCellFormula(oldCell18.getCellFormula());
                        evaluator.evaluate(cell18);
                    }
                    if (cell19 != null && oldCell19 != null && oldCell19.getCellType()==Cell.CELL_TYPE_FORMULA && oldCell19.getCellFormula() != null) {
                        cell19.setCellFormula(oldCell19.getCellFormula());
                        evaluator.evaluate(cell19);
                    }
                    // set style
                    int oldCellStyleIndex = oldCell_Row1_Col17.getCellStyle().getIndex();
                    XSSFCellStyle newCellStyle = wb.createCellStyle();
                    newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                    cell17.setCellStyle(newCellStyle);
                    cell18.setCellStyle(newCellStyle);
                    cell19.setCellStyle(newCellStyle);
                }
            }

            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            oldWb.close();
            String fileName = newFile.getAbsoluteFile().toString();
            System.out.println("Copied AnalystReco Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (IOException e) {
            System.out.println("Exception while copying AnalystReco Sheet for " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")));
            e.printStackTrace();
        }
    }

    private static void copyHistoryAndRatioSheet(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("History&Ratio");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("History&Ratio");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator evaluatorOld = oldWb.getCreationHelper().createFormulaEvaluator();

            Row row = ws.getRow(1);
            Row rowOld = oldWs.getRow(1);

            // Copy data if header match
            for (int col = 1; col <= 14; col++) {
                Cell cell = row.getCell(col);
                evaluator.evaluateFormulaCell(cell);
                String date = "" + cell.getNumericCellValue();

                int oldCol = 0;
                for (int i = 1; i <= 14; i++) {
                    Cell cellOld = rowOld.getCell(i);
                    evaluatorOld.evaluateFormulaCell(cellOld);
                    if ((cellOld.getNumericCellValue() + "").equals(date)) {
                        oldCol = i;
                        break;
                    }
                }
                Row oldDatarow = null;
                Cell oldDataCell = null;
                double data = 0.0;
                if (oldCol != 0) {
                    // Copy row 4 to 85
                    for (int rownum = 3; rownum <= 84; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        if (oldDatarow != null) {
                            oldDataCell = oldDatarow.getCell(oldCol);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            if (ws.getRow(rownum) != null && ws.getRow(rownum).getCell(col) == null) {
                                ws.getRow(rownum).createCell(col);
                            }
                            if(oldDataCell != null) {
                                if (oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                    ws.getRow(rownum).getCell(col).setCellType(Cell.CELL_TYPE_NUMERIC);
                                    data = oldDataCell.getNumericCellValue();
                                    ws.getRow(rownum).getCell(col).setCellValue(data);
                                }
                                if (oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA){
                                    data = evaluatorOld.evaluate(oldDataCell).getNumberValue();
                                    ws.getRow(rownum).getCell(col).setCellValue(data);
                                }
                                if (oldDataCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    ws.getRow(rownum).getCell(col).setCellType(Cell.CELL_TYPE_BLANK);
                                }
                            }
                        }
                    }
                }
            }

            // Copy additional data from Annual Report
            row = ws.getRow(1);
            rowOld = oldWs.getRow(1);
            // Copy data if header match
            for (int col = 26; col <= 29; col++) {
                int oldCol = 0;
                if(col < 29) {
                    Cell cell = row.getCell(col);
                    evaluator.evaluateFormulaCell(cell);
                    String date = "" + cell.getNumericCellValue();


                    for (int i = 26; i <= 29; i++) {
                        Cell cellOld = rowOld.getCell(i);
                        evaluatorOld.evaluateFormulaCell(cellOld);
                        if(cellOld.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            if (cellOld != null && (cellOld.getNumericCellValue() + "").equals(date)) {
                                oldCol = i;
                                break;
                            }
                        } else if(cellOld.getCellType() == Cell.CELL_TYPE_STRING) {
                            if (cellOld != null && cellOld.getStringCellValue().equals(date)) {
                                oldCol = i;
                                break;
                            }
                        } else if(cellOld.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            if (cellOld != null && (cellOld.getNumericCellValue() + "").equals(date)) {
                                oldCol = i;
                                break;
                            }
                        }

                    }
                } else if (col == 29) {
                    oldCol = 29;
                }
                Row oldDatarow = null;
                Cell oldDataCell = null;
                double data = 0.0;
                if (oldCol != 0) {
                    // Copy row 4 to 85
                    for (int rownum = 3; rownum <= 84; rownum++) {
                        oldDatarow = oldWs.getRow(rownum);
                        if (oldDatarow != null) {
                            oldDataCell = oldDatarow.getCell(oldCol);
                            evaluatorOld.evaluateFormulaCell(oldDataCell);
                            if(oldDataCell != null) {
                                if(ws.getRow(rownum) == null) {
                                    ws.createRow(rownum);
                                }
                                if(ws.getRow(rownum) != null && ws.getRow(rownum).getCell(col) == null) {
                                    ws.getRow(rownum).createCell(col);
                                }
                                if (oldDataCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                    data = oldDataCell.getNumericCellValue();
                                    ws.getRow(rownum).getCell(col).setCellValue(data);
                                }
                                if (oldDataCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                    String formula = oldDataCell.getCellFormula();
                                    ws.getRow(rownum).getCell(col).setCellValue(formula);
                                    evaluator.evaluateFormulaCell(ws.getRow(rownum).getCell(col));
                                }
                                if (oldDataCell.getCellType() == Cell.CELL_TYPE_STRING) {
                                    String notes = oldDataCell.getStringCellValue();
                                    ws.getRow(rownum).getCell(col).setCellValue(notes);
                                }
                                if (oldDataCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    ws.getRow(rownum).getCell(col).setCellType(Cell.CELL_TYPE_BLANK);
                                }
                            }
                        }
                    }
                }
            }

            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            oldWb.close();
            String fileName = newFile.getAbsoluteFile().toString();
            System.out.println("Copied History&Ratio Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (Exception e) {
            System.out.println("Exception while copying History&Ratio Sheet for " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")));
            e.printStackTrace();
        }
    }

    private static void changeDataSheet(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("Data Sheet");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("Data Sheet");

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator evaluatorOld = oldWb.getCreationHelper().createFormulaEvaluator();

            Row oldDatarow = null;
            Cell oldDataCell = null;

            oldDatarow = oldWs.getRow(0);
            oldDataCell = oldDatarow.getCell(1);
            String strData = oldDataCell.getStringCellValue();
            ws.getRow(0).getCell(1).setCellValue(strData);

            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            oldWb.close();
            String fileName = newFile.getAbsoluteFile().toString();
            System.out.println("Copied Data Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyConditionalFormattingRules(XSSFWorkbook oldWb, XSSFWorkbook wb, XSSFSheet oldWs, XSSFSheet ws, int rownum, int oldCol, int col) {


        XSSFCell styleCell = oldWs.getRow(rownum).getCell(oldCol);
        XSSFCell cell = ws.getRow(rownum).getCell(col);
        SheetConditionalFormatting oldSheetCF = oldWs.getSheetConditionalFormatting();
        SheetConditionalFormatting sheetCF = oldWs.getSheetConditionalFormatting();
        for(int idx = 0;idx<oldSheetCF.getNumConditionalFormattings();idx++){
            XSSFConditionalFormatting cf = (XSSFConditionalFormatting) oldSheetCF.getConditionalFormattingAt(idx);
            List<CellRangeAddress> cra = Arrays.asList(cf.getFormattingRanges());
            List<CellRangeAddress> newCra = new ArrayList();
            for(CellRangeAddress c:cra){
                if(containsCell(c, styleCell) && !containsCell(c,cell)){
                    newCra.add(new CellRangeAddress(Math.min(c.getFirstRow(), cell.getRowIndex()),Math.max(c.getLastRow(),cell.getRowIndex()),Math.min(c.getFirstColumn(), cell.getColumnIndex()),Math.max(c.getLastColumn(),cell.getColumnIndex())));
                } else{
                    newCra.add(c);
                }
            }
            ArrayList<XSSFConditionalFormattingRule> cfs = new ArrayList();
            for(int ci=0;ci<cf.getNumberOfRules();ci++){
                cfs.add(cf.getRule(ci));
            }

            sheetCF.addConditionalFormatting(newCra.toArray(new CellRangeAddress[newCra.size()]),cfs.toArray(new XSSFConditionalFormattingRule[cfs.size()]));
            sheetCF.removeConditionalFormatting(idx);
        }

    }

    private static boolean containsCell(CellRangeAddress cra, Cell cell){
        if(cra.getFirstRow()<=cell.getRowIndex() && cra.getLastRow()>=cell.getRowIndex()){
            if(cra.getFirstColumn()<=cell.getColumnIndex() && cra.getLastColumn()>=cell.getColumnIndex()){
                return true;
            }
        }
        return false;
    }


}


