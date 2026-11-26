package net.oxog.phonebox.hp71.theme.anime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import net.oxog.phonebox.hp71.R;
import net.oxog.phonebox.hp71.theme.ThemeColors;

/**
 * Sequência de entrada animada personalizada para os 4 mundos vivos (toque para pular):
 * - "sakura": Sakura Storm (lua crescente + anel de pétalas + estrelas douradas);
 * - "lofi": Lofi Room (disco de vinil girando + equalizador quente + luzinhas);
 * - "starfall": Starfall Aurora (arco de aurora + constelação autodesenhada + Torii);
 * - "miku": Hatsune Miku 01 (anéis neon ciano/magenta + equalizador + holograma da mascote Miku!).
 * 100% API 17, zero alocações em onDraw() e zero dependência de glifos Unicode fora do DroidSans.
 */
public final class AnimeEntranceOverlayView extends View {

    private static final long TOTAL_MS = 2200L;
    private static final long FRAME_MS = 34L;
    private static final int RING_ITEMS = 16;

    private final Paint bgPaint = new Paint();
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path workPath = new Path();
    private final Path starPath = new Path();
    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String world = "sakura";
    private Bitmap mikuBitmap = null;
    private boolean mikuLoadTried = false;

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
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStyle(Paint.Style.FILL);

        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        subPaint.setTypeface(Typeface.DEFAULT_BOLD);
        subPaint.setTextAlign(Paint.Align.CENTER);

        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setTextSize(11.5f * density);
    }

    public void setWorld(String worldId) {
        String norm = ThemeColors.normalizePresetId(worldId);
        if (!ThemeColors.isLivingWorldId(norm)) norm = "sakura";
        this.world = norm;
        if ("miku".equals(norm)) ensureMikuBitmap();
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

    public void play(Runnable onDone) {
        if ("miku".equals(world)) ensureMikuBitmap();
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
        float masterFade = 1f;
        if (elapsed > TOTAL_MS - 420L) {
            masterFade = Math.max(0f, 1f - ((elapsed - (TOTAL_MS - 420L)) / 420f));
        }

        if ("lofi".equals(world)) {
            drawLofiEntrance(canvas, w, h, elapsed, progress, masterFade);
        } else if ("starfall".equals(world)) {
            drawStarfallEntrance(canvas, w, h, elapsed, progress, masterFade);
        } else if ("miku".equals(world)) {
            drawMikuEntrance(canvas, w, h, elapsed, progress, masterFade);
        } else {
            drawSakuraEntrance(canvas, w, h, elapsed, progress, masterFade);
        }
    }

    private void drawSakuraEntrance(Canvas canvas, int w, int h, long elapsed, float progress, float fade) {
        bgPaint.setColor((Math.round(240f * fade) << 24) | 0x000A061A);
        canvas.drawRect(0f, 0f, w, h, bgPaint);

        float cx = w * 0.5f;
        float cy = h * 0.46f;
        float minDim = Math.min(w, h);
        float ease = 1f - (1f - progress) * (1f - progress) * (1f - progress);
        float ringR = minDim * (0.14f + 0.38f * ease);

        strokePaint.setStrokeWidth(2.2f * density);
        strokePaint.setColor((Math.round(135f * fade * (1f - progress * 0.4f)) << 24) | 0x00FF7ED4);
        canvas.drawCircle(cx, cy, ringR, strokePaint);
        strokePaint.setStrokeWidth(1.2f * density);
        strokePaint.setColor((Math.round(115f * fade) << 24) | 0x007CE8FF);
        canvas.drawCircle(cx, cy, ringR * 0.72f, strokePaint);

        float moonR = Math.max(18f * density, minDim * 0.068f) * Math.min(1f, elapsed / 320f);
        float my = cy - 48f * density;
        fillPaint.setColor((Math.round(240f * fade) << 24) | 0x00FFF2CC);
        canvas.drawCircle(cx, my, moonR, fillPaint);
        fillPaint.setColor((Math.round(245f * fade) << 24) | 0x000A061A);
        canvas.drawCircle(cx - moonR * 0.38f, my - moonR * 0.16f, moonR * 0.86f, fillPaint);

        for (int i = 0; i < RING_ITEMS; i++) {
            double ang = (i * Math.PI * 2.0 / RING_ITEMS) + progress * 2.4;
            float dist = minDim * (0.08f + 0.42f * ease) * (0.82f + (i % 3) * 0.14f);
            float px = cx + (float) Math.cos(ang) * dist;
            float py = cy + (float) Math.sin(ang) * dist * 0.75f;
            fillPaint.setColor((Math.round(210f * fade) << 24) | (i % 2 == 0 ? 0x00FF7ED4 : 0x00FFD1F0));
            drawPetal(canvas, px, py, (6.5f + (i % 3) * 1.6f) * density, (float) Math.toDegrees(ang) + 45f);
        }

        drawTitleBlock(canvas, w, h, cx, cy, elapsed, fade,
                "PHONEBOX  SAKURA", "SAKURA STORM EDITION  //  HP 7.1",
                0x00FFF5FD, 0x00FF7ED4, 0x00FFD56B, 0x00C0A2E8);
    }

    private void drawLofiEntrance(Canvas canvas, int w, int h, long elapsed, float progress, float fade) {
        bgPaint.setColor((Math.round(242f * fade) << 24) | 0x00130A1E);
        canvas.drawRect(0f, 0f, w, h, bgPaint);

        float cx = w * 0.5f;
        float cy = h * 0.47f;
        float minDim = Math.min(w, h);
        float ease = 1f - (1f - progress) * (1f - progress) * (1f - progress);

        // Spinning warm vinyl emblem above title
        float vr = Math.max(24f * density, minDim * 0.088f) * Math.min(1f, elapsed / 300f);
        float vy = cy - 54f * density;
        fillPaint.setColor((Math.round(45f * fade) << 24) | 0x00FFB38A);
        canvas.drawCircle(cx, vy, vr * 1.5f, fillPaint);
        fillPaint.setColor((Math.round(235f * fade) << 24) | 0x001E112C);
        canvas.drawCircle(cx, vy, vr, fillPaint);
        strokePaint.setStrokeWidth(1.5f * density);
        strokePaint.setColor((Math.round(160f * fade) << 24) | 0x00FFB38A);
        canvas.drawCircle(cx, vy, vr, strokePaint);
        strokePaint.setStrokeWidth(1f * density);
        strokePaint.setColor((Math.round(95f * fade) << 24) | 0x00FFE066);
        canvas.drawCircle(cx, vy, vr * 0.72f, strokePaint);
        canvas.drawCircle(cx, vy, vr * 0.52f, strokePaint);
        fillPaint.setColor((Math.round(235f * fade) << 24) | 0x00FFB38A);
        canvas.drawCircle(cx, vy, vr * 0.30f, fillPaint);
        fillPaint.setColor((Math.round(245f * fade) << 24) | 0x00130A1E);
        canvas.drawCircle(cx, vy, vr * 0.09f, fillPaint);

        // Rotating glint on vinyl
        double spin = progress * Math.PI * 3.5;
        strokePaint.setStrokeWidth(2.2f * density);
        strokePaint.setColor((Math.round(180f * fade) << 24) | 0x00FFF2CC);
        canvas.drawLine(
                cx + (float) Math.cos(spin) * vr * 0.40f, vy + (float) Math.sin(spin) * vr * 0.40f,
                cx + (float) Math.cos(spin) * vr * 0.86f, vy + (float) Math.sin(spin) * vr * 0.86f,
                strokePaint);

        // Warm bouncing equalizer bars below hint
        int bars = 14;
        float barW = 7f * density;
        float gap = 5f * density;
        float totalW = bars * (barW + gap);
        float startX = cx - totalW * 0.5f;
        float baseY = cy + 104f * density;
        for (int i = 0; i < bars; i++) {
            float bh = (6f + 18f * (0.5f + 0.5f * (float) Math.sin(elapsed * 0.012f + i * 0.7f))) * density * ease;
            fillPaint.setColor((Math.round(185f * fade) << 24) | (i % 3 == 0 ? 0x009FE3C9 : (i % 2 == 0 ? 0x00FFB38A : 0x00FFE066)));
            canvas.drawRect(startX + i * (barW + gap), baseY - bh, startX + i * (barW + gap) + barW, baseY, fillPaint);
        }

        drawTitleBlock(canvas, w, h, cx, cy, elapsed, fade,
                "PHONEBOX  LOFI", "COZY RAINY ROOM  //  72 BPM",
                0x00FFF4EC, 0x00FFB38A, 0x00FFE066, 0x00C9A6C4);
    }

    private void drawStarfallEntrance(Canvas canvas, int w, int h, long elapsed, float progress, float fade) {
        bgPaint.setColor((Math.round(242f * fade) << 24) | 0x00020512);
        canvas.drawRect(0f, 0f, w, h, bgPaint);

        float cx = w * 0.5f;
        float cy = h * 0.47f;
        float minDim = Math.min(w, h);
        float ease = 1f - (1f - progress) * (1f - progress) * (1f - progress);

        // Expanding aurora emerald & violet rings
        strokePaint.setStrokeWidth(2.6f * density);
        strokePaint.setColor((Math.round(135f * fade) << 24) | 0x006DFFB0);
        canvas.drawCircle(cx, cy, minDim * (0.14f + 0.36f * ease), strokePaint);
        strokePaint.setStrokeWidth(1.5f * density);
        strokePaint.setColor((Math.round(120f * fade) << 24) | 0x00B57CFF);
        canvas.drawCircle(cx, cy, minDim * (0.10f + 0.28f * ease), strokePaint);

        // Self-drawing constellation crown above title
        float ay = cy - 54f * density;
        float span = Math.min(w * 0.28f, 110f * density);
        float[] cxs = { cx - span, cx - span * 0.45f, cx, cx + span * 0.45f, cx + span };
        float[] cys = { ay + 12f * density, ay - 14f * density, ay + 4f * density, ay - 16f * density, ay + 10f * density };
        float drawn = 4f * Math.min(1f, progress * 1.8f);
        strokePaint.setStrokeWidth(1.6f * density);
        strokePaint.setColor((Math.round(205f * fade) << 24) | 0x007FE7FF);
        for (int i = 0; i < 4; i++) {
            if (i > drawn) break;
            float f = Math.min(1f, drawn - i);
            canvas.drawLine(cxs[i], cys[i], cxs[i] + (cxs[i + 1] - cxs[i]) * f, cys[i] + (cys[i + 1] - cys[i]) * f, strokePaint);
        }
        for (int i = 0; i < 5; i++) {
            fillPaint.setColor((Math.round(235f * fade) << 24) | (i % 2 == 0 ? 0x006DFFB0 : 0x00FFE066));
            drawStar(canvas, cxs[i], cys[i], (4.2f + (i == 2 ? 2.5f : 0f)) * density, fillPaint);
        }

        drawTitleBlock(canvas, w, h, cx, cy, elapsed, fade,
                "PHONEBOX  STARFALL", "AURORA & CONSTELLATIONS  //  HP 7.1",
                0x00F0FBFF, 0x007FE7FF, 0x006DFFB0, 0x0088B4DC);
    }

    private void drawMikuEntrance(Canvas canvas, int w, int h, long elapsed, float progress, float fade) {
        bgPaint.setColor((Math.round(244f * fade) << 24) | 0x00031015);
        canvas.drawRect(0f, 0f, w, h, bgPaint);

        boolean portrait = h > w;
        float cx = portrait ? (w * 0.5f) : (w * 0.40f);
        float cy = portrait ? (h * 0.54f) : (h * 0.48f);
        float minDim = Math.min(w, h);
        float ease = 1f - (1f - progress) * (1f - progress) * (1f - progress);

        // Glowing Teal & Magenta concert rings
        float ringCx = portrait ? (w * 0.5f) : (w * 0.72f);
        float ringCy = portrait ? (h * 0.30f) : (h * 0.50f);
        strokePaint.setStrokeWidth(2.4f * density);
        strokePaint.setColor((Math.round(150f * fade) << 24) | 0x0039C5BB);
        canvas.drawCircle(ringCx, ringCy, minDim * (0.16f + 0.24f * ease), strokePaint);
        strokePaint.setStrokeWidth(1.5f * density);
        strokePaint.setColor((Math.round(135f * fade) << 24) | 0x00FF2E88);
        canvas.drawCircle(ringCx, ringCy, minDim * (0.12f + 0.19f * ease), strokePaint);

        // Crisp floating Hatsune Miku mascot rising into stage spotlight
        if (mikuBitmap != null && !mikuBitmap.isRecycled()) {
            float mH = Math.min(h * (portrait ? 0.36f : 0.68f), 260f * density);
            float ratio = mikuBitmap.getWidth() / (float) Math.max(1, mikuBitmap.getHeight());
            float mW = mH * ratio;
            float rise = (1f - ease) * 26f * density;
            float bob = (float) Math.sin(elapsed * 0.008f) * 4f * density;
            float left = ringCx - mW * 0.5f;
            float top = ringCy - mH * 0.5f + rise + bob;
            srcRect.set(0, 0, mikuBitmap.getWidth(), mikuBitmap.getHeight());
            dstRect.set(left, top, left + mW, top + mH);
            bitmapPaint.setAlpha(Math.round(245f * Math.min(1f, progress * 2.2f) * fade));
            canvas.drawBitmap(mikuBitmap, srcRect, dstRect, bitmapPaint);
        }

        // Animated concert equalizer bars along bottom
        int bars = 18;
        float slot = w / (float) bars;
        for (int i = 0; i < bars; i++) {
            float bh = (10f + 34f * (0.5f + 0.5f * (float) Math.sin(elapsed * 0.014f + i * 0.6f))) * density * ease;
            fillPaint.setColor((Math.round(150f * fade) << 24) | (i % 5 == 2 ? 0x00FF2E88 : (i % 3 == 0 ? 0x007FE7FF : 0x0039C5BB)));
            canvas.drawRect(i * slot + 3f * density, h - bh, (i + 1) * slot - 3f * density, h, fillPaint);
        }

        drawTitleBlock(canvas, w, h, cx, cy, elapsed, fade,
                "HATSUNE MIKU  01", "CYBER VOCAL STAGE  //  HP 7.1",
                0x00EEFFFE, 0x0039C5BB, 0x00FF2E88, 0x0079C4C7);
    }

    private void drawTitleBlock(Canvas canvas, int w, int h, float cx, float cy, long elapsed, float fade,
                                String title, String subtitle, int titleRgb, int accentRgb, int lineRgb, int hintRgb) {
        float textAlpha = Math.min(1f, Math.max(0f, (elapsed - 160L) / 360f)) * fade;
        int ta = Math.round(255f * textAlpha);
        boolean portrait = h > w;

        titlePaint.setTextSize((portrait ? 21f : 26f) * density);
        titlePaint.setColor((ta << 24) | titleRgb);
        titlePaint.setShadowLayer(8f, 0f, 0f, (Math.round(190f * textAlpha) << 24) | accentRgb);
        canvas.drawText(title, cx, cy + 16f * density, titlePaint);

        float lineHalf = Math.min(w * 0.30f, 142f * density) * Math.min(1f, Math.max(0f, (elapsed - 260L) / 400f));
        strokePaint.setStrokeWidth(Math.max(1.2f, 1.5f * density));
        strokePaint.setColor((Math.round(210f * textAlpha) << 24) | lineRgb);
        canvas.drawLine(cx - lineHalf, cy + 28f * density, cx + lineHalf, cy + 28f * density, strokePaint);

        subPaint.setTextSize((portrait ? 12f : 13.5f) * density);
        subPaint.setColor((ta << 24) | accentRgb);
        canvas.drawText(subtitle, cx, cy + 48f * density, subPaint);

        hintPaint.setColor((Math.round(175f * textAlpha) << 24) | hintRgb);
        canvas.drawText("Toque para entrar", cx, cy + 74f * density, hintPaint);

        fillPaint.setColor((ta << 24) | lineRgb);
        float sp = 0.7f + 0.3f * (float) Math.sin(elapsed * 0.014f);
        drawStar(canvas, cx - lineHalf - 10f * density, cy + 28f * density, 6.2f * density * sp, fillPaint);
        drawStar(canvas, cx + lineHalf + 10f * density, cy + 28f * density, 6.2f * density * sp, fillPaint);
    }

    private void drawPetal(Canvas canvas, float cx, float cy, float s, float deg) {
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(deg);
        workPath.reset();
        workPath.moveTo(0f, -s);
        workPath.quadTo(s * 0.85f, -s * 0.25f, 0f, s * 0.9f);
        workPath.quadTo(-s * 0.85f, -s * 0.25f, 0f, -s);
        workPath.close();
        canvas.drawPath(workPath, fillPaint);
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
