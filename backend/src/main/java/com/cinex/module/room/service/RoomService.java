package com.cinex.module.room.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.module.room.dto.RoomRequest;
import com.cinex.module.room.dto.RoomResponse;
import com.cinex.module.room.entity.Room;
import com.cinex.module.room.entity.RoomStatus;
import com.cinex.module.room.mapper.RoomMapper;
import com.cinex.module.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    /**
     * Danh sách phòng.
     * includeDeleted = false (mặc định): chỉ trả phòng chưa xóa (filter SQL)
     * includeDeleted = true: trả tất cả kể cả đã xóa (admin xem lại + khôi phục)
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms(boolean includeDeleted) {
        List<Room> rooms = includeDeleted
                ? roomRepository.findAll()
                : roomRepository.findAllActive();
        return rooms.stream().map(roomMapper::toResponse).toList();
    }

    /**
     * Chi tiết phòng — trả về bình thường, KHÔNG filter DELETED.
     * Lý do: list đã control hiển thị, user click từ list → phải thấy chi tiết.
     * FE tự xử lý hiển thị dựa vào storageState trong response.
     */
    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.ROOM_EXISTED,
                    "Room '" + request.getName() + "' already exists");
        }

        Room room = Room.builder()
                .name(request.getName())
                .type(request.getType())
                .totalSeats(request.getTotalSeats())
                .status(request.getStatus() != null ? request.getStatus() : RoomStatus.ACTIVE)
                .build();

        roomRepository.save(room);
        log.info("Created room: {}", room.getName());
        return roomMapper.toResponse(room);
    }

    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getName().equals(request.getName()) && roomRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.ROOM_EXISTED,
                    "Room '" + request.getName() + "' already exists");
        }

        room.setName(request.getName());
        room.setType(request.getType());
        room.setTotalSeats(request.getTotalSeats());
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        roomRepository.save(room);
        log.info("Updated room: {}", room.getName());
        return roomMapper.toResponse(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        room.setStorageState("DELETED");
        roomRepository.save(room);
        log.info("Soft deleted room: {}", room.getName());
    }

    /**
     * (ADMIN) Khôi phục phòng đã xóa mềm.
     */
    @Transactional
    public RoomResponse restoreRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        room.setStorageState(null);
        roomRepository.save(room);
        log.info("Restored room: {}", room.getName());
        return roomMapper.toResponse(room);
    }
}
