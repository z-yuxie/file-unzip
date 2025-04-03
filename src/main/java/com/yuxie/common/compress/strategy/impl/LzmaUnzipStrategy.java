package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * LZMA格式解压策略实现
 */
public class LzmaUnzipStrategy extends AbstractCompressedFileUnzipStrategy {
    
    public LzmaUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.LZMA;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.LZMA};
    }
    
    @Override
    protected CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException {
        return new LZMACompressorInputStream(inputStream);
    }
} 