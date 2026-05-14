# Task: Module Payment — Thanh toán, xuất vé

## Status: PENDING

## Module
backend

## Mô tả
Xử lý thanh toán cho booking. Giai đoạn đầu giả lập thanh toán (mock), sau có thể tích hợp VNPay/Momo thật. Sau khi thanh toán thành công → xuất vé điện tử.

**Kết quả mong đợi:**
- POST `/api/payments/create` → tạo yêu cầu thanh toán cho booking → trả URL thanh toán (mock)
- GET `/api/payments/callback` → callback sau khi thanh toán (VNPay pattern)
- GET `/api/payments/{bookingId}` → xem trạng thái thanh toán
- GET `/api/bookings/{id}/ticket` → xuất vé điện tử (JSON hoặc PDF)

## Việc cần làm

### Entity & Liquibase
- [ ] `012-create-payments-table.xml` — bảng `payments` (id, bookingId, amount, method, transactionCode, status, paidAt, createdAt)
- [ ] `Payment.java` entity — `@OneToOne` với Booking

### DTO
- [ ] `CreatePaymentRequest.java` — bookingId, paymentMethod (VNPAY/MOMO/CASH)
- [ ] `PaymentResponse.java` — id, bookingId, amount, method, status, paymentUrl
- [ ] `TicketResponse.java` — bookingCode, movieTitle, showtimeInfo, roomName, seats[], totalAmount, qrCode

### Service
- [ ] `PaymentService.java`:
  - `createPayment(request)` → tạo payment record → sinh URL thanh toán mock
  - `handleCallback(params)` → verify callback → cập nhật status → confirm booking
  - `getPaymentStatus(bookingId)` → trạng thái
- [ ] `TicketService.java`:
  - `generateTicket(bookingId)` → tạo thông tin vé
  - (Tùy chọn) `generateTicketPdf(bookingId)` → xuất PDF

### Controller
- [ ] `PaymentController.java`

### Pattern: Factory
- [ ] `PaymentProcessor` interface — processPayment(), verifyCallback()
- [ ] `MockPaymentProcessor` — giả lập thanh toán (luôn thành công sau 3 giây)
- [ ] (Sau này) `VNPayPaymentProcessor`, `MomoPaymentProcessor`
- [ ] `PaymentProcessorFactory` — trả đúng processor theo method

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] Tạo payment → nhận URL mock → gọi callback → booking confirmed
- [ ] Xem vé điện tử có đủ thông tin: phim, suất, phòng, ghế, mã QR
- [ ] Payment status: PENDING → COMPLETED hoặc FAILED
- [ ] Factory pattern hoạt động: đổi method → đổi processor

## Design Patterns cần áp dụng (MỤC TIÊU HỌC)

### 1. Factory Pattern
- **Ở đâu:** `PaymentProcessorFactory.getProcessor(method)` → trả về đúng loại processor
- **Tại sao:** Khi thêm phương thức thanh toán mới (VD: ZaloPay), chỉ cần tạo class mới + đăng ký vào factory, KHÔNG sửa code cũ
- **Học được gì:** Factory Method, Open/Closed Principle (SOLID), tại sao tránh switch-case dài

### 2. Strategy Pattern
- **Ở đâu:** `PaymentProcessor` interface với nhiều implementation (MockProcessor, VNPayProcessor, ...)
- **Tại sao:** Mỗi cổng thanh toán có cách xử lý khác nhau (tạo URL khác, verify callback khác), nhưng Service gọi cùng 1 interface
- **Học được gì:** Interface abstraction, dependency injection, polymorphism thực tế

### 3. Observer Pattern (Spring Events)
- **Ở đâu:** Khi thanh toán thành công → publish `PaymentCompletedEvent` → listener gửi email, cập nhật booking
- **Tại sao:** Payment Service không cần biết về Email Service hay Booking Service. Loose coupling
- **Học được gì:** `ApplicationEventPublisher`, `@EventListener`, decoupling

## Tham khảo
- Payment method: `VNPAY`, `MOMO`, `CASH` (tại quầy)
- Status: `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`
- `docs/erd.md` — sơ đồ bảng payments

## Ghi chú
- Giai đoạn đầu dùng MockPaymentProcessor (luôn thành công)
- QR code cho vé: có thể dùng bookingCode encode thành QR
- Tích hợp VNPay/Momo thật cần API key → làm sau nếu còn thời gian
- Cash payment: admin xác nhận tại quầy → không cần callback

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] **Giải thích từng file** đã tạo: tác dụng, design pattern nào
- [ ] **Viết `/docs/payment-explained.md`** bao gồm:
  1. Factory Pattern: vẽ sơ đồ class, giải thích tại sao không dùng if-else/switch
  2. Strategy Pattern: interface PaymentProcessor và các implementation
  3. Observer Pattern: Spring Events, decoupling giữa Payment và Email/Booking
  4. Luồng thanh toán từng bước (VNPay pattern: redirect → callback)
  5. So sánh mock vs thật: khác gì, chuyển đổi thế nào
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
