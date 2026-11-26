package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.text.TextUtils;
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
 * Card modular de Rede, Ping e Privacidade (Microfone / Câmera ativos).
 */
public final class NetPrivacyCardWidget implements DashboardWidget {

    private final CardViewContainer card;
    private final TextView pingView;
    private final TextView downTag;
    private final TextView downVal;
    private final TextView upTag;
    private final TextView upVal;
    private final TextView privView;

    public NetPrivacyCardWidget(final WidgetContext wctx) {
        ThemeDimens d = AppTheme.get().dimens();
        card = new CardViewContainer(wctx.context);
        card.setClickable(true);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { if (wctx.host != null) wctx.host.navigateToView(1); }
        });

        pingView = new TextView(wctx.context);
        pingView.setTypeface(Typeface.DEFAULT_BOLD);
        pingView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
        pingView.setText("-- ms");
        card.setHeader("REDE & PING").setHeaderRight(pingView);

        LinearLayout rowDown = new LinearLayout(wctx.context);
        rowDown.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rdLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rdLp.topMargin = d.dp(8);
        downTag = new TextView(wctx.context);
        downTag.setTypeface(Typeface.DEFAULT_BOLD);
        downTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        downTag.setText("DOWN");
        rowDown.addView(downTag, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        downVal = new TextView(wctx.context);
        downVal.setTypeface(Typeface.DEFAULT_BOLD);
        downVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        downVal.setGravity(Gravity.RIGHT);
        downVal.setText("--");
        rowDown.addView(downVal);
        card.addBodyView(rowDown, rdLp);

        LinearLayout rowUp = new LinearLayout(wctx.context);
        rowUp.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams ruLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ruLp.topMargin = d.dp(6);
        ruLp.bottomMargin = d.dp(10);
        upTag = new TextView(wctx.context);
        upTag.setTypeface(Typeface.DEFAULT_BOLD);
        upTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        upTag.setText("UP");
        rowUp.addView(upTag, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        upVal = new TextView(wctx.context);
        upVal.setTypeface(Typeface.DEFAULT_BOLD);
        upVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        upVal.setGravity(Gravity.RIGHT);
        upVal.setText("--");
        rowUp.addView(upVal);
        card.addBodyView(rowUp, ruLp);

        privView = new TextView(wctx.context);
        privView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        privView.setSingleLine(true);
        privView.setEllipsize(TextUtils.TruncateAt.END);
        privView.setText("Microfone / Camera: livres");
        card.addBodyView(privView);

        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        ThemeColors c = AppTheme.get().colors();
        pingView.setText(state.ping.hasMs ? (Math.round(state.ping.ms) + " ms") : "-- ms");
        downVal.setText(FormatUtils.fmtBytesPerSec(state.pc.netDownBps));
        upVal.setText(FormatUtils.fmtBytesPerSec(state.pc.netUpBps));

        StringBuilder p = new StringBuilder();
        if (state.privacy.mic) {
            p.append("MIC");
            if (state.privacy.micApps.length() > 0) p.append(": ").append(state.privacy.micApps);
        }
        if (state.privacy.camera) {
            if (p.length() > 0) p.append(" · ");
            p.append("CAM");
            if (state.privacy.cameraApps.length() > 0) p.append(": ").append(state.privacy.cameraApps);
        }
        if (p.length() > 0) {
            privView.setText(p.toString());
            privView.setTextColor(c.red);
            privView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            privView.setText("Microfone / Camera: livres");
            privView.setTextColor(c.textMuted);
            privView.setTypeface(Typeface.DEFAULT);
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
        pingView.setTextColor(c.sky);
        downTag.setTextColor(c.accent);
        downVal.setTextColor(c.textPrimary);
        upTag.setTextColor(c.amber);
        upVal.setTextColor(c.textPrimary);
    }
}

