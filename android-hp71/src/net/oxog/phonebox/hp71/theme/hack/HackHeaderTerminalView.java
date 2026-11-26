package net.oxog.phonebox.hp71.theme.hack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Terminal Typewriter vivo para a barra superior no tema Hack.
 * Digita comandos estilo h4ck3r caractere a caractere com cursor piscante (|) e telemetria real,
 * além de micro-glitches de sinal e pulso de atividade.
 */
public final class HackHeaderTerminalView extends View {

    private static final long TICK_MS = 85L;

    private final Paint bgPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glitchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cursorPaint = new Paint();
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final String[] dynamicFeed = new String[] {
            "> h4ck3d_0s // R00T",
            "> m4tr1x_l1nk [0K]",
            "> cpu_l04d: --%",
            "> sy5_gl1tch :: 0x71",
            "> n3t_p1ng: --ms",
            "> r00t@hp71:~# _"
    };

    private float density = 1f;
    private boolean running = false;
    private boolean attached = false;
    private int msgIdx = 0;
    private int typedChars = 0;
    private int holdTicks = 0;
    private int tick = 0;
    private boolean glitchFlash = false;

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!running || !attached || getVisibility() != VISIBLE) return;
            tick++;
            String cur = dynamicFeed[msgIdx];
            if (typedChars < cur.length()) {
                typedChars++;
                holdTicks = 0;
            } else {
                holdTicks++;
                if (holdTicks >= 24) {
                    msgIdx = (msgIdx + 1) % dynamicFeed.length;
                    typedChars = 0;
                    holdTicks = 0;
                }
            }
            glitchFlash = (tick % 37 == 0 || tick % 37 == 1);
            invalidate();
            handler.postDelayed(this, TICK_MS);
        }
    };

    public HackHeaderTerminalView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        if (this.density <= 0f) this.density = 1f;
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(0xE0020E07);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, density));
        borderPaint.setColor(0xFF157A42);

        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setTextSize(10.5f * density);
        textPaint.setColor(0xFF00FF66);

        glitchPaint.setTypeface(Typeface.MONOSPACE);
        glitchPaint.setTextSize(10.5f * density);
        glitchPaint.setColor(0xCC00F0FF);

        cursorPaint.setStyle(Paint.Style.FILL);
        cursorPaint.setColor(0xFF00FF66);

        dotPaint.setStyle(Paint.Style.FILL);
    }

    public void updateLiveTelemetry(int cpuPct, int pingMs, boolean online) {
        dynamicFeed[1] = online ? "> m4tr1x_l1nk [0K]" : "> l1nk_l05t [R3TRY]";
        dynamicFeed[2] = "> cpu_l04d: " + cpuPct + "%";
        dynamicFeed[4] = pingMs >= 0 ? ("> n3t_p1ng: " + pingMs + "ms") : "> n3t_p1ng: --ms";
    }

    public void setRunning(boolean enabled) {
        if (this.running == enabled) return;
        this.running = enabled;
        handler.removeCallbacks(loop);
        if (running && attached && getVisibility() == VISIBLE) {
            handler.post(loop);
        } else {
            glitchFlash = false;
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
        if (w <= 6 || h <= 6) return;

        // Cyber terminal frame
        canvas.drawRect(0.5f, 0.5f, w - 0.5f, h - 0.5f, bgPaint);
        borderPaint.setColor(glitchFlash ? 0xFF00F0FF : 0xFF157A42);
        canvas.drawRect(0.5f, 0.5f, w - 0.5f, h - 0.5f, borderPaint);

        // Pulsing status LED dot
        boolean pulseOn = !running || ((tick / 5) % 2 == 0);
        dotPaint.setColor(glitchFlash ? 0xFFFF2A55 : (pulseOn ? 0xFF00FF66 : 0xFF0C522B));
        float dotX = 6f * density;
        float dotY = h * 0.5f;
        canvas.drawCircle(dotX, dotY, 2.3f * density, dotPaint);

        String line = dynamicFeed[msgIdx];
        int show = running ? Math.min(line.length(), typedChars) : line.length();
        float tx = 12f * density;
        float ty = h * 0.5f + 3.6f * density;

        canvas.save();
        canvas.clipRect(tx - 1f, 1f, w - 3f * density, h - 1f);
        if (glitchFlash) {
            canvas.drawText(line, 0, show, tx + 2f * density, ty, glitchPaint);
            canvas.drawText(line, 0, show, tx - 1.5f * density, ty - 0.5f * density, textPaint);
        } else {
            canvas.drawText(line, 0, show, tx, ty, textPaint);
        }

        // Blinking typewriter cursor (|)
        boolean blinkOn = !running || ((tick / 4) % 2 == 0);
        if (blinkOn) {
            float tw = textPaint.measureText(line, 0, show);
            float cx = tx + tw + 1.5f * density;
            if (cx < w - 5f * density) {
                cursorPaint.setColor(glitchFlash ? 0xFF00F0FF : 0xFF00FF66);
                canvas.drawRect(cx, ty - 9f * density, cx + 4.5f * density, ty + 1.5f * density, cursorPaint);
            }
        }
        canvas.restore();
    }
}
