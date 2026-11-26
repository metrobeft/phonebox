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
 * Drawable nativo API 17 estilo Anime Crystal / Sakura Twilight HUD:
 * - Bordas arredondadas macias com duplo aro luminoso (borda sakura/orquídea + brilho interno superior);
 * - Faixa sutil de brilho cel-shaded no topo do card;
 * - Estrela brilhante de 4 pontas (✦) + ponto dourado nos cards em destaque.
 * Zero alocações em draw().
 */
public final class AnimeCardDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF outerRect = new RectF();
    private final RectF innerRect = new RectF();
    private final RectF sheenRect = new RectF();
    private final Path starPath = new Path();

    private final float radiusPx;
    private final float density;
    private final boolean highlight;

    public AnimeCardDrawable(int fillColor, int borderColor, int starColor, int goldColor,
                             float density, boolean highlight) {
        this.density = density;
        this.highlight = highlight;
        this.radiusPx = (highlight ? 13f : 10f) * density;

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        sheenPaint.setStyle(Paint.Style.FILL);
        sheenPaint.setColor(highlight ? 0x24FF7ED4 : 0x14C792FF);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1.2f, (highlight ? 1.6f : 1.15f) * density));
        borderPaint.setColor(borderColor);

        innerRimPaint.setStyle(Paint.Style.STROKE);
        innerRimPaint.setStrokeWidth(Math.max(1f, density * 0.9f));
        innerRimPaint.setColor(highlight ? 0x44FFE3F7 : 0x22E8D5FF);

        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(starColor);

        goldPaint.setStyle(Paint.Style.FILL);
        goldPaint.setColor(goldColor);
    }

    @Override
    protected void onBoundsChange(Rect b) {
        super.onBoundsChange(b);
        float pad = 0.8f * density;
        outerRect.set(b.left + pad, b.top + pad, b.right - pad, b.bottom - pad);
        float in = 2.4f * density;
        innerRect.set(b.left + in, b.top + in, b.right - in, b.bottom - in);
        sheenRect.set(b.left + in, b.top + in, b.right - in,
                Math.min(b.bottom - in, b.top + Math.max(14f * density, b.height() * 0.16f)));
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 6 || b.height() <= 6) return;
        if (outerRect.isEmpty()) onBoundsChange(b);

        // Translucent twilight glass fill
        canvas.drawRoundRect(outerRect, radiusPx, radiusPx, fillPaint);

        // Cel-shaded soft specular top sheen
        canvas.drawRoundRect(sheenRect, radiusPx * 0.75f, radiusPx * 0.75f, sheenPaint);

        // Inner soft moonlight rim + outer glowing sakura/orchid border
        canvas.drawRoundRect(innerRect, Math.max(2f, radiusPx - 1.5f * density),
                Math.max(2f, radiusPx - 1.5f * density), innerRimPaint);
        canvas.drawRoundRect(outerRect, radiusPx, radiusPx, borderPaint);

        // Decorative 4-point anime starlight sparkle in top-right corner on highlight cards
        if (highlight && b.width() > 64f * density && b.height() > 36f * density) {
            float sx = b.right - 13f * density;
            float sy = b.top + 11f * density;
            drawFourPointStar(canvas, sx, sy, 5.2f * density, starPaint);
            drawFourPointStar(canvas, sx - 9f * density, sy + 5f * density, 2.8f * density, goldPaint);
            canvas.drawCircle(b.left + 11f * density, b.bottom - 9f * density, 1.8f * density, goldPaint);
        }
    }

    private void drawFourPointStar(Canvas canvas, float cx, float cy, float r, Paint paint) {
        float inner = r * 0.28f;
        starPath.reset();
        starPath.moveTo(cx, cy - r);
        starPath.lineTo(cx + inner, cy - inner);
        starPath.lineTo(cx + r, cy);
        starPath.lineTo(cx + inner, cy + inner);
        starPath.lineTo(cx, cy + r);
        starPath.lineTo(cx - inner, cy + inner);
        starPath.lineTo(cx - r, cy);
        starPath.lineTo(cx - inner, cy - inner);
        starPath.close();
        canvas.drawPath(starPath, paint);
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
