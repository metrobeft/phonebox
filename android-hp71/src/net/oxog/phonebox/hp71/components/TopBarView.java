package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.R;
import net.oxog.phonebox.hp71.core.AppPrefs;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.theme.anime.AnimeHeaderRibbonView;
import net.oxog.phonebox.hp71.theme.hack.HackHeaderTerminalView;

/**
 * Barra superior modular: ponto de status online, hostname + mini resumo CPU/GPU/RAM,
 * terminal typewriter no tema Hack, faixa estelar no tema Anime, badge de privacidade e botões AUTO/HORIZ/VERT, LUZ e Config.
 */
public class TopBarView extends LinearLayout {

    public interface Actions {
        void onToggleOrient();
        void onBrightnessClick();
        void onServerConfigClick();
    }

    private final View statusDot;
    private final TextView hostView;
    private final TextView miniStatsView;
    private final StatusPillView privacyPill;
    private final ActionButtonView orientBtn;
    private final ActionButtonView lightBtn;
    private final ActionButtonView ipBtn;
    private final TextView deviceStatus;
    private final ImageView themeArt;
    private final HackHeaderTerminalView hackTerminal;
    private final AnimeHeaderRibbonView animeRibbon;
    private final Paint hackBottomRule = new Paint();
    private final Paint hackCyanTick = new Paint();
    private final Paint animeBottomRule = new Paint();
    private final Paint animeGoldTick = new Paint();

    private boolean online = false;
    private boolean lastPortrait = false;

    public TopBarView(Context context, final Actions actions) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(d.dp(10), d.dp(4), d.dp(10), d.dp(4));
        setWillNotDraw(false);

        hackBottomRule.setStyle(Paint.Style.FILL);
        hackBottomRule.setColor(0xFF15693B);
        hackCyanTick.setStyle(Paint.Style.FILL);
        hackCyanTick.setColor(0xFF00FF66);
        animeBottomRule.setStyle(Paint.Style.FILL);
        animeBottomRule.setColor(0x88FF7ED4);
        animeGoldTick.setStyle(Paint.Style.FILL);
        animeGoldTick.setColor(0xCCFFD56B);

        statusDot = new View(context);
        int dotSize = d.dp(9);
        LayoutParams dotLp = new LayoutParams(dotSize, dotSize);
        dotLp.rightMargin = d.dp(7);
        addView(statusDot, dotLp);

        hostView = new TextView(context);
        hostView.setTypeface(Typeface.DEFAULT_BOLD);
        hostView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        hostView.setSingleLine(true);
        hostView.setEllipsize(TextUtils.TruncateAt.END);
        hostView.setText("PhoneBox");
        addView(hostView);

        miniStatsView = new TextView(context);
        miniStatsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        miniStatsView.setSingleLine(true);
        miniStatsView.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams miniLp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        miniLp.leftMargin = d.dp(10);
        miniLp.rightMargin = d.dp(6);
        addView(miniStatsView, miniLp);

        themeArt = new ImageView(context);
        themeArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams artLp = new LayoutParams(d.dp(74), d.dp(26));
        artLp.rightMargin = d.dp(5);
        addView(themeArt, artLp);

        hackTerminal = new HackHeaderTerminalView(context);
        LayoutParams termLp = new LayoutParams(d.dp(138), d.dp(26));
        termLp.rightMargin = d.dp(6);
        addView(hackTerminal, termLp);

        animeRibbon = new AnimeHeaderRibbonView(context);
        LayoutParams ribLp = new LayoutParams(d.dp(138), d.dp(26));
        ribLp.rightMargin = d.dp(6);
        addView(animeRibbon, ribLp);

        privacyPill = new StatusPillView(context);
        privacyPill.setVisibility(GONE);
        privacyPill.setMaxWidth(d.dp(170));
        LayoutParams privLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        privLp.rightMargin = d.dp(5);
        addView(privacyPill, privLp);

