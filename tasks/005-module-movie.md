# Task: Module Movie — CRUD phim, thể loại, tìm kiếm

## Status: PENDING

## Module
backend

## Mô tả
Quản lý phim và thể loại phim. Admin có thể thêm/sửa/xóa phim. User có thể xem danh sách phim, tìm kiếm, xem chi tiết.

**Kết quả mong đợi:**
- GET `/api/movies` → danh sách phim (phân trang, tìm kiếm theo tên, lọc theo thể loại, trạng thái)
- GET `/api/movies/{id}` → chi tiết phim
- POST `/api/movies` → (ADMIN) thêm phim mới
- PUT `/api/movies/{id}` → (ADMIN) sửa phim
- DELETE `/api/movies/{id}` → (ADMIN) xóa phim
- GET `/api/genres` → danh sách thể loại
- POST `/api/genres` → (ADMIN) thêm thể loại

## Việc cần làm

### Entity & Liquibase
- [ ] `004-create-genres-table.xml` — bảng `genres` (id, name, description)
- [ ] `005-create-movies-table.xml` — bảng `movies` (id, title, description, duration, releaseDate, endDate, posterUrl, trailerUrl, director, cast, language, rating, status)
- [ ] `006-create-movie-genres-table.xml` — bảng liên kết `movie_genres` (movieId, genreId) — quan hệ nhiều-nhiều
- [ ] `Genre.java` entity
- [ ] `Movie.java` entity — `@ManyToMany` với Genre

### DTO
- [ ] `MovieRequest.java` — title, description, duration, releaseDate, genreIds, ... (validation)
- [ ] `MovieResponse.java` — tất cả field + danh sách genre names
- [ ] `MovieListResponse.java` — version rút gọn cho danh sách (id, title, posterUrl, duration, rating, genres)
- [ ] `GenreRequest.java` — name, description
- [ ] `GenreResponse.java` — id, name, description

### Repository
- [ ] `MovieRepository.java` — findAll(Pageable), search by title, filter by genre/status
- [ ] `GenreRepository.java` — findAll, findByName

### Service
- [ ] `MovieService.java` — CRUD + search + filter
- [ ] `GenreService.java` — list + create

### Controller
- [ ] `MovieController.java` — 5 endpoint
- [ ] `GenreController.java` — 2 endpoint

### Mapper
- [ ] `MovieMapper.java` — Movie ↔ MovieRequest/MovieResponse
- [ ] `GenreMapper.java` — Genre ↔ GenreRequest/GenreResponse

## Tiêu chí hoàn thành (Definition of Done)
- [ ] Build pass
- [ ] Admin tạo/sửa/xóa phim thành công
- [ ] User xem danh sách phim, tìm kiếm theo tên
- [ ] Lọc phim theo thể loại hoạt động
- [ ] Phim có nhiều thể loại (nhiều-nhiều)

## Design Patterns cần áp dụng (MỤC TIÊU HỌC)

### 1. Mapper Pattern (MapStruct)
- **Ở đâu:** `MovieMapper` — tự động chuyển `Movie entity ↔ MovieResponse DTO`
- **Tại sao:** Giảm boilerplate code set từng field thủ công. MapStruct sinh code lúc compile, nhanh hơn reflection
- **Học được gì:** Annotation `@Mapper`, `@Mapping`, cách xử lý quan hệ N:N khi mapping

### 2. Specification Pattern (tìm kiếm động)
- **Ở đâu:** `MovieSpecification` — build query WHERE động (tìm theo tên + lọc theo genre + lọc theo status)
- **Tại sao:** Tránh viết nhiều method: `findByTitle`, `findByTitleAndGenre`, `findByTitleAndStatus`, ... Specification cho phép ghép nhiều điều kiện linh hoạt
- **Học được gì:** JPA Criteria API, `Specification<Movie>`, cách compose filter

### 3. Enum Pattern
- **Ở đâu:** `MovieStatus` enum (COMING_SOON, NOW_SHOWING, ENDED)
- **Tại sao:** Thay vì dùng String "coming_soon" dễ sai chính tả → dùng enum compile-time safe
- **Học được gì:** Java enum, `@Enumerated(EnumType.STRING)` trong JPA

## Tham khảo
- Trạng thái phim: `COMING_SOON`, `NOW_SHOWING`, `ENDED`
- `docs/erd.md` — sơ đồ bảng movies, genres, movie_genres

## Ghi chú
- Poster/trailer lưu URL (upload file sẽ làm sau hoặc dùng link ngoài)
- duration tính bằng phút (VD: 120 = 2 tiếng)
- rating: điểm đánh giá (0-10), có thể cập nhật sau

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] **Giải thích từng file** đã tạo: tác dụng, design pattern nào
- [ ] **Viết `/docs/movie-explained.md`** bao gồm:
  1. Quan hệ N:N (movie ↔ genre) hoạt động thế nào trong JPA
  2. MapStruct mapping có quan hệ N:N: cách lấy genre names từ genre entities
  3. Specification Pattern: cách build query động, so sánh với viết nhiều method repository
  4. Tại sao dùng Enum cho status thay vì String
- [ ] Cập nhật docs nếu cần
- [ ] Đổi Status từ IN_PROGRESS sang DONE
- [ ] Tick tất cả checkbox [x]
- [ ] Move file này sang `/tasks/done/`
