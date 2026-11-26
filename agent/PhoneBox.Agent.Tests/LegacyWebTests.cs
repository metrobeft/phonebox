using System.Net;
using Microsoft.AspNetCore.Http;
using PhoneBox.Agent;
using Xunit;

/// <summary>Tests for the HP 7.1 1201 / Android 4.2.2 local-LAN legacy web mode.</summary>
public sealed class LegacyWebTests
{
    [Theory]
    [InlineData("127.0.0.1", true)]
    [InlineData("192.168.1.50", true)]
    [InlineData("10.0.0.15", true)]
    [InlineData("172.16.0.1", true)]
    [InlineData("172.31.255.254", true)]
    [InlineData("169.254.10.20", true)]
    [InlineData("::1", true)]
    [InlineData("::ffff:192.168.1.50", true)]
    [InlineData("fe80::1", true)]
    [InlineData("fd12:3456:789a::1", true)]
    [InlineData("8.8.8.8", false)]
    [InlineData("203.0.113.10", false)]
    [InlineData("172.15.255.255", false)]
    [InlineData("172.32.0.1", false)]
    [InlineData("100.64.0.1", false)]
    [InlineData("::ffff:8.8.8.8", false)]
    public void PrivateLanAddressCheckOnlyAllowsPrivateAndLoopback(string ip, bool expected) =>
        Assert.Equal(expected, LegacyWeb.IsPrivateAddress(IPAddress.Parse(ip)));

    [Fact]
    public void NullAddressIsRejected() => Assert.False(LegacyWeb.IsPrivateAddress(null));

    [Theory]
    [InlineData("GET", "/", true)]
    [InlineData("GET", "/favicon.ico", true)]
    [InlineData("GET", "/legacy", true)]
    [InlineData("GET", "/legacy/app.js", true)]
    [InlineData("GET", "/legacy/style.css", true)]
    [InlineData("GET", "/legacy/PhoneBox-HP71.apk", true)]
    [InlineData("HEAD", "/legacy/PhoneBox-HP71.apk", true)]
    [InlineData("GET", "/api/state", true)]
    [InlineData("GET", "/api/config", true)]
    [InlineData("GET", "/api/legacy/sync", true)]
    [InlineData("GET", "/api/legacy/weather", true)]
    [InlineData("GET", "/api/procs", true)]
    [InlineData("GET", "/api/push", true)]
    [InlineData("POST", "/api/volume", true)]
    [InlineData("POST", "/api/media/playpause", true)]
    [InlineData("POST", "/api/run/terminal", true)]
    [InlineData("POST", "/api/approvals/abc123", true)]
    [InlineData("POST", "/api/controls/command/audio", true)]
    [InlineData("GET", "/api/discord", true)]
    [InlineData("GET", "/api/discord/img", true)]
    [InlineData("POST", "/api/discord/mute", true)]
    [InlineData("GET", "/api/settings", false)]
    [InlineData("PUT", "/api/settings", false)]
    [InlineData("POST", "/api/restart", false)]
    [InlineData("POST", "/api/update/apply", false)]
    [InlineData("POST", "/api/claude/hook", false)]
    [InlineData("POST", "/api/pair/start", false)]
    [InlineData("POST", "/api/pair/claim", false)]
    [InlineData("POST", "/api/push/demo", false)]
    [InlineData("POST", "/api/notify", false)]
    [InlineData("POST", "/api/shortcuts/test", false)]
    [InlineData("GET", "/api/info", false)]
    [InlineData("GET", "/api/legacy/info", false)]
    public void AuthorizeRequestAllowsOnlySafePanelRoutes(string method, string path, bool expected)
    {
        var cfg = new LegacyWebConfig { Enabled = true, Port = 8788, AllowPrivateLanOnly = true };
        var lanIp = IPAddress.Parse("192.168.1.50");
        Assert.Equal(expected, LegacyWeb.AuthorizeRequest(cfg, lanIp, method, path, "192.168.1.20:8788", "http://192.168.1.20:8788"));
    }

    [Fact]
    public void PublicClientAndCrossSitePostAreBlocked()
    {
        var cfg = new LegacyWebConfig { Enabled = true, Port = 8788, AllowPrivateLanOnly = true };
        var pubIp = IPAddress.Parse("203.0.113.10");
        var lanIp = IPAddress.Parse("192.168.1.50");

        Assert.False(LegacyWeb.AuthorizeRequest(cfg, pubIp, "GET", "/", "192.168.1.20:8788", ""));
        Assert.False(LegacyWeb.AuthorizeRequest(cfg, lanIp, "POST", "/api/run/terminal", "192.168.1.20:8788", "https://evil.example"));
        Assert.True(LegacyWeb.AuthorizeRequest(cfg, lanIp, "POST", "/api/run/terminal", "192.168.1.20:8788", "http://192.168.1.20:8788"));
        Assert.True(LegacyWeb.AuthorizeRequest(cfg, lanIp, "POST", "/api/run/terminal", "192.168.1.20:8788", ""));

        cfg.Enabled = false;
        Assert.False(LegacyWeb.AuthorizeRequest(cfg, lanIp, "GET", "/api/state", "192.168.1.20:8788", ""));
    }

    [Theory]
    [InlineData("""{"legacyWeb":{"enabled":true,"port":8765}}""", 8765, true)]
    [InlineData("""{"legacyWeb":{"enabled":true,"port":8766},"lan":{"enabled":true,"port":8766}}""", 8765, true)]
    [InlineData("""{"legacyWeb":{"enabled":true,"port":8766},"lan":{"enabled":false,"port":8766}}""", 8765, false)]
    [InlineData("""{"legacyWeb":{"enabled":false,"port":8765}}""", 8765, false)]
    [InlineData("""{"legacyWeb":{"enabled":true,"port":8788}}""", 8765, false)]
    public void PortCollisionCheckMatchesLocalAndActiveLanPorts(string json, int localPort, bool collides) =>
        Assert.Equal(collides, LegacyWeb.PortCollides(json, localPort));

    [Fact]
    public void EmbeddedLegacyAssetsExist()
    {
        Assert.NotNull(AppPaths.Resource("legacy/index.html"));
        Assert.NotNull(AppPaths.Resource("legacy/style.css"));
        Assert.NotNull(AppPaths.Resource("legacy/app.js"));
        Assert.NotNull(AppPaths.Resource("legacy/PhoneBox-HP71.apk"));
        foreach (var asset in new[] { "theme_anime.png", "theme_hack.png", "theme_lofi.png", "theme_starfall.png", "theme_miku.png", "theme_fantasy.png", "miku_mascot.png" })
            Assert.NotNull(AppPaths.Resource($"legacy/{asset}"));
        foreach (var id in new[] { "wsl", "explorer", "vscode", "brave", "spotify", "steam", "riot", "claude", "antigravity", "calc", "settings", "snip", "taskmgr", "monitoroff", "lock" })
            Assert.NotNull(AppPaths.Resource($"legacy/icons/{id}.png"));
    }

    [Fact]
    public void ConfigDefaultsEnableLegacyWebAndDisableUpstreamAutoUpdate()
    {
        var path = Path.Combine(Path.GetTempPath(), $"phonebox-legacy-{Guid.NewGuid():N}.json");
        File.WriteAllText(path, "{}");
        try
        {
            var cfg = new ConfigStore(path).Get();
            Assert.True(cfg.LegacyWeb.Enabled);
            Assert.Equal(8788, cfg.LegacyWeb.Port);
            Assert.True(cfg.LegacyWeb.AllowPrivateLanOnly);
            Assert.False(cfg.Updates.Auto);
        }
        finally { File.Delete(path); }
    }
}

