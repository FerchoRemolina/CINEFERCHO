package com.cinefercho.repository;

import com.cinefercho.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @Query("""
            select s from Screening s
            join fetch s.movie
            join fetch s.hall h
            join fetch h.theater t
            join fetch t.city
            where s.id = :id
            """)
    Optional<Screening> findDetailedById(@Param("id") Long id);

    boolean existsByMovie_Id(Long movieId);

    boolean existsByHall_Id(Long hallId);

    @Query("""
            select s from Screening s
            join fetch s.movie m
            join fetch s.hall h
            join fetch h.theater t
            where m.id = :movieId
              and t.id = :theaterId
              and s.startTime >= :from
              and s.startTime < :to
            order by s.startTime
            """)
    List<Screening> findByMovieTheaterAndDateRange(
            @Param("movieId") Long movieId,
            @Param("theaterId") Long theaterId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    default List<Screening> findByMovieTheaterAndDate(Long movieId, Long theaterId, LocalDate date) {
        return findByMovieTheaterAndDateRange(
                movieId,
                theaterId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
    }
}
