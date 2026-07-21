package com.officewars.infrastructure.web;

import com.officewars.application.usecase.ArrangeOfficeUseCase;
import com.officewars.application.usecase.AutoArrangeUseCase;
import com.officewars.application.usecase.CreateRoomUseCase;
import com.officewars.application.usecase.GetRoomStateUseCase;
import com.officewars.application.usecase.JoinRoomUseCase;
import com.officewars.application.usecase.ListOpenRoomsUseCase;
import com.officewars.application.usecase.SetReadyUseCase;
import com.officewars.application.usecase.StartGameUseCase;
import com.officewars.application.usecase.TakeShotUseCase;
import com.officewars.domain.core.Player;
import com.officewars.domain.core.Room;
import com.officewars.infrastructure.web.dto.Requests.ArrangeRequest;
import com.officewars.infrastructure.web.dto.Requests.CreateRoomRequest;
import com.officewars.infrastructure.web.dto.Requests.JoinRequest;
import com.officewars.infrastructure.web.dto.Requests.ShotRequest;
import com.officewars.infrastructure.web.dto.Responses.CreatedRoom;
import com.officewars.infrastructure.web.dto.Responses.Joined;
import com.officewars.infrastructure.web.dto.Responses.RoomState;
import com.officewars.infrastructure.web.dto.Responses.RoomSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final CreateRoomUseCase createRoom;
    private final JoinRoomUseCase joinRoom;
    private final StartGameUseCase startGame;
    private final ArrangeOfficeUseCase arrangeOffice;
    private final AutoArrangeUseCase autoArrange;
    private final SetReadyUseCase setReady;
    private final TakeShotUseCase takeShot;
    private final GetRoomStateUseCase getState;
    private final ListOpenRoomsUseCase listRooms;
    private final RoomStateMapper mapper;

    public RoomController(CreateRoomUseCase createRoom, JoinRoomUseCase joinRoom,
                          StartGameUseCase startGame, ArrangeOfficeUseCase arrangeOffice,
                          AutoArrangeUseCase autoArrange, SetReadyUseCase setReady,
                          TakeShotUseCase takeShot, GetRoomStateUseCase getState,
                          ListOpenRoomsUseCase listRooms, RoomStateMapper mapper) {
        this.createRoom = createRoom;
        this.joinRoom = joinRoom;
        this.startGame = startGame;
        this.arrangeOffice = arrangeOffice;
        this.autoArrange = autoArrange;
        this.setReady = setReady;
        this.takeShot = takeShot;
        this.getState = getState;
        this.listRooms = listRooms;
        this.mapper = mapper;
    }

    @GetMapping
    public List<RoomSummary> list() {
        return listRooms.execute().stream()
                .map(r -> new RoomSummary(r.getCode(), r.getName(), r.getModeId(),
                        r.getStatus().name(), r.getPlayers().size(), r.getMaxPlayers()))
                .toList();
    }

    @PostMapping
    public CreatedRoom create(@RequestBody(required = false) CreateRoomRequest req) {
        String name = req != null ? req.name() : null;
        String modeId = req != null ? req.modeId() : null;
        Boolean listed = req != null ? req.listed() : null;
        Room room = createRoom.execute(name, modeId, listed);
        return new CreatedRoom(room.getCode());
    }

    @PostMapping("/{code}/join")
    public Joined join(@PathVariable String code, @RequestBody JoinRequest req) {
        Player player = joinRoom.execute(code, req.nickname());
        return new Joined(player.getId(), player.getNickname(), player.getColor());
    }

    @PostMapping("/{code}/start")
    public RoomState start(@PathVariable String code, @RequestParam(required = false) String playerId) {
        Room room = startGame.execute(code);
        return mapper.toState(room, playerId);
    }

    @PostMapping("/{code}/arrange")
    public RoomState arrange(@PathVariable String code, @RequestBody ArrangeRequest req) {
        List<ArrangeOfficeUseCase.Placement> placements = req.objects() == null ? List.of()
                : req.objects().stream()
                        .map(o -> new ArrangeOfficeUseCase.Placement(o.type(), o.x(), o.y()))
                        .toList();
        Room room = arrangeOffice.execute(code, req.playerId(), req.avatarX(), req.avatarY(), placements);
        return mapper.toState(room, req.playerId());
    }

    @PostMapping("/{code}/auto-arrange")
    public RoomState autoArrange(@PathVariable String code, @RequestParam String playerId) {
        Room room = autoArrange.execute(code, playerId);
        return mapper.toState(room, playerId);
    }

    @PostMapping("/{code}/ready")
    public RoomState ready(@PathVariable String code, @RequestParam String playerId) {
        Room room = setReady.execute(code, playerId);
        return mapper.toState(room, playerId);
    }

    @PostMapping("/{code}/shot")
    public RoomState shot(@PathVariable String code, @RequestBody ShotRequest req) {
        takeShot.execute(code, req.playerId(), req.targetId(), req.x(), req.y());
        Room room = getState.execute(code);
        return mapper.toState(room, req.playerId());
    }

    @GetMapping("/{code}/state")
    public RoomState state(@PathVariable String code, @RequestParam(required = false) String playerId) {
        Room room = getState.execute(code);
        return mapper.toState(room, playerId);
    }
}
