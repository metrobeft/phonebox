using System.Diagnostics;
using System.Runtime.InteropServices;
using Windows.Media.Control;

namespace PhoneBox.Agent;

/// <summary>Watches and controls the current Windows media session (Spotify, browsers, ...).</summary>
public sealed class MediaMonitor(ILogger<MediaMonitor> log) : BackgroundService
{
    private GlobalSystemMediaTransportControlsSessionManager? _mgr;
    private volatile MediaSnapshot? _snap;
    private readonly MediaArtworkCache _art = new();
    private readonly SemaphoreSlim _pollGate = new(1, 1);
    private GlobalSystemMediaTransportControlsSession? _observed;
    private GlobalSystemMediaTransportControlsSession? _activeSession;
    private long _revision;
    private string? _lastError;

    public AlbumArt? Art => _art.Current;
    public MediaSnapshot? Snapshot() => _snap;

    protected override async Task ExecuteAsync(CancellationToken ct)
    {
        try
        {
            while (!ct.IsCancellationRequested)
            {
                try
                {
                    if (_mgr is null)
                    {
                        _mgr = await GlobalSystemMediaTransportControlsSessionManager.RequestAsync();
                        _mgr.CurrentSessionChanged += SessionChanged;
                        _mgr.SessionsChanged += SessionsChanged;
                        _lastError = null;
                    }
                    await Poll(ct);
                }
                catch (OperationCanceledException) when (ct.IsCancellationRequested) { break; }
                catch (Exception ex)
                {
                    if (_lastError != ex.Message)
                    {
                        _lastError = ex.Message;
                        log.LogWarning("media: {Msg}", ex.Message);
                    }
                    // Recover automatically if WinRT SMTC RPC channel disconnects or explorer restarts.
                    if (ex is COMException or ObjectDisposedException or InvalidOperationException)
                        ResetManager();
                    var fallback = TryReadSpotifyWindowFallback();
                    if (fallback is not null) _snap = fallback;
                    else _snap = null;
                }
                await Task.Delay(1000, ct);
            }
        }
        finally
        {
            ResetManager();
            _art.Clear();
        }
    }

    private void ResetManager()
    {
        if (_mgr is not null)
        {
            try { _mgr.CurrentSessionChanged -= SessionChanged; } catch { }
            try { _mgr.SessionsChanged -= SessionsChanged; } catch { }
            _mgr = null;
        }
        _activeSession = null;
        Observe(null);
    }

    private void SessionChanged(GlobalSystemMediaTransportControlsSessionManager sender, CurrentSessionChangedEventArgs args)
    {
        Interlocked.Increment(ref _revision);
        _ = Task.Run(() => TriggerQuickPoll());
    }

    private void SessionsChanged(GlobalSystemMediaTransportControlsSessionManager sender, SessionsChangedEventArgs args)
    {
        Interlocked.Increment(ref _revision);
        _ = Task.Run(() => TriggerQuickPoll());
    }

    private void PropertiesChanged(GlobalSystemMediaTransportControlsSession sender, MediaPropertiesChangedEventArgs args)
    {
        Interlocked.Increment(ref _revision);
        _ = Task.Run(() => TriggerQuickPoll());
    }

    private void PlaybackInfoChanged(GlobalSystemMediaTransportControlsSession sender, PlaybackInfoChangedEventArgs args)
    {
        Interlocked.Increment(ref _revision);
        _ = Task.Run(() => TriggerQuickPoll());
    }

    private async Task TriggerQuickPoll()
    {
        try { await Poll(); } catch { }
    }

    private void Observe(GlobalSystemMediaTransportControlsSession? session)
    {
        if (ReferenceEquals(session, _observed)) return;
        if (_observed is not null)
        {
            try { _observed.MediaPropertiesChanged -= PropertiesChanged; } catch { }
            try { _observed.PlaybackInfoChanged -= PlaybackInfoChanged; } catch { }
        }
        _observed = session;
        if (_observed is not null)
        {
            try { _observed.MediaPropertiesChanged += PropertiesChanged; } catch { }
            try { _observed.PlaybackInfoChanged += PlaybackInfoChanged; } catch { }
        }
    }

    private async Task Poll(CancellationToken ct = default)
    {
        await _pollGate.WaitAsync(ct);
        var before = _snap;
        try { await PollCore(ct); }
        finally
        {
            _pollGate.Release();
            // a new track, a pause or a seek reaches the phone now, not at its next whole-second state
            if (StateSignal.MediaChanged(before, _snap)) StateSignal.Poke();
        }
    }

    private sealed class InspectedSession
    {
        public required GlobalSystemMediaTransportControlsSession Session { get; init; }
        public required MediaSessionPicker.Candidate Candidate { get; init; }
        public GlobalSystemMediaTransportControlsSessionMediaProperties? Props { get; init; }
        public GlobalSystemMediaTransportControlsSessionPlaybackInfo? Playback { get; init; }
        public GlobalSystemMediaTransportControlsSessionTimelineProperties? Timeline { get; init; }
    }

