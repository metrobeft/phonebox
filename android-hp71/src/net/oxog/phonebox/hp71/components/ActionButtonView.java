package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Botão tátil modular com variantes (Normal, Primary/Accent, Active, Warn, Danger)
 * e suporte embutido a confirmação em 2 toques (usado em Processos, Discord, Mídia, etc.).
 */
public class ActionButtonView extends TextView {

    public static final int VARIANT_NORMAL = 0;
    public static final int VARIANT_PRIMARY = 1;
    public static final int VARIANT_ACTIVE = 2;
    public static final int VARIANT_WARN = 3;
    public static final int VARIANT_DANGER = 4;

    private int variant = VARIANT_NORMAL;
    private int appliedEffVariant = -1;
    private boolean twoTapConfirm = false;
    private String baseLabel = "";
    private String confirmLabel = "Confirmar?";
    private long confirmUntilMs = 0L;
    private OnClickListener confirmedListener;

    public ActionButtonView(Context context) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setGravity(Gravity.CENTER);
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.END);
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        setPadding(d.dp(10), d.dp(6), d.dp(10), d.dp(6));
        setMinHeight(d.dp(32));
        setClickable(true);
        applyTheme();
    }

    public ActionButtonView setButton(String label, int variant) {
        String safe = label == null ? "" : label;
        boolean textChanged = !safe.equals(this.baseLabel);
        this.baseLabel = safe;
        this.variant = variant;
        if (!isConfirmPending() && (textChanged || !safe.equals(getText().toString()))) {
            setText(this.baseLabel);
        }
        int eff = isConfirmPending() ? VARIANT_DANGER : variant;
        if (eff != appliedEffVariant) {
            applyTheme();
        }
        return this;
    }

    public ActionButtonView setCompact() {
        ThemeDimens d = AppTheme.get().dimens();
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        setPadding(d.dp(7), d.dp(4), d.dp(7), d.dp(4));
        setMinHeight(d.dp(28));
        return this;
    }

    public ActionButtonView setTwoTapConfirm(String confirmText, OnClickListener onConfirmed) {
        this.twoTapConfirm = true;
        this.confirmLabel = confirmText != null ? confirmText : "Confirmar?";
        this.confirmedListener = onConfirmed;
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now < confirmUntilMs) {
                    confirmUntilMs = 0L;
                    setText(baseLabel);
                    applyTheme();
                    if (confirmedListener != null) confirmedListener.onClick(v);
                } else {
                    confirmUntilMs = now + 4500L;
                    setText(confirmLabel);
                    applyTheme();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (System.currentTimeMillis() >= confirmUntilMs && confirmUntilMs > 0L) {
                                confirmUntilMs = 0L;
                                setText(baseLabel);
                                applyTheme();
                            }
                        }
                    }, 4600L);
                }
            }
        });
        return this;
    }

    private boolean isConfirmPending() {
        return twoTapConfirm && System.currentTimeMillis() < confirmUntilMs;
    }

    public void applyTheme() {
        AppTheme t = AppTheme.get();
        ThemeColors c = t.colors();
        ThemeDimens d = t.dimens();
        int effVariant = isConfirmPending() ? VARIANT_DANGER : variant;
        appliedEffVariant = effVariant;
        int bg;
        int pressed;
        int stroke;
        int fg;
        switch (effVariant) {
            case VARIANT_PRIMARY:
                bg = c.accent;
                pressed = ThemeColors.blend(0xFFFFFFFF, c.accent, 0.2f);
                stroke = c.accent;
                fg = c.textOnAccent;
                break;
            case VARIANT_ACTIVE:
                bg = c.accentDim;
                pressed = c.surfacePressed;
                stroke = c.accent;
                fg = c.accent;
                break;
            case VARIANT_WARN:
                bg = c.amberDim;
                pressed = c.amber;
                stroke = c.amber;
                fg = 0xFFFDE68A;
                break;
            case VARIANT_DANGER:
                bg = c.redDim;
                pressed = c.red;
                stroke = c.red;
                fg = 0xFFFFFFFF;
                break;
            default:
                bg = c.surfaceControl;
                pressed = c.surfacePressed;
                stroke = c.border;
                fg = c.textPrimary;
                break;
        }
        setTextColor(fg);
        setBackground(DrawableFactory.touchableRounded(bg, pressed, stroke, c.accent, d.strokePx, d.controlRadius));
    }
}

