package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.InputStream;

/**
 * ZIP格式解压策略实现
 */
public class ZipUnzipStrategy extends AbstractCommonsCompressStrategy {
    
    /**
     * 构造函数
     *
     * @param unzipConfig 解压配置
     */
    public ZipUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.ZIP;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.ZIP};
    }
    
    @Override
    protected ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws Exception {
        return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
    }
} 