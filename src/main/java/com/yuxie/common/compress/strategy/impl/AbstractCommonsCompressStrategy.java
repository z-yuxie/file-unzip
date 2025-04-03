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
 * <p>
 * 该抽象类实现了 {@link UnzipStrategy} 接口，提供了基于Apache Commons Compress库的通用解压功能。
 * 主要用于处理归档格式（如ZIP、TAR等）的解压，提供了统一的解压流程和资源管理。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>统一的解压流程：处理输入流、创建归档输入流、提取文件内容</li>
 *   <li>进度回调支持：通过 {@link UnzipProgressCallback} 报告解压进度</li>
 *   <li>资源管理：自动关闭输入流和输出流</li>
 *   <li>配置管理：通过 {@link UnzipConfig} 统一管理解压配置</li>
 * </ul>
 * </p>
 * <p>
 * 子类需要实现：
 * <ul>
 *   <li>{@link #createArchiveInputStream(InputStream)} 方法，创建特定格式的归档输入流</li>
 *   <li>{@link #isSupportedFormat(CompressionFormat)} 方法，指定支持的压缩格式</li>
 *   <li>{@link #getSupportedFormats()} 方法，返回支持的压缩格式列表</li>
 * </ul>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipStrategy
 * @see UnzipConfig
 * @see UnzipProgressCallback
 * @see CompressionCompositeInputStream
 */
@Slf4j
public abstract class AbstractCommonsCompressStrategy implements UnzipStrategy {
    
    /**
     * 解压配置
     */
    protected final UnzipConfig unzipConfig;
    
    /**
     * 构造函数
     * <p>
     * 初始化解压策略，设置解压配置。
     * </p>
     *
     * @param unzipConfig 解压配置，不能为null
     * @throws IllegalArgumentException 当unzipConfig为null时抛出
     */
    protected AbstractCommonsCompressStrategy(UnzipConfig unzipConfig) {
        if (unzipConfig == null) {
            throw new IllegalArgumentException("解压配置不能为空");
        }
        this.unzipConfig = unzipConfig;
    }
    
    /**
     * 检查是否支持指定的压缩格式
     * <p>
     * 默认实现返回false，子类需要重写此方法以指定支持的格式。
     * </p>
     *
     * @param format 要检查的压缩格式
     * @return 如果支持该格式则返回true，否则返回false
     */
    @Override
    public boolean isSupportedFormat(CompressionFormat format) {
        return false;
    }
    
    /**
     * 获取支持的压缩格式列表
     * <p>
     * 默认实现返回空数组，子类需要重写此方法以返回支持的格式列表。
     * </p>
     *
     * @return 支持的压缩格式数组
     */
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[0];
    }
    
    /**
     * 创建归档输入流
     * <p>
     * 子类必须实现此方法，根据输入流创建对应格式的归档输入流。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @return 归档输入流实例
     * @throws Exception 当创建失败时抛出异常
     */
    protected abstract ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws Exception;
    
    /**
     * 解压文件（基本版本）
     * <p>
     * 不带密码和进度回调的基本解压方法。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException {
        return unzip(inputStream, null, null);
    }
    
    /**
     * 解压文件（带密码）
     * <p>
     * 使用密码解压加密的压缩文件。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException {
        return unzip(inputStream, password, null);
    }
    
    /**
     * 解压文件（带进度回调）
     * <p>
     * 在解压过程中通过回调接口报告进度。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException {
        return unzip(inputStream, null, callback);
    }
    
    /**
     * 解压文件（完整版本）
     * <p>
     * 支持密码和进度回调的完整解压方法。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        if (inputStream == null) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "输入流不能为空");
        }

        try (CompressionCompositeInputStream compositeInputStream = new CompressionCompositeInputStream(inputStream)) {
            return unzipWithCompositeStream(compositeInputStream, password, callback);
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "解压失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取压缩包中的条目内容
     * <p>
     * 从归档输入流中读取指定条目的内容。
     * </p>
     *
     * @param archiveInputStream 归档输入流，不能为null
     * @param entry 归档条目，不能为null
     * @return 条目内容的字节数组
     * @throws IOException 当读取过程中发生IO错误时抛出
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

    /**
     * 关闭资源
     * <p>
     * 默认实现不需要额外清理资源。
     * </p>
     *
     * @throws IOException 当关闭资源时发生IO错误
     */
    @Override
    public void close() throws IOException {
        // 不需要额外清理资源
    }

    /**
     * 解压文件（使用复合输入流，基本版本）
     * <p>
     * 使用已封装的复合输入流进行解压，不带密码和进度回调。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream) throws UnzipException {
        return unzipWithCompositeStream(compositeInputStream, null, null);
    }

    /**
     * 解压文件（使用复合输入流，带进度回调）
     * <p>
     * 使用已封装的复合输入流进行解压，并通过回调接口报告进度。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, UnzipProgressCallback callback) throws UnzipException {
        return unzipWithCompositeStream(compositeInputStream, null, callback);
    }

    /**
     * 解压文件（使用复合输入流，带密码）
     * <p>
     * 使用已封装的复合输入流和密码进行解压。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, String password) throws UnzipException {
        return unzipWithCompositeStream(compositeInputStream, password, null);
    }

    /**
     * 解压文件（使用复合输入流，完整版本）
     * <p>
     * 使用已封装的复合输入流、密码进行解压，并通过回调接口报告进度。
     * 这是最完整的复合流解压方法，支持所有功能。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 当解压过程中发生错误时抛出
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        if (compositeInputStream == null) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "输入流不能为空");
        }

        try {
            // 创建归档输入流
            ArchiveInputStream archiveInputStream = createArchiveInputStream(compositeInputStream);
            if (archiveInputStream == null) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "不支持的压缩格式");
            }

            Map<FileInfo, byte[]> result = new HashMap<>();
            ArchiveEntry entry;
            int totalEntries = 0;
            int processedEntries = 0;

            // 通知开始解压
            if (callback != null) {
                callback.onStart(compositeInputStream.available(), 1);
            }

            while ((entry = archiveInputStream.getNextEntry()) != null) {
                totalEntries++;
                String entryName = entry.getName();
                
                // 检查文件大小限制
                if (unzipConfig.isEnableFileSizeCheck() && entry.getSize() > unzipConfig.getMaxFileSize()) {
                    throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "文件大小超过限制: " + entryName);
                }

                // 读取文件内容
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[unzipConfig.getBufferSize()];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = archiveInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 通知进度
                    if (callback != null) {
                        callback.onProgress(entryName, totalBytesRead, entry.getSize(), processedEntries + 1, totalEntries);
                    }
                }

                // 创建文件信息
                FileInfo fileInfo = FileInfo.builder()
                    .fileName(entryName)
                    .path(entryName)
                    .size(totalBytesRead)
                    .lastModified(entry.getLastModifiedDate().getTime())
                    .build();

                result.put(fileInfo, outputStream.toByteArray());
                processedEntries++;
            }

            // 通知完成
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
} 