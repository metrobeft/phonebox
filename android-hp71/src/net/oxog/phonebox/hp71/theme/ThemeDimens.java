package net.oxog.phonebox.hp71.theme;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Escalas, espaçamentos, raios de borda e tipografia centralizados.
 * Ajustado para HP 7.1 1201 (7" 1024x600 mdpi / ~170 dpi).
 */
public final class ThemeDimens {

    public final float density;
    public final int spaceXs;
    public final int spaceSm;
    public final int spaceMd;
    public final int spaceLg;
    public final int cardPaddingH;
    public final int cardPaddingV;
    public final int cardRadius;
    public final int controlRadius;
    public final int pillRadius;
    public final int strokePx;
    public final int barHeight;
    public final int topBarHeight;
    public final int navBarHeight;
    public final int touchMinHeight;

    public final float textMicroSp;
    public final float textLabelSp;
    public final float textBodySp;
    public final float textTitleSp;
    public final float textMetricSp;
    public final float textHeroSp;

    public ThemeDimens(Context ctx) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        this.density = dm.density > 0f ? dm.density : 1f;
        this.spaceXs = dp(4);
        this.spaceSm = dp(8);
        this.spaceMd = dp(9);
        this.spaceLg = dp(12);
        this.cardPaddingH = dp(13);
        this.cardPaddingV = dp(8);
        this.cardRadius = dp(4);
        this.controlRadius = dp(4);
        this.pillRadius = dp(4);
        this.strokePx = Math.max(1, dp(1));
        this.barHeight = dp(7);
        // Otimizado para Android 4.2.2 com barra fixa preta do sistema (~48dp) sempre visível:
        // em 1024x600 -> janela real ~1024x552dp -> área útil ~1024x466dp.
        this.topBarHeight = dp(36);
        this.navBarHeight = dp(50);
        this.touchMinHeight = dp(48);

        this.textMicroSp = 11.5f;
        this.textLabelSp = 12.5f;
        this.textBodySp = 13.5f;
        this.textTitleSp = 16f;
        this.textMetricSp = 24f;
        this.textHeroSp = 54f;
    }

    public int dp(float value) {
        return Math.round(value * density);
    }
}

