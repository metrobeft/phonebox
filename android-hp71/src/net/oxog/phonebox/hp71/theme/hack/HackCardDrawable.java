package net.oxog.phonebox.hp71.theme.hack;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Drawable nativo API 17 com geometria Cyberdeck HUD para cards no tema Hack:
 * - Cantos chanfrados 45° (topo-direito e base-esquerdo);
 * - Superfície CRT translúcida com micro-scanlines sutis;
 * - Colchetes táticos neon nos cantos e trilha de telemetria no topo.
 * Zero alocações em draw().
 */
public final class HackCardDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bracketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scanPaint = new Paint();
    private final Paint notchPaint = new Paint();
    private final Path clipPath = new Path();

    private final float cutPx;
    private final float bracketLen;
    private final float density;
    private final boolean highlight;

    public HackCardDrawable(int fillColor, int borderColor, int bracketColor, int secondaryNotchColor,
                            float density, boolean highlight) {
        this.density = density;
        this.highlight = highlight;
        this.cutPx = (highlight ? 11f : 8f) * density;
        this.bracketLen = (highlight ? 12f : 8f) * density;

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, (highlight ? 1.35f : 1.0f) * density));
        borderPaint.setColor(borderColor);

        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(Math.max(1.5f, (highlight ? 2.2f : 1.6f) * density));
        bracketPaint.setStrokeCap(Paint.Cap.SQUARE);
        bracketPaint.setColor(bracketColor);

        scanPaint.setStyle(Paint.Style.STROKE);
        scanPaint.setStrokeWidth(Math.max(1f, density * 0.75f));
        scanPaint.setColor(highlight ? 0x1600FF66 : 0x0E00FF66);

        notchPaint.setStyle(Paint.Style.FILL);
        notchPaint.setColor(secondaryNotchColor);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        rebuildPath(bounds);
    }

    private void rebuildPath(Rect b) {
        clipPath.reset();
        float l = b.left + 0.5f;
        float t = b.top + 0.5f;
        float r = b.right - 0.5f;
        float bot = b.bottom - 0.5f;
        float c = Math.min(cutPx, Math.min((r - l) * 0.25f, (bot - t) * 0.25f));
        if (c < 1f) c = 1f;

        // Chamfer top-right & bottom-left for distinctive cyberdeck silhouette
        clipPath.moveTo(l, t);
        clipPath.lineTo(r - c, t);
        clipPath.lineTo(r, t + c);
        clipPath.lineTo(r, bot);
        clipPath.lineTo(l + c, bot);
        clipPath.lineTo(l, bot - c);
        clipPath.close();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        int w = b.width();
        int h = b.height();
        if (w <= 4 || h <= 4) return;
        if (clipPath.isEmpty()) rebuildPath(b);

        canvas.drawPath(clipPath, fillPaint);

        // Subtle CRT interior scanlines clipped to chamfered hull
        float step = Math.max(4f, 4.5f * density);
        float c = Math.min(cutPx, Math.min(w * 0.25f, h * 0.25f));
        for (float y = b.top + step; y < b.bottom - 2f; y += step) {
            float xStart = b.left + 2f;
            float xEnd = b.right - 2f;
            float dyTop = y - b.top;
            if (dyTop < c) xEnd = b.right - (c - dyTop) - 1f;
            float dyBot = b.bottom - y;
            if (dyBot < c) xStart = b.left + (c - dyBot) + 1f;
            if (xEnd > xStart + 2f) {
                canvas.drawLine(xStart, y, xEnd, y, scanPaint);
            }
        }

        // Wireframe polygon border
        canvas.drawPath(clipPath, borderPaint);

        // Neon tactical L-brackets (top-left, bottom-right, and chamfer accent slash)
        float l = b.left + 1f;
        float t = b.top + 1f;
        float r = b.right - 1f;
        float bot = b.bottom - 1f;
        float bl = Math.min(bracketLen, Math.min(w * 0.2f, h * 0.28f));

        // Top-left L
        canvas.drawLine(l, t, l + bl, t, bracketPaint);
        canvas.drawLine(l, t, l, t + bl, bracketPaint);

        // Bottom-right L
        canvas.drawLine(r - bl, bot, r, bot, bracketPaint);
        canvas.drawLine(r, bot - bl, r, bot, bracketPaint);

        // Top-right chamfer highlight diagonal
        canvas.drawLine(r - c, t, r, t + c, bracketPaint);

        // Telemetry status pip on top edge for highlight cards
        if (highlight && w > 64f * density) {
            float barX = l + bl + 5f * density;
            float barW = 18f * density;
            float barH = Math.max(2f, 2f * density);
            canvas.drawRect(barX, t, barX + barW, t + barH, notchPaint);
            canvas.drawRect(barX + barW + 3f * density, t, barX + barW + 6f * density, t + barH, notchPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        borderPaint.setAlpha(alpha);
        bracketPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        fillPaint.setColorFilter(cf);
        borderPaint.setColorFilter(cf);
        bracketPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
