package com.officewars.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Persistencia del agregado Room como snapshot JSON (columna TEXT). */
@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    private String code;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String data;

    protected RoomEntity() {
    }

    public RoomEntity(String code, String data) {
        this.code = code;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
