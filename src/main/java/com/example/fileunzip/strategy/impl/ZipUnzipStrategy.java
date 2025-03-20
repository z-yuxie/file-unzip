package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.util.CompressionFormatDetector;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ZIP格式解压策略
 */
public class ZipUnzipStrategy extends AbstractArchiveUnzipStrategy {
    
    public ZipUnzipStrategy(UnzipConfig unzipConfig, SecurityConfig securityConfig) {
        super(unzipConfig, securityConfig);
    }
    
    @Override
    protected IInArchive openArchive(IInStream inStream) throws SevenZipException {
        return SevenZip.openInArchive(ArchiveFormat.ZIP, inStream);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format) {
        return format == CompressionFormatDetector.CompressionFormat.ZIP;
    }
    
    @Override
    protected String getTempFileExtension() {
        return ".zip";
    }

    @Override
    public CompressionFormatDetector.CompressionFormat[] getSupportedFormats() {
        return new CompressionFormatDetector.CompressionFormat[]{
            CompressionFormatDetector.CompressionFormat.ZIP
        };
    }
} 