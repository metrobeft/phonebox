package net.oxog.phonebox.hp71.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preferências persistentes do aplicativo em SharedPreferences.
 */
public final class AppPrefs {

    public static final String DEFAULT_HOST = "192.168.1.100";
    public static final int DEFAULT_PORT = 8788;

    public static final String ORIENT_AUTO = "auto";
    public static final String ORIENT_LANDSCAPE = "landscape";
    public static final String ORIENT_PORTRAIT = "portrait";

    private static final String PREFS_NAME = "phonebox_hp71_prefs";
    private final SharedPreferences sp;

    public AppPrefs(Context ctx) {
        this.sp = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getHost() {
        String h = sp.getString("host", DEFAULT_HOST);
        return (h == null || h.trim().length() == 0) ? DEFAULT_HOST : h.trim();
    }

    public int getPort() {
        int p = sp.getInt("port", DEFAULT_PORT);
        return (p < 1 || p > 65535) ? DEFAULT_PORT : p;
    }

    public String getBaseUrl() {
        return "http://" + getHost() + ":" + getPort();
    }

    public void setServer(String host, int port) {
        String cleanHost = (host == null || host.trim().length() == 0) ? DEFAULT_HOST : host.trim();
        cleanHost = cleanHost.replace("http://", "").replace("https://", "");
        int slash = cleanHost.indexOf('/');
        if (slash > 0) cleanHost = cleanHost.substring(0, slash);
        int validPort = (port >= 1 && port <= 65535) ? port : DEFAULT_PORT;
        sp.edit().putString("host", cleanHost).putInt("port", validPort).apply();
    }

    public String getOrientMode() {
        return sp.getString("orient", ORIENT_AUTO);
    }

    public void setOrientMode(String mode) {
        sp.edit().putString("orient", mode).apply();
    }

    public String getLocalTheme() {
        return sp.getString("local_theme", "default");
    }

    public void setLocalTheme(String id) {
        sp.edit().putString("local_theme", id).apply();
    }

    public boolean themeEffectsEnabled() {
        return sp.getBoolean("theme_effects", true);
    }

    public void setThemeEffectsEnabled(boolean enabled) {
        sp.edit().putBoolean("theme_effects", enabled).apply();
    }

    public boolean isAutoCheckUpdates() {
        return sp.getBoolean("auto_check_updates", true);
    }

    public void setAutoCheckUpdates(boolean enabled) {
        sp.edit().putBoolean("auto_check_updates", enabled).apply();
    }

    public int getLastPage() {
        return sp.getInt("last_page", 0);
    }

    public void setLastPage(int page) {
        sp.edit().putInt("last_page", page).apply();
    }

    /** Brilho desejado: 0.02f a 1.0f (ou -1f para automático do sistema). */
    public float getBrightness() {
        return sp.getFloat("brightness", 1.0f);
    }

    public void setBrightness(float b) {
        sp.edit().putFloat("brightness", b).apply();
    }

    /** True = botões físicos de volume trocam abas; False = controlam volume do PC. */
    public boolean isVolumeKeysSwitchTabs() {
        return sp.getBoolean("vol_keys_tabs", true);
    }

    public void setVolumeKeysSwitchTabs(boolean switchTabs) {
        sp.edit().putBoolean("vol_keys_tabs", switchTabs).apply();
    }
}

