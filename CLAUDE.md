# CineX — Quy tắc làm việc

## Về dự án
- **Đồ án tốt nghiệp:** Hệ thống đặt vé xem phim online
- **Mục tiêu chính:** Học hỏi design patterns, kiến trúc phần mềm, công nghệ thực tế
- **Đối tượng:** Sinh viên đang học, cần hiểu SÂU từng khái niệm, không chỉ copy code
- **Ngôn ngữ giao tiếp:** Tiếng Việt có dấu

---

## QUY TRÌNH LÀM VIỆC BẮT BUỘC

### Bước 1: Trước khi code
- Đọc file task trong `/tasks/` → chỉ làm task có Status: PENDING hoặc IN_PROGRESS
- Đọc `/docs/erd.md` để hiểu quan hệ dữ liệu
- Đổi Status task sang IN_PROGRESS khi bắt đầu
- Đọc lại các file `/docs/*-explained.md` liên quan để không làm trùng hoặc sai kiến trúc

### Bước 2: Khi code — TỰ ĐỘNG HÓA KIỂM TRA
- **Tự build:** Chạy `cd /Users/vutuongan/cinex/backend && ./gradlew clean build -x test` sau khi viết code
- **Tự kiểm tra log lỗi:** Nếu build fail → đọc log → sửa → build lại cho đến khi pass
- **Tự chạy server test:** Khi cần verify runtime → `./gradlew bootRun` → đọc log startup → kiểm tra lỗi
- **Tự test API:** Khi tạo endpoint mới → dùng curl hoặc chỉ dẫn user test qua Swagger
- **Không bỏ qua lỗi:** Mỗi lỗi compile/runtime phải được xử lý, không skip
- **Không hardcode:** Giá trị cấu hình đọc từ `system_config` hoặc `application.yml`, không viết thẳng trong code

### Bước 3: Sau khi code — VIẾT TÀI LIỆU (BẮT BUỘC, KHÔNG ĐƯỢC BỎ QUA)

Sau mỗi task hoàn thành, **PHẢI** viết file `/docs/{module}-explained.md`. Đây là phần QUAN TRỌNG NHẤT vì user cần học từ code.

**Cấu trúc file docs BẮT BUỘC:**

```markdown
# Module {Tên} — Giải thích chi tiết

## 1. Tổng quan
- Module này làm gì, giải quyết bài toán gì

## 2. Danh sách files đã tạo/sửa
- Bảng: tên file | tác dụng | design pattern

## 3. Design Patterns đã áp dụng
Với MỖI pattern:
  a) Pattern tên gì, thuộc nhóm nào (Creational/Structural/Behavioral)
  b) Giải thích pattern bằng ví dụ đời thường (VD: Factory = nhà máy sản xuất)
  c) Áp dụng ở đâu trong code (file:line)
  d) Tại sao dùng pattern này (giải quyết vấn đề gì)
  e) So sánh: KHÔNG dùng pattern → code xấu thế nào (before/after)
  f) Khi nào KHÔNG nên dùng pattern này

## 4. Sơ đồ luồng xử lý
- Vẽ bằng ASCII diagram
- Ghi rõ từng bước: request đi qua đâu, gọi class nào, query gì

## 5. Khái niệm mới cần biết
- Giải thích đơn giản, VD:
  - "Optimistic Lock giống như 2 người cùng edit Google Doc — ai save trước thì được"
  - "ACID giống hợp đồng: hoặc ký hết hoặc hủy hết, không ký nửa chừng"

## 6. Annotation/API mới sử dụng
- Liệt kê từng annotation: tên, tác dụng, ví dụ
- VD: @Transactional → đảm bảo tất cả query trong method này chạy trong 1 transaction

## 7. SQL được sinh ra
- Ghi lại SQL mà JPA/Hibernate tự sinh cho các method quan trọng
- VD: findByUsername("vanan") → SELECT * FROM users WHERE username = 'vanan'

## 8. Request/Response mẫu
- Curl command mẫu
- JSON request body mẫu
- JSON response mẫu (cả success và error)

## 9. Câu hỏi tự kiểm tra
- 3-5 câu hỏi để user tự test kiến thức
- VD: "Nếu bỏ @Transactional thì điều gì xảy ra khi hold 3 ghế mà ghế thứ 3 lỗi?"
```

### Bước 4: Kết thúc task
- Tick tất cả checkbox [x] trong file task
- Đổi Status → DONE
- Move file task sang `/tasks/done/`
- Cập nhật `/docs/erd.md` nếu thêm bảng mới
- Cập nhật bảng Design Patterns trong file CLAUDE.md này nếu thêm pattern mới

