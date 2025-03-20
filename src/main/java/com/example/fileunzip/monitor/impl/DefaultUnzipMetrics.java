package com.example.fileunzip.monitor.impl;

import com.example.fileunzip.exception.UnzipErrorCode;
import com.example.fileunzip.monitor.UnzipMetrics;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认解压监控实现
 */
@Slf4j
public class DefaultUnzipMetrics implements UnzipMetrics {
    private long totalUnzipTime;
    private long totalUnzipSize;
    private long totalMemoryUsage;
    private int totalFileCount;
    private int errorCount;
    private int successCount;

    @Override
    public void recordUnzipTime(long milliseconds) {
        this.totalUnzipTime += milliseconds;
        log.debug("记录解压时间: {}ms", milliseconds);
    }

    @Override
    public void recordUnzipSize(long bytes) {
        this.totalUnzipSize += bytes;
        log.debug("记录解压大小: {} bytes", bytes);
    }

    @Override
    public void recordError(UnzipErrorCode errorCode) {
        this.errorCount++;
        log.error("记录错误: {}", errorCode.getMessage());
    }

    @Override
    public void recordSuccess() {
        this.successCount++;
        log.debug("记录成功");
    }

    @Override
    public void recordMemoryUsage(long bytes) {
        this.totalMemoryUsage += bytes;
        log.debug("记录内存使用: {} bytes", bytes);
    }

    @Override
    public void recordFileCount(int count) {
        this.totalFileCount += count;
        log.debug("记录文件数量: {}", count);
    }

    public long getTotalUnzipTime() {
        return totalUnzipTime;
    }

    public long getTotalUnzipSize() {
        return totalUnzipSize;
    }

    public long getTotalMemoryUsage() {
        return totalMemoryUsage;
    }

    public int getTotalFileCount() {
        return totalFileCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void reset() {
        this.totalUnzipTime = 0;
        this.totalUnzipSize = 0;
        this.totalMemoryUsage = 0;
        this.totalFileCount = 0;
        this.errorCount = 0;
        this.successCount = 0;
    }
} 