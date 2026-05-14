package com.cinex.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class xử lý tiền tệ.
 *
 * Tại sao dùng BigDecimal thay vì double?
 * → double bị lỗi floating point: 0.1 + 0.2 = 0.30000000000000004
 * → Tiền tệ PHẢI chính xác 100%, dùng BigDecimal.
 *
 * Anti-pattern: ĐỪNG BAO GIỜ dùng double/float cho tiền.
 * VD sai: double price = 75000.0;
 * VD đúng: BigDecimal price = new BigDecimal("75000");
 */
public final class MoneyUtil {

    private MoneyUtil() {}

    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        VND_FORMAT = new DecimalFormat("#,###", symbols);
    }

    /**
     * Format số tiền VND: 75000 → "75.000 ₫"
     *
     * VD: formatVND(75000)   → "75.000 ₫"
     *     formatVND(1500000) → "1.500.000 ₫"
     *     formatVND(0)       → "0 ₫"
     */
    public static String formatVND(long amount) {
        return VND_FORMAT.format(amount) + " ₫";
    }

    public static String formatVND(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return VND_FORMAT.format(amount.longValue()) + " ₫";
    }

    /**
     * Format không có đơn vị: 75000 → "75.000"
     */
    public static String formatNumber(long amount) {
        return VND_FORMAT.format(amount);
    }

    /**
     * Tính tổng tiền từ giá + số lượng.
     *
     * VD: calculateTotal(75000, 3) → 225000
     *
     * Tại sao dùng BigDecimal?
     * → Tránh lỗi overflow với long khi số lớn
     * → Tương thích với DECIMAL trong SQL Server
     */
    public static BigDecimal calculateTotal(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null || quantity <= 0) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Tính giá sau khi áp dụng % giảm giá.
     *
     * VD: applyDiscount(100000, 20) → 80000  (giảm 20%)
     *     applyDiscount(75000, 10)  → 67500  (giảm 10%)
     *
     * Dùng cho: khuyến mãi, giảm giá sinh nhật, ...
     */
    public static BigDecimal applyDiscount(BigDecimal price, int discountPercent) {
        if (price == null || discountPercent <= 0) return price;
        if (discountPercent >= 100) return BigDecimal.ZERO;
        BigDecimal discount = price.multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return price.subtract(discount);
    }

    /**
     * Tính giá sau khi nhân hệ số (VD: hệ số VIP = 1.5, cuối tuần = 1.2).
     *
     * VD: applyMultiplier(75000, 1.5) → 112500  (VIP)
     *     applyMultiplier(75000, 1.2) → 90000   (cuối tuần)
     */
    public static BigDecimal applyMultiplier(BigDecimal price, double multiplier) {
        if (price == null || multiplier <= 0) return price;
        return price.multiply(BigDecimal.valueOf(multiplier))
                .setScale(0, RoundingMode.HALF_UP);
    }
}
