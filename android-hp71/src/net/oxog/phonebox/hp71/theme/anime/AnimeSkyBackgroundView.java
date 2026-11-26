package net.oxog.phonebox.hp71.theme.anime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import net.oxog.phonebox.hp71.R;
import net.oxog.phonebox.hp71.theme.ThemeColors;

/**
 * Motor unificado de mundos animados full-screen (Theme Studio v2.0 — 100% API 17):
 * - "sakura": Sakura Storm (céu crepuscular, lua crescente, galho de cerejeira com lanternas balançando, pétalas 3D, meteoros);
 * - "lofi": Lofi Room (janela noturna chuvosa, skyline, pisca-pisca quente, chuva + gotas no vidro, abajur pêssego, vapor de chá);
 * - "starfall": Starfall Aurora (Via Láctea, cortinas de Aurora Boreal esmeralda/violeta, constelações autodesenhadas, Torii + vaga-lumes);
 * - "miku": Hatsune Miku 01 (palco vocaloid ciano/magenta, ondas synth, equalizador de 22 bandas, notas musicais, holograma Miku).
 * Zero alocações em onDraw(), pausa completa em onPause() e transição suave para a barra preta do Android 4.2.2.
 */
public final class AnimeSkyBackgroundView extends View {

    private static final int STAR_COUNT = 42;
    private static final int PARTICLE_COUNT = 26;
    private static final int RAIN_COUNT = 34;
    private static final int EQ_BARS = 22;
    private static final long FRAME_MS = 50L; // ~20 FPS suave no Cortex-A7 1GHz

    // Constelações normalizadas (x, y) + pares de arestas para o mundo Starfall
    private static final float[][] CONST_PTS = {
            // Orion-like
            { 0.14f, 0.20f, 0.24f, 0.17f, 0.21f, 0.30f, 0.18f, 0.31f, 0.15f, 0.32f, 0.26f, 0.43f, 0.12f, 0.44f },
            // Cassiopeia W
            { 0.56f, 0.15f, 0.63f, 0.24f, 0.70f, 0.17f, 0.77f, 0.26f, 0.85f, 0.16f },
            // Cygnus Cross
            { 0.36f, 0.14f, 0.42f, 0.24f, 0.48f, 0.35f, 0.31f, 0.28f, 0.53f, 0.20f }
    };
    private static final int[][] CONST_EDGES = {
            { 0, 1, 1, 2, 2, 3, 3, 4, 4, 0, 2, 5, 4, 6 },
            { 0, 1, 1, 2, 2, 3, 3, 4 },
            { 0, 1, 1, 2, 3, 1, 1, 4 }
    };

    private final Paint skyPaint = new Paint();
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonCutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint meteorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final Path workPath = new Path();
    private final Path starPath = new Path();
    private final RectF workRect = new RectF();
    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final int[] starColor = new int[STAR_COUNT];
    private final boolean[] starDiamond = new boolean[STAR_COUNT];

    // Partículas reutilizadas entre mundos (pétalas / vaga-lumes / notas musicais / gotas lentas)
    private final float[] ptX = new float[PARTICLE_COUNT];
    private final float[] ptY = new float[PARTICLE_COUNT];
    private final float[] ptVx = new float[PARTICLE_COUNT];
    private final float[] ptVy = new float[PARTICLE_COUNT];
    private final float[] ptSize = new float[PARTICLE_COUNT];
    private final float[] ptAngle = new float[PARTICLE_COUNT];
    private final float[] ptSpin = new float[PARTICLE_COUNT];
    private final float[] ptPhase = new float[PARTICLE_COUNT];
    private final int[] ptColor = new int[PARTICLE_COUNT];

    // Chuva inclinada (Lofi)
    private final float[] rainX = new float[RAIN_COUNT];
    private final float[] rainY = new float[RAIN_COUNT];
    private final float[] rainLen = new float[RAIN_COUNT];
    private final float[] rainSpd = new float[RAIN_COUNT];

    // Bandas do equalizador vocaloid (Miku)
    private final float[] eqLevel = new float[EQ_BARS];
    private final float[] eqPeak = new float[EQ_BARS];

    // Meteoro / relâmpago
    private boolean meteorActive = false;
    private float meteorX, meteorY, meteorVx, meteorVy;
    private float meteorLife = 0f;
    private long nextMeteorAtMs = 0L;
    private float lofiFlash = 0f;
    private long nextFlashAtMs = 0L;

    private String world = "sakura";
    private Bitmap mikuBitmap = null;
    private boolean mikuLoadTried = false;

    private float density = 1f;
    private float timeSec = 0f;
    private int rng = 0x5A4B5552;
    private boolean running = false;
    private boolean attached = false;
    private long burstUntilMs = 0L;

    private final Runnable frameLoop = new Runnable() {
        @Override
        public void run() {
            if (!running || !attached || getVisibility() != VISIBLE) return;
            stepSimulation();
            invalidate();
            handler.postDelayed(this, FRAME_MS);
        }
    };

    public AnimeSkyBackgroundView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        if (this.density <= 0f) this.density = 1f;
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        moonPaint.setStyle(Paint.Style.FILL);
        moonCutPaint.setStyle(Paint.Style.FILL);
        shapePaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        meteorPaint.setStyle(Paint.Style.STROKE);
        meteorPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setWorld(String worldId) {
        String norm = ThemeColors.normalizePresetId(worldId);
        if (!ThemeColors.isLivingWorldId(norm)) norm = "sakura";
        if (norm.equals(this.world)) return;
        this.world = norm;
        if ("miku".equals(norm)) ensureMikuBitmap();
        if (getWidth() > 0 && getHeight() > 0) {
            rebuildShaders(getWidth(), getHeight());
            initScene(getWidth(), getHeight());
        }
        invalidate();
    }

