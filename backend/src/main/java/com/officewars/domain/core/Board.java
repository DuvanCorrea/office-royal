package com.officewars.domain.core;

/** Dimensiones de la grilla del mapa. El tema (oficina, hospital...) lo aporta el GameMode. */
public record Board(int width, int height) {
    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }
}
