param(
    [string]$AndroidSdk = "$env:LOCALAPPDATA\Android\Sdk",
    [string]$JdkHome = "",
    [string]$KeystorePath = "$PSScriptRoot\phonebox-hp71-release.keystore",
    [string]$KeystorePass = "phoneboxhp71",
    [string]$KeyAlias = "phoneboxhp71",
    [switch]$SkipAgentCopy
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Optional personal overrides (ignored by Git — see local.config.example.ps1)
$localCfg = Join-Path $PSScriptRoot "local.config.ps1"
if (Test-Path $localCfg) { . $localCfg }

$root = $PSScriptRoot
$build = Join-Path $root "build"
$dist = Join-Path $root "dist"

# ── Resolve JDK ────────────────────────────────────────────────────────────────
if (-not $JdkHome) {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\javac.exe"))) {
        $JdkHome = $env:JAVA_HOME
    } elseif (Test-Path "C:\Program Files\Java\jdk-25\bin\javac.exe") {
        $JdkHome = "C:\Program Files\Java\jdk-25"
    } else {
        $javacCmd = Get-Command javac -ErrorAction SilentlyContinue
        if ($javacCmd) {
            $JdkHome = Split-Path (Split-Path $javacCmd.Source -Parent) -Parent
        }
    }
}
if (-not $JdkHome -or -not (Test-Path (Join-Path $JdkHome "bin\javac.exe"))) {
    throw "JDK nao encontrado. Informe -JdkHome ou configure JAVA_HOME."
}
$javac = Join-Path $JdkHome "bin\javac.exe"
$keytool = Join-Path $JdkHome "bin\keytool.exe"
$env:JAVA_HOME = $JdkHome
$env:PATH = "$(Join-Path $JdkHome 'bin');$env:PATH"

# ── Resolve Android SDK Build-Tools & Platform ────────────────────────────────
$btDir = Get-ChildItem (Join-Path $AndroidSdk "build-tools") -Directory | Sort-Object Name -Descending | Select-Object -First 1
if (-not $btDir) { throw "Android SDK build-tools nao encontrado em $AndroidSdk\build-tools" }

$platDir = Get-ChildItem (Join-Path $AndroidSdk "platforms") -Directory | Where-Object { Test-Path (Join-Path $_.FullName "android.jar") } | Sort-Object Name -Descending | Select-Object -First 1
if (-not $platDir) { throw "Android platform android.jar nao encontrado em $AndroidSdk\platforms" }

$aapt2     = Join-Path $btDir.FullName "aapt2.exe"
$d8        = Join-Path $btDir.FullName "d8.bat"
$zipalign  = Join-Path $btDir.FullName "zipalign.exe"
$apksigner = Join-Path $btDir.FullName "apksigner.bat"
$androidJar = Join-Path $platDir.FullName "android.jar"

Write-Host "=== PhoneBox HP 7.1 Native APK Build ===" -ForegroundColor Cyan
Write-Host "JDK:         $JdkHome"
Write-Host "Build-Tools: $($btDir.Name)"
Write-Host "Platform:    $($platDir.Name)"
Write-Host "Target:      minSdk=17 / targetSdk=17 (Android 4.2.2 Jelly Bean)"

# ── Clean & prepare directories ───────────────────────────────────────────────
if (Test-Path $build) { Remove-Item $build -Recurse -Force }
New-Item -ItemType Directory -Force -Path "$build\gen", "$build\classes", "$build\dex", $dist | Out-Null

