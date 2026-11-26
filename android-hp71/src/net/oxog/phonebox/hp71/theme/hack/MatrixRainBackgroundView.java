package net.oxog.phonebox.hp71.theme.hack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Fundo full-screen de chuva digital Matrix 100% nativo em Canvas (API 17).
 * - Multi-profundidade (colunas lentas ao fundo + fluxos brilhantes com cabeça branca-fosforescente);
 * - Grade tática de micro-cruzes (+) e pulso de radar sutil;
 * - Reage a picos de glitch alterando colunas para ciano (#00F0FF) e vermelho (#FF2A55);
 * - Usa exclusivamente caracteres ASCII/Latin-1 garantidos no DroidSansMono do Android 4.2.2 (zero tofu);
 * - Zero alocações em onDraw() e pausa total em onPause()/detach.
 */
public final class MatrixRainBackgroundView extends View {

    private static final char[] GLYPHS = (
            "010101010123456789ABCDEF"
                    + "h4ck3r$#@%&*+=<>/?[]{}:;_|~^"
                    + "01X7Z9Q4W3E5R7T8Y0"
    ).toCharArray();

    private static final int MAX_COLS = 44;
    private static final long FRAME_MS = 55L; // ~18 FPS suave e leve no Cortex-A7 1GHz

    private final Paint glyphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint();
    private final Paint pulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final float[] colY = new float[MAX_COLS];
    private final float[] colSpeed = new float[MAX_COLS];
    private final int[] colTrail = new int[MAX_COLS];
    private final int[] colTier = new int[MAX_COLS]; // 0 = dim bg, 1 = mid, 2 = bright hero
    private final int[] colCharSeed = new int[MAX_COLS];
    private final char[] drawBuf = new char[1];

    private int activeCols = 0;
    private float cellW = 24f;
    private float cellH = 18f;
    private float density = 1f;
    private int rng = 0x5A71C3B9;
    private long tickCount = 0L;

    private boolean running = false;
    private boolean attached = false;
    private long surgeUntilMs = 0L;
    private float radarRadius = 0f;

    private final Runnable frameLoop = new Runnable() {
        @Override
        public void run() {
            if (!running || !attached || getVisibility() != VISIBLE) return;
            stepSimulation();
            invalidate();
            handler.postDelayed(this, FRAME_MS);
        }
    };

    public MatrixRainBackgroundView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        if (this.density <= 0f) this.density = 1f;
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        glyphPaint.setTypeface(Typeface.MONOSPACE);
        glyphPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(Math.max(1f, density * 0.8f));
        gridPaint.setColor(0x1E00FF66);

        pulsePaint.setStyle(Paint.Style.STROKE);
        pulsePaint.setStrokeWidth(Math.max(1f, 1.2f * density));
    }

    public void setRunning(boolean enabled) {
        if (this.running == enabled) return;
        this.running = enabled;
        handler.removeCallbacks(frameLoop);
        if (running && attached && getVisibility() == VISIBLE) {
            handler.post(frameLoop);
        } else {
            invalidate();
        }
    }

    public void triggerGlitchSurge(int durationMs) {
        long until = System.currentTimeMillis() + Math.max(120, durationMs);
        if (until > surgeUntilMs) surgeUntilMs = until;
        if (running && attached) invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (running && getVisibility() == VISIBLE) {
            handler.removeCallbacks(frameLoop);
            handler.post(frameLoop);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        handler.removeCallbacks(frameLoop);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        float targetStep = 26f * density;
        activeCols = Math.max(10, Math.min(MAX_COLS, Math.round(w / targetStep)));
        cellW = (float) w / (float) activeCols;
        cellH = 15.5f * density;
        glyphPaint.setTextSize(13f * density);
        initColumns(h);
    }

    private void initColumns(int h) {
        int rows = Math.max(12, Math.round(h / cellH));
        for (int i = 0; i < activeCols; i++) {
            colY[i] = (nextRand() & 0x7FFF) % (rows + 14) - 8;
            resetColumnParams(i, false);
        }
    }

    private void resetColumnParams(int i, boolean topReset) {
        int r = nextRand() & 0xFFFF;
        int tier = (r % 10 < 4) ? 0 : ((r % 10 < 8) ? 1 : 2);
        colTier[i] = tier;
        if (tier == 0) {
            colSpeed[i] = 0.22f + ((r & 0x1F) / 110f);
            colTrail[i] = 7 + (r % 7);
        } else if (tier == 1) {
            colSpeed[i] = 0.42f + ((r & 0x1F) / 70f);
            colTrail[i] = 9 + (r % 8);
        } else {
            colSpeed[i] = 0.68f + ((r & 0x1F) / 55f);
            colTrail[i] = 11 + (r % 9);
        }
        colCharSeed[i] = nextRand() & 0x7FFFFFFF;
        if (topReset) {
            colY[i] = -(2 + (r % 10));
        }
    }

    private void stepSimulation() {
        tickCount++;
        int h = getHeight();
        if (h <= 0 || activeCols <= 0) return;
        float maxRows = (h / cellH) + 2f;
        boolean surge = System.currentTimeMillis() < surgeUntilMs;
        float speedMul = surge ? 1.75f : 1.0f;

        for (int i = 0; i < activeCols; i++) {
            colY[i] += colSpeed[i] * speedMul;
            if ((tickCount + i) % (surge ? 2 : 5) == 0) {
                colCharSeed[i] = (colCharSeed[i] * 1103515245 + 12345) & 0x7FFFFFFF;
            }
            if (colY[i] - colTrail[i] > maxRows) {
                resetColumnParams(i, true);
            }
        }

        // Subtle ambient radar pulse expanding from bottom-right
        float maxR = Math.max(getWidth(), h) * 1.15f;
        radarRadius += (surge ? 9f : 3.2f) * density;
        if (radarRadius > maxR) {
            radarRadius = -180f * density; // quiet interval between pulses
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Pure pitch-black terminal canvas — merges seamlessly into Android 4.2.2 fixed black nav bar
        canvas.drawColor(0xFF000000);

        // Subtle cyberdeck coordinate grid crosses (+)
        float gridStep = 64f * density;
        float cross = 3f * density;
        for (float gx = gridStep * 0.5f; gx < w; gx += gridStep) {
            for (float gy = gridStep * 0.5f; gy < h; gy += gridStep) {
                canvas.drawLine(gx - cross, gy, gx + cross, gy, gridPaint);
                canvas.drawLine(gx, gy - cross, gx, gy + cross, gridPaint);
            }
        }

        // Ambient radar ring sweep
        if (radarRadius > 10f && running) {
            float maxR = Math.max(w, h) * 1.15f;
            float fade = 1f - Math.min(1f, radarRadius / maxR);
            int alpha = Math.round(fade * 36f);
            if (alpha > 2) {
                pulsePaint.setColor((alpha << 24) | 0x0000FF66);
                canvas.drawCircle(w * 0.85f, h * 0.85f, radarRadius, pulsePaint);
            }
        }

        if (activeCols <= 0) return;
        boolean surge = System.currentTimeMillis() < surgeUntilMs;
        int glyphLen = GLYPHS.length;

        for (int col = 0; col < activeCols; col++) {
            float cx = (col + 0.5f) * cellW;
            int headRow = (int) colY[col];
            int trail = colTrail[col];
            int tier = colTier[col];
            int seed = colCharSeed[col];

            // During glitch surge, turn select columns electric cyan or crimson alert
            boolean cyanCol = surge && ((col + (int) tickCount) % 5 == 0);
            boolean redCol = surge && !cyanCol && ((col * 3 + (int) tickCount) % 11 == 0);

            for (int t = 0; t < trail; t++) {
                int row = headRow - t;
                float py = row * cellH;
                if (py < -cellH || py > h + cellH) continue;

                int alpha;
                int rgb;
                if (t == 0) {
                    if (redCol) {
                        rgb = 0xFF5577;
                        alpha = 235;
                    } else if (cyanCol) {
                        rgb = 0xB8FFFF;
                        alpha = 235;
                    } else {
                        rgb = tier == 2 ? 0xEEFFF5 : (tier == 1 ? 0x99FFCC : 0x33BB66);
                        alpha = tier == 2 ? 230 : (tier == 1 ? 165 : 90);
                    }
                } else {
                    float frac = 1f - ((float) t / (float) trail);
                    float maxA = tier == 2 ? 155f : (tier == 1 ? 105f : 55f);
                    if (!running) maxA *= 0.45f;
                    alpha = Math.max(8, Math.round(frac * frac * maxA));
                    if (redCol) {
                        rgb = 0xFF2A55;
                    } else if (cyanCol) {
                        rgb = 0x00F0FF;
                    } else {
                        rgb = t <= 2 ? 0x00FF66 : 0x00BB4D;
                    }
                }

                glyphPaint.setColor((alpha << 24) | rgb);
                int idx = Math.abs((seed + row * 17 + col * 31 + (t == 0 ? (int) tickCount : 0)) % glyphLen);
                drawBuf[0] = GLYPHS[idx];
                canvas.drawText(drawBuf, 0, 1, cx, py, glyphPaint);
            }
        }
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
