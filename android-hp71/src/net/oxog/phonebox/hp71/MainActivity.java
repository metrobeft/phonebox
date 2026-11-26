package net.oxog.phonebox.hp71;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.components.ApprovalModalOverlay;
import net.oxog.phonebox.hp71.components.BottomNavBarView;
import net.oxog.phonebox.hp71.components.TopBarView;
import net.oxog.phonebox.hp71.core.ApiClient;
import net.oxog.phonebox.hp71.core.ApkUpdater;
import net.oxog.phonebox.hp71.core.AppPrefs;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.core.BitmapCache;
import net.oxog.phonebox.hp71.core.SyncManager;
import net.oxog.phonebox.hp71.screens.AiScreen;
import net.oxog.phonebox.hp71.screens.BaseScreen;
import net.oxog.phonebox.hp71.screens.DeckScreen;
import net.oxog.phonebox.hp71.screens.DiscordScreen;
import net.oxog.phonebox.hp71.screens.HomeScreen;
import net.oxog.phonebox.hp71.screens.NightScreen;
import net.oxog.phonebox.hp71.screens.ProcsScreen;
import net.oxog.phonebox.hp71.screens.SystemScreen;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.theme.anime.AnimeEntranceOverlayView;
import net.oxog.phonebox.hp71.theme.anime.AnimeSkyBackgroundView;
import net.oxog.phonebox.hp71.theme.anime.AnimeSparkleOverlayView;
import net.oxog.phonebox.hp71.theme.hack.HackBootOverlayView;
import net.oxog.phonebox.hp71.theme.hack.HackGlitchOverlayView;
import net.oxog.phonebox.hp71.theme.hack.HackTextGlitchManager;
import net.oxog.phonebox.hp71.theme.hack.MatrixRainBackgroundView;
import net.oxog.phonebox.hp71.widgets.DashboardWidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Shell nativo ultra-leve para HP 7.1 1201 (Android 4.2.2 / API 17).
 * Zero WebView, tela cheia real, KEEP_SCREEN_ON, brilho real de hardware,
 * gestos de swipe horizontal, troca de abas por botões físicos de volume e
 * sincronização 1:1.
 */
