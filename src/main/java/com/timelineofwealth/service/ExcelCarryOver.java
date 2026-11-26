package com.timelineofwealth.service;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Carries everything the user added in last quarter's workbook into the newly downloaded Screener workbook
 * (same template sheets, fresh "Data Sheet"), without any row/column configuration:
 *
 *  - Rows are matched by block (title text in column B) and row label (column C, D or E); a row whose label
 *    was customised (e.g. segment names typed over "Segment1") is matched by its position inside the block.
 *  - Inside a block, columns under a period header (a row with FY26 / FY27Q2 labels) are matched by label,
 *    so a new quarter or a new financial year shifts the data automatically; other columns map one-to-one.
 *  - Typed values (numbers, text, dates) are copied with their formatting, so manual inputs (segments, other
 *    information, bank data, model assumptions, forecasts) survive. For reported periods a template formula
 *    wins over a typed/pasted value (the history must follow the Data Sheet), except Share Price and Result
 *    Date rows.
 *  - Typed values of Share Price, Market Cap, EPS (Diluted) and Result Date are kept for reported periods too.
 *  - Where the new cell is empty, an old formula is copied as a formula, moved like an Excel copy (relative
 *    references follow the shifted columns), e.g. the user's formulas on the Other Information pages. Formulas
 *    that read the Data Sheet directly go in as values, which fills the history columns Screener no longer
 *    provides (the 11th/12th quarter or year). Formulas of the new workbook are kept.
 *  - Formatting of empty cells is copied too, so custom pages keep their look.
 */
public class ExcelCarryOver {

    /** sheets never carried over: Screener's own sheets and sheets no longer used */
    public static final Set<String> SKIP_SHEETS = new HashSet<>(Arrays.asList("Data Sheet", "Customization", "Analysts&Consensus"));
    /** sheets removed from the new workbook */
    public static final Set<String> DROP_SHEETS = new HashSet<>(Collections.singletonList("Analysts&Consensus"));

    private static final Pattern PERIOD = Pattern.compile("^FY\\d{2}(Q[1-4])?$");
    /** rows whose typed (non-formula) values beat the template formula, annual and quarterly */
    private static final Set<String> KEEP_TYPED = new HashSet<>(Arrays.asList(
            "Share Price", "Result Date", "EPS (Diluted)", "Market Cap"));

    private final XSSFWorkbook oldWb, newWb;
    private final FormulaEvaluator oldEv, newEv;
    private final Map<Short, XSSFCellStyle> styleCache = new HashMap<>();
    private final DataFormatter fmt = new DataFormatter();

    public int values, carried, formulas, styles;
    private final Map<Cell, CellValue> moved = new LinkedHashMap<>();

    public ExcelCarryOver(XSSFWorkbook oldWb, XSSFWorkbook newWb) {
        this.oldWb = oldWb;
        this.newWb = newWb;
        this.oldEv = oldWb.getCreationHelper().createFormulaEvaluator();
        this.newEv = newWb.getCreationHelper().createFormulaEvaluator();
    }

    /** carries every sheet over and drops the unused ones; returns a one-line summary */
    public String run() {
        StringBuilder log = new StringBuilder();
        for (int i = 0; i < oldWb.getNumberOfSheets(); i++) {
            XSSFSheet os = oldWb.getSheetAt(i);
            if (SKIP_SHEETS.contains(os.getSheetName())) continue;
            XSSFSheet ns = newWb.getSheet(os.getSheetName());
            if (ns == null) { log.append(os.getSheetName()).append(": not in the new workbook; "); continue; }
            int v0 = values, c0 = carried, f0 = formulas;
            carrySheet(os, ns);
            log.append(os.getSheetName()).append(": ").append(values - v0).append(" values, ")
               .append(carried - c0).append(" carried, ").append(formulas - f0).append(" formulas; ");
        }
        int fixed = fixErrors();
        if (fixed > 0) log.append(fixed).append(" moved formulas gave errors, kept as values; ");
        for (String name : DROP_SHEETS) {
            int ix = newWb.getSheetIndex(name);
            if (ix >= 0) { newWb.removeSheetAt(ix); log.append("dropped ").append(name).append("; "); }
        }
        newWb.setForceFormulaRecalculation(true);
        return log.toString();
    }

