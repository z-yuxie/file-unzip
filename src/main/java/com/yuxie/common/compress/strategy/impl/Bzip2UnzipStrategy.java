package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * BZIP2格式解压策略实现
 */
public class Bzip2UnzipStrategy extends CompressedFileUnzipStrategy {
    
    public Bzip2UnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.BZIP2;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.BZIP2};
    }
    
    @Override
    protected CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException {
        return new BZip2CompressorInputStream(inputStream);
    }
} 