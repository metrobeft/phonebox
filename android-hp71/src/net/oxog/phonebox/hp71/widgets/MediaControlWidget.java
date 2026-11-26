package net.oxog.phonebox.hp71.widgets;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.core.BitmapCache;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Widget modular de controle de mídia (capa via BitmapCache, título, artista, Anterior/Play-Pause/Próxima).
 */
public final class MediaControlWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final LinearLayout root;
    private final ImageView artView;
    private final TextView appView;
    private final TextView titleView;
    private final TextView artistView;
    private final ActionButtonView prevBtn;
    private final ActionButtonView playBtn;
    private final ActionButtonView nextBtn;

    private String lastArtKey = "";

    public MediaControlWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        ThemeDimens d = AppTheme.get().dimens();
        root = new LinearLayout(wctx.context);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout head = new LinearLayout(wctx.context);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        artView = new ImageView(wctx.context);
        artView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int artSz = d.dp(62);
        LinearLayout.LayoutParams artLp = new LinearLayout.LayoutParams(artSz, artSz);
        artLp.rightMargin = d.dp(12);
        head.addView(artView, artLp);

        LinearLayout info = new LinearLayout(wctx.context);
        info.setOrientation(LinearLayout.VERTICAL);

        appView = new TextView(wctx.context);
        appView.setTypeface(Typeface.DEFAULT_BOLD);
        appView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        appView.setSingleLine(true);
        appView.setText("MIDIA");
        info.addView(appView);

        titleView = new TextView(wctx.context);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setText("Nada tocando");
        info.addView(titleView);

        artistView = new TextView(wctx.context);
        artistView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        artistView.setSingleLine(true);
        artistView.setEllipsize(TextUtils.TruncateAt.END);
        artistView.setText("Reproduza musica ou video no PC");
        info.addView(artistView);

        head.addView(info, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(head);

        LinearLayout btnRow = new LinearLayout(wctx.context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams brLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.dp(44));
        brLp.topMargin = d.dp(10);

        prevBtn = new ActionButtonView(wctx.context);
        prevBtn.setButton("|<", ActionButtonView.VARIANT_NORMAL);
        prevBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/media/prev", null, null, true); }
        });
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        p1.rightMargin = d.dp(8);
        btnRow.addView(prevBtn, p1);

        playBtn = new ActionButtonView(wctx.context);
        playBtn.setButton("PLAY", ActionButtonView.VARIANT_PRIMARY);
        playBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/media/playpause", null, null, true); }
        });
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.2f);
        p2.rightMargin = d.dp(8);
        btnRow.addView(playBtn, p2);

        nextBtn = new ActionButtonView(wctx.context);
        nextBtn.setButton(">|", ActionButtonView.VARIANT_NORMAL);
        nextBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/media/next", null, null, true); }
        });
        btnRow.addView(nextBtn, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        root.addView(btnRow, brLp);
        applyTheme();
    }

    @Override
    public View getView() {
        return root;
    }

    @Override
    public void bindData(AppState state) {
        AppState.MediaInfo m = state.media;
        boolean hasTrack = (m.title != null && m.title.length() > 0) || m.playing;
        if (!hasTrack) {
            appView.setText("M\u00cdDIA");
            titleView.setText("Nada tocando");
            artistView.setText("Reproduza m\u00fasica ou v\u00eddeo no PC");
            playBtn.setButton("PLAY", ActionButtonView.VARIANT_PRIMARY);
            if (lastArtKey.length() > 0) {
                lastArtKey = "";
                artView.setImageBitmap(null);
            }
            return;
        }
        String appLbl = m.app.length() > 0 ? m.app.toUpperCase() : "M\u00cdDIA";
        appView.setText(appLbl + (m.playing ? " \u00b7 TOCANDO" : " \u00b7 PAUSADO"));
        titleView.setText(m.title.length() > 0 ? m.title : appLbl);
        artistView.setText(m.artist.length() > 0 ? m.artist : m.album);
        playBtn.setButton(m.playing ? "PAUSAR" : "PLAY", ActionButtonView.VARIANT_PRIMARY);

        String key = m.artKey();
        if (!key.equals(lastArtKey)) {
            lastArtKey = key;
            if (m.hasArt) {
                String url = wctx.api.baseUrl() + "/api/art?v=" + m.artId + "&_t=" + System.currentTimeMillis();
                wctx.bitmaps.load("art:" + key, url, null, 160, new BitmapCache.Callback() {
                    @Override
                    public void onBitmapLoaded(String k, Bitmap bmp) {
                        if (("art:" + lastArtKey).equals(k)) artView.setImageBitmap(bmp);
                    }
                });
            } else {
                artView.setImageBitmap(null);
            }
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        AppTheme t = AppTheme.get();
        ThemeColors c = t.colors();
        ThemeDimens d = t.dimens();
        artView.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.controlRadius));
        appView.setTextColor(c.accent);
        titleView.setTextColor(c.textPrimary);
        artistView.setTextColor(c.textMuted);
        prevBtn.applyTheme();
        playBtn.applyTheme();
        nextBtn.applyTheme();
    }
}

