package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.format.CompressionFormatDetector;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 复合压缩格式解压策略
 * 用于处理tar.gz、tar.bz2、tar.xz等复合压缩格式
 */
public class CompoundArchiveUnzipStrategy implements UnzipStrategy {
    
    private final UnzipConfig unzipConfig;
    
    public CompoundArchiveUnzipStrategy(UnzipConfig unzipConfig) {
        if (unzipConfig == null) {
            throw new IllegalArgumentException("解压配置不能为空");
        }
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
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }
        
        try {
            // 读取所有数据
            byte[] data = IOUtils.toByteArray(inputStream);
            
            // 检查文件大小限制
            if (unzipConfig.isEnableFileSizeCheck() && data.length > unzipConfig.getMaxCompoundFileSize()) {
                throw new UnzipException(
                    String.format("复合压缩文件大小超过限制: %d > %d", data.length, unzipConfig.getMaxCompoundFileSize()));
            }
            
            // 检测压缩格式
            CompressionFormat format = CompressionFormatDetector.detect(data);
            if (!isSupportedFormat(format)) {
                throw new UnzipException("不支持的复合压缩格式: " + format);
            }
            
            // 创建解压链
            InputStream decompressor = new ByteArrayInputStream(data);
            if (callback != null) {
                callback.onStart(data.length, -1);
            }
            
            try {
                // 第一步：解压外层压缩格式
                decompressor = createFirstLevelDecompressor(decompressor, format);
                
                // 第二步：使用TAR解压策略处理解压后的数据
                TarUnzipStrategy tarStrategy = new TarUnzipStrategy(unzipConfig);
                Map<FileInfo, byte[]> result = tarStrategy.unzip(decompressor, password, callback);
                
                if (callback != null) {
                    callback.onComplete();
                }
                
                return result;
            } finally {
                decompressor.close();
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onError("解压失败: " + e.getMessage());
            }
            throw new UnzipException("解压失败", e);
        }
    }
    
    private InputStream createFirstLevelDecompressor(InputStream input, CompressionFormat format) throws IOException {
        switch (format) {
            case TAR_GZ:
                return new GzipCompressorInputStream(input);
            case TAR_BZ2:
                return new BZip2CompressorInputStream(input);
            case TAR_XZ:
                return new XZCompressorInputStream(input);
            default:
                throw new IllegalArgumentException("不支持的复合压缩格式: " + format);
        }
    }
    
    private boolean isSupportedFormat(CompressionFormat format) {
        if (format == null) {
            return false;
        }
        for (CompressionFormat supportedFormat : getSupportedFormats()) {
            if (supportedFormat == format) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{
            CompressionFormat.TAR_GZ,
            CompressionFormat.TAR_BZ2,
            CompressionFormat.TAR_XZ
        };
    }
    
    @Override
    public void close() throws IOException {
        // 无需实现，因为所有资源都在unzip方法中通过try-with-resources关闭
    }
} 