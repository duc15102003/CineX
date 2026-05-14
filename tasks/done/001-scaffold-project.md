# Task: Dung khung chuan chinh du an Van-Cinema

## Status: DONE

## Module
fullstack

## Mo ta
Khoi tao bo khung chuan cho do an tot nghiep Van-Cinema (he thong dat ve xem phim online).
Task nay CHI dung skeleton (khung suon) - KHONG lam module nghiep vu (movie/booking/payment).
Cac module nghiep vu se duoc tach thanh task rieng o cac task sau.

**Muc tieu hoc tap:** Lam quen voi Spring Boot + Gradle, JPA, Liquibase, Spring Security/JWT,
Redis cache, MapStruct, Swagger, Docker, GitHub Actions CI, va React TS + Vite + Tailwind.

**Ket qua mong doi:**
- Backend chay duoc tai `http://localhost:8080`, healthcheck OK, Swagger UI mo duoc
- Frontend chay duoc tai `http://localhost:5173`, hien trang Home
- `docker-compose up` chay duoc full stack (BE + FE + SQL Server + Redis)
- File `docs/setup.md` ghi chi tiet cach setup tu A-Z

---

## Tech Stack chot

### Backend
- Java 21, Spring Boot 3.3.x, Gradle (Groovy DSL)
- spring-boot-starter-web, data-jpa, security, validation, data-redis
- mssql-jdbc (SQL Server driver)
- liquibase-core
- jjwt-api / jjwt-impl / jjwt-jackson
- mapstruct + lombok
- springdoc-openapi-starter-webmvc-ui (Swagger UI v2)
- Test: JUnit 5, Mockito, Testcontainers (mssqlserver module)

### Frontend
- Node 20, Vite, React 18, TypeScript
- react-router-dom v6
- axios + JWT interceptor
- @tanstack/react-query
- zustand
- tailwindcss + shadcn/ui (cai dat sau khi co Tailwind)
- react-hook-form + zod
- ESLint + Prettier

### Infra
- Docker Compose: SQL Server 2022 + Redis 7 + BE + FE
- GitHub Actions: lint + build + test

---

## Cau truc thu muc dich

```
Van-Cinema/
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradle/wrapper/
│   ├── gradlew, gradlew.bat
│   └── src/main/java/com/cinex/
│       ├── CineXApplication.java
│       ├── common/
│       │   ├── exception/         (GlobalExceptionHandler, BusinessException, ErrorCode)
│       │   ├── response/          (ApiResponse<T>, PageResponse<T>)
│       │   ├── config/            (SecurityConfig, RedisConfig, OpenApiConfig, CorsConfig)
│       │   └── util/
│       ├── security/              (JwtUtil, JwtAuthFilter, CustomUserDetailsService)
│       └── module/
│           └── health/            (HealthController de test)
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── db/changelog/
│           ├── db.changelog-master.xml
│           └── changes/001-create-users-table.xml
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── api/axios.ts
│       ├── components/
│       ├── features/
│       ├── hooks/
│       ├── store/
│       ├── routes/
│       ├── types/
│       └── utils/
├── docker-compose.yml
├── .github/workflows/ci.yml
├── docs/
│   ├── setup.md
│   └── architecture.md
├── .gitignore
└── README.md
```

---

## Viec can lam

### A. Backend skeleton
- [x] Tao thu muc `backend/`, init Gradle project (`gradle init --type java-application`)
- [x] Cau hinh `build.gradle` voi day du dependency da liet ke
- [x] Tao package `com.cinex` va class `CineXApplication.java` voi `@SpringBootApplication`
- [x] Tao `common/response/ApiResponse.java` (generic wrapper: success, message, data, timestamp)
- [x] Tao `common/response/PageResponse.java` (cho phan trang)
- [x] Tao `common/exception/ErrorCode.java` (enum cac ma loi he thong)
- [x] Tao `common/exception/BusinessException.java` (extend RuntimeException)
- [x] Tao `common/exception/GlobalExceptionHandler.java` voi `@RestControllerAdvice`
- [x] Tao `common/config/CorsConfig.java` (cho phep `http://localhost:5173`)
- [x] Tao `common/config/OpenApiConfig.java` (cau hinh Swagger title, version, JWT security scheme)
- [x] Tao `common/config/RedisConfig.java` (RedisTemplate + StringRedisSerializer)
- [x] Tao `security/JwtUtil.java` (generate, parse, validate token)
- [x] Tao `security/JwtAuthFilter.java` (extend OncePerRequestFilter)
- [x] Tao `common/config/SecurityConfig.java` (SecurityFilterChain, PasswordEncoder bean)
- [x] Tao `module/health/HealthController.java` voi GET `/api/health` tra ve `ApiResponse.ok("UP")`
- [x] Tao `application.yml` co `spring.profiles.active`, cau hinh server.port=8080
- [x] Tao `application-dev.yml` co datasource SQL Server, redis, jwt secret (doc tu env var)
- [x] Tao `db/changelog/db.changelog-master.xml` include cac file con
- [x] Tao `db/changelog/changes/001-create-users-table.xml` (cot: id, username, email, password, role, created_at, updated_at)
- [x] Build thu: `./gradlew clean build` -> pass
- [ ] Run: `./gradlew bootRun` -> truy cap `http://localhost:8080/api/health` -> JSON OK
- [ ] Mo `http://localhost:8080/swagger-ui/index.html` -> hien Swagger UI

