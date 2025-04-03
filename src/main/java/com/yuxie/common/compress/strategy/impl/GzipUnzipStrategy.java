package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * GZIP格式解压策略实现
 */
public class GzipUnzipStrategy extends CompressedFileUnzipStrategy {
    
    public GzipUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.GZIP;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.GZIP};
    }
    
    @Override
    protected CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException {
        return new GzipCompressorInputStream(inputStream);
    }
} 