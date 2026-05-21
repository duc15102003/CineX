package com.cinex.module.payment.controller;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.common.response.ApiResponse;
import com.cinex.common.util.SecurityUtil;
import com.cinex.module.auth.repository.UserRepository;
import com.cinex.module.payment.dto.CreatePaymentRequest;
import com.cinex.module.payment.dto.PaymentResponse;
import com.cinex.module.payment.dto.TicketResponse;
import com.cinex.module.payment.service.PaymentService;
import com.cinex.module.payment.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment processing")
public class PaymentController {

    private final PaymentService paymentService;
    private final TicketService ticketService;
    private final UserRepository userRepository;

    @PostMapping("/payments/create")
    @Operation(summary = "Create payment for a booking")
    public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Long userId = getCurrentUserId();
        return ApiResponse.ok("Payment created", paymentService.createPayment(userId, request));
    }

    /**
     * Callback từ cổng thanh toán (VNPay pattern).
     * Cổng redirect user về URL này với params (transactionCode, status, ...).
     */
    @GetMapping("/payments/callback")
    @Operation(summary = "Payment callback (from payment gateway)")
    public ApiResponse<PaymentResponse> paymentCallback(@RequestParam Map<String, String> params) {
        return ApiResponse.ok("Payment processed", paymentService.handleCallback(params));
    }

    @GetMapping("/payments/{bookingId}")
    @Operation(summary = "Get payment status by booking ID")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable Long bookingId) {
        return ApiResponse.ok(paymentService.getPaymentByBookingId(bookingId));
    }

    @GetMapping("/bookings/{bookingId}/ticket")
    @Operation(summary = "Get digital ticket with QR code")
    public ApiResponse<TicketResponse> getTicket(@PathVariable Long bookingId) {
        Long userId = getCurrentUserId();
        return ApiResponse.ok(ticketService.generateTicket(userId, bookingId));
    }

    private Long getCurrentUserId() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }
}
