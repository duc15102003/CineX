package com.cinex.module.seat.repository;

import com.cinex.module.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Chỉ lấy ghế chưa xóa, sắp xếp theo hàng + cột
    List<Seat> findByRoomIdAndStorageStateIsNullOrderByRowLabelAscColNumberAsc(Long roomId);

    // Soft delete tất cả ghế của phòng (thay vì hard delete)
    @Modifying
    @Query("UPDATE Seat s SET s.storageState = 'DELETED' WHERE s.room.id = :roomId AND (s.storageState IS NULL OR s.storageState <> 'DELETED')")
    void softDeleteByRoomId(Long roomId);
}
