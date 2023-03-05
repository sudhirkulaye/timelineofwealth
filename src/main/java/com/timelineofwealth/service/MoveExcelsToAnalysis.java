package com.timelineofwealth.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class MoveExcelsToAnalysis {
    public static void main(String[] args) {
        // Create a Properties object to store the configuration data
        Properties config = new Properties();

        try {
            // Load the configuration file using FileInputStream
            FileInputStream input = new FileInputStream("C:\\temp\\config.properties");
            config.load(input);
            input.close();

            // Loop through each key-value pair in the configuration file
            for (int i = 1; config.containsKey("file" + i); i++) {
                // Get the source file path and destination folder path from the configuration file
                String sourceFile = config.getProperty("file"+i);
                String destinationFolder = config.getProperty("destinationFolder" + i);

                // Create Path objects for the source file and destination folder
                Path sourcePath = Paths.get(sourceFile);
                Path destinationPath = Paths.get(destinationFolder);

                // Check if the source file and destination folder exist
                if (Files.exists(sourcePath) && Files.isDirectory(destinationPath)) {
                    // Move and replace the source file to the destination folder using Files.move() method
                    Files.move(sourcePath, destinationPath.resolve(sourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Moved " + sourceFile + " to " + destinationFolder);
                } else {
                    System.out.println("Invalid source or destination: " + sourceFile + ", " + destinationFolder);
                }
            }
        } catch (IOException e) {
            // Handle any IO exception that may occur
            e.printStackTrace();
        }
    }
}
