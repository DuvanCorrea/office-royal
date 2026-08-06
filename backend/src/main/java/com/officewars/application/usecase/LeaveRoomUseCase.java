package com.officewars.application.usecase;

import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.application.usecase.support.RoomPlayerRemover;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Saca a un jugador de la sala. Si la sala se queda sin nadie, se elimina para que no
 * queden "servidores vacíos" en la lista.
 */
@Service
public class LeaveRoomUseCase {

    private final RoomRepository rooms;
    private final GameEventPublisher publisher;
    private final RoomPlayerRemover remover;

    public LeaveRoomUseCase(RoomRepository rooms, GameEventPublisher publisher, RoomPlayerRemover remover) {
        this.rooms = rooms;
        this.publisher = publisher;
        this.remover = remover;
    }

    public void execute(String code, String playerId) {
        Optional<Room> found = rooms.findByCode(code);
        if (found.isEmpty()) {
            return; // sala ya eliminada: salir es idempotente
        }
        Room room = found.get();
        Optional<Player> player = room.findPlayer(playerId);
        if (player.isEmpty()) {
            return;
        }

        room.addFeed("LEAVE", player.get().getNickname() + " salió de la sala");
        boolean deleted = remover.removeAndReconcile(room, playerId, rooms);
        if (deleted) {
            return;
        }

        rooms.save(room);
        publisher.notifyRoomChanged(code);
    }
}
