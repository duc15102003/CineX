package com.cinex.module.payment.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * [Strategy] Mock payment — giả lập thanh toán luôn thành công.
 * Dùng cho dev/test. Production sẽ thay bằng VNPayPaymentProcessor.
 */
@Component("VNPAY")
@Slf4j
public class MockPaymentProcessor implements PaymentProcessor {

    @Override
    public String createPayment(String transactionCode, BigDecimal amount, String description) {
        // Mock: trả URL giả lập → FE redirect đến URL này → gọi callback
        String paymentUrl = "http://localhost:8088/api/payments/callback"
                + "?transactionCode=" + transactionCode
                + "&status=SUCCESS";
        log.info("Mock payment created: {} - {} VND - URL: {}", transactionCode, amount, paymentUrl);
        return paymentUrl;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        // Mock: luôn thành công nếu status = SUCCESS
        return "SUCCESS".equals(params.get("status"));
    }
}
