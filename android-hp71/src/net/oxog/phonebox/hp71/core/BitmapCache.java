package net.oxog.phonebox.hp71.core;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cache leve em memória (LruCache ~4 MB) para ícones oficiais do Windows,
 * avatares do Discord e capa de mídia, seguro para 1 GB de RAM do HP 7.1.
 */
public final class BitmapCache {

    public interface Callback {
        void onBitmapLoaded(String key, Bitmap bmp);
    }

    private final ApiClient api;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService pool = Executors.newFixedThreadPool(2);
    private final LruCache<String, Bitmap> cache;
    private final Map<String, List<Callback>> inFlight = new HashMap<String, List<Callback>>();
    private final Map<String, Long> failedUntil = new HashMap<String, Long>();

    public BitmapCache(ApiClient api) {
        this.api = api;
        // 4 MB cache em KB (ideal para Cortex-A7 / 1 GB RAM)
        this.cache = new LruCache<String, Bitmap>(4 * 1024) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return Math.max(1, value.getByteCount() / 1024);
            }
        };
    }

    public Bitmap getCached(String key) {
        return key == null ? null : cache.get(key);
    }

    public void load(final String key, final String primaryUrl, final String fallbackUrl, final int maxSizePx, final Callback cb) {
        if (key == null || primaryUrl == null) return;
        Bitmap cached = cache.get(key);
        if (cached != null) {
            if (cb != null) cb.onBitmapLoaded(key, cached);
            return;
        }
        synchronized (inFlight) {
            Long retryAt = failedUntil.get(key);
            if (retryAt != null && System.currentTimeMillis() < retryAt.longValue()) return;
            List<Callback> waiters = inFlight.get(key);
            if (waiters != null) {
                if (cb != null) waiters.add(cb);
                return;
            }
            waiters = new ArrayList<Callback>(2);
            if (cb != null) waiters.add(cb);
            inFlight.put(key, waiters);
        }
        pool.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = api.fetchBitmap(primaryUrl, maxSizePx);
                if (bmp == null && fallbackUrl != null) {
                    bmp = api.fetchBitmap(fallbackUrl, maxSizePx);
                }
                final Bitmap result = bmp;
                final List<Callback> waiters;
                synchronized (inFlight) {
                    waiters = inFlight.remove(key);
                    if (result != null) {
                        cache.put(key, result);
                        failedUntil.remove(key);
                    } else {
                        failedUntil.put(key, Long.valueOf(System.currentTimeMillis() + 30000L));
                    }
                }
                if (result != null && waiters != null && !waiters.isEmpty()) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < waiters.size(); i++) {
                                waiters.get(i).onBitmapLoaded(key, result);
                            }
                        }
                    });
                }
            }
        });
    }

    public void clear() {
        cache.evictAll();
        synchronized (inFlight) {
            failedUntil.clear();
        }
    }

    public void shutdown() {
        pool.shutdownNow();
    }
}

