package com.timelineofwealth.service;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MOSLPDFExtractor {

    /** Matches an 8-digit yyyyMMdd date in a URL, e.g. ...POLYCAB-20261119-MOFSL-CU-PG010.pdf */
    private static final Pattern URL_DATE = Pattern.compile("(?<!\\d)(20\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01]))(?!\\d)");

    public static void main(String[] args) throws IOException {
        // Load config file
        Properties config = new Properties();
        config.load(new FileInputStream("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\MOSLMorningIndia\\config.properties"));

        // Get source file name from config
        String sourceFile = config.getProperty("sourceFile");

        // Open PDF document
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile));

        // Iterate through configured links
        for (int i = 1; config.containsKey("link" + i); i++) {
            try {
                int pageNum = Integer.parseInt(config.getProperty("link" + i).trim());
                String destFolder = config.getProperty("destFolder" + i);
                String fileName = config.getProperty("fileName" + i);

                // A page can carry several link annotations (current report plus older/broken ones).
                // Collect them all, then download only one: the one with the latest date in its URL.
                List<String> candidates = collectLinks(pdfDoc, pageNum);
                if (candidates.isEmpty()) {
                    System.err.println("⚠️ link" + i + ": no http links found on page " + pageNum);
                    continue;
                }
                candidates.sort(Comparator.comparing(MOSLPDFExtractor::urlDate).reversed()); // stable: ties keep page order

                boolean saved = false;
                for (String pdfLink : candidates) {
                    byte[] data = download(pdfLink);
                    if (data == null) continue; // not a valid PDF, try next candidate

                    String newFileName = uniqueFileName(destFolder, fileName);
                    try (FileOutputStream fos = new FileOutputStream(newFileName)) {
                        fos.write(data);
                    }
                    if (candidates.size() > 1) {
                        System.out.println("✅ Downloaded: " + fileName + " (found multiple versions, downloaded the latest)");
                    } else {
                        System.out.println("✅ Downloaded: " + fileName);
                    }
                    saved = true;
                    break;
                }
                if (!saved) {
                    System.err.println("⚠️ link" + i + ": none of the links on page " + pageNum + " returned a valid PDF");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error processing link" + i + ": " + e.getMessage());
            }
        }

        pdfDoc.close();
    }

    /** Returns the distinct http(s) URLs of all link annotations on the page, in page order. */
    private static List<String> collectLinks(PdfDocument pdfDoc, int pageNum) {
        Set<String> links = new LinkedHashSet<>();
        for (PdfAnnotation annotation : pdfDoc.getPage(pageNum).getAnnotations()) {
            String url = extractUrl(annotation);
            if (url != null) links.add(url);
        }
        return new ArrayList<>(links);
    }

    private static String extractUrl(PdfAnnotation annotation) {
        PdfObject actionObj = annotation.getPdfObject().get(PdfName.A);
        if (actionObj == null) return null;

        String url = null;
        if (actionObj instanceof PdfDictionary) {
            PdfString uri = ((PdfDictionary) actionObj).getAsString(PdfName.URI);
            if (uri != null) url = uri.toUnicodeString();
        }
        if (url == null) {
            // Fallback to the original string parsing
            String tag = actionObj.toString();
            int start = tag.indexOf("http");
            if (start < 0) return null;
            int end = tag.indexOf(" >>", start);
            url = end > start ? tag.substring(start, end) : tag.substring(start);
        }
        url = url.trim();
        if (!url.startsWith("http")) return null;
        return url.replaceAll(" ", "%20");
    }

    /** yyyyMMdd date found in the URL, or "" if none (so undated links sort last). Uses the latest if several. */
    private static String urlDate(String url) {
        String best = "";
        Matcher m = URL_DATE.matcher(url);
        while (m.find()) {
            if (m.group(1).compareTo(best) > 0) best = m.group(1);
        }
        return best;
    }

    /** Downloads the URL; returns the bytes only if they are a real PDF, otherwise null. */
    private static byte[] download(String pdfLink) {
        try {
            URLConnection conn = new URL(pdfLink).openConnection();
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(60000);
            if (conn instanceof HttpURLConnection) {
                int code = ((HttpURLConnection) conn).getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = conn.getInputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            }
            byte[] data = out.toByteArray();
            return isPdf(data) ? data : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isPdf(byte[] data) {
        // PDF header "%PDF-" must appear within the first 1024 bytes
        int limit = Math.min(data.length - 5, 1024);
        for (int i = 0; i <= limit; i++) {
            if (data[i] == '%' && data[i + 1] == 'P' && data[i + 2] == 'D' && data[i + 3] == 'F' && data[i + 4] == '-') {
                return true;
            }
        }
        return false;
    }

    private static String uniqueFileName(String destFolder, String fileName) {
        String newFileName = destFolder + fileName + ".pdf";
        int fileCount = 1;
        while (new File(newFileName).exists()) {
            newFileName = destFolder + fileName + "_" + fileCount + ".pdf";
            fileCount++;
        }
        return newFileName;
    }
}
