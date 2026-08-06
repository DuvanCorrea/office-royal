# Office Wars

Juego web multijugador **por turnos asíncrono**, tipo **Batalla Naval de oficina**: cada jugador
esconde su avatar y coloca objetos en su propia oficina, y por turnos dispara a ciegas al tablero
de un rival. Construido como un **framework de "Game Modes"**: Office Wars es el primer modo; el
motor sirve para Hospital / School / Warehouse / Mall Wars.

> Lee el [Plan Maestro](./MASTER_PLAN.md) para la visión, arquitectura y roadmap completos.

## Stack

- **Backend**: Spring Boot 3 (Java 21), Clean Architecture / DDD, WebSocket (STOMP), PostgreSQL.
- **Frontend**: React + TypeScript + Vite, Zustand, React Query, STOMP. Estilo tipo Duolingo con
  modo claro/oscuro.
- **Infra**: Docker Compose (`db` + `backend` + `frontend` con Nginx).

## Levantar todo (una sola orden)

Requisitos: Docker + Docker Compose.

```bash
cp .env.example .env      # opcional, ya trae valores por defecto
docker compose up --build
```

Luego abre **http://localhost:5173**.

> **Puertos**: el frontend usa el `5173` y el backend publica el `8090` en el host
> (configurable con `BACKEND_PORT` en `.env`; se usa 8090 para no chocar con otros servidores
> locales en el 8080). El navegador siempre habla con el frontend: Nginx hace de proxy de
> `/api` y `/ws` hacia el backend por la red interna de Docker.

## Jugar en tu red local (WiFi de casa u oficina)

**No hay que instalar nada extra.** Docker Desktop publica el puerto en todas las interfaces y
el frontend usa rutas relativas, así que funciona desde cualquier dispositivo de la misma red.

> **Si usas Podman en Windows en vez de Docker Desktop**, esto **no** es automático: Podman
> corre dentro de WSL2 y el reenvío de puertos de WSL (`wslrelay.exe`) solo escucha en
> `localhost` por defecto — el puerto ni siquiera es alcanzable por tu propia IP de red, no solo
> desde otros equipos. Hace falta un paso extra (`netsh interface portproxy`) además del
> firewall; `scripts\lan-access.ps1 -On` hace las dos cosas. Si `http://<tu-ip>:5173` no carga
> **ni desde tu propio PC**, es justo este problema y no el firewall — revisa la sección de abajo.

1. Averigua la IP local del PC que corre Docker:
   - Windows: `ipconfig` → "Dirección IPv4" de tu adaptador de red (ej. `10.67.0.202`).
     Ignora las de `vEthernet (WSL...)` o `Default Switch`: esas son virtuales.
   - Linux/macOS: `ip addr` / `ifconfig`.
2. Desde el celular o cualquier PC de la misma WiFi, abre:

   ```
   http://<IP-DEL-PC>:5173
   ```

   Por ejemplo `http://10.67.0.202:5173`.

