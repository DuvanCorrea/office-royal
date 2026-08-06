package com.officewars.application.usecase;

import com.officewars.application.GameException;
import com.officewars.application.GameModeRegistry;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Office;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import com.officewars.domain.mode.GameMode;
import org.springframework.stereotype.Service;

/** El servidor acomoda automáticamente la oficina del jugador (botón "Ordenar automáticamente"). */
@Service
public class AutoArrangeUseCase {

    private final RoomRepository rooms;
    private final GameModeRegistry modes;

    public AutoArrangeUseCase(RoomRepository rooms, GameModeRegistry modes) {
        this.rooms = rooms;
        this.modes = modes;
    }

    public Room execute(String code, String playerId) {
        Room room = rooms.findByCode(code)
                .orElseThrow(() -> new GameException("Sala no encontrada: " + code));
        if (room.getStatus() != RoomStatus.PREPARING) {
            throw new GameException("Solo se puede ordenar durante la preparación");
        }
        Player player = room.findPlayer(playerId)
                .orElseThrow(() -> new GameException("Jugador no encontrado"));

        GameMode mode = modes.get(room.getModeId());
        Office office = new Office(room.getOfficeWidth(), room.getOfficeHeight());
        mode.autoArrange(office);
        player.setOffice(office);
        player.touch();

        rooms.save(room);
        return room;
    }
}
