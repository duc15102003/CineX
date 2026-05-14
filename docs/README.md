# Tài liệu dự án CineX

Mở file bất kỳ bên dưới để đọc. Mỗi thư mục là 1 chủ đề, nhìn tên là biết nội dung.

---

## 📁 project/ — Tổng quan dự án (đọc đầu tiên)

| File | Nội dung |
|---|---|
| [setup.md](project/setup.md) | Hướng dẫn cài đặt, chạy dự án từ A-Z |
| [architecture.md](project/architecture.md) | Kiến trúc Layered Architecture, cấu trúc package |
| [business-flow.md](project/business-flow.md) | **Luồng nghiệp vụ** — User đặt vé thế nào, Admin quản lý gì, danh sách API đầy đủ |
| [erd.md](project/erd.md) | Sơ đồ ERD, chi tiết từng bảng, quan hệ |
| [erd.drawio](project/erd.drawio) | ERD dạng đồ họa (mở bằng https://app.diagrams.net) |

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
| [01-spring-boot-basics.md](backend/01-spring-boot-basics.md) | Annotation, Bean, DI, @Value, Profile, @Transactional, REST API convention |
| [02-jpa-hibernate.md](backend/02-jpa-hibernate.md) | Entity, @Column, quan hệ, Cascade, Repository query method, Entity Lifecycle |
| [03-security.md](backend/03-security.md) | JWT, BCrypt, CORS, SecurityFilterChain, Validation (@Valid), Exception Handling |
| [04-spring-features.md](backend/04-spring-features.md) | AOP (Audit Log), @Scheduled (job tự động), Spring Events (Observer), Cache-aside |

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
