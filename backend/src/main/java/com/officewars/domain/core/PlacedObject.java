package com.officewars.domain.core;

/** Un objeto colocado en la oficina de un jugador. POJO serializable (snapshot JSON). */
public class PlacedObject {

    private String id;
    private ObjectType type;
    private int x;
    private int y;
    private int health;
    private boolean destroyed;

    public PlacedObject() {
    }

    public PlacedObject(String id, ObjectType type, int x, int y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.health = type.health();
    }

    /** Aplica un impacto. Devuelve true si el objeto quedó destruido con este golpe. */
    public boolean hit() {
        if (destroyed) {
            return false;
        }
        health--;
        if (health <= 0) {
            destroyed = true;
            return true;
        }
        return false;
    }

    public boolean isAt(int x, int y) {
        return this.x == x && this.y == y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
