package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.InputStream;

/**
 * TAR格式解压策略实现
 */
public class TarUnzipStrategy extends AbstractCommonsCompressStrategy {
    
    public TarUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.TAR;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.TAR};
    }
    
    @Override
    protected ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws Exception {
        return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, inputStream);
    }
} 