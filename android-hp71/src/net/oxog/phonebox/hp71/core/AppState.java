package net.oxog.phonebox.hp71.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelos de dados centralizados e buffer circular de histórico (60 amostras).
 * Todos os widgets consomem somente esta estrutura imutável/sincronizada.
 */
public final class AppState {

    public boolean online = false;
    public int failCount = 0;
    public long lastSyncAt = 0L;

    public String hostName = "PhoneBox PC";
    public String cfgVersion = "";
    public long evSeq = 0L;

    public final PcStats pc = new PcStats();
    public final MediaInfo media = new MediaInfo();
    public final AudioInfo audio = new AudioInfo();
    public final PingInfo ping = new PingInfo();
    public final PrivacyInfo privacy = new PrivacyInfo();
    public final Map<String, Boolean> toggles = new HashMap<String, Boolean>();

    public final DisplayConfig display = new DisplayConfig();
    public final NightConfig night = new NightConfig();
    public final List<PageTab> pages = new ArrayList<PageTab>();
    public final List<ShortcutItem> shortcuts = new ArrayList<ShortcutItem>();
    public final List<AudioSession> audioSessions = new ArrayList<AudioSession>();

    public int totalProcs = 0;
    public int groupProcs = 0;
    public final List<ProcessGroup> processes = new ArrayList<ProcessGroup>();

    public final List<QuotaProvider> quotaProviders = new ArrayList<QuotaProvider>();
    public long quotasUpdatedAt = 0L;
    public final List<AiSession> aiSessions = new ArrayList<AiSession>();
    public String githubUser = "";
    public final List<GithubPr> githubPrs = new ArrayList<GithubPr>();
    public int githubReviewCount = 0;
    public final List<PushEntry> pushEntries = new ArrayList<PushEntry>();

    public final DiscordState discord = new DiscordState();
    public final WeatherData weather = new WeatherData();
    public final List<ApprovalItem> approvals = new ArrayList<ApprovalItem>();
    public final ApkReleaseInfo apk = new ApkReleaseInfo();

    // Opções vindas das configurações de widgets (http://127.0.0.1:8765/)
    public int shortcutsColumns = 7;
    public boolean showAudioMixer = true;
    public boolean allowProcessKill = true;
    public boolean showHomeDate = true;
    public boolean showHomeWeather = true;
    public boolean showDiscordControl = true;
    public boolean showDiscordChannels = true;
    public boolean showDiscordNotifs = true;
    public final List<String> quotaFilterProviders = new ArrayList<String>();
    public final List<String> worldClockZones = new ArrayList<String>();

    // Históricos de 60 amostras para gráficos Canvas nativos
    public final FloatHistory histCpu = new FloatHistory(60);
    public final FloatHistory histRam = new FloatHistory(60);
    public final FloatHistory histGpu = new FloatHistory(60);
    public final FloatHistory histTemp = new FloatHistory(60);

    public static final class FloatHistory {
        private final float[] data;
        private int count = 0;
        private int head = 0;

        public FloatHistory(int capacity) {
            this.data = new float[capacity];
        }

        public void push(float value) {
            if (count < data.length) {
                data[(head + count) % data.length] = value;
                count++;
            } else {
                data[head] = value;
                head = (head + 1) % data.length;
            }
        }

        public int size() {
            return count;
        }

        public int capacity() {
            return data.length;
        }

        public float get(int index) {
            return data[(head + index) % data.length];
        }
    }

    public static final class PcStats {
        public float cpuPct;
        public boolean hasCpuTemp;
        public float cpuTemp;
        public String cpuName = "Processador";
        public float ramUsedGb;
        public float ramTotalGb = 1f;
        public long netDownBps;
        public long netUpBps;
        public long uptimeSec;
        public boolean hasGpu;
        public String gpuName = "";
        public float gpuUtil;
        public float gpuTemp;
        public boolean hasGpuTemp;
        public boolean hasGpuPower;
        public float gpuPower;
        public float gpuPowerLimit;
        public boolean hasGpuVram;
        public float gpuVramUsed;
        public float gpuVramTotal;
        public float gpuSharedUsed;
        public float gpuSharedTotal;
        public boolean hasGpuClock;
        public int gpuClock;
        public boolean hasGpuMemClock;
        public int gpuMemClock;
        public boolean hasGpuFan;
        public int gpuFan;
        public String gpuDriver = "";
        public final List<DiskInfo> disks = new ArrayList<DiskInfo>();

        public int ramPct() {
            if (ramTotalGb <= 0f) return 0;
            return Math.max(0, Math.min(100, Math.round((ramUsedGb / ramTotalGb) * 100f)));
        }
    }

    public static final class DiskInfo {
        public String name = "";
        public float usedGb;
        public float totalGb = 1f;
    }

    public static final class MediaInfo {
        public String app = "";
        public String title = "";
        public String artist = "";
        public String album = "";
        public boolean playing;
        public int artId;
        public boolean hasArt;

        public String artKey() {
            return title + "|" + artist + "|" + artId + "|" + (hasArt ? "1" : "0");
        }
    }

    public static final class AudioInfo {
        public int volume;
        public boolean muted;
    }

    public static final class AudioSession {
        public String id = "";
        public String name = "";
        public String processName = "";
        public int volume;
        public boolean muted;
        public boolean active;
    }

