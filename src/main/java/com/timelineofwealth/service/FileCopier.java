package com.timelineofwealth.service;

// if (file.lastModified() > yesterday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())

import java.io.*;
import java.nio.file.*;

public class FileCopier {
    public static void main(String[] args) throws IOException {
        // Set the source and destination folders

        String localLaptop = "C:\\MyDocuments\\03Business";
        String paragLaptop = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business";
        String redMILaptop = "\\\\REDMIBOOK15\\MyDocuments\\03Business";
        String hpSpectreLaptop = "\\DESKTOP-UV0PRDE\03Business";
        String dailyData = "\\03DailyData";
        String excels = "\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
        String reports = "\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
        String clientReports = "\\06ClientData\\PMS\\ClientReports";
        String researchAndAnalysis = "\\05ResearchAndAnalysis";
        String javaWorkspace = "\\02workspace\\timelineofwealth";
        String pythonWorkspace = "\\02workspace\\PythonDev";

        //*********************************************** From Local to OneDrive ***********************************************

//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String dstFolder = "C:\\Users\\sudhi\\OneDrive\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";

//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String dstFolder = "C:\\Users\\sudhi\\OneDrive\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";

//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String dstFolder = "C:\\Users\\sudhi\\OneDrive\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";

//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String dstFolder = "C:\\Users\\sudhi\\OneDrive\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";

        //*********************************************** From HP to Redmi ***********************************************

        // Move Jupyter Notes projects to 02workspace
//        String srcFolder = "C:\\Users\\Sudhir Kulaye\\JupyterProjects";
//        String dstFolder = "C:\\MyDocuments\\03Business\\02workspace\\JupyterProjects";

        String srcFolder = "\\\\HPSpectre\\MyDocuments\\";
        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\";

        //Move latest reports from HP to Redmi
//        String srcFolder = "\\\\HPSpectre\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY24Q2";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY24Q2";

        //Move Daily Data from HP to Redmii
//        String srcFolder = "\\\\HPSpectre\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";

        //*********************************************** From redmi to HP ***********************************************

        // Move Jupyter Notes projects to 02workspace
//        String srcFolder = "C:\\Users\\sudhi\\JupyterProjects";
//        String dstFolder = "C:\\MyDocuments\\03Business\\02workspace\\JupyterProjects";

//        String srcFolder = "\\\\REDMIBOOK15\\MyDocuments\\";
//        String dstFolder = "\\\\HPSpectre\\MyDocuments\\";

        //Move latest DailyData redmi to HP
//        String srcFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\03DailyData";
//        String dstFolder = "\\\\HPSpectre\\03Business\\03DailyData";

        //Move latest reports redmi to HP
//        String srcFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports";
//        String dstFolder = "\\\\HPSpectre\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports";

        //Move latest Excels from redmi to HP
//        String srcFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String dstFolder = "\\\\HPSpectre\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";

        //Move latest Code from redmi to HP
//        String srcFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\06ClientData\\PMS";
//        String dstFolder = "\\\\HPSpectre\\03Business\\06ClientData\\PMS";

        //Move latest JAVA Code from redmi to HP
//        String srcFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\java\\com\\timelineofwealth";
//        String dstFolder = "\\\\HPSpectre\\03Business\\02workspace\\timelineofwealth\\src\\main\\java\\com\\timelineofwealth";

        //Move latest SQL Code from redmi to HP
//        String srcFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\sql";
//        String dstFolder = "\\\\HPSpectre\\03Business\\02workspace\\timelineofwealth\\src\\main\\sql";

        //*********************************************** From Local Laptop to Parag's Laptop ***********************************************

//        String srcFolder = localLaptop + reports;
//        String dstFolder = paragLaptop + reports;

//        String srcFolder = localLaptop + excels;
//        String dstFolder = paragLaptop + excels;

//        String srcFolder = localLaptop + dailyData;
//        String dstFolder = paragLaptop +

//        String srcFolder = "C:\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\java\\com\\timelineofwealth";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\java\\com\\timelineofwealth";
//        String srcFolder = "C:\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\sql";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\sql";
//        String srcFolder = "C:\\MyDocuments\\03Business\\02workspace\\JupyterProjects";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\02workspace\\JupyterProjects";
//        String srcFolder = "C:\\MyDocuments\\03Business\\03DailyData";
////        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\03DailyData";
//        //String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments";
//        //String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String srcFolder = "C:\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports";
//        String dstFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports";


        //*********************************************** From Parag's Laptop to Local Laptop ***********************************************

//        String srcFolder = localLaptop + reports;
//        String dstFolder = paragLaptop + reports;

//        String srcFolder = localLaptop + excels;
//        String dstFolder = paragLaptop + excels;

//        String srcFolder = localLaptop + dailyData;
//        String dstFolder = paragLaptop +

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\03DailyData";
//        String dstFolder = "C:\\MyDocuments\\03Business\\03DailyData";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2025Q4";
//        String dstFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2025Q4";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
//        String dstFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";
//        String dstFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Charts";
//        String dstFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Charts";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String dstFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String dstFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String dstFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";

//        String srcFolder = "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports";
//        String dstFolder = "C:\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports";


//        String srcFolder = "C:\\MyDocuments\\02UsefulSoftwares\\EditPlus 3";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\02UsefulSoftwares\\EditPlus 3";
//        String srcFolder = "C:\\MyDocuments\\03Business\\02workspace\\timelineofwealth";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\02workspace\\timelineofwealth";
//        String srcFolder = "C:\\MyDocuments\\03Business\\03DailyData";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\03DailyData";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2024Q2";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\2024Q2";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q2";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q2";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\AMBITDaily";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\AMBITDaily";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\KotakDaily";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\KotakDaily";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";
//        String srcFolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\05ResearchAndAnalysis";
//        String srcFolder = "C:\\MyDocuments\\03Business\\06ClientData\\";
//        String dstFolder = "\\\\REDMIBOOK15\\MyDocuments\\03Business\\06ClientData\\";

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
                    System.out.println("Deleteing File " + dstFile);
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
