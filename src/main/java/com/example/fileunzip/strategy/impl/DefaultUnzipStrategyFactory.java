package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.strategy.UnzipStrategyFactory;
import com.example.fileunzip.util.CompressionFormatDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认解压策略工厂实现
 */
@Slf4j
public class DefaultUnzipStrategyFactory implements UnzipStrategyFactory {
    private final Map<CompressionFormatDetector.CompressionFormat, UnzipStrategy> strategies;
    private final UnzipConfig unzipConfig;
    private final SecurityConfig securityConfig;

    public DefaultUnzipStrategyFactory(UnzipConfig unzipConfig, SecurityConfig securityConfig) {
        this.unzipConfig = unzipConfig;
        this.securityConfig = securityConfig;
        this.strategies = new HashMap<>();
        initializeDefaultStrategies();
    }

    private void initializeDefaultStrategies() {
        // 注册默认策略
        registerStrategy(CompressionFormatDetector.CompressionFormat.ZIP, new ZipUnzipStrategy(unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.RAR, new RarUnzipStrategy(unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.SEVEN_ZIP, new SevenZipUnzipStrategy(unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR, new TarUnzipStrategy(unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR_GZ, new TarUnzipStrategy(unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR_BZ2, new TarUnzipStrategy(unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.TAR_XZ, new TarUnzipStrategy(unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.GZIP, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.GZIP, unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.BZIP2, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.BZIP2, unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.XZ, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.XZ, unzipConfig, securityConfig));
        registerStrategy(CompressionFormatDetector.CompressionFormat.LZMA, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.LZMA, unzipConfig, securityConfig));
    }

    @Override
    public void registerStrategy(CompressionFormatDetector.CompressionFormat format, UnzipStrategy strategy) {
        if (format == null || strategy == null) {
            throw new IllegalArgumentException("格式和策略不能为空");
        }
        strategies.put(format, strategy);
        log.info("注册解压策略: {} -> {}", format, strategy.getClass().getSimpleName());
    }

    @Override
    public UnzipStrategy getStrategy(CompressionFormatDetector.CompressionFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("格式不能为空");
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
            throw new IllegalArgumentException("格式不能为空");
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
} 