    // ------------------------------------------------------------------ layout

    /** what we know about each row of a sheet */
    private static final class Layout {
        final Map<Integer, String> block = new HashMap<>();        // row -> block title
        final Map<Integer, Integer> offset = new HashMap<>();      // row -> rows below the block title
        final Map<Integer, String> label = new HashMap<>();        // row -> row label (may be empty)
        final Map<Integer, Integer> header = new HashMap<>();      // row -> its period header row (if any)
        final Map<Integer, Map<String, Integer>> periods = new HashMap<>();   // header row -> label -> column
        final Map<String, Integer> byKey = new HashMap<>();        // block|label#n -> row
        final Map<String, Integer> byOffset = new HashMap<>();     // block@offset -> row
        int last;
    }

    private Layout layout(Sheet s, FormulaEvaluator ev) {
        Layout l = new Layout();
        l.last = s.getLastRowNum();
        String block = "";
        int blockRow = 0, header = -1;
        Map<String, Integer> seen = new HashMap<>();
        for (int r = 0; r <= l.last; r++) {
            Row row = s.getRow(r);
            String b = text(row, 1, ev);
            if (!b.isEmpty()) {                       // a new block (company-name rows count too, harmless)
                block = b; blockRow = r; header = -1;
            }
            Map<String, Integer> p = periodsOf(row, ev);
            if (p.size() >= 3) { header = r; l.periods.put(r, p); }
            String lab = text(row, 2, ev);
            if (lab.isEmpty()) lab = text(row, 3, ev);
            if (lab.isEmpty()) lab = text(row, 4, ev);
            l.block.put(r, block);
            l.offset.put(r, r - blockRow);
            l.label.put(r, lab);
            if (header >= 0) l.header.put(r, header);
            String key = block + "|" + lab;
            int n = seen.merge(key, 1, Integer::sum);
            if (!lab.isEmpty()) l.byKey.putIfAbsent(key + "#" + n, r);
            l.byOffset.put(block + "@" + (r - blockRow), r);
        }
        return l;
    }

    /**
     * Period label -> column of a header row, keyed "section:label": section 0 is the first run of period
     * columns (history), 1 the run after the spacer columns (forecast). Matching on the key keeps history with
     * history and forecast with forecast: last quarter's forecast column FY27Q2 must not land in this quarter's
     * reported FY27Q2 column (its forecast formula would turn circular there).
     */
    private Map<String, Integer> periodsOf(Row row, FormulaEvaluator ev) {
        Map<String, Integer> p = new LinkedHashMap<>();
        if (row == null) return p;
        int section = 0, prev = -1;
        for (int c = 5; c < Math.min(row.getLastCellNum(), 60); c++) {
            String t = text(row, c, ev);
            if (!PERIOD.matcher(t).matches()) continue;
            if (prev >= 0 && c > prev + 1) section++;
            prev = c;
            p.putIfAbsent(section + ":" + t, c);
        }
        return p;
    }

    private static String label(String key) { return key.substring(key.indexOf(':') + 1); }

    /** old row -> new row: same block + label (n-th occurrence), else same position inside the same block */
    private Map<Integer, Integer> rowMap(Layout o, Layout n) {
        Map<Integer, Integer> map = new HashMap<>();
        Set<Integer> used = new HashSet<>();
        Map<String, Integer> seen = new HashMap<>();
        for (int r = 0; r <= o.last; r++) {
            String lab = o.label.get(r);
            String key = o.block.get(r) + "|" + lab;
            int k = seen.merge(key, 1, Integer::sum);
            if (lab.isEmpty()) continue;
            Integer t = n.byKey.get(key + "#" + k);
            if (t != null && used.add(t)) map.put(r, t);
        }
        for (int r = 0; r <= o.last; r++) {
            if (map.containsKey(r)) continue;
            Integer t = n.byOffset.get(o.block.get(r) + "@" + o.offset.get(r));
            if (t != null && !used.contains(t)) { map.put(r, t); used.add(t); }
        }
        return map;
    }

