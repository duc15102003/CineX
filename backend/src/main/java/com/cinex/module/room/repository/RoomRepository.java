package com.cinex.module.room.repository;

import com.cinex.module.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByName(String name);

    @Query("SELECT r FROM Room r WHERE r.storageState IS NULL OR r.storageState <> 'DELETED'")
    List<Room> findAllActive();
}
