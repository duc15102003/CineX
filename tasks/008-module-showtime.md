# Task: Module Showtime — Suất chiếu

## Status: PENDING

## Module
backend

## Mô tả
Quản lý suất chiếu: phim nào chiếu ở phòng nào, ngày giờ nào, giá vé bao nhiêu. Admin tạo suất chiếu, user xem danh sách suất chiếu theo phim hoặc theo ngày.

**Kết quả mong đợi:**
- GET `/api/showtimes` — danh sách suất chiếu (filter: movieId, roomId, date, status, includeDeleted)
- GET `/api/showtimes/{id}` — chi tiết suất chiếu (kèm thông tin phim, phòng, ghế trống)
- POST `/api/showtimes` — (ADMIN) tạo suất chiếu mới
- PUT `/api/showtimes/{id}` — (ADMIN) sửa suất chiếu
- DELETE `/api/showtimes/{id}` — (ADMIN) xóa mềm suất chiếu
- POST `/api/showtimes/{id}/restore` — (ADMIN) khôi phục suất chiếu đã xóa

## Đã có sẵn (KHÔNG cần tạo lại)
- Liquibase `010-create-showtimes-table.xml` — bảng `showtimes`
- FileUploadService, CloudinaryConfig — đã có
- Pattern: Filter DTO + Specification — áp dụng thống nhất

## Việc cần làm

### Entity
- [ ] `Showtime.java` entity — `@ManyToOne` Movie, `@ManyToOne` Room
- [ ] `ShowtimeStatus.java` enum — SCHEDULED, ONGOING, FINISHED, CANCELLED

### DTO
- [ ] `ShowtimeFilter.java` — movieId, roomId, date, status, includeDeleted
- [ ] `ShowtimeRequest.java` — movieId, roomId, startTime, basePrice, vipPrice, couplePrice (validation)
- [ ] `ShowtimeResponse.java` — id, storageState, movie info, room info, startTime, endTime, prices, availableSeats
- [ ] `ShowtimeListResponse.java` — version rút gọn, có storageState, createdAt, updatedAt

### Repository
- [ ] `ShowtimeRepository.java` — extends JpaSpecificationExecutor, findByRoomIdAndTimeRange (kiểm tra trùng lịch)

### Specification
- [ ] `ShowtimeSpecification.java` — fromFilter(ShowtimeFilter): movieId, roomId, date range, status, notDeleted

### Service
- [ ] `ShowtimeService.java`:
  - `listShowtimes(ShowtimeFilter, Pageable)` — Filter DTO + Specification pattern
  - `getShowtime(id)` — chi tiết, không filter DELETED
  - `createShowtime(request)` — kiểm tra phòng trống + tính endTime + buffer
  - `updateShowtime(id, request)` — sửa
  - `deleteShowtime(id)` — soft delete
  - `restoreShowtime(id)` — khôi phục
  - Buffer minutes đọc từ `SystemConfigService` (key: showtime.buffer_minutes, default: 15)

### Controller
- [ ] `ShowtimeController.java` — 6 endpoints

### Mapper
- [ ] `ShowtimeMapper.java`

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] Admin tạo suất chiếu, tự tính endTime = startTime + movie.duration + buffer
- [ ] Tạo suất trùng giờ phòng -> lỗi 409
- [ ] User xem danh sách suất chiếu (filter movieId, date, status)
- [ ] Chi tiết suất chiếu hiển thị số ghế trống
- [ ] Soft delete + restore hoạt động
- [ ] Response DTO có storageState

## Ghi chú
- Không cho tạo suất chiếu trong quá khứ
- Phụ thuộc: task 005 (Movie), task 006 (Room)

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Viết `/docs/module-guides/showtime-explained.md`
- [ ] Đổi Status -> DONE, tick [x], move sang `done/`
