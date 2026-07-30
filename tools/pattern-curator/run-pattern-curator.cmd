@echo off
setlocal
set "BUNDLED_PYTHON=%USERPROFILE%\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
if exist "%BUNDLED_PYTHON%" (
  "%BUNDLED_PYTHON%" "%~dp0pattern_curator.py"
) else (
  py -3 "%~dp0pattern_curator.py"
)
