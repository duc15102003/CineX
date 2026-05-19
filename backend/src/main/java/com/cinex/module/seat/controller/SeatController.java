package com.cinex.module.seat.controller;

import com.cinex.common.response.ApiResponse;
import com.cinex.module.seat.dto.SeatGenerateRequest;
import com.cinex.module.seat.dto.SeatMapResponse;
import com.cinex.module.seat.dto.SeatResponse;
import com.cinex.module.seat.dto.UpdateSeatRequest;
import com.cinex.module.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Seat", description = "Seat map management")
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/rooms/{roomId}/seats")
    @Operation(summary = "Get seat map of a room")
    public ApiResponse<SeatMapResponse> getSeatMap(@PathVariable Long roomId) {
        return ApiResponse.ok(seatService.getSeatMap(roomId));
    }

    @PostMapping("/rooms/{roomId}/seats/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "(Admin) Generate seats for a room")
    public ApiResponse<SeatMapResponse> generateSeats(
            @PathVariable Long roomId,
            @Valid @RequestBody SeatGenerateRequest request) {
        return ApiResponse.ok("Seats generated", seatService.generateSeats(roomId, request));
    }

    @PutMapping("/seats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "(Admin) Update seat type or status")
    public ApiResponse<SeatResponse> updateSeat(
            @PathVariable Long id,
            @RequestBody UpdateSeatRequest request) {
        return ApiResponse.ok("Seat updated", seatService.updateSeat(id, request));
    }
}
