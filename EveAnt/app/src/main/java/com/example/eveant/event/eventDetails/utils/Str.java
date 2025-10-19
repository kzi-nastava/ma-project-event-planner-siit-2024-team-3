package com.example.eveant.event.eventDetails.utils;

public class Str {
    public static String nz(String s){ return s==null? "" : s; }
    public static String nz(String s, String def){ return (s==null || s.isEmpty()) ? def : s; }
    public static String trim(String s){ return s==null ? "" : s.trim(); }

    public static String join(String sep, String... parts){
        StringBuilder sb=new StringBuilder();
        for (String p:parts) if (p!=null && !p.trim().isEmpty()){ if (sb.length()>0) sb.append(sep); sb.append(p.trim()); }
        return sb.toString();
    }

    public static String safeFileName(String s){ return s.replaceAll("[\\\\/:*?\"<>|]", "_").trim(); }
}
