package net.oxog.phonebox.hp71.theme;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;

import net.oxog.phonebox.hp71.theme.anime.AnimeButtonDrawable;
import net.oxog.phonebox.hp71.theme.anime.AnimeCardDrawable;
import net.oxog.phonebox.hp71.theme.anime.AnimeTileDrawable;
import net.oxog.phonebox.hp71.theme.hack.HackButtonDrawable;
import net.oxog.phonebox.hp71.theme.hack.HackCardDrawable;
import net.oxog.phonebox.hp71.theme.hack.HackTileDrawable;

/**
 * Fábrica central de drawables 100% compatível com Android 4.2.2 (API 17).
 * No tema Hack usa geometria Cyberdeck chanfrada; nos mundos vivos (Sakura, Lofi, Starfall, Miku)
 * aplica cards translúcidos cristalinos com brilho superior e detalhes no tom exato de cada mundo.
 */
public final class DrawableFactory {

    private DrawableFactory() { }

    public static GradientDrawable roundedRect(int fillColor, int strokeColor, int strokePx, float radiusPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        gd.setCornerRadius(radiusPx);
        if (strokePx > 0 && strokeColor != 0) {
            gd.setStroke(strokePx, strokeColor);
        }
        return gd;
    }

    public static Drawable cardBackground(AppTheme theme, int variant) {
        ThemeColors c = theme.colors();
        if (theme.isHack()) {
            float d = theme.dimens().density;
            if (variant == 1) {
                return new HackCardDrawable(c.cardHighlightBg, c.accent, c.accent, c.glitchCyan, d, true);
            }
            if (variant == 2) {
                return new HackCardDrawable(c.cardAltBg, c.border, c.textMuted, c.accent, d, false);
            }
            if (variant == 3) {
                return new HackCardDrawable(0xD8052613, c.green, c.green, c.glitchCyan, d, true);
            }
            return new HackCardDrawable(c.cardBg, c.border, c.accent, c.accent, d, false);
        }
        if (theme.isAnime()) {
            float d = theme.dimens().density;
            if (variant == 1) {
                return new AnimeCardDrawable(c.cardHighlightBg, c.accent, c.accent, c.starGold,
                        c.worldSheenHighlight, c.worldRimHighlight, d, true);
            }
            if (variant == 2) {
                return new AnimeCardDrawable(c.cardAltBg, c.border, c.purple, c.starGold,
                        c.worldSheen, c.worldRim, d, false);
            }
            if (variant == 3) {
                return new AnimeCardDrawable(c.greenDim, c.green, c.green, c.starGold,
                        c.worldSheenHighlight, c.worldRimHighlight, d, true);
            }
            return new AnimeCardDrawable(c.cardBg, c.border, c.accent, c.starGold,
                    c.worldSheen, c.worldRim, d, false);
        }
        int stroke = theme.isBorderlessCards() ? 0 : theme.dimens().strokePx;
        float r = theme.effectiveCardRadiusPx();
        if (variant == 1) {
            return roundedRect(c.cardHighlightBg, c.accent, stroke, r);
        }
        if (variant == 2) {
            return roundedRect(c.cardAltBg, c.border, stroke, r);
        }
        if (variant == 3) {
            return roundedRect(ThemeColors.blend(c.green, c.cardBg, 0.14f), c.green, stroke, r);
        }
        return roundedRect(c.cardBg, c.border, stroke, r);
    }

    public static Drawable pillBackground(AppTheme theme, int fillColor, int strokeColor) {
        ThemeDimens d = theme.dimens();
        if (theme.isHack()) {
            return new HackButtonDrawable(fillColor, strokeColor, strokeColor, d.density, false, true);
        }
        if (theme.isAnime()) {
            ThemeColors c = theme.colors();
            return new AnimeButtonDrawable(fillColor, strokeColor, c.starGold, c.worldRim, d.density, false, true);
        }
        return roundedRect(fillColor, strokeColor, d.strokePx, d.pillRadius);
    }

