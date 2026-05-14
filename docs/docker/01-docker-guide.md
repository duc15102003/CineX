# Docker — Hướng dẫn từ zero

## Docker là gì?

Docker là công cụ **đóng gói ứng dụng + tất cả thứ nó cần** (ngôn ngữ, thư viện, config) vào 1 "hộp" gọi là **container**. Ai có Docker đều chạy được, không cần cài thêm gì.

## Ví dụ đời thường

Bạn muốn gửi cho bạn bè 1 món ăn:
- **Không có Docker:** Gửi công thức → bạn bè tự mua nguyên liệu, tự nấu. Thiếu 1 thứ là hỏng.
- **Có Docker:** Gửi luôn hộp cơm hoàn chỉnh → mở nắp là ăn. Ai nhận cũng ăn được.

## Tại sao cần Docker cho CineX?

Dự án cần chạy:
1. **SQL Server 2022** — database
2. **Redis 7** — cache
3. **Java 21** — backend
4. **Node 20** — frontend

Không có Docker → ai clone code về phải **tự cài 4 thứ**, sai version 1 cái là lỗi.
Có Docker → chạy 1 lệnh `docker-compose up` → tất cả tự chạy.

## Thuật ngữ Docker

### Image — Bản thiết kế
- File **read-only** chứa tất cả thứ cần để chạy ứng dụng
- Giống file ISO cài Windows — bản thiết kế, chưa chạy
- VD: `mcr.microsoft.com/mssql/server:2022-latest` = image SQL Server 2022

### Container — Cái đang chạy
- **Instance** tạo từ image, đang chạy thật
- Giống máy ảo nhưng nhẹ hơn nhiều
- 1 image → tạo nhiều container
- VD: `cinex-sqlserver-1` = container SQL Server đang chạy

### Dockerfile — Hướng dẫn build image
Script từng bước để tạo image từ code.

```dockerfile
# backend/Dockerfile — đọc từng dòng:

# Bước 1: Bắt đầu từ image Java 21 (có compiler javac)
FROM eclipse-temurin:21-jdk AS build

# Bước 2: Tạo thư mục làm việc trong container
WORKDIR /app

# Bước 3: Copy file build config trước (tận dụng cache)
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./

# Bước 4: Download dependencies (chỉ chạy lại khi build.gradle thay đổi)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Bước 5: Copy source code
COPY src src

# Bước 6: Build ra file JAR
RUN ./gradlew clean build -x test --no-daemon

# ---- Stage 2: Chạy (image nhẹ hơn, chỉ có JRE) ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Chỉ copy file JAR từ stage build → image nhỏ gọn
COPY --from=build /app/build/libs/*.jar app.jar

# Mở port 8088
EXPOSE 8088

# Chạy JAR khi container start
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Tại sao 2 stage?**
- Stage 1 (build): Dùng JDK ~400MB (có compiler) — chỉ để build
- Stage 2 (run): Dùng JRE ~200MB (không có compiler) — chỉ để chạy
- Image cuối cùng nhỏ hơn ~200MB thay vì ~400MB

### docker-compose.yml — Chạy nhiều container cùng lúc

```yaml
# docker-compose.yml — đọc từng dòng:

services:
  # ---- Service 1: SQL Server ----
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    # ↑ Dùng image SQL Server từ Microsoft (Docker Hub tự download)
    environment:
      ACCEPT_EULA: "Y"                    # Đồng ý license Microsoft
      MSSQL_SA_PASSWORD: "CineX@2026" # Password cho user SA
    ports:
      - "1433:1433"
      # ↑ Map port: máy bạn:container
      # Truy cập localhost:1433 → vào SQL Server trong container
    volumes:
      - sqlserver-data:/var/opt/mssql
      # ↑ Volume: data lưu bên ngoài container
      # Container bị xóa → data vẫn còn

  # ---- Service 2: Redis ----
  redis:
    image: redis:7-alpine        # Image Redis 7, bản nhẹ (Alpine Linux)
    ports:
      - "6379:6379"

  # ---- Service 3: Backend ----
  backend:
    build:
      context: ./backend         # Build từ Dockerfile trong thư mục backend
    ports:
      - "8088:8088"
    environment:                 # Biến môi trường cho ứng dụng
      DB_HOST: sqlserver         # ← Tên service = hostname trong Docker network
      DB_PORT: 1433
      DB_NAME: cinex
      DB_USERNAME: sa
      DB_PASSWORD: "CineX@2026"
      REDIS_HOST: redis          # ← Redis cũng dùng tên service
    depends_on:
      - sqlserver                # ← Chờ sqlserver start xong mới start backend
      - redis

  # ---- Service 4: Frontend ----
  frontend:
    build:
      context: ./frontend
    ports:
      - "5173:80"                # FE chạy Nginx port 80, map ra 5173
    depends_on:
      - backend

