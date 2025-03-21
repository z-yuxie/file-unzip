package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import com.yuxie.common.compress.strategy.UnzipStrategyFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认解压策略工厂实现
 */
@Slf4j
public class DefaultUnzipStrategyFactory implements UnzipStrategyFactory {
    
    private final Map<CompressionFormat, UnzipStrategy> strategies;
    private final UnzipConfig unzipConfig;
    
    /**
     * 构造函数
     *
     * @param unzipConfig 解压配置
     */
    public DefaultUnzipStrategyFactory(UnzipConfig unzipConfig) {
        if (unzipConfig == null) {
            throw new IllegalArgumentException("解压配置不能为空");
        }
        this.unzipConfig = unzipConfig;
        this.strategies = new HashMap<>();
        initDefaultStrategies();
    }
    
    private void initDefaultStrategies() {
        // 注册基本格式策略
        registerStrategy(CompressionFormat.ZIP, new ZipUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormat.RAR, new RarUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormat.SEVEN_ZIP, new SevenZipUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormat.TAR, new TarUnzipStrategy(unzipConfig));
        
        // 注册复合格式策略
        CompoundArchiveUnzipStrategy compoundStrategy = new CompoundArchiveUnzipStrategy(unzipConfig);
        for (CompressionFormat format : compoundStrategy.getSupportedFormats()) {
            registerStrategy(format, compoundStrategy);
        }
        
        // 注册单文件压缩格式策略
        CompressedFileUnzipStrategy compressedStrategy = new CompressedFileUnzipStrategy(unzipConfig);
        for (CompressionFormat format : compressedStrategy.getSupportedFormats()) {
            registerStrategy(format, compressedStrategy);
        }
    }
    
    @Override
    public void registerStrategy(CompressionFormat format, UnzipStrategy strategy) {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("解压策略不能为空");
        }
        strategies.put(format, strategy);
        log.info("注册解压策略: {} -> {}", format, strategy.getClass().getSimpleName());
    }
    
    @Override
    public UnzipStrategy getStrategy(CompressionFormat format) throws UnzipException {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        UnzipStrategy strategy = strategies.get(format);
        if (strategy == null) {
            throw new UnzipException("不支持的压缩格式: " + format);
        }
        return strategy;
    }
    
    @Override
    public void removeStrategy(CompressionFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        UnzipStrategy strategy = strategies.remove(format);
        if (strategy != null) {
            log.info("移除解压策略: {} -> {}", format, strategy.getClass().getSimpleName());
        }
    }
    
    @Override
    public boolean supportsFormat(CompressionFormat format) {
        if (format == null) {
            return false;
        }
        return strategies.containsKey(format);
    }
    
    /**
     * 创建解压策略
     *
     * @param format 压缩格式
     * @return 解压策略
     * @throws UnzipException 当不支持指定格式时抛出异常
     */
    public UnzipStrategy createStrategy(CompressionFormat format) throws UnzipException {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        
        UnzipStrategy strategy = strategies.get(format);
        if (strategy != null) {
            return strategy;
        }

        return switch (format) {
            case ZIP -> new ZipUnzipStrategy(unzipConfig);
            case RAR -> new RarUnzipStrategy(unzipConfig);
            case TAR, TAR_GZ, TAR_BZ2, TAR_XZ -> new TarUnzipStrategy(unzipConfig);
            case SEVEN_ZIP -> new SevenZipUnzipStrategy(unzipConfig);
            default -> new CompressedFileUnzipStrategy(unzipConfig);
        };
    }
} 