package com.officewars.application.usecase;

import com.officewars.application.GameDefaults;
import com.officewars.application.GameException;
import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import org.springframework.stereotype.Service;

/** Pasa la sala de WAITING a PREPARING: cada jugador acomoda su oficina antes de la caza. */
@Service
public class StartGameUseCase {

    private final RoomRepository rooms;
    private final GameEventPublisher publisher;

    public StartGameUseCase(RoomRepository rooms, GameEventPublisher publisher) {
        this.rooms = rooms;
        this.publisher = publisher;
    }

    public Room execute(String code) {
        Room room = rooms.findByCode(code)
                .orElseThrow(() -> new GameException("Sala no encontrada: " + code));

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new GameException("La sala no está en espera");
        }
        if (room.getPlayers().size() < GameDefaults.MIN_PLAYERS_TO_START) {
            throw new GameException("Se necesitan al menos " + GameDefaults.MIN_PLAYERS_TO_START + " jugadores");
        }

        room.getPlayers().forEach(p -> p.setReady(false));
        room.setStatus(RoomStatus.PREPARING);
        room.addFeed("SYSTEM", "¡A preparar sus oficinas! Escondan su avatar.");

        rooms.save(room);
        publisher.notifyRoomChanged(code);
        return room;
    }
}
