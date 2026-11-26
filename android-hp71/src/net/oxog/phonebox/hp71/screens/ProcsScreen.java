package net.oxog.phonebox.hp71.screens;

import net.oxog.phonebox.hp71.widgets.DashboardWidget;
import net.oxog.phonebox.hp71.widgets.ProcessTableWidget;

/**
 * Aba 3 (Processos): compõe ProcessTableWidget (4 KPIs + lista com ícones e barras CPU/RAM).
 */
public final class ProcsScreen extends BaseScreen {

    public ProcsScreen(DashboardWidget.WidgetContext wctx) {
        super(wctx.context);
        ProcessTableWidget table = register(new ProcessTableWidget(wctx));
        content.addView(table.getView());
    }
}

