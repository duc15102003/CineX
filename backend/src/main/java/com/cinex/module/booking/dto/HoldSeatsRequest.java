package com.cinex.module.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HoldSeatsRequest {

    @NotNull(message = "Showtime ID is required")
    private Long showtimeId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 8, message = "Maximum 8 seats per booking")
    private List<Long> seatIds;
}
