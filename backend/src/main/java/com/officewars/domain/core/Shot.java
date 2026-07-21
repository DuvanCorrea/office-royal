package com.officewars.domain.core;

/**
 * Disparo recibido en la oficina de un jugador. Visible como marcador para quien atacó
 * (y en el feed). {@code objectType} solo aplica cuando el disparo golpeó un objeto.
 */
public record Shot(int x, int y, String byPlayerId, String outcome, String objectType) {
}
