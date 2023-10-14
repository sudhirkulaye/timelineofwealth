package com.timelineofwealth.service;

// if (file.lastModified() > yesterday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())

import java.io.*;
import java.nio.file.*;

public class FileCopier {
    public static void main(String[] args) throws IOException {
        // Set the source and destination folders

        String localLaptop = "C:\\MyDocuments\\03Business";
        String paragLaptop = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business";
        String redMILaptop = "\\\\redmibook15\\03Business";
        String hpSpectreLaptop = "\\DESKTOP-UV0PRDE\03Business";
        String dailyData = "\\03DailyData";
        String excels = "\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
        String reports = "\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
        String clientReports = "\\06ClientData\\PMS\\ClientReports";
        String researchAndAnalysis = "\\05ResearchAndAnalysis";
        String javaWorkspace = "\\02workspace\\timelineofwealth";
        String pythonWorkspace = "\\02workspace\\PythonDev";

        String localMyDocuments = "C:\\MyDocuments\\";
        String redMIMyDocuments = "\\\\redmibook15\\MyDocuments";

        String srcFolder = localMyDocuments; // localLaptop + researchAndAnalysis;
        String dstFolder = redMIMyDocuments; // redMILaptop + researchAndAnalysis;

        //Move latest reports from HP to Redmi
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY24Q2";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY24Q2";

        //Move latest Excels redmi to temp
//        String srcFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2024Q2";
//        String dstFolder = "C:\\temp";

//        String srcFolder = localLaptop + reports;
//        String dstFolder = paragLaptop + reports;

//        String srcFolder = localLaptop + excels;
//        String dstFolder = paragLaptop + excels;

//        String srcFolder = localLaptop + dailyData;
//        String dstFolder = paragLaptop + dailyData;

//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2024Q2";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2024Q2";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q2";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q2";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\AMBITDaily";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\AMBITDaily";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\KotakDaily";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\KotakDaily";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String srcFolder = "C:\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports";

//        String srcFolder = "C:\\MyDocuments\\02UsefulSoftwares\\EditPlus 3";
//        String dstFolder = "\\\\redmibook15\\02UsefulSoftwares\\EditPlus 3";
//        String srcFolder = "C:\\MyDocuments\\03Business\\02workspace\\timelineofwealth";
//        String dstFolder = "\\\\redmibook15\\03Business\\02workspace\\timelineofwealth";
//        String srcFolder = "C:\\MyDocuments\\03Business\\03DailyData";
//        String dstFolder = "\\\\redmibook15\\03Business\\03DailyData";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2024Q2";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2024Q2";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q2";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q2";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\AMBITDaily";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\AMBITDaily";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\KotakDaily";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\KotakDaily";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis";
//        String dstFolder = "\\\\redmibook15\\03Business\\05ResearchAndAnalysis";
//        String srcFolder = "C:\\MyDocuments\\03Business\\06ClientData\\";
//        String dstFolder = "\\\\redmibook15\\03Business\\06ClientData\\";

        // Create the destination folder structure
        createFolderStructure(srcFolder, dstFolder);

        // Copy all files from the source to the destination
        copyFiles(srcFolder, dstFolder);
    }
    private static void createFolderStructure(String srcFolder, String dstFolder) throws IOException {
        // Get the list of all subfolders in the source folder
        File srcDir = new File(srcFolder);
        File[] srcSubfolders = srcDir.listFiles(File::isDirectory);

        // Create the subfolders in the destination folder
        if (srcSubfolders != null) {
            for (File srcSubfolder : srcSubfolders) {
                String subfolderName = srcSubfolder.getName();
                String dstSubfolder = dstFolder + "\\" + subfolderName;
                Files.createDirectories(Paths.get(dstSubfolder));

                // Recursively create the folder structure for the subfolder
                createFolderStructure(srcSubfolder.getAbsolutePath(), dstSubfolder);
            }
        }
    }
    private static void copyFiles(String srcFolder, String dstFolder) throws IOException {
        // Get the list of all files in the source and destination folders
        File srcDir = new File(srcFolder);
        File dstDir = new File(dstFolder);
        File[] srcFiles = srcDir.listFiles(File::isFile);
        File[] dstFiles = dstDir.listFiles(File::isFile);

        // Delete any additional files in the destination
        if (dstFiles != null) {
            for (File dstFile : dstFiles) {
                String dstFileName = dstFile.getName();
                boolean found = false;
                if (srcFiles != null) {
                    for (File srcFile : srcFiles) {
                        if (srcFile.getName().equals(dstFileName)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    dstFile.delete();
                }
            }
        }

        // Copy the files from the source to the destination
        if (srcFiles != null) {
            for (File srcFile : srcFiles) {
                String srcFilePath = srcFile.getAbsolutePath();
                String fileName = srcFile.getName();
                String dstFilePath = dstFolder + "\\" + fileName;

                copyFile(srcFilePath, dstFilePath);
            }
        }

        // Get the list of all subfolders in the source and destination folders
        File[] srcSubfolders = srcDir.listFiles(File::isDirectory);
        File[] dstSubfolders = dstDir.listFiles(File::isDirectory);

        // Delete any additional subfolders in the destination
        if (dstSubfolders != null) {
            for (File dstSubfolder : dstSubfolders) {
                String dstSubfolderName = dstSubfolder.getName();
                boolean found = false;
                if (srcSubfolders != null) {
                    for (File srcSubfolder : srcSubfolders) {
                        if (srcSubfolder.getName().equals(dstSubfolderName)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    System.out.println("Deleting Folder " + dstSubfolder.getAbsolutePath() );
                    deleteFolder(dstSubfolder.getAbsolutePath());
                }
            }
        }

        // Recursively copy the files from the subfolders
        if (srcSubfolders != null) {
            for (File srcSubfolder : srcSubfolders) {
                String srcSubfolderPath = srcSubfolder.getAbsolutePath();
                String subfolderName = srcSubfolder.getName();
                String dstSubfolderPath = dstFolder + "\\" + subfolderName;

                copyFiles(srcSubfolderPath, dstSubfolderPath);
            }
        }
    }

    private static void deleteFolder(String folder) throws IOException {
        File dir = new File(folder);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file.getAbsolutePath());
                } else {
                    System.out.println("Deleting file " + file.getAbsolutePath() );
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    private static void copyFile(String srcFile, String dstFile) throws IOException {
        // Get the last modified time of the source and destination files
        File src = new File(srcFile);
        File dst = new File(dstFile);
        long srcLastModified = src.lastModified();
        long dstLastModified = dst.lastModified();

        // Check if the destination file is missing or if its last modified time is older than the source file's
        if (!dst.exists() || dstLastModified < srcLastModified) {
            System.out.println("Copying File " + srcFile );
            /*try (InputStream in = new FileInputStream(srcFile)) {
                try (OutputStream out = new FileOutputStream(dstFile)) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }*/
            try {
                Path srcPath = Paths.get(srcFile);
                Path dstPath = Paths.get(dstFile);
                Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS);
            } catch (Exception e) {}
        }
    }

}
