package com.timelineofwealth.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section names of watchlists.properties ("001_FocusFive", "101_FinancialServices_BanksLargePrivate") and how the
 * public pages show them. Only Focus Five, the three long-term shortlists and the sector groups (101 onwards) are
 * public; the other sections (002 and the 006-099 themes) stay internal.
 */
public final class WatchlistNames {
    private static final Pattern CODE = Pattern.compile("^(\\d{3})_(.+)$");
    private static final Set<Integer> PUBLIC_IDEAS = new HashSet<>(Arrays.asList(1, 3, 4, 5));
    public static final String IDEAS_GROUP = "Portfolio and shortlists";
    public static final String OTHER = "Other";

    private WatchlistNames() {}

    public static int code(String section) {
        Matcher m = section == null ? null : CODE.matcher(section.trim());
        return m != null && m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }

    public static boolean isPublic(String section) {
        int c = code(section);
        return PUBLIC_IDEAS.contains(c) || c >= 101;
    }

    /** "101_FinancialServices_BanksLargePrivate" -> "Financial Services – Banks Large Private"; "001_FocusFive" -> "Focus Five". */
    public static String display(String section) {
        if (section == null) return null;
        Matcher m = CODE.matcher(section.trim());
        String body = m.matches() ? m.group(2) : section.trim();
        if (OTHER.equalsIgnoreCase(body)) return OTHER;
        String[] parts = body.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" – ");
            sb.append(words(p));
        }
        return sb.toString();
    }

    /** Heading the section is listed under: the sector for 101+ groups, else "Portfolio and shortlists". */
    public static String group(String section) {
        int c = code(section);
        if (c >= 101) {
            String d = display(section);
            int i = d.indexOf(" – ");
            return i > 0 ? d.substring(0, i) : d;
        }
        return c > 0 ? IDEAS_GROUP : OTHER;
    }

    /** "Financial Services – Banks Large Private" without the sector: "Banks Large Private". */
    public static String shortName(String section) {
        String d = display(section);
        int i = d == null ? -1 : d.indexOf(" – ");
        return i > 0 ? d.substring(i + 3) : d;
    }

    /** CamelCase to words, keeping acronyms and "&" together: "ER&DTech" -> "ER&D Tech", "QSRFoodServices" -> "QSR Food Services". */
    static String words(String s) {
        String out = s.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("(?<=[A-Z&])(?=[A-Z][a-z])", " ");
        out = out.replaceAll("\\b([A-Z]{2,}) ([A-Z]s)\\b", "$1$2");   // plural acronyms: "NBF Cs" -> "NBFCs"
        return out.replaceAll("\\s+", " ").trim();
    }
}
