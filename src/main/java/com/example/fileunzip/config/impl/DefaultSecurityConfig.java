package com.example.fileunzip.config.impl;

import com.example.fileunzip.config.SecurityConfig;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 默认安全配置实现
 */
@Data
@Builder
public class DefaultSecurityConfig implements SecurityConfig {
    private static final long DEFAULT_MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final int DEFAULT_MAX_FILE_COUNT = 1000;
    private static final Set<String> DEFAULT_ALLOWED_FILE_TYPES = new HashSet<>();

    static {
        DEFAULT_ALLOWED_FILE_TYPES.add("txt");
        DEFAULT_ALLOWED_FILE_TYPES.add("csv");
        DEFAULT_ALLOWED_FILE_TYPES.add("json");
        DEFAULT_ALLOWED_FILE_TYPES.add("xml");
        DEFAULT_ALLOWED_FILE_TYPES.add("pdf");
        DEFAULT_ALLOWED_FILE_TYPES.add("doc");
        DEFAULT_ALLOWED_FILE_TYPES.add("docx");
        DEFAULT_ALLOWED_FILE_TYPES.add("xls");
        DEFAULT_ALLOWED_FILE_TYPES.add("xlsx");
        DEFAULT_ALLOWED_FILE_TYPES.add("jpg");
        DEFAULT_ALLOWED_FILE_TYPES.add("jpeg");
        DEFAULT_ALLOWED_FILE_TYPES.add("png");
        DEFAULT_ALLOWED_FILE_TYPES.add("gif");
    }

    @Builder.Default
    private long maxFileSize = DEFAULT_MAX_FILE_SIZE;

    @Builder.Default
    private Set<String> allowedFileTypes = new HashSet<>(DEFAULT_ALLOWED_FILE_TYPES);

    @Builder.Default
    private int maxFileCount = DEFAULT_MAX_FILE_COUNT;

    @Builder.Default
    private boolean pathTraversalCheckEnabled = true;

    @Builder.Default
    private boolean fileTypeCheckEnabled = true;

    @Builder.Default
    private boolean fileSizeCheckEnabled = true;

    @Builder.Default
    private boolean fileCountCheckEnabled = true;

    public static DefaultSecurityConfig createDefault() {
        return DefaultSecurityConfig.builder().build();
    }
} 