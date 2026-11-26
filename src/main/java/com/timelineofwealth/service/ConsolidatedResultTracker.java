package com.timelineofwealth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Fills QuarterResultsScreenerExcels\DBInsert\ResultTracker.xlsx, sheet FY{yy}Q{n}: one row per ticker with the
 * result-day price, valuation range, broker recos and the last five quarters' trends, to see how the quarter's results
 * trend and how the price moves after the result.
 *
 * Every value comes from the NEW sheets of the ticker's result workbook ({TICKER}_FY{yy}Q{n}.xlsx), found by label:
 *   HistoricalData > Quarterly Income Statement   Share Price (result-day price, frozen by updateMCapAndPrice),
 *                                                 Revenue, Other Income, (Ex-Other Inc.) Op. Profit Margin%,
 *                                                 (Ex-Other Inc.) EBIT Margin%, Tax Rate%
 *   ModelBFSI > Quarterly Income Expense History  Net Interest Income, Net Interest Margin, Standalone Profit,
 *                                                 subsidiary rows, Consolidated Profit (financials)
 *   ModelBFSI > Quarterly Banking History         Loans / AUM, Loan/AUM Growth, GNPA, NNPA, GNPA%, NNPA%, Slippages
 *   Data Sheet                                    Current Price (result price while Share Price is still a formula)
 *   DBInsert\AnalystViewTracker.xlsx > AnalystsRecoNew   reco + target per broker (Firm) for the last four quarters
 * Derived: NOPLAT = Revenue x (Ex-Other Inc.) EBIT Margin x (1 - Tax Rate); after-tax other income = Other Income x
 * (1 - Tax Rate); subsidiaries = consolidated - standalone profit; TTM = sum of four quarters; growth = YoY.
 * MinPrice / MaxPrice are not filled (no valuation range in the new sheets yet).
 * The old sheets (QuarterP&L, AnalystReco, ValuationHistory) are no longer read.
 */
public class ConsolidatedResultTracker {
    static Boolean IS_OVERRIDE_PRICE_DATA = true;
    static Boolean IS_OVERRIDE_RECO_DATA = true;
    static Boolean IS_OVERRIDE_OTHER_DATA = true;
    // Tracker workbook cell locations
    static Integer TICKER_LOCATION = 1;
    static Integer FOLDER_LOCATION = 2;
    static Integer MOSL_REPORT_STATUS_LOCATION = 7;
    static Integer AMBIT_REPORT_STATUS_LOCATION = 8;
    static Integer AXIS_REPORT_STATUS_LOCATION = 9;
    static Integer ICICIDIRECT_REPORT_STATUS_LOCATION = 10;
    static Integer PL_REPORT_STATUS_LOCATION = 11;
    static Integer KOTAK_REPORT_STATUS_LOCATION = 12;
    static Integer RESULT_PRICE_LOCATION = 15;
    static Integer MIN_VAKYATUIB_PRICE_LOCATION = 19;
    static Integer MAX_VAKYATUIB_PRICE_LOCATION = 21;
    static Integer TTM_REV_TREND_LOCATION = 23;
    static Integer TTM_NOPLAT_TREND_LOCATION = 24;
    static Integer TTM_SUBSIDIARY_TREND_LOCATION = 25;
    static Integer REV_G_Q0_RATIO_LOCATION = 26;
    static Integer NOPLAT_G_Q0_RATIO_LOCATION = 31;
    static Integer OPM_Q0_RATIO_LOCATION = 36;
    static Integer MOSL_RECO_LOCATION = 41;
    static Integer AMBIT_RECO_LOCATION = 42;
    static Integer AXIS_RECO_LOCATION = 43;
    static Integer ICICIDIRECT_RECO_LOCATION = 44;
    static Integer PL_RECO_LOCATION = 45;
    static Integer KOTAK_RECO_LOCATION = 46;
    static Integer AUM0_LOCATION = 47;
    static Integer AUM_G_Q0_RATIO_LOCATION = 48;
    static Integer GNPA_TREND_LOCATION = 53;
    static Integer NNPA_TREND_LOCATION = 54;
    static Integer GNPA_RATIO_TREND_LOCATION = 55;
    static Integer NNPA_RATIO_TREND_LOCATION = 56;
    static Integer SLIPPAGES_TREND_LOCATION = 57;

