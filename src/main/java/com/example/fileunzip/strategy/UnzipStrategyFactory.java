package com.example.fileunzip.strategy;

import com.example.fileunzip.format.CompressionFormat;
import com.example.fileunzip.format.CompressionFormatDetector;
import com.example.fileunzip.exception.UnzipException;

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
    void registerStrategy(CompressionFormat format, UnzipStrategy strategy);

    /**
     * 获取解压策略
     *
     * @param format 压缩格式
     * @return 解压策略
     * @throws UnzipException 当不支持指定格式时抛出异常
     */
    UnzipStrategy getStrategy(CompressionFormat format) throws UnzipException;

    /**
     * 移除解压策略
     *
     * @param format 压缩格式
     */
    void removeStrategy(CompressionFormat format);

    /**
     * 检查是否支持指定的压缩格式
     *
     * @param format 压缩格式
     * @return 是否支持
     */
    boolean supportsFormat(CompressionFormat format);
} 