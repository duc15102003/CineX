package com.cinex.module.movie.specification;

import com.cinex.module.movie.entity.Movie;
import com.cinex.module.movie.entity.MovieStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * [Specification Pattern — Behavioral] Build query WHERE động cho Movie.
 *
 * Ví dụ đời thường:
 * Bạn tìm phim trên CGV: có thể lọc theo tên, thể loại, trạng thái.
 * Có khi chỉ lọc theo tên, có khi lọc theo tên + thể loại + trạng thái.
 * → Số tổ hợp rất nhiều → không thể viết 1 method cho mỗi tổ hợp.
 *
 * Specification giải quyết: mỗi điều kiện = 1 method nhỏ, GHÉP lại tùy ý:
 *   where(hasTitle("Avengers"))
 *     .and(hasStatus(NOW_SHOWING))
 *     .and(hasGenre(1L))
 *     .and(notDeleted())
 *
 * SQL sinh ra:
 *   SELECT * FROM movies m
 *   LEFT JOIN movie_genres mg ON m.id = mg.movie_id
 *   WHERE m.title LIKE '%Avengers%'
 *     AND m.status = 'NOW_SHOWING'
 *     AND mg.genre_id = 1
 *     AND (m.storage_state IS NULL OR m.storage_state <> 'DELETED')
 */
public class MovieSpecification {

    private MovieSpecification() {}

    /**
     * Tìm phim theo tên (LIKE %keyword%).
     * Case-insensitive bằng LOWER().
     */
    public static Specification<Movie> hasTitle(String keyword) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    /**
     * Lọc theo trạng thái phim.
     */
    public static Specification<Movie> hasStatus(MovieStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    /**
     * Lọc phim thuộc thể loại (qua bảng join movie_genres).
     *
     * root.join("genres") → JPA tự sinh JOIN movie_genres + genres
     * LEFT JOIN: trả cả phim không có genre (tránh mất data)
     */
    public static Specification<Movie> hasGenre(Long genreId) {
        return (root, query, cb) -> {
            // Dùng LEFT JOIN để không bị mất phim khi kết hợp với các filter khác
            var genreJoin = root.join("genres", JoinType.LEFT);
            return cb.equal(genreJoin.get("id"), genreId);
        };
    }

    /**
     * Lọc bỏ entity đã soft delete.
     * BaseEntity.storageState = null hoặc != "DELETED"
     */
    public static Specification<Movie> notDeleted() {
        return (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("storageState")),
                        cb.notEqual(root.get("storageState"), "DELETED")
                );
    }
}