    private async Task PollCore(CancellationToken ct)
    {
        if (_mgr is null)
        {
            _activeSession = null;
            Observe(null);
            _snap = TryReadSpotifyWindowFallback();
            if (_snap is null) _art.Clear();
            return;
        }

        GlobalSystemMediaTransportControlsSession? current = null;
        try { current = _mgr.GetCurrentSession(); } catch { }

        var allSessions = new List<GlobalSystemMediaTransportControlsSession>();
        try
        {
            var list = _mgr.GetSessions();
            if (list is not null)
            {
                foreach (var item in list)
                    if (item is not null) allSessions.Add(item);
            }
        }
        catch { }

        if (current is not null && !allSessions.Any(x => ReferenceEquals(x, current)
            || string.Equals(x.SourceAppUserModelId, current.SourceAppUserModelId, StringComparison.OrdinalIgnoreCase)))
        {
            allSessions.Insert(0, current);
        }

        // Inspect every SMTC session independently so a broken browser tab never hides Spotify or another active player.
        var inspected = new List<InspectedSession>(allSessions.Count);
        foreach (var sess in allSessions)
        {
            ct.ThrowIfCancellationRequested();
            try
            {
                var appId = sess.SourceAppUserModelId ?? "";
                GlobalSystemMediaTransportControlsSessionPlaybackInfo? pb = null;
                try { pb = sess.GetPlaybackInfo(); } catch { }
                GlobalSystemMediaTransportControlsSessionMediaProperties? props = null;
                try { props = await sess.TryGetMediaPropertiesAsync(); } catch { }
                GlobalSystemMediaTransportControlsSessionTimelineProperties? tl = null;
                try { tl = sess.GetTimelineProperties(); } catch { }

                var status = pb?.PlaybackStatus ?? GlobalSystemMediaTransportControlsSessionPlaybackStatus.Closed;
                var isCurrent = current is not null && (ReferenceEquals(sess, current)
                    || string.Equals(appId, current.SourceAppUserModelId, StringComparison.OrdinalIgnoreCase));
                var friendly = MediaSessionPicker.FriendlyApp(appId);
                var title = (props?.Title ?? "").Trim();
                var artist = (props?.Artist ?? "").Trim();

                inspected.Add(new InspectedSession
                {
                    Session = sess,
                    Props = props,
                    Playback = pb,
                    Timeline = tl,
                    Candidate = new MediaSessionPicker.Candidate(
                        AppId: appId,
                        FriendlyApp: friendly,
                        Title: title,
                        Artist: artist,
                        IsPlaying: status == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing,
                        IsPaused: status == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Paused
                            || status == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Opened
                            || status == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Changing,
                        IsClosed: status == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Closed
                            || status == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Stopped,
                        IsCurrent: isCurrent),
                });
            }
            catch (OperationCanceledException) when (ct.IsCancellationRequested) { throw; }
            catch { /* Ignore broken/exiting individual session and keep inspecting the rest. */ }
        }

        var bestIdx = MediaSessionPicker.SelectBestIndex(inspected.Select(x => x.Candidate).ToList());
        if (bestIdx < 0)
        {
            _activeSession = null;
            Observe(null);
            var fallback = TryReadSpotifyWindowFallback();
            _snap = fallback;
            if (fallback is null) _art.Clear();
            return;
        }

        var chosen = inspected[bestIdx];
        var s = chosen.Session;
        _activeSession = s;
        Observe(s);

        var props2 = chosen.Props;
        var pb2 = chosen.Playback;
        var tl2 = chosen.Timeline;
        var playing = chosen.Candidate.IsPlaying;
        var titleText = chosen.Candidate.Title;
        var artistText = chosen.Candidate.Artist;
        var albumText = (props2?.AlbumTitle ?? "").Trim();
        var appText = chosen.Candidate.FriendlyApp;

        // If SMTC reports Playing with empty title (common with some players/streams), still show it instead of "Nothing playing".
        if (string.IsNullOrEmpty(titleText))
        {
            if (string.Equals(appText, "Spotify", StringComparison.OrdinalIgnoreCase) && TryReadSpotifyWindowFallback() is { } spWin)
            {
                titleText = spWin.Title;
                if (string.IsNullOrEmpty(artistText)) artistText = spWin.Artist;
            }
            else if (playing)
            {
                titleText = !string.IsNullOrEmpty(artistText) ? artistText : (!string.IsNullOrEmpty(appText) ? appText : "Reproduzindo mídia");
            }
            else if (TryReadSpotifyWindowFallback() is { } spFallback)
            {
                _snap = spFallback;
                return;
            }
            else
            {
                _snap = null;
                _art.Clear();
                return;
            }
        }

        var revision = Interlocked.Read(ref _revision);
        var key = $"{chosen.Candidate.AppId}|{titleText}|{artistText}";
        // Refresh after metadata events and while a new track's thumbnail is still settling.
        try
        {
            await _art.UpdateAsync(key, revision, props2?.Thumbnail is { } thumb ? async token =>
            {
                using var stream = await thumb.OpenReadAsync();
                if (stream.Size > 8 * 1024 * 1024) return null;
                using var ms = new MemoryStream();
                await stream.AsStreamForRead().CopyToAsync(ms, token);
                return ms.Length > 0 ? new AlbumArt(ms.ToArray(), string.IsNullOrEmpty(stream.ContentType) ? "image/png" : stream.ContentType) : null;
            } : null, ct);
        }
        catch (OperationCanceledException) when (ct.IsCancellationRequested) { throw; }
        catch (Exception ex) { log.LogDebug("cover art: {Msg}", ex.Message); }

        var sampledAt = Stopwatch.GetTimestamp();
        var start = tl2?.StartTime ?? TimeSpan.Zero;
        var pos = (tl2?.Position ?? TimeSpan.Zero) - start;
        if (playing && tl2 is not null && tl2.LastUpdatedTime.Year > 2000)
            pos += DateTimeOffset.UtcNow - tl2.LastUpdatedTime;
        var dur = tl2 is not null ? (tl2.EndTime - start) : TimeSpan.Zero;
        if (dur > TimeSpan.Zero && pos > dur) pos = dur;
        if (pos < TimeSpan.Zero) pos = TimeSpan.Zero;

        _snap = new MediaSnapshot(
            App: appText,
            Title: titleText,
            Artist: artistText,
            Album: albumText,
            Playing: playing,
            PosMs: (long)pos.TotalMilliseconds,
            DurMs: (long)Math.Max(0, dur.TotalMilliseconds),
            ArtId: Art is null ? 0 : _art.Id,
            CanSeek: pb2?.Controls?.IsPlaybackPositionEnabled == true) { SampledAt = sampledAt };
    }

