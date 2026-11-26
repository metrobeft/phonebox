package net.oxog.phonebox.hp71.theme.anime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Fundo full-screen animado do tema Anime 2.0 ("Sakura Midnight / Neon Tokyo"):
 * - Céu crepuscular índigo -> meia-noite quase preto na base (funde com a barra preta do Android 4.2.2);
 * - Lua crescente luminosa + névoa sakura suave;
 * - 40 estrelas cintilantes + brilhos estelares de 4 pontas (Canvas Path, zero dependência de fonte);
 * - 20 pétalas de cerejeira (Sakura) flutuando com balanço senoidal e rotação;
 * - Estrelas cadentes (meteoros) cruzando o céu periodicamente e rajada extra ao trocar de aba.
 * Zero alocações em onDraw(), 100% API 17 e pausa completa em onPause().
 */
public final class AnimeSkyBackgroundView extends View {

    private static final int STAR_COUNT = 42;
    private static final int PETAL_COUNT = 20;
    private static final long FRAME_MS = 50L; // ~20 FPS suave no Cortex-A7 1GHz

    private final Paint skyPaint = new Paint();
    private final Paint nebulaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonCutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint petalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint meteorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path petalPath = new Path();
    private final Path starPath = new Path();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final int[] starColor = new int[STAR_COUNT];
    private final boolean[] starDiamond = new boolean[STAR_COUNT];

    private final float[] petX = new float[PETAL_COUNT];
    private final float[] petY = new float[PETAL_COUNT];
    private final float[] petVx = new float[PETAL_COUNT];
    private final float[] petVy = new float[PETAL_COUNT];
    private final float[] petSize = new float[PETAL_COUNT];
    private final float[] petAngle = new float[PETAL_COUNT];
    private final float[] petSpin = new float[PETAL_COUNT];
    private final float[] petPhase = new float[PETAL_COUNT];
    private final int[] petColor = new int[PETAL_COUNT];

    // Shooting star state
    private boolean meteorActive = false;
    private float meteorX, meteorY, meteorVx, meteorVy;
    private float meteorLife = 0f;
    private long nextMeteorAtMs = 0L;

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
        moonPaint.setColor(0xD8FFF2CC);
        moonCutPaint.setStyle(Paint.Style.FILL);
        moonCutPaint.setColor(0xFF150B30);
        starPaint.setStyle(Paint.Style.FILL);
        petalPaint.setStyle(Paint.Style.FILL);
        meteorPaint.setStyle(Paint.Style.STROKE);
        meteorPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setRunning(boolean enabled) {
        if (this.running == enabled) return;
        this.running = enabled;
        handler.removeCallbacks(frameLoop);
        if (running && attached && getVisibility() == VISIBLE) {
            nextMeteorAtMs = System.currentTimeMillis() + 1800L;
            handler.post(frameLoop);
        } else {
            invalidate();
        }
    }

    public void triggerSakuraBurst() {
        long now = System.currentTimeMillis();
        burstUntilMs = now + 900L;
        if (!meteorActive && getWidth() > 0 && getHeight() > 0) {
            spawnMeteor(getWidth(), getHeight());
        }
        if (running && attached) invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (running && getVisibility() == VISIBLE) {
            nextMeteorAtMs = System.currentTimeMillis() + 1800L;
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

        // Twilight gradient melting into pitch-dark #03020A at bottom (seamless with Android 4.2.2 black bar)
        skyPaint.setShader(new LinearGradient(
                0f, 0f, 0f, h,
                new int[] { 0xFF180C38, 0xFF0E0824, 0xFF070414, 0xFF020108 },
                new float[] { 0f, 0.42f, 0.80f, 1f },
                Shader.TileMode.CLAMP));

        nebulaPaint.setShader(new RadialGradient(
                w * 0.82f, h * 0.18f, Math.max(w, h) * 0.42f,
                new int[] { 0x38FF7ED4, 0x1CC792FF, 0x00090516 },
                new float[] { 0f, 0.48f, 1f },
                Shader.TileMode.CLAMP));

        initSky(w, h);
    }

    private void initSky(int w, int h) {
        int[] starPalette = { 0x00FFF6FD, 0x00FFD56B, 0x007CE8FF, 0x00FFB8E8 };
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = Math.abs(nextRand()) % Math.max(1, w);
            starY[i] = Math.abs(nextRand()) % Math.max(1, (int) (h * 0.88f));
            starSize[i] = (1.1f + (Math.abs(nextRand()) % 18) * 0.12f) * density;
            starPhase[i] = (Math.abs(nextRand()) % 628) * 0.01f;
            starColor[i] = starPalette[i % starPalette.length];
            starDiamond[i] = (i % 5 == 0);
        }

        int[] petalPalette = { 0x00FF7ED4, 0x00FFA8E2, 0x00FFD1F0, 0x00E49BFF };
        for (int i = 0; i < PETAL_COUNT; i++) {
            petX[i] = Math.abs(nextRand()) % Math.max(1, w);
            petY[i] = Math.abs(nextRand()) % Math.max(1, h);
            resetPetalKinematics(i, false, w);
            petColor[i] = petalPalette[i % petalPalette.length];
        }
    }

