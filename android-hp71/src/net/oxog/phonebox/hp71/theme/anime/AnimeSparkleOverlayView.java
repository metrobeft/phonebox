package net.oxog.phonebox.hp71.theme.anime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import net.oxog.phonebox.hp71.theme.ThemeColors;

/**
 * Overlay transparente ao toque adaptado a todos os 4 mundos vivos (Sakura, Lofi, Starfall, Miku):
 * - Moldura delicada de cantos no tom de cada mundo (acima da barra preta fixa do Android 4.2.2);
 * - Partículas marginais temáticas (pétalas / bokeh quente / vaga-lumes / notas musicais);
 * - Faixa luminosa de transição ao trocar de aba.
 * Zero alocações em onDraw(), 100% API 17 e nunca intercepta toques.
 */
public final class AnimeSparkleOverlayView extends View {

    private static final int EDGE_PARTICLES = 6;
    private static final int SWEEP_STARS = 8;

    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint partPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ribbonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path starPath = new Path();
    private final Path partPath = new Path();
    private final RectF noteOval = new RectF();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final float[] px = new float[EDGE_PARTICLES];
    private final float[] py = new float[EDGE_PARTICLES];
    private final float[] pvx = new float[EDGE_PARTICLES];
    private final float[] pvy = new float[EDGE_PARTICLES];
    private final float[] pang = new float[EDGE_PARTICLES];
    private final float[] psize = new float[EDGE_PARTICLES];

