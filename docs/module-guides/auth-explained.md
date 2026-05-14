# Module Auth — Giải thích chi tiết

## 1. Luồng hoạt động

### Đăng ký (Register)

```
Client                                    Backend
  │                                          │
  ├─ POST /api/auth/register ───────────────▶│
  │  {                                       │
  │    "username": "vanan",                  ├─ 1. Kiểm tra username đã tồn tại? → existsByUsername()
  │    "email": "an@gmail.com",              ├─ 2. Kiểm tra email đã tồn tại? → existsByEmail()
  │    "password": "123456",                 ├─ 3. Hash password: BCrypt.encode("123456")
  │    "fullName": "Vũ Tường An"             │      → "$2a$10$N9qo8uLOi..."  (60 ký tự)
  │  }                                       ├─ 4. Lưu User vào DB
  │                                          ├─ 5. Tạo access token (JWT, 15 phút)
  │                                          ├─ 6. Tạo refresh token (UUID, 7 ngày, lưu DB)
  │◀─── {                               ────┤
  │       "accessToken": "eyJhbG...",        │
  │       "refreshToken": "550e8400-...",    │
  │       "tokenType": "Bearer",             │
  │       "expiresIn": 900                   │
  │     }                                    │
```

### Đăng nhập (Login)

```
Client                                    Backend
  │                                          │
  ├─ POST /api/auth/login ──────────────────▶│
  │  { "username": "vanan",                  ├─ 1. Tìm user theo username → findByUsername()
  │    "password": "123456" }                ├─ 2. So sánh: BCrypt.matches("123456", hash trong DB)
  │                                          ├─ 3. Nếu đúng → tạo access token + refresh token
  │                                          ├─ 4. Revoke tất cả refresh token cũ (logout thiết bị khác)
  │◀─── { accessToken, refreshToken } ──────┤
```

### Refresh Token (lấy access token mới)

```
Client                                    Backend
  │                                          │
  │  (Access token hết hạn sau 15 phút)      │
  │                                          │
  ├─ POST /api/auth/refresh ────────────────▶│
  │  { "refreshToken": "550e8400-..." }      ├─ 1. Tìm refresh token trong DB
  │                                          ├─ 2. Kiểm tra: chưa revoke + chưa hết hạn
  │                                          ├─ 3. Tạo access token MỚI
  │◀─── { accessToken (mới), refreshToken }─┤
```

## 2. Access Token vs Refresh Token — Tại sao cần cả 2?

| | Access Token | Refresh Token |
|---|---|---|
| **Là gì** | JWT chứa thông tin user | Chuỗi UUID ngẫu nhiên |
| **Lưu ở đâu** | Client (localStorage) | Client (localStorage) + Server (DB) |
| **Thời hạn** | Ngắn: 15 phút | Dài: 7 ngày |
| **Dùng để** | Gọi API (gắn vào header) | Lấy access token mới khi hết hạn |
| **Có thể thu hồi?** | Không (phải chờ hết hạn) | Có (set revoked=true trong DB) |

**Tại sao không dùng 1 token thôi?**

- Nếu access token thời hạn dài (7 ngày) → bị lộ thì hacker dùng được 7 ngày
- Nếu access token thời hạn ngắn (15 phút) → user phải login lại mỗi 15 phút, rất phiền
- **Giải pháp:** Access token ngắn hạn (15 phút) + Refresh token dài hạn (7 ngày)
  - Bình thường: access token hết → dùng refresh token lấy cái mới → user không biết
  - Bị hack: access token lộ → chỉ dùng được 15 phút. Refresh token lộ → admin revoke trong DB

## 3. BCrypt — Hash password 1 chiều

```
Input:  "123456"
Output: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
         ──── ── ──────────────────────── ──────────────────────────────
          │    │         │                              │
          │    │         Salt (22 ký tự)                Hash (31 ký tự)
          │    Cost factor (10 = 2^10 = 1024 rounds)
          Algorithm version (2a)
```

**Đặc điểm:**
- **1 chiều:** Không thể giải mã ngược từ hash → password. Chỉ có thể verify
- **Salt:** Mỗi lần hash cùng 1 password → ra hash KHÁC NHAU (vì salt ngẫu nhiên)
- **Verify:** `BCrypt.matches("123456", hash)` → tách salt từ hash → hash lại input với cùng salt → so sánh

**Tại sao không dùng MD5/SHA?**
- MD5/SHA quá nhanh → hacker brute force hàng tỷ lần/giây
- BCrypt chậm có chủ đích (cost factor) → mỗi lần hash mất ~100ms → brute force rất lâu

## 4. JWT — Cấu trúc 3 phần

```
eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInN1YiI6InZhbmFuIiwiaWF0IjoxNzE1...signature
─────────────────────  ───────────────────────────────────────────────────────────  ─────────
      Header                                Payload                                Signature
```

### Header (thuật toán ký)
```json
{
  "alg": "HS256"        // Thuật toán: HMAC-SHA256
}
```

