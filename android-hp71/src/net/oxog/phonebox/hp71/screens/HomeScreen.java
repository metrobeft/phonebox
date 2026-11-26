package net.oxog.phonebox.hp71.screens;

import android.view.View;
import android.widget.LinearLayout;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.layouts.ResponsiveGridLayout;
import net.oxog.phonebox.hp71.layouts.SplitColumnLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.widgets.ClockWeatherWidget;
import net.oxog.phonebox.hp71.widgets.CpuCardWidget;
import net.oxog.phonebox.hp71.widgets.DashboardWidget;
import net.oxog.phonebox.hp71.widgets.GpuCardWidget;
import net.oxog.phonebox.hp71.widgets.MasterVolumeWidget;
import net.oxog.phonebox.hp71.widgets.MediaControlWidget;
import net.oxog.phonebox.hp71.widgets.NetPrivacyCardWidget;
import net.oxog.phonebox.hp71.widgets.RamCardWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Aba 0 (Início): compõe ClockWeather + (Media + MasterVolume) no topo e 4 cards de telemetria abaixo.
 */
public final class HomeScreen extends BaseScreen {

    private final SplitColumnLayout topSplit;
    private final CardViewContainer mediaVolCard;
    private final View volDivider;
    private final ResponsiveGridLayout metricsGrid;
    private final ClockWeatherWidget clockWidget;

    public HomeScreen(DashboardWidget.WidgetContext wctx) {
        super(wctx.context);
        ThemeDimens d = AppTheme.get().dimens();

        clockWidget = register(new ClockWeatherWidget(wctx));
        MediaControlWidget mediaWidget = register(new MediaControlWidget(wctx));
        MasterVolumeWidget volWidget = register(new MasterVolumeWidget(wctx));

        mediaVolCard = new CardViewContainer(wctx.context);
        mediaVolCard.addBodyView(mediaWidget.getView());
        volDivider = new View(wctx.context);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.strokePx);
        divLp.topMargin = d.dp(10);
        divLp.bottomMargin = d.dp(8);
        mediaVolCard.addBodyView(volDivider, divLp);
        mediaVolCard.addBodyView(volWidget.getView());

        topSplit = new SplitColumnLayout(wctx.context).setWeights(0.55f, 0.45f).setGapPx(d.spaceSm);
        topSplit.setPanes(clockWidget.getView(), mediaVolCard);
        LinearLayout.LayoutParams topLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topLp.bottomMargin = d.spaceLg;
        content.addView(topSplit, topLp);

        CpuCardWidget cpu = register(new CpuCardWidget(wctx));
        GpuCardWidget gpu = register(new GpuCardWidget(wctx));
        RamCardWidget ram = register(new RamCardWidget(wctx));
        NetPrivacyCardWidget net = register(new NetPrivacyCardWidget(wctx));

        metricsGrid = new ResponsiveGridLayout(wctx.context).setColumns(4, 2).setGapPx(d.spaceSm);
        List<View> tiles = new ArrayList<View>(4);
        tiles.add(cpu.getView());
        tiles.add(gpu.getView());
        tiles.add(ram.getView());
        tiles.add(net.getView());
        metricsGrid.setItems(tiles);
        content.addView(metricsGrid);

        applyTheme();
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        topSplit.setPortrait(portrait);
        metricsGrid.setPortrait(portrait);
        super.onOrientationChanged(portrait);
    }

    @Override
    public void applyTheme() {
        super.applyTheme();
        mediaVolCard.applyTheme();
        volDivider.setBackgroundColor(AppTheme.get().colors().divider);
    }

    @Override
    public void tickClock() {
        clockWidget.tickClock();
    }
}

