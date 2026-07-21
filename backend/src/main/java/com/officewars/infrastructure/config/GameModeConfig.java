package com.officewars.infrastructure.config;

import com.officewars.application.GameModeRegistry;
import com.officewars.domain.mode.GameMode;
import com.officewars.domain.mode.OfficeWarsMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cablea los modos de juego disponibles en el registro (Factory).
 * Para añadir Hospital Wars, School Wars, etc.: registrar aquí su instancia. El core no cambia.
 */
@Configuration
public class GameModeConfig {

    @Bean
    public GameModeRegistry gameModeRegistry() {
        List<GameMode> modes = List.of(
                new OfficeWarsMode()
                // new HospitalWarsMode(), new SchoolWarsMode(), ...
        );
        return new GameModeRegistry(modes);
    }
}
