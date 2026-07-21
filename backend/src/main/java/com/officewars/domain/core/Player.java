package com.officewars.domain.core;

/**
 * Jugador dentro de una sala. Cada jugador tiene su propia {@link Office} (oculta para los
 * rivales). POJO mutable serializable con Jackson (snapshot JSON del estado).
 */
public class Player {

    private String id;
    private String nickname;
    private String color;
    private int lives;
    private int score;
    private boolean ready;
    private PlayerStatus status = PlayerStatus.ALIVE;
    private Office office;

    public Player() {
    }

    public Player(String id, String nickname, String color, int lives, Office office) {
        this.id = id;
        this.nickname = nickname;
        this.color = color;
        this.lives = lives;
        this.office = office;
    }

    public boolean isAlive() {
        return status == PlayerStatus.ALIVE;
    }

    /** Aplica un impacto al avatar: resta una vida y marca eliminación si llega a cero. */
    public void takeHit() {
        if (lives > 0) {
            lives--;
        }
        if (lives <= 0) {
            status = PlayerStatus.ELIMINATED;
        }
    }

    public void addScore(int points) {
        this.score += points;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }
}
