package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * RAR格式解压策略实现
 */
@Slf4j
public class RarUnzipStrategy extends AbstractSevenZipStrategy {
    
    public RarUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.RAR;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.RAR};
    }
} 