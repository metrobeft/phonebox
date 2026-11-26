package net.oxog.phonebox.hp71.theme;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador central do tema ativo.
 * Permite trocar preset (default/hack/anime/amoled/midnight/warm/graphite), cor de destaque (accent)
 * e raio de bordas em tempo real com notificação imediata a toda a árvore de UI.
 */
public final class AppTheme {

    public interface ThemeListener {
        void onThemeChanged(AppTheme theme);
    }

    private static AppTheme instance;

    private ThemeColors colors;
    private final ThemeDimens dimens;
    private int customCardRadiusDp = -1;
    private boolean borderlessCards = false;
    private final List<ThemeListener> listeners = new ArrayList<ThemeListener>();

    private AppTheme(Context ctx) {
        this.dimens = new ThemeDimens(ctx.getApplicationContext());
        this.colors = ThemeColors.forPreset("default", null);
    }

    public static synchronized AppTheme init(Context ctx) {
        if (instance == null) {
            instance = new AppTheme(ctx);
        }
        return instance;
    }

    public static AppTheme get() {
        return instance;
    }

    public ThemeColors colors() {
        return colors;
    }

    public ThemeDimens dimens() {
        return dimens;
    }

    public boolean isHack() {
        return colors != null && colors.isHack;
    }

    public boolean isAnime() {
        return colors != null && colors.isAnime;
    }

    public int effectiveCardRadiusPx() {
        if (isHack()) return 0;
        if (isAnime()) return dimens.dp(12);
        if (customCardRadiusDp >= 0) return dimens.dp(customCardRadiusDp);
        return dimens.cardRadius;
    }

    public boolean isBorderlessCards() {
        return !isHack() && !isAnime() && borderlessCards;
    }

    public boolean update(String presetId, String accentHex, int cardRadiusDp, boolean borderless) {
        ThemeColors next = ThemeColors.forPreset(presetId, accentHex);
        boolean same = next.id.equals(colors.id)
                && next.accent == colors.accent
                && customCardRadiusDp == cardRadiusDp
                && borderlessCards == borderless;
        if (same) return false;
        this.colors = next;
        this.customCardRadiusDp = cardRadiusDp;
        this.borderlessCards = borderless;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onThemeChanged(this);
        }
        return true;
    }

    public void addListener(ThemeListener l) {
        if (l != null && !listeners.contains(l)) listeners.add(l);
    }

    public void removeListener(ThemeListener l) {
        listeners.remove(l);
    }
}
