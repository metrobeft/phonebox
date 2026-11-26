using System.Globalization;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text.Json;
using System.Text.RegularExpressions;

namespace PhoneBox.Agent;

/// <summary>
/// Local-LAN HTTP web mode for older tablets (specifically HP 7.1 1201 / Android 4.2.2 Jelly Bean)
/// that cannot install the Android 5+ APK or run the Chromium 80+ panel. Serves an ES5-only web app
/// and strictly forwards only phone-safe API paths to private local network clients.
/// </summary>
public static partial class LegacyWeb
{
    private static readonly HttpClient WeatherHttp = new() { Timeout = TimeSpan.FromSeconds(10) };
    private static readonly SemaphoreSlim WeatherGate = new(1, 1);
    private static string? _weatherCacheKey;
    private static long _weatherCachedAt;
    private static string? _weatherCachedJson;
    private static object? _weatherCachedObj;
    private const long WeatherTtlMs = 10 * 60_000;
    private static readonly string BootCacheTag = DateTimeOffset.UtcNow.ToUnixTimeSeconds().ToString(CultureInfo.InvariantCulture);
    private static readonly Lazy<object> ApkInfoCache = new(BuildApkInfo);

    static LegacyWeb()
    {
        WeatherHttp.DefaultRequestHeaders.UserAgent.ParseAdd("PhoneBox/" + AppPaths.Version);
    }

    /// <summary>
    /// True only for loopback, RFC1918 private LAN (10/8, 172.16/12, 192.168/16), IPv4 link-local (169.254/16),
    /// and IPv6 loopback / ULA (fc00::/7) / link-local (fe80::/10). Rejects public IPs and CGNAT/Tailscale (100.64/10).
    /// </summary>
    public static bool IsPrivateAddress(IPAddress? address)
    {
        if (address is null) return false;
        if (address.IsIPv4MappedToIPv6) address = address.MapToIPv4();
        if (IPAddress.IsLoopback(address)) return true;

        if (address.AddressFamily == AddressFamily.InterNetwork)
        {
            var b = address.GetAddressBytes();
            return b[0] == 10
                || (b[0] == 172 && b[1] is >= 16 and <= 31)
                || (b[0] == 192 && b[1] == 168)
                || (b[0] == 169 && b[1] == 254)
                || b[0] == 127;
        }

        if (address.AddressFamily == AddressFamily.InterNetworkV6)
        {
            if (address.IsIPv6LinkLocal) return true;
            var b = address.GetAddressBytes();
            return (b[0] & 0xFE) == 0xFC; // fc00::/7 Unique Local Address
        }

        return false;
    }

    /// <summary>Static files and safe phone-panel API endpoints reachable on the legacy LAN web port.</summary>
    public static bool AllowedPath(string method, PathString path)
    {
        var isRead = HttpMethods.IsGet(method) || HttpMethods.IsHead(method);
        if (path == "/" || path == "/favicon.ico" || path == "/legacy" || path.StartsWithSegments("/legacy"))
            return isRead;
        if (path == "/api/legacy/weather" || path == "/api/legacy/sync" || path == "/api/legacy/apk-info")
            return isRead;
        if (path == "/api/push" || path.StartsWithSegments("/api/push"))
            return isRead; // tablet can read pushed items, not overwrite webhooks
        if (path == "/api/pair/claim" || Lan.IsWebhook(path))
            return false;
        return Lan.AllowedOverLan(path);
    }

    /// <summary>Authorizes a request arriving on the legacy LAN HTTP port.</summary>
    public static bool AuthorizeRequest(LegacyWebConfig cfg, IPAddress? remoteIp, string method, PathString path, string host, string origin)
    {
        if (!cfg.Enabled) return false;
        if (cfg.AllowPrivateLanOnly && !IsPrivateAddress(remoteIp)) return false;
        if (!AllowedPath(method, path)) return false;

        // Block cross-site POST/PUT/DELETE from external websites opened in another browser tab on the tablet.
        if (!HttpMethods.IsGet(method) && !HttpMethods.IsHead(method) && !string.IsNullOrEmpty(origin))
        {
            if (!Uri.TryCreate(origin, UriKind.Absolute, out var originUri)) return false;
            var expectedAuthority = host.Trim();
            var actualAuthority = originUri.IsDefaultPort ? originUri.Host : $"{originUri.Host}:{originUri.Port}";
            if (!string.Equals(originUri.Scheme, "http", StringComparison.OrdinalIgnoreCase)
                || !string.Equals(actualAuthority, expectedAuthority, StringComparison.OrdinalIgnoreCase))
                return false;
        }

        return true;
    }

