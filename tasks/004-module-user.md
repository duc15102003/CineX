# Task: Module User — Quản lý thông tin user, phân quyền

## Status: PENDING

## Module
backend

## Mô tả
Quản lý thông tin user: xem profile, cập nhật profile, đổi mật khẩu, phân quyền ADMIN/USER. Admin có thể xem danh sách user.

**Kết quả mong đợi:**
- GET `/api/users/me` → xem profile của mình
- PUT `/api/users/me` → cập nhật profile (fullName, phone, ...)
- PUT `/api/users/me/password` → đổi mật khẩu
- GET `/api/users` → (ADMIN) danh sách user có phân trang
- PUT `/api/users/{id}/role` → (ADMIN) đổi role user

## Việc cần làm

### Entity & Liquibase
- [ ] Thêm Liquibase `003-alter-users-add-profile.xml` — thêm cột: fullName, phone, avatarUrl, enabled, createdAt, updatedAt
- [ ] Cập nhật `User.java` entity thêm các field mới

### DTO
- [ ] `UserProfileResponse.java` — id, username, email, fullName, phone, avatarUrl, role, createdAt
- [ ] `UpdateProfileRequest.java` — fullName, phone (có validation)
- [ ] `ChangePasswordRequest.java` — oldPassword, newPassword, confirmPassword
- [ ] `UpdateRoleRequest.java` — role (ADMIN/USER)

### Repository
- [ ] `UserRepository.java` — thêm method findAll(Pageable)

### Service
- [ ] `UserService.java`:
  - `getProfile(userId)` → trả UserProfileResponse
  - `updateProfile(userId, UpdateProfileRequest)` → cập nhật, trả profile mới
  - `changePassword(userId, ChangePasswordRequest)` → verify old password → hash new password → save
  - `listUsers(Pageable)` → (ADMIN) trả PageResponse<UserProfileResponse>
  - `updateRole(userId, UpdateRoleRequest)` → (ADMIN) đổi role

### Controller
- [ ] `UserController.java` — 5 endpoint ở trên
- [ ] Dùng `@PreAuthorize("hasRole('ADMIN')")` cho các API admin

### Mapper
- [ ] `UserMapper.java` — thêm mapping User → UserProfileResponse

### Security
- [ ] Enum `Role` (USER, ADMIN) nếu chưa có
- [ ] Cập nhật `SecurityConfig` — enable `@PreAuthorize`

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] User xem/sửa được profile của mình
- [ ] Đổi mật khẩu: sai old password → lỗi 400
- [ ] Admin xem được danh sách user
- [ ] User thường gọi API admin → lỗi 403

## Tham khảo
- Module Auth (task 002) — phụ thuộc, cần hoàn thành trước

## Ghi chú
- Lấy userId từ SecurityContext (không truyền qua URL) để tránh user sửa profile người khác
- Cân nhắc thêm endpoint disable/enable user cho admin

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích đã làm gì và tác dụng của từng phần
- [ ] Cập nhật docs nếu cần
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