---

## DESIGN PATTERNS — BẢNG TỔNG HỢP

### Đã áp dụng

| Pattern | Nhóm | Áp dụng ở đâu | File tham khảo |
|---|---|---|---|
| **BaseEntity (Inheritance)** | Structural | Tất cả entity kế thừa (id, version, storageState, audit) | `common/entity/BaseEntity.java` |
| **IdTracker (Sequence)** | Creational | Sinh code tự động cho entity | `common/entity/tracker/IdTrackerService.java` |
| **Soft Delete** | Behavioral | Xóa mềm qua storageState, không DELETE thật | BaseEntity.storageState |
| **DTO** | Structural | Tách biệt request/response với entity | `module/*/dto/` |
| **Repository** | Structural | Trừu tượng hóa truy vấn DB | `module/*/repository/` |
| **Builder** | Creational | Lombok @Builder tạo object phức tạp | ApiResponse, AuthResponse, User |
| **Filter (Chain of Resp.)** | Behavioral | JwtAuthFilter xác thực trước Controller | `security/JwtAuthFilter.java` |
| **Wrapper/Facade** | Structural | ApiResponse<T> bọc mọi response cùng format | `common/response/ApiResponse.java` |
| **Enum** | — | Role, ErrorCode, MovieStatus, ... type-safe | `module/auth/entity/Role.java` |
| **Mapper (MapStruct)** | Structural | Tự sinh code chuyển User ↔ DTO, compile-time | `module/user/mapper/UserMapper.java` |
| **Method Security** | Cross-cutting | @PreAuthorize phân quyền ADMIN method-level | `module/user/controller/UserController.java` |
| **Specification** | Behavioral | Build query WHERE động cho tất cả list API | `module/*/specification/*Specification.java` |
| **Filter DTO** | Structural | Nhận search/filter params từ FE, type-safe | `module/*/dto/*Filter.java` |

### Sẽ áp dụng (theo task)

| Pattern | Nhóm | Task | Mô tả |
|---|---|---|---|
| **Factory** | Creational | 009 Payment | PaymentProcessorFactory trả đúng processor theo method |
| **Strategy** | Behavioral | 007-008 | PricingStrategy tính giá vé theo nhiều yếu tố |
| **Observer (Events)** | Behavioral | 008-009 | Spring Events: booking confirmed → email + notification |
| **State Machine** | Behavioral | 008 Booking | Booking status flow: HOLDING → CONFIRMED / EXPIRED |
| **Pessimistic Lock** | Concurrency | 008 Booking | Lock row DB khi hold ghế, tránh bán trùng |
| **Scheduled Task** | — | 008 Booking | @Scheduled dọn booking hết hạn mỗi phút |
| **Audit Log (AOP)** | Cross-cutting | Chung | @Aspect ghi log thay đổi entity quan trọng |
| **Config Table** | — | Chung | Bảng system_config: cấu hình động |

---

## QUY TẮC CODE CHI TIẾT

### Entity
- Tất cả entity nghiệp vụ **PHẢI** extends `BaseEntity`
- Entity nào cần code tự sinh → dùng `IdTrackerService.nextCode()` hoặc `nextCodeWithDate()`
- Xóa = soft delete (`storageState = 'DELETED'`), KHÔNG dùng `DELETE FROM`
- Mỗi entity mới → tạo Liquibase changelog, KHÔNG dùng `ddl-auto=update`
- Dùng `@Enumerated(EnumType.STRING)` cho enum fields (không dùng ORDINAL)
- Quan hệ `@ManyToOne` dùng `fetch = FetchType.LAZY` mặc định
- `@Column` ghi rõ `name`, `length`, `nullable` cho mọi field

### DTO & Validation
- Mỗi API endpoint có DTO riêng: `XxxRequest` (input) và `XxxResponse` (output)
- KHÔNG BAO GIỜ trả entity thẳng cho client (lộ field nhạy cảm)
- Request DTO phải có validation: `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max`, ...
- Response DTO dùng `@Builder` để dễ tạo
- Nếu list cần phân trang → trả `PageResponse<T>`
- Nếu 1 entity cần nhiều response khác nhau (list vs detail) → tạo nhiều DTO

### Service
- Mỗi public method cần `@Transactional` (hoặc `@Transactional(readOnly = true)` cho query)
- Business logic CHỈ nằm trong Service, KHÔNG nằm trong Controller hoặc Repository
- Khi có lỗi nghiệp vụ → throw `BusinessException(ErrorCode.XXX)`, KHÔNG return null
- Log quan trọng: dùng `log.info()` cho action thành công, `log.warn()` cho action đáng ngờ
- Method tên rõ ràng: `createBooking()` thay vì `process()`, `handle()` thay vì `doStuff()`

