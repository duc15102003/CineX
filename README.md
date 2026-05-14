# CineX

Hệ thống đặt vé xem phim online - Đồ án tốt nghiệp.

## Tech Stack

**Backend:** Java 21, Spring Boot 3.3, Gradle, Spring Security + JWT, JPA, Liquibase, Redis, MapStruct, Swagger (springdoc-openapi)

**Frontend:** React 18, TypeScript, Vite, Tailwind CSS, TanStack Query, Zustand, React Hook Form + Zod

**Database:** SQL Server 2022

**Infra:** Docker Compose, GitHub Actions CI

## Chạy nhanh

### Chạy từng phần

```bash
# Backend
cd backend && ./gradlew bootRun

# Frontend
cd frontend && npm install && npm run dev
```

### Chạy full stack với Docker

```bash
docker-compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8088
- Swagger UI: http://localhost:8088/swagger-ui/index.html
- Health check: http://localhost:8088/api/health

## Tài liệu

- [Hướng dẫn setup chi tiết](docs/setup.md)
- [Kiến trúc hệ thống](docs/architecture.md)
