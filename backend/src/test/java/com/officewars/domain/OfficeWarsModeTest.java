package com.officewars.domain;

import com.officewars.domain.core.Coordinate;
import com.officewars.domain.core.Office;
import com.officewars.domain.core.ObjectType;
import com.officewars.domain.core.PlacedObject;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.PlayerStatus;
import com.officewars.domain.core.Room;
import com.officewars.domain.mode.OfficeWarsMode;
import com.officewars.domain.mode.ShotOutcome;
import com.officewars.domain.mode.ShotResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfficeWarsModeTest {

    private final OfficeWarsMode mode = new OfficeWarsMode();

    private Room roomWith(Player... players) {
        Room room = new Room("TEST01", "Test", OfficeWarsMode.ID, 8,
                mode.officeWidth(), mode.officeHeight());
        for (Player p : players) {
            room.addPlayer(p);
        }
        return room;
    }

    private Player player(String id, int lives) {
        return new Player(id, id, "#fff", lives, new Office(mode.officeWidth(), mode.officeHeight()));
    }

    @Test
    void autoArrangePlacesAvatarAndObjectsWithoutOverlap() {
        Office office = new Office(mode.officeWidth(), mode.officeHeight());
        mode.autoArrange(office);

        assertNotNull(office.getAvatar());
        assertEquals(8, office.getObjects().size());
        // sin dos elementos en la misma celda
        long distinct = office.getObjects().stream()
                .map(o -> o.getX() + "," + o.getY())
                .distinct()
                .count();
        assertEquals(8, distinct);
        assertNull(office.objectAt(office.getAvatar().x(), office.getAvatar().y()));
    }

    @Test
    void missOnEmptyCell() {
        Player shooter = player("A", 3);
        Player target = player("B", 3);
        target.getOffice().setAvatar(new Coordinate(0, 0));
        Room room = roomWith(shooter, target);

        ShotResult r = mode.resolveShot(room, shooter, target, 7, 7);

        assertEquals(ShotOutcome.MISS, r.outcome());
        assertEquals(0, shooter.getScore());
    }

    @Test
    void destroyingObjectGivesPoints() {
        Player shooter = player("A", 3);
        Player target = player("B", 3);
        target.getOffice().setAvatar(new Coordinate(0, 0));
        target.getOffice().getObjects().add(new PlacedObject("o1", ObjectType.MONITOR, 4, 4));
        Room room = roomWith(shooter, target);

        ShotResult r = mode.resolveShot(room, shooter, target, 4, 4);

        assertEquals(ShotOutcome.OBJECT_DESTROYED, r.outcome());
        assertEquals(ObjectType.MONITOR.points(), shooter.getScore());
    }

    @Test
    void twoHitObjectSurvivesFirstShot() {
        Player shooter = player("A", 3);
        Player target = player("B", 3);
        target.getOffice().setAvatar(new Coordinate(0, 0));
        target.getOffice().getObjects().add(new PlacedObject("o1", ObjectType.DESK, 4, 4)); // vida 2
        Room room = roomWith(shooter, target);

        assertEquals(ShotOutcome.OBJECT_HIT, mode.resolveShot(room, shooter, target, 4, 4).outcome());
        assertEquals(0, shooter.getScore());
        assertEquals(ShotOutcome.OBJECT_DESTROYED, mode.resolveShot(room, shooter, target, 4, 4).outcome());
        assertEquals(ObjectType.DESK.points(), shooter.getScore());
    }

    @Test
    void hittingAvatarReducesLifeAndRelocates() {
        Player shooter = player("A", 3);
        Player target = player("B", 3);
        target.getOffice().setAvatar(new Coordinate(5, 5));
        Room room = roomWith(shooter, target);

        ShotResult r = mode.resolveShot(room, shooter, target, 5, 5);

        assertEquals(ShotOutcome.AVATAR_HIT, r.outcome());
        assertEquals(2, target.getLives());
        assertTrue(target.isAlive());
        assertFalse(target.getOffice().getAvatar().at(5, 5), "el avatar debe reubicarse");
    }

    @Test
    void avatarNeverEscapesToAnAlreadyShotCell() {
        Player shooter = player("A", 3);
        Player target = player("B", 99); // muchas vidas para golpearlo muchas veces
        target.getOffice().setAvatar(new Coordinate(0, 0));
        Room room = roomWith(shooter, target);

        // Golpea repetidamente: tras cada impacto el avatar huye; nunca debe caer en una celda
        // ya disparada (si lo hiciera, sería imposible volver a alcanzarlo).
        for (int i = 0; i < 30; i++) {
            Coordinate pos = target.getOffice().getAvatar();
            assertFalse(target.getOffice().alreadyShot(pos.x(), pos.y()),
                    "el avatar quedó en una celda ya atacada: " + pos);
            mode.resolveShot(room, shooter, target, pos.x(), pos.y());
        }
    }

    @Test
    void lastLifeEliminatesTargetAndFinishesGame() {
        Player winner = player("A", 3);
        Player loser = player("B", 1);
        loser.getOffice().setAvatar(new Coordinate(5, 5));
        Room room = roomWith(winner, loser);

        ShotResult r = mode.resolveShot(room, winner, loser, 5, 5);

        assertEquals(ShotOutcome.AVATAR_ELIMINATED, r.outcome());
        assertEquals(PlayerStatus.ELIMINATED, loser.getStatus());
        assertTrue(mode.isFinished(room));
        assertEquals("A", mode.winnerId(room));
    }
}