### Payload (dữ liệu)
```json
{
  "sub": "vanan",       // Subject = username
  "role": "USER",       // Extra claim: vai trò
  "iat": 1715320000,    // Issued at: thời điểm tạo (Unix timestamp)
  "exp": 1715320900     // Expiration: thời điểm hết hạn
}
```

### Signature (chữ ký)
```
HMACSHA256(
  base64(header) + "." + base64(payload),
  secret_key
)
```

**Tại sao an toàn?**
- Payload ai cũng đọc được (chỉ là Base64, không mã hóa)
- Nhưng không thể SỬA payload vì không có secret key để tạo lại signature
- Server verify: tạo lại signature từ header+payload+secret → so sánh với signature trong token

## 5. Tại sao dùng DTO thay vì trả Entity thẳng?

### Nếu trả Entity thẳng:
```json
// GET /api/users/me → trả User entity
{
  "id": 1,
  "username": "vanan",
  "email": "an@gmail.com",
  "password": "$2a$10$N9qo8uLOickgx2ZMRZoMye...",   // ← LỘ PASSWORD HASH!
  "role": "USER",
  "enabled": true,
  "createdBy": "system",
  "updatedBy": null,
  "createdAt": "2026-05-10T10:00:00",
  "updatedAt": "2026-05-10T10:00:00"
}
```

### Dùng DTO:
```json
// GET /api/users/me → trả UserProfileResponse DTO
{
  "id": 1,
  "username": "vanan",
  "email": "an@gmail.com",
  "fullName": "Vũ Tường An",
  "role": "USER"
  // Không có password, không có field nội bộ
}
```

**Lợi ích DTO:**
1. **Bảo mật:** Không lộ password hash, field nội bộ
2. **Linh hoạt:** Cùng 1 entity, có thể trả response khác nhau (list trả ít field, detail trả nhiều field)
3. **Validation:** `RegisterRequest` có `@NotBlank`, `@Email` → validate input rõ ràng
4. **Tách biệt:** Thay đổi DB (thêm cột) không ảnh hưởng API response

## 6. SecurityFilterChain — Luồng xử lý request

```
Client gửi request
    │
    ▼
┌─────────────────────────────┐
│  CorsFilter                │  ← Kiểm tra origin (localhost:5173 OK)
└─────────────┬───────────────┘
              ▼
┌─────────────────────────────┐
│  JwtAuthFilter              │  ← Lấy token từ header → validate → set SecurityContext
│  (OncePerRequestFilter)     │
│                             │
│  if (có token hợp lệ) {    │
│    → set Authentication     │
│  } else {                   │
│    → bỏ qua, đi tiếp       │
│  }                          │
└─────────────┬───────────────┘
              ▼
┌─────────────────────────────┐
│  AuthorizationFilter        │  ← Kiểm tra URL có cần auth không
│  (Spring Security built-in) │
│                             │
│  /api/auth/** → permitAll   │  ← Cho qua không cần token
│  /api/health  → permitAll   │  ← Cho qua
│  /swagger-ui  → permitAll   │  ← Cho qua
│  /* (còn lại) → authenticated│ ← Phải có Authentication trong SecurityContext
│                             │
│  if (cần auth mà chưa có)  │
│    → trả 401 Unauthorized   │
└─────────────┬───────────────┘
              ▼
┌─────────────────────────────┐
│  Controller                 │  ← Xử lý request
│  (AuthController,           │
│   HealthController, ...)    │
└─────────────────────────────┘
```

## 7. Danh sách files đã tạo/sửa

| File | Tác dụng | Design Pattern |
|---|---|---|
| `entity/Role.java` | Enum vai trò: USER, ADMIN | Enum Pattern |
| `entity/User.java` | Entity map bảng `users`, extends BaseEntity | Repository Pattern |
| `entity/RefreshToken.java` | Entity map bảng `refresh_tokens` | Repository Pattern |
| `dto/RegisterRequest.java` | Data đăng ký + validation | DTO Pattern |
| `dto/LoginRequest.java` | Data đăng nhập + validation | DTO Pattern |
| `dto/AuthResponse.java` | Response chứa tokens | DTO Pattern, Builder |
| `dto/RefreshTokenRequest.java` | Data refresh token | DTO Pattern |
| `repository/UserRepository.java` | Truy vấn bảng users | Repository Pattern |
| `repository/RefreshTokenRepository.java` | Truy vấn bảng refresh_tokens | Repository Pattern |
| `service/AuthService.java` | Logic register/login/refresh | Service Layer |
| `service/RefreshTokenService.java` | Tạo/validate/revoke refresh token | Service Layer |
| `controller/AuthController.java` | 3 API endpoint | Controller Layer |
| `security/JwtUtil.java` (sửa) | Thêm method generateToken với TTL tùy chỉnh | — |
| `security/CustomUserDetailsService.java` (sửa) | Load user thật từ DB thay vì throw exception | — |
| Liquibase `002-*.xml` | Thêm cột profile + audit vào bảng users | — |
| Liquibase `003-*.xml` | Tạo bảng refresh_tokens | — |
| `application.yml` (sửa) | Access token: 15 phút, Refresh token: 7 ngày | — |
