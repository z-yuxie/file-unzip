package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * LZ4格式解压策略实现
 */
public class Lz4UnzipStrategy extends CompressedFileUnzipStrategy {
    
    public Lz4UnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.LZ4;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.LZ4};
    }
    
    @Override
    protected CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException {
        return new BlockLZ4CompressorInputStream(inputStream);
    }
} 