        orientBtn = new ActionButtonView(context).setCompact();
        orientBtn.setButton("AUTO", ActionButtonView.VARIANT_NORMAL);
        orientBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) { if (actions != null) actions.onToggleOrient(); }
        });
        LayoutParams bLp1 = new LayoutParams(LayoutParams.WRAP_CONTENT, d.dp(28));
        bLp1.rightMargin = d.dp(5);
        addView(orientBtn, bLp1);

        lightBtn = new ActionButtonView(context).setCompact();
        lightBtn.setButton("LUZ", ActionButtonView.VARIANT_NORMAL);
        lightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) { if (actions != null) actions.onBrightnessClick(); }
        });
        LayoutParams bLp2 = new LayoutParams(LayoutParams.WRAP_CONTENT, d.dp(28));
        bLp2.rightMargin = d.dp(5);
        addView(lightBtn, bLp2);

        ipBtn = new ActionButtonView(context).setCompact();
        ipBtn.setButton("Config.", ActionButtonView.VARIANT_NORMAL);
        ipBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) { if (actions != null) actions.onServerConfigClick(); }
        });
        addView(ipBtn, new LayoutParams(LayoutParams.WRAP_CONTENT, d.dp(28)));

        deviceStatus = new TextView(context);
        deviceStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        deviceStatus.setSingleLine();
        deviceStatus.setPadding(d.dp(7), 0, 0, 0);
        addView(deviceStatus, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        applyTheme();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        AppTheme t = AppTheme.get();
        if (t.isHack()) {
            int w = getWidth();
            int h = getHeight();
            float d = t.dimens().density;
            float lineH = Math.max(1f, d);
            canvas.drawRect(0f, h - lineH, w, h, hackBottomRule);
            canvas.drawRect(0f, h - lineH * 1.8f, 64f * d, h, hackCyanTick);
            canvas.drawRect(w - 36f * d, h - lineH * 1.8f, w, h, hackCyanTick);
        } else if (t.isAnime()) {
            int w = getWidth();
            int h = getHeight();
            float d = t.dimens().density;
            float lineH = Math.max(1f, d);
            canvas.drawRect(0f, h - lineH, w, h, animeBottomRule);
            canvas.drawRect(0f, h - lineH * 1.7f, 52f * d, h, animeGoldTick);
            canvas.drawRect(w - 36f * d, h - lineH * 1.7f, w, h, animeGoldTick);
        }
    }

    public void setDeviceStatus(int batteryPercent, boolean charging, boolean wifi) {
        String text = (batteryPercent >= 0 ? batteryPercent + "%" : "–%")
                + (charging ? " +" : "") + (wifi ? " · Wi-Fi" : " · Sem Wi-Fi");
        if (!text.contentEquals(deviceStatus.getText())) {
            deviceStatus.setText(text);
            deviceStatus.setContentDescription("Bateria " + (batteryPercent >= 0 ? batteryPercent + "%" : "indisponível")
                    + (charging ? ", carregando" : "") + (wifi ? ", Wi-Fi conectado" : ", Wi-Fi desconectado"));
        }
    }

    public void setThemeEffects(boolean enabled) {
        AppTheme t = AppTheme.get();
        hackTerminal.setRunning(enabled && t.isHack());
        animeRibbon.setRunning(enabled && t.isAnime());
    }

    public void bind(AppState s, boolean portrait, String orientMode, float brightness) {
        AppTheme t = AppTheme.get();
        ThemeDimens d = t.dimens();
        this.online = s.online;
        this.lastPortrait = portrait;
        hostView.setText(s.hostName);
        hostView.setMaxWidth(portrait ? d.dp(92) : d.dp(180));
        privacyPill.setMaxWidth(d.dp(portrait ? 54 : 132));
        miniStatsView.setVisibility(portrait ? GONE : VISIBLE);

        // Em retrato (600px), compacta terminal/ribbon e oculta o banner no topo para evitar aperto
        if (t.isHack()) {
            themeArt.setVisibility(portrait ? GONE : VISIBLE);
            LayoutParams tlp = (LayoutParams) hackTerminal.getLayoutParams();
            int targetW = d.dp(portrait ? 104 : 146);
            if (tlp != null && tlp.width != targetW) {
                tlp.width = targetW;
                hackTerminal.setLayoutParams(tlp);
            }
        } else if (t.isAnime()) {
            themeArt.setVisibility(portrait ? GONE : VISIBLE);
            LayoutParams rlp = (LayoutParams) animeRibbon.getLayoutParams();
            int targetW = d.dp(portrait ? 104 : 146);
            if (rlp != null && rlp.width != targetW) {
                rlp.width = targetW;
                animeRibbon.setLayoutParams(rlp);
            }
        }

        if (s.lastSyncAt > 0L) {
            int cpu = Math.round(s.pc.cpuPct);
            int ram = s.pc.ramPct();
            int ping = s.ping.hasMs ? Math.round(s.ping.ms) : -1;
            String gpuPart = s.pc.hasGpu
                    ? (" · GPU " + Math.round(s.pc.gpuUtil) + "%" + (s.pc.hasGpuTemp ? (" " + Math.round(s.pc.gpuTemp) + "°C") : ""))
                    : "";
            miniStatsView.setText("CPU " + cpu + "%" + gpuPart + " · RAM " + ram + "%");
            hackTerminal.updateLiveTelemetry(cpu, ping, s.online);
            animeRibbon.updateLiveTelemetry(cpu, ping, s.online);
        } else {
            miniStatsView.setText("Conectando...");
            hackTerminal.updateLiveTelemetry(0, -1, false);
            animeRibbon.updateLiveTelemetry(0, -1, false);
        }

        StringBuilder priv = new StringBuilder();
        if (s.privacy.mic) {
            priv.append("MIC");
            if (s.privacy.micApps.length() > 0) priv.append(": ").append(s.privacy.micApps);
        }
        if (s.privacy.camera) {
            if (priv.length() > 0) priv.append(" · ");
            priv.append("CAM");
            if (s.privacy.cameraApps.length() > 0) priv.append(": ").append(s.privacy.cameraApps);
        }
        if (priv.length() > 0) {
            privacyPill.setPill(priv.toString(), StatusPillView.STYLE_DANGER);
            privacyPill.setVisibility(VISIBLE);
        } else {
            privacyPill.setVisibility(GONE);
        }

        String oLabel = AppPrefs.ORIENT_LANDSCAPE.equals(orientMode) ? "HORIZ"
                : (AppPrefs.ORIENT_PORTRAIT.equals(orientMode) ? "VERT" : "AUTO");
        orientBtn.setButton(oLabel, AppPrefs.ORIENT_AUTO.equals(orientMode)
                ? ActionButtonView.VARIANT_NORMAL : ActionButtonView.VARIANT_ACTIVE);

        int pctLight = Math.round(brightness * 100f);
        lightBtn.setButton(pctLight < 95 ? (pctLight + "%") : "LUZ",
                pctLight < 95 ? ActionButtonView.VARIANT_ACTIVE : ActionButtonView.VARIANT_NORMAL);

        updateDotColor();
    }

    private void updateDotColor() {
        ThemeColors c = AppTheme.get().colors();
        if (c.isHack) {
            statusDot.setBackground(DrawableFactory.roundedRect(online ? c.green : c.red, c.glitchCyan, 1, 1f));
        } else if (c.isAnime) {
            statusDot.setBackground(DrawableFactory.circle(online ? c.green : c.red, c.starGold, 1));
        } else {
            statusDot.setBackground(DrawableFactory.circle(online ? c.green : c.red, 0, 0));
        }
    }

    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        setBackgroundColor(c.topBar);
        animeBottomRule.setColor(ThemeColors.withAlpha(c.accent, 0x88));
        animeGoldTick.setColor(ThemeColors.withAlpha(c.starGold, 0xCC));
        hostView.setTextColor(c.isHack ? c.accent : c.textPrimary);
        miniStatsView.setTextColor(c.textMuted);
        deviceStatus.setTextColor((c.isHack || c.isAnime) ? c.textSecondary : c.textPrimary);
        int art = 0;
        if (c.isSakura) art = R.drawable.theme_anime;
        else if (c.isLofi) art = R.drawable.theme_lofi;
        else if (c.isStarfall) art = R.drawable.theme_starfall;
        else if (c.isMiku) art = R.drawable.theme_miku;
        else if (c.isHack) art = R.drawable.theme_hack;
        themeArt.setImageResource(art);
        boolean hideArtPort = lastPortrait && (c.isHack || c.isAnime);
        themeArt.setVisibility((art == 0 || hideArtPort) ? GONE : VISIBLE);
        hackTerminal.setVisibility(c.isHack ? VISIBLE : GONE);
        animeRibbon.setWorld(c.id);
        animeRibbon.setVisibility(c.isAnime ? VISIBLE : GONE);
        privacyPill.applyTheme();
        orientBtn.applyTheme();
        lightBtn.applyTheme();
        ipBtn.applyTheme();
        updateDotColor();
        invalidate();
    }
}
