package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Tile tátil estilo Stream Deck (suporta modo somente-ícone com PNG oficial do Windows,
 * fallback elegante com monograma, faixa inferior na cor do atalho e estado ativo).
 */
public class TouchActionTileView extends LinearLayout {

    private final FrameLayout iconHolder;
    private final ImageView iconImage;
    private final TextView fallbackMono;
    private final TextView labelView;

    private int accentBarColor = 0xFF2DD4BF;
    private boolean activeState = false;
    private boolean iconOnly = true;

    public TouchActionTileView(Context context) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setClickable(true);
        setPadding(d.dp(6), d.dp(8), d.dp(6), d.dp(10));

        iconHolder = new FrameLayout(context);
        int icoBox = d.dp(56);
        addView(iconHolder, new LayoutParams(icoBox, icoBox));

        iconImage = new ImageView(context);
        iconImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iconImage.setVisibility(GONE);
        FrameLayout.LayoutParams imgLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        int pad = d.dp(3);
        imgLp.setMargins(pad, pad, pad, pad);
        iconHolder.addView(iconImage, imgLp);

        fallbackMono = new TextView(context);
        fallbackMono.setGravity(Gravity.CENTER);
        fallbackMono.setTypeface(Typeface.DEFAULT_BOLD);
        fallbackMono.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        iconHolder.addView(fallbackMono, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        labelView = new TextView(context);
        labelView.setGravity(Gravity.CENTER);
        labelView.setSingleLine(true);
        labelView.setEllipsize(TextUtils.TruncateAt.END);
        labelView.setTypeface(Typeface.DEFAULT_BOLD);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        labelView.setVisibility(GONE);
        LayoutParams lblLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lblLp.topMargin = d.dp(4);
        addView(labelView, lblLp);

        applyTheme();
    }

    public TouchActionTileView setIconOnly(boolean only) {
        this.iconOnly = only;
        labelView.setVisibility(only ? GONE : VISIBLE);
        return this;
    }

    public TouchActionTileView setTile(String label, int accentColor) {
        this.accentBarColor = accentColor != 0 ? accentColor : AppTheme.get().colors().accent;
        labelView.setText(label == null ? "" : label);
        fallbackMono.setText(monogram(label));
        setContentDescription(label);
        applyTheme();
        return this;
    }

    public void setIconBitmap(Bitmap bmp) {
        if (bmp != null) {
            iconImage.setImageBitmap(bmp);
            iconImage.setVisibility(VISIBLE);
            fallbackMono.setVisibility(GONE);
        } else {
            iconImage.setVisibility(GONE);
            fallbackMono.setVisibility(VISIBLE);
        }
    }

    public void setActiveState(boolean active) {
        if (this.activeState != active) {
            this.activeState = active;
            applyTheme();
        }
    }

    public void applyTheme() {
        AppTheme t = AppTheme.get();
        ThemeColors c = t.colors();
        ThemeDimens d = t.dimens();
        int bg = activeState ? c.accentDim : c.cardHighlightBg;
        int border = activeState ? c.accent : c.border;
        setBackground(DrawableFactory.accentBottomTile(
                bg, c.surfacePressed, border, accentBarColor, d.strokePx, d.dp(4), d.dp(12)));
        fallbackMono.setTextColor(0xFFFFFFFF);
        fallbackMono.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.dp(10)));
        labelView.setTextColor(c.textPrimary);
    }

    public static String monogram(String text) {
        if (text == null) return "?";
        String clean = text.trim();
        if (clean.length() == 0) return "?";
        String[] parts = clean.split("\\s+");
        if (parts.length >= 2 && parts[0].length() > 0 && parts[1].length() > 0) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return clean.substring(0, Math.min(2, clean.length())).toUpperCase();
    }
}

