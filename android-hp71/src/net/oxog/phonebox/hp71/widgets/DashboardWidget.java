package net.oxog.phonebox.hp71.widgets;

import android.content.Context;
import android.view.View;

import net.oxog.phonebox.hp71.core.ApiClient;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.core.BitmapCache;
import net.oxog.phonebox.hp71.core.SyncManager;

/**
 * Contrato único de todos os widgets funcionais autocontidos.
 * Cada widget gerencia apenas sua própria atualização visual, sem conhecer widgets vizinhos.
 */
public interface DashboardWidget {

    View getView();

    void bindData(AppState state);

    void onOrientationChanged(boolean portrait);

    void setActive(boolean visible);

    void applyTheme();

    /**
     * Contexto de dependências leves injetado nos widgets (evita acoplamento com MainActivity).
     */
    final class WidgetContext {
        public final Context context;
        public final ApiClient api;
        public final BitmapCache bitmaps;
        public final SyncManager sync;
        public final HostCallbacks host;

        public WidgetContext(Context context, ApiClient api, BitmapCache bitmaps, SyncManager sync, HostCallbacks host) {
            this.context = context;
            this.api = api;
            this.bitmaps = bitmaps;
            this.sync = sync;
            this.host = host;
        }
    }

    interface HostCallbacks {
        void navigateToView(int viewIdx);
        void setHardwareBrightness(float level);
        float getHardwareBrightness();
        void showToast(String msg, boolean error);
        int getViewportWidthPx();
        int getViewportHeightPx();
    }
}

