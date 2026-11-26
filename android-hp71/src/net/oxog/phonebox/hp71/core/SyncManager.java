package net.oxog.phonebox.hp71.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.json.JSONObject;

import java.util.List;

/**
 * Orquestrador de polling em background (HandlerThread dedicado, sem empilhar conexões).
 * - Sync geral unificado a cada 1,5s (/api/legacy/sync).
 * - Regra da aba IA (viewIdx == 4): liveAi=1 a cada 5s EXCLUSIVAMENTE enquanto a aba IA estiver aberta.
 */
public final class SyncManager {

    public interface Listener {
        void onStateUpdated(AppState state, List<AppState.LiveEvent> events);
        void onActionFeedback(String message, boolean error);
    }

    private static final long SYNC_INTERVAL_MS = 1500L;
    private static final long AI_LIVE_INTERVAL_MS = 5000L;

    private final ApiClient api;
    private final AppState state;
    private final Listener listener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final HandlerThread workerThread;
    private final Handler worker;

    private volatile boolean running = false;
    private volatile int activeViewIdx = 0;
    private boolean syncInProgress = false;
    private long lastLiveAiAt = 0L;

    private final Runnable regularLoop = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            boolean needLiveAi = (activeViewIdx == 4)
                    && (System.currentTimeMillis() - lastLiveAiAt >= AI_LIVE_INTERVAL_MS);
            doSync(needLiveAi);
            if (running) worker.postDelayed(this, SYNC_INTERVAL_MS);
        }
    };

    private final Runnable aiLiveLoop = new Runnable() {
        @Override
        public void run() {
            if (!running || activeViewIdx != 4) return;
            doSync(true);
            if (running && activeViewIdx == 4) {
                worker.postDelayed(this, AI_LIVE_INTERVAL_MS);
            }
        }
    };

    public SyncManager(ApiClient api, AppState state, Listener listener) {
        this.api = api;
        this.state = state;
        this.listener = listener;
        this.workerThread = new HandlerThread("PhoneBoxSync");
        this.workerThread.start();
        this.worker = new Handler(workerThread.getLooper());
    }

    public void start() {
        if (running) return;
        running = true;
        worker.removeCallbacks(regularLoop);
        worker.removeCallbacks(aiLiveLoop);
        worker.post(regularLoop);
        if (activeViewIdx == 4) {
            worker.post(aiLiveLoop);
        }
    }

    public void stop() {
        running = false;
        worker.removeCallbacks(regularLoop);
        worker.removeCallbacks(aiLiveLoop);
    }

    public void destroy() {
        stop();
        workerThread.quit();
    }

    public void setActiveViewIdx(int viewIdx) {
        boolean changed = (this.activeViewIdx != viewIdx);
        this.activeViewIdx = viewIdx;
        worker.removeCallbacks(aiLiveLoop);
        if (running) {
            if (viewIdx == 4) {
                worker.post(aiLiveLoop);
            } else if (changed) {
                requestImmediateSync(false);
            }
        }
    }

    public void requestImmediateSync(final boolean liveAi) {
        worker.post(new Runnable() {
            @Override
            public void run() {
                doSync(liveAi);
            }
        });
    }

    public void postAction(final String pathAndQuery, final String okToast, final String errToast, final boolean refreshAfter) {
        worker.post(new Runnable() {
            @Override
            public void run() {
                final ApiClient.PostResult res = api.postDetailed(pathAndQuery);
                if (res.ok && okToast != null) {
                    notifyFeedback(okToast, false);
                } else if (!res.ok && errToast != null) {
                    String msg = (res.error != null && res.error.length() > 0)
                            ? (errToast + ": " + res.error) : errToast;
                    notifyFeedback(msg, true);
                }
                if (refreshAfter) {
                    doSync(activeViewIdx == 4);
                }
            }
        });
    }

    public void resetConfigVersion() {
        state.cfgVersion = "";
        state.evSeq = 0L;
    }

    private void doSync(boolean liveAi) {
        if (syncInProgress) return;
        syncInProgress = true;
        try {
            int view = activeViewIdx;
            boolean sendLiveAi = liveAi && view == 4;
            if (sendLiveAi) lastLiveAiAt = System.currentTimeMillis();
            JSONObject json = api.fetchSync(view, state.cfgVersion, state.evSeq, sendLiveAi);
            if (json != null) {
                final List<AppState.LiveEvent> events;
                synchronized (state) {
                    state.online = true;
                    state.failCount = 0;
                    state.lastSyncAt = System.currentTimeMillis();
                    events = ApiClient.applySyncJson(state, json);
                }
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            synchronized (state) {
                                listener.onStateUpdated(state, events);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            synchronized (state) {
                state.failCount++;
                if (state.failCount >= 2) state.online = false;
            }
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        synchronized (state) {
                            listener.onStateUpdated(state, null);
                        }
                    }
                }
            });
        } finally {
            syncInProgress = false;
        }
    }

    private void notifyFeedback(final String msg, final boolean err) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) listener.onActionFeedback(msg, err);
            }
        });
    }
}

