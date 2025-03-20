package com.example.fileunzip.strategy;

import com.example.fileunzip.util.CompressionFormatDetector;

/**
 * 解压策略工厂接口
 */
public interface UnzipStrategyFactory {
    /**
     * 注册解压策略
     *
     * @param format 压缩格式
     * @param strategy 解压策略
     */
    void registerStrategy(CompressionFormatDetector.CompressionFormat format, UnzipStrategy strategy);

    /**
     * 获取解压策略
     *
     * @param format 压缩格式
     * @return 解压策略
     */
    UnzipStrategy getStrategy(CompressionFormatDetector.CompressionFormat format);

    /**
     * 移除解压策略
     *
     * @param format 压缩格式
     */
    void removeStrategy(CompressionFormatDetector.CompressionFormat format);

    /**
     * 检查是否支持指定的压缩格式
     *
     * @param format 压缩格式
     * @return 是否支持
     */
    boolean supportsFormat(CompressionFormatDetector.CompressionFormat format);
} 