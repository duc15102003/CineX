# Task: FE Admin — Trang quản trị cho Admin

## Status: PENDING

## Module
frontend

## Mô tả
Giao diện quản trị cho Admin: quản lý phim, phòng chiếu, suất chiếu, booking, user, quét QR check-in. Layout riêng với sidebar navigation.

**Kết quả mong đợi:**
- Layout admin `/admin` — sidebar (menu) + main content
- Dashboard `/admin/dashboard` — thống kê nhanh (tổng booking hôm nay, doanh thu, phim hot)
- CRUD phim `/admin/movies` — bảng danh sách + form thêm/sửa
- CRUD phòng `/admin/rooms` — bảng danh sách + form thêm/sửa + generate ghế
- CRUD suất chiếu `/admin/showtimes` — bảng danh sách + form thêm/sửa (chọn phim, phòng, giờ, giá)
- Quản lý booking `/admin/bookings` — bảng danh sách, lọc theo trạng thái
- Quản lý user `/admin/users` — bảng danh sách, đổi role, vô hiệu hóa
- Quét QR check-in `/admin/check-in` — input nhập mã hoặc quét QR camera

## Việc cần làm

### Layout Admin
- [ ] `AdminLayout.tsx` — sidebar + header + main content
- [ ] `AdminSidebar.tsx` — menu: Dashboard, Phim, Phòng chiếu, Suất chiếu, Booking, User, Check-in
- [ ] `AdminRoute.tsx` — chỉ cho ADMIN vào, user thường → redirect

### Dashboard
- [ ] `DashboardPage.tsx` — card thống kê: booking hôm nay, doanh thu hôm nay, phim đang chiếu, user mới

### Quản lý phim
- [ ] `AdminMovieListPage.tsx` — bảng danh sách phim, tìm kiếm, nút thêm/sửa/xóa
- [ ] `AdminMovieForm.tsx` — form thêm/sửa phim (title, description, duration, genre, poster URL, ...)
- [ ] `AdminGenreListPage.tsx` — bảng danh sách thể loại, thêm/sửa

### Quản lý phòng chiếu
- [ ] `AdminRoomListPage.tsx` — bảng danh sách phòng, thêm/sửa/xóa
- [ ] `AdminRoomForm.tsx` — form thêm/sửa (tên, loại 2D/3D/IMAX)
- [ ] `AdminSeatGenerator.tsx` — form generate ghế (số hàng, số cột, hàng VIP, hàng couple) + preview sơ đồ ghế

### Quản lý suất chiếu
- [ ] `AdminShowtimeListPage.tsx` — bảng danh sách, lọc theo ngày/phim/phòng
- [ ] `AdminShowtimeForm.tsx` — form tạo suất chiếu (chọn phim, phòng, giờ bắt đầu, giá vé)
- [ ] Hiện cảnh báo nếu trùng giờ phòng

### Quản lý booking
- [ ] `AdminBookingListPage.tsx` — bảng danh sách tất cả booking, lọc theo trạng thái/ngày
- [ ] `AdminBookingDetailPage.tsx` — chi tiết booking, nút hủy vé

### Quản lý user
- [ ] `AdminUserListPage.tsx` — bảng danh sách user, tìm kiếm
- [ ] Nút đổi role (USER ↔ ADMIN), nút vô hiệu hóa tài khoản

### Check-in (quét QR)
- [ ] `CheckInPage.tsx` — input nhập mã booking thủ công + nút "Check-in"
- [ ] (Tùy chọn) Quét QR bằng camera (dùng thư viện `html5-qrcode`)
- [ ] Hiện thông tin vé sau khi quét: phim, suất, ghế, trạng thái
- [ ] Nút "Xác nhận check-in" → gọi API → đổi status CHECKED_IN

### API Hooks
- [ ] `useAdminMovies()` — CRUD phim
- [ ] `useAdminRooms()` — CRUD phòng
- [ ] `useAdminShowtimes()` — CRUD suất chiếu
- [ ] `useAdminBookings()` — danh sách booking
- [ ] `useAdminUsers()` — danh sách user, đổi role
- [ ] `useCheckIn()` — useMutation check-in

## Tiêu chí hoàn thành (Definition of Done)
- [ ] `npm run build` pass
- [ ] Admin login → thấy sidebar admin
- [ ] CRUD phim/phòng/suất chiếu hoạt động
- [ ] Generate sơ đồ ghế cho phòng hoạt động
- [ ] Tạo suất chiếu trùng giờ → hiện cảnh báo
- [ ] Xem danh sách booking + user
- [ ] Check-in bằng mã booking hoạt động
- [ ] User thường vào /admin → redirect về trang chủ

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích từng file, tác dụng
- [ ] Viết `/docs/fe-admin-explained.md`
- [ ] Đổi Status → DONE, tick [x], move sang `done/`
