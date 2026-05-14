# Task: Common Infra — BaseService, Audit Log, Config Table, Seed Data

## Status: PENDING

## Module
backend

## Mô tả
Tạo các thành phần dùng chung cho tất cả module sau này. Gồm: BaseService (CRUD chung), Audit Log (ghi lại thay đổi), Config Table (cấu hình động), Seed Data (dữ liệu mẫu khi clone project).

**Kết quả mong đợi:**
- `BaseService<E, ID>` — các module chỉ cần extends, không viết lại CRUD
- Bảng `audit_log` — ghi lại ai sửa gì, lúc nào, giá trị cũ → mới
- Bảng `system_config` — admin thay đổi config không cần deploy
- Liquibase seed data: 1 tài khoản admin, thể loại phim mẫu

## Việc cần làm

### BaseService (Template Method Pattern)
- [ ] `BaseService<E extends BaseEntity, ID>` abstract class:
  - `findById(ID id)` → trả entity hoặc throw NOT_FOUND
  - `findAll(Pageable)` → trả PageResponse
  - `save(E entity)` → save + return
  - `softDelete(ID id)` → set storageState='DELETED'
  - `abstract getRepository()` → mỗi module override trả repository riêng
- [ ] Các module sau chỉ cần: `class MovieService extends BaseService<Movie, Long>`

### Audit Log (AOP Pattern)
- [ ] Liquibase `006-create-audit-log-table.xml` — bảng `audit_log` (id, tableName, recordId, action, fieldName, oldValue, newValue, changedBy, changedAt)
- [ ] `AuditLog.java` entity
- [ ] `AuditLogRepository.java`
- [ ] `AuditLogService.java` — ghi log thay đổi
- [ ] (Tùy chọn) `@Aspect` AuditAspect — tự động log khi entity thay đổi

### Config Table
- [ ] Liquibase `007-create-system-config-table.xml` — bảng `system_config` (id, configKey, configValue, description)
- [ ] `SystemConfig.java` entity
- [ ] `SystemConfigRepository.java`
- [ ] `SystemConfigService.java`:
  - `getString(key, defaultValue)`
  - `getInt(key, defaultValue)`
  - `getLong(key, defaultValue)`
  - `getBoolean(key, defaultValue)`
- [ ] Seed data: booking.hold_minutes=10, booking.max_seats=8, ticket.prefix=VC, ...

### Seed Data (Liquibase insert)
- [ ] Liquibase `008-seed-default-data.xml`:
  - 1 tài khoản admin (username: admin, password: BCrypt hash của "Admin@123")
  - Thể loại phim mẫu: Hành động, Kinh dị, Hài, Tình cảm, Khoa học viễn tưởng, Hoạt hình, Phiêu lưu, Tâm lý, Tài liệu, Âm nhạc
  - Cấu hình mặc định trong system_config
  - IdTracker entries cho các entity mới (ROOM, GENRE, SHOWTIME)

## Design Patterns cần áp dụng (MỤC TIÊU HỌC)

### 1. Template Method Pattern
- **Ở đâu:** `BaseService` — method `findById`, `softDelete` đã viết sẵn, subclass override `getRepository()`
- **Tại sao:** 8 module đều có CRUD giống nhau, không viết lại 8 lần
- **Học được gì:** Abstract class, template method, DRY principle

### 2. AOP (Aspect-Oriented Programming)
- **Ở đâu:** `AuditAspect` — tự động ghi log khi save/update entity
- **Tại sao:** Không cần viết code audit trong mỗi service. Cross-cutting concern
- **Học được gì:** `@Aspect`, `@Around`, Pointcut, JoinPoint

### 3. Cache-aside Pattern (cho Config)
- **Ở đâu:** `SystemConfigService` — đọc từ cache (Redis/Map) trước, miss thì đọc DB
- **Tại sao:** Config đọc rất thường xuyên, không cần query DB mỗi lần
- **Học được gì:** Caching strategy, cache invalidation

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] BaseService hoạt động: module kế thừa có đủ CRUD
- [ ] Audit log ghi lại khi tạo/sửa/xóa entity
- [ ] SystemConfigService đọc được config từ DB
- [ ] Chạy bootRun → Liquibase seed admin + thể loại + config mặc định
- [ ] Login bằng admin/Admin@123 thành công

## Tham khảo
- `common/entity/BaseEntity.java` — đã có sẵn
- `common/entity/tracker/IdTrackerService.java` — pattern tương tự cho config

## Ghi chú
- Seed data password admin phải hash BCrypt trước khi insert
- Config table nên cache trong memory (Map hoặc Redis), reload khi admin sửa
- Audit log chỉ ghi cho entity quan trọng (booking, payment, user role change), không ghi tất cả

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích từng file đã tạo
- [ ] Viết `/docs/common-infra-explained.md`:
  1. Template Method Pattern: BaseService hoạt động thế nào
  2. AOP: Aspect là gì, Pointcut là gì, so sánh với viết code thủ công
  3. Cache-aside: khi nào đọc cache, khi nào đọc DB
  4. Seed data: tại sao cần, Liquibase insert hoạt động thế nào
- [ ] Đổi Status → DONE, tick [x], move sang `done/`
