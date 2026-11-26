package net.oxog.phonebox.hp71.theme.anime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

/**
 * Overlay transparente ao toque para o tema Anime 2.0:
 * - Filigrana estelar sutil nos cantos da tela (enquadra o HP 7.1 acima da barra preta fixa);
 * - Pétalas de sakura delicadas flutuando pelas bordas (sem atrapalhar leitura central);
 * - Faixa luminosa de estrela + partículas sakura ao trocar de aba.
 * Zero alocações em onDraw(), 100% API 17 e nunca intercepta toques.
 */
public final class AnimeSparkleOverlayView extends View {

    private static final int EDGE_PETALS = 5;
    private static final int SWEEP_STARS = 8;

    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint petalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ribbonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path starPath = new Path();
    private final Path petalPath = new Path();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final float[] px = new float[EDGE_PETALS];
    private final float[] py = new float[EDGE_PETALS];
    private final float[] pvx = new float[EDGE_PETALS];
    private final float[] pvy = new float[EDGE_PETALS];
    private final float[] pang = new float[EDGE_PETALS];
    private final float[] psize = new float[EDGE_PETALS];

    private float density = 1f;
    private boolean animeActive = false;
    private boolean effectsRunning = false;
    private boolean attached = false;
    private float timeSec = 0f;

