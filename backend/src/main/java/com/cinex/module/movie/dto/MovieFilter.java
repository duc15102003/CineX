package com.cinex.module.movie.dto;

import com.cinex.module.movie.entity.MovieStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieFilter {

    private String keyword;
    private MovieStatus status;
    private Long genreId;
    private Boolean includeDeleted;
}
