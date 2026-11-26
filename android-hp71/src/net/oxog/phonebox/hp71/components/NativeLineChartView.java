package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;

/**
 * Gráfico de linha nativo em Canvas reutilizável (1 ou 2 séries, preenchimento translúcido,
 * grade 25/50/75%, modo sparkline ou auto-escala).
 * Zero alocações em onDraw() e 100% compatível com Android 4.2.2 (API 17).
 */
public class NativeLineChartView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint line1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint();
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final RectF bgRect = new RectF();
    private static final String[] GRID_TICKS = { "75", "50", "25" };

    private AppState.FloatHistory hist1;
    private AppState.FloatHistory hist2;
    private float[] arraySeries;
    private int[] arrayBars;
    private String[] arrayLabels;
    private boolean autoScale = false;
    private boolean sparkline = false;
    private boolean showGrid = true;
    private int color1 = 0xFF2DD4BF;
    private int color2 = 0xFFA78BFA;
    private float cornerRadiusPx;

    public NativeLineChartView(Context context) {
        super(context);
        float d = AppTheme.get().dimens().density;
        cornerRadiusPx = 6f * d;

        bgPaint.setStyle(Paint.Style.FILL);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, d));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(Math.max(1f, d * 0.8f));

        line1Paint.setStyle(Paint.Style.STROKE);
        line1Paint.setStrokeWidth(2.2f * d);
        line1Paint.setStrokeCap(Paint.Cap.ROUND);
        line1Paint.setStrokeJoin(Paint.Join.ROUND);

        glow1Paint.setStyle(Paint.Style.STROKE);
        glow1Paint.setStrokeWidth(5.2f * d);
        glow1Paint.setStrokeCap(Paint.Cap.ROUND);
        glow1Paint.setStrokeJoin(Paint.Join.ROUND);

        fill1Paint.setStyle(Paint.Style.FILL);

        line2Paint.setStyle(Paint.Style.STROKE);
        line2Paint.setStrokeWidth(2.0f * d);
        line2Paint.setStrokeCap(Paint.Cap.ROUND);
        line2Paint.setStrokeJoin(Paint.Join.ROUND);

        barPaint.setStyle(Paint.Style.FILL);
        dotPaint.setStyle(Paint.Style.FILL);

        labelPaint.setTextSize(10f * d);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        axisPaint.setTextSize(9f * d);
        axisPaint.setTextAlign(Paint.Align.LEFT);
        applyTheme();
    }

    public NativeLineChartView setColors(int primaryColor, int secondaryColor) {
        this.color1 = primaryColor;
        this.color2 = secondaryColor;
        AppTheme t = AppTheme.get();
        line1Paint.setColor(color1);
        glow1Paint.setColor(ThemeColors.withAlpha(color1, t.isAnime() ? 72 : 56));
        fill1Paint.setColor(ThemeColors.withAlpha(color1, t.isAnime() ? 48 : (t.isHack() ? 44 : 36)));
        line2Paint.setColor(color2);
        dotPaint.setColor(t.isAnime() ? t.colors().starGold : t.colors().sky);
        barPaint.setColor(ThemeColors.withAlpha(t.colors().sky, 64));
        invalidate();
        return this;
    }

    public NativeLineChartView setSparklineMode(boolean spark) {
        this.sparkline = spark;
        this.showGrid = !spark;
        invalidate();
        return this;
    }

    public NativeLineChartView setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
        invalidate();
        return this;
    }

    public void setHistories(AppState.FloatHistory primary, AppState.FloatHistory secondary) {
        this.hist1 = primary;
        this.hist2 = secondary;
        this.arraySeries = null;
        this.arrayBars = null;
        invalidate();
    }

    public void setArrayData(float[] values, int[] popBars) {
        this.arraySeries = values;
        this.arrayBars = popBars;
        if (values != null) {
            this.arrayLabels = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                this.arrayLabels[i] = Math.round(values[i]) + "°";
            }
        } else {
            this.arrayLabels = null;
        }
        this.hist1 = null;
        this.hist2 = null;
        invalidate();
    }

    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        bgPaint.setColor(sparkline ? 0x00000000 : (c.isHack ? 0xD8010A05 : (c.isAnime ? 0xCC110A26 : c.bg)));
        borderPaint.setColor(c.isAnime ? 0x996B4C9A : c.border);
        gridPaint.setColor(c.isHack ? 0x5515693B : (c.isAnime ? 0x44C792FF : c.divider));
        labelPaint.setColor(c.textPrimary);
        labelPaint.setTypeface(c.isHack ? android.graphics.Typeface.MONOSPACE : android.graphics.Typeface.DEFAULT);
        axisPaint.setColor(ThemeColors.withAlpha(c.textMuted, (c.isHack || c.isAnime) ? 215 : 170));
        axisPaint.setTypeface(c.isHack ? android.graphics.Typeface.MONOSPACE : android.graphics.Typeface.DEFAULT);
        line1Paint.setColor(color1);
        glow1Paint.setColor(ThemeColors.withAlpha(color1, c.isAnime ? 72 : 56));
        fill1Paint.setColor(ThemeColors.withAlpha(color1, c.isAnime ? 48 : (c.isHack ? 44 : 36)));
        line2Paint.setColor(color2);
        dotPaint.setColor(c.isAnime ? c.starGold : c.sky);
        barPaint.setColor(ThemeColors.withAlpha(c.sky, 64));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 8 || h <= 8) return;
        boolean hack = AppTheme.get().isHack();
        boolean anime = AppTheme.get().isAnime();

        if (!sparkline) {
            if (hack) {
                canvas.drawRect(0f, 0f, w, h, bgPaint);
                canvas.drawRect(0.5f, 0.5f, w - 0.5f, h - 0.5f, borderPaint);
            } else {
                bgRect.set(0.5f, 0.5f, w - 0.5f, h - 0.5f);
                float r = anime ? (cornerRadiusPx * 1.3f) : cornerRadiusPx;
                canvas.drawRoundRect(bgRect, r, r, bgPaint);
                if (anime) {
                    canvas.drawRoundRect(bgRect, r, r, borderPaint);
                }
            }
        }

        if (showGrid) {
            float tickX = 4f * AppTheme.get().dimens().density;
            for (int g = 1; g <= 3; g++) {
                float gy = (h * g) / 4f;
                canvas.drawLine(0f, gy, w, gy, gridPaint);
                if (arraySeries == null) {
                    canvas.drawText(GRID_TICKS[g - 1], tickX, gy - 2f, axisPaint);
                }
            }
            if (hack) {
                // Vertical oscilloscope radar grid lines
                for (int v = 1; v <= 5; v++) {
                    float gx = (w * v) / 6f;
                    canvas.drawLine(gx, 0f, gx, h, gridPaint);
                }
            }
        } else {
            canvas.drawLine(0f, h - 1f, w, h - 1f, gridPaint);
        }

        if (arraySeries != null) {
            drawArraySeries(canvas, w, h);
            return;
        }
        if (hist2 != null && hist2.size() >= 2) {
            drawHistory(canvas, hist2, w, h, line2Paint, null, null);
        }
        if (hist1 != null && hist1.size() >= 2) {
            drawHistory(canvas, hist1, w, h, line1Paint, (hack || anime) ? glow1Paint : null, sparkline ? null : fill1Paint);
        }
    }

    private void drawHistory(Canvas canvas, AppState.FloatHistory hist, int w, int h, Paint stroke, Paint glow, Paint fill) {
        int n = hist.size();
        int cap = Math.max(2, hist.capacity());
        float d = AppTheme.get().dimens().density;
        float padX = 2f * d;
        float usableW = Math.max(1f, w - padX * 2f);
        float padTop = 5f;
        float padBot = 4f;
        float usableH = Math.max(1f, h - padTop - padBot);

        linePath.reset();
        boolean started = false;
        float firstX = padX;
        float lastX = padX;
        float lastY = h - padBot;
        int validCount = 0;
        for (int i = 0; i < n; i++) {
            float raw = hist.get(i);
            if (Float.isNaN(raw)) continue;
            float x = padX + ((float) (cap - n + i) / (float) (cap - 1)) * usableW;
            float pct = Math.max(0f, Math.min(100f, raw));
            float y = h - padBot - (pct / 100f) * usableH;
            if (!started) {
                linePath.moveTo(x, y);
                firstX = x;
                started = true;
            } else {
                linePath.lineTo(x, y);
            }
            lastX = x;
            lastY = y;
            validCount++;
        }
        if (validCount < 2) return;

        if (fill != null) {
            fillPath.set(linePath);
            fillPath.lineTo(lastX, h - padBot);
            fillPath.lineTo(firstX, h - padBot);
            fillPath.close();
            canvas.drawPath(fillPath, fill);
        }
        if (glow != null) {
            canvas.drawPath(linePath, glow);
        }
        canvas.drawPath(linePath, stroke);
        if ((AppTheme.get().isHack() || AppTheme.get().isAnime()) && glow != null) {
            // Glowing oscilloscope/starlight head dot at newest sample
            canvas.drawCircle(lastX, lastY, 2.8f * d, dotPaint);
        }
    }

    private void drawArraySeries(Canvas canvas, int w, int h) {
        int n = arraySeries.length;
        if (n < 2) return;
        float minV = 0f;
        float maxV = 100f;
        if (autoScale) {
            minV = Float.MAX_VALUE;
            maxV = -Float.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (arraySeries[i] < minV) minV = arraySeries[i];
                if (arraySeries[i] > maxV) maxV = arraySeries[i];
            }
            if (maxV - minV < 3f) {
                minV -= 1.5f;
                maxV += 1.5f;
            }
        }
        float d = AppTheme.get().dimens().density;
        float padX = 18f * d;
        float topY = 18f * d;
        float botY = h - 8f * d;
        float usableW = Math.max(1f, w - padX * 2f);
        float usableH = Math.max(1f, botY - topY);

        if (arrayBars != null) {
            float barW = Math.max(6f * d, (usableW / n) * 0.45f);
            for (int i = 0; i < n && i < arrayBars.length; i++) {
                int pop = Math.max(0, Math.min(100, arrayBars[i]));
                if (pop > 0) {
                    float cx = padX + ((float) i / (float) (n - 1)) * usableW;
                    float bh = (pop / 100f) * (usableH * 0.55f);
                    canvas.drawRect(cx - barW * 0.5f, h - 3f * d - bh, cx + barW * 0.5f, h - 3f * d, barPaint);
                }
            }
        }

        linePath.reset();
        for (int i = 0; i < n; i++) {
            float px = padX + ((float) i / (float) (n - 1)) * usableW;
            float ratio = (arraySeries[i] - minV) / Math.max(0.1f, maxV - minV);
            float py = botY - ratio * usableH;
            if (i == 0) linePath.moveTo(px, py);
            else linePath.lineTo(px, py);
        }
        fillPath.set(linePath);
        fillPath.lineTo(padX + usableW, h - 3f * d);
        fillPath.lineTo(padX, h - 3f * d);
        fillPath.close();
        canvas.drawPath(fillPath, fill1Paint);
        canvas.drawPath(linePath, line1Paint);

        for (int i = 0; i < n; i += 2) {
            float px = padX + ((float) i / (float) (n - 1)) * usableW;
            float ratio = (arraySeries[i] - minV) / Math.max(0.1f, maxV - minV);
            float py = botY - ratio * usableH;
            canvas.drawCircle(px, py, 3f * d, dotPaint);
            if (arrayLabels != null && i < arrayLabels.length) {
                canvas.drawText(arrayLabels[i], px, py - 6f * d, labelPaint);
            }
        }
    }
}

