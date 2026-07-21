package com.officewars.domain.mode;

import com.officewars.domain.core.Coordinate;
import com.officewars.domain.core.Office;
import com.officewars.domain.core.ObjectType;
import com.officewars.domain.core.PlacedObject;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.domain.core.Shot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Primer modo: cada jugador esconde su avatar y coloca objetos en su oficina; los rivales
 * disparan a ciegas (tipo Batalla Naval). Destruir objetos da puntos; encontrar el avatar
 * quita vidas. Al ser golpeado, el avatar se reubica dentro de su propia oficina.
 */
public class OfficeWarsMode implements GameMode {

    public static final String ID = "office-wars";
    private static final int SIZE = 8;
    private static final int ELIMINATION_BONUS = 50;

    /** Objetos que cada oficina recibe al ordenarse automáticamente. */
    private static final List<ObjectType> LOADOUT = List.of(
            ObjectType.DESK, ObjectType.MONITOR, ObjectType.PLANT, ObjectType.PRINTER,
            ObjectType.COFFEE, ObjectType.CHAIR, ObjectType.BIN, ObjectType.SOFA);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Office Wars";
    }

    @Override
    public int officeWidth() {
        return SIZE;
    }

    @Override
    public int officeHeight() {
        return SIZE;
    }

    @Override
    public void autoArrange(Office office) {
        office.getObjects().clear();
        office.setAvatar(freeCell(office));
        for (ObjectType type : LOADOUT) {
            Coordinate c = freeCell(office);
            office.getObjects().add(new PlacedObject(shortId(), type, c.x(), c.y()));
        }
    }

    private Coordinate freeCell(Office office) {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = ThreadLocalRandom.current().nextInt(office.getWidth());
            int y = ThreadLocalRandom.current().nextInt(office.getHeight());
            if (!office.occupied(x, y)) {
                return new Coordinate(x, y);
            }
        }
        return new Coordinate(0, 0);
    }

    /**
     * Celda a la que huye el avatar tras un impacto: libre y <b>que aún no haya sido disparada</b>,
     * para que siga siendo alcanzable (si se escondiera en una celda ya atacada sería inmortal).
     */
    private Coordinate escapeCell(Office office) {
        List<Coordinate> fresh = new ArrayList<>();
        List<Coordinate> free = new ArrayList<>();
        for (int y = 0; y < office.getHeight(); y++) {
            for (int x = 0; x < office.getWidth(); x++) {
                if (office.occupied(x, y)) {
                    continue;
                }
                Coordinate c = new Coordinate(x, y);
                free.add(c);
                if (!office.alreadyShot(x, y)) {
                    fresh.add(c);
                }
            }
        }
        List<Coordinate> pool = !fresh.isEmpty() ? fresh : free;
        if (pool.isEmpty()) {
            return office.getAvatar();
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    @Override
    public ShotResult resolveShot(Room room, Player shooter, Player target, int x, int y) {
        Office office = target.getOffice();

        // ¿avatar?
        if (office.getAvatar() != null && office.getAvatar().at(x, y)) {
            target.takeHit();
            if (!target.isAlive()) {
                shooter.addScore(ELIMINATION_BONUS);
                office.getShots().add(new Shot(x, y, shooter.getId(), ShotOutcome.AVATAR_ELIMINATED.name(), null));
                return new ShotResult(ShotOutcome.AVATAR_ELIMINATED, target.getId(), null, ELIMINATION_BONUS,
                        shooter.getNickname() + " encontró y eliminó a " + target.getNickname() + " 🎯");
            }
            // Se registra el disparo ANTES de huir, para que la celda recién atacada
            // también quede excluida como escondite.
            office.getShots().add(new Shot(x, y, shooter.getId(), ShotOutcome.AVATAR_HIT.name(), null));
            office.setAvatar(escapeCell(office)); // huye a una celda libre y aún no disparada
            return new ShotResult(ShotOutcome.AVATAR_HIT, target.getId(), null, 0,
                    shooter.getNickname() + " golpeó a " + target.getNickname()
                            + " (le quedan " + target.getLives() + " vidas)");
        }

        // ¿objeto?
        PlacedObject obj = office.objectAt(x, y);
        if (obj != null) {
            boolean destroyed = obj.hit();
            String typeName = obj.getType().name();
            if (destroyed) {
                shooter.addScore(obj.getType().points());
                office.getShots().add(new Shot(x, y, shooter.getId(), ShotOutcome.OBJECT_DESTROYED.name(), typeName));
                return new ShotResult(ShotOutcome.OBJECT_DESTROYED, target.getId(), typeName, obj.getType().points(),
                        shooter.getNickname() + " destruyó " + friendly(obj.getType()) + " de "
                                + target.getNickname() + " (+" + obj.getType().points() + ")");
            }
            office.getShots().add(new Shot(x, y, shooter.getId(), ShotOutcome.OBJECT_HIT.name(), typeName));
            return new ShotResult(ShotOutcome.OBJECT_HIT, target.getId(), typeName, 0,
                    shooter.getNickname() + " dañó " + friendly(obj.getType()) + " de " + target.getNickname());
        }

        // agua
        office.getShots().add(new Shot(x, y, shooter.getId(), ShotOutcome.MISS.name(), null));
        return new ShotResult(ShotOutcome.MISS, target.getId(), null, 0,
                shooter.getNickname() + " disparó a la oficina de " + target.getNickname() + " — nada.");
    }

    private String friendly(ObjectType type) {
        return switch (type) {
            case DESK -> "el escritorio";
            case MONITOR -> "el monitor";
            case PLANT -> "la planta";
            case PRINTER -> "la impresora";
            case COFFEE -> "la cafetera";
            case CHAIR -> "la silla";
            case BIN -> "la papelera";
            case SOFA -> "el sofá";
            case WATER -> "el dispensador";
        };
    }

    @Override
    public boolean isFinished(Room room) {
        return room.alivePlayers().size() <= 1;
    }

    @Override
    public String winnerId(Room room) {
        List<Player> alive = room.alivePlayers();
        return alive.size() == 1 ? alive.get(0).getId() : null;
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
