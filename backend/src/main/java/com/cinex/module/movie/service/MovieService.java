package com.cinex.module.movie.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.common.response.PageResponse;
import com.cinex.common.service.FileUploadService;
import com.cinex.module.movie.dto.MovieListResponse;
import com.cinex.module.movie.dto.MovieRequest;
import com.cinex.module.movie.dto.MovieResponse;
import com.cinex.module.movie.entity.Genre;
import com.cinex.module.movie.entity.Movie;
import com.cinex.module.movie.entity.MovieStatus;
import com.cinex.module.movie.mapper.MovieMapper;
import com.cinex.module.movie.repository.GenreRepository;
import com.cinex.module.movie.repository.MovieRepository;
import com.cinex.module.movie.specification.MovieSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapper movieMapper;
    private final FileUploadService fileUploadService;

    /**
     * Danh sách phim có phân trang + tìm kiếm + lọc.
     *
     * [Specification Pattern] Ghép điều kiện WHERE động:
     * - keyword → LIKE %keyword% trên title
     * - status → WHERE status = ?
     * - genreId → JOIN movie_genres WHERE genre_id = ?
     * - Luôn filter soft delete
     *
     * Client gọi: GET /api/movies?keyword=Avengers&status=NOW_SHOWING&genreId=1&page=0&size=10
     */
    @Transactional(readOnly = true)
    public PageResponse<MovieListResponse> listMovies(String keyword, MovieStatus status,
                                                       Long genreId, Pageable pageable) {
        // Bắt đầu với filter soft delete (luôn có)
        Specification<Movie> spec = MovieSpecification.notDeleted();

        // Ghép thêm điều kiện nếu client truyền
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(MovieSpecification.hasTitle(keyword));
        }
        if (status != null) {
            spec = spec.and(MovieSpecification.hasStatus(status));
        }
        if (genreId != null) {
            spec = spec.and(MovieSpecification.hasGenre(genreId));
        }

        Page<MovieListResponse> page = movieRepository.findAll(spec, pageable)
                .map(movieMapper::toListResponse);
        return PageResponse.from(page);
    }

    /**
     * Chi tiết phim — trả đầy đủ tất cả field + genres.
     */
    @Transactional(readOnly = true)
    public MovieResponse getMovie(Long id) {
        Movie movie = findMovieById(id);
        return movieMapper.toResponse(movie);
    }

    /**
     * (ADMIN) Tạo phim mới.
     * Client gửi genreIds = [1, 3, 5] → Service query Genre entities → set vào Movie.genres
     */
    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .releaseDate(request.getReleaseDate())
                .endDate(request.getEndDate())
                .trailerUrl(request.getTrailerUrl())
                .director(request.getDirector())
                .cast(request.getCast())
                .language(request.getLanguage())
                .rating(request.getRating())
                .status(request.getStatus())
                .genres(resolveGenres(request.getGenreIds()))
                .build();

        movieRepository.save(movie);
        log.info("Created movie: {}", movie.getTitle());
        return movieMapper.toResponse(movie);
    }

    /**
     * (ADMIN) Sửa phim.
     */
    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = findMovieById(id);

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setEndDate(request.getEndDate());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setLanguage(request.getLanguage());
        movie.setRating(request.getRating());
        movie.setStatus(request.getStatus());
        movie.setGenres(resolveGenres(request.getGenreIds()));

        movieRepository.save(movie);
        log.info("Updated movie: {}", movie.getTitle());
        return movieMapper.toResponse(movie);
    }

    /**
     * (ADMIN) Xóa mềm phim — set storageState = "DELETED".
     */
    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = findMovieById(id);
        movie.setStorageState("DELETED");
        movieRepository.save(movie);
        log.info("Soft deleted movie: {}", movie.getTitle());
    }

    /**
     * (ADMIN) Upload poster phim lên Cloudinary.
     * Nếu phim đã có poster cũ → xóa ảnh cũ trên Cloudinary trước khi upload mới.
     */
    @Transactional
    public MovieResponse uploadPoster(Long id, MultipartFile file) {
        Movie movie = findMovieById(id);

        // Xóa poster cũ nếu có
        if (movie.getPosterUrl() != null) {
            fileUploadService.deleteImage(movie.getPosterUrl());
        }

        String posterUrl = fileUploadService.uploadImage(file, "cinex/posters");
        movie.setPosterUrl(posterUrl);
        movieRepository.save(movie);
        log.info("Uploaded poster for movie: {}", movie.getTitle());
        return movieMapper.toResponse(movie);
    }

    /**
     * Chuyển Set<Long> genreIds → Set<Genre> entities.
     * Nếu genreId không tồn tại → throw GENRE_NOT_FOUND.
     */
    private Set<Genre> resolveGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(genreIds));
        if (genres.size() != genreIds.size()) {
            throw new BusinessException(ErrorCode.GENRE_NOT_FOUND,
                    "One or more genres not found");
        }
        return genres;
    }

    private Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .filter(m -> !"DELETED".equals(m.getStorageState()))
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
    }
}
