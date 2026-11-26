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
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Widget modular de Cotas de IA (Claude, Antigravity e Codex).
 * Atualizado automaticamente a cada 5s pelo SyncManager EXCLUSIVAMENTE enquanto a aba IA estiver aberta.
 */
public final class AiQuotasWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final CardViewContainer card;
    private final ActionButtonView refreshBtn;
    private final LinearLayout listBox;
    private String lastSignature = "";

    public AiQuotasWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        card = new CardViewContainer(wctx.context).setHeader("COTAS DE IA");
        refreshBtn = new ActionButtonView(wctx.context).setCompact().setButton("Atualizar", ActionButtonView.VARIANT_NORMAL);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.requestImmediateSync(true); }
        });
        card.setHeaderRight(refreshBtn);

        listBox = new LinearLayout(wctx.context);
        listBox.setOrientation(LinearLayout.VERTICAL);
        card.addBodyView(listBox);

        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();

        if (state.quotasUpdatedAt > 0L) {
            card.setHeaderMeta(FormatUtils.fmtTimeOnly(state.quotasUpdatedAt) + " (5s)");
        } else {
            card.setHeaderMeta("5s ao vivo");
        }

        List<AppState.QuotaProvider> provs = new ArrayList<AppState.QuotaProvider>();
        for (int i = 0; i < state.quotaProviders.size(); i++) {
            AppState.QuotaProvider p = state.quotaProviders.get(i);
            if (state.quotaFilterProviders.isEmpty() || state.quotaFilterProviders.contains(p.id)) {
                provs.add(p);
            }
        }
        if (provs.isEmpty()) provs.addAll(state.quotaProviders);

        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < provs.size(); i++) {
            AppState.QuotaProvider p = provs.get(i);
            sig.append(p.id).append('|').append(p.plan).append('|').append(p.extra).append('|').append(p.error).append(':');
            for (int j = 0; j < p.limits.size(); j++) {
                AppState.QuotaLimit l = p.limits.get(j);
                sig.append(l.label).append('=').append(Math.round(l.percent * 10f)).append('/').append(l.resetsAt / 60000L).append('/').append(l.resetDescription).append(',');
            }
            sig.append(';');
        }
        String nextSig = sig.toString();
        if (nextSig.equals(lastSignature) && listBox.getChildCount() > 0) return;
        lastSignature = nextSig;

        listBox.removeAllViews();
        if (provs.isEmpty()) {
            TextView empty = new TextView(wctx.context);
            empty.setText("Verificando cotas de IA...");
            empty.setTextColor(c.textMuted);
            listBox.addView(empty);
            return;
        }

        for (int i = 0; i < provs.size(); i++) {
            AppState.QuotaProvider p = provs.get(i);
            LinearLayout box = new LinearLayout(wctx.context);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(d.dp(10), d.dp(8), d.dp(10), d.dp(8));
            box.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.controlRadius));
            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) bLp.topMargin = d.dp(8);

            LinearLayout head = new LinearLayout(wctx.context);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            TextView name = new TextView(wctx.context);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            name.setTextColor(c.textPrimary);
            name.setText(p.name);
            head.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            if (p.plan.length() > 0 && !p.plan.equals(p.name)) {
                StatusPillView pill = new StatusPillView(wctx.context).setPill(p.plan, StatusPillView.STYLE_ACCENT);
                head.addView(pill);
            }
            box.addView(head);

            Map<String, Integer> dupMap = new HashMap<String, Integer>();
            for (int j = 0; j < p.limits.size(); j++) {
                AppState.QuotaLimit l = p.limits.get(j);
                Integer prev = dupMap.get(l.label);
                int cnt = (prev == null ? 0 : prev.intValue()) + 1;
                dupMap.put(l.label, Integer.valueOf(cnt));
                String lbl = FormatUtils.translateLimitLabel(l.label, cnt);

                float used = Math.max(0f, Math.min(100f, l.percent));
                float rem = Math.max(0f, Math.round((100f - used) * 10f) / 10f);
                String resetTxt = l.resetDescription.length() > 0 ? l.resetDescription : FormatUtils.fmtResetTime(l.resetsAt);

                MetricProgressBarView bar = new MetricProgressBarView(wctx.context).setThresholds(80f, 95f).setBarHeightDp(7);
                if ("critical".equals(l.severity)) bar.setFixedColor(c.red);
                else if ("warning".equals(l.severity)) bar.setFixedColor(c.amber);
                bar.setMetric(lbl, FormatUtils.round1(used) + "% usado", used,
                        "Disponivel: " + FormatUtils.round1(rem) + "%", resetTxt);
                LinearLayout.LayoutParams lpBar = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lpBar.topMargin = d.dp(7);
                box.addView(bar, lpBar);
            }

            if (p.extra.length() > 0) {
                TextView ex = new TextView(wctx.context);
                ex.setText(p.extra);
                ex.setTextColor(c.textMuted);
                ex.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                LinearLayout.LayoutParams exLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                exLp.topMargin = d.dp(6);
                box.addView(ex, exLp);
            }
            if (p.error.length() > 0) {
                TextView err = new TextView(wctx.context);
                String msg = p.error;
                if (msg.indexOf("No Claude Code sign-in") >= 0) {
                    msg = "Plano detectado. Execute \"claude auth login\" no PC para ler a cota direta do Claude Code CLI.";
                }
                err.setText(msg);
                err.setTextColor(c.amber);
                err.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                LinearLayout.LayoutParams erLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                erLp.topMargin = d.dp(6);
                box.addView(err, erLp);
            }
            listBox.addView(box, bLp);
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        lastSignature = "";
        card.applyTheme();
        refreshBtn.applyTheme();
    }
}

