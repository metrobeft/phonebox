package net.oxog.phonebox.hp71.theme;

import android.graphics.Color;

/**
 * Paleta centralizada de cores do Design System (Theme Studio v2.0).
 * Suporta mundos animados completos: Hack 2.0, Sakura Storm (alias anime),
 * Lofi Room, Starfall Aurora e Hatsune Miku 01, além dos presets clássicos.
 */
public final class ThemeColors {

    public final String id;
    public final boolean isHack;
    /** True para todos os mundos cênicos translúcidos (sakura, lofi, starfall, miku). */
    public final boolean isAnime;
    public final boolean isSakura;
    public final boolean isLofi;
    public final boolean isStarfall;
    public final boolean isMiku;

    public final int bg;
    public final int topBar;
    public final int navBar;
    public final int cardBg;
    public final int cardAltBg;
    public final int cardHighlightBg;
    public final int surfaceControl;
    public final int surfacePressed;
    public final int border;
    public final int divider;
    public final int trackBg;

    public final int textPrimary;
    public final int textSecondary;
    public final int textMuted;
    public final int textOnAccent;

    public final int accent;
    public final int accentDim;
    public final int green;
    public final int greenDim;
    public final int sky;
    public final int skyDim;
    public final int purple;
    public final int amber;
    public final int amberDim;
    public final int red;
    public final int redDim;

    public final int glitchCyan;
    public final int glitchRed;
    public final int starGold;

    // Tokens cromáticos específicos por mundo vivo (evitam vazamento de rosa em Lofi/Starfall/Miku)
    public final int worldSheen;
    public final int worldSheenHighlight;
    public final int worldRim;
    public final int worldRimHighlight;
    public final int worldChartBg;
    public final int worldGridColor;
    public final int worldModalScrim;

    public ThemeColors(
            String id,
            int bg, int topBar, int navBar, int cardBg, int cardAltBg, int cardHighlightBg,
            int surfaceControl, int surfacePressed, int border, int divider, int trackBg,
            int textPrimary, int textSecondary, int textMuted, int accent) {
        this(id,
                bg, topBar, navBar, cardBg, cardAltBg, cardHighlightBg,
                surfaceControl, surfacePressed, border, divider, trackBg,
                textPrimary, textSecondary, textMuted, accent,
                0xFF22C55E, 0xFF083522,
                0xFF38BDF8, 0xFF0C2B45,
                0xFFA78BFA,
                0xFFF59E0B, 0xFF422506,
                0xFFEF4444, 0xFF7F1D1D,
                0xFF080B10,
                blend(accent, bg, 0.22f),
                0xFFFFD56B,
                0x14FFFFFF, 0x24FFFFFF,
                0x22FFFFFF, 0x44FFFFFF,
                bg, divider, 0xDD04060A);
    }

    public ThemeColors(
            String id,
            int bg, int topBar, int navBar, int cardBg, int cardAltBg, int cardHighlightBg,
            int surfaceControl, int surfacePressed, int border, int divider, int trackBg,
            int textPrimary, int textSecondary, int textMuted, int accent,
            int green, int greenDim,
            int sky, int skyDim,
            int purple,
            int amber, int amberDim,
            int red, int redDim,
            int textOnAccent, int accentDim, int starGold,
            int worldSheen, int worldSheenHighlight,
            int worldRim, int worldRimHighlight,
            int worldChartBg, int worldGridColor, int worldModalScrim) {
        this.id = id;
        this.isHack = "hack".equals(id);
        this.isSakura = "sakura".equals(id);
        this.isLofi = "lofi".equals(id);
        this.isStarfall = "starfall".equals(id);
        this.isMiku = "miku".equals(id);
        this.isAnime = isSakura || isLofi || isStarfall || isMiku;

        this.bg = bg;
        this.topBar = topBar;
        this.navBar = navBar;
        this.cardBg = cardBg;
        this.cardAltBg = cardAltBg;
        this.cardHighlightBg = cardHighlightBg;
        this.surfaceControl = surfaceControl;
        this.surfacePressed = surfacePressed;
        this.border = border;
        this.divider = divider;
        this.trackBg = trackBg;

        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.textMuted = textMuted;
        this.textOnAccent = textOnAccent;

        this.accent = accent;
        this.accentDim = accentDim;
        this.green = green;
        this.greenDim = greenDim;
        this.sky = sky;
        this.skyDim = skyDim;
        this.purple = purple;
        this.amber = amber;
        this.amberDim = amberDim;
        this.red = red;
        this.redDim = redDim;

        this.glitchCyan = 0xFF00F0FF;
        this.glitchRed = 0xFFFF2A55;
        this.starGold = starGold;
        this.worldSheen = worldSheen;
        this.worldSheenHighlight = worldSheenHighlight;
        this.worldRim = worldRim;
        this.worldRimHighlight = worldRimHighlight;
        this.worldChartBg = worldChartBg;
        this.worldGridColor = worldGridColor;
        this.worldModalScrim = worldModalScrim;
    }

