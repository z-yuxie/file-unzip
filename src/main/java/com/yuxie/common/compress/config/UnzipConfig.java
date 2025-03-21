package com.yuxie.common.compress.config;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.HashSet;

/**
 * 解压配置类
 * 用于配置解压过程中的各种参数，包括：
 * 1. 文件大小限制
 * 2. 缓冲区大小
 * 3. 临时目录
 * 4. 允许的文件类型
 * 5. 最大文件数量
 * 6. 安全检查配置
 * 7. 性能配置
 * 8. 进度回调
 * 9. 校验和验证
 * 10. 病毒扫描
 */
@Data
@Builder
@Slf4j
public class UnzipConfig {
    /**
     * 文件大小限制
     * 默认值为 100MB
     */
    @Builder.Default
    private long maxFileSize = 1024L * 1024L * 100L;
    
    /**
     * 缓冲区大小
     * 用于文件读写操作的缓冲区大小，默认值为 8192 字节
     */
    @Builder.Default
    private int bufferSize = 8192;
    
    /**
     * 临时目录
     * 用于存储解压过程中的临时文件，默认为系统临时目录
     */
    @Builder.Default
    private String tempDirectory = System.getProperty("java.io.tmpdir");
    
    /**
     * 允许的文件类型集合
     * 包含所有允许解压的文件扩展名
     */
    @Builder.Default
    private Set<String> allowedFileTypes = new HashSet<>();
    
    /**
     * 最大文件数量
     * 单个压缩包中允许的最大文件数量，默认值为 1000
     */
    @Builder.Default
    private int maxFileCount = 1000;
    
    /**
     * 是否启用路径遍历检查
     * 用于防止路径遍历攻击，默认为 true
     */
    @Builder.Default
    private boolean enablePathTraversalCheck = true;
    
    /**
     * 是否启用文件类型检查
     * 用于验证文件类型是否在允许列表中，默认为 true
     */
    @Builder.Default
    private boolean enableFileTypeCheck = true;
    
    /**
     * 是否启用文件大小检查
     * 用于验证文件大小是否超过限制，默认为 true
     */
    @Builder.Default
    private boolean enableFileSizeCheck = true;
    
    /**
     * 是否启用文件数量检查
     * 用于验证压缩包中的文件数量是否超过限制，默认为 true
     */
    @Builder.Default
    private boolean enableFileCountCheck = true;
    
    /**
     * 是否启用并发解压
     * 用于提高解压性能，默认为 true
     */
    @Builder.Default
    private boolean enableConcurrentUnzip = true;
    
    /**
     * 并发线程数
     * 用于并发解压的线程数量，默认为系统处理器核心数
     */
    @Builder.Default
    private int concurrentThreads = Runtime.getRuntime().availableProcessors();
    
    /**
     * 解压超时时间
     * 单个解压操作的最大执行时间，默认为 60 秒
     */
    @Builder.Default
    private long unzipTimeout = 60000L;
    
    /**
     * 是否启用进度回调
     * 用于实时获取解压进度，默认为 true
     */
    @Builder.Default
    private boolean enableProgressCallback = true;
    
    /**
     * 是否启用校验和验证
     * 用于验证解压文件的完整性，默认为 false
     */
    @Builder.Default
    private boolean enableChecksumValidation = false;
    
    /**
     * 是否启用病毒扫描
     * 用于扫描解压文件是否包含病毒，默认为 false
     */
    @Builder.Default
    private boolean enableVirusScan = false;
    
    /**
     * 是否启用复合格式检测
     * 用于检测复合格式文件，默认为 true
     */
    @Builder.Default
    private boolean enableCompoundFormatDetection = true;
    
    /**
     * 复合格式文件大小限制
     * 用于限制复合格式文件的大小，默认为 200MB
     */
    @Builder.Default
    private long maxCompoundFileSize = 1024L * 1024L * 200L;
    
    /**
     * 验证配置参数的有效性
     *
     * @throws IllegalArgumentException 当配置参数无效时抛出异常
     */
    public void validate() {
        if (maxFileSize <= 0) {
            throw new IllegalArgumentException("最大文件大小必须为正数");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("缓冲区大小必须为正数");
        }
        if (tempDirectory == null || tempDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("临时目录不能为空");
        }
        if (maxFileCount <= 0) {
            throw new IllegalArgumentException("最大文件数量必须为正数");
        }
        if (concurrentThreads <= 0) {
            throw new IllegalArgumentException("concurrentThreads must be positive");
        }
        if (unzipTimeout <= 0) {
            throw new IllegalArgumentException("unzipTimeout must be positive");
        }
    }
    
    /**
     * 获取默认配置
     *
     * @return 包含默认值的 UnzipConfig 实例
     */
    public static UnzipConfig getDefaultConfig() {
        UnzipConfig config = UnzipConfig.builder()
            .maxFileSize(1024L * 1024L * 100L)
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
            .unzipTimeout(60000L)
            .enableProgressCallback(true)
            .enableChecksumValidation(false)
            .enableVirusScan(false)
            .enableCompoundFormatDetection(true)
            .maxCompoundFileSize(1024L * 1024L * 200L)
            .build();
            
        config.validate();
        return config;
    }
    
    /**
     * 获取默认允许的文件类型集合
     *
     * @return 包含默认允许文件类型的 Set 集合
     */
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