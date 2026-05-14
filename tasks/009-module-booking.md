# Task: Module Booking — Đặt vé xem phim

## Status: PENDING

## Module
backend

## Mô tả
Chức năng cốt lõi: user chọn suất chiếu → chọn ghế → giữ ghế tạm (5-10 phút) → xác nhận đặt vé → nhận QR code → đến rạp check-in. Hệ thống phải đảm bảo không bán trùng ghế (concurrency).

**Kết quả mong đợi:**
- GET `/api/showtimes/{id}/seats` → sơ đồ ghế với trạng thái (trống/đã đặt/đang giữ)
- POST `/api/bookings/hold` → giữ ghế tạm (lock 10 phút)
- POST `/api/bookings/confirm` → xác nhận đặt vé (sau khi thanh toán)
- GET `/api/bookings/me` → danh sách vé của user
- GET `/api/bookings/{id}` → chi tiết vé (FE dùng bookingCode render QR)
- PUT `/api/bookings/{id}/cancel` → hủy vé (nếu chưa chiếu)
- POST `/api/bookings/check-in?code=xxx` → (STAFF/ADMIN) nhân viên quét QR → verify → đổi status CHECKED_IN

## Việc cần làm

### Entity & Liquibase
- [ ] `010-create-bookings-table.xml` — bảng `bookings` (id, userId, showtimeId, totalAmount, status, bookingCode, createdAt, confirmedAt, cancelledAt)
- [ ] `011-create-booking-seats-table.xml` — bảng `booking_seats` (id, bookingId, seatId, price, status)
- [ ] `Booking.java` entity — `@ManyToOne` User, `@ManyToOne` Showtime, `@OneToMany` BookingSeat
- [ ] `BookingSeat.java` entity — `@ManyToOne` Booking, `@ManyToOne` Seat

### DTO
- [ ] `HoldSeatsRequest.java` — showtimeId, seatIds[] (validation: không rỗng, tối đa 8 ghế)
- [ ] `HoldSeatsResponse.java` — bookingId, holdExpiry, totalAmount, seats[]
- [ ] `ConfirmBookingRequest.java` — bookingId, paymentMethod
- [ ] `BookingResponse.java` — id, bookingCode, movie, showtime, room, seats[], totalAmount, status, createdAt
- [ ] `BookingListResponse.java` — version rút gọn cho danh sách

### Repository
- [ ] `BookingRepository.java` — findByUserId, findByBookingCode
- [ ] `BookingSeatRepository.java` — findByShowtimeIdAndSeatIdIn (kiểm tra ghế đã đặt)

### Service
- [ ] `BookingService.java`:
  - `holdSeats(userId, request)` → kiểm tra ghế trống → tạo booking status=HOLDING → set hết hạn 10 phút
  - `confirmBooking(userId, request)` → kiểm tra booking còn hold → đổi status=CONFIRMED
  - `cancelBooking(userId, bookingId)` → kiểm tra quyền + chưa chiếu → đổi status=CANCELLED
  - `checkIn(bookingCode)` → (STAFF/ADMIN) verify booking → đổi status=CHECKED_IN
  - `getMyBookings(userId, pageable)` → danh sách vé
  - `getBookingDetail(userId, bookingId)` → chi tiết vé (chứa bookingCode để FE render QR)
  - `releaseExpiredHolds()` → `@Scheduled` chạy mỗi phút, hủy các booking HOLDING quá hạn
- [ ] `SeatAvailabilityService.java`:
  - `getAvailableSeats(showtimeId)` → trả sơ đồ ghế với trạng thái

### Controller
- [ ] `BookingController.java` — 7 endpoint (thêm check-in)

### Concurrency
- [ ] Dùng `@Lock(PESSIMISTIC_WRITE)` hoặc Redis lock khi hold ghế để tránh bán trùng

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] User chọn ghế → hold → confirm thành công
- [ ] 2 user chọn cùng ghế → chỉ 1 người hold được
- [ ] Hold quá 10 phút → tự động hủy, ghế trở lại trống
- [ ] Hủy vé trước giờ chiếu thành công
- [ ] bookingCode unique dạng "VC-20260515-001"
- [ ] Check-in: quét QR (bookingCode) → verify → đổi CHECKED_IN
- [ ] Check-in booking đã CHECKED_IN → lỗi "Vé đã được sử dụng"
- [ ] Check-in booking CANCELLED/EXPIRED → lỗi

## Design Patterns cần áp dụng (MỤC TIÊU HỌC)

### 1. Pessimistic Locking (Concurrency Control)
- **Ở đâu:** `SeatRepository.findByIdIn()` với `@Lock(PESSIMISTIC_WRITE)`
- **Tại sao:** 2 user chọn cùng ghế cùng lúc → chỉ 1 người hold được. Pessimistic lock = khóa row trong DB, user thứ 2 phải chờ
- **Học được gì:** Race condition, DB locking, `@Lock` annotation, khi nào dùng Pessimistic vs Optimistic

### 2. Scheduled Task Pattern
- **Ở đâu:** `BookingCleanupScheduler` với `@Scheduled(fixedRate = 60000)` — chạy mỗi phút
- **Tại sao:** Tự động hủy các booking HOLDING quá 10 phút, trả ghế về trống
- **Học được gì:** Spring `@Scheduled`, `@EnableScheduling`, cron expression

### 3. State Pattern (trạng thái đơn hàng)
- **Ở đâu:** Booking status flow: `HOLDING → CONFIRMED → CHECKED_IN` / `EXPIRED` / `CANCELLED`
- **Tại sao:** Mỗi trạng thái chỉ cho phép chuyển sang một số trạng thái nhất định (VD: CHECKED_IN không thể quay lại CONFIRMED)
- **Học được gì:** State machine, validation chuyển trạng thái, tại sao cần quản lý state

### 4. Transaction Management
- **Ở đâu:** `@Transactional` trên holdSeats() — nếu 1 bước lỗi → rollback tất cả
- **Tại sao:** Hold 3 ghế mà ghế thứ 3 bị trùng → rollback cả 3, không hold nửa vời
- **Học được gì:** ACID, `@Transactional`, rollback behavior

## Tham khảo
- Status booking: `HOLDING`, `CONFIRMED`, `CHECKED_IN`, `CANCELLED`, `EXPIRED`
- Status booking_seat: `HELD`, `BOOKED`, `CANCELLED`
- `docs/erd.md` — sơ đồ bảng bookings, booking_seats

## Ghi chú
- Đây là module phức tạp nhất, cần xử lý concurrency cẩn thận
- bookingCode format: "VC-{yyyyMMdd}-{sequence}" hoặc UUID rút gọn
- Tối đa 8 ghế/lần đặt
- Redis có thể dùng để cache trạng thái ghế realtime

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] **Giải thích từng file** đã tạo: tác dụng, design pattern nào
- [ ] **Viết `/docs/booking-explained.md`** bao gồm:
  1. Luồng đặt vé từng bước (vẽ sơ đồ)
  2. Race condition là gì, demo 2 user chọn cùng ghế → Pessimistic Lock giải quyết thế nào
  3. Pessimistic vs Optimistic Locking: khác gì, khi nào dùng cái nào
  4. `@Transactional`: ACID là gì, rollback khi nào
  5. `@Scheduled`: cách hoạt động, tại sao cần cleanup expired holds
  6. State flow diagram: HOLDING → CONFIRMED → CHECKED_IN / EXPIRED / CANCELLED
  7. Luồng check-in: nhân viên quét QR → verify → phát vé cứng
  8. QR code: FE render từ bookingCode, backend không cần xử lý gì thêm
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
