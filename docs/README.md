# Tài liệu dự án CineX

Mở file bất kỳ bên dưới để đọc. Mỗi thư mục là 1 chủ đề, nhìn tên là biết nội dung.

---

## Tech Stack tổng hợp

### Backend

| Công nghệ | Version | Mục đích |
|---|---|---|
| Java | 21 | Ngôn ngữ chính |
| Spring Boot | 3.3.5 | Framework chính |
| Spring Security | (BOM) | Xác thực & phân quyền (JWT stateless) |
| Spring Data JPA | (BOM) | ORM — Hibernate + Repository pattern |
| Spring Data Redis | (BOM) | Cache, lưu refresh token |
| Hibernate | (BOM) | JPA implementation, sinh SQL tự động |
| SQL Server | 2022 | Database chính (chạy Docker) |
| Redis | 7 | Cache layer |
| Liquibase | (BOM) | Migration schema DB (version control cho DB) |
| JJWT | 0.12.6 | Tạo/parse/validate JWT token |
| MapStruct | 1.6.3 | Tự sinh code chuyển Entity <-> DTO |
| Lombok | (BOM) | Giảm boilerplate (@Getter, @Builder, @Slf4j...) |
| Spring Mail | (BOM) | Gửi email xác nhận vé, reset password |
| Cloudinary | 2.0.0 | Upload + quản lý ảnh (poster, avatar) trên cloud |
| ZXing | 3.5.3 | Sinh QR code cho vé điện tử |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI — test API trên trình duyệt |
| Gradle | wrapper | Build tool |
| Testcontainers | 1.20.4 | Chạy SQL Server thật trong Docker khi test |
| JUnit 5 + Mockito | (BOM) | Unit test + mock |

### Frontend

| Công nghệ | Version | Mục đích |
|---|---|---|
| React | 19.2 | UI framework |
| TypeScript | 6.0 | Type safety |
| Vite | 8.0 | Build tool + dev server (port 5173) |
| Tailwind CSS | 4.2 | Utility-first CSS framework |
| React Router | 7.15 | Client-side routing (BrowserRouter) |
| TanStack Query | 5.x | Server state management, caching, async data |
| Zustand | 5.x | Client state (auth store, localStorage sync) |
| Axios | 1.16 | HTTP client + JWT interceptor |
| React Hook Form | 7.75 | Form management |
| Zod | 4.4 | Schema validation (kết hợp với form) |
| @hookform/resolvers | 5.2 | Kết nối React Hook Form + Zod |
| Sonner | 2.0 | Toast notification (thông báo popup) |
| class-variance-authority | 0.7 | Tạo variant cho component (button sizes, colors) |
| clsx + tailwind-merge | — | Merge className thông minh → hàm `cn()` |
| Lucide React | 1.16 | Icon library (SVG icons) |
| react-qr-code | latest | Render QR code từ bookingCode trên màn hình |
| Recharts | latest | Biểu đồ thống kê (Admin Dashboard) |
| ESLint + Prettier | — | Lint + format code tự động |

### UI Components (Custom shadcn-style)

Không dùng Radix UI. Components viết tay bằng HTML native + Tailwind + cva:

| Component | Mô tả |
|---|---|
| Button | Nhiều variant (default/destructive/outline/ghost/link) + sizes + loading |
| Input, Label, Textarea, Select | Form elements native + Tailwind styling |
| Card | Container với header/content/footer |
| Dialog | Modal popup (custom, không phải Radix) |
| Table | Bảng dữ liệu responsive |
| Badge | Label nhỏ (status, tag) |
| Skeleton | Loading placeholder animated |
| Avatar | Ảnh user / fallback initials |
| Tabs | Tab navigation |
| Separator | Đường kẻ phân cách |
| Toaster (Sonner) | Toast notification container |

### Infrastructure

| Công cụ | Mục đích |
|---|---|
| Docker + Docker Compose | Chạy SQL Server, Redis, build production |
| Nginx | Serve frontend static files + proxy API (production) |
| GitHub Actions | CI/CD — build + lint + test tự động |

---

## 📁 project/ — Tổng quan dự án (đọc đầu tiên)

