package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.MetricProgressBarView;
import net.oxog.phonebox.hp71.components.NativeLineChartView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Card modular de CPU com barra + sparkline nativo de 60 amostras.
 */
public final class CpuCardWidget implements DashboardWidget {

    private final CardViewContainer card;
    private final TextView valView;
    private final MetricProgressBarView bar;
    private final NativeLineChartView spark;
    private final TextView subView;

    public CpuCardWidget(final WidgetContext wctx) {
        ThemeDimens d = AppTheme.get().dimens();
        card = new CardViewContainer(wctx.context);
        card.setClickable(true);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { if (wctx.host != null) wctx.host.navigateToView(1); }
        });

        valView = new TextView(wctx.context);
        valView.setTypeface(Typeface.DEFAULT_BOLD);
        valView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
        valView.setGravity(Gravity.RIGHT);
        valView.setText("--%");
        card.setHeader("CPU").setHeaderRight(valView);

        bar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        bar.setMetric("", "", 0f, null, null);
        card.addBodyView(bar);

        spark = new NativeLineChartView(wctx.context).setSparklineMode(true);
        LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.dp(42));
        spLp.topMargin = d.dp(4);
        spLp.bottomMargin = d.dp(4);
        card.addBodyView(spark, spLp);

        subView = new TextView(wctx.context);
        subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        subView.setSingleLine(true);
        subView.setEllipsize(TextUtils.TruncateAt.END);
        subView.setText("--");
        card.addBodyView(subView);

        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        int pct = Math.round(state.pc.cpuPct);
        valView.setText(pct + "%");
        bar.setMetric("", "", pct, null, null);
        spark.setHistories(state.histCpu, null);
        String temp = state.pc.cpuTemp > 1f ? (Math.round(state.pc.cpuTemp) + "°C · ") : "";
        subView.setText(temp + state.pc.cpuName);
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        card.applyTheme();
        valView.setTextColor(c.accent);
        bar.setFixedColor(c.accent).applyTheme();
        spark.setColors(c.accent, c.purple).applyTheme();
        subView.setTextColor(c.textMuted);
    }
}

