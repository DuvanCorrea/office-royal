package com.officewars.application;

import com.officewars.domain.mode.GameMode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory / registro de modos de juego. Resuelve un modeId a su {@link GameMode}.
 * Agregar un modo nuevo solo requiere registrar su instancia (Open/Closed).
 */
public class GameModeRegistry {

    private final Map<String, GameMode> modes;
    private final String defaultModeId;

    public GameModeRegistry(List<GameMode> availableModes) {
        if (availableModes.isEmpty()) {
            throw new IllegalStateException("No hay modos de juego registrados");
        }
        this.modes = availableModes.stream()
                .collect(Collectors.toMap(GameMode::id, Function.identity()));
        this.defaultModeId = availableModes.get(0).id();
    }

    public GameMode get(String modeId) {
        GameMode mode = modes.get(modeId);
        if (mode == null) {
            throw new IllegalArgumentException("Modo de juego desconocido: " + modeId);
        }
        return mode;
    }

    public String defaultModeId() {
        return defaultModeId;
    }
}
