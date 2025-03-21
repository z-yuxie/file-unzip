package com.example.fileunzip.monitor;

import com.example.fileunzip.exception.UnzipErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.HashMap;

/**
 * 默认解压监控实现
 * 使用原子类实现线程安全的指标收集，包括：
 * 1. 解压时间和大小统计
 * 2. 文件数量统计
 * 3. 错误和成功计数
 * 4. 内存使用监控
 * 5. 解压速度计算
 * 6. 并发任务统计
 * 7. 安全验证统计
 */
@Slf4j
public class DefaultUnzipMetrics implements UnzipMetrics {
    /** 总解压时间（毫秒） */
    private final AtomicLong totalUnzipTime = new AtomicLong(0);
    
    /** 总解压大小（字节） */
    private final AtomicLong totalUnzipSize = new AtomicLong(0);
    
    /** 总文件数量 */
    private final AtomicInteger totalFiles = new AtomicInteger(0);
    
    /** 错误计数 */
    private final AtomicInteger errorCount = new AtomicInteger(0);
    
    /** 成功计数 */
    private final AtomicInteger successCount = new AtomicInteger(0);
    
    /** 峰值内存使用量（字节） */
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    
    /** 总解压速度（字节/秒） */
    private final AtomicLong totalUnzipSpeed = new AtomicLong(0);
    
    /** 速度采样次数 */
    private final AtomicInteger speedCount = new AtomicInteger(0);
    
    /** 最大并发任务数 */
    private final AtomicInteger maxConcurrentTasks = new AtomicInteger(0);
    
    /** 校验和验证成功次数 */
    private final AtomicInteger checksumValidationCount = new AtomicInteger(0);
    
    /** 病毒扫描通过次数 */
    private final AtomicInteger virusScanCount = new AtomicInteger(0);
    
    @Override
    public void recordUnzipTime(long milliseconds) {
        totalUnzipTime.addAndGet(milliseconds);
    }
    
    @Override
    public void recordUnzipSize(long bytes) {
        totalUnzipSize.addAndGet(bytes);
    }
    
    @Override
    public void recordError(UnzipErrorCode errorCode) {
        errorCount.incrementAndGet();
        log.error("解压错误: {}", errorCode);
    }
    
    @Override
    public void recordSuccess() {
        successCount.incrementAndGet();
    }
    
    @Override
    public void recordMemoryUsage(long bytes) {
        long currentPeak = peakMemoryUsage.get();
        while (bytes > currentPeak) {
            if (peakMemoryUsage.compareAndSet(currentPeak, bytes)) {
                break;
            }
            currentPeak = peakMemoryUsage.get();
        }
    }
    
    @Override
    public void recordFileCount(int count) {
        totalFiles.addAndGet(count);
    }
    
    @Override
    public void recordUnzipSpeed(long bytesPerSecond) {
        totalUnzipSpeed.addAndGet(bytesPerSecond);
        speedCount.incrementAndGet();
    }
    
    @Override
    public void recordPeakMemoryUsage(long bytes) {
        peakMemoryUsage.set(bytes);
    }
    
    @Override
    public void recordConcurrentTasks(int count) {
        int currentMax = maxConcurrentTasks.get();
        while (count > currentMax) {
            if (maxConcurrentTasks.compareAndSet(currentMax, count)) {
                break;
            }
            currentMax = maxConcurrentTasks.get();
        }
    }
    
    @Override
    public void recordChecksumValidation(boolean isValid) {
        if (isValid) {
            checksumValidationCount.incrementAndGet();
        }
    }
    
    @Override
    public void recordVirusScan(boolean isClean) {
        if (isClean) {
            virusScanCount.incrementAndGet();
        }
    }
    
    @Override
    public UnzipMetricsSnapshot getSnapshot() {
        return UnzipMetricsSnapshot.builder()
            .totalUnzipTime(totalUnzipTime.get())
            .totalUnzipSize(totalUnzipSize.get())
            .totalFiles(totalFiles.get())
            .errorCount(errorCount.get())
            .successCount(successCount.get())
            .peakMemoryUsage(peakMemoryUsage.get())
            .averageUnzipSpeed(speedCount.get() > 0 ? totalUnzipSpeed.get() / speedCount.get() : 0)
            .maxConcurrentTasks(maxConcurrentTasks.get())
            .checksumValidationCount(checksumValidationCount.get())
            .virusScanCount(virusScanCount.get())
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    @Override
    public void persist() {
        // TODO: 实现监控数据持久化
        log.info("保存监控数据快照: {}", getSnapshot());
    }
    
    @Override
    public void reset() {
        totalUnzipTime.set(0);
        totalUnzipSize.set(0);
        totalFiles.set(0);
        errorCount.set(0);
        successCount.set(0);
        peakMemoryUsage.set(0);
        totalUnzipSpeed.set(0);
        speedCount.set(0);
        maxConcurrentTasks.set(0);
        checksumValidationCount.set(0);
        virusScanCount.set(0);
    }
    
    @Override
    public void recordProcessingTime(long time) {
        recordUnzipTime(time);
    }
    
    @Override
    public void recordBytesProcessed(long bytes) {
        recordUnzipSize(bytes);
    }
    
    @Override
    public void recordFilesProcessed(int count) {
        recordFileCount(count);
    }
    
    @Override
    public void updatePeakMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        recordPeakMemoryUsage(runtime.totalMemory() - runtime.freeMemory());
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        UnzipMetricsSnapshot snapshot = getSnapshot();
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUnzipTime", snapshot.getTotalUnzipTime());
        metrics.put("totalUnzipSize", snapshot.getTotalUnzipSize());
        metrics.put("totalFiles", snapshot.getTotalFiles());
        metrics.put("errorCount", snapshot.getErrorCount());
        metrics.put("successCount", snapshot.getSuccessCount());
        metrics.put("peakMemoryUsage", snapshot.getPeakMemoryUsage());
        metrics.put("averageUnzipSpeed", snapshot.getAverageUnzipSpeed());
        metrics.put("maxConcurrentTasks", snapshot.getMaxConcurrentTasks());
        return metrics;
    }
} 