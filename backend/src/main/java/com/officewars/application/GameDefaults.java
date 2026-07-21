package com.officewars.application;

/** Configuración por defecto del slice v1 (será configurable por sala en el MVP). */
public final class GameDefaults {

    public static final int LIVES = 3;
    public static final int MAX_PLAYERS = 8;
    public static final int MIN_PLAYERS_TO_START = 2;

    /** Paleta de colores de avatar asignados por orden de ingreso. */
    public static final String[] COLORS = {
            "#ef4444", "#f59e0b", "#22c55e", "#06b6d4",
            "#3b82f6", "#8b5cf6", "#ec4899", "#eab308"
    };

    private GameDefaults() {
    }
}
