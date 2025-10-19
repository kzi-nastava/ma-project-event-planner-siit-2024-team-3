package com.example.eveant.event.eventDetails.utils;

import android.os.Build;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeFmt {
    private static final Pattern DATE_RE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern TIME_RE = Pattern.compile("(\\d{2}:\\d{2})");

    public static String extractDate(String s) {
        if (TextUtils.isEmpty(s)) return "—";
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                return odt.toLocalDate().toString();
            } catch (Exception ignored) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    return ldt.toLocalDate().toString();
                } catch (Exception ignored2) { }
            }
        }
        Matcher m = DATE_RE.matcher(s);
        return m.find() ? m.group(1) : "—";
    }

    public static String extractTime(String s) {
        if (TextUtils.isEmpty(s)) return "—";
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                return odt.toLocalTime().toString().substring(0,5);
            } catch (Exception ignored) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    return ldt.toLocalTime().toString().substring(0,5);
                } catch (Exception ignored2) { }
            }
        }
        Matcher m = TIME_RE.matcher(s);
        return m.find() ? m.group(1) : "—";
    }

    public static long parseMinutes(String iso) {
        if (iso == null) return Long.MAX_VALUE;
        try {
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            if (t > 0 && iso.length() >= t + 5) {
                int h = Integer.parseInt(iso.substring(t + 1, t + 3));
                int m = Integer.parseInt(iso.substring(t + 4, t + 6));
                return h * 60L + m;
            }
        } catch (Exception ignored) {}
        return Long.MAX_VALUE;
    }

    public static String formatHHmm(String iso) {
        if (iso == null) return "—";
        try {
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            if (t > 0 && iso.length() >= t + 5) return iso.substring(t + 1, t + 6);
            return iso;
        } catch (Exception e) { return "—"; }
    }
}
