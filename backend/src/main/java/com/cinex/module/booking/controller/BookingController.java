package com.cinex.module.booking.controller;

import com.cinex.common.response.ApiResponse;
import com.cinex.common.response.PageResponse;
import com.cinex.common.service.QrCodeService;
import com.cinex.common.util.SecurityUtil;
import com.cinex.module.auth.entity.User;
import com.cinex.module.auth.repository.UserRepository;
import com.cinex.module.booking.dto.*;
import com.cinex.module.booking.service.BookingService;
import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Booking management — hold, confirm, cancel, check-in")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    @PostMapping("/hold")
    @Operation(summary = "Hold seats for a showtime (10 min)")
    public ApiResponse<HoldSeatsResponse> holdSeats(@Valid @RequestBody HoldSeatsRequest request) {
        Long userId = getCurrentUserId();
        return ApiResponse.ok("Seats held", bookingService.holdSeats(userId, request));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm booking (after payment)")
    public ApiResponse<BookingResponse> confirmBooking(@Valid @RequestBody ConfirmBookingRequest request) {
        Long userId = getCurrentUserId();
        return ApiResponse.ok("Booking confirmed", bookingService.confirmBooking(userId, request));
    }

    @GetMapping("/me")
    @Operation(summary = "List my bookings")
    public ApiResponse<PageResponse<BookingListResponse>> getMyBookings(
            BookingFilter filter,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = getCurrentUserId();
        return ApiResponse.ok(PageResponse.from(bookingService.getMyBookings(userId, filter, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking detail")
    public ApiResponse<BookingResponse> getBookingDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ApiResponse.ok(bookingService.getBookingDetail(userId, id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking (before showtime starts)")
    public ApiResponse<BookingResponse> cancelBooking(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ApiResponse.ok("Booking cancelled", bookingService.cancelBooking(userId, id));
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "(Admin/Staff) Check-in by booking code")
    public ApiResponse<BookingResponse> checkIn(@RequestParam String code) {
        return ApiResponse.ok("Checked in", bookingService.checkIn(code));
    }

    @GetMapping("/{id}/qr")
    @Operation(summary = "Get QR code for booking (base64)")
    public ApiResponse<String> getQrCode(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        BookingResponse booking = bookingService.getBookingDetail(userId, id);
        String qrBase64 = qrCodeService.generateQrCodeBase64(booking.getBookingCode(), 300);
        return ApiResponse.ok(qrBase64);
    }

    private Long getCurrentUserId() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }
}
