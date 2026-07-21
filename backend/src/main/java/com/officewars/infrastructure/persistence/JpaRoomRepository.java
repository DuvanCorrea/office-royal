package com.officewars.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto RoomRepository. Serializa el agregado a JSON (Repository Pattern). */
@Repository
public class JpaRoomRepository implements RoomRepository {

    private final SpringRoomJpa jpa;
    private final ObjectMapper mapper;

    public JpaRoomRepository(SpringRoomJpa jpa, ObjectMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Room save(Room room) {
        jpa.save(new RoomEntity(room.getCode(), serialize(room)));
        return room;
    }

    @Override
    public Optional<Room> findByCode(String code) {
        return jpa.findById(code).map(e -> deserialize(e.getData()));
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsById(code);
    }

    @Override
    public List<Room> findListedWaiting() {
        return jpa.findAll().stream()
                .map(e -> deserialize(e.getData()))
                .filter(r -> r.isListed() && r.getStatus() == RoomStatus.WAITING)
                .toList();
    }

    private String serialize(Room room) {
        try {
            return mapper.writeValueAsString(room);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la sala", e);
        }
    }

    private Room deserialize(String data) {
        try {
            return mapper.readValue(data, Room.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo leer la sala", e);
        }
    }
}