    /** broker report-status column, reco column and the Firm code(s) in AnalystViewTracker */
    private static final Object[][] BROKERS = {
            {MOSL_REPORT_STATUS_LOCATION, MOSL_RECO_LOCATION, new String[]{"MOSL"}},
            {AMBIT_REPORT_STATUS_LOCATION, AMBIT_RECO_LOCATION, new String[]{"AMBIT"}},
            {AXIS_REPORT_STATUS_LOCATION, AXIS_RECO_LOCATION, new String[]{"AXIS"}},
            {ICICIDIRECT_REPORT_STATUS_LOCATION, ICICIDIRECT_RECO_LOCATION, new String[]{"ICICIDIRECT", "ICICI"}},
            {PL_REPORT_STATUS_LOCATION, PL_RECO_LOCATION, new String[]{"PL"}},
            {KOTAK_REPORT_STATUS_LOCATION, KOTAK_RECO_LOCATION, new String[]{"KOTAK"}},
    };

    public void updateResultTrackerExcel() {
        String basePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";
        File base = new File(basePath);

        String[] folders = base.list((dir, name) -> name.matches("\\d{4}Q[1-4]"));
        if (folders == null || folders.length == 0) {
            throw new RuntimeException("No quarter folders found.");
        }

        Arrays.sort(folders);
        String latestFolder = folders[folders.length - 1];

        System.out.println("Auto-detected latest folder: " + latestFolder);
        updateResultTrackerExcel(latestFolder);
    }

