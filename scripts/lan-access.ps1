<#
.SYNOPSIS
  Habilita o deshabilita el acceso a Office Wars (puerto 5173) desde otros equipos de tu red local.

.DESCRIPTION
  Crea (o reutiliza) una regla de firewall de entrada para el puerto 5173, detectando
  automáticamente el perfil de red activo (Private, Public, DomainAuthenticated) en vez de
  asumir "Private" a ciegas — ese es el error típico: crear la regla para "Private" cuando la
  red real es "DomainAuthenticated" (redes de empresa), lo que deja la regla creada pero sin
  efecto. Nunca crea la regla para el perfil "Public" (redes no confiables como cafés o
  aeropuertos) salvo que se use -IncludePublic explícitamente.

  Debe ejecutarse en una PowerShell como Administrador (clic derecho → "Ejecutar como
  administrador"), igual que los comandos manuales documentados en el README.

.PARAMETER On
  Crea/habilita la regla de firewall y muestra la URL para conectarte desde otro equipo.

.PARAMETER Off
  Deshabilita la regla de firewall (no la borra; usa -Remove para eso).

.PARAMETER Remove
  Junto con -Off, borra la regla de firewall por completo en vez de solo deshabilitarla.

.PARAMETER IncludePublic
  Permite incluir el perfil "Public" al habilitar. No recomendado — úsalo solo si sabes lo que
  haces.

.EXAMPLE
  .\scripts\lan-access.ps1 -On
.EXAMPLE
  .\scripts\lan-access.ps1 -Off
.EXAMPLE
  .\scripts\lan-access.ps1
  # Sin parámetros: solo muestra el estado actual, no cambia nada.
#>
param(
    [switch]$On,
    [switch]$Off,
    [switch]$Remove,
    [switch]$IncludePublic
)

$ErrorActionPreference = "Stop"
$Port = 5173
$RuleName = "Office Wars $Port"

function Get-ActiveProfiles {
    $categories = Get-NetConnectionProfile | Select-Object -ExpandProperty NetworkCategory
    if (-not $IncludePublic) {
        $categories = $categories | Where-Object { $_ -ne "Public" }
    }
    return $categories | Select-Object -Unique
}

function Show-Status {
    $rule = Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue
    if (-not $rule) {
        Write-Host "Sin regla de firewall para el puerto $Port todavía. Usa -On para crearla." -ForegroundColor Yellow
        return
    }
    $state = if ($rule.Enabled -eq "True") { "HABILITADA" } else { "deshabilitada" }
    Write-Host "Regla '$RuleName': $state (perfiles: $($rule.Profile))" -ForegroundColor Cyan
}

if ($On) {
    $profiles = Get-ActiveProfiles
    if (-not $profiles) {
        Write-Host "No hay ningún perfil de red confiable activo (¿estás en una red 'Public'? usa -IncludePublic si de verdad quieres exponerlo ahí)." -ForegroundColor Red
        exit 1
    }
    $profileList = $profiles -join ","

    $existing = Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue
    if ($existing) {
        Set-NetFirewallRule -DisplayName $RuleName -Enabled True -Profile $profileList
        Write-Host "Regla '$RuleName' ya existía — habilitada para: $profileList" -ForegroundColor Green
    } else {
        New-NetFirewallRule -DisplayName $RuleName -Direction Inbound -Protocol TCP `
            -LocalPort $Port -Action Allow -Profile $profileList | Out-Null
        Write-Host "Regla '$RuleName' creada para: $profileList" -ForegroundColor Green
    }

    $ip = Get-NetIPAddress -AddressFamily IPv4 |
        Where-Object { $_.InterfaceAlias -notmatch "Loopback|vEthernet|WSL" -and $_.PrefixOrigin -ne "WellKnown" } |
        Select-Object -First 1 -ExpandProperty IPAddress
    Write-Host ""
    if ($ip) {
        Write-Host "Desde otro equipo de la misma red: http://${ip}:${Port}" -ForegroundColor Cyan
    }
    exit 0
}

if ($Off) {
    $existing = Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue
    if (-not $existing) {
        Write-Host "No había ninguna regla '$RuleName' que quitar." -ForegroundColor Yellow
        exit 0
    }
    if ($Remove) {
        Remove-NetFirewallRule -DisplayName $RuleName
        Write-Host "Regla '$RuleName' eliminada." -ForegroundColor Green
    } else {
        Disable-NetFirewallRule -DisplayName $RuleName
        Write-Host "Regla '$RuleName' deshabilitada (usa -Off -Remove para borrarla del todo)." -ForegroundColor Green
    }
    exit 0
}

Show-Status
