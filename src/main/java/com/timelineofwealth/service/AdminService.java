package com.timelineofwealth.service;

import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.dto.ConsolidatedAssetsDTO;
import com.timelineofwealth.dto.ResultExcelDTO;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("AdminService")
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private static AdviserUserMappingRepository adviserUserMappingRepository;
    @Autowired
    public void setAdviserUserMappingRepository(AdviserUserMappingRepository adviserUserMappingRepository){
        AdminService.adviserUserMappingRepository = adviserUserMappingRepository;
    }

    public static List<ResultExcelDTO> getLatestResultExcels() {
        logger.debug(String.format("In AdminService.getLatestResultExcels"));

        String basePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
        File baseFolder = new File(basePath);

        if (!baseFolder.exists() || !baseFolder.isDirectory()) {
            // Handle the case where the base folder doesn't exist or is not a directory
            return new ArrayList<>();
        }

        // List all subdirectories (quarter result folders) within the base folder
        File[] subdirectories = baseFolder.listFiles(File::isDirectory);

        if (subdirectories == null || subdirectories.length == 0) {
            // Handle the case where no subdirectories (quarter result folders) are found
            return new ArrayList<>();
        }

        // Find the latest quarter result folder by comparing folder names
        File latestQuarterFolder = subdirectories[0];
        for (File folder : subdirectories) {
            if (isQuarterFolder(folder)) {
                if (folder.getName().compareTo(latestQuarterFolder.getName()) > 0) {
                    latestQuarterFolder = folder;
                }
            }
        }

        // Define a filter to select only Excel files (with .xlsx extension)
        FileFilter excelFilter = file -> file.isFile() &&
                file.getName().endsWith(".xlsx") &&
                !file.getName().startsWith("$") &&
                !file.getName().contains("$");

        File[] excelFiles = latestQuarterFolder.listFiles(excelFilter);

        if (excelFiles == null || excelFiles.length == 0) {
            // Handle the case where no Excel files are found in the latest quarter result folder
            return new ArrayList<>();
        }

        // Create a list of ResultExcelDTO to store the file information
        List<ResultExcelDTO> excelDTOList = new ArrayList<>();

        // Iterate through the Excel files and gather information
        for (File excelFile : excelFiles) {
            ResultExcelDTO excelDTO = new ResultExcelDTO();
            excelDTO.setExcelFileName(excelFile.getName());
            excelDTO.setExcelFilePath(excelFile.getAbsolutePath());
            excelDTO.setDateModified(new Date(excelFile.lastModified()));

            excelDTOList.add(excelDTO);
        }

        // Sort the list of DTOs by date modified in reverse order (latest first)
        excelDTOList.sort(Comparator.comparing(ResultExcelDTO::getDateModified).reversed());

        return excelDTOList;
    }

    // Helper method to check if a folder name is in the format YYYYQn
    private static boolean isQuarterFolder(File folder) {
        String folderName = folder.getName();
        return folderName.matches("\\d{4}Q[1-4]");
    }
}