    /// <summary>Private LAN HTTP URLs (excludes Tailscale 100.64/10) for the tablet browser.</summary>
    public static IReadOnlyList<string> LocalUrls(int port) =>
        Lan.Addresses()
            .Where(ip => IPAddress.TryParse(ip, out var parsed) && IsPrivateAddress(parsed))
            .Select(ip => $"http://{ip}:{port}")
            .Distinct(StringComparer.OrdinalIgnoreCase)
            .ToList();

    /// <summary>Serves an embedded asset from legacy/ with Android 4.2.2-friendly headers.</summary>
    public static IResult ServeAsset(string? file, HttpResponse? res = null)
    {
        var name = string.IsNullOrEmpty(file) ? "index.html" : file.TrimStart('/');
        if (!SafeAssetName().IsMatch(name) || name.Contains("..")) return Results.NotFound();
        var stream = AppPaths.Resource("legacy/" + name);
        if (stream is null) return Results.NotFound();

        var ext = Path.GetExtension(name).ToLowerInvariant();
        var contentType = ext switch
        {
            ".html" => "text/html; charset=utf-8",
            ".js" => "application/javascript; charset=utf-8",
            ".css" => "text/css; charset=utf-8",
            ".svg" => "image/svg+xml",
            ".png" => "image/png",
            ".ico" => "image/x-icon",
            ".json" => "application/json; charset=utf-8",
            ".apk" => "application/vnd.android.package-archive",
            _ => "application/octet-stream",
        };
        if (res is not null)
        {
            res.Headers.CacheControl = "no-store, no-cache, must-revalidate";
            res.Headers.Pragma = "no-cache";
            res.Headers.Expires = "0";
            if (ext == ".apk")
                res.Headers.ContentDisposition = $"attachment; filename=\"{Path.GetFileName(name)}\"";
        }
        if (ext == ".apk")
        {
            using (stream)
            using (var ms = new MemoryStream())
            {
                stream.CopyTo(ms);
                return Results.Bytes(ms.ToArray(), contentType);
            }
        }
        if (name.Equals("index.html", StringComparison.OrdinalIgnoreCase))
        {
            using (stream)
            using (var reader = new StreamReader(stream))
            {
                var html = reader.ReadToEnd()
                    .Replace("/legacy/style.css", $"/legacy/style.css?v={BootCacheTag}", StringComparison.Ordinal)
                    .Replace("/legacy/app.js", $"/legacy/app.js?v={BootCacheTag}", StringComparison.Ordinal);
                return Results.Content(html, contentType);
            }
        }
        return Results.Stream(stream, contentType);
    }

    /// <summary>Metadata of the embedded HP 7.1 native APK (computed once from embedded resources).</summary>
    public static object GetApkInfo() => ApkInfoCache.Value;

    private static object BuildApkInfo()
    {
        var pkg = "net.oxog.phonebox.hp71";
        var verCode = 0;
        var verName = "";
        var builtAt = "";
        long size = 0;
        var sha256 = "";
        var available = false;

        try
        {
            using var apkStream = AppPaths.Resource("legacy/PhoneBox-HP71.apk");
            if (apkStream is not null)
            {
                using var ms = new MemoryStream();
                apkStream.CopyTo(ms);
                var bytes = ms.ToArray();
                size = bytes.Length;
                sha256 = Convert.ToHexString(SHA256.HashData(bytes));
                available = size > 0;
            }
        }
        catch { }

        try
        {
            using var metaStream = AppPaths.Resource("legacy/apk-meta.json");
            if (metaStream is not null)
            {
                using var doc = JsonDocument.Parse(metaStream);
                var root = doc.RootElement;
                if (root.TryGetProperty("package", out var pEl) && pEl.ValueKind == JsonValueKind.String)
                    pkg = pEl.GetString() ?? pkg;
                if (root.TryGetProperty("versionCode", out var cEl) && cEl.TryGetInt32(out var vc))
                    verCode = vc;
                if (root.TryGetProperty("versionName", out var nEl) && nEl.ValueKind == JsonValueKind.String)
                    verName = nEl.GetString() ?? "";
                if (root.TryGetProperty("builtAt", out var bEl) && bEl.ValueKind == JsonValueKind.String)
                    builtAt = bEl.GetString() ?? "";
            }
        }
        catch { }

