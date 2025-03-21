package com.example.fileunzip.monitor;

import lombok.Builder;
import lombok.Data;

/**
 * 解压监控数据快照
 * 用于记录解压过程中的关键指标，包括：
 * 1. 解压时间和大小
 * 2. 文件数量统计
 * 3. 错误和成功计数
 * 4. 内存使用情况
 * 5. 解压速度
 * 6. 并发任务数
 * 7. 安全验证统计
 */
@Data
@Builder
public class UnzipMetricsSnapshot {
    /** 总解压时间（毫秒） */
    private long totalUnzipTime;
    
    /** 总解压大小（字节） */
    private long totalUnzipSize;
    
    /** 总文件数量 */
    private int totalFiles;
    
    /** 错误计数 */
    private int errorCount;
    
    /** 成功计数 */
    private int successCount;
    
    /** 峰值内存使用量（字节） */
    private long peakMemoryUsage;
    
    /** 平均解压速度（字节/秒） */
    private long averageUnzipSpeed;
    
    /** 最大并发任务数 */
    private int maxConcurrentTasks;
    
    /** 校验和验证次数 */
    private int checksumValidationCount;
    
    /** 病毒扫描次数 */
    private int virusScanCount;
    
    /** 快照创建时间戳（毫秒） */
    private long timestamp;
} 