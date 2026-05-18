# Task 003 — Common Infra

## Đã làm gì?

Tạo 3 thành phần dùng chung cho toàn bộ dự án:

### 1. BaseService — CRUD dùng chung (Template Method Pattern)

**File:** `common/service/BaseService.java`

**Mục đích:** Viết CRUD 1 lần, 8 module dùng lại. Thay vì mỗi module tự viết `findById`, `save`, `softDelete`, chỉ cần:

```java
public class MovieService extends BaseService<Movie, Long> {
    @Override
    protected JpaRepository<Movie, Long> getRepository() {
        return movieRepository;
    }
    // Tự động có: findById, findAll, save, softDelete
}
```

**Method có sẵn:**
- `findById(id)` — tìm theo id, tự động filter entity đã soft delete (storageState = DELETED)
- `findAll(pageable)` — phân trang, trả PageResponse
- `save(entity)` — lưu mới hoặc cập nhật
- `softDelete(id)` — KHÔNG xoá thật, chỉ set storageState = DELETED

**Lưu ý:**
- `findById` filter soft delete bằng Java (`.filter()`), không phải SQL WHERE — đơn giản nhưng nếu dữ liệu lớn cần thêm `@Where` annotation hoặc custom query
- `softDelete` gọi `findById` trước để đảm bảo entity tồn tại — 2 query (SELECT + UPDATE)
- Subclass có thể override bất kỳ method nào nếu logic CRUD khác chuẩn

---

### 2. AuditLog — Ghi lại thay đổi

**Files:**
- `module/audit/entity/AuditLog.java` — entity map bảng `audit_log`
- `module/audit/dto/AuditLogRequest.java` — wrap tham số (thay vì truyền 6 tham số)
- `module/audit/repository/AuditLogRepository.java` — truy vấn
- `module/audit/service/AuditLogService.java` — ghi log

**Mục đích:** Khi entity quan trọng thay đổi (booking status, user role, payment), ghi lại:
- Ai sửa (changed_by)
- Sửa bảng nào, record nào (table_name, record_id)
- Sửa field gì, giá trị cũ → mới (field_name, old_value, new_value)
- Lúc nào (changed_at)

**Cách gọi:**
```java
auditLogService.log(AuditLogRequest.builder()
        .tableName("bookings")
        .recordId(bookingId)
        .action("UPDATE")
        .fieldName("status")
        .oldValue("HOLDING")
        .newValue("CONFIRMED")
        .build());
```

**Lưu ý:**
- Dùng `Propagation.REQUIRES_NEW` — tạo transaction riêng, dù transaction chính rollback thì audit log vẫn được ghi
- Không ghi audit cho mọi thay đổi — chỉ ghi cho entity quan trọng (booking, payment, role change)
- `old_value` và `new_value` tự động truncate 500 ký tự để không vượt quá column size

---

### 3. SystemConfig — Cấu hình động (Cache-aside Pattern)

**Files:**
- `module/config/entity/SystemConfig.java` — entity map bảng `system_config`
- `module/config/repository/SystemConfigRepository.java` — truy vấn
- `module/config/service/SystemConfigService.java` — đọc config từ cache

**Mục đích:** Lưu cấu hình có thể thay đổi mà không cần restart server:

| Key | Value | Dùng cho |
|---|---|---|
| `booking.hold_minutes` | 10 | Thời gian giữ ghế (phút) |
| `booking.max_seats` | 8 | Số ghế tối đa mỗi lần đặt |
| `showtime.buffer_minutes` | 15 | Thời gian dọn phòng giữa 2 suất |

**Cách gọi:**
```java
int holdMinutes = configService.getInt("booking.hold_minutes", 10);
int maxSeats = configService.getInt("booking.max_seats", 8);
String value = configService.getString("some.key", "default");
```

**Cách hoạt động:**
```
Server khởi động
    |
    v
@PostConstruct loadAll() — SELECT * FROM system_config
    |
    v
Lưu tất cả vào ConcurrentHashMap (key -> value)
    |
    v
Khi Service gọi getInt("booking.max_seats", 8)
    |
    v
Đọc từ HashMap (0ms) — KHÔNG query DB
```

**Lưu ý:**
- Cache load 1 lần khi startup, không tự động refresh
- Khi admin sửa config qua `updateConfig()` → cập nhật DB + cache đồng thời
- Nếu cần sync lại toàn bộ → gọi `reload()`
- Dùng `ConcurrentHashMap` (thread-safe) thay vì `HashMap` vì Spring Bean là singleton, nhiều thread truy cập đồng thời
- Nếu parse lỗi (VD: value = "abc" mà gọi `getInt()`) → trả defaultValue + log warning, KHÔNG throw exception

