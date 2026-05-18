# Common Infra — Giai thich chi tiet

## 1. Tong quan

Task nay tao 3 thanh phan dung chung cho tat ca module:
- **BaseService** — CRUD chung, module chi can extends
- **AuditLog** — ghi lai ai sua gi, luc nao
- **SystemConfig** — cau hinh dong, doc tu DB + cache

## 2. Danh sach files da tao

| File | Tac dung | Design Pattern |
|---|---|---|
| `common/service/BaseService.java` | Abstract class CRUD chung | Template Method |
| `module/audit/entity/AuditLog.java` | Entity map bang `audit_log` | — |
| `module/audit/repository/AuditLogRepository.java` | Truy van audit log | Repository |
| `module/audit/service/AuditLogService.java` | Ghi log thay doi | — |
| `module/config/entity/SystemConfig.java` | Entity map bang `system_config` | — |
| `module/config/repository/SystemConfigRepository.java` | Truy van config | Repository |
| `module/config/service/SystemConfigService.java` | Doc config tu cache/DB | Cache-aside |

## 3. Design Patterns da ap dung

### 3.1 Template Method Pattern (Behavioral)

#### Pattern la gi?
Dinh nghia "khung suon" cua 1 thuat toan trong class cha, nhung cho class con override 1 so buoc cu the.

#### Vi du doi thuong
Nghi nhu **quy trinh lam banh**:
- Buoc chung (class cha da viet): nhao bot -> nuong -> dong goi
- Buoc rieng (class con override): **loai nhan** (banh mi thi nhan thit, banh ngot thi nhan kem)

#### Ap dung o dau trong code

```java
// BaseService.java — class cha
public abstract class BaseService<E extends BaseEntity, ID> {

    // Template method: class con PHAI override de tra repository rieng
    protected abstract JpaRepository<E, ID> getRepository();

    // Cac method chung — DA VIET SAN, class con KHONG can viet lai
    public E findById(ID id) { ... }
    public PageResponse<E> findAll(Pageable pageable) { ... }
    public E save(E entity) { ... }
    public void softDelete(ID id) { ... }
}

// MovieService.java — class con (se tao o task 005)
public class MovieService extends BaseService<Movie, Long> {

    private final MovieRepository movieRepository;

    @Override
    protected JpaRepository<Movie, Long> getRepository() {
        return movieRepository;  // Chi can tra repository, CRUD co san
    }

    // Chi viet them logic rieng cua Movie
    public List<Movie> searchByTitle(String keyword) { ... }
}
```

#### Tai sao dung pattern nay?
- 8 module (User, Movie, Room, Seat, Showtime, Booking, Payment, Genre) deu co CRUD giong nhau
- KHONG dung: viet lai findById, save, softDelete **8 lan** — vi pham DRY
- CO dung: viet 1 lan trong BaseService, 8 module chi override 1 method

#### So sanh before/after

**KHONG dung (code xau):**
```java
// MovieService.java — viet day du
public Movie findById(Long id) {
    return movieRepository.findById(id).orElseThrow(...);
}
public void softDelete(Long id) {
    Movie movie = findById(id);
    movie.setStorageState("DELETED");
    movieRepository.save(movie);
}

// RoomService.java — LAP LAI Y CHANG
public Room findById(Long id) {
    return roomRepository.findById(id).orElseThrow(...);
}
public void softDelete(Long id) {
    Room room = findById(id);
    room.setStorageState("DELETED");
    roomRepository.save(room);
}
// ... lap lai 6 lan nua cho Seat, Showtime, Booking, ...
```

**CO dung (code tot):**
```java
// MovieService chi can 3 dong
public class MovieService extends BaseService<Movie, Long> {
    @Override
    protected JpaRepository<Movie, Long> getRepository() {
        return movieRepository;
    }
}
// Tu dong co: findById, findAll, save, softDelete
```

#### Khi nao KHONG nen dung?
- Khi logic CRUD khac nhau hoan toan giua cac module (VD: module A can hard delete, module B can soft delete voi logic phuc tap)
- Khi chi co 1-2 module — tao abstract class qua som la over-engineering

---

### 3.2 Cache-aside Pattern

