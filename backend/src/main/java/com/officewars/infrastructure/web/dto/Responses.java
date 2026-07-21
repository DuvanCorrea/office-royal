package com.officewars.infrastructure.web.dto;

import java.util.List;

/** DTOs de salida de la API REST. */
public final class Responses {

    public record CreatedRoom(String code) {
    }

    public record Joined(String playerId, String nickname, String color) {
    }

    public record RoomSummary(String code, String name, String modeId, String status,
                              int players, int maxPlayers) {
    }

    public record Coord(int x, int y) {
    }

    /** Resumen público de un jugador (para el marcador). */
    public record PlayerView(String id, String nickname, String color, int lives, int score,
                             String status, boolean ready) {
    }

    public record ObjectView(String id, String type, int x, int y, int health, int maxHealth,
                             boolean destroyed) {
    }

    /** Disparo recibido, visible en tu propia oficina. */
    public record ShotView(int x, int y, String byPlayerId, String outcome, String objectType) {
    }

    /** Tu oficina, totalmente visible para ti. */
    public record OwnOffice(int width, int height, Coord avatar, List<ObjectView> objects,
                            List<ShotView> shots) {
    }

    /** Celda revelada en la oficina de un rival (solo lo que ya se disparó). */
    public record RevealedCell(int x, int y, String outcome, String objectType, String byPlayerId) {
    }

    public record OpponentOffice(int width, int height, List<RevealedCell> revealed) {
    }

    public record OpponentView(String id, String nickname, String color, int lives, int score,
                               String status, boolean ready, OpponentOffice office) {
    }

    public record FeedView(long seq, String type, String message, long timestamp) {
    }

    public record RoomState(String code, String name, String modeId, String status,
                            String currentPlayerId, String winnerId, String you,
                            int officeWidth, int officeHeight, List<PlayerView> players,
                            OwnOffice yourOffice, List<OpponentView> opponents,
                            List<FeedView> feed) {
    }

    private Responses() {
    }
}
