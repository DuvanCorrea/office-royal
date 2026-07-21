package com.officewars.infrastructure.web;

import com.officewars.domain.core.Coordinate;
import com.officewars.domain.core.Office;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.infrastructure.web.dto.Responses.Coord;
import com.officewars.infrastructure.web.dto.Responses.FeedView;
import com.officewars.infrastructure.web.dto.Responses.ObjectView;
import com.officewars.infrastructure.web.dto.Responses.OpponentOffice;
import com.officewars.infrastructure.web.dto.Responses.OpponentView;
import com.officewars.infrastructure.web.dto.Responses.OwnOffice;
import com.officewars.infrastructure.web.dto.Responses.PlayerView;
import com.officewars.infrastructure.web.dto.Responses.RevealedCell;
import com.officewars.infrastructure.web.dto.Responses.RoomState;
import com.officewars.infrastructure.web.dto.Responses.ShotView;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Construye la vista de estado para un espectador concreto: su propia oficina completa,
 * y las de los rivales enmascaradas (solo las celdas ya disparadas se revelan).
 */
@Component
public class RoomStateMapper {

    public RoomState toState(Room room, String viewerId) {
        List<PlayerView> players = room.getPlayers().stream()
                .map(this::toPlayerView)
                .toList();

        Player viewer = viewerId == null ? null : room.findPlayer(viewerId).orElse(null);
        OwnOffice yourOffice = viewer == null ? null : toOwnOffice(viewer.getOffice());

        List<OpponentView> opponents = room.getPlayers().stream()
                .filter(p -> !p.getId().equals(viewerId))
                .map(this::toOpponentView)
                .toList();

        List<FeedView> feed = room.getFeed().stream()
                .map(f -> new FeedView(f.seq(), f.type(), f.message(), f.timestamp()))
                .toList();

        return new RoomState(
                room.getCode(), room.getName(), room.getModeId(), room.getStatus().name(),
                room.currentPlayerId(), room.getWinnerId(), viewerId,
                room.getOfficeWidth(), room.getOfficeHeight(),
                players, yourOffice, opponents, feed);
    }

    private PlayerView toPlayerView(Player p) {
        return new PlayerView(p.getId(), p.getNickname(), p.getColor(), p.getLives(),
                p.getScore(), p.getStatus().name(), p.isReady());
    }

    private OwnOffice toOwnOffice(Office office) {
        if (office == null) {
            return null;
        }
        Coordinate a = office.getAvatar();
        Coord avatar = a == null ? null : new Coord(a.x(), a.y());
        List<ObjectView> objects = office.getObjects().stream()
                .map(o -> new ObjectView(o.getId(), o.getType().name(), o.getX(), o.getY(),
                        o.getHealth(), o.getType().health(), o.isDestroyed()))
                .toList();
        List<ShotView> shots = office.getShots().stream()
                .map(s -> new ShotView(s.x(), s.y(), s.byPlayerId(), s.outcome(), s.objectType()))
                .toList();
        return new OwnOffice(office.getWidth(), office.getHeight(), avatar, objects, shots);
    }

    private OpponentView toOpponentView(Player p) {
        Office office = p.getOffice();
        List<RevealedCell> revealed = office == null ? List.of() : office.getShots().stream()
                .map(s -> new RevealedCell(s.x(), s.y(), s.outcome(), s.objectType(), s.byPlayerId()))
                .toList();
        int w = office == null ? 0 : office.getWidth();
        int h = office == null ? 0 : office.getHeight();
        return new OpponentView(p.getId(), p.getNickname(), p.getColor(), p.getLives(),
                p.getScore(), p.getStatus().name(), p.isReady(),
                new OpponentOffice(w, h, revealed));
    }
}
