package com.timelineofwealth.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

public class UpdateQuarterlyExcels {

    private static final String BASE_PATH = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";

    public static void main(String[] args) throws IOException, EncryptedDocumentException {
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream(BASE_PATH + "\\Analysis\\config.properties");
        prop.load(input);
        input.close();

        String filesToBeUpdated = prop.getProperty("FilesToBeUpdated", "0Days").trim();
        int days = extractDurationInDays(filesToBeUpdated);
        int minutes = extractDurationInMinutes(filesToBeUpdated);

        String latestFolder = getLatestQuarterFolder();
        String previousFolder = getPreviousQuarter(latestFolder);

        File latestDir = new File(BASE_PATH + File.separator + latestFolder);
        File previousDir = new File(BASE_PATH + File.separator + previousFolder);

        if (!latestDir.exists() || !previousDir.exists()) {
            System.out.println("Required folders not found.");
            return;
        }

        List<File> filesToProcess = getRecentlyModifiedFiles(latestDir, days, minutes);

        for (File newFile : filesToProcess) {
            String newFileName = newFile.getName();
            if (!newFileName.endsWith(".xlsx")) continue;

            String ticker = newFileName.split("_")[0];
            String oldFileName = ticker + "_FY" + previousFolder.substring(2) + ".xlsx";
            File oldFile = new File(previousDir, oldFileName);

            if (!oldFile.exists()) {
                System.out.println("Old file " + oldFileName + " not found in " + previousFolder);
                continue;
            }

            copyQuarterPAndLSheet(oldFile, newFile);
            copyAnnualResultsSheet(oldFile, newFile);
            copySegmentAnalysisSheet(oldFile, newFile);
//            copyValuationHistorySheet(oldFile, newFile);
            copyAnalystRecoSheet(oldFile, newFile);
//            copyHistoryAndRatioSheet(oldFile, newFile);
            copyReports(oldFile, newFile);
            changeDataSheet(oldFile, newFile);
            copyNewSheets(oldFile, newFile);
        }

        CreateFolderStructureForIndustry.updateMCapAndPrice(latestFolder);
        ConsolidatedResultTracker.updateResultTrackerExcel(latestFolder);
    }

    private static int extractDurationInDays(String str) {
        if (str.endsWith("Days")) {
            return Integer.parseInt(str.replace("Days", ""));
        }
        return 0;
    }

    private static int extractDurationInMinutes(String str) {
        if (str.endsWith("Min") || str.endsWith("Mins")) {
            return Integer.parseInt(str.replaceAll("Min[s]?", ""));
        }
        return 0;
    }

