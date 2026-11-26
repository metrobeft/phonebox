package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.MetricProgressBarView;
import net.oxog.phonebox.hp71.components.NativeLineChartView;
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.components.WeatherGlyphView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.layouts.ResponsiveGridLayout;
import net.oxog.phonebox.hp71.layouts.SplitColumnLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Widget modular completo de Clima, Hora & Relógio de Mesa (3 sub-abas limpas):
 * 0) Clima & Previsão (Hero + 4 Gauges + Curva 12h + Previsão 7 dias com barras)
 * 1) Hora, Sol & Fusos (Relógio por extenso, progresso dia/semana/mês/ano e 6 fusos)
 * 2) Relógio de Mesa gigante com controle real do brilho do hardware (100%, 60%, 30%, 10%, 2%)
 */
public final class NightStandClockWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final LinearLayout root;

    // Header + switcher (responsivo: 1 linha horizontal / 2 linhas vertical 600px)
    private final CardViewContainer topBar;
    private final LinearLayout tbContainer;
    private final LinearLayout tbLeftRow;
    private final View tbSpacer;
    private final LinearLayout tbTabsRow;
    private final StatusPillView locBadge;
    private final TextView miniClock;
    private final StatusPillView topRainPill;
    private final ActionButtonView tabWxBtn;
    private final ActionButtonView tabTimeBtn;
    private final ActionButtonView tabDeskBtn;

    // Sub-view 0: Clima & Previsão
    private final LinearLayout viewWeather;
    private final SplitColumnLayout wxTopSplit;
    private final CardViewContainer heroCard;
    private final WeatherGlyphView heroGlyph;
    private final TextView heroTemp;
    private final TextView heroCond;
    private final TextView heroFeels;
    private final TextView heroMinMax;
    private final ResponsiveGridLayout gaugeGrid;
    private final CardViewContainer gRainCard, gHumCard, gUvCard, gWindCard;
    private final MetricProgressBarView gRainBar, gHumBar, gUvBar, gWindBar;
    private final SplitColumnLayout wxBotSplit;
    private final CardViewContainer hourlyCard;
    private final NativeLineChartView hourlyChart;
    private final CardViewContainer dailyCard;
    private final LinearLayout dailyList;

    // Sub-view 1: Hora & Fusos
    private final SplitColumnLayout viewTime;
    private final CardViewContainer wordClockCard;
    private final TextView bigTimeInline;
    private final TextView wordClockText;
    private final CardViewContainer progressCard;
    private final LinearLayout progressBox;
    private final CardViewContainer worldCard;
    private final ResponsiveGridLayout worldGrid;

    // Sub-view 2: Relógio de Mesa
    private final CardViewContainer viewDesk;
    private final TextView deskClock;
    private final TextView deskSec;
    private final TextView deskDate;
    private final TextView deskWeather;
    private final TextView deskStats;
    private final TextView deskMedia;
    private final List<ActionButtonView> brightBtns = new ArrayList<ActionButtonView>();
    private final float[] brightLevels = { 1.0f, 0.60f, 0.30f, 0.10f, 0.02f };

    private int subTab = 0;
    private boolean showSeconds = true;
    private boolean hour12 = false;
    private List<String> zones = new ArrayList<String>();
    private String lastZonesSig = "";
    private final List<WorldClockHolder> worldHolders = new ArrayList<WorldClockHolder>();
    private final List<MetricProgressBarView> dailyBarPool = new ArrayList<MetricProgressBarView>();

    private static final class WorldClockHolder {
        final CardViewContainer card;
        final TextView cityView;
        final TextView timeView;
        final TextView tagView;
        final String tz;

        WorldClockHolder(CardViewContainer card, TextView cityView, TextView timeView, TextView tagView, String tz) {
            this.card = card;
            this.cityView = cityView;
            this.timeView = timeView;
            this.tagView = tagView;
            this.tz = tz;
        }
    }

    public NightStandClockWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        ThemeDimens d = AppTheme.get().dimens();
        root = new LinearLayout(wctx.context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Top switcher bar (responsiva para 600px vertical)
        topBar = new CardViewContainer(wctx.context);
        tbContainer = new LinearLayout(wctx.context);
        tbContainer.setOrientation(LinearLayout.HORIZONTAL);
        tbContainer.setGravity(Gravity.CENTER_VERTICAL);

        tbLeftRow = new LinearLayout(wctx.context);
        tbLeftRow.setOrientation(LinearLayout.HORIZONTAL);
        tbLeftRow.setGravity(Gravity.CENTER_VERTICAL);

        locBadge = new StatusPillView(wctx.context).setPill("RIO DE JANEIRO, BRASIL", StatusPillView.STYLE_ACCENT);
        locBadge.setMaxWidth(d.dp(190));
        tbLeftRow.addView(locBadge);

        miniClock = new TextView(wctx.context);
        miniClock.setTypeface(Typeface.DEFAULT_BOLD);
        miniClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        miniClock.setSingleLine(true);
        LinearLayout.LayoutParams mcLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mcLp.leftMargin = d.dp(8);
        mcLp.rightMargin = d.dp(8);
        tbLeftRow.addView(miniClock, mcLp);

        topRainPill = new StatusPillView(wctx.context).setPill("SEM CHUVA", StatusPillView.STYLE_SUCCESS);
        tbLeftRow.addView(topRainPill);

        tbContainer.addView(tbLeftRow);

        tbSpacer = new View(wctx.context);
        tbContainer.addView(tbSpacer, new LinearLayout.LayoutParams(0, 1, 1f));

        tbTabsRow = new LinearLayout(wctx.context);
        tbTabsRow.setOrientation(LinearLayout.HORIZONTAL);
        tbTabsRow.setGravity(Gravity.CENTER_VERTICAL);

        tabWxBtn = new ActionButtonView(wctx.context).setCompact().setButton("Clima", ActionButtonView.VARIANT_PRIMARY);
        tabWxBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { setSubTab(0); } });
        LinearLayout.LayoutParams t1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32));
        t1.rightMargin = d.dp(5);
        tbTabsRow.addView(tabWxBtn, t1);

        tabTimeBtn = new ActionButtonView(wctx.context).setCompact().setButton("Hora/Fusos", ActionButtonView.VARIANT_NORMAL);
        tabTimeBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { setSubTab(1); } });
        LinearLayout.LayoutParams t2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32));
        t2.rightMargin = d.dp(5);
        tbTabsRow.addView(tabTimeBtn, t2);

        tabDeskBtn = new ActionButtonView(wctx.context).setCompact().setButton("Relógio Mesa", ActionButtonView.VARIANT_NORMAL);
        tabDeskBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { setSubTab(2); } });
        tbTabsRow.addView(tabDeskBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32)));

        tbContainer.addView(tbTabsRow);
        topBar.addBodyView(tbContainer);
        LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tbLp.bottomMargin = d.spaceSm;
        root.addView(topBar, tbLp);

        // === SUB-VIEW 0: CLIMA & PREVISÃO ===
        viewWeather = new LinearLayout(wctx.context);
        viewWeather.setOrientation(LinearLayout.VERTICAL);

        wxTopSplit = new SplitColumnLayout(wctx.context).setWeights(0.40f, 0.60f).setGapPx(d.spaceSm);

        heroCard = new CardViewContainer(wctx.context).setVariant(CardViewContainer.VARIANT_HIGHLIGHT);
        LinearLayout heroRow = new LinearLayout(wctx.context);
        heroRow.setOrientation(LinearLayout.HORIZONTAL);
        heroRow.setGravity(Gravity.CENTER_VERTICAL);

        heroGlyph = new WeatherGlyphView(wctx.context);
        LinearLayout.LayoutParams hgLp = new LinearLayout.LayoutParams(d.dp(66), d.dp(66));
        hgLp.rightMargin = d.dp(12);
        heroRow.addView(heroGlyph, hgLp);

        LinearLayout heroInfo = new LinearLayout(wctx.context);
        heroInfo.setOrientation(LinearLayout.VERTICAL);
        heroTemp = new TextView(wctx.context);
        heroTemp.setTypeface(Typeface.DEFAULT_BOLD);
        heroTemp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f);
        heroTemp.setText("--°C");
        heroInfo.addView(heroTemp);

        heroCond = new TextView(wctx.context);
        heroCond.setTypeface(Typeface.DEFAULT_BOLD);
        heroCond.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        heroInfo.addView(heroCond);

        heroFeels = new TextView(wctx.context);
        heroFeels.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        heroInfo.addView(heroFeels);
        heroRow.addView(heroInfo, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        heroCard.addBodyView(heroRow);

        heroMinMax = new TextView(wctx.context);
        heroMinMax.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        LinearLayout.LayoutParams mmLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mmLp.topMargin = d.dp(8);
        heroCard.addBodyView(heroMinMax, mmLp);

        gaugeGrid = new ResponsiveGridLayout(wctx.context).setColumns(2, 2).setGapPx(d.spaceSm);
        gRainCard = new CardViewContainer(wctx.context).setHeader("CHUVA");
        gRainBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        gRainCard.addBodyView(gRainBar);
        gHumCard = new CardViewContainer(wctx.context).setHeader("UMIDADE");
        gHumBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        gHumCard.addBodyView(gHumBar);
        gUvCard = new CardViewContainer(wctx.context).setHeader("INDICE UV");
        gUvBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        gUvCard.addBodyView(gUvBar);
        gWindCard = new CardViewContainer(wctx.context).setHeader("VENTO & AR");
        gWindBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        gWindCard.addBodyView(gWindBar);
        List<View> gs = new ArrayList<View>(4);
        gs.add(gRainCard); gs.add(gHumCard); gs.add(gUvCard); gs.add(gWindCard);
        gaugeGrid.setItems(gs);

        wxTopSplit.setPanes(heroCard, gaugeGrid);
        LinearLayout.LayoutParams wtLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wtLp.bottomMargin = d.spaceSm;
        viewWeather.addView(wxTopSplit, wtLp);

        wxBotSplit = new SplitColumnLayout(wctx.context).setWeights(1f, 1f).setGapPx(d.spaceSm);
        hourlyCard = new CardViewContainer(wctx.context).setHeader("PRÓXIMAS 12H · CURVA TÉRMICA & CHUVA");
        hourlyChart = new NativeLineChartView(wctx.context).setAutoScale(true);
        hourlyCard.addBodyView(hourlyChart, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.dp(116)));

        dailyCard = new CardViewContainer(wctx.context).setHeader("PREVISÃO DE 7 DIAS");
        dailyList = new LinearLayout(wctx.context);
        dailyList.setOrientation(LinearLayout.VERTICAL);
        dailyCard.addBodyView(dailyList);

        wxBotSplit.setPanes(hourlyCard, dailyCard);
        viewWeather.addView(wxBotSplit);
        root.addView(viewWeather);

        // === SUB-VIEW 1: HORA & FUSOS ===
        viewTime = new SplitColumnLayout(wctx.context).setWeights(1f, 1f).setGapPx(d.spaceSm);
        LinearLayout timeLeft = new LinearLayout(wctx.context);
        timeLeft.setOrientation(LinearLayout.VERTICAL);

        wordClockCard = new CardViewContainer(wctx.context).setHeader("HORA LOCAL · UTC-3 BRASILIA");
        bigTimeInline = new TextView(wctx.context);
        bigTimeInline.setTypeface(Typeface.DEFAULT_BOLD);
        bigTimeInline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f);
        wordClockCard.addBodyView(bigTimeInline);
        wordClockText = new TextView(wctx.context);
        wordClockText.setTypeface(Typeface.DEFAULT_BOLD);
        wordClockText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        wordClockCard.addBodyView(wordClockText);
        LinearLayout.LayoutParams wcLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wcLp.bottomMargin = d.spaceSm;
        timeLeft.addView(wordClockCard, wcLp);

        progressCard = new CardViewContainer(wctx.context).setHeader("PROGRESSO DO TEMPO");
        progressBox = new LinearLayout(wctx.context);
        progressBox.setOrientation(LinearLayout.VERTICAL);
        progressCard.addBodyView(progressBox);
        timeLeft.addView(progressCard);

        worldCard = new CardViewContainer(wctx.context).setHeader("RELOGIOS MUNDIAIS & FUSOS");
        worldGrid = new ResponsiveGridLayout(wctx.context).setColumns(2, 2).setGapPx(d.spaceSm);
        worldCard.addBodyView(worldGrid);

        viewTime.setPanes(timeLeft, worldCard);
        viewTime.setVisibility(View.GONE);
        root.addView(viewTime);

        // === SUB-VIEW 2: RELÓGIO DE MESA GIGANTE + BRILHO REAL DO HARDWARE ===
        viewDesk = new CardViewContainer(wctx.context).setVariant(CardViewContainer.VARIANT_HIGHLIGHT);
        viewDesk.body().setGravity(Gravity.CENTER_HORIZONTAL);
        viewDesk.setPadding(d.dp(16), d.dp(24), d.dp(16), d.dp(20));

        LinearLayout deskClockRow = new LinearLayout(wctx.context);
        deskClockRow.setOrientation(LinearLayout.HORIZONTAL);
        deskClockRow.setGravity(Gravity.CENTER);
        deskClock = new TextView(wctx.context);
        deskClock.setTypeface(Typeface.DEFAULT_BOLD);
        deskClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 96f);
        deskClock.setIncludeFontPadding(false);
        deskClockRow.addView(deskClock);

        deskSec = new TextView(wctx.context);
        deskSec.setTypeface(Typeface.DEFAULT_BOLD);
        deskSec.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
        LinearLayout.LayoutParams dsLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dsLp.leftMargin = d.dp(6);
        deskClockRow.addView(deskSec, dsLp);
        viewDesk.addBodyView(deskClockRow);

        deskDate = new TextView(wctx.context);
        deskDate.setGravity(Gravity.CENTER);
        deskDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        LinearLayout.LayoutParams ddLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ddLp.topMargin = d.dp(8);
        viewDesk.addBodyView(deskDate, ddLp);

        deskWeather = new TextView(wctx.context);
        deskWeather.setGravity(Gravity.CENTER);
        deskWeather.setTypeface(Typeface.DEFAULT_BOLD);
        deskWeather.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f);
        LinearLayout.LayoutParams dwLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dwLp.topMargin = d.dp(10);
        viewDesk.addBodyView(deskWeather, dwLp);

        deskStats = new TextView(wctx.context);
        deskStats.setGravity(Gravity.CENTER);
        deskStats.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stLp.topMargin = d.dp(10);
        viewDesk.addBodyView(deskStats, stLp);

        deskMedia = new TextView(wctx.context);
        deskMedia.setGravity(Gravity.CENTER);
        deskMedia.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        LinearLayout.LayoutParams dmLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dmLp.topMargin = d.dp(5);
        viewDesk.addBodyView(deskMedia, dmLp);

        // Controle real de brilho do backlight (100%, 60%, 30%, 10%, 2%)
        LinearLayout brightRow = new LinearLayout(wctx.context);
        brightRow.setOrientation(LinearLayout.HORIZONTAL);
        brightRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams brLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        brLp.topMargin = d.dp(18);
        String[] bLabels = { "100%", "60%", "30%", "10%", "2%" };
        for (int i = 0; i < bLabels.length; i++) {
            final float lvl = brightLevels[i];
            ActionButtonView b = new ActionButtonView(wctx.context).setCompact().setButton(bLabels[i], ActionButtonView.VARIANT_NORMAL);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (wctx.host != null) wctx.host.setHardwareBrightness(lvl);
                    highlightBrightnessButtons();
                }
            });
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(d.dp(56), d.dp(36));
            if (i > 0) blp.leftMargin = d.dp(6);
            brightRow.addView(b, blp);
            brightBtns.add(b);
        }
        viewDesk.addBodyView(brightRow, brLp);

        viewDesk.setVisibility(View.GONE);
        root.addView(viewDesk);

        applyTheme();
        tickClock();
    }

    public void setSubTab(int idx) {
        this.subTab = idx;
        viewWeather.setVisibility(idx == 0 ? View.VISIBLE : View.GONE);
        viewTime.setVisibility(idx == 1 ? View.VISIBLE : View.GONE);
        viewDesk.setVisibility(idx == 2 ? View.VISIBLE : View.GONE);
        tabWxBtn.setButton("Clima", idx == 0 ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
        tabTimeBtn.setButton("Hora/Fusos", idx == 1 ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
        tabDeskBtn.setButton("Relógio Mesa", idx == 2 ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
        highlightBrightnessButtons();
    }

    private void highlightBrightnessButtons() {
        float cur = wctx.host != null ? wctx.host.getHardwareBrightness() : 1.0f;
        String[] labels = { "100%", "60%", "30%", "10%", "2%" };
        for (int i = 0; i < brightBtns.size(); i++) {
            boolean active = Math.abs(brightLevels[i] - cur) < 0.06f;
            brightBtns.get(i).setButton(labels[i], active ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
        }
    }

    public void tickClock() {
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY);
        int m = now.get(Calendar.MINUTE);
        int s = now.get(Calendar.SECOND);
        String hm = FormatUtils.fmtHourMin(h, m, hour12);
        String suf = FormatUtils.amPmSuffix(h, hour12);
        String ss = FormatUtils.pad2(s);

        miniClock.setText(hm + (showSeconds ? (":" + ss) : "") + suf);
        bigTimeInline.setText(hm + (showSeconds ? (":" + ss) : "") + suf);
        wordClockText.setText(FormatUtils.wordClockPt(h, m));

        deskClock.setText(hm);
        deskSec.setText((showSeconds ? ss : "") + suf);
        deskSec.setVisibility((showSeconds || suf.length() > 0) ? View.VISIBLE : View.GONE);
        deskDate.setText(FormatUtils.fmtDatePt(now));

        if (subTab == 1) {
            renderTimeProgress(now);
            renderWorldClocks(now);
        }
    }

    private void renderTimeProgress(Calendar now) {
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        int dayPct = Math.round(((now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND)) / 86400f) * 100f);
        int dow = now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : (now.get(Calendar.DAY_OF_WEEK) - 1);
        int weekPct = Math.round((((dow - 1) + dayPct / 100f) / 7f) * 100f);
        int dim = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        int monthPct = Math.round((((now.get(Calendar.DAY_OF_MONTH) - 1) + dayPct / 100f) / dim) * 100f);
        int diy = now.getActualMaximum(Calendar.DAY_OF_YEAR);
        int yearPct = Math.round((now.get(Calendar.DAY_OF_YEAR) * 100f) / diy);

        String[] names = { "Dia de hoje", "Semana atual", "Mes atual", "Ano de " + now.get(Calendar.YEAR) };
        int[] vals = { dayPct, weekPct, monthPct, yearPct };
        int[] cols = { c.accent, c.sky, c.purple, c.green };

        if (progressBox.getChildCount() != 4) {
            progressBox.removeAllViews();
            for (int i = 0; i < 4; i++) {
                MetricProgressBarView bar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i > 0) lp.topMargin = d.dp(6);
                progressBox.addView(bar, lp);
            }
        }
        for (int i = 0; i < 4; i++) {
            MetricProgressBarView bar = (MetricProgressBarView) progressBox.getChildAt(i);
            bar.setFixedColor(cols[i]).setMetric(names[i], vals[i] + "%", vals[i], null, null);
        }
    }

    private void renderWorldClocks(Calendar now) {
        ThemeColors c = AppTheme.get().colors();
        List<String> list = zones;
        if (list.isEmpty()) {
            list = new ArrayList<String>();
            list.add("Rio de Janeiro=America/Sao_Paulo");
            list.add("Lisboa=Europe/Lisbon");
            list.add("Londres=Europe/London");
            list.add("Nova York=America/New_York");
            list.add("Berlim=Europe/Berlin");
            list.add("Tóquio=Asia/Tokyo");
        }

        int count = Math.min(6, list.size());
        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < count; i++) sig.append(list.get(i)).append(';');
        String nextSig = sig.toString();

        // Constrói os 6 cards UMA única vez; no tick de 1s apenas atualiza o texto (zero alocação/GC)
        if (!nextSig.equals(lastZonesSig) || worldHolders.size() != count) {
            lastZonesSig = nextSig;
            worldHolders.clear();
            List<View> cards = new ArrayList<View>(count);
            for (int i = 0; i < count; i++) {
                String raw = list.get(i);
                int eq = raw.indexOf('=');
                String label = eq >= 0 ? raw.substring(0, eq) : raw;
                String tz = eq >= 0 ? raw.substring(eq + 1) : raw;

                CardViewContainer box = new CardViewContainer(wctx.context)
                        .setVariant("America/Sao_Paulo".equals(tz) ? CardViewContainer.VARIANT_HIGHLIGHT : CardViewContainer.VARIANT_ALT);
                TextView ct = new TextView(wctx.context);
                ct.setTextColor(c.textMuted);
                ct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                ct.setTypeface(Typeface.DEFAULT_BOLD);
                ct.setSingleLine(true);
                ct.setText(label);
                box.addBodyView(ct);

                TextView tm = new TextView(wctx.context);
                tm.setTextColor(c.textPrimary);
                tm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
                tm.setTypeface(Typeface.DEFAULT_BOLD);
                tm.setSingleLine(true);
                box.addBodyView(tm);

                TextView tg = new TextView(wctx.context);
                tg.setTextColor(c.accent);
                tg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                tg.setSingleLine(true);
                box.addBodyView(tg);

                cards.add(box);
                worldHolders.add(new WorldClockHolder(box, ct, tm, tg, tz));
            }
            worldGrid.setItems(cards);
        }

        long utcMs = now.getTimeInMillis();
        Calendar zc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        for (int i = 0; i < worldHolders.size(); i++) {
            WorldClockHolder wh = worldHolders.get(i);
            double offH = FormatUtils.tzOffsetHours(wh.tz, utcMs);
            zc.setTimeInMillis(utcMs + Math.round(offH * 3600000.0));
            String hm = FormatUtils.fmtHourMin(zc.get(Calendar.HOUR_OF_DAY), zc.get(Calendar.MINUTE), hour12)
                    + FormatUtils.amPmSuffix(zc.get(Calendar.HOUR_OF_DAY), hour12);
            wh.timeView.setText(hm);
            long offRounded = Math.round(offH);
            wh.tagView.setText("UTC" + (offRounded >= 0 ? "+" : "") + offRounded);
        }
    }

    @Override
    public View getView() { return root; }

    @Override
    public void bindData(AppState state) {
        this.showSeconds = state.display.showSeconds;
        this.hour12 = state.display.hour12;
        this.zones = state.worldClockZones;

        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        AppState.WeatherData w = state.weather;
        locBadge.setPill(w.city.toUpperCase(), StatusPillView.STYLE_ACCENT);

        if (w.ok) {
            String u = "°" + w.unit;
            String desc = FormatUtils.wmoPt(w.code);
            String sr = FormatUtils.extractTimePart(w.sunrise);
            String ss = FormatUtils.extractTimePart(w.sunset);
            heroGlyph.setWeather(w.code, w.isDay);
            heroTemp.setText(Math.round(w.temp) + u);
            heroCond.setText(desc);
            heroFeels.setText("Sensacao termica: " + Math.round(w.feels) + u);
            heroMinMax.setText("Min " + Math.round(w.lo) + u + "  ·  Max " + Math.round(w.hi) + u + "  ·  Sol " + sr + "–" + ss);

            boolean raining = w.code >= 51 || w.precipMm > 0.05f;
            if (raining) topRainPill.setPill("CHOVENDO · " + w.rainProb + "%", StatusPillView.STYLE_INFO);
            else if (w.rainProb >= 50) topRainPill.setPill("CHUVA ALTA · " + w.rainProb + "%", StatusPillView.STYLE_WARN);
            else topRainPill.setPill("SEM CHUVA · " + w.rainProb + "%", StatusPillView.STYLE_SUCCESS);

            gRainBar.setFixedColor(w.rainProb >= 55 ? c.amber : c.sky)
                    .setMetric(w.rainProb + "%", w.precipMm > 0 ? (w.precipMm + " mm") : "Hoje", Math.max(4, w.rainProb), null, null);
            gHumBar.setFixedColor(c.sky)
                    .setMetric(w.humidity + "%", w.humidity >= 75 ? "Umido" : "Confort.", Math.max(4, w.humidity), null, null);
            gUvBar.setFixedColor(w.uv >= 8 ? c.red : (w.uv >= 5 ? c.amber : c.green))
                    .setMetric("UV " + FormatUtils.round1(w.uv), FormatUtils.uvLabelPt(w.uv), Math.min(100f, (w.uv / 11f) * 100f), null, null);
            gWindBar.setFixedColor(c.accent)
                    .setMetric(Math.round(w.windKmh) + " km/h " + FormatUtils.windDirPt(w.windDir), "Ar " + FormatUtils.aqiLabelPt(w.aqi), Math.min(100f, Math.max(10f, w.aqi / 2f)), null, null);

            // 12h Canvas chart
            int hn = Math.min(12, w.hourly.size());
            if (hn >= 2) {
                float[] temps = new float[hn];
                int[] pops = new int[hn];
                for (int i = 0; i < hn; i++) {
                    temps[i] = w.hourly.get(i).temp;
                    pops[i] = w.hourly.get(i).pop;
                }
                hourlyChart.setArrayData(temps, pops);
            }

            // 7-day forecast (pool reutilizável, zero removeAllViews por tick)
            int dayCount = Math.min(7, w.daily.size());
            while (dailyBarPool.size() < dayCount) {
                MetricProgressBarView bar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (!dailyBarPool.isEmpty()) bLp.topMargin = d.dp(3);
                dailyList.addView(bar, bLp);
                dailyBarPool.add(bar);
            }
            float weekMin = 999f, weekMax = -999f;
            for (int i = 0; i < w.daily.size(); i++) {
                if (w.daily.get(i).lo < weekMin) weekMin = w.daily.get(i).lo;
                if (w.daily.get(i).hi > weekMax) weekMax = w.daily.get(i).hi;
            }
            float span = Math.max(1f, weekMax - weekMin);
            for (int i = 0; i < dailyBarPool.size(); i++) {
                MetricProgressBarView bar = dailyBarPool.get(i);
                if (i >= dayCount) {
                    bar.setVisibility(View.GONE);
                    continue;
                }
                bar.setVisibility(View.VISIBLE);
                AppState.DailyPoint dp = w.daily.get(i);
                String dayLbl = i == 0 ? "Hoje" : (i == 1 ? "Amanhã" : dp.date.substring(Math.min(5, dp.date.length())));
                float pct = Math.max(10f, Math.min(100f, ((dp.hi - weekMin) / span) * 100f));
                bar.setFixedColor(c.sky).setMetric(dayLbl + " · " + dp.pop + "% chuva",
                        Math.round(dp.lo) + "° / " + Math.round(dp.hi) + "°", pct, null, null);
            }

            deskWeather.setText(Math.round(w.temp) + u + " · " + desc + " (" + w.city + ")");
        }

        // Desk clock live summary
        String gTemp = state.pc.hasGpu && state.pc.hasGpuTemp ? (" (" + Math.round(state.pc.gpuTemp) + "°C)") : "";
        deskStats.setText("CPU " + Math.round(state.pc.cpuPct) + "%  ·  GPU " + Math.round(state.pc.gpuUtil) + "%" + gTemp
                + "  ·  RAM " + FormatUtils.round1(state.pc.ramUsedGb) + "/" + FormatUtils.round1(state.pc.ramTotalGb) + " GB");
        deskMedia.setText(state.media.title.length() > 0
                ? (state.media.title + (state.media.artist.length() > 0 ? (" — " + state.media.artist) : ""))
                : "");

        highlightBrightnessButtons();
        tickClock();
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        ThemeDimens d = AppTheme.get().dimens();
        tbContainer.setOrientation(portrait ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        tbContainer.setGravity(portrait ? Gravity.LEFT : Gravity.CENTER_VERTICAL);
        tbSpacer.setVisibility(portrait ? View.GONE : View.VISIBLE);
        LinearLayout.LayoutParams trLp = (LinearLayout.LayoutParams) tbTabsRow.getLayoutParams();
        if (trLp != null) {
            trLp.width = portrait ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT;
            trLp.topMargin = portrait ? d.dp(6) : 0;
            tbTabsRow.setLayoutParams(trLp);
        }
        for (int i = 0; i < tbTabsRow.getChildCount(); i++) {
            View ch = tbTabsRow.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ch.getLayoutParams();
            if (lp != null) {
                lp.width = portrait ? 0 : LinearLayout.LayoutParams.WRAP_CONTENT;
                lp.weight = portrait ? 1f : 0f;
                ch.setLayoutParams(lp);
            }
        }
        LinearLayout.LayoutParams hcLp = (LinearLayout.LayoutParams) hourlyChart.getLayoutParams();
        if (hcLp != null) {
            hcLp.height = d.dp(portrait ? 138 : 112);
            hourlyChart.setLayoutParams(hcLp);
        }
        wxTopSplit.setPortrait(portrait);
        wxBotSplit.setPortrait(portrait);
        viewTime.setPortrait(portrait);
        deskClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, portrait ? 78f : 86f);
    }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        topBar.applyTheme();
        locBadge.applyTheme();
        topRainPill.applyTheme();
        miniClock.setTextColor(c.textPrimary);
        tabWxBtn.applyTheme();
        tabTimeBtn.applyTheme();
        tabDeskBtn.applyTheme();

        heroCard.applyTheme();
        heroTemp.setTextColor(c.sky);
        heroCond.setTextColor(c.textPrimary);
        heroFeels.setTextColor(c.textMuted);
        heroMinMax.setTextColor(c.textSecondary);
        gRainCard.applyTheme(); gHumCard.applyTheme(); gUvCard.applyTheme(); gWindCard.applyTheme();
        hourlyCard.applyTheme();
        hourlyChart.setColors(c.accent, c.sky).applyTheme();
        dailyCard.applyTheme();

        wordClockCard.applyTheme();
        bigTimeInline.setTextColor(c.textPrimary);
        wordClockText.setTextColor(c.sky);
        progressCard.applyTheme();
        worldCard.applyTheme();
        for (int i = 0; i < worldHolders.size(); i++) {
            WorldClockHolder wh = worldHolders.get(i);
            wh.card.applyTheme();
            wh.cityView.setTextColor(c.textMuted);
            wh.timeView.setTextColor(c.textPrimary);
            wh.tagView.setTextColor(c.accent);
        }
        for (int i = 0; i < dailyBarPool.size(); i++) {
            dailyBarPool.get(i).applyTheme();
        }

        viewDesk.applyTheme();
        deskClock.setTextColor(c.textPrimary);
        deskSec.setTextColor(c.accent);
        deskDate.setTextColor(c.textSecondary);
        deskWeather.setTextColor(c.sky);
        deskStats.setTextColor(c.textMuted);
        deskMedia.setTextColor(c.accent);
        highlightBrightnessButtons();
    }
}

