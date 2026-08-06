package com.officewars.application.usecase;

import com.officewars.application.GameDefaults;
import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.application.usecase.support.RoomPlayerRemover;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Barrido periódico: remueve jugadores inactivos (pestaña cerrada, wifi caída, etc. — nunca
 * llamaron a /leave) y borra la sala si queda vacía. El umbral depende del estado de la sala:
 * en espera/preparación se espera actividad activa; en curso el juego es asíncrono por diseño
 * ("vuelves horas después"), así que el umbral es mucho más laxo.
 */
@Service
public class CleanupStalePlayersUseCase {

    private final RoomRepository rooms;
    private final GameEventPublisher publisher;
    private final RoomPlayerRemover remover;

    public CleanupStalePlayersUseCase(RoomRepository rooms, GameEventPublisher publisher, RoomPlayerRemover remover) {
        this.rooms = rooms;
        this.publisher = publisher;
        this.remover = remover;
    }

    public void execute() {
        long now = System.currentTimeMillis();
        for (Room room : rooms.findAll()) {
            if (room.getStatus() == RoomStatus.FINISHED) {
                continue;
            }
            sweepRoom(room, now);
        }
    }

    private void sweepRoom(Room room, long now) {
        long timeout = room.getStatus() == RoomStatus.RUNNING
                ? GameDefaults.RUNNING_INACTIVITY_TIMEOUT_MS
                : GameDefaults.PREP_INACTIVITY_TIMEOUT_MS;

        boolean changed = false;
        for (Player p : List.copyOf(room.getPlayers())) {
            if (p.getLastActivityAt() <= 0) {
                // Dato pre-existente a este campo (deploy sobre datos viejos): sana en vez de
                // tratarlo como "inactivo desde 1970" y purgar a todo el mundo de golpe.
                p.touch();
                changed = true;
                continue;
            }
            if (now - p.getLastActivityAt() > timeout) {
                room.addFeed("TIMEOUT", p.getNickname() + " fue desconectado por inactividad");
                boolean deleted = remover.removeAndReconcile(room, p.getId(), rooms);
                changed = true;
                if (deleted) {
                    return;
                }
            }
        }

        if (changed) {
            rooms.save(room);
            publisher.notifyRoomChanged(room.getCode());
        }
    }
}
