package com.yuxie.common.compress.strategy;

import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.exception.UnzipException;

/**
 * 解压策略工厂接口
 * <p>
 * 该接口定义了管理不同压缩格式解压策略的工厂方法。
 * 采用工厂模式实现，用于创建和管理各种压缩格式（如ZIP、RAR、7Z等）的解压策略。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>策略注册：支持动态注册新的解压策略</li>
 *   <li>策略获取：根据压缩格式获取对应的解压策略</li>
 *   <li>策略移除：支持移除已注册的解压策略</li>
 *   <li>格式支持检查：检查是否支持特定的压缩格式</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建工厂实例
 * UnzipStrategyFactory factory = new DefaultUnzipStrategyFactory(config);
 * 
 * // 注册自定义策略
 * factory.registerStrategy(CompressionFormat.ZIP, new CustomZipStrategy());
 * 
 * // 获取解压策略
 * UnzipStrategy strategy = factory.getStrategy(CompressionFormat.ZIP);
 * 
 * // 使用策略解压文件
 * Map<FileInfo, byte[]> result = strategy.unzip(inputStream);
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipStrategy
 * @see CompressionFormat
 */
public interface UnzipStrategyFactory {
    /**
     * 注册解压策略
     * <p>
     * 将指定的解压策略与压缩格式关联。
     * 如果该格式已存在策略，则会覆盖原有策略。
     * </p>
     *
     * @param format 压缩格式，不能为null
     * @param strategy 解压策略，不能为null
     * @throws IllegalArgumentException 当format或strategy为null时抛出
     */
    void registerStrategy(CompressionFormat format, UnzipStrategy strategy);

    /**
     * 获取解压策略
     * <p>
     * 根据指定的压缩格式获取对应的解压策略。
     * 如果找不到对应的策略，将抛出UnzipException异常。
     * </p>
     *
     * @param format 压缩格式，不能为null
     * @return 解压策略实例
     * @throws UnzipException 当不支持指定格式时抛出异常
     * @throws IllegalArgumentException 当format为null时抛出
     */
    UnzipStrategy getStrategy(CompressionFormat format) throws UnzipException;

    /**
     * 移除解压策略
     * <p>
     * 移除指定压缩格式的解压策略。
     * 如果该格式不存在策略，则不会有任何操作。
     * </p>
     *
     * @param format 要移除的压缩格式，不能为null
     * @throws IllegalArgumentException 当format为null时抛出
     */
    void removeStrategy(CompressionFormat format);

    /**
     * 检查是否支持指定的压缩格式
     * <p>
     * 检查工厂中是否已注册了指定格式的解压策略。
     * </p>
     *
     * @param format 要检查的压缩格式，不能为null
     * @return 如果支持该格式则返回true，否则返回false
     * @throws IllegalArgumentException 当format为null时抛出
     */
    boolean supportsFormat(CompressionFormat format);
} 