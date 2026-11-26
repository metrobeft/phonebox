package net.oxog.phonebox.hp71.theme.hack;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Drawable tático chanfrado para botões, keycaps e pílulas no tema Hack (100% API 17).
 * Zero alocações em draw().
 */
public final class HackButtonDrawable extends Drawable {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint();
    private final Paint tickPaint = new Paint();
    private final Path hull = new Path();

    private final float cutPx;
    private final float density;
    private final boolean pressed;
    private final boolean pillStyle;

    public HackButtonDrawable(int fillColor, int borderColor, int tickColor, float density,
                              boolean pressed, boolean pillStyle) {
        this.density = density;
        this.pressed = pressed;
        this.pillStyle = pillStyle;
        this.cutPx = (pillStyle ? 4f : 5.5f) * density;

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, (pressed ? 1.8f : 1.15f) * density));
        borderPaint.setColor(borderColor);

        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(Math.max(1f, density));
        highlightPaint.setColor(pressed ? 0x5500F0FF : 0x2800FF66);

        tickPaint.setStyle(Paint.Style.FILL);
        tickPaint.setColor(tickColor);
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
        float c = Math.min(cutPx, Math.min((r - l) * 0.22f, (bot - t) * 0.35f));
        if (c < 1f) c = 1f;

        // Top-left and bottom-right chamfered keycap
        hull.moveTo(l + c, t);
        hull.lineTo(r, t);
        hull.lineTo(r, bot - c);
        hull.lineTo(r - c, bot);
        hull.lineTo(l, bot);
        hull.lineTo(l, t + c);
        hull.close();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 3 || b.height() <= 3) return;
        if (hull.isEmpty()) rebuildPath(b);

        canvas.drawPath(hull, fillPaint);
        canvas.drawPath(hull, borderPaint);

        // Specular top phosphor hairline
        float c = Math.min(cutPx, Math.min(b.width() * 0.22f, b.height() * 0.35f));
        canvas.drawLine(b.left + c + 1f, b.top + 1.5f * density, b.right - 2f, b.top + 1.5f * density, highlightPaint);

        // Bracket accents for pills or tactile LED pip on buttons
        float pip = Math.max(2f, 2.2f * density);
        if (pillStyle) {
            canvas.drawRect(b.left + 1f, b.bottom - 4f * density, b.left + 1f + pip, b.bottom - 1f, tickPaint);
            canvas.drawRect(b.right - 1f - pip, b.top + 1f, b.right - 1f, b.top + 4f * density, tickPaint);
        } else {
            canvas.drawRect(b.right - 1f - pip * 2f, b.top + 1.5f, b.right - 1.5f, b.top + 1.5f + pip, tickPaint);
            if (pressed) {
                canvas.drawRect(b.left + 1.5f, b.bottom - 1.5f - pip, b.left + 1.5f + pip * 3f, b.bottom - 1.5f, tickPaint);
            }
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
