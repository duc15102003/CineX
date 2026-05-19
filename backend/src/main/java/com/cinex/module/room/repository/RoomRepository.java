package com.cinex.module.room.repository;

import com.cinex.module.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByName(String name);

    // Filter soft delete ở SQL — DB chỉ trả row chưa xóa
    @Query("SELECT r FROM Room r WHERE r.storageState IS NULL OR r.storageState <> 'DELETED'")
    List<Room> findAllActive();

    @Query("SELECT r FROM Room r WHERE r.id = :id AND (r.storageState IS NULL OR r.storageState <> 'DELETED')")
    Optional<Room> findActiveById(Long id);
}
