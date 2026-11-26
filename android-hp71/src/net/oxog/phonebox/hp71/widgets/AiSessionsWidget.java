package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Widget modular de Sessões de IA (Claude Code, Codex, Antigravity) e GitHub PRs/Reviews.
 */
public final class AiSessionsWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final CardViewContainer card;
    private final LinearLayout listBox;
    private String lastSignature = "";

    public AiSessionsWidget(WidgetContext wctx) {
        this.wctx = wctx;
        card = new CardViewContainer(wctx.context).setHeader("SESSOES DE IA & GITHUB");
        listBox = new LinearLayout(wctx.context);
        listBox.setOrientation(LinearLayout.VERTICAL);
        card.addBodyView(listBox);
        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < state.aiSessions.size(); i++) {
            AppState.AiSession s = state.aiSessions.get(i);
            sig.append(s.id).append('|').append(s.title).append('|').append(s.status).append('|').append(s.lastTool).append('|').append(s.totalTokens).append(';');
        }
        sig.append("#GH:").append(state.githubUser).append('|').append(state.githubReviewCount).append('|').append(state.githubPrs.size());
        for (int i = 0; i < state.githubPrs.size(); i++) {
            AppState.GithubPr pr = state.githubPrs.get(i);
            sig.append('|').append(pr.repo).append('#').append(pr.number).append(pr.title);
        }
        String nextSig = sig.toString();
        if (nextSig.equals(lastSignature) && listBox.getChildCount() > 0) return;
        lastSignature = nextSig;

        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        listBox.removeAllViews();

        if (state.aiSessions.isEmpty()) {
            TextView empty = new TextView(wctx.context);
            empty.setText("Nenhuma sessao ativa de Claude Code, Codex ou Antigravity.");
            empty.setTextColor(c.textMuted);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            listBox.addView(empty);
        } else {
            for (int i = 0; i < state.aiSessions.size(); i++) {
                AppState.AiSession s = state.aiSessions.get(i);
                LinearLayout row = new LinearLayout(wctx.context);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, d.dp(6), 0, d.dp(6));

                LinearLayout top = new LinearLayout(wctx.context);
                top.setOrientation(LinearLayout.HORIZONTAL);
                top.setGravity(Gravity.CENTER_VERTICAL);

                TextView title = new TextView(wctx.context);
                title.setTypeface(Typeface.DEFAULT_BOLD);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
                title.setTextColor(c.textPrimary);
                title.setSingleLine(true);
                title.setEllipsize(TextUtils.TruncateAt.END);
                title.setText(s.title);
                top.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                int stStyle = "busy".equals(s.status) ? StatusPillView.STYLE_SUCCESS
                        : ("waiting".equals(s.status) ? StatusPillView.STYLE_WARN : StatusPillView.STYLE_NEUTRAL);
                String stTxt = "busy".equals(s.status) ? "EM EXECUCAO"
                        : ("waiting".equals(s.status) ? "AGUARDANDO" : "CONCLUIDO");
                StatusPillView pill = new StatusPillView(wctx.context).setPill(stTxt, stStyle);
                LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                pLp.leftMargin = d.dp(6);
                top.addView(pill, pLp);
                row.addView(top);

                StringBuilder meta = new StringBuilder(s.tool.toUpperCase());
                if (s.model.length() > 0) meta.append(" · ").append(s.model);
                if (s.lastTool.length() > 0) meta.append(" · tool: ").append(s.lastTool);
                String tok = FormatUtils.fmtTokens(s.totalTokens);
                if (tok.length() > 0) meta.append(" · ").append(tok);

                TextView sub = new TextView(wctx.context);
                sub.setText(meta.toString());
                sub.setTextColor(c.textMuted);
                sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                sub.setSingleLine(true);
                sub.setEllipsize(TextUtils.TruncateAt.END);
                row.addView(sub);

                listBox.addView(row);
                if (i < state.aiSessions.size() - 1) {
                    View sep = new View(wctx.context);
                    sep.setBackgroundColor(c.divider);
                    listBox.addView(sep, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, d.strokePx));
                }
            }
        }

        if (state.githubUser.length() > 0) {
            View sep = new View(wctx.context);
            sep.setBackgroundColor(c.border);
            LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, d.strokePx);
            sLp.topMargin = d.dp(10);
            sLp.bottomMargin = d.dp(8);
            listBox.addView(sep, sLp);

            TextView ghHead = new TextView(wctx.context);
            ghHead.setTypeface(Typeface.DEFAULT_BOLD);
            ghHead.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            ghHead.setTextColor(c.textMuted);
            ghHead.setText("GITHUB (@" + state.githubUser + ") · " + state.githubPrs.size() + " PRs · " + state.githubReviewCount + " Reviews");
            listBox.addView(ghHead);

            for (int i = 0; i < state.githubPrs.size(); i++) {
                AppState.GithubPr pr = state.githubPrs.get(i);
                TextView p1 = new TextView(wctx.context);
                p1.setTypeface(Typeface.DEFAULT_BOLD);
                p1.setTextColor(c.textPrimary);
                p1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                p1.setText(pr.repo + " #" + pr.number);
                p1.setPadding(0, d.dp(5), 0, 0);
                listBox.addView(p1);

                TextView p2 = new TextView(wctx.context);
                p2.setTextColor(c.textMuted);
                p2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
                p2.setSingleLine(true);
                p2.setEllipsize(TextUtils.TruncateAt.END);
                p2.setText(pr.title);
                listBox.addView(p2);
            }
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
    }
}

