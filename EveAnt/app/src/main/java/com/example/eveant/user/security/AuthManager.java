package com.example.eveant.user.security;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF = "UserSession";
    private static final String KEY_TOKEN = "token";

    private static volatile AuthManager instance;
    private final SharedPreferences prefs;

    private AuthManager(Context ctx) {
        this.prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static AuthManager getInstance(Context ctx) {
        if (instance == null) {
            synchronized (AuthManager.class) {
                if (instance == null) instance = new AuthManager(ctx);
            }
        }
        return instance;
    }

    // --- Token CRUD ---
    public void saveToken(String jwt) {
        prefs.edit().putString(KEY_TOKEN, jwt == null ? "" : jwt).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public void clear() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    // --- Claims helpers ---
    public String getEmail() {
        return JwtDecoder.getClaim(getToken(), "sub"); // adjust if your claim key is different
    }

    public String getRole() {
        return JwtDecoder.getClaim(getToken(), "role");
    }

    public boolean isLoggedIn() {
        String t = getToken();
        return t != null && !t.isEmpty() && !JwtDecoder.isExpired(t);
    }
}
