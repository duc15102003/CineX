# Task: Module Payment — Thanh toán

## Status: PENDING

## Module
backend

## Mô tả
Xử lý thanh toán cho booking. Giai đoạn đầu giả lập thanh toán (mock), sau có thể tích hợp VNPay/Momo thật.

**Kết quả mong đợi:**
- POST `/api/payments/create` — tạo yêu cầu thanh toán -> trả URL thanh toán (mock)
- GET `/api/payments/callback` — callback sau khi thanh toán (VNPay pattern)
- GET `/api/payments/{bookingId}` — xem trạng thái thanh toán (không filter DELETED)
- GET `/api/bookings/{id}/ticket` — xuất vé điện tử (JSON)

## Đã có sẵn (KHÔNG cần tạo lại)
- Liquibase `012-create-payments-table.xml` — bảng `payments`
- Pattern: Filter DTO + Specification — áp dụng thống nhất

## Việc cần làm

### Entity
- [ ] `Payment.java` entity — `@OneToOne` với Booking
- [ ] `PaymentMethod.java` enum — VNPAY, MOMO, CASH
- [ ] `PaymentStatus.java` enum — PENDING, COMPLETED, FAILED, REFUNDED

### DTO
- [ ] `CreatePaymentRequest.java` — bookingId, paymentMethod
- [ ] `PaymentResponse.java` — id, storageState, bookingId, amount, method, status, paymentUrl, createdAt, updatedAt
- [ ] `TicketResponse.java` — bookingCode, movieTitle, showtimeInfo, roomName, seats[], totalAmount, qrCodeBase64

### Service
- [ ] `PaymentService.java`:
  - `createPayment(request)` — tạo payment record -> gọi processor -> trả URL
  - `handleCallback(params)` — verify callback -> cập nhật status -> confirm booking
  - `getPaymentStatus(bookingId)` — trạng thái, không filter DELETED
- [ ] `TicketService.java`:
  - `generateTicket(bookingId)` — tạo TicketResponse (chứa QR base64)

### Controller
- [ ] `PaymentController.java`

### Pattern: Factory + Strategy
- [ ] `PaymentProcessor` interface — processPayment(), verifyCallback()
- [ ] `MockPaymentProcessor` — giả lập (luôn thành công)
- [ ] `PaymentProcessorFactory` — trả đúng processor theo method

### Spring Events (Observer Pattern)
- [ ] `PaymentCompletedEvent.java` — event class
- [ ] `PaymentEventListener.java` — `@EventListener` gửi email vé khi payment completed

## Design Patterns cần áp dụng

### 1. Factory Pattern — PaymentProcessorFactory
### 2. Strategy Pattern — PaymentProcessor interface
### 3. Observer Pattern — Spring Events

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] Tạo payment -> nhận URL mock -> gọi callback -> booking confirmed
- [ ] Xem vé điện tử có đủ thông tin + QR code
- [ ] Factory pattern hoạt động: đổi method -> đổi processor
- [ ] Event gửi email khi payment completed
- [ ] Response DTO có storageState, createdAt, updatedAt

## Ghi chú
- Giai đoạn đầu dùng MockPaymentProcessor (luôn thành công)
- Phụ thuộc: task 009 (Booking)

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Viết `/docs/module-guides/payment-explained.md`
- [ ] Đổi Status -> DONE, tick [x], move sang `done/`
