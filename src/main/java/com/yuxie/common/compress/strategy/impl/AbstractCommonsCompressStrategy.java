package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipErrorCode;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import com.yuxie.common.compress.util.CompressionCompositeInputStream;
import com.yuxie.common.compress.util.UnzipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.*;
import java.util.*;

/**
 * 基于Apache Commons Compress的压缩文件解压抽象基类
 */
@Slf4j
public abstract class AbstractCommonsCompressStrategy implements UnzipStrategy {
    
    protected final UnzipConfig unzipConfig;
    
    /**
     * 构造函数
     *
     * @param unzipConfig 解压配置
     */
    protected AbstractCommonsCompressStrategy(UnzipConfig unzipConfig) {
        if (unzipConfig == null) {
            throw new IllegalArgumentException("解压配置不能为空");
        }
        this.unzipConfig = unzipConfig;
    }
    
    /**
     * 检查是否为支持的压缩格式
     *
     * @param format 压缩格式
     * @return 如果支持返回true，否则返回false
     */
    protected abstract boolean isSupportedFormat(CompressionFormat format);
    
    /**
     * 获取支持的压缩格式列表
     *
     * @return 支持的压缩格式数组
     */
    @Override
    public abstract CompressionFormat[] getSupportedFormats();
    
    /**
     * 创建ArchiveInputStream
     *
     * @param inputStream 输入流
     * @return ArchiveInputStream实例
     * @throws Exception 当创建失败时抛出异常
     */
    protected abstract ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws Exception;
    
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
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "输入流不能为空");
        }

        // 使用复合输入流管理资源
        CompressionCompositeInputStream compositeInputStream = new CompressionCompositeInputStream(inputStream);
        
        try (ArchiveInputStream archiveInputStream = createArchiveInputStream(compositeInputStream)) {
            // 获取所有条目
            List<ArchiveEntry> entries = new ArrayList<>();
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                entries.add(entry);
            }

            // 检查文件数量限制
            if (unzipConfig.isEnableFileCountCheck() && entries.size() > unzipConfig.getMaxFileCount()) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT,
                    String.format("文件数量超过限制: %d > %d", entries.size(), unzipConfig.getMaxFileCount()));
            }

            // 通知开始解压
            if (callback != null) {
                callback.onStart(compositeInputStream.available(), entries.size());
            }

            // 解压文件
            Map<FileInfo, byte[]> result = new HashMap<>();
            int currentFile = 0;
            long totalBytesRead = 0;

            for (ArchiveEntry archiveEntry : entries) {
                String path = archiveEntry.getName();
                if (path == null || path.trim().isEmpty()) {
                    continue;
                }

                // 安全检查
                UnzipUtils.validatePath(path, unzipConfig);
                UnzipUtils.validateFileType(path, unzipConfig);

                // 提取文件内容
                byte[] content = extractEntry(archiveInputStream, archiveEntry);
                if (content == null) {
                    continue;
                }

                // 检查文件大小
                UnzipUtils.validateFileSize(content.length, unzipConfig);

                // 创建文件信息
                FileInfo fileInfo = FileInfo.builder()
                    .fileName(new File(path).getName())
                    .path(path)
                    .size(content.length)
                    .lastModified(archiveEntry.getLastModifiedDate().getTime())
                    .build();

                // 添加到结果集
                result.put(fileInfo, content);

                // 更新进度
                currentFile++;
                totalBytesRead += content.length;
                if (callback != null) {
                    callback.onProgress(fileInfo.getFileName(), totalBytesRead, compositeInputStream.available(), currentFile, entries.size());
                }
            }

            // 通知解压完成
            if (callback != null) {
                callback.onComplete();
            }

            return result;

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "解压失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取压缩包中的条目内容
     */
    private byte[] extractEntry(ArchiveInputStream archiveInputStream, ArchiveEntry entry) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[unzipConfig.getBufferSize()];
        int bytesRead;
        
        while ((bytesRead = archiveInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        return outputStream.toByteArray();
    }

    @Override
    public void close() throws IOException {
        // 不需要额外清理资源
    }
} 