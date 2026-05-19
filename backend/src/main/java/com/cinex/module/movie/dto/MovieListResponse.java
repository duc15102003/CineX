package com.cinex.module.movie.dto;

import com.cinex.module.movie.entity.MovieStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Response rút gọn cho danh sách phim — chỉ giữ field cần hiển thị trên list.
 * Dùng cho: GET /api/movies (danh sách)
 *
 * Tại sao tách riêng MovieListResponse và MovieResponse?
 * - List: hiển thị card → chỉ cần title, poster, duration, rating, genres
 * - Detail: hiển thị full → cần thêm description, director, cast, trailer, ...
 * - Trả ít field hơn = response nhẹ hơn = load nhanh hơn
 */
@Getter
@Builder
public class MovieListResponse {

    private Long id;
    private String storageState;
    private String title;
    private String posterUrl;
    private Integer duration;
    private BigDecimal rating;
    private MovieStatus status;
    private Set<String> genres; // Chỉ trả tên genre, không trả ID
}
