package com.yuxie.common.compress.monitor;

import com.yuxie.common.compress.exception.UnzipErrorCode;
import java.util.Map;

/**
 * 解压监控接口
 * 用于收集和记录解压过程中的各种指标，包括：
 * 1. 解压时间和速度
 * 2. 文件大小和数量
 * 3. 内存使用情况
 * 4. 错误和成功记录
 * 5. 并发任务数
 * 6. 安全验证结果
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

    /**
     * 记录处理时间
     *
     * @param time 处理时间（毫秒）
     */
    void recordProcessingTime(long time);
    
    /**
     * 记录已处理的字节数
     *
     * @param bytes 已处理的字节数
     */
    void recordBytesProcessed(long bytes);
    
    /**
     * 记录已处理的文件数量
     *
     * @param count 已处理的文件数量
     */
    void recordFilesProcessed(int count);
    
    /**
     * 更新峰值内存使用量
     * 将当前内存使用量与历史峰值进行比较，如果当前值更大则更新
     */
    void updatePeakMemoryUsage();
    
    /**
     * 获取所有监控指标
     *
     * @return 包含所有监控指标的 Map，key 为指标名称，value 为指标值
     */
    Map<String, Object> getMetrics();
} 