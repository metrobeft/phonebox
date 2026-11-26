package net.oxog.phonebox.hp71.screens;

import android.widget.LinearLayout;

import net.oxog.phonebox.hp71.layouts.SplitColumnLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.widgets.DashboardWidget;
import net.oxog.phonebox.hp71.widgets.DiskListWidget;
import net.oxog.phonebox.hp71.widgets.HardwareSpecsWidget;
import net.oxog.phonebox.hp71.widgets.SystemChartsWidget;

/**
 * Aba 1 (Sistema): compõe SystemChartsWidget no topo + SplitColumn(DiskList, HardwareSpecs) abaixo.
 */
public final class SystemScreen extends BaseScreen {

    private final SplitColumnLayout bottomSplit;

    public SystemScreen(DashboardWidget.WidgetContext wctx) {
        super(wctx.context);
        ThemeDimens d = AppTheme.get().dimens();

        SystemChartsWidget charts = register(new SystemChartsWidget(wctx));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cLp.bottomMargin = d.spaceSm;
        content.addView(charts.getView(), cLp);

        DiskListWidget disks = register(new DiskListWidget(wctx));
        HardwareSpecsWidget specs = register(new HardwareSpecsWidget(wctx));
        bottomSplit = new SplitColumnLayout(wctx.context).setWeights(0.48f, 0.52f).setGapPx(d.spaceSm);
        bottomSplit.setPanes(disks.getView(), specs.getView());
        content.addView(bottomSplit);
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        bottomSplit.setPortrait(portrait);
        super.onOrientationChanged(portrait);
    }
}

