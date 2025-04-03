package com.yuxie.common.compress.config;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.HashSet;

/**
 * 解压配置类
 * <p>
 * 该类用于配置解压过程中的各种参数，提供了丰富的配置选项以满足不同的解压需求。
 * 主要配置项包括：
 * </p>
 * <ul>
 * <li>文件大小限制：控制单个文件的最大大小</li>
 * <li>缓冲区大小：优化文件读写性能</li>
 * <li>临时目录：指定解压过程中的临时文件存储位置</li>
 * <li>允许的文件类型：控制可解压的文件类型</li>
 * <li>最大文件数量：限制压缩包中的文件数量</li>
 * <li>安全检查配置：包括路径遍历、文件类型、大小等检查</li>
 * <li>性能配置：并发解压、超时设置等</li>
 * <li>进度回调：实时获取解压进度</li>
 * <li>校验和验证：确保文件完整性</li>
 * <li>病毒扫描：防止恶意文件</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * UnzipConfig config = UnzipConfig.builder()
 *     .maxFileSize(1024L * 1024L * 100L)  // 100MB
 *     .bufferSize(8192)
 *     .enablePathTraversalCheck(true)
 *     .build();
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipConfigManager
 */
@Data
@Builder
@Slf4j
public class UnzipConfig {
    /**
     * 文件大小限制
     * <p>
     * 控制单个解压文件的最大大小，超过此限制将抛出异常。
     * 默认值为100MB。
     * </p>
     */
    @Builder.Default
    private long maxFileSize = 1024L * 1024L * 100L; // 100MB
    
    /**
     * 缓冲区大小
     * <p>
     * 用于文件读写操作的缓冲区大小，影响解压性能。
     * 默认值为8192字节（8KB）。
     * </p>
     */
    @Builder.Default
    private int bufferSize = 8192;
    
    /**
     * 临时目录
     * <p>
     * 用于存储解压过程中的临时文件。
     * 默认为系统临时目录（java.io.tmpdir）。
     * </p>
     */
    @Builder.Default
    private String tempDirectory = System.getProperty("java.io.tmpdir");
    
    /**
     * 允许的文件类型集合
     * <p>
     * 包含所有允许解压的文件扩展名。
     * 如果启用文件类型检查，只有扩展名在此集合中的文件才会被解压。
     * </p>
     */
    @Builder.Default
    private Set<String> allowedFileTypes = new HashSet<String>() {{
        add("txt");
        add("log");
        add("json");
        add("xml");
        add("csv");
        add("md");
        add("properties");
        add("java");
        add("py");
        add("js");
        add("html");
        add("css");
        add("sql");
        add("sh");
        add("bat");
    }};
    
    /**
     * 最大文件数量
     * <p>
     * 单个压缩包中允许的最大文件数量。
     * 默认值为1000个文件。
     * </p>
     */
    @Builder.Default
    private int maxFileCount = 1000;
    
    /**
     * 是否启用路径遍历检查
     * <p>
     * 用于防止路径遍历攻击，确保解压的文件不会超出指定目录。
     * 默认启用。
     * </p>
     */
    @Builder.Default
    private boolean enablePathTraversalCheck = true;
    
    /**
     * 是否启用文件类型检查
     * <p>
     * 用于验证文件类型是否在允许列表中。
     * 默认启用。
     * </p>
     */
    @Builder.Default
    private boolean enableFileTypeCheck = true;
    
    /**
     * 是否启用文件大小检查
     * <p>
     * 用于验证文件大小是否超过限制。
     * 默认启用。
     * </p>
     */
    @Builder.Default
    private boolean enableFileSizeCheck = true;
    
    /**
     * 是否启用文件数量检查
     * <p>
     * 用于验证压缩包中的文件数量是否超过限制。
     * 默认启用。
     * </p>
     */
    @Builder.Default
    private boolean enableFileCountCheck = true;
    
    /**
     * 是否启用并发解压
     * <p>
     * 用于提高解压性能，通过多线程并行处理提高解压速度。
     * 默认启用。
     * </p>
     */
    @Builder.Default
    private boolean enableConcurrentUnzip = true;
    
    /**
     * 并发线程数
     * <p>
     * 用于并发解压的线程数量。
     * 默认为系统处理器核心数。
     * </p>
     */
    @Builder.Default
    private int concurrentThreads = Runtime.getRuntime().availableProcessors();
    
    /**
     * 解压超时时间
     * <p>
     * 单个解压操作的最大执行时间（毫秒）。
     * 默认值为60秒。
     * </p>
     */
    @Builder.Default
    private long unzipTimeout = 60000L;
    
    /**
     * 是否启用进度回调
     * <p>
     * 用于实时获取解压进度。
     * 默认启用。
     * </p>
     */
    @Builder.Default
    private boolean enableProgressCallback = true;
    
    /**
     * 是否启用校验和验证
     * <p>
     * 用于验证解压文件的完整性。
     * 默认禁用。
     * </p>
     */
    @Builder.Default
    private boolean enableChecksumValidation = false;
    
    /**
     * 是否启用病毒扫描
     * <p>
     * 用于扫描解压文件是否包含病毒。
     * 默认禁用。
     * </p>
     */
    @Builder.Default
    private boolean enableVirusScan = false;
    
    /**
     * 是否启用复合格式检测
     * <p>
     * 用于检测复合格式文件（如tar.gz）。
     * 默认启用。
     * </p>
     */
    @Builder.Default
    private boolean enableCompoundFormatDetection = true;
    
    /**
     * 复合格式文件大小限制
     * <p>
     * 用于限制复合格式文件的大小。
     * 默认值为200MB。
     * </p>
     */
    @Builder.Default
    private long maxCompoundFileSize = 1024L * 1024L * 200L;
    
    /**
     * 最大路径长度
     * <p>
     * 解压文件路径的最大长度限制。
     * 默认值为255个字符。
     * </p>
     */
    @Builder.Default
    private int maxPathLength = 255;
    
    /**
     * 验证配置参数的有效性
     * <p>
     * 检查所有配置参数是否在有效范围内。
     * 如果发现无效参数，将抛出IllegalArgumentException异常。
     * </p>
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
            throw new IllegalArgumentException("并发线程数必须为正数");
        }
        if (unzipTimeout <= 0) {
            throw new IllegalArgumentException("解压超时时间必须为正数");
        }
    }
    
    /**
     * 获取默认配置
     * <p>
     * 创建一个包含所有默认值的UnzipConfig实例。
     * 默认配置已经过验证，可以直接使用。
     * </p>
     *
     * @return 包含默认值的UnzipConfig实例
     */
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
            .unzipTimeout(60000L)
            .enableProgressCallback(true)
            .enableChecksumValidation(false)
            .enableVirusScan(false)
            .enableCompoundFormatDetection(true)
            .maxCompoundFileSize(1024L * 1024L * 200L)
            .maxPathLength(255)
            .build();
            
        config.validate();
        return config;
    }
    
    /**
     * 获取默认允许的文件类型集合
     * <p>
     * 返回一个包含常用文件类型的Set集合。
     * 这些文件类型被认为是安全的，可以默认解压。
     * </p>
     *
     * @return 包含默认允许文件类型的Set集合
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