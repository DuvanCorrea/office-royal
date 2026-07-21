package com.officewars.application.usecase;

import com.officewars.application.GameException;
import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Marca al jugador como listo. Cuando todos lo están, la partida arranca (RUNNING). */
@Service
public class SetReadyUseCase {

    private final RoomRepository rooms;
    private final GameEventPublisher publisher;

    public SetReadyUseCase(RoomRepository rooms, GameEventPublisher publisher) {
        this.rooms = rooms;
        this.publisher = publisher;
    }

    public Room execute(String code, String playerId) {
        Room room = rooms.findByCode(code)
                .orElseThrow(() -> new GameException("Sala no encontrada: " + code));
        if (room.getStatus() != RoomStatus.PREPARING) {
            throw new GameException("La sala no está en preparación");
        }
        Player player = room.findPlayer(playerId)
                .orElseThrow(() -> new GameException("Jugador no encontrado"));
        if (player.getOffice() == null || player.getOffice().getAvatar() == null) {
            throw new GameException("Primero coloca tu avatar");
        }

        player.setReady(true);
        room.addFeed("READY", player.getNickname() + " está listo");

        if (room.allReady()) {
            List<String> order = new ArrayList<>(room.getPlayers().stream().map(Player::getId).toList());
            Collections.shuffle(order);
            room.setTurnOrder(order);
            room.setCurrentTurnIndex(0);
            room.setStatus(RoomStatus.RUNNING);
            room.addFeed("SYSTEM", "¡Que empiece la caza! Encuentra a los rivales.");
        }

        rooms.save(room);
        publisher.notifyRoomChanged(code);
        return room;
    }
}
