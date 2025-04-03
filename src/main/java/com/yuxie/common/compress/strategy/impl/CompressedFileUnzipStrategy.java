package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * 单文件压缩格式解压策略实现
 * 支持以下格式：
 * 1. GZIP (.gz)
 * 2. BZIP2 (.bz2)
 * 3. XZ (.xz)
 */
public class CompressedFileUnzipStrategy extends AbstractCommonsCompressStrategy implements UnzipStrategy {
    
    public CompressedFileUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.GZIP ||
               format == CompressionFormat.BZIP2 ||
               format == CompressionFormat.XZ;
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{
            CompressionFormat.GZIP,
            CompressionFormat.BZIP2,
            CompressionFormat.XZ
        };
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

        // 读取输入流数据
        byte[] data;
        try {
            data = readInputStream(inputStream);
        } catch (IOException e) {
            throw new UnzipException("读取输入流失败", e);
        }

        try (CompressorInputStream compressorInputStream = createCompressorInputStream(new ByteArrayInputStream(data))) {
            // 读取解压后的数据
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[unzipConfig.getBufferSize()];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = compressorInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // 检查文件大小
                if (unzipConfig.isEnableFileSizeCheck() && totalBytesRead > unzipConfig.getMaxFileSize()) {
                    throw new UnzipException(
                        String.format("文件大小超过限制: %d > %d", totalBytesRead, unzipConfig.getMaxFileSize()));
                }

                // 更新进度
                if (callback != null) {
                    callback.onProgress("解压中", totalBytesRead, data.length, 1, 1);
                }
            }

            // 创建文件信息
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

            return Collections.singletonMap(fileInfo, outputStream.toByteArray());

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException("解压失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建压缩输入流
     */
    private CompressorInputStream createCompressorInputStream(InputStream inputStream) throws Exception {
        // 根据文件格式创建对应的压缩输入流
        if (isSupportedFormat(CompressionFormat.GZIP)) {
            return new GzipCompressorInputStream(inputStream);
        } else if (isSupportedFormat(CompressionFormat.BZIP2)) {
            return new BZip2CompressorInputStream(inputStream);
        } else if (isSupportedFormat(CompressionFormat.XZ)) {
            return new XZCompressorInputStream(inputStream);
        } else {
            throw new IllegalArgumentException("不支持的压缩格式");
        }
    }
    
    @Override
    public void close() throws IOException {
        // 无需关闭资源
    }
} 