# Task: Module Seat — Sơ đồ ghế theo phòng chiếu

## Status: PENDING

## Module
backend

## Mô tả
Quản lý sơ đồ ghế của từng phòng chiếu. Mỗi ghế có vị trí (hàng, cột) và loại (thường/VIP/couple). Admin có thể tạo sơ đồ ghế cho phòng.

**Kết quả mong đợi:**
- GET `/api/rooms/{roomId}/seats` → sơ đồ ghế của phòng (trả về dạng grid: hàng A-J, cột 1-15)
- POST `/api/rooms/{roomId}/seats/generate` → (ADMIN) tự động sinh ghế theo cấu hình (số hàng, số cột, hàng VIP)
- PUT `/api/seats/{id}` → (ADMIN) sửa loại ghế
- DELETE `/api/seats/{id}` → (ADMIN) xóa ghế

## Việc cần làm

### Entity & Liquibase
- [ ] `008-create-seats-table.xml` — bảng `seats` (id, roomId, rowLabel, colNumber, seatNumber, seatType, status)
- [ ] `Seat.java` entity — `@ManyToOne` với Room

### DTO
- [ ] `SeatResponse.java` — id, rowLabel, colNumber, seatNumber, seatType, status
- [ ] `SeatGenerateRequest.java` — totalRows, totalCols, vipRows (VD: ["E","F","G"]), coupleRow (VD: "J")
- [ ] `SeatMapResponse.java` — danh sách seats nhóm theo hàng (để FE render grid)

### Repository
- [ ] `SeatRepository.java` — findByRoomId, findByRoomIdOrderByRowLabelAscColNumberAsc

### Service
- [ ] `SeatService.java`:
  - `getSeatMap(roomId)` → trả sơ đồ ghế nhóm theo hàng
  - `generateSeats(roomId, SeatGenerateRequest)` → xóa ghế cũ → sinh ghế mới theo config
  - `updateSeat(seatId, ...)` → sửa loại ghế

### Controller
- [ ] `SeatController.java`

### Mapper
- [ ] `SeatMapper.java`

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] Generate ghế cho phòng: VD 10 hàng x 12 cột = 120 ghế
- [ ] Mỗi ghế có label dạng "A1", "B5", "VIP-E3"
- [ ] API trả sơ đồ ghế dạng grid cho FE render

## Tham khảo
- seatType: `STANDARD`, `VIP`, `COUPLE`
- seatNumber: format "A1", "A2", ..., "J12"
- rowLabel: "A", "B", ..., "J"

## Ghi chú
- Couple seat chiếm 2 cột (sẽ cần xử lý đặc biệt ở FE)
- Status ghế: `AVAILABLE`, `BROKEN`, `RESERVED` (reserved dùng ở module Booking)
- Cập nhật Room.totalSeats sau khi generate

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích đã làm gì và tác dụng của từng phần
- [ ] Cập nhật docs nếu cần
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