---

## Công nghệ / Khái niệm đã học

### 1. Template Method Pattern
- **Nhóm:** Behavioral (GoF)
- **Ý tưởng:** Class cha định nghĩa "khung" thuật toán, class con chỉ override bước cần thiết
- **Trong project:** BaseService viết sẵn CRUD, subclass chỉ override `getRepository()`
- **Khi nào dùng:** Nhiều class có logic giống nhau, chỉ khác 1-2 bước
- **Khi nào KHÔNG dùng:** Chỉ có 1-2 class, hoặc logic khác nhau hoàn toàn

### 2. Cache-aside Pattern
- **Ý tưởng:** Đọc cache trước, miss thì đọc DB, lưu cache
- **Trong project:** SystemConfigService dùng ConcurrentHashMap làm cache
- **Biến thể:** Mình dùng "pre-load all" (load hết khi startup) thay vì "lazy load" (load khi miss) — vì config ít record, load hết cho nhanh

### 3. Propagation.REQUIRES_NEW
- **Là gì:** Tạo transaction MỚI, độc lập với transaction đang chạy
- **Tại sao:** Audit log cần được ghi ngay cả khi transaction chính fail
- **Ví dụ:** holdSeats() fail rollback → audit log "HOLD_FAILED" vẫn được ghi

### 4. @PostConstruct
- **Là gì:** Method chạy 1 lần sau khi Bean được tạo + inject dependencies xong
- **Thứ tự:** Constructor → @Autowired → @PostConstruct
- **Dùng cho:** Khởi tạo cache, load config, validate trạng thái ban đầu

### 5. ConcurrentHashMap vs HashMap
- **HashMap:** KHÔNG thread-safe, 2 thread ghi đồng thời có thể corrupt data
- **ConcurrentHashMap:** Thread-safe, dùng lock từng segment (không lock toàn bộ map)
- **Khi nào dùng:** Bất kỳ khi nào Map được share giữa nhiều thread (Spring singleton bean)

### 6. Quy tắc tham số method
- Tối đa 3 tham số, nếu hơn thì wrap vào object (DTO/Request)
- `AuditLogRequest` là ví dụ — thay vì truyền 6 string, wrap thành 1 object với Builder

---

## Quy tắc util đã áp dụng

Khi dọn lại các file util:

| Loại logic | Để ở đâu | Ví dụ |
|---|---|---|
| Format/convert thuần tuý | Util class | `MoneyUtil.formatVND()`, `StringUtil.toSlug()` |
| Logic nghiệp vụ | Service | Tính giá vé, check trùng giờ, check cuối tuần |
| Check null object | `== null` trực tiếp | `cache.get(key) == null` |
| Validate chuỗi user input | `String.isBlank()` hoặc Spring `StringUtils.hasText()` | Validate form field |
| Dùng 1 chỗ duy nhất | Viết trực tiếp trong Service | Không tạo util sớm (YAGNI) |

---

## Cấu trúc files đã tạo/sửa

```
backend/src/main/java/com/cinex/
├── common/
│   ├── service/
│   │   └── BaseService.java              ← [MỚI] CRUD dùng chung
│   └── util/
│       ├── StringUtil.java               ← [SỬA] Bỏ isBlank, truncate, capitalize
│       ├── DateTimeUtil.java             ← [SỬA] Chỉ giữ formatter constants
│       ├── MoneyUtil.java                ← [SỬA] Chỉ giữ formatVND
│       └── SecurityUtil.java             ← [GIỮ NGUYÊN]
└── module/
    ├── audit/
    │   ├── entity/AuditLog.java          ← [MỚI]
    │   ├── dto/AuditLogRequest.java      ← [MỚI]
    │   ├── repository/AuditLogRepository.java  ← [MỚI]
    │   └── service/AuditLogService.java  ← [MỚI]
    └── config/
        ├── entity/SystemConfig.java      ← [MỚI]
        ├── repository/SystemConfigRepository.java  ← [MỚI]
        └── service/SystemConfigService.java  ← [MỚI]
```

## Liquibase (đã có sẵn, KHÔNG tạo mới)

- `004-create-system-config-table.xml` — bảng system_config + 3 record mặc định
- `005-create-audit-log-table.xml` — bảng audit_log + index (table_name, record_id)
- `013-seed-default-data.xml` — tài khoản admin + 10 thể loại phim
