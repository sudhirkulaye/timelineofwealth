package com.timelineofwealth.service;

import java.io.File;
import java.io.IOException;

public class CreateFolderStructureForIndustry {
    public static void main(String[] argv) throws IOException {
        String sourcePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY21Q4";
        String destinationPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\FY22Q1";

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
}
