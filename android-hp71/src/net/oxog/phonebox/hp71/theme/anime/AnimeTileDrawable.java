package net.oxog.phonebox.hp71.theme.anime;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Tile cristalino estilo Anime para a grade de atalhos (Stream Deck) no tema Anime 2.0.
 */
public final class AnimeTileDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF glossRect = new RectF();
    private final RectF barRect = new RectF();
    private final Path starPath = new Path();

    private final float density;
    private final float radiusPx;

    public AnimeTileDrawable(int fillColor, int borderColor, int barColor, float density, boolean pressed) {
        this.density = density;
        this.radiusPx = 12f * density;

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        glossPaint.setStyle(Paint.Style.FILL);
        glossPaint.setColor(pressed ? 0x35FF7ED4 : 0x1CFFE6F9);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1.2f, (pressed ? 2.0f : 1.25f) * density));
        borderPaint.setColor(borderColor);

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(pressed ? 0xFFFFD56B : 0xAAFF7ED4);
    }

    @Override
    protected void onBoundsChange(Rect b) {
        super.onBoundsChange(b);
        float p = 0.8f * density;
        rect.set(b.left + p, b.top + p, b.right - p, b.bottom - p);
        float gp = 3f * density;
        glossRect.set(b.left + gp, b.top + gp, b.right - gp, b.top + b.height() * 0.36f);
        float barH = 4f * density;
        barRect.set(b.left + 8f * density, b.bottom - barH - 2f * density, b.right - 8f * density, b.bottom - 2f * density);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 6 || b.height() <= 6) return;
        if (rect.isEmpty()) onBoundsChange(b);

        canvas.drawRoundRect(rect, radiusPx, radiusPx, fillPaint);
        canvas.drawRoundRect(glossRect, radiusPx * 0.7f, radiusPx * 0.7f, glossPaint);
        canvas.drawRoundRect(barRect, 2f * density, 2f * density, barPaint);
        canvas.drawRoundRect(rect, radiusPx, radiusPx, borderPaint);

        // Tiny anime starlight sparkle in top-right
        float cx = b.right - 9f * density;
        float cy = b.top + 9f * density;
        float r = 3.6f * density;
        float in = r * 0.28f;
        starPath.reset();
        starPath.moveTo(cx, cy - r);
        starPath.lineTo(cx + in, cy - in);
        starPath.lineTo(cx + r, cy);
        starPath.lineTo(cx + in, cy + in);
        starPath.lineTo(cx, cy + r);
        starPath.lineTo(cx - in, cy + in);
        starPath.lineTo(cx - r, cy);
        starPath.lineTo(cx - in, cy - in);
        starPath.close();
        canvas.drawPath(starPath, starPaint);
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
