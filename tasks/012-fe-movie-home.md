# Task: FE Trang chủ + Phim — Hiển thị phim, tìm kiếm, chi tiết

## Status: PENDING

## Module
frontend

## Mô tả
Trang chủ hiển thị phim đang chiếu/sắp chiếu. Trang danh sách phim có tìm kiếm + lọc thể loại. Trang chi tiết phim có trailer, mô tả, danh sách suất chiếu.

**Kết quả mong đợi:**
- Trang chủ `/` — banner phim hot, grid phim đang chiếu, phim sắp chiếu
- Danh sách phim `/movies` — grid phim, ô tìm kiếm, lọc theo thể loại
- Chi tiết phim `/movies/:id` — poster, trailer, mô tả, chọn ngày → danh sách suất chiếu

## Việc cần làm

### Trang chủ
- [ ] `HomePage.tsx` — hero banner (phim nổi bật), section "Đang chiếu", section "Sắp chiếu"
- [ ] Component `MovieCard.tsx` — card hiển thị phim (poster, tên, thể loại, rating, thời lượng)
- [ ] Component `MovieGrid.tsx` — grid responsive chứa nhiều MovieCard

### Danh sách phim
- [ ] `MovieListPage.tsx` — ô tìm kiếm, dropdown lọc thể loại, grid phim, phân trang
- [ ] Component `SearchBar.tsx` — input tìm kiếm (debounce 300ms)
- [ ] Component `GenreFilter.tsx` — dropdown/chip chọn thể loại

### Chi tiết phim
- [ ] `MovieDetailPage.tsx` — poster lớn, trailer (iframe YouTube), thông tin chi tiết
- [ ] Component `ShowtimeSelector.tsx` — chọn ngày (tab ngày) → hiện danh sách suất chiếu
- [ ] Component `ShowtimeCard.tsx` — giờ chiếu, phòng, giá vé, nút "Đặt vé" → navigate sang trang chọn ghế

### API Hooks
- [ ] `useMovies(params)` — useQuery lấy danh sách phim (search, genre, status, page)
- [ ] `useMovie(id)` — useQuery lấy chi tiết 1 phim
- [ ] `useGenres()` — useQuery lấy danh sách thể loại
- [ ] `useShowtimes(movieId, date)` — useQuery lấy suất chiếu theo phim + ngày

## Tiêu chí hoàn thành (Definition of Done)
- [ ] `npm run build` pass
- [ ] Trang chủ hiện phim đang chiếu + sắp chiếu
- [ ] Tìm kiếm phim theo tên hoạt động (debounce)
- [ ] Lọc phim theo thể loại hoạt động
- [ ] Chi tiết phim hiện trailer + danh sách suất chiếu theo ngày
- [ ] Click "Đặt vé" → navigate sang trang chọn ghế

## Sau khi hoàn thành (BẮT BUỘC)
- [ ] Giải thích từng file, tác dụng
- [ ] Viết `/docs/fe-movie-explained.md`
- [ ] Đổi Status → DONE, tick [x], move sang `done/`
