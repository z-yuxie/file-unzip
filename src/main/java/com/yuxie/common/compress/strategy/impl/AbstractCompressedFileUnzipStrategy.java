package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.format.CompressionFormatDetector;
import com.yuxie.common.compress.model.FileInfo;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * 单文件压缩格式解压策略的抽象基类
 */
public abstract class AbstractCompressedFileUnzipStrategy extends AbstractCommonsCompressStrategy {
    
    protected AbstractCompressedFileUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws Exception {
        // 单文件压缩格式不需要ArchiveInputStream
        return null;
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        if (inputStream == null) {
            throw new UnzipException("输入流不能为空");
        }
        
        try {
            // 创建解压输入流
            CompressorInputStream compressorInputStream = createCompressorInputStream(inputStream);
            
            // 读取解压后的数据
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[unzipConfig.getBufferSize()];
            int bytesRead;
            long totalBytesRead = 0;
            
            // 通知开始解压
            if (callback != null) {
                callback.onStart(0, 1);
            }
            
            while ((bytesRead = compressorInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // 检查文件大小限制
                if (unzipConfig.isEnableFileSizeCheck() && totalBytesRead > unzipConfig.getMaxFileSize()) {
                    throw new UnzipException("解压后的文件大小超过限制");
                }
                
                // 通知进度
                if (callback != null) {
                    callback.onProgress("decompressed", totalBytesRead, totalBytesRead, 1, 1);
                }
            }
            
            byte[] decompressedData = outputStream.toByteArray();
            
            // 检查是否为TAR格式
            CompressionFormat innerFormat = CompressionFormatDetector.detectFormat(new ByteArrayInputStream(decompressedData));
            if (innerFormat == CompressionFormat.TAR) {
                TarUnzipStrategy tarStrategy = new TarUnzipStrategy(unzipConfig);
                return tarStrategy.unzip(new ByteArrayInputStream(decompressedData), password, callback);
            }
            
            // 如果不是TAR格式，返回当前解压结果
            FileInfo fileInfo = FileInfo.builder()
                .fileName("decompressed")
                .path("decompressed")
                .size(totalBytesRead)
                .lastModified(System.currentTimeMillis())
                .build();

            // 通知完成
            if (callback != null) {
                callback.onComplete();
            }

            return Collections.singletonMap(fileInfo, decompressedData);

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException("解压失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建压缩输入流
     *
     * @param inputStream 输入流
     * @return 压缩输入流
     * @throws IOException 创建输入流失败时抛出
     */
    protected abstract CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException;
    
    @Override
    public void close() throws IOException {
        // 无需关闭资源
    }
} 