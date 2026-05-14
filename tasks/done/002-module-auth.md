# Task: Module Auth — Đăng ký, đăng nhập, refresh token

## Status: DONE

## Module
backend

## Mô tả
Xây dựng module xác thực cho hệ thống. User có thể đăng ký tài khoản, đăng nhập nhận JWT token, và refresh token khi hết hạn.

**Kết quả mong đợi:**
- POST `/api/auth/register` → tạo tài khoản mới
- POST `/api/auth/login` → trả về access token + refresh token
- POST `/api/auth/refresh` → trả về access token mới từ refresh token
- Password được hash bằng BCrypt, KHÔNG lưu plain text
- Swagger UI test được cả 3 API

## Việc cần làm

### Entity & Liquibase
- [ ] Tạo `module/auth/entity/User.java` map với bảng `users` (đã tạo bởi Liquibase 001)
- [ ] Thêm Liquibase `002-add-refresh-token-table.xml` — bảng `refresh_tokens` (id, userId, token, expiryDate, revoked)

### DTO
- [ ] `RegisterRequest.java` — username, email, password (có validation `@NotBlank`, `@Email`, `@Size`)
- [ ] `LoginRequest.java` — username, password
- [ ] `AuthResponse.java` — accessToken, refreshToken, tokenType, expiresIn
- [ ] `RefreshTokenRequest.java` — refreshToken

### Repository
- [ ] `UserRepository.java` — findByUsername, findByEmail, existsByUsername, existsByEmail

### Service
- [ ] `AuthService.java`:
  - `register(RegisterRequest)` → kiểm tra trùng username/email → BCrypt hash password → save user → trả AuthResponse
  - `login(LoginRequest)` → verify credentials → generate JWT → generate refresh token → trả AuthResponse
  - `refreshToken(RefreshTokenRequest)` → validate refresh token → generate JWT mới → trả AuthResponse

### Controller
- [ ] `AuthController.java` — 3 endpoint: register, login, refresh

### Security
- [ ] Cập nhật `CustomUserDetailsService` — load user thật từ DB thay vì throw exception
- [ ] Cập nhật `SecurityConfig` — thêm `/api/auth/**` vào PUBLIC_URLS (đã có sẵn)

### Mapper
- [ ] `UserMapper.java` (MapStruct) — RegisterRequest → User entity

### Test
- [ ] Test thủ công qua Swagger UI: register → login → dùng token gọi API private

## Tiêu chí hoàn thành (Definition of Done)
- [ ] `./gradlew clean build -x test` pass
- [ ] Register tạo user mới, password lưu dạng BCrypt hash
- [ ] Login trả về JWT token hợp lệ
- [ ] Dùng token gọi được API `/api/health` (header Authorization)
- [ ] Trùng username/email → trả lỗi 409 với message rõ ràng
- [ ] Sai password → trả lỗi 401

## Design Patterns cần áp dụng (MỤC TIÊU HỌC)

### 1. DTO Pattern (Data Transfer Object)
- **Ở đâu:** `RegisterRequest`, `LoginRequest`, `AuthResponse`
- **Tại sao:** Tách biệt data client gửi lên và entity trong DB. VD: `RegisterRequest` có field `confirmPassword` nhưng entity `User` thì không
- **Học được gì:** Không bao giờ trả entity thẳng cho client (lộ password hash, lộ field nội bộ)

### 2. Repository Pattern
- **Ở đâu:** `UserRepository extends JpaRepository`
- **Tại sao:** Trừu tượng hóa truy vấn DB. Service gọi `userRepo.findByUsername()` mà không cần biết SQL bên dưới
- **Học được gì:** Spring Data JPA tự sinh SQL từ tên method

### 3. Filter Pattern (Chain of Responsibility)
- **Ở đâu:** `JwtAuthFilter` (đã có sẵn, nhưng cần hiểu sâu khi kết nối với auth thật)
- **Tại sao:** Xử lý xác thực trước khi request đến Controller, không cần viết code auth trong từng Controller
- **Học được gì:** Request pipeline, SecurityFilterChain, OncePerRequestFilter

### 4. Builder Pattern
- **Ở đâu:** `ApiResponse.builder().success(true).data(token).build()`
- **Tại sao:** Tạo object phức tạp dễ đọc hơn constructor nhiều tham số
- **Học được gì:** Lombok `@Builder`, method chaining

## Tham khảo
- `security/JwtUtil.java` — đã có sẵn generateToken, validateToken
- `security/JwtAuthFilter.java` — đã có sẵn filter
- `common/exception/ErrorCode.java` — đã có USER_EXISTED, INVALID_CREDENTIALS
- `docs/erd.md` — sơ đồ bảng users, refresh_tokens

## Ghi chú
- Refresh token nên lưu DB (không phải JWT) để có thể revoke
- Access token TTL: 15 phút (ngắn hơn), Refresh token TTL: 7 ngày
- Cân nhắc cập nhật `JwtUtil` để hỗ trợ TTL khác nhau cho access/refresh
- User entity phải `extends BaseEntity` để tự có createdBy, updatedBy, createdAt, updatedAt

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] **Giải thích từng file** đã tạo: tác dụng, tại sao cần, design pattern nào được dùng
- [ ] **Viết `/docs/auth-explained.md`** bao gồm:
  1. Luồng register/login/refresh vẽ sơ đồ
  2. Access token vs Refresh token: khác gì, tại sao cần cả 2
  3. BCrypt hoạt động thế nào (hash 1 chiều, salt)
  4. JWT cấu trúc 3 phần (header.payload.signature)
  5. Tại sao dùng DTO thay vì trả entity thẳng
  6. SecurityFilterChain hoạt động thế nào
- [ ] Cập nhật `/docs/setup.md` — thêm mục test API auth
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
