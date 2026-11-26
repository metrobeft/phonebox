package net.oxog.phonebox.hp71.screens;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeDimens;
import net.oxog.phonebox.hp71.widgets.DashboardWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Base modular de todas as abas (Screens).
 * Uma Screen atua exclusivamente como compositora de layout dos seus DashboardWidgets.
 */
public abstract class BaseScreen {

    protected final ScrollView scrollView;
    protected final LinearLayout content;
    private final List<DashboardWidget> widgets = new ArrayList<DashboardWidget>();
    private boolean active = false;

    protected int viewportWidthPx = 0;
    protected int viewportHeightPx = 0;
    protected boolean viewportPortrait = false;

    protected BaseScreen(Context ctx) {
        ThemeDimens d = AppTheme.get().dimens();
        scrollView = new ScrollView(ctx);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.setScrollbarFadingEnabled(true);

        content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(d.spaceLg, d.spaceSm, d.spaceLg, d.spaceLg);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
    }

    public void onViewportChanged(int widthPx, int heightPx, boolean portrait) {
        this.viewportWidthPx = widthPx;
        this.viewportHeightPx = heightPx;
        this.viewportPortrait = portrait;
        onOrientationChanged(portrait);
    }

    protected <T extends DashboardWidget> T register(T widget) {
        widgets.add(widget);
        return widget;
    }

    public View getView() {
        return scrollView;
    }

    public void scrollToTop() {
        scrollView.scrollTo(0, 0);
    }

    public void bindData(AppState state) {
        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).bindData(state);
        }
    }

    public void onOrientationChanged(boolean portrait) {
        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).onOrientationChanged(portrait);
        }
    }

    public void setActive(boolean visible) {
        this.active = visible;
        scrollView.setVisibility(visible ? View.VISIBLE : View.GONE);
        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).setActive(visible);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void applyTheme() {
        AppTheme t = AppTheme.get();
        // Nos temas Hack e Anime, mantém o ScrollView transparente para revelar o fundo animado full-screen atrás dos cards
        scrollView.setBackgroundColor((t.isHack() || t.isAnime()) ? 0x00000000 : t.colors().bg);
        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).applyTheme();
        }
    }

    public void tickClock() { }
}

