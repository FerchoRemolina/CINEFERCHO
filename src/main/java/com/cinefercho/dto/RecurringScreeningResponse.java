package com.cinefercho.dto;

import java.util.List;

public record RecurringScreeningResponse(
        int created,
        List<ScreeningResponse> screenings
) {
}
