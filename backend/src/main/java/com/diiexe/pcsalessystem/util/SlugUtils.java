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

        String result = input.toLowerCase(Locale.ROOT);
        
        // Thay thế các ký tự tiếng Việt đặc biệt trước khi Normalizer
        result = result.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        result = result.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        result = result.replaceAll("[ìíịỉĩ]", "i");
        result = result.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        result = result.replaceAll("[ùúụủũưừứựửữ]", "u");
        result = result.replaceAll("[ỳýỵỷỹ]", "y");
        result = result.replaceAll("đ", "d");

        // Normalizer để xử lý các trường hợp còn sót lại
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = DIACRITICS.matcher(result).replaceAll("");

        // Đổi khoảng trắng thành gạch ngang và xóa các ký tự không phải chữ, số, hoặc gạch ngang
        result = result.replace(" ", "-");
        result = NOT_ALPHANUMERIC.matcher(result).replaceAll("");

        // Gộp nhiều dấu gạch ngang liên tiếp thành 1 và cắt bỏ gạch ngang ở 2 đầu
        result = MULTI_DASH.matcher(result).replaceAll("-");
        
        return result.replaceAll("^-|-$", "");
    }
}