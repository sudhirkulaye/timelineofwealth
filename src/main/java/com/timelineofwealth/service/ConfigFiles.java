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
 * The old locations (QuarterResultsScreenerExcels, CompanyResearchReports, 03DailyData) are no longer used: a file
 * missing from this folder is logged as an error and the program fails on the path shown here.
 */
public final class ConfigFiles {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFiles.class);

    private ConfigFiles() {}

    public static String root() {
        String r = System.getProperty("tow.property.root");
        if (r == null || r.trim().isEmpty()) r = System.getenv("TOW_PROPERTY_ROOT");
        if (r == null || r.trim().isEmpty()) r = "C:\\MyDocuments\\03Business\\11Property";
        return r.endsWith("\\") || r.endsWith("/") ? r.substring(0, r.length() - 1) : r;
    }

    // ---------------- website
    public static String watchlists()            { return file("website", "watchlists.properties"); }
    public static String chartForSheetAndRow()   { return file("website", "ChartForSheet&Row.property"); }
    public static String tickerChartCombination(){ return file("website", "TickerChartCombination.property"); }

    // ---------------- quarterly result Excels
    public static String updateQuarterlyExcels() { return file("quarterexcels", "update_quarterly_excels.properties"); }
    public static String copyConfig()            { return file("quarterexcels", "copy_config.properties"); }

    // ---------------- broker report PDFs
    public static String dailyReportSplitter()   { return file("reportpdfs", "daily_report_splitter.properties"); }
    public static String moslMorningIndia()      { return file("reportpdfs", "mosl_morning_india.properties"); }
    public static String reportConfig()          { return file("reportpdfs", "reportconfig.properties"); }
    /** where an uploaded reportconfig.properties is saved */
    public static String reportConfigForWrite()  { return root() + File.separator + "reportpdfs" + File.separator + "reportconfig.properties"; }

    // ---------------- daily data
    public static String downloadConfig()        { return file("dailydata", "downloadconfig.property"); }

    /**
     * A setup file named inside another config (e.g. ReportDataExtractConfigFilePath in reportconfig.properties):
     * the copy in 11Property\<group> with the same file name is used, so an old path written in an uploaded file
     * still works. If that copy is missing, the configured path is used as written.
     */
    public static String preferProperty(String group, String configuredPath) {
        if (configuredPath == null || configuredPath.trim().isEmpty()) return configuredPath;
        String name = new File(configuredPath.trim()).getName();
        File f = new File(root() + File.separator + group + File.separator + name);
        return f.isFile() ? f.getPath() : configuredPath.trim();
    }

    private static String file(String group, String name) {
        File f = new File(root() + File.separator + group + File.separator + name);
        if (!f.isFile()) logger.error("Setup file missing: {} (all setup files live under {})", f.getPath(), root());
        return f.getPath();
    }
}
