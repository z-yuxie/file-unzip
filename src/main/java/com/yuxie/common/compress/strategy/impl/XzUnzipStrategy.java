package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * XZ格式解压策略实现
 */
public class XzUnzipStrategy extends CompressedFileUnzipStrategy {
    
    public XzUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.XZ;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.XZ};
    }
    
    @Override
    protected CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException {
        return new XZCompressorInputStream(inputStream);
    }
} 