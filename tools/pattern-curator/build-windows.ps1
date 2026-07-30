$ErrorActionPreference = "Stop"
$Python = "$env:USERPROFILE\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
if (-not (Test-Path -LiteralPath $Python)) {
    $Python = (Get-Command python -ErrorAction Stop).Source
}
$ToolRoot = Split-Path -Parent $PSCommandPath
$BuildTools = Join-Path $ToolRoot ".build-tools"
if (-not (Test-Path -LiteralPath (Join-Path $BuildTools "PyInstaller"))) {
    & $Python -m pip install --disable-pip-version-check --target $BuildTools "pyinstaller==6.15.0"
}
$env:PYTHONPATH = $BuildTools
& $Python -m PyInstaller `
    --noconfirm `
    --clean `
    --windowed `
    --onefile `
    --name "TraceLens-Pattern-Curator" `
    --distpath (Join-Path $ToolRoot "dist") `
    --workpath (Join-Path $ToolRoot "build") `
    --specpath $ToolRoot `
    (Join-Path $ToolRoot "pattern_curator.py")