public final class MainActivity extends Activity implements
        SyncManager.Listener,
        AppTheme.ThemeListener,
        DashboardWidget.HostCallbacks {

    private AppPrefs prefs;
    private AppState appState;
    private ApiClient api;
    private ApkUpdater apkUpdater;
    private BitmapCache bitmaps;
    private SyncManager sync;
    private AppTheme theme;

    private FrameLayout rootFrame;
    private MatrixRainBackgroundView matrixRainBg;
    private AnimeSkyBackgroundView animeSkyBg;
    private LinearLayout mainColumn;
    private TopBarView topBar;
    private TextView offlineBanner;
    private TextView updateBanner;
    private FrameLayout screenHost;
    private BottomNavBarView navBar;
    private HackGlitchOverlayView hackGlitchOverlay;
    private AnimeSparkleOverlayView animeSparkleOverlay;
    private HackBootOverlayView hackBootOverlay;
    private AnimeEntranceOverlayView animeEntranceOverlay;
    private final HackTextGlitchManager hackTextGlitch = new HackTextGlitchManager();
    private View nightDimOverlay;
    private ApprovalModalOverlay approvalOverlay;
    private String lastThemeId = "";
    private boolean coldBootPlayed = false;
    private boolean settingsDialogOpen = false;
    private boolean pendingHackBootOnDialogClose = false;
    private boolean pendingAnimeEntranceOnDialogClose = false;
    private String promptedApkKey = "";
    private AppState.ApkReleaseInfo latestServerApk = null;

    private BaseScreen[] allScreens;
    private final List<AppState.PageTab> activeTabs = new ArrayList<AppState.PageTab>();
    private int currentTabPos = 0;
    private int prevTabPos = 0;
    private boolean navAtTop = false;
    private boolean isPortrait = false;
    private int viewportWidthPx = 0;
    private int viewportHeightPx = 0;
    private float currentBrightness = 1.0f;
    private boolean manualOrient = false;
    private boolean manualBrightness = false;
    private boolean manualVolKeys = false;
    private String lastServerOrient = "";
    private boolean autoNightDimActive = false;
    private boolean idleDimActive = false;
    private long lastTouchAt = 0L;

    private boolean resumed;
    private int deviceStatusTicks;
    private GestureDetector swipeDetector;
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockTicker = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < allScreens.length; i++) {
                if (allScreens[i] != null && allScreens[i].isActive()) {
                    allScreens[i].tickClock();
                }
            }
            if (deviceStatusTicks++ % 5 == 0)
                updateDeviceStatus();
            checkAutoNightAndIdle();
            clockHandler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        theme = AppTheme.init(this);
        prefs = new AppPrefs(this);
        appState = new AppState();
        applyPanelTheme();
        theme.addListener(this);
        api = new ApiClient(prefs);
        apkUpdater = new ApkUpdater(this, api);
        bitmaps = new BitmapCache(api);
        sync = new SyncManager(api, appState, this);

        lastTouchAt = System.currentTimeMillis();
        buildDefaultTabs();
        buildUi();
        setupSwipeDetector();

        applyOrientationMode(prefs.getOrientMode(), false);
        setHardwareBrightness(prefs.getBrightness());
        int savedPage = prefs.getLastPage();
        if (savedPage < 0 || savedPage >= activeTabs.size())
            savedPage = 0;
        selectTabPosition(savedPage, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyLowProfileSystemUi();
        resumed = true;
        syncThemeEffectsState(false, false);
        if (!coldBootPlayed && prefs.themeEffectsEnabled()) {
            if (theme.isHack() && hackBootOverlay != null) {
                coldBootPlayed = true;
                playHackBootSequence();
            } else if (theme.isAnime() && animeEntranceOverlay != null) {
                coldBootPlayed = true;
                playAnimeEntranceSequence();
            }
        }
        deviceStatusTicks = 0;
        sync.start();
        clockHandler.removeCallbacks(clockTicker);
        clockHandler.post(clockTicker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
        syncThemeEffectsState(false, false);
        if (hackBootOverlay != null)
            hackBootOverlay.stop();
        if (animeEntranceOverlay != null)
            animeEntranceOverlay.stop();
        sync.stop();
        clockHandler.removeCallbacks(clockTicker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hackTextGlitch.setRunning(false);
        if (matrixRainBg != null)
            matrixRainBg.setRunning(false);
        if (animeSkyBg != null)
            animeSkyBg.setRunning(false);
        if (hackGlitchOverlay != null)
            hackGlitchOverlay.setState(false, false);
        if (animeSparkleOverlay != null)
            animeSparkleOverlay.setState(false, false);
        if (hackBootOverlay != null)
            hackBootOverlay.stop();
        if (animeEntranceOverlay != null)
            animeEntranceOverlay.stop();
        theme.removeListener(this);
        sync.destroy();
        bitmaps.shutdown();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePortraitFlag();
    }

    private void applyLowProfileSystemUi() {
        try {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } catch (Exception ignored) {
        }
    }

    private void buildDefaultTabs() {
        activeTabs.clear();
        String[] ids = { "home", "system", "deck", "procs", "ai", "discord", "world" };
        for (int i = 0; i < 7; i++) {
            AppState.PageTab t = new AppState.PageTab();
            t.id = ids[i];
            t.title = ApiClient.defaultTabTitle(i);
            t.viewIdx = i;
            activeTabs.add(t);
        }
    }

    private void buildUi() {
        ThemeDimens d = theme.dimens();
        rootFrame = new FrameLayout(this);

        // Camada 0 (tema Hack): chuva digital Matrix full-screen atrás dos cards
        // translúcidos
        matrixRainBg = new MatrixRainBackgroundView(this);
        matrixRainBg.setVisibility(View.GONE);
        rootFrame.addView(matrixRainBg, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Camada 0b (tema Anime): céu crepuscular estrelado + lua crescente + pétalas
        // de sakura
        animeSkyBg = new AnimeSkyBackgroundView(this);
        animeSkyBg.setVisibility(View.GONE);
        rootFrame.addView(animeSkyBg, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mainColumn = new LinearLayout(this);
        mainColumn.setOrientation(LinearLayout.VERTICAL);

        topBar = new TopBarView(this, new TopBarView.Actions() {
            @Override
            public void onToggleOrient() {
                cycleOrientMode();
            }

            @Override
            public void onBrightnessClick() {
                showBrightnessDialog();
            }

            @Override
            public void onServerConfigClick() {
                showSettingsDialog();
            }
        });

        offlineBanner = new TextView(this);
        offlineBanner.setText("PC desconectado — tentando reconectar na rede local...");
        offlineBanner.setGravity(Gravity.CENTER);
        offlineBanner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        offlineBanner.setPadding(d.dp(8), d.dp(5), d.dp(8), d.dp(5));
        offlineBanner.setVisibility(View.GONE);

        updateBanner = new TextView(this);
        updateBanner.setGravity(Gravity.CENTER);
        updateBanner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        updateBanner.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        updateBanner.setPadding(d.dp(10), d.dp(6), d.dp(10), d.dp(6));
        updateBanner.setClickable(true);
        updateBanner.setVisibility(View.GONE);
        updateBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latestServerApk != null && latestServerApk.available) {
                    apkUpdater.showUpdateDialog(MainActivity.this, latestServerApk, true);
                } else {
                    triggerManualApkCheck();
                }
            }
        });

        screenHost = new FrameLayout(this);
        screenHost.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int w = right - left;
                int h = bottom - top;
                if (w > 0 && h > 0 && (w != viewportWidthPx || h != viewportHeightPx)) {
                    viewportWidthPx = w;
                    viewportHeightPx = h;
                    updatePortraitFlag();
                }
            }
        });

        DashboardWidget.WidgetContext wctx = new DashboardWidget.WidgetContext(this, api, bitmaps, sync, this);
        allScreens = new BaseScreen[] {
                new HomeScreen(wctx),
                new SystemScreen(wctx),
                new DeckScreen(wctx),
                new ProcsScreen(wctx),
                new AiScreen(wctx),
                new DiscordScreen(wctx),
                new NightScreen(wctx)
        };
        for (int i = 0; i < allScreens.length; i++) {
            allScreens[i].setActive(false);
            screenHost.addView(allScreens[i].getView(), new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }

        navBar = new BottomNavBarView(this);
        navBar.setTabs(activeTabs);
        navBar.setListener(new BottomNavBarView.OnTabSelected() {
            @Override
            public void onSelect(int tabPosition) {
                selectTabPosition(tabPosition, true);
            }
        });

        assembleMainColumn(false);
        rootFrame.addView(mainColumn, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        hackTextGlitch.attachRoot(mainColumn);

        // Camada CRT & Glitch visual full-screen (transparente ao toque, sincronizada
        // com Matrix + texto h4ck3r)
        hackGlitchOverlay = new HackGlitchOverlayView(this);
        hackGlitchOverlay.setCallbacks(new HackGlitchOverlayView.GlitchCallbacks() {
            @Override
            public void onUiHorizontalJitter(float translationXPx) {
                if (mainColumn != null)
                    mainColumn.setTranslationX(translationXPx);
            }

            @Override
            public void onGlitchBurstStarted(int intensity, int durationMs) {
                if (matrixRainBg != null)
                    matrixRainBg.triggerGlitchSurge(durationMs + 220);
                hackTextGlitch.triggerSyncWave(intensity >= 2 ? 6 : 4);
            }
        });
        rootFrame.addView(hackGlitchOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Camada de brilhos estelares + pétalas marginais + varredura ao trocar de aba
        // (tema Anime)
        animeSparkleOverlay = new AnimeSparkleOverlayView(this);
        rootFrame.addView(animeSparkleOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Sequência animada de boot/invasão Typewriter (toque para pular)
        hackBootOverlay = new HackBootOverlayView(this);
        rootFrame.addView(hackBootOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Sequência animada de entrada Sakura Midnight Bloom (toque para pular)
        animeEntranceOverlay = new AnimeEntranceOverlayView(this);
        rootFrame.addView(animeEntranceOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Overlay extra para brilho noturno ultra-baixo (2%)
        nightDimOverlay = new View(this);
        nightDimOverlay.setBackgroundColor(0xFF000000);
        nightDimOverlay.setAlpha(0f);
        nightDimOverlay.setVisibility(View.GONE);
        rootFrame.addView(nightDimOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Modal global de aprovações IA
        approvalOverlay = new ApprovalModalOverlay(this);
        approvalOverlay.setDecisionListener(new ApprovalModalOverlay.DecisionListener() {
            @Override
            public void onDecide(String approvalId, String decision) {
                sync.postAction(
                        "/api/approvals/" + ApiClient.enc(approvalId) + "?decision=" + ApiClient.enc(decision),
                        "allow".equals(decision) ? "Permitido" : "Negado",
                        "Falha ao responder aprovacao",
                        true);
            }
        });
        rootFrame.addView(approvalOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(rootFrame);
        onThemeChanged(theme);
        updatePortraitFlag();
    }

    private void assembleMainColumn(boolean controlsTop) {
        ThemeDimens d = theme.dimens();
        this.navAtTop = controlsTop;
        mainColumn.removeAllViews();
        mainColumn.addView(topBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.topBarHeight));
        if (controlsTop) {
            navBar.setPositionTop(true);
            mainColumn.addView(navBar, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, d.navBarHeight));
        }
        mainColumn.addView(offlineBanner, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mainColumn.addView(updateBanner, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mainColumn.addView(screenHost, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        if (!controlsTop) {
            navBar.setPositionTop(false);
            mainColumn.addView(navBar, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, d.navBarHeight));
        }
    }

    private void setupSwipeDetector() {
        swipeDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null)
                    return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dx) > 90f && Math.abs(dx) > Math.abs(dy) * 1.6f && Math.abs(velocityX) > 200f) {
                    if (dx < 0)
                        selectTabPosition(currentTabPos + 1, true);
                    else
                        selectTabPosition(currentTabPos - 1, true);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchAt = System.currentTimeMillis();
            if (idleDimActive) {
                idleDimActive = false;
                applyHardwareBrightness(prefs.getBrightness(), false);
            }
        }
        if (swipeDetector != null
                && approvalOverlay.getVisibility() != View.VISIBLE
                && (hackBootOverlay == null || hackBootOverlay.getVisibility() != View.VISIBLE)
                && (animeEntranceOverlay == null || animeEntranceOverlay.getVisibility() != View.VISIBLE)) {
            swipeDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            lastTouchAt = System.currentTimeMillis();
            if (prefs.isVolumeKeysSwitchTabs()) {
                selectTabPosition(keyCode == KeyEvent.KEYCODE_VOLUME_UP ? (currentTabPos - 1) : (currentTabPos + 1),
                        true);
            } else {
                sync.postAction(keyCode == KeyEvent.KEYCODE_VOLUME_UP ? "/api/volume?op=up" : "/api/volume?op=down",
                        null, null, true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void selectTabPosition(int pos, boolean savePref) {
        int count = activeTabs.size();
        if (count == 0)
            return;
        if (pos < 0)
            pos = count - 1;
        if (pos >= count)
            pos = 0;

        if (pos != currentTabPos)
            prevTabPos = currentTabPos;
        boolean tabChanged = (pos != currentTabPos);
        currentTabPos = pos;
        if (savePref)
            prefs.setLastPage(pos);

        int viewIdx = activeTabs.get(pos).viewIdx;
        synchronized (appState) {
            for (int i = 0; i < allScreens.length; i++) {
                boolean show = (i == viewIdx);
                allScreens[i].setActive(show);
                if (show) {
                    allScreens[i].scrollToTop();
                    allScreens[i].bindData(appState);
                }
            }
        }
        navBar.setSelectedPosition(pos);
        sync.setActiveViewIdx(viewIdx);
        if (theme.isHack()) {
            hackTextGlitch.refreshDynamicTypography();
            if (tabChanged && resumed && prefs.themeEffectsEnabled() && hackGlitchOverlay != null) {
                hackGlitchOverlay.triggerTabGlitch();
            }
        } else if (theme.isAnime()) {
            if (tabChanged && resumed && prefs.themeEffectsEnabled()) {
                if (animeSparkleOverlay != null)
                    animeSparkleOverlay.triggerTabSweep();
                if (animeSkyBg != null)
                    animeSkyBg.triggerSakuraBurst();
            }
        }
    }

    @Override
    public void navigateToView(int viewIdx) {
        for (int i = 0; i < activeTabs.size(); i++) {
            if (activeTabs.get(i).viewIdx == viewIdx) {
                selectTabPosition(i, true);
                return;
            }
        }
    }

    private int findTabByIdOrNumber(String key) {
        if (key == null || key.length() == 0)
            return -1;
        String low = key.toLowerCase();
        for (int i = 0; i < activeTabs.size(); i++) {
            if (low.equals(activeTabs.get(i).id.toLowerCase()))
                return i;
        }
        try {
            int n = Integer.parseInt(low);
            if (n >= 1 && n <= activeTabs.size())
                return n - 1;
        } catch (Exception ignored) {
        }
        int v = ApiClient.mapBuiltInView(low);
        if (v >= 0) {
            for (int i = 0; i < activeTabs.size(); i++) {
                if (activeTabs.get(i).viewIdx == v)
                    return i;
            }
        }
        return -1;
    }

    private void updatePortraitFlag() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int effW = viewportWidthPx > 0 ? viewportWidthPx : dm.widthPixels;
        int effH = viewportHeightPx > 0 ? viewportHeightPx
                : Math.max(200, dm.heightPixels - theme.dimens().topBarHeight - theme.dimens().navBarHeight);
        String mode = prefs.getOrientMode();
        boolean nextPort = AppPrefs.ORIENT_PORTRAIT.equals(mode)
                || (!AppPrefs.ORIENT_LANDSCAPE.equals(mode) && dm.heightPixels > dm.widthPixels);
        this.isPortrait = nextPort;
        for (int i = 0; i < allScreens.length; i++) {
            allScreens[i].onViewportChanged(effW, effH, nextPort);
        }
        topBar.bind(appState, isPortrait, mode, currentBrightness);
    }

    @Override
    public int getViewportWidthPx() {
        if (viewportWidthPx > 0)
            return viewportWidthPx;
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public int getViewportHeightPx() {
        if (viewportHeightPx > 0)
            return viewportHeightPx;
        ThemeDimens d = theme.dimens();
        return Math.max(200, getResources().getDisplayMetrics().heightPixels - d.topBarHeight - d.navBarHeight);
    }

    private void cycleOrientMode() {
        manualOrient = true;
        String cur = prefs.getOrientMode();
        String next = AppPrefs.ORIENT_AUTO.equals(cur) ? AppPrefs.ORIENT_LANDSCAPE
                : (AppPrefs.ORIENT_LANDSCAPE.equals(cur) ? AppPrefs.ORIENT_PORTRAIT : AppPrefs.ORIENT_AUTO);
        applyOrientationMode(next, true);
        showToast("Proporção: " + (AppPrefs.ORIENT_AUTO.equals(next) ? "Automática"
                : (AppPrefs.ORIENT_LANDSCAPE.equals(next) ? "Horizontal" : "Vertical")), false);
    }

    private void applyOrientationMode(String mode, boolean persist) {
        if (persist)
            prefs.setOrientMode(mode);
        if (AppPrefs.ORIENT_LANDSCAPE.equals(mode)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if (AppPrefs.ORIENT_PORTRAIT.equals(mode)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        updatePortraitFlag();
    }

    @Override
    public void setHardwareBrightness(float level) {
        manualBrightness = true;
        prefs.setBrightness(level);
        applyHardwareBrightness(level, false);
    }

    private void applyHardwareBrightness(float level, boolean isAuto) {
        float clamped = Math.max(0.02f, Math.min(1.0f, level));
        currentBrightness = clamped;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = Math.max(0.05f, clamped);
        getWindow().setAttributes(lp);

        // Para modo noturno 2%–10%, adiciona overlay preto suave sobre o backlight
        // mínimo
        if (clamped < 0.12f) {
            float alpha = clamped <= 0.03f ? 0.72f : 0.42f;
            nightDimOverlay.setAlpha(alpha);
            nightDimOverlay.setVisibility(View.VISIBLE);
        } else {
            nightDimOverlay.setAlpha(0f);
            nightDimOverlay.setVisibility(View.GONE);
        }
        if (topBar != null)
            topBar.bind(appState, isPortrait, prefs.getOrientMode(), currentBrightness);
    }

    @Override
    public float getHardwareBrightness() {
        return currentBrightness;
    }

    private void checkAutoNightAndIdle() {
        long nowMs = System.currentTimeMillis();
        // Idle return to Home page
        AppState.PageTab curTab = (currentTabPos >= 0 && currentTabPos < activeTabs.size())
                ? activeTabs.get(currentTabPos)
                : null;
        int retSec = (curTab != null && curTab.idleSec >= 0) ? curTab.idleSec : appState.display.idleReturnSec;
        if (retSec > 0 && (nowMs - lastTouchAt) >= retSec * 1000L) {
            int homePos = findTabByIdOrNumber(appState.display.homePage);
            if (homePos < 0)
                homePos = 0;
            if (currentTabPos != homePos) {
                lastTouchAt = nowMs;
                selectTabPosition(homePos, false);
            }
        }

        // Idle dim
        if (appState.display.idleDimMin > 0 && !manualBrightness && !idleDimActive
                && (nowMs - lastTouchAt) >= appState.display.idleDimMin * 60000L) {
            idleDimActive = true;
            applyHardwareBrightness(0.08f, true);
        }

        // Night mode schedule
        if (!manualBrightness && appState.night.enabled) {
            Calendar cal = Calendar.getInstance();
            int curMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
            int sMin = parseHmMinutes(appState.night.start, 30);
            int eMin = parseHmMinutes(appState.night.end, 450);
            boolean inNight = sMin <= eMin ? (curMin >= sMin && curMin < eMin) : (curMin >= sMin || curMin < eMin);
            if (inNight && !autoNightDimActive) {
                autoNightDimActive = true;
                applyHardwareBrightness(Math.max(0.02f, appState.night.brightness), true);
            } else if (!inNight && autoNightDimActive) {
                autoNightDimActive = false;
                applyHardwareBrightness(1.0f, true);
            }
        }
    }

    private static int parseHmMinutes(String hhmm, int def) {
        if (hhmm == null)
            return def;
        String[] p = hhmm.split(":");
        if (p.length < 2)
            return def;
        try {
            return Integer.parseInt(p[0].trim()) * 60 + Integer.parseInt(p[1].trim());
        } catch (Exception e) {
            return def;
        }
    }

    // ==================== DIALOGOS NATIVOS (IP / BRILHO) ====================

    private void updateDeviceStatus() {
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = battery == null ? -1 : battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery == null ? -1 : battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        boolean charging = battery != null && battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm == null ? null : cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        topBar.setDeviceStatus(level >= 0 && scale > 0 ? Math.round(level * 100f / scale) : -1,
                charging, wifi != null && wifi.isConnected());
    }

    private void applyPanelTheme() {
        String local = ThemeColors.normalizePresetId(prefs.getLocalTheme());
        boolean custom = ThemeColors.isCustomLocalTheme(local);
        theme.update(custom ? local : appState.display.theme, appState.display.accent,
                appState.display.cardRadiusDp, appState.display.borderlessCards);
    }

    private void showSettingsDialog() {
        final String[] ids = { "sakura", "lofi", "starfall", "miku", "fantasy", "hack", "default" };
        final String[] labels = { "Sakura Storm", "Lofi Room", "Starfall Aurora", "Hatsune Miku 01", "Arcane Fantasy", "Hack Cyberdeck", "Default Navy" };
        final int[] banners = {
                R.drawable.theme_anime, R.drawable.theme_lofi, R.drawable.theme_starfall,
                R.drawable.theme_miku, R.drawable.theme_fantasy, R.drawable.theme_hack, 0
        };
        final int[] accents = { 0xFFFF7ED4, 0xFFFFB38A, 0xFF7FE7FF, 0xFF39C5BB, 0xFFF4C858, 0xFF00FF66, 0xFF8BD0FF };

        final ThemeDimens d = theme.dimens();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(d.dp(12), d.dp(8), d.dp(12), d.dp(6));

        TextView galleryTitle = new TextView(this);
        galleryTitle.setText("Theme Studio — Mundos Animados");
        galleryTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        galleryTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        galleryTitle.setPadding(d.dp(2), 0, d.dp(2), d.dp(6));
        box.addView(galleryTitle);

        final LinearLayout[] tileCards = new LinearLayout[ids.length];
        final TextView[] tileTitles = new TextView[ids.length];
        final String[] selectedId = new String[] { ThemeColors.normalizePresetId(prefs.getLocalTheme()) };
        if (!ThemeColors.isCustomLocalTheme(selectedId[0])) selectedId[0] = "default";

        final Runnable refreshSelection = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < ids.length; i++) {
                    boolean active = ids[i].equals(selectedId[0]);
                    tileCards[i].setBackground(DrawableFactory.roundedRect(
                            active ? 0xFF1F2B47 : 0xFF12192A,
                            active ? accents[i] : 0xFF31415E,
                            d.dp(active ? 2 : 1), d.dp(8)));
                    tileTitles[i].setText((active ? "* " : "") + labels[i]);
                    tileTitles[i].setTextColor(active ? accents[i] : 0xFFE6EFFC);
                }
            }
        };

        // 6 mundos animados em grade simétrica 2x3
        for (int row = 0; row < 6; row += 2) {
            LinearLayout rowBox = new LinearLayout(this);
            rowBox.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = d.dp(7);
            for (int col = 0; col < 2; col++) {
                final int idx = row + col;
                LinearLayout tile = new LinearLayout(this);
                tile.setOrientation(LinearLayout.VERTICAL);
                tile.setPadding(d.dp(5), d.dp(5), d.dp(5), d.dp(6));
                tile.setClickable(true);

                ImageView img = new ImageView(this);
                img.setImageResource(banners[idx]);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                tile.addView(img, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, d.dp(48)));

                TextView lbl = new TextView(this);
                lbl.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                lbl.setSingleLine(true);
                lbl.setEllipsize(android.text.TextUtils.TruncateAt.END);
                lbl.setGravity(Gravity.CENTER_HORIZONTAL);
                lbl.setPadding(d.dp(2), d.dp(4), d.dp(2), 0);
                tile.addView(lbl, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                tileCards[idx] = tile;
                tileTitles[idx] = lbl;
                tile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedId[0] = ids[idx];
                        prefs.setLocalTheme(ids[idx]);
                        applyPanelTheme();
                        refreshSelection.run();
                    }
                });

                LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                if (col == 0) tLp.rightMargin = d.dp(7);
                rowBox.addView(tile, tLp);
            }
            box.addView(rowBox, rowLp);
        }

        // Default Navy em barra limpa de largura total abaixo dos 6 mundos animados
        final int defIdx = 6;
        LinearLayout defTile = new LinearLayout(this);
        defTile.setOrientation(LinearLayout.HORIZONTAL);
        defTile.setGravity(Gravity.CENTER_VERTICAL);
        defTile.setPadding(d.dp(10), d.dp(7), d.dp(10), d.dp(7));
        defTile.setClickable(true);
        View navySwatch = new View(this);
        navySwatch.setBackground(DrawableFactory.roundedRect(0xFF152641, 0xFF8BD0FF, d.dp(1), d.dp(4)));
        LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(d.dp(34), d.dp(18));
        swLp.rightMargin = d.dp(10);
        defTile.addView(navySwatch, swLp);
        TextView defLbl = new TextView(this);
        defLbl.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        defLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        defTile.addView(defLbl, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tileCards[defIdx] = defTile;
        tileTitles[defIdx] = defLbl;
        defTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedId[0] = ids[defIdx];
                prefs.setLocalTheme(ids[defIdx]);
                applyPanelTheme();
                refreshSelection.run();
            }
        });
        LinearLayout.LayoutParams defRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        defRowLp.bottomMargin = d.dp(6);
        box.addView(defTile, defRowLp);
        refreshSelection.run();

        CheckBox effects = new CheckBox(this);
        effects.setText("Animar mundos (Matrix, Sakura, Lofi, Aurora, Miku, Fantasia)");
        effects.setChecked(prefs.themeEffectsEnabled());
        effects.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton button, boolean checked) {
                prefs.setThemeEffectsEnabled(checked);
                syncThemeEffectsState(false, false);
            }
        });
        box.addView(effects);

        CheckBox autoUpd = new CheckBox(this);
        autoUpd.setText("Verificar atualizações do APK automaticamente");
        autoUpd.setChecked(prefs.isAutoCheckUpdates());
        autoUpd.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton button, boolean checked) {
                prefs.setAutoCheckUpdates(checked);
                updateUpdateBannerVisibility();
            }
        });
        box.addView(autoUpd);

        boolean hasNewerApk = latestServerApk != null && apkUpdater.isUpdateAvailable(latestServerApk);
        TextView verInfo = new TextView(this);
        verInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        verInfo.setPadding(d.dp(4), d.dp(6), d.dp(4), d.dp(6));
        String verLine = "App instalado: " + apkUpdater.formatLocalVersion();
        if (hasNewerApk) {
            verLine += "\nNova versão no PC: " + ApkUpdater.formatRemoteVersion(latestServerApk);
        } else if (latestServerApk != null && latestServerApk.available) {
            verLine += " · Servidor atualizado";
        }
        verInfo.setText(verLine);
        box.addView(verInfo);

        final ActionButtonView checkUpdBtn = new ActionButtonView(this);
        checkUpdBtn.setButton(
                hasNewerApk ? ("Baixar atualização " + ApkUpdater.formatRemoteVersion(latestServerApk))
                        : "Verificar / Atualizar APK no servidor",
                hasNewerApk ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_ACTIVE);
        LinearLayout.LayoutParams updBtnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.dp(40));
        updBtnLp.bottomMargin = d.dp(6);
        box.addView(checkUpdBtn, updBtnLp);

        android.widget.ScrollView scrollBox = new android.widget.ScrollView(this);
        scrollBox.addView(box);

        settingsDialogOpen = true;
        pendingHackBootOnDialogClose = false;
        pendingAnimeEntranceOnDialogClose = false;
        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Configurações").setView(scrollBox)
                .setNeutralButton("Conexão do PC", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d1, int which) {
                        pendingHackBootOnDialogClose = false;
                        pendingAnimeEntranceOnDialogClose = false;
                        showServerDialog();
                    }
                })
                .setNegativeButton("Testar Animação", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d1, int which) {
                        prefs.setThemeEffectsEnabled(true);
                        String cur = ThemeColors.normalizePresetId(prefs.getLocalTheme());
                        if ("hack".equals(cur) || theme.isHack()) {
                            prefs.setLocalTheme("hack");
                            pendingHackBootOnDialogClose = true;
                            pendingAnimeEntranceOnDialogClose = false;
                        } else {
                            String targetWorld = ThemeColors.isLivingWorldId(cur) ? cur : "sakura";
                            prefs.setLocalTheme(targetWorld);
                            pendingAnimeEntranceOnDialogClose = true;
                            pendingHackBootOnDialogClose = false;
                        }
                        applyPanelTheme();
                    }
                })
                .setPositiveButton("Fechar", null)
                .create();
        checkUpdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingHackBootOnDialogClose = false;
                pendingAnimeEntranceOnDialogClose = false;
                dialog.dismiss();
                triggerManualApkCheck();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d1) {
                settingsDialogOpen = false;
                if (pendingHackBootOnDialogClose && theme.isHack() && resumed && prefs.themeEffectsEnabled()) {
                    pendingHackBootOnDialogClose = false;
                    playHackBootSequence();
                } else if (pendingAnimeEntranceOnDialogClose && theme.isAnime() && resumed
                        && prefs.themeEffectsEnabled()) {
                    pendingAnimeEntranceOnDialogClose = false;
                    playAnimeEntranceSequence();
                }
            }
        });
        dialog.show();
    }

    private void triggerManualApkCheck() {
        showToast("Verificando versão do APK no PC...", false);
        apkUpdater.checkNow(new ApkUpdater.CheckCallback() {
            @Override
            public void onResult(AppState.ApkReleaseInfo remote, boolean updateAvailable, String error) {
                if (isFinishing())
                    return;
                if (error != null || remote == null || !remote.available) {
                    showToast("Não foi possível verificar atualização"
                            + (error != null && error.length() > 0 ? (": " + error) : ""), true);
                    return;
                }
                latestServerApk = remote;
                updateUpdateBannerVisibility();
                apkUpdater.showUpdateDialog(MainActivity.this, remote, true);
            }
        });
    }

    private void handleServerApkCheck(AppState.ApkReleaseInfo remote, boolean online) {
        if (!online || remote == null || !remote.available) {
            if (!online && updateBanner != null)
                updateBanner.setVisibility(View.GONE);
            return;
        }
        latestServerApk = remote;
        updateUpdateBannerVisibility();

        if (!prefs.isAutoCheckUpdates() || !apkUpdater.isUpdateAvailable(remote))
            return;
        String key = remote.releaseKey();
        if (key.equals(promptedApkKey))
            return;
        if (!resumed || settingsDialogOpen || isFinishing())
            return;
        if (approvalOverlay != null && approvalOverlay.getVisibility() == View.VISIBLE)
            return;
        if (hackBootOverlay != null && hackBootOverlay.getVisibility() == View.VISIBLE)
            return;
        if (animeEntranceOverlay != null && animeEntranceOverlay.getVisibility() == View.VISIBLE)
            return;

        promptedApkKey = key;
        apkUpdater.showUpdateDialog(this, remote, false);
    }

    private void updateUpdateBannerVisibility() {
        if (updateBanner == null)
            return;
        boolean show = latestServerApk != null
                && appState.online
                && prefs.isAutoCheckUpdates()
                && apkUpdater.isUpdateAvailable(latestServerApk);
        if (show) {
            updateBanner.setText("Nova versão disponível: " + ApkUpdater.formatRemoteVersion(latestServerApk)
                    + " — Toque para baixar e instalar");
            updateBanner.setVisibility(View.VISIBLE);
        } else {
            updateBanner.setVisibility(View.GONE);
        }
    }

    private void showServerDialog() {
        ThemeDimens d = theme.dimens();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(d.dp(18), d.dp(12), d.dp(18), d.dp(8));

        TextView lblHost = new TextView(this);
        lblHost.setText("IP do PC na Rede Local (ex.: 192.168.1.100):");
        box.addView(lblHost);

        final EditText hostIn = new EditText(this);
        hostIn.setSingleLine(true);
        hostIn.setText(prefs.getHost());
        box.addView(hostIn);

        TextView lblPort = new TextView(this);
        lblPort.setText("Porta HTTP Local (padrao 8788):");
        box.addView(lblPort);

        final EditText portIn = new EditText(this);
        portIn.setSingleLine(true);
        portIn.setInputType(InputType.TYPE_CLASS_NUMBER);
        portIn.setText(String.valueOf(prefs.getPort()));
        box.addView(portIn);

        final CheckBox volTabs = new CheckBox(this);
        volTabs.setText("Botoes de volume do tablet trocam de aba");
        volTabs.setChecked(prefs.isVolumeKeysSwitchTabs());
        box.addView(volTabs);

        new AlertDialog.Builder(this)
                .setTitle("Conexao PhoneBox (LAN)")
                .setView(box)
                .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int port = AppPrefs.DEFAULT_PORT;
                        try {
                            port = Integer.parseInt(portIn.getText().toString().trim());
                        } catch (Exception ignored) {
                        }
                        prefs.setServer(hostIn.getText().toString(), port);
                        manualVolKeys = true;
                        prefs.setVolumeKeysSwitchTabs(volTabs.isChecked());
                        bitmaps.clear();
                        sync.resetConfigVersion();
                        sync.requestImmediateSync(false);
                        showToast("Conectando em " + prefs.getBaseUrl(), false);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showBrightnessDialog() {
        final String[] labels = { "100% (Maximo)", "60% (Dia)", "30% (Ambiente)", "10% (Noturno)", "2% (Madrugada)" };
        final float[] vals = { 1.0f, 0.60f, 0.30f, 0.10f, 0.02f };
        new AlertDialog.Builder(this)
                .setTitle("Brilho da Tela (HP 7.1)")
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setHardwareBrightness(vals[which]);
                    }
                })
                .show();
    }

    // ==================== CALLBACKS DE SYNC E TEMA ====================

    @Override
    public void onStateUpdated(AppState s, List<AppState.LiveEvent> events) {
        // 1. Aplica tema/accent vindos de http://127.0.0.1:8765/
        applyPanelTheme();

        // 1b. Sincroniza orientação e teclas de volume do servidor (se não sobrescritos
        // manualmente)
        if (!manualOrient && s.display.orientation != null && s.display.orientation.length() > 0
                && !s.display.orientation.equals(lastServerOrient)) {
            lastServerOrient = s.display.orientation;
            if (!s.display.orientation.equals(prefs.getOrientMode())) {
                applyOrientationMode(s.display.orientation, false);
            }
        }
        if (!manualVolKeys && s.display.hasVolumeKeys && s.display.volumeKeys != prefs.isVolumeKeysSwitchTabs()) {
            prefs.setVolumeKeysSwitchTabs(s.display.volumeKeys);
        }

        // 2. Posição da barra de navegação (topo ou base)
        if (s.display.controlsTop != navAtTop) {
            assembleMainColumn(s.display.controlsTop);
        }

        // 3. Sincroniza ordem/títulos das abas habilitadas
        if (!s.pages.isEmpty()) {
            String curId = (currentTabPos >= 0 && currentTabPos < activeTabs.size()) ? activeTabs.get(currentTabPos).id
                    : "home";
            activeTabs.clear();
            activeTabs.addAll(s.pages);
            navBar.setTabs(activeTabs);
            int nextPos = findTabByIdOrNumber(curId);
            if (nextPos < 0 || nextPos >= activeTabs.size())
                nextPos = 0;
            if (nextPos != currentTabPos)
                selectTabPosition(nextPos, false);
        }

        // 4. Banner offline + verificação de atualização do APK + barra superior +
        // modal de aprovações
        offlineBanner.setVisibility(s.online ? View.GONE : View.VISIBLE);
        handleServerApkCheck(s.apk, s.online);
        topBar.bind(s, isPortrait, prefs.getOrientMode(), currentBrightness);
        approvalOverlay.bind(s);

        // 5. Atualiza somente a tela ativa (máxima economia de CPU/RAM no Cortex-A7)
        if (currentTabPos >= 0 && currentTabPos < activeTabs.size()) {
            int viewIdx = activeTabs.get(currentTabPos).viewIdx;
            if (viewIdx >= 0 && viewIdx < allScreens.length) {
                allScreens[viewIdx].bindData(s);
            }
        }
        if (theme.isHack()) {
            hackTextGlitch.refreshDynamicTypography();
        }

        // 6. Processa eventos ao vivo (goto, back, notify, refresh, reload)
        if (events != null) {
            for (int i = 0; i < events.size(); i++) {
                handleLiveEvent(events.get(i));
            }
        }
    }

    private void handleLiveEvent(AppState.LiveEvent ev) {
        if (ev == null)
            return;
        if ("goto".equals(ev.kind)) {
            int pos = findTabByIdOrNumber(ev.page);
            if (pos >= 0) {
                lastTouchAt = System.currentTimeMillis();
                selectTabPosition(pos, true);
                showToast("Aba aberta: " + activeTabs.get(pos).title, false);
            }
        } else if ("back".equals(ev.kind)) {
            selectTabPosition(prevTabPos, true);
        } else if ("notify".equals(ev.kind)) {
            String msg = (ev.title.length() > 0 ? (ev.title + " — ") : "") + ev.text;
            if (msg.length() > 0)
                showToast(msg, "error".equals(ev.level));
            if (ev.page.length() > 0) {
                int pos = findTabByIdOrNumber(ev.page);
                if (pos >= 0)
                    selectTabPosition(pos, true);
            }
        } else if ("refresh".equals(ev.kind) || "reload".equals(ev.kind)) {
            bitmaps.clear();
            sync.resetConfigVersion();
            sync.requestImmediateSync(currentTabPos < activeTabs.size() && activeTabs.get(currentTabPos).viewIdx == 4);
            showToast("Painel atualizado", false);
        }
    }

    @Override
    public void onActionFeedback(String message, boolean error) {
        showToast(message, error);
    }

    @Override
    public void showToast(String msg, boolean error) {
        if (msg == null || msg.length() == 0)
            return;
        Toast.makeText(this, msg, error ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    private void playHackBootSequence() {
        if (hackBootOverlay == null || !theme.isHack())
            return;
        if (animeEntranceOverlay != null)
            animeEntranceOverlay.stop();
        hackBootOverlay.play(new Runnable() {
            @Override
            public void run() {
                if (hackGlitchOverlay != null && theme.isHack() && resumed && prefs.themeEffectsEnabled()) {
                    hackGlitchOverlay.triggerManualGlitchBurst();
                }
            }
        });
    }

    private void playAnimeEntranceSequence() {
        if (animeEntranceOverlay == null || !theme.isAnime())
            return;
        if (hackBootOverlay != null)
            hackBootOverlay.stop();
        animeEntranceOverlay.play(new Runnable() {
            @Override
            public void run() {
                if (theme.isAnime() && resumed && prefs.themeEffectsEnabled()) {
                    if (animeSkyBg != null)
                        animeSkyBg.triggerSakuraBurst();
                    if (animeSparkleOverlay != null)
                        animeSparkleOverlay.triggerTabSweep();
                }
            }
        });
    }

    private void syncThemeEffectsState(boolean enteredHackNow, boolean enteredAnimeNow) {
        boolean isHack = theme.isHack();
        boolean isAnime = theme.isAnime();
        boolean animateHack = isHack && resumed && prefs.themeEffectsEnabled();
        boolean animateAnime = isAnime && resumed && prefs.themeEffectsEnabled();

        if (topBar != null)
            topBar.setThemeEffects(animateHack || animateAnime);

        if (matrixRainBg != null) {
            matrixRainBg.setVisibility(isHack ? View.VISIBLE : View.GONE);
            matrixRainBg.setRunning(animateHack);
        }
        if (hackGlitchOverlay != null) {
            hackGlitchOverlay.setState(isHack, animateHack);
        }
        if (!isHack && mainColumn != null) {
            mainColumn.setTranslationX(0f);
        }
        if (!isHack && hackBootOverlay != null) {
            hackBootOverlay.stop();
        }
        hackTextGlitch.setRunning(animateHack);

        if (animeSkyBg != null) {
            if (isAnime) animeSkyBg.setWorld(theme.worldId());
            animeSkyBg.setVisibility(isAnime ? View.VISIBLE : View.GONE);
            animeSkyBg.setRunning(animateAnime);
        }
        if (animeSparkleOverlay != null) {
            if (isAnime) animeSparkleOverlay.setWorld(theme.worldId());
            animeSparkleOverlay.setState(isAnime, animateAnime);
        }
        if (animeEntranceOverlay != null) {
            if (isAnime) animeEntranceOverlay.setWorld(theme.worldId());
            else animeEntranceOverlay.stop();
        }

        if (enteredHackNow && animateHack) {
            coldBootPlayed = true;
            if (settingsDialogOpen) {
                pendingHackBootOnDialogClose = true;
                pendingAnimeEntranceOnDialogClose = false;
            } else {
                playHackBootSequence();
            }
        } else if (enteredAnimeNow && animateAnime) {
            coldBootPlayed = true;
            if (settingsDialogOpen) {
                pendingAnimeEntranceOnDialogClose = true;
                pendingHackBootOnDialogClose = false;
            } else {
                playAnimeEntranceSequence();
            }
        }
    }

    @Override
    public void onThemeChanged(AppTheme t) {
        ThemeColors c = t.colors();
        boolean isHack = t.isHack();
        boolean isAnime = t.isAnime();
        boolean changedTheme = !c.id.equals(lastThemeId);
        boolean enteredHack = isHack && changedTheme;
        boolean enteredAnime = isAnime && changedTheme;
        lastThemeId = c.id;

        rootFrame.setBackgroundColor(c.bg);
        mainColumn.setBackgroundColor((isHack || isAnime) ? 0x00000000 : c.bg);
        offlineBanner.setBackgroundColor(c.redDim);
        offlineBanner.setTextColor(isHack ? 0xFFFF6B8A : (isAnime ? c.textPrimary : 0xFFFEF2F2));
        updateBanner.setBackgroundColor(isHack ? 0xE6052B15 : (isAnime ? c.accentDim : c.accentDim));
        updateBanner.setTextColor(isHack ? c.accent : c.textPrimary);
        topBar.applyTheme();
        navBar.applyTheme();
        approvalOverlay.applyTheme();
        for (int i = 0; i < allScreens.length; i++) {
            allScreens[i].applyTheme();
        }
        // Aplica ou restaura tipografia Monospace + engine de texto glitch h4ck3r em
        // toda a árvore
        hackTextGlitch.syncTreeStyle(isHack, resumed && prefs.themeEffectsEnabled());
        syncThemeEffectsState(enteredHack, enteredAnime);
    }
}
