package com.cinefercho.repository;

import com.cinefercho.entity.Movie;
import com.cinefercho.entity.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status);
}