| File | Nội dung |
|---|---|
| [setup.md](project/setup.md) | Hướng dẫn cài đặt, chạy dự án từ A-Z |
| [architecture.md](project/architecture.md) | Kiến trúc Layered Architecture, cấu trúc package |
| [business-flow.md](project/business-flow.md) | **Luồng nghiệp vụ** — User đặt vé thế nào, Admin quản lý gì, danh sách API đầy đủ |
| [erd.md](project/erd.md) | Sơ đồ ERD, chi tiết từng bảng, quan hệ |
| [erd.drawio](project/erd.drawio) | ERD dạng đồ họa (mở bằng https://app.diagrams.net) |
| [git-guide.md](project/git-guide.md) | Git & GitHub — lệnh cơ bản, branch, commit, workflow nhóm |

---

## 📁 design-patterns/ — Mẫu thiết kế

| File | Nội dung |
|---|---|
| [01-creational-patterns.md](design-patterns/01-creational-patterns.md) | **Builder**, **Factory**, **Singleton** — Tạo đối tượng |
| [02-structural-patterns.md](design-patterns/02-structural-patterns.md) | **DTO**, **Repository**, **Mapper** (MapStruct), **Facade** — Cấu trúc code |
| [03-behavioral-patterns.md](design-patterns/03-behavioral-patterns.md) | **Template Method**, **Strategy**, **Observer**, **State**, **Filter**, **Specification**, **Enum** — Hành vi |
| [04-solid-principles.md](design-patterns/04-solid-principles.md) | **SOLID** (5 nguyên tắc), **DRY**, **KISS** |

---

## 📁 backend/ — Kiến thức Backend (Spring Boot + Java)

| File | Nội dung |
|---|---|
| [00-java-spring-fundamentals.md](backend/00-java-spring-fundamentals.md) | **NỀN TẢNG:** Java chạy thế nào, JVM, JAR, Classpath, Gradle, Spring Container, IoC, DI sâu, Auto-Config, Bean Lifecycle |
| [01-spring-boot-basics.md](backend/01-spring-boot-basics.md) | Annotation, Bean, DI, @Value, Profile, @Transactional, REST API convention |
| [02-jpa-hibernate.md](backend/02-jpa-hibernate.md) | Entity, @Column, quan hệ, Cascade, Repository query method, Entity Lifecycle |
| [03-security.md](backend/03-security.md) | JWT, BCrypt, CORS, SecurityFilterChain, Validation (@Valid), Exception Handling |
| [04-spring-features.md](backend/04-spring-features.md) | AOP (Audit Log), @Scheduled (job tự động), Spring Events (Observer), Cache-aside |
| [05-lombok.md](backend/05-lombok.md) | @Getter, @Setter, @Builder, @RequiredArgsConstructor, @Slf4j — giảm boilerplate |
| [06-swagger.md](backend/06-swagger.md) | Swagger UI test API trên trình duyệt, @Tag, @Operation |
| [07-gradle.md](backend/07-gradle.md) | build.gradle đọc thế nào, lệnh gradlew, thêm dependency |
| [08-redis.md](backend/08-redis.md) | Redis cache, config, cách dùng, khi nào dùng |
| [09-email-cloudinary-qr.md](backend/09-email-cloudinary-qr.md) | Email (Spring Mail), Upload ảnh (Cloudinary), QR Code (ZXing + react-qr-code), Recharts |

---

## 📁 database/ — Database

| File | Nội dung |
|---|---|
| [01-database-techniques.md](database/01-database-techniques.md) | Optimistic/Pessimistic Lock, ACID, Soft Delete, N+1 Problem, Lazy/Eager, Pagination |
| [02-liquibase-guide.md](database/02-liquibase-guide.md) | Liquibase từ zero — tạo bảng, thêm cột, insert data mẫu (Seed Data), quy tắc |
| [03-id-tracker.md](database/03-id-tracker.md) | IdTracker sinh mã code tự động (VC-20260512-001) |

---

## 📁 docker/ — Docker

| File | Nội dung |
|---|---|
| [01-docker-guide.md](docker/01-docker-guide.md) | Docker từ zero — Image, Container, Volume, Dockerfile, docker-compose, lệnh thường dùng |

---

## 📁 frontend/ — Kiến thức Frontend (React + TypeScript)

| File | Nội dung |
|---|---|
| [00-typescript-basics.md](frontend/00-typescript-basics.md) | TypeScript — kiểu dữ liệu, Interface, Type, Generic, định nghĩa types cho dự án |
| [01-react-basics.md](frontend/01-react-basics.md) | Component, Props, JSX, useState, useEffect, Event, List/Conditional rendering |
| [02-react-router.md](frontend/02-react-router.md) | Routing, Layout, URL params, ProtectedRoute, Navigate, Link |
| [03-tanstack-query.md](frontend/03-tanstack-query.md) | useQuery (GET), useMutation (POST/PUT/DELETE), Custom hooks, Cache |
| [04-zustand-state.md](frontend/04-zustand-state.md) | Auth store, Seat selection store, khi nào dùng Zustand vs Query |
| [05-tailwind-css.md](frontend/05-tailwind-css.md) | Utility CSS, Layout, Spacing, Colors, Responsive, ví dụ MovieCard + SeatMap |
| [06-form-validation.md](frontend/06-form-validation.md) | react-hook-form + Zod, form đăng ký mẫu |
| [07-axios-api.md](frontend/07-axios-api.md) | Axios instance, JWT interceptor, cách gọi API, xử lý response/error |
| [08-shadcn-ui.md](frontend/08-shadcn-ui.md) | UI Components custom (shadcn-style), cva, cn(), Sonner toast |
| [09-project-config-files.md](frontend/09-project-config-files.md) | **package.json, lock file, tsconfig, vite, eslint, prettier** — giải thích A-Z từng file |
| [10-how-frontend-works.md](frontend/10-how-frontend-works.md) | **NEN TANG:** SPA, Virtual DOM, Component, State, Props, Routing, Axios, Query, Zustand, Tailwind, Build |

---

## 📁 module-guides/ — Giải thích từng module (tạo sau khi code xong)

| File | Nội dung |
|---|---|
| [auth-explained.md](module-guides/auth-explained.md) | Module Auth: register/login/refresh, JWT, BCrypt |
| movie-explained.md | _(sẽ tạo khi xong task 005)_ |
| booking-explained.md | _(sẽ tạo khi xong task 009)_ |
| payment-explained.md | _(sẽ tạo khi xong task 010)_ |

---

## Thứ tự đọc gợi ý

1. `project/business-flow.md` — Hiểu dự án làm gì
2. `project/setup.md` — Cài đặt chạy thử
3. `project/erd.md` — Hiểu database
4. `design-patterns/` — Hiểu các pattern sẽ dùng
5. `backend/` hoặc `frontend/` — Tùy bạn làm BE hay FE
6. `docker/` — Khi cần chạy SQL Server
7. `database/` — Khi cần tạo bảng mới
