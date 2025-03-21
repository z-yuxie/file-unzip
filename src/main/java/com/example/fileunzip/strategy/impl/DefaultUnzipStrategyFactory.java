package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.strategy.UnzipStrategyFactory;
import com.example.fileunzip.util.CompressionFormatDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认解压策略工厂实现类
 */
@Slf4j
public class DefaultUnzipStrategyFactory implements UnzipStrategyFactory {
    
    private final UnzipConfig unzipConfig;
    private final Map<CompressionFormatDetector.CompressionFormat, UnzipStrategy> strategies;
    
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
        initializeDefaultStrategies();
    }
    
    private void initializeDefaultStrategies() {
        // 注册默认策略
        registerStrategy(CompressionFormatDetector.CompressionFormat.ZIP, new ZipUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.RAR, new RarUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR, new TarUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR_GZ, new TarUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR_BZ2, new TarUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR_XZ, new TarUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.SEVEN_ZIP, new SevenZipUnzipStrategy(unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.GZIP, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.GZIP, unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.BZIP2, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.BZIP2, unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.XZ, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.XZ, unzipConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.LZMA, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.LZMA, unzipConfig));
    }
    
    @Override
    public void registerStrategy(CompressionFormatDetector.CompressionFormat format, UnzipStrategy strategy) {
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
    public UnzipStrategy getStrategy(CompressionFormatDetector.CompressionFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        UnzipStrategy strategy = strategies.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的压缩格式: " + format);
        }
        return strategy;
    }
    
    @Override
    public void removeStrategy(CompressionFormatDetector.CompressionFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        UnzipStrategy strategy = strategies.remove(format);
        if (strategy != null) {
            log.info("移除解压策略: {} -> {}", format, strategy.getClass().getSimpleName());
        }
    }
    
    @Override
    public boolean supportsFormat(CompressionFormatDetector.CompressionFormat format) {
        return format != null && strategies.containsKey(format);
    }
    
    /**
     * 创建解压策略
     *
     * @param format 压缩格式
     * @return 解压策略
     */
    public UnzipStrategy createStrategy(CompressionFormatDetector.CompressionFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        
        UnzipStrategy strategy = strategies.get(format);
        if (strategy != null) {
            return strategy;
        }
        
        switch (format) {
            case ZIP:
                return new ZipUnzipStrategy(unzipConfig);
            case RAR:
                return new RarUnzipStrategy(unzipConfig);
            case TAR:
            case TAR_GZ:
            case TAR_BZ2:
            case TAR_XZ:
                return new TarUnzipStrategy(unzipConfig);
            case SEVEN_ZIP:
                return new SevenZipUnzipStrategy(unzipConfig);
            case GZIP:
            case BZIP2:
            case XZ:
            case LZMA:
            default:
                return new CompressedFileUnzipStrategy(format, unzipConfig);
        }
    }
} 