# Task: Module Booking — Đặt vé xem phim

## Status: PENDING

## Module
backend

## Mô tả
Chức năng cốt lõi: user chọn suất chiếu -> chọn ghế -> giữ ghế tạm (10 phút) -> xác nhận đặt vé -> nhận QR code -> đến rạp check-in. Hệ thống phải đảm bảo không bán trùng ghế (concurrency).

**Kết quả mong đợi:**
- GET `/api/showtimes/{id}/seats` — sơ đồ ghế với trạng thái (trống/đã đặt/đang giữ)
- POST `/api/bookings/hold` — giữ ghế tạm (lock 10 phút)
- POST `/api/bookings/confirm` — xác nhận đặt vé (sau khi thanh toán)
- GET `/api/bookings/me` — danh sách vé của user (filter: status, includeDeleted)
- GET `/api/bookings/{id}` — chi tiết vé (không filter DELETED, FE tự xử lý)
- PUT `/api/bookings/{id}/cancel` — hủy vé (nếu chưa chiếu)
- POST `/api/bookings/check-in?code=xxx` — (STAFF/ADMIN) quét QR -> verify -> đổi CHECKED_IN

## Đã có sẵn (KHÔNG cần tạo lại)
- Liquibase `011-create-bookings-table.xml` — bảng `bookings` + `booking_seats`
- `system_config`: booking.hold_minutes=10, booking.max_seats=8
- Pattern: Filter DTO + Specification — áp dụng thống nhất

## Việc cần làm

### Entity
- [ ] `Booking.java` entity — `@ManyToOne` User, `@ManyToOne` Showtime, `@OneToMany` BookingSeat
- [ ] `BookingSeat.java` entity — `@ManyToOne` Booking, `@ManyToOne` Seat
- [ ] `BookingStatus.java` enum — HOLDING, CONFIRMED, CHECKED_IN, CANCELLED, EXPIRED
- [ ] `BookingSeatStatus.java` enum — HELD, BOOKED, CANCELLED

### DTO
- [ ] `BookingFilter.java` — status, includeDeleted (cho danh sách vé user/admin)
- [ ] `HoldSeatsRequest.java` — showtimeId, seatIds[] (validation: không rỗng, tối đa 8 ghế)
- [ ] `HoldSeatsResponse.java` — bookingId, holdExpiry, totalAmount, seats[]
- [ ] `ConfirmBookingRequest.java` — bookingId, paymentMethod
- [ ] `BookingResponse.java` — id, storageState, bookingCode, movie, showtime, room, seats[], totalAmount, status, createdAt, updatedAt
- [ ] `BookingListResponse.java` — version rút gọn, có storageState, createdAt, updatedAt

### Repository
- [ ] `BookingRepository.java` — extends JpaSpecificationExecutor, findByBookingCode
- [ ] `BookingSeatRepository.java` — findByShowtimeIdAndSeatIdIn

### Specification
- [ ] `BookingSpecification.java` — fromFilter(BookingFilter): status, userId, notDeleted

### Service
- [ ] `BookingService.java`:
  - `holdSeats(userId, request)` — kiểm tra ghế trống -> tạo booking HOLDING -> set hết hạn
  - `confirmBooking(userId, request)` — kiểm tra booking còn hold -> đổi CONFIRMED
  - `cancelBooking(userId, bookingId)` — kiểm tra quyền + chưa chiếu -> đổi CANCELLED
  - `checkIn(bookingCode)` — (STAFF/ADMIN) verify -> đổi CHECKED_IN
  - `getMyBookings(userId, BookingFilter, Pageable)` — Filter DTO + Specification
  - `getBookingDetail(userId, bookingId)` — không filter DELETED
- [ ] `BookingCleanupScheduler.java` — `@Scheduled(fixedRate = 60000)` hủy booking HOLDING quá hạn
- [ ] `SeatAvailabilityService.java`:
  - `getAvailableSeats(showtimeId)` — trả sơ đồ ghế với trạng thái

### Controller
- [ ] `BookingController.java` — 7 endpoints

### QR Code (ZXing)
- [ ] `QrCodeService.java` — generateQrCode, generateQrCodeBase64

### Email xác nhận (Spring Mail)
- [ ] `EmailService.java` — sendBookingConfirmation (HTML + QR inline)
- [ ] Gọi @Async sau khi confirmBooking thành công

### Concurrency
- [ ] Dùng `@Lock(PESSIMISTIC_WRITE)` khi hold ghế để tránh bán trùng

## Design Patterns cần áp dụng

### 1. Pessimistic Locking (Concurrency Control)
### 2. Scheduled Task Pattern (@Scheduled)
### 3. State Pattern (booking status flow)
### 4. Transaction Management (@Transactional + rollback)

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] User chọn ghế -> hold -> confirm thành công
- [ ] 2 user chọn cùng ghế -> chỉ 1 người hold được
- [ ] Hold quá 10 phút -> tự động hủy, ghế trở lại trống
- [ ] Hủy vé trước giờ chiếu thành công
- [ ] bookingCode unique dạng "CX-20260515-001" (dùng IdTrackerService)
- [ ] Check-in: quét QR -> verify -> đổi CHECKED_IN
- [ ] Response DTO có storageState, createdAt, updatedAt

## Ghi chú
- Đây là module phức tạp nhất, cần xử lý concurrency cẩn thận
- Phụ thuộc: task 007 (Seat), task 008 (Showtime)

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Viết `/docs/module-guides/booking-explained.md`
- [ ] Đổi Status -> DONE, tick [x], move sang `done/`