# ── Ensure RSA 2048 keystore for v1 signing ───────────────────────────────────
if (-not (Test-Path $KeystorePath)) {
    Write-Host "[0/7] Gerando keystore RSA 2048 em $KeystorePath ..." -ForegroundColor Yellow
    & $keytool -genkeypair `
        -v `
        -keystore $KeystorePath `
        -storetype PKCS12 `
        -storepass $KeystorePass `
        -keypass $KeystorePass `
        -alias $KeyAlias `
        -keyalg RSA `
        -keysize 2048 `
        -sigalg SHA256withRSA `
        -validity 10000 `
        -dname "CN=PhoneBox HP71, OU=LocalLAN, O=PhoneBox, L=Rio de Janeiro, ST=RJ, C=BR"
    if ($LASTEXITCODE -ne 0) { throw "Falha ao gerar keystore com keytool." }
}

# ── 1. Compile Android resources (aapt2 compile) ──────────────────────────────
Write-Host "[1/7] Compilando recursos Android (aapt2 compile)..." -ForegroundColor Green
$resZip = Join-Path $build "res.zip"
& $aapt2 compile --dir (Join-Path $root "res") -o $resZip
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile falhou." }

# ── 2. Link resources & generate R.java (aapt2 link) ──────────────────────────
Write-Host "[2/7] Linkando manifesto e recursos (aapt2 link)..." -ForegroundColor Green
$unalignedApk = Join-Path $build "unaligned.apk"
& $aapt2 link `
    -o $unalignedApk `
    -I $androidJar `
    --manifest (Join-Path $root "AndroidManifest.xml") `
    --min-sdk-version 17 `
    --target-sdk-version 17 `
    --java (Join-Path $build "gen") `
    $resZip
if ($LASTEXITCODE -ne 0) { throw "aapt2 link falhou." }

# ── 3. Compile Java sources (javac -source 8 -target 8) ───────────────────────
Write-Host "[3/7] Compilando fontes Java (Java 8 bytecode para API 17)..." -ForegroundColor Green
$javaFiles = @(
    Get-ChildItem (Join-Path $root "src") -Recurse -Filter "*.java"
    Get-ChildItem (Join-Path $build "gen") -Recurse -Filter "*.java"
) | Select-Object -ExpandProperty FullName

$sourcesList = Join-Path $build "sources.txt"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($sourcesList, ($javaFiles | ForEach-Object { '"' + ($_ -replace '\\', '/') + '"' }), $utf8NoBom)

& $javac `
    -source 8 `
    -target 8 `
    -encoding UTF-8 `
    -Xlint:-options `
    -classpath $androidJar `
    -d (Join-Path $build "classes") `
    "@$sourcesList"
if ($LASTEXITCODE -ne 0) { throw "javac falhou." }

# ── 4. Dex bytecode (d8 --min-api 17) ─────────────────────────────────────────
Write-Host "[4/7] Gerando classes.dex (d8 --min-api 17)..." -ForegroundColor Green
Add-Type -AssemblyName System.IO.Compression.FileSystem
$classesJar = Join-Path $build "classes.jar"
[System.IO.Compression.ZipFile]::CreateFromDirectory((Join-Path $build "classes"), $classesJar)

& $d8 `
    --min-api 17 `
    --lib $androidJar `
    --output (Join-Path $build "dex") `
    $classesJar
if ($LASTEXITCODE -ne 0) { throw "d8 falhou." }

$dexFile = Join-Path $build "dex\classes.dex"
if (-not (Test-Path $dexFile)) { throw "classes.dex nao foi gerado pelo d8." }

# ── 5. Inject classes.dex into APK preserving resources.arsc ──────────────────
Write-Host "[5/7] Empacotando classes.dex no APK..." -ForegroundColor Green
$zip = [System.IO.Compression.ZipFile]::Open($unalignedApk, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    $existing = $zip.GetEntry("classes.dex")
    if ($existing) { $existing.Delete() }
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $dexFile, "classes.dex", [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
} finally {
    $zip.Dispose()
}

# ── 6. Zipalign (4-byte alignment) ────────────────────────────────────────────
Write-Host "[6/7] Alinhando APK (zipalign -f 4)..." -ForegroundColor Green
$alignedApk = Join-Path $build "aligned.apk"
& $zipalign -f 4 $unalignedApk $alignedApk
if ($LASTEXITCODE -ne 0) { throw "zipalign falhou." }

# ── 7. Sign APK with v1-only RSA 2048 (Jelly Bean API 17 compatible) ──────────
Write-Host "[7/7] Assinando APK com esquema v1 (RSA 2048, compativel Android 4.2.2)..." -ForegroundColor Green
$outApk = Join-Path $dist "PhoneBox-HP71.apk"
& $apksigner sign `
    --ks $KeystorePath `
    --ks-key-alias $KeyAlias `
    --ks-pass "pass:$KeystorePass" `
    --key-pass "pass:$KeystorePass" `
    --v1-signing-enabled true `
    --v2-signing-enabled false `
    --v3-signing-enabled false `
    --v4-signing-enabled false `
    --out $outApk `
    $alignedApk
if ($LASTEXITCODE -ne 0) { throw "apksigner sign falhou." }

# ── Verify signature & manifest metadata ──────────────────────────────────────
Write-Host "`n=== Verificacao (apksigner verify) ===" -ForegroundColor Cyan
& $apksigner verify --verbose $outApk
if ($LASTEXITCODE -ne 0) { throw "apksigner verify falhou." }

Write-Host "`n=== Metadados (aapt2 dump badging) ===" -ForegroundColor Cyan
$badging = & $aapt2 dump badging $outApk
$badging | Select-String -Pattern "^(package:|minSdkVersion:|sdkVersion:|targetSdkVersion:|application-label:|launchable-activity:)" | ForEach-Object { Write-Host $_.Line }

$fileInfo = Get-Item $outApk
$sizeKb = [Math]::Round($fileInfo.Length / 1024.0, 1)
$sha256 = (Get-FileHash $outApk -Algorithm SHA256).Hash

$pkgLine = ($badging | Select-String -Pattern "^package:" | Select-Object -First 1).Line
$verCode = 0
$verName = ""
$pkgName = "net.oxog.phonebox.hp71"
if ($pkgLine -match "name='([^']+)'") { $pkgName = $Matches[1] }
if ($pkgLine -match "versionCode='(\d+)'") { $verCode = [int]$Matches[1] }
if ($pkgLine -match "versionName='([^']+)'") { $verName = $Matches[1] }

$metaObj = [ordered]@{
    package     = $pkgName
    versionCode = $verCode
    versionName = $verName
    size        = $fileInfo.Length
    sha256      = $sha256
    builtAt     = (Get-Date).ToString("o")
    url         = "/legacy/PhoneBox-HP71.apk"
}
$metaJson = $metaObj | ConvertTo-Json -Depth 3
$distMeta = Join-Path $dist "apk-meta.json"
[System.IO.File]::WriteAllText($distMeta, $metaJson, $utf8NoBom)

Write-Host "`nAPK gerado com sucesso:" -ForegroundColor Green
Write-Host "  Arquivo: $outApk"
Write-Host "  Versao:  $verName (versionCode=$verCode)"
Write-Host "  Tamanho: $sizeKb KB ($($fileInfo.Length) bytes)"
Write-Host "  SHA-256: $sha256"

if (-not $SkipAgentCopy) {
    $agentLegacyDir = Join-Path (Split-Path $root -Parent) "agent\PhoneBox.Agent\legacy"
    $agentLegacyApk = Join-Path $agentLegacyDir "PhoneBox-HP71.apk"
    $agentLegacyMeta = Join-Path $agentLegacyDir "apk-meta.json"
    Copy-Item -Path $outApk -Destination $agentLegacyApk -Force
    [System.IO.File]::WriteAllText($agentLegacyMeta, $metaJson, $utf8NoBom)
    Write-Host "  Copiado para o Agente: $agentLegacyApk (+ apk-meta.json)" -ForegroundColor Cyan
}
