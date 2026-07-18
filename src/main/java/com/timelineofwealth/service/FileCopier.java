package com.timelineofwealth.service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * FileCopier — Smart incremental folder sync / mirror utility.
 *
 * HOW TO USE:
 *   1. Set ACTIVE_PROFILE to the profile you want to run (see PROFILES section below)
 *   2. Set DRY_RUN = true to preview what will happen, false to actually sync
 *   3. Run main()
 *
 * FILES (both at C:\MyDocuments\03Business\02workspace\timelineofwealth\):
 *   FileSyncLog.properties  — stores LastTransferTime per profile, one file for ALL profiles
 *   FileSyncLog.log         — appended each run, never auto-cleared (clear manually when needed)
 */
public class FileCopier {

    // =========================================================================
    //  STEP 1 — SET THE ACTIVE PROFILE  (must match a key in PROFILES map below)
    // =========================================================================


//    private static final String ACTIVE_PROFILE = "HPUser_to_Workspace_JupyterProjects";
//    private static final String ACTIVE_PROFILE = "RedmiUser_to_Workspace_JupyterProjects";


//    private static final String ACTIVE_PROFILE = "HP_to_Redmi_Full";
    private static final String ACTIVE_PROFILE = "HP_to_Redmi_DailyData";

//    private static final String ACTIVE_PROFILE = "Redmi_to_HP_Full"; // Set DRY_RUN = TRUE
//    private static final String ACTIVE_PROFILE = "Redmi_to_HP_DailyData";

//    private static final String ACTIVE_PROFILE = "Local_to_Parag_JavaCode";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_SQL";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_JupyterProjects";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_DailyData";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_Excels";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_MacroEconomy";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_SectorReports";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_YahooConsensus";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_CompanyReports";
//    private static final String ACTIVE_PROFILE = "Local_to_Parag_ClientReports";

//    private static final String ACTIVE_PROFILE = "Local_to_OneDrive_Excels";
//    private static final String ACTIVE_PROFILE = "Local_to_OneDrive_Reports";

    // =========================================================================
    //  STEP 2 — SET DRY RUN FLAG
    //    true  = only show what WOULD happen, no changes made
    //    false = actually copy, replace and delete files
    // =========================================================================

    private static final boolean DRY_RUN = false;
//    private static final boolean DRY_RUN = true;

    // =========================================================================
    //  FIXED PATHS — same on every machine, property file and log file location
    // =========================================================================

    private static final String BASE_DIR  = "C:\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\";
    private static final String PROP_FILE = BASE_DIR + "FileSyncLog.properties";
    private static final String LOG_FILE  = BASE_DIR + "FileSyncLog.log";

    // =========================================================================
    //  PROFILES — all your folder combinations, named for easy selection
    //  Format: PROFILES.put("ProfileName", p("srcFolder", "dstFolder"));
    // =========================================================================

    private static final Map<String, String[]> PROFILES = new LinkedHashMap<>();

