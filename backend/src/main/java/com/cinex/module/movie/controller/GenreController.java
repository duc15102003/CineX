package com.cinex.module.movie.controller;

import com.cinex.common.response.ApiResponse;
import com.cinex.module.movie.dto.GenreRequest;
import com.cinex.module.movie.dto.GenreResponse;
import com.cinex.module.movie.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@Tag(name = "Genre", description = "Movie genres")
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    @Operation(summary = "List all genres")
    public ApiResponse<List<GenreResponse>> listGenres() {
        return ApiResponse.ok(genreService.listGenres());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "(Admin) Create a new genre")
    public ApiResponse<GenreResponse> createGenre(@Valid @RequestBody GenreRequest request) {
        return ApiResponse.ok("Genre created", genreService.createGenre(request));
    }
}
