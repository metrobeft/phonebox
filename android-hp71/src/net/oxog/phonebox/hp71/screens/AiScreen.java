package net.oxog.phonebox.hp71.screens;

import android.widget.LinearLayout;

import net.oxog.phonebox.hp71.layouts.SplitColumnLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.widgets.AiQuotasWidget;
import net.oxog.phonebox.hp71.widgets.AiSessionsWidget;
import net.oxog.phonebox.hp71.widgets.DashboardWidget;
import net.oxog.phonebox.hp71.widgets.WebhookFeedWidget;

/**
 * Aba 4 (IA): mostra cotas ao lado da atividade recente em paisagem e empilha em retrato.
 */
public final class AiScreen extends BaseScreen {

    private final SplitColumnLayout split;

    public AiScreen(DashboardWidget.WidgetContext wctx) {
        super(wctx.context);
        ThemeDimens d = AppTheme.get().dimens();

        LinearLayout activityCol = new LinearLayout(wctx.context);
        activityCol.setOrientation(LinearLayout.VERTICAL);

        AiQuotasWidget quotas = register(new AiQuotasWidget(wctx));
        LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        qLp.bottomMargin = d.spaceSm;
        AiSessionsWidget sessions = register(new AiSessionsWidget(wctx));
        activityCol.addView(sessions.getView(), qLp);
        WebhookFeedWidget webhooks = register(new WebhookFeedWidget(wctx));
        activityCol.addView(webhooks.getView());

        split = new SplitColumnLayout(wctx.context).setMatchHeightHorizontal(false).setWeights(0.43f, 0.57f).setGapPx(d.spaceSm);
        split.setPanes(quotas.getView(), activityCol);
        content.addView(split);
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        split.setPortrait(portrait);
        super.onOrientationChanged(portrait);
    }
}

