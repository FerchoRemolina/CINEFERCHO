package com.cinefercho.repository;

import com.cinefercho.entity.Movie;
import com.cinefercho.entity.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status);

    @Query("""
            select distinct m from Movie m
            where exists (
                select 1 from Screening s
                where s.movie = m
                  and s.startTime < :now
                  and s.hall.theater.id = :theaterId
            )
            order by m.title
            """)
    List<Movie> findNowShowingAtTheater(
            @Param("now") LocalDateTime now,
            @Param("theaterId") Long theaterId);

    @Query("""
            select m from Movie m
            where m.releaseDate is not null
              and m.releaseDate >= :today
              and m.releaseDate <= :limit
              and not exists (
                  select 1 from Screening s
                  where s.movie = m and s.startTime < :now
              )
              and exists (
                  select 1 from Screening s
                  where s.movie = m
                    and s.hall.theater.id = :theaterId
              )
            order by m.releaseDate, m.title
            """)
    List<Movie> findPresaleAtTheater(
            @Param("today") LocalDate today,
            @Param("limit") LocalDate limit,
            @Param("now") LocalDateTime now,
            @Param("theaterId") Long theaterId);

    @Query("""
            select m from Movie m
            where m.releaseDate is not null
              and m.releaseDate > :limit
              and not exists (
                  select 1 from Screening s
                  where s.movie = m and s.startTime < :now
              )
              and (
                  not exists (
                      select 1 from Screening s
                      where s.movie = m
                  )
                  or exists (
                      select 1 from Screening s
                      where s.movie = m
                        and s.hall.theater.id = :theaterId
                  )
              )
            order by m.releaseDate, m.title
            """)
    List<Movie> findUpcomingAtTheater(
            @Param("limit") LocalDate limit,
            @Param("now") LocalDateTime now,
            @Param("theaterId") Long theaterId);
}
