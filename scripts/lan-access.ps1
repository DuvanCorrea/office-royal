<#
.SYNOPSIS
  Habilita o deshabilita el acceso a Office Wars (puerto 5173) desde otros equipos de tu red local.

.DESCRIPTION
  Hace dos cosas, las dos necesarias para que otro equipo de tu red pueda entrar:

  1. Regla de firewall de entrada para el puerto 5173, detectando el perfil de red activo
     (Private, Public, DomainAuthenticated) en vez de asumir "Private" a ciegas. El parametro
     -Profile de los cmdlets de firewall usa nombres distintos a NetworkCategory
     (DomainAuthenticated se llama "Domain" ahi), asi que se traduce.

  2. Un "port proxy" (netsh interface portproxy) que reenvia trafico de 0.0.0.0:5173 hacia
     127.0.0.1:5173. Esto hace falta porque Podman en Windows corre dentro de WSL2 y el
     reenvio de puertos de WSL (wslrelay.exe) solo escucha en localhost por defecto — a
     diferencia de Docker Desktop, que expone el puerto en todas las interfaces el solo. Sin
     este paso, ni siquiera tu propio PC puede llegar al juego por su IP de red (solo por
     localhost), y por supuesto tampoco otros equipos.

  Debe ejecutarse en una PowerShell como Administrador (clic derecho, "Ejecutar como
  administrador"). Si no detecta privilegios de administrador, no hace nada y avisa.

.PARAMETER On
  Crea/habilita la regla de firewall y el port proxy, y muestra la URL para conectarte desde
  otro equipo.

.PARAMETER Off
  Deshabilita la regla de firewall y borra el port proxy.

.PARAMETER Remove
  Junto con -Off, borra tambien la regla de firewall por completo en vez de solo
  deshabilitarla.

.PARAMETER IncludePublic
  Permite incluir el perfil "Public" al habilitar el firewall. No recomendado, usalo solo si
  sabes lo que haces.

.EXAMPLE
  .\scripts\lan-access.ps1 -On
.EXAMPLE
  .\scripts\lan-access.ps1 -Off
.EXAMPLE
  .\scripts\lan-access.ps1
  # Sin parametros: solo muestra el estado actual, no cambia nada.
#>
param(
    [switch]$On,
    [switch]$Off,
    [switch]$Remove,
    [switch]$IncludePublic
)

$Port = 5173
$RuleName = "Office Wars $Port"

function Test-IsAdmin {
    $id = [Security.Principal.WindowsIdentity]::GetCurrent()
    $p = New-Object Security.Principal.WindowsPrincipal($id)
    return $p.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Get-ActiveProfiles {
    # Get-NetConnectionProfile devuelve NetworkCategory: Public, Private o DomainAuthenticated.
    # El parametro -Profile de New-NetFirewallRule/Set-NetFirewallRule usa un enum DISTINTO:
    # Any, Domain, Private, Public, NotApplicable. "DomainAuthenticated" no es un valor valido
    # ahi, hay que traducirlo a "Domain".
    $map = @{
        "Public"              = "Public"
        "Private"              = "Private"
        "DomainAuthenticated" = "Domain"
    }
    $categories = Get-NetConnectionProfile | Select-Object -ExpandProperty NetworkCategory
    $profiles = $categories | ForEach-Object { $map[[string]$_] } | Where-Object { $_ }
    if (-not $IncludePublic) {
        $profiles = $profiles | Where-Object { $_ -ne "Public" }
    }
    return $profiles | Select-Object -Unique
}

function Show-Status {
    $rule = Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue
    if ($rule) {
        $state = if ($rule.Enabled -eq "True") { "HABILITADA" } else { "deshabilitada" }
        Write-Host "Firewall '$RuleName': $state (perfiles: $($rule.Profile))" -ForegroundColor Cyan
    } else {
        Write-Host "Firewall: sin regla para el puerto $Port todavia." -ForegroundColor Yellow
    }

    $proxy = netsh interface portproxy show v4tov4 | Select-String ":$Port\s"
    if ($proxy) {
        Write-Host "Port proxy: activo -> $proxy" -ForegroundColor Cyan
    } else {
        Write-Host "Port proxy: no configurado (el puerto solo es alcanzable por localhost)." -ForegroundColor Yellow
    }
}

if (($On -or $Off) -and -not (Test-IsAdmin)) {
    Write-Host "Este script necesita PowerShell como Administrador (clic derecho -> Ejecutar como administrador)." -ForegroundColor Red
    Write-Host "Sin eso, Windows rechaza los cambios de firewall y de reenvio de puertos en silencio o con 'Acceso denegado'." -ForegroundColor Red
    exit 1
}

if ($On) {
    $ok = $true

    # 1) Firewall
    $profiles = Get-ActiveProfiles
    if (-not $profiles) {
        Write-Host "No hay ningun perfil de red confiable activo (si estas en una red 'Public', usa -IncludePublic si de verdad quieres exponerlo ahi)." -ForegroundColor Red
        exit 1
    }
    $profileList = $profiles -join ","
    try {
        $existing = Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue
        if ($existing) {
            Set-NetFirewallRule -DisplayName $RuleName -Enabled True -Profile $profileList -ErrorAction Stop
            Write-Host "Firewall: regla '$RuleName' habilitada para: $profileList" -ForegroundColor Green
        } else {
            New-NetFirewallRule -DisplayName $RuleName -Direction Inbound -Protocol TCP `
                -LocalPort $Port -Action Allow -Profile $profileList -ErrorAction Stop | Out-Null
            Write-Host "Firewall: regla '$RuleName' creada para: $profileList" -ForegroundColor Green
        }
    } catch {
        Write-Host "Firewall: fallo al crear/habilitar la regla -> $($_.Exception.Message)" -ForegroundColor Red
        $ok = $false
    }

    # 2) Port proxy (necesario en Podman/WSL2 - ver .DESCRIPTION)
    try {
        netsh interface portproxy delete v4tov4 listenport=$Port listenaddress=0.0.0.0 | Out-Null
        $addResult = netsh interface portproxy add v4tov4 listenport=$Port listenaddress=0.0.0.0 connectport=$Port connectaddress=127.0.0.1 2>&1
        if ($LASTEXITCODE -ne 0) { throw "netsh salio con codigo $LASTEXITCODE : $addResult" }
        Write-Host "Port proxy: 0.0.0.0:$Port -> 127.0.0.1:$Port creado." -ForegroundColor Green
    } catch {
        Write-Host "Port proxy: fallo al crearlo -> $($_.Exception.Message)" -ForegroundColor Red
        $ok = $false
    }

    if (-not $ok) { exit 1 }

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
    $ok = $true

    $existing = Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue
    if ($existing) {
        try {
            if ($Remove) {
                Remove-NetFirewallRule -DisplayName $RuleName -ErrorAction Stop
                Write-Host "Firewall: regla '$RuleName' eliminada." -ForegroundColor Green
            } else {
                Disable-NetFirewallRule -DisplayName $RuleName -ErrorAction Stop
                Write-Host "Firewall: regla '$RuleName' deshabilitada (usa -Off -Remove para borrarla del todo)." -ForegroundColor Green
            }
        } catch {
            Write-Host "Firewall: fallo -> $($_.Exception.Message)" -ForegroundColor Red
            $ok = $false
        }
    } else {
        Write-Host "Firewall: no habia ninguna regla '$RuleName' que quitar." -ForegroundColor Yellow
    }

    try {
        $deleteResult = netsh interface portproxy delete v4tov4 listenport=$Port listenaddress=0.0.0.0 2>&1
        if ($LASTEXITCODE -ne 0) { throw "netsh salio con codigo $LASTEXITCODE : $deleteResult" }
        Write-Host "Port proxy: eliminado." -ForegroundColor Green
    } catch {
        Write-Host "Port proxy: fallo al eliminarlo -> $($_.Exception.Message)" -ForegroundColor Red
        $ok = $false
    }

    if (-not $ok) { exit 1 }
    exit 0
}

Show-Status
