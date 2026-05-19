package com.cinex.module.seat.repository;

import com.cinex.module.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByRoomIdOrderByRowLabelAscColNumberAsc(Long roomId);

    int countByRoomId(Long roomId);

    /**
     * Xóa THẬT tất cả ghế của phòng (không soft delete).
     * Dùng khi admin generate lại sơ đồ ghế — xóa hết rồi tạo mới.
     *
     * @Modifying: đánh dấu đây là query UPDATE/DELETE (không phải SELECT)
     * Nếu thiếu @Modifying → Spring nghĩ là SELECT → lỗi runtime
     */
    @Modifying
    @Query("DELETE FROM Seat s WHERE s.room.id = :roomId")
    void deleteAllByRoomId(Long roomId);
}
