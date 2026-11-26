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

import net.oxog.phonebox.hp71.theme.ThemeColors;

/**
 * Cápsula animada na barra superior para os 4 mundos vivos (Sakura, Lofi, Starfall, Miku):
 * - Sakura: estrela dourada + micro pétala + telemetria ao vivo;
 * - Lofi: mini equalizador âmbar/menta pulsando + "LOFI 72 BPM";
 * - Starfall: estrela aurora ciano/esmeralda + vaga-lume + "AURORA LIVE";
 * - Miku: mini espectro vocaloid ciano/magenta + "MIKU 01 LIVE".
 */
public final class AnimeHeaderRibbonView extends View {

    private static final long TICK_MS = 70L;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accent2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF capsule = new RectF();
    private final Path starPath = new Path();
    private final Path petalPath = new Path();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final String[] messages = new String[] {
            "SAKURA STORM",
            "LINK TOKYO OK",
            "CPU --%",
            "PING --ms",
            "HANAMI HUD"
    };

    private String world = "sakura";
    private int textRgb = 0x00FFF5FD;
    private int starColor = 0xFFFFD56B;
    private int secondaryColor = 0x66FF7ED4;

    private int lastCpu = 0;
    private int lastPing = -1;
    private boolean lastOnline = true;

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
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, 1.1f * density));
        iconPaint.setStyle(Paint.Style.FILL);
        accent2Paint.setStyle(Paint.Style.FILL);

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(10.5f * density);
        applyWorldStyle();
    }

    public void setWorld(String worldId) {
        String norm = ThemeColors.normalizePresetId(worldId);
        if (!ThemeColors.isLivingWorldId(norm)) norm = "sakura";
        if (norm.equals(this.world)) return;
        this.world = norm;
        this.msgIdx = 0;
        this.msgTicks = 0;
        applyWorldStyle();
        refreshMessages();
        invalidate();
    }

    private void applyWorldStyle() {
        if ("lofi".equals(world)) {
            bgPaint.setColor(0xE0291638);
            borderPaint.setColor(0xCCFFB38A);
            starColor = 0xFFFFE066;
            secondaryColor = 0xCC9FE3C9;
            textRgb = 0x00FFF4EC;
        } else if ("starfall".equals(world)) {
            bgPaint.setColor(0xE00A1B3D);
            borderPaint.setColor(0xCC7FE7FF);
            starColor = 0xFF6DFFB0;
            secondaryColor = 0x99DCFF78;
            textRgb = 0x00F0FBFF;
        } else if ("miku".equals(world)) {
            bgPaint.setColor(0xE007222B);
            borderPaint.setColor(0xCC39C5BB);
            starColor = 0xFF39C5BB;
            secondaryColor = 0xEEFF2E88;
            textRgb = 0x00EEFFFE;
        } else {
            bgPaint.setColor(0xE0251449);
            borderPaint.setColor(0xCCFF7ED4);
            starColor = 0xFFFFD56B;
            secondaryColor = 0x66FF7ED4;
            textRgb = 0x00FFF5FD;
        }
    }

    private void refreshMessages() {
        if ("lofi".equals(world)) {
            messages[0] = "LOFI 72 BPM";
            messages[1] = lastOnline ? "CHUVA NA JANELA" : "RECONECTANDO...";
            messages[2] = "CPU " + lastCpu + "% · CHILL";
            messages[3] = lastPing >= 0 ? ("PING " + lastPing + "ms") : "PING --ms";
            messages[4] = "STUDY BEATS";
        } else if ("starfall".equals(world)) {
            messages[0] = "STARFALL AURORA";
            messages[1] = lastOnline ? "CONSTELACAO OK" : "RECONECTANDO...";
            messages[2] = "CPU " + lastCpu + "% · NOVA";
            messages[3] = lastPing >= 0 ? ("PING " + lastPing + "ms") : "PING --ms";
            messages[4] = "METEOR SHOWER";
        } else if ("miku".equals(world)) {
            messages[0] = "MIKU 01 STAGE";
            messages[1] = lastOnline ? "VOCAL SYNTH OK" : "RECONECTANDO...";
            messages[2] = "CPU " + lastCpu + "% · 39HZ";
            messages[3] = lastPing >= 0 ? ("PING " + lastPing + "ms") : "PING --ms";
            messages[4] = "01 LIVE SYNC";
        } else {
            messages[0] = "SAKURA STORM";
            messages[1] = lastOnline ? "LINK TOKYO OK" : "RECONECTANDO...";
            messages[2] = "CPU " + lastCpu + "% · SAKURA";
            messages[3] = lastPing >= 0 ? ("PING " + lastPing + "ms") : "PING --ms";
            messages[4] = "HANAMI HUD";
        }
    }

    public void updateLiveTelemetry(int cpuPct, int pingMs, boolean online) {
        this.lastCpu = cpuPct;
        this.lastPing = pingMs;
        this.lastOnline = online;
        refreshMessages();
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

        float textLeft = 20f * density;
        if ("lofi".equals(world) || "miku".equals(world)) {
            // 4-bar live mini audio equalizer on left
            float barW = 2.2f * density;
            float gap = 1.4f * density;
            float bx = 7.5f * density;
            float baseY = h * 0.74f;
            float maxH = h * 0.46f;
            for (int i = 0; i < 4; i++) {
                float lvl = running ? (0.32f + 0.68f * (float) Math.abs(Math.sin(tick * 0.24f + i * 1.15f))) : (0.45f + (i % 2) * 0.35f);
                iconPaint.setColor((i == 2) ? secondaryColor : starColor);
                canvas.drawRect(bx + i * (barW + gap), baseY - maxH * lvl, bx + i * (barW + gap) + barW, baseY, iconPaint);
            }
            textLeft = 24f * density;
        } else {
            // Pulsing 4-point star gem on left
            float pulse = running ? (0.75f + 0.25f * (float) Math.sin(tick * 0.22f)) : 0.9f;
            iconPaint.setColor(starColor);
            drawStar(canvas, 11f * density, h * 0.5f, 4.6f * density * pulse, iconPaint);

            // Drifting micro sakura petal or firefly on right
            if (running) {
                float px = w - 8f * density - ((tick * 1.1f) % Math.max(12f * density, w * 0.42f));
                float py = h * 0.5f + (float) Math.sin(tick * 0.2f) * (h * 0.22f);
                accent2Paint.setColor(secondaryColor);
                if ("starfall".equals(world)) {
                    canvas.drawCircle(px, py, 2.2f * density, accent2Paint);
                } else {
                    float ps = 3.2f * density;
                    petalPath.reset();
                    petalPath.moveTo(px, py - ps);
                    petalPath.quadTo(px + ps, py, px, py + ps);
                    petalPath.quadTo(px - ps, py, px, py - ps);
                    petalPath.close();
                    canvas.drawPath(petalPath, accent2Paint);
                }
            }
        }

        float alpha = 1f;
        if (running) {
            if (msgTicks < 5) alpha = msgTicks / 5f;
            else if (msgTicks > 37) alpha = (42 - msgTicks) / 5f;
        }
        textPaint.setColor((Math.max(30, Math.min(255, Math.round(alpha * 255f))) << 24) | textRgb);

        canvas.save();
        canvas.clipRect(textLeft - 2f * density, 1f, w - 6f * density, h - 1f);
        canvas.drawText(messages[msgIdx], textLeft, h * 0.5f + 3.6f * density, textPaint);
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
