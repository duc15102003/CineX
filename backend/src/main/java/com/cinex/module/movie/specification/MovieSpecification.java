package com.cinex.module.movie.specification;

import com.cinex.module.movie.dto.MovieFilter;
import com.cinex.module.movie.entity.Movie;
import com.cinex.module.movie.entity.MovieStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * [Specification Pattern] Build query WHERE động cho Movie.
 *
 * fromFilter(MovieFilter) nhận Filter DTO → ghép điều kiện tự động.
 * Thêm filter mới = thêm field vào DTO + thêm if trong fromFilter.
 */
public class MovieSpecification {

    private MovieSpecification() {}

    /**
     * Entry point: chuyển Filter DTO → Specification.
     * Tất cả module dùng cùng pattern này.
     */
    public static Specification<Movie> fromFilter(MovieFilter filter) {
        Specification<Movie> spec = Specification.where(null);

        if (!Boolean.TRUE.equals(filter.getIncludeDeleted())) {
            spec = spec.and(notDeleted());
        }
        if (StringUtils.hasText(filter.getKeyword())) {
            spec = spec.and(hasTitle(filter.getKeyword()));
        }
        if (filter.getStatus() != null) {
            spec = spec.and(hasStatus(filter.getStatus()));
        }
        if (filter.getGenreId() != null) {
            spec = spec.and(hasGenre(filter.getGenreId()));
        }
        return spec;
    }

    public static Specification<Movie> hasTitle(String keyword) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Movie> hasStatus(MovieStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Movie> hasGenre(Long genreId) {
        return (root, query, cb) -> {
            var genreJoin = root.join("genres", JoinType.LEFT);
            return cb.equal(genreJoin.get("id"), genreId);
        };
    }

    public static Specification<Movie> notDeleted() {
        return (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("storageState")),
                        cb.notEqual(root.get("storageState"), "DELETED")
                );
    }
}