        return new
        {
            available,
            package = pkg,
            versionCode = verCode,
            versionName = verName,
            size,
            sha256,
            builtAt,
            url = "/legacy/PhoneBox-HP71.apk",
        };
    }

    /// <summary>
    /// Returns cached weather immediately when fresh for the current location/unit, or awaits a fast refresh (up to 2.5s)
    /// when the location/unit changed in settings so the tablet updates immediately after Save.
    /// </summary>
    public static async Task<object?> GetWeatherObjectAsync(WeatherConfig weather, CancellationToken ct)
    {
        var unit = weather.Unit == "fahrenheit" ? "fahrenheit" : "celsius";
        var key = $"{weather.City}|{weather.Lat.ToString("F4", CultureInfo.InvariantCulture)}|{weather.Lon.ToString("F4", CultureInfo.InvariantCulture)}|{unit}|{weather.Model}";
        var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        if (_weatherCachedObj is not null && _weatherCacheKey == key && now - _weatherCachedAt < WeatherTtlMs)
            return _weatherCachedObj;

        var refreshTask = Task.Run(async () => { try { await GetWeatherAsync(weather, CancellationToken.None); } catch { } });
        if (_weatherCachedObj is null || _weatherCacheKey != key)
        {
            try { await Task.WhenAny(refreshTask, Task.Delay(2500, ct)); }
            catch { }
        }
        return _weatherCachedObj;
    }

