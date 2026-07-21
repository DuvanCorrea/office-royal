package com.officewars.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * La oficina de un jugador: una grilla donde esconde su avatar y coloca objetos.
 * Los rivales disparan a esta grilla a ciegas (modelo tipo Batalla Naval).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Office {

    private int width;
    private int height;
    private Coordinate avatar;
    private List<PlacedObject> objects = new ArrayList<>();
    private List<Shot> shots = new ArrayList<>();

    public Office() {
    }

    public Office(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public PlacedObject objectAt(int x, int y) {
        return objects.stream()
                .filter(o -> !o.isDestroyed() && o.isAt(x, y))
                .findFirst()
                .orElse(null);
    }

    public boolean occupied(int x, int y) {
        if (avatar != null && avatar.at(x, y)) {
            return true;
        }
        return objects.stream().anyMatch(o -> o.isAt(x, y));
    }

    public boolean alreadyShot(int x, int y) {
        return shots.stream().anyMatch(s -> s.x() == x && s.y() == y);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Coordinate getAvatar() {
        return avatar;
    }

    public void setAvatar(Coordinate avatar) {
        this.avatar = avatar;
    }

    public List<PlacedObject> getObjects() {
        return objects;
    }

    public void setObjects(List<PlacedObject> objects) {
        this.objects = objects;
    }

    public List<Shot> getShots() {
        return shots;
    }

    public void setShots(List<Shot> shots) {
        this.shots = shots;
    }
}
