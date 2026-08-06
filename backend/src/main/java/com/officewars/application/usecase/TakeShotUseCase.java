package com.officewars.application.usecase;

import com.officewars.application.GameException;
import com.officewars.application.GameModeRegistry;
import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import com.officewars.domain.mode.GameMode;
import com.officewars.domain.mode.ShotResult;
import org.springframework.stereotype.Service;

@Service
public class TakeShotUseCase {

    private final RoomRepository rooms;
    private final GameModeRegistry modes;
    private final GameEventPublisher publisher;

    public TakeShotUseCase(RoomRepository rooms, GameModeRegistry modes, GameEventPublisher publisher) {
        this.rooms = rooms;
        this.modes = modes;
        this.publisher = publisher;
    }

    public ShotResult execute(String code, String playerId, String targetId, int x, int y) {
        Room room = rooms.findByCode(code)
                .orElseThrow(() -> new GameException("Sala no encontrada: " + code));

        if (room.getStatus() != RoomStatus.RUNNING) {
            throw new GameException("La partida no está en curso");
        }
        if (!playerId.equals(room.currentPlayerId())) {
            throw new GameException("No es tu turno");
        }

        Player shooter = room.findPlayer(playerId)
                .orElseThrow(() -> new GameException("Jugador no encontrado"));
        Player target = room.findPlayer(targetId)
                .orElseThrow(() -> new GameException("Rival no encontrado"));

        if (target.getId().equals(shooter.getId())) {
            throw new GameException("No puedes dispararte a ti mismo");
        }
        if (!target.isAlive()) {
            throw new GameException("Ese rival ya fue eliminado");
        }
        if (!target.getOffice().contains(x, y)) {
            throw new GameException("Coordenada fuera de la oficina");
        }
        if (target.getOffice().alreadyShot(x, y)) {
            throw new GameException("Ya disparaste a esa celda");
        }

        shooter.touch();
        GameMode mode = modes.get(room.getModeId());
        ShotResult result = mode.resolveShot(room, shooter, target, x, y);
        room.addFeed(result.outcome().name(), result.message());

        if (mode.isFinished(room)) {
            room.setStatus(RoomStatus.FINISHED);
            String winnerId = mode.winnerId(room);
            room.setWinnerId(winnerId);
            room.findPlayer(winnerId).ifPresent(w ->
                    room.addFeed("WIN", "🏆 " + w.getNickname() + " ganó la partida!"));
        } else {
            room.advanceTurn();
        }

        rooms.save(room);
        publisher.notifyRoomChanged(code);
        return result;
    }
}
