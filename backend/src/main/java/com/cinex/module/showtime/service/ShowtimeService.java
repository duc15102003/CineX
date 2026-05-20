package com.cinex.module.showtime.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.module.config.service.SystemConfigService;
import com.cinex.module.movie.entity.Movie;
import com.cinex.module.movie.repository.MovieRepository;
import com.cinex.module.room.entity.Room;
import com.cinex.module.room.repository.RoomRepository;
import com.cinex.module.seat.repository.SeatRepository;
import com.cinex.module.showtime.dto.ShowtimeFilter;
import com.cinex.module.showtime.dto.ShowtimeListResponse;
import com.cinex.module.showtime.dto.ShowtimeRequest;
import com.cinex.module.showtime.dto.ShowtimeResponse;
import com.cinex.module.showtime.entity.Showtime;
import com.cinex.module.showtime.entity.ShowtimeStatus;
import com.cinex.module.showtime.mapper.ShowtimeMapper;
import com.cinex.module.showtime.repository.ShowtimeRepository;
import com.cinex.module.showtime.specification.ShowtimeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeMapper showtimeMapper;
    private final SystemConfigService systemConfigService;

    @Transactional(readOnly = true)
    public Page<ShowtimeListResponse> listShowtimes(ShowtimeFilter filter, Pageable pageable) {
        var spec = ShowtimeSpecification.fromFilter(filter);
        return showtimeRepository.findAll(spec, pageable)
                .map(showtimeMapper::toListResponse);
    }

    @Transactional(readOnly = true)
    public ShowtimeResponse getShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOWTIME_NOT_FOUND));

        ShowtimeResponse response = showtimeMapper.toResponse(showtime);

        // Tính số ghế trống = tổng ghế phòng - ghế đã đặt/đang giữ
        int totalSeats = showtime.getRoom().getTotalSeats();
        // TODO: trừ ghế đã booking khi có module Booking (task 009)
        return ShowtimeResponse.builder()
                .id(response.getId())
                .storageState(response.getStorageState())
                .movieId(response.getMovieId())
                .movieTitle(response.getMovieTitle())
                .moviePosterUrl(response.getMoviePosterUrl())
                .movieDuration(response.getMovieDuration())
                .roomId(response.getRoomId())
                .roomName(response.getRoomName())
                .roomType(response.getRoomType())
                .startTime(response.getStartTime())
                .endTime(response.getEndTime())
                .basePrice(response.getBasePrice())
                .vipPrice(response.getVipPrice())
                .couplePrice(response.getCouplePrice())
                .status(response.getStatus())
                .availableSeats(totalSeats)
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }

    /**
     * Tạo suất chiếu:
     * 1. Validate: phim + phòng tồn tại
     * 2. Không cho tạo suất trong quá khứ
     * 3. Tính endTime = startTime + movie.duration + buffer (từ SystemConfig)
     * 4. Kiểm tra phòng trống (không trùng giờ với suất khác)
     * 5. Save
     */
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // Không cho tạo suất trong quá khứ
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Cannot create showtime in the past");
        }

        // Tính endTime = startTime + duration + buffer
        int bufferMinutes = systemConfigService.getInt("showtime.buffer_minutes", 15);
        LocalDateTime endTime = request.getStartTime()
                .plusMinutes(movie.getDuration())
                .plusMinutes(bufferMinutes);

        // Kiểm tra phòng trống
        List<Showtime> conflicts = showtimeRepository.findConflictingShowtimes(
                room.getId(), request.getStartTime(), endTime);
        if (!conflicts.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOWTIME_CONFLICT,
                    "Room '" + room.getName() + "' already has a showtime during this time");
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .basePrice(request.getBasePrice())
                .vipPrice(request.getVipPrice())
                .couplePrice(request.getCouplePrice())
                .status(ShowtimeStatus.SCHEDULED)
                .build();

        showtimeRepository.save(showtime);
        log.info("Created showtime: {} at {} in {}", movie.getTitle(), request.getStartTime(), room.getName());
        return getShowtime(showtime.getId());
    }

    @Transactional
    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOWTIME_NOT_FOUND));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        int bufferMinutes = systemConfigService.getInt("showtime.buffer_minutes", 15);
        LocalDateTime endTime = request.getStartTime()
                .plusMinutes(movie.getDuration())
                .plusMinutes(bufferMinutes);

        // Kiểm tra trùng giờ (loại trừ chính nó)
        List<Showtime> conflicts = showtimeRepository.findConflictingShowtimes(
                room.getId(), request.getStartTime(), endTime);
        conflicts.removeIf(s -> s.getId().equals(id));
        if (!conflicts.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOWTIME_CONFLICT,
                    "Room '" + room.getName() + "' already has a showtime during this time");
        }

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(endTime);
        showtime.setBasePrice(request.getBasePrice());
        showtime.setVipPrice(request.getVipPrice());
        showtime.setCouplePrice(request.getCouplePrice());

        showtimeRepository.save(showtime);
        log.info("Updated showtime {}", id);
        return getShowtime(id);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOWTIME_NOT_FOUND));
        showtime.setStorageState("DELETED");
        showtimeRepository.save(showtime);
        log.info("Soft deleted showtime {}", id);
    }

    @Transactional
    public ShowtimeResponse restoreShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOWTIME_NOT_FOUND));
        showtime.setStorageState(null);
        showtimeRepository.save(showtime);
        log.info("Restored showtime {}", id);
        return getShowtime(id);
    }
}
