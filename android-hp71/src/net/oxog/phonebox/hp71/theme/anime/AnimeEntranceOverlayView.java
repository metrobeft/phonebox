package net.oxog.phonebox.hp71.theme.anime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Sequência de entrada animada do tema Anime 2.0 ("Sakura Midnight Bloom"):
 * - Halo lunar crescente + anel expansivo de pétalas de cerejeira + estrelas douradas de 4 pontas;
 * - Revelação estilizada do título "PHONEBOX · SAKURA" + subtítulo "MIDNIGHT ANIME EDITION";
 * - Toque em qualquer lugar para pular instantaneamente.
 * 100% API 17 e zero dependência de glifos Unicode fora do DroidSans.
 */
public final class AnimeEntranceOverlayView extends View {

    private static final long TOTAL_MS = 2150L;
    private static final long FRAME_MS = 34L;
    private static final int RING_PETALS = 16;

    private final Paint bgPaint = new Paint();
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonCutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint petalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path petalPath = new Path();
    private final Path starPath = new Path();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private float density = 1f;
    private long startMs = 0L;
    private boolean playing = false;
    private boolean attached = false;
    private Runnable onFinished;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!playing || !attached) return;
            long elapsed = System.currentTimeMillis() - startMs;
            if (elapsed >= TOTAL_MS) {
                stop();
                return;
            }
            invalidate();
            handler.postDelayed(this, FRAME_MS);
        }
    };

    public AnimeEntranceOverlayView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        if (this.density <= 0f) this.density = 1f;
        setVisibility(GONE);
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        bgPaint.setStyle(Paint.Style.FILL);
        glowPaint.setStyle(Paint.Style.STROKE);
        moonPaint.setStyle(Paint.Style.FILL);
        moonCutPaint.setStyle(Paint.Style.FILL);

        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(0xFFFFF5FD);

        subPaint.setTypeface(Typeface.DEFAULT_BOLD);
        subPaint.setTextAlign(Paint.Align.CENTER);
        subPaint.setColor(0xFFFF7ED4);

        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setColor(0xFFC0A2E8);
        hintPaint.setTextSize(11.5f * density);

        petalPaint.setStyle(Paint.Style.FILL);
        starPaint.setStyle(Paint.Style.FILL);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(Math.max(1.2f, 1.5f * density));
    }

    public void play(Runnable onDone) {
        this.onFinished = onDone;
        this.startMs = System.currentTimeMillis();
        this.playing = true;
        setVisibility(VISIBLE);
        handler.removeCallbacks(ticker);
        if (attached) handler.post(ticker);
    }

    public void stop() {
        boolean was = playing;
        playing = false;
        handler.removeCallbacks(ticker);
        setVisibility(GONE);
        if (was && onFinished != null) {
            Runnable cb = onFinished;
            onFinished = null;
            cb.run();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (playing) {
            handler.removeCallbacks(ticker);
            handler.post(ticker);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!playing) return;
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        long elapsed = System.currentTimeMillis() - startMs;
        float progress = Math.min(1f, elapsed / (float) TOTAL_MS);

        // Smooth fade out in final 420ms
        float masterFade = 1f;
        if (elapsed > TOTAL_MS - 420L) {
            masterFade = Math.max(0f, 1f - ((elapsed - (TOTAL_MS - 420L)) / 420f));
        }

        bgPaint.setColor((Math.round(240f * masterFade) << 24) | 0x000A061A);
        canvas.drawRect(0f, 0f, w, h, bgPaint);

        float cx = w * 0.5f;
        float cy = h * 0.46f;
        float minDim = Math.min(w, h);

        // Expanding glowing sakura rings
        float ease = 1f - (1f - progress) * (1f - progress) * (1f - progress);
        float ringR = minDim * (0.14f + 0.38f * ease);
        glowPaint.setStrokeWidth(2.2f * density);
        glowPaint.setColor((Math.round(135f * masterFade * (1f - progress * 0.4f)) << 24) | 0x00FF7ED4);
        canvas.drawCircle(cx, cy, ringR, glowPaint);

        glowPaint.setStrokeWidth(1.2f * density);
        glowPaint.setColor((Math.round(115f * masterFade) << 24) | 0x007CE8FF);
        canvas.drawCircle(cx, cy, ringR * 0.72f, glowPaint);

        // Radiant crescent moon emblem above title
        float moonR = Math.max(18f * density, minDim * 0.068f) * Math.min(1f, elapsed / 320f);
        float my = cy - 48f * density;
        moonPaint.setColor((Math.round(240f * masterFade) << 24) | 0x00FFF2CC);
        canvas.drawCircle(cx, my, moonR, moonPaint);
        moonCutPaint.setColor((Math.round(245f * masterFade) << 24) | 0x000A061A);
        canvas.drawCircle(cx - moonR * 0.38f, my - moonR * 0.16f, moonR * 0.86f, moonCutPaint);

        // Swirling ring of Sakura petals blossoming outward
        for (int i = 0; i < RING_PETALS; i++) {
            double ang = (i * Math.PI * 2.0 / RING_PETALS) + progress * 2.4;
            float dist = minDim * (0.08f + 0.42f * ease) * (0.82f + (i % 3) * 0.14f);
            float px = cx + (float) Math.cos(ang) * dist;
            float py = cy + (float) Math.sin(ang) * dist * 0.75f;
            int pa = Math.round(210f * masterFade);
            petalPaint.setColor((pa << 24) | (i % 2 == 0 ? 0x00FF7ED4 : 0x00FFD1F0));
            drawPetal(canvas, px, py, (6.5f + (i % 3) * 1.6f) * density, (float) Math.toDegrees(ang) + 45f, petalPaint);
        }

        // Title & subtitle reveal
        float textAlpha = Math.min(1f, Math.max(0f, (elapsed - 160L) / 360f)) * masterFade;
        int ta = Math.round(255f * textAlpha);
        boolean portrait = h > w;
        titlePaint.setTextSize((portrait ? 22f : 27f) * density);
        titlePaint.setColor((ta << 24) | 0x00FFF5FD);
        titlePaint.setShadowLayer(8f, 0f, 0f, (Math.round(190f * textAlpha) << 24) | 0x00FF7ED4);
        canvas.drawText("PHONEBOX  SAKURA", cx, cy + 16f * density, titlePaint);

        float lineHalf = Math.min(w * 0.34f, 150f * density) * Math.min(1f, Math.max(0f, (elapsed - 260L) / 420f));
        linePaint.setColor((Math.round(200f * textAlpha) << 24) | 0x00FFD56B);
        canvas.drawLine(cx - lineHalf, cy + 28f * density, cx + lineHalf, cy + 28f * density, linePaint);

        subPaint.setTextSize((portrait ? 12.5f : 14f) * density);
        subPaint.setColor((ta << 24) | 0x00FF7ED4);
        canvas.drawText("MIDNIGHT ANIME EDITION  //  HP 7.1", cx, cy + 48f * density, subPaint);

        hintPaint.setColor((Math.round(170f * textAlpha) << 24) | 0x00C0A2E8);
        canvas.drawText("Toque para entrar", cx, cy + 74f * density, hintPaint);

        // Twinkling golden 4-point stars framing title
        starPaint.setColor((ta << 24) | 0x00FFD56B);
        float sp = 0.7f + 0.3f * (float) Math.sin(elapsed * 0.014f);
        drawStar(canvas, cx - lineHalf - 10f * density, cy + 28f * density, 6.5f * density * sp, starPaint);
        drawStar(canvas, cx + lineHalf + 10f * density, cy + 28f * density, 6.5f * density * sp, starPaint);
        starPaint.setColor((ta << 24) | 0x007CE8FF);
        drawStar(canvas, cx + moonR * 1.45f, my - moonR * 0.6f, 5.0f * density * sp, starPaint);
    }

    private void drawPetal(Canvas canvas, float cx, float cy, float s, float deg, Paint paint) {
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(deg);
        petalPath.reset();
        petalPath.moveTo(0f, -s);
        petalPath.quadTo(s * 0.85f, -s * 0.25f, 0f, s * 0.9f);
        petalPath.quadTo(-s * 0.85f, -s * 0.25f, 0f, -s);
        petalPath.close();
        canvas.drawPath(petalPath, paint);
        canvas.restore();
    }

    private void drawStar(Canvas canvas, float cx, float cy, float r, Paint paint) {
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
