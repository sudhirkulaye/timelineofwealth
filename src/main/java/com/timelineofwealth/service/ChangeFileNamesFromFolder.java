package com.timelineofwealth.service;

import java.io.File;
import java.io.IOException;

public class ChangeFileNamesFromFolder {

    public static void main(String[] argv) throws IOException {
        String absolutePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY21Q3";
        File dir = new File(absolutePath);
        renameFile(dir, absolutePath);
        System.out.println("conversion is done");
    }

    /**
     * This program changes file name in a directory and sub directory e.g. 2021 to FY21
     * Change the line name.replace("2021", "FY21"); for appropriately
     * @param dir
     * @param absolutePath
     */
    public static void renameFile(File dir, String absolutePath){
        File[] filesInDir = dir.listFiles();

        for (File file:filesInDir) {
            if(!file.isDirectory()) {
                String name = file.getName();
                String newName = name.replace("2021", "FY21"); // change this line
                String newPath = absolutePath + "\\" + newName;
                file.renameTo(new File(newPath));
                System.out.println(name + " changed to " + newName);
            } else {
                renameFile(file, file.getAbsolutePath());
            }
        }
    }
}
