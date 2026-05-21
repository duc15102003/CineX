# Module Payment — Giải thích chi tiết

## 1. Tổng quan
Module Payment xử lý **thanh toán** cho booking — tạo payment, xử lý callback từ cổng thanh toán, xuất vé điện tử.

Giai đoạn đầu dùng **MockPaymentProcessor** (giả lập luôn thành công). Sau có thể thay bằng VNPay/Momo thật mà **không sửa code cũ** (Factory + Strategy pattern).

## 2. Luồng thanh toán

```
User hold ghế (task 009)
    │
    ▼
POST /api/payments/create { bookingId: 1, paymentMethod: "VNPAY" }
    │
    ▼
PaymentService.createPayment()
    ├── Factory chọn processor: "VNPAY" → MockPaymentProcessor
    ├── Processor tạo payment → trả paymentUrl
    ├── Lưu Payment (PENDING)
    └── Trả response { paymentUrl: "http://...callback?transactionCode=PAY-20260520-001&status=SUCCESS" }
    │
    ▼
FE redirect user đến paymentUrl (mock hoặc VNPay trang thanh toán)
    │
    ▼
User thanh toán xong → cổng redirect về callback URL
    │
    ▼
GET /api/payments/callback?transactionCode=PAY-20260520-001&status=SUCCESS
    │
    ▼
PaymentService.handleCallback()
    ├── Tìm Payment theo transactionCode
    ├── Processor verify callback → SUCCESS
    ├── Payment → COMPLETED, paidAt = now
    ├── Booking → CONFIRMED, BookingSeats → BOOKED
    ├── Publish PaymentCompletedEvent (Observer)
    │       └── PaymentEventListener: gửi email xác nhận (async)
    └── Trả response { status: "COMPLETED" }
```

## 3. Design Patterns

### 3.1 Factory Pattern — Chọn đúng processor

```java
// PaymentProcessorFactory — Spring tự inject Map<tên, processor>
@Component
public class PaymentProcessorFactory {
    private final Map<String, PaymentProcessor> processors;
    // Spring inject: {"VNPAY": MockPaymentProcessor, "CASH": CashPaymentProcessor}

    public PaymentProcessor getProcessor(PaymentMethod method) {
        return processors.get(method.name());  // "VNPAY" → MockPaymentProcessor
    }
}
```

**Tại sao không dùng if-else?**
```java
// ❌ if-else: thêm MOMO → sửa code cũ → vi phạm Open/Closed
if (method == VNPAY) return new VNPayProcessor();
else if (method == MOMO) return new MomoProcessor();  // phải sửa!
else if (method == CASH) return new CashProcessor();

// ✅ Factory: thêm MOMO → tạo MomoProcessor + @Component("MOMO") → xong!
// PaymentProcessorFactory KHÔNG SỬA
```

### 3.2 Strategy Pattern — Mỗi cổng xử lý khác nhau

```java
// Interface chung
public interface PaymentProcessor {
    String createPayment(String code, BigDecimal amount, String desc);
    boolean verifyCallback(Map<String, String> params);
}

// Mock (dev): luôn thành công
@Component("VNPAY")
public class MockPaymentProcessor implements PaymentProcessor { ... }

// Cash: admin xác nhận tại quầy
@Component("CASH")
public class CashPaymentProcessor implements PaymentProcessor { ... }

// Sau này: VNPay thật
@Component("VNPAY")  // thay thế Mock
public class VNPayPaymentProcessor implements PaymentProcessor { ... }
```

### 3.3 Observer Pattern — Spring Events

```java
// Publisher: PaymentService
eventPublisher.publishEvent(new PaymentCompletedEvent(this, payment));
// PaymentService KHÔNG biết ai lắng nghe → loose coupling

// Listener: tự động chạy khi event được publish
@Async  // Chạy trên thread riêng, không block response
@EventListener
public void handlePaymentCompleted(PaymentCompletedEvent event) {
    // Gửi email, push notification, ghi log, ...
}
```

## 4. Request/Response mẫu

### POST /api/payments/create
```bash
curl -X POST http://localhost:8088/api/payments/create \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"bookingId": 1, "paymentMethod": "VNPAY"}'
```

```json
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 1,
    "bookingCode": "CX-20260520-001",
    "amount": 275000,
    "method": "VNPAY",
    "transactionCode": "PAY-20260520-001",
    "status": "PENDING",
    "paymentUrl": "http://localhost:8088/api/payments/callback?transactionCode=PAY-20260520-001&status=SUCCESS"
  }
}
```

### GET /api/bookings/{id}/ticket
```json
{
  "bookingCode": "CX-20260520-001",
  "movieTitle": "Avengers: Endgame",
  "startTime": "2026-05-25T14:00",
  "roomName": "Room IMAX",
  "seats": [
    {"seatNumber": "E1", "seatType": "VIP", "price": 100000},
    {"seatNumber": "E2", "seatType": "VIP", "price": 100000}
  ],
  "totalAmount": 200000,
  "paymentMethod": "VNPAY",
  "qrCodeBase64": "iVBORw0KGgoAAAANSUhEUg..."
}
```

## 5. Câu hỏi tự kiểm tra

1. **Factory Pattern: Spring inject `Map<String, PaymentProcessor>` từ đâu?**
   → Từ tất cả Bean implement PaymentProcessor. Key = tên @Component ("VNPAY", "CASH"). Spring tự gom lại thành Map.

2. **Thêm cổng Momo → cần sửa bao nhiêu file?**
   → Chỉ 1 file: tạo `MomoPaymentProcessor implements PaymentProcessor` + `@Component("MOMO")`. Factory + Service KHÔNG SỬA.

3. **@Async trên EventListener để làm gì?**
   → Chạy trên thread riêng. User nhận response "Payment completed" ngay, email gửi ngầm phía sau. Không có @Async → user chờ gửi email xong mới nhận response.

4. **Tại sao callback là GET không phải POST?**
   → Vì cổng thanh toán (VNPay) redirect user bằng URL GET. Browser redirect = GET request.
