package net.oxog.phonebox.hp71.theme.hack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

/**
 * Overlay full-screen transparente ao toque para efeitos CRT & Glitch visual no tema Hack:
 * - Feixe de varredura fosforescente (CRT refresh sweep) suave;
 * - Moldura HUD nos 4 cantos com micro-telemetria acima da barra preta do Android 4.2.2;
 * - Surtos orgânicos de "bug visual" (fatiamento horizontal RGB ciano/vermelho/verde, blocos de estática,
 *   micro-jitter horizontal de sincronismo na UI e alerta-relâmpago de invasão);
 * - Varredura CRT instantânea ao trocar de aba.
 * Zero alocações em onDraw(), 100% API 17 e nunca bloqueia eventos de toque.
 */
public final class HackGlitchOverlayView extends View {

    public interface GlitchCallbacks {
        void onUiHorizontalJitter(float translationXPx);
        void onGlitchBurstStarted(int intensity, int durationMs);
    }

    private static final String[] FLASH_LOGS = {
            ">> SYS_GLITCH :: 0x4F",
            "[PKT_INJ] R00T_SYNC",
            "> CRT_D3SYNC // 60Hz",
            "[0V3RR1D3] M3M_7F",
            ">> H4CK3D_51GN4L",
            "[!] BYP455_OK"
    };

    private static final int MAX_SLICES = 6;
    private static final int MAX_BLOCKS = 8;

    private final Paint scanLinePaint = new Paint();
    private final Paint beamPaint = new Paint();
    private final Paint sliceCyanPaint = new Paint();
    private final Paint sliceRedPaint = new Paint();
    private final Paint sliceGreenPaint = new Paint();
    private final Paint blockPaint = new Paint();
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint logBgPaint = new Paint();
    private final Paint logTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float[] sliceY = new float[MAX_SLICES];
    private final float[] sliceH = new float[MAX_SLICES];
    private final float[] sliceShift = new float[MAX_SLICES];
    private int sliceCount = 0;

    private final float[] blkX = new float[MAX_BLOCKS];
    private final float[] blkY = new float[MAX_BLOCKS];
    private final float[] blkW = new float[MAX_BLOCKS];
    private final float[] blkH = new float[MAX_BLOCKS];
    private final int[] blkColor = new int[MAX_BLOCKS];
    private int blockCount = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private GlitchCallbacks callbacks;

    private float density = 1f;
    private boolean hackActive = false;
    private boolean effectsRunning = false;
    private boolean attached = false;

    private float scanBeamY = -40f;
    private long burstEndMs = 0L;
    private long nextAutoBurstMs = 0L;
    private int burstFrame = 0;
    private boolean tabWipeActive = false;
    private float tabWipeY = 0f;
    private String activeFlashLog = null;
    private float flashLogX = 0f;
    private float flashLogY = 0f;
    private int rng = 0x1337C0DE;

    private final Runnable animTick = new Runnable() {
        @Override
        public void run() {
            if (!hackActive || !effectsRunning || !attached) return;
            long now = System.currentTimeMillis();
            int h = getHeight();
            int w = getWidth();

            // 1. Move ambient CRT refresh beam smoothly
            if (h > 0) {
                scanBeamY += 4.2f * density;
                if (scanBeamY > h + 90f * density) {
                    scanBeamY = -90f * density;
                }
            }

            // 2. Advance tab-switch CRT wipe
            if (tabWipeActive && h > 0) {
                tabWipeY += Math.max(26f * density, h * 0.19f);
                if (tabWipeY > h + 24f * density) {
                    tabWipeActive = false;
                }
            }

            // 3. Auto-schedule organic glitch bursts every 3.6s - 7.4s
            if (now >= nextAutoBurstMs && now > burstEndMs) {
                startBurstInternal(1, 220 + (Math.abs(nextRand()) % 170));
                scheduleNextBurst(now);
            }

            // 4. Update active burst artifacts
            boolean inBurst = now < burstEndMs;
            if (inBurst && w > 0 && h > 0) {
                burstFrame++;
                regenerateBurstGeometry(w, h);
                if (callbacks != null) {
                    // Brief horizontal sync-jitter only on peak burst frames
                    float jitter = (burstFrame % 3 == 1)
                            ? (((nextRand() & 1) == 0 ? 1f : -1f) * (2.2f + (Math.abs(nextRand()) % 3)) * density)
                            : 0f;
                    callbacks.onUiHorizontalJitter(jitter);
                }
            } else if (sliceCount > 0 || blockCount > 0 || activeFlashLog != null) {
                sliceCount = 0;
                blockCount = 0;
                activeFlashLog = null;
                if (callbacks != null) callbacks.onUiHorizontalJitter(0f);
            }

            invalidate();
            long nextDelay = (inBurst || tabWipeActive) ? 45L : 95L;
            handler.postDelayed(this, nextDelay);
        }
    };

