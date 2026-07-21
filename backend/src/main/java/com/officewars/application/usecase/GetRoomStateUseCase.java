package com.officewars.application.usecase;

import com.officewars.application.GameException;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Room;
import org.springframework.stereotype.Service;

@Service
public class GetRoomStateUseCase {

    private final RoomRepository rooms;

    public GetRoomStateUseCase(RoomRepository rooms) {
        this.rooms = rooms;
    }

    public Room execute(String code) {
        return rooms.findByCode(code)
                .orElseThrow(() -> new GameException("Sala no encontrada: " + code));
    }
}
