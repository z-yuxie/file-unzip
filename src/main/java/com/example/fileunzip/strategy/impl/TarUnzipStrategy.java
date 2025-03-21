package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.util.CompressionFormatDetector;
import net.sf.sevenzipjbinding.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * TAR格式解压策略实现类
 * 使用Apache Commons Compress库实现TAR格式的解压功能
 * 支持以下格式：
 * 1. TAR (.tar)
 * 2. TAR.GZ (.tar.gz, .tgz)
 * 3. TAR.BZ2 (.tar.bz2)
 * 4. TAR.XZ (.tar.xz)
 */
public class TarUnzipStrategy extends AbstractArchiveUnzipStrategy {

    /**
     * 构造函数
     *
     * @param unzipConfig 解压配置
     */
    public TarUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }

    @Override
    protected boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format) {
        return format == CompressionFormatDetector.CompressionFormat.TAR;
    }

    @Override
    protected String getTempFileExtension() {
        return ".tar";
    }

    @Override
    protected IInArchive openArchive(IInStream inStream) throws SevenZipException {
        return SevenZip.openInArchive(ArchiveFormat.TAR, inStream);
    }

    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException {
        return super.unzip(inputStream);
    }

    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException {
        return super.unzip(inputStream, password);
    }

    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException {
        return super.unzip(inputStream, callback);
    }

    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        return super.unzip(inputStream, password, callback);
    }

    @Override
    public CompressionFormatDetector.CompressionFormat[] getSupportedFormats() {
        return new CompressionFormatDetector.CompressionFormat[]{
            CompressionFormatDetector.CompressionFormat.TAR
        };
    }
    
    /**
     * 检查是否为TAR相关格式
     */
    private boolean isTarFormat(CompressionFormatDetector.CompressionFormat format) {
        return format == CompressionFormatDetector.CompressionFormat.TAR;
    }

    /**
     * 读取TAR条目内容
     */
    private byte[] readEntryContent(InputStream inputStream, long size) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[unzipConfig.getBufferSize()];
            int bytesRead;
            long remainingBytes = size;
            
            while (remainingBytes > 0 && (bytesRead = inputStream.read(buffer, 0, 
                    (int) Math.min(buffer.length, remainingBytes))) != -1) {
                bos.write(buffer, 0, bytesRead);
                remainingBytes -= bytesRead;
            }
            
            return bos.toByteArray();
        }
    }
} 