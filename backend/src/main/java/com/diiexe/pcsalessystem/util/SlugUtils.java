package com.diiexe.pcsalessystem.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    // Regex nhận diện các dấu (diacritics) sau khi Normalizer tách ra
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\-]");
    private static final Pattern MULTI_DASH = Pattern.compile("-+");

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "";

        // 1. Chuyển thành chữ thường và xử lý riêng chữ "đ"
        String result = input.toLowerCase(Locale.ROOT).replace("đ", "d");

        // 2. Normalizer: Tách các ký tự có dấu thành [ký tự gốc] + [dấu] (VD: "á" -> "a" + "´")
        result = Normalizer.normalize(result, Normalizer.Form.NFD);

        // 3. Xóa toàn bộ các dấu vừa được tách ra
        result = DIACRITICS.matcher(result).replaceAll("");

        // 4. Đổi khoảng trắng thành gạch ngang và xóa các ký tự không phải chữ, số, hoặc gạch ngang
        result = result.replace(" ", "-");
        result = NOT_ALPHANUMERIC.matcher(result).replaceAll("");

        // 5. Gộp nhiều dấu gạch ngang liên tiếp thành 1 và cắt bỏ gạch ngang ở 2 đầu
        result = MULTI_DASH.matcher(result).replaceAll("-");
        
        return result.replaceAll("^-|-$", "");
    }
}