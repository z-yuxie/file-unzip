package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import com.yuxie.common.compress.strategy.UnzipStrategyFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认解压策略工厂实现
 * <p>
 * 该工厂类实现了 {@link UnzipStrategyFactory} 接口，提供了默认的解压策略管理功能。
 * 在初始化时会自动注册所有支持的压缩格式解压策略，包括：
 * <ul>
 *   <li>ZIP、TAR、RAR、7Z 等归档格式</li>
 *   <li>GZIP、BZIP2、XZ、LZMA、SNAPPY、LZ4 等压缩格式</li>
 * </ul>
 * </p>
 * <p>
 * 主要特点：
 * <ul>
 *   <li>线程安全：使用 {@link HashMap} 存储策略，所有操作都是同步的</li>
 *   <li>可扩展性：支持动态注册和移除解压策略</li>
 *   <li>配置灵活：通过 {@link UnzipConfig} 统一管理解压配置</li>
 * </ul>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipStrategyFactory
 * @see UnzipStrategy
 * @see UnzipConfig
 */
@Slf4j
public class DefaultUnzipStrategyFactory implements UnzipStrategyFactory {
    
    /**
     * 存储压缩格式与解压策略的映射关系
     */
    private final Map<CompressionFormat, UnzipStrategy> strategyMap;
    
    /**
     * 构造函数
     * <p>
     * 初始化工厂并注册所有支持的解压策略。
     * </p>
     *
     * @param unzipConfig 解压配置，不能为null
     * @throws IllegalArgumentException 当unzipConfig为null时抛出
     */
    public DefaultUnzipStrategyFactory(UnzipConfig unzipConfig) {
        this.strategyMap = new HashMap<>();
        registerStrategies(unzipConfig);
    }
    
    /**
     * 注册所有支持的解压策略
     * <p>
     * 在工厂初始化时调用，注册所有默认支持的压缩格式解压策略。
     * </p>
     *
     * @param unzipConfig 解压配置，不能为null
     */
    private void registerStrategies(UnzipConfig unzipConfig) {
        // 注册ZIP格式策略
        strategyMap.put(CompressionFormat.ZIP, new ZipUnzipStrategy(unzipConfig));
        
        // 注册TAR格式策略
        strategyMap.put(CompressionFormat.TAR, new TarUnzipStrategy(unzipConfig));
        
        // 注册RAR格式策略
        strategyMap.put(CompressionFormat.RAR, new RarUnzipStrategy(unzipConfig));
        
        // 注册7Z格式策略
        strategyMap.put(CompressionFormat.SEVEN_ZIP, new SevenZipUnzipStrategy(unzipConfig));
        
        // 注册压缩格式策略
        strategyMap.put(CompressionFormat.GZIP, new GzipUnzipStrategy(unzipConfig));
        strategyMap.put(CompressionFormat.BZIP2, new Bzip2UnzipStrategy(unzipConfig));
        strategyMap.put(CompressionFormat.XZ, new XzUnzipStrategy(unzipConfig));
        strategyMap.put(CompressionFormat.LZMA, new LzmaUnzipStrategy(unzipConfig));
        strategyMap.put(CompressionFormat.SNAPPY, new SnappyUnzipStrategy(unzipConfig));
        strategyMap.put(CompressionFormat.LZ4, new Lz4UnzipStrategy(unzipConfig));
    }
    
    /**
     * 获取解压策略
     * <p>
     * 根据指定的压缩格式获取对应的解压策略。
     * 如果找不到对应的策略，将返回null。
     * </p>
     *
     * @param format 压缩格式，不能为null
     * @return 解压策略实例，如果未找到则返回null
     * @throws IllegalArgumentException 当format为null时抛出
     */
    @Override
    public UnzipStrategy getStrategy(CompressionFormat format) {
        return strategyMap.get(format);
    }
    
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
    @Override
    public boolean supportsFormat(CompressionFormat format) {
        return strategyMap.containsKey(format);
    }
    
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
    @Override
    public void removeStrategy(CompressionFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        UnzipStrategy strategy = strategyMap.remove(format);
        if (strategy != null) {
            log.info("移除解压策略: {} -> {}", format, strategy.getClass().getSimpleName());
        }
    }
    
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
    @Override
    public void registerStrategy(CompressionFormat format, UnzipStrategy strategy) {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("解压策略不能为空");
        }
        strategyMap.put(format, strategy);
    }
} 