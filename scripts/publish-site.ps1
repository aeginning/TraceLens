param(
    [string]$ApkPath = "releases\TraceLens-1.6.apk"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$configPath = Join-Path $projectRoot ".env.deploy"

if (-not (Test-Path -LiteralPath $configPath)) {
    throw "Brak lokalnego pliku .env.deploy."
}

$config = @{}
Get-Content -LiteralPath $configPath | ForEach-Object {
    if ($_ -and -not $_.StartsWith("#")) {
        $key, $value = $_ -split "=", 2
        $config[$key] = $value
    }
}

$required = "FTP_HOST", "FTP_PORT", "FTP_USER", "FTP_PASSWORD", "FTP_REMOTE_PATH"
foreach ($key in $required) {
    if (-not $config[$key]) { throw "Brak ustawienia $key w .env.deploy." }
}

$baseUrl = "ftp://$($config.FTP_HOST):$($config.FTP_PORT)$($config.FTP_REMOTE_PATH.TrimEnd('/'))/"
$credentials = "$($config.FTP_USER):$($config.FTP_PASSWORD)"
$siteDirectory = Join-Path $projectRoot "website"
$files = "index.php", "privacy.php", "styles.css", "app.js", "versions.json", "og.png", ".htaccess"

foreach ($fileName in $files) {
    $localPath = Join-Path $siteDirectory $fileName
    if (-not (Test-Path -LiteralPath $localPath)) { throw "Brak pliku $localPath." }
    & curl.exe --fail --silent --show-error --ftp-create-dirs `
        --user $credentials --upload-file $localPath "$baseUrl$fileName"
    if ($LASTEXITCODE -ne 0) { throw "Publikacja $fileName nie powiodła się." }
}

$screenshotsDirectory = Join-Path $siteDirectory "screenshots"
Get-ChildItem -LiteralPath $screenshotsDirectory -File | ForEach-Object {
    & curl.exe --fail --silent --show-error --ftp-create-dirs `
        --user $credentials --upload-file $_.FullName "$baseUrl/screenshots/$($_.Name)"
    if ($LASTEXITCODE -ne 0) { throw "Publikacja $($_.Name) nie powiodła się." }
}

$resolvedApk = Join-Path $projectRoot $ApkPath
if (Test-Path -LiteralPath $resolvedApk) {
    & curl.exe --fail --silent --show-error --user $credentials `
        --upload-file $resolvedApk "$baseUrl$([IO.Path]::GetFileName($resolvedApk))"
    if ($LASTEXITCODE -ne 0) { throw "Publikacja APK nie powiodła się." }
}

Write-Host "Publikacja zakończona."
