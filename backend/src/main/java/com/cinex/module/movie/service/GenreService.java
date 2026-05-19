package com.cinex.module.movie.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.module.movie.dto.GenreRequest;
import com.cinex.module.movie.dto.GenreResponse;
import com.cinex.module.movie.entity.Genre;
import com.cinex.module.movie.mapper.GenreMapper;
import com.cinex.module.movie.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Transactional(readOnly = true)
    public List<GenreResponse> listGenres() {
        return genreRepository.findAll().stream()
                .filter(g -> !"DELETED".equals(g.getStorageState()))
                .map(genreMapper::toResponse)
                .toList();
    }

    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.GENRE_EXISTED,
                    "Genre '" + request.getName() + "' already exists");
        }

        Genre genre = genreMapper.toEntity(request);
        genreRepository.save(genre);
        return genreMapper.toResponse(genre);
    }
}
