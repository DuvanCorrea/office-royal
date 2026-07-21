package com.officewars.domain.core;

/** Posición dentro de la grilla del edificio. */
public record Coordinate(int x, int y) {
    public boolean at(int x, int y) {
        return this.x == x && this.y == y;
    }
}
