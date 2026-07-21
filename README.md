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

**No hay que instalar nada extra.** Docker ya publica el puerto en todas las interfaces y el
frontend usa rutas relativas, así que funciona desde cualquier dispositivo de la misma red.

1. Averigua la IP local del PC que corre Docker:
   - Windows: `ipconfig` → "Dirección IPv4" de tu adaptador de red (ej. `10.67.0.202`).
     Ignora las de `vEthernet (WSL...)` o `Default Switch`: esas son virtuales.
   - Linux/macOS: `ip addr` / `ifconfig`.
2. Desde el celular o cualquier PC de la misma WiFi, abre:

   ```
   http://<IP-DEL-PC>:5173
   ```

   Por ejemplo `http://10.67.0.202:5173`.

3. Si no carga, casi siempre es el **firewall de Windows**. Abre PowerShell **como
   administrador** y permite el puerto:

   ```powershell
   New-NetFirewallRule -DisplayName "Office Wars 5173" -Direction Inbound `
     -Protocol TCP -LocalPort 5173 -Action Allow -Profile Private
   ```

   > Usa `-Profile Private` para exponerlo solo en redes marcadas como privadas (tu casa/oficina),
   > no en redes públicas.

**Notas**
- Esto lo deja disponible **solo dentro de tu red local**, no en Internet.
- La IP puede cambiar si el router la reasigna; si quieres algo estable, configura una IP fija o
  una reserva DHCP en el router.
- Para que la URL sea aún más corta (`http://<IP>` sin puerto), cambia `FRONTEND_PORT=80` en `.env`.

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
