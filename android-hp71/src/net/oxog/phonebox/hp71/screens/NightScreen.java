package net.oxog.phonebox.hp71.screens;

import net.oxog.phonebox.hp71.widgets.DashboardWidget;
import net.oxog.phonebox.hp71.widgets.NightStandClockWidget;

/**
 * Aba 6 (Clima/Hora & Relógio de Mesa): compõe NightStandClockWidget (3 sub-abas + brilho hardware).
 */
public final class NightScreen extends BaseScreen {

    private final NightStandClockWidget clockWidget;

    public NightScreen(DashboardWidget.WidgetContext wctx) {
        super(wctx.context);
        clockWidget = register(new NightStandClockWidget(wctx));
        content.addView(clockWidget.getView());
    }

    @Override
    public void tickClock() {
        clockWidget.tickClock();
    }
}

