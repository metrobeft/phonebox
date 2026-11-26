<#
.SYNOPSIS
  Allows incoming connections from your private local network (Wi-Fi/LAN) to the PhoneBox Legacy Tablet Web port.
.DESCRIPTION
  Run this script once as Administrator if Windows Firewall blocks your HP 7.1 tablet from opening
  http://<PC-IP>:8788 in its browser.
#>
param(
    [int]$Port = 8788
)

$ErrorActionPreference = 'Stop'
$identity = [Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()
if (-not $identity.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "Reopening PowerShell as Administrator to add the Private-network firewall rule for TCP port $Port ..." -ForegroundColor Yellow
    Start-Process powershell -Verb RunAs -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', "`"$PSCommandPath`"", '-Port', $Port)
    return
}

$ruleName = "PhoneBox Legacy Tablet Web (TCP $Port - Private LAN)"
$existing = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue
if ($existing) {
    Remove-NetFirewallRule -DisplayName $ruleName
}

New-NetFirewallRule `
    -DisplayName $ruleName `
    -Direction Inbound `
    -Action Allow `
    -Protocol TCP `
    -LocalPort $Port `
    -Profile Private `
    -RemoteAddress LocalSubnet `
    -Description "Allows private local-network tablets (such as HP 7.1 1201) to access the PhoneBox Legacy Web panel." | Out-Null

Write-Host "OK: Regra de firewall criada para TCP $Port (apenas rede local / Private)." -ForegroundColor Green
Start-Sleep -Seconds 2

