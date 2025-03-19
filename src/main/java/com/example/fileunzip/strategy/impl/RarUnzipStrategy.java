package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.util.CompressionFormatDetector;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * RAR格式解压策略实现类
 * 使用7-Zip-JBinding库实现RAR格式的解压功能
 * 支持标准RAR压缩格式文件
 * 
 * 实现说明：
 * 1. 使用静态初始化块加载7-Zip-JBinding库
 * 2. 通过自动检测压缩格式的方式处理字节数组输入
 * 3. 使用RandomAccessFile和IInArchive接口进行文件解压
 * 4. 支持提取文件元数据（路径、大小等）
 * 5. 自动清理临时文件
 */
public class RarUnzipStrategy extends AbstractArchiveUnzipStrategy {
    
    @Override
    public Map<FileInfo, byte[]> unzip(byte[] data) throws IOException {
        // 使用父类的通用实现
        return super.unzip(data);
    }

    @Override
    protected boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format) {
        return format == CompressionFormatDetector.CompressionFormat.RAR;
    }

    @Override
    protected String getTempFileExtension() {
        return ".rar";
    }

    @Override
    protected IInArchive openArchive(RandomAccessFile randomAccessFile) throws IOException {
        try {
            return SevenZip.openInArchive(ArchiveFormat.RAR, new RandomAccessFileInStream(randomAccessFile));
        } catch (SevenZipException e) {
            throw new UnzipException("打开RAR文件失败", e);
        }
    }

    @Override
    public CompressionFormatDetector.CompressionFormat[] getSupportedFormats() {
        return new CompressionFormatDetector.CompressionFormat[]{
            CompressionFormatDetector.CompressionFormat.RAR
        };
    }
} 