    public static StateListDrawable touchableRounded(
            int normalBg, int pressedBg, int strokeColor, int pressedStroke, int strokePx, float radiusPx) {
        AppTheme t = AppTheme.get();
        if (t != null && t.isHack()) {
            float d = t.dimens().density;
            StateListDrawable sld = new StateListDrawable();
            Drawable pressed = new HackButtonDrawable(pressedBg, pressedStroke, t.colors().glitchCyan, d, true, false);
            Drawable normal = new HackButtonDrawable(normalBg, strokeColor, strokeColor, d, false, false);
            sld.addState(new int[] { android.R.attr.state_pressed }, pressed);
            sld.addState(new int[] { android.R.attr.state_selected }, pressed);
            sld.addState(new int[] { }, normal);
            return sld;
        }
        if (t != null && t.isAnime()) {
            float d = t.dimens().density;
            ThemeColors c = t.colors();
            StateListDrawable sld = new StateListDrawable();
            Drawable pressed = new AnimeButtonDrawable(pressedBg, pressedStroke, c.starGold, c.worldRimHighlight, d, true, false);
            Drawable normal = new AnimeButtonDrawable(normalBg, strokeColor, c.accent, c.worldRim, d, false, false);
            sld.addState(new int[] { android.R.attr.state_pressed }, pressed);
            sld.addState(new int[] { android.R.attr.state_selected }, pressed);
            sld.addState(new int[] { }, normal);
            return sld;
        }
        StateListDrawable sld = new StateListDrawable();
        GradientDrawable pressed = roundedRect(pressedBg, pressedStroke, strokePx, radiusPx);
        GradientDrawable normal = roundedRect(normalBg, strokeColor, strokePx, radiusPx);
        sld.addState(new int[] { android.R.attr.state_pressed }, pressed);
        sld.addState(new int[] { android.R.attr.state_selected }, pressed);
        sld.addState(new int[] { }, normal);
        return sld;
    }

    public static StateListDrawable accentBottomTile(
            int normalBg, int pressedBg, int borderColor, int accentBarColor, int strokePx, int barHeightPx, float radiusPx) {
        AppTheme t = AppTheme.get();
        if (t != null && t.isHack()) {
            float d = t.dimens().density;
            StateListDrawable sld = new StateListDrawable();
            Drawable pressed = new HackTileDrawable(pressedBg, t.colors().glitchCyan, accentBarColor, d, true);
            Drawable normal = new HackTileDrawable(normalBg, borderColor, accentBarColor, d, false);
            sld.addState(new int[] { android.R.attr.state_pressed }, pressed);
            sld.addState(new int[] { }, normal);
            return sld;
        }
        if (t != null && t.isAnime()) {
            float d = t.dimens().density;
            ThemeColors c = t.colors();
            StateListDrawable sld = new StateListDrawable();
            Drawable pressed = new AnimeTileDrawable(pressedBg, c.accent, accentBarColor, c.worldSheenHighlight, c.starGold, d, true);
            Drawable normal = new AnimeTileDrawable(normalBg, borderColor, accentBarColor, c.worldRim, c.accent, d, false);
            sld.addState(new int[] { android.R.attr.state_pressed }, pressed);
            sld.addState(new int[] { }, normal);
            return sld;
        }
        Drawable normal = buildBottomBarLayer(normalBg, borderColor, accentBarColor, strokePx, barHeightPx, radiusPx);
        Drawable pressed = buildBottomBarLayer(pressedBg, accentBarColor, accentBarColor, strokePx, barHeightPx, radiusPx);
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[] { android.R.attr.state_pressed }, pressed);
        sld.addState(new int[] { }, normal);
        return sld;
    }

    private static Drawable buildBottomBarLayer(
            int bg, int border, int barColor, int strokePx, int barHeightPx, float radiusPx) {
        GradientDrawable accentBase = roundedRect(barColor, 0, 0, radiusPx);
        GradientDrawable face = roundedRect(bg, border, strokePx, radiusPx);
        LayerDrawable ld = new LayerDrawable(new Drawable[] { accentBase, face });
        ld.setLayerInset(1, 0, 0, 0, barHeightPx);
        return ld;
    }

    public static GradientDrawable circle(int fillColor, int strokeColor, int strokePx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(fillColor);
        if (strokePx > 0 && strokeColor != 0) {
            gd.setStroke(strokePx, strokeColor);
        }
        return gd;
    }
}
