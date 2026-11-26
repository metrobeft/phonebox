package net.oxog.phonebox.hp71.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Widget modular de controle do volume mestre do PC (botão mudo compacto, -5%, barra tocável e +5%).
 */
public final class MasterVolumeWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final LinearLayout root;
    private final ActionButtonView muteBtn;
    private final ActionButtonView downBtn;
    private final VolumeTrackView trackView;
    private final TextView pctView;
    private final ActionButtonView upBtn;

    private int volume = 0;
    private boolean muted = false;

    public MasterVolumeWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        ThemeDimens d = AppTheme.get().dimens();
        root = new LinearLayout(wctx.context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);

        muteBtn = new ActionButtonView(wctx.context).setCompact();
        muteBtn.setButton("SOM", ActionButtonView.VARIANT_NORMAL);
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/volume?op=mute", null, null, true); }
        });
        LinearLayout.LayoutParams lpMute = new LinearLayout.LayoutParams(d.dp(50), d.dp(36));
        lpMute.rightMargin = d.dp(6);
        root.addView(muteBtn, lpMute);

        downBtn = new ActionButtonView(wctx.context).setCompact();
        downBtn.setButton("-", ActionButtonView.VARIANT_NORMAL);
        downBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/volume?op=down", null, null, true); }
        });
        LinearLayout.LayoutParams lpDown = new LinearLayout.LayoutParams(d.dp(38), d.dp(36));
        lpDown.rightMargin = d.dp(8);
        root.addView(downBtn, lpDown);

        trackView = new VolumeTrackView(wctx.context);
        trackView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                int act = ev.getAction();
                if (act == MotionEvent.ACTION_DOWN) {
                    ViewParent vp = v.getParent();
                    if (vp != null) vp.requestDisallowInterceptTouchEvent(true);
                }
                if (act == MotionEvent.ACTION_DOWN || act == MotionEvent.ACTION_MOVE || act == MotionEvent.ACTION_UP) {
                    int w = v.getWidth();
                    if (w > 0) {
                        int pct = Math.max(0, Math.min(100, Math.round((ev.getX() / w) * 100f)));
                        trackView.setLevel(pct, false);
                        pctView.setText(pct + "%");
                        if (act == MotionEvent.ACTION_UP) {
                            wctx.sync.postAction("/api/volume?level=" + pct, null, null, true);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        LinearLayout.LayoutParams lpTrack = new LinearLayout.LayoutParams(0, d.dp(32), 1f);
        lpTrack.rightMargin = d.dp(8);
        root.addView(trackView, lpTrack);

        pctView = new TextView(wctx.context);
        pctView.setTypeface(Typeface.DEFAULT_BOLD);
        pctView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        pctView.setGravity(Gravity.CENTER);
        pctView.setText("--%");
        LinearLayout.LayoutParams lpPct = new LinearLayout.LayoutParams(d.dp(42), LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPct.rightMargin = d.dp(6);
        root.addView(pctView, lpPct);

        upBtn = new ActionButtonView(wctx.context).setCompact();
        upBtn.setButton("+", ActionButtonView.VARIANT_NORMAL);
        upBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/volume?op=up", null, null, true); }
        });
        root.addView(upBtn, new LinearLayout.LayoutParams(d.dp(38), d.dp(36)));

        applyTheme();
    }

    @Override
    public View getView() {
        return root;
    }

    @Override
    public void bindData(AppState state) {
        this.volume = state.audio.volume;
        this.muted = state.audio.muted;
        trackView.setLevel(volume, muted);
        pctView.setText(muted ? "0%" : (volume + "%"));
        muteBtn.setButton(muted ? "MUDO" : "SOM",
                muted ? ActionButtonView.VARIANT_DANGER : ActionButtonView.VARIANT_NORMAL);
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        pctView.setTextColor(c.accent);
        muteBtn.applyTheme();
        downBtn.applyTheme();
        upBtn.applyTheme();
        trackView.applyTheme();
    }

    private static final class VolumeTrackView extends View {
        private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeSheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeTipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private int level = 0;
        private boolean muted = false;

        VolumeTrackView(android.content.Context ctx) {
            super(ctx);
            animeRimPaint.setStyle(Paint.Style.STROKE);
            animeRimPaint.setStrokeWidth(Math.max(1f, AppTheme.get().dimens().density * 0.9f));
            animeRimPaint.setColor(0xAA6B4C9A);
            animeSheenPaint.setStyle(Paint.Style.FILL);
            animeSheenPaint.setColor(0x52FFFFFF);
            animeTipPaint.setStyle(Paint.Style.FILL);
            animeTipPaint.setColor(0xF0FFF5FD);
            applyTheme();
        }

        void setLevel(int lvl, boolean isMuted) {
            this.level = Math.max(0, Math.min(100, lvl));
            this.muted = isMuted;
            applyTheme();
        }

        void applyTheme() {
            ThemeColors c = AppTheme.get().colors();
            trackPaint.setColor(c.trackBg);
            fillPaint.setColor(muted ? c.red : c.accent);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;
            float d = AppTheme.get().dimens().density;
            float barH = AppTheme.get().dimens().dp(14);
            float top = (h - barH) * 0.5f;
            if (AppTheme.get().isHack()) {
                canvas.drawRect(0f, top, w, top + barH, trackPaint);
                if (!muted && level > 0) {
                    int segs = Math.max(8, Math.round(w / (8f * d)));
                    float step = (w - 4f * d) / (float) segs;
                    int lit = Math.max(1, Math.min(segs, Math.round((segs * level) / 100f)));
                    for (int i = 0; i < lit; i++) {
                        float x0 = 2f * d + i * step;
                        canvas.drawRect(x0, top + 2f * d, x0 + Math.max(2f, step - 2f * d), top + barH - 2f * d, fillPaint);
                    }
                }
                return;
            }
            float r = barH * 0.5f;
            boolean anime = AppTheme.get().isAnime();
            rect.set(0f, top, w, top + barH);
            canvas.drawRoundRect(rect, r, r, trackPaint);
            if (anime) {
                rect.set(0.5f, top + 0.5f, w - 0.5f, top + barH - 0.5f);
                canvas.drawRoundRect(rect, r, r, animeRimPaint);
            }
            if (!muted && level > 0) {
                float fw = Math.max(barH, (w * level) / 100f);
                if (fw > w) fw = w;
                rect.set(0f, top, fw, top + barH);
                canvas.drawRoundRect(rect, r, r, fillPaint);
                if (anime) {
                    rect.set(barH * 0.2f, top + barH * 0.14f, Math.max(barH * 0.5f, fw - barH * 0.2f), top + barH * 0.48f);
                    canvas.drawRoundRect(rect, r * 0.45f, r * 0.45f, animeSheenPaint);
                    canvas.drawCircle(fw - barH * 0.42f, top + barH * 0.5f, Math.max(2f, barH * 0.22f), animeTipPaint);
                }
            }
        }
    }
}

