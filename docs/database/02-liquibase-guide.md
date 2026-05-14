# Liquibase — Quản lý schema database

## Liquibase là gì?

Liquibase là công cụ **quản lý version cho database schema** (bảng, cột, index, ...).
Giống Git quản lý version code → Liquibase quản lý version DB.

## Ví dụ đời thường

Xây nhà:
- **Không có Liquibase:** Thợ xây tự ý sửa nhà, không ghi chép. Ai muốn biết nhà đã sửa gì → không biết.
- **Có Liquibase:** Mỗi lần sửa nhà → ghi vào sổ (changeset). Ai đọc sổ đều biết: ngày nào sửa gì, ai sửa.

## Tại sao không dùng `ddl-auto=update`?

Hibernate có thể tự tạo/sửa bảng khi start (`ddl-auto=update`). Nhưng:

| Vấn đề | ddl-auto=update | Liquibase |
|---|---|---|
| Xóa cột | Có thể xóa cột → **MẤT DATA** | Không tự xóa, phải viết changeset rõ ràng |
| Track lịch sử | Không biết ai sửa gì, khi nào | Có bảng `DATABASECHANGELOG` ghi lại |
| Rollback | Không thể | Có thể rollback từng changeset |
| Team nhiều dev | Dev A thêm cột, Dev B không biết | Mỗi dev tạo changeset, merge qua Git |
| Production | **TUYỆT ĐỐI KHÔNG DÙNG** | An toàn, kiểm soát được |

**Quy tắc trong CineX:** `ddl-auto=validate` — Hibernate chỉ **kiểm tra** schema khớp entity, KHÔNG tự sửa.

## Cấu trúc file Liquibase

```
backend/src/main/resources/
└── db/changelog/
    ├── db.changelog-master.xml          ← File gốc, include các file con
    └── changes/
        ├── 001-create-users-table.xml   ← Changeset 1: tạo bảng users
        ├── 002-alter-users-add-profile-and-audit.xml  ← Changeset 2: thêm cột
        ├── 003-create-refresh-tokens-table.xml        ← Changeset 3: tạo bảng mới
        ├── 004-alter-users-add-version-storage-state.xml
        └── 005-create-id-tracker-table.xml
```

### File master — `db.changelog-master.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>
    <!-- Liquibase đọc file này → chạy từng file con theo thứ tự -->
    <include file="db/changelog/changes/001-create-users-table.xml"/>
    <include file="db/changelog/changes/002-alter-users-add-profile-and-audit.xml"/>
    <include file="db/changelog/changes/003-create-refresh-tokens-table.xml"/>
    <include file="db/changelog/changes/004-alter-users-add-version-storage-state.xml"/>
    <include file="db/changelog/changes/005-create-id-tracker-table.xml"/>
</databaseChangeLog>
```

### File changeset — Ví dụ tạo bảng

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>

    <changeSet id="001" author="cinex">
    <!--          ↑ id unique     ↑ ai tạo -->

        <createTable tableName="users">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="username" type="NVARCHAR(50)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="email" type="NVARCHAR(100)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="password" type="NVARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="role" type="NVARCHAR(20)" defaultValue="USER">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="DATETIME2" defaultValueComputed="GETDATE()">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="DATETIME2" defaultValueComputed="GETDATE()">
                <constraints nullable="false"/>
            </column>
        </createTable>

    </changeSet>

</databaseChangeLog>
```

### File changeset — Thêm cột vào bảng có sẵn

```xml
<changeSet id="002" author="cinex">
    <addColumn tableName="users">
        <column name="full_name" type="NVARCHAR(100)"/>
        <column name="phone" type="NVARCHAR(20)"/>
        <column name="enabled" type="BIT" defaultValueBoolean="true">
            <constraints nullable="false"/>
        </column>
    </addColumn>
</changeSet>
```

### File changeset — Insert dữ liệu mẫu (Seed Data)

```xml
<changeSet id="008" author="cinex">
    <insert tableName="users">
        <column name="username" value="admin"/>
        <column name="email" value="admin@cinex.com"/>
        <column name="password" value="$2a$10$...BCrypt hash..."/>
        <column name="role" value="ADMIN"/>
    </insert>

    <insert tableName="genres">
        <column name="name" value="Action"/>
    </insert>
    <insert tableName="genres">
        <column name="name" value="Horror"/>
    </insert>
</changeSet>
```

## Cách Liquibase hoạt động — Từng bước

```
Backend start (./gradlew bootRun)
    │
    ▼
Liquibase đọc application-dev.yml:
    spring.liquibase.change-log: classpath:db/changelog/db.changelog-master.xml
    │
    ▼
Đọc db.changelog-master.xml → tìm tất cả file con
    │
    ▼
Kiểm tra bảng DATABASECHANGELOG trong DB
    (Lần đầu chạy: bảng chưa có → Liquibase TỰ TẠO)
    │
    ▼
So sánh: changeset nào ĐÃ CHẠY (có trong DATABASECHANGELOG)?
         changeset nào CHƯA CHẠY?
    │
    ▼
Chạy các changeset CHƯA CHẠY theo thứ tự:
    001-create-users-table.xml      → CREATE TABLE users (...)     ✅ đã chạy
    002-alter-users-add-profile.xml → ALTER TABLE users ADD (...)  ✅ đã chạy
    003-create-refresh-tokens.xml   → CREATE TABLE refresh_tokens  ❌ chưa chạy → CHẠY
    004-alter-users-add-version.xml → ALTER TABLE users ADD (...)  ❌ chưa chạy → CHẠY
    005-create-id-tracker.xml       → CREATE TABLE id_tracker      ❌ chưa chạy → CHẠY
    │
    ▼
Ghi lại vào DATABASECHANGELOG:
    id=003, author=cinex, dateExecuted=2026-05-12, ...
    id=004, author=cinex, dateExecuted=2026-05-12, ...
    id=005, author=cinex, dateExecuted=2026-05-12, ...
    │
    ▼
Xong! Backend tiếp tục start bình thường.
```

