package com.timelineofwealth.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Moves broker PDF reports from \QUARTER\temp into the correct industry subfolder under the same QUARTER.
 * Now processes the latest and the second-latest quarters.
 *
 * Filename format:
 *   FY26Q1_SAFARI_AMBIT.pdf
 *   FY26Q1_SAFARI_AMBIT_1.pdf
 *   FY26Q1_VIPIND_AXIS.pdf
 *
 * Logic per quarter being processed:
 *  1) Parse (QUARTER, TICKER, BROKER) from filename.
 *  2) Find industry folder in the same quarter; if not found, look back up to 4 previous quarters
 *     to identify the correct industry folder name, and create that folder in the current quarter.
 *  3) Determine the next suffix for QUARTER_TICKER_BROKER*.pdf and move the file.
 *  4) If ticker not found in last (up to) 5 quarters including current, leave in temp.
 */
public class QuarterlyReportOrganizer {

    static final String ROOT = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports";
    static final String TEMP_DIR_NAME = "temp";

    // Quarter dir pattern: FY26Q1
    static final Pattern QUARTER_DIR = Pattern.compile("^FY(\\d{2})Q([1-4])$");
    // Report name pattern (case-insensitive .pdf)
    static final Pattern REPORT_NAME = Pattern.compile("^(FY\\d{2}Q[1-4])_([A-Z0-9]+)_([A-Z0-9]+)(?:_(\\d+))?\\.pdf$", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) throws IOException {
        File root = new File(ROOT);
        if (!root.isDirectory()) {
            System.out.println("❌ ROOT does not exist or is not a directory: " + ROOT);
            return;
        }

        // Build quarters sorted by (FY asc, Q asc)
        List<File> quartersAsc = getQuarterDirsSorted(root);
        if (quartersAsc.isEmpty()) {
            System.out.println("❌ No valid quarter folders found under: " + ROOT);
            return;
        }

        // Identify latest and second latest (if present)
        File latest = quartersAsc.get(quartersAsc.size() - 1);
        File secondLatest = quartersAsc.size() >= 2 ? quartersAsc.get(quartersAsc.size() - 2) : null;

        System.out.println("📦 Latest quarter: " + latest.getName());
        if (secondLatest != null) System.out.println("📦 Second-latest quarter: " + secondLatest.getName());

        // Process latest quarter temp
        processQuarterTemp(latest, quartersAsc);

        // Process second-latest quarter temp (if exists)
        if (secondLatest != null) {
            processQuarterTemp(secondLatest, quartersAsc);
        }

        System.out.println("\n🏁 Done.");
    }

