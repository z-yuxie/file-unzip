package com.example.fileunzip.monitor;

import com.example.fileunzip.exception.UnzipErrorCode;

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
} 