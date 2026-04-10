package com.ra.base_spring_boot.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * Chuyển đổi chuỗi rống/chỉ toàn khoảng trắng thành null
     */
    public static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String slugify(String input) {
        if (input == null || input.isEmpty()) return "";

        String slug = input.toLowerCase().trim();

        // Loại bỏ dấu tiếng Việt
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = slug.replaceAll("\\p{M}", "");

        // Thay d thành d (Normalizer không xử lý được d/D)
        slug = slug.replaceAll("đ", "d");

        // Loại bỏ ký tự đặc biệt, giữ lại chữ cái, số và khoảng trắng
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");

        // Thay thế khoảng trắng và gạch ngang liên tiếp bằng một gạch ngang
        slug = slug.replaceAll("[\\s-]+", "-");

        // Loại bỏ gạch ngang ở đầu và cuối
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }
}
