package com.cinefercho.mapper;

import com.cinefercho.dto.SeatMapResponse;
import com.cinefercho.dto.SeatMapResponse.SeatAvailability;
import com.cinefercho.dto.SeatMapResponse.SeatCellResponse;
import com.cinefercho.dto.SeatMapResponse.SeatRowResponse;
import com.cinefercho.entity.CinemaHall;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.Seat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SeatMapMapper {

    public SeatMapResponse toResponse(Screening screening, List<Seat> seats, Set<Long> occupiedSeatIds) {
        CinemaHall hall = screening.getHall();
        List<Seat> orderedSeats = seats.stream()
                .sorted(Comparator.comparing(Seat::getRowLetter).thenComparingInt(Seat::getSeatNumber))
                .toList();

        Map<String, List<SeatCellResponse>> seatsByRow = new LinkedHashMap<>();
        for (Seat seat : orderedSeats) {
            SeatAvailability availability = occupiedSeatIds.contains(seat.getId())
                    ? SeatAvailability.OCUPADO
                    : SeatAvailability.LIBRE;
            seatsByRow
                    .computeIfAbsent(seat.getRowLetter(), row -> new ArrayList<>())
                    .add(new SeatCellResponse(
                            seat.getId(),
                            seat.getRowLetter(),
                            seat.getSeatNumber(),
                            seat.isVip(),
                            availability));
        }

        List<SeatRowResponse> rows = seatsByRow.entrySet().stream()
                .map(entry -> new SeatRowResponse(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();

        return new SeatMapResponse(
                screening.getId(),
                hall.getId(),
                hall.getName(),
                hall.getHallType(),
                hall.getTotalRows(),
                hall.getTotalColumns(),
                rows);
    }
}
