package com.timelineofwealth.service;

import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DownloadEODFiles {
    @Autowired
    NsePriceHistoryRepository nsePriceHistoryRepository;
    @Autowired
    public void setNsePriceHistoryRepository(NsePriceHistoryRepository nsePriceHistoryRepository){
        this.nsePriceHistoryRepository = nsePriceHistoryRepository;
    }
    @Autowired
    BsePriceHistoryRepository bsePriceHistoryRepository;
    @Autowired
    public void setBsePriceHistoryRepository(BsePriceHistoryRepository bsePriceHistoryRepository){
        this.bsePriceHistoryRepository = bsePriceHistoryRepository;
    }
    @Autowired
    MutualFundNavHistoryRepository mutualFundNavHistoryRepository;
    @Autowired
    public void setMutualFundNavHistoryRepository(MutualFundNavHistoryRepository mutualFundNavHistoryRepository){
        this.mutualFundNavHistoryRepository = mutualFundNavHistoryRepository;
    }
    @Autowired
    MutualFundUniverseRepository mutualFundUniverseRepository;
    @Autowired
    public void setMutualFundUniverseRepository(MutualFundUniverseRepository mutualFundUniverseRepository){
        this.mutualFundNavHistoryRepository = mutualFundNavHistoryRepository;
    }
    @Autowired
    DailyDataSRepository dailyDataSRepository;
    @Autowired
    public void setDailyDataSRepository(DailyDataSRepository dailyDataSRepository){
        this.dailyDataSRepository = dailyDataSRepository;
    }

    public static void main(String[] args) throws IOException {
        DownloadEODFiles downloadEODFiles = new DownloadEODFiles();
        downloadEODFiles.oneClickUpload(downloadEODFiles.nsePriceHistoryRepository, downloadEODFiles.bsePriceHistoryRepository, downloadEODFiles.mutualFundNavHistoryRepository, downloadEODFiles.dailyDataSRepository, downloadEODFiles.mutualFundUniverseRepository);
    }

    public int oneClickUpload(NsePriceHistoryRepository nsePriceHistoryRepository, BsePriceHistoryRepository bsePriceHistoryRepository, MutualFundNavHistoryRepository mutualFundNavHistoryRepository, DailyDataSRepository dailyDataSRepository, MutualFundUniverseRepository mutualFundUniverseRepository) throws IOException {
        int returnValue = 0;
        try {
            this.nsePriceHistoryRepository = nsePriceHistoryRepository;
            this.bsePriceHistoryRepository = bsePriceHistoryRepository;
            this.mutualFundNavHistoryRepository = mutualFundNavHistoryRepository;
            this.dailyDataSRepository = dailyDataSRepository;
            this.mutualFundUniverseRepository = mutualFundUniverseRepository;
            Properties properties = new Properties();
            properties.load(new FileInputStream("C:\\MyDocuments\\03Business\\03DailyData\\downloadconfig.property"));
            returnValue = downloadAndUploadBSEBhavCopy(properties);
            if (returnValue < 0)
                return returnValue;
            returnValue = downloadAndUploadNSEBhavCopy(properties);
            if (returnValue < 0)
                return returnValue;
            returnValue = downloadAndUploadMutualFundNAV(properties);
            if (returnValue < 0)
                return returnValue;
//        downloadScreenerFile(properties);
            returnValue = convertFromCSVToXLSXAndUploadScreenerFile(properties);
            if (returnValue < 0)
                return returnValue;
        } catch (Exception e){
            return -1;
        }
        return returnValue;
    }

    public int downloadAndUploadNSEBhavCopy(Properties properties) throws IOException {
        String nseDomain = properties.getProperty("nseDomain"); //https://www1.nseindia.com
        String nseBhavCopyZipFileName = properties.getProperty("nsebhavcopyzipfilename");
        String nseBhavCopyZipFileExtractPath = properties.getProperty("nsebhavcopyzipfileextractpath");

        String filename = getNSEURL(getDateFromBseBhavCopy(properties));
        String href = nseDomain + filename;
        System.out.println(href);
        URL downloadUrl = new URL(href);
        InputStream inputStream = downloadUrl.openStream();
        ReadableByteChannel byteChannel = Channels.newChannel(inputStream);
        FileOutputStream outputStream = new FileOutputStream(nseBhavCopyZipFileName);
        outputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
        outputStream.close();

        // Create a new input stream from the downloaded file
        FileInputStream fileInputStream = new FileInputStream(nseBhavCopyZipFileName);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry;
        String fileName = "";
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            fileName = zipEntry.getName();
            FileOutputStream fileOutputStream = new FileOutputStream(nseBhavCopyZipFileExtractPath + fileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.close();
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
        return uploadNseDailyPriceData(nseBhavCopyZipFileExtractPath + fileName);
    }

    public int downloadAndUploadBSEBhavCopy(Properties properties) throws IOException {
        String url = properties.getProperty("bsebhavcopyurl"); //"https://www.bseindia.com/markets/MarketInfo/BhavCopy.aspx";
        String bseBhavCopyLinkSearch = "a:contains(" + properties.getProperty("bsebhavcopylinksearch") + ")"; //"a:contains(Equity with ISIN)"
        String bseBhavCopyZipFileName = properties.getProperty("bsebhavcopyzipfilename");
        String bseBhavCopyZipFileExtractPath = properties.getProperty("bsebhavcopyzipfileextractpath");

        Document doc = Jsoup.connect(url).get();
        Element link = doc.selectFirst(bseBhavCopyLinkSearch);
        String href = link.attr("href");
        String downloadUrl = href;
        String referer = properties.getProperty("bsebhavcopyrefer"); //"https://www.bseindia.com/markets/equity/EQReports/Equitydebcopy.aspx";
        String cookie = properties.getProperty("bsebhavcopycookie"); //"BSEindia=ASP.NET_SessionId=5wm5p5f5uz5efj55c4wwrs55; bse+utmcampaign=direct; bse+utmmedium=direct; bse+utmsource=direct; bse+utmcontent=direct; __utmc=202936964; __utmz=202936964.1648111371.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); _ga=GA1.2.2076863416.1648111371; _gid=GA1.2.575918930.1648111371; bse+GA1.2.2076863416.1648111371=Direct; __utma=202936964.2076863416.1648111371.1648111371.1648111371.1; __utmt=1; bse+utmcs=windows-1252; bse+timezone=Asia%2FCalcutta; __utmb=202936964.2.10.1648111371; _gat=1";

        Connection.Response response = Jsoup.connect(downloadUrl)
                .referrer(referer)
                .header("Cookie", cookie)
                .maxBodySize(0)
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute();

        InputStream inputStream = response.bodyStream();
        FileOutputStream outputStream = new FileOutputStream(bseBhavCopyZipFileName); //"C:/MyDocuments/03Business/03DailyData/bse_bhavcopy.zip"
        outputStream.getChannel().transferFrom(Channels.newChannel(inputStream), 0, Long.MAX_VALUE);
        outputStream.close();

        // Create a new input stream from the downloaded file
        FileInputStream fileInputStream = new FileInputStream(bseBhavCopyZipFileName);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry;
        String fileName = "";
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            fileName = zipEntry.getName();
            FileOutputStream fileOutputStream = new FileOutputStream(bseBhavCopyZipFileExtractPath + fileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.close();
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
        return uploadBseDailyPriceData(bseBhavCopyZipFileExtractPath + fileName);
    }

    public int downloadAndUploadMutualFundNAV(Properties properties) throws IOException {
        String url = properties.getProperty("mutualfundurl");
        String mutualFundFilePath = properties.getProperty("mutualfundfilename");
        URL downloadUrl = new URL(url);

        InputStream inputStream = downloadUrl.openStream();
        ReadableByteChannel byteChannel = Channels.newChannel(inputStream);
        String strDate = getDateFromNseBhavCopy(properties); //new SimpleDateFormat("yyyyMMdd").format(new Date());

        FileOutputStream outputStream = new FileOutputStream(mutualFundFilePath+strDate+"MutualFund.txt");
        outputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
        outputStream.close();
        return uploadMFNAVData(mutualFundFilePath+strDate+"MutualFund.txt");
    }

    public void downloadScreenerFile(Properties properties) throws IOException {
        // Set URL and login credentials
        String loginUrl = properties.getProperty("screenerloginurl");
        Document loginPage = Jsoup.connect(loginUrl).get();
        String csrfToken = loginPage.selectFirst("input[name=csrfmiddlewaretoken]").attr("value");
        String username = properties.getProperty("screenerusername");
        String password = properties.getProperty("screenerpassword");

        HashMap<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);
        loginData.put("csrfmiddlewaretoken", csrfToken);
        loginData.put("next", "");

        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36";
        Connection.Response loginResponse = Jsoup.connect(loginUrl)
                .userAgent(userAgent)
                .data(loginData)
                .method(Connection.Method.POST)
                .header("Connection", "keep-alive")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("accept-encoding", "gzip, deflate, br")
                .header("accept-language", "en-GB,en;q=0.9")
                .header("referer", "https://www.screener.in/login/?")
                .header("sec-fetch-dest", "document")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "same-origin")
                .header("sec-ch-ua", "\"Google Chrome\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"")
                .header("upgrade-insecure-requests", "1")
                .header("cookie", "_gcl_au=1.1.1752722124.1679743703; _ga=GA1.2.1071712217.1679743703; _gid=GA1.2.648591741.1679743703; csrftoken=0OT8uzQ5oN1rjhnoGUbrrKXD4RzgNweV")
                .execute();

        if (loginResponse.cookies().isEmpty()) {
            System.out.println("Login failed!");
            return;
        }

        String downloadUrl = properties.getProperty("screenerdownloadurl");
        String screenerFileName = properties.getProperty("screenerfilename");

        Connection.Response downloadResponse = Jsoup.connect(downloadUrl)
                .cookies(loginResponse.cookies())
                .ignoreContentType(true)
                .execute();
        Files.write(Paths.get(screenerFileName), downloadResponse.bodyAsBytes());

        /*Connection.Response response = Jsoup.connect(downlodUrl)
                .data("email", username)
                .data("password", password)
                .method(Connection.Method.POST)
                .execute();*/
        // Get input stream and save to file
        /*InputStream inputStream = response.bodyStream();
        FileOutputStream outputStream = new FileOutputStream(screenerFileName);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();*/
    }

    public int convertFromCSVToXLSXAndUploadScreenerFile(Properties properties) throws IOException {
        String csvFilePath = properties.getProperty("csvfilepath");
        String dateString = getDateFromNseBhavCopy(properties);
        String xlsxFilePath = properties.getProperty("xlsxfilepath") + dateString + "ScreenerDaily.xlsx";

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("core-watchlist");
        String currentLine;
        int rowNum = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while ((currentLine = br.readLine()) != null) {
                String[] data = currentLine.split(",");
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < data.length; i++) {
                    Cell cell = row.createCell(i);
                    if (rowNum > 1 && i > 3) {
                        if (data[i] == null || data[i].isEmpty()) {
                            cell.setCellValue(0);
                        } else {
                            cell.setCellValue(Double.parseDouble(data[i]));
                        }
                    } else {
                        cell.setCellValue(data[i]);
                    }
                }
            }
            FileOutputStream outputStream = new FileOutputStream(xlsxFilePath);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            workbook.close();
            e.printStackTrace();
        }
        return uploadDailyDataS(xlsxFilePath, dateString);
    }

    private String getDateFromNseBhavCopy(Properties properties) {
        String zipFilePath = properties.getProperty("nsebhavcopyzipfilename");
        String formattedDateString = "";

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zis.getNextEntry();
            String fileName = entry.getName();
            String dateString = fileName.substring(2, 11);
            String year = dateString.substring(5);
            String month = getMonthNumber(dateString.substring(2, 5));
            String day = dateString.substring(0, 2);
            formattedDateString = year + month + day;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return formattedDateString;
    }

    private String getDateFromBseBhavCopy(Properties properties) {
        String zipFilePath = properties.getProperty("bsebhavcopyzipfilename");
        String formattedDateString = "";

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zis.getNextEntry();
            String fileName = entry.getName();
            String dateString = fileName.substring(12, 18);
            String year = "20" + dateString.substring(4);
            String month = dateString.substring(2, 4);
            String day = dateString.substring(0, 2);
            formattedDateString = year + month + day;
//            System.out.println(formattedDateString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return formattedDateString;
    }

    private String getMonthNumber(String monthString) {
        switch (monthString) {
            case "JAN":
                return "01";
            case "FEB":
                return "02";
            case "MAR":
                return "03";
            case "APR":
                return "04";
            case "MAY":
                return "05";
            case "JUN":
                return "06";
            case "JUL":
                return "07";
            case "AUG":
                return "08";
            case "SEP":
                return "09";
            case "OCT":
                return "10";
            case "NOV":
                return "11";
            case "DEC":
                return "12";
            default:
                throw new IllegalArgumentException("Invalid month: " + monthString);
        }
    }

    private String getNSEURL(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String url = String.format("/%d/%s/cm%sbhav.csv.zip", localDate.getYear(), localDate.getMonth().toString().substring(0, 3).toUpperCase(), localDate.format(DateTimeFormatter.ofPattern("ddMMMyyyy")).toUpperCase());
//        System.out.println(url);
        return url;
    }

    private int uploadNseDailyPriceData(String file){

        if (file == null || file.isEmpty()) {
            System.out.println("NSE File is empty");
            return -1;
        }
        try {
            /*File csvFile = new File(file);
            csvFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(file.getBytes());
            fos.close();
//            file.transferTo(csvFile);*/
            Scanner scanner = new Scanner(new File(file));
            boolean isHeader = true;
            CSVUtils csvUtils = new CSVUtils();
            List<NsePriceHistory> nsePriceHistories = new ArrayList<>();
            int nseTickerPosition = -1, seriesPosition = -1, openPosition = -1,
                    highPosition = -1, lowPosition = -1, closePosition = -1,
                    lastPosition = -1, previousClosePosition = -1, totalTradedQuantityPosition = -1,
                    totalTradedValuePosition = -1, dateStringPosition = -1, totalTradesPosition = -1, isinCodePosition = -1;
            while (scanner.hasNext()) {
                List<String> line = csvUtils.parseLine(scanner.nextLine());
                if(isHeader){
                    isHeader = false;
                    for (int i=0; i<line.size(); i++) {
                        String column = line.get(i);
                        if (column.trim().equalsIgnoreCase("SYMBOL")){
                            nseTickerPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("SERIES")){
                            seriesPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("OPEN") || column.trim().equalsIgnoreCase("Open Price")){
                            openPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("HIGH") || column.trim().equalsIgnoreCase("High Price")){
                            highPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LOW") || column.trim().equalsIgnoreCase("Low Price")){
                            lowPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LOW") || column.trim().equalsIgnoreCase("Low Price")){
                            lowPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("CLOSE") || column.trim().equalsIgnoreCase("Close Price")){
                            closePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LAST") || column.trim().equalsIgnoreCase("Last Price")){
                            lastPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("PREVCLOSE") || column.trim().equalsIgnoreCase("Prev Close")){
                            previousClosePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TOTTRDQTY") || column.trim().equalsIgnoreCase("Total Traded Quantity")){
                            totalTradedQuantityPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TOTTRDVAL") || column.trim().equalsIgnoreCase("Turnover")){
                            totalTradedValuePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TIMESTAMP") || column.trim().equalsIgnoreCase("Date")){
                            dateStringPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TOTALTRADES") || column.trim().equalsIgnoreCase("No. of Trades")){
                            totalTradesPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISINISIN") ){
                            isinCodePosition = i;
                        }
                    }
                    continue;
                }
                if (nseTickerPosition == -1 || dateStringPosition == -1){
                    continue;
                }
                String nseTicker = line.get(nseTickerPosition);
                String dateString = line.get(dateStringPosition);
                if(nseTicker == null || nseTicker.isEmpty()|| dateString == null || dateString.isEmpty()){
                    continue;
                }
                String series = line.get(seriesPosition);
                if(!(series.equalsIgnoreCase("EQ") ||
                        series.equalsIgnoreCase("BM") ||
                        series.equalsIgnoreCase("SM") ||
                        series.equalsIgnoreCase("BE"))){
                    continue;
                }
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                java.sql.Date date = null;
                try {
                    date = new java.sql.Date(format.parse(dateString).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                BigDecimal openPrice = new BigDecimal(0);
                if(openPosition > -1){
                    openPrice = line.get(openPosition) != null && !line.get(openPosition).isEmpty() ? new BigDecimal(line.get(openPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal highPrice = new BigDecimal(0);
                if(highPosition > -1) {
                    highPrice = line.get(highPosition) != null && !line.get(highPosition).isEmpty() ? new BigDecimal(line.get(highPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal lowPrice = new BigDecimal(0);
                if(lowPosition > -1) {
                    lowPrice = line.get(lowPosition) != null && !line.get(lowPosition).isEmpty() ? new BigDecimal(line.get(lowPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal closePrice = new BigDecimal(0);
                if (closePosition > -1) {
                    closePrice = line.get(closePosition) != null && !line.get(closePosition).isEmpty() ? new BigDecimal(line.get(closePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal lastPrice = new BigDecimal(0);
                if (lastPosition > -1) {
                    lastPrice = line.get(lastPosition) != null && !line.get(lastPosition).isEmpty() ? new BigDecimal(line.get(lastPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal previousClosePrice = new BigDecimal(0);
                if (previousClosePosition > -1) {
                    previousClosePrice = line.get(previousClosePosition) != null && !line.get(previousClosePosition).isEmpty() ? new BigDecimal(line.get(previousClosePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTradedQuantity = new BigDecimal(0);
                if (totalTradedQuantityPosition > -1){
                    totalTradedQuantity = line.get(totalTradedQuantityPosition) != null && !line.get(totalTradedQuantityPosition).isEmpty() ? new BigDecimal(line.get(totalTradedQuantityPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTradedValue = new BigDecimal(0);
                if (totalTradesPosition > -1) {
                    totalTradedValue = line.get(totalTradedValuePosition) != null && !line.get(totalTradedValuePosition).isEmpty() ? new BigDecimal(line.get(totalTradedValuePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTrades = new BigDecimal(0);
                if (totalTradesPosition > -1) {
                    totalTrades = line.get(totalTradesPosition) != null && !line.get(totalTradesPosition).isEmpty() ? new BigDecimal(line.get(totalTradesPosition).trim()) : new BigDecimal(0);
                }

                String isinCode = "";
                if (isinCodePosition > -1) { isinCode = line.get(isinCodePosition);}

                NsePriceHistory nsePriceHistory = new NsePriceHistory();
                nsePriceHistory.setKey(new NsePriceHistory.NsePriceHistoryKey(nseTicker, date));
                nsePriceHistory.setSeries(series);
                nsePriceHistory.setOpenPrice(openPrice);
                nsePriceHistory.setHighPrice(highPrice);
                nsePriceHistory.setLowPrice(lowPrice);
                nsePriceHistory.setClosePrice(closePrice);
                nsePriceHistory.setLastPrice(lastPrice);
                nsePriceHistory.setPreviousClosePrice(previousClosePrice);
                nsePriceHistory.setTotalTradedQuantity(totalTradedQuantity);
                nsePriceHistory.setTotalTradedValue(totalTradedValue);
                nsePriceHistory.setTotalTrades(totalTrades);
                nsePriceHistory.setInsinCode(isinCode);

                nsePriceHistories.add(nsePriceHistory);
            }
            //nsePriceHistories.sort(Comparator.comparing(CompanyDailyDataG::getCompanyDailyMarketCapNo).reversed());
            nsePriceHistoryRepository.saveAll(nsePriceHistories);
            scanner.close();

            System.out.println("Successfully uploaded NSE File " + file );

        } catch (Exception e) {
            System.out.println("Error in uploadeding NSE File " + file );
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private int uploadBseDailyPriceData(String file) {
        if (file == null || file.isEmpty()) {
            System.out.println("BSE File is empty");
            return -1;
        }
        try {
            /*File csvFile = new File(file);
            csvFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(file.getBytes());
            fos.close();
//            file.transferTo(csvFile);*/
            Scanner scanner = new Scanner(new File(file));
            boolean isHeader = true;
            CSVUtils csvUtils = new CSVUtils();
            List<BsePriceHistory> bsePriceHistories = new ArrayList<>();

            while (scanner.hasNext()) {
                List<String> line = csvUtils.parseLine(scanner.nextLine());
                if(isHeader){
                    isHeader = false;
                    continue;
                }
                String bseTicker = line.get(0);
                String dateString = line.get(15);
                if(bseTicker == null || bseTicker.isEmpty()|| dateString == null || dateString.isEmpty()){
                    continue;
                }
                String companyName = line.get(1);
                String companyGroup = line.get(2).trim();
                String companyType = line.get(3);

                if(!(companyGroup.equalsIgnoreCase("A") ||
                        companyGroup.equalsIgnoreCase("B") ||
                        companyGroup.equalsIgnoreCase("T"))){
                    continue;
                }
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy");
                java.sql.Date date = null;
                try {
                    date = new java.sql.Date(format.parse(dateString).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                BigDecimal opernPrice = line.get(4) != null && !line.get(4).isEmpty() ? new BigDecimal(line.get(4)) : new BigDecimal(0);
                BigDecimal highPrice = line.get(5) != null && !line.get(5).isEmpty() ? new BigDecimal(line.get(5)) : new BigDecimal(0);
                BigDecimal lowPrice = line.get(6) != null && !line.get(6).isEmpty() ? new BigDecimal(line.get(6)) : new BigDecimal(0);
                BigDecimal closePrice = line.get(7) != null && !line.get(7).isEmpty() ? new BigDecimal(line.get(7)) : new BigDecimal(0);
                BigDecimal lastPrice = line.get(8) != null && !line.get(8).isEmpty() ? new BigDecimal(line.get(8)) : new BigDecimal(0);
                BigDecimal previousClosePrice = line.get(9) != null && !line.get(9).isEmpty() ? new BigDecimal(line.get(9)) : new BigDecimal(0);
                BigDecimal totalTrades = line.get(10) != null && !line.get(10).isEmpty() ? new BigDecimal(line.get(10)) : new BigDecimal(0);

                BigDecimal totalTradedQuantity = line.get(11) != null && !line.get(11).isEmpty() ? new BigDecimal(line.get(11)) : new BigDecimal(0);
                BigDecimal totalTradedValue = line.get(12) != null && !line.get(12).isEmpty() ? new BigDecimal(line.get(12)) : new BigDecimal(0);;

                String isinCode = line.get(14);

                BsePriceHistory bsePriceHistory = new BsePriceHistory();
                bsePriceHistory.setKey(new BsePriceHistory.BsePriceHistoryKey(bseTicker, date));
                bsePriceHistory.setCompanyName(companyName);
                bsePriceHistory.setCompanyGroup(companyGroup);
                bsePriceHistory.setCompanyType(companyType);
                bsePriceHistory.setOpenPrice(opernPrice);
                bsePriceHistory.setHighPrice(highPrice);
                bsePriceHistory.setLowPrice(lowPrice);
                bsePriceHistory.setClosePrice(closePrice);
                bsePriceHistory.setLastPrice(lastPrice);
                bsePriceHistory.setPreviousClosePrice(previousClosePrice);
                bsePriceHistory.setTotalTradedQuantity(totalTradedQuantity);
                bsePriceHistory.setTotalTradedValue(totalTradedValue);
                bsePriceHistory.setTotalTrades(totalTrades);
                bsePriceHistory.setInsinCode(isinCode);

                bsePriceHistories.add(bsePriceHistory);
            }
            //nsePriceHistories.sort(Comparator.comparing(CompanyDailyDataG::getCompanyDailyMarketCapNo).reversed());

            bsePriceHistoryRepository.saveAll(bsePriceHistories);
            scanner.close();

            System.out.println("Successfully uploaded BSE File " + file );

        } catch (Exception e) {
            System.out.println("Error in uploadeding BSE File " + file );
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private int uploadMFNAVData(String file){
        if (file == null || file.isEmpty()) {
            System.out.println("MF NAV File is empty");
            return -1;
        }
        try {
            Scanner scanner = new Scanner(new File(file));
            boolean isHeader = true;
            CSVUtils csvUtils = new CSVUtils();
            List<MutualFundNavHistory> mutualFundNavHistories = new ArrayList<>();
            int positionOfSchemeCode = 0, positionOfDate = 0, positionOfNav = 0, schemeNamePostion = 0, isinPosition = 0, isinReinvestmentPosition = 0;
            while (scanner.hasNext()) {
                List<String> line = csvUtils.parseLine(scanner.nextLine(),';');
                if(isHeader){
                    isHeader = false;
                    for (int i=0; i<line.size(); i++) {
                        String column = line.get(i);
                        if (column.trim().equalsIgnoreCase("Scheme Code")){
                            positionOfSchemeCode = i;
                        }
                        if (column.trim().equalsIgnoreCase("Net Asset Value") ||
                                column.trim().equalsIgnoreCase("NAV")){
                            positionOfNav = i;
                        }
                        if (column.trim().equalsIgnoreCase("Date")){
                            positionOfDate = i;
                        }
                        if (column.trim().equalsIgnoreCase("Scheme Name")){
                            schemeNamePostion = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISIN Div Payout/ISIN Growth")){
                            isinPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISIN Div Reinvestment")){
                            isinReinvestmentPosition = i;
                        }
                    }
                    continue;
                }
                if(line.isEmpty()|| line.size() < 2){ //any blank spaces
                    continue;
                }
                Integer code = Integer.parseInt(line.get(positionOfSchemeCode));
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                java.sql.Date date = null;

                try {
                    date = new java.sql.Date(format.parse(line.get(positionOfDate)).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                BigDecimal nav = null;
                try {
                    nav = line.get(positionOfNav)!=null && !line.get(positionOfNav).isEmpty() ? new BigDecimal(line.get(positionOfNav)) : new BigDecimal(0);
                } catch (NumberFormatException e) {
                    System.out.println(String.format("/Error in NAV for MutualFund(List)/%d/", code));
                    nav = new BigDecimal(0);
                }
                String schemeName = line.get(schemeNamePostion);
                schemeName.trim();
                String isin = "";
                if(isinPosition != 0) {
                    isin = line.get(isinPosition);
                }
                String isinReinvestment = "";
                if(isinReinvestmentPosition != 0) {
                    isinReinvestment = line.get(isinReinvestmentPosition);
                }

                MutualFundNavHistory mutualFundNavHistory = new MutualFundNavHistory();
                mutualFundNavHistory.setKey(new MutualFundNavHistory.MutualFundNavHistoryKey(code, date));
                mutualFundNavHistory.setNav(nav);
                mutualFundNavHistories.add(mutualFundNavHistory);
                int countBySchemeCode = mutualFundUniverseRepository.countBySchemeCode(new Long(code));

                if (countBySchemeCode == 0 && date.after(CommonService.getSetupDates().getDateLastTradingDay())){
                    MutualFundUniverse mutualFundUniverse = new MutualFundUniverse();
                    mutualFundUniverse.setSchemeCode(new Long(code));
                    mutualFundUniverse.setSchemeNameFull(schemeName);
                    mutualFundUniverse.setIsinDivPayoutIsinGrowth(isin);
                    mutualFundUniverse.setIsinDivReinvestment(isinReinvestment);
                    mutualFundUniverse.setDateLatestNav(date);
                    mutualFundUniverse.setFundHouse("XXX");
                    mutualFundUniverse.setSchemeNamePart("XXX");
                    mutualFundUniverse.setLatestNav(nav);
                    if(schemeName.toLowerCase().contains("direct")){
                        mutualFundUniverse.setDirectRegular("Direct");
                    }
                    if(schemeName.toLowerCase().contains("regular")){
                        mutualFundUniverse.setDirectRegular("Regular");
                    }
                    if(schemeName.toLowerCase().contains("dividend")) {
                        mutualFundUniverse.setDividendGrowth("Dividend");
                    }
                    if(schemeName.toLowerCase().contains("growth")) {
                        mutualFundUniverse.setDividendGrowth("Growth");
                    }
                    mutualFundUniverse.setIsinDivPayoutIsinGrowth("XXX");
                    mutualFundUniverseRepository.save(mutualFundUniverse);
                } /*else { //Run this code once in a quarter or month (offline) to update Mutual Fund
                    MutualFundUniverse existingFund = mutualFundUniverseRepository.findBySchemeCode(new Long(code));
                    if(!existingFund.getSchemeNameFull().equals(schemeName)
                            || (existingFund.getIsinDivReinvestment() != null && !existingFund.getIsinDivReinvestment().equals(isinReinvestment))
                            || (existingFund.getIsinDivPayoutIsinGrowth() != null && !existingFund.getIsinDivPayoutIsinGrowth().equals(isin))) {
                        existingFund.setSchemeNameFull(schemeName);
                        existingFund.setIsinDivPayoutIsinGrowth(isin);
                        existingFund.setIsinDivReinvestment(isinReinvestment);
                        //String temp = "Updating Fund Full Name "+code+"-Old Name: "+existingFund.getSchemeNameFull()+"; New Name: "+schemeName;
                        logger.debug(String.format("Name changed for %d", code));
                        mutualFundUniverseRepository.save(existingFund);
                    }
                }*/
            }
            mutualFundNavHistories.sort(Comparator.comparing(l->l.getKey().getSchemeCode()));
            //mutualFundRepository.deleteAllInBatch();
            mutualFundNavHistoryRepository.saveAll(mutualFundNavHistories);
            scanner.close();

            System.out.println("Successfully uploaded MF NAV File " + file );
        } catch (Exception e) {
            System.out.println("Error in uploadeding MF NAV File " + file );
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private int uploadDailyDataS(String file, String dateString){
        if (file == null || file.isEmpty()) {
            System.out.println("Daily Screener Data File is empty");
            return -1;
        }
        try {
            List<DailyDataS> dailyDataSList = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(new File(file));
            XSSFSheet worksheet = workbook.getSheetAt(0);
            java.sql.Date date =  java.sql.Date.valueOf(dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6));

            for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
                DailyDataS dailyDataS = new DailyDataS();

                XSSFRow row = worksheet.getRow(i);
                dailyDataS.setKey(new DailyDataS.DailyDataSKey());
                dailyDataS.getKey().setDate(date); // set as date
                dailyDataS.setRank(i);

                if(row.getCell(2).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(2).getCellType() == Cell.CELL_TYPE_STRING && !row.getCell(2).getStringCellValue().trim().isEmpty())
                    dailyDataS.getKey().setName((String) row.getCell(2).getStringCellValue());
                else if (row.getCell(1).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(1).getCellType() == Cell.CELL_TYPE_STRING)
                    dailyDataS.getKey().setName((String) row.getCell(1).getStringCellValue());
                else
                    continue;

                if(row.getCell(4) != null && row.getCell(4).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(4).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setCmp(new BigDecimal(row.getCell(4).getNumericCellValue()));
                else
                    dailyDataS.setCmp(new BigDecimal(0));

                if(row.getCell(5) != null && row.getCell(5).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(5).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setMarketCap(new BigDecimal(row.getCell(5).getNumericCellValue()));
                else
                    dailyDataS.setMarketCap(new BigDecimal(0));

                if(row.getCell(6) != null && row.getCell(6).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(6).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setLastResultDate((int) row.getCell(6).getNumericCellValue());
                else
                    dailyDataS.setLastResultDate(0);

                if(row.getCell(7) != null && row.getCell(7).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(7).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNetProfit(new BigDecimal(row.getCell(7).getNumericCellValue()));
                else
                    dailyDataS.setNetProfit(new BigDecimal(0));

                if(row.getCell(8) != null && row.getCell(8).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(8).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSales(new BigDecimal(row.getCell(8).getNumericCellValue()));
                else
                    dailyDataS.setSales(new BigDecimal(0));

                if(row.getCell(9) != null && row.getCell(9).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(9).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setYoyQuarterlySalesGrowth(new BigDecimal(row.getCell(9).getNumericCellValue()));
                else
                    dailyDataS.setYoyQuarterlySalesGrowth(new BigDecimal(0));

                if(row.getCell(10) != null && row.getCell(10).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(10).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setYoyQuarterlyProfitGrowth(new BigDecimal(row.getCell(10).getNumericCellValue()));
                else
                    dailyDataS.setYoyQuarterlyProfitGrowth(new BigDecimal(0));

                if(row.getCell(11) != null && row.getCell(11).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(11).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setQoqSalesGrowth(new BigDecimal(row.getCell(11).getNumericCellValue()));
                else
                    dailyDataS.setQoqSalesGrowth(new BigDecimal(0));

                if(row.getCell(12) != null && row.getCell(12).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(12).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setQoqProfitGrowth(new BigDecimal(row.getCell(12).getNumericCellValue()));
                else
                    dailyDataS.setQoqProfitGrowth(new BigDecimal(0));

                if(row.getCell(13) != null && row.getCell(13).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(13).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setOpmLatestQuarter(new BigDecimal(row.getCell(13).getNumericCellValue()));
                else
                    dailyDataS.setOpmLatestQuarter(new BigDecimal(0));

                if(row.getCell(14) != null && row.getCell(14).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(14).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setOpmLastYear(new BigDecimal(row.getCell(14).getNumericCellValue()));
                else
                    dailyDataS.setOpmLastYear(new BigDecimal(0));

                if(row.getCell(15) != null && row.getCell(15).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(15).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNpmLatestQuarter(new BigDecimal(row.getCell(15).getNumericCellValue()));
                else
                    dailyDataS.setNpmLatestQuarter(new BigDecimal(0));

                if(row.getCell(16) != null && row.getCell(16).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(16).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNpmLastYear(new BigDecimal(row.getCell(16).getNumericCellValue()));
                else
                    dailyDataS.setNpmLastYear(new BigDecimal(0));

                if(row.getCell(17) != null && row.getCell(17).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(17).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setProfitGrowth3years(new BigDecimal(row.getCell(17).getNumericCellValue()));
                else
                    dailyDataS.setProfitGrowth3years(new BigDecimal(0));

                if(row.getCell(18) != null && row.getCell(18).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(18).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSalesGrowth3years(new BigDecimal(row.getCell(18).getNumericCellValue()));
                else
                    dailyDataS.setSalesGrowth3years(new BigDecimal(0));

                if(row.getCell(19) != null && row.getCell(19).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(19).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setPeTtm(new BigDecimal(row.getCell(19).getNumericCellValue()));
                else
                    dailyDataS.setPeTtm(new BigDecimal(0));

                if(row.getCell(20) != null && row.getCell(20).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(20).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setHistoricalPe3years(new BigDecimal(row.getCell(20).getNumericCellValue()));
                else
                    dailyDataS.setHistoricalPe3years(new BigDecimal(0));

                if(row.getCell(21) != null && row.getCell(21).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(21).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setPegRatio(new BigDecimal(row.getCell(21).getNumericCellValue()));
                else
                    dailyDataS.setPegRatio(new BigDecimal(0));

                if(row.getCell(22) != null && row.getCell(22).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(22).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setPbTtm(new BigDecimal(row.getCell(22).getNumericCellValue()));
                else
                    dailyDataS.setPbTtm(new BigDecimal(0));

                if(row.getCell(23) != null && row.getCell(23).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(23).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setEvToEbit(new BigDecimal(row.getCell(23).getNumericCellValue()));
                else
                    dailyDataS.setEvToEbit(new BigDecimal(0));

                if(row.getCell(24) != null && row.getCell(24).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(24).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDividendPayout(new BigDecimal(row.getCell(24).getNumericCellValue()));
                else
                    dailyDataS.setDividendPayout(new BigDecimal(0));

                if(row.getCell(25) != null && row.getCell(25).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(25).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setRoe(new BigDecimal(row.getCell(25).getNumericCellValue()));
                else
                    dailyDataS.setRoe(new BigDecimal(0));

                if(row.getCell(26) != null && row.getCell(26).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(26).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setAvgRoe3years(new BigDecimal(row.getCell(26).getNumericCellValue()));
                else
                    dailyDataS.setAvgRoe3years(new BigDecimal(0));

                if(row.getCell(27) != null && row.getCell(27).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(27).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDebt(new BigDecimal(row.getCell(27).getNumericCellValue()));
                else
                    dailyDataS.setDebt(new BigDecimal(0));

                if(row.getCell(28) != null && row.getCell(28).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(28).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDebtToEquity(new BigDecimal(row.getCell(28).getNumericCellValue()));
                else
                    dailyDataS.setDebtToEquity(new BigDecimal(0));

                if(row.getCell(29) != null && row.getCell(29).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(29).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDebt3yearsback(new BigDecimal(row.getCell(29).getNumericCellValue()));
                else
                    dailyDataS.setDebt3yearsback(new BigDecimal(0));

                if(row.getCell(30) != null && row.getCell(30).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(30).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setRoce(new BigDecimal(row.getCell(30).getNumericCellValue()));
                else
                    dailyDataS.setRoce(new BigDecimal(0));

                if(row.getCell(31) != null && row.getCell(31).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(31).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setAvgRoce3years(new BigDecimal(row.getCell(31).getNumericCellValue()));
                else
                    dailyDataS.setAvgRoce3years(new BigDecimal(0));

                if(row.getCell(32) != null && row.getCell(32).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(32).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setFcfS(new BigDecimal(row.getCell(32).getNumericCellValue()));
                else
                    dailyDataS.setFcfS(new BigDecimal(0));

                if(row.getCell(33) != null && row.getCell(33).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(33).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSalesGrowth5years(new BigDecimal(row.getCell(33).getNumericCellValue()));
                else
                    dailyDataS.setSalesGrowth5years(new BigDecimal(0));

                if(row.getCell(34) != null && row.getCell(34).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(34).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSalesGrowth10years(new BigDecimal(row.getCell(34).getNumericCellValue()));
                else
                    dailyDataS.setSalesGrowth10years(new BigDecimal(0));

                if(row.getCell(35) != null && row.getCell(35).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(35).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNoplat(new BigDecimal(row.getCell(35).getNumericCellValue()));
                else
                    dailyDataS.setNoplat(new BigDecimal(0));

                if(row.getCell(36) != null && row.getCell(36).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(36).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setCapex(new BigDecimal(row.getCell(36).getNumericCellValue()));
                else
                    dailyDataS.setCapex(new BigDecimal(0));

                if(row.getCell(37) != null && row.getCell(37).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(37).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setFcff(new BigDecimal(row.getCell(37).getNumericCellValue()));
                else
                    dailyDataS.setFcff(new BigDecimal(0));

                if(row.getCell(38) != null && row.getCell(38).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(38).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setInvestedCapital(new BigDecimal(row.getCell(38).getNumericCellValue()));
                else
                    dailyDataS.setInvestedCapital(new BigDecimal(0));

                if(row.getCell(39) != null && row.getCell(39).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(39).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setRoic(new BigDecimal(row.getCell(39).getNumericCellValue()));
                else
                    dailyDataS.setRoic(new BigDecimal(0));

                dailyDataS.setMcapToNetprofit(new BigDecimal(0));
                dailyDataS.setMcapToSales(new BigDecimal(0));
                dailyDataS.setSector("");
                dailyDataS.setIndustry("");
                dailyDataS.setSubIndustry("");

                dailyDataSList.add(dailyDataS);
            }
            dailyDataSList.sort(Comparator.comparing(DailyDataS::getMarketCap).reversed());
            dailyDataSRepository.saveAll(dailyDataSList);

            System.out.println("Successfully uploaded Screener Data File " + file );
        } catch (Exception e) {
            System.out.println("Error in uploadeding Screener Data File " + file );
            e.printStackTrace();
            return -1;
        }
        return 0;

    }
}
