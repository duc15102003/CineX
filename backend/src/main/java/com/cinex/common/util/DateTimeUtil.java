package com.cinex.common.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class xử lý ngày giờ.
 *
 * Tại sao không dùng java.util.Date?
 * → Date là API cũ (Java 1.0), mutable, không thread-safe.
 * → LocalDateTime (Java 8+) immutable, thread-safe, API rõ ràng hơn.
 * → Quy tắc: LUÔN dùng java.time.* (LocalDate, LocalDateTime, Instant), KHÔNG dùng java.util.Date.
 */
public final class DateTimeUtil {

    private DateTimeUtil() {}

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Format ngày: 10/05/2026
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    /**
     * Format ngày giờ: 10/05/2026 19:00
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : "";
    }

    /**
     * Format chỉ giờ: 19:00
     * Dùng cho: hiển thị suất chiếu
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMAT) : "";
    }

    /**
     * Format khoảng thời gian phim: "2h 30m"
     *
     * VD: formatDuration(150) → "2h 30m"
     *     formatDuration(90)  → "1h 30m"
     *     formatDuration(45)  → "45m"
     */
    public static String formatDuration(int totalMinutes) {
        if (totalMinutes <= 0) return "0m";
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours == 0) return minutes + "m";
        if (minutes == 0) return hours + "h";
        return hours + "h " + minutes + "m";
    }

    /**
     * Tính endTime suất chiếu = startTime + duration + buffer dọn phòng.
     *
     * VD: calculateEndTime(19:00, 150, 15) → 21:45
     *     (phim 150 phút + 15 phút dọn phòng)
     *
     * Tại sao cần buffer? Nhân viên cần thời gian dọn dẹp giữa 2 suất chiếu.
     */
    public static LocalDateTime calculateEndTime(LocalDateTime startTime, int durationMinutes, int bufferMinutes) {
        return startTime.plusMinutes(durationMinutes + bufferMinutes);
    }

    /**
     * Kiểm tra 2 khoảng thời gian có trùng nhau không.
     *
     * Dùng cho: kiểm tra suất chiếu trùng giờ trong cùng phòng.
     * VD: Phòng 1 đã có suất 19:00-21:45
     *     → Tạo suất 20:00-22:30 → isOverlapping = true → không cho tạo
     *     → Tạo suất 22:00-00:30 → isOverlapping = true (vẫn trùng)
     *     → Tạo suất 22:00-00:30 với start sau end cũ → false → OK
     */
    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Tính thời gian còn lại dạng text.
     *
     * VD: timeAgo(5 phút trước) → "5 phút trước"
     *     timeAgo(2 giờ trước)  → "2 giờ trước"
     *     timeAgo(3 ngày trước) → "3 ngày trước"
     *
     * Dùng cho: hiển thị "đặt vé 5 phút trước" trên giao diện.
     */
    public static String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "just now";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 2592000) return (seconds / 86400) + " days ago";
        return formatDate(dateTime.toLocalDate());
    }

    /**
     * Kiểm tra ngày có phải cuối tuần không.
     *
     * Dùng cho: tính giá vé cuối tuần (đắt hơn ngày thường).
     */
    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6; // 6=Saturday, 7=Sunday
    }

    /**
     * Kiểm tra thời gian có phải suất tối không (sau 18h).
     *
     * Dùng cho: tính giá vé suất tối (đắt hơn suất ngày).
     */
    public static boolean isEveningShow(LocalDateTime showTime) {
        return showTime.getHour() >= 18;
    }
}
