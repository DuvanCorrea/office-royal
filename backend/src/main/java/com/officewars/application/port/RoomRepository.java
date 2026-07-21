package com.officewars.application.port;

import com.officewars.domain.core.Room;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Room. Implementado en infrastructure. */
public interface RoomRepository {

    Room save(Room room);

    Optional<Room> findByCode(String code);

    boolean existsByCode(String code);

    /** Salas públicas en espera (WAITING) para la lista de servidores. */
    List<Room> findListedWaiting();
}