### Controller
- Chỉ làm 3 việc: nhận request → gọi service → trả `ApiResponse`
- KHÔNG chứa business logic, KHÔNG gọi repository trực tiếp
- Dùng `@Valid` cho request body validation
- Dùng Swagger annotation: `@Tag`, `@Operation` cho mỗi endpoint
- URL convention: `/api/{module}/{action}`, dùng danh từ số nhiều (`/api/movies`, `/api/bookings`)
- HTTP method đúng: GET (đọc), POST (tạo), PUT (sửa toàn bộ), PATCH (sửa 1 phần), DELETE (xóa)

### Repository
- Tên method theo convention Spring Data: `findByXxx`, `existsByXxx`, `countByXxx`
- Query phức tạp → dùng `@Query` với JPQL hoặc Specification
- Khi cần lock → dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)`
- KHÔNG viết native SQL trừ khi JPQL không đáp ứng được

### Exception Handling
- Mỗi loại lỗi mới → thêm vào `ErrorCode` enum
- `BusinessException` cho lỗi nghiệp vụ (user gây ra): 4xx
- Lỗi hệ thống (bug, DB down): để `GlobalExceptionHandler` bắt → trả 500
- KHÔNG BAO GIỜ để stack trace lộ ra cho client

### Liquibase
- File đặt trong `resources/db/changelog/changes/`
- Đánh số tăng dần: `001-xxx.xml`, `002-xxx.xml`, ...
- Thêm `<include>` vào `db.changelog-master.xml`
- Có thể dùng `<insert>` để seed data mẫu (admin account, genres, ...)
- Mỗi changeset có `id` unique, `author` = "cinex"
- KHÔNG sửa changeset đã chạy → tạo changeset mới để alter

### Comment trong code
- Comment giải thích **TẠI SAO**, không giải thích **CÁI GÌ** (code tự nói cái gì)
- Khi áp dụng design pattern → comment ghi tên pattern và lý do
  ```java
  // [Strategy Pattern] Mỗi payment method có processor riêng
  // để thêm method mới không cần sửa code cũ (Open/Closed Principle)
  ```
- Khi có logic phức tạp → comment giải thích business rule
  ```java
  // Business rule: Không cho đặt quá 8 ghế/lần
  // để tránh 1 người chiếm hết ghế (scalper prevention)
  ```

---

## KIẾN THỨC CẦN GIẢI THÍCH KHI GẶP

Khi code gặp khái niệm mới, PHẢI giải thích trong docs. Danh sách khái niệm thường gặp:

### Spring Boot / Java
- `@SpringBootApplication` — auto-configuration là gì
- `@Component`, `@Service`, `@Repository`, `@Controller` — stereotype annotations
- `@Autowired` vs constructor injection — tại sao prefer constructor
- `@Transactional` — ACID, rollback, propagation
- `@Scheduled` — cron expression, fixedRate vs fixedDelay
- `@Aspect` / AOP — cross-cutting concern, pointcut, advice
- `@Value` vs `@ConfigurationProperties` — đọc config
- Bean lifecycle — singleton, prototype, request scope

### JPA / Hibernate
- Entity lifecycle: Transient → Managed → Detached → Removed
- Lazy vs Eager loading — N+1 problem
- `@Version` — Optimistic Locking
- `@Lock(PESSIMISTIC_WRITE)` — Pessimistic Locking
- `@ManyToOne`, `@OneToMany`, `@ManyToMany` — quan hệ, cascade, orphanRemoval
- First-level cache vs Second-level cache
- JPQL vs Native Query vs Specification

### Spring Security
- SecurityFilterChain — luồng filter
- Authentication vs Authorization — xác thực vs phân quyền
- `@PreAuthorize` — method-level security
- CORS — tại sao cần, cách hoạt động
- CSRF — tại sao tắt khi dùng JWT

### Design Patterns (GoF)
- **Creational:** Factory, Builder, Singleton
- **Structural:** Adapter, Facade, Decorator, DTO
- **Behavioral:** Strategy, Observer, Template Method, State, Chain of Responsibility
- **SOLID principles:** Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion

### Database
- Index — tại sao cần, khi nào tạo, B-tree
- Transaction isolation levels — Read Uncommitted, Read Committed, Repeatable Read, Serializable
- Deadlock — là gì, cách tránh
- N+1 query problem — là gì, cách fix (JOIN FETCH, @EntityGraph)
- Database normalization — 1NF, 2NF, 3NF

### Redis
- Cache-aside pattern — đọc cache trước, miss thì đọc DB rồi ghi cache
- TTL — time to live, cache expiration
- Cache invalidation — khi nào xóa cache (update/delete entity)

### API Design
- RESTful conventions — resource naming, HTTP methods, status codes
- Pagination — offset-based vs cursor-based
- API versioning — URL vs header
- Rate limiting — tại sao cần, thuật toán Token Bucket

---

## CẤU TRÚC CODE

### Backend
```
backend/src/main/java/com/cinex/
├── common/
│   ├── entity/
│   │   ├── BaseEntity.java          # Class cha: id, version, storageState, audit
│   │   └── tracker/                 # IdTracker: sinh code tự động
│   ├── config/                      # Security, CORS, Redis, OpenAPI, JpaAuditing
│   ├── exception/                   # ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── response/                    # ApiResponse<T>, PageResponse<T>
│   └── util/
├── security/                        # JWT: JwtUtil, JwtAuthFilter, CustomUserDetailsService
└── module/
    └── {tên_module}/
        ├── entity/                  # JPA entities (extends BaseEntity)
        ├── dto/                     # Request/Response DTOs
        ├── repository/              # JPA repositories
        ├── service/                 # Business logic
        ├── controller/              # REST endpoints
        └── mapper/                  # MapStruct mappers
