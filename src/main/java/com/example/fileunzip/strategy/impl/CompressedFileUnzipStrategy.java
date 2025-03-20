package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.exception.UnzipErrorCode;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.util.CompressionFormatDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import net.sf.sevenzipjbinding.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用压缩文件解压策略实现类
 * 使用Apache Commons Compress库实现各种单文件压缩格式的解压功能
 * 支持以下格式：
 * 1. GZIP (.gz)
 * 2. BZIP2 (.bz2)
 * 3. XZ (.xz)
 * 4. LZMA (.lzma)
 */
@Slf4j
public class CompressedFileUnzipStrategy extends AbstractArchiveUnzipStrategy {
    
    private final CompressionFormatDetector.CompressionFormat format;
    
    /**
     * 构造函数
     *
     * @param format 压缩格式（GZIP、BZIP2、XZ、LZMA）
     */
    public CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat format,
                                     UnzipConfig unzipConfig,
                                     SecurityConfig securityConfig) {
        super(unzipConfig, securityConfig);
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        if (!isSupportedCompressionFormat(format)) {
            throw new IllegalArgumentException("不支持的压缩格式: " + format);
        }
        this.format = format;
    }

    /**
     * 检查是否为支持的压缩格式
     */
    private boolean isSupportedCompressionFormat(CompressionFormatDetector.CompressionFormat format) {
        return format == CompressionFormatDetector.CompressionFormat.GZIP ||
               format == CompressionFormatDetector.CompressionFormat.BZIP2 ||
               format == CompressionFormatDetector.CompressionFormat.XZ ||
               format == CompressionFormatDetector.CompressionFormat.LZMA;
    }

    @Override
    protected boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format) {
        return this.format == format;
    }

    @Override
    protected String getTempFileExtension() {
        return getDefaultExtension();
    }

    @Override
    protected IInArchive openArchive(IInStream inStream) throws SevenZipException {
        throw new UnsupportedOperationException("单文件压缩格式不支持通过7-Zip-JBinding处理");
    }

    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException {
        return unzip(inputStream, null, null);
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException {
        return unzip(inputStream, password, null);
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException {
        return unzip(inputStream, null, callback);
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        try {
            // 创建压缩流
            CompressorInputStream compressorInputStream = createCompressorInputStream(inputStream);
            
            // 读取压缩数据
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[unzipConfig.getBufferSize()];
            int bytesRead;
            long totalBytesRead = 0;
            
            while ((bytesRead = compressorInputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                validateFileSize(totalBytesRead);
                outputStream.write(buffer, 0, bytesRead);
                
                // 通知进度
                if (callback != null) {
                    callback.onProgress("decompressed_file", totalBytesRead, totalBytesRead, 1, 1);
                }
            }
            
            // 创建文件信息
            FileInfo fileInfo = FileInfo.builder()
                .fileName("decompressed_file")
                .path("decompressed_file")
                .size(outputStream.size())
                .lastModified(System.currentTimeMillis())
                .build();
            
            Map<FileInfo, byte[]> result = new HashMap<>();
            result.put(fileInfo, outputStream.toByteArray());
            
            // 通知完成
            if (callback != null) {
                callback.onComplete();
            }
            
            return result;
        } catch (IOException e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "解压文件失败", e);
        }
    }

    private CompressorInputStream createCompressorInputStream(InputStream inputStream) throws UnzipException {
        try {
            CompressorStreamFactory factory = new CompressorStreamFactory();
            switch (format) {
                case GZIP:
                    return factory.createCompressorInputStream(CompressorStreamFactory.GZIP, inputStream);
                case BZIP2:
                    return factory.createCompressorInputStream(CompressorStreamFactory.BZIP2, inputStream);
                case XZ:
                    return factory.createCompressorInputStream(CompressorStreamFactory.XZ, inputStream);
                case LZMA:
                    return factory.createCompressorInputStream(CompressorStreamFactory.LZMA, inputStream);
                default:
                    throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "不支持的压缩格式: " + format);
            }
        } catch (Exception e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "创建压缩流失败", e);
        }
    }

    @Override
    public CompressionFormatDetector.CompressionFormat[] getSupportedFormats() {
        return new CompressionFormatDetector.CompressionFormat[]{format};
    }
    
    /**
     * 获取解压后的默认文件扩展名
     *
     * @return 默认文件扩展名
     */
    private String getDefaultExtension() {
        switch (format) {
            case GZIP:
                return ".gz";
            case BZIP2:
                return ".bz2";
            case XZ:
                return ".xz";
            case LZMA:
                return ".lzma";
            default:
                return ".compressed";
        }
    }
} 