    /// <summary>
    /// Proxies Open-Meteo weather, 12-hour hourly forecast, 7-day daily forecast, sun/UV, and air quality
    /// through the Windows PC (modern HTTPS) so Android 4.2.2 tablets receive full live weather over local HTTP.
    /// </summary>
    public static async Task<IResult> GetWeatherAsync(WeatherConfig weather, CancellationToken ct)
    {
        var unit = weather.Unit == "fahrenheit" ? "fahrenheit" : "celsius";
        var key = $"{weather.City}|{weather.Lat.ToString("F4", CultureInfo.InvariantCulture)}|{weather.Lon.ToString("F4", CultureInfo.InvariantCulture)}|{unit}|{weather.Model}";
        var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        if (_weatherCachedJson is not null && _weatherCacheKey == key && now - _weatherCachedAt < WeatherTtlMs)
            return Results.Text(_weatherCachedJson, "application/json; charset=utf-8");

        await WeatherGate.WaitAsync(ct);
        try
        {
            now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            if (_weatherCachedJson is not null && _weatherCacheKey == key && now - _weatherCachedAt < WeatherTtlMs)
                return Results.Text(_weatherCachedJson, "application/json; charset=utf-8");

            var lat = weather.Lat.ToString(CultureInfo.InvariantCulture);
            var lon = weather.Lon.ToString(CultureInfo.InvariantCulture);
            var url = $"https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}"
                + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure,uv_index,is_day"
                + "&hourly=temperature_2m,precipitation_probability,weather_code"
                + "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max,precipitation_sum,daylight_duration"
                + "&timezone=auto&forecast_days=7";
            if (!string.IsNullOrWhiteSpace(weather.Model)) url += "&models=" + Uri.EscapeDataString(weather.Model);

            var aqUrl = $"https://air-quality-api.open-meteo.com/v1/air-quality?latitude={lat}&longitude={lon}&current=european_aqi,us_aqi,pm10,pm2_5&timezone=auto";
            var forecastTask = WeatherHttp.GetAsync(url, ct);
            var aqTask = Task.Run(async () =>
            {
                try
                {
                    using var aqResp = await WeatherHttp.GetAsync(aqUrl, ct);
                    if (!aqResp.IsSuccessStatusCode) return null;
                    return await aqResp.Content.ReadAsStringAsync(ct);
                }
                catch { return null; }
            }, ct);

            using var resp = await forecastTask;
            resp.EnsureSuccessStatusCode();
            using var doc = JsonDocument.Parse(await resp.Content.ReadAsStringAsync(ct));
            var root = doc.RootElement;
            var cur = root.GetProperty("current");
            var daily = root.GetProperty("daily");

            static double ConvertTemp(double c, string u) => u == "fahrenheit" ? Math.Round(c * 9.0 / 5.0 + 32.0, 1) : Math.Round(c, 1);
            static double GetDbl(JsonElement el, string prop, double def = 0) =>
                el.TryGetProperty(prop, out var v) && v.ValueKind == JsonValueKind.Number && v.TryGetDouble(out var d) ? d : def;
            static int GetInt(JsonElement el, string prop, int def = 0) =>
                el.TryGetProperty(prop, out var v) && v.ValueKind == JsonValueKind.Number && v.TryGetInt32(out var i) ? i : def;

            var tempC = GetDbl(cur, "temperature_2m");
            var feelsC = GetDbl(cur, "apparent_temperature", tempC);
            var humidity = GetInt(cur, "relative_humidity_2m");
            var precipMm = Math.Round(GetDbl(cur, "precipitation"), 1);
            var code = GetInt(cur, "weather_code");
            var windKmh = Math.Round(GetDbl(cur, "wind_speed_10m"), 1);
            var windDir = GetInt(cur, "wind_direction_10m");
            var pressureHpa = Math.Round(GetDbl(cur, "surface_pressure"), 1);
            var uvIndex = Math.Round(GetDbl(cur, "uv_index"), 1);
            var isDay = GetInt(cur, "is_day", 1) == 1;
            var curTimeStr = cur.TryGetProperty("time", out var ctEl) && ctEl.ValueKind == JsonValueKind.String ? ctEl.GetString() ?? "" : "";

            var tz = root.TryGetProperty("timezone", out var tzEl) && tzEl.ValueKind == JsonValueKind.String ? tzEl.GetString() ?? "America/Sao_Paulo" : "America/Sao_Paulo";
            var tzAbbr = root.TryGetProperty("timezone_abbreviation", out var tzaEl) && tzaEl.ValueKind == JsonValueKind.String ? tzaEl.GetString() ?? "" : "";
            var utcOffsetSec = GetInt(root, "utc_offset_seconds", -10800);
            var elevation = Math.Round(GetDbl(root, "elevation"), 0);

            var hiC = daily.GetProperty("temperature_2m_max")[0].GetDouble();
            var loC = daily.GetProperty("temperature_2m_min")[0].GetDouble();
            var sunrise0 = daily.TryGetProperty("sunrise", out var srArr) && srArr.GetArrayLength() > 0 ? srArr[0].GetString() ?? "" : "";
            var sunset0 = daily.TryGetProperty("sunset", out var ssArr) && ssArr.GetArrayLength() > 0 ? ssArr[0].GetString() ?? "" : "";
            var daylightSec0 = daily.TryGetProperty("daylight_duration", out var dlArr) && dlArr.GetArrayLength() > 0 && dlArr[0].TryGetDouble(out var dls) ? dls : 0;
            var rainProb0 = daily.TryGetProperty("precipitation_probability_max", out var rpArr) && rpArr.GetArrayLength() > 0 && rpArr[0].TryGetInt32(out var rp0) ? rp0 : 0;
            var uvMax0 = daily.TryGetProperty("uv_index_max", out var uvmArr) && uvmArr.GetArrayLength() > 0 && uvmArr[0].TryGetDouble(out var uvm0) ? Math.Round(uvm0, 1) : uvIndex;

            // Next 12 hours from current local hour
            var hourlyList = new List<object>(12);
            if (root.TryGetProperty("hourly", out var hourly)
                && hourly.TryGetProperty("time", out var hTimes)
                && hourly.TryGetProperty("temperature_2m", out var hTemps))
            {
                var hProbs = hourly.TryGetProperty("precipitation_probability", out var hp) ? hp : default;
                var hCodes = hourly.TryGetProperty("weather_code", out var hc) ? hc : default;
                var len = Math.Min(hTimes.GetArrayLength(), hTemps.GetArrayLength());
                var startIdx = 0;
                if (!string.IsNullOrEmpty(curTimeStr) && curTimeStr.Length >= 13)
                {
                    var curHourPrefix = curTimeStr[..13];
                    for (var i = 0; i < len; i++)
                    {
                        var tStr = hTimes[i].GetString() ?? "";
                        if (string.CompareOrdinal(tStr, curHourPrefix) >= 0) { startIdx = i; break; }
                    }
                }
                var endIdx = Math.Min(len, startIdx + 12);
                for (var i = startIdx; i < endIdx; i++)
                {
                    var tStr = hTimes[i].GetString() ?? "";
                    var hTemp = hTemps[i].ValueKind == JsonValueKind.Number ? ConvertTemp(hTemps[i].GetDouble(), unit) : 0;
                    var hProb = hProbs.ValueKind == JsonValueKind.Array && i < hProbs.GetArrayLength() && hProbs[i].ValueKind == JsonValueKind.Number ? hProbs[i].GetInt32() : 0;
                    var hCode = hCodes.ValueKind == JsonValueKind.Array && i < hCodes.GetArrayLength() && hCodes[i].ValueKind == JsonValueKind.Number ? hCodes[i].GetInt32() : 0;
                    hourlyList.Add(new { time = tStr, temp = hTemp, pop = hProb, code = hCode });
                }
            }

            // 7-day daily forecast
            var dailyList = new List<object>(7);
            if (daily.TryGetProperty("time", out var dTimes)
                && daily.TryGetProperty("temperature_2m_max", out var dMax)
                && daily.TryGetProperty("temperature_2m_min", out var dMin))
            {
                var dCodes = daily.TryGetProperty("weather_code", out var dc) ? dc : default;
                var dProbs = daily.TryGetProperty("precipitation_probability_max", out var dp) ? dp : default;
                var dSums = daily.TryGetProperty("precipitation_sum", out var ds) ? ds : default;
                var dSr = daily.TryGetProperty("sunrise", out var dsr) ? dsr : default;
                var dSs = daily.TryGetProperty("sunset", out var dss) ? dss : default;
                var dUv = daily.TryGetProperty("uv_index_max", out var duv) ? duv : default;
                var dLen = Math.Min(7, Math.Min(dTimes.GetArrayLength(), Math.Min(dMax.GetArrayLength(), dMin.GetArrayLength())));
                for (var i = 0; i < dLen; i++)
                {
                    dailyList.Add(new
                    {
                        date = dTimes[i].GetString() ?? "",
                        hi = dMax[i].ValueKind == JsonValueKind.Number ? ConvertTemp(dMax[i].GetDouble(), unit) : 0,
                        lo = dMin[i].ValueKind == JsonValueKind.Number ? ConvertTemp(dMin[i].GetDouble(), unit) : 0,
                        code = dCodes.ValueKind == JsonValueKind.Array && i < dCodes.GetArrayLength() && dCodes[i].ValueKind == JsonValueKind.Number ? dCodes[i].GetInt32() : 0,
                        pop = dProbs.ValueKind == JsonValueKind.Array && i < dProbs.GetArrayLength() && dProbs[i].ValueKind == JsonValueKind.Number ? dProbs[i].GetInt32() : 0,
                        rainMm = dSums.ValueKind == JsonValueKind.Array && i < dSums.GetArrayLength() && dSums[i].ValueKind == JsonValueKind.Number ? Math.Round(dSums[i].GetDouble(), 1) : 0,
                        sunrise = dSr.ValueKind == JsonValueKind.Array && i < dSr.GetArrayLength() ? dSr[i].GetString() ?? "" : "",
                        sunset = dSs.ValueKind == JsonValueKind.Array && i < dSs.GetArrayLength() ? dSs[i].GetString() ?? "" : "",
                        uvMax = dUv.ValueKind == JsonValueKind.Array && i < dUv.GetArrayLength() && dUv[i].ValueKind == JsonValueKind.Number ? Math.Round(dUv[i].GetDouble(), 1) : 0,
                    });
                }
            }

            // Optional Air Quality
            object? airObj = null;
            var aqJson = await aqTask;
            if (!string.IsNullOrEmpty(aqJson))
            {
                try
                {
                    using var aqDoc = JsonDocument.Parse(aqJson);
                    if (aqDoc.RootElement.TryGetProperty("current", out var aqCur))
                    {
                        airObj = new
                        {
                            euAqi = GetInt(aqCur, "european_aqi"),
                            usAqi = GetInt(aqCur, "us_aqi"),
                            pm25 = Math.Round(GetDbl(aqCur, "pm2_5"), 1),
                            pm10 = Math.Round(GetDbl(aqCur, "pm10"), 1),
                        };
                    }
                }
                catch { }
            }

            var obj = new
            {
                ok = true,
                city = weather.City ?? "Rio de Janeiro, Brasil",
                lat = Math.Round(weather.Lat, 4),
                lon = Math.Round(weather.Lon, 4),
                elevation,
                timezone = tz,
                tzAbbr,
                utcOffsetSeconds = utcOffsetSec,
                unit = unit == "fahrenheit" ? "F" : "C",
                temp = ConvertTemp(tempC, unit),
                feels = ConvertTemp(feelsC, unit),
                hi = ConvertTemp(hiC, unit),
                lo = ConvertTemp(loC, unit),
                humidity,
                precipMm,
                rainProb = rainProb0,
                windKmh,
                windDir,
                pressureHpa,
                uv = uvIndex,
                uvMax = uvMax0,
                isDay,
                sunrise = sunrise0,
                sunset = sunset0,
                daylightSec = Math.Round(daylightSec0, 0),
                code,
                hourly = hourlyList,
                daily = dailyList,
                air = airObj,
                updated = now,
            };
            var payload = JsonSerializer.Serialize(obj);
            _weatherCacheKey = key;
            _weatherCachedAt = now;
            _weatherCachedJson = payload;
            _weatherCachedObj = obj;
            return Results.Text(payload, "application/json; charset=utf-8");
        }
        catch (Exception ex) when (ex is not OperationCanceledException)
        {
            if (_weatherCachedJson is not null && _weatherCacheKey == key)
                return Results.Text(_weatherCachedJson, "application/json; charset=utf-8");
            return Results.Json(new { ok = false, city = weather.City ?? "Rio de Janeiro, Brasil", lat = weather.Lat, lon = weather.Lon, error = "unavailable" });
        }
        finally
        {
            WeatherGate.Release();
        }
    }

