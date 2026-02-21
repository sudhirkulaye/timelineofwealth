package com.timelineofwealth.service;

import com.timelineofwealth.entities.DailyDataS;
import com.timelineofwealth.repositories.DailyDataSRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class ResultTrackerRunner {

    public static void runValuationUpdate() {

        ConfigurableApplicationContext ctx =
                new SpringApplicationBuilder(com.timelineofwealth.wealthmanagement.WealthmanagementApplication.class)
                        .run();

        ResultTrackerService service = ctx.getBean(ResultTrackerService.class);

        service.updateResultTracker();

        ctx.close();
    }
}

class DailyDataSRepairRunner {
    public static void reuploadAffectedDates(String screenerDailyPath) {
        ConfigurableApplicationContext ctx =
                new SpringApplicationBuilder(com.timelineofwealth.wealthmanagement.WealthmanagementApplication.class)
                        .run();

        try {
            DailyDataSRepository repository = ctx.getBean(DailyDataSRepository.class);
            List<Date> affectedDates = repository.findDistinctDatesWithCmpZero();

            if (affectedDates == null || affectedDates.isEmpty()) {
                System.out.println("No affected dates found (cmp = 0).");
                return;
            }

            System.out.println("Affected dates found: " + affectedDates.size());
            for (Date affectedDate : affectedDates) {
                String yyyymmdd = affectedDate.toString().replace("-", "");
                File sourceFile = findDateCsvFile(screenerDailyPath, yyyymmdd);

                if (sourceFile == null) {
                    System.out.println("[SKIP] File not found for date " + yyyymmdd + " in " + screenerDailyPath);
                    continue;
                }

                List<DailyDataS> dailyDataSList = readCsvFile(sourceFile, affectedDate);

                if (dailyDataSList.isEmpty()) {
                    System.out.println("[SKIP] Empty parsed data for " + sourceFile.getName());
                    continue;
                }

                dailyDataSList.sort(Comparator.comparing(DailyDataS::getMarketCap).reversed());
                repository.saveAll(dailyDataSList);
                System.out.println("[DONE] Upserted " + dailyDataSList.size() + " rows for date " + yyyymmdd + " from " + sourceFile.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ctx.close();
        }
    }

    private static File findDateCsvFile(String basePath, String yyyymmdd) {
        File csvFile = new File(basePath, yyyymmdd + "ScreenerDaily.csv");
        return csvFile.exists() && csvFile.isFile() ? csvFile : null;
    }

    private static List<DailyDataS> readCsvFile(File file, Date date) throws IOException {
        List<DailyDataS> dataList = new ArrayList<>();
        int rank = 1;
        try (Reader reader = new BufferedReader(new FileReader(file));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                DailyDataS daily = buildDailyDataS(rank++, date,
                        getCsvValue(record, "NSE Code"),
                        getCsvValue(record, "BSE Code"));
                if (daily == null) continue;

                populateDailyDataFromRecord(daily, record);
                dataList.add(daily);
            }
        }
        return dataList;
    }

    private static DailyDataS buildDailyDataS(int rank, Date date, String nseCode, String bseCode) {
        String name = nseCode == null || nseCode.trim().isEmpty() ? bseCode : nseCode;
        if (name == null || name.trim().isEmpty()) return null;

        DailyDataS daily = new DailyDataS();
        daily.setKey(new DailyDataS.DailyDataSKey());
        daily.getKey().setDate(date);
        daily.getKey().setName(name.trim());
        daily.setRank(rank);
        return daily;
    }

    private static void populateDailyDataFromRecord(DailyDataS daily, CSVRecord record) {
        daily.setCmp(getCsvBigDecimal(record, "Current Price"));
        daily.setMarketCap(getCsvBigDecimal(record, "Market Capitalization"));
        daily.setLastResultDate(getInt(getCsvValue(record, "Last result date")));
        daily.setNetProfit(getCsvBigDecimal(record, "Net profit"));
        daily.setSales(getCsvBigDecimal(record, "Sales"));
        daily.setYoyQuarterlySalesGrowth(getCsvBigDecimal(record, "YOY Quarterly sales growth"));
        daily.setYoyQuarterlyProfitGrowth(getCsvBigDecimal(record, "YOY Quarterly profit growth"));
        daily.setQoqSalesGrowth(getCsvBigDecimal(record, "QoQ sales growth"));
        daily.setQoqProfitGrowth(getCsvBigDecimal(record, "QoQ profit growth"));
        daily.setOpmLatestQuarter(getCsvBigDecimal(record, "OPM latest quarter"));
        daily.setOpmLastYear(getCsvBigDecimal(record, "OPM last year"));
        daily.setNpmLatestQuarter(getCsvBigDecimal(record, "NPM latest quarter"));
        daily.setNpmLastYear(getCsvBigDecimal(record, "NPM last year"));
        daily.setProfitGrowth3years(getCsvBigDecimal(record, "Profit growth 3Years"));
        daily.setSalesGrowth3years(getCsvBigDecimal(record, "Sales growth 3Years"));
        daily.setPeTtm(getCsvBigDecimal(record, "Price to Earning"));
        daily.setHistoricalPe3years(getCsvBigDecimal(record, "Historical PE 3Years"));
        daily.setPegRatio(getCsvBigDecimal(record, "PEG Ratio"));
        daily.setPbTtm(getCsvBigDecimal(record, "Price to book value"));
        daily.setEvToEbit(getCsvBigDecimal(record, "Enterprise Value to EBIT"));
        daily.setDividendPayout(getCsvBigDecimal(record, "Dividend Payout Ratio"));
        daily.setRoe(getCsvBigDecimal(record, "Return on equity"));
        daily.setAvgRoe3years(getCsvBigDecimal(record, "Average return on equity 3Years"));
        daily.setDebt(getCsvBigDecimal(record, "Debt"));
        daily.setDebtToEquity(getCsvBigDecimal(record, "Debt to equity"));
        daily.setDebt3yearsback(getCsvBigDecimal(record, "Debt 3Years back"));
        daily.setRoce(getCsvBigDecimal(record, "Return on capital employed"));
        daily.setAvgRoce3years(getCsvBigDecimal(record, "Average return on capital employed 3Years"));
        daily.setFcfS(getCsvBigDecimal(record, "Free cash flow last year"));
        daily.setSalesGrowth5years(getCsvBigDecimal(record, "Sales growth 5Years"));
        daily.setSalesGrowth10years(getCsvBigDecimal(record, "Sales growth 10Years"));
        daily.setNoplat(getCsvBigDecimal(record, "NOPLAT"));
        daily.setCapex(getCsvBigDecimal(record, "Capex"));
        daily.setFcff(getCsvBigDecimal(record, "FCFF"));
        daily.setInvestedCapital(getCsvBigDecimal(record, "Invested Capital"));
        daily.setRoic(getCsvBigDecimal(record, "RoIC"));
        daily.setReturn1D(getCsvBigDecimal(record, "Return over 1day"));
        daily.setReturn1W(getCsvBigDecimal(record, "Return over 1week"));
        daily.setReturn1M(getCsvBigDecimal(record, "Return over 1month"));
        daily.setReturn3M(getCsvBigDecimal(record, "Return over 3months"));
        daily.setReturn6M(getCsvBigDecimal(record, "Return over 6months"));
        daily.setReturn1Y(getCsvBigDecimal(record, "Return over 1year"));
        daily.setUp52wMin(getCsvBigDecimal(record, "Up from 52w low"));
        daily.setDown52wMax(getCsvBigDecimal(record, "Down from 52w high"));
        daily.setVolume1D(getCsvBigDecimal(record, "Volume"));
        daily.setVolume1W(getCsvBigDecimal(record, "Volume 1week average"));
        daily.setDma50(getCsvBigDecimal(record, "DMA 50"));
        daily.setDma200(getCsvBigDecimal(record, "DMA 200"));
        daily.setRsi(getCsvBigDecimal(record, "RSI"));
        daily.setTotalAssets(getCsvBigDecimal(record, "Total Assets"));
        daily.setNetBlock(getCsvBigDecimal(record, "Net block"));
        daily.setWorkingCapital(getCsvBigDecimal(record, "Working capital"));
        daily.setInventory(getCsvBigDecimal(record, "Inventory"));
        daily.setTradeReceivables(getCsvBigDecimal(record, "Trade receivables"));
        daily.setTradePayables(getCsvBigDecimal(record, "Trade Payables"));
        daily.setSharesOutstandingCr(getCsvBigDecimal(record, "Number of equity shares"));
        daily.setIndustry(getCsvValue(record, "Industry"));
        daily.setSector("");
        daily.setSubIndustry("");
        computeRatios(daily);
    }

    private static void computeRatios(DailyDataS daily) {
        if (daily.getNetProfit() != null && daily.getNetProfit().compareTo(BigDecimal.ZERO) != 0) {
            daily.setMcapToNetprofit(daily.getMarketCap().divide(daily.getNetProfit(), 2, RoundingMode.HALF_UP));
        } else {
            daily.setMcapToNetprofit(BigDecimal.ZERO);
        }

        if (daily.getSales() != null && daily.getSales().compareTo(BigDecimal.ZERO) != 0) {
            daily.setMcapToSales(daily.getMarketCap().divide(daily.getSales(), 2, RoundingMode.HALF_UP));
        } else {
            daily.setMcapToSales(BigDecimal.ZERO);
        }
    }

    private static String getCsvValue(CSVRecord record, String columnName) {
        if (!record.isMapped(columnName)) return "";
        String value = record.get(columnName);
        return value == null ? "" : value.trim();
    }

    private static BigDecimal getCsvBigDecimal(CSVRecord record, String columnName) {
        try {
            String value = getCsvValue(record, columnName);
            return value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static int getInt(String value) {
        try {
            return new BigDecimal(value).intValue();
        } catch (Exception e) {
            return 0;
        }
    }
}

public class CreateFolderStructureForIndustry {

    public static void main(String[] argv) throws Exception {

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\nSelect an option to run:");
            System.out.println(" 1. Create Folder Structure from source folder");
            System.out.println(" 2. List Duplicate excel files in Analysis Folder");
            System.out.println(" 3. List Files and their path in Analysis Folder");
            System.out.println(" 4. File Name and Corresponding Folder Name");
            System.out.println(" 5. Update ResultTracker.xlsx");
            System.out.println(" 6. Create Auto Sales Table");
            System.out.println(" 7. Update MCap and Price Data");
            System.out.println(" 8. Consolidate Quarter Data");
            System.out.println(" 9. Update Valuation Sheet of ResultTracker");
            System.out.println("10. Upsert affected daily_data_s dates (cmp=0) from ScreenerDaily CSV");
            System.out.println("11. Exit");
            System.out.print("Enter your choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    createFolderStructure(argv);
                    break;

                case "2":
                    listDuplicateFiles(argv);
                    break;

                case "3":
                    listFilesAndFolderPath(argv);
                    break;

                case "4":
                    listFileNameAndSectorFolder();
                    break;

                case "5":
                    new ConsolidatedResultTracker().updateResultTrackerExcel();
                    break;

                case "6":
                    createAutoSalesTable();
                    break;

                case "7":
                    updateMCapAndPrice(new UpdateQuarterlyExcels().getLatestQuarterFolder());
                    break;

                case "8":
                    ConsolidateQuarterData.main(argv);
                    break;

                case "9":
                    ResultTrackerRunner.runValuationUpdate();
                    break;

                case "10":
                    DailyDataSRepairRunner.reuploadAffectedDates("C:\\MyDocuments\\03Business\\03DailyData\\ScreenerDaily");
                    break;

                case "11":
                    System.out.println("Exiting...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }


    public static void createFolderStructure(String[] argv) throws IOException {
        Scanner sc = new Scanner(System.in);

        String basePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";

        System.out.println("Base folder: " + basePath);

        System.out.print("Enter source quarter folder (e.g. FY26Q2): ");
        String sourceQuarter = sc.nextLine().trim();

        System.out.print("Enter destination quarter folder (e.g. FY26Q3): ");
        String destinationQuarter = sc.nextLine().trim();

        String sourcePath = basePath + "\\" + sourceQuarter;
        String destinationPath = basePath + "\\" + destinationQuarter;

        System.out.println("Source path: " + sourcePath);
        System.out.println("Destination path: " + destinationPath);

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

    public static void createAutoSalesTable() {
        String inputFilePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\AutoSales.xlsx";
        String outputFilePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\AutoSalesDBInsert.xlsx";

        try (FileInputStream fileInputStream = new FileInputStream(inputFilePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheet("AutoMonthly");

            // Read header row separately
            Row headerRow = sheet.getRow(1);
            List<String> headers = new ArrayList<>();
            for (int i = 2; i < headerRow.getLastCellNum(); i++) {
                Cell headerCell = headerRow.getCell(i);

                if (DateUtil.isCellDateFormatted(headerCell)) {
                    java.util.Date date = headerCell.getDateCellValue();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    headers.add(dateFormat.format(date));
                } else {
                    headers.add(headerCell.getStringCellValue());
                }
            }

            // Delete existing file if it exists
            File existingFile = new File(outputFilePath);
            if (existingFile.exists()) {
                existingFile.delete();
            }

            // Create a new workbook and sheet
            try (Workbook newWorkbook = new XSSFWorkbook();
                 FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {

                Sheet newSheet = newWorkbook.createSheet("AutoSalesDBInsert");

                // Write header row
                Row newHeaderRow = newSheet.createRow(0);
                newHeaderRow.createCell(0).setCellValue("Date");
                newHeaderRow.createCell(1).setCellValue("Company");
                newHeaderRow.createCell(2).setCellValue("Parameter");
                newHeaderRow.createCell(3).setCellValue("Sales");
                newHeaderRow.createCell(4).setCellValue("3M");
                newHeaderRow.createCell(5).setCellValue("6M");
                newHeaderRow.createCell(6).setCellValue("12M");

                // Read data and create records
                int rowIndex = 1;
                Iterator<Row> rowIterator = sheet.iterator();
                rowIterator.next(); // Skip blank row
                rowIterator.next(); // Skip header row
                String lastParameter = "";
                int skip3M = 0, skip6M = 0, skip12M = 0;
                while (rowIterator.hasNext()) {
                    Row dataRow = rowIterator.next();
                    String company = dataRow.getCell(0).getStringCellValue();
                    String parameter = dataRow.getCell(1).getStringCellValue();

                    if (!parameter.equals(lastParameter)) {
                        // Parameter changed, reset skip counters
                        lastParameter = parameter;
                        skip3M = 2; // Skip next 2 records for 3M
                        skip6M = 5; // Skip next 5 records for 6M
                        skip12M = 11; // Skip next 11 records for 12M
                    }

                    for (int i = 2; i < dataRow.getLastCellNum(); i++) {
                        Cell dateCell = headerRow.getCell(i);
                        java.util.Date date = dateCell.getDateCellValue();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        Row newRow = newSheet.createRow(rowIndex++);
                        newRow.createCell(0).setCellValue(dateFormat.format(date));
                        newRow.createCell(1).setCellValue(company);
                        newRow.createCell(2).setCellValue(parameter);

                        // Format sales numbers with thousand separators
                        CellStyle commaStyle = newWorkbook.createCellStyle();
                        commaStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("#,##0"));
                        Cell salesCell = newRow.createCell(3);
                        salesCell.setCellValue(dataRow.getCell(i).getNumericCellValue());
                        salesCell.setCellStyle(commaStyle);

                        // Calculate 3M, 6M, 12M sales
                        calculateCumulativeSales(newSheet, newRow, rowIndex, 3, 4, skip3M);
                        calculateCumulativeSales(newSheet, newRow, rowIndex, 6, 5, skip6M);
                        calculateCumulativeSales(newSheet, newRow, rowIndex, 12, 6, skip12M);

                        // Decrement skip counters
                        skip3M = Math.max(0, skip3M - 1);
                        skip6M = Math.max(0, skip6M - 1);
                        skip12M = Math.max(0, skip12M - 1);
                    }
                }

                // Save the new workbook
                newWorkbook.write(fileOutputStream);
            }

            System.out.println("Excel file created successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void calculateCumulativeSales(Sheet sheet, Row currentRow, int currentRowIndex, int period, int column, int skip) {
        if (currentRowIndex >= period && skip == 0) {
            String formula = "SUM(D" + (currentRowIndex - period + 1) + ":D" + currentRowIndex + ")";
            currentRow.createCell(column).setCellFormula(formula);
        }
    }

    public static void updateMCapAndPrice(String latestFolder) {
        String folderPath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\" + latestFolder;
        File folder = new File(folderPath);

        // Get the list of files modified or created today
        File[] files = folder.listFiles((dir, name) -> {
            Calendar today = Calendar.getInstance();
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(new File(dir, name).lastModified());
            return lastModified.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    lastModified.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        });

        if (files != null) {
            for (File file : files) {
                try {
                    if(!file.getName().contains("$") && file.getName().contains(".xlsx"))
                        updateExcel(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void updateExcel(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Access the "QuarterP&L" sheet
            Sheet quarterPLSheet = workbook.getSheet("QuarterP&L");
            if (quarterPLSheet != null) {
                Row row13 = quarterPLSheet.getRow(12); // R13 corresponds to index 12
                Row row14 = quarterPLSheet.getRow(13); // R14 corresponds to index 13
                Row row15 = quarterPLSheet.getRow(14); // R15 corresponds to index 14

                // Check if R15 is blank, and update with yesterday's date if true
                String yesterdayDate = getYesterdayDate();
                if (isNullOrEmpty(row15.getCell(17))) {
                    updateCellValue(row15, 17, yesterdayDate);
                }

                // Check if R13 is blank, empty, null, or contains value 0
                if (isNullOrEmptyOrZero(row13.getCell(17))) {
                    // Access the "AnnualResults" sheet
                    Sheet annualResultsSheet = workbook.getSheet("AnnualResults");
                    if (annualResultsSheet != null) {
                        // Access cell P45 (assuming this is a valid cell index)
                        Cell cellP45 = annualResultsSheet.getRow(44).getCell(15);

                        // Evaluate the formula in cell P45 to get the actual value
                        evaluateFormulaCell(cellP45, workbook);

                        // Get the numeric value from the formula cell
                        double valueP45 = cellP45.getNumericCellValue();

                        // Update cell R13 in "QuarterP&L" with the value from "AnnualResults"
                        updateCellValue(row13, 17, valueP45);
                    }
                }

                // Check if R14 is blank, empty, null, or contains value 0
                if (isNullOrEmptyOrZero(row14.getCell(17))) {
                    // Access the "AnnualResults" sheet
                    Sheet annualResultsSheet = workbook.getSheet("AnnualResults");
                    if (annualResultsSheet != null) {
                        // Access cell P46 (assuming this is a valid cell index)
                        Cell cellP46 = annualResultsSheet.getRow(45).getCell(15);

                        // Evaluate the formula in cell P46 to get the actual value
                        evaluateFormulaCell(cellP46, workbook);

                        // Get the numeric value from the formula cell
                        double valueP46 = cellP46.getNumericCellValue();

                        // Update cell R14 in "QuarterP&L" with the value from "AnnualResults"
                        updateCellValue(row14, 17, valueP46);
                    }
                }
            }

            // Save the changes
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private static boolean isNullOrEmptyOrZero(Cell cell) {
        return cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK ||
                (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && cell.getNumericCellValue() == 0);
    }

    private static boolean isNullOrEmpty(Cell cell) {
        return cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK;
    }

    private static void updateCellValue(Row row, int columnIndex, Object value) {
        Cell cell = row.getCell(columnIndex);
        if (cell != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof String) {
                String stringValue = (String) value;
                if (isDateInFormat(stringValue, "yyyy-MM-dd")) {
                    cell.setCellValue(stringValue);
                } else {
                    cell.setCellValue(Double.parseDouble(stringValue));
                }
            }
        }
    }

    private static boolean isDateInFormat(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private static String getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        java.util.Date yesterday = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(yesterday);
    }

    private static void evaluateFormulaCell(Cell cell, Workbook workbook) {
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateFormulaCell(cell);
        }
    }
}