    // ------------------------------------------------------------------ copy

    /**
     * Rewrites shared formulas as ordinary per-cell formulas: overwriting the first cell of a shared group
     * would otherwise blank the formulas of every cell that shares it.
     */
    private static void unshareFormulas(XSSFSheet s) {
        for (Row r : s) for (Cell c : r) {
            if (c.getCellType() != Cell.CELL_TYPE_FORMULA || c.isPartOfArrayFormulaGroup()) continue;
            XSSFCell x = (XSSFCell) c;
            if (x.getCTCell().getF() == null || x.getCTCell().getF().getT() != org.openxmlformats.schemas
                    .spreadsheetml.x2006.main.STCellFormulaType.SHARED) continue;
            try { x.setCellFormula(x.getCellFormula()); } catch (Exception ignored) { }
        }
    }

    private void carrySheet(XSSFSheet os, XSSFSheet ns) {
        unshareFormulas(ns);
        Layout lo = layout(os, oldEv), ln = layout(ns, newEv);
        Map<Integer, Integer> rows = rowMap(lo, ln);
        for (Map.Entry<Integer, Integer> e : rows.entrySet()) {
            Row or = os.getRow(e.getKey());
            if (or == null) continue;
            int nr = e.getValue();
            Row target = ns.getRow(nr);
            if (target == null) target = ns.createRow(nr);
            if (or.isFormatted() || or.getHeight() != os.getDefaultRowHeight()) target.setHeight(or.getHeight());

            Map<String, Integer> op = lo.header.containsKey(e.getKey()) ? lo.periods.get(lo.header.get(e.getKey())) : null;
            Map<String, Integer> np = ln.header.containsKey(nr) ? ln.periods.get(ln.header.get(nr)) : null;
            Map<Integer, String> colLabel = new HashMap<>();
            if (op != null) for (Map.Entry<String, Integer> p : op.entrySet()) colLabel.put(p.getValue(), p.getKey());

            for (Cell oc : or) {
                int c = oc.getColumnIndex();
                int nc = c;
                String period = colLabel.get(c);
                if (period != null) {                         // a period column: follow the label
                    Integer m = np == null ? null : np.get(period);
                    if (m == null) continue;                  // period dropped out of the window
                    nc = m;
                } else if (op != null && np != null && isBetweenPeriods(op, c)) {
                    continue;                                 // spacer column inside the period range
                }
                Cell nc_ = target.getCell(nc);
                if (nc_ == null) nc_ = target.createCell(nc);
                // reported periods: the template's formulas (Data Sheet links and calculations) win over values
                // pasted in old workbooks; typed share prices / result dates are kept
                boolean templateWins = period != null && isActual(label(period))
                        && !(keepTyped(lo.label.get(e.getKey())) && !isZero(oc));
                copy(oc, nc_, templateWins);
            }
        }
    }

    /** Share Price, Market Cap, EPS, Result Date and Total Volume (units, headcount...: the template's
     *  formula there is only a placeholder = revenue) keep typed numbers */
    private static boolean keepTyped(String label) {
        return label != null && (KEEP_TYPED.contains(label) || label.startsWith("Total Volume"));
    }

    /** a typed 0 is a placeholder, not data: the template formula is kept */
    private static boolean isZero(Cell c) {
        return c.getCellType() == Cell.CELL_TYPE_NUMERIC && c.getNumericCellValue() == 0;
    }

    private static boolean isBetweenPeriods(Map<String, Integer> p, int c) {
        int lo = Integer.MAX_VALUE, hi = -1;
        for (int v : p.values()) { lo = Math.min(lo, v); hi = Math.max(hi, v); }
        return c > lo && c < hi;
    }

