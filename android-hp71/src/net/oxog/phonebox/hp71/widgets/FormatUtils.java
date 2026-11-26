package net.oxog.phonebox.hp71.widgets;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Formatadores PT-BR puros (API 17, sem java.time).
 */
public final class FormatUtils {

    public static final String[] DAYS_PT = {
            "", "domingo", "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado"
    };
    public static final String[] DAYS_SHORT_PT = {
            "", "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"
    };
    public static final String[] MONTHS_PT = {
            "janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    };
    private static final String[] HOURS_WORDS_PT = {
            "meia-noite", "uma", "duas", "três", "quatro", "cinco", "seis",
            "sete", "oito", "nove", "dez", "onze", "meio-dia"
    };

    private FormatUtils() { }

    public static String pad2(int n) {
        return n < 10 ? ("0" + n) : String.valueOf(n);
    }

    public static String round1(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) return "--";
        float r = Math.round(v * 10f) / 10f;
        if (Math.abs(r - Math.round(r)) < 0.05f) return String.valueOf(Math.round(r));
        return String.format(Locale.US, "%.1f", r);
    }

    public static String fmtBytesPerSec(long bps) {
        if (bps < 0) return "0 B/s";
        if (bps >= 1048576L) return round1(bps / 1048576f) + " MB/s";
        if (bps >= 1024L) return Math.round(bps / 1024f) + " KB/s";
        return bps + " B/s";
    }

    public static String fmtUptime(long sec) {
        if (sec < 0) return "--";
        long d = sec / 86400L;
        long h = (sec % 86400L) / 3600L;
        long m = (sec % 3600L) / 60L;
        if (d > 0) return d + "d " + h + "h " + m + "m";
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }

    public static String fmtMemMb(float mb) {
        if (mb >= 1024f) return round1(mb / 1024f) + " GB";
        return Math.round(mb) + " MB";
    }

    public static String fmtTokens(long n) {
        if (n <= 0L) return "";
        if (n >= 1000000L) return round1(n / 1000000f) + "M tok";
        if (n >= 1000L) return round1(n / 1000f) + "k tok";
        return n + " tok";
    }

    public static String fmtTimeOnly(long tsMs) {
        if (tsMs <= 0L) return "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tsMs);
        return pad2(cal.get(Calendar.HOUR_OF_DAY)) + ":" + pad2(cal.get(Calendar.MINUTE)) + ":" + pad2(cal.get(Calendar.SECOND));
    }

    public static String fmtResetTime(long resetsAtMs) {
        if (resetsAtMs <= 0L) return "";
        long diffSec = Math.max(0L, (resetsAtMs - System.currentTimeMillis()) / 1000L);
        if (diffSec <= 0L) return "Resetando agora";
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(resetsAtMs);
        String hm = pad2(c.get(Calendar.HOUR_OF_DAY)) + ":" + pad2(c.get(Calendar.MINUTE));
        long days = diffSec / 86400L;
        long hrs = (diffSec % 86400L) / 3600L;
        long mins = (diffSec % 3600L) / 60L;
        if (days > 0) return "Reseta em " + days + "d " + hrs + "h (" + pad2(c.get(Calendar.DAY_OF_MONTH)) + "/" + pad2(c.get(Calendar.MONTH) + 1) + " " + hm + ")";
        if (hrs > 0) return "Reseta em " + hrs + "h " + mins + "m (" + hm + ")";
        return "Reseta em " + mins + "m (" + hm + ")";
    }

    public static String fmtHourMin(int h24, int m, boolean use12h) {
        if (!use12h) return pad2(h24) + ":" + pad2(m);
        int h = h24 % 12;
        if (h == 0) h = 12;
        return pad2(h) + ":" + pad2(m);
    }

    public static String amPmSuffix(int h24, boolean use12h) {
        if (!use12h) return "";
        return h24 >= 12 ? " PM" : " AM";
    }

