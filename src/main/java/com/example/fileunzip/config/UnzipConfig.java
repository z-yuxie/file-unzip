package com.example.fileunzip.config;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.HashSet;

/**
 * 解压配置类
 */
@Data
@Builder
@Slf4j
public class UnzipConfig {
    // 文件大小限制
    @Builder.Default
    private long maxFileSize = 1024L * 1024L * 100L; // 100MB
    
    // 缓冲区大小
    @Builder.Default
    private int bufferSize = 8192;
    
    // 临时目录
    @Builder.Default
    private String tempDirectory = System.getProperty("java.io.tmpdir");
    
    // 允许的文件类型
    @Builder.Default
    private Set<String> allowedFileTypes = new HashSet<>();
    
    // 最大文件数量
    @Builder.Default
    private int maxFileCount = 1000;
    
    // 安全检查配置
    @Builder.Default
    private boolean enablePathTraversalCheck = true;
    @Builder.Default
    private boolean enableFileTypeCheck = true;
    @Builder.Default
    private boolean enableFileSizeCheck = true;
    @Builder.Default
    private boolean enableFileCountCheck = true;
    
    // 性能配置
    @Builder.Default
    private boolean enableConcurrentUnzip = true;
    @Builder.Default
    private int concurrentThreads = Runtime.getRuntime().availableProcessors();
    @Builder.Default
    private long unzipTimeout = 60000L; // 60秒
    
    // 进度回调
    @Builder.Default
    private boolean enableProgressCallback = true;
    
    // 校验和验证
    @Builder.Default
    private boolean enableChecksumValidation = false;
    
    // 病毒扫描
    @Builder.Default
    private boolean enableVirusScan = false;
    
    public void validate() {
        if (maxFileSize <= 0) {
            throw new IllegalArgumentException("maxFileSize must be positive");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be positive");
        }
        if (tempDirectory == null || tempDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("tempDirectory cannot be empty");
        }
        if (maxFileCount <= 0) {
            throw new IllegalArgumentException("maxFileCount must be positive");
        }
        if (concurrentThreads <= 0) {
            throw new IllegalArgumentException("concurrentThreads must be positive");
        }
        if (unzipTimeout <= 0) {
            throw new IllegalArgumentException("unzipTimeout must be positive");
        }
    }
    
    public static UnzipConfig getDefaultConfig() {
        UnzipConfig config = UnzipConfig.builder()
            .maxFileSize(1024L * 1024L * 100L) // 100MB
            .bufferSize(8192)
            .tempDirectory(System.getProperty("java.io.tmpdir"))
            .allowedFileTypes(getDefaultAllowedFileTypes())
            .maxFileCount(1000)
            .enablePathTraversalCheck(true)
            .enableFileTypeCheck(true)
            .enableFileSizeCheck(true)
            .enableFileCountCheck(true)
            .enableConcurrentUnzip(true)
            .concurrentThreads(Runtime.getRuntime().availableProcessors())
            .unzipTimeout(60000L) // 60秒
            .enableProgressCallback(true)
            .enableChecksumValidation(false)
            .enableVirusScan(false)
            .build();
            
        config.validate();
        return config;
    }
    
    private static Set<String> getDefaultAllowedFileTypes() {
        Set<String> types = new HashSet<>();
        types.add("txt");
        types.add("pdf");
        types.add("doc");
        types.add("docx");
        types.add("xls");
        types.add("xlsx");
        types.add("jpg");
        types.add("jpeg");
        types.add("png");
        types.add("gif");
        return types;
    }
} 