    public static final class PingInfo {
        public String host = "1.1.1.1";
        public boolean hasMs;
        public float ms;
        public boolean hasAvgMs;
        public float avgMs;
        public float lossPct;
    }

    public static final class PrivacyInfo {
        public boolean mic;
        public boolean camera;
        public String micApps = "";
        public String cameraApps = "";
    }

    public static final class DisplayConfig {
        public boolean showSeconds = true;
        public boolean hour12 = false;
        public String orientation = "auto";
        public String theme = "default";
        public String accent = "#8bd0ff";
        public boolean controlsTop = false;
        public boolean volumeKeys = true;
        public boolean hasVolumeKeys = false;
        public int idleReturnSec = 90;
        public int idleDimMin = 0;
        public String homePage = "home";
        public int cardRadiusDp = -1;
        public boolean borderlessCards = false;
    }

    public static final class NightConfig {
        public boolean enabled = true;
        public String start = "00:30";
        public String end = "07:30";
        public float brightness = 0.02f;
    }

    public static final class PageTab {
        public String id = "";
        public String title = "";
        public int viewIdx; // 0..6
        public int idleSec = -1;
    }

    public static final class ShortcutItem {
        public String id = "";
        public String label = "";
        public String color = "#2dd4bf";
        public String iconRev = "";
        public boolean hold;
    }

    public static final class ProcessGroup {
        public String name = "";
        public String title = "";
        public int count = 1;
        public float cpu;
        public float memMb;
        public boolean locked;
        public boolean hasIcon = true;

        public String displayName() {
            return (title != null && title.length() > 0) ? title : name;
        }
    }

    public static final class QuotaProvider {
        public String id = "";
        public String name = "";
        public String plan = "";
        public String extra = "";
        public String error = "";
        public final List<QuotaLimit> limits = new ArrayList<QuotaLimit>();
    }

    public static final class QuotaLimit {
        public String label = "";
        public float percent;
        public String severity = "";
        public String resetDescription = "";
        public long resetsAt;
    }

    public static final class AiSession {
        public String id = "";
        public String tool = "";
        public String title = "";
        public String status = "";
        public String model = "";
        public String lastTool = "";
        public long totalTokens;
    }

    public static final class GithubPr {
        public String repo = "";
        public int number;
        public String title = "";
    }

    public static final class PushEntry {
        public String key = "";
        public String value = "";
        public String sub = "";
        public long at;
    }

    public static final class DiscordState {
        public boolean enabled;
        public boolean controlAllowed = true;
        public boolean connected;
        public String error = "";
        public String user = "";
        public boolean muted;
        public boolean deafened;
        public String inputMode = "";
        public String pttKey = "";
        public String channelId = "";
        public String channelName = "";
        public String channelKind = "";
        public final List<DiscordMember> members = new ArrayList<DiscordMember>();
        public final List<DiscordChannel> channels = new ArrayList<DiscordChannel>();
        public final List<DiscordNotif> notifications = new ArrayList<DiscordNotif>();
    }

    public static final class DiscordMember {
        public String id = "";
        public String name = "";
        public String avatar = "";
        public boolean speaking;
        public boolean muted;
        public boolean deafened;
    }

    public static final class DiscordChannel {
        public String id = "";
        public String name = "";
        public String guild = "";
        public String guildId = "";
        public int peopleCount;
        public String peopleSummary = "";
    }

    public static final class DiscordNotif {
        public String title = "";
        public String body = "";
        public long ts;
    }

    public static final class WeatherData {
        public boolean ok;
        public String city = "Rio de Janeiro, Brasil";
        public double lat = -22.9068;
        public double lon = -43.1729;
        public String timezone = "America/Sao_Paulo";
        public String tzAbbr = "UTC-3";
        public String unit = "C";
        public float temp;
        public float feels;
        public float hi;
        public float lo;
        public int humidity;
        public float precipMm;
        public int rainProb;
        public float windKmh;
        public int windDir;
        public float pressureHpa;
        public float uv;
        public float uvMax;
        public boolean isDay = true;
        public String sunrise = "";
        public String sunset = "";
        public int daylightSec;
        public int code;
        public int aqi;
        public final List<HourlyPoint> hourly = new ArrayList<HourlyPoint>();
        public final List<DailyPoint> daily = new ArrayList<DailyPoint>();
    }

    public static final class HourlyPoint {
        public String time = "";
        public float temp;
        public int pop;
        public int code;
    }

    public static final class DailyPoint {
        public String date = "";
        public float hi;
        public float lo;
        public int code;
        public int pop;
    }

    public static final class ApprovalItem {
        public String id = "";
        public String source = "";
        public String project = "";
        public String cwd = "";
        public String tool = "";
        public String detail = "";
    }

    public static final class LiveEvent {
        public long seq;
        public String kind = "";
        public String page = "";
        public String title = "";
        public String text = "";
        public String level = "";
        public int seconds;
    }

    public static final class ApkReleaseInfo {
        public boolean available = false;
        public int versionCode = 0;
        public String versionName = "";
        public long sizeBytes = 0L;
        public String sha256 = "";
        public String url = "/legacy/PhoneBox-HP71.apk";

        public String releaseKey() {
            return versionCode + ":" + (sha256 == null ? "" : sha256);
        }
    }
}

