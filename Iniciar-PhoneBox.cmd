@echo off
title Iniciando PhoneBox Agent...
echo Reiniciando o PhoneBox Agent na sua sessao grafica do Windows...
start "" "%LOCALAPPDATA%\PhoneBox\app\PhoneBoxAgent.exe" --replace
timeout /t 2 >nul
echo OK! O PhoneBox esta ativo na bandeja do Windows:
echo   - Configuracoes PC: http://127.0.0.1:8765/
for /f "usebackq delims=" %%u in (`powershell -NoProfile -Command "try { (Invoke-RestMethod 'http://127.0.0.1:8765/api/legacy/info' -TimeoutSec 2).urls -join ', ' } catch { 'http://<IP-LOCAL-DO-PC>:8788' }"`) do echo   - Tablet HP 7.1   : %%u
timeout /t 2 >nul

