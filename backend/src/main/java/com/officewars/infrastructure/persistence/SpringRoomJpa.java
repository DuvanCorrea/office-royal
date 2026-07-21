package com.officewars.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringRoomJpa extends JpaRepository<RoomEntity, String> {
}
