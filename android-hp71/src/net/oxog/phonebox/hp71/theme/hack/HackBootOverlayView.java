package net.oxog.phonebox.hp71.theme.hack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Sequência de inicialização / invasão animada do tema Hack (CRT Power-On + Typewriter + Cursor piscante + Glitch Dissolve).
 * Inspirada diretamente na animação typewriter com cursor piscante, expandida em sequência completa de cyberdeck.
 * Toque em qualquer lugar para pular instantaneamente.
 */
public final class HackBootOverlayView extends View {

    private static final String[] LINES = {
            "> [SYS_BREACH] BOOTING ZERO-DAY TERMINAL v7.1",
            "> h4ck3d_m0d3 :: PH0N3B0X_HP71 [R00T_OK]",
            "> loading m4tr1x_r41n + crt_gl1tch_c0r3...",
            "> overriding display_hud :: 1024x600 // API_17",
            "> @system.root :: ACCESS GRANTED"
    };

    private static final String[] HEX_SIDE = {
            "0x7F00: 4A 9C E1 00",
            "0x7F10: FF 3B 88 12",
            "0x7F20: 00 FF 66 A9",
            "0x7F30: C0 DE 13 37",
            "0x7F40: 91 0F 44 D2",
            "0x7F50: 71 HP 42 20"
    };

    private static final long TOTAL_MS = 2450L;
    private static final long FRAME_MS = 38L;

    private final Paint bgPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dimTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cyanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cursorPaint = new Paint();
    private final Paint barBgPaint = new Paint();
    private final Paint barFillPaint = new Paint();
    private final Paint scanPaint = new Paint();

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

    public HackBootOverlayView(Context context) {
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
        bgPaint.setColor(0xF5000000);

        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setColor(0xFF00FF66);
        textPaint.setTextSize(15f * density);

        dimTextPaint.setTypeface(Typeface.MONOSPACE);
        dimTextPaint.setColor(0xFF2E8F57);
        dimTextPaint.setTextSize(11f * density);

        cyanPaint.setTypeface(Typeface.MONOSPACE);
        cyanPaint.setColor(0xFF00F0FF);
        cyanPaint.setTextSize(15f * density);

        redPaint.setStyle(Paint.Style.FILL);
        redPaint.setTypeface(Typeface.MONOSPACE);
        redPaint.setTextSize(15f * density);
        redPaint.setColor(0x99FF2A55);

        cursorPaint.setStyle(Paint.Style.FILL);
        cursorPaint.setColor(0xFF00FF66);

        barBgPaint.setStyle(Paint.Style.STROKE);
        barBgPaint.setStrokeWidth(Math.max(1f, 1.5f * density));
        barBgPaint.setColor(0xFF1A6B3E);

        barFillPaint.setStyle(Paint.Style.FILL);
        barFillPaint.setColor(0xFF00FF66);

        scanPaint.setStyle(Paint.Style.STROKE);
        scanPaint.setStrokeWidth(Math.max(1f, density * 0.8f));
        scanPaint.setColor(0x1E00FF66);
    }

    public void play(Runnable onDone) {
        this.onFinished = onDone;
        this.startMs = System.currentTimeMillis();
        this.playing = true;
        setAlpha(1f);
        setVisibility(VISIBLE);
        handler.removeCallbacks(ticker);
        if (attached) handler.post(ticker);
    }

