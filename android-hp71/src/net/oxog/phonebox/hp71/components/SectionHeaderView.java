package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Cabeçalho modular de seção com título, subtítulo/contador e slot opcional à direita.
 * No tema Hack, exibe prompt terminal (">_") e régua luminosa inferior.
 * No tema Anime, exibe gema estelar dourada (Canvas Path 4 pontas) e trilha luminosa sakura.
 */
public class SectionHeaderView extends LinearLayout {

    private final TextView promptView;
    private final StarGemView starGemView;
    private final TextView titleView;
    private final TextView metaView;
    private final LinearLayout rightSlot;
    private final Paint hackRulePaint = new Paint();
    private final Paint hackAccentPaint = new Paint();
    private final Paint animeRulePaint = new Paint();
    private final Paint animeAccentPaint = new Paint();
    private final Paint animeGoldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SectionHeaderView(Context context) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(0, 0, 0, d.spaceSm);
        setWillNotDraw(false);

        hackRulePaint.setStyle(Paint.Style.FILL);
        hackRulePaint.setColor(0x6615693B);
        hackAccentPaint.setStyle(Paint.Style.FILL);
        hackAccentPaint.setColor(0xFF00FF66);
        animeRulePaint.setStyle(Paint.Style.FILL);
        animeRulePaint.setColor(0x666B4C9A);
        animeAccentPaint.setStyle(Paint.Style.FILL);
        animeAccentPaint.setColor(0xCCFF7ED4);
        animeGoldPaint.setStyle(Paint.Style.FILL);
        animeGoldPaint.setColor(0xFFFFD56B);

        LinearLayout left = new LinearLayout(context);
        left.setOrientation(HORIZONTAL);
        left.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams leftLp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        left.setLayoutParams(leftLp);

        promptView = new TextView(context);
        promptView.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        promptView.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.textLabelSp);
        promptView.setText(">_");
        promptView.setVisibility(GONE);
        LayoutParams prLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        prLp.rightMargin = d.dp(5);
        left.addView(promptView, prLp);

        starGemView = new StarGemView(context);
        starGemView.setVisibility(GONE);
        LayoutParams sgLp = new LayoutParams(d.dp(11), d.dp(11));
        sgLp.rightMargin = d.dp(5);
        left.addView(starGemView, sgLp);

        titleView = new TextView(context);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.textLabelSp);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        left.addView(titleView);

        metaView = new TextView(context);
        metaView.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.textMicroSp);
        metaView.setSingleLine(true);
        metaView.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams metaLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        metaLp.leftMargin = d.dp(8);
        metaView.setLayoutParams(metaLp);
        metaView.setVisibility(GONE);
        left.addView(metaView);

        rightSlot = new LinearLayout(context);
        rightSlot.setOrientation(HORIZONTAL);
        rightSlot.setGravity(Gravity.CENTER_VERTICAL);
        rightSlot.setVisibility(GONE);

        addView(left);
        addView(rightSlot);
        applyTheme();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        AppTheme t = AppTheme.get();
        if (t.isHack()) {
            int w = getWidth();
            int h = getHeight();
            float d = t.dimens().density;
            float y = h - Math.max(1.5f, 2f * d);
            canvas.drawRect(0f, y, w, y + Math.max(1f, d * 0.8f), hackRulePaint);
            canvas.drawRect(0f, y - 0.5f * d, Math.min(w, 26f * d), y + Math.max(1.5f, 1.6f * d), hackAccentPaint);
        } else if (t.isAnime()) {
            int w = getWidth();
            int h = getHeight();
            float d = t.dimens().density;
            float y = h - Math.max(1.5f, 2f * d);
            canvas.drawRect(0f, y, w, y + Math.max(1f, d * 0.8f), animeRulePaint);
            float seg = Math.min(w, 34f * d);
            canvas.drawRect(0f, y - 0.4f * d, seg, y + Math.max(1.5f, 1.5f * d), animeAccentPaint);
            if (seg + 5f * d < w) {
                canvas.drawCircle(seg + 4.5f * d, y + 0.5f * d, 1.6f * d, animeGoldPaint);
            }
        }
    }

    public SectionHeaderView setTitle(String title) {
        titleView.setText(title == null ? "" : title.toUpperCase());
        return this;
    }

    public SectionHeaderView setMeta(String meta) {
        if (meta == null || meta.length() == 0) {
            metaView.setVisibility(GONE);
        } else {
            metaView.setText(meta);
            metaView.setVisibility(VISIBLE);
        }
        return this;
    }

    public SectionHeaderView setRightView(View v) {
        rightSlot.removeAllViews();
        if (v != null) {
            rightSlot.addView(v);
            rightSlot.setVisibility(VISIBLE);
        } else {
            rightSlot.setVisibility(GONE);
        }
        return this;
    }

    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        animeRulePaint.setColor(ThemeColors.withAlpha(c.border, 0x66));
        animeAccentPaint.setColor(ThemeColors.withAlpha(c.accent, 0xCC));
        animeGoldPaint.setColor(c.starGold);
        starGemView.setColor(c.starGold);
        promptView.setTextColor(c.glitchCyan);
        promptView.setVisibility(c.isHack ? VISIBLE : GONE);
        starGemView.setVisibility(c.isAnime ? VISIBLE : GONE);
        titleView.setTextColor((c.isHack || c.isAnime) ? c.accent : c.textMuted);
        metaView.setTextColor(c.textSecondary);
        invalidate();
    }

    private static final class StarGemView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        StarGemView(Context context) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFD56B);
        }

        void setColor(int color) {
            paint.setColor(color);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float r = Math.min(w, h) * 0.48f;
            if (r <= 1f) return;
            float cx = w * 0.5f;
            float cy = h * 0.5f;
            float in = r * 0.28f;
            path.reset();
            path.moveTo(cx, cy - r);
            path.lineTo(cx + in, cy - in);
            path.lineTo(cx + r, cy);
            path.lineTo(cx + in, cy + in);
            path.lineTo(cx, cy + r);
            path.lineTo(cx - in, cy + in);
            path.lineTo(cx - r, cy);
            path.lineTo(cx - in, cy - in);
            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
