package com.example.eveant.user.security;

import org.json.JSONObject;

public class JwtDecoder {

    public static String getClaim(String jwt, String key) {
        try {
            JSONObject payload = getPayload(jwt);
            return payload.optString(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isExpired(String jwt) {
        try {
            JSONObject payload = getPayload(jwt);
            long exp = payload.optLong("exp", 0L); // seconds since epoch
            if (exp == 0L) return false;
            long nowSec = System.currentTimeMillis() / 1000L;
            return nowSec >= exp;
        } catch (Exception e) {
            return true;
        }
    }

    private static JSONObject getPayload(String jwt) throws Exception {
        if (jwt == null) throw new IllegalArgumentException("jwt null");
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("not JWT");

        // Base64URL decode with padding fix
        String b64 = parts[1].replace('-', '+').replace('_', '/');
        int pad = (4 - (b64.length() % 4)) % 4;
        for (int i = 0; i < pad; i++) b64 += "=";

        String json = new String(android.util.Base64.decode(b64, android.util.Base64.DEFAULT),
                java.nio.charset.StandardCharsets.UTF_8);
        return new org.json.JSONObject(json);
    }
}
