package com.officewars.domain.mode;

import com.officewars.domain.core.Office;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;

/**
 * Estrategia que define las reglas variables de un modo de juego. El motor (salas, turnos,
 * preparación, feed, WebSocket) es fijo; cada modo (Office Wars, Hospital Wars...) implementa
 * esta interfaz. Agregar un modo = nueva implementación. No se toca el core (Open/Closed).
 */
public interface GameMode {

    String id();

    String displayName();

    int officeWidth();

    int officeHeight();

    /** Distribuye avatar y objetos automáticamente en la oficina (botón "Ordenar automáticamente"). */
    void autoArrange(Office office);

    /**
     * Resuelve un disparo del jugador de turno hacia (x, y) en la oficina del objetivo,
     * mutando el estado afectado (vida del objetivo, objetos, puntuación del atacante).
     */
    ShotResult resolveShot(Room room, Player shooter, Player target, int x, int y);

    boolean isFinished(Room room);

    String winnerId(Room room);
}
