# Task: Module Room — CRUD phòng chiếu

## Status: PENDING

## Module
backend

## Mô tả
Quản lý phòng chiếu trong rạp. Admin có thể tạo/sửa/xóa phòng chiếu. Mỗi phòng có tên, loại (2D/3D/IMAX), tổng số ghế.

**Kết quả mong đợi:**
- GET `/api/rooms` → danh sách phòng chiếu
- GET `/api/rooms/{id}` → chi tiết phòng (kèm sơ đồ ghế)
- POST `/api/rooms` → (ADMIN) tạo phòng mới
- PUT `/api/rooms/{id}` → (ADMIN) sửa phòng
- DELETE `/api/rooms/{id}` → (ADMIN) xóa phòng

## Việc cần làm

### Entity & Liquibase
- [ ] `007-create-rooms-table.xml` — bảng `rooms` (id, name, type, totalSeats, status, createdAt, updatedAt)
- [ ] `Room.java` entity

### DTO
- [ ] `RoomRequest.java` — name, type, totalSeats (validation)
- [ ] `RoomResponse.java` — id, name, type, totalSeats, status

### Repository
- [ ] `RoomRepository.java` — findAll, findByName, existsByName

### Service
- [ ] `RoomService.java` — CRUD

### Controller
- [ ] `RoomController.java` — 5 endpoint (admin only cho CUD)

### Mapper
- [ ] `RoomMapper.java`

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] Admin CRUD phòng chiếu thành công
- [ ] Trùng tên phòng → lỗi 409
- [ ] Xóa phòng đang có suất chiếu → lỗi (hoặc soft delete)

## Tham khảo
- Loại phòng: `2D`, `3D`, `IMAX`, `4DX`
- Status: `ACTIVE`, `MAINTENANCE`, `INACTIVE`

## Ghi chú
- Phòng chiếu là khung sườn, ghế ngồi sẽ làm ở task 006 (Seat)
- totalSeats sẽ tự tính từ số ghế thực tế sau khi có module Seat

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích đã làm gì và tác dụng của từng phần
- [ ] Cập nhật docs nếu cần
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
