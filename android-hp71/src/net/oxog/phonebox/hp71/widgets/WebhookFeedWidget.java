package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Widget modular de Mensagens / Webhooks recebidos via POST /api/push/{key}.
 */
public final class WebhookFeedWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final CardViewContainer card;
    private final LinearLayout listBox;
    private String lastSignature = "";

    public WebhookFeedWidget(WidgetContext wctx) {
        this.wctx = wctx;
        card = new CardViewContainer(wctx.context).setHeader("MENSAGENS / WEBHOOKS");
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
        for (int i = 0; i < state.pushEntries.size(); i++) {
            AppState.PushEntry pe = state.pushEntries.get(i);
            sig.append(pe.key).append('=').append(pe.value).append('|').append(pe.sub).append('|').append(pe.at).append(';');
        }
        String nextSig = sig.toString();
        if (nextSig.equals(lastSignature) && listBox.getChildCount() > 0) return;
        lastSignature = nextSig;

        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        listBox.removeAllViews();

        if (state.pushEntries.isEmpty()) {
            TextView empty = new TextView(wctx.context);
            empty.setText("Nenhum valor recebido via webhook.");
            empty.setTextColor(c.textMuted);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            listBox.addView(empty);
            return;
        }

        for (int i = 0; i < state.pushEntries.size(); i++) {
            AppState.PushEntry pe = state.pushEntries.get(i);
            LinearLayout row = new LinearLayout(wctx.context);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, d.dp(5), 0, d.dp(5));

            LinearLayout top = new LinearLayout(wctx.context);
            top.setOrientation(LinearLayout.HORIZONTAL);
            TextView k = new TextView(wctx.context);
            k.setTypeface(Typeface.DEFAULT_BOLD);
            k.setTextColor(c.textPrimary);
            k.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            k.setText(pe.key);
            top.addView(k, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView v = new TextView(wctx.context);
            v.setTypeface(Typeface.DEFAULT_BOLD);
            v.setTextColor(c.accent);
            v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            v.setGravity(Gravity.RIGHT);
            v.setText(pe.value);
            top.addView(v);
            row.addView(top);

            String meta = pe.sub;
            if (pe.at > 0L) {
                String t = FormatUtils.fmtTimeOnly(pe.at);
                meta = (meta.length() > 0) ? (meta + " · " + t) : t;
            }
            if (meta.length() > 0) {
                TextView sub = new TextView(wctx.context);
                sub.setText(meta);
                sub.setTextColor(c.textMuted);
                sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                row.addView(sub);
            }

            listBox.addView(row);
            if (i < state.pushEntries.size() - 1) {
                View sep = new View(wctx.context);
                sep.setBackgroundColor(c.divider);
                listBox.addView(sep, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, d.strokePx));
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

