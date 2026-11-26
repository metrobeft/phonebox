package net.oxog.phonebox.hp71.theme.anime;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Botão e pílula estilo Anime Cel-Shaded (arredondado macio, reflexo superior e brilho sakura ao toque).
 */
public final class AnimeButtonDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF glossRect = new RectF();

    private final float density;
    private final boolean pressed;
    private final boolean pillStyle;

    public AnimeButtonDrawable(int fillColor, int borderColor, int gemColor,
                               float density, boolean pressed, boolean pillStyle) {
        this.density = density;
        this.pressed = pressed;
        this.pillStyle = pillStyle;

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        glossPaint.setStyle(Paint.Style.FILL);
        glossPaint.setColor(pressed ? 0x40FFFFFF : 0x26FFE6F9);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1.1f, (pressed ? 1.8f : 1.2f) * density));
        borderPaint.setColor(borderColor);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(gemColor);
    }

    @Override
    protected void onBoundsChange(Rect b) {
        super.onBoundsChange(b);
        float p = 0.8f * density;
        rect.set(b.left + p, b.top + p, b.right - p, b.bottom - p);
        float gp = 2.2f * density;
        glossRect.set(b.left + gp, b.top + gp, b.right - gp,
                b.top + Math.max(4f * density, b.height() * 0.42f));
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 4 || b.height() <= 4) return;
        if (rect.isEmpty()) onBoundsChange(b);

        float r = pillStyle ? (b.height() * 0.5f) : (9f * density);
        canvas.drawRoundRect(rect, r, r, fillPaint);
        canvas.drawRoundRect(glossRect, r * 0.7f, r * 0.7f, glossPaint);
        canvas.drawRoundRect(rect, r, r, borderPaint);

        if (!pillStyle && b.width() > 36f * density) {
            canvas.drawCircle(b.right - 6f * density, b.top + 6f * density, 1.6f * density, dotPaint);
        }
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
