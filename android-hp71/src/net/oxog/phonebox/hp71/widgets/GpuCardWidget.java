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
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Card modular de GPU (utilização, temperatura colorida por faixa, potência W e VRAM).
 */
public final class GpuCardWidget implements DashboardWidget {

    private final CardViewContainer card;
    private final TextView valView;
    private final MetricProgressBarView bar;
    private final TextView tempView;
    private final TextView powerView;
    private final TextView subView;

    public GpuCardWidget(final WidgetContext wctx) {
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
        valView.setText("--%");
        card.setHeader("GPU").setHeaderRight(valView);

        bar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        card.addBodyView(bar);

        LinearLayout kv = new LinearLayout(wctx.context);
        kv.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams kvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        kvLp.topMargin = d.dp(10);
        kvLp.bottomMargin = d.dp(6);

        tempView = new TextView(wctx.context);
        tempView.setTypeface(Typeface.DEFAULT_BOLD);
        tempView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        tempView.setText("--°C");
        kv.addView(tempView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        powerView = new TextView(wctx.context);
        powerView.setTypeface(Typeface.DEFAULT_BOLD);
        powerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        powerView.setGravity(Gravity.RIGHT);
        powerView.setText("-- W");
        kv.addView(powerView);
        card.addBodyView(kv, kvLp);

        subView = new TextView(wctx.context);
        subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        subView.setSingleLine(true);
        subView.setEllipsize(TextUtils.TruncateAt.END);
        subView.setText("VRAM --");
        card.addBodyView(subView);

        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        ThemeColors c = AppTheme.get().colors();
        AppState.PcStats pc = state.pc;
        if (!pc.hasGpu) {
            valView.setText("--");
            bar.setMetric("", "", 0f, null, null);
            tempView.setText("--°C");
            powerView.setText("-- W");
            subView.setText("Sem GPU detectada");
            return;
        }
        int util = Math.round(pc.gpuUtil);
        valView.setText(util + "%");
        bar.setMetric("", "", util, null, null);
        if (pc.hasGpuTemp) {
            int t = Math.round(pc.gpuTemp);
            tempView.setText(t + "°C");
            tempView.setTextColor(t >= 80 ? c.red : (t >= 68 ? c.amber : c.green));
        } else {
            tempView.setText("--°C");
            tempView.setTextColor(c.textSecondary);
        }
        powerView.setText(Math.round(pc.gpuPower) + " W");
        if (pc.gpuVramTotal > 0f) {
            subView.setText("VRAM " + FormatUtils.round1(pc.gpuVramUsed) + " / " + FormatUtils.round1(pc.gpuVramTotal) + " GB");
        } else {
            subView.setText(pc.gpuName);
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        card.applyTheme();
        valView.setTextColor(c.green);
        bar.setFixedColor(c.green).applyTheme();
        powerView.setTextColor(c.textPrimary);
        subView.setTextColor(c.textMuted);
    }
}