    /** FY27 ends 31 Mar 2027, FY27Q2 ends 30 Sep 2026; true when that date has passed */
    private static boolean isActual(String period) {
        java.util.regex.Matcher m = Pattern.compile("^FY(\\d{2})(?:Q([1-4]))?$").matcher(period);
        if (!m.matches()) return false;
        int fy = 2000 + Integer.parseInt(m.group(1));
        Calendar cal = Calendar.getInstance();
        cal.clear();
        if (m.group(2) == null) cal.set(fy, Calendar.MARCH, 31);
        else {
            int q = Integer.parseInt(m.group(2));
            int[] month = {0, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.DECEMBER, Calendar.MARCH};
            int[] day = {0, 30, 30, 31, 31};
            cal.set(q == 4 ? fy : fy - 1, month[q], day[q]);
        }
        return !cal.getTime().after(new Date());
    }

    private void copy(Cell oc, Cell nc, boolean templateWins) {
        int ot = oc.getCellType();
        boolean newHasFormula = nc.getCellType() == Cell.CELL_TYPE_FORMULA;
        if (templateWins && newHasFormula) return;
        boolean newEmpty = nc.getCellType() == Cell.CELL_TYPE_BLANK
                || (nc.getCellType() == Cell.CELL_TYPE_STRING && nc.getStringCellValue().isEmpty());
        boolean wrote = false;

        if (ot == Cell.CELL_TYPE_NUMERIC || ot == Cell.CELL_TYPE_STRING || ot == Cell.CELL_TYPE_BOOLEAN) {
            if (newHasFormula) {
                try { nc.setCellType(Cell.CELL_TYPE_BLANK); } catch (IllegalStateException arrayPart) { return; }
            }
            switch (ot) {
                case Cell.CELL_TYPE_NUMERIC: nc.setCellValue(oc.getNumericCellValue()); break;
                case Cell.CELL_TYPE_STRING:  nc.setCellValue(oc.getRichStringCellValue().getString()); break;
                default:                     nc.setCellValue(oc.getBooleanCellValue());
            }
            values++;
            wrote = true;
        } else if (ot == Cell.CELL_TYPE_FORMULA && newEmpty && !newHasFormula) {
            // the user's own formula (e.g. on the Other Information pages): copy it, moved like an Excel copy so
            // relative references follow the shifted columns. Formulas reading the Data Sheet directly (and
            // array formulas) are carried as values: the Data Sheet's columns move on their own each quarter.
            String f = shiftedFormula(oc, nc.getRowIndex() - oc.getRowIndex(), nc.getColumnIndex() - oc.getColumnIndex());
            if (f != null) {
                try {
                    nc.setCellFormula(f);
                    formulas++;
                    moved.put(nc, evaluate(oc));              // checked for errors once everything is copied
                    nc.setCellStyle(style((XSSFCellStyle) oc.getCellStyle()));
                    styles++;
                    return;
                } catch (Exception bad) { nc.setCellType(Cell.CELL_TYPE_BLANK); }
            }
            CellValue v = evaluate(oc);
            if (v != null) {
                if (v.getCellType() == Cell.CELL_TYPE_NUMERIC) { nc.setCellValue(v.getNumberValue()); carried++; wrote = true; }
                else if (v.getCellType() == Cell.CELL_TYPE_STRING && !v.getStringValue().isEmpty()) { nc.setCellValue(v.getStringValue()); carried++; wrote = true; }
            }
        }
        // formatting: always for written cells, and for empty styled cells (custom pages keep their look)
        if (wrote || (ot == Cell.CELL_TYPE_BLANK && oc.getCellStyle().getIndex() != 0 && !newHasFormula)) {
            nc.setCellStyle(style((XSSFCellStyle) oc.getCellStyle()));
            styles++;
        }
    }

