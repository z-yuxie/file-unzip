package com.yuxie.common.compress.strategy;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.strategy.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认解压策略工厂类
 */
public class DefaultUnzipStrategyFactory implements UnzipStrategyFactory {
    
    private final Map<CompressionFormat, UnzipStrategy> strategyMap;
    
    public DefaultUnzipStrategyFactory(UnzipConfig unzipConfig) {
        this.strategyMap = new HashMap<>();
        
        // 注册支持的压缩格式
        registerStrategy(new ZipUnzipStrategy(unzipConfig));
        registerStrategy(new TarUnzipStrategy(unzipConfig));
        registerStrategy(new CompoundArchiveUnzipStrategy(unzipConfig));
        registerStrategy(new CompressedFileUnzipStrategy(unzipConfig));
        registerStrategy(new RarUnzipStrategy(unzipConfig));
        registerStrategy(new SevenZipUnzipStrategy(unzipConfig));
    }
    
    @Override
    public UnzipStrategy getStrategy(CompressionFormat format) {
        return strategyMap.get(format);
    }
    
    @Override
    public void registerStrategy(CompressionFormat format, UnzipStrategy strategy) {
        strategyMap.put(format, strategy);
    }
    
    @Override
    public void removeStrategy(CompressionFormat format) {
        strategyMap.remove(format);
    }
    
    @Override
    public boolean supportsFormat(CompressionFormat format) {
        return strategyMap.containsKey(format);
    }
    
    private void registerStrategy(UnzipStrategy strategy) {
        for (CompressionFormat format : strategy.getSupportedFormats()) {
            registerStrategy(format, strategy);
        }
    }
} 