    public void updateResultTrackerExcel(String latestFolder) {
        updateResultTrackerExcel(latestFolder,
                "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\DBInsert\\ResultTracker.xlsx",
                "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\" + latestFolder);
    }

    /** trackerPath / quarterFolder are parameters so the update can be tried on copies */
    public void updateResultTrackerExcel(String latestFolder, String trackerPath, String quarterFolder) {
        String year = latestFolder.substring(2, 4);
        String quarter = latestFolder.substring(5);
        String fyQuarter = "FY" + year + "Q" + quarter;

        try (FileInputStream trackerFileInputStream = new FileInputStream(new File(trackerPath));
             Workbook trackerWorkbook = new XSSFWorkbook(trackerFileInputStream)) {

            Sheet fySheet = trackerWorkbook.getSheet(fyQuarter);
            if (fySheet == null) {
                throw new RuntimeException("Sheet " + fyQuarter + " not found in tracker file.");
            }
            FormulaEvaluator trackerFileEvaluator = trackerWorkbook.getCreationHelper().createFormulaEvaluator();
            List<String> lastFour = Arrays.asList(fyQuarter, previousQuarter(fyQuarter, 1), previousQuarter(fyQuarter, 2), previousQuarter(fyQuarter, 3));
            Map<String, Map<String, Map<String, String[]>>> allRecos = readAnalystViewTracker(ConfigFiles.analystViewTracker());

            int done = 0;
            for (int rowIndex = 2; rowIndex < fySheet.getLastRowNum() + 1; rowIndex++) {
                Row row = fySheet.getRow(rowIndex);
                if (row == null) continue;
                Cell tickerCell = row.getCell(TICKER_LOCATION);
                if (tickerCell == null || tickerCell.getCellTypeEnum() != CellType.STRING || tickerCell.getStringCellValue().trim().isEmpty()) continue;
                String ticker = tickerCell.getStringCellValue().trim();

                File tickerFile = new File(quarterFolder, ticker + "_" + fyQuarter + ".xlsx");
                if (!tickerFile.isFile()) {
                    System.out.println("Could not find file for " + ticker + ": " + tickerFile.getName());
                    continue;
                }
                try (FileInputStream in = new FileInputStream(tickerFile); Workbook tickerWorkbook = new XSSFWorkbook(in)) {
                    FormulaEvaluator ev = tickerWorkbook.getCreationHelper().createFormulaEvaluator();
                    ev.setIgnoreMissingWorkbooks(true);

                    boolean isFinancialTicker = false;
                    try {
                        CellValue cv = trackerFileEvaluator.evaluate(row.getCell(FOLDER_LOCATION));
                        isFinancialTicker = cv != null && cv.getStringValue() != null && cv.getStringValue().startsWith("Financials");
                    } catch (Exception ignored) { /* no folder / sector text */ }

                    Block inc = Block.read(tickerWorkbook.getSheet("HistoricalData"), "Quarterly Income Statement", fyQuarter, ev);
                    Block bfsi = Block.read(tickerWorkbook.getSheet("ModelBFSI"), "Quarterly Income Expense History", fyQuarter, ev);
                    Block bank = Block.read(tickerWorkbook.getSheet("ModelBFSI"), "Quarterly Banking History", fyQuarter, ev);
                    if (inc == null) {
                        System.out.println(ticker + ": HistoricalData 'Quarterly Income Statement' / column " + fyQuarter + " not found");
                        continue;
                    }

                    // result-day price: HistoricalData Share Price of the quarter once updateMCapAndPrice has frozen it;
                    // while that cell is still the template formula (annual price), the Data Sheet's Current Price
                    Double price = inc.currentIsFormula("Share Price") ? labelledValue(tickerWorkbook.getSheet("Data Sheet"), "Current Price", ev)
                            : inc.val("Share Price", 0);
                    setNumber(row, RESULT_PRICE_LOCATION, price, IS_OVERRIDE_PRICE_DATA, true);
                    // MinPrice / MaxPrice (old ValuationHistory, already empty) are left as they are: the new sheets have
                    // no valuation range yet (Model's intrinsic / relative target price equal the price until modelled)

                    // broker recos (last four quarters, latest first)
                    Map<String, Map<String, String[]>> recos = allRecos.getOrDefault(ticker.toUpperCase(), Collections.emptyMap());
                    for (Object[] b : BROKERS) {
                        Cell status = row.getCell((Integer) b[0]);
                        if (status == null || status.getCellTypeEnum() != CellType.STRING || status.getStringCellValue().isEmpty()) continue;
                        Cell recoCell = row.getCell((Integer) b[1]);
                        boolean empty = recoCell == null || recoCell.getCellTypeEnum() != CellType.STRING || recoCell.getStringCellValue().isEmpty();
                        if (!empty && !IS_OVERRIDE_RECO_DATA) continue;
                        Map<String, String[]> byQuarter = null;
                        for (String firm : (String[]) b[2]) if (byQuarter == null) byQuarter = recos.get(firm.toUpperCase());
                        List<String[]> list = new ArrayList<>();
                        for (String q : lastFour) list.add(byQuarter == null ? null : byQuarter.get(q));
                        if (recoCell == null) recoCell = row.createCell((Integer) b[1], CellType.STRING);
                        setReco(ticker, trackerWorkbook, recoCell, list);
                    }

                    // quarterly P&L series (k = 0 is the result quarter, 1 the quarter before ...)
                    Series revenue = inc.series("Revenue");
                    Series opm = inc.series("(Ex-Other Inc.) Op. Profit Margin%");
                    Series ebitMargin = inc.series("(Ex-Other Inc.) EBIT Margin%");
                    Series taxRate = inc.series("Tax Rate%");
                    Series otherIncome = inc.series("Other Income");
                    Series noplat = k -> {
                        Double r = revenue.at(k), m = ebitMargin.at(k), t = taxRate.at(k);
                        return r == null || m == null ? null : r * m * (1 - (t == null ? 0 : t));
                    };
                    Series otherIncAfterTax = k -> {
                        Double o = otherIncome.at(k), t = taxRate.at(k);
                        return o == null ? null : o * (1 - (t == null ? 0 : t));
                    };

                    if (!isFinancialTicker) {
                        setTrend(row, TTM_REV_TREND_LOCATION, ttm(revenue), 1000, false);
                        setTrend(row, TTM_NOPLAT_TREND_LOCATION, ttm(noplat), 1000, false);
                        setTrend(row, TTM_SUBSIDIARY_TREND_LOCATION, ttm(otherIncAfterTax), 1000, false);
                        setFive(row, REV_G_Q0_RATIO_LOCATION, yoy(revenue, false));
                        setFive(row, NOPLAT_G_Q0_RATIO_LOCATION, yoy(noplat, true));
                        setFive(row, OPM_Q0_RATIO_LOCATION, opm);
                    } else {
                        Series nii = bfsi == null ? null : bfsi.seriesIfReported("Net Interest Income");
                        Series stPat = bfsi == null ? null : bfsi.seriesIfReported("Standalone Profit");
                        Series conPat = bfsi == null ? null : bfsi.seriesIfReported("Consolidated Profit");
                        Series nim = bfsi == null ? null : bfsi.seriesIfReported("Net Interest Margin");
                        // subsidiaries' contribution = consolidated - standalone profit (as before); else the subsidiary rows
                        Series subs = bfsi == null ? null : bfsi.sumUnder("Subsidiary Profit");
                        if (stPat != null && conPat != null) {
                            Series st = stPat, con = conPat;
                            subs = k -> st.at(k) == null || con.at(k) == null ? null : con.at(k) - st.at(k);
                        }
                        // TTM NII (else TTM revenue), TTM standalone PAT (else consolidated), TTM subsidiaries
                        setTrend(row, TTM_REV_TREND_LOCATION, ttm(nii != null ? nii : revenue), 1000, false);
                        Series pat = stPat != null ? stPat : (conPat != null ? conPat : inc.series("Net Income"));
                        setTrend(row, TTM_NOPLAT_TREND_LOCATION, ttm(pat), 1000, false);
                        if (subs != null) setTrend(row, TTM_SUBSIDIARY_TREND_LOCATION, ttm(subs), 1000, false);
                        setFive(row, REV_G_Q0_RATIO_LOCATION, yoy(nii != null ? nii : revenue, false));
                        setFive(row, NOPLAT_G_Q0_RATIO_LOCATION, yoy(pat, true));
                        setFive(row, OPM_Q0_RATIO_LOCATION, nim != null ? nim : opm);
                        if (bank != null) {
                            Series aum = bank.seriesIfReported("Loans / AUM");
                            if (aum != null) setTrend(row, AUM0_LOCATION, aum, 100000, false);
                            if (aum != null) setFive(row, AUM_G_Q0_RATIO_LOCATION, yoy(aum, false));   // YoY (ModelBFSI's growth row is QoQ)
                            Series s;
                            if ((s = bank.seriesIfReported("GNPA")) != null) setTrend(row, GNPA_TREND_LOCATION, s, 1000, false);
                            if ((s = bank.seriesIfReported("NNPA")) != null) setTrend(row, NNPA_TREND_LOCATION, s, 1000, false);
                            if ((s = bank.seriesIfReported("GNPA%")) != null) setTrend(row, GNPA_RATIO_TREND_LOCATION, s, 1, true);
                            if ((s = bank.seriesIfReported("NNPA%")) != null) setTrend(row, NNPA_RATIO_TREND_LOCATION, s, 1, true);
                            if ((s = bank.seriesIfReported("Slippages")) != null) setTrend(row, SLIPPAGES_TREND_LOCATION, s, 1000, false);
                        }
                    }
                    done++;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Exception in processing ticker " + ticker);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(new File(trackerPath))) {
                trackerWorkbook.write(fos);
            }
            System.out.println("ResultTracker " + fyQuarter + ": " + done + " tickers updated from the new sheets");
        } catch (IOException e) {
            throw new RuntimeException("Error updating result tracker: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ workbook readers

    /** a value per quarter, k quarters before the result quarter (null when missing) */
    interface Series { Double at(int k); }

    /** One history block (title in column B, "Quarters" header in column C, row labels in column C or D). */
    static class Block {
        final Map<String, List<Double>> rows = new LinkedHashMap<>();
        final List<String> order = new ArrayList<>();
        final List<List<Double>> values = new ArrayList<>();
        final List<Boolean> mainRow = new ArrayList<>();
        final Set<String> formulaAtCurrent = new HashSet<>();
        int current;     // index of the result quarter among the block's columns

        static Block read(Sheet sheet, String title, String fyQuarter, FormulaEvaluator ev) {
            if (sheet == null) return null;
            int last = sheet.getLastRowNum(), r0 = -1, hdr = -1, end = last + 1;
            for (int r = 0; r <= last && r0 < 0; r++) if (text(cellAt(sheet, r, 1), ev).startsWith(title)) r0 = r;
            if (r0 < 0) return null;
            for (int r = r0; r <= Math.min(last, r0 + 6) && hdr < 0; r++) if (text(cellAt(sheet, r, 2), ev).startsWith("Quarter")) hdr = r;
            if (hdr < 0) return null;
            for (int r = hdr + 1; r <= last; r++) if (!text(cellAt(sheet, r, 1), ev).isEmpty()) { end = r; break; }
            List<Integer> cols = new ArrayList<>();
            Row hr = sheet.getRow(hdr);
            int current = -1;
            for (int c = 3; hr != null && c < hr.getLastCellNum(); c++) {
                String l = text(hr.getCell(c), ev);
                if (!l.matches("FY\\d{2}Q[1-4]")) continue;
                cols.add(c);
                if (l.equals(fyQuarter)) { current = cols.size() - 1; break; }     // nothing after the result quarter
            }
            if (current < 0) return null;
            Block b = new Block();
            b.current = current;
            for (int r = hdr + 1; r < end; r++) {
                String c = text(cellAt(sheet, r, 2), ev), d = text(cellAt(sheet, r, 3), ev);
                if (c.isEmpty() && d.isEmpty()) continue;
                List<Double> v = new ArrayList<>();
                for (int col : cols) v.add(number(cellAt(sheet, r, col), ev));
                String label = c.isEmpty() ? d : c;
                b.order.add(label);
                b.values.add(v);
                b.mainRow.add(!c.isEmpty());
                if (!b.rows.containsKey(label)) {
                    Cell cc = cellAt(sheet, r, cols.get(current));
                    if (cc != null && cc.getCellTypeEnum() == CellType.FORMULA) b.formulaAtCurrent.add(label);
                }
                b.rows.putIfAbsent(label, v);
            }
            return b;
        }

        Double val(String label, int k) { return series(label).at(k); }

        boolean currentIsFormula(String label) { return formulaAtCurrent.contains(label); }

        Series series(String label) {
            List<Double> v = rows.get(label);
            return k -> v == null || current - k < 0 ? null : v.get(current - k);
        }

        /** the row's series, or null when it reports nothing (template row left at 0 / blank) */
        Series seriesIfReported(String label) {
            List<Double> v = rows.get(label);
            if (v == null || v.stream().noneMatch(x -> x != null && x != 0)) return null;
            return series(label);
        }

        /** sum of the sub-rows below a column-C header (e.g. the subsidiaries under "Subsidiary Profit") */
        Series sumUnder(String header) {
            int i = order.indexOf(header);
            if (i < 0) return null;
            List<List<Double>> subs = new ArrayList<>();
            for (int j = i + 1; j < order.size() && !mainRow.get(j); j++) subs.add(values.get(j));
            if (subs.stream().noneMatch(v -> v.stream().anyMatch(x -> x != null && x != 0))) return null;
            return k -> {
                if (current - k < 0) return null;
                double s = 0;
                boolean any = false;
                for (List<Double> v : subs) { Double x = v.get(current - k); if (x != null) { s += x; any = true; } }
                return any ? s : null;
            };
        }
    }

    /** ticker -> Firm -> quarter -> {reco, target} from AnalystViewTracker.xlsx (sheet AnalystsRecoNew) */
    static Map<String, Map<String, Map<String, String[]>>> readAnalystViewTracker(String path) {
        try (FileInputStream in = new FileInputStream(path); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheet("AnalystsRecoNew");
            if (sheet == null) { System.out.println(path + ": sheet AnalystsRecoNew not found, no broker recos"); return new HashMap<>(); }
            return readRecos(sheet, wb.getCreationHelper().createFormulaEvaluator());
        } catch (Exception e) {
            System.out.println("Could not read broker recos from " + path + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    /** latest reco + target per Ticker, Firm and quarter; header row with Ticker / Quarter / Date / Firm / Reco / TP */
    static Map<String, Map<String, Map<String, String[]>>> readRecos(Sheet sheet, FormulaEvaluator ev) {
        Map<String, Map<String, Map<String, String[]>>> out = new HashMap<>();
        if (sheet == null) return out;
        int last = sheet.getLastRowNum(), hdr = -1;
        int cTicker = -1, cQuarter = -1, cDate = -1, cFirm = -1, cReco = -1, cTp = -1;
        for (int r = 0; r <= last && hdr < 0; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 0; c < Math.min(row.getLastCellNum(), 40); c++) {
                String t = text(row.getCell(c), ev);
                if (t.equals("Ticker")) cTicker = c;
                else if (t.equals("Firm")) cFirm = c;
                else if (t.equals("Quarter")) cQuarter = c;
                else if (t.equals("Date")) cDate = c;
                else if (t.equals("Reco")) cReco = c;
                else if (t.equals("TP")) cTp = c;
            }
            if (cTicker >= 0 && cFirm >= 0 && cQuarter >= 0 && cReco >= 0) hdr = r;
            else { cTicker = cQuarter = cDate = cFirm = cReco = cTp = -1; }
        }
        if (hdr < 0) return out;
        Map<String, Double> latest = new HashMap<>();
        for (int r = hdr + 1; r <= last; r++) {
            String tk = text(cellAt(sheet, r, cTicker), ev).toUpperCase();
            String firm = text(cellAt(sheet, r, cFirm), ev).toUpperCase(), q = text(cellAt(sheet, r, cQuarter), ev);
            String reco = text(cellAt(sheet, r, cReco), ev);
            if (tk.isEmpty() || firm.isEmpty() || q.isEmpty() || reco.isEmpty()) continue;
            Double tp = cTp < 0 ? null : number(cellAt(sheet, r, cTp), ev);
            Double date = cDate < 0 ? null : number(cellAt(sheet, r, cDate), ev);
            String key = tk + "|" + firm + "|" + q;
            double d = date == null ? 0 : date;
            if (latest.containsKey(key) && latest.get(key) > d) continue;      // keep the latest report of the quarter
            latest.put(key, d);
            out.computeIfAbsent(tk, t -> new HashMap<>()).computeIfAbsent(firm, f -> new HashMap<>())
               .put(q, new String[]{reco, tp == null ? "" : String.valueOf(Math.round(tp))});
        }
        return out;
    }

    /** first number to the right of a label in columns A-E, e.g. "Current Price" in the Data Sheet */
    static Double labelledValue(Sheet sheet, String label, FormulaEvaluator ev) {
        if (sheet == null) return null;
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 0; c <= 4; c++) {
                if (!text(row.getCell(c), ev).equals(label)) continue;
                for (int x = c + 1; x <= c + 12; x++) {
                    Double v = number(row.getCell(x), ev);
                    if (v != null) return v;
                }
                return null;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ derived series

    static Series ttm(Series s) {
        return k -> {
            double sum = 0;
            for (int i = k; i < k + 4; i++) { Double v = s.at(i); if (v == null) return null; sum += v; }
            return sum;
        };
    }

    /** YoY growth; for profits (positiveOnly) 0 unless both quarters are positive, as before */
    static Series yoy(Series s, boolean positiveOnly) {
        return k -> {
            Double cur = s.at(k), prev = s.at(k + 4);
            if (cur == null || prev == null || prev == 0) return positiveOnly ? 0d : null;
            if (positiveOnly && (cur <= 0 || prev <= 0)) return 0d;
            return Math.round((cur / prev - 1) * 10000) / 10000.0;
        };
    }

    // ------------------------------------------------------------------ tracker writers

    private static void setNumber(Row row, int col, Double value, boolean override, boolean positiveOnly) {
        if (value == null || (positiveOnly && value <= 0)) return;
        Cell c = row.getCell(col);
        boolean blank = c == null || c.getCellTypeEnum() == CellType.BLANK
                || (c.getCellTypeEnum() == CellType.NUMERIC && c.getNumericCellValue() == 0);
        if (!blank && !override) return;
        if (c == null) c = row.createCell(col, CellType.NUMERIC);
        c.setCellValue(value);
    }

    /** five numbers Q0..Q4 into five consecutive cells starting at col */
    private static void setFive(Row row, int col, Series s) {
        for (int k = 0; k < 5; k++) {
            Double v = s.at(k);
            Cell c = row.getCell(col + k);
            boolean blank = c == null || c.getCellTypeEnum() == CellType.BLANK;
            if (!blank && !IS_OVERRIDE_OTHER_DATA) continue;
            if (c == null) c = row.createCell(col + k, CellType.NUMERIC);
            if (v == null) c.setCellType(CellType.BLANK); else c.setCellValue(v);
        }
    }

    /** "q0 / q1 / q2 / q3 / q4" (latest first), values / unit with 2 decimals, or x100 with "%" */
    private static void setTrend(Row row, int col, Series s, double unit, boolean isPercent) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < 5; k++) {
            Double v = s.at(k);
            if (v != null) {
                double x = isPercent ? v * 100 : v / unit;
                sb.append(Math.round(x * 100) / 100.0);
                if (isPercent) sb.append('%');
            }
            if (k < 4) sb.append(" / ");
        }
        Cell c = row.getCell(col);
        if (c == null) c = row.createCell(col, CellType.STRING);
        c.setCellValue(sb.toString());
    }

    /**
     * "reco tp / reco tp / ..." for the last four quarters (latest first, blank when the broker has no report);
     * a reco or target is green when better than the broker's previous quarter, red when worse.
     */
    private void setReco(String ticker, Workbook trackerWorkbook, Cell trackerCell, List<String[]> lastFour) {
        XSSFWorkbook workbook = (XSSFWorkbook) trackerWorkbook;
        XSSFFont greenFont = workbook.createFont();
        greenFont.setColor(IndexedColors.GREEN.getIndex());
        XSSFFont redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.getIndex());

        StringBuilder sb = new StringBuilder();
        List<int[]> spans = new ArrayList<>();          // {start, end, 1=green / -1=red}
        for (int i = 0; i < lastFour.size(); i++) {
            String[] cur = lastFour.get(i);
            if (i > 0) sb.append(" / ");
            if (cur == null) continue;
            String[] prev = null;
            for (int j = i + 1; j < lastFour.size() && prev == null; j++) prev = lastFour.get(j);
            int recoStart = sb.length();
            sb.append(cur[0]);
            int recoEnd = sb.length();
            sb.append(' ');
            int tpStart = sb.length();
            sb.append(cur[1]);
            int tpEnd = sb.length();
            if (prev == null) continue;
            if (isUpgrade(prev[0], cur[0])) spans.add(new int[]{recoStart, recoEnd, 1});
            if (isDowngrade(prev[0], cur[0])) spans.add(new int[]{recoStart, recoEnd, -1});
            try {
                int t = Integer.parseInt(cur[1]), p = Integer.parseInt(prev[1]);
                if (t > p) spans.add(new int[]{tpStart, tpEnd, 1});
                if (t < p) spans.add(new int[]{tpStart, tpEnd, -1});
            } catch (NumberFormatException ignored) { /* no target */ }
        }
        try {
            XSSFRichTextString rich = new XSSFRichTextString(sb.toString());
            for (int[] s : spans) if (s[1] > s[0]) rich.applyFont(s[0], s[1], s[2] > 0 ? greenFont : redFont);
            trackerCell.setCellValue(rich);
        } catch (Exception e) {
            System.out.println("Exception in setting Rich Text for " + ticker + ": " + e.getMessage());
            trackerCell.setCellValue(sb.toString());
        }
    }

    public boolean isUpgrade(String lastReco, String currentReco) {
        int lastRecoRank = getRecommendationRank(lastReco);
        int currentRecoRank = getRecommendationRank(currentReco);
        return currentRecoRank > 0 && lastRecoRank > 0 && currentRecoRank < lastRecoRank;
    }

    public boolean isDowngrade(String lastReco, String currentReco) {
        int lastRecoRank = getRecommendationRank(lastReco);
        int currentRecoRank = getRecommendationRank(currentReco);
        return currentRecoRank > 0 && lastRecoRank > 0 && currentRecoRank > lastRecoRank;
    }

    public int getRecommendationRank(String reco) {
        switch (reco.trim().toLowerCase()) {
            case "buy":
                return 1;
            case "accumulate":
            case "add":
                return 2;
            case "hold":
                return 3;
            case "neutral":
                return 4;
            case "reduce":
            case "sell":
                return 5;
            case "ur":
            case "na":
            case "nr":
                return 6;
            default:
                return 0; // Return 0 for unknown or invalid recommendations
        }
    }

    // ------------------------------------------------------------------ cell helpers

    /** FY27Q2 minus n quarters (FY27Q1, FY26Q4, ...) */
    static String previousQuarter(String fyq, int n) {
        int fy = Integer.parseInt(fyq.substring(2, 4)), q = Integer.parseInt(fyq.substring(5));
        int idx = fy * 4 + (q - 1) - n;
        return String.format("FY%02dQ%d", idx / 4, idx % 4 + 1);
    }

    private static Cell cellAt(Sheet sheet, int row, int col) {
        if (col < 0) return null;
        Row r = sheet.getRow(row);
        return r == null ? null : r.getCell(col);
    }

    /** trimmed string value (formulas evaluated), "" when not text */
    static String text(Cell c, FormulaEvaluator ev) {
        if (c == null) return "";
        try {
            if (c.getCellTypeEnum() == CellType.STRING) return c.getStringCellValue().trim();
            if (c.getCellTypeEnum() == CellType.FORMULA) {
                CellValue v = ev.evaluate(c);
                return v != null && v.getCellTypeEnum() == CellType.STRING ? v.getStringValue().trim() : "";
            }
        } catch (Exception e) {
            try { return c.getCachedFormulaResultTypeEnum() == CellType.STRING ? c.getStringCellValue().trim() : ""; }
            catch (Exception ignored) { return ""; }
        }
        return "";
    }

    /** numeric value (formulas evaluated, cached result if evaluation fails), null when not a number */
    static Double number(Cell c, FormulaEvaluator ev) {
        if (c == null) return null;
        try {
            if (c.getCellTypeEnum() == CellType.NUMERIC) return c.getNumericCellValue();
            if (c.getCellTypeEnum() == CellType.FORMULA) {
                CellValue v = ev.evaluate(c);
                return v != null && v.getCellTypeEnum() == CellType.NUMERIC ? v.getNumberValue() : null;
            }
        } catch (Exception e) {
            try { return c.getCachedFormulaResultTypeEnum() == CellType.NUMERIC ? c.getNumericCellValue() : null; }
            catch (Exception ignored) { return null; }
        }
        return null;
    }
}