#### Pattern la gi?
Khi can doc du lieu:
1. Doc tu **cache** truoc
2. Neu cache co (hit) -> tra ve ngay (nhanh)
3. Neu cache khong co (miss) -> doc tu **DB** -> luu vao cache -> tra ve

#### Vi du doi thuong
Giong **tu lanh** o nha:
- Muon uong nuoc -> mo tu lanh lay (cache hit) -> KHONG can ra sieu thi
- Tu lanh het nuoc (cache miss) -> phai ra sieu thi mua (doc DB) -> bo vao tu lanh (luu cache) -> uong

#### Ap dung o dau trong code

```java
// SystemConfigService.java
@Service
public class SystemConfigService {

    // Cache = ConcurrentHashMap (thread-safe)
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct  // Chay 1 lan khi server khoi dong
    public void loadAll() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        cache.clear();
        configs.forEach(c -> cache.put(c.getConfigKey(), c.getConfigValue()));
        // Sau buoc nay, moi lan doc config = doc tu HashMap (0ms)
        // KHONG can query DB moi lan
    }

    public int getInt(String key, int defaultValue) {
        String value = cache.get(key);  // Doc tu cache, KHONG query DB
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    // Khi admin sua config -> luu DB + cap nhat cache
    public void updateConfig(String key, String value) {
        systemConfigRepository.save(...);  // Luu DB
        cache.put(key, value);             // Cap nhat cache
    }
}
```

#### Tai sao dung Cache-aside?
- Config doc RAT THUONG XUYEN (moi lan hold ghe, moi lan tao showtime deu doc)
- Config HIEM KHI THAY DOI (chi admin sua)
- Query DB moi lan = lang phi -> dung cache, doc tu memory (nhanh gap 1000 lan)

#### Cache invalidation
Khi admin sua config, co 2 cach:
1. **Xoa cache** -> lan doc tiep se doc DB lai (lazy)
2. **Cap nhat cache** -> luon dong bo (eager) — **minh dung cach nay**

---

### 3.3 AOP (Aspect-Oriented Programming) — Audit Log

#### AOP la gi?
**Cross-cutting concern** = logic can chay o NHIEU noi nhung KHONG THUOC logic chinh.

VD: Ghi log thay doi entity — can ghi o MovieService, BookingService, UserService, ... nhung khong lien quan den logic dat ve hay CRUD phim.

#### Vi du doi thuong
Nghi nhu **camera an ninh** trong sieu thi:
- Nhan vien ban hang (Service) chi can ban hang — KHONG can tu quay camera
- Camera (Aspect) tu dong ghi hinh TAT CA nguoi ra vao — nhan vien KHONG CAN BIET
- Neu bo camera, sieu thi van hoat dong binh thuong

#### Ap dung o dau trong code

Hien tai dung **thu cong** — goi `auditLogService.log(...)` khi can:

```java
// Trong BookingService (se viet o task 009)
public void confirmBooking(Long bookingId) {
    Booking booking = findById(bookingId);
    String oldStatus = booking.getStatus().name();
    booking.setStatus(BookingStatus.CONFIRMED);
    save(booking);

    // Ghi audit log
    auditLogService.log("bookings", bookingId, "UPDATE",
        "status", oldStatus, "CONFIRMED");
}
```

(Tuy chon) Neu muon **tu dong** bang `@Aspect`:
```java
@Aspect
@Component
public class AuditAspect {
    @Around("@annotation(Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) {
        // Tu dong ghi log truoc/sau khi method chay
    }
}
```

#### Propagation.REQUIRES_NEW
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(...) { ... }
```
- Audit log chay trong **transaction rieng**
- Neu transaction chinh rollback (VD: hold ghe loi), audit log VAN DUOC GHI
- Giong nhu camera van ghi hinh ke ca khi giao dich that bai

---

## 4. So do luong xu ly

### BaseService — findById
```
Controller goi: movieService.findById(5)
        |
        v
BaseService.findById(5)
        |
        v
getRepository() -> MovieRepository (class con override)
        |
        v
movieRepository.findById(5)
        |
        +-- Tim thay + storageState != DELETED -> tra entity
        |
        +-- Khong tim thay HOAC storageState = DELETED -> throw NOT_FOUND
