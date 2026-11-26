namespace PhoneBox.Agent;

/// <summary>
/// Cross-platform pure ranking and parsing helpers for media session selection
/// (used by Windows MediaMonitor and unit-tested under net10.0).
/// </summary>
internal static class MediaSessionPicker
{
    internal readonly record struct Candidate(
        string AppId,
        string FriendlyApp,
        string Title,
        string Artist,
        bool IsPlaying,
        bool IsPaused,
        bool IsClosed,
        bool IsCurrent);

    /// <summary>
    /// Picks the best SMTC session across all active sessions.
    /// Prefers an actively Playing session with metadata over a paused/empty GetCurrentSession().
    /// </summary>
    public static int SelectBestIndex(IReadOnlyList<Candidate> candidates)
    {
        if (candidates.Count == 0) return -1;
        var bestIdx = -1;
        var bestScore = int.MinValue;
        for (var i = 0; i < candidates.Count; i++)
        {
            var c = candidates[i];
            var hasTitle = !string.IsNullOrWhiteSpace(c.Title);
            var hasArtist = !string.IsNullOrWhiteSpace(c.Artist);
            if (c.IsClosed && !hasTitle && !c.IsPlaying) continue;

            int score;
            if (c.IsPlaying && hasTitle) score = 1000;
            else if (c.IsPlaying) score = 700;
            else if (!c.IsClosed && hasTitle) score = 400;
            else if (hasTitle) score = 150;
            else if (!c.IsClosed) score = 50;
            else continue;

            if (c.IsCurrent) score += 50;
            if (IsDedicatedMediaPlayer(c.FriendlyApp)) score += 25;
            if (hasArtist) score += 10;

            if (score > bestScore)
            {
                bestScore = score;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    public static bool IsDedicatedMediaPlayer(string friendlyApp) =>
        friendlyApp is "Spotify" or "Apple Music" or "TIDAL" or "Deezer" or "VLC"
            or "Media Player" or "foobar2000" or "MusicBee" or "PotPlayer" or "mpv" or "YouTube Music";

    public static (string Artist, string Title)? ParseSpotifyWindowTitle(string? rawTitle)
    {
        if (string.IsNullOrWhiteSpace(rawTitle)) return null;
        var t = rawTitle.Trim();
        if (t.Equals("Spotify", StringComparison.OrdinalIgnoreCase)
            || t.Equals("Spotify Free", StringComparison.OrdinalIgnoreCase)
            || t.Equals("Spotify Premium", StringComparison.OrdinalIgnoreCase)
            || t.Equals("AngleHiddenWindow", StringComparison.OrdinalIgnoreCase)
            || t.Equals("Default IME", StringComparison.OrdinalIgnoreCase)
            || t.Equals("MSCTFIME UI", StringComparison.OrdinalIgnoreCase))
            return null;

        var sep = t.IndexOf(" - ", StringComparison.Ordinal);
        if (sep <= 0 || sep + 3 >= t.Length) return null;
        var artist = t[..sep].Trim();
        var title = t[(sep + 3)..].Trim();
        if (artist.Length == 0 || title.Length == 0) return null;
        return (artist, title);
    }

    public static string FriendlyApp(string? id)
    {
        if (string.IsNullOrWhiteSpace(id)) return "Mídia";
        var l = id.ToLowerInvariant();
        if (l.Contains("spotify")) return "Spotify";
        if (l.Contains("brave")) return "Brave";
        if (l.Contains("chrome")) return "Chrome";
        if (l.Contains("firefox") || l.Contains("308046b0af4a39cb")) return "Firefox";
        if (l.Contains("msedge")) return "Edge";
        if (l.Contains("opera")) return "Opera";
        if (l.Contains("zune") || l.Contains("microsoft.zunemusic") || l.Contains("mediaplayer")) return "Media Player";
        if (l.Contains("vlc")) return "VLC";
        if (l.Contains("tidal")) return "TIDAL";
        if (l.Contains("deezer")) return "Deezer";
        if (l.Contains("apple") || l.Contains("itunes")) return "Apple Music";
        if (l.Contains("foobar")) return "foobar2000";
        if (l.Contains("musicbee")) return "MusicBee";
        if (l.Contains("potplayer")) return "PotPlayer";
        if (l.Contains("mpv")) return "mpv";
        if (l.Contains("youtube")) return "YouTube Music";
        return Path.GetFileNameWithoutExtension(id.Split('!')[0]);
    }
}

