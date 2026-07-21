# Office Wars — Plan Maestro

> Framework multijugador por turnos de "mapas ocultos". Office Wars es el primer **Game Mode**.

## 1. Visión y filosofía

Office Wars es un juego web multijugador **por turnos asíncrono**. Reinvención de Battleship:
en lugar de buscar barcos en un océano, buscas a otros jugadores escondidos dentro de un
**edificio navegable**.

- Diseñado para gente que solo tiene ratos libres: **cada turno dura 10–30s**.
- Entras cuando quieras, haces tu acción y vuelves horas después. **Sin presión de estar conectado.**
- Debe sentirse **divertido, relajado, social, impredecible**; fácil de aprender, difícil de dominar.
- Genera conversación: _"¿Quién me rompió la impresora?"_, _"Ya sé dónde se esconde Andrés"_.

**Público:** oficinas, empresas, equipos de desarrollo, universidades, amigos, comunidades de
Discord, streamers. Jugable entre personas de distintos países.

## 2. Concepto clave: Game Modes

El **motor** (salas, turnos, mapa, jugadores, eventos, WebSocket) es **fijo**. Las **reglas** cambian
según el modo de juego, enchufado con el patrón **Strategy**:

| Modo | Ambientación |
|------|--------------|
| **Office Wars** (v1) | Oficinas de una empresa |
| Hospital Wars | Salas de pacientes, quirófanos, laboratorios |
| School Wars | Aulas, biblioteca, cafetería |
| Warehouse Wars | Estanterías, montacargas, bodegas |
| Mall Wars | Tiendas, cine, plaza de comidas |

> Agregar un modo = nuevo `GameMode` + set de tiles/sprites. **No se toca el core** (Open/Closed).
> Esto convierte el proyecto en un *framework* de juegos, no en "un juego de oficina".

## 3. Arquitectura de alto nivel

**Clean Architecture** en el backend. La lógica de juego vive **100% en el servidor**; el frontend
solo representa el estado.

```
┌─────────────────────────────────────────────────────────┐
│ frontend (React + Phaser)  →  solo renderiza el estado   │
└───────────────┬─────────────────────────┬────────────────┘
        REST (acciones)            WebSocket/STOMP (feed en vivo)
                │                         │
┌───────────────┴─────────────────────────┴────────────────┐
│ infrastructure/web  (controllers REST, WebSocketConfig)   │
├───────────────────────────────────────────────────────────┤
│ application  (casos de uso · Command · puertos)           │
├───────────────────────────────────────────────────────────┤
│ domain/core  (Room, Player, Board, Turn, DomainEvents)    │
│ domain/mode  (GameMode Strategy · OfficeWarsMode)         │
├───────────────────────────────────────────────────────────┤
│ infrastructure/persistence  (JPA · Postgres)              │
└───────────────────────────────────────────────────────────┘
```

### Flujo de un turno
1. El jugador de turno hace clic en una coordenada → `POST /api/rooms/{code}/shot`.
2. `TakeShotUseCase` carga la sala, delega en `GameMode.resolveShot(...)`.
3. El dominio aplica daño / eliminación y emite **eventos de dominio** → líneas de feed.
4. Se persiste el nuevo estado y se **publica por WebSocket** a `/topic/room/{code}`.
5. Todos los clientes actualizan feed, vidas y turno en tiempo real.

## 4. Modelo de dominio (core)

- **Coordinate** `(x, y)` en la grilla.
- **Board** — dimensiones de la grilla del edificio.
- **Player** — `id`, `nickname`, `lives`, `position` (oculta), `status` (ALIVE/ELIMINATED), color.
- **Room** (agregado raíz) — `code`, `name`, `modeId`, `maxPlayers`, board, jugadores, `turnOrder`,
  `currentTurnIndex`, `status`, `winnerId`, `feed`.
- **RoomStatus** — `WAITING → PREPARING → RUNNING → FINISHED` (`ARCHIVED` reservado).
- **DomainEvent** — `ShotFired`, `PlayerHit`, `PlayerEliminated`, `GameFinished`.

### GameMode (Strategy, `domain/mode`)
```
generateBoard(width, height)          // crea el mapa del modo
placePlayer(room, player)             // posición inicial oculta
resolveShot(room, shooter, target)    // → ShotResult (MISS | HIT | ELIMINATED)
isFinished(room) / winner(room)       // condición de victoria
```
`OfficeWarsMode` es la primera implementación (mensajes de feed con temática de oficina).

## 5. Contrato de API (v1)

### REST
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/rooms` | Crea sala → `{ code }` |
| `POST` | `/api/rooms/{code}/join` | `{ nickname }` → `{ playerId }` |
| `POST` | `/api/rooms/{code}/start` | Inicia la partida |
| `POST` | `/api/rooms/{code}/shot` | `{ playerId, x, y }` |
| `GET`  | `/api/rooms/{code}/state?playerId=` | Estado (posiciones ajenas ocultas) |

### WebSocket (STOMP + SockJS)
- Endpoint de conexión: `/ws`
- Suscripción: `/topic/room/{code}` → `{ type: STATE_UPDATE | FEED, payload }`

## 6. Roadmap por fases

- **v1 · Slice vertical (ACTUAL)** — una sala, unirse por código, nickname invitado, colocar avatar,
  turnos de disparo, feed en tiempo real, victoria. Docker Compose.
- **MVP** — cuentas + JWT, crear/listar salas públicas, preparación con drag & drop de objetos,
  movimiento automático del avatar al recibir daño, Radar y objetos.
- **v1.1** — chat por sala, replays/timeline, skins, más mapas.
- **v1.2** — objetos especiales, eventos diarios (Apagón, Viernes de pizza…), poderes.
- **v2** — editor visual de mapas, IA de auto-organización, logros, torneos, ranking, API pública.

## 7. Convenciones

- **Patrones**: Repository, Factory (registro de modos), Strategy (GameMode), Command (casos de uso),
  Domain Events. No acoplar lógica de juego con la UI.
- **SOLID / Open-Closed**: agregar mapas, objetos, eventos, modos **sin modificar** el código existente.
- **Testing**: objetivo de cobertura 80% (unitarios de dominio, integración de casos de uso, E2E futuro).
- **Calidad**: lint + format; SonarQube-ready.

## 8. Notas de decisión (v1)

- **Motor genérico desde el día 1**: separación `domain/core` (no sabe qué es una oficina) vs
  `domain/mode` (las reglas). Es el corazón del framework.
- **Estado como snapshot JSON en Postgres**: evita sobre-modelar el esquema relacional antes de que
  las reglas se estabilicen. Se normalizará cuando lleguen ranking y replays.
- **Nickname invitado**: sin cuentas ni JWT todavía; se añaden en el MVP.
