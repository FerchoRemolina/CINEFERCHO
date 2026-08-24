package com.cinefercho.repository;

import com.cinefercho.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByHall_IdOrderByRowLetterAscSeatNumberAsc(Long hallId);

    List<Seat> findByIdInAndHall_Id(Collection<Long> ids, Long hallId);
}
