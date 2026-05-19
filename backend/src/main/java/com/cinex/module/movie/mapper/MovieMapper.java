package com.cinex.module.movie.mapper;

import com.cinex.module.movie.dto.MovieListResponse;
import com.cinex.module.movie.dto.MovieResponse;
import com.cinex.module.movie.entity.Genre;
import com.cinex.module.movie.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * [Mapper Pattern — MapStruct] Chuyển Movie entity ↔ DTO.
 *
 * Khó khăn với N:N:
 * - Entity Movie.genres = Set<Genre> (object đầy đủ)
 * - MovieResponse.genres = Set<GenreResponse> (DTO)
 * - MovieListResponse.genres = Set<String> (chỉ tên)
 *
 * MapStruct cần chỉ dẫn cách chuyển bằng @Mapping + @Named.
 */
@Mapper(componentModel = "spring", uses = GenreMapper.class)
public interface MovieMapper {

    /**
     * Movie → MovieResponse (chi tiết).
     * genres: Set<Genre> → Set<GenreResponse> — MapStruct tự dùng GenreMapper.toResponse()
     * vì đã khai báo uses = GenreMapper.class
     */
    MovieResponse toResponse(Movie movie);

    /**
     * Movie → MovieListResponse (rút gọn).
     * genres: Set<Genre> → Set<String> — cần custom mapping vì kiểu khác nhau.
     *
     * @Mapping(source = "genres", target = "genres", qualifiedByName = "genreNames")
     * → Khi map field "genres", gọi method có @Named("genreNames") để chuyển đổi
     */
    @Mapping(source = "genres", target = "genres", qualifiedByName = "genreNames")
    MovieListResponse toListResponse(Movie movie);

    /**
     * Custom mapping: Set<Genre> → Set<String> (chỉ lấy tên).
     * MapStruct không tự biết cách chuyển Genre → String,
     * nên mình phải viết method này + đánh dấu @Named.
     */
    @Named("genreNames")
    default Set<String> mapGenreNames(Set<Genre> genres) {
        if (genres == null) return Set.of();
        return genres.stream()
                .map(Genre::getName)
                .collect(Collectors.toSet());
    }
}
