package com.cinex.module.booking.service;

import com.cinex.common.entity.tracker.IdTrackerService;
import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.module.auth.entity.User;
import com.cinex.module.auth.repository.UserRepository;
import com.cinex.module.booking.dto.*;
import com.cinex.module.booking.entity.Booking;
import com.cinex.module.booking.entity.BookingSeat;
import com.cinex.module.booking.entity.BookingSeatStatus;
import com.cinex.module.booking.entity.BookingStatus;
import com.cinex.module.booking.repository.BookingRepository;
import com.cinex.module.booking.repository.BookingSeatRepository;
import com.cinex.module.booking.specification.BookingSpecification;
import com.cinex.module.config.service.SystemConfigService;
import com.cinex.module.seat.entity.Seat;
import com.cinex.module.seat.entity.SeatType;
import com.cinex.module.seat.repository.SeatRepository;
import com.cinex.module.showtime.entity.Showtime;
import com.cinex.module.booking.service.SeatWebSocketService;
import com.cinex.module.showtime.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final IdTrackerService idTrackerService;
    private final SystemConfigService systemConfigService;
    private final SeatWebSocketService seatWebSocketService;

    /**
     * Hold ghế — tạo booking HOLDING, lock ghế 10 phút.
     *
     * Luồng:
     * 1. Validate: showtime tồn tại, chưa chiếu, max seats
     * 2. Kiểm tra ghế trống (không ai HELD/BOOKED)
     * 3. Tính tổng tiền theo loại ghế (STANDARD/VIP/COUPLE)
     * 4. Tạo Booking + BookingSeats
     * 5. Sinh bookingCode unique (IdTrackerService)
     */
    @Transactional
    public HoldSeatsResponse holdSeats(Long userId, HoldSeatsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOWTIME_NOT_FOUND));

        // Validate: suất chiếu chưa bắt đầu
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Showtime has already started");
        }

        // Validate: max seats từ SystemConfig
        int maxSeats = systemConfigService.getInt("booking.max_seats", 8);
        if (request.getSeatIds().size() > maxSeats) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Maximum " + maxSeats + " seats per booking");
        }

        // Kiểm tra ghế trống — nếu có ai HELD/BOOKED → lỗi
        List<BookingSeat> occupied = bookingSeatRepository.findHeldOrBookedSeats(
                request.getShowtimeId(), request.getSeatIds());
        if (!occupied.isEmpty()) {
            String takenSeats = occupied.stream()
                    .map(bs -> bs.getSeat().getSeatNumber())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            throw new BusinessException(ErrorCode.SEAT_ALREADY_BOOKED,
                    "Seats already taken: " + takenSeats);
        }

        // Lấy seat entities
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new BusinessException(ErrorCode.SEAT_NOT_FOUND, "One or more seats not found");
        }

        // Tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal price = getPriceForSeat(seat.getSeatType(), showtime);
            totalAmount = totalAmount.add(price);
        }

        // Tạo booking
        String bookingCode = idTrackerService.nextCodeWithDate("BOOKING");
        int holdMinutes = systemConfigService.getInt("booking.hold_minutes", 10);

        Booking booking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .totalAmount(totalAmount)
                .status(BookingStatus.HOLDING)
                .bookingCode(bookingCode)
                .build();

        bookingRepository.save(booking);

        // Tạo booking seats
        List<BookingSeatResponse> seatResponses = seats.stream().map(seat -> {
            BigDecimal price = getPriceForSeat(seat.getSeatType(), showtime);
            BookingSeat bs = BookingSeat.builder()
                    .booking(booking)
                    .seat(seat)
                    .price(price)
                    .status(BookingSeatStatus.HELD)
                    .build();
            bookingSeatRepository.save(bs);

            return BookingSeatResponse.builder()
                    .seatId(seat.getId())
                    .seatNumber(seat.getSeatNumber())
                    .seatType(seat.getSeatType().name())
                    .price(price)
                    .status(BookingSeatStatus.HELD.name())
                    .build();
        }).toList();

        log.info("User {} held {} seats for showtime {}, booking {}",
                user.getUsername(), seats.size(), showtime.getId(), bookingCode);

        // Real-time: notify tất cả user đang xem sơ đồ ghế
        seatWebSocketService.notifySeatChanged(showtime.getId(), request.getSeatIds(), "HELD");

        return HoldSeatsResponse.builder()
                .bookingId(booking.getId())
                .bookingCode(bookingCode)
                .holdExpiry(booking.getCreatedAt().plusMinutes(holdMinutes))
                .totalAmount(totalAmount)
                .seats(seatResponses)
                .build();
    }

    /**
     * Confirm booking — đổi HOLDING → CONFIRMED.
     */
    @Transactional
    public BookingResponse confirmBooking(Long userId, ConfirmBookingRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        // Validate quyền
        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Not your booking");
        }

        // Validate trạng thái
        if (booking.getStatus() != BookingStatus.HOLDING) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Booking is not in HOLDING status, current: " + booking.getStatus());
        }

        // Kiểm tra hết hạn hold
        int holdMinutes = systemConfigService.getInt("booking.hold_minutes", 10);
        if (booking.getCreatedAt().plusMinutes(holdMinutes).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED, "Hold has expired");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        booking.getBookingSeats().forEach(bs -> bs.setStatus(BookingSeatStatus.BOOKED));
        bookingRepository.save(booking);

        log.info("Booking {} confirmed", booking.getBookingCode());
        return toBookingResponse(booking);
    }

    /**
     * Cancel booking — user hủy vé (chỉ khi suất chiếu chưa bắt đầu).
     */
    @Transactional
    public BookingResponse cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Not your booking");
        }

        if (booking.getStatus() != BookingStatus.HOLDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Cannot cancel booking with status: " + booking.getStatus());
        }

        // Chỉ hủy nếu suất chiếu chưa bắt đầu
        if (booking.getShowtime().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Showtime has already started, cannot cancel");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.getBookingSeats().forEach(bs -> bs.setStatus(BookingSeatStatus.CANCELLED));
        bookingRepository.save(booking);

        log.info("Booking {} cancelled by user", booking.getBookingCode());

        // Real-time: ghế trả lại → notify AVAILABLE
        List<Long> seatIds = booking.getBookingSeats().stream()
                .map(bs -> bs.getSeat().getId()).toList();
        seatWebSocketService.notifySeatChanged(booking.getShowtime().getId(), seatIds, "AVAILABLE");

        return toBookingResponse(booking);
    }

    /**
     * Check-in — staff/admin quét QR (bookingCode) → đổi CHECKED_IN.
     */
    @Transactional
    public BookingResponse checkIn(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND,
                        "Booking not found: " + bookingCode));

        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket already used");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Booking is not confirmed, status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);

        log.info("Booking {} checked in", bookingCode);
        return toBookingResponse(booking);
    }

    /**
     * Danh sách vé của user — Filter DTO + Specification.
     */
    @Transactional(readOnly = true)
    public Page<BookingListResponse> getMyBookings(Long userId, BookingFilter filter, Pageable pageable) {
        var spec = BookingSpecification.fromFilter(filter, userId);
        return bookingRepository.findAll(spec, pageable)
                .map(this::toBookingListResponse);
    }

    /**
     * Chi tiết vé — không filter DELETED.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingDetail(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Not your booking");
        }

        return toBookingResponse(booking);
    }

    /**
     * Sơ đồ ghế với trạng thái cho 1 suất chiếu.
     */
    @Transactional(readOnly = true)
    public List<BookingSeat> getOccupiedSeats(Long showtimeId) {
        return bookingSeatRepository.findAllOccupiedByShowtimeId(showtimeId);
    }

    // === Private helpers ===

    private BigDecimal getPriceForSeat(SeatType seatType, Showtime showtime) {
        return switch (seatType) {
            case VIP -> showtime.getVipPrice();
            case COUPLE -> showtime.getCouplePrice();
            default -> showtime.getBasePrice();
        };
    }

    private BookingResponse toBookingResponse(Booking booking) {
        List<BookingSeatResponse> seatResponses = booking.getBookingSeats().stream()
                .map(bs -> BookingSeatResponse.builder()
                        .seatId(bs.getSeat().getId())
                        .seatNumber(bs.getSeat().getSeatNumber())
                        .seatType(bs.getSeat().getSeatType().name())
                        .price(bs.getPrice())
                        .status(bs.getStatus().name())
                        .build())
                .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .storageState(booking.getStorageState())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus())
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .moviePosterUrl(booking.getShowtime().getMovie().getPosterUrl())
                .showtimeId(booking.getShowtime().getId())
                .startTime(booking.getShowtime().getStartTime())
                .endTime(booking.getShowtime().getEndTime())
                .roomName(booking.getShowtime().getRoom().getName())
                .roomType(booking.getShowtime().getRoom().getType().name())
                .seats(seatResponses)
                .totalAmount(booking.getTotalAmount())
                .confirmedAt(booking.getConfirmedAt())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private BookingListResponse toBookingListResponse(Booking booking) {
        return BookingListResponse.builder()
                .id(booking.getId())
                .storageState(booking.getStorageState())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus())
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .moviePosterUrl(booking.getShowtime().getMovie().getPosterUrl())
                .startTime(booking.getShowtime().getStartTime())
                .roomName(booking.getShowtime().getRoom().getName())
                .totalAmount(booking.getTotalAmount())
                .seatCount(booking.getBookingSeats().size())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
