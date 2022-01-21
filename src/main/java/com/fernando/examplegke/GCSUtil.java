package com.fernando.examplegke;

import org.springframework.web.multipart.MultipartFile;

public final class GCSUtil {
    private GCSUtil() {
        throw new IllegalStateException("Cannot instance this class");
    }

    private static final String EMISION_FORMATTER = "emision/%s";

    public static String buildRoute(MultipartFile multipartFile) {
        return String.format(EMISION_FORMATTER, multipartFile.getOriginalFilename());
    }
}
