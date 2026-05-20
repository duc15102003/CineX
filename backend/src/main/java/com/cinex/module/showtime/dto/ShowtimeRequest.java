package com.cinex.module.showtime.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ShowtimeRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    @NotNull(message = "VIP price is required")
    private BigDecimal vipPrice;

    @NotNull(message = "Couple price is required")
    private BigDecimal couplePrice;
}
