# Task: Module Snack — Đồ ăn kèm đặt vé

## Status: PENDING (⚠️ CẦN CONFIRM VỚI USER TRƯỚC KHI LÀM)

## Module
backend

## Mô tả
Admin tạo menu đồ ăn (bắp rang, nước, combo). User chọn đồ ăn khi đặt vé.

**Kết quả mong đợi:**
- GET `/api/snacks` — menu đồ ăn
- POST `/api/snacks` — (ADMIN) tạo đồ ăn mới
- PUT `/api/snacks/{id}` — (ADMIN) sửa
- DELETE `/api/snacks/{id}` — (ADMIN) xóa mềm
- Tích hợp booking: user chọn combo khi hold ghế

## Bảng: snacks + booking_snacks
- snacks: name, description, price, image_url, category, available (BaseEntity)
- booking_snacks: booking_id, snack_id, quantity, price (giá tại thời điểm đặt)

## Độ khó: ⭐⭐ Trung bình (~3-4h)
- snacks: CRUD đơn giản
- booking_snacks: cần sửa booking flow (thêm bước chọn combo)