    public HackGlitchOverlayView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        if (this.density <= 0f) this.density = 1f;
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        scanLinePaint.setStyle(Paint.Style.STROKE);
        scanLinePaint.setStrokeWidth(Math.max(1f, density * 0.7f));
        scanLinePaint.setColor(0x0D00FF66);

        beamPaint.setStyle(Paint.Style.FILL);

        sliceCyanPaint.setStyle(Paint.Style.FILL);
        sliceCyanPaint.setColor(0x4A00F0FF);

        sliceRedPaint.setStyle(Paint.Style.FILL);
        sliceRedPaint.setColor(0x46FF2A55);

        sliceGreenPaint.setStyle(Paint.Style.FILL);
        sliceGreenPaint.setColor(0x5400FF66);

        blockPaint.setStyle(Paint.Style.FILL);

        hudPaint.setStyle(Paint.Style.STROKE);
        hudPaint.setStrokeWidth(Math.max(1.2f, 1.5f * density));
        hudPaint.setColor(0x9900FF66);

        logBgPaint.setStyle(Paint.Style.FILL);
        logBgPaint.setColor(0xD8020D07);

        logTextPaint.setTypeface(Typeface.MONOSPACE);
        logTextPaint.setTextSize(10.5f * density);
        logTextPaint.setColor(0xFF00F0FF);
    }

    public void setCallbacks(GlitchCallbacks cb) {
        this.callbacks = cb;
    }

    public void setState(boolean hackActive, boolean effectsEnabled) {
        this.hackActive = hackActive;
        this.effectsRunning = hackActive && effectsEnabled;
        setVisibility(hackActive ? VISIBLE : GONE);
        handler.removeCallbacks(animTick);
        if (!this.effectsRunning) {
            sliceCount = 0;
            blockCount = 0;
            tabWipeActive = false;
            activeFlashLog = null;
            if (callbacks != null) callbacks.onUiHorizontalJitter(0f);
            invalidate();
            return;
        }
        if (attached) {
            scheduleNextBurst(System.currentTimeMillis());
            handler.post(animTick);
        }
    }

    public void triggerTabGlitch() {
        if (!hackActive || !effectsRunning) return;
        tabWipeActive = true;
        tabWipeY = 0f;
        startBurstInternal(1, 190);
        handler.removeCallbacks(animTick);
        if (attached) handler.post(animTick);
    }

    public void triggerManualGlitchBurst() {
        if (!hackActive) return;
        tabWipeActive = true;
        tabWipeY = 0f;
        startBurstInternal(2, 360);
        if (effectsRunning && attached) {
            handler.removeCallbacks(animTick);
            handler.post(animTick);
        }
    }

    private void startBurstInternal(int intensity, int durationMs) {
        long now = System.currentTimeMillis();
        burstEndMs = Math.max(burstEndMs, now + durationMs);
        burstFrame = 0;
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            regenerateBurstGeometry(w, h);
            if (intensity >= 1 && (Math.abs(nextRand()) % 10 < 7)) {
                activeFlashLog = FLASH_LOGS[Math.abs(nextRand()) % FLASH_LOGS.length];
                boolean rightSide = (nextRand() & 1) == 0;
                flashLogX = rightSide ? Math.max(12f * density, w - 165f * density) : 14f * density;
                flashLogY = 46f * density + (Math.abs(nextRand()) % Math.max(1, (int) (h * 0.65f)));
            }
        }
        if (callbacks != null) {
            callbacks.onGlitchBurstStarted(intensity, durationMs);
        }
    }

    private void scheduleNextBurst(long now) {
        nextAutoBurstMs = now + 3600L + (Math.abs(nextRand()) % 3900);
    }

    private void regenerateBurstGeometry(int w, int h) {
        sliceCount = 2 + (Math.abs(nextRand()) % (MAX_SLICES - 1));
        for (int i = 0; i < sliceCount; i++) {
            sliceY[i] = Math.abs(nextRand()) % Math.max(1, h);
            sliceH[i] = (1.5f + (Math.abs(nextRand()) % 7)) * density;
            sliceShift[i] = (((nextRand() & 1) == 0 ? 1f : -1f) * (5f + (Math.abs(nextRand()) % 14))) * density;
        }
        blockCount = 3 + (Math.abs(nextRand()) % (MAX_BLOCKS - 2));
        for (int i = 0; i < blockCount; i++) {
            // Favor edges & bands so center content remains effortlessly legible
            boolean edge = (i % 2 == 0);
            if (edge) {
                blkX[i] = ((nextRand() & 1) == 0)
                        ? (Math.abs(nextRand()) % Math.max(1, w / 5))
                        : (w - (Math.abs(nextRand()) % Math.max(1, w / 5)) - 22f * density);
            } else {
                blkX[i] = Math.abs(nextRand()) % Math.max(1, w);
            }
            blkY[i] = Math.abs(nextRand()) % Math.max(1, h);
            blkW[i] = (8f + (Math.abs(nextRand()) % 28)) * density;
            blkH[i] = (2f + (Math.abs(nextRand()) % 7)) * density;
            int pick = Math.abs(nextRand()) % 3;
            blkColor[i] = pick == 0 ? 0x6600FF66 : (pick == 1 ? 0x6600F0FF : 0x5EFF2A55);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (hackActive && effectsRunning) {
            scheduleNextBurst(System.currentTimeMillis());
            handler.removeCallbacks(animTick);
            handler.post(animTick);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        handler.removeCallbacks(animTick);
        if (callbacks != null) callbacks.onUiHorizontalJitter(0f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false; // Always pass touches straight through to UI below
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!hackActive) return;
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. Subtle CRT horizontal scanline sheen (sparse 6dp interval — crisp & ultra-fast on API 17)
        float step = Math.max(5f, 6f * density);
        for (float y = 0f; y < h; y += step) {
            canvas.drawLine(0f, y, w, y, scanLinePaint);
        }

        // 2. Ambient sweeping phosphor refresh beam
        if (effectsRunning && scanBeamY > -70f * density && scanBeamY < h + 70f * density) {
            beamPaint.setColor(0x0E00FF66);
            canvas.drawRect(0f, scanBeamY - 26f * density, w, scanBeamY + 26f * density, beamPaint);
            beamPaint.setColor(0x2600FF66);
            canvas.drawRect(0f, scanBeamY - 2f * density, w, scanBeamY + 2f * density, beamPaint);
        }

        // 3. Tab-switch laser CRT wipe
        if (tabWipeActive) {
            beamPaint.setColor(0x5500F0FF);
            canvas.drawRect(0f, tabWipeY - 12f * density, w, tabWipeY + 3f * density, beamPaint);
            beamPaint.setColor(0xCC00FF66);
            canvas.drawRect(0f, tabWipeY, w, tabWipeY + 2.5f * density, beamPaint);
        }

        // 4. Visual glitch burst: RGB chromatic slice tears + corruption blocks + flash log
        if (sliceCount > 0) {
            for (int i = 0; i < sliceCount; i++) {
                float y = sliceY[i];
                float sh = sliceH[i];
                float dx = sliceShift[i];
                canvas.drawRect(Math.max(0f, dx), y, Math.min(w, w + dx), y + sh, sliceCyanPaint);
                canvas.drawRect(Math.max(0f, -dx), y + density, Math.min(w, w - dx), y + sh + density, sliceRedPaint);
                if (i % 2 == 0) {
                    canvas.drawRect(0f, y, w, y + Math.max(1f, sh * 0.4f), sliceGreenPaint);
                }
            }
        }
        if (blockCount > 0) {
            for (int i = 0; i < blockCount; i++) {
                blockPaint.setColor(blkColor[i]);
                canvas.drawRect(blkX[i], blkY[i], blkX[i] + blkW[i], blkY[i] + blkH[i], blockPaint);
            }
        }
        if (activeFlashLog != null) {
            float tw = logTextPaint.measureText(activeFlashLog) + 12f * density;
            float th = 17f * density;
            canvas.drawRect(flashLogX, flashLogY - th + 4f * density, flashLogX + tw, flashLogY + 4f * density, logBgPaint);
            canvas.drawRect(flashLogX, flashLogY - th + 4f * density, flashLogX + 2.5f * density, flashLogY + 4f * density, sliceRedPaint);
            canvas.drawText(activeFlashLog, flashLogX + 6f * density, flashLogY, logTextPaint);
        }

        // 5. Crisp cyberdeck viewport corner reticles (frames the HP 7.1 screen cleanly above the black nav bar)
        float m = 3f * density;
        float cLen = 11f * density;
        // Top-left & Top-right
        canvas.drawLine(m, m, m + cLen, m, hudPaint);
        canvas.drawLine(m, m, m, m + cLen, hudPaint);
        canvas.drawLine(w - m - cLen, m, w - m, m, hudPaint);
        canvas.drawLine(w - m, m, w - m, m + cLen, hudPaint);
        // Bottom-left & Bottom-right (sits right on the boundary with the solid black Android 4.2.2 bar!)
        canvas.drawLine(m, h - m, m + cLen, h - m, hudPaint);
        canvas.drawLine(m, h - m - cLen, m, h - m, hudPaint);
        canvas.drawLine(w - m - cLen, h - m, w - m, h - m, hudPaint);
        canvas.drawLine(w - m, h - m - cLen, w - m, h - m, hudPaint);
    }

    private int nextRand() {
        int x = rng;
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        rng = x;
        return x;
    }
}
