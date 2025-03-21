package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.format.CompressionFormat;
import com.example.fileunzip.format.CompressionFormatDetector;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 压缩文件解压策略
 * 用于处理单文件压缩格式，如GZIP、BZIP2等
 */
public class CompressedFileUnzipStrategy implements UnzipStrategy {
    
    private final UnzipConfig unzipConfig;
    
    public CompressedFileUnzipStrategy(UnzipConfig unzipConfig) {
        this.unzipConfig = unzipConfig;
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
            // 读取所有数据
            byte[] data;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[unzipConfig.getBufferSize()];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                data = outputStream.toByteArray();
            }
            
            // 检测压缩格式
            CompressionFormat format = CompressionFormatDetector.detect(data);
            if (format == CompressionFormat.UNKNOWN) {
                throw new UnzipException("不支持的压缩格式");
            }
            
            // 创建解压输入流并读取数据
            Map<FileInfo, byte[]> result = new HashMap<>();
            try (InputStream decompressor = CompressionFormatDetector.createDecompressor(data, format);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[unzipConfig.getBufferSize()];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = decompressor.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    // 检查文件大小限制
                    if (unzipConfig.isEnableFileSizeCheck() && totalBytesRead > unzipConfig.getMaxFileSize()) {
                        throw new UnzipException(
                            String.format("文件大小超过限制: %d > %d", totalBytesRead, unzipConfig.getMaxFileSize()));
                    }
                    
                    // 通知进度
                    if (callback != null) {
                        callback.onProgress("compressed_file", totalBytesRead, -1, 1, 1);
                    }
                }
                
                // 创建结果
                byte[] content = outputStream.toByteArray();
                
                // 检查解压后的文件大小
                if (unzipConfig.isEnableFileSizeCheck() && content.length > unzipConfig.getMaxFileSize()) {
                    throw new UnzipException(
                        String.format("解压后文件大小超过限制: %d > %d", content.length, unzipConfig.getMaxFileSize()));
                }
                
                FileInfo fileInfo = FileInfo.builder()
                    .fileName("compressed_file")
                    .path("compressed_file")
                    .size(content.length)
                    .lastModified(System.currentTimeMillis())
                    .build();
                
                result.put(fileInfo, content);
            }
            
            // 通知完成
            if (callback != null) {
                callback.onComplete();
            }
            
            return result;
            
        } catch (IOException e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException("解压文件失败", e);
        }
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{
            CompressionFormat.GZIP,
            CompressionFormat.BZIP2,
            CompressionFormat.XZ,
            CompressionFormat.LZMA,
            CompressionFormat.SNAPPY,
            CompressionFormat.LZ4
        };
    }
    
    @Override
    public void close() throws IOException {
        // 无需关闭资源
    }
} 