```

### SystemConfigService — luong doc config
```
BookingService goi: configService.getInt("booking.max_seats", 8)
        |
        v
cache.get("booking.max_seats")
        |
        +-- Co trong cache -> tra "8" -> parse int -> return 8
        |
        +-- Khong co -> return defaultValue (8)

Khi server khoi dong:
        |
        v
@PostConstruct loadAll()
        |
        v
SELECT * FROM system_config
        |
        v
Luu tat ca vao ConcurrentHashMap
        |
        v
Log: "Loaded 3 system configs into cache"
```

## 5. Khai niem moi can biet

### @PostConstruct
- Annotation danh dau method chay **1 lan duy nhat** sau khi Bean duoc tao
- Dung de khoi tao data, load cache, ...
- Tuong tu constructor nhung chay SAU khi dependency injection xong

### ConcurrentHashMap
- Giong HashMap nhung **thread-safe** — nhieu thread doc/ghi dong thoi khong bi loi
- Tai sao khong dung HashMap? Vi Spring Bean la singleton — nhieu request (thread) cung dung 1 instance

### Propagation.REQUIRES_NEW
- Tao transaction MOI, doc lap voi transaction hien tai
- Neu transaction chinh rollback, transaction moi VAN COMMIT
- Dung cho audit log — dam bao log duoc ghi du co loi

### Abstract class vs Interface
- **Abstract class:** co the co method DA VIET SAN (BaseService co findById, save, ...)
- **Interface:** chi dinh nghia method, KHONG co body (truoc Java 8)
- Template Method dung abstract class vi can viet san logic chung

## 6. Annotation moi su dung

| Annotation | Tac dung |
|---|---|
| `@MappedSuperclass` | BaseEntity: JPA khong tao bang, chi dung de ke thua |
| `@Version` | Optimistic Locking: tu dong tang version khi update |
| `@CreatedBy` / `@LastModifiedBy` | Tu dong ghi ai tao/sua (lay tu SecurityContext) |
| `@CreatedDate` / `@LastModifiedDate` | Tu dong ghi thoi gian tao/sua |
| `@PostConstruct` | Chay 1 lan sau khi Bean duoc tao |
| `@Transactional(propagation = REQUIRES_NEW)` | Tao transaction doc lap |

## 7. SQL duoc sinh ra

### SystemConfigService.loadAll()
```sql
SELECT id, config_key, config_value, description FROM system_config
```

### BaseService.findById(5)
```sql
SELECT * FROM movies WHERE id = 5
-- Sau do Java check: storageState != 'DELETED'
```

### BaseService.softDelete(5)
```sql
SELECT * FROM movies WHERE id = 5
UPDATE movies SET storage_state = 'DELETED', version = version + 1, updated_at = ..., updated_by = ... WHERE id = 5 AND version = 0
```

### AuditLogService.log(...)
```sql
INSERT INTO audit_log (table_name, record_id, action, field_name, old_value, new_value, changed_by, changed_at)
VALUES ('bookings', 1, 'UPDATE', 'status', 'HOLDING', 'CONFIRMED', 'admin', '2026-05-18 10:00:00')
```

## 8. Cau hoi tu kiem tra

1. **Template Method Pattern khac gi Strategy Pattern?** Template Method dung ke thua (abstract class), Strategy dung composition (interface). Khi nao dung cai nao?

2. **Neu khong co cache, SystemConfigService se the nao?** Moi lan goi getInt() = 1 query DB. Neu 100 request/giay, moi request doc config 3 lan = 300 query/giay chi de doc config — lang phi.

3. **Tai sao AuditLogService dung REQUIRES_NEW?** Neu dung REQUIRED (mac dinh), khi transaction chinh rollback thi audit log cung bi rollback — mat log.

4. **Tai sao BaseService.findById() check storageState?** Vi soft delete chi set storageState='DELETED' chu khong xoa row. Neu khong filter, se tra ve entity da "xoa".

5. **ConcurrentHashMap khac HashMap the nao?** HashMap khong thread-safe: 2 thread ghi dong thoi co the lam hu data. ConcurrentHashMap lock tung segment, an toan voi multi-thread.
