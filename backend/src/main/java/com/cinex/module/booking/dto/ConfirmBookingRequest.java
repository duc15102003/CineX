package com.cinex.module.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmBookingRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;
}
