# Office Wars

Juego web multijugador **por turnos asíncrono** — reinvención de Battleship donde buscas a otros
jugadores escondidos dentro de un edificio. Construido como un **framework de "Game Modes"**:
Office Wars es el primer modo; el motor sirve para Hospital / School / Warehouse / Mall Wars.

> Lee el [Plan Maestro](./MASTER_PLAN.md) para la visión, arquitectura y roadmap completos.

## Stack

- **Backend**: Spring Boot 3 (Java 21), Clean Architecture / DDD, WebSocket (STOMP), PostgreSQL.
- **Frontend**: React + TypeScript + Vite, **Phaser** (mundo/cámara), Zustand, React Query, STOMP.
- **Infra**: Docker Compose (`db` + `backend` + `frontend`).

## Levantar todo (una sola orden)

Requisitos: Docker + Docker Compose.

```bash
cp .env.example .env      # opcional, ya trae valores por defecto
docker compose up --build
```

Luego abre **http://localhost:5173**.

## Probar la partida mínima

1. En una pestaña: **Crear sala** → copia el código (ej. `AB34JK`).
2. En otra pestaña (o navegador): **Unirse** con ese código y un nickname.
3. En la primera: **Iniciar partida**.
4. Por turnos, haz clic en una celda del edificio para **disparar**. Verás el **feed en vivo**,
   las vidas bajar y, al quedar uno solo, la **victoria**.

## Estructura

```
office-wars/
├── MASTER_PLAN.md          # biblia del proyecto
├── docker-compose.yml
├── backend/                # Spring Boot — toda la lógica del juego
└── frontend/               # React + Phaser — solo renderiza el estado
```

## Desarrollo local (sin Docker)

**Backend**
```bash
cd backend
./gradlew bootRun    # necesita un Postgres local o usa el del compose
```

**Frontend**
```bash
cd frontend
npm install
npm run dev
```
