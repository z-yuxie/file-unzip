package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.util.CompressionFormatDetector;
import net.sf.sevenzipjbinding.IInArchive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
public class CompressedFileUnzipStrategy extends AbstractArchiveUnzipStrategy {
    
    private final CompressionFormatDetector.CompressionFormat format;
    
    /**
     * 构造函数
     *
     * @param format 压缩格式（GZIP、BZIP2、XZ、LZMA）
     */
    public CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat format) {
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
        return "." + getDefaultExtension();
    }

    @Override
    protected IInArchive openArchive(RandomAccessFile randomAccessFile) throws IOException {
        throw new UnsupportedOperationException("单文件压缩格式不支持通过7-Zip-JBinding处理");
    }

    @Override
    public Map<FileInfo, byte[]> unzip(byte[] data) throws IOException {
        Map<FileInfo, byte[]> result = new HashMap<>();
        
        // 检查输入数据大小
        if (data.length > MAX_FILE_SIZE) {
            throw new UnzipException("文件大小超过限制: " + data.length + " > " + MAX_FILE_SIZE);
        }
        
        // 检测压缩格式
        CompressionFormatDetector.CompressionFormat detectedFormat = CompressionFormatDetector.detect(data);
        
        // 验证检测到的格式是否与预期一致
        if (detectedFormat != format) {
            throw new UnzipException("文件格式与预期不符: 预期=" + format + ", 实际=" + detectedFormat);
        }
        
        // 创建解压输入流
        try (InputStream decompressor = CompressionFormatDetector.createDecompressor(data, format);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            // 解压数据
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;
            
            while ((bytesRead = decompressor.read(buffer)) != -1) {
                // 检查解压后的文件大小
                totalBytesRead += bytesRead;
                if (totalBytesRead > MAX_FILE_SIZE) {
                    throw new UnzipException("解压后的文件大小超过限制: " + totalBytesRead + " > " + MAX_FILE_SIZE);
                }
                
                bos.write(buffer, 0, bytesRead);
            }
            
            // 创建文件信息
            FileInfo fileInfo = FileInfo.builder()
                .fileName("decompressed." + getDefaultExtension())
                .size(bos.size())
                .lastModified(System.currentTimeMillis())
                .path("decompressed." + getDefaultExtension())
                .build();
            
            result.put(fileInfo, bos.toByteArray());
        } catch (IOException e) {
            throw new UnzipException("解压文件失败: " + e.getMessage(), e);
        }
        return result;
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
                return "gz";
            case BZIP2:
                return "bz2";
            case XZ:
                return "xz";
            case LZMA:
                return "lzma";
            default:
                return "txt";
        }
    }
} 