    private boolean sweepActive = false;
    private float sweepProgress = 0f;

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!animeActive || !effectsRunning || !attached) return;
            int w = getWidth();
            int h = getHeight();
            timeSec += 0.06f;

            if (w > 0 && h > 0) {
                for (int i = 0; i < EDGE_PETALS; i++) {
                    px[i] += pvx[i] + (float) Math.sin(timeSec * 2.1f + i * 1.7f) * 0.55f * density;
                    py[i] += pvy[i];
                    pang[i] = (pang[i] + 2.2f) % 360f;
                    if (py[i] > h + 16f * density || px[i] > w + 16f * density) {
                        respawnEdgePetal(i, w, h, true);
                    }
                }
            }

            if (sweepActive) {
                sweepProgress += 0.11f;
                if (sweepProgress >= 1.0f) {
                    sweepActive = false;
                    sweepProgress = 0f;
                }
            }

            invalidate();
            handler.postDelayed(this, sweepActive ? 36L : 65L);
        }
    };

    public AnimeSparkleOverlayView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        if (this.density <= 0f) this.density = 1f;
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(Math.max(1.2f, 1.4f * density));
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);
        cornerPaint.setColor(0x99FF7ED4);

        starPaint.setStyle(Paint.Style.FILL);
        petalPaint.setStyle(Paint.Style.FILL);
        ribbonPaint.setStyle(Paint.Style.FILL);
    }

    public void setState(boolean animeActive, boolean effectsEnabled) {
        this.animeActive = animeActive;
        this.effectsRunning = animeActive && effectsEnabled;
        setVisibility(animeActive ? VISIBLE : GONE);
        handler.removeCallbacks(loop);
        if (!this.effectsRunning) {
            sweepActive = false;
            invalidate();
            return;
        }
        if (attached) handler.post(loop);
    }

    public void triggerTabSweep() {
        if (!animeActive || !effectsRunning) return;
        sweepActive = true;
        sweepProgress = 0f;
        handler.removeCallbacks(loop);
        if (attached) handler.post(loop);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        for (int i = 0; i < EDGE_PETALS; i++) {
            respawnEdgePetal(i, w, h, false);
        }
    }

    private void respawnEdgePetal(int i, int w, int h, boolean top) {
        // Keep foreground petals mostly near left/right margins so center cards stay crisp
        boolean rightSide = (i % 2 == 1);
        float marginW = Math.max(48f * density, w * 0.18f);
        px[i] = rightSide ? (w - marginW + (i * 19f * density) % marginW) : ((i * 23f * density) % marginW);
        py[i] = top ? (-10f * density - i * 14f * density) : ((i * 97f * density) % Math.max(1, h));
        pvx[i] = (0.45f + (i % 3) * 0.25f) * density;
        pvy[i] = (0.85f + (i % 4) * 0.22f) * density;
        pang[i] = (i * 63) % 360;
        psize[i] = (4.5f + (i % 3) * 1.4f) * density;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (animeActive && effectsRunning) {
            handler.removeCallbacks(loop);
            handler.post(loop);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        handler.removeCallbacks(loop);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!animeActive) return;
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. Delicate foreground sakura petals drifting near edges
        if (effectsRunning) {
            for (int i = 0; i < EDGE_PETALS; i++) {
                petalPaint.setColor(i % 2 == 0 ? 0x6EFF9CE0 : 0x62FFD6F4);
                canvas.save();
                canvas.translate(px[i], py[i]);
                canvas.rotate(pang[i]);
                float s = psize[i];
                petalPath.reset();
                petalPath.moveTo(0f, -s);
                petalPath.quadTo(s * 0.85f, -s * 0.25f, 0f, s * 0.9f);
                petalPath.quadTo(-s * 0.85f, -s * 0.25f, 0f, -s);
                petalPath.close();
                canvas.drawPath(petalPath, petalPaint);
                canvas.restore();
            }
        }

        // 2. Tab-switch sakura starlight ribbon sweep
        if (sweepActive) {
            float x = w * sweepProgress;
            float fade = 1f - Math.abs(sweepProgress - 0.5f) * 1.7f;
            if (fade < 0f) fade = 0f;
            int aPink = Math.round(fade * 80f);
            int aGold = Math.round(fade * 215f);
            ribbonPaint.setColor((aPink << 24) | 0x00FF7ED4);
            canvas.drawRect(x - 24f * density, 0f, x + 12f * density, h, ribbonPaint);
            ribbonPaint.setColor((aGold << 24) | 0x00FFF2CC);
            canvas.drawRect(x - 2f * density, 0f, x + 2f * density, h, ribbonPaint);
            starPaint.setColor((aGold << 24) | 0x00FFD56B);
            for (int s = 0; s < SWEEP_STARS; s++) {
                float sy = ((s + 0.5f) / (float) SWEEP_STARS) * h;
                float sx = x + ((s % 2 == 0 ? 1f : -1f) * 16f * density);
                drawStar(canvas, sx, sy, (3.5f + (s % 3) * 2f) * density * fade, starPaint);
            }
        }

        // 3. Delicate Anime Starlight Corner Accents (frames the viewport cleanly above the black Android 4.2.2 bar)
        float m = 4f * density;
        float arm = 12f * density;
        canvas.drawLine(m, m, m + arm, m, cornerPaint);
        canvas.drawLine(m, m, m, m + arm, cornerPaint);
        canvas.drawLine(w - m - arm, m, w - m, m, cornerPaint);
        canvas.drawLine(w - m, m, w - m, m + arm, cornerPaint);
        canvas.drawLine(m, h - m, m + arm, h - m, cornerPaint);
        canvas.drawLine(m, h - m - arm, m, h - m, cornerPaint);
        canvas.drawLine(w - m - arm, h - m, w - m, h - m, cornerPaint);
        canvas.drawLine(w - m, h - m - arm, w - m, h - m, cornerPaint);

        float twinkle = effectsRunning ? (0.65f + 0.35f * (float) Math.sin(timeSec * 2.8f)) : 0.85f;
        starPaint.setColor((Math.round(twinkle * 220f) << 24) | 0x00FFD56B);
        drawStar(canvas, m + 1.5f * density, m + 1.5f * density, 3.8f * density, starPaint);
        drawStar(canvas, w - m - 1.5f * density, h - m - 1.5f * density, 3.8f * density, starPaint);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float r, Paint paint) {
        if (r <= 0.5f) return;
        float in = r * 0.27f;
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
        canvas.drawPath(starPath, paint);
    }
}
