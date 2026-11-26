package net.oxog.phonebox.hp71.widgets;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.TouchActionTileView;
import net.oxog.phonebox.hp71.core.ApiClient;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.core.BitmapCache;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Widget modular do Mixer por Aplicativo do Windows (lê audio.sessions e comanda /api/controls/command/session).
 * Ícone oficial do app (com monograma fallback), barra tocável/arrastável, porcentagem e botões - / + / mudo.
 */
public final class AudioMixerWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final CardViewContainer card;
    private final ActionButtonView refreshBtn;
    private final LinearLayout listBox;
    private final TextView emptyView;
    private boolean draggingBar = false;
    private String lastSignature = "";

    public AudioMixerWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        card = new CardViewContainer(wctx.context).setHeader("MIXER POR APLICATIVO");
        refreshBtn = new ActionButtonView(wctx.context).setCompact().setButton("Atualizar", ActionButtonView.VARIANT_NORMAL);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.requestImmediateSync(false); }
        });
        card.setHeaderRight(refreshBtn);

        listBox = new LinearLayout(wctx.context);
        listBox.setOrientation(LinearLayout.VERTICAL);
        card.addBodyView(listBox);

        emptyView = new TextView(wctx.context);
        emptyView.setText("Nenhum aplicativo reproduzindo audio agora.");
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        listBox.addView(emptyView);

        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        card.setVisibility(state.showAudioMixer ? View.VISIBLE : View.GONE);
        if (!state.showAudioMixer || draggingBar) return;

        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        if (state.audioSessions.isEmpty()) {
            if (!"EMPTY".equals(lastSignature)) {
                lastSignature = "EMPTY";
                listBox.removeAllViews();
                emptyView.setTextColor(c.textMuted);
                listBox.addView(emptyView);
            }
            return;
        }

        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < state.audioSessions.size(); i++) {
            AppState.AudioSession s = state.audioSessions.get(i);
            sig.append(s.id).append('|').append(s.volume).append('|').append(s.muted).append(';');
        }
        String nextSig = sig.toString();
        if (nextSig.equals(lastSignature)) return;
        lastSignature = nextSig;
        listBox.removeAllViews();

        String base = wctx.api.baseUrl();
        for (int i = 0; i < state.audioSessions.size(); i++) {
            final AppState.AudioSession s = state.audioSessions.get(i);
            LinearLayout row = new LinearLayout(wctx.context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, d.dp(6), 0, d.dp(6));

            // Ícone oficial do processo com fallback monograma (para "System sounds" etc.)
            FrameLayout icoBox = new FrameLayout(wctx.context);
            icoBox.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.dp(7)));
            final TextView monoView = new TextView(wctx.context);
            monoView.setGravity(Gravity.CENTER);
            monoView.setTypeface(Typeface.DEFAULT_BOLD);
            monoView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            monoView.setTextColor(c.accent);
            monoView.setText(TouchActionTileView.monogram(s.name));
            icoBox.addView(monoView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            final ImageView icoImg = new ImageView(wctx.context);
            icoImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icoImg.setPadding(d.dp(4), d.dp(4), d.dp(4), d.dp(4));
            icoImg.setVisibility(View.GONE);
            icoBox.addView(icoImg, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            LinearLayout.LayoutParams icoLp = new LinearLayout.LayoutParams(d.dp(34), d.dp(34));
            icoLp.rightMargin = d.dp(10);
            row.addView(icoBox, icoLp);

            String procKey = s.processName.replace(".exe", "").replace(".EXE", "");
            wctx.bitmaps.load("proc:" + procKey, base + "/api/procs/icon?name=" + ApiClient.enc(procKey), null, 64,
                    new BitmapCache.Callback() {
                        @Override
                        public void onBitmapLoaded(String key, Bitmap bmp) {
                            if (bmp != null) {
                                icoImg.setImageBitmap(bmp);
                                icoImg.setVisibility(View.VISIBLE);
                                monoView.setVisibility(View.GONE);
                            }
                        }
                    });

            // Porcentagem (referenciada pela barra arrastável)
            final TextView pctView = new TextView(wctx.context);
            pctView.setTypeface(Typeface.DEFAULT_BOLD);
            pctView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            pctView.setTextColor(s.muted ? c.red : c.accent);
            pctView.setGravity(Gravity.CENTER);
            pctView.setText(s.muted ? "0%" : (s.volume + "%"));

            // Barra tocável e arrastável
            final SessionBar bar = new SessionBar(wctx.context, s.muted ? 0 : s.volume, s.muted);
            bar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent ev) {
                    int act = ev.getAction();
                    int w = v.getWidth();
                    if (act == MotionEvent.ACTION_DOWN) {
                        draggingBar = true;
                        ViewParent vp = v.getParent();
                        if (vp != null) vp.requestDisallowInterceptTouchEvent(true);
                    }
                    if ((act == MotionEvent.ACTION_DOWN || act == MotionEvent.ACTION_MOVE || act == MotionEvent.ACTION_UP) && w > 0) {
                        int pct = Math.max(0, Math.min(100, Math.round((ev.getX() / w) * 100f)));
                        bar.setLevel(pct, false);
                        pctView.setText(pct + "%");
                        if (act == MotionEvent.ACTION_UP) {
                            draggingBar = false;
                            wctx.sync.postAction(
                                    "/api/controls/command/session?id=" + ApiClient.enc(s.id) + "&value=" + pct,
                                    null, null, true);
                        }
                        return true;
                    }
                    if (act == MotionEvent.ACTION_CANCEL) {
                        draggingBar = false;
                    }
                    return false;
                }
            });
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(0, d.dp(28), 1f);
            barLp.rightMargin = d.dp(8);
            row.addView(bar, barLp);

            LinearLayout.LayoutParams pctLp = new LinearLayout.LayoutParams(d.dp(44), LinearLayout.LayoutParams.WRAP_CONTENT);
            pctLp.rightMargin = d.dp(6);
            row.addView(pctView, pctLp);

            // Controles -, +, Mute
            ActionButtonView minus = new ActionButtonView(wctx.context).setCompact().setButton("-", ActionButtonView.VARIANT_NORMAL);
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int next = Math.max(0, s.volume - 10);
                    wctx.sync.postAction("/api/controls/command/session?id=" + ApiClient.enc(s.id) + "&value=" + next, null, null, true);
                }
            });
            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(d.dp(36), d.dp(32));
            bLp.rightMargin = d.dp(5);
            row.addView(minus, bLp);

            ActionButtonView plus = new ActionButtonView(wctx.context).setCompact().setButton("+", ActionButtonView.VARIANT_NORMAL);
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int next = Math.min(100, s.volume + 10);
                    wctx.sync.postAction("/api/controls/command/session?id=" + ApiClient.enc(s.id) + "&value=" + next, null, null, true);
                }
            });
            LinearLayout.LayoutParams bLp2 = new LinearLayout.LayoutParams(d.dp(36), d.dp(32));
            bLp2.rightMargin = d.dp(5);
            row.addView(plus, bLp2);

            ActionButtonView mute = new ActionButtonView(wctx.context).setCompact()
                    .setButton(s.muted ? "OFF" : "ON", s.muted ? ActionButtonView.VARIANT_DANGER : ActionButtonView.VARIANT_NORMAL);
            mute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wctx.sync.postAction("/api/controls/command/session?id=" + ApiClient.enc(s.id) + "&muted=" + (!s.muted), null, null, true);
                }
            });
            row.addView(mute, new LinearLayout.LayoutParams(d.dp(46), d.dp(32)));

            listBox.addView(row);
            if (i < state.audioSessions.size() - 1) {
                View sep = new View(wctx.context);
                sep.setBackgroundColor(c.divider);
                listBox.addView(sep, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, d.strokePx));
            }
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        lastSignature = "";
        card.applyTheme();
        refreshBtn.applyTheme();
        emptyView.setTextColor(AppTheme.get().colors().textMuted);
    }

    private static final class SessionBar extends View {
        private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeRim = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeSheen = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint animeTip = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private int pct;

        SessionBar(android.content.Context ctx, int pct, boolean muted) {
            super(ctx);
            animeRim.setStyle(Paint.Style.STROKE);
            animeRim.setStrokeWidth(Math.max(1f, AppTheme.get().dimens().density * 0.85f));
            animeRim.setColor(0xAA6B4C9A);
            animeSheen.setStyle(Paint.Style.FILL);
            animeSheen.setColor(0x52FFFFFF);
            animeTip.setStyle(Paint.Style.FILL);
            animeTip.setColor(0xF0FFF5FD);
            setLevel(pct, muted);
        }

        void setLevel(int pct, boolean muted) {
            this.pct = Math.max(0, Math.min(100, pct));
            ThemeColors c = AppTheme.get().colors();
            track.setColor(c.trackBg);
            fill.setColor(muted ? c.red : c.accent);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;
            float d = AppTheme.get().dimens().density;
            float bh = AppTheme.get().dimens().dp(11);
            float top = (h - bh) * 0.5f;
            if (AppTheme.get().isHack()) {
                canvas.drawRect(0f, top, w, top + bh, track);
                if (pct > 0) {
                    int segs = Math.max(8, Math.round(w / (7f * d)));
                    float step = (w - 4f * d) / (float) segs;
                    int lit = Math.max(1, Math.min(segs, Math.round((segs * pct) / 100f)));
                    for (int i = 0; i < lit; i++) {
                        float x0 = 2f * d + i * step;
                        canvas.drawRect(x0, top + 1.5f * d, x0 + Math.max(2f, step - 1.8f * d), top + bh - 1.5f * d, fill);
                    }
                }
                return;
            }
            float r = bh * 0.5f;
            boolean anime = AppTheme.get().isAnime();
            rect.set(0f, top, w, top + bh);
            canvas.drawRoundRect(rect, r, r, track);
            if (anime) {
                rect.set(0.5f, top + 0.5f, w - 0.5f, top + bh - 0.5f);
                canvas.drawRoundRect(rect, r, r, animeRim);
            }
            if (pct > 0) {
                float fw = Math.max(bh, (w * pct) / 100f);
                if (fw > w) fw = w;
                rect.set(0f, top, fw, top + bh);
                canvas.drawRoundRect(rect, r, r, fill);
                if (anime) {
                    rect.set(bh * 0.2f, top + bh * 0.14f, Math.max(bh * 0.5f, fw - bh * 0.2f), top + bh * 0.48f);
                    canvas.drawRoundRect(rect, r * 0.45f, r * 0.45f, animeSheen);
                    canvas.drawCircle(fw - bh * 0.42f, top + bh * 0.5f, Math.max(1.8f, bh * 0.22f), animeTip);
                }
            }
        }
    }
}