```

### Frontend
- **Font chữ: Inter** — dùng cho toàn bộ giao diện, không dùng font khác

```
frontend/src/
├── api/axios.ts                     # HTTP client + JWT interceptor
├── features/{module}/               # Trang theo module
├── components/                      # Component dùng chung
├── hooks/                           # Custom hooks (useQuery, useMutation)
├── store/                           # Zustand stores
├── routes/                          # React Router
├── types/                           # TypeScript types
└── utils/                           # Utility functions
```

### Docs
```
docs/
├── setup.md                         # Hướng dẫn setup A-Z
├── architecture.md                  # Kiến trúc + design patterns tổng quan
├── erd.md                           # Sơ đồ ERD + chi tiết bảng
├── auth-explained.md                # Giải thích module Auth
├── movie-explained.md               # (sẽ tạo) Giải thích module Movie
├── booking-explained.md             # (sẽ tạo) Giải thích module Booking
├── payment-explained.md             # (sẽ tạo) Giải thích module Payment
└── glossary.md                      # (sẽ tạo) Từ điển thuật ngữ
```

---

## SERVER & PORTS

| Service | Host | Port | Credentials |
|---|---|---|---|
| Backend | localhost | 8088 | — |
| Frontend | localhost | 5173 | — |
| SQL Server | localhost | 1433 | sa / CineX@2026 / cinex |
| Redis | localhost | 6379 | — |

## COMMANDS

```bash
# Build BE
cd /Users/vutuongan/cinex/backend && ./gradlew clean build -x test

# Run BE
cd /Users/vutuongan/cinex/backend && ./gradlew bootRun

# Build FE
cd /Users/vutuongan/cinex/frontend && npm run build

# Run FE
cd /Users/vutuongan/cinex/frontend && npm run dev

# Docker DB
cd /Users/vutuongan/cinex && docker-compose up sqlserver redis -d

# Tạo database (lần đầu)
docker exec cinex-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'CineX@2026' -C -Q "CREATE DATABASE cinex"
```

## TASK MANAGEMENT

- Task folder: `/Users/vutuongan/cinex/tasks/`
- Done folder: `/Users/vutuongan/cinex/tasks/done/`
- Template: `/Users/vutuongan/cinex/tasks/TEMPLATE.md`
- Quy tắc:
  1. Chỉ làm task PENDING/IN_PROGRESS
  2. KHÔNG làm lại task trong `done/`
  3. Khi bắt đầu → đổi Status sang IN_PROGRESS
  4. Khi xong → đổi DONE, tick [x], move sang `done/`
  5. Nếu không có task PENDING → hỏi user muốn làm gì

## GHI NHỚ QUAN TRỌNG

- **User đang học:** Giải thích mọi thứ như đang dạy, dùng ví dụ đời thường
- **Không giả định user biết:** Khi gặp thuật ngữ mới → giải thích ngay
- **So sánh before/after:** Khi dùng pattern → chỉ ra code XẤU (không dùng) vs code TỐT (có dùng)
- **Tại sao quan trọng hơn cái gì:** Giải thích WHY trước WHAT
- **Thực tế production:** Giải thích "ngoài đời thật người ta cũng làm thế này vì..."
- **Anti-patterns:** Khi thấy cách làm sai phổ biến → cảnh báo "ĐỪNG làm thế này vì..."
