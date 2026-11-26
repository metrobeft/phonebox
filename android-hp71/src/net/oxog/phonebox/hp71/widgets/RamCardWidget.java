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
 * Card modular de Memória RAM (%, usada/total GB e livre GB).
 */
public final class RamCardWidget implements DashboardWidget {

    private final CardViewContainer card;
    private final TextView valView;
    private final MetricProgressBarView bar;
    private final TextView usedView;
    private final TextView totView;
    private final TextView subView;

    public RamCardWidget(final WidgetContext wctx) {
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
        card.setHeader("MEMORIA RAM").setHeaderRight(valView);

        bar = new MetricProgressBarView(wctx.context).setBarHeightDp(6);
        card.addBodyView(bar);

        LinearLayout kv = new LinearLayout(wctx.context);
        kv.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams kvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        kvLp.topMargin = d.dp(10);
        kvLp.bottomMargin = d.dp(6);

        usedView = new TextView(wctx.context);
        usedView.setTypeface(Typeface.DEFAULT_BOLD);
        usedView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        usedView.setText("-- GB");
        kv.addView(usedView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        totView = new TextView(wctx.context);
        totView.setTypeface(Typeface.DEFAULT_BOLD);
        totView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        totView.setGravity(Gravity.RIGHT);
        totView.setText("/ -- GB");
        kv.addView(totView);
        card.addBodyView(kv, kvLp);

        subView = new TextView(wctx.context);
        subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        subView.setSingleLine(true);
        subView.setEllipsize(TextUtils.TruncateAt.END);
        subView.setText("Livre: -- GB");
        card.addBodyView(subView);

        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        int pct = state.pc.ramPct();
        valView.setText(pct + "%");
        bar.setMetric("", "", pct, null, null);
        usedView.setText(FormatUtils.round1(state.pc.ramUsedGb) + " GB");
        totView.setText("/ " + FormatUtils.round1(state.pc.ramTotalGb) + " GB");
        float free = Math.max(0f, state.pc.ramTotalGb - state.pc.ramUsedGb);
        subView.setText("Livre: " + FormatUtils.round1(free) + " GB");
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        card.applyTheme();
        valView.setTextColor(c.purple);
        bar.setFixedColor(c.purple).applyTheme();
        usedView.setTextColor(c.textPrimary);
        totView.setTextColor(c.textSecondary);
        subView.setTextColor(c.textMuted);
    }
}

