package com.timelineofwealth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Where the setup / property files live. Everything is under one folder, grouped by use:
 *
 *   C:\MyDocuments\03Business\11Property\        (override with -Dtow.property.root=... or env TOW_PROPERTY_ROOT)
 *     website\        watchlists.properties, ChartForSheet&Row.property, TickerChartCombination.property
 *     quarterexcels\  update_quarterly_excels.properties (FilesToBeUpdated window), copy_config.properties
 *     reportpdfs\     daily_report_splitter.properties (AMBIT / Kotak daily PDF split), mosl_morning_india.properties,
 *                     reportconfig.properties and ReportDataExtractConfig2.0.xlsx (AnalystRecoExtractor)
 *     dailydata\      downloadconfig.property (daily EOD downloads)
 *
 * While the move is being tested, a file missing from the new folder is read from its old location instead
 * (and a line is logged), so nothing breaks if the Property folder is not on a laptop yet.
 */
public final class ConfigFiles {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFiles.class);
    private static final String SA = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\";
    private static final String QRS = SA + "QuarterResultsScreenerExcels\\";
    private static final String CRR = SA + "ResearchReports\\CompanyResearchReports\\";

    private ConfigFiles() {}

    public static String root() {
        String r = System.getProperty("tow.property.root");
        if (r == null || r.trim().isEmpty()) r = System.getenv("TOW_PROPERTY_ROOT");
        if (r == null || r.trim().isEmpty()) r = "C:\\MyDocuments\\03Business\\11Property";
        return r.endsWith("\\") || r.endsWith("/") ? r.substring(0, r.length() - 1) : r;
    }

    // ---------------- website
    public static String watchlists()            { return pick("website", "watchlists.properties", QRS + "watchlists.properties"); }
    public static String chartForSheetAndRow()   { return pick("website", "ChartForSheet&Row.property", QRS + "ChartForSheet&Row.property"); }
    public static String tickerChartCombination(){ return pick("website", "TickerChartCombination.property", QRS + "TickerChartCombination.property"); }

    // ---------------- quarterly result Excels
    public static String updateQuarterlyExcels() { return pick("quarterexcels", "update_quarterly_excels.properties", QRS + "Analysis\\config.properties"); }
    public static String copyConfig()            { return pick("quarterexcels", "copy_config.properties", QRS + "copy_config.properties"); }

    // ---------------- broker report PDFs
    public static String dailyReportSplitter()   { return pick("reportpdfs", "daily_report_splitter.properties", CRR + "KotakDaily\\config.properties"); }
    public static String moslMorningIndia()      { return pick("reportpdfs", "mosl_morning_india.properties", CRR + "MOSLMorningIndia\\config.properties"); }
    public static String reportConfig()          { return pick("reportpdfs", "reportconfig.properties", CRR + "reportconfig.properties"); }
    /** where an uploaded reportconfig.properties is saved: the Property folder when it exists */
    public static String reportConfigForWrite()  { return writeTarget("reportpdfs", "reportconfig.properties", CRR + "reportconfig.properties"); }

    // ---------------- daily data
    public static String downloadConfig()        { return pick("dailydata", "downloadconfig.property", "C:\\MyDocuments\\03Business\\03DailyData\\downloadconfig.property"); }

    /**
     * A setup file named inside another config (e.g. ReportDataExtractConfigFilePath in reportconfig.properties):
     * the copy in Property\<group> with the same file name wins, so old paths in uploaded files keep working.
     */
    public static String preferProperty(String group, String configuredPath) {
        if (configuredPath == null || configuredPath.trim().isEmpty()) return configuredPath;
        String name = new File(configuredPath.trim()).getName();
        File f = new File(root() + File.separator + group + File.separator + name);
        return f.isFile() ? f.getPath() : configuredPath.trim();
    }

    private static String pick(String group, String name, String legacy) {
        File f = new File(root() + File.separator + group + File.separator + name);
        if (f.isFile()) return f.getPath();
        logger.info("{} not found in {}; using the old location {}", name, f.getParent(), legacy);
        return legacy;
    }

    private static String writeTarget(String group, String name, String legacy) {
        File dir = new File(root() + File.separator + group);
        return dir.isDirectory() ? new File(dir, name).getPath() : legacy;
    }
}
