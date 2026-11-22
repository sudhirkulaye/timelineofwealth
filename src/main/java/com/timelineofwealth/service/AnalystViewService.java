package com.timelineofwealth.service;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Broker views per stock from the research workbook AnalystVeiwInsight.xlsx:
 *   ConsolidatedView (one row per stock, one consolidated view per broker) and the per-broker email sheets
 *   (Ticker, Date, Name, Category / Theme, Critical Factors, ...).
 *
 * Broker names are replaced with neutral labels ("Broker A", "Broker B") before anything leaves the server,
 * because the research is shared privately. The workbook is read with the streaming API (it is several MB)
 * and cached until the file changes.
 */
@Service
public class AnalystViewService {
    private static final Logger logger = LoggerFactory.getLogger(AnalystViewService.class);
    static final String FILE = "C:/MyDocuments/03Business/05ResearchAndAnalysis/StockInvestments/CoworkProject/ResearchIntelligence/AnalystVeiwInsight.xlsx";

    /** email sheet -> neutral label; ConsolidatedView columns are matched by the same key word. */
    private static final String[][] BROKERS = {{"Ambit Emails", "AMBIT", "Broker A"}, {"MOSL Emails", "MOSL", "Broker B"}};
    private static final Pattern[] SCRUB_B = {
            Pattern.compile("\\bMotilal(\\s+Oswal)?(\\s+Financial\\s+Services)?(\\s+(Limited|Ltd\\.?))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bOswal\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bMOFSL\\b|\\bMOSL\\b|\\bMOFS\\b", Pattern.CASE_INSENSITIVE)};
    private static final Pattern[] SCRUB_A = {
            Pattern.compile("\\bAmbit(\\s+Capital)?('s)?\\b", Pattern.CASE_INSENSITIVE)};
    private static final Pattern DUP = Pattern.compile("(Broker [AB])\\s*\\(\\s*\\1\\s*\\)");

    private volatile long loadedStamp = -1;
    private volatile Map<String, Map<String, Object>> byTicker = Collections.emptyMap();

    public Map<String, Object> getViews(String ticker) {
        ensureLoaded();
        Map<String, Object> v = byTicker.get(ticker.toUpperCase(Locale.ROOT));
        if (v != null) return v;
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("ticker", scrub(ticker));
        empty.put("consolidated", new ArrayList<>());
        empty.put("notes", new ArrayList<>());
        return empty;
    }

    static String scrub(String s) {
        if (s == null) return null;
        String out = s;
        for (Pattern p : SCRUB_B) out = p.matcher(out).replaceAll("Broker B");
        for (Pattern p : SCRUB_A) out = p.matcher(out).replaceAll("Broker A");
        return DUP.matcher(out).replaceAll("$1").trim();
    }

    private synchronized void ensureLoaded() {
        File f = new File(FILE);
        if (!f.exists()) { byTicker = Collections.emptyMap(); return; }
        long stamp = f.lastModified();
        if (stamp == loadedStamp) return;
        long t0 = System.currentTimeMillis();
        try {
            Map<String, List<List<String>>> sheets = readSheets(f, new HashSet<>(Arrays.asList("ConsolidatedView", BROKERS[0][0], BROKERS[1][0])));
            byTicker = build(sheets);
            loadedStamp = stamp;
            logger.debug("Loaded broker views for {} stocks in {} ms", byTicker.size(), System.currentTimeMillis() - t0);
        } catch (Exception e) {
            logger.error("Could not read {}", FILE, e);
        }
    }