    public void stop() {
        boolean wasPlaying = playing;
        playing = false;
        handler.removeCallbacks(ticker);
        setVisibility(GONE);
        if (wasPlaying && onFinished != null) {
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

        // Fade-out shutter during final 380ms
        int bgAlpha = 246;
        if (elapsed > TOTAL_MS - 380L) {
            float f = 1f - ((elapsed - (TOTAL_MS - 380L)) / 380f);
            bgAlpha = Math.max(0, Math.round(246f * f));
        }
        bgPaint.setAlpha(bgAlpha);
        canvas.drawRect(0f, 0f, w, h, bgPaint);

        // CRT horizontal scanlines
        for (float y = 0f; y < h; y += 5f * density) {
            canvas.drawLine(0f, y, w, y, scanPaint);
        }

        // Phase 1: CRT Power-on horizontal beam (0 - 200ms)
        if (elapsed < 200L) {
            float p = elapsed / 200f;
            float cy = h * 0.5f;
            float halfH = Math.max(2f * density, (h * 0.48f) * p * p);
            barFillPaint.setColor(0xCC00FF66);
            canvas.drawRect(0f, cy - halfH, w, cy - halfH + 3f * density, barFillPaint);
            canvas.drawRect(0f, cy + halfH - 3f * density, w, cy + halfH, barFillPaint);
            canvas.drawRect(0f, cy - 2f * density, w, cy + 2f * density, cyanPaint);
            return;
        }

        // Phase 2: Multi-line step-by-step Typewriter with blinking block cursor
        float padX = Math.max(22f * density, w * 0.08f);
        float startY = Math.max(76f * density, h * 0.24f);
        float lineStep = 28f * density;
        boolean portrait = h > w;
        float txtSp = (portrait ? 12.5f : 15.5f) * density;
        textPaint.setTextSize(txtSp);
        cyanPaint.setTextSize(txtSp);
        redPaint.setTextSize(txtSp);

        long typeElapsed = elapsed - 200L;
        int charsPerSec = 98;
        int totalCharsBudget = (int) ((typeElapsed * charsPerSec) / 1000L);
        int remaining = totalCharsBudget;
        float cursorX = padX;
        float cursorY = startY;

        for (int i = 0; i < LINES.length; i++) {
            String line = LINES[i];
            float y = startY + i * lineStep;
            if (remaining <= 0) break;
            int showLen = Math.min(line.length(), remaining);
            remaining -= line.length() + 5; // crisp pause between lines

            boolean isFinalGrant = (i == LINES.length - 1);
            Paint p = isFinalGrant ? cyanPaint : textPaint;
            if (isFinalGrant && showLen == line.length()) {
                // Chromatic ghost echo on ACCESS GRANTED
                canvas.drawText(line, 0, showLen, padX - 2f * density, y, redPaint);
                canvas.drawText(line, 0, showLen, padX + 2f * density, y, textPaint);
            }
            canvas.drawText(line, 0, showLen, padX, y, p);
            cursorX = padX + p.measureText(line, 0, showLen) + 3f * density;
            cursorY = y;
        }

        // Blinking terminal cursor (block █)
        boolean cursorOn = ((elapsed / 180L) % 2L) == 0L;
        if (cursorOn) {
            float cw = 9f * density;
            float ch = (portrait ? 13f : 16f) * density;
            canvas.drawRect(cursorX, cursorY - ch + 2f * density, cursorX + cw, cursorY + 2f * density, cursorPaint);
        }

        // Segmented breach progress bar
        float barY = startY + (LINES.length + 0.6f) * lineStep;
        float barW = Math.min(w - padX * 2f, 440f * density);
        float barH = 14f * density;
        canvas.drawRect(padX, barY, padX + barW, barY + barH, barBgPaint);

        float loadFrac = Math.min(1f, Math.max(0f, (elapsed - 240L) / 1650f));
        int segments = 24;
        int lit = Math.round(segments * loadFrac);
        float segW = (barW - 6f * density) / segments;
        for (int s = 0; s < lit; s++) {
            float sx = padX + 3f * density + s * segW;
            barFillPaint.setColor(s == lit - 1 ? 0xFF00F0FF : 0xFF00FF66);
            canvas.drawRect(sx, barY + 3f * density, sx + Math.max(2f, segW - 2.5f * density), barY + barH - 3f * density, barFillPaint);
        }

        int pct = Math.round(loadFrac * 100f);
        canvas.drawText("SYNC " + pct + "%  [TOQUE PARA PULAR]", padX, barY + barH + 20f * density, dimTextPaint);

        // Live scrolling hex dump on right edge in landscape
        if (!portrait && w > 680f * density) {
            float hx = w - 175f * density;
            int shift = (int) (elapsed / 95L);
            for (int i = 0; i < HEX_SIDE.length; i++) {
                String hex = HEX_SIDE[(i + shift) % HEX_SIDE.length];
                canvas.drawText(hex, hx, startY + i * 20f * density, dimTextPaint);
            }
        }

        // Phase 3: Final RGB Glitch Tear Dissolve (last 380ms)
        if (elapsed > TOTAL_MS - 380L) {
            int f = (int) (elapsed / 35L);
            for (int s = 0; s < 5; s++) {
                float sy = ((f * 97 + s * 131) % Math.max(1, h));
                float sh = (4f + (s * 3)) * density;
                canvas.drawRect(0f, sy, w, sy + sh, (s % 2 == 0) ? redPaint : cyanPaint);
            }
        }
    }
}
