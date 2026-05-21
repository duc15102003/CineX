# Task: Module Review — Đánh giá phim

## Status: PENDING (⚠️ CẦN CONFIRM VỚI USER TRƯỚC KHI LÀM)

## Module
backend

## Mô tả
User đánh giá phim (1-10 sao + comment). Rating phim tự cập nhật AVG.

**Kết quả mong đợi:**
- GET `/api/movies/{movieId}/reviews` — danh sách review của phim
- POST `/api/movies/{movieId}/reviews` — tạo review (cần login)
- PUT `/api/reviews/{id}` — sửa review (chỉ chủ sở hữu)
- DELETE `/api/reviews/{id}` — xóa review (chủ sở hữu hoặc admin)

## Bảng: reviews
- user_id, movie_id, rating (1-10), comment
- UNIQUE(user_id, movie_id) — 1 user review 1 phim 1 lần
- Tạo/sửa/xóa review → cập nhật movie.rating = AVG(reviews.rating)

## Độ khó: ⭐ Dễ (~1-2h)
