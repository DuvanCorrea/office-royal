package com.officewars.application.usecase;

import com.officewars.application.GameDefaults;
import com.officewars.application.port.GameEventPublisher;
import com.officewars.application.port.RoomRepository;
import com.officewars.application.usecase.support.RoomPlayerRemover;
import com.officewars.domain.core.Office;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanupStalePlayersUseCaseTest {

    /** Repositorio en memoria, mismo espíritu simple que RoomTest.java: sin mocks, POJOs planos. */
    private static class FakeRoomRepository implements RoomRepository {
        final Map<String, Room> byCode = new LinkedHashMap<>();

        @Override
        public Room save(Room room) {
            byCode.put(room.getCode(), room);
            return room;
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.ofNullable(byCode.get(code));
        }

        @Override
        public boolean existsByCode(String code) {
            return byCode.containsKey(code);
        }

        @Override
        public List<Room> findListedWaiting() {
            return byCode.values().stream()
                    .filter(r -> r.isListed() && r.getStatus() == RoomStatus.WAITING && !r.getPlayers().isEmpty())
                    .toList();
        }

        @Override
        public List<Room> findAll() {
            return List.copyOf(byCode.values());
        }

        @Override
        public void delete(String code) {
            byCode.remove(code);
        }
    }

    private static class FakePublisher implements GameEventPublisher {
        int notifications = 0;

        @Override
        public void notifyRoomChanged(String code) {
            notifications++;
        }
    }

    private FakeRoomRepository rooms;
    private FakePublisher publisher;
    private CleanupStalePlayersUseCase cleanup;

    @BeforeEach
    void setUp() {
        rooms = new FakeRoomRepository();
        publisher = new FakePublisher();
        cleanup = new CleanupStalePlayersUseCase(rooms, publisher, new RoomPlayerRemover());
    }

    private Player player(String id, long lastActivityAt) {
        Player p = new Player(id, id, "#fff", 3, new Office(8, 8));
        p.setLastActivityAt(lastActivityAt);
        return p;
    }

    private Room room(String code, RoomStatus status, Player... players) {
        Room room = new Room(code, "Sala " + code, "office-wars", 8, 8, 8);
        room.setStatus(status);
        for (Player p : players) {
            room.addPlayer(p);
        }
        room.setTurnOrder(new java.util.ArrayList<>(java.util.Arrays.stream(players).map(Player::getId).toList()));
        rooms.save(room);
        return room;
    }

    @Test
    void removesStalePlayerInWaitingAfterShortTimeout() {
        long now = System.currentTimeMillis();
        long staleAt = now - GameDefaults.PREP_INACTIVITY_TIMEOUT_MS - 1000;
        room("R1", RoomStatus.WAITING, player("A", staleAt), player("B", now));

        cleanup.execute();

        Room updated = rooms.findByCode("R1").orElseThrow();
        assertEquals(1, updated.getPlayers().size());
        assertEquals("B", updated.getPlayers().get(0).getId());
        assertEquals(1, publisher.notifications);
    }

    @Test
    void keepsRecentPlayerInWaiting() {
        long now = System.currentTimeMillis();
        room("R1", RoomStatus.WAITING, player("A", now), player("B", now));

        cleanup.execute();

        assertEquals(2, rooms.findByCode("R1").orElseThrow().getPlayers().size());
        assertEquals(0, publisher.notifications);
    }

    @Test
    void runningRoomUsesLongTimeoutSoRecentlyIdlePlayerSurvives() {
        long now = System.currentTimeMillis();
        // Inactivo más allá del umbral corto de espera, pero muy por debajo del umbral de 24h de RUNNING.
        long idleButAsync = now - GameDefaults.PREP_INACTIVITY_TIMEOUT_MS - 1000;
        room("R1", RoomStatus.RUNNING, player("A", idleButAsync), player("B", now));

        cleanup.execute();

        assertEquals(2, rooms.findByCode("R1").orElseThrow().getPlayers().size(),
                "una partida en curso es asincrona: no debe purgar por inactividad corta");
    }

    @Test
    void runningRoomRemovesPlayerPastTheLongTimeout() {
        long now = System.currentTimeMillis();
        long staleAt = now - GameDefaults.RUNNING_INACTIVITY_TIMEOUT_MS - 1000;
        room("R1", RoomStatus.RUNNING, player("A", staleAt), player("B", now));

        cleanup.execute();

        Room updated = rooms.findByCode("R1").orElseThrow();
        assertEquals(1, updated.getPlayers().size());
        assertEquals("B", updated.getPlayers().get(0).getId());
    }

    @Test
    void deletesRoomWhenLastPlayerTimesOut() {
        long now = System.currentTimeMillis();
        long staleAt = now - GameDefaults.PREP_INACTIVITY_TIMEOUT_MS - 1000;
        room("R1", RoomStatus.PREPARING, player("A", staleAt));

        cleanup.execute();

        assertTrue(rooms.findByCode("R1").isEmpty());
    }

    @Test
    void healsMissingLastActivityInsteadOfPurgingEveryone() {
        // lastActivityAt == 0 simula datos persistidos antes de que este campo existiera.
        room("R1", RoomStatus.WAITING, player("A", 0L));

        cleanup.execute();

        Room updated = rooms.findByCode("R1").orElseThrow();
        assertEquals(1, updated.getPlayers().size(), "no debe purgar por un timestamp legado en 0");
        assertTrue(updated.getPlayers().get(0).getLastActivityAt() > 0, "debe sanar el timestamp");
    }

    @Test
    void ignoresFinishedRooms() {
        long now = System.currentTimeMillis();
        long staleAt = now - GameDefaults.RUNNING_INACTIVITY_TIMEOUT_MS - 1000;
        room("R1", RoomStatus.FINISHED, player("A", staleAt));

        cleanup.execute();

        assertEquals(1, rooms.findByCode("R1").orElseThrow().getPlayers().size());
        assertFalse(rooms.findByCode("R1").isEmpty());
    }
}
