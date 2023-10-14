package com.timelineofwealth.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CreateFolderStructureForIndustry {

    public static void main(String[] argv) throws Exception {
        //1. Create Folder Structure from source folder
//        createFolderStructure(argv);
        //2. List Duplicate excel files in Analysis Folder
        listDuplicateFiles(argv);
        //3. List Files and their path in Analysis Folder
//        listFilesAndFolderPath(argv);
        //4. File Name and Corresponding Folder Name
//        listFileNameAndSectorFolder();
    }

    public static void createFolderStructure(String[] argv) throws IOException {
//        String sourcePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY24Q1";
        String sourcePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";
        String destinationPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY24Q2";

        File sourceDirectory = new File(sourcePath);

        File[] directoriesInSourceDirectory = sourceDirectory.listFiles();
        for (File directory:directoriesInSourceDirectory){
            if(directory.isDirectory()){
                //System.out.println(destinationPath+"\\"+directory.getName());
                File destinationDirectory = new File(destinationPath+"\\"+directory.getName());
                if(!destinationDirectory.exists()){
                    destinationDirectory.mkdirs();
                }
            }
        }

        System.out.println("Folders for industries created...");
    }

    static void RecursivePrint(File[] arr, int index, int level) throws Exception {
        // terminate condition
        if (index == arr.length)
            return;

        // tabs for internal levels
//        for (int i = 0; i < level; i++)
//            System.out.print("\t");

        // for files
        if (arr[index].isFile())
            System.out.println(arr[index].getName() + "\t" + arr[index].getParent());

            // for sub-directories
        else if (arr[index].isDirectory() && !arr[index].getName().equalsIgnoreCase("Old")) {
//            System.out.println("[" + arr[index].getName()
//                    + "]");

            // recursion for sub-directories
            RecursivePrint(arr[index].listFiles(), 0,
                    level + 1);
        }

        // recursion for main directory
        RecursivePrint(arr, ++index, level);
    }

    // Driver Method
    public static void listFilesAndFolderPath(String[] args) throws Exception {
        // Provide full path for directory(change
        // accordingly)
        String maindirpath
                = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis";

        // File object
        File maindir = new File(maindirpath);

        if (maindir.exists() && maindir.isDirectory()) {

            // array for files and sub-directories
            // of directory pointed by maindir
            File arr[] = maindir.listFiles();

            System.out.println(
                    "**********************************************");
            System.out.println(
                    "Files from main directory : " + maindir);
            System.out.println(
                    "**********************************************");

            // Calling recursive method
            RecursivePrint(arr, 0, 0);
        }
    }

    //List of duplicate files in multiple folders under the folder \QuarterResultsScreenerExcels\Analysis
    public static void listDuplicateFiles(String[] args) throws IOException {
        // base folder
        Path start = Paths.get("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis");
        // set to store file names
        HashSet<String> fileNames = new HashSet<>();

        try (Stream<Path> stream = Files.walk(start)) {
            stream
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        if (fileNames.contains(fileName)) {
                            System.out.println("Duplicate file: " + file);
                        } else {
                            fileNames.add(fileName);
                        }
                    });
        }
    }

    public static void listFileNameAndSectorFolder(){
        // Specify the directory path
        String directoryPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector";

        // Create a File object for the directory
        File directory = new File(directoryPath);

        // Check if the directory exists
        if (directory.exists() && directory.isDirectory()) {
            // Define the pattern for matching file names
            Pattern pattern = Pattern.compile("\\d{8}_.+_.+\\.pdf");

            // Create a Map to store unique file names and their corresponding subfolder names
            Map<String, String> uniqueFileAndSubfolder = new HashMap<>();

            // List all subdirectories
            File[] subdirectories = directory.listFiles(File::isDirectory);

            if (subdirectories != null) {
                for (File subdirectory : subdirectories) {
                    // List files in each subdirectory
                    File[] files = subdirectory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            // Check if the file name matches the pattern
                            if (pattern.matcher(file.getName()).matches()) {
                                // Extract the NameOfTheFile and add it to the map
                                String[] parts = file.getName().split("_");
                                if (parts.length >= 3) {
                                    String nameOfFile = parts[1];
                                    uniqueFileAndSubfolder.put(nameOfFile, subdirectory.getName());
                                }
                            }
                        }
                    }
                }
            }

            // Print the unique file names and their corresponding subfolder names
            for (Map.Entry<String, String> entry : uniqueFileAndSubfolder.entrySet()) {
                System.out.println(entry.getKey() + ", " + entry.getValue());
            }
        } else {
            System.out.println("Directory does not exist or is not a directory.");
        }
    }
}
