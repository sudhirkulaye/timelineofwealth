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
import org.springframework.beans.factory.annotation.Autowired;

public class UpdateQuarterlyExcels {

    private final String BASE_PATH = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";

    public static void main(String[] args) throws IOException, EncryptedDocumentException {
        UpdateQuarterlyExcels updateQuarterlyExcels = new UpdateQuarterlyExcels();
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream(ConfigFiles.updateQuarterlyExcels());
        prop.load(input);
        input.close();

        String filesToBeUpdated = prop.getProperty("FilesToBeUpdated", "0Days").trim();
        int days = extractDurationInDays(filesToBeUpdated);
        int minutes = extractDurationInMinutes(filesToBeUpdated);

        String latestFolder = updateQuarterlyExcels.getLatestQuarterFolder();
        String previousFolder = getPreviousQuarter(latestFolder);

        File latestDir = new File(updateQuarterlyExcels.BASE_PATH + File.separator + latestFolder);
        File previousDir = new File(updateQuarterlyExcels.BASE_PATH + File.separator + previousFolder);

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

            carryOver(oldFile, newFile, ticker);
        }

        CreateFolderStructureForIndustry.updateMCapAndPrice(latestFolder);
        new ConsolidatedResultTracker().updateResultTrackerExcel(latestFolder);
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

    public String getLatestQuarterFolder() {
        File base = new File(this.BASE_PATH);
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

    /**
     * newFile is the fresh Screener download (customised template sheets + new Data Sheet). Everything typed
     * in last quarter's workbook (model inputs, assumptions, segments, other information, bank data, result
     * dates and prices, formatting) and the history Screener no longer provides is carried into it by
     * ExcelCarryOver; Analysts&Consensus is dropped. Data Sheet B1 is set to the ticker.
     */
    private static void carryOver(File oldFile, File newFile, String ticker) {
        try (FileInputStream oi = new FileInputStream(oldFile); FileInputStream ni = new FileInputStream(newFile);
             XSSFWorkbook oldWb = new XSSFWorkbook(oi); XSSFWorkbook newWb = new XSSFWorkbook(ni)) {
            if (newWb.getSheet("HistoricalData") == null) {
                System.out.println(newFile.getName() + ": no HistoricalData sheet - download it with the customised "
                        + "template (Screener > Excel > upload template), skipped");
                return;
            }
            for (XSSFWorkbook wb : new XSSFWorkbook[]{oldWb, newWb}) {
                XSSFSheet ds = wb.getSheet("Data Sheet");
                if (ds == null) continue;
                Row r = ds.getRow(0) != null ? ds.getRow(0) : ds.createRow(0);
                Cell c = r.getCell(1) != null ? r.getCell(1) : r.createCell(1);
                c.setCellValue(ticker);                       // same ticker in both, so block titles match
            }
            String summary = new ExcelCarryOver(oldWb, newWb).run();
            try (FileOutputStream out = new FileOutputStream(newFile)) { newWb.write(out); }
            System.out.println(newFile.getName() + " <- " + oldFile.getName() + ": " + summary);
        } catch (Exception e) {
            System.err.println("Carry-over failed for " + newFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
