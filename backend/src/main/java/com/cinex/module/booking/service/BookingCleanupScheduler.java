package com.cinex.module.booking.service;

import com.cinex.module.booking.entity.Booking;
import com.cinex.module.booking.entity.BookingSeatStatus;
import com.cinex.module.booking.entity.BookingStatus;
import com.cinex.module.booking.repository.BookingRepository;
import com.cinex.module.config.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [Scheduled Task Pattern] Chạy mỗi phút, hủy booking HOLDING quá hạn.
 *
 * Ví dụ đời thường: như nhân viên rạp đi kiểm tra "ghế nào giữ quá 10 phút
 * mà chưa thanh toán → trả lại cho người khác đặt".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupScheduler {

    private final BookingRepository bookingRepository;
    private final SystemConfigService systemConfigService;

    /**
     * @Scheduled(fixedRate = 60000) = chạy mỗi 60 giây (1 phút).
     * Tìm booking HOLDING + createdAt < (now - holdMinutes) → đổi EXPIRED.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredHolds() {
        int holdMinutes = systemConfigService.getInt("booking.hold_minutes", 10);
        LocalDateTime expireBefore = LocalDateTime.now().minusMinutes(holdMinutes);

        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(
                BookingStatus.HOLDING, expireBefore);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.getBookingSeats().forEach(bs -> bs.setStatus(BookingSeatStatus.CANCELLED));
            bookingRepository.save(booking);
            log.info("Expired booking {} (held for > {} minutes)", booking.getBookingCode(), holdMinutes);
        }

        if (!expiredBookings.isEmpty()) {
            log.info("Cleaned up {} expired bookings", expiredBookings.size());
        }
    }
}
