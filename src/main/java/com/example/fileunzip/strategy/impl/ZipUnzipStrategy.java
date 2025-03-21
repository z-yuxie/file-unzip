package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.format.CompressionFormat;
import com.example.fileunzip.format.CompressionFormatDetector;
import net.sf.sevenzipjbinding.*;

/**
 * ZIP格式解压策略
 */
public class ZipUnzipStrategy extends AbstractArchiveUnzipStrategy {
    
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
    protected String getTempFileExtension() {
        return ".zip";
    }
    
    @Override
    protected IInArchive openArchive(IInStream inStream) throws SevenZipException {
        return SevenZip.openInArchive(ArchiveFormat.ZIP, inStream);
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.ZIP};
    }
} 