    /// <summary>
    /// Fallback for Spotify Desktop when SMTC has no session (e.g. desktop media overlay disabled):
    /// Spotify sets its main window title to "Artist - Track" while playing and "Spotify"/"Spotify Premium" when paused.
    /// </summary>
    private static MediaSnapshot? TryReadSpotifyWindowFallback()
    {
        try
        {
            var mySession = Process.GetCurrentProcess().SessionId;
            var procs = Process.GetProcessesByName("Spotify");
            try
            {
                foreach (var p in procs)
                {
                    try
                    {
                        if (p.SessionId != mySession) continue;
                        var parsed = MediaSessionPicker.ParseSpotifyWindowTitle(p.MainWindowTitle);
                        if (parsed is not null)
                        {
                            return new MediaSnapshot(
                                App: "Spotify",
                                Title: parsed.Value.Title,
                                Artist: parsed.Value.Artist,
                                Album: "",
                                Playing: true,
                                PosMs: 0,
                                DurMs: 0,
                                ArtId: 0,
                                CanSeek: false) { SampledAt = Stopwatch.GetTimestamp() };
                        }
                    }
                    catch { }
                }
            }
            finally
            {
                foreach (var p in procs) p.Dispose();
            }
        }
        catch { }
        return null;
    }

    private GlobalSystemMediaTransportControlsSession? ResolveControlSession()
    {
        if (_activeSession is not null) return _activeSession;
        try
        {
            var cur = _mgr?.GetCurrentSession();
            if (cur is not null) return cur;
            var all = _mgr?.GetSessions();
            if (all is { Count: > 0 })
            {
                foreach (var s in all)
                {
                    try
                    {
                        if (s.GetPlaybackInfo()?.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing)
                            return s;
                    }
                    catch { }
                }
                return all[0];
            }
        }
        catch { }
        return null;
    }

    public async Task<bool> Control(string action, long? ms)
    {
        var s = ResolveControlSession();
        if (s is null)
        {
            // No SMTC session: send global Windows media keys (controls Spotify / browser even without SMTC).
            switch (action)
            {
                case "playpause": Keys.Tap(Keys.VK_MEDIA_PLAY_PAUSE); return true;
                case "next": Keys.Tap(Keys.VK_MEDIA_NEXT_TRACK); return true;
                case "prev": Keys.Tap(Keys.VK_MEDIA_PREV_TRACK); return true;
                default: return false;
            }
        }
        bool ok;
        try
        {
            ok = action switch
            {
                "playpause" => await s.TryTogglePlayPauseAsync(),
                "next" => await s.TrySkipNextAsync(),
                "prev" => await s.TrySkipPreviousAsync(),
                "seek" when ms is { } m => await s.TryChangePlaybackPositionAsync(
                    (s.GetTimelineProperties()?.StartTime.Ticks ?? 0L) + TimeSpan.FromMilliseconds(m).Ticks),
                _ => false,
            };
        }
        catch
        {
            ok = false;
        }
        if (!ok && action is "playpause" or "next" or "prev")
        {
            Keys.Tap(action == "playpause" ? Keys.VK_MEDIA_PLAY_PAUSE : action == "next" ? Keys.VK_MEDIA_NEXT_TRACK : Keys.VK_MEDIA_PREV_TRACK);
            ok = true;
        }
        try { await Poll(); } catch { }
        return ok;
    }

    public async Task<bool> PauseAsync()
    {
        var s = ResolveControlSession();
        if (s is null || s.GetPlaybackInfo().PlaybackStatus != GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing)
            return false;
        var ok = await s.TryPauseAsync();
        try { await Poll(); } catch { }
        return ok;
    }
}