    private String world = "sakura";
    private int cornerColor = 0x99FF7ED4;
    private int starRgb = 0x00FFD56B;
    private int sweepPrimaryRgb = 0x00FF7ED4;
    private int sweepCoreRgb = 0x00FFF2CC;

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
                boolean upward = "miku".equals(world) || "starfall".equals(world) || "lofi".equals(world) || "fantasy".equals(world);
                for (int i = 0; i < EDGE_PARTICLES; i++) {
                    px[i] += pvx[i] + (float) Math.sin(timeSec * 2.1f + i * 1.7f) * 0.55f * density;
                    py[i] += upward ? -pvy[i] : pvy[i];
                    pang[i] = (pang[i] + 2.2f) % 360f;
                    if (upward) {
                        if (py[i] < -18f * density || px[i] < -18f * density || px[i] > w + 18f * density) {
                            respawnEdgeParticle(i, w, h, true);
                        }
                    } else {
                        if (py[i] > h + 16f * density || px[i] > w + 16f * density) {
                            respawnEdgeParticle(i, w, h, true);
                        }
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

        starPaint.setStyle(Paint.Style.FILL);
        partPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        ribbonPaint.setStyle(Paint.Style.FILL);
        applyWorldPalette();
    }

    public void setWorld(String worldId) {
        String norm = ThemeColors.normalizePresetId(worldId);
        if (!ThemeColors.isLivingWorldId(norm)) norm = "sakura";
        if (norm.equals(this.world)) return;
        this.world = norm;
        applyWorldPalette();
        if (getWidth() > 0 && getHeight() > 0) {
            for (int i = 0; i < EDGE_PARTICLES; i++) {
                respawnEdgeParticle(i, getWidth(), getHeight(), false);
            }
        }
        invalidate();
    }

    private void applyWorldPalette() {
        if ("lofi".equals(world)) {
            cornerColor = 0x99FFB38A;
            starRgb = 0x00FFE066;
            sweepPrimaryRgb = 0x00FFB38A;
            sweepCoreRgb = 0x00FFF2CC;
        } else if ("starfall".equals(world)) {
            cornerColor = 0x997FE7FF;
            starRgb = 0x006DFFB0;
            sweepPrimaryRgb = 0x006DFFB0;
            sweepCoreRgb = 0x00E4FBFF;
        } else if ("miku".equals(world)) {
            cornerColor = 0xA839C5BB;
            starRgb = 0x00FF2E88;
            sweepPrimaryRgb = 0x0039C5BB;
            sweepCoreRgb = 0x00FF5CA8;
        } else if ("fantasy".equals(world)) {
            cornerColor = 0xAAF4C858;
            starRgb = 0x004CF2B2;
            sweepPrimaryRgb = 0x00F4C858;
            sweepCoreRgb = 0x004CF2B2;
        } else {
            cornerColor = 0x99FF7ED4;
            starRgb = 0x00FFD56B;
            sweepPrimaryRgb = 0x00FF7ED4;
            sweepCoreRgb = 0x00FFF2CC;
        }
        cornerPaint.setColor(cornerColor);
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
        for (int i = 0; i < EDGE_PARTICLES; i++) {
            respawnEdgeParticle(i, w, h, false);
        }
    }

    private void respawnEdgeParticle(int i, int w, int h, boolean edgeSpawn) {
        boolean rightSide = (i % 2 == 1);
        float marginW = Math.max(44f * density, w * 0.16f);
        px[i] = rightSide ? (w - marginW + (i * 19f * density) % marginW) : ((i * 23f * density) % marginW);
        boolean upward = "miku".equals(world) || "starfall".equals(world) || "lofi".equals(world) || "fantasy".equals(world);
        if (edgeSpawn) {
            py[i] = upward ? (h + 10f * density + i * 12f * density) : (-10f * density - i * 14f * density);
        } else {
            py[i] = (i * 97f * density) % Math.max(1, h);
        }
        pvx[i] = upward ? ((rightSide ? -0.18f : 0.18f) * density) : ((0.45f + (i % 3) * 0.25f) * density);
        pvy[i] = (0.72f + (i % 4) * 0.20f) * density;
        pang[i] = (i * 63) % 360;
        psize[i] = (4.2f + (i % 3) * 1.3f) * density;
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

        // 1. Delicate foreground edge particles tailored per world
        if (effectsRunning) {
            for (int i = 0; i < EDGE_PARTICLES; i++) {
                if ("lofi".equals(world)) {
                    float pulse = 0.55f + 0.45f * (float) Math.sin(timeSec * 2.2f + i);
                    partPaint.setColor((Math.round(pulse * 95f) << 24) | (i % 2 == 0 ? 0x00FFE066 : 0x00FFB38A));
                    canvas.drawCircle(px[i], py[i], psize[i] * 0.72f, partPaint);
                } else if ("starfall".equals(world)) {
                    float pulse = 0.5f + 0.5f * (float) Math.sin(timeSec * 2.8f + i * 1.3f);
                    partPaint.setColor((Math.round(pulse * 145f) << 24) | (i % 2 == 0 ? 0x00DCFF78 : 0x007FE7FF));
                    drawStar(canvas, px[i], py[i], psize[i] * 0.85f, partPaint);
                } else if ("miku".equals(world)) {
                    int col = (i % 2 == 0) ? 0x8239C5BB : 0x7AFF2E88;
                    drawEdgeNote(canvas, px[i], py[i], psize[i] * 0.92f, col);
                } else if ("fantasy".equals(world)) {
                    float pulse = 0.55f + 0.45f * (float) Math.sin(timeSec * 2.5f + i * 1.4f);
                    int rgb = (i % 3 == 0) ? 0x004CF2B2 : ((i % 2 == 0) ? 0x00F4C858 : 0x00B77BFF);
                    partPaint.setColor((Math.round(pulse * 65f) << 24) | rgb);
                    canvas.drawCircle(px[i], py[i], psize[i] * 1.55f, partPaint);
                    partPaint.setColor((Math.round(pulse * 190f) << 24) | rgb);
                    drawStar(canvas, px[i], py[i], psize[i] * 0.9f, partPaint);
                } else {
                    partPaint.setColor(i % 2 == 0 ? 0x6EFF9CE0 : 0x62FFD6F4);
                    canvas.save();
                    canvas.translate(px[i], py[i]);
                    canvas.rotate(pang[i]);
                    float s = psize[i];
                    partPath.reset();
                    partPath.moveTo(0f, -s);
                    partPath.quadTo(s * 0.85f, -s * 0.25f, 0f, s * 0.9f);
                    partPath.quadTo(-s * 0.85f, -s * 0.25f, 0f, -s);
                    partPath.close();
                    canvas.drawPath(partPath, partPaint);
                    canvas.restore();
                }
            }
        }

        // 2. Tab-switch themed luminous ribbon sweep
        if (sweepActive) {
            float x = w * sweepProgress;
            float fade = 1f - Math.abs(sweepProgress - 0.5f) * 1.7f;
            if (fade < 0f) fade = 0f;
            int aOuter = Math.round(fade * 82f);
            int aCore = Math.round(fade * 215f);
            ribbonPaint.setColor((aOuter << 24) | sweepPrimaryRgb);
            canvas.drawRect(x - 24f * density, 0f, x + 12f * density, h, ribbonPaint);
            ribbonPaint.setColor((aCore << 24) | sweepCoreRgb);
            canvas.drawRect(x - 2f * density, 0f, x + 2f * density, h, ribbonPaint);
            starPaint.setColor((aCore << 24) | starRgb);
            for (int s = 0; s < SWEEP_STARS; s++) {
                float sy = ((s + 0.5f) / (float) SWEEP_STARS) * h;
                float sx = x + ((s % 2 == 0 ? 1f : -1f) * 16f * density);
                drawStar(canvas, sx, sy, (3.5f + (s % 3) * 2f) * density * fade, starPaint);
            }
        }

        // 3. Corner Accents framing the HP 7.1 viewport cleanly above the fixed Android 4.2.2 bar
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
        starPaint.setColor((Math.round(twinkle * 220f) << 24) | starRgb);
        drawStar(canvas, m + 1.5f * density, m + 1.5f * density, 3.8f * density, starPaint);
        drawStar(canvas, w - m - 1.5f * density, h - m - 1.5f * density, 3.8f * density, starPaint);
    }

    private void drawEdgeNote(Canvas canvas, float cx, float cy, float s, int color) {
        partPaint.setColor(color);
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(Math.max(1.2f, s * 0.24f));
        noteOval.set(cx - s * 0.6f, cy + s * 0.1f, cx + s * 0.35f, cy + s * 0.8f);
        canvas.drawOval(noteOval, partPaint);
        canvas.drawLine(cx + s * 0.25f, cy + s * 0.45f, cx + s * 0.25f, cy - s * 1.05f, strokePaint);
        partPath.reset();
        partPath.moveTo(cx + s * 0.25f, cy - s * 1.05f);
        partPath.quadTo(cx + s * 1.1f, cy - s * 0.78f, cx + s * 0.82f, cy - s * 0.22f);
        canvas.drawPath(partPath, strokePaint);
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
