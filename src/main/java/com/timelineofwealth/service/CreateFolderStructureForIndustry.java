package com.timelineofwealth.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

public class CreateFolderStructureForIndustry {
/*    public static void main(String[] argv) throws IOException {
        String sourcePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q2";
        String destinationPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY23Q3";

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
    }*/

    static void RecursivePrint(File[] arr, int index, int level) throws Exception
    {
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
    public static void main1(String[] args) throws Exception
    {
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

    public static void main(String[] args) throws IOException {
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
}
