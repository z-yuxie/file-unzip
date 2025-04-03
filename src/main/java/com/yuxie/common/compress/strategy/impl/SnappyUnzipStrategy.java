package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * SNAPPY格式解压策略实现
 */
public class SnappyUnzipStrategy extends AbstractCompressedFileUnzipStrategy {
    
    public SnappyUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.SNAPPY;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.SNAPPY};
    }
    
    @Override
    protected CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException {
        return new SnappyCompressorInputStream(inputStream);
    }
} 