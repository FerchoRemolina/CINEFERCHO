package com.cinefercho.util;

import com.cinefercho.entity.CinemaHall;
import com.cinefercho.entity.Seat;

public final class SeatFactory {

    private SeatFactory() {
    }

    public static void fillHall(CinemaHall hall) {
        int rows = hall.getTotalRows();
        int columns = hall.getTotalColumns();
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            String rowLetter = String.valueOf((char) ('A' + rowIndex));
            boolean vip = rowIndex == rows - 1;
            for (int seatNumber = 1; seatNumber <= columns; seatNumber++) {
                Seat seat = new Seat();
                seat.setRowLetter(rowLetter);
                seat.setSeatNumber(seatNumber);
                seat.setVip(vip);
                hall.addSeat(seat);
            }
        }
    }
}
