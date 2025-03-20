package com.example.fileunzip.config;

import lombok.Data;
import lombok.Builder;

/**
 * 解压配置类
 */
@Data
@Builder
public class UnzipConfig {
    // 文件大小限制（默认100MB）
    @Builder.Default
    private long maxFileSize = 100 * 1024 * 1024;
    
    // 缓冲区大小（默认8KB）
    @Builder.Default
    private int bufferSize = 8 * 1024;
    
    // 临时文件目录
    @Builder.Default
    private String tempDirectory = System.getProperty("java.io.tmpdir");
    
    // 是否启用文件类型验证
    @Builder.Default
    private boolean enableFileTypeValidation = true;
    
    // 是否启用路径安全检查
    @Builder.Default
    private boolean enablePathSecurityCheck = true;
    
    // 是否启用校验和验证
    @Builder.Default
    private boolean enableChecksumValidation = false;
    
    // 是否启用病毒扫描
    @Builder.Default
    private boolean enableVirusScan = false;
    
    // 是否启用并发解压
    @Builder.Default
    private boolean enableConcurrentUnzip = false;
    
    // 并发解压线程数
    @Builder.Default
    private int concurrentThreads = Runtime.getRuntime().availableProcessors();
    
    // 解压超时时间（毫秒）
    @Builder.Default
    private long unzipTimeout = 300000; // 5分钟
    
    // 是否启用进度回调
    @Builder.Default
    private boolean enableProgressCallback = true;
    
    /**
     * 获取默认配置
     */
    public static UnzipConfig getDefaultConfig() {
        return UnzipConfig.builder().build();
    }
} 