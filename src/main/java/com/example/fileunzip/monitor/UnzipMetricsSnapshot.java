package com.example.fileunzip.monitor;

import lombok.Builder;
import lombok.Data;

/**
 * 解压监控数据快照
 */
@Data
@Builder
public class UnzipMetricsSnapshot {
    private long totalUnzipTime;
    private long totalUnzipSize;
    private int totalFiles;
    private int errorCount;
    private int successCount;
    private long peakMemoryUsage;
    private long averageUnzipSpeed;
    private int maxConcurrentTasks;
    private int checksumValidationCount;
    private int virusScanCount;
    private long timestamp;
} 