package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.util.CompressionFormatDetector;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.SevenZipException;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 7Z格式解压策略实现类
 * 使用7-Zip-JBinding库实现7Z格式的解压功能
 * 支持标准7Z压缩格式文件
 * 
 * 实现说明：
 * 1. 使用静态初始化块加载7-Zip-JBinding库
 * 2. 通过创建临时文件的方式处理字节数组输入
 * 3. 使用RandomAccessFile和IInArchive接口进行文件解压
 * 4. 支持提取文件元数据（路径、大小等）
 * 5. 自动清理临时文件
 */
public class SevenZipUnzipStrategy extends AbstractArchiveUnzipStrategy {
    
    public SevenZipUnzipStrategy(UnzipConfig unzipConfig, SecurityConfig securityConfig) {
        super(unzipConfig, securityConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format) {
        return format == CompressionFormatDetector.CompressionFormat.SEVEN_ZIP;
    }

    @Override
    protected String getTempFileExtension() {
        return ".7z";
    }

    @Override
    protected IInArchive openArchive(IInStream inStream) throws SevenZipException {
        return SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, inStream);
    }

    @Override
    public CompressionFormatDetector.CompressionFormat[] getSupportedFormats() {
        return new CompressionFormatDetector.CompressionFormat[]{
            CompressionFormatDetector.CompressionFormat.SEVEN_ZIP
        };
    }
} 