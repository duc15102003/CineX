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

    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms() {
        return roomRepository.findAll().stream()
                .filter(r -> !"DELETED".equals(r.getStorageState()))
                .map(roomMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long id) {
        return roomMapper.toResponse(findRoomById(id));
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
        Room room = findRoomById(id);

        // Kiểm tra trùng tên với phòng khác
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
        Room room = findRoomById(id);
        room.setStorageState("DELETED");
        roomRepository.save(room);
        log.info("Soft deleted room: {}", room.getName());
    }

    private Room findRoomById(Long id) {
        return roomRepository.findById(id)
                .filter(r -> !"DELETED".equals(r.getStorageState()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }
}
