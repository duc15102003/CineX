package com.cinex.module.payment.service;

import com.cinex.module.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * [Observer Pattern] Lắng nghe PaymentCompletedEvent.
 * Khi thanh toán thành công → gửi email xác nhận vé.
 *
 * @Async: chạy trên thread riêng → không block response của PaymentService.
 * User nhận response "Payment completed" ngay, email gửi ngầm phía sau.
 */
@Component
@Slf4j
public class PaymentEventListener {

    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Payment payment = event.getPayment();
        String bookingCode = payment.getBooking().getBookingCode();
        String userEmail = payment.getBooking().getUser().getEmail();

        // TODO: Tích hợp EmailService gửi email xác nhận vé + QR code
        log.info("Payment completed for booking {}. Sending confirmation email to {}",
                bookingCode, userEmail);
    }
}
