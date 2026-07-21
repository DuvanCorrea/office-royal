package com.officewars.domain;

import com.officewars.domain.core.Office;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.PlayerStatus;
import com.officewars.domain.core.Room;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomTest {

    private Player player(String id) {
        return new Player(id, id, "#fff", 3, new Office(8, 8));
    }

    private Room runningRoom() {
        Room room = new Room("TEST01", "Test", "office-wars", 8, 8, 8);
        room.addPlayer(player("A"));
        room.addPlayer(player("B"));
        room.addPlayer(player("C"));
        room.setTurnOrder(List.of("A", "B", "C"));
        return room;
    }

    @Test
    void advanceTurnCyclesThroughPlayers() {
        Room room = runningRoom();
        assertEquals("A", room.currentPlayerId());
        room.advanceTurn();
        assertEquals("B", room.currentPlayerId());
        room.advanceTurn();
        assertEquals("C", room.currentPlayerId());
        room.advanceTurn();
        assertEquals("A", room.currentPlayerId());
    }

    @Test
    void advanceTurnSkipsEliminatedPlayers() {
        Room room = runningRoom();
        room.findPlayer("B").orElseThrow().setStatus(PlayerStatus.ELIMINATED);

        assertEquals("A", room.currentPlayerId());
        room.advanceTurn();
        assertEquals("C", room.currentPlayerId(), "debe saltar a B eliminado");
    }
}