    /** Process the temp folder for a given quarter, moving files into that quarter's industry subfolders. */
    private static void processQuarterTemp(File currentQuarterDir, List<File> quartersAsc) {
        String quarterName = currentQuarterDir.getName();
        File tempFolder = new File(currentQuarterDir, TEMP_DIR_NAME);

        System.out.println("\n==== Processing quarter: " + quarterName + " ====");

        if (!tempFolder.isDirectory()) {
            System.out.println("ℹ️ No temp folder for " + quarterName + " → " + tempFolder.getAbsolutePath());
            return;
        }

        File[] tempReports = tempFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".pdf"));
        if (tempReports == null || tempReports.length == 0) {
            System.out.println("ℹ️ No reports found in temp for " + quarterName + ".");
            return;
        }

        // Build a search list: current quarter + up to 4 previous quarters
        List<File> searchQuarters = buildSearchWindow(currentQuarterDir, quartersAsc, 5);

        for (File report : Objects.requireNonNull(tempReports)) {
            String fileName = report.getName();
            Matcher m = REPORT_NAME.matcher(fileName);
            if (!m.matches()) {
                System.out.println("❌ Skipping unrecognized filename format: " + fileName);
                continue;
            }

            String fileQuarter = m.group(1); // from filename (info only)
            String ticker = m.group(2).toUpperCase(Locale.ROOT);
            String broker = m.group(3).toUpperCase(Locale.ROOT);

            System.out.println("\n— Processing: " + fileName + " | Parsed → quarter=" + fileQuarter + ", ticker=" + ticker + ", broker=" + broker);

            // 1) Try to find an industry folder in the CURRENT quarter
            File destIndustryFolder = findIndustryFolderWithTicker(currentQuarterDir, ticker);

            // 2) If not found, look back in previous quarters (up to the window size)
            if (destIndustryFolder == null) {
                for (int i = 1; i < searchQuarters.size(); i++) { // start from 1 to skip current
                    File prevQ = searchQuarters.get(i);
                    File matched = findIndustryFolderWithTicker(prevQ, ticker);
                    if (matched != null) {
                        System.out.println("🔎 Found ticker in " + prevQ.getName()
                                + " → industry folder: " + matched.getName());
                        // Use that folder name under CURRENT quarter
                        destIndustryFolder = new File(currentQuarterDir, matched.getName());
                        break;
                    }
                }
            }

            // 3) If still not found, leave in temp
            if (destIndustryFolder == null) {
                System.out.println("🕒 Ticker " + ticker + " not found in the last " + searchQuarters.size()
                        + " quarters (including " + quarterName + "). Leaving in temp.");
                continue;
            }

            // Ensure destination folder exists
            if (!destIndustryFolder.exists()) {
                if (destIndustryFolder.mkdirs()) {
                    System.out.println("📁 Created destination industry folder: " + destIndustryFolder.getAbsolutePath());
                } else {
                    System.out.println("❌ Failed to create destination folder: " + destIndustryFolder.getAbsolutePath());
                    continue;
                }
            }

            // 4) Build base using the CURRENT quarter (the quarter being processed)
            String base = quarterName + "_" + ticker + "_" + broker;
            File destFile = computeNextAvailableName(destIndustryFolder, base);

            // 5) Move
            try {
                Files.move(report.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ Moved: " + fileName + " → " + destFile.getAbsolutePath());
            } catch (IOException moveEx) {
                System.out.println("❌ Move failed for " + fileName + ": " + moveEx.getMessage());
            }
        }
    }

    /** Build a window of quarters starting from 'current' going backwards, up to maxCount. */
    private static List<File> buildSearchWindow(File current, List<File> quartersAsc, int maxCount) {
        List<File> window = new ArrayList<>();
        int idx = quartersAsc.indexOf(current);
        if (idx < 0) return window;

        // Include current + up to (maxCount-1) previous
        for (int i = idx; i >= 0 && window.size() < maxCount; i--) {
            window.add(quartersAsc.get(i));
        }
        return window;
    }

    /** Return quarter directories sorted by (FY asc, Q asc). */
    private static List<File> getQuarterDirsSorted(File root) {
        File[] dirs = root.listFiles(File::isDirectory);
        if (dirs == null) return Collections.emptyList();

        List<File> quarters = new ArrayList<>();
        for (File d : dirs) {
            if (QUARTER_DIR.matcher(d.getName()).matches()) {
                quarters.add(d);
            }
        }
        quarters.sort(Comparator.comparingInt((File f) -> parseFy(f.getName()))
                .thenComparingInt(f -> parseQ(f.getName())));
        return quarters;
    }

    private static int parseFy(String quarterName) {
        Matcher m = QUARTER_DIR.matcher(quarterName);
        return m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static int parseQ(String quarterName) {
        Matcher m = QUARTER_DIR.matcher(quarterName);
        return m.matches() ? Integer.parseInt(m.group(2)) : -1;
    }

    /** Finds an industry folder in the given quarter dir that already contains this ticker in any report. Skips "temp". */
    private static File findIndustryFolderWithTicker(File quarterDir, String ticker) {
        File[] industryDirs = quarterDir.listFiles(file ->
                file.isDirectory() && !file.getName().equalsIgnoreCase(TEMP_DIR_NAME));
        if (industryDirs == null) return null;

        String needle = "_" + ticker + "_";
        for (File industry : industryDirs) {
            File[] matches = industry.listFiles((dir, name) ->
                    name.toLowerCase(Locale.ROOT).endsWith(".pdf") &&
                            name.toUpperCase(Locale.ROOT).contains(needle));
            if (matches != null && matches.length > 0) {
                return industry;
            }
        }
        return null;
    }

    /**
     * Computes the next available filename for base = QUARTER_TICKER_BROKER:
     *  - If no existing with that base, return base.pdf
     *  - If base.pdf or base_#.pdf exists, return base_{max+1}.pdf
     */
    private static File computeNextAvailableName(File destDir, String base) {
        File[] existing = destDir.listFiles((dir, name) -> {
            String upper = name.toUpperCase(Locale.ROOT);
            String b = base.toUpperCase(Locale.ROOT);
            return upper.startsWith(b) && upper.endsWith(".PDF")
                    && (upper.equals(b + ".PDF") || upper.matches(Pattern.quote(b) + "_\\d+\\.PDF"));
        });

        if (existing == null || existing.length == 0) {
            return new File(destDir, base + ".pdf");
        }

        int maxSuffix = 0;
        boolean baseExists = false;

        for (File f : existing) {
            String n = f.getName();
            if (n.equalsIgnoreCase(base + ".pdf")) {
                baseExists = true;
                continue;
            }
            int us = n.lastIndexOf('_');
            int dot = n.lastIndexOf('.');
            if (us > 0 && dot > us) {
                try {
                    int sfx = Integer.parseInt(n.substring(us + 1, dot));
                    if (sfx > maxSuffix) maxSuffix = sfx;
                } catch (NumberFormatException ignored) {}
            }
        }

        int next = (baseExists || maxSuffix > 0) ? maxSuffix + 1 : 0;
        String finalName = (next == 0) ? (base + ".pdf") : (base + "_" + next + ".pdf");
        return new File(destDir, finalName);
    }
}
