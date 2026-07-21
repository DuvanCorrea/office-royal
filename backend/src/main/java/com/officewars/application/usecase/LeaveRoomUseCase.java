package com.officewars.application.usecase;

import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
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

    public LeaveRoomUseCase(RoomRepository rooms, GameEventPublisher publisher) {
        this.rooms = rooms;
        this.publisher = publisher;
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

        room.getPlayers().remove(player.get());
        room.getTurnOrder().remove(playerId);
        room.addFeed("LEAVE", player.get().getNickname() + " salió de la sala");

        if (room.getPlayers().isEmpty()) {
            rooms.delete(code);
            return;
        }

        // Si el que se fue tenía el turno, pasa al siguiente vivo.
        if (playerId.equals(room.currentPlayerId())) {
            room.advanceTurn();
        }
        if (room.getCurrentTurnIndex() >= room.getTurnOrder().size()) {
            room.setCurrentTurnIndex(0);
        }

        rooms.save(room);
        publisher.notifyRoomChanged(code);
    }
}
