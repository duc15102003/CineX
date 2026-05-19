package com.cinex.module.movie.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.module.movie.dto.GenreFilter;
import com.cinex.module.movie.dto.GenreRequest;
import com.cinex.module.movie.dto.GenreResponse;
import com.cinex.module.movie.entity.Genre;
import com.cinex.module.movie.mapper.GenreMapper;
import com.cinex.module.movie.repository.GenreRepository;
import com.cinex.module.movie.specification.GenreSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    /**
     * Pattern thống nhất: Filter DTO → Specification.fromFilter() → findAll(spec, pageable)
     */
    @Transactional(readOnly = true)
    public Page<GenreResponse> listGenres(GenreFilter filter, Pageable pageable) {
        var spec = GenreSpecification.fromFilter(filter);
        return genreRepository.findAll(spec, pageable)
                .map(genreMapper::toResponse);
    }

    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.GENRE_EXISTED,
                    "Genre '" + request.getName() + "' already exists");
        }

        Genre genre = genreMapper.toEntity(request);
        genreRepository.save(genre);
        log.info("Created genre: {}", genre.getName());
        return genreMapper.toResponse(genre);
    }

    @Transactional
    public GenreResponse updateGenre(Long id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GENRE_NOT_FOUND));

        if (!genre.getName().equals(request.getName()) && genreRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.GENRE_EXISTED,
                    "Genre '" + request.getName() + "' already exists");
        }

        genreMapper.updateEntity(request, genre);
        genreRepository.save(genre);
        log.info("Updated genre: {}", genre.getName());
        return genreMapper.toResponse(genre);
    }

    @Transactional
    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GENRE_NOT_FOUND));
        genre.setStorageState("DELETED");
        genreRepository.save(genre);
        log.info("Soft deleted genre: {}", genre.getName());
    }

    @Transactional
    public GenreResponse restoreGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GENRE_NOT_FOUND));
        genre.setStorageState(null);
        genreRepository.save(genre);
        log.info("Restored genre: {}", genre.getName());
        return genreMapper.toResponse(genre);
    }
}