    static {

        // ── HP Spectre → Redmi ───────────────────────────────────────────────
        PROFILES.put("HP_to_Redmi_Full",
                new String[]{"\\\\HPSpectre\\MyDocuments\\",
                        "\\\\REDMIBOOK15\\MyDocuments\\"});

        PROFILES.put("HP_to_Redmi_DailyData",
                new String[]{"\\\\HPSpectre\\MyDocuments\\03Business\\03DailyData",
                        "\\\\REDMIBOOK15\\MyDocuments\\03Business\\03DailyData"});

        PROFILES.put("HP_to_Redmi_CFA2025",
                new String[]{"\\\\HPSpectre\\CFA2025",
                        "\\\\REDMIBOOK15\\CFA2025"});

        // ── Redmi → HP Spectre ───────────────────────────────────────────────
        PROFILES.put("Redmi_to_HP_Full",
                new String[]{"\\\\REDMIBOOK15\\MyDocuments\\",
                        "\\\\HPSpectre\\MyDocuments\\"});

        PROFILES.put("Redmi_to_HP_DailyData",
                new String[]{"\\\\REDMIBOOK15\\MyDocuments\\03Business\\03DailyData",
                        "\\\\HPSpectre\\MyDocuments\\03Business\\03DailyData"});

        // ── Local (HP) → Parag's Laptop (DESKTOP-UV0PRDE) ───────────────────
        PROFILES.put("Local_to_Parag_JavaCode",
                new String[]{"C:\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\java\\com\\timelineofwealth",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\java\\com\\timelineofwealth"});

        PROFILES.put("Local_to_Parag_SQL",
                new String[]{"C:\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\sql",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\02workspace\\timelineofwealth\\src\\main\\sql"});

        PROFILES.put("Local_to_Parag_JupyterProjects",
                new String[]{"C:\\MyDocuments\\03Business\\02workspace\\JupyterProjects",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\02workspace\\JupyterProjects"});

        PROFILES.put("Local_to_Parag_DailyData",
                new String[]{"C:\\MyDocuments\\03Business\\03DailyData",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\03DailyData"});

        PROFILES.put("Local_to_Parag_Excels",
                new String[]{"C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels"});

        PROFILES.put("Local_to_Parag_MacroEconomy",
                new String[]{"C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy"});

        PROFILES.put("Local_to_Parag_SectorReports",
                new String[]{"C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector"});

        PROFILES.put("Local_to_Parag_YahooConsensus",
                new String[]{"C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\YahooConsensus",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\YahooConsensus"});

        PROFILES.put("Local_to_Parag_CompanyReports",
                new String[]{"C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports"});

        PROFILES.put("Local_to_Parag_ClientReports",
                new String[]{"C:\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports",
                        "\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports"});

        // ── Parag's Laptop (DESKTOP-UV0PRDE) → Local ────────────────────────
        PROFILES.put("Parag_to_Local_DailyData",
                new String[]{"\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\03DailyData",
                        "C:\\MyDocuments\\03Business\\03DailyData"});

        PROFILES.put("Parag_to_Local_Excels_Analysis",
                new String[]{"\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis",
                        "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis"});

        PROFILES.put("Parag_to_Local_Excels_DBInsert",
                new String[]{"\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert",
                        "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert"});

        PROFILES.put("Parag_to_Local_CompanyReports",
                new String[]{"\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports",
                        "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports"});

        PROFILES.put("Parag_to_Local_MacroEconomy",
                new String[]{"\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy",
                        "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\MacroEconomy"});

        PROFILES.put("Parag_to_Local_SectorReports",
                new String[]{"\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector",
                        "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\Sector"});

        PROFILES.put("Parag_to_Local_ClientReports",
                new String[]{"\\\\DESKTOP-UV0PRDE\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports",
                        "C:\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports"});

        // ── Local → OneDrive ─────────────────────────────────────────────────
        PROFILES.put("Local_to_OneDrive_Excels",
                new String[]{"C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels",
                        "C:\\Users\\sudhi\\OneDrive\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels"});

        PROFILES.put("Local_to_OneDrive_Reports",
                new String[]{"C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports",
                        "C:\\Users\\sudhi\\OneDrive\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports"});

        // ── HP Spectre (Local User Folder) → Workspace ─────────────────────────────
        PROFILES.put("HPUser_to_Workspace_JupyterProjects",
                new String[]{"C:\\Users\\Sudhir Kulaye\\JupyterProjects",
                        "C:\\MyDocuments\\03Business\\02workspace\\JupyterProjects"});

        // ── Redmi (Local User Folder) → Workspace ───────────────────────────────────
        PROFILES.put("RedmiUser_to_Workspace_JupyterProjects",
                new String[]{"C:\\Users\\sudhi\\JupyterProjects",
                        "C:\\MyDocuments\\03Business\\02workspace\\JupyterProjects"});
    }

    // =========================================================================
    //  INTERNALS — no need to edit below this line
    // =========================================================================

    private static final DateTimeFormatter FMT       = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Deletion list — built during Phase 2 scan, used for prompt + execute
    private static final List<String[]> toDelete      = new ArrayList<>(); // [path, "FILE"|"FOLDER"]

    // Conflicts — collected during Phase 1 scan+copy, reported afterwards
    private static final List<String[]> needAttention = new ArrayList<>(); // [srcPath, srcDate, dstDate]

    // DRY_RUN only — lists for report (not used in live mode)
    private static final List<String[]> toCopy        = new ArrayList<>(); // [srcPath, srcDate, "NEW"]
    private static final List<String[]> toReplace     = new ArrayList<>(); // [srcPath, srcDate, dstDate]

    // Live mode counters
    private static int copiedCount   = 0;
    private static int replacedCount = 0;

    // Set after profile lookup
    private static String SRC_FOLDER;
    private static String DST_FOLDER;

    // =========================================================================
    //  MAIN
    // =========================================================================

    public static void main(String[] args) throws IOException {

        // ── Resolve profile ──────────────────────────────────────────────────
        String[] profile = PROFILES.get(ACTIVE_PROFILE);
        if (profile == null) {
            System.out.println("ERROR: Profile not found: " + ACTIVE_PROFILE);
            System.out.println("Available profiles:");
            PROFILES.keySet().forEach(k -> System.out.println("  " + k));
            return;
        }
        SRC_FOLDER = profile[0];
        DST_FOLDER = profile[1];

        // ── Print + log header ───────────────────────────────────────────────
        String header = buildHeader();
        print(header);
        appendToLog(header);

        // ── Validate folders ─────────────────────────────────────────────────
        File srcDir = new File(SRC_FOLDER);
        File dstDir = new File(DST_FOLDER);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            String msg = "ERROR: Source folder not found: " + SRC_FOLDER;
            print(msg); appendToLog(msg); return;
        }
        if (!dstDir.exists() || !dstDir.isDirectory()) {
            String msg = "ERROR: Destination folder not found: " + DST_FOLDER;
            print(msg); appendToLog(msg); return;
        }

        // ── Read last transfer time from property file ───────────────────────
        long lastTransferEpoch = readLastTransferTime(ACTIVE_PROFILE);
        String lastSyncMsg = lastTransferEpoch > 0
                ? " Last Sync   : " + epochToStr(lastTransferEpoch)
                : " Last Sync   : NONE — full scan will run";
        print(lastSyncMsg);
        appendToLog(lastSyncMsg);
        print("=================================================\n");

        // ── Phase 1: Scan + Copy / Replace in one pass ──────────────────────
        print("[ Phase 1 ] Copying / replacing files...");
        scanAndCopy(srcDir, dstDir, lastTransferEpoch);

        if (DRY_RUN) {
            print("            Would copy " + toCopy.size() + " new,  replace "
                    + toReplace.size() + ",  " + needAttention.size() + " conflict(s).\n");
        } else {
            print("            Copied " + copiedCount + " new,  replaced "
                    + replacedCount + ",  " + needAttention.size() + " conflict(s).\n");
            writeLastTransferTime(ACTIVE_PROFILE);
            String attentionReport = buildAttentionReport();
            print(attentionReport);
            appendToLog(attentionReport);
        }

        // ── Phase 2: Deletion detection + interactive prompt ─────────────────
        print("[ Phase 2 ] Scanning for deletions...");
        detectDeletions(srcDir, dstDir);
        print("            Found " + toDelete.size() + " item(s) to delete.\n");

        if (DRY_RUN) {
            String report = buildDryRunReport();
            print(report);
            appendToLog(report);
        } else if (toDelete.isEmpty()) {
            print("No extra items in destination. Done.\n");
        } else {
            print("The following items exist in destination but NOT in source:");
            for (String[] r : toDelete)
                print("  [" + r[1] + "] " + r[0]);
            System.out.print("\nDelete these " + toDelete.size() + " item(s)? (yes/no): ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("yes") || answer.equals("y")) {
                executeDeletes();
                print("Deletion complete.\n");
            } else {
                print("Skipping deletion. Copy/replace already done.\n");
            }
        }
    }

    // =========================================================================
    //  PHASE 1 — DELETION DETECTION  (recursive count-narrowing, top-down)
    //  Only loads filenames into memory at folders where count mismatch occurs
    // =========================================================================

    private static void detectDeletions(File srcDir, File dstDir) {

        // ── Files directly in this folder (non-recursive count) ──────────────
        File[] srcFiles = srcDir.listFiles(File::isFile);
        File[] dstFiles = dstDir.listFiles(File::isFile);
        int srcCount = srcFiles == null ? 0 : srcFiles.length;
        int dstCount = dstFiles == null ? 0 : dstFiles.length;

        if (srcCount != dstCount) {
            // Mismatch here — load ONLY this folder's names to find what was deleted
            Set<String> srcNames = new HashSet<>();
            if (srcFiles != null)
                for (File f : srcFiles) srcNames.add(f.getName());
            if (dstFiles != null)
                for (File f : dstFiles)
                    if (!srcNames.contains(f.getName()))
                        toDelete.add(new String[]{ f.getAbsolutePath(), "FILE" });
        }

        // ── Subfolders directly in this folder ───────────────────────────────
        File[] srcDirs = srcDir.listFiles(File::isDirectory);
        File[] dstDirs = dstDir.listFiles(File::isDirectory);
        int srcDirCount = srcDirs == null ? 0 : srcDirs.length;
        int dstDirCount = dstDirs == null ? 0 : dstDirs.length;

        // Build dst dir map for O(1) lookup (used both for diff and recursion)
        Map<String, File> dstDirMap = new HashMap<>();
        if (dstDirs != null)
            for (File d : dstDirs) dstDirMap.put(d.getName(), d);

        if (srcDirCount != dstDirCount) {
            // Mismatch — find which subfolder(s) were deleted from source
            Set<String> srcDirNames = new HashSet<>();
            if (srcDirs != null)
                for (File d : srcDirs) srcDirNames.add(d.getName());
            for (Map.Entry<String, File> e : dstDirMap.entrySet())
                if (!srcDirNames.contains(e.getKey()))
                    collectFolderForDeletion(e.getValue());
        }

        // ── Recurse only into folders present on BOTH sides ──────────────────
        if (srcDirs != null)
            for (File srcSub : srcDirs) {
                File dstSub = dstDirMap.get(srcSub.getName());
                if (dstSub != null)
                    detectDeletions(srcSub, dstSub);
            }
    }

    /** Recursively collect all files + the folder itself for deletion */
    private static void collectFolderForDeletion(File dir) {
        File[] entries = dir.listFiles();
        if (entries != null)
            for (File f : entries) {
                if (f.isDirectory()) collectFolderForDeletion(f);
                else toDelete.add(new String[]{ f.getAbsolutePath(), "FILE" });
            }
        toDelete.add(new String[]{ dir.getAbsolutePath(), "FOLDER" });
    }

    // =========================================================================
    //  PHASE 1 — SCAN + COPY / REPLACE IN ONE PASS
    //  Eliminates the separate scan-then-act double traversal.
    //  In live mode: copies/replaces each file immediately as it is found.
    //  In dry-run mode: collects into toCopy/toReplace lists for reporting.
    // =========================================================================

    private static void scanAndCopy(File srcDir, File dstDir, long lastTransferEpoch) throws IOException {

        // O(1) lookup for dst files — one listFiles() call per folder
        File[] dstFiles = dstDir.listFiles(File::isFile);
        Map<String, File> dstFileMap = new HashMap<>();
        if (dstFiles != null)
            for (File f : dstFiles) dstFileMap.put(f.getName(), f);

        File[] srcFiles = srcDir.listFiles(File::isFile);
        if (srcFiles != null) {
            for (File srcFile : srcFiles) {
                long   srcMs   = srcFile.lastModified();
                String dstPath = srcFile.getAbsolutePath().replace(SRC_FOLDER, DST_FOLDER);
                File   dstFile = dstFileMap.get(srcFile.getName());

                if (dstFile == null || !dstFile.exists()) {
                    // New file — copy immediately
                    if (DRY_RUN) toCopy.add(new String[]{ srcFile.getAbsolutePath(), msToDate(srcMs), "NEW" });
                    else        { performCopy(srcFile.getAbsolutePath(), dstPath, "Copying  "); copiedCount++; }

                } else {
                    long dstMs = dstFile.lastModified();

                    if (dstMs > srcMs) {
                        // Destination is NEWER — flag, never auto-overwrite
                        needAttention.add(new String[]{ srcFile.getAbsolutePath(), msToDate(srcMs), msToDate(dstMs) });

                    } else if (srcMs > dstMs) {
                        if (lastTransferEpoch > 0 && srcMs < lastTransferEpoch) {
                            // Already synced in a previous run — skip silently
                        } else {
                            // Source is newer — replace immediately
                            if (DRY_RUN) toReplace.add(new String[]{ srcFile.getAbsolutePath(), msToDate(srcMs), msToDate(dstMs) });
                            else        { performCopy(srcFile.getAbsolutePath(), dstPath, "Replacing"); replacedCount++; }
                        }
                    }
                    // Same timestamp — already in sync, skip silently
                }
            }
        }

        // ── Recurse into subfolders ──────────────────────────────────────────
        File[] srcSubDirs = srcDir.listFiles(File::isDirectory);
        if (srcSubDirs != null) {
            for (File srcSub : srcSubDirs) {
                File dstSub = new File(dstDir.getAbsolutePath() + File.separator + srcSub.getName());
                if (!dstSub.exists()) {
                    if (!DRY_RUN) dstSub.mkdirs();
                    copyEntireFolder(srcSub, dstSub);   // brand new folder on source side
                } else {
                    scanAndCopy(srcSub, dstSub, lastTransferEpoch);
                }
            }
        }
    }

    /** Copies an entire new src folder to dst (no timestamp checks needed — everything is new). */
    private static void copyEntireFolder(File srcDir, File dstDir) throws IOException {
        File[] entries = srcDir.listFiles();
        if (entries != null) {
            for (File f : entries) {
                if (f.isDirectory()) {
                    File dstSub = new File(dstDir.getAbsolutePath() + File.separator + f.getName());
                    if (!DRY_RUN) dstSub.mkdirs();
                    copyEntireFolder(f, dstSub);
                } else {
                    if (DRY_RUN) toCopy.add(new String[]{ f.getAbsolutePath(), msToDate(f.lastModified()), "NEW" });
                    else        { performCopy(f.getAbsolutePath(), f.getAbsolutePath().replace(SRC_FOLDER, DST_FOLDER), "Copying  "); copiedCount++; }
                }
            }
        }
    }

    // =========================================================================
    //  PHASE 3a — DRY RUN REPORT
    // =========================================================================

    private static String buildDryRunReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== DRY RUN REPORT ==========\n\n");

        sb.append("Files to be COPIED (new — not in destination):  ").append(toCopy.size()).append("\n");
        for (String[] r : toCopy)
            sb.append("  + ").append(rel(r[0])).append("  [src: ").append(r[1]).append("]\n");

        sb.append("\nFiles to be REPLACED (source is newer than destination):  ").append(toReplace.size()).append("\n");
        for (String[] r : toReplace)
            sb.append("  ~ ").append(rel(r[0]))
                    .append("  [src: ").append(r[1]).append("  dst: ").append(r[2]).append("]\n");

        sb.append("\nFiles / Folders to be DELETED (removed from source):  ").append(toDelete.size()).append("\n");
        for (String[] r : toDelete)
            sb.append("  - [").append(r[1]).append("] ").append(r[0]).append("\n");

        sb.append("\nFiles needing ATTENTION (destination is NEWER — will NOT be auto-replaced):  ")
                .append(needAttention.size()).append("\n");
        for (String[] r : needAttention)
            sb.append("  ! ").append(rel(r[0]))
                    .append("  [src: ").append(r[1]).append("  dst: ").append(r[2]).append("]\n");

        sb.append("\n=====================================\n");
        sb.append(String.format("Summary:  %d to copy,  %d to replace,  %d to delete,  %d need attention%n",
                toCopy.size(), toReplace.size(), toDelete.size(), needAttention.size()));
        sb.append("No changes made — dry run mode.\n");
        return sb.toString();
    }

    // =========================================================================
    //  DELETE EXECUTION
    // =========================================================================

    private static void executeDeletes() {
        // Reverse order ensures files are deleted before their parent folder
        List<String[]> reversedDeletes = new ArrayList<>(toDelete);
        Collections.reverse(reversedDeletes);
        for (String[] r : reversedDeletes) {
            File f = new File(r[0]);
            if (f.exists()) {
                boolean ok = f.delete();
                String msg = (ok ? "  Deleted " : "  FAILED to delete ") + r[1] + ": " + r[0];
                print(msg); appendToLog(msg);
            }
        }
    }

    private static void performCopy(String srcPath, String dstPath, String label) {
        try {
            Path src = Paths.get(srcPath);
            Path dst = Paths.get(dstPath);
            Files.createDirectories(dst.getParent());
            Files.copy(src, dst,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES,
                    LinkOption.NOFOLLOW_LINKS);
            String msg = "  " + label + ": " + rel(srcPath);
            print(msg); appendToLog(msg);
        } catch (Exception e) {
            String msg = "  ERROR " + label.trim() + ": " + srcPath + " — " + e.getMessage();
            print(msg); appendToLog(msg);
        }
    }

    // =========================================================================
    //  ATTENTION REPORT — shown at end of live run
    // =========================================================================

    private static String buildAttentionReport() {
        if (needAttention.isEmpty())
            return "\nAll done. No conflicts found.\n";
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== ATTENTION NEEDED ==========\n");
        sb.append("These files were NOT replaced — destination is NEWER than source.\n");
        sb.append("Review manually:\n\n");
        for (String[] r : needAttention)
            sb.append("  ! ").append(rel(r[0]))
                    .append("\n      src: ").append(r[1]).append("   dst: ").append(r[2]).append("\n");
        sb.append("======================================\n");
        return sb.toString();
    }

    // =========================================================================
    //  PROPERTY FILE — single file, all profiles, keyed by profile name
    //
    //  Format example:
    //    [HP_to_Redmi_DailyData]
    //    SourceFolder=\\HPSpectre\MyDocuments\03Business\03DailyData
    //    DestinationFolder=\\REDMIBOOK15\MyDocuments\03Business\03DailyData
    //    LastTransferTime=2025-04-23 22:30:00
    //
    //    [Local_to_Parag_JavaCode]
    //    ...
    // =========================================================================

    private static long readLastTransferTime(String profileName) {
        File propFile = new File(PROP_FILE);
        if (!propFile.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(propFile))) {
            String line;
            boolean inSection = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("[" + profileName + "]"))       { inSection = true; }
                else if (line.startsWith("[") && line.endsWith("]")) { inSection = false; }
                else if (inSection && line.startsWith("LastTransferTime=")) {
                    String val = line.substring("LastTransferTime=".length()).trim();
                    if (!val.isEmpty()) {
                        LocalDateTime ldt = LocalDateTime.parse(val, FMT);
                        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    }
                }
            }
        } catch (Exception e) {
            print("Warning: Could not read property file — " + e.getMessage());
        }
        return 0;
    }

    private static void writeLastTransferTime(String profileName) {
        try {
            // Read all existing lines
            File propFile = new File(PROP_FILE);
            List<String> lines = new ArrayList<>();
            if (propFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(propFile))) {
                    String line;
                    while ((line = br.readLine()) != null) lines.add(line);
                }
            }

            String now = LocalDateTime.now().format(FMT);
            boolean sectionFound = false;
            boolean inSection = false;

            // Update existing section if found
            for (int i = 0; i < lines.size(); i++) {
                String l = lines.get(i).trim();
                if (l.equals("[" + profileName + "]"))          { inSection = true; sectionFound = true; }
                else if (l.startsWith("[") && l.endsWith("]"))  { inSection = false; }
                else if (inSection && l.startsWith("LastTransferTime=")) {
                    lines.set(i, "LastTransferTime=" + now);
                    inSection = false;
                }
            }

            // Append new section if not found
            if (!sectionFound) {
                if (!lines.isEmpty()) lines.add("");
                lines.add("[" + profileName + "]");
                lines.add("SourceFolder=" + SRC_FOLDER);
                lines.add("DestinationFolder=" + DST_FOLDER);
                lines.add("LastTransferTime=" + now);
            }

            // Write all lines back
            Files.createDirectories(Paths.get(BASE_DIR));
            try (PrintWriter pw = new PrintWriter(new FileWriter(propFile))) {
                for (String l : lines) pw.println(l);
            }
            String msg = "\nProperty file updated — [" + profileName + "] LastTransferTime=" + now;
            print(msg); appendToLog(msg);

        } catch (Exception e) {
            print("Warning: Could not write property file — " + e.getMessage());
        }
    }

    // =========================================================================
    //  LOG FILE — appended every run, never auto-cleared
    // =========================================================================

    private static void appendToLog(String text) {
        try {
            Files.createDirectories(Paths.get(BASE_DIR));
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                fw.write(text);
                fw.write(System.lineSeparator());
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not write log — " + e.getMessage());
        }
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private static String buildHeader() {
        return "\n=================================================" +
                "\n FileCopier  |  Profile : " + ACTIVE_PROFILE +
                "\n Mode        : " + (DRY_RUN ? "DRY RUN (no changes will be made)" : "LIVE SYNC") +
                "\n Run Time    : " + LocalDateTime.now().format(FMT) +
                "\n Source      : " + SRC_FOLDER +
                "\n Destination : " + DST_FOLDER;
    }

    /** Relative path for cleaner output — strips the source root prefix */
    private static String rel(String fullPath) {
        if (SRC_FOLDER != null && fullPath.startsWith(SRC_FOLDER))
            return fullPath.substring(SRC_FOLDER.length());
        return fullPath;
    }

    private static String msToDate(long ms) {
        if (ms <= 0) return "---";
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ms), ZoneId.systemDefault())
                .format(FMT_SHORT);
    }

    private static String epochToStr(long ms) {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ms), ZoneId.systemDefault())
                .format(FMT);
    }

    /** Shorthand for creating src/dst pair in static initialiser */
    private static String[] p(String src, String dst) {
        return new String[]{ src, dst };
    }

    /** Single print method — easy to extend to a GUI later if needed */
    private static void print(String msg) {
        System.out.println(msg);
    }
}