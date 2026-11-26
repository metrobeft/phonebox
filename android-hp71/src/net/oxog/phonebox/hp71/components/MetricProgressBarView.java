package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
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
 * Barra gráfica de métrica/progresso reutilizável (Discos, Cotas IA, Gauges de Clima, Processos).
 * Desenho nativo em Canvas com zero alocações no onDraw() e 100% API 17.
 */
public class MetricProgressBarView extends LinearLayout {

    private final LinearLayout topRow;
    private final TextView labelView;
    private final TextView valueView;
    private final BarCanvas barView;
    private final LinearLayout footerRow;
    private final TextView subLeftView;
    private final TextView subRightView;

    private int fixedBarColor = 0;
    private float warnThreshold = 80f;
    private float critThreshold = 92f;
    private float lastPct = -1f;

    public MetricProgressBarView(Context context) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setOrientation(VERTICAL);

        topRow = new LinearLayout(context);
        topRow.setOrientation(HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        labelView = new TextView(context);
        labelView.setTypeface(Typeface.DEFAULT_BOLD);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        labelView.setSingleLine(true);
        labelView.setEllipsize(TextUtils.TruncateAt.END);
        topRow.addView(labelView, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        valueView = new TextView(context);
        valueView.setTypeface(Typeface.DEFAULT_BOLD);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        valueView.setSingleLine(true);
        LayoutParams valLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        valLp.leftMargin = d.dp(6);
        topRow.addView(valueView, valLp);

        addView(topRow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        barView = new BarCanvas(context);
        LayoutParams barLp = new LayoutParams(LayoutParams.MATCH_PARENT, d.barHeight);
        barLp.topMargin = d.dp(4);
        barLp.bottomMargin = d.dp(3);
        addView(barView, barLp);

        footerRow = new LinearLayout(context);
        footerRow.setOrientation(HORIZONTAL);
        footerRow.setVisibility(GONE);

        subLeftView = new TextView(context);
        subLeftView.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.textMicroSp);
        subLeftView.setSingleLine(true);
        subLeftView.setEllipsize(TextUtils.TruncateAt.END);
        footerRow.addView(subLeftView, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        subRightView = new TextView(context);
        subRightView.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.textMicroSp);
        subRightView.setSingleLine(true);
        subRightView.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams srLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        srLp.leftMargin = d.dp(8);
        footerRow.addView(subRightView, srLp);

        addView(footerRow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        applyTheme();
    }

    public MetricProgressBarView setFixedColor(int color) {
        this.fixedBarColor = color;
        updateBarColors();
        return this;
    }

    public MetricProgressBarView setThresholds(float warnPct, float critPct) {
        this.warnThreshold = warnPct;
        this.critThreshold = critPct;
        return this;
    }

    public MetricProgressBarView setBarHeightDp(int dp) {
        LayoutParams lp = (LayoutParams) barView.getLayoutParams();
        lp.height = AppTheme.get().dimens().dp(dp);
        barView.setLayoutParams(lp);
        return this;
    }

    public void setMetric(String label, String valueText, float percent, String subLeft, String subRight) {
        String lbl = label == null ? "" : label;
        String val = valueText == null ? "" : valueText;
        boolean hasTop = lbl.length() > 0 || val.length() > 0;
        topRow.setVisibility(hasTop ? VISIBLE : GONE);
        if (hasTop) {
            labelView.setText(lbl);
            valueView.setText(val);
        }
        float clamped = Math.max(0f, Math.min(100f, Float.isNaN(percent) ? 0f : percent));
        this.lastPct = clamped;
        barView.setPercent(clamped);
        updateBarColors();

        boolean hasSub = (subLeft != null && subLeft.length() > 0) || (subRight != null && subRight.length() > 0);
        if (hasSub) {
            subLeftView.setText(subLeft == null ? "" : subLeft);
            subRightView.setText(subRight == null ? "" : subRight);
            footerRow.setVisibility(VISIBLE);
        } else {
            footerRow.setVisibility(GONE);
        }
    }

    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        labelView.setTextColor(c.textPrimary);
        subLeftView.setTextColor(c.textMuted);
        subRightView.setTextColor(c.textMuted);
        barView.setTrackColor(c.trackBg);
        updateBarColors();
    }

    private void updateBarColors() {
        ThemeColors c = AppTheme.get().colors();
        int fill;
        if (fixedBarColor != 0) {
            fill = fixedBarColor;
        } else if (lastPct >= critThreshold) {
            fill = c.red;
        } else if (lastPct >= warnThreshold) {
            fill = c.amber;
        } else {
            fill = c.accent;
        }
        barView.setFillColor(fill);
        valueView.setTextColor(fixedBarColor != 0 ? fixedBarColor : (lastPct >= critThreshold ? c.red : (lastPct >= warnThreshold ? c.amber : c.accent)));
    }

    private static final class BarCanvas extends View {
        private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint();
        private final Paint tipPaint = new Paint();
        private final Paint animeRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeSheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeTipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private float percent = 0f;

        BarCanvas(Context context) {
            super(context);
            float d = AppTheme.get().dimens().density;
            trackPaint.setStyle(Paint.Style.FILL);
            fillPaint.setStyle(Paint.Style.FILL);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(Math.max(1f, d * 0.9f));
            borderPaint.setColor(0xFF15693B);
            tipPaint.setStyle(Paint.Style.FILL);
            tipPaint.setColor(0xFFE8FFF0);
            animeRimPaint.setStyle(Paint.Style.STROKE);
            animeRimPaint.setStrokeWidth(Math.max(1f, d * 0.85f));
            animeRimPaint.setColor(0xAA6B4C9A);
            animeSheenPaint.setStyle(Paint.Style.FILL);
            animeSheenPaint.setColor(0x52FFFFFF);
            animeTipPaint.setStyle(Paint.Style.FILL);
            animeTipPaint.setColor(0xF0FFF5FD);
        }

        void setTrackColor(int color) {
            if (trackPaint.getColor() != color) {
                trackPaint.setColor(color);
                invalidate();
            }
        }

        void setFillColor(int color) {
            if (fillPaint.getColor() != color) {
                fillPaint.setColor(color);
                invalidate();
            }
        }

        void setPercent(float pct) {
            if (Math.abs(this.percent - pct) >= 0.05f) {
                this.percent = pct;
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            if (AppTheme.get().isHack()) {
                // Cyberdeck segmented tactical LED matrix bar
                float d = AppTheme.get().dimens().density;
                canvas.drawRect(0f, 0f, w, h, trackPaint);
                canvas.drawRect(0.5f, 0.5f, w - 0.5f, h - 0.5f, borderPaint);

                float pad = Math.max(1f, 1.5f * d);
                float innerW = w - pad * 2f;
                if (innerW > 4f && percent > 0.1f) {
                    float segUnit = Math.max(4f, 5.5f * d);
                    int totalSegs = Math.max(4, (int) (innerW / segUnit));
                    float step = innerW / (float) totalSegs;
                    float gap = Math.max(1f, 1.5f * d);
                    int litSegs = Math.max(1, Math.min(totalSegs, Math.round((totalSegs * percent) / 100f)));
                    for (int i = 0; i < litSegs; i++) {
                        float x0 = pad + i * step;
                        float x1 = Math.min(w - pad, x0 + Math.max(1.5f, step - gap));
                        Paint p = (i == litSegs - 1 && litSegs < totalSegs) ? tipPaint : fillPaint;
                        canvas.drawRect(x0, pad, x1, h - pad, p);
                    }
                }
                return;
            }

            float r = h * 0.5f;
            boolean anime = AppTheme.get().isAnime();
            rect.set(0f, 0f, w, h);
            canvas.drawRoundRect(rect, r, r, trackPaint);
            if (anime) {
                rect.set(0.5f, 0.5f, w - 0.5f, h - 0.5f);
                canvas.drawRoundRect(rect, r, r, animeRimPaint);
            }
            if (percent > 0.1f) {
                float fw = Math.max(h, (w * percent) / 100f);
                if (fw > w) fw = w;
                rect.set(0f, 0f, fw, h);
                canvas.drawRoundRect(rect, r, r, fillPaint);
                if (anime && fw > h * 0.8f) {
                    // Cel-shaded top glossy sheen + glowing starlight tip
                    rect.set(h * 0.2f, h * 0.12f, Math.max(h * 0.5f, fw - h * 0.2f), h * 0.48f);
                    canvas.drawRoundRect(rect, r * 0.45f, r * 0.45f, animeSheenPaint);
                    canvas.drawCircle(fw - h * 0.42f, h * 0.5f, Math.max(1.5f, h * 0.22f), animeTipPaint);
                }
            }
        }
    }
}

