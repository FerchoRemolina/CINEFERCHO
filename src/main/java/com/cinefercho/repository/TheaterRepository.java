package com.cinefercho.repository;

import com.cinefercho.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

    @Query("""
            select t from Theater t
            join fetch t.city
            where t.city.id = :cityId and t.active = true
            """)
    List<Theater> findActiveByCityId(@Param("cityId") Long cityId);

    @Query("""
            select t from Theater t
            join fetch t.city
            where t.id = :id
            """)
    Optional<Theater> findDetailedById(@Param("id") Long id);
}
