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

    @Query("""
            select s from Screening s
            join fetch s.movie
            join fetch s.hall h
            join fetch h.theater t
            join fetch t.city
            order by s.startTime
            """)
    List<Screening> findAllDetailed();

    boolean existsByMovie_Id(Long movieId);

    boolean existsByHall_Id(Long hallId);

    boolean existsByMovie_IdAndStartTimeBefore(Long movieId, LocalDateTime time);

    @Query("""
            select s from Screening s
            join fetch s.hall h
            join fetch h.theater
            where s.movie.id = :movieId
            order by s.startTime
            """)
    List<Screening> findByMovieIdWithHall(@Param("movieId") Long movieId);

    @Query("""
            select s from Screening s
            join fetch s.hall h
            join fetch h.theater
            where s.movie.id = :movieId
              and h.theater.id = :theaterId
            order by s.startTime
            """)
    List<Screening> findByMovieIdAndTheaterIdWithHall(
            @Param("movieId") Long movieId,
            @Param("theaterId") Long theaterId);

    @Query("""
            select (count(s) > 0) from Screening s
            where s.hall.id = :hallId
              and (:excludeId is null or s.id <> :excludeId)
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    boolean existsHallOverlap(
            @Param("hallId") Long hallId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId);

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