### B. Frontend skeleton
- [x] Tao `frontend/` voi `npm create vite@latest . -- --template react-ts`
- [x] Cai dat: `react-router-dom`, `axios`, `@tanstack/react-query`, `zustand`, `react-hook-form`, `zod`
- [x] Cai Tailwind: `npm install -D tailwindcss @tailwindcss/vite`
- [x] Cau hinh Tailwind v4 voi `@tailwindcss/vite` plugin
- [x] Them `@import 'tailwindcss'` vao `src/index.css`
- [x] Tao `src/api/axios.ts` (instance voi baseURL tu env, interceptor gan JWT tu localStorage)
- [x] Tao `src/routes/AppRouter.tsx` voi React Router (route `/` hien Home)
- [x] Tao `src/features/home/HomePage.tsx` (function component, hello UI co Tailwind)
- [x] Wrap `App.tsx` voi `QueryClientProvider`
- [x] Tao `.env.development` co `VITE_API_BASE_URL=http://localhost:8080`
- [x] Setup ESLint + Prettier (config chuan)
- [x] Build: `npm run build` -> pass

### C. Docker Compose
- [x] Tao `docker-compose.yml` voi 4 service:
  - `sqlserver`: image `mcr.microsoft.com/mssql/server:2022-latest`, env `SA_PASSWORD`, volume persist
  - `redis`: image `redis:7-alpine`
  - `backend`: build tu `./backend`, depends_on sqlserver+redis, expose 8080
  - `frontend`: build tu `./frontend`, expose 5173
- [x] Tao `backend/Dockerfile` (multi-stage: build voi gradle, run voi eclipse-temurin:21-jre)
- [x] Tao `frontend/Dockerfile` (multi-stage: build voi node, serve voi nginx)
- [ ] Test: `docker-compose up --build` -> tat ca service chay OK
- [ ] Test cross: BE ket noi duoc SQL Server va Redis trong network docker

### D. CI/CD
- [x] Tao `.github/workflows/ci.yml` voi 2 job:
  - `backend-ci`: setup Java 21, run `./gradlew build` (bao gom test)
  - `frontend-ci`: setup Node 20, run `npm ci && npm run lint && npm run build`
- [x] Trigger: push hoac PR vao `main`

### E. Git & Docs
- [x] Tao `.gitignore` (gop cho ca BE Java/Gradle va FE Node/Vite va IDE)
- [x] `git init` tai `/Users/vutuongan/Van-Cinema/`
- [x] Tao `README.md` co: gioi thieu du an, tech stack, cach run nhanh
- [ ] Commit dau tien: "chore: initial project scaffold"

---

## Tieu chi hoan thanh (Definition of Done)
- [ ] `cd backend && ./gradlew bootRun` -> `/api/health` tra ve OK
- [x] `cd frontend && npm run dev` -> `localhost:5173` hien Home
- [ ] `docker-compose up --build` -> ca 4 service len OK
- [ ] Swagger UI truy cap duoc
- [ ] Liquibase auto-tao bang `users` khi BE start lan dau
- [ ] CI pass tren GitHub
- [x] File `/docs/setup.md` da viet day du

---

## Tham khao
- Spring Boot 3.x docs: https://docs.spring.io/spring-boot/index.html
- Liquibase XML changelog: https://docs.liquibase.com/concepts/changelogs/xml-format.html
- SpringDoc OpenAPI v2: https://springdoc.org/v2/
- Vite + React TS: https://vitejs.dev/guide/
- Testcontainers MSSQL: https://java.testcontainers.org/modules/databases/mssqlserver/

## Ghi chu
- SA_PASSWORD cho SQL Server phai du complexity (>=8 ky tu, co hoa/thuong/so/ky tu dac biet)
- JWT secret nen dat trong env var, KHONG hardcode trong yml
- Lan dau Liquibase chay se tao bang `DATABASECHANGELOG` va `DATABASECHANGELOGLOCK` tu dong
- Khi gap loi bien dich MapStruct, kiem tra annotationProcessor da khai bao trong build.gradle

---

## Sau khi hoan thanh (BAT BUOC)
- [x] **Giai thich da lam gi:** Liet ke tung file/cau hinh da tao + tac dung cua no (vi du: tai sao can `JwtAuthFilter`, `ApiResponse<T>` giup gi, Liquibase khac gi voi JPA `ddl-auto`)
- [x] **Viet `/docs/setup.md`** voi cac muc:
  1. Yeu cau he thong (Java 21, Node 20, Docker)
  2. Cach run tung phan: BE rieng, FE rieng, full stack docker-compose
  3. Cach reset DB / chay lai Liquibase
  4. Cach test API qua Swagger
  5. Cau truc thu muc va y nghia tung folder
  6. Cac bien moi truong can set
  7. Troubleshooting cac loi thuong gap
- [x] **Viet `/docs/architecture.md`** giai thich Layered Architecture va design pattern da ap dung
- [ ] Doi Status -> DONE, tick het [x], move file -> `/tasks/done/`