    public static String normalizePresetId(String presetId) {
        if (presetId == null) return "default";
        String key = presetId.trim().toLowerCase();
        if ("anime".equals(key)) return "sakura";
        return key;
    }

    public static boolean isCustomLocalTheme(String presetId) {
        String key = normalizePresetId(presetId);
        return "sakura".equals(key) || "lofi".equals(key) || "starfall".equals(key)
                || "miku".equals(key) || "hack".equals(key);
    }

    public static boolean isLivingWorldId(String presetId) {
        String key = normalizePresetId(presetId);
        return "sakura".equals(key) || "lofi".equals(key) || "starfall".equals(key) || "miku".equals(key);
    }

    public static ThemeColors forPreset(String presetId, String accentHex) {
        int acc = parseColorSafe(accentHex, 0xFF8BD0FF);
        String key = normalizePresetId(presetId);

        if ("sakura".equals(key)) {
            // Sakura Storm (evolução do Anime 2.0 inspirada em 01-sakura-storm.html)
            return new ThemeColors("sakura",
                    0xFF090516, // bg
                    0xEE140C2B, // topBar
                    0xF50B061B, // navBar
                    0xCC181034, // cardBg
                    0xD4211645, // cardAltBg
                    0xDC281954, // cardHighlightBg
                    0xE62B1D59, // surfaceControl
                    0xFF422C7F, // surfacePressed
                    0xFF6B4C9A, // border
                    0xFF442F6E, // divider
                    0xFF1A1138, // trackBg
                    0xFFFFF5FD, // textPrimary
                    0xFFF4C6EE, // textSecondary
                    0xFFC0A2E8, // textMuted
                    0xFFFF7ED4, // accent (sakura blossom)
                    0xFF5BF5B8, 0xE6103D33,
                    0xFF7CE8FF, 0xE610324D,
                    0xFFC792FF,
                    0xFFFFD56B, 0xE645310E,
                    0xFFFF5C8D, 0xE64E102B,
                    0xFF170926, 0xE63D194E, 0xFFFFD56B,
                    0x14C792FF, 0x24FF7ED4,
                    0x22E8D5FF, 0x44FFE3F7,
                    0xCC110A26, 0x44C792FF, 0xE8090516);
        }

        if ("lofi".equals(key)) {
            // Lofi Room (inspirado em 06-lofi-room.html: ameixa quente + pêssego lâmpada + menta)
            return new ThemeColors("lofi",
                    0xFF130A1E, // bg (warm twilight plum melting into black Android bar)
                    0xEE1D102C, // topBar
                    0xF511081A, // navBar
                    0xCC211330, // cardBg (cozy plum glass)
                    0xD42A183D, // cardAltBg
                    0xDC351E4A, // cardHighlightBg (warm lamp-lit focus card)
                    0xE63A224F, // surfaceControl
                    0xFF52316B, // surfacePressed
                    0xFF835584, // border (warm dusty orchid)
                    0xFF4F3058, // divider
                    0xFF1D102A, // trackBg
                    0xFFFFF4EC, // textPrimary (warm parchment cream)
                    0xFFF5D3BF, // textSecondary (soft peach glow)
                    0xFFC9A6C4, // textMuted (lavender mist)
                    0xFFFFB38A, // accent (warm desk lamp peach)
                    0xFF9FE3C9, 0xE6163C34, // green (lofi mint)
                    0xFF8CE2FF, 0xE614334C, // sky (rain window blue)
                    0xFFD4A5FF,             // purple (soft plum)
                    0xFFFFE066, 0xE64A360D, // amber (fairy light gold)
                    0xFFFF6E91, 0xE64D152B, // red (rose berry)
                    0xFF1C0D26, 0xE64A253A, 0xFFFFE066,
                    0x16FFB38A, 0x26FFE066,
                    0x22FFE3D1, 0x44FFF0D9,
                    0xCC150B22, 0x44FFB38A, 0xE8130A1E);
        }

        if ("starfall".equals(key)) {
            // Starfall Aurora (inspirado em 07-starfall.html: céu cósmico + aurora esmeralda/violeta + ciano)
            return new ThemeColors("starfall",
                    0xFF020512, // bg (deep cosmic midnight)
                    0xEE07122B, // topBar
                    0xF5040A1A, // navBar
                    0xCC0A1833, // cardBg (translucent starlight sapphire)
                    0xD4102247, // cardAltBg
                    0xDC132B57, // cardHighlightBg
                    0xE6163261, // surfaceControl
                    0xFF214A88, // surfacePressed
                    0xFF2E6AA6, // border (constellation sapphire)
                    0xFF1D436E, // divider
                    0xFF0A1630, // trackBg
                    0xFFF0FBFF, // textPrimary (starlight white)
                    0xFFC4EAFF, // textSecondary (aurora ice)
                    0xFF88B4DC, // textMuted (celestial blue)
                    0xFF7FE7FF, // accent (constellation cyan)
                    0xFF6DFFB0, 0xE60B3E2E, // green (aurora emerald)
                    0xFF7FE7FF, 0xE60E3552, // sky (starlight cyan)
                    0xFFB57CFF,             // purple (aurora violet)
                    0xFFFFE066, 0xE642340A, // amber (shooting star gold)
                    0xFFFF6B9D, 0xE647112B, // red (nebula rose)
                    0xFF031024, 0xE60E3152, 0xFF6DFFB0,
                    0x147FE7FF, 0x246DFFB0,
                    0x24BCEFFF, 0x44D9FFF0,
                    0xCC061024, 0x447FE7FF, 0xE8020512);
        }

        if ("miku".equals(key)) {
            // Hatsune Miku 01 Cyber Vocal Stage (Teal #39C5BB + Stage Magenta #FF2E88 + Aqua #7FE7FF)
            return new ThemeColors("miku",
                    0xFF031015, // bg (pitch cyber vocal stage)
                    0xEE061C24, // topBar
                    0xF5031117, // navBar
                    0xCC072029, // cardBg (holographic dark teal glass)
                    0xD40A2B36, // cardAltBg
                    0xDC0D3644, // cardHighlightBg
                    0xE60F3D4C, // surfaceControl
                    0xFF185E70, // surfacePressed
                    0xFF218688, // border (vocaloid teal frame)
                    0xFF134F57, // divider
                    0xFF061B22, // trackBg
                    0xFFEEFFFE, // textPrimary (stage spotlight white)
                    0xFFB9F5F2, // textSecondary (mint aqua)
                    0xFF79C4C7, // textMuted (cyber teal slate)
                    0xFF39C5BB, // accent (iconic Hatsune Miku teal)
                    0xFF5BF5B8, 0xE6083D32, // green (stage mint)
                    0xFF7FE7FF, 0xE609374A, // sky (synth aqua)
                    0xFFFF5CA8,             // purple/magenta (Miku headset pink)
                    0xFFFFD866, 0xE642320A, // amber (stage strobe gold)
                    0xFFFF2E88, 0xE64A0B2B, // red/magenta (stage magenta)
                    0xFF021417, 0xE6093B3E, 0xFFFF2E88,
                    0x1639C5BB, 0x267FE7FF,
                    0x267FE7FF, 0x48B6FFF9,
                    0xCC04141A, 0x4439C5BB, 0xE8031015);
        }

        if ("hack".equals(key)) {
            // Zero-Day Cyberdeck Terminal
            return new ThemeColors("hack",
                    0xFF000000,
                    0xEE020A05,
                    0xF5020804,
                    0xCC031008,
                    0xD205160C,
                    0xDA061F11,
                    0xE6072213,
                    0xFF0F4025,
                    0xFF15693B,
                    0xFF0E3E22,
                    0xFF04170D,
                    0xFFE8FFF0,
                    0xFF7AF5A8,
                    0xFF3FB874,
                    0xFF00FF66,
                    0xFF00FF66, 0xE6062B15,
                    0xFF00F0FF, 0xE6042530,
                    0xFFE879F9,
                    0xFFFFB800, 0xE6332000,
                    0xFFFF2A55, 0xE63D0714,
                    0xFF020804, 0xE6052B15, 0xFF00F0FF,
                    0x1400FF66, 0x2400FF66,
                    0x2200FF66, 0x4400F0FF,
                    0xD8010A05, 0x5515693B, 0xEB000503);
        }

        if ("amoled".equals(key)) {
            return new ThemeColors("amoled",
                    0xFF000000, 0xFF050608, 0xFF050608, 0xFF0A0C10, 0xFF07090D, 0xFF0F141D,
                    0xFF131823, 0xFF1E283A, 0xFF1A202C, 0xFF141923, 0xFF161C28,
                    0xFFF1F5F9, 0xFFCBD5E1, 0xFF8592A6, acc);
        }
        if ("midnight".equals(key)) {
            return new ThemeColors("midnight",
                    0xFF060D1F, 0xFF0A1530, 0xFF0A1530, 0xFF0F1D3D, 0xFF0C1733, 0xFF162854,
                    0xFF17284E, 0xFF233A6E, 0xFF1E3361, 0xFF192B52, 0xFF1A2B50,
                    0xFFEAF2FF, 0xFFC7DAF5, 0xFF8DA7CC, acc);
        }
        if ("warm".equals(key)) {
            return new ThemeColors("warm",
                    0xFF120E0B, 0xFF1A1410, 0xFF1A1410, 0xFF221A15, 0xFF1C1611, 0xFF2C211A,
                    0xFF2E231C, 0xFF403127, 0xFF3A2D23, 0xFF30251D, 0xFF33271F,
                    0xFFF5ECE4, 0xFFD9C9BC, 0xFFA89687, acc);
        }
        if ("graphite".equals(key)) {
            return new ThemeColors("graphite",
                    0xFF111317, 0xFF171A20, 0xFF171A20, 0xFF1D2128, 0xFF181C22, 0xFF252A34,
                    0xFF272C36, 0xFF343B48, 0xFF2E3440, 0xFF262B35, 0xFF2A303B,
                    0xFFECEFF4, 0xFFCFD6E0, 0xFF9AA5B4, acc);
        }
        return new ThemeColors("default",
                0xFF0B1427, 0xFF101E36, 0xFF101E36, 0xFF152641, 0xFF12223D, 0xFF203C63,
                0xFF233C61, 0xFF354F76, 0xFF354F76, 0xFF2D4568, 0xFF2D4568,
                0xFFF2F7FF, 0xFFD9E6FA, 0xFFB2C5E1, acc);
    }

    public static int parseColorSafe(String hex, int fallback) {
        if (hex == null) return fallback;
        String s = hex.trim();
        if (s.length() == 7 && s.charAt(0) == '#') {
            try {
                return Color.parseColor(s);
            } catch (Exception ignored) { }
        }
        return fallback;
    }

    public static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    public static int blend(int fg, int bg, float ratio) {
        float inv = 1f - ratio;
        int r = Math.round(Color.red(fg) * ratio + Color.red(bg) * inv);
        int g = Math.round(Color.green(fg) * ratio + Color.green(bg) * inv);
        int b = Math.round(Color.blue(fg) * ratio + Color.blue(bg) * inv);
        return Color.rgb(r, g, b);
    }
}
