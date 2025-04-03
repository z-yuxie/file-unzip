package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipErrorCode;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.format.CompressionFormatDetector;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.util.CompressionCompositeInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * 压缩格式解压策略的抽象基类
 * <p>
 * 该抽象类继承自 {@link AbstractCommonsCompressStrategy}，专门用于处理单一压缩格式（如GZIP、BZIP2等）的解压。
 * 与归档格式不同，压缩格式通常只包含单个文件，解压后直接得到原始数据。
 * </p>
 * <p>
 * 主要特点：
 * <ul>
 *   <li>单一文件处理：每次解压只处理一个文件</li>
 *   <li>格式检测：支持检测解压后的数据是否为TAR格式</li>
 *   <li>自动转换：如果解压后的数据是TAR格式，会自动使用TAR策略进行二次解压</li>
 *   <li>进度报告：支持通过回调接口报告解压进度</li>
 * </ul>
 * </p>
 * <p>
 * 子类需要实现：
 * <ul>
 *   <li>{@link #createCompressorInputStream(InputStream)} 方法，创建特定格式的压缩输入流</li>
 * </ul>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see AbstractCommonsCompressStrategy
 * @see UnzipConfig
 * @see UnzipProgressCallback
 * @see CompressionCompositeInputStream
 */
@Slf4j
public abstract class AbstractCompressedUnzipStrategy extends AbstractCommonsCompressStrategy {
    
    /**
     * 支持的压缩格式
     */
    private final CompressionFormat supportedFormat;
    
    /**
     * 构造函数
     * <p>
     * 初始化解压策略，设置解压配置和支持的压缩格式。
     * </p>
     *
     * @param unzipConfig 解压配置，不能为null
     * @param supportedFormat 支持的压缩格式，不能为null
     * @throws IllegalArgumentException 当unzipConfig或supportedFormat为null时抛出
     */
    protected AbstractCompressedUnzipStrategy(UnzipConfig unzipConfig, CompressionFormat supportedFormat) {
        super(unzipConfig);
        this.supportedFormat = supportedFormat;
    }
    
    /**
     * 检查是否支持指定的压缩格式
     * <p>
     * 判断当前策略是否支持解压指定格式的压缩文件。
     * </p>
     *
     * @param format 要检查的压缩格式
     * @return 如果支持该格式则返回true，否则返回false
     */
    @Override
    public boolean isSupportedFormat(CompressionFormat format) {
        return format == supportedFormat;
    }
    
    /**
     * 获取支持的压缩格式列表
     * <p>
     * 返回当前策略支持的所有压缩格式。
     * </p>
     *
     * @return 支持的压缩格式数组，不会返回null
     */
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{supportedFormat};
    }
    
    /**
     * 创建归档输入流
     * <p>
     * 压缩格式不需要归档输入流，因此返回null。
     * </p>
     *
     * @param inputStream 输入流
     * @return 始终返回null
     * @throws Exception 不会抛出异常
     */
    @Override
    protected ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws Exception {
        // 压缩格式不需要ArchiveInputStream
        return null;
    }
    
    /**
     * 解压文件（使用复合输入流，完整版本）
     * <p>
     * 使用已封装的复合输入流、密码进行解压，并通过回调接口报告进度。
     * 如果解压后的数据是TAR格式，会自动使用TAR策略进行二次解压。
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
            // 创建压缩输入流
            CompressorInputStream compressorInputStream = createCompressorInputStream(compositeInputStream);
            
            // 读取解压后的数据
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[unzipConfig.getBufferSize()];
            int bytesRead;
            long totalBytesRead = 0;
            
            // 通知开始解压
            if (callback != null) {
                callback.onStart(compositeInputStream.available(), 1);
            }
            
            while ((bytesRead = compressorInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // 检查文件大小限制
                if (unzipConfig.isEnableFileSizeCheck() && totalBytesRead > unzipConfig.getMaxFileSize()) {
                    throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "解压后的文件大小超过限制");
                }
                
                // 通知进度
                if (callback != null) {
                    callback.onProgress("decompressed", totalBytesRead, compositeInputStream.available(), 1, 1);
                }
            }
            
            byte[] decompressedData = outputStream.toByteArray();
            
            // 检查是否为TAR格式
            CompressionFormat innerFormat = CompressionFormatDetector.detectFormat(decompressedData);
            if (innerFormat == CompressionFormat.TAR) {
                TarUnzipStrategy tarStrategy = new TarUnzipStrategy(unzipConfig);
                return tarStrategy.unzipWithCompositeStream(new CompressionCompositeInputStream(new ByteArrayInputStream(decompressedData)), password, callback);
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
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "解压失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建压缩输入流
     * <p>
     * 子类必须实现此方法，根据输入流创建对应格式的压缩输入流。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @return 压缩输入流实例
     * @throws IOException 当创建输入流失败时抛出
     */
    protected abstract CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException;
} 