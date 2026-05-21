# Task: Booking Status History — Lịch sử trạng thái đơn đặt

## Status: PENDING (⚠️ CẦN CONFIRM VỚI USER TRƯỚC KHI LÀM)

## Module
backend

## Mô tả
Ghi lại mỗi lần booking đổi trạng thái: ai đổi, lúc nào, từ trạng thái gì sang trạng thái gì.

**Kết quả mong đợi:**
- GET `/api/bookings/{id}/history` — lịch sử đổi trạng thái
- Tự động insert khi booking đổi status (không cần API tạo thủ công)

## Bảng: booking_status_history
- booking_id, from_status, to_status, changed_by, changed_at, note

## Độ khó: ⭐ Dễ (~1-2h)
- Dùng pattern giống AuditLog đã có
- Sửa BookingService: mỗi lần setStatus → insert history record
