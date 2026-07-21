package com.officewars.application.usecase;

import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Room;
import org.springframework.stereotype.Service;

import java.util.List;

/** Devuelve las salas públicas en espera para la lista de servidores del menú. */
@Service
public class ListOpenRoomsUseCase {

    private final RoomRepository rooms;

    public ListOpenRoomsUseCase(RoomRepository rooms) {
        this.rooms = rooms;
    }

    public List<Room> execute() {
        return rooms.findListedWaiting();
    }
}
