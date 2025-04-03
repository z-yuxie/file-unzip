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
 */
@Slf4j
public class DefaultUnzipStrategyFactory implements UnzipStrategyFactory {
    
    private final UnzipConfig unzipConfig;
    private final Map<CompressionFormat, UnzipStrategy> strategyMap;
    
    public DefaultUnzipStrategyFactory(UnzipConfig unzipConfig) {
        this.unzipConfig = unzipConfig;
        this.strategyMap = new HashMap<>();
        initializeStrategies();
    }
    
    /**
     * 初始化解压策略
     */
    private void initializeStrategies() {
        // 注册ZIP解压策略
        registerStrategy(new ZipUnzipStrategy(unzipConfig));
        
        // 注册RAR解压策略
        registerStrategy(new RarUnzipStrategy(unzipConfig));
        
        // 注册7Z解压策略
        registerStrategy(new SevenZipUnzipStrategy(unzipConfig));
        
        // 注册TAR解压策略
        registerStrategy(new TarUnzipStrategy(unzipConfig));
        
        // 注册压缩文件解压策略
        registerStrategy(new CompressedFileUnzipStrategy(unzipConfig));
    }
    
    /**
     * 注册解压策略
     */
    private void registerStrategy(UnzipStrategy strategy) {
        for (CompressionFormat format : strategy.getSupportedFormats()) {
            registerStrategy(format, strategy);
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
        strategyMap.put(format, strategy);
        log.info("注册解压策略: {} -> {}", format, strategy.getClass().getSimpleName());
    }
    
    @Override
    public UnzipStrategy getStrategy(CompressionFormat format) {
        return strategyMap.get(format);
    }
    
    @Override
    public boolean supportsFormat(CompressionFormat format) {
        return strategyMap.containsKey(format);
    }
    
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
} 