    private static List<File> getRecentlyModifiedFiles(File folder, int days, int minutes) {
        long now = System.currentTimeMillis();
        long threshold;

        if (days > 0) {
            threshold = now - days * 24L * 60 * 60 * 1000;
        } else if (minutes > 0) {
            threshold = now - minutes * 60L * 1000;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            threshold = calendar.getTimeInMillis();
        }

        return Arrays.stream(Objects.requireNonNull(folder.listFiles((dir, name) -> name.endsWith(".xlsx"))))
                .filter(file -> {
                    try {
                        FileTime fileTime = Files.getLastModifiedTime(file.toPath());
                        return fileTime.toMillis() >= threshold;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public static String getLatestQuarterFolder() {
        File base = new File(BASE_PATH);
        String[] folders = base.list((dir, name) -> name.matches("\\d{4}Q[1-4]"));
        if (folders == null || folders.length < 2)
            throw new IllegalStateException("Not enough quarter folders");
        Arrays.sort(folders);
        return folders[folders.length - 1];
    }

    private static String getPreviousQuarter(String latest) {
        Pattern p = Pattern.compile("(\\d{4})Q([1-4])");
        Matcher m = p.matcher(latest);
        if (!m.matches()) throw new IllegalArgumentException("Invalid quarter folder name: " + latest);

        int year = Integer.parseInt(m.group(1));
        int quarter = Integer.parseInt(m.group(2));

        if (quarter == 1) {
            return (year - 1) + "Q4";
        } else {
            return year + "Q" + (quarter - 1);
        }
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
                        for (int rownum = 3; rownum <= 219; rownum++) {
                            oldDatarow = oldWs.getRow(rownum);
                            if (oldDatarow != null) {
                                oldDataCell = oldDatarow.getCell(0);
                                evaluatorOld.evaluateFormulaCell(oldDataCell);
                                strData = oldDataCell.getStringCellValue();
                                ws.getRow(rownum).getCell(0).setCellValue(strData);
                                /*try {
                                    int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                    XSSFCellStyle newCellStyle = wb.createCellStyle();
                                    newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                    ws.getRow(rownum).getCell(0).setCellValue(strData);
                                    ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);
                                } catch (Exception e) {  }*/

                                // Get the comment from the old sheet cell
                                Comment comment = oldDataCell.getCellComment();
                                if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                    try {
                                        // Create a drawing object in the destination sheet
                                        XSSFDrawing drawing = ws.createDrawingPatriarch();
                                        // Create a single anchor outside the loop
                                        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5);
                                        // Create a comment object using the drawing object
                                        XSSFComment newComment = drawing.createCellComment(anchor);
                                        // Set the comment text
                                        newComment.setString(comment.getString());
                                        // Set the author of the comment
                                        newComment.setAuthor(comment.getAuthor());
                                        // Set the cell reference of the comment
                                        newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                        // Set the comment to the destination sheet cell
                                        ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                    } catch (Exception e) {
                                        System.out.println("Exception in copying comment... Please check.");
                                        e.printStackTrace();
                                    }
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
                                    /*try {
                                        int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                        XSSFCellStyle newCellStyle = wb.createCellStyle();
                                        newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                        ws.getRow(rownum).getCell(0).setCellValue(strData);
                                        ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);
                                    }catch (Exception e) { }*/

                                    // Get the comment from the old sheet cell
                                    Comment comment = oldDataCell.getCellComment();
                                    if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                        try {
                                            // Create a drawing object in the destination sheet
                                            XSSFDrawing drawing = ws.createDrawingPatriarch();
                                            // Create a single anchor outside the loop
                                            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5);
                                            // Create a comment object using the drawing object
                                            XSSFComment newComment = drawing.createCellComment(anchor);
                                            // Set the comment text
                                            newComment.setString(comment.getString());
                                            // Set the author of the comment
                                            newComment.setAuthor(comment.getAuthor());
                                            // Set the cell reference of the comment
                                            if (ws.getRow(rownum) != null && ws.getRow(rownum).getCell(0) != null && ws.getRow(rownum).getCell(0).getAddress() != null) {
                                                newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                                // Set the comment to the destination sheet cell
                                                ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                            }
                                        } catch (Exception e){
                                            System.out.println("Exception in copying comment... Please check.");
                                            e.printStackTrace();
                                        }
                                    }
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
                                            /*try {
                                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                            } catch (Exception e) {

                                            }*/

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
                                            /*try {
                                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                if (isMathamatical == true)
                                                    ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                                else
                                                    ws.getRow(rownum).getCell(oldCol).setCellStyle(newCellStyle);

//                                            copyConditionalFormattingRules(oldWb, wb, oldWs, ws, rownum, oldCol, col);
                                            }catch (Exception e) {

                                            }*/

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
                        for (int rownum = 3; rownum <= oldWs.getLastRowNum(); rownum++) {
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
                                    /*try {
                                        int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                        XSSFCellStyle newCellStyle = wb.createCellStyle();
                                        newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                        ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);
                                    } catch (Exception e) {

                                    }*/

                                    // Get the comment from the old sheet cell
                                    Comment comment = oldDataCell.getCellComment();
                                    if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                        try {
                                            // Create a drawing object in the destination sheet
                                            XSSFDrawing drawing = ws.createDrawingPatriarch();
                                            // Create a single anchor outside the loop
                                            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5);
                                            // Create a comment object using the drawing object
                                            XSSFComment newComment = drawing.createCellComment(anchor);
                                            // Set the comment text
                                            newComment.setString(comment.getString());
                                            // Set the author of the comment
                                            newComment.setAuthor(comment.getAuthor());
                                            // Set the cell reference of the comment
                                            newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                            // Set the comment to the destination sheet cell
                                            ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                        } catch (Exception e){
                                            System.out.println("Exception in copying comment... Please check.");
                                            e.printStackTrace();
                                        }
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
                                            /*try {
                                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                            } catch (Exception e) {

                                            }*/
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
                                            /*try {
                                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                if (isMathamatical == true)
                                                    ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                                else
                                                    ws.getRow(rownum).getCell(oldCol).setCellStyle(newCellStyle);
                                            } catch (Exception e) {

                                            }*/
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
                            /*try {
                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);
                            } catch (Exception e) {

                            }*/

                            // Get the comment from the old sheet cell
                            Comment comment = oldDataCell.getCellComment();
                            if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                try {
                                    // Create a drawing object in the destination sheet
                                    XSSFDrawing drawing = ws.createDrawingPatriarch();
                                    // Create a single anchor outside the loop
                                    XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5);
                                    // Create a comment object using the drawing object
                                    XSSFComment newComment = drawing.createCellComment(anchor);
                                    // Set the comment text
                                    newComment.setString(comment.getString());
                                    // Set the author of the comment
                                    newComment.setAuthor(comment.getAuthor());
                                    // Set the cell reference of the comment
                                    newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                    // Set the comment to the destination sheet cell
                                    ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                } catch (Exception e){
                                    System.out.println("Exception in copying comment... Please check.");
                                    e.printStackTrace();
                                }
                            }
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
                                    /*try {
                                        int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                        XSSFCellStyle newCellStyle = wb.createCellStyle();
                                        newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                        ws.getRow(rownum).getCell(0).setCellStyle(newCellStyle);
                                    }catch (Exception e) {

                                    }*/

                                    // Get the comment from the old sheet cell
                                    Comment comment = oldDataCell.getCellComment();
                                    if(comment != null && comment.getString() != null && !comment.getString().toString().isEmpty()) {
                                        try {
                                            // Create a drawing object in the destination sheet
                                            XSSFDrawing drawing = ws.createDrawingPatriarch();
                                            // Create a single anchor outside the loop
                                            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 2, 6, 5);
                                            // Create a comment object using the drawing object
                                            XSSFComment newComment = drawing.createCellComment(anchor);
                                            // Set the comment text
                                            newComment.setString(comment.getString());
                                            // Set the author of the comment
                                            newComment.setAuthor(comment.getAuthor());
                                            // Set the cell reference of the comment
                                            if (ws.getRow(rownum) != null && ws.getRow(rownum).getCell(0) != null && ws.getRow(rownum).getCell(0).getAddress() != null) {
                                                newComment.setAddress(ws.getRow(rownum).getCell(0).getAddress());
                                                // Set the comment to the destination sheet cell
                                                ws.getRow(rownum).getCell(0).setCellComment(newComment);
                                            }
                                        } catch (Exception e) {
                                            System.out.println("Exception in copying comment... Please check.");
                                            e.printStackTrace();
                                        }
                                    }
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
                                            /*try {
                                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                            } catch (Exception e) {

                                            }*/
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
                                            /*try {
                                                int oldCellStyleIndex = oldDataCell.getCellStyle().getIndex();
                                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                ws.getRow(rownum).getCell(col).setCellStyle(newCellStyle);
                                            } catch (Exception e) {

                                            }*/
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

                XSSFRow row = null;
                row = ws.getRow(j);
                if (row == null || row.getCell(0) == null || row.getCell(0).getStringCellValue().trim().equals("")) { // Bug Fix: copy rows only if it is blank or null
                    row = ws.createRow(j);

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
            XSSFRow oldHeaderRow = oldWs.getRow(0);
            if (newHeaderRow.getLastCellNum() == oldHeaderRow.getLastCellNum()) {
                for (int i = 0; i <= newHeaderRow.getLastCellNum(); i++) {
                    XSSFCell oldCell = oldHeaderRow.getCell(i);
                    XSSFCell newCell = newHeaderRow.getCell(i);
                    if (oldCell!= null) {
                        if(newCell == null)
                            newHeaderRow.createCell(i);
                        newCell = newHeaderRow.getCell(i);
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
                                            try {
                                                int oldCellStyleIndex = oldCell.getCellStyle().getIndex();
                                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                                newCellStyle.cloneStyleFrom(oldWb.getStylesSource().getStyleAt(oldCellStyleIndex));
                                                newCell.setCellStyle(newCellStyle);
                                            } catch (Exception e) {
                                            }
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

    private static void copyReports(File oldFileRenamed, File newFile) {
        try {
            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("Reports");

            // Open the old workbook
            XSSFWorkbook oldWb = new XSSFWorkbook(new FileInputStream(oldFileRenamed));
            XSSFSheet oldWs = oldWb.getSheet("Reports");

            // Ensure the target sheet is cleared
            if (ws != null) {
                int lastRow = ws.getLastRowNum();
                for (int i = 0; i <= lastRow; i++) {
                    ws.removeRow(ws.getRow(i));
                }
            } else {
                ws = wb.createSheet("Reports");
            }

            // Copy rows from oldWs to ws
            if (oldWs != null) {
                for (int rowIndex = 0; rowIndex <= oldWs.getLastRowNum(); rowIndex++) {
                    XSSFRow oldRow = oldWs.getRow(rowIndex);
                    XSSFRow newRow = ws.createRow(rowIndex);

                    if (oldRow != null) {
                        for (int colIndex = 0; colIndex < oldRow.getLastCellNum(); colIndex++) {
                            XSSFCell oldCell = oldRow.getCell(colIndex);
                            XSSFCell newCell = newRow.createCell(colIndex);

                            if (oldCell != null) {
                                // Copy cell value
                                switch (oldCell.getCellType()) {
                                    case Cell.CELL_TYPE_STRING:
                                        newCell.setCellValue(oldCell.getStringCellValue());
                                        break;
                                    case Cell.CELL_TYPE_NUMERIC:
                                        newCell.setCellValue(oldCell.getNumericCellValue());
                                        break;
                                    default:
                                        newCell.setCellValue(oldCell.toString());
                                        break;
                                }

                                // Copy cell style
                                XSSFCellStyle newCellStyle = wb.createCellStyle();
                                newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                                newCell.setCellStyle(newCellStyle);
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
            System.out.println("Copied Reports Sheet for " + fileName.substring(fileName.lastIndexOf("\\")));
        } catch (Exception e) {
            System.out.println("Exception while copying History&Ratio Sheet for " + newFile.getAbsoluteFile().toString().substring(newFile.getAbsoluteFile().toString().lastIndexOf("\\")));
            e.printStackTrace();
        }
    }

    private static void changeDataSheet(File oldFileRenamed, File newFile) {
        try {
            // Extract ticker from filename
            String fileName = newFile.getName();
            String ticker = fileName.substring(0, fileName.indexOf("_"));

            // Open the new workbook
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(newFile));
            XSSFSheet ws = wb.getSheet("Data Sheet");

            // Set ticker into cell B1 (row 0, cell 1)
            Row row = ws.getRow(0);
            Cell cell = row.getCell(1);
            cell.setCellValue(ticker);

            // Save the workbook
            FileOutputStream fileOut = new FileOutputStream(newFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();

            System.out.println("Updated Data Sheet for " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties properties = new Properties();

    private static void copyNewSheets(File oldFileRenamed, File newFile) {
        FileInputStream oldFis = null;
        FileInputStream newFis = null;
        FileOutputStream fos = null;
        XSSFWorkbook oldWb = null;
        XSSFWorkbook newWb = null;

        try {
            System.out.println("Starting data copy from " + oldFileRenamed.getName() + " to " + newFile.getName());

            // Load properties file ONCE
            properties.load(new FileInputStream("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\copy_config.properties"));

            // OPEN workbooks ONCE
            oldFis = new FileInputStream(oldFileRenamed);
            newFis = new FileInputStream(newFile);
            oldWb = new XSSFWorkbook(oldFis);
            newWb = new XSSFWorkbook(newFis);

            // 1. Copy standalone cells
            copyStandaloneCellsOnlyInMemory(oldWb, newWb);

            // 2. Copy block cells
            copyBlockCellsInMemory(oldWb, newWb);

            // 3. Copy header-based data
            copyHeaderBasedDataInMemory(oldWb, newWb);

            // 4. Copy formatting-only cells
            copyFormattingOnlyCellsInMemory(oldWb, newWb);

            // SAVE workbook ONCE at the end
            fos = new FileOutputStream(newFile);
            newWb.write(fos);

            System.out.println("Data copy completed successfully for " + newFile.getName());

        } catch (Exception e) {
            System.err.println("Error during data copying: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources properly
            try { if (fos != null) fos.close(); } catch (Exception e) {}
            try { if (newWb != null) newWb.close(); } catch (Exception e) {}
            try { if (oldWb != null) oldWb.close(); } catch (Exception e) {}
            try { if (newFis != null) newFis.close(); } catch (Exception e) {}
            try { if (oldFis != null) oldFis.close(); } catch (Exception e) {}
        }
    }

    private static void copyFormattingOnlyCellsInMemory(XSSFWorkbook oldWb, XSSFWorkbook newWb) throws IOException {
        System.out.println("\n[STEP 4] Copying formatting-only cells...");

        // Parse formatting-only cell configurations from properties
        Map<String, List<CellRange>> sheetFormattingCells = parseFormattingCells();

        if (sheetFormattingCells.isEmpty()) {
            System.out.println("No formatting-only cells found in properties file.");
            return;
        }

        // Process each sheet configuration
        for (Map.Entry<String, List<CellRange>> entry : sheetFormattingCells.entrySet()) {
            String sheetName = entry.getKey();
            List<CellRange> formattingRanges = entry.getValue();

            if (formattingRanges.isEmpty()) continue;

            Sheet oldSheet = oldWb.getSheet(sheetName);
            Sheet newSheet = newWb.getSheet(sheetName);

            if (oldSheet == null || newSheet == null) {
                System.err.println("  Sheet '" + sheetName + "' not found in one of the files");
                continue;
            }

            int totalFormatted = 0;

            // Copy formatting for each range
            for (CellRange range : formattingRanges) {
                int formattedCount = copyFormattingRangeBulk(oldSheet, newSheet, range);
                totalFormatted += formattedCount;
            }

            System.out.println("  " + sheetName + ": " + totalFormatted + " cells formatted");
        }
        System.out.println("Formatting-only cells copy completed.");
    }

    private static void copyHeaderBasedDataInMemory(XSSFWorkbook oldWb, XSSFWorkbook newWb) throws IOException {
        System.out.println("\n[STEP 3] Copying header-based data...");

        FormulaEvaluator oldEvaluator = oldWb.getCreationHelper().createFormulaEvaluator();
        FormulaEvaluator newEvaluator = newWb.getCreationHelper().createFormulaEvaluator();

        Map<String, SheetHeaderConfig> sheetHeaderConfigs = parseHeaderBasedConfigs();

        if (sheetHeaderConfigs.isEmpty()) {
            System.out.println("No header-based configurations found.");
            return;
        }

        for (Map.Entry<String, SheetHeaderConfig> entry : sheetHeaderConfigs.entrySet()) {
            String sheetName = entry.getKey();
            SheetHeaderConfig config = entry.getValue();

            Sheet oldSheet = oldWb.getSheet(sheetName);
            Sheet newSheet = newWb.getSheet(sheetName);

            if (oldSheet == null || newSheet == null) {
                continue;
            }

            // Process all ranges
            if (config.finYearHeaderRange != null && !config.finYearDataRanges.isEmpty()) {
                processHeaderBasedRanges(oldSheet, newSheet, oldEvaluator, newEvaluator,
                        config.finYearHeaderRange, config.finYearDataRanges, "");
            }

            if (config.quarterHeaderRange != null && !config.quarterDataRanges.isEmpty()) {
                processHeaderBasedRanges(oldSheet, newSheet, oldEvaluator, newEvaluator,
                        config.quarterHeaderRange, config.quarterDataRanges, "");
            }

            if (config.expFinYearHeaderRange != null && !config.expFinYearDataRanges.isEmpty()) {
                processHeaderBasedRanges(oldSheet, newSheet, oldEvaluator, newEvaluator,
                        config.expFinYearHeaderRange, config.expFinYearDataRanges, "");
            }

            if (config.expQuarterHeaderRange != null && !config.expQuarterDataRanges.isEmpty()) {
                processHeaderBasedRanges(oldSheet, newSheet, oldEvaluator, newEvaluator,
                        config.expQuarterHeaderRange, config.expQuarterDataRanges, "");
            }
        }
        System.out.println("Header-based data copy completed.");
    }

    private static void copyBlockCellsInMemory(XSSFWorkbook oldWb, XSSFWorkbook newWb) throws IOException {
        System.out.println("\n[STEP 2] Copying block cells...");

        // Parse block cell configurations from properties
        Map<String, List<CellRange>> sheetBlockCells = parseBlockCells();

        if (sheetBlockCells.isEmpty()) {
            System.out.println("No block cells found in properties file.");
            return;
        }

        // Process each sheet configuration
        for (Map.Entry<String, List<CellRange>> entry : sheetBlockCells.entrySet()) {
            String sheetName = entry.getKey();
            List<CellRange> blockRanges = entry.getValue();

            if (blockRanges.isEmpty()) continue;

            Sheet oldSheet = oldWb.getSheet(sheetName);
            Sheet newSheet = newWb.getSheet(sheetName);

            if (oldSheet == null || newSheet == null) {
                System.err.println("  Sheet '" + sheetName + "' not found in one of the files");
                continue;
            }

            int totalCopied = 0;
            int totalSkipped = 0;

            // Copy each block range
            for (CellRange range : blockRanges) {
                int[] results = copyBlockRange(oldSheet, newSheet, range);
                totalCopied += results[0];
                totalSkipped += results[1];
            }

            System.out.println("  " + sheetName + ": " + totalCopied + " cells copied, " + totalSkipped + " skipped");
        }
        System.out.println("Block cells copy completed.");
    }

    private static void copyStandaloneCellsOnlyInMemory(XSSFWorkbook oldWb, XSSFWorkbook newWb) throws IOException {
        System.out.println("\n[STEP 1] Copying standalone cells...");

        // Parse sheet configurations from properties
        Map<String, List<String>> sheetStandaloneCells = parseStandaloneCells();

        // Process each sheet configuration
        for (Map.Entry<String, List<String>> entry : sheetStandaloneCells.entrySet()) {
            String sheetName = entry.getKey();
            List<String> cellRefs = entry.getValue();

            if (cellRefs.isEmpty()) continue;

            Sheet oldSheet = oldWb.getSheet(sheetName);
            Sheet newSheet = newWb.getSheet(sheetName);

            if (oldSheet == null || newSheet == null) {
                System.err.println("  Sheet '" + sheetName + "' not found in one of the files");
                continue;
            }

            int copiedCount = 0;
            int skippedCount = 0;

            // Copy each standalone cell
            for (String cellRef : cellRefs) {
                boolean copied = copySingleCell(oldSheet, newSheet, cellRef);
                if (copied) {
                    copiedCount++;
                } else {
                    skippedCount++;
                }
            }

            System.out.println("  " + sheetName + ": " + copiedCount + " cells copied, " + skippedCount + " skipped");
        }
        System.out.println("Standalone cells copy completed.");
    }

    private static Map<String, List<String>> parseStandaloneCells() {
        Map<String, List<String>> sheetCells = new HashMap<String, List<String>>();
        List<String> currentCellList = null;
        String currentSheet = null;

        String propertiesPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\copy_config.properties";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(propertiesPath));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Check if this is a sheet header
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSheet = line.substring(1, line.length() - 1);
                    currentCellList = new ArrayList<String>();
                    sheetCells.put(currentSheet, currentCellList);
                    continue;
                }

                if (currentSheet == null || currentCellList == null) {
                    continue;
                }

                // Check if this line contains StandaloneCells
                if (line.startsWith("StandaloneCells")) {
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String value = line.substring(equalsIndex + 1).trim();
                        List<String> cells = parseSimpleCellList(value);
                        currentCellList.addAll(cells);
                    }
                }
            }
            reader.close();

        } catch (Exception e) {
            System.err.println("Error reading properties file: " + e.getMessage());
        }

        return sheetCells;
    }

    private static List<String> parseSimpleCellList(String cellList) {
        List<String> cells = new ArrayList<String>();

        if (cellList == null || cellList.trim().isEmpty()) {
            return cells;
        }

        // Split by comma
        String[] parts = cellList.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                cells.add(trimmed.toUpperCase());
            }
        }

        return cells;
    }

    private static boolean copySingleCell(Sheet oldSheet, Sheet newSheet, String cellRef) {
        try {
            CellReference ref = new CellReference(cellRef);
            int rowIdx = ref.getRow();
            int colIdx = ref.getCol();

            // Get the cell from old sheet
            Row oldRow = oldSheet.getRow(rowIdx);
            if (oldRow == null) {
                return false;
            }

            Cell oldCell = oldRow.getCell(colIdx);
            if (oldCell == null) {
                return false;
            }

            // Check old cell type
            int oldCellType = oldCell.getCellType();

            // Skip if old is formula
            if (oldCellType == Cell.CELL_TYPE_FORMULA) {
                return false;
            }

            // Get or create the cell in new sheet
            Row newRow = newSheet.getRow(rowIdx);
            if (newRow == null) {
                newRow = newSheet.createRow(rowIdx);
            }

            Cell newCell = newRow.getCell(colIdx);
            if (newCell == null) {
                newCell = newRow.createCell(colIdx);
            }

            // Check new cell type
            int newCellType = newCell.getCellType();

            // Skip if both are formulas
            if (oldCellType == Cell.CELL_TYPE_FORMULA && newCellType == Cell.CELL_TYPE_FORMULA) {
                return false;
            }

            // Save the existing style from new cell
            CellStyle existingStyle = newCell.getCellStyle();

            // Clear any formula in the new cell
            if (newCellType == Cell.CELL_TYPE_FORMULA) {
                newCell.setCellType(Cell.CELL_TYPE_BLANK);
            }

            // Copy ONLY THE VALUE from old to new, preserve new cell's style
            switch (oldCellType) {
                case Cell.CELL_TYPE_STRING:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;

                case Cell.CELL_TYPE_NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());

                    // Special handling for dates
                    if (DateUtil.isCellDateFormatted(oldCell)) {
                        CellStyle dateStyle = newCell.getSheet().getWorkbook().createCellStyle();
                        if (existingStyle != null) {
                            dateStyle.cloneStyleFrom(existingStyle);
                        }
                        CellStyle oldDateStyle = oldCell.getCellStyle();
                        if (oldDateStyle != null) {
                            dateStyle.setDataFormat(oldDateStyle.getDataFormat());
                        }
                        newCell.setCellStyle(dateStyle);
                    }
                    break;

                case Cell.CELL_TYPE_BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;

                case Cell.CELL_TYPE_BLANK:
                    // Keep it blank
                    break;

                case Cell.CELL_TYPE_ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;

                default:
                    return false;
            }

            // Set the cell type based on old cell
            newCell.setCellType(oldCellType);

            // If we didn't apply a special style (like for dates), restore the original style
            if (oldCellType != Cell.CELL_TYPE_NUMERIC || !DateUtil.isCellDateFormatted(oldCell)) {
                if (existingStyle != null) {
                    newCell.setCellStyle(existingStyle);
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error copying cell " + cellRef + ": " + e.getMessage());
            return false;
        }
    }

    private static Map<String, List<CellRange>> parseBlockCells() {
        Map<String, List<CellRange>> sheetBlockCells = new HashMap<String, List<CellRange>>();
        String currentSheet = null;

        String propertiesPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\copy_config.properties";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(propertiesPath));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Check if this is a sheet header
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSheet = line.substring(1, line.length() - 1);
                    sheetBlockCells.put(currentSheet, new ArrayList<CellRange>());
                    continue;
                }

                if (currentSheet == null || !sheetBlockCells.containsKey(currentSheet)) {
                    continue;
                }

                // Check if this line contains BlockCellsRange
                if (line.startsWith("BlockCellsRange")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        List<CellRange> ranges = parseBlockRangeList(value);
                        sheetBlockCells.get(currentSheet).addAll(ranges);
                    }
                }
            }

            reader.close();

        } catch (Exception e) {
            System.err.println("Error reading properties file: " + e.getMessage());
        }

        return sheetBlockCells;
    }

    private static List<CellRange> parseBlockRangeList(String rangeList) {
        List<CellRange> ranges = new ArrayList<CellRange>();

        if (rangeList == null || rangeList.trim().isEmpty()) {
            return ranges;
        }

        // Split by comma to handle multiple ranges
        String[] rangeParts = rangeList.split(",");
        for (String rangePart : rangeParts) {
            String trimmed = rangePart.trim();
            if (!trimmed.isEmpty() && trimmed.contains(":")) {
                try {
                    CellRange range = parseSingleRange(trimmed);
                    ranges.add(range);
                } catch (Exception e) {
                    System.err.println("Invalid range format: '" + trimmed + "' - " + e.getMessage());
                }
            }
        }

        return ranges;
    }

    private static CellRange parseSingleRange(String rangeStr) {
        String[] parts = rangeStr.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid range format. Expected 'A1:B2', got: " + rangeStr);
        }

        String start = parts[0].trim().toUpperCase();
        String end = parts[1].trim().toUpperCase();

        // Validate cell references
        CellReference startRef = new CellReference(start);
        CellReference endRef = new CellReference(end);

        // Ensure start is top-left, end is bottom-right
        int startRow = Math.min(startRef.getRow(), endRef.getRow());
        int endRow = Math.max(startRef.getRow(), endRef.getRow());
        int startCol = Math.min(startRef.getCol(), endRef.getCol());
        int endCol = Math.max(startRef.getCol(), endRef.getCol());

        // Convert back to cell references
        String actualStart = new CellReference(startRow, startCol).formatAsString();
        String actualEnd = new CellReference(endRow, endCol).formatAsString();

        return new CellRange(actualStart, actualEnd);
    }

    private static int[] copyBlockRange(Sheet oldSheet, Sheet newSheet, CellRange range) {
        int copied = 0;
        int skipped = 0;

        try {
            CellReference startRef = new CellReference(range.startCell);
            CellReference endRef = new CellReference(range.endCell);

            int startRow = startRef.getRow();
            int endRow = endRef.getRow();
            int startCol = startRef.getCol();
            int endCol = endRef.getCol();

            for (int row = startRow; row <= endRow; row++) {
                Row oldRow = oldSheet.getRow(row);
                if (oldRow == null) {
                    skipped += (endCol - startCol + 1);
                    continue;
                }

                Row newRow = newSheet.getRow(row);
                if (newRow == null) {
                    newRow = newSheet.createRow(row);
                }

                for (int col = startCol; col <= endCol; col++) {
                    Cell oldCell = oldRow.getCell(col);
                    if (oldCell == null) {
                        skipped++;
                        continue;
                    }

                    // Check old cell type
                    int oldCellType = oldCell.getCellType();

                    // Skip if old cell is formula
                    if (oldCellType == Cell.CELL_TYPE_FORMULA) {
                        skipped++;
                        continue;
                    }

                    Cell newCell = newRow.getCell(col);
                    if (newCell == null) {
                        newCell = newRow.createCell(col);
                    }

                    // Copy the cell value
                    if (copyCellValuePreservingStyle(oldCell, newCell)) {
                        copied++;
                    } else {
                        skipped++;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error copying block range " + range.startCell + ":" + range.endCell + ": " + e.getMessage());
        }

        return new int[]{copied, skipped};
    }

    private static boolean copyCellValuePreservingStyle(Cell oldCell, Cell newCell) {
        try {
            int oldCellType = oldCell.getCellType();
            int newCellType = newCell.getCellType();

            // Save the existing style from new cell
            CellStyle existingStyle = newCell.getCellStyle();

            // Clear any formula in the new cell
            if (newCellType == Cell.CELL_TYPE_FORMULA) {
                newCell.setCellType(Cell.CELL_TYPE_BLANK);
            }

            // Copy the value based on old cell type
            switch (oldCellType) {
                case Cell.CELL_TYPE_STRING:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;

                case Cell.CELL_TYPE_NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());

                    // Special handling for dates
                    if (DateUtil.isCellDateFormatted(oldCell)) {
                        CellStyle dateStyle = newCell.getSheet().getWorkbook().createCellStyle();
                        if (existingStyle != null) {
                            dateStyle.cloneStyleFrom(existingStyle);
                        }
                        CellStyle oldDateStyle = oldCell.getCellStyle();
                        if (oldDateStyle != null) {
                            dateStyle.setDataFormat(oldDateStyle.getDataFormat());
                        }
                        newCell.setCellStyle(dateStyle);
                    }
                    break;

                case Cell.CELL_TYPE_BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;

                case Cell.CELL_TYPE_BLANK:
                    // Keep it blank
                    break;

                case Cell.CELL_TYPE_ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;

                default:
                    return false;
            }

            // Set the cell type based on old cell
            newCell.setCellType(oldCellType);

            // If we didn't apply a special style (like for dates), restore the original style
            if (oldCellType != Cell.CELL_TYPE_NUMERIC || !DateUtil.isCellDateFormatted(oldCell)) {
                if (existingStyle != null) {
                    newCell.setCellStyle(existingStyle);
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error copying cell value: " + e.getMessage());
            return false;
        }
    }

    private static Map<String, SheetHeaderConfig> parseHeaderBasedConfigs() {
        Map<String, SheetHeaderConfig> sheetConfigs = new HashMap<String, SheetHeaderConfig>();
        SheetHeaderConfig currentConfig = null;
        String currentSheet = null;

        String propertiesPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\copy_config.properties";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(propertiesPath));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Check if this is a sheet header
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSheet = line.substring(1, line.length() - 1);
                    currentConfig = new SheetHeaderConfig();
                    sheetConfigs.put(currentSheet, currentConfig);
                    continue;
                }

                if (currentSheet == null || currentConfig == null) {
                    continue;
                }

                // Parse regular header ranges
                if (line.startsWith("FinYearHeaderRange=")) {
                    String value = line.substring("FinYearHeaderRange=".length()).trim();
                    currentConfig.finYearHeaderRange = parseSingleRange(value);
                }
                else if (line.startsWith("QuarerHeaderRange=")) {
                    String value = line.substring("QuarerHeaderRange=".length()).trim();
                    currentConfig.quarterHeaderRange = parseSingleRange(value);
                }
                // Parse expanded/forecast header ranges
                else if (line.startsWith("ExpFinYearHeaderRange=")) {
                    String value = line.substring("ExpFinYearHeaderRange=".length()).trim();
                    currentConfig.expFinYearHeaderRange = parseSingleRange(value);
                }
                else if (line.startsWith("ExpQuarerHeaderRange=")) {
                    String value = line.substring("ExpQuarerHeaderRange=".length()).trim();
                    currentConfig.expQuarterHeaderRange = parseSingleRange(value);
                }
                // Parse regular data ranges
                else if (line.startsWith("FinYearDataRange")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        List<CellRange> ranges = parseBlockRangeList(value);
                        currentConfig.finYearDataRanges.addAll(ranges);
                    }
                }
                else if (line.startsWith("QuarerDataRange")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        List<CellRange> ranges = parseBlockRangeList(value);
                        currentConfig.quarterDataRanges.addAll(ranges);
                    }
                }
                // Parse expanded/forecast data ranges
                else if (line.startsWith("ExpFinYearDataRange")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        List<CellRange> ranges = parseBlockRangeList(value);
                        currentConfig.expFinYearDataRanges.addAll(ranges);
                    }
                }
                else if (line.startsWith("ExpQuarerDataRange")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        List<CellRange> ranges = parseBlockRangeList(value);
                        currentConfig.expQuarterDataRanges.addAll(ranges);
                    }
                }
            }

            reader.close();

        } catch (Exception e) {
            System.err.println("Error parsing header configurations: " + e.getMessage());
        }

        return sheetConfigs;
    }

    private static Map<Integer, Integer> createHeaderColumnMapping(Sheet oldSheet, Sheet newSheet,
                                                                   FormulaEvaluator oldEvaluator,
                                                                   FormulaEvaluator newEvaluator,
                                                                   CellRange headerRange) {
        Map<Integer, Integer> columnMapping = new HashMap<Integer, Integer>();
        Map<String, Integer> oldHeaderMap = new HashMap<String, Integer>();

        CellReference startRef = new CellReference(headerRange.startCell);
        CellReference endRef = new CellReference(headerRange.endCell);

        int headerRow = startRef.getRow();
        int startCol = startRef.getCol();
        int endCol = endRef.getCol();

        // 1. Read OLD headers
        Row oldHeaderRow = oldSheet.getRow(headerRow);
        if (oldHeaderRow != null) {
            for (int oldCol = startCol; oldCol <= endCol; oldCol++) {
                Cell oldHeaderCell = oldHeaderRow.getCell(oldCol);
                if (oldHeaderCell != null) {
                    String headerValue = getEvaluatedCellValue(oldHeaderCell, oldEvaluator);
                    if (headerValue != null && !headerValue.trim().isEmpty()) {
                        // Right-most match for duplicates
                        oldHeaderMap.put(headerValue.trim(), oldCol);
                    }
                }
            }
        }

        // 2. Match NEW headers with OLD headers
        Row newHeaderRow = newSheet.getRow(headerRow);
        if (newHeaderRow != null) {
            for (int newCol = startCol; newCol <= endCol; newCol++) {
                Cell newHeaderCell = newHeaderRow.getCell(newCol);
                if (newHeaderCell != null) {
                    String headerValue = getEvaluatedCellValue(newHeaderCell, newEvaluator);
                    if (headerValue != null && !headerValue.trim().isEmpty()) {
                        String trimmedValue = headerValue.trim();
                        Integer oldCol = oldHeaderMap.get(trimmedValue);
                        if (oldCol != null) {
                            columnMapping.put(newCol, oldCol);
                        }
                    }
                }
            }
        }

        return columnMapping;
    }

    // Helper method to convert column index to letter (A, B, C, ... AA, AB, etc.)
    private static String getColumnLetter(int colIndex) {
        StringBuilder column = new StringBuilder();
        while (colIndex >= 0) {
            int remainder = colIndex % 26;
            column.insert(0, (char) ('A' + remainder));
            colIndex = (colIndex / 26) - 1;
        }
        return column.toString();
    }

    // Remove logging from processDataRange too
    private static int processDataRange(Sheet oldSheet, Sheet newSheet,
                                        FormulaEvaluator oldEvaluator, FormulaEvaluator newEvaluator,
                                        Map<Integer, Integer> columnMapping,
                                        CellRange dataRange, CellRange headerRange) {
        int processedCount = 0;

        CellReference dataStartRef = new CellReference(dataRange.startCell);
        CellReference dataEndRef = new CellReference(dataRange.endCell);
        CellReference headerStartRef = new CellReference(headerRange.startCell);

        int dataStartRow = dataStartRef.getRow();
        int dataEndRow = dataEndRef.getRow();
        int dataStartCol = dataStartRef.getCol();
        int dataEndCol = dataEndRef.getCol();
        int headerStartCol = headerStartRef.getCol();

        for (int dataCol = dataStartCol; dataCol <= dataEndCol; dataCol++) {
            int headerCol = headerStartCol + (dataCol - dataStartCol);

            if (!columnMapping.containsKey(headerCol)) {
                continue;
            }

            int oldDataCol = columnMapping.get(headerCol);

            for (int row = dataStartRow; row <= dataEndRow; row++) {
                Row oldRow = oldSheet.getRow(row);
                Row newRow = newSheet.getRow(row);

                if (newRow == null) {
                    newRow = newSheet.createRow(row);
                }

                if (oldRow == null) continue;

                Cell oldCell = oldRow.getCell(oldDataCol);
                if (oldCell == null) continue;

                Cell newCell = newRow.getCell(dataCol);
                if (newCell == null) {
                    newCell = newRow.createCell(dataCol);
                }

                if (shouldCopyCell(oldCell, newCell, oldEvaluator)) {
                    CellStyle originalStyle = newCell.getCellStyle();
                    Object oldValue = getCellValueForCopy(oldCell, oldEvaluator);

                    if (oldValue != null) {
                        if (copyValueToCell(oldValue, newCell)) {
                            if (originalStyle != null) {
                                newCell.setCellStyle(originalStyle);
                            }
                            processedCount++;
                        }
                    }
                }
            }
        }

        return processedCount;
    }

    // FIXED: shouldCopyCell method - allow copying when OLD has formula (we'll evaluate it)
    private static boolean shouldCopyCell(Cell oldCell, Cell newCell, FormulaEvaluator oldEvaluator) {
        int oldCellType = oldCell.getCellType();
        int newCellType = newCell.getCellType();

        // Skip if OLD cell is error
        if (oldCellType == Cell.CELL_TYPE_ERROR) {
            return false;
        }

        // Skip if BOTH are formulas
        if (oldCellType == Cell.CELL_TYPE_FORMULA && newCellType == Cell.CELL_TYPE_FORMULA) {
            return false;
        }

        // Check NEW cell state
        switch (newCellType) {
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_ERROR:
                // Always copy to blank or error cells
                return true;

            case Cell.CELL_TYPE_FORMULA:
                // Replace formulas with values (even if old is formula - we'll evaluate it)
                return true;

            case Cell.CELL_TYPE_STRING:
            case Cell.CELL_TYPE_NUMERIC:
            case Cell.CELL_TYPE_BOOLEAN:
                // Don't overwrite manually entered values
                return false;

            default:
                return false;
        }
    }

    // FIXED: Handle formula evaluation properly
    private static Object getCellValueForCopy(Cell cell, FormulaEvaluator evaluator) {
        int cellType = cell.getCellType();

        // IMPORTANT: If cell is formula, EVALUATE it
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            try {
                CellValue cellValue = evaluator.evaluate(cell);
                if (cellValue != null) {
                    switch (cellValue.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            return cellValue.getStringValue();
                        case Cell.CELL_TYPE_NUMERIC:
                            return cellValue.getNumberValue();
                        case Cell.CELL_TYPE_BOOLEAN:
                            return cellValue.getBooleanValue();
                        case Cell.CELL_TYPE_ERROR:
                            // Skip cells with formula errors
                            return null;
                        default:
                            return null;
                    }
                }
            } catch (Exception e) {
                // Formula evaluation failed
                return null;
            }
            return null;
        }

        // For non-formula cells
        switch (cellType) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_ERROR:
                return null;
            default:
                return null;
        }
    }

    private static boolean copyValueToCell(Object value, Cell targetCell) {
        try {
            if (value == null) {
                targetCell.setCellType(Cell.CELL_TYPE_BLANK);
                return true;
            }

            if (value instanceof String) {
                targetCell.setCellValue((String) value);
                targetCell.setCellType(Cell.CELL_TYPE_STRING);
                return true;
            }
            else if (value instanceof Number) {
                double numValue = ((Number) value).doubleValue();
                targetCell.setCellValue(numValue);
                targetCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                return true;
            }
            else if (value instanceof Boolean) {
                targetCell.setCellValue((Boolean) value);
                targetCell.setCellType(Cell.CELL_TYPE_BOOLEAN);
                return true;
            }
            else if (value instanceof Date) {
                targetCell.setCellValue((Date) value);
                targetCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                return true;
            }
            else if (value instanceof Byte) {
                targetCell.setCellErrorValue((Byte) value);
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error copying value to cell: " + e.getMessage());
            return false;
        }
    }

    private static String getEvaluatedCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }

        int cellType = cell.getCellType();

        // Evaluate formula if present
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            try {
                CellValue cellValue = evaluator.evaluate(cell);
                if (cellValue != null && cellValue.getCellType() == Cell.CELL_TYPE_STRING) {
                    return cellValue.getStringValue();
                }
            } catch (Exception e) {
                // If formula evaluation fails, try to get formula string
                return cell.getCellFormula();
            }
        }

        // For non-formula string cells
        if (cellType == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        }

        // For numeric cells (like year numbers), convert to string
        if (cellType == Cell.CELL_TYPE_NUMERIC) {
            double numValue = cell.getNumericCellValue();
            // Check if it's a whole number (likely a year)
            if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                return String.valueOf((long) numValue);
            }
            return String.valueOf(numValue);
        }

        return null;
    }

    private static int processHeaderBasedRanges(Sheet oldSheet, Sheet newSheet,
                                                FormulaEvaluator oldEvaluator, FormulaEvaluator newEvaluator,
                                                CellRange headerRange, List<CellRange> dataRanges, String rangeType) {
        int totalProcessed = 0;

        try {
            Map<Integer, Integer> columnMapping = createHeaderColumnMapping(oldSheet, newSheet,
                    oldEvaluator, newEvaluator,
                    headerRange);

            if (columnMapping.isEmpty()) {
                return 0;
            }

            for (CellRange dataRange : dataRanges) {
                int rangeProcessed = processDataRange(oldSheet, newSheet, oldEvaluator, newEvaluator,
                        columnMapping, dataRange, headerRange);
                totalProcessed += rangeProcessed;
            }

        } catch (Exception e) {
            // Only log errors
        }

        return totalProcessed;
    }

    // Parser for formatting cells (NEW)
    private static Map<String, List<CellRange>> parseFormattingCells() {
        Map<String, List<CellRange>> sheetFormattingCells = new HashMap<String, List<CellRange>>();
        String currentSheet = null;

        String propertiesPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\copy_config.properties";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(propertiesPath));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Check if this is a sheet header
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSheet = line.substring(1, line.length() - 1);
                    sheetFormattingCells.put(currentSheet, new ArrayList<CellRange>());
                    continue;
                }

                if (currentSheet == null || !sheetFormattingCells.containsKey(currentSheet)) {
                    continue;
                }

                // Check if this line contains FormattingBlockCellsRange
                if (line.startsWith("FormattingBlockCellsRange")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        List<CellRange> ranges = parseBlockRangeList(value);
                        sheetFormattingCells.get(currentSheet).addAll(ranges);
                    }
                }
            }

            reader.close();

        } catch (Exception e) {
            System.err.println("Error reading formatting cells from properties file: " + e.getMessage());
        }

        return sheetFormattingCells;
    }

    // Bulk formatting method (USE THIS - optimized for large ranges)
    private static int copyFormattingRangeBulk(Sheet oldSheet, Sheet newSheet, CellRange range) {
        try {
            CellReference startRef = new CellReference(range.startCell);
            CellReference endRef = new CellReference(range.endCell);

            int startRow = startRef.getRow();
            int endRow = endRef.getRow();
            int startCol = startRef.getCol();
            int endCol = endRef.getCol();

            int totalCells = (endRow - startRow + 1) * (endCol - startCol + 1);

            // Bulk copy: Copy entire row formatting
            for (int row = startRow; row <= endRow; row++) {
                Row oldRow = oldSheet.getRow(row);
                if (oldRow == null) continue;

                Row newRow = newSheet.getRow(row);
                if (newRow == null) {
                    newRow = newSheet.createRow(row);
                }

                // Copy row height
                newRow.setHeight(oldRow.getHeight());

                // Copy cell formatting for each column in range
                for (int col = startCol; col <= endCol; col++) {
                    Cell oldCell = oldRow.getCell(col);
                    if (oldCell == null) continue;

                    Cell newCell = newRow.getCell(col);
                    if (newCell == null) {
                        newCell = newRow.createCell(col);
                    }

                    // Copy cell style
                    CellStyle oldStyle = oldCell.getCellStyle();
                    CellStyle newStyle = newSheet.getWorkbook().createCellStyle();
                    newStyle.cloneStyleFrom(oldStyle);
                    newCell.setCellStyle(newStyle);
                }
            }

            // Copy column widths
            for (int col = startCol; col <= endCol; col++) {
                newSheet.setColumnWidth(col, oldSheet.getColumnWidth(col));
            }

            return totalCells;

        } catch (Exception e) {
            System.err.println("Error copying formatting for range " + range.startCell + ":" + range.endCell + ": " + e.getMessage());
            return 0;
        }
    }

    // Configuration class for header-based copying
    static class SheetHeaderConfig {
        // Regular headers (for historical data)
        CellRange finYearHeaderRange;
        CellRange quarterHeaderRange;
        List<CellRange> finYearDataRanges = new ArrayList<CellRange>();
        List<CellRange> quarterDataRanges = new ArrayList<CellRange>();

        // Expanded/Forecast headers (for forecast data)
        CellRange expFinYearHeaderRange;
        CellRange expQuarterHeaderRange;
        List<CellRange> expFinYearDataRanges = new ArrayList<CellRange>();
        List<CellRange> expQuarterDataRanges = new ArrayList<CellRange>();
    }

    // Helper class for cell ranges
    static class CellRange {
        String startCell;
        String endCell;

        CellRange(String start, String end) {
            this.startCell = start;
            this.endCell = end;
        }
    }

}


