package com.officewars.application.usecase;

import com.officewars.application.GameDefaults;
import com.officewars.application.GameModeRegistry;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Room;
import com.officewars.domain.mode.GameMode;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class CreateRoomUseCase {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    private final RoomRepository rooms;
    private final GameModeRegistry modes;

    public CreateRoomUseCase(RoomRepository rooms, GameModeRegistry modes) {
        this.rooms = rooms;
        this.modes = modes;
    }

    public Room execute(String name, String modeId, Boolean listed) {
        String resolvedModeId = (modeId == null || modeId.isBlank()) ? modes.defaultModeId() : modeId;
        GameMode mode = modes.get(resolvedModeId);

        String roomName = (name == null || name.isBlank()) ? "Sala " + mode.displayName() : name.trim();
        Room room = new Room(generateUniqueCode(), roomName, resolvedModeId,
                GameDefaults.MAX_PLAYERS, mode.officeWidth(), mode.officeHeight());
        room.setListed(listed == null || listed);
        room.addFeed("SYSTEM", "Sala creada. ¡Compartan el código para unirse!");
        return rooms.save(room);
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length())));
            }
            code = sb.toString();
        } while (rooms.existsByCode(code));
        return code;
    }
}
