# Task: FE Auth + Layout — Đăng nhập, đăng ký, khung giao diện

## Status: PENDING

## Module
frontend

## Mô tả
Tạo khung giao diện chung (layout, header, footer) và các trang xác thực (đăng nhập, đăng ký). Đây là nền tảng cho tất cả trang FE sau này.

**Kết quả mong đợi:**
- Layout chung: Header (logo, nav, user menu) + Footer
- Trang đăng nhập `/login` — form + validation + gọi API + lưu token
- Trang đăng ký `/register` — form + validation + gọi API + tự login
- ProtectedRoute — chưa login → redirect về /login
- Responsive: hoạt động trên desktop + mobile

## Việc cần làm

### Layout
- [ ] `MainLayout.tsx` — wrapper: Header + `<Outlet />` + Footer
- [ ] `Header.tsx` — logo "CineX", nav (Trang chủ, Phim, ...), user dropdown (Profile, Vé của tôi, Đăng xuất)
- [ ] `Footer.tsx` — copyright, link
- [ ] Responsive: hamburger menu trên mobile

### Auth Pages
- [ ] `LoginPage.tsx` — form username/password, validation (react-hook-form + zod), gọi API `/api/auth/login`, lưu token vào zustand store, redirect về trang chủ
- [ ] `RegisterPage.tsx` — form username/email/password/fullName, validation, gọi API `/api/auth/register`, tự login sau khi đăng ký
- [ ] `authStore.ts` — (đã có) cập nhật nếu cần: lưu user info (username, role)

### Routing
- [ ] `ProtectedRoute.tsx` — kiểm tra token, chưa login → redirect `/login`
- [ ] `AdminRoute.tsx` — kiểm tra role ADMIN, không phải admin → redirect `/`
- [ ] Cập nhật `AppRouter.tsx` — nested routes với MainLayout

### API Hooks
- [ ] `useLogin()` — useMutation gọi API login
- [ ] `useRegister()` — useMutation gọi API register
- [ ] `useLogout()` — xóa token, redirect

### Components chung
- [ ] `Loading.tsx` — spinner loading
- [ ] `EmptyState.tsx` — hiển thị khi không có data
- [ ] `ErrorBoundary.tsx` — bắt lỗi React, hiện trang lỗi thân thiện

## Tiêu chí hoàn thành (Definition of Done)
- [ ] `npm run build` pass
- [ ] Đăng ký → đăng nhập → thấy tên user trên header
- [ ] Đăng xuất → header hiện nút "Đăng nhập"
- [ ] Chưa login → vào trang cần auth → redirect về /login
- [ ] Responsive trên mobile

## Tham khảo
- `src/store/authStore.ts` — đã có sẵn
- `src/api/axios.ts` — đã có JWT interceptor
- Tailwind CSS: utility classes
- react-hook-form + zod: form validation

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích từng file, tác dụng
- [ ] Viết `/docs/fe-auth-explained.md`
- [ ] Đổi Status → DONE, tick [x], move sang `done/`
