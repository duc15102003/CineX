package com.cinex.module.showtime.dto;

import com.cinex.module.showtime.entity.ShowtimeStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ShowtimeListResponse {

    private Long id;
    private String storageState;
    private String movieTitle;
    private String moviePosterUrl;
    private String roomName;
    private String roomType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private ShowtimeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
