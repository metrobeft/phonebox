@echo off
chcp 65001 >nul
setlocal EnableExtensions
title PhoneBox - Parar Servidor

echo ============================================================
echo   PhoneBox - Parando servidor local
echo ============================================================
echo.

tasklist /FI "IMAGENAME eq PhoneBoxAgent.exe" 2>nul | find /I "PhoneBoxAgent.exe" >nul
if errorlevel 1 (
    echo [INFO] O servidor PhoneBox ja estava parado.
    echo.
    timeout /t 2 >nul
    exit /b 0
)

echo Encerrando PhoneBoxAgent.exe...
taskkill /IM PhoneBoxAgent.exe /F >nul 2>&1

timeout /t 1 /nobreak >nul
tasklist /FI "IMAGENAME eq PhoneBoxAgent.exe" 2>nul | find /I "PhoneBoxAgent.exe" >nul
if errorlevel 1 (
    echo [OK] Servidor PhoneBox parado com sucesso!
) else (
    echo [ERRO] Nao foi possivel encerrar PhoneBoxAgent.exe. Tente executar como Administrador.
    pause
    exit /b 1
)

echo.
timeout /t 2 >nul
exit /b 0
