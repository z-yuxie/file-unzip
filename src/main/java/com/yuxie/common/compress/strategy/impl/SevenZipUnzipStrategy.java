package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * 7Z格式解压策略实现
 */
@Slf4j
public class SevenZipUnzipStrategy extends AbstractSevenZipStrategy {
    
    public SevenZipUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.SEVEN_ZIP;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.SEVEN_ZIP};
    }
} 