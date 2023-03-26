package com.timelineofwealth.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadEODFiles {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("C:\\MyDocuments\\03Business\\03DailyData\\downloadconfig.property"));
//        downloadNSEBhavCopy(properties);
//        downloadBSEBhavCopy(properties);
//        downloadMutualFundNAV(properties);
        downloadScreenerFile(properties);
    }

    public static void downloadNSEBhavCopy(Properties properties) throws IOException {
        String url = properties.getProperty("nsebhavcopyurl");
        String nseBhavCopyLinkSearch = "a:contains(" + properties.getProperty("nsebhavcopylinksearch") + ")";
        String nseDomain = properties.getProperty("nseDomain"); //https://www1.nseindia.com
        String nseBhavCopyZipFileName = properties.getProperty("nsebhavcopyzipfilename");
        String nseBhavCopyZipFileExtractPath = properties.getProperty("nsebhavcopyzipfileextractpath");

        Document doc = Jsoup.connect(url).get();
        Element link = doc.selectFirst(nseBhavCopyLinkSearch);
        String href = nseDomain + link.attr("href");
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
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
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
    }

    public static void downloadBSEBhavCopy(Properties properties) throws IOException {
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
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
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
    }

    public static void downloadMutualFundNAV(Properties properties) throws IOException {
        URL downloadUrl = new URL("https://www.amfiindia.com/spages/NAVAll.txt");
        InputStream inputStream = downloadUrl.openStream();
        ReadableByteChannel byteChannel = Channels.newChannel(inputStream);
        String strDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        FileOutputStream outputStream = new FileOutputStream("C:/MyDocuments/03Business/03DailyData/MutualFunds/"+strDate+"MutualFund.txt");
        outputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
        outputStream.close();
    }

    public static void downloadScreenerFile(Properties properties) throws IOException {
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
}