    private void ensureMikuBitmap() {
        if (mikuBitmap != null || mikuLoadTried) return;
        mikuLoadTried = true;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            mikuBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.miku_mascot, opts);
        } catch (Throwable ignored) {
            mikuBitmap = null;
        }
    }

    public void setRunning(boolean enabled) {
        if (this.running == enabled) return;
        this.running = enabled;
        handler.removeCallbacks(frameLoop);
        if (running && attached && getVisibility() == VISIBLE) {
            long now = System.currentTimeMillis();
            nextMeteorAtMs = now + 1600L;
            nextFlashAtMs = now + 6500L;
            handler.post(frameLoop);
        } else {
            invalidate();
        }
    }

    public void triggerSakuraBurst() {
        long now = System.currentTimeMillis();
        burstUntilMs = now + 950L;
        if ("lofi".equals(world)) {
            lofiFlash = 0.55f;
        } else if (!meteorActive && getWidth() > 0 && getHeight() > 0) {
            spawnMeteor(getWidth(), getHeight());
        }
        if (running && attached) invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (running && getVisibility() == VISIBLE) {
            long now = System.currentTimeMillis();
            nextMeteorAtMs = now + 1600L;
            nextFlashAtMs = now + 6500L;
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
        if ("miku".equals(world)) ensureMikuBitmap();
        rebuildShaders(w, h);
        initScene(w, h);
    }

    private void rebuildShaders(int w, int h) {
        float diag = Math.max(w, h);
        if ("lofi".equals(world)) {
            skyPaint.setShader(new LinearGradient(
                    0f, 0f, 0f, h,
                    new int[] { 0xFF231134, 0xFF180C26, 0xFF0E0718, 0xFF050209 },
                    new float[] { 0f, 0.42f, 0.80f, 1f },
                    Shader.TileMode.CLAMP));
            glowPaint.setShader(new RadialGradient(
                    w * 0.86f, h * 0.78f, diag * 0.45f,
                    new int[] { 0x48FFB38A, 0x20FFE066, 0x00130A1E },
                    new float[] { 0f, 0.45f, 1f },
                    Shader.TileMode.CLAMP));
            glow2Paint.setShader(new RadialGradient(
                    w * 0.22f, h * 0.22f, diag * 0.36f,
                    new int[] { 0x24FF7EB6, 0x109FE3C9, 0x00130A1E },
                    new float[] { 0f, 0.55f, 1f },
                    Shader.TileMode.CLAMP));
            moonPaint.setColor(0xE0FFF1D0);
            moonCutPaint.setColor(0xFF1D0E2D);
        } else if ("starfall".equals(world)) {
            skyPaint.setShader(new LinearGradient(
                    0f, 0f, 0f, h,
                    new int[] { 0xFF020514, 0xFF091432, 0xFF070F26, 0xFF010208 },
                    new float[] { 0f, 0.44f, 0.82f, 1f },
                    Shader.TileMode.CLAMP));
            glowPaint.setShader(new RadialGradient(
                    w * 0.52f, h * 0.28f, diag * 0.52f,
                    new int[] { 0x306DFFB0, 0x1CB57CFF, 0x00020512 },
                    new float[] { 0f, 0.52f, 1f },
                    Shader.TileMode.CLAMP));
            glow2Paint.setShader(new RadialGradient(
                    w * 0.18f, h * 0.18f, diag * 0.38f,
                    new int[] { 0x247FE7FF, 0x104A5FFF, 0x00020512 },
                    new float[] { 0f, 0.55f, 1f },
                    Shader.TileMode.CLAMP));
            moonPaint.setColor(0xD8E8FAFF);
            moonCutPaint.setColor(0xFF061026);
        } else if ("miku".equals(world)) {
            skyPaint.setShader(new LinearGradient(
                    0f, 0f, 0f, h,
                    new int[] { 0xFF04171F, 0xFF051C24, 0xFF031015, 0xFF010608 },
                    new float[] { 0f, 0.45f, 0.82f, 1f },
                    Shader.TileMode.CLAMP));
            glowPaint.setShader(new RadialGradient(
                    w * 0.80f, h * 0.55f, diag * 0.48f,
                    new int[] { 0x3C39C5BB, 0x1C7FE7FF, 0x00031015 },
                    new float[] { 0f, 0.50f, 1f },
                    Shader.TileMode.CLAMP));
            glow2Paint.setShader(new RadialGradient(
                    w * 0.18f, h * 0.28f, diag * 0.40f,
                    new int[] { 0x2AFF2E88, 0x1239C5BB, 0x00031015 },
                    new float[] { 0f, 0.55f, 1f },
                    Shader.TileMode.CLAMP));
            moonPaint.setColor(0xD87FE7FF);
            moonCutPaint.setColor(0xFF04171F);
        } else {
            // sakura
            skyPaint.setShader(new LinearGradient(
                    0f, 0f, 0f, h,
                    new int[] { 0xFF180C38, 0xFF0E0824, 0xFF070414, 0xFF020108 },
                    new float[] { 0f, 0.42f, 0.80f, 1f },
                    Shader.TileMode.CLAMP));
            glowPaint.setShader(new RadialGradient(
                    w * 0.82f, h * 0.18f, diag * 0.42f,
                    new int[] { 0x38FF7ED4, 0x1CC792FF, 0x00090516 },
                    new float[] { 0f, 0.48f, 1f },
                    Shader.TileMode.CLAMP));
            glow2Paint.setShader(new RadialGradient(
                    w * 0.16f, h * 0.24f, diag * 0.30f,
                    new int[] { 0x26FFD56B, 0x12FF7ED4, 0x00090516 },
                    new float[] { 0f, 0.52f, 1f },
                    Shader.TileMode.CLAMP));
            moonPaint.setColor(0xD8FFF2CC);
            moonCutPaint.setColor(0xFF150B30);
        }
    }

    private void initScene(int w, int h) {
        int[] starPalette;
        if ("lofi".equals(world)) {
            starPalette = new int[] { 0x00FFF1D0, 0x00FFB38A, 0x009FE3C9, 0x00FF88BC };
        } else if ("starfall".equals(world)) {
            starPalette = new int[] { 0x00FFFFFF, 0x007FE7FF, 0x006DFFB0, 0x00FFE066, 0x00B57CFF };
        } else if ("miku".equals(world)) {
            starPalette = new int[] { 0x0039C5BB, 0x007FE7FF, 0x00FF2E88, 0x00EEFFFE };
        } else {
            starPalette = new int[] { 0x00FFF6FD, 0x00FFD56B, 0x007CE8FF, 0x00FFB8E8 };
        }

        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = Math.abs(nextRand()) % Math.max(1, w);
            starY[i] = Math.abs(nextRand()) % Math.max(1, (int) (h * 0.84f));
            starSize[i] = (1.0f + (Math.abs(nextRand()) % 18) * 0.12f) * density;
            starPhase[i] = (Math.abs(nextRand()) % 628) * 0.01f;
            starColor[i] = starPalette[i % starPalette.length];
            starDiamond[i] = (i % 5 == 0);
        }

        int[] partPalette;
        if ("lofi".equals(world)) {
            partPalette = new int[] { 0x00FFE066, 0x00FFB38A, 0x009FE3C9, 0x00B8ECFF };
        } else if ("starfall".equals(world)) {
            partPalette = new int[] { 0x00DCFF78, 0x006DFFB0, 0x007FE7FF, 0x00FFE066 };
        } else if ("miku".equals(world)) {
            partPalette = new int[] { 0x0039C5BB, 0x007FE7FF, 0x00FF2E88, 0x005BF5B8 };
        } else {
            partPalette = new int[] { 0x00FF7ED4, 0x00FFA8E2, 0x00FFD1F0, 0x00E49BFF };
        }

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            ptX[i] = Math.abs(nextRand()) % Math.max(1, w);
            ptY[i] = Math.abs(nextRand()) % Math.max(1, h);
            resetParticle(i, false, w, h);
            ptColor[i] = partPalette[i % partPalette.length];
        }

        for (int i = 0; i < RAIN_COUNT; i++) {
            rainX[i] = Math.abs(nextRand()) % Math.max(1, w);
            rainY[i] = Math.abs(nextRand()) % Math.max(1, h);
            rainLen[i] = (10f + (Math.abs(nextRand()) % 14)) * density;
            rainSpd[i] = (6.5f + (Math.abs(nextRand()) % 7)) * density;
        }

        for (int i = 0; i < EQ_BARS; i++) {
            eqLevel[i] = 0.25f + (i % 5) * 0.12f;
            eqPeak[i] = eqLevel[i];
        }
    }

    private void resetParticle(int i, boolean respawnEdge, int w, int h) {
        int r = Math.abs(nextRand());
        float depth = 0.65f + (r % 10) * 0.08f;
        ptPhase[i] = (r % 628) * 0.01f;
        if ("lofi".equals(world)) {
            // Gotas lentas no vidro (0..13) + partículas quentes flutuantes (14..25)
            ptSize[i] = (2.2f + (r % 4) * 0.7f) * density;
            ptVx[i] = ((r & 1) == 0 ? 0.15f : -0.15f) * density;
            ptVy[i] = (i < 14 ? (0.85f + (r % 6) * 0.25f) : -(0.35f + (r % 4) * 0.12f)) * density;
            ptAngle[i] = r % 360;
            ptSpin[i] = 1f;
            if (respawnEdge) {
                ptX[i] = r % Math.max(1, w);
                ptY[i] = i < 14 ? (-8f * density) : (h + 8f * density);
            }
        } else if ("starfall".equals(world)) {
            // Vaga-lumes suaves na metade inferior
            ptSize[i] = (2.0f + (r % 4) * 0.65f) * density;
            ptVx[i] = (((r % 11) - 5) * 0.14f) * density;
            ptVy[i] = -(0.22f + (r % 5) * 0.10f) * density;
            ptAngle[i] = r % 360;
            ptSpin[i] = 1.4f;
            if (respawnEdge) {
                ptX[i] = r % Math.max(1, w);
                ptY[i] = h * (0.58f + (r % 38) * 0.01f);
            } else {
                ptY[i] = h * (0.48f + (r % 48) * 0.01f);
            }
        } else if ("miku".equals(world)) {
            // Notas musicais flutuando para cima
            ptSize[i] = (4.5f + (r % 4) * 1.1f) * density * depth;
            ptVx[i] = (((r % 7) - 3) * 0.18f) * density;
            ptVy[i] = -(0.65f + (r % 6) * 0.20f) * density;
            ptAngle[i] = ((r % 24) - 12);
            ptSpin[i] = ((r & 1) == 0 ? 0.8f : -0.8f);
            if (respawnEdge) {
                ptX[i] = r % Math.max(1, w);
                ptY[i] = h + 14f * density;
            }
        } else {
            // Pétalas de sakura com giro 3D
            ptSize[i] = (4.8f + (r % 5) * 1.15f) * density * depth;
            ptVx[i] = (0.75f + (r % 7) * 0.18f) * density * depth;
            ptVy[i] = (0.95f + (r % 6) * 0.22f) * density * depth;
            ptAngle[i] = r % 360;
            ptSpin[i] = ((r & 1) == 0 ? 1f : -1f) * (1.3f + (r % 5) * 0.45f);
            if (respawnEdge) {
                if ((r & 3) == 0) {
                    ptX[i] = -14f * density;
                    ptY[i] = (r % Math.max(1, Math.max(200, h))) * 0.7f;
                } else {
                    ptX[i] = (r % Math.max(1, w)) - w * 0.15f;
                    ptY[i] = -12f * density;
                }
            }
        }
    }

    private void spawnMeteor(int w, int h) {
        meteorActive = true;
        meteorLife = 1.0f;
        meteorX = w * (0.28f + (Math.abs(nextRand()) % 55) * 0.01f);
        meteorY = h * (0.04f + (Math.abs(nextRand()) % 24) * 0.01f);
        meteorVx = -(11.5f + (Math.abs(nextRand()) % 6)) * density;
        meteorVy = (5.0f + (Math.abs(nextRand()) % 4)) * density;
    }

    private void stepSimulation() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        timeSec += 0.05f;
        long now = System.currentTimeMillis();
        boolean bursting = now < burstUntilMs;
        float boost = bursting ? 2.1f : 1.0f;

        if ("lofi".equals(world)) {
            for (int i = 0; i < RAIN_COUNT; i++) {
                rainX[i] -= rainSpd[i] * 0.26f;
                rainY[i] += rainSpd[i] * (bursting ? 1.45f : 1.0f);
                if (rainY[i] > h + 20f * density || rainX[i] < -20f * density) {
                    rainX[i] = Math.abs(nextRand()) % Math.max(1, (int) (w * 1.15f));
                    rainY[i] = -(Math.abs(nextRand()) % 60) * density;
                }
            }
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                if (i < 14) {
                    ptY[i] += ptVy[i] * (0.5f + 0.5f * (float) Math.abs(Math.sin(timeSec * 1.4f + ptPhase[i])));
                    ptX[i] += (float) Math.sin(timeSec * 2.2f + ptPhase[i]) * 0.22f * density;
                    if (ptY[i] > h + 14f * density) resetParticle(i, true, w, h);
                } else {
                    ptX[i] += (float) Math.sin(timeSec * 1.3f + ptPhase[i]) * 0.45f * density;
                    ptY[i] += ptVy[i];
                    if (ptY[i] < -14f * density) resetParticle(i, true, w, h);
                }
            }
            if (lofiFlash > 0f) {
                lofiFlash = Math.max(0f, lofiFlash - 0.07f);
            } else if (now >= nextFlashAtMs) {
                lofiFlash = 0.45f;
                nextFlashAtMs = now + 9000L + (Math.abs(nextRand()) % 9000);
            }
            return;
        }

        if ("starfall".equals(world)) {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                ptX[i] += ptVx[i] + (float) Math.sin(timeSec * 1.5f + ptPhase[i]) * 0.45f * density;
                ptY[i] += ptVy[i] + (float) Math.cos(timeSec * 1.2f + ptPhase[i]) * 0.25f * density;
                if (ptY[i] < h * 0.35f || ptX[i] < -16f * density || ptX[i] > w + 16f * density) {
                    resetParticle(i, true, w, h);
                }
            }
            stepMeteor(w, h, now, 2800L);
            return;
        }

        if ("miku".equals(world)) {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                ptX[i] += ptVx[i] + (float) Math.sin(timeSec * 2.1f + ptPhase[i]) * 0.5f * density;
                ptY[i] += ptVy[i] * boost;
                ptAngle[i] = (float) Math.sin(timeSec * 2.0f + ptPhase[i]) * 14f;
                if (ptY[i] < -20f * density) {
                    resetParticle(i, true, w, h);
                }
            }
            for (int i = 0; i < EQ_BARS; i++) {
                float wave1 = 0.48f + 0.42f * (float) Math.sin(timeSec * 4.2f + i * 0.55f);
                float wave2 = 0.35f + 0.35f * (float) Math.cos(timeSec * 2.7f - i * 0.38f);
                float target = Math.min(1f, (wave1 * 0.65f + wave2 * 0.35f) * (bursting ? 1.25f : 1.0f));
                eqLevel[i] = eqLevel[i] * 0.62f + target * 0.38f;
                if (eqLevel[i] > eqPeak[i]) {
                    eqPeak[i] = eqLevel[i];
                } else {
                    eqPeak[i] = Math.max(eqLevel[i], eqPeak[i] - 0.025f);
                }
            }
            return;
        }

        // Default living world: sakura
        float gust = (float) Math.sin(timeSec * 0.7f) * 0.55f;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float sway = (float) Math.sin(timeSec * 1.9f + ptPhase[i]) * 0.75f * density;
            ptX[i] += (ptVx[i] + gust * density) * boost + sway;
            ptY[i] += ptVy[i] * (bursting ? 1.45f : 1.0f);
            ptAngle[i] = (ptAngle[i] + ptSpin[i] * boost) % 360f;
            if (ptY[i] > h + 16f * density || ptX[i] > w + 22f * density) {
                resetParticle(i, true, w, h);
            }
        }
        stepMeteor(w, h, now, 3800L);
    }

    private void stepMeteor(int w, int h, long now, long minDelay) {
        if (meteorActive) {
            meteorX += meteorVx;
            meteorY += meteorVy;
            meteorLife -= 0.06f;
            if (meteorLife <= 0f || meteorX < -70f * density || meteorY > h + 40f * density) {
                meteorActive = false;
                nextMeteorAtMs = now + minDelay + (Math.abs(nextRand()) % 3200);
            }
        } else if (now >= nextMeteorAtMs) {
            spawnMeteor(w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawRect(0f, 0f, w, h, skyPaint);
        canvas.drawRect(0f, 0f, w, h, glowPaint);
        canvas.drawRect(0f, 0f, w, h, glow2Paint);

        if ("lofi".equals(world)) {
            drawLofiScene(canvas, w, h);
        } else if ("starfall".equals(world)) {
            drawStarfallScene(canvas, w, h);
        } else if ("miku".equals(world)) {
            drawMikuScene(canvas, w, h);
        } else {
            drawSakuraScene(canvas, w, h);
        }
    }

    // ==================== WORLD 1: SAKURA STORM ====================
    private void drawSakuraScene(Canvas canvas, int w, int h) {
        // 1. Radiant crescent moon
        float moonR = Math.min(w, h) * 0.072f;
        float mx = w * 0.87f;
        float my = h * 0.16f;
        shapePaint.setColor(0x22FFD56B);
        canvas.drawCircle(mx, my, moonR * 1.65f, shapePaint);
        canvas.drawCircle(mx, my, moonR, moonPaint);
        canvas.drawCircle(mx - moonR * 0.36f, my - moonR * 0.18f, moonR * 0.88f, moonCutPaint);

        // 2. Twinkling starfield + 4-point diamonds
        drawStars(canvas, 0.88f);

        // 3. Distant misty mountain ridge melting into bottom black Android nav bar
        workPath.reset();
        workPath.moveTo(0f, h);
        workPath.lineTo(0f, h * 0.85f);
        workPath.quadTo(w * 0.22f, h * 0.74f, w * 0.45f, h * 0.83f);
        workPath.quadTo(w * 0.72f, h * 0.90f, w, h * 0.78f);
        workPath.lineTo(w, h);
        workPath.close();
        shapePaint.setColor(0xA6070312);
        canvas.drawPath(workPath, shapePaint);

        // 4. Silhouetted cherry blossom branch + 2 swaying Japanese paper lanterns (chōchin)
        strokePaint.setColor(0xAA2A1238);
        strokePaint.setStrokeWidth(4.5f * density);
        workPath.reset();
        workPath.moveTo(-6f * density, h * 0.12f);
        workPath.quadTo(w * 0.14f, h * 0.17f, w * 0.28f, h * 0.10f);
        canvas.drawPath(workPath, strokePaint);

        strokePaint.setStrokeWidth(2.6f * density);
        workPath.reset();
        workPath.moveTo(w * 0.12f, h * 0.14f);
        workPath.quadTo(w * 0.18f, h * 0.22f, w * 0.25f, h * 0.20f);
        canvas.drawPath(workPath, strokePaint);

        drawLantern(canvas, w * 0.11f, h * 0.145f, 16f * density, 0.0f);
        drawLantern(canvas, w * 0.23f, h * 0.12f, 22f * density, 1.8f);

        // 5. Shooting star comet
        drawMeteor(canvas, 0x00FF7ED4, 0x007CE8FF, 0x00FFF8E7);

        // 6. 3D fluttering cherry blossom petals
        int pAlpha = running ? 178 : 108;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            shapePaint.setColor((pAlpha << 24) | ptColor[i]);
            float flutter = 0.42f + 0.58f * Math.abs((float) Math.cos(timeSec * 2.3f + ptPhase[i]));
            drawSakuraPetal(canvas, ptX[i], ptY[i], ptSize[i], ptAngle[i], flutter, shapePaint);
        }
    }

    private void drawLantern(Canvas canvas, float anchorX, float anchorY, float cordLen, float phase) {
        float sway = running ? (float) Math.sin(timeSec * 1.4f + phase) * 5f * density : 0f;
        float lx = anchorX + sway;
        float ly = anchorY + cordLen;
        strokePaint.setColor(0x998C629C);
        strokePaint.setStrokeWidth(1.2f * density);
        canvas.drawLine(anchorX, anchorY, lx, ly, strokePaint);

        float r = 7.5f * density;
        shapePaint.setColor(0x28FFD56B);
        canvas.drawCircle(lx, ly + r, r * 2.2f, shapePaint);
        shapePaint.setColor(0xCCFF9E68);
        workRect.set(lx - r * 0.85f, ly, lx + r * 0.85f, ly + r * 2.0f);
        canvas.drawOval(workRect, shapePaint);
        shapePaint.setColor(0xE8FFF0B8);
        workRect.set(lx - r * 0.45f, ly + r * 0.35f, lx + r * 0.45f, ly + r * 1.65f);
        canvas.drawOval(workRect, shapePaint);
        shapePaint.setColor(0xDD2E133A);
        canvas.drawRect(lx - r * 0.55f, ly - 1.5f * density, lx + r * 0.55f, ly + 1.5f * density, shapePaint);
        canvas.drawRect(lx - r * 0.55f, ly + r * 1.9f, lx + r * 0.55f, ly + r * 2.2f, shapePaint);
    }

    // ==================== WORLD 2: LOFI ROOM ====================
    private void drawLofiScene(Canvas canvas, int w, int h) {
        // Optional subtle distant lightning flash
        if (lofiFlash > 0.01f) {
            shapePaint.setColor((Math.round(lofiFlash * 72f) << 24) | 0x00D8E8FF);
            canvas.drawRect(0f, 0f, w, h, shapePaint);
        }

        // 1. Stars + crescent moon through rainy night window
        drawStars(canvas, 0.55f);
        float moonR = Math.min(w, h) * 0.062f;
        float mx = w * 0.78f;
        float my = h * 0.22f;
        shapePaint.setColor(0x20FFE066);
        canvas.drawCircle(mx, my, moonR * 1.7f, shapePaint);
        canvas.drawCircle(mx, my, moonR, moonPaint);
        canvas.drawCircle(mx - moonR * 0.35f, my - moonR * 0.16f, moonR * 0.88f, moonCutPaint);

        // 2. Distant cozy nocturnal city skyline silhouettes with tiny warm lights
        shapePaint.setColor(0x990A0513);
        int bCount = 12;
        float bw = w / (float) bCount;
        for (int i = 0; i < bCount; i++) {
            float bh = h * (0.14f + ((i * 37) % 15) * 0.011f);
            float bx = i * bw;
            float by = h - bh;
            canvas.drawRect(bx, by, bx + bw + 1f, h, shapePaint);
            shapePaint.setColor(i % 3 == 0 ? 0x55FFB38A : 0x44FFE066);
            float winW = Math.max(2f * density, bw * 0.14f);
            canvas.drawRect(bx + bw * 0.24f, by + 10f * density, bx + bw * 0.24f + winW, by + 14f * density, shapePaint);
            canvas.drawRect(bx + bw * 0.56f, by + 22f * density, bx + bw * 0.56f + winW, by + 26f * density, shapePaint);
            shapePaint.setColor(0x990A0513);
        }

        // 3. Subtle rainy window frame mullions
        strokePaint.setColor(0x364D2D5A);
        strokePaint.setStrokeWidth(3f * density);
        canvas.drawLine(w * 0.64f, 0f, w * 0.64f, h * 0.86f, strokePaint);
        canvas.drawLine(0f, h * 0.86f, w, h * 0.86f, strokePaint);

        // 4. Hanging fairy lights catenary string across upper screen
        strokePaint.setColor(0x555F3D6B);
        strokePaint.setStrokeWidth(1.2f * density);
        workPath.reset();
        workPath.moveTo(0f, h * 0.08f);
        workPath.quadTo(w * 0.25f, h * 0.18f, w * 0.50f, h * 0.10f);
        workPath.quadTo(w * 0.75f, h * 0.18f, w, h * 0.08f);
        canvas.drawPath(workPath, strokePaint);

        int[] bulbCols = { 0x00FFE066, 0x00FFB38A, 0x00FF88BC, 0x009FE3C9 };
        int bulbs = 11;
        for (int i = 0; i < bulbs; i++) {
            float t = (i + 0.5f) / (float) bulbs;
            float bx = w * t;
            float sag = (float) Math.sin(t * Math.PI * 2.0) * (h * 0.032f);
            float by = h * 0.115f + Math.abs(sag);
            float glow = running ? (0.65f + 0.35f * (float) Math.sin(timeSec * 2.4f + i * 0.9f)) : 0.85f;
            int col = bulbCols[i % bulbCols.length];
            shapePaint.setColor((Math.round(glow * 55f) << 24) | col);
            canvas.drawCircle(bx, by, 10f * density, shapePaint);
            shapePaint.setColor((Math.round(glow * 230f) << 24) | col);
            canvas.drawCircle(bx, by, 3.0f * density, shapePaint);
        }

        // 5. Slanted falling rain streaks outside window
        strokePaint.setColor(0x489FE3C9);
        strokePaint.setStrokeWidth(1.1f * density);
        for (int i = 0; i < RAIN_COUNT; i++) {
            canvas.drawLine(rainX[i], rainY[i], rainX[i] - rainLen[i] * 0.26f, rainY[i] + rainLen[i], strokePaint);
        }

        // 6. Slow glass raindrops (0..13) + cozy floating warm dust/bokeh motes (14..25)
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            if (i < 14) {
                strokePaint.setColor(0x32C4E8FF);
                strokePaint.setStrokeWidth(1.4f * density);
                canvas.drawLine(ptX[i], ptY[i] - 10f * density, ptX[i], ptY[i], strokePaint);
                shapePaint.setColor(0x88E2F4FF);
                canvas.drawCircle(ptX[i], ptY[i], ptSize[i], shapePaint);
            } else {
                float a = running ? (0.35f + 0.35f * (float) Math.sin(timeSec * 1.8f + ptPhase[i])) : 0.45f;
                shapePaint.setColor((Math.round(a * 165f) << 24) | ptColor[i]);
                canvas.drawCircle(ptX[i], ptY[i], ptSize[i] * 1.25f, shapePaint);
            }
        }

        // 7. Gentle rising teacup steam wisps on lower-right above warm lamp glow
        strokePaint.setStrokeWidth(2.0f * density);
        for (int s = 0; s < 3; s++) {
            float sx = w * 0.77f + (s - 1) * 10f * density;
            float baseY = h * 0.88f;
            strokePaint.setColor(0x32FFEBD8);
            workPath.reset();
            workPath.moveTo(sx, baseY);
            float w1 = (float) Math.sin(timeSec * 1.8f + s * 1.4f) * 9f * density;
            float w2 = (float) Math.cos(timeSec * 1.5f + s * 1.1f) * 11f * density;
            workPath.cubicTo(sx + w1, baseY - 26f * density, sx - w2, baseY - 52f * density, sx + w1 * 0.6f, baseY - 78f * density);
            canvas.drawPath(workPath, strokePaint);
        }
    }

    // ==================== WORLD 3: STARFALL AURORA ====================
    private void drawStarfallScene(Canvas canvas, int w, int h) {
        // 1. Waving Aurora Borealis curtains (emerald + cosmic violet)
        drawAuroraBand(canvas, w, h, h * 0.24f, h * 0.14f, 0x346DFFB0, 0.0f);
        drawAuroraBand(canvas, w, h, h * 0.33f, h * 0.12f, 0x2AB57CFF, 2.1f);

        // 2. Twinkling cosmic stars + bright diamond cross stars
        drawStars(canvas, 0.94f);

        // 3. Self-drawing cyan constellations cycling smoothly
        float cycle = (timeSec * 0.18f) % CONST_PTS.length;
        int cIdx = ((int) cycle) % CONST_PTS.length;
        float frac = cycle - (int) cycle;
        float reveal = frac < 0.75f ? Math.min(1f, frac / 0.45f) : Math.max(0f, (1f - frac) / 0.25f);
        drawConstellation(canvas, w, h, cIdx, reveal);

        // 4. Golden shooting star comet
        drawMeteor(canvas, 0x00FFE066, 0x007FE7FF, 0x00FFFFFF);

        // 5. Twin dark horizon hills + silhouetted Japanese Torii gate at bottom center
        workPath.reset();
        workPath.moveTo(0f, h);
        workPath.lineTo(0f, h * 0.86f);
        workPath.quadTo(w * 0.24f, h * 0.80f, w * 0.50f, h * 0.84f);
        workPath.quadTo(w * 0.76f, h * 0.88f, w, h * 0.81f);
        workPath.lineTo(w, h);
        workPath.close();
        shapePaint.setColor(0xD6030714);
        canvas.drawPath(workPath, shapePaint);

        drawToriiGate(canvas, w * 0.50f, h * 0.855f, Math.min(w, h) * 0.14f);

        // 6. Floating yellow-green fireflies near bottom horizon
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float glow = running ? (0.35f + 0.65f * (float) Math.abs(Math.sin(timeSec * 2.6f + ptPhase[i]))) : 0.7f;
            shapePaint.setColor((Math.round(glow * 55f) << 24) | ptColor[i]);
            canvas.drawCircle(ptX[i], ptY[i], ptSize[i] * 2.8f, shapePaint);
            shapePaint.setColor((Math.round(glow * 235f) << 24) | ptColor[i]);
            canvas.drawCircle(ptX[i], ptY[i], ptSize[i], shapePaint);
        }
    }

    private void drawAuroraBand(Canvas canvas, int w, int h, float baseY, float thickness, int color, float phase) {
        workPath.reset();
        int segs = 12;
        for (int i = 0; i <= segs; i++) {
            float x = (w * i) / (float) segs;
            float y = baseY + (float) Math.sin(timeSec * 0.85f + i * 0.55f + phase) * (20f * density);
            if (i == 0) workPath.moveTo(x, y);
            else workPath.lineTo(x, y);
        }
        for (int i = segs; i >= 0; i--) {
            float x = (w * i) / (float) segs;
            float y = baseY + thickness
                    + (float) Math.cos(timeSec * 0.70f + i * 0.48f + phase) * (16f * density);
            workPath.lineTo(x, y);
        }
        workPath.close();
        shapePaint.setColor(color);
        canvas.drawPath(workPath, shapePaint);
    }

    private void drawConstellation(Canvas canvas, int w, int h, int idx, float reveal) {
        if (reveal <= 0.02f) return;
        float[] pts = CONST_PTS[idx];
        int[] edges = CONST_EDGES[idx];
        int edgeCount = edges.length / 2;
        float drawnEdges = edgeCount * reveal;

        strokePaint.setColor((Math.round(reveal * 135f) << 24) | 0x007FE7FF);
        strokePaint.setStrokeWidth(1.3f * density);
        for (int e = 0; e < edgeCount; e++) {
            if (e > drawnEdges) break;
            float segFrac = Math.min(1f, drawnEdges - e);
            int a = edges[e * 2];
            int b = edges[e * 2 + 1];
            float x1 = pts[a * 2] * w;
            float y1 = pts[a * 2 + 1] * h;
            float x2 = pts[b * 2] * w;
            float y2 = pts[b * 2 + 1] * h;
            canvas.drawLine(x1, y1, x1 + (x2 - x1) * segFrac, y1 + (y2 - y1) * segFrac, strokePaint);
        }
        int nodeCount = pts.length / 2;
        for (int n = 0; n < nodeCount; n++) {
            float nx = pts[n * 2] * w;
            float ny = pts[n * 2 + 1] * h;
            shapePaint.setColor((Math.round(reveal * 215f) << 24) | 0x00E8FBFF);
            drawDiamondStar(canvas, nx, ny, 3.6f * density, shapePaint);
        }
    }

    private void drawToriiGate(Canvas canvas, float cx, float baseY, float size) {
        shapePaint.setColor(0xE8040814);
        float pillarH = size * 0.85f;
        float halfSpan = size * 0.42f;
        float pillarW = Math.max(3f * density, size * 0.08f);
        // Pillars
        canvas.drawRect(cx - halfSpan - pillarW * 0.5f, baseY - pillarH, cx - halfSpan + pillarW * 0.5f, baseY + 6f * density, shapePaint);
        canvas.drawRect(cx + halfSpan - pillarW * 0.5f, baseY - pillarH, cx + halfSpan + pillarW * 0.5f, baseY + 6f * density, shapePaint);
        // Top Kasagi beam (slightly curved upward tips)
        workPath.reset();
        workPath.moveTo(cx - halfSpan * 1.55f, baseY - pillarH - size * 0.08f);
        workPath.quadTo(cx, baseY - pillarH + size * 0.03f, cx + halfSpan * 1.55f, baseY - pillarH - size * 0.08f);
        workPath.lineTo(cx + halfSpan * 1.42f, baseY - pillarH + size * 0.06f);
        workPath.quadTo(cx, baseY - pillarH + size * 0.14f, cx - halfSpan * 1.42f, baseY - pillarH + size * 0.06f);
        workPath.close();
        canvas.drawPath(workPath, shapePaint);
        // Nuki lower crossbeam
        canvas.drawRect(cx - halfSpan * 1.25f, baseY - pillarH * 0.72f, cx + halfSpan * 1.25f, baseY - pillarH * 0.72f + pillarW * 0.85f, shapePaint);
        // Center tablet
        canvas.drawRect(cx - pillarW * 0.55f, baseY - pillarH * 0.92f, cx + pillarW * 0.55f, baseY - pillarH * 0.72f, shapePaint);
    }

    // ==================== WORLD 4: HATSUNE MIKU 01 STAGE ====================
    private void drawMikuScene(Canvas canvas, int w, int h) {
        // 1. Subtle stage sparkle field
        drawStars(canvas, 0.75f);

        // 2. Pulsing holographic vocaloid stage rings behind center-right
        float rx = w * 0.80f;
        float ry = h * 0.58f;
        float baseR = Math.min(w, h) * 0.22f;
        float pulse = running ? (1.0f + 0.06f * (float) Math.sin(timeSec * 3.0f)) : 1.0f;
        strokePaint.setStrokeWidth(1.6f * density);
        strokePaint.setColor(0x5239C5BB);
        canvas.drawCircle(rx, ry, baseR * pulse, strokePaint);
        strokePaint.setColor(0x40FF2E88);
        canvas.drawCircle(rx, ry, baseR * 1.22f / pulse, strokePaint);

        // 3. Flowing dual neon synth waves across mid-screen
        drawSynthWave(canvas, w, h * 0.52f, 20f * density, 0.012f, 2.2f, 0x4C39C5BB, 2.0f * density);
        drawSynthWave(canvas, w, h * 0.57f, 15f * density, 0.015f, -1.7f, 0x3EFF2E88, 1.5f * density);

        // 4. Live 22-band concert spectrum equalizer melting into bottom black Android 4.2.2 bar
        float barSlot = w / (float) EQ_BARS;
        float barGap = Math.max(2f * density, barSlot * 0.24f);
        float maxBarH = h * 0.24f;
        for (int i = 0; i < EQ_BARS; i++) {
            float bh = Math.max(4f * density, eqLevel[i] * maxBarH);
            float bx0 = i * barSlot + barGap * 0.5f;
            float bx1 = (i + 1) * barSlot - barGap * 0.5f;
            int col = (i % 5 == 2) ? 0x62FF2E88 : ((i % 3 == 0) ? 0x687FE7FF : 0x6A39C5BB);
            shapePaint.setColor(col);
            canvas.drawRect(bx0, h - bh, bx1, h, shapePaint);

            float peakY = h - Math.max(6f * density, eqPeak[i] * maxBarH);
            shapePaint.setColor((i % 5 == 2) ? 0xCCFF66B2 : 0xCC9AF8FF);
            canvas.drawRect(bx0, peakY - 2.2f * density, bx1, peakY, shapePaint);
        }

        // 5. Floating drawn Canvas eighth-notes (zero Unicode dependency!)
        int nAlpha = running ? 175 : 110;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            drawMusicNote(canvas, ptX[i], ptY[i], ptSize[i], ptAngle[i], (nAlpha << 24) | ptColor[i]);
        }

        // 6. Graceful translucent Hatsune Miku hologram cameo in bottom-right corner
        if (mikuBitmap != null && !mikuBitmap.isRecycled()) {
            float targetH = Math.min(h * 0.48f, 210f * density);
            float ratio = mikuBitmap.getWidth() / (float) Math.max(1, mikuBitmap.getHeight());
            float targetW = targetH * ratio;
            float bob = running ? (float) Math.sin(timeSec * 2.1f) * 5.5f * density : 0f;
            float rightPad = 14f * density;
            float bottomPad = 6f * density;
            srcRect.set(0, 0, mikuBitmap.getWidth(), mikuBitmap.getHeight());
            dstRect.set(w - targetW - rightPad, h - targetH - bottomPad + bob,
                    w - rightPad, h - bottomPad + bob);
            bitmapPaint.setAlpha(105);
            canvas.drawBitmap(mikuBitmap, srcRect, dstRect, bitmapPaint);
        }
    }

    private void drawSynthWave(Canvas canvas, int w, float centerY, float amp, float freq, float speed, int color, float width) {
        workPath.reset();
        int steps = 24;
        for (int i = 0; i <= steps; i++) {
            float x = (w * i) / (float) steps;
            float y = centerY + (float) Math.sin(x * freq + timeSec * speed) * amp;
            if (i == 0) workPath.moveTo(x, y);
            else workPath.lineTo(x, y);
        }
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(width);
        canvas.drawPath(workPath, strokePaint);
    }

    // ==================== SHARED PRIMITIVES (ZERO ALLOCATIONS) ====================
    private void drawStars(Canvas canvas, float maxAlphaFrac) {
        for (int i = 0; i < STAR_COUNT; i++) {
            float pulse = running ? (0.45f + 0.55f * (float) Math.sin(timeSec * 2.2f + starPhase[i])) : 0.65f;
            int alpha = Math.max(36, Math.min(245, Math.round(pulse * 240f * maxAlphaFrac)));
            shapePaint.setColor((alpha << 24) | starColor[i]);
            if (starDiamond[i]) {
                drawDiamondStar(canvas, starX[i], starY[i], starSize[i] * (1.8f + pulse * 0.9f), shapePaint);
            } else {
                canvas.drawCircle(starX[i], starY[i], starSize[i], shapePaint);
            }
        }
    }

    private void drawMeteor(Canvas canvas, int outerRgb, int innerRgb, int headRgb) {
        if (!meteorActive || meteorLife <= 0f) return;
        float tailX = meteorX - meteorVx * 6.8f;
        float tailY = meteorY - meteorVy * 6.8f;
        int aOuter = Math.max(0, Math.min(255, Math.round(meteorLife * 120f)));
        int aCore = Math.max(0, Math.min(255, Math.round(meteorLife * 245f)));
        meteorPaint.setStrokeWidth(4.0f * density);
        meteorPaint.setColor((aOuter << 24) | outerRgb);
        canvas.drawLine(meteorX, meteorY, tailX, tailY, meteorPaint);
        meteorPaint.setStrokeWidth(1.8f * density);
        meteorPaint.setColor((aCore << 24) | innerRgb);
        canvas.drawLine(meteorX, meteorY, (meteorX + tailX) * 0.5f, (meteorY + tailY) * 0.5f, meteorPaint);
        shapePaint.setColor((aCore << 24) | headRgb);
        drawDiamondStar(canvas, meteorX, meteorY, 5.2f * density * meteorLife, shapePaint);
    }

    private void drawSakuraPetal(Canvas canvas, float cx, float cy, float s, float deg, float scaleX, Paint paint) {
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(deg);
        canvas.scale(scaleX, 1f);
        workPath.reset();
        workPath.moveTo(0f, -s);
        workPath.quadTo(s * 0.85f, -s * 0.25f, 0f, s * 0.9f);
        workPath.quadTo(-s * 0.85f, -s * 0.25f, 0f, -s);
        workPath.close();
        canvas.drawPath(workPath, paint);
        canvas.restore();
    }

    private void drawMusicNote(Canvas canvas, float cx, float cy, float s, float deg, int color) {
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(deg);
        shapePaint.setColor(color);
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(Math.max(1.2f, s * 0.24f));
        workRect.set(-s * 0.65f, s * 0.15f, s * 0.35f, s * 0.85f);
        canvas.drawOval(workRect, shapePaint);
        canvas.drawLine(s * 0.26f, s * 0.45f, s * 0.26f, -s * 1.15f, strokePaint);
        workPath.reset();
        workPath.moveTo(s * 0.26f, -s * 1.15f);
        workPath.quadTo(s * 1.15f, -s * 0.85f, s * 0.85f, -s * 0.25f);
        canvas.drawPath(workPath, strokePaint);
        canvas.restore();
    }

    private void drawDiamondStar(Canvas canvas, float cx, float cy, float r, Paint paint) {
        float in = r * 0.26f;
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

    private int nextRand() {
        int x = rng;
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        rng = x;
        return x;
    }
}
