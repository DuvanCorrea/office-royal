package com.officewars.domain.mode;

/** Resultado de resolver un disparo contra la oficina de un rival. */
public record ShotResult(ShotOutcome outcome, String targetPlayerId, String objectType,
                         int points, String message) {

    public boolean isHit() {
        return outcome != ShotOutcome.MISS;
    }
}
