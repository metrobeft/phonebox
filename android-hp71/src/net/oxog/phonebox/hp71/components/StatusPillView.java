package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Badge/pílula reutilizável para estados (ONLINE, FALANDO, MUDO, CHOVENDO, MIC/CAM, planos de IA).
 */
public class StatusPillView extends TextView {

    public static final int STYLE_NEUTRAL = 0;
    public static final int STYLE_ACCENT = 1;
    public static final int STYLE_SUCCESS = 2;
    public static final int STYLE_INFO = 3;
    public static final int STYLE_WARN = 4;
    public static final int STYLE_DANGER = 5;

    private int style = -1;
    private String lastText = null;
    private int appliedStyle = -1;

    public StatusPillView(Context context) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setGravity(Gravity.CENTER);
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.END);
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, d.textMicroSp);
        setPadding(d.dp(8), d.dp(3), d.dp(8), d.dp(3));
        this.style = STYLE_NEUTRAL;
        applyTheme();
    }

    public StatusPillView setPill(String text, int style) {
        String safe = text == null ? "" : text;
        if (!safe.equals(lastText)) {
            lastText = safe;
            setText(safe);
        }
        this.style = style;
        if (appliedStyle != style) {
            applyTheme();
        }
        return this;
    }

    public void applyTheme() {
        AppTheme t = AppTheme.get();
        ThemeColors c = t.colors();
        appliedStyle = style;
        int bg;
        int stroke;
        int fg;
        switch (style) {
            case STYLE_ACCENT:
                bg = c.accentDim;
                stroke = c.accent;
                fg = c.accent;
                break;
            case STYLE_SUCCESS:
                bg = c.greenDim;
                stroke = c.green;
                fg = c.isHack ? 0xFF00FF66 : (c.isAnime ? c.green : 0xFF86EFAC);
                break;
            case STYLE_INFO:
                bg = c.skyDim;
                stroke = c.sky;
                fg = c.isHack ? 0xFF00F0FF : (c.isAnime ? c.sky : 0xFF7DD3FC);
                break;
            case STYLE_WARN:
                bg = c.amberDim;
                stroke = c.amber;
                fg = c.isHack ? 0xFFFFD166 : (c.isAnime ? c.amber : 0xFFFDE68A);
                break;
            case STYLE_DANGER:
                bg = c.redDim;
                stroke = c.red;
                fg = c.isHack ? 0xFFFF6B8A : (c.isAnime ? c.red : 0xFFFEE2E2);
                break;
            default:
                bg = c.surfaceControl;
                stroke = c.border;
                fg = c.textSecondary;
                break;
        }
        setTextColor(fg);
        setBackground(DrawableFactory.pillBackground(t, bg, stroke));
    }
}
