package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * Ícone climático vetorial desenhado diretamente em Canvas (sol, lua, nuvens, neblina, garoa, chuva, tempestade).
 * 100% compatível com API 17 e zero alocações em onDraw().
 */
public class WeatherGlyphView extends View {

    private final Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudDarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint boltPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final Path boltPath = new Path();

    private int wmoCode = 0;
    private boolean isDay = true;

    public WeatherGlyphView(Context context) {
        super(context);
        sunPaint.setColor(0xFFFBBF24);
        sunPaint.setStyle(Paint.Style.FILL);

        rayPaint.setColor(0xFFF59E0B);
        rayPaint.setStyle(Paint.Style.STROKE);
        rayPaint.setStrokeCap(Paint.Cap.ROUND);

        moonPaint.setColor(0xFFA5B4FC);
        moonPaint.setStyle(Paint.Style.FILL);

        cloudLightPaint.setColor(0xFFE2E8F0);
        cloudLightPaint.setStyle(Paint.Style.FILL);

        cloudDarkPaint.setColor(0xFF94A3B8);
        cloudDarkPaint.setStyle(Paint.Style.FILL);

        rainPaint.setColor(0xFF38BDF8);
        rainPaint.setStyle(Paint.Style.STROKE);
        rainPaint.setStrokeCap(Paint.Cap.ROUND);

        boltPaint.setColor(0xFFFACC15);
        boltPaint.setStyle(Paint.Style.FILL);
    }

    public void setWeather(int code, boolean day) {
        if (this.wmoCode != code || this.isDay != day) {
            this.wmoCode = code;
            this.isDay = day;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float s = Math.min(getWidth(), getHeight());
        if (s <= 4f) return;
        float ox = (getWidth() - s) * 0.5f;
        float oy = (getHeight() - s) * 0.5f;
        float u = s / 64f;

        rayPaint.setStrokeWidth(3f * u);
        rainPaint.setStrokeWidth(2.8f * u);

        if (wmoCode == 0 || wmoCode == 1) {
            if (isDay) drawSun(canvas, ox + 32f * u, oy + 32f * u, 13f * u, 25f * u);
            else drawMoon(canvas, ox + 32f * u, oy + 32f * u, 15f * u);
        } else if (wmoCode == 2) {
            if (isDay) drawSun(canvas, ox + 22f * u, oy + 22f * u, 9.5f * u, 18f * u);
            else drawMoon(canvas, ox + 22f * u, oy + 22f * u, 10f * u);
            drawCloud(canvas, ox, oy, u, cloudLightPaint, 0f);
        } else if (wmoCode == 3 || wmoCode == 45 || wmoCode == 48) {
            drawCloud(canvas, ox, oy, u, cloudDarkPaint, -4f);
            drawCloud(canvas, ox, oy, u, cloudLightPaint, 2f);
            if (wmoCode >= 45) {
                rainPaint.setColor(0xFF94A3B8);
                canvas.drawLine(ox + 16f * u, oy + 52f * u, ox + 48f * u, oy + 52f * u, rainPaint);
                canvas.drawLine(ox + 20f * u, oy + 58f * u, ox + 44f * u, oy + 58f * u, rainPaint);
                rainPaint.setColor(0xFF38BDF8);
            }
        } else if ((wmoCode >= 51 && wmoCode <= 61) || wmoCode == 80) {
            drawCloud(canvas, ox, oy, u, cloudDarkPaint, -3f);
            drawCloud(canvas, ox, oy, u, cloudLightPaint, 0f);
            drawRain(canvas, ox, oy, u, 3);
        } else if (wmoCode >= 63 && wmoCode <= 82) {
            drawCloud(canvas, ox, oy, u, cloudDarkPaint, -3f);
            drawRain(canvas, ox, oy, u, 5);
        } else {
            drawCloud(canvas, ox, oy, u, cloudDarkPaint, -3f);
            boltPath.reset();
            boltPath.moveTo(ox + 34f * u, oy + 41f * u);
            boltPath.lineTo(ox + 25f * u, oy + 53f * u);
            boltPath.lineTo(ox + 32f * u, oy + 53f * u);
            boltPath.lineTo(ox + 28f * u, oy + 62f * u);
            boltPath.lineTo(ox + 42f * u, oy + 48f * u);
            boltPath.lineTo(ox + 35f * u, oy + 48f * u);
            boltPath.close();
            canvas.drawPath(boltPath, boltPaint);
            canvas.drawLine(ox + 20f * u, oy + 48f * u, ox + 17f * u, oy + 58f * u, rainPaint);
            canvas.drawLine(ox + 46f * u, oy + 48f * u, ox + 43f * u, oy + 58f * u, rainPaint);
        }
    }

    private void drawSun(Canvas canvas, float cx, float cy, float r, float outerR) {
        for (int i = 0; i < 8; i++) {
            double ang = i * (Math.PI / 4.0);
            float x1 = cx + (float) (Math.cos(ang) * (r + (outerR - r) * 0.35f));
            float y1 = cy + (float) (Math.sin(ang) * (r + (outerR - r) * 0.35f));
            float x2 = cx + (float) (Math.cos(ang) * outerR);
            float y2 = cy + (float) (Math.sin(ang) * outerR);
            canvas.drawLine(x1, y1, x2, y2, rayPaint);
        }
        canvas.drawCircle(cx, cy, r, sunPaint);
    }

    private void drawMoon(Canvas canvas, float cx, float cy, float r) {
        oval.set(cx - r, cy - r, cx + r, cy + r);
        canvas.drawArc(oval, 40f, 250f, true, moonPaint);
        canvas.drawCircle(cx - r * 0.9f, cy - r * 0.7f, r * 0.14f, sunPaint);
        canvas.drawCircle(cx + r * 0.95f, cy - r * 0.5f, r * 0.11f, sunPaint);
    }

    private void drawCloud(Canvas canvas, float ox, float oy, float u, Paint paint, float shiftY) {
        float y = oy + shiftY * u;
        canvas.drawCircle(ox + 24f * u, y + 36f * u, 10f * u, paint);
        canvas.drawCircle(ox + 34f * u, y + 30f * u, 12f * u, paint);
        canvas.drawCircle(ox + 45f * u, y + 36f * u, 9.5f * u, paint);
        oval.set(ox + 15f * u, y + 34f * u, ox + 54f * u, y + 46f * u);
        canvas.drawRoundRect(oval, 6f * u, 6f * u, paint);
    }

    private void drawRain(Canvas canvas, float ox, float oy, float u, int drops) {
        float startX = drops == 3 ? 23f : 18f;
        float step = drops == 3 ? 10f : 7.5f;
        for (int i = 0; i < drops; i++) {
            float x = ox + (startX + i * step) * u;
            canvas.drawLine(x, oy + 49f * u, x - 3.5f * u, oy + 59f * u, rainPaint);
        }
    }
}

