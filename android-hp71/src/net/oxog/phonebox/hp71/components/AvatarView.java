package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;

/**
 * Componente circular de avatar com imagem via BitmapCache, fallback com iniciais
 * e anel colorido de status / fala ao vivo (Discord). Zero alocações em onDraw().
 */
public class AvatarView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix shaderMatrix = new Matrix();

    private Bitmap bitmap;
    private String initials = "?";
    private boolean speaking = false;
    private int customRingColor = 0;

    public AvatarView(Context context) {
        super(context);
        bgPaint.setStyle(Paint.Style.FILL);
        ringPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        applyTheme();
    }

    public void setInitials(String name) {
        this.initials = TouchActionTileView.monogram(name);
        invalidate();
    }

    public void setBitmap(Bitmap bmp) {
        if (this.bitmap == bmp) return;
        this.bitmap = bmp;
        if (bmp != null) {
            imgPaint.setShader(new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        } else {
            imgPaint.setShader(null);
        }
        invalidate();
    }

    public void setSpeaking(boolean speaking) {
        if (this.speaking != speaking) {
            this.speaking = speaking;
            applyTheme();
        }
    }

    public void setCustomRingColor(int color) {
        this.customRingColor = color;
        applyTheme();
    }

    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        bgPaint.setColor(c.surfaceControl);
        textPaint.setColor(c.textPrimary);
        int ring = speaking ? c.green : (customRingColor != 0 ? customRingColor : c.border);
        ringPaint.setColor(ring);
        ringPaint.setStrokeWidth(AppTheme.get().dimens().dp(speaking ? 3 : 1));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h);
        if (size <= 2) return;
        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float ringW = ringPaint.getStrokeWidth();
        float radius = (size * 0.5f) - ringW;

        canvas.drawCircle(cx, cy, radius, bgPaint);

        if (bitmap != null && imgPaint.getShader() != null) {
            float bw = bitmap.getWidth();
            float bh = bitmap.getHeight();
            if (bw > 0 && bh > 0) {
                float scale = (radius * 2f) / Math.min(bw, bh);
                shaderMatrix.reset();
                shaderMatrix.postScale(scale, scale);
                shaderMatrix.postTranslate(cx - (bw * scale) * 0.5f, cy - (bh * scale) * 0.5f);
                imgPaint.getShader().setLocalMatrix(shaderMatrix);
                canvas.drawCircle(cx, cy, radius, imgPaint);
            }
        } else {
            textPaint.setTextSize(radius * 0.82f);
            float baseline = cy - ((textPaint.descent() + textPaint.ascent()) * 0.5f);
            canvas.drawText(initials, cx, baseline, textPaint);
        }

        if (ringW > 0f) {
            canvas.drawCircle(cx, cy, radius + ringW * 0.5f, ringPaint);
        }
    }
}

