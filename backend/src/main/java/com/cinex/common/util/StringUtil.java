package com.cinex.common.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class xử lý chuỗi.
 *
 * [Utility Pattern] Tất cả method đều static, không cần tạo instance.
 *
 * [Performance] Tất cả regex đều compile 1 LẦN thành static final Pattern.
 * Tại sao? String.replaceAll("regex") compile regex MỖI LẦN gọi → chậm.
 * Pattern.compile() 1 lần → dùng lại → nhanh gấp 5-10 lần.
 */
public final class StringUtil {

    private StringUtil() {}

    // Compile regex 1 lần, dùng lại mãi mãi (thread-safe vì Pattern immutable)
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final Pattern LEADING_TRAILING_DASH = Pattern.compile("^-|-$");
    private static final Pattern SPLIT_WORDS = Pattern.compile("\\s+");

    /**
     * Kiểm tra chuỗi null hoặc rỗng (sau khi trim).
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Bỏ dấu tiếng Việt.
     * Dùng Unicode Normalization Form D (NFD) → tách ký tự gốc và dấu → xóa dấu.
     *
     * VD: removeDiacritics("Vũ Tường An") → "Vu Tuong An"
     */
    public static String removeDiacritics(String str) {
        if (isBlank(str)) return str;
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized).replaceAll("")
                .replace('đ', 'd').replace('Đ', 'D');
    }

    /**
     * Tạo slug URL-friendly từ chuỗi tiếng Việt.
     *
     * VD: toSlug("Avengers: Hồi Kết") → "avengers-hoi-ket"
     *     toSlug("Phòng chiếu IMAX 3D!") → "phong-chieu-imax-3d"
     */
    public static String toSlug(String str) {
        if (isBlank(str)) return "";
        String result = removeDiacritics(str).toLowerCase();
        result = NON_ALPHANUMERIC.matcher(result).replaceAll("");
        result = WHITESPACE.matcher(result).replaceAll("-");
        result = MULTI_DASH.matcher(result).replaceAll("-");
        result = LEADING_TRAILING_DASH.matcher(result).replaceAll("");
        return result;
    }

    /**
     * Ẩn giữa email, chỉ hiện 2 ký tự đầu + domain.
     *
     * VD: maskEmail("vanan@gmail.com") → "va***@gmail.com"
     */
    public static String maskEmail(String email) {
        if (isBlank(email)) return email;
        int atIndex = email.indexOf('@');
        if (atIndex < 0) return email;
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        int show = Math.min(2, name.length());
        return name.substring(0, show) + "***" + domain;
    }

    /**
     * Ẩn giữa số điện thoại, chỉ hiện 3 số đầu + 2 số cuối.
     *
     * VD: maskPhone("0912345678") → "091*****78"
     */
    public static String maskPhone(String phone) {
        if (isBlank(phone) || phone.length() < 6) return phone;
        int len = phone.length();
        return phone.substring(0, 3) + "*".repeat(len - 5) + phone.substring(len - 2);
    }

    /**
     * Cắt chuỗi + thêm "..." nếu quá dài.
     *
     * VD: truncate("Avengers: Endgame - Hồi kết", 15) → "Avengers: En..."
     */
    public static String truncate(String str, int maxLength) {
        if (isBlank(str) || str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Viết hoa chữ cái đầu mỗi từ.
     *
     * VD: capitalize("vũ tường an") → "Vũ Tường An"
     */
    public static String capitalize(String str) {
        if (isBlank(str)) return str;
        String[] words = SPLIT_WORDS.split(str.toLowerCase());
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1));
            }
        }
        return sb.toString();
    }
}
