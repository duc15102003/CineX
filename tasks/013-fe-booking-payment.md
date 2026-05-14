# Task: FE Đặt vé + Thanh toán — Chọn ghế, thanh toán, QR code, lịch sử vé

## Status: PENDING

## Module
frontend

## Mô tả
Luồng đặt vé hoàn chỉnh: chọn ghế → xem giá → thanh toán → nhận QR code. User có thể xem lịch sử vé và hủy vé.

**Kết quả mong đợi:**
- Chọn ghế `/booking/:showtimeId` — sơ đồ ghế, chọn ghế, hiện giá, nút "Thanh toán"
- Thanh toán `/payment/:bookingId` — thông tin booking, chọn phương thức, nút thanh toán
- Kết quả `/payment/result` — thành công → hiện QR code
- Lịch sử vé `/my-tickets` — danh sách vé, trạng thái
- Chi tiết vé `/my-tickets/:id` — thông tin vé + QR code
- Profile `/profile` — xem/sửa thông tin, đổi mật khẩu

## Việc cần làm

### Chọn ghế
- [ ] `SeatSelectionPage.tsx` — thông tin suất chiếu ở trên, sơ đồ ghế ở giữa, tóm tắt giá ở dưới
- [ ] Component `SeatMap.tsx` — grid ghế:
  - Xanh = trống (AVAILABLE)
  - Đỏ = đã đặt (BOOKED)
  - Cam = đang giữ bởi người khác (HELD)
  - Vàng = VIP trống
  - Tím = Couple trống
  - Xanh lá = ghế đang chọn (user click)
  - Xám = ghế hỏng (BROKEN)
- [ ] Component `BookingSummary.tsx` — danh sách ghế đã chọn, giá từng ghế, tổng tiền, nút "Thanh toán"
- [ ] Countdown timer: hiện đếm ngược 10 phút hold ghế

### Thanh toán
- [ ] `PaymentPage.tsx` — thông tin booking, chọn phương thức (VNPay/Momo/Tại quầy), nút "Thanh toán"
- [ ] `PaymentResultPage.tsx` — thành công: hiện QR code + thông tin vé, thất bại: hiện lỗi + nút thử lại

### QR Code + Vé
- [ ] Component `TicketQRCode.tsx` — render QR từ bookingCode (dùng `react-qr-code`)
- [ ] `MyTicketsPage.tsx` — danh sách vé, filter theo trạng thái (tất cả/sắp chiếu/đã xem/đã hủy)
- [ ] `TicketDetailPage.tsx` — thông tin vé đầy đủ + QR code lớn + nút "Hủy vé"

### Profile
- [ ] `ProfilePage.tsx` — xem/sửa fullName, phone, email
- [ ] `ChangePasswordForm.tsx` — form đổi mật khẩu (old + new + confirm)

### API Hooks
- [ ] `useSeatMap(showtimeId)` — useQuery lấy sơ đồ ghế + trạng thái
- [ ] `useHoldSeats()` — useMutation hold ghế
- [ ] `useConfirmBooking()` — useMutation xác nhận đặt vé
- [ ] `useCancelBooking()` — useMutation hủy vé
- [ ] `useCreatePayment()` — useMutation tạo thanh toán
- [ ] `useMyBookings()` — useQuery lấy danh sách vé
- [ ] `useBooking(id)` — useQuery lấy chi tiết vé
- [ ] `useProfile()` — useQuery lấy profile
- [ ] `useUpdateProfile()` — useMutation sửa profile
- [ ] `useChangePassword()` — useMutation đổi mật khẩu

## Tiêu chí hoàn thành (Definition of Done)
- [ ] `npm run build` pass
- [ ] Luồng end-to-end: chọn ghế → hold → thanh toán → QR code
- [ ] Sơ đồ ghế hiện đúng màu theo trạng thái
- [ ] Countdown 10 phút hold ghế hoạt động
- [ ] QR code hiện trên trang kết quả + trang chi tiết vé
- [ ] Hủy vé hoạt động
- [ ] Profile: sửa thông tin + đổi mật khẩu

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích từng file, tác dụng
- [ ] Viết `/docs/fe-booking-explained.md`
- [ ] Đổi Status → DONE, tick [x], move sang `done/`
