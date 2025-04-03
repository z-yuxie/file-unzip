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
        registerStrategies(unzipConfig);
    }
    
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
        
        // 注册复合格式策略
        strategyMap.put(CompressionFormat.COMPOUND, new CompoundArchiveUnzipStrategy(unzipConfig));
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