# Volume: data tồn tại khi restart
volumes:
  sqlserver-data:
```

### Volume — Giữ data khi container restart

```
Không có volume:
  docker-compose down → container bị xóa → DATA MẤT HẾT
  docker-compose up   → SQL Server mới, DB trống

Có volume:
  docker-compose down → container bị xóa, nhưng volume vẫn còn
  docker-compose up   → SQL Server mới, nhưng DATA VẪN CÒN (đọc từ volume)

  docker-compose down -v → xóa CẢ volume → DATA MẤT (dùng khi muốn reset DB)
```

## Lệnh Docker thường dùng

### Khởi động
```bash
# Mở Docker Desktop trước (Mac: open -a Docker)

# Chạy SQL Server + Redis (background)
docker-compose up sqlserver redis -d

# Chạy TẤT CẢ services
docker-compose up -d

# Chạy + build lại image (khi code thay đổi)
docker-compose up --build -d
```

### Xem trạng thái
```bash
# Danh sách container đang chạy
docker ps

# Kết quả:
# NAMES                    STATUS          PORTS
# cinex-sqlserver-1   Up 5 minutes    0.0.0.0:1433->1433/tcp
# cinex-redis-1       Up 5 minutes    0.0.0.0:6379->6379/tcp

# Xem log của 1 container
docker logs cinex-sqlserver-1

# Xem log realtime (follow)
docker logs -f cinex-sqlserver-1
```

### Dừng / Xóa
```bash
# Dừng tất cả container
docker-compose down

# Dừng + xóa volume (RESET DB — mất hết data)
docker-compose down -v

# Xóa 1 container cụ thể
docker rm -f cinex-sqlserver-1
```

### Chạy lệnh trong container
```bash
# Mở shell trong container SQL Server
docker exec -it cinex-sqlserver-1 bash

# Chạy SQL command
docker exec cinex-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'CineX@2026' -C \
  -Q "SELECT name FROM sys.databases"

# Tạo database
docker exec cinex-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'CineX@2026' -C \
  -Q "CREATE DATABASE cinex"
```

## Luồng hoạt động từ A đến Z

```
Bước 1: Cài Docker Desktop (1 lần duy nhất)
    ↓
Bước 2: Clone code từ git
    git clone https://github.com/xxx/cinex.git
    ↓
Bước 3: Chạy Docker Compose
    cd cinex
    docker-compose up sqlserver redis -d
    ↓
    Docker tự động:
    ├── Download image SQL Server (lần đầu ~700MB, lần sau dùng cache)
    ├── Download image Redis (lần đầu ~30MB)
    ├── Tạo container sqlserver → chạy ở port 1433
    ├── Tạo container redis → chạy ở port 6379
    └── Tạo volume sqlserver-data
    ↓
Bước 4: Tạo database (lần đầu)
    docker exec cinex-sqlserver-1 ... "CREATE DATABASE cinex"
    ↓
Bước 5: Chạy backend
    cd backend && ./gradlew bootRun
    ↓
    Liquibase tự tạo bảng trong cinex
    ↓
Bước 6: Xong! Test API tại http://localhost:8088/api/health
```

## Khi nào KHÔNG cần Docker?

Nếu máy bạn đã cài SQL Server + Redis sẵn → không cần Docker, chỉ cần:
1. Tạo database `cinex` trong SQL Server
2. Sửa password trong `application-dev.yml` cho khớp
3. `./gradlew bootRun`

Docker chỉ là **tiện lợi**, không bắt buộc.

