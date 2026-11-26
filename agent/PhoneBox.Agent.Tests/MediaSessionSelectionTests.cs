using PhoneBox.Agent;
using Xunit;

public sealed class MediaSessionSelectionTests
{
    [Fact]
    public void MediaSnapshotHasArtReflectsArtId()
    {
        var noArt = new MediaSnapshot("Spotify", "Song", "Artist", "Album", true, 0, 1000, 0, true);
        var withArt = new MediaSnapshot("Spotify", "Song", "Artist", "Album", true, 0, 1000, 2, true);
        Assert.False(noArt.HasArt);
        Assert.True(withArt.HasArt);
    }

    [Fact]
    public void PlayingSpotifyWinsOverPausedOrEmptyCurrentBrowserSession()
    {
        var candidates = new[]
        {
            new MediaSessionPicker.Candidate("brave.exe", "Brave", "", "", false, true, false, true),
            new MediaSessionPicker.Candidate("SpotifyAB.SpotifyMusic!Spotify", "Spotify", "Blinding Lights", "The Weeknd", true, false, false, false),
        };
        Assert.Equal(1, MediaSessionPicker.SelectBestIndex(candidates));
    }

    [Fact]
    public void CurrentSessionBreaksTieWhenTwoPlayersArePlaying()
    {
        var candidates = new[]
        {
            new MediaSessionPicker.Candidate("Spotify.exe", "Spotify", "Track A", "Artist A", true, false, false, false),
            new MediaSessionPicker.Candidate("brave.exe", "Brave", "Video B", "Channel B", true, false, false, true),
        };
        Assert.Equal(1, MediaSessionPicker.SelectBestIndex(candidates));
    }

    [Fact]
    public void PausedSpotifyWithMetadataWinsOverClosedCurrentSession()
    {
        var candidates = new[]
        {
            new MediaSessionPicker.Candidate("msedge.exe", "Edge", "", "", false, false, true, true),
            new MediaSessionPicker.Candidate("Spotify.exe", "Spotify", "Starboy", "The Weeknd", false, true, false, false),
        };
        Assert.Equal(1, MediaSessionPicker.SelectBestIndex(candidates));
    }

    [Fact]
    public void ParseSpotifyWindowTitleSplitsTrackAndIgnoresIdleTitles()
    {
        var parsed = MediaSessionPicker.ParseSpotifyWindowTitle("The Weeknd - Blinding Lights");
        Assert.NotNull(parsed);
        Assert.Equal("The Weeknd", parsed!.Value.Artist);
        Assert.Equal("Blinding Lights", parsed.Value.Title);

        Assert.Null(MediaSessionPicker.ParseSpotifyWindowTitle("Spotify"));
        Assert.Null(MediaSessionPicker.ParseSpotifyWindowTitle("Spotify Premium"));
        Assert.Null(MediaSessionPicker.ParseSpotifyWindowTitle("Spotify Free"));
        Assert.Null(MediaSessionPicker.ParseSpotifyWindowTitle(""));
    }

    [Fact]
    public void FriendlyAppRecognizesBraveAndSpotify()
    {
        Assert.Equal("Spotify", MediaSessionPicker.FriendlyApp("SpotifyAB.SpotifyMusic_zpdnekdrzrea0!Spotify"));
        Assert.Equal("Brave", MediaSessionPicker.FriendlyApp("Brave.Q2QWMSZJL7Y7A"));
    }
}
