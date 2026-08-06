package com.officewars.application.usecase;

import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Room;
import org.springframework.stereotype.Service;

/**
 * Late de vida periódico del cliente mientras la pestaña sigue abierta. Deliberadamente NO
 * publica por WebSocket: es solo para actualizar lastActivityAt y no debe hacer que el resto de
 * jugadores refresquen su estado a cada rato.
 */
@Service
public class HeartbeatUseCase {

    private final RoomRepository rooms;

    public HeartbeatUseCase(RoomRepository rooms) {
        this.rooms = rooms;
    }

    public void execute(String code, String playerId) {
        rooms.findByCode(code).ifPresent(room -> {
            room.findPlayer(playerId).ifPresent(player -> {
                player.touch();
                rooms.save(room);
            });
        });
    }
}
