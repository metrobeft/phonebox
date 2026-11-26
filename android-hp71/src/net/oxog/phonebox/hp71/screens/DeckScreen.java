package net.oxog.phonebox.hp71.screens;

import android.widget.LinearLayout;

import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.widgets.AudioMixerWidget;
import net.oxog.phonebox.hp71.widgets.DashboardWidget;
import net.oxog.phonebox.hp71.widgets.ShortcutGridWidget;

/**
 * Aba 2 (Atalhos): compõe Stream Deck somente-ícones + Mixer por Aplicativo do Windows.
 */
public final class DeckScreen extends BaseScreen {

    public DeckScreen(DashboardWidget.WidgetContext wctx) {
        super(wctx.context);
        ThemeDimens d = AppTheme.get().dimens();

        ShortcutGridWidget grid = register(new ShortcutGridWidget(wctx));
        LinearLayout.LayoutParams gLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gLp.bottomMargin = d.spaceLg;
        content.addView(grid.getView(), gLp);

        AudioMixerWidget mixer = register(new AudioMixerWidget(wctx));
        content.addView(mixer.getView());
    }
}

