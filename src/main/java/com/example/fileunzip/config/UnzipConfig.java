package com.example.fileunzip.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnzipConfig {
    // 文件大小限制（默认100MB）
    private long maxFileSize;
    
    // 缓冲区大小（默认8KB）
    private int bufferSize;
    
    // 临时文件目录
    private String tempDirectory;
    
    // 是否启用文件类型验证
    private boolean enableFileTypeValidation;
    
    // 是否启用路径安全检查
    private boolean enablePathSecurityCheck;
    
    // 是否启用校验和验证
    private boolean enableChecksumValidation;
    
    // 是否启用病毒扫描
    private boolean enableVirusScan;
    
    // 是否启用并发解压
    private boolean enableConcurrentUnzip;
    
    // 并发解压线程数
    private int concurrentThreads;
    
    // 解压超时时间（毫秒）
    private long unzipTimeout;
    
    // 是否启用进度回调
    private boolean enableProgressCallback;
    
    // 默认配置
    public static UnzipConfig getDefaultConfig() {
        return UnzipConfig.builder()
            .maxFileSize(100 * 1024 * 1024) // 100MB
            .bufferSize(8 * 1024) // 8KB
            .tempDirectory(System.getProperty("java.io.tmpdir"))
            .enableFileTypeValidation(true)
            .enablePathSecurityCheck(true)
            .enableChecksumValidation(false)
            .enableVirusScan(false)
            .enableConcurrentUnzip(false)
            .concurrentThreads(Runtime.getRuntime().availableProcessors())
            .unzipTimeout(300000) // 5分钟
            .enableProgressCallback(true)
            .build();
    }
} 