    /**
     * A moved formula that now gives an error (#DIV/0!, #REF!..., e.g. a TTM ratio moved next to an empty
     * column) is replaced by the old cell's value, or left empty. Unsupported functions count as fine.
     */
    private int fixErrors() {
        newEv.clearAllCachedResultValues();
        int n = 0;
        for (Map.Entry<Cell, CellValue> e : moved.entrySet()) {
            Cell c = e.getKey();
            CellValue v;
            try { v = newEv.evaluate(c); } catch (Exception ex) { continue; }
            if (v == null || v.getCellType() != Cell.CELL_TYPE_ERROR) continue;
            CellValue old = e.getValue();
            c.setCellType(Cell.CELL_TYPE_BLANK);
            if (old != null && old.getCellType() == Cell.CELL_TYPE_NUMERIC) c.setCellValue(old.getNumberValue());
            formulas--;
            n++;
        }
        return n;
    }

    /** the old cell's formula moved by (dRow, dCol) like an Excel copy/paste; null when it must go in as a value */
    private String shiftedFormula(Cell oc, int dRow, int dCol) {
        try {
            if (oc.isPartOfArrayFormulaGroup()) return null;
            String f = oc.getCellFormula();
            if (f.contains("Data Sheet") || f.contains("Analysts&Consensus") || f.contains("[")) return null;
            int sheetIx = oldWb.getSheetIndex(oc.getSheet());
            Ptg[] ptgs = FormulaParser.parse(f, XSSFEvaluationWorkbook.create(oldWb), FormulaType.CELL, sheetIx);
            for (Ptg p : ptgs) {
                if (p instanceof RefPtgBase) {
                    RefPtgBase r = (RefPtgBase) p;
                    if (r.isRowRelative()) r.setRow(r.getRow() + dRow);
                    if (r.isColRelative()) r.setColumn(r.getColumn() + dCol);
                    if (r.getRow() < 0 || r.getColumn() < 0) return null;
                } else if (p instanceof AreaPtgBase) {
                    AreaPtgBase a = (AreaPtgBase) p;
                    if (a.isFirstRowRelative()) a.setFirstRow(a.getFirstRow() + dRow);
                    if (a.isLastRowRelative()) a.setLastRow(a.getLastRow() + dRow);
                    if (a.isFirstColRelative()) a.setFirstColumn(a.getFirstColumn() + dCol);
                    if (a.isLastColRelative()) a.setLastColumn(a.getLastColumn() + dCol);
                    if (a.getFirstRow() < 0 || a.getFirstColumn() < 0) return null;
                }
            }
            return FormulaRenderer.toFormulaString(XSSFEvaluationWorkbook.create(newWb), ptgs);
        } catch (Exception e) {
            return null;
        }
    }

    private CellValue evaluate(Cell c) {
        try {
            return oldEv.evaluate(c);
        } catch (Exception e) {                               // unsupported function: use the cached result
            try {
                switch (c.getCachedFormulaResultType()) {
                    case Cell.CELL_TYPE_NUMERIC: return new CellValue(c.getNumericCellValue());
                    case Cell.CELL_TYPE_STRING:  return new CellValue(c.getStringCellValue());
                    default: return null;
                }
            } catch (Exception ignored) { return null; }
        }
    }

    private XSSFCellStyle style(XSSFCellStyle s) {
        return styleCache.computeIfAbsent(s.getIndex(), k -> {
            XSSFCellStyle n = newWb.createCellStyle();
            n.cloneStyleFrom(s);
            return n;
        });
    }

    private String text(Row row, int col, FormulaEvaluator ev) {
        if (row == null) return "";
        Cell c = row.getCell(col);
        if (c == null) return "";
        try {
            if (c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                CellValue v = ev.evaluate(c);
                return v != null && v.getCellType() == Cell.CELL_TYPE_STRING ? v.getStringValue().trim() : "";
            }
            return c.getCellType() == Cell.CELL_TYPE_STRING ? c.getStringCellValue().trim() : "";
        } catch (Exception e) {
            try { return c.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING ? c.getStringCellValue().trim() : ""; }
            catch (Exception ignored) { return ""; }
        }
    }
}
