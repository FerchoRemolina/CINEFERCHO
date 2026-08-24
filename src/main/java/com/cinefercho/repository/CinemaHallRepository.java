package com.cinefercho.repository;

import com.cinefercho.entity.CinemaHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CinemaHallRepository extends JpaRepository<CinemaHall, Long> {

    List<CinemaHall> findByTheater_Id(Long theaterId);

    @Query("""
            select h from CinemaHall h
            join fetch h.theater t
            join fetch t.city
            where h.id = :id
            """)
    Optional<CinemaHall> findDetailedById(@Param("id") Long id);
}
