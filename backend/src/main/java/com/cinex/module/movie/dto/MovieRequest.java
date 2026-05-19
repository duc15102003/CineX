package com.cinex.module.movie.dto;

import com.cinex.module.movie.entity.MovieStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class MovieRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    private LocalDate releaseDate;

    private LocalDate endDate;

    private String trailerUrl;

    @Size(max = 100, message = "Director name must not exceed 100 characters")
    private String director;

    @Size(max = 500, message = "Cast must not exceed 500 characters")
    private String cast;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    private BigDecimal rating;

    @NotNull(message = "Status is required")
    private MovieStatus status;

    // Danh sách ID thể loại — client gửi [1, 3, 5] thay vì object Genre
    private Set<Long> genreIds;
}
