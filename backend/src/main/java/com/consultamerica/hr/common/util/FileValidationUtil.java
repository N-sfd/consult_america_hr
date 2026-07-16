package com.consultamerica.hr.common.util;

import org.springframework.web.multipart.MultipartFile;

public final class FileValidationUtil {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private FileValidationUtil() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the maximum allowed size (5MB)");
        }
    }

    /** Normalizes the frontend's known "undefined" string quirk (unbound form controls serialize as the literal word) to null. */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("undefined") || trimmed.equalsIgnoreCase("null")) {
            return null;
        }
        return trimmed;
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "download";
        }
        return fileName.replaceAll("[\\r\\n\"]", "_");
    }
}
