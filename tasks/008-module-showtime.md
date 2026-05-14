# Task: Module Showtime — Suất chiếu

## Status: PENDING

## Module
backend

## Mô tả
Quản lý suất chiếu: phim nào chiếu ở phòng nào, ngày giờ nào, giá vé bao nhiêu. Admin tạo suất chiếu, user xem danh sách suất chiếu theo phim hoặc theo ngày.

**Kết quả mong đợi:**
- GET `/api/showtimes?movieId=1&date=2026-05-15` → danh sách suất chiếu của phim theo ngày
- GET `/api/showtimes/{id}` → chi tiết suất chiếu (kèm thông tin phim, phòng, ghế trống)
- POST `/api/showtimes` → (ADMIN) tạo suất chiếu mới
- PUT `/api/showtimes/{id}` → (ADMIN) sửa suất chiếu
- DELETE `/api/showtimes/{id}` → (ADMIN) xóa suất chiếu

## Việc cần làm

### Entity & Liquibase
- [ ] `009-create-showtimes-table.xml` — bảng `showtimes` (id, movieId, roomId, startTime, endTime, basePrice, vipPrice, couplePrice, status, createdAt)
- [ ] `Showtime.java` entity — `@ManyToOne` với Movie, `@ManyToOne` với Room

### DTO
- [ ] `ShowtimeRequest.java` — movieId, roomId, startTime, basePrice, vipPrice, couplePrice (validation)
- [ ] `ShowtimeResponse.java` — id, movie (tên + poster), room (tên + loại), startTime, endTime, prices, availableSeats
- [ ] `ShowtimeListResponse.java` — version rút gọn cho danh sách

### Repository
- [ ] `ShowtimeRepository.java` — findByMovieIdAndDate, findByRoomIdAndTimeRange (kiểm tra trùng lịch)

### Service
- [ ] `ShowtimeService.java`:
  - `createShowtime(request)` → kiểm tra phòng trống (không trùng giờ) → tính endTime từ movie.duration → save
  - `getShowtimesByMovieAndDate(movieId, date)` → danh sách suất chiếu
  - `getShowtimeDetail(id)` → chi tiết kèm ghế trống

### Controller
- [ ] `ShowtimeController.java`

### Mapper
- [ ] `ShowtimeMapper.java`

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] Admin tạo suất chiếu, tự tính endTime = startTime + movie.duration + 15 phút dọn phòng
- [ ] Tạo suất trùng giờ phòng → lỗi 409
- [ ] User xem danh sách suất chiếu theo phim + ngày
- [ ] Chi tiết suất chiếu hiển thị số ghế trống

## Tham khảo
- endTime = startTime + duration (phút) + 15 phút (dọn dẹp)
- Status: `SCHEDULED`, `ONGOING`, `FINISHED`, `CANCELLED`

## Ghi chú
- Giá vé theo loại ghế: basePrice (thường), vipPrice, couplePrice
- Không cho tạo suất chiếu trong quá khứ
- Cân nhắc thêm API `GET /api/movies/{id}/showtimes` cho tiện FE

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích đã làm gì và tác dụng của từng phần
- [ ] Cập nhật docs nếu cần
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
