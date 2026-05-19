package com.cinex.module.seat.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.module.room.entity.Room;
import com.cinex.module.room.repository.RoomRepository;
import com.cinex.module.seat.dto.SeatGenerateRequest;
import com.cinex.module.seat.dto.SeatMapResponse;
import com.cinex.module.seat.dto.SeatResponse;
import com.cinex.module.seat.dto.UpdateSeatRequest;
import com.cinex.module.seat.entity.Seat;
import com.cinex.module.seat.entity.SeatStatus;
import com.cinex.module.seat.entity.SeatType;
import com.cinex.module.seat.mapper.SeatMapper;
import com.cinex.module.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final SeatMapper seatMapper;

    /**
     * Trả sơ đồ ghế nhóm theo hàng — FE dùng render grid.
     *
     * Kết quả: { "A": [ghế A1, A2, ...], "B": [ghế B1, B2, ...], ... }
     * LinkedHashMap giữ thứ tự insert → hàng A trước B trước C.
     */
    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long roomId) {
        Room room = findRoomById(roomId);
        List<Seat> seats = seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(roomId);

        // Nhóm ghế theo hàng, giữ thứ tự
        Map<String, List<SeatResponse>> seatMap = new LinkedHashMap<>();
        for (Seat seat : seats) {
            seatMap.computeIfAbsent(seat.getRowLabel(), k -> new ArrayList<>())
                    .add(seatMapper.toResponse(seat));
        }

        return SeatMapResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .totalSeats(seats.size())
                .seatMap(seatMap)
                .build();
    }

    /**
     * (ADMIN) Tự động sinh ghế cho phòng theo cấu hình.
     *
     * Luồng:
     * 1. Xóa hết ghế cũ (nếu có)
     * 2. Sinh ghế mới: hàng A→J, cột 1→12
     * 3. Gán loại: vipRows = VIP, coupleRow = COUPLE, còn lại = STANDARD
     * 4. Cập nhật Room.totalSeats
     *
     * VD: totalRows=10, totalCols=12, vipRows=["E","F"], coupleRow="J"
     * → A1-A12 (STANDARD), ..., E1-E12 (VIP), F1-F12 (VIP), ..., J1-J12 (COUPLE)
     */
    @Transactional
    public SeatMapResponse generateSeats(Long roomId, SeatGenerateRequest request) {
        Room room = findRoomById(roomId);

        // Xóa ghế cũ
        seatRepository.deleteAllByRoomId(roomId);

        Set<String> vipRows = request.getVipRows() != null ? request.getVipRows() : Set.of();
        String coupleRow = request.getCoupleRow();

        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < request.getTotalRows(); row++) {
            // row 0 → "A", row 1 → "B", ..., row 25 → "Z"
            String rowLabel = String.valueOf((char) ('A' + row));

            // Xác định loại ghế cho hàng này
            SeatType seatType;
            if (rowLabel.equalsIgnoreCase(coupleRow)) {
                seatType = SeatType.COUPLE;
            } else if (vipRows.contains(rowLabel)) {
                seatType = SeatType.VIP;
            } else {
                seatType = SeatType.STANDARD;
            }

            for (int col = 1; col <= request.getTotalCols(); col++) {
                Seat seat = Seat.builder()
                        .room(room)
                        .rowLabel(rowLabel)
                        .colNumber(col)
                        .seatNumber(rowLabel + col)  // "A1", "B5", "J12"
                        .seatType(seatType)
                        .status(SeatStatus.AVAILABLE)
                        .build();
                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);

        // Cập nhật totalSeats trong Room
        room.setTotalSeats(seats.size());
        roomRepository.save(room);

        log.info("Generated {} seats for room {}", seats.size(), room.getName());
        return getSeatMap(roomId);
    }

    /**
     * (ADMIN) Sửa loại/trạng thái ghế.
     * VD: đổi ghế A1 từ STANDARD → VIP, hoặc đánh dấu ghế hỏng BROKEN.
     */
    @Transactional
    public SeatResponse updateSeat(Long seatId, UpdateSeatRequest request) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        if (request.getSeatType() != null) {
            seat.setSeatType(request.getSeatType());
        }
        if (request.getStatus() != null) {
            seat.setStatus(request.getStatus());
        }

        seatRepository.save(seat);
        log.info("Updated seat {}: type={}, status={}", seat.getSeatNumber(), seat.getSeatType(), seat.getStatus());
        return seatMapper.toResponse(seat);
    }

    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .filter(r -> !"DELETED".equals(r.getStorageState()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }
}
