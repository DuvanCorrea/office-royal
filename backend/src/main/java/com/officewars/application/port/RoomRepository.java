package com.officewars.application.port;

import com.officewars.domain.core.Room;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Room. Implementado en infrastructure. */
public interface RoomRepository {

    Room save(Room room);

    Optional<Room> findByCode(String code);

    boolean existsByCode(String code);

    /** Salas públicas en espera (WAITING) y con jugadores, para la lista de servidores. */
    List<Room> findListedWaiting();

    /** Todas las salas, sin filtrar — usado por el barrido de limpieza de inactividad. */
    List<Room> findAll();

    /** Elimina la sala (p. ej. cuando se queda sin jugadores). */
    void delete(String code);
}
