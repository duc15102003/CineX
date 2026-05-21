# Task: Module Notification — Thông báo hệ thống

## Status: PENDING (⚠️ CẦN CONFIRM VỚI USER TRƯỚC KHI LÀM)

## Module
backend

## Mô tả
Gửi thông báo cho user khi có sự kiện: booking confirmed, suất sắp chiếu, khuyến mãi.

**Kết quả mong đợi:**
- GET `/api/notifications/me` — danh sách thông báo của user (phân trang)
- PUT `/api/notifications/{id}/read` — đánh dấu đã đọc
- PUT `/api/notifications/read-all` — đánh dấu tất cả đã đọc
- GET `/api/notifications/me/unread-count` — số thông báo chưa đọc (badge)

## Bảng: notifications
- user_id, title, content, type (BOOKING/PROMOTION/SYSTEM), read, created_at

## Độ khó: ⭐⭐ Trung bình (~2-3h)
- CRUD đơn giản
- Tích hợp Spring Events: booking confirm → tạo notification
