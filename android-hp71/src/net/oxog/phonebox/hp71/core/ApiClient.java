package net.oxog.phonebox.hp71.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Cliente HTTP puro (HttpURLConnection + org.json, 100% API 17).
 * Consome /api/legacy/sync e os endpoints POST de controle sem dependências externas.
 */
public final class ApiClient {

    public static final String HEADER_DEVICE = "hp71native";
    public static final String HEADER_MODEL = "HP 7.1 1201 (Native)";
    public static final String HEADER_APP = "legacy-web";

    private final AppPrefs prefs;

    public ApiClient(AppPrefs prefs) {
        this.prefs = prefs;
    }

    public String baseUrl() {
        return prefs.getBaseUrl();
    }

    public static String enc(String s) {
        if (s == null) return "";
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    public JSONObject fetchSync(int page, String cfgVer, long evSeq, boolean liveAi) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl()).append("/api/legacy/sync?page=").append(page);
        sb.append("&cfg=").append(enc(cfgVer == null ? "" : cfgVer));
        if (evSeq > 0) sb.append("&ev=").append(evSeq);
        if (liveAi) sb.append("&liveAi=1");
        sb.append("&_t=").append(System.currentTimeMillis());
        String body = request("GET", sb.toString(), liveAi ? 4500 : 3500);
        return (body != null && body.length() > 0) ? new JSONObject(body) : null;
    }

    public JSONObject getJson(String pathAndQuery) throws Exception {
        String sep = pathAndQuery.indexOf('?') >= 0 ? "&" : "?";
        String body = request("GET", baseUrl() + pathAndQuery + sep + "_t=" + System.currentTimeMillis(), 3500);
        return (body != null && body.length() > 0) ? new JSONObject(body) : null;
    }

    public AppState.ApkReleaseInfo fetchApkInfo() throws Exception {
        JSONObject obj = getJson("/api/legacy/apk-info");
        AppState.ApkReleaseInfo info = new AppState.ApkReleaseInfo();
        parseApkInfo(info, obj);
        return info;
    }

    public static final class PostResult {
        public final boolean ok;
        public final int statusCode;
        public final String error;

        public PostResult(boolean ok, int statusCode, String error) {
            this.ok = ok;
            this.statusCode = statusCode;
            this.error = error == null ? "" : error;
        }
    }

    public boolean post(String pathAndQuery) {
        return postDetailed(pathAndQuery).ok;
    }

    public PostResult postDetailed(String pathAndQuery) {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = open(baseUrl() + pathAndQuery, "POST", 3500);
            conn.setFixedLengthStreamingMode(0);
            conn.setDoOutput(true);
            conn.getOutputStream().close();
            int code = conn.getResponseCode();
            in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String body = "";
            if (in != null) {
                byte[] bytes = readAll(in, 64 * 1024);
                if (bytes != null) body = new String(bytes, "UTF-8");
            }
            if (code >= 200 && code < 300) {
                return new PostResult(true, code, "");
            }
            String errMsg = "";
            if (body.length() > 0 && body.charAt(0) == '{') {
                try {
                    JSONObject jo = new JSONObject(body);
                    errMsg = safeStr(jo, "error", safeStr(jo, "detail", ""));
                } catch (Exception ignored) { }
            }
            return new PostResult(false, code, errMsg);
        } catch (Exception e) {
            return new PostResult(false, 0, "");
        } finally {
            closeQuietly(in);
            if (conn != null) conn.disconnect();
        }
    }

    public Bitmap fetchBitmap(String fullUrl, int maxSizePx) {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = open(fullUrl, "GET", 4000);
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) return null;
            in = conn.getInputStream();
            byte[] raw = readAll(in, 512 * 1024);
            if (raw == null || raw.length == 0) return null;

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(raw, 0, raw.length, bounds);
            int sample = 1;
            int maxDim = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxSizePx > 0 && maxDim / (sample * 2) >= maxSizePx) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(raw, 0, raw.length, opts);
        } catch (Exception e) {
            return null;
        } finally {
            closeQuietly(in);
            if (conn != null) conn.disconnect();
        }
    }

    private String request(String method, String urlStr, int timeoutMs) throws Exception {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = open(urlStr, method, timeoutMs);
            if ("POST".equals(method)) {
                conn.setFixedLengthStreamingMode(0);
                conn.setDoOutput(true);
                conn.getOutputStream().close();
            }
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new Exception("HTTP " + code);
            }
            in = conn.getInputStream();
            byte[] bytes = readAll(in, 1024 * 1024);
            return bytes == null ? "" : new String(bytes, "UTF-8");
        } finally {
            closeQuietly(in);
            if (conn != null) conn.disconnect();
        }
    }

    private HttpURLConnection open(String urlStr, String method, int timeoutMs) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setUseCaches(false);
        conn.setRequestProperty("X-PhoneBox-Device", HEADER_DEVICE);
        conn.setRequestProperty("X-PhoneBox-Model", HEADER_MODEL);
        conn.setRequestProperty("X-PhoneBox-App", HEADER_APP);
        conn.setRequestProperty("Accept", "application/json, image/png, */*");
        return conn;
    }

    private static byte[] readAll(InputStream in, int maxBytes) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        byte[] buf = new byte[4096];
        int total = 0;
        int n;
        while ((n = in.read(buf)) > 0) {
            total += n;
            if (total > maxBytes) break;
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static void closeQuietly(InputStream in) {
        if (in != null) {
            try { in.close(); } catch (Exception ignored) { }
        }
    }

    // ==================== NULL-SAFE JSON HELPERS (AOSP API 17 FIX) ====================
    // Em Android 4.2.2 (API 17), JSONObject.optString(key, def) retorna a string literal "null"
    // quando o valor no JSON e null, e optDouble(key, def) retorna Double.NaN.

    private static String safeStr(JSONObject o, String key, String def) {
        if (o == null || o.isNull(key)) return def;
        String v = o.optString(key, def);
        if (v == null || "null".equals(v)) return def;
        return v;
    }

    private static String safeStrAny(JSONObject o, String key, String def) {
        if (o == null || o.isNull(key)) return def;
        Object raw = o.opt(key);
        if (raw == null || raw == JSONObject.NULL) return def;
        String v = String.valueOf(raw);
        return "null".equals(v) ? def : v;
    }

    private static float safeFloat(JSONObject o, String key, float def) {
        if (o == null || o.isNull(key)) return def;
        double d = o.optDouble(key, def);
        return (Double.isNaN(d) || Double.isInfinite(d)) ? def : (float) d;
    }

    private static double safeDouble(JSONObject o, String key, double def) {
        if (o == null || o.isNull(key)) return def;
        double d = o.optDouble(key, def);
        return (Double.isNaN(d) || Double.isInfinite(d)) ? def : d;
    }

    private static long safeLong(JSONObject o, String key, long def) {
        if (o == null || o.isNull(key)) return def;
        return o.optLong(key, def);
    }

    private static int safeInt(JSONObject o, String key, int def) {
        if (o == null || o.isNull(key)) return def;
        return o.optInt(key, def);
    }

    // ==================== PARSING JSON -> AppState ====================

    public static List<AppState.LiveEvent> applySyncJson(AppState s, JSONObject root) {
        List<AppState.LiveEvent> emitted = new ArrayList<AppState.LiveEvent>();
        if (root == null) return emitted;

        // 1. Config
        JSONObject cfgWrap = root.optJSONObject("config");
        if (cfgWrap != null) {
            String ver = safeStrAny(cfgWrap, "version", "");
            if (ver.length() > 0) s.cfgVersion = ver;
            JSONObject cfg = cfgWrap.optJSONObject("config");
            if (cfg != null) parseConfig(s, cfg);
        }

        // 2. State
        JSONObject st = root.optJSONObject("state");
        if (st != null) parseState(s, st, emitted);

        // 3. Weather
        JSONObject wx = root.optJSONObject("weather");
        if (wx != null) parseWeather(s, wx);

        // 4. Controls / Audio Mixer
        JSONObject ctrls = root.optJSONObject("controls");
        if (ctrls != null) parseControls(s, ctrls);

        // 5. Processes
        JSONObject pr = root.optJSONObject("procs");
        if (pr != null) parseProcs(s, pr);

        // 6. Quotas
        JSONObject qt = root.optJSONObject("quotas");
        if (qt != null) parseQuotas(s, qt);

        // 7. Work (Sessions + GitHub)
        JSONObject wk = root.optJSONObject("work");
        if (wk != null) parseWork(s, wk);

        // 8. Push Webhooks (Dictionary)
        JSONObject ps = root.optJSONObject("push");
        if (ps != null) parsePush(s, ps);

        // 9. Discord
        JSONObject dc = root.optJSONObject("discord");
        if (dc != null) parseDiscord(s, dc);

        // 10. Approvals
        JSONObject ap = root.optJSONObject("approvals");
        if (ap != null) parseApprovals(s, ap);

        // 11. Server APK release metadata (self-update check)
        JSONObject apk = root.optJSONObject("apk");
        if (apk != null) parseApkInfo(s.apk, apk);

        return emitted;
    }

    public static void parseApkInfo(AppState.ApkReleaseInfo target, JSONObject o) {
        if (target == null || o == null) return;
        target.available = o.optBoolean("available", false);
        target.versionCode = safeInt(o, "versionCode", 0);
        target.versionName = safeStr(o, "versionName", "");
        target.sizeBytes = safeLong(o, "size", 0L);
        target.sha256 = safeStr(o, "sha256", "");
        String u = safeStr(o, "url", "/legacy/PhoneBox-HP71.apk");
        target.url = u.length() > 0 ? u : "/legacy/PhoneBox-HP71.apk";
    }

    private static void parseState(AppState s, JSONObject st, List<AppState.LiveEvent> emitted) {
        s.hostName = safeStr(st, "host", s.hostName);
        long nextSeq = safeLong(st, "evSeq", s.evSeq);
        boolean hadSeq = s.evSeq > 0;
        if (nextSeq > s.evSeq) s.evSeq = nextSeq;
        if (hadSeq) {
            JSONArray evs = st.optJSONArray("events");
            if (evs != null) {
                for (int i = 0; i < evs.length(); i++) {
                    JSONObject eo = evs.optJSONObject(i);
                    if (eo == null) continue;
                    AppState.LiveEvent le = new AppState.LiveEvent();
                    le.seq = safeLong(eo, "seq", 0);
                    le.kind = safeStr(eo, "kind", safeStr(eo, "type", ""));
                    JSONObject ed = eo.optJSONObject("data");
                    if (ed != null) {
                        le.page = safeStr(ed, "page", "");
                        le.title = safeStr(ed, "title", "");
                        le.text = safeStr(ed, "text", "");
                        le.level = safeStr(ed, "level", "info");
                        le.seconds = safeInt(ed, "seconds", 4);
                    }
                    emitted.add(le);
                }
            }
        }

        // PC
        JSONObject pc = st.optJSONObject("pc");
        if (pc != null) {
            s.pc.cpuPct = safeFloat(pc, "cpu", 0f);
            s.pc.hasCpuTemp = pc.has("cpuTemp") && !pc.isNull("cpuTemp");
            s.pc.cpuTemp = safeFloat(pc, "cpuTemp", 0f);
            s.pc.cpuName = safeStr(pc, "cpuName", s.pc.cpuName);
            s.pc.ramUsedGb = safeFloat(pc, "ramUsedGb", 0f);
            s.pc.ramTotalGb = safeFloat(pc, "ramTotalGb", 1f);
            s.pc.netDownBps = safeLong(pc, "netDownBps", 0L);
            s.pc.netUpBps = safeLong(pc, "netUpBps", 0L);
            s.pc.uptimeSec = safeLong(pc, "uptimeSec", 0L);

            JSONObject gpu = pc.optJSONObject("gpu");
            if (gpu != null) {
                s.pc.hasGpu = true;
                s.pc.gpuName = safeStr(gpu, "name", "");
                s.pc.gpuUtil = safeFloat(gpu, "util", 0f);
                s.pc.hasGpuTemp = gpu.has("temp") && !gpu.isNull("temp");
                s.pc.gpuTemp = safeFloat(gpu, "temp", 0f);
                s.pc.hasGpuPower = gpu.has("power") && !gpu.isNull("power");
                s.pc.gpuPower = safeFloat(gpu, "power", 0f);
                s.pc.gpuPowerLimit = safeFloat(gpu, "powerLimit", 0f);
                s.pc.hasGpuVram = gpu.has("vramTotal") && !gpu.isNull("vramTotal") && safeFloat(gpu, "vramTotal", 0f) > 0f;
                s.pc.gpuVramUsed = safeFloat(gpu, "vramUsed", 0f);
                s.pc.gpuVramTotal = safeFloat(gpu, "vramTotal", 0f);
                s.pc.gpuSharedUsed = safeFloat(gpu, "sharedUsed", 0f);
                s.pc.gpuSharedTotal = safeFloat(gpu, "sharedTotal", 0f);
                s.pc.hasGpuClock = gpu.has("clock") && !gpu.isNull("clock");
                s.pc.gpuClock = Math.round(safeFloat(gpu, "clock", 0f));
                s.pc.hasGpuMemClock = gpu.has("memClock") && !gpu.isNull("memClock");
                s.pc.gpuMemClock = Math.round(safeFloat(gpu, "memClock", 0f));
                s.pc.hasGpuFan = gpu.has("fan") && !gpu.isNull("fan");
                s.pc.gpuFan = Math.round(safeFloat(gpu, "fan", 0f));
                s.pc.gpuDriver = safeStr(gpu, "driver", "");
            } else {
                s.pc.hasGpu = false;
                s.pc.hasGpuTemp = false;
                s.pc.hasGpuPower = false;
                s.pc.hasGpuVram = false;
                s.pc.hasGpuClock = false;
                s.pc.hasGpuMemClock = false;
                s.pc.hasGpuFan = false;
            }

            s.pc.disks.clear();
            JSONArray disks = pc.optJSONArray("disks");
            if (disks != null) {
                for (int i = 0; i < disks.length(); i++) {
                    JSONObject dk = disks.optJSONObject(i);
                    if (dk == null) continue;
                    AppState.DiskInfo d = new AppState.DiskInfo();
                    d.name = safeStr(dk, "name", "");
                    d.usedGb = safeFloat(dk, "usedGb", 0f);
                    d.totalGb = safeFloat(dk, "totalGb", 1f);
                    s.pc.disks.add(d);
                }
            }

            s.histCpu.push(s.pc.cpuPct);
            s.histRam.push(s.pc.ramPct());
            s.histGpu.push(s.pc.hasGpu ? s.pc.gpuUtil : Float.NaN);
            s.histTemp.push(s.pc.hasGpu && s.pc.hasGpuTemp ? s.pc.gpuTemp : Float.NaN);
        }

        // Media
        JSONObject media = st.optJSONObject("media");
        if (media != null) {
            s.media.app = safeStr(media, "app", "");
            s.media.title = safeStr(media, "title", "");
            s.media.artist = safeStr(media, "artist", "");
            s.media.album = safeStr(media, "album", "");
            s.media.playing = media.optBoolean("playing", false);
            s.media.artId = safeInt(media, "artId", 0);
            s.media.hasArt = (s.media.artId > 0) || media.optBoolean("hasArt", false);
        } else {
            s.media.title = "";
            s.media.artist = "";
            s.media.album = "";
            s.media.app = "";
            s.media.playing = false;
            s.media.artId = 0;
            s.media.hasArt = false;
        }

        // Audio
        JSONObject audio = st.optJSONObject("audio");
        if (audio != null) {
            s.audio.volume = Math.max(0, Math.min(100, Math.round(safeFloat(audio, "volume", 0f))));
            s.audio.muted = audio.optBoolean("muted", false);
        }

        // Ping
        JSONObject ping = st.optJSONObject("ping");
        if (ping != null) {
            s.ping.host = safeStr(ping, "host", "1.1.1.1");
            s.ping.hasMs = ping.has("ms") && !ping.isNull("ms");
            s.ping.ms = safeFloat(ping, "ms", 0f);
            s.ping.hasAvgMs = ping.has("avgMs") && !ping.isNull("avgMs");
            s.ping.avgMs = safeFloat(ping, "avgMs", 0f);
            s.ping.lossPct = safeFloat(ping, "lossPct", 0f);
        } else {
            s.ping.hasMs = false;
            s.ping.hasAvgMs = false;
            s.ping.lossPct = 0f;
        }

        // Privacy
        JSONObject priv = st.optJSONObject("privacy");
        if (priv != null) {
            s.privacy.mic = priv.optBoolean("mic", false);
            s.privacy.camera = priv.optBoolean("camera", false);
            s.privacy.micApps = joinStrings(priv.optJSONArray("micApps"));
            s.privacy.cameraApps = joinStrings(priv.optJSONArray("cameraApps"));
        }

        // Toggles
        s.toggles.clear();
        JSONObject tog = st.optJSONObject("toggles");
        if (tog != null) {
            Iterator<String> it = tog.keys();
            while (it.hasNext()) {
                String k = it.next();
                s.toggles.put(k, tog.optBoolean(k, false));
            }
        }
    }

    private static void parseConfig(AppState s, JSONObject cfg) {
        JSONObject disp = cfg.optJSONObject("display");
        if (disp != null) {
            s.display.showSeconds = disp.optBoolean("showSeconds", true);
            Object h12 = disp.isNull("hour12") ? null : disp.opt("hour12");
            s.display.hour12 = Boolean.TRUE.equals(h12) || "true".equals(String.valueOf(h12));
            s.display.orientation = safeStr(disp, "orientation", "auto");
            s.display.theme = safeStr(disp, "theme", "default");
            s.display.accent = safeStr(disp, "accent", "#8bd0ff");
            s.display.controlsTop = disp.optBoolean("controlsTop", false);
            if (disp.has("volumeKeys") && !disp.isNull("volumeKeys")) {
                s.display.hasVolumeKeys = true;
                s.display.volumeKeys = disp.optBoolean("volumeKeys", true);
            }
            s.display.idleReturnSec = safeInt(disp, "idleReturnSec", 90);
            s.display.idleDimMin = safeInt(disp, "idleDimMin", 0);
            s.display.homePage = safeStr(disp, "homePage", "home");
            JSONObject ap = disp.optJSONObject("appearance");
            if (ap != null) {
                s.display.cardRadiusDp = ap.has("radius") && !ap.isNull("radius") ? safeInt(ap, "radius", -1) : -1;
                s.display.borderlessCards = ap.has("border") && !ap.optBoolean("border", true);
            } else {
                s.display.cardRadiusDp = -1;
                s.display.borderlessCards = false;
            }
        }

        JSONObject ng = cfg.optJSONObject("night");
        if (ng != null) {
            s.night.enabled = ng.optBoolean("enabled", true);
            s.night.start = safeStr(ng, "start", "00:30");
            s.night.end = safeStr(ng, "end", "07:30");
            s.night.brightness = safeFloat(ng, "brightness", 0.02f);
        }

        s.quotaFilterProviders.clear();
        JSONObject cb = cfg.optJSONObject("codexBar");
        if (cb != null) {
            JSONArray ps = cb.optJSONArray("providers");
            if (ps != null) {
                for (int i = 0; i < ps.length(); i++) {
                    if (ps.isNull(i)) continue;
                    String pid = ps.optString(i, "");
                    if (pid != null && pid.length() > 0 && !"null".equals(pid)) s.quotaFilterProviders.add(pid);
                }
            }
        }

        // Shortcuts
        s.shortcuts.clear();
        JSONArray scs = cfg.optJSONArray("shortcuts");
        if (scs != null) {
            for (int i = 0; i < scs.length(); i++) {
                JSONObject o = scs.optJSONObject(i);
                if (o == null) continue;
                AppState.ShortcutItem item = new AppState.ShortcutItem();
                item.id = safeStr(o, "id", "");
                item.label = safeStr(o, "label", item.id);
                item.color = safeStr(o, "color", "#2dd4bf");
                item.iconRev = safeStrAny(o, "iconRev", "");
                item.hold = o.optBoolean("hold", false);
                s.shortcuts.add(item);
            }
        }

        // Pages & Widget options
        s.shortcutsColumns = 7;
        s.showAudioMixer = true;
        s.allowProcessKill = true;
        s.showHomeDate = true;
        s.showHomeWeather = true;
        s.showDiscordControl = true;
        s.showDiscordChannels = true;
        s.showDiscordNotifs = true;
        s.worldClockZones.clear();

        s.pages.clear();
        JSONArray pgs = cfg.optJSONArray("pages");
        boolean[] usedView = new boolean[7];
        if (pgs != null) {
            for (int i = 0; i < pgs.length(); i++) {
                JSONObject p = pgs.optJSONObject(i);
                if (p == null || !p.optBoolean("enabled", true)) continue;
                String rawId = safeStr(p, "id", "");
                String id = rawId.toLowerCase();
                int vIdx = mapBuiltInView(id);
                if (vIdx >= 0 && !usedView[vIdx]) {
                    usedView[vIdx] = true;
                    AppState.PageTab tab = new AppState.PageTab();
                    tab.id = rawId;
                    tab.title = safeStr(p, "title", defaultTabTitle(vIdx));
                    tab.viewIdx = vIdx;
                    tab.idleSec = p.has("idleSec") && !p.isNull("idleSec") ? safeInt(p, "idleSec", -1) : -1;
                    s.pages.add(tab);
                }

                JSONArray ws = p.optJSONArray("widgets");
                boolean deckHasMixer = false;
                if (ws != null) {
                    for (int j = 0; j < ws.length(); j++) {
                        JSONObject w = ws.optJSONObject(j);
                        if (w == null) continue;
                        String wt = safeStr(w, "type", "");
                        JSONObject wo = w.optJSONObject("options");
                        if ("shortcuts".equals(wt) && wo != null) {
                            int c = safeInt(wo, "columns", 7);
                            if (c >= 2 && c <= 8) s.shortcutsColumns = c;
                        } else if ("audio-mixer".equals(wt)) {
                            deckHasMixer = true;
                        } else if ("processes".equals(wt) && wo != null) {
                            s.allowProcessKill = wo.optBoolean("allowKill", true);
                        } else if ("clock".equals(wt) && wo != null) {
                            s.showHomeDate = wo.optBoolean("date", true);
                            s.showHomeWeather = wo.optBoolean("weather", true);
                        } else if ("discord".equals(wt) && wo != null) {
                            s.showDiscordControl = wo.optBoolean("control", true);
                            s.showDiscordChannels = wo.optBoolean("channels", true);
                            s.showDiscordNotifs = wo.optBoolean("notifications", true);
                        } else if ("worldclock".equals(wt) && wo != null) {
                            JSONArray zs = wo.optJSONArray("zones");
                            if (zs != null) {
                                for (int k = 0; k < zs.length(); k++) {
                                    if (zs.isNull(k)) continue;
                                    String z = zs.optString(k, "");
                                    if (z != null && z.length() > 0 && !"null".equals(z)) s.worldClockZones.add(z);
                                }
                            }
                        }
                    }
                }
                if (vIdx == 2) s.showAudioMixer = deckHasMixer;
            }
        }
    }

    public static int mapBuiltInView(String id) {
        if ("home".equals(id)) return 0;
        if ("system".equals(id) || "dashboard".equals(id) || "status".equals(id)) return 1;
        if ("deck".equals(id) || "shortcuts".equals(id) || "audio-deck".equals(id)) return 2;
        if ("procs".equals(id) || "processes".equals(id)) return 3;
        if ("ai".equals(id) || "codexbar".equals(id) || "work".equals(id)) return 4;
        if ("discord".equals(id)) return 5;
        if ("world".equals(id) || "clock".equals(id) || "nightstand".equals(id) || "ambient".equals(id)) return 6;
        return -1;
    }

    public static String defaultTabTitle(int viewIdx) {
        switch (viewIdx) {
            case 0: return "Início";
            case 1: return "Sistema";
            case 2: return "Atalhos";
            case 3: return "Processos";
            case 4: return "IA";
            case 5: return "Discord";
            case 6: return "Clima/Hora";
            default: return "Painel";
        }
    }

    private static void parseWeather(AppState s, JSONObject w) {
        AppState.WeatherData wd = s.weather;
        wd.ok = w.optBoolean("ok", false);
        wd.city = safeStr(w, "city", wd.city);
        wd.lat = safeDouble(w, "lat", wd.lat);
        wd.lon = safeDouble(w, "lon", wd.lon);
        wd.timezone = safeStr(w, "timezone", wd.timezone);
        wd.tzAbbr = safeStr(w, "tzAbbr", wd.tzAbbr);
        wd.unit = safeStr(w, "unit", "C");
        if (!wd.ok) return;
        wd.temp = safeFloat(w, "temp", 0f);
        wd.feels = safeFloat(w, "feels", wd.temp);
        wd.hi = safeFloat(w, "hi", wd.temp);
        wd.lo = safeFloat(w, "lo", wd.temp);
        wd.humidity = safeInt(w, "humidity", 0);
        wd.precipMm = safeFloat(w, "precipMm", 0f);
        wd.rainProb = safeInt(w, "rainProb", 0);
        wd.windKmh = safeFloat(w, "windKmh", 0f);
        wd.windDir = safeInt(w, "windDir", 0);
        wd.pressureHpa = safeFloat(w, "pressureHpa", 0f);
        wd.uv = safeFloat(w, "uv", 0f);
        wd.uvMax = safeFloat(w, "uvMax", wd.uv);
        wd.isDay = w.optBoolean("isDay", true);
        wd.sunrise = safeStr(w, "sunrise", "");
        wd.sunset = safeStr(w, "sunset", "");
        wd.daylightSec = safeInt(w, "daylightSec", 0);
        wd.code = safeInt(w, "code", 0);
        JSONObject air = w.optJSONObject("air");
        if (air != null) {
            int eu = safeInt(air, "euAqi", 0);
            wd.aqi = eu > 0 ? eu : safeInt(air, "usAqi", 35);
        }

        wd.hourly.clear();
        JSONArray hl = w.optJSONArray("hourly");
        if (hl != null) {
            for (int i = 0; i < hl.length(); i++) {
                JSONObject o = hl.optJSONObject(i);
                if (o == null) continue;
                AppState.HourlyPoint hp = new AppState.HourlyPoint();
                hp.time = safeStr(o, "time", "");
                hp.temp = safeFloat(o, "temp", 0f);
                hp.pop = safeInt(o, "pop", 0);
                hp.code = safeInt(o, "code", 0);
                wd.hourly.add(hp);
            }
        }
        wd.daily.clear();
        JSONArray dl = w.optJSONArray("daily");
        if (dl != null) {
            for (int i = 0; i < dl.length(); i++) {
                JSONObject o = dl.optJSONObject(i);
                if (o == null) continue;
                AppState.DailyPoint dp = new AppState.DailyPoint();
                dp.date = safeStr(o, "date", "");
                dp.hi = safeFloat(o, "hi", 0f);
                dp.lo = safeFloat(o, "lo", 0f);
                dp.code = safeInt(o, "code", 0);
                dp.pop = safeInt(o, "pop", 0);
                wd.daily.add(dp);
            }
        }
    }

    private static void parseControls(AppState s, JSONObject ctrls) {
        s.audioSessions.clear();
        JSONObject audio = ctrls.optJSONObject("audio");
        if (audio == null) return;
        JSONArray arr = audio.optJSONArray("sessions");
        if (arr == null) arr = audio.optJSONArray("apps");
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            AppState.AudioSession as = new AppState.AudioSession();
            as.id = safeStr(o, "id", "");
            as.name = safeStr(o, "name", safeStr(o, "label", as.id));
            as.processName = safeStr(o, "processName", as.name);
            as.volume = Math.max(0, Math.min(100, Math.round(safeFloat(o, "volume", 0f))));
            as.muted = o.optBoolean("muted", false);
            as.active = o.optBoolean("active", false);
            s.audioSessions.add(as);
        }
    }

    private static void parseProcs(AppState s, JSONObject pr) {
        s.totalProcs = safeInt(pr, "total", 0);
        s.groupProcs = safeInt(pr, "groupCount", 0);
        if (pr.has("allowKill") && !pr.isNull("allowKill")) {
            s.allowProcessKill = pr.optBoolean("allowKill", s.allowProcessKill);
        }
        s.processes.clear();
        JSONArray gs = pr.optJSONArray("groups");
        if (gs != null) {
            for (int i = 0; i < gs.length(); i++) {
                JSONObject o = gs.optJSONObject(i);
                if (o == null) continue;
                AppState.ProcessGroup pg = new AppState.ProcessGroup();
                pg.name = safeStr(o, "name", "?");
                pg.title = safeStr(o, "title", pg.name);
                pg.count = safeInt(o, "count", 1);
                pg.cpu = safeFloat(o, "cpu", 0f);
                pg.memMb = safeFloat(o, "memMb", 0f);
                pg.locked = o.optBoolean("locked", false);
                pg.hasIcon = o.optBoolean("hasIcon", true);
                s.processes.add(pg);
            }
        }
    }

    private static void parseQuotas(AppState s, JSONObject qt) {
        s.quotasUpdatedAt = System.currentTimeMillis();
        s.quotaProviders.clear();
        JSONArray arr = qt.optJSONArray("providers");
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            AppState.QuotaProvider qp = new AppState.QuotaProvider();
            qp.id = safeStr(o, "id", "");
            qp.name = safeStr(o, "name", qp.id);
            qp.plan = safeStr(o, "plan", "");
            qp.extra = safeStr(o, "extra", "");
            qp.error = safeStr(o, "error", "");
            JSONArray ls = o.optJSONArray("limits");
            if (ls != null) {
                for (int j = 0; j < ls.length(); j++) {
                    JSONObject lo = ls.optJSONObject(j);
                    if (lo == null) continue;
                    AppState.QuotaLimit ql = new AppState.QuotaLimit();
                    ql.label = safeStr(lo, "label", "Limite");
                    ql.percent = safeFloat(lo, "percent", 0f);
                    ql.severity = safeStr(lo, "severity", "");
                    ql.resetDescription = safeStr(lo, "resetDescription", "");
                    ql.resetsAt = safeLong(lo, "resetsAt", 0L);
                    qp.limits.add(ql);
                }
            }
            s.quotaProviders.add(qp);
        }
    }

    private static void parseWork(AppState s, JSONObject wk) {
        s.aiSessions.clear();
        JSONArray ss = wk.optJSONArray("sessions");
        if (ss != null) {
            for (int i = 0; i < ss.length(); i++) {
                JSONObject o = ss.optJSONObject(i);
                if (o == null) continue;
                AppState.AiSession as = new AppState.AiSession();
                as.id = safeStr(o, "id", "");
                as.tool = safeStr(o, "tool", "IA");
                String fallbackTitle = safeStr(o, "project", safeStr(o, "name", as.id));
                as.title = safeStr(o, "title", fallbackTitle);
                if (as.title.length() == 0) as.title = fallbackTitle;
                as.status = safeStr(o, "status", "idle");
                as.model = safeStr(o, "model", "");
                as.lastTool = safeStr(o, "lastTool", "");
                as.totalTokens = safeLong(o, "totalTokens", 0L);
                s.aiSessions.add(as);
            }
        }
        s.githubPrs.clear();
        s.githubUser = "";
        s.githubReviewCount = 0;
        JSONObject gh = wk.optJSONObject("github");
        if (gh != null) {
            s.githubUser = safeStr(gh, "user", "");
            JSONArray prs = gh.optJSONArray("myPrs");
            if (prs != null) {
                for (int i = 0; i < prs.length(); i++) {
                    JSONObject o = prs.optJSONObject(i);
                    if (o == null) continue;
                    AppState.GithubPr p = new AppState.GithubPr();
                    p.repo = safeStr(o, "repo", "");
                    p.number = safeInt(o, "number", 0);
                    p.title = safeStr(o, "title", "");
                    s.githubPrs.add(p);
                }
            }
            JSONArray revs = gh.optJSONArray("reviewRequests");
            s.githubReviewCount = revs != null ? revs.length() : 0;
        }
    }

    private static void parsePush(AppState s, JSONObject ps) {
        s.pushEntries.clear();
        JSONObject itemsObj = ps.optJSONObject("items");
        if (itemsObj == null) return;
        Iterator<String> keys = itemsObj.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            JSONObject entry = itemsObj.optJSONObject(k);
            if (entry == null) continue;
            AppState.PushEntry pe = new AppState.PushEntry();
            pe.key = k;
            pe.at = safeLong(entry, "at", 0L);
            Object dataObj = entry.isNull("data") ? null : entry.opt("data");
            if (dataObj instanceof JSONObject) {
                JSONObject d = (JSONObject) dataObj;
                if (d.has("value") && !d.isNull("value")) {
                    String unit = safeStr(d, "unit", "");
                    pe.value = safeStrAny(d, "value", "") + (unit.length() > 0 ? " " + unit : "");
                    pe.sub = safeStr(d, "sub", safeStr(d, "title", ""));
                } else if (d.has("text") && !d.isNull("text")) {
                    pe.value = safeStr(d, "text", "");
                    pe.sub = safeStr(d, "title", safeStr(d, "sub", ""));
                } else {
                    pe.value = d.toString();
                }
            } else {
                pe.value = dataObj != null ? String.valueOf(dataObj) : "--";
            }
            s.pushEntries.add(pe);
        }
    }

    private static void parseDiscord(AppState s, JSONObject dc) {
        AppState.DiscordState ds = s.discord;
        ds.enabled = dc.optBoolean("enabled", false);
        ds.controlAllowed = dc.optBoolean("control", true);
        JSONObject snap = dc.optJSONObject("snapshot");
        ds.members.clear();
        ds.channels.clear();
        ds.notifications.clear();
        if (snap == null) {
            ds.connected = false;
            ds.error = "";
            ds.user = "";
            ds.channelId = "";
            ds.channelName = "";
            ds.channelKind = "";
            ds.inputMode = "";
            return;
        }
        ds.connected = snap.optBoolean("connected", false);
        ds.error = safeStr(snap, "error", "");
        ds.user = safeStr(snap, "user", "");
        ds.muted = snap.optBoolean("muted", false);
        ds.deafened = snap.optBoolean("deafened", false);
        ds.inputMode = safeStr(snap, "inputMode", "");
        ds.pttKey = safeStrAny(snap, "pttKey", "");
        ds.channelId = safeStr(snap, "channelId", "");
        ds.channelName = safeStr(snap, "channelName", "");
        ds.channelKind = safeStr(snap, "channelKind", "");

        JSONArray ms = snap.optJSONArray("members");
        if (ms != null) {
            for (int i = 0; i < ms.length(); i++) {
                JSONObject o = ms.optJSONObject(i);
                if (o == null) continue;
                AppState.DiscordMember m = new AppState.DiscordMember();
                m.id = safeStr(o, "id", "");
                m.name = safeStr(o, "name", "Usuário");
                m.avatar = safeStr(o, "avatar", "");
                m.speaking = o.optBoolean("speaking", false);
                m.muted = o.optBoolean("muted", false);
                m.deafened = o.optBoolean("deafened", false);
                ds.members.add(m);
            }
        }
        JSONArray cs = snap.optJSONArray("channels");
        if (cs != null) {
            for (int i = 0; i < cs.length(); i++) {
                JSONObject o = cs.optJSONObject(i);
                if (o == null) continue;
                AppState.DiscordChannel ch = new AppState.DiscordChannel();
                ch.id = safeStr(o, "id", "");
                ch.name = safeStr(o, "name", "Canal");
                ch.guild = safeStr(o, "guild", "Servidor");
                ch.guildId = safeStr(o, "guildId", ch.guild);
                JSONArray ppl = o.optJSONArray("people");
                if (ppl != null && ppl.length() > 0) {
                    ch.peopleCount = ppl.length();
                    StringBuilder psb = new StringBuilder();
                    for (int p = 0; p < ppl.length(); p++) {
                        JSONObject po = ppl.optJSONObject(p);
                        if (po != null) {
                            String pn = safeStr(po, "name", "");
                            if (pn.length() > 0) {
                                if (psb.length() > 0) psb.append(", ");
                                psb.append(pn);
                            }
                        }
                    }
                    ch.peopleSummary = psb.toString();
                }
                ds.channels.add(ch);
            }
        }
        JSONArray ns = snap.optJSONArray("notifications");
        if (ns != null) {
            for (int i = 0; i < ns.length(); i++) {
                JSONObject o = ns.optJSONObject(i);
                if (o == null) continue;
                AppState.DiscordNotif n = new AppState.DiscordNotif();
                n.title = safeStr(o, "title", "Discord");
                n.body = safeStr(o, "body", "");
                n.ts = safeLong(o, "ts", 0L);
                ds.notifications.add(n);
            }
        }
    }

    private static void parseApprovals(AppState s, JSONObject ap) {
        s.approvals.clear();
        JSONArray arr = ap.optJSONArray("items");
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            AppState.ApprovalItem item = new AppState.ApprovalItem();
            item.id = safeStr(o, "id", "");
            item.source = safeStr(o, "source", "Agente IA");
            item.project = safeStr(o, "project", "");
            item.cwd = safeStr(o, "cwd", "");
            item.tool = safeStr(o, "tool", "Ação");
            item.detail = safeStr(o, "detail", safeStr(o, "description", ""));
            s.approvals.add(item);
        }
    }

    private static String joinStrings(JSONArray arr) {
        if (arr == null || arr.length() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (arr.isNull(i)) continue;
            String v = arr.optString(i, "");
            if (v != null && v.length() > 0 && !"null".equals(v)) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(v);
            }
        }
        return sb.toString();
    }
}

