package net.oxog.phonebox.hp71.widgets;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.MetricProgressBarView;
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.components.TouchActionTileView;
import net.oxog.phonebox.hp71.core.ApiClient;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.core.BitmapCache;
import net.oxog.phonebox.hp71.layouts.ResponsiveGridLayout;
import net.oxog.phonebox.hp71.layouts.SplitColumnLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Widget modular de Processos com 4 KPI cards, barra superior responsiva (1 linha horiz. / 2 linhas vert.),
 * cabeçalho de colunas, pool reutilizável de até 30 RowHolders (zero GC / zero travamento de scroll no Cortex-A7),
 * ícones reais com fallback monograma e encerramento em 2 toques persistido entre ticks.
 */
public final class ProcessTableWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final LinearLayout root;

    private final CardViewContainer topBarCard;
    private final LinearLayout topContainer;
    private final LinearLayout topBtnsRow;
    private final View topSpacer;
    private final StatusPillView metaPill;
    private final ActionButtonView sortRamBtn;
    private final ActionButtonView sortCpuBtn;
    private final ActionButtonView limitBtn;
    private final ActionButtonView refreshBtn;

    private final ResponsiveGridLayout kpiGrid;
    private final SplitColumnLayout processSplit;
    private final CardViewContainer kpiCpuCard;
    private final MetricProgressBarView kpiCpuBar;
    private final CardViewContainer kpiRamCard;
    private final MetricProgressBarView kpiRamBar;
    private final CardViewContainer kpiSysCard;
    private final MetricProgressBarView kpiSysBar;
    private final CardViewContainer kpiHeavyCard;
    private final MetricProgressBarView kpiHeavyBar;

    private final CardViewContainer listCard;
    private final LinearLayout colHeaderRow;
    private final TextView hdrApp, hdrCpu, hdrRam, hdrAct;
    private final LinearLayout rowsBox;
    private final TextView emptyView;
    private final List<RowHolder> rowPool = new ArrayList<RowHolder>();

    private boolean sortByCpu = false;
    private int limit = 15;
    private boolean portrait = false;
    private AppState lastState;
    private String armedKillName = "";
    private long armedKillUntilMs = 0L;

    private static final class RowHolder {
        final LinearLayout row;
        final FrameLayout icoBox;
        final ImageView icoImg;
        final TextView monoView;
        final TextView nameView;
        final MetricProgressBarView cpuBar;
        final MetricProgressBarView ramBar;
        final ActionButtonView killBtn;
        final TextView lockView;
        String boundIconKey = "";
        String boundProcName = "";
        String boundDisplayName = "";

        RowHolder(LinearLayout row, FrameLayout icoBox, ImageView icoImg, TextView monoView,
                  TextView nameView, MetricProgressBarView cpuBar, MetricProgressBarView ramBar,
                  ActionButtonView killBtn, TextView lockView) {
            this.row = row;
            this.icoBox = icoBox;
            this.icoImg = icoImg;
            this.monoView = monoView;
            this.nameView = nameView;
            this.cpuBar = cpuBar;
            this.ramBar = ramBar;
            this.killBtn = killBtn;
            this.lockView = lockView;
        }
    }

    public ProcessTableWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        ThemeDimens d = AppTheme.get().dimens();
        root = new LinearLayout(wctx.context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Barra superior responsiva (1 linha em horizontal, 2 linhas em vertical 600px)
        topBarCard = new CardViewContainer(wctx.context);
        topContainer = new LinearLayout(wctx.context);
        topContainer.setOrientation(LinearLayout.HORIZONTAL);
        topContainer.setGravity(Gravity.CENTER_VERTICAL);

        metaPill = new StatusPillView(wctx.context).setPill("PROCESSOS", StatusPillView.STYLE_ACCENT);
        topContainer.addView(metaPill);

        topSpacer = new View(wctx.context);
        topContainer.addView(topSpacer, new LinearLayout.LayoutParams(0, 1, 1f));

        topBtnsRow = new LinearLayout(wctx.context);
        topBtnsRow.setOrientation(LinearLayout.HORIZONTAL);
        topBtnsRow.setGravity(Gravity.CENTER_VERTICAL);

        sortRamBtn = new ActionButtonView(wctx.context).setCompact().setButton("Por RAM", ActionButtonView.VARIANT_PRIMARY);
        sortRamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByCpu = false;
                updateToggleStyles();
                if (lastState != null) bindData(lastState);
            }
        });
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32));
        lpBtn.rightMargin = d.dp(5);
        topBtnsRow.addView(sortRamBtn, lpBtn);

        sortCpuBtn = new ActionButtonView(wctx.context).setCompact().setButton("Por CPU", ActionButtonView.VARIANT_NORMAL);
        sortCpuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByCpu = true;
                updateToggleStyles();
                if (lastState != null) bindData(lastState);
            }
        });
        LinearLayout.LayoutParams lpBtn2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32));
        lpBtn2.rightMargin = d.dp(5);
        topBtnsRow.addView(sortCpuBtn, lpBtn2);

        limitBtn = new ActionButtonView(wctx.context).setCompact().setButton("Top 15", ActionButtonView.VARIANT_NORMAL);
        limitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limit = (limit == 15) ? 30 : 15;
                limitBtn.setButton(limit == 15 ? "Top 15" : "Todos 30", ActionButtonView.VARIANT_NORMAL);
                if (lastState != null) bindData(lastState);
            }
        });
        LinearLayout.LayoutParams lpBtn3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32));
        lpBtn3.rightMargin = d.dp(5);
        topBtnsRow.addView(limitBtn, lpBtn3);

        refreshBtn = new ActionButtonView(wctx.context).setCompact().setButton("Atualizar", ActionButtonView.VARIANT_NORMAL);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.requestImmediateSync(false); }
        });
        topBtnsRow.addView(refreshBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32)));

        topContainer.addView(topBtnsRow);
        topBarCard.addBodyView(topContainer);

        LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tbLp.bottomMargin = d.spaceSm;
        root.addView(topBarCard, tbLp);

        // 4 KPI Cards
        kpiGrid = new ResponsiveGridLayout(wctx.context).setColumns(1, 2).setGapPx(d.spaceSm);
        kpiCpuCard = new CardViewContainer(wctx.context).setHeader("MAIOR USO DE CPU");
        kpiCpuBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        kpiCpuCard.addBodyView(kpiCpuBar);

        kpiRamCard = new CardViewContainer(wctx.context).setHeader("MAIOR USO DE RAM");
        kpiRamBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        kpiRamCard.addBodyView(kpiRamBar);

        kpiSysCard = new CardViewContainer(wctx.context).setHeader("CARGA DO SISTEMA");
        kpiSysBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        kpiSysCard.addBodyView(kpiSysBar);

        kpiHeavyCard = new CardViewContainer(wctx.context).setHeader("APPS PESADOS");
        kpiHeavyBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        kpiHeavyCard.addBodyView(kpiHeavyBar);

        List<View> kpis = new ArrayList<View>(4);
        kpis.add(kpiCpuCard);
        kpis.add(kpiRamCard);
        kpis.add(kpiSysCard);
        kpis.add(kpiHeavyCard);
        kpiGrid.setItems(kpis);


        // Lista com cabeçalho de colunas + pool de linhas
        listCard = new CardViewContainer(wctx.context).setVariant(CardViewContainer.VARIANT_HIGHLIGHT).setHeader("APLICATIVOS EM EXECUÇÃO");

        colHeaderRow = new LinearLayout(wctx.context);
        colHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        colHeaderRow.setPadding(d.dp(4), 0, d.dp(4), d.dp(5));
        hdrApp = makeColHeader("APLICATIVO", Gravity.LEFT);
        hdrCpu = makeColHeader("CPU", Gravity.RIGHT);
        hdrRam = makeColHeader("RAM", Gravity.RIGHT);
        hdrAct = makeColHeader("AÇÃO", Gravity.CENTER);

        LinearLayout.LayoutParams haLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.55f);
        haLp.leftMargin = d.dp(36);
        haLp.rightMargin = d.dp(8);
        colHeaderRow.addView(hdrApp, haLp);

        LinearLayout.LayoutParams hcLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.85f);
        hcLp.rightMargin = d.dp(8);
        colHeaderRow.addView(hdrCpu, hcLp);

        LinearLayout.LayoutParams hrLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.05f);
        hrLp.rightMargin = d.dp(8);
        colHeaderRow.addView(hdrRam, hrLp);

        colHeaderRow.addView(hdrAct, new LinearLayout.LayoutParams(d.dp(78), LinearLayout.LayoutParams.WRAP_CONTENT));
        listCard.addBodyView(colHeaderRow);

        rowsBox = new LinearLayout(wctx.context);
        rowsBox.setOrientation(LinearLayout.VERTICAL);
        listCard.addBodyView(rowsBox);

        emptyView = new TextView(wctx.context);
        emptyView.setText("Carregando lista de processos...");
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        emptyView.setPadding(d.dp(4), d.dp(8), d.dp(4), d.dp(8));
        rowsBox.addView(emptyView);

        processSplit = new SplitColumnLayout(wctx.context).setWeights(0.76f, 0.24f)
                .setMatchHeightHorizontal(false).setGapPx(d.spaceSm);
        processSplit.setPanes(listCard, kpiGrid);
        root.addView(processSplit);
        applyTheme();
    }

    private TextView makeColHeader(String text, int gravity) {
        TextView tv = new TextView(wctx.context);
        tv.setText(text);
        tv.setGravity(gravity);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, AppTheme.get().dimens().textMicroSp);
        tv.setSingleLine(true);
        return tv;
    }

    private void updateToggleStyles() {
        sortRamBtn.setButton("Por RAM", sortByCpu ? ActionButtonView.VARIANT_NORMAL : ActionButtonView.VARIANT_PRIMARY);
        sortCpuBtn.setButton("Por CPU", sortByCpu ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
    }

    private RowHolder createRowHolder() {
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();

        LinearLayout row = new LinearLayout(wctx.context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(d.dp(4), d.dp(5), d.dp(4), d.dp(5));

        FrameLayout icoBox = new FrameLayout(wctx.context);
        icoBox.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.dp(6)));

        TextView monoView = new TextView(wctx.context);
        monoView.setGravity(Gravity.CENTER);
        monoView.setTypeface(Typeface.DEFAULT_BOLD);
        monoView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        monoView.setTextColor(c.accent);
        icoBox.addView(monoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        ImageView icoImg = new ImageView(wctx.context);
        icoImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icoImg.setPadding(d.dp(3), d.dp(3), d.dp(3), d.dp(3));
        icoImg.setVisibility(View.GONE);
        icoBox.addView(icoImg, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout.LayoutParams icoLp = new LinearLayout.LayoutParams(d.dp(28), d.dp(28));
        icoLp.rightMargin = d.dp(8);
        row.addView(icoBox, icoLp);

        TextView nameView = new TextView(wctx.context);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        nameView.setTextColor(c.textPrimary);
        nameView.setSingleLine(true);
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.55f);
        nameLp.rightMargin = d.dp(8);
        row.addView(nameView, nameLp);

        MetricProgressBarView cpuBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        LinearLayout.LayoutParams cpuLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.85f);
        cpuLp.rightMargin = d.dp(8);
        row.addView(cpuBar, cpuLp);

        MetricProgressBarView ramBar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        LinearLayout.LayoutParams ramLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.05f);
        ramLp.rightMargin = d.dp(8);
        row.addView(ramBar, ramLp);

        ActionButtonView killBtn = new ActionButtonView(wctx.context).setCompact();
        killBtn.setButton("X", ActionButtonView.VARIANT_NORMAL);
        row.addView(killBtn, new LinearLayout.LayoutParams(d.dp(78), d.dp(34)));

        TextView lockView = new TextView(wctx.context);
        lockView.setText("Sist.");
        lockView.setGravity(Gravity.CENTER);
        lockView.setTextColor(c.textMuted);
        lockView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        lockView.setVisibility(View.GONE);
        row.addView(lockView, new LinearLayout.LayoutParams(d.dp(78), d.dp(34)));

        final RowHolder holder = new RowHolder(row, icoBox, icoImg, monoView, nameView, cpuBar, ramBar, killBtn, lockView);
        killBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String procName = holder.boundProcName;
                String dispName = holder.boundDisplayName;
                if (procName == null || procName.length() == 0) return;
                long now = System.currentTimeMillis();
                if (procName.equals(armedKillName) && now < armedKillUntilMs) {
                    armedKillName = "";
                    armedKillUntilMs = 0L;
                    wctx.sync.postAction(
                            "/api/procs/kill?name=" + ApiClient.enc(procName),
                            "Encerrado: " + dispName,
                            "Não foi possível encerrar " + dispName,
                            true);
                } else {
                    armedKillName = procName;
                    armedKillUntilMs = now + 4500L;
                    if (lastState != null) bindData(lastState);
                    root.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (armedKillUntilMs > 0L && System.currentTimeMillis() >= armedKillUntilMs) {
                                armedKillName = "";
                                armedKillUntilMs = 0L;
                                if (lastState != null) bindData(lastState);
                            }
                        }
                    }, 4600L);
                }
            }
        });

        rowsBox.addView(row);
        rowPool.add(holder);
        return holder;
    }

    @Override
    public View getView() { return root; }

    @Override
    public void bindData(AppState state) {
        this.lastState = state;
        ThemeColors c = AppTheme.get().colors();

        List<AppState.ProcessGroup> groups = new ArrayList<AppState.ProcessGroup>(state.processes);
        metaPill.setPill(state.totalProcs + " processos · " + state.groupProcs + " apps", StatusPillView.STYLE_ACCENT);

        AppState.ProcessGroup topCpu = null;
        AppState.ProcessGroup topRam = null;
        float maxMemMb = 512f;
        int heavy = 0;
        for (int i = 0; i < groups.size(); i++) {
            AppState.ProcessGroup g = groups.get(i);
            if (topCpu == null || g.cpu > topCpu.cpu) topCpu = g;
            if (topRam == null || g.memMb > topRam.memMb) topRam = g;
            if (g.memMb > maxMemMb) maxMemMb = g.memMb;
            if (g.memMb >= 300f || g.cpu >= 5f) heavy++;
        }

        float sysRamTotMb = Math.max(1024f, state.pc.ramTotalGb * 1024f);
        float topCpuPctVal = topCpu != null ? Math.min(100f, topCpu.cpu) : 0f;
        kpiCpuBar.setFixedColor(topCpuPctVal >= 50f ? c.red : (topCpuPctVal >= 25f ? c.amber : c.accent)).setMetric(
                topCpu != null ? topCpu.displayName() : "--",
                topCpu != null ? (FormatUtils.round1(topCpu.cpu) + "%") : "0%",
                topCpu != null ? Math.max(4f, topCpuPctVal) : 0f, null, null);
        float topRamPctVal = topRam != null ? Math.min(100f, (topRam.memMb / sysRamTotMb) * 100f) : 0f;
        kpiRamBar.setFixedColor(topRamPctVal >= 35f ? c.red : (topRamPctVal >= 18f ? c.amber : c.purple)).setMetric(
                topRam != null ? topRam.displayName() : "--",
                topRam != null ? FormatUtils.fmtMemMb(topRam.memMb) : "0 MB",
                topRam != null ? Math.max(5f, topRamPctVal) : 0f, null, null);
        int ramPct = state.pc.ramPct();
        kpiSysBar.setFixedColor(ramPct >= 85 ? c.red : c.sky).setMetric(
                "CPU " + Math.round(state.pc.cpuPct) + "% · " + FormatUtils.round1(state.pc.ramUsedGb) + "/" + FormatUtils.round1(state.pc.ramTotalGb) + "G",
                ramPct + "% RAM",
                ramPct, null, null);
        int heavyPct = groups.isEmpty() ? 0 : Math.min(100, Math.round((heavy * 100f) / groups.size()));
        kpiHeavyBar.setFixedColor(c.amber).setMetric(
                heavy + " apps pesados",
                state.totalProcs + " proc.",
                heavyPct, null, null);

        // Ordenação determinística com desempate estável
        Collections.sort(groups, new Comparator<AppState.ProcessGroup>() {
            @Override
            public int compare(AppState.ProcessGroup a, AppState.ProcessGroup b) {
                if (sortByCpu) {
                    if (b.cpu != a.cpu) return b.cpu > a.cpu ? 1 : -1;
                    if (b.memMb != a.memMb) return b.memMb > a.memMb ? 1 : -1;
                } else {
                    if (b.memMb != a.memMb) return b.memMb > a.memMb ? 1 : -1;
                    if (b.cpu != a.cpu) return b.cpu > a.cpu ? 1 : -1;
                }
                return a.name.compareToIgnoreCase(b.name);
            }
        });

        hdrAct.setVisibility(state.allowProcessKill ? View.VISIBLE : View.GONE);
        int shown = Math.min(limit, groups.size());
        if (shown == 0) {
            emptyView.setVisibility(View.VISIBLE);
            for (int i = 0; i < rowPool.size(); i++) rowPool.get(i).row.setVisibility(View.GONE);
            return;
        }
        emptyView.setVisibility(View.GONE);

        while (rowPool.size() < shown) {
            createRowHolder();
        }

        String base = wctx.api.baseUrl();
        long nowMs = System.currentTimeMillis();
        if (armedKillUntilMs > 0L && nowMs >= armedKillUntilMs) {
            armedKillName = "";
            armedKillUntilMs = 0L;
        }

        for (int i = 0; i < rowPool.size(); i++) {
            final RowHolder rh = rowPool.get(i);
            if (i >= shown) {
                rh.row.setVisibility(View.GONE);
                continue;
            }
            AppState.ProcessGroup g = groups.get(i);
            rh.row.setVisibility(View.VISIBLE);
            rh.row.setBackgroundColor((i % 2 == 1) ? ThemeColors.blend(c.textPrimary, c.cardHighlightBg, 0.06f) : 0x00000000);
            rh.boundProcName = g.name;
            rh.boundDisplayName = g.displayName();

            // Ícone oficial (reutiliza bitmap em cache e só carrega quando chave muda + g.hasIcon)
            String iconKey = g.hasIcon ? ("proc:" + g.name) : "";
            if (!iconKey.equals(rh.boundIconKey)) {
                rh.boundIconKey = iconKey;
                rh.monoView.setText(TouchActionTileView.monogram(g.displayName()));
                rh.icoImg.setImageBitmap(null);
                rh.icoImg.setVisibility(View.GONE);
                rh.monoView.setVisibility(View.VISIBLE);
                if (g.hasIcon) {
                    final String expectedKey = iconKey;
                    wctx.bitmaps.load(iconKey, base + "/api/procs/icon?name=" + ApiClient.enc(g.name), null, 64,
                            new BitmapCache.Callback() {
                                @Override
                                public void onBitmapLoaded(String key, Bitmap bmp) {
                                    if (bmp != null && expectedKey.equals(rh.boundIconKey)) {
                                        rh.icoImg.setImageBitmap(bmp);
                                        rh.icoImg.setVisibility(View.VISIBLE);
                                        rh.monoView.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }

            rh.nameView.setText(g.displayName() + (g.count > 1 ? (" ×" + g.count) : ""));

            int cpuColor = g.cpu >= 40f ? c.red : (g.cpu >= 15f ? c.amber : c.accent);
            rh.cpuBar.setFixedColor(cpuColor).setMetric("", FormatUtils.round1(g.cpu) + "%", Math.min(100f, g.cpu), null, null);

            float ramRatio = Math.min(100f, Math.max(3f, (g.memMb / maxMemMb) * 100f));
            rh.ramBar.setFixedColor(g.memMb >= 2048f ? c.amber : c.purple)
                    .setMetric("", FormatUtils.fmtMemMb(g.memMb), ramRatio, null, null);

            if (!state.allowProcessKill) {
                rh.killBtn.setVisibility(View.GONE);
                rh.lockView.setVisibility(View.GONE);
            } else if (g.locked) {
                rh.killBtn.setVisibility(View.GONE);
                rh.lockView.setVisibility(View.VISIBLE);
            } else {
                rh.lockView.setVisibility(View.GONE);
                rh.killBtn.setVisibility(View.VISIBLE);
                boolean isArmed = g.name.equals(armedKillName) && nowMs < armedKillUntilMs;
                rh.killBtn.setButton(isArmed ? "Fechar?" : "X",
                        isArmed ? ActionButtonView.VARIANT_DANGER : ActionButtonView.VARIANT_NORMAL);
            }
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        this.portrait = portrait;
        processSplit.setPortrait(portrait);
        kpiGrid.setPortrait(portrait);
        ThemeDimens d = AppTheme.get().dimens();
        topContainer.setOrientation(portrait ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        topContainer.setGravity(portrait ? Gravity.LEFT : Gravity.CENTER_VERTICAL);
        topSpacer.setVisibility(portrait ? View.GONE : View.VISIBLE);

        LinearLayout.LayoutParams brLp = (LinearLayout.LayoutParams) topBtnsRow.getLayoutParams();
        if (brLp != null) {
            brLp.width = portrait ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT;
            brLp.topMargin = portrait ? d.dp(6) : 0;
            topBtnsRow.setLayoutParams(brLp);
        }
        for (int i = 0; i < topBtnsRow.getChildCount(); i++) {
            View ch = topBtnsRow.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ch.getLayoutParams();
            if (lp != null) {
                lp.width = portrait ? 0 : LinearLayout.LayoutParams.WRAP_CONTENT;
                lp.weight = portrait ? 1f : 0f;
                ch.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void setActive(boolean visible) {
        if (!visible) {
            armedKillName = "";
            armedKillUntilMs = 0L;
        }
    }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        topBarCard.applyTheme();
        metaPill.applyTheme();
        sortRamBtn.applyTheme();
        sortCpuBtn.applyTheme();
        limitBtn.applyTheme();
        refreshBtn.applyTheme();
        kpiCpuCard.applyTheme();
        kpiCpuBar.applyTheme();
        kpiRamCard.applyTheme();
        kpiRamBar.applyTheme();
        kpiSysCard.applyTheme();
        kpiSysBar.applyTheme();
        kpiHeavyCard.applyTheme();
        kpiHeavyBar.applyTheme();
        listCard.applyTheme();
        hdrApp.setTextColor(c.textMuted);
        hdrCpu.setTextColor(c.textMuted);
        hdrRam.setTextColor(c.textMuted);
        hdrAct.setTextColor(c.textMuted);
        emptyView.setTextColor(c.textMuted);
        for (int i = 0; i < rowPool.size(); i++) {
            RowHolder rh = rowPool.get(i);
            rh.icoBox.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.dp(6)));
            rh.monoView.setTextColor(c.accent);
            rh.nameView.setTextColor(c.textPrimary);
            rh.cpuBar.applyTheme();
            rh.ramBar.applyTheme();
            rh.killBtn.applyTheme();
            rh.lockView.setTextColor(c.textMuted);
        }
    }
}
