package net.oxog.phonebox.hp71.theme.anime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Faixa cristalina animada do tema Anime 2.0 na barra superior:
 * - Gema estelar dourada pulsando (Canvas Path 4 pontas);
 * - Micro pétala sakura flutuando + ticker de status animado com telemetria real (CPU/Ping).
 */
public final class AnimeHeaderRibbonView extends View {

    private static final long TICK_MS = 70L;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint petalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF capsule = new RectF();
    private final Path starPath = new Path();
    private final Path petalPath = new Path();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final String[] messages = new String[] {
            "SAKURA MIDNIGHT",
            "LINK TOKYO OK",
            "CPU --%",
            "PING --ms",
            "STARLIGHT HUD"
    };

    private float density = 1f;
    private boolean running = false;
    private boolean attached = false;
    private int tick = 0;
    private int msgIdx = 0;
    private int msgTicks = 0;

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!running || !attached || getVisibility() != VISIBLE) return;
            tick++;
            msgTicks++;
            if (msgTicks >= 42) {
                msgTicks = 0;
                msgIdx = (msgIdx + 1) % messages.length;
            }
            invalidate();
            handler.postDelayed(this, TICK_MS);
        }
    };

    public AnimeHeaderRibbonView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        if (this.density <= 0f) this.density = 1f;
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(0xE0251449);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, 1.1f * density));
        borderPaint.setColor(0xCCFF7ED4);

        starPaint.setStyle(Paint.Style.FILL);
        petalPaint.setStyle(Paint.Style.FILL);

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(10.5f * density);
        textPaint.setColor(0xFFFFF5FD);
    }

    public void updateLiveTelemetry(int cpuPct, int pingMs, boolean online) {
        messages[1] = online ? "LINK TOKYO OK" : "RECONECTANDO...";
        messages[2] = "CPU " + cpuPct + "% · SAKURA";
        messages[3] = pingMs >= 0 ? ("PING " + pingMs + "ms") : "PING --ms";
    }

    public void setRunning(boolean enabled) {
        if (this.running == enabled) return;
        this.running = enabled;
        handler.removeCallbacks(loop);
        if (running && attached && getVisibility() == VISIBLE) {
            handler.post(loop);
        } else {
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (running && getVisibility() == VISIBLE) {
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
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 8 || h <= 8) return;

        float r = h * 0.48f;
        capsule.set(0.8f * density, 0.8f * density, w - 0.8f * density, h - 0.8f * density);
        canvas.drawRoundRect(capsule, r, r, bgPaint);
        canvas.drawRoundRect(capsule, r, r, borderPaint);

        // Pulsing golden 4-point star gem on left
        float pulse = running ? (0.75f + 0.25f * (float) Math.sin(tick * 0.22f)) : 0.9f;
        starPaint.setColor(0xFFFFD56B);
        drawStar(canvas, 11f * density, h * 0.5f, 4.6f * density * pulse, starPaint);

        // Tiny sakura petal drifting across right edge
        if (running) {
            float px = w - 8f * density - ((tick * 1.1f) % Math.max(12f * density, w * 0.45f));
            float py = h * 0.5f + (float) Math.sin(tick * 0.2f) * (h * 0.22f);
            petalPaint.setColor(0x66FF7ED4);
            float ps = 3.2f * density;
            petalPath.reset();
            petalPath.moveTo(px, py - ps);
            petalPath.quadTo(px + ps, py, px, py + ps);
            petalPath.quadTo(px - ps, py, px, py - ps);
            petalPath.close();
            canvas.drawPath(petalPath, petalPaint);
        }

        // Smooth fade-in/out ticker label
        float alpha = 1f;
        if (running) {
            if (msgTicks < 5) alpha = msgTicks / 5f;
            else if (msgTicks > 37) alpha = (42 - msgTicks) / 5f;
        }
        textPaint.setColor((Math.max(30, Math.min(255, Math.round(alpha * 255f))) << 24) | 0x00FFF5FD);

        canvas.save();
        canvas.clipRect(18f * density, 1f, w - 6f * density, h - 1f);
        canvas.drawText(messages[msgIdx], 20f * density, h * 0.5f + 3.6f * density, textPaint);
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