    private Map<String, Map<String, Object>> build(Map<String, List<List<String>>> sheets) {
        Map<String, Map<String, Object>> out = new HashMap<>();

        // ConsolidatedView: Industry, Sub-Industry, Stock, Ticker, ..., "<BROKER> Consolidated View"
        List<List<String>> cv = sheets.getOrDefault("ConsolidatedView", Collections.emptyList());
        if (!cv.isEmpty()) {
            List<String> hdr = cv.get(0);
            int tickerCol = hdr.indexOf("Ticker");
            for (List<String> row : cv.subList(1, cv.size())) {
                String t = at(row, tickerCol);
                if (t == null || t.isEmpty()) continue;
                Map<String, Object> m = entry(out, t);
                m.put("industry", at(row, 0));
                m.put("subIndustry", at(row, 1));
                for (String[] b : BROKERS) {
                    for (int c = 0; c < hdr.size(); c++) {
                        String h = hdr.get(c);
                        if (h != null && h.toUpperCase(Locale.ROOT).contains(b[1]) && h.toLowerCase(Locale.ROOT).contains("consolidated")) {
                            String view = at(row, c);
                            if (view != null && !view.isEmpty()) {
                                Map<String, String> x = new LinkedHashMap<>();
                                x.put("broker", b[2]);
                                x.put("view", scrub(view));
                                list(m, "consolidated").add(x);
                            }
                        }
                    }
                }
            }
        }

        // broker email sheets: Ticker, Date, Name, Category / Theme, Critical Factors
        for (String[] b : BROKERS) {
            List<List<String>> rows = sheets.getOrDefault(b[0], Collections.emptyList());
            if (rows.isEmpty()) continue;
            List<String> hdr = rows.get(0);
            int cT = hdr.indexOf("Ticker"), cD = hdr.indexOf("Date"), cTheme = hdr.indexOf("Category / Theme"), cF = hdr.indexOf("Critical Factors");
            for (List<String> row : rows.subList(1, rows.size())) {
                String t = at(row, cT);
                if (t == null || t.isEmpty()) continue;
                String theme = at(row, cTheme), factors = at(row, cF);
                if ((theme == null || theme.isEmpty()) && (factors == null || factors.isEmpty())) continue;
                Map<String, String> note = new LinkedHashMap<>();
                note.put("broker", b[2]);
                note.put("date", at(row, cD));
                note.put("theme", scrub(theme));
                note.put("factors", scrub(factors));
                list(entry(out, t), "notes").add(note);
            }
        }
        // newest first
        for (Map<String, Object> m : out.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> notes = (List<Map<String, String>>) (List<?>) list(m, "notes");
            notes.sort((a, c) -> String.valueOf(c.get("date")).compareTo(String.valueOf(a.get("date"))));
        }
        return out;
    }

    private static Map<String, Object> entry(Map<String, Map<String, Object>> out, String ticker) {
        return out.computeIfAbsent(ticker.trim().toUpperCase(Locale.ROOT), k -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ticker", scrub(k));   // some rows are topics, not tickers (e.g. a broker's conference)
            m.put("consolidated", new ArrayList<>());
            m.put("notes", new ArrayList<>());
            return m;
        });
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Map<String, Object> m, String key) { return (List<Object>) m.get(key); }

    private static String at(List<String> row, int i) {
        if (i < 0 || i >= row.size()) return null;
        String s = row.get(i);
        return s == null ? null : s.trim();
    }

    // ---------- streaming reader ----------
    private static Map<String, List<List<String>>> readSheets(File f, Set<String> wanted) throws Exception {
        Map<String, List<List<String>>> result = new HashMap<>();
        try (OPCPackage pkg = OPCPackage.open(f, PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(pkg);
            ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);
            StylesTable styles = reader.getStylesTable();
            DataFormatter fmt = new IsoDateFormatter();
            XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) reader.getSheetsData();
            while (it.hasNext()) {
                try (InputStream in = it.next()) {
                    String name = it.getSheetName();
                    if (!wanted.contains(name)) continue;
                    List<List<String>> rows = new ArrayList<>();
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    spf.setNamespaceAware(true);   // POI's sheet handler matches on namespace-qualified element names
                    XMLReader xml = spf.newSAXParser().getXMLReader();
                    xml.setContentHandler(new XSSFSheetXMLHandler(styles, null, sst, new Collector(rows), fmt, false));
                    xml.parse(new InputSource(in));
                    result.put(name, rows);
                }
            }
        }
        return result;
    }

    /** Formats date cells as yyyy-MM-dd so notes sort and parse reliably. */
    private static class IsoDateFormatter extends DataFormatter {
        @Override
        public String formatRawCellContents(double value, int formatIndex, String formatString) {
            if (DateUtil.isADateFormat(formatIndex, formatString) && DateUtil.isValidExcelDate(value)) {
                return new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.getJavaDate(value));
            }
            return super.formatRawCellContents(value, formatIndex, formatString);
        }
    }

    private static class Collector implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final List<List<String>> rows;
        private List<String> cur;

        Collector(List<List<String>> rows) { this.rows = rows; }

        @Override public void startRow(int rowNum) {
            while (rows.size() < rowNum) rows.add(new ArrayList<>());   // keep row positions
            cur = new ArrayList<>();
        }
        @Override public void endRow(int rowNum) { rows.add(cur); }
        @Override public void cell(String ref, String value, XSSFComment comment) {
            int col = new CellReference(ref).getCol();
            while (cur.size() < col) cur.add(null);
            cur.add(value);
        }
        @Override public void headerFooter(String text, boolean isHeader, String tagName) { /* not used */ }
    }
}
