package com.diiexe.pcsalessystem.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\-]");
    private static final Pattern MULTI_DASH = Pattern.compile("\\-+");

    public static String toSlug(String input){
        if (input == null || input.isBlank()) return "";
        
        String result = input.toLowerCase(Locale.ROOT);
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = NON_ASCII.matcher(result).replaceAll("");
        result = result.replace(" ", "-");
        result = NOT_ALPHANUMERIC.matcher(result).replaceAll("");
        result = MULTI_DASH.matcher(result).replaceAll("-");
        return result.strip().replaceAll("^-|-$", "");
    }
}
