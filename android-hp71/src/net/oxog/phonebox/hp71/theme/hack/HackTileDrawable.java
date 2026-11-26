package net.oxog.phonebox.hp71.theme.hack;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Tile cyberdeck para a grade de atalhos (Stream Deck) no tema Hack.
 * Cantos chanfrados, colchetes neon, scanlines sutis e barra luminosa inferior.
 */
public final class HackTileDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bracketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scanPaint = new Paint();
    private final Path hull = new Path();

    private final float cutPx;
    private final float barH;
    private final float density;

    public HackTileDrawable(int fillColor, int borderColor, int barColor, float density, boolean pressed) {
        this.density = density;
        this.cutPx = 7f * density;
        this.barH = 3.5f * density;

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, (pressed ? 1.8f : 1.1f) * density));
        borderPaint.setColor(borderColor);

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(Math.max(1.4f, 1.8f * density));
        bracketPaint.setColor(pressed ? 0xFF00F0FF : barColor);

        scanPaint.setStyle(Paint.Style.STROKE);
        scanPaint.setStrokeWidth(Math.max(1f, 0.7f * density));
        scanPaint.setColor(0x1200FF66);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        rebuildPath(bounds);
    }

    private void rebuildPath(Rect b) {
        hull.reset();
        float l = b.left + 0.5f;
        float t = b.top + 0.5f;
        float r = b.right - 0.5f;
        float bot = b.bottom - 0.5f;
        float c = Math.min(cutPx, Math.min((r - l) * 0.22f, (bot - t) * 0.22f));
        hull.moveTo(l, t);
        hull.lineTo(r - c, t);
        hull.lineTo(r, t + c);
        hull.lineTo(r, bot);
        hull.lineTo(l + c, bot);
        hull.lineTo(l, bot - c);
        hull.close();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 4 || b.height() <= 4) return;
        if (hull.isEmpty()) rebuildPath(b);

        canvas.drawPath(hull, fillPaint);

        float step = Math.max(4f, 5f * density);
        for (float y = b.top + step; y < b.bottom - barH - 2f; y += step) {
            canvas.drawLine(b.left + 2f, y, b.right - 2f, y, scanPaint);
        }

        // Bottom neon energy bar
        float c = Math.min(cutPx, Math.min(b.width() * 0.22f, b.height() * 0.22f));
        canvas.drawRect(b.left + c, b.bottom - barH - 0.5f, b.right - 0.5f, b.bottom - 0.5f, barPaint);

        canvas.drawPath(hull, borderPaint);

        // Top-left tactical corner tick
        float bl = 7f * density;
        canvas.drawLine(b.left + 1f, b.top + 1f, b.left + 1f + bl, b.top + 1f, bracketPaint);
        canvas.drawLine(b.left + 1f, b.top + 1f, b.left + 1f, b.top + 1f + bl, bracketPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        borderPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        fillPaint.setColorFilter(cf);
        borderPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
