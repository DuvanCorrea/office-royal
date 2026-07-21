package com.officewars.application.usecase;

import com.officewars.application.GameException;
import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Coordinate;
import com.officewars.domain.core.Office;
import com.officewars.domain.core.ObjectType;
import com.officewars.domain.core.PlacedObject;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** El jugador envía la disposición de su oficina (avatar + objetos) durante la preparación. */
@Service
public class ArrangeOfficeUseCase {

    private final RoomRepository rooms;
    private final GameEventPublisher publisher;

    public ArrangeOfficeUseCase(RoomRepository rooms, GameEventPublisher publisher) {
        this.rooms = rooms;
        this.publisher = publisher;
    }

    /** Colocación de un objeto solicitada por el cliente. */
    public record Placement(String type, int x, int y) {
    }

    public Room execute(String code, String playerId, int avatarX, int avatarY, List<Placement> placements) {
        Room room = rooms.findByCode(code)
                .orElseThrow(() -> new GameException("Sala no encontrada: " + code));
        if (room.getStatus() != RoomStatus.PREPARING) {
            throw new GameException("Solo se puede ordenar durante la preparación");
        }
        Player player = room.findPlayer(playerId)
                .orElseThrow(() -> new GameException("Jugador no encontrado"));

        Office office = new Office(room.getOfficeWidth(), room.getOfficeHeight());
        if (!office.contains(avatarX, avatarY)) {
            throw new GameException("El avatar está fuera de la oficina");
        }

        Set<String> used = new HashSet<>();
        used.add(avatarX + "," + avatarY);
        List<PlacedObject> objects = new ArrayList<>();
        for (Placement p : placements) {
            ObjectType type = parseType(p.type());
            if (!office.contains(p.x(), p.y())) {
                throw new GameException("Un objeto está fuera de la oficina");
            }
            if (!used.add(p.x() + "," + p.y())) {
                throw new GameException("Dos elementos ocupan la misma celda");
            }
            objects.add(new PlacedObject(shortId(), type, p.x(), p.y()));
        }

        office.setAvatar(new Coordinate(avatarX, avatarY));
        office.setObjects(objects);
        player.setOffice(office);

        rooms.save(room);
        publisher.notifyRoomChanged(code);
        return room;
    }

    private ObjectType parseType(String raw) {
        try {
            return ObjectType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new GameException("Tipo de objeto inválido: " + raw);
        }
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
