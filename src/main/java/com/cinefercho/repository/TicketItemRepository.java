package com.cinefercho.repository;

import com.cinefercho.entity.TicketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface TicketItemRepository extends JpaRepository<TicketItem, Long> {

    boolean existsByScreeningIdAndSeatId(Long screeningId, Long seatId);

    boolean existsByScreening_Id(Long screeningId);

    @Query("select ti.seat.id from TicketItem ti where ti.screening.id = :screeningId")
    Set<Long> findOccupiedSeatIdsByScreeningId(@Param("screeningId") Long screeningId);
}
