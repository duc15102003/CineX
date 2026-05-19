package com.cinex.module.movie.repository;

import com.cinex.module.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * [Specification Pattern] extends JpaSpecificationExecutor để hỗ trợ query động.
 *
 * JpaRepository: CRUD cơ bản (findAll, findById, save, delete)
 * JpaSpecificationExecutor: thêm findAll(Specification) — build WHERE clause động
 *
 * Tại sao cần Specification thay vì viết nhiều method?
 * Nếu không dùng Specification, phải viết:
 *   findByTitle(String title)
 *   findByStatus(MovieStatus status)
 *   findByTitleAndStatus(String title, MovieStatus status)
 *   findByTitleAndStatusAndGenresId(String title, MovieStatus status, Long genreId)
 *   ... → SỐ LƯỢNG METHOD TĂNG THEO CẤP SỐ NHÂN khi thêm filter mới
 *
 * Specification cho phép GHÉP nhiều điều kiện linh hoạt:
 *   Specification.where(hasTitle("Avengers")).and(hasStatus(NOW_SHOWING)).and(hasGenre(1L))
 */
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
}