    public static String fmtDatePt(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int dom = cal.get(Calendar.DAY_OF_MONTH);
        int mon = cal.get(Calendar.MONTH);
        int yr = cal.get(Calendar.YEAR);
        String dayName = (dow >= 1 && dow < DAYS_PT.length) ? DAYS_PT[dow] : "";
        String monName = (mon >= 0 && mon < MONTHS_PT.length) ? MONTHS_PT[mon] : "";
        return dayName + ", " + dom + " de " + monName + " de " + yr;
    }

    public static String wordClockPt(int h24, int m) {
        int h12 = h24 % 12;
        String period = h24 < 6 ? " da madrugada" : (h24 < 12 ? " da manhã" : (h24 < 18 ? " da tarde" : " da noite"));
        String hWord = (h24 == 0) ? "Meia-noite" : ((h24 == 12) ? "Meio-dia" : HOURS_WORDS_PT[h12]);
        hWord = Character.toUpperCase(hWord.charAt(0)) + hWord.substring(1);
        int mRound = Math.round(m / 5f) * 5;
        if (mRound >= 60) mRound = 55;
        String mText;
        if (mRound == 0) mText = (h24 == 0 || h24 == 12) ? " em ponto" : " horas";
        else if (mRound == 15) mText = " e quinze";
        else if (mRound == 30) mText = " e meia";
        else if (mRound == 45) mText = " e quarenta e cinco";
        else mText = " e " + mRound + " min";
        if (h24 == 0 || h24 == 12) period = "";
        return hWord + mText + period;
    }

    public static String extractTimePart(String iso) {
        if (iso == null) return "--:--";
        int idx = iso.indexOf('T');
        if (idx < 0 || idx + 6 > iso.length()) return iso;
        return iso.substring(idx + 1, idx + 6);
    }

    public static String wmoPt(int code) {
        switch (code) {
            case 0: return "Céu limpo";
            case 1: return "Poucas nuvens";
            case 2: return "Parcialmente nublado";
            case 3: return "Nublado";
            case 45: return "Neblina";
            case 48: return "Nevoeiro";
            case 51: return "Garoa leve";
            case 53: return "Garoa";
            case 55: return "Garoa forte";
            case 61: return "Chuva fraca";
            case 63: return "Chuva";
            case 65: return "Chuva forte";
            case 80: return "Pancadas leves";
            case 81: return "Pancadas de chuva";
            case 82: return "Chuva intensa";
            case 95:
            case 96: return "Tempestade";
            case 99: return "Tempestade forte";
            default: return "Tempo variável";
        }
    }

    public static String windDirPt(int deg) {
        String[] dirs = { "N", "NE", "L", "SE", "S", "SO", "O", "NO" };
        int norm = ((deg % 360) + 360) % 360;
        return dirs[Math.round(norm / 45f) % 8];
    }

    public static String uvLabelPt(float uv) {
        if (uv < 3f) return "Baixo";
        if (uv < 6f) return "Moderado";
        if (uv < 8f) return "Alto";
        if (uv < 11f) return "Muito Alto";
        return "Extremo";
    }

    public static String aqiLabelPt(int aqi) {
        if (aqi <= 50) return "Boa";
        if (aqi <= 100) return "Moderada";
        if (aqi <= 150) return "Sensível";
        if (aqi <= 200) return "Insalubre";
        return "Crítica";
    }

    public static String translateLimitLabel(String raw, int dupCount) {
        String s = raw == null ? "Limite" : raw;
        s = s.replace("5-hour session", "Sessão de 5 horas")
                .replace("5-hour", "Janela de 5 horas")
                .replace("Weekly", "Limite Semanal");
        if (dupCount > 1) s = s + " #" + dupCount;
        return s;
    }

    /** Usa TimeZone nativo do Android (DST-accurate para qualquer fuso IANA). */
    public static double tzOffsetHours(String tz, long utcMs) {
        if (tz == null || tz.length() == 0) return -3.0;
        TimeZone zone = TimeZone.getTimeZone(tz);
        if (zone == null) return -3.0;
        return zone.getOffset(utcMs) / 3600000.0;
    }

    public static double tzOffsetHours(String tz) {
        return tzOffsetHours(tz, System.currentTimeMillis());
    }
}

