# Module Booking — Giải thích chi tiết

## 1. Tổng quan
Module phức tạp nhất — xử lý **luồng đặt vé hoàn chỉnh**:
1. User chọn suất chiếu → chọn ghế → **hold 10 phút**
2. Thanh toán → **confirm** booking
3. Đến rạp → staff quét QR → **check-in**
4. Hết 10 phút không thanh toán → **tự động hủy** (scheduled task)

**Bài toán khó:** 2 user chọn cùng ghế cùng lúc → chỉ 1 người hold được (concurrency).

## 2. Luồng đặt vé — State Machine

```
                    ┌─────────────┐
                    │   HOLDING   │ ← Hold ghế (10 phút)
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
      ┌──────────┐  ┌──────────┐  ┌──────────┐
      │CONFIRMED │  │ EXPIRED  │  │CANCELLED │
      │(đã TT)   │  │(hết hạn) │  │(user hủy)│
      └────┬─────┘  └──────────┘  └──────────┘
           ▼
      ┌──────────┐
      │CHECKED_IN│ ← Staff quét QR tại rạp
      └──────────┘
```

Quy tắc chuyển trạng thái:
- HOLDING → CONFIRMED (thanh toán xong)
- HOLDING → EXPIRED (hết 10 phút, scheduler tự đổi)
- HOLDING → CANCELLED (user hủy)
- CONFIRMED → CANCELLED (user hủy trước giờ chiếu)
- CONFIRMED → CHECKED_IN (staff quét QR)

## 3. Design Patterns

### 3.1 Scheduled Task (@Scheduled)

```java
@Scheduled(fixedRate = 60000)  // Chạy mỗi 60 giây
@Transactional
public void cleanupExpiredHolds() {
    int holdMinutes = systemConfigService.getInt("booking.hold_minutes", 10);
    LocalDateTime expireBefore = LocalDateTime.now().minusMinutes(holdMinutes);

    List<Booking> expired = bookingRepository
        .findByStatusAndCreatedAtBefore(BookingStatus.HOLDING, expireBefore);

    for (Booking booking : expired) {
        booking.setStatus(BookingStatus.EXPIRED);
        booking.getBookingSeats().forEach(bs -> bs.setStatus(BookingSeatStatus.CANCELLED));
    }
}
```

**Ví dụ:** Booking tạo lúc 14:00, holdMinutes=10 → hết hạn lúc 14:10.
Scheduler chạy lúc 14:11 → tìm HOLDING + createdAt < 14:01 → đổi EXPIRED.

### 3.2 Concurrency — Kiểm tra ghế trống

```java
// Kiểm tra: có ai HELD/BOOKED ghế này cho suất chiếu này chưa?
List<BookingSeat> occupied = bookingSeatRepository
    .findHeldOrBookedSeats(showtimeId, seatIds);

if (!occupied.isEmpty()) {
    throw new BusinessException(ErrorCode.SEAT_ALREADY_BOOKED,
        "Seats already taken: A1, A2");
}
```

### 3.3 IdTrackerService — Sinh mã booking unique

```java
String bookingCode = idTrackerService.nextCodeWithDate("BOOKING");
// → "CX-20260520-001", "CX-20260520-002", ...
// CX = prefix, 20260520 = ngày, 001 = số thứ tự tăng dần
```

### 3.4 QR Code (ZXing)

```java
// Sinh QR từ bookingCode
String qrBase64 = qrCodeService.generateQrCodeBase64("CX-20260520-001", 300);
// → Base64 string, FE hiển thị: <img src="data:image/png;base64,..." />
```

## 4. Sơ đồ luồng

### Hold ghế
```
POST /api/bookings/hold
Body: { "showtimeId": 1, "seatIds": [10, 11, 12] }
│
▼
BookingService.holdSeats(userId, request)
│
├── 1. Validate showtime chưa bắt đầu
├── 2. Validate max seats (8, từ SystemConfig)
├── 3. Check ghế trống: findHeldOrBookedSeats(showtimeId, seatIds)
│      → Nếu có → throw SEAT_ALREADY_BOOKED
├── 4. Tính tổng tiền: STANDARD=75k, VIP=100k, COUPLE=150k
├── 5. Sinh bookingCode: "CX-20260520-001"
├── 6. Tạo Booking(HOLDING) + 3 BookingSeat(HELD)
└── 7. Trả HoldSeatsResponse (bookingId, holdExpiry, totalAmount, seats)
```

### Confirm
```
POST /api/bookings/confirm { "bookingId": 1 }
│
├── Check: booking thuộc user? ✅
├── Check: status == HOLDING? ✅
├── Check: chưa hết hạn hold? ✅
├── Đổi: Booking → CONFIRMED, BookingSeats → BOOKED
└── Set confirmedAt = now
```

## 5. Request/Response mẫu

### Hold ghế
```bash
curl -X POST http://localhost:8088/api/bookings/hold \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"showtimeId": 1, "seatIds": [10, 11, 12]}'
```

```json
{
  "success": true,
  "message": "Seats held",
  "data": {
    "bookingId": 1,
    "bookingCode": "CX-20260520-001",
    "holdExpiry": "2026-05-20T14:10:00",
    "totalAmount": 275000,
    "seats": [
      {"seatId": 10, "seatNumber": "E1", "seatType": "VIP", "price": 100000},
      {"seatId": 11, "seatNumber": "E2", "seatType": "VIP", "price": 100000},
      {"seatId": 12, "seatNumber": "A1", "seatType": "STANDARD", "price": 75000}
    ]
  }
}
```

### Ghế đã bị giữ (409)
```json
{"success": false, "message": "Seats already taken: E1, E2"}
```

### Check-in (Admin)
```bash
curl -X POST "http://localhost:8088/api/bookings/check-in?code=CX-20260520-001" \
  -H "Authorization: Bearer <admin_token>"
```

### Vé đã sử dụng
```json
{"success": false, "message": "Ticket already used"}
```

## 6. Câu hỏi tự kiểm tra

1. **Hold ghế 10 phút, nếu không thanh toán thì sao?**
   → BookingCleanupScheduler chạy mỗi phút, tìm HOLDING + createdAt < 10 phút trước → đổi EXPIRED + BookingSeats → CANCELLED → ghế trả lại.

2. **2 user hold cùng ghế cùng lúc thì sao?**
   → `findHeldOrBookedSeats()` check trước khi hold. Trong cùng transaction, user đến sau sẽ thấy ghế đã HELD → throw SEAT_ALREADY_BOOKED.

3. **Tại sao lưu price trong BookingSeat thay vì lấy từ Showtime?**
   → Vì giá có thể thay đổi sau khi đặt. Lưu tại thời điểm đặt = "snapshot price" → không bị ảnh hưởng khi admin đổi giá.

4. **@EnableScheduling để ở đâu?**
   → Trên class main `CineXApplication.java`. Thiếu annotation này → @Scheduled không chạy.