3. Si no carga, casi siempre es el **firewall de Windows**. Primero mira **qué perfil** tiene tu
   red, porque la regla debe crearse para ese perfil:

   ```powershell
   Get-NetConnectionProfile | Select-Object Name, InterfaceAlias, NetworkCategory
   ```

   Luego, en PowerShell **como administrador**, permite el puerto para el perfil que te salió
   (`Private` en casa, `DomainAuthenticated` → usa `Domain` en una red corporativa):

   ```powershell
   New-NetFirewallRule -DisplayName "Office Wars 5173" -Direction Inbound `
     -Protocol TCP -LocalPort 5173 -Action Allow -Profile Domain,Private
   ```

   > ⚠️ Error típico: crear la regla con `-Profile Private` cuando la red es `DomainAuthenticated`.
   > La regla existe pero **no aplica** y parece que "el firewall no es el problema".
   > Nunca uses `Public`: son redes no confiables (cafés, aeropuertos).

   **Atajo:** `scripts/lan-access.ps1` hace justo esto — detecta el perfil de red activo solo
   (nunca `Public`, salvo que uses `-IncludePublic`) y crea la regla para el perfil correcto —
   y con Podman, además crea el port proxy que hace falta (ver nota de arriba). Revisa que
   diga que cada paso se aplicó de verdad: si no corres la PowerShell como administrador, los
   comandos fallan con "Acceso denegado" y el script lo va a marcar en rojo (no como éxito).
   En PowerShell **como administrador**, desde la raíz del proyecto:

   ```powershell
   .\scripts\lan-access.ps1 -On    # habilita y muestra la URL para el otro equipo
   .\scripts\lan-access.ps1        # sin parámetros: solo muestra el estado actual
   .\scripts\lan-access.ps1 -Off   # deshabilita (usa -Off -Remove para borrar la regla)
   ```

### Si aun así no entra (redes de empresa)

En una red corporativa el firewall suele estar **gestionado por política de grupo (GPO)** y la red
puede tener **aislamiento de clientes**, que impide que dos equipos se vean entre sí — por diseño,
y no se arregla desde tu PC.

Cómo distinguirlo, desde el otro equipo:

```powershell
ping 10.67.0.202                      # ¿hay ruta hasta el PC?
Test-NetConnection 10.67.0.202 -Port 5173   # ¿el puerto responde?
```

- **`ping` falla** → la red bloquea el tráfico entre equipos (aislamiento) o están en subredes
  distintas. No hay nada que configurar en el juego; usa otra red.
- **`ping` responde pero el puerto no** → es el firewall del PC anfitrión (revisa el perfil de la
  regla, punto 3).

Alternativas cuando la red de la oficina no lo permite:
- Probar en una **red de casa** o compartiendo **datos del celular** (hotspot): ahí el perfil suelez
  ser `Private` y no hay aislamiento.
- Usar una **VPN de malla privada** como [Tailscale](https://tailscale.com) en ambos equipos: crea
  una red privada entre tus dispositivos (no publica el juego en Internet). Requiere poder instalar
  software; en equipos corporativos puede estar restringido.
- Consultar con IT antes de abrir puertos en un equipo de la empresa.

**Notas**
- Esto lo deja disponible **solo dentro de tu red local**, no en Internet.
- La IP puede cambiar si el router la reasigna; si quieres algo estable, configura una IP fija o
  una reserva DHCP en el router.
- Para que la URL sea aún más corta (`http://<IP>` sin puerto), cambia `FRONTEND_PORT=80` en `.env`.

### Cerrar el puerto (dejar de exponerlo)

Tienes tres niveles, de lo más rápido a lo más definitivo:

**1. Apagar el juego** — si nadie va a jugar, con esto ya no hay nada escuchando:

```bash
docker compose down
```

**2. Quitar la regla del firewall** — deshace el permiso de entrada. PowerShell **como
administrador**:

```powershell
.\scripts\lan-access.ps1 -Off -Remove
```

o a mano:

```powershell
Remove-NetFirewallRule -DisplayName "Office Wars 5173"
```

Para comprobar si la regla existe (o confirmar que ya no está):

```powershell
.\scripts\lan-access.ps1
# o: Get-NetFirewallRule -DisplayName "Office Wars 5173" -ErrorAction SilentlyContinue
```

> Si en lugar de borrarla solo quieres desactivarla temporalmente:
> `.\scripts\lan-access.ps1 -Off` (y luego `-On` para volver a activarla).

**3. Que Docker ni siquiera publique el puerto a la red** — lo más seguro: haz que solo escuche en
tu propio PC. En `.env` antepone `127.0.0.1:` al puerto:

```bash
FRONTEND_PORT=127.0.0.1:5173
BACKEND_PORT=127.0.0.1:8090
```

Y recrea los contenedores:

```bash
docker compose up -d
```

Así el juego sigue funcionando en `http://localhost:5173` para ti, pero **ningún otro equipo de la
red podrá entrar**, aunque el firewall lo permita. Para volver a compartirlo, quita el `127.0.0.1:`.

Verificar desde otro dispositivo: si al abrir `http://<IP-DEL-PC>:5173` da "no se puede conectar" o
se queda cargando, el puerto ya está cerrado.

## Cómo se juega

1. **Crea o únete a una sala** (lista de servidores o código de 6 caracteres).
2. **Prepara tu oficina**: arrastra tu avatar 🧑 y tus objetos, o pulsa *Ordenar automáticamente*.
   Cuando todos pulsan *Estoy listo*, empieza la partida.
3. **Por turnos**, elige un rival y haz clic en una celda de su tablero (oculto).
   - 💥 Destruir objetos da **puntos**.
   - 🎯 Encontrar su avatar le quita una **vida** (y el avatar huye a otra celda de su oficina).
4. Sin vidas = eliminado. **El último en pie gana.**

## Estructura

```
office-wars/
├── MASTER_PLAN.md          # biblia del proyecto
├── docker-compose.yml
├── backend/                # Spring Boot — toda la lógica del juego
└── frontend/               # React — solo representa el estado
```

## Desarrollo local (sin Docker)

**Backend**
```bash
cd backend
gradle bootRun    # necesita un Postgres local o usa el del compose
```

**Frontend**
```bash
cd frontend
npm install
npm run dev       # hace proxy a http://localhost:8080
```

## Pruebas

```bash
cd backend
gradle test
```
