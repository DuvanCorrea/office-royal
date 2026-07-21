package com.officewars.infrastructure.web.dto;

import java.util.List;

/** DTOs de entrada de la API REST. */
public final class Requests {

    public record CreateRoomRequest(String name, String modeId, Boolean listed) {
    }

    public record JoinRequest(String nickname) {
    }

    public record ShotRequest(String playerId, String targetId, int x, int y) {
    }

    public record ObjectPlacement(String type, int x, int y) {
    }

    public record ArrangeRequest(String playerId, int avatarX, int avatarY,
                                 List<ObjectPlacement> objects) {
    }

    private Requests() {
    }
}
