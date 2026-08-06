package com.officewars.infrastructure.scheduling;

import com.officewars.application.usecase.CleanupStalePlayersUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StalePlayerCleanupScheduler {

    private final CleanupStalePlayersUseCase cleanup;

    public StalePlayerCleanupScheduler(CleanupStalePlayersUseCase cleanup) {
        this.cleanup = cleanup;
    }

    @Scheduled(fixedDelay = 120_000)
    public void run() {
        cleanup.execute();
    }
}
