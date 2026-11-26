package net.oxog.phonebox.hp71.widgets;

import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.MetricProgressBarView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Widget modular de discos de armazenamento (C:, D:, E:, G:) com MetricProgressBarView.
 */
public final class DiskListWidget implements DashboardWidget {

    private final CardViewContainer card;
    private final LinearLayout listBox;
    private final TextView emptyView;

    public DiskListWidget(WidgetContext wctx) {
        card = new CardViewContainer(wctx.context).setHeader("ARMAZENAMENTO (DISCOS)");
        listBox = new LinearLayout(wctx.context);
        listBox.setOrientation(LinearLayout.VERTICAL);
        card.addBodyView(listBox);

        emptyView = new TextView(wctx.context);
        emptyView.setText("Carregando discos...");
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        listBox.addView(emptyView);

        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        int count = state.pc.disks.size();
        card.setHeaderMeta(count + (count == 1 ? " unidade" : " unidades"));
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();

        if (count == 0) {
            listBox.removeAllViews();
            emptyView.setText("Nenhum disco reportado.");
            emptyView.setTextColor(c.textMuted);
            listBox.addView(emptyView);
            return;
        }

        // Reutiliza views quando a quantidade de discos não muda (evita GC no Cortex-A7)
        boolean needRebuild = listBox.getChildCount() != count || !(listBox.getChildAt(0) instanceof MetricProgressBarView);
        if (needRebuild) {
            listBox.removeAllViews();
            for (int i = 0; i < count; i++) {
                MetricProgressBarView bar = new MetricProgressBarView(card.getContext())
                        .setBarHeightDp(7);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i > 0) lp.topMargin = d.dp(7);
                listBox.addView(bar, lp);
            }
        }

        for (int i = 0; i < count; i++) {
            AppState.DiskInfo dk = state.pc.disks.get(i);
            float tot = Math.max(1f, dk.totalGb);
            float used = Math.max(0f, dk.usedGb);
            int pct = Math.min(100, Math.round((used / tot) * 100f));
            int free = Math.max(0, Math.round(tot - used));
            MetricProgressBarView bar = (MetricProgressBarView) listBox.getChildAt(i);
            int color = pct >= 90 ? c.red : (pct >= 80 ? c.amber : c.sky);
            bar.setFixedColor(color);
            bar.setMetric(
                    dk.name + "  (" + free + " GB livres)",
                    Math.round(used) + " / " + Math.round(tot) + " GB (" + pct + "%)",
                    pct, null, null);
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        card.applyTheme();
        emptyView.setTextColor(AppTheme.get().colors().textMuted);
        for (int i = 0; i < listBox.getChildCount(); i++) {
            View ch = listBox.getChildAt(i);
            if (ch instanceof MetricProgressBarView) ((MetricProgressBarView) ch).applyTheme();
        }
    }
}

