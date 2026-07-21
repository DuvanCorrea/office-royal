package com.officewars.domain.core;

/** Catálogo de objetos de oficina. Cada tipo tiene vida (impactos para destruir) y puntos. */
public enum ObjectType {
    MONITOR(1, 10),
    PLANT(1, 10),
    CHAIR(1, 10),
    BIN(1, 10),
    COFFEE(2, 20),
    PRINTER(2, 20),
    WATER(2, 20),
    DESK(2, 25),
    SOFA(3, 40);

    private final int health;
    private final int points;

    ObjectType(int health, int points) {
        this.health = health;
        this.points = points;
    }

    public int health() {
        return health;
    }

    public int points() {
        return points;
    }
}
