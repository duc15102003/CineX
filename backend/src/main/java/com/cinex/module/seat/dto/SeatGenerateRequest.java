package com.cinex.module.seat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Cấu hình để tự động sinh ghế cho phòng.
 *
 * VD: totalRows=10, totalCols=12, vipRows=["E","F","G"], coupleRow="J"
 * → Sinh 10 hàng (A-J), mỗi hàng 12 cột
 * → Hàng E, F, G = VIP
 * → Hàng J = COUPLE (ghế đôi)
 * → Còn lại = STANDARD
 */
@Getter
@Setter
public class SeatGenerateRequest {

    @NotNull(message = "Total rows is required")
    @Min(value = 1, message = "Minimum 1 row")
    @Max(value = 26, message = "Maximum 26 rows (A-Z)")
    private Integer totalRows;

    @NotNull(message = "Total columns is required")
    @Min(value = 1, message = "Minimum 1 column")
    @Max(value = 30, message = "Maximum 30 columns")
    private Integer totalCols;

    // Hàng VIP (VD: ["E", "F", "G"])
    private Set<String> vipRows;

    // Hàng couple (VD: "J") — ghế đôi, thường hàng cuối
    private String coupleRow;
}