## Bảng DATABASECHANGELOG — Liquibase tự tạo

```
┌─────┬─────────────┬──────────────┬────────────────────┐
│ ID  │ AUTHOR      │ FILENAME     │ DATEEXECUTED       │
├─────┼─────────────┼──────────────┼────────────────────┤
│ 001 │ cinex   │ 001-create.. │ 2026-05-12 20:00   │
│ 002 │ cinex   │ 002-alter..  │ 2026-05-12 20:00   │
│ 003 │ cinex   │ 003-create.. │ 2026-05-12 20:00   │
│ 004 │ cinex   │ 004-alter..  │ 2026-05-12 20:01   │
│ 005 │ cinex   │ 005-create.. │ 2026-05-12 20:01   │
└─────┴─────────────┴──────────────┴────────────────────┘
```

Lần start tiếp theo: Liquibase thấy 001-005 đã chạy → **bỏ qua** → không chạy lại.
Nếu thêm file 006 mới → chỉ chạy 006.

## Hướng dẫn: Thêm bảng mới

**Bước 1:** Tạo file changeset mới

```bash
# Đặt tên theo số thứ tự + mô tả
backend/src/main/resources/db/changelog/changes/006-create-movies-table.xml
```

**Bước 2:** Viết nội dung changeset

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>
    <changeSet id="006" author="cinex">
        <createTable tableName="movies">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="title" type="NVARCHAR(200)">
                <constraints nullable="false"/>
            </column>
            <!-- ... các cột khác ... -->
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Bước 3:** Thêm vào file master

```xml
<!-- db.changelog-master.xml -->
<include file="db/changelog/changes/005-create-id-tracker-table.xml"/>
<include file="db/changelog/changes/006-create-movies-table.xml"/>  <!-- THÊM DÒNG NÀY -->
```

**Bước 4:** Restart backend → Liquibase tự chạy changeset 006

## Quy tắc quan trọng

| Quy tắc | Giải thích |
|---|---|
| **KHÔNG SỬA changeset đã chạy** | Liquibase kiểm tra checksum. Sửa → lỗi. Muốn thay đổi → tạo changeset MỚI |
| **Số thứ tự tăng dần** | 001, 002, 003, ... Không nhảy, không trùng |
| **1 changeset = 1 thay đổi** | Không gộp tạo 5 bảng vào 1 changeset. Tách ra dễ rollback |
| **id unique** | id + author + filename phải unique |

## Reset DB (chạy lại từ đầu)

```bash
# Cách 1: Docker — xóa volume
docker-compose down -v
docker-compose up sqlserver redis -d
# Chờ 15 giây
docker exec cinex-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'CineX@2026' -C -Q "CREATE DATABASE cinex"
# Chạy backend → Liquibase tạo lại tất cả bảng

# Cách 2: SQL Server có sẵn — xóa database
# Chạy SQL: DROP DATABASE cinex; CREATE DATABASE cinex;
# Chạy backend → Liquibase tạo lại
```

---

# 3. Seed Data — Dữ liệu mẫu

## Là gì?
Dữ liệu **insert sẵn** vào DB khi chạy lần đầu. Ai clone code về đều có data cơ bản.

## Ví dụ đời thường
Mua điện thoại mới: đã có sẵn app Điện thoại, Tin nhắn, Camera. Không cần tự cài.

## Tại sao cần?
- Clone code → chạy → **có ngay** admin account để login
- Không cần tự tạo thể loại phim bằng tay
- Demo cho thầy: chạy 1 lệnh là xong

## Trong CineX — Seed gì?

```xml
<!-- 1. Admin account (password: Admin@123) -->
<insert tableName="users">
    <column name="username" value="admin"/>
    <column name="email" value="admin@cinex.com"/>
    <column name="password" value="$2a$10$...BCrypt hash của Admin@123..."/>
    <column name="role" value="ADMIN"/>
    <column name="enabled" valueBoolean="true"/>
</insert>

<!-- 2. Thể loại phim -->
<insert tableName="genres"><column name="name" value="Action"/></insert>
<insert tableName="genres"><column name="name" value="Horror"/></insert>
<insert tableName="genres"><column name="name" value="Comedy"/></insert>
<insert tableName="genres"><column name="name" value="Romance"/></insert>
<insert tableName="genres"><column name="name" value="Sci-Fi"/></insert>
<insert tableName="genres"><column name="name" value="Animation"/></insert>

<!-- 3. IdTracker entries -->
<insert tableName="id_tracker">
    <column name="entity_type" value="BOOKING"/>
    <column name="prefix" value="VC"/>
    <column name="current_value" value="0"/>
</insert>

<!-- 4. Config mặc định -->
<insert tableName="system_config">
    <column name="config_key" value="booking.hold_minutes"/>
    <column name="config_value" value="10"/>
</insert>
```