    /// <summary>True when saved JSON gives LegacyWeb a port colliding with the local desktop port or active Wi-Fi port.</summary>
    public static bool PortCollides(string json, int localPort)
    {
        try
        {
            using var doc = JsonDocument.Parse(json, new JsonDocumentOptions { CommentHandling = JsonCommentHandling.Skip, AllowTrailingCommas = true });
            if (doc.RootElement.ValueKind != JsonValueKind.Object) return false;
            var legacyEnabled = true;
            var legacyPort = 8788;
            var lanEnabled = false;
            var lanPort = 8766;
            foreach (var prop in doc.RootElement.EnumerateObject())
            {
                if (prop.Name.Equals("legacyWeb", StringComparison.OrdinalIgnoreCase) && prop.Value.ValueKind == JsonValueKind.Object)
                {
                    foreach (var lp in prop.Value.EnumerateObject())
                    {
                        if (lp.Name.Equals("enabled", StringComparison.OrdinalIgnoreCase) && lp.Value.ValueKind is JsonValueKind.True or JsonValueKind.False)
                            legacyEnabled = lp.Value.GetBoolean();
                        else if (lp.Name.Equals("port", StringComparison.OrdinalIgnoreCase) && lp.Value.ValueKind == JsonValueKind.Number && lp.Value.TryGetInt32(out var p))
                            legacyPort = Math.Clamp(p, 1024, 65535);
                    }
                }
                else if (prop.Name.Equals("lan", StringComparison.OrdinalIgnoreCase) && prop.Value.ValueKind == JsonValueKind.Object)
                {
                    foreach (var lp in prop.Value.EnumerateObject())
                    {
                        if (lp.Name.Equals("enabled", StringComparison.OrdinalIgnoreCase) && lp.Value.ValueKind is JsonValueKind.True or JsonValueKind.False)
                            lanEnabled = lp.Value.GetBoolean();
                        else if (lp.Name.Equals("port", StringComparison.OrdinalIgnoreCase) && lp.Value.ValueKind == JsonValueKind.Number && lp.Value.TryGetInt32(out var p))
                            lanPort = Math.Clamp(p, 1024, 65535);
                    }
                }
            }
            return legacyEnabled && (legacyPort == localPort || (lanEnabled && legacyPort == lanPort));
        }
        catch (JsonException) { return false; }
    }

    private static readonly string[] DefaultIconIds =
        ["wsl", "explorer", "vscode", "brave", "spotify", "steam", "riot", "claude", "antigravity", "calc", "settings", "snip", "taskmgr", "monitoroff", "lock"];

    /// <summary>Copies the embedded official Windows shortcut icons into the config icons folder so /api/shortcuts/icon serves them.</summary>
    public static void SeedDefaultIcons(string iconsDir)
    {
        try
        {
            Directory.CreateDirectory(iconsDir);
            foreach (var id in DefaultIconIds)
            {
                var target = Path.Combine(iconsDir, id + ".png");
                using var stream = AppPaths.Resource($"legacy/icons/{id}.png");
                if (stream is null) continue;
                using var dst = AppPaths.CreatePrivate(target);
                stream.CopyTo(dst);
            }
        }
        catch { }
    }

    [GeneratedRegex(@"^(icons/)?[A-Za-z0-9_.-]+$")]
    private static partial Regex SafeAssetName();
}

