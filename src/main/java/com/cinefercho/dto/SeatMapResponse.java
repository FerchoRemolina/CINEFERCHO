package com.cinefercho.dto;

import com.cinefercho.entity.enums.HallType;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public record SeatMapResponse(
        Long screeningId,
        Long hallId,
        String hallName,
        HallType hallType,
        int totalRows,
        int totalColumns,
        List<SeatRowResponse> rows
) {

    public record SeatRowResponse(
            String rowLetter,
            List<SeatCellResponse> seats
    ) {
    }

    public record SeatCellResponse(
            Long id,
            String rowLetter,
            int seatNumber,
            boolean vip,
            SeatAvailability availability
    ) {
    }

    public enum SeatAvailability {
        LIBRE("libre"),
        OCUPADO("ocupado");

        private final String value;

        SeatAvailability(String value) {
            this.value = value;
        }

        @JsonValue
        public String value() {
            return value;
        }
    }
}
