package com.cinex.module.room.repository;

import com.cinex.module.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByName(String name);
}
