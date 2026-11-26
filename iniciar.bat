@echo off
chcp 65001 >nul
setlocal EnableExtensions
title PhoneBox - Iniciar Servidor (HP 7.1)

set "ROOT=%~dp0"
set "APP_DIR=%LOCALAPPDATA%\PhoneBox\app"
set "APP_EXE=%APP_DIR%\PhoneBoxAgent.exe"

echo ============================================================
echo   PhoneBox - Iniciando servidor local (PC + Tablet HP 7.1)
echo ============================================================
echo.

if not exist "%APP_EXE%" (
    echo [INFO] Executavel nao encontrado em "%APP_DIR%".
    echo [INFO] Compilando e publicando o agente automaticamente...
    dotnet publish "%ROOT%agent\PhoneBox.Agent" -c Release -f net10.0-windows10.0.19041.0 -o "%APP_DIR%" --nologo
    if errorlevel 1 (
        echo.
        echo [ERRO] Falha ao compilar o PhoneBoxAgent.
        pause
        exit /b 1
    )
)

echo [1/2] Iniciando PhoneBoxAgent na area de trabalho principal...
start "" /D "%APP_DIR%" "%APP_EXE%" --replace
timeout /t 1 /nobreak >nul

echo [2/2] Aguardando servidor ficar pronto...
set "READY=0"
for /L %%i in (1,1,15) do (
    curl.exe -fsS --max-time 1 "http://127.0.0.1:8765/api/info" >nul 2>&1
    if not errorlevel 1 (
        set "READY=1"
        goto :done
    )
    timeout /t 1 /nobreak >nul
)

:done
echo.
if "%READY%"=="1" (
    echo [OK] Servidor PhoneBox ativo!
    echo      - Configuracoes no PC : http://127.0.0.1:8765/
    for /f "usebackq delims=" %%u in (`powershell -NoProfile -Command "try { (Invoke-RestMethod 'http://127.0.0.1:8765/api/legacy/info' -TimeoutSec 2).urls -join ', ' } catch { '' }"`) do (
        if not "%%u"=="" echo      - Tablet HP 7.1 (LAN) : %%u
    )
    echo      - Icone disponivel na bandeja do Windows.
    echo.
    timeout /t 4 >nul
) else (
    echo [AVISO] O agente foi iniciado, mas ainda nao respondeu em http://127.0.0.1:8765/.
    echo         Verifique o log em: %LOCALAPPDATA%\PhoneBox\agent.log
    echo.
    pause
)
exit /b 0
