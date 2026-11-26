package net.oxog.phonebox.hp71.theme;

import android.graphics.Color;

/**
 * Paleta centralizada de cores do Design System.
 * Alterar presets ou adicionar novos temas aqui muda o visual inteiro do app.
 */
public final class ThemeColors {

    public final String id;
    public final boolean isHack;
    public final boolean isAnime;
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
                0xFFEF4444, 0xFF7F1D1D);
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
            int red, int redDim) {
        this.id = id;
        this.isHack = "hack".equals(id);
        this.isAnime = "anime".equals(id);
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
        this.textOnAccent = isHack ? 0xFF020804 : (isAnime ? 0xFF170926 : 0xFF080B10);

        this.accent = accent;
        this.accentDim = isHack ? 0xE6052B15 : (isAnime ? 0xE63D194E : blend(accent, bg, 0.22f));
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
        this.starGold = 0xFFFFD56B;
    }

    public static ThemeColors forPreset(String presetId, String accentHex) {
        int acc = parseColorSafe(accentHex, 0xFF8BD0FF);
        String key = presetId == null ? "default" : presetId.toLowerCase();
        if ("anime".equals(key)) {
            // Sakura Midnight // Neon Tokyo Anime HUD:
            // Fundo índigo-meia-noite profundo funde com a barra preta fixa do Android 4.2.2,
            // cards em vidro cristalino translúcido revelam o céu estrelado e pétalas de sakura flutuando.
            return new ThemeColors(key,
                    0xFF090516, // bg (deep twilight indigo, seamless toward black Android 4.2.2 bar)
                    0xEE140C2B, // topBar
                    0xF50B061B, // navBar
                    0xCC181034, // cardBg (translucent twilight crystal)
                    0xD4211645, // cardAltBg
                    0xDC281954, // cardHighlightBg (luminous sakura focus card)
                    0xE62B1D59, // surfaceControl
                    0xFF422C7F, // surfacePressed
                    0xFF6B4C9A, // border (glowing orchid frame)
                    0xFF442F6E, // divider
                    0xFF1A1138, // trackBg
                    0xFFFFF5FD, // textPrimary (moonlight pearl)
                    0xFFF4C6EE, // textSecondary (sakura mist)
                    0xFFC0A2E8, // textMuted (twilight wisteria)
                    0xFFFF7ED4, // accent (sakura blossom pink)
                    0xFF5BF5B8, 0xE6103D33, // green (mint leaf) / greenDim
                    0xFF7CE8FF, 0xE610324D, // sky (celestial cyan) / skyDim
                    0xFFC792FF,             // purple (starlight lavender)
                    0xFFFFD56B, 0xE645310E, // amber (starlight gold) / amberDim
                    0xFFFF5C8D, 0xE64E102B  // red (crimson blossom) / redDim
            );
        }
        if ("hack".equals(key)) {
            // Zero-Day Cyberdeck Terminal:
            // Fundo #000000 funde perfeitamente com a barra preta fixa do Android 4.2.2 (HP 7.1),
            // cards em vidro CRT translúcido revelam a chuva Matrix ao fundo mantendo alto contraste.
            return new ThemeColors(key,
                    0xFF000000, // bg (pitch black seamless with Android 4.2.2 system nav bar)
                    0xEE020A05, // topBar
                    0xF5020804, // navBar (melts cleanly into fixed black Android 4.2.2 bar)
                    0xCC031008, // cardBg (translucent phosphor obsidian)
                    0xD205160C, // cardAltBg
                    0xDA061F11, // cardHighlightBg
                    0xE6072213, // surfaceControl (tactile terminal keycap)
                    0xFF0F4025, // surfacePressed
                    0xFF15693B, // border (crisp matrix wireframe)
                    0xFF0E3E22, // divider
                    0xFF04170D, // trackBg
                    0xFFE8FFF0, // textPrimary (crisp phosphor white-mint)
                    0xFF7AF5A8, // textSecondary (bright terminal green)
                    0xFF3FB874, // textMuted (radar emerald, legible on dark glass)
                    0xFF00FF66, // accent (radioactive phosphor green)
                    0xFF00FF66, 0xE6062B15, // green / greenDim
                    0xFF00F0FF, 0xE6042530, // sky (electric cyber-cyan) / skyDim
                    0xFFE879F9,             // purple (glitch neon magenta)
                    0xFFFFB800, 0xE6332000, // amber (hazard terminal) / amberDim
                    0xFFFF2A55, 0xE63D0714  // red (crimson breach alert) / redDim
            );
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
