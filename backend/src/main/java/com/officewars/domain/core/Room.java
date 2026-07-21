package com.officewars.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Agregado raíz. Contiene el estado completo de una partida: jugadores (cada uno con su
 * oficina), orden de turno y feed. Las reglas variables las aporta el GameMode (Strategy);
 * aquí viven las invariantes y mutaciones genéricas del motor.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {

    private String code;
    private String name;
    private String modeId;
    private int maxPlayers;
    private int officeWidth;
    private int officeHeight;
    private RoomStatus status = RoomStatus.WAITING;
    private boolean listed = true;

    private List<Player> players = new ArrayList<>();
    private List<String> turnOrder = new ArrayList<>();
    private int currentTurnIndex = 0;

    private List<FeedEntry> feed = new ArrayList<>();
    private long feedSeq = 0;

    private String winnerId;

    public Room() {
    }

    public Room(String code, String name, String modeId, int maxPlayers, int officeWidth, int officeHeight) {
        this.code = code;
        this.name = name;
        this.modeId = modeId;
        this.maxPlayers = maxPlayers;
        this.officeWidth = officeWidth;
        this.officeHeight = officeHeight;
    }

    // --- Comportamiento del motor ---

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Optional<Player> findPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst();
    }

    public List<Player> alivePlayers() {
        return players.stream().filter(Player::isAlive).toList();
    }

    public boolean allReady() {
        return !players.isEmpty() && players.stream().allMatch(Player::isReady);
    }

    public String currentPlayerId() {
        if (turnOrder.isEmpty()) {
            return null;
        }
        return turnOrder.get(currentTurnIndex);
    }

    /** Avanza al siguiente jugador vivo en el orden de turno. */
    public void advanceTurn() {
        if (turnOrder.isEmpty()) {
            return;
        }
        for (int i = 0; i < turnOrder.size(); i++) {
            currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
            String candidateId = turnOrder.get(currentTurnIndex);
            if (findPlayer(candidateId).map(Player::isAlive).orElse(false)) {
                return;
            }
        }
    }

    public FeedEntry addFeed(String type, String message) {
        FeedEntry entry = new FeedEntry(++feedSeq, type, message, System.currentTimeMillis());
        feed.add(entry);
        return entry;
    }

    // --- Getters / setters (serialización JSON) ---

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModeId() {
        return modeId;
    }

    public void setModeId(String modeId) {
        this.modeId = modeId;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getOfficeWidth() {
        return officeWidth;
    }

    public void setOfficeWidth(int officeWidth) {
        this.officeWidth = officeWidth;
    }

    public int getOfficeHeight() {
        return officeHeight;
    }

    public void setOfficeHeight(int officeHeight) {
        this.officeHeight = officeHeight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public boolean isListed() {
        return listed;
    }

    public void setListed(boolean listed) {
        this.listed = listed;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<String> getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(List<String> turnOrder) {
        this.turnOrder = turnOrder;
    }

    public int getCurrentTurnIndex() {
        return currentTurnIndex;
    }

    public void setCurrentTurnIndex(int currentTurnIndex) {
        this.currentTurnIndex = currentTurnIndex;
    }

    public List<FeedEntry> getFeed() {
        return feed;
    }

    public void setFeed(List<FeedEntry> feed) {
        this.feed = feed;
    }

    public long getFeedSeq() {
        return feedSeq;
    }

    public void setFeedSeq(long feedSeq) {
        this.feedSeq = feedSeq;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
}
