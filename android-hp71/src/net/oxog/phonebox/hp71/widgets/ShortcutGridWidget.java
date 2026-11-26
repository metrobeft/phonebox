package net.oxog.phonebox.hp71.widgets;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.LinearLayout;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.components.TouchActionTileView;
import net.oxog.phonebox.hp71.core.ApiClient;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.core.BitmapCache;
import net.oxog.phonebox.hp71.layouts.ResponsiveGridLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Widget modular Stream Deck somente-ícones (7 colunas na horizontal / 4 na vertical)
 * carregando os ícones oficiais do Windows (/api/shortcuts/icon + fallback /legacy/icons).
 */
public final class ShortcutGridWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final CardViewContainer card;
    private final StatusPillView countPill;
    private final ResponsiveGridLayout grid;
    private final Map<String, TouchActionTileView> tilesById = new HashMap<String, TouchActionTileView>();

    private String lastSignature = "";
    private int lastCols = 7;

    public ShortcutGridWidget(WidgetContext wctx) {
        this.wctx = wctx;
        ThemeDimens d = AppTheme.get().dimens();
        card = new CardViewContainer(wctx.context).setVariant(CardViewContainer.VARIANT_HIGHLIGHT);
        countPill = new StatusPillView(wctx.context).setPill("14 icones", StatusPillView.STYLE_ACCENT);
        card.setHeader("ATALHOS DO PC").setHeaderRight(countPill);

        grid = new ResponsiveGridLayout(wctx.context).setColumns(7, 4).setGapPx(d.dp(8));
        card.addBodyView(grid);
        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        int cols = Math.max(2, Math.min(8, state.shortcutsColumns));
        int portCols = Math.min(4, cols);
        if (cols != lastCols) {
            lastCols = cols;
            grid.setColumns(cols, portCols);
        }

        List<AppState.ShortcutItem> list = state.shortcuts;
        countPill.setPill(list.size() + " ícones · " + cols + " col.", StatusPillView.STYLE_ACCENT);

        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            AppState.ShortcutItem s = list.get(i);
            sig.append(s.id).append('|').append(s.color).append('|').append(s.iconRev).append('|').append(s.hold).append(';');
        }
        String nextSig = sig.toString();
        if (!nextSig.equals(lastSignature)) {
            lastSignature = nextSig;
            rebuildTiles(list);
        }

        // Atualiza estados toggle ao vivo
        for (Map.Entry<String, TouchActionTileView> e : tilesById.entrySet()) {
            Boolean on = state.toggles.get(e.getKey());
            e.getValue().setActiveState(on != null && on.booleanValue());
        }
    }

    private void rebuildTiles(List<AppState.ShortcutItem> list) {
        ThemeDimens d = AppTheme.get().dimens();
        tilesById.clear();
        List<View> views = new ArrayList<View>(list.size());
        String base = wctx.api.baseUrl();
        int tileH = d.dp(84);

        for (int i = 0; i < list.size(); i++) {
            final AppState.ShortcutItem s = list.get(i);
            final TouchActionTileView tile = new TouchActionTileView(wctx.context)
                    .setIconOnly(true)
                    .setTile(s.label, ThemeColors.parseColorSafe(s.color, AppTheme.get().colors().accent));
            tile.setMinimumHeight(tileH);
            tile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wctx.sync.postAction(
                            "/api/run/" + ApiClient.enc(s.id),
                            "Aberto: " + s.label,
                            "Falha ao executar atalho",
                            true);
                }
            });
            if (s.hold) {
                tile.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        wctx.sync.postAction(
                                "/api/run/" + ApiClient.enc(s.id) + "?hold=true",
                                "Segurar: " + s.label,
                                "Falha ao executar ação secundária",
                                true);
                        return true;
                    }
                });
            }

            String iconKey = "sc:" + s.id + ":" + s.iconRev;
            String primary = base + "/api/shortcuts/icon?id=" + ApiClient.enc(s.id)
                    + (s.iconRev.length() > 0 ? ("&v=" + ApiClient.enc(s.iconRev)) : "");
            String fallback = base + "/legacy/icons/" + ApiClient.enc(s.id) + ".png";
            wctx.bitmaps.load(iconKey, primary, fallback, 128, new BitmapCache.Callback() {
                @Override
                public void onBitmapLoaded(String key, Bitmap bmp) {
                    tile.setIconBitmap(bmp);
                }
            });

            tilesById.put(s.id, tile);
            views.add(tile);
        }
        grid.setItems(views);
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        grid.setPortrait(portrait);
    }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        card.applyTheme();
        countPill.applyTheme();
        for (TouchActionTileView tile : tilesById.values()) {
            tile.applyTheme();
        }
    }
}

