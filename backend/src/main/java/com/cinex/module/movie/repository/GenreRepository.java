package com.cinex.module.movie.repository;

import com.cinex.module.movie.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT g FROM Genre g WHERE g.storageState IS NULL OR g.storageState <> 'DELETED'")
    List<Genre> findAllActive();
}