    private void resetPetalKinematics(int i, boolean respawnEdge, int w) {
        int r = Math.abs(nextRand());
        float depth = 0.65f + (r % 10) * 0.08f;
        petSize[i] = (4.8f + (r % 5) * 1.15f) * density * depth;
        petVx[i] = (0.75f + (r % 7) * 0.18f) * density * depth;
        petVy[i] = (0.95f + (r % 6) * 0.22f) * density * depth;
        petAngle[i] = (r % 360);
        petSpin[i] = ((r & 1) == 0 ? 1f : -1f) * (1.2f + (r % 5) * 0.45f);
        petPhase[i] = (r % 628) * 0.01f;
        if (respawnEdge) {
            if ((r & 3) == 0) {
                petX[i] = -14f * density;
                petY[i] = (r % Math.max(1, Math.max(200, getHeight()))) * 0.7f;
            } else {
                petX[i] = (r % Math.max(1, w)) - w * 0.15f;
                petY[i] = -12f * density;
            }
        }
    }

    private void spawnMeteor(int w, int h) {
        meteorActive = true;
        meteorLife = 1.0f;
        meteorX = w * (0.28f + (Math.abs(nextRand()) % 55) * 0.01f);
        meteorY = h * (0.04f + (Math.abs(nextRand()) % 22) * 0.01f);
        meteorVx = -(11f + (Math.abs(nextRand()) % 6)) * density;
        meteorVy = (5.2f + (Math.abs(nextRand()) % 4)) * density;
    }

    private void stepSimulation() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        timeSec += 0.05f;
        long now = System.currentTimeMillis();
        float windMul = now < burstUntilMs ? 2.15f : 1.0f;

        for (int i = 0; i < PETAL_COUNT; i++) {
            float sway = (float) Math.sin(timeSec * 1.9f + petPhase[i]) * 0.65f * density;
            petX[i] += petVx[i] * windMul + sway;
            petY[i] += petVy[i] * (now < burstUntilMs ? 1.45f : 1.0f);
            petAngle[i] = (petAngle[i] + petSpin[i] * windMul) % 360f;
            if (petY[i] > h + 16f * density || petX[i] > w + 20f * density) {
                resetPetalKinematics(i, true, w);
            }
        }

        if (meteorActive) {
            meteorX += meteorVx;
            meteorY += meteorVy;
            meteorLife -= 0.065f;
            if (meteorLife <= 0f || meteorX < -60f * density || meteorY > h + 40f * density) {
                meteorActive = false;
                nextMeteorAtMs = now + 3800L + (Math.abs(nextRand()) % 3600);
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

        // 1. Deep midnight twilight gradient sky + soft sakura nebula glow
        canvas.drawRect(0f, 0f, w, h, skyPaint);
        canvas.drawRect(0f, 0f, w, h, nebulaPaint);

        // 2. Soft radiant crescent moon in upper-right twilight sky
        float moonR = Math.min(w, h) * 0.075f;
        float mx = w * 0.87f;
        float my = h * 0.16f;
        starPaint.setColor(0x22FFD56B);
        canvas.drawCircle(mx, my, moonR * 1.65f, starPaint);
        canvas.drawCircle(mx, my, moonR, moonPaint);
        canvas.drawCircle(mx - moonR * 0.36f, my - moonR * 0.18f, moonR * 0.88f, moonCutPaint);

        // 3. Twinkling starfield + 4-point anime starlight diamonds
        for (int i = 0; i < STAR_COUNT; i++) {
            float pulse = running ? (0.45f + 0.55f * (float) Math.sin(timeSec * 2.2f + starPhase[i])) : 0.65f;
            int alpha = Math.max(40, Math.min(245, Math.round(pulse * 225f)));
            starPaint.setColor((alpha << 24) | starColor[i]);
            if (starDiamond[i]) {
                drawDiamondStar(canvas, starX[i], starY[i], starSize[i] * (1.8f + pulse * 0.9f), starPaint);
            } else {
                canvas.drawCircle(starX[i], starY[i], starSize[i], starPaint);
            }
        }

        // 4. Shooting star comet streak
        if (meteorActive && meteorLife > 0f) {
            float tailX = meteorX - meteorVx * 6.5f;
            float tailY = meteorY - meteorVy * 6.5f;
            int aOuter = Math.max(0, Math.min(255, Math.round(meteorLife * 110f)));
            int aCore = Math.max(0, Math.min(255, Math.round(meteorLife * 245f)));
            meteorPaint.setStrokeWidth(4.0f * density);
            meteorPaint.setColor((aOuter << 24) | 0x00FF7ED4);
            canvas.drawLine(meteorX, meteorY, tailX, tailY, meteorPaint);
            meteorPaint.setStrokeWidth(1.8f * density);
            meteorPaint.setColor((aCore << 24) | 0x007CE8FF);
            canvas.drawLine(meteorX, meteorY, (meteorX + tailX) * 0.5f, (meteorY + tailY) * 0.5f, meteorPaint);
            starPaint.setColor((aCore << 24) | 0x00FFF8E7);
            drawDiamondStar(canvas, meteorX, meteorY, 5.2f * density * meteorLife, starPaint);
        }

        // 5. Drifting cherry blossom (Sakura) petals
        for (int i = 0; i < PETAL_COUNT; i++) {
            int alpha = running ? 175 : 105;
            petalPaint.setColor((alpha << 24) | petColor[i]);
            drawSakuraPetal(canvas, petX[i], petY[i], petSize[i], petAngle[i], petalPaint);
        }
    }

    private void drawSakuraPetal(Canvas canvas, float cx, float cy, float s, float deg, Paint paint) {
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
