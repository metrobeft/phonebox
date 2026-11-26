package net.oxog.phonebox.hp71.screens;

import net.oxog.phonebox.hp71.layouts.SplitColumnLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.widgets.DashboardWidget;
import net.oxog.phonebox.hp71.widgets.DiscordCallWidget;
import net.oxog.phonebox.hp71.widgets.DiscordVoiceDirectoryWidget;

/**
 * Aba 5 (Discord): compõe DiscordCallWidget à esquerda e DiscordVoiceDirectoryWidget à direita,
 * repassando a altura real da viewport (já descontada a barra preta fixa de 48dp do Android 4.2.2)
 * para manter listas enormes estritamente contidas dentro da tela disponível.
 */
public final class DiscordScreen extends BaseScreen {

    private final SplitColumnLayout split;
    private final DiscordCallWidget callWidget;
    private final DiscordVoiceDirectoryWidget dirWidget;

    public DiscordScreen(DashboardWidget.WidgetContext wctx) {
        super(wctx.context);
        ThemeDimens d = AppTheme.get().dimens();

        callWidget = register(new DiscordCallWidget(wctx));
        dirWidget = register(new DiscordVoiceDirectoryWidget(wctx));

        split = new SplitColumnLayout(wctx.context)
                .setMatchHeightHorizontal(false)
                .setWeights(0.39f, 0.61f)
                .setGapPx(d.spaceLg);
        split.setPanes(callWidget.getView(), dirWidget.getView());
        content.addView(split);
    }

    @Override
    public void onViewportChanged(int widthPx, int heightPx, boolean portrait) {
        super.onViewportChanged(widthPx, heightPx, portrait);
        callWidget.updateMaxListHeight(heightPx, portrait);
        dirWidget.updateMaxListHeight(heightPx, portrait);
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        split.setPortrait(portrait);
        super.onOrientationChanged(portrait);
    }
}
