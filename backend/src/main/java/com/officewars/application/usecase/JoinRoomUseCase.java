package com.officewars.application.usecase;

import com.officewars.application.GameDefaults;
import com.officewars.application.GameException;
import com.officewars.application.GameModeRegistry;
import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Office;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import com.officewars.domain.mode.GameMode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JoinRoomUseCase {

    private final RoomRepository rooms;
    private final GameModeRegistry modes;
    private final GameEventPublisher publisher;

    public JoinRoomUseCase(RoomRepository rooms, GameModeRegistry modes, GameEventPublisher publisher) {
        this.rooms = rooms;
        this.modes = modes;
        this.publisher = publisher;
    }

    public Player execute(String code, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new GameException("El nickname es obligatorio");
        }
        Room room = rooms.findByCode(code)
                .orElseThrow(() -> new GameException("Sala no encontrada: " + code));

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new GameException("La partida ya comenzó; no se puede unir");
        }
        if (room.getPlayers().size() >= room.getMaxPlayers()) {
            throw new GameException("La sala está llena");
        }

        GameMode mode = modes.get(room.getModeId());
        Office office = new Office(room.getOfficeWidth(), room.getOfficeHeight());
        mode.autoArrange(office); // layout por defecto; el jugador puede reordenar en preparación

        String color = GameDefaults.COLORS[room.getPlayers().size() % GameDefaults.COLORS.length];
        Player player = new Player(shortId(), nickname.trim(), color, GameDefaults.LIVES, office);
        room.addPlayer(player);
        room.addFeed("JOIN", player.getNickname() + " entró a la sala");

        rooms.save(room);
        publisher.notifyRoomChanged(code);
        return player;
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
