package com.example.fileunzip.monitor;

import com.example.fileunzip.exception.UnzipErrorCode;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.HashMap;

/**
 * 解压监控接口
 */
public interface UnzipMetrics {
    /**
     * 记录解压时间
     *
     * @param milliseconds 解压耗时（毫秒）
     */
    void recordUnzipTime(long milliseconds);

    /**
     * 记录解压大小
     *
     * @param bytes 解压的字节数
     */
    void recordUnzipSize(long bytes);

    /**
     * 记录错误
     *
     * @param errorCode 错误码
     */
    void recordError(UnzipErrorCode errorCode);

    /**
     * 记录成功
     */
    void recordSuccess();

    /**
     * 记录内存使用
     *
     * @param bytes 使用的内存字节数
     */
    void recordMemoryUsage(long bytes);

    /**
     * 记录文件数量
     *
     * @param count 文件数量
     */
    void recordFileCount(int count);
    
    /**
     * 记录解压速度
     *
     * @param bytesPerSecond 每秒解压的字节数
     */
    void recordUnzipSpeed(long bytesPerSecond);
    
    /**
     * 记录峰值内存使用
     *
     * @param bytes 峰值内存使用字节数
     */
    void recordPeakMemoryUsage(long bytes);
    
    /**
     * 记录并发解压任务数
     *
     * @param count 并发任务数
     */
    void recordConcurrentTasks(int count);
    
    /**
     * 记录校验和验证结果
     *
     * @param isValid 验证是否通过
     */
    void recordChecksumValidation(boolean isValid);
    
    /**
     * 记录病毒扫描结果
     *
     * @param isClean 是否安全
     */
    void recordVirusScan(boolean isClean);
    
    /**
     * 获取监控数据快照
     *
     * @return 监控数据快照
     */
    UnzipMetricsSnapshot getSnapshot();
    
    /**
     * 持久化监控数据
     */
    void persist();
    
    /**
     * 重置监控数据
     */
    void reset();

    void recordProcessingTime(long time);
    
    void recordBytesProcessed(long bytes);
    
    void recordFilesProcessed(int count);
    
    void updatePeakMemoryUsage();
    
    Map<String, Object> getMetrics();
} 