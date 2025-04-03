package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.format.CompressionFormatDetector;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.util.CompressionCompositeInputStream;
import lombok.Getter;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.function.Function;

/**
 * 单文件压缩格式解压策略实现
 * 支持以下格式：
 * 1. GZIP (.gz)
 * 2. BZIP2 (.bz2)
 * 3. XZ (.xz)
 * 4. LZMA (.lzma)
 * 5. SNAPPY (.snappy)
 * 6. LZ4 (.lz4)
 * <p>
 * 支持复合格式：
 * 1. TAR.GZ (.tar.gz)
 * 2. TAR.BZ2 (.tar.bz2)
 * 3. TAR.XZ (.tar.xz)
 */
public class CompressedFileUnzipStrategy extends AbstractCommonsCompressStrategy {
    
    /**
     * 支持的压缩格式及其对应的输入流创建函数
     */
    private enum SupportedFormat {
        GZIP(CompressionFormat.GZIP, input -> {
            try {
                return new GzipCompressorInputStream(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }),
        BZIP2(CompressionFormat.BZIP2, input -> {
            try {
                return new BZip2CompressorInputStream(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }),
        XZ(CompressionFormat.XZ, input -> {
            try {
                return new XZCompressorInputStream(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }),
        LZMA(CompressionFormat.LZMA, input -> {
            try {
                return new LZMACompressorInputStream(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }),
        SNAPPY(CompressionFormat.SNAPPY, input -> {
            try {
                return new SnappyCompressorInputStream(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }),
        LZ4(CompressionFormat.LZ4, BlockLZ4CompressorInputStream::new);
        
        @Getter
        private final CompressionFormat format;
        private final Function<InputStream, CompressorInputStream> factory;
        
        SupportedFormat(CompressionFormat format, Function<InputStream, CompressorInputStream> factory) {
            this.format = format;
            this.factory = factory;
        }

        public CompressorInputStream createInputStream(InputStream inputStream) {
            return factory.apply(inputStream);
        }
        
        public static SupportedFormat fromCompressionFormat(CompressionFormat format) {
            for (SupportedFormat supportedFormat : values()) {
                if (supportedFormat.format == format) {
                    return supportedFormat;
                }
            }
            return null;
        }
    }
    
    private final Map<CompressionFormat, SupportedFormat> formatMap;
    
    public CompressedFileUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
        this.formatMap = new EnumMap<>(CompressionFormat.class);
        for (SupportedFormat format : SupportedFormat.values()) {
            formatMap.put(format.getFormat(), format);
        }
    }
    
    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return formatMap.containsKey(format);
    }
    
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return Arrays.stream(SupportedFormat.values())
            .map(SupportedFormat::getFormat)
            .toArray(CompressionFormat[]::new);
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
            CompressorInputStream compressorInputStream = createCompressorInputStream(inputStream, CompressionFormat.GZIP);
            
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
     * @param format 压缩格式
     * @return 压缩输入流
     * @throws IOException 创建输入流失败时抛出
     */
    private CompressorInputStream createCompressorInputStream(InputStream inputStream, CompressionFormat format) throws IOException {
        SupportedFormat supportedFormat = formatMap.get(format);
        if (supportedFormat == null) {
            throw new IllegalArgumentException("不支持的压缩格式: " + format);
        }
        return supportedFormat.createInputStream(inputStream);
    }
    
    @Override
    public void close() throws IOException {
        // 无需关闭资源
    }
} 