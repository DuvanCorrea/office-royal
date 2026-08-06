package com.officewars.application.usecase.support;

import com.officewars.application.port.RoomRepository;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Quita a un jugador de la sala y reconcilia turno/vacío. Compartido entre la salida explícita
 * (LeaveRoomUseCase) y el barrido de limpieza por inactividad (CleanupStalePlayersUseCase) para
 * que ambos caminos se comporten igual.
 */
@Component
public class RoomPlayerRemover {

    /** @return true si la sala quedó vacía y fue eliminada. */
    public boolean removeAndReconcile(Room room, String playerId, RoomRepository rooms) {
        Optional<Player> player = room.findPlayer(playerId);
        if (player.isEmpty()) {
            return false;
        }

        room.getPlayers().remove(player.get());
        room.getTurnOrder().remove(playerId);

        if (room.getPlayers().isEmpty()) {
            rooms.delete(room.getCode());
            return true;
        }

        if (playerId.equals(room.currentPlayerId())) {
            room.advanceTurn();
        }
        if (room.getCurrentTurnIndex() >= room.getTurnOrder().size()) {
            room.setCurrentTurnIndex(0);
        }
        return false;
    }
}
