package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.util.CompressionFormatDetector;
import net.sf.sevenzipjbinding.IInArchive;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * TAR格式解压策略实现类
 * 使用Apache Commons Compress库实现TAR格式的解压功能
 * 支持以下格式：
 * 1. TAR (.tar)
 * 2. TAR.GZ (.tar.gz, .tgz)
 * 3. TAR.BZ2 (.tar.bz2)
 * 4. TAR.XZ (.tar.xz)
 */
public class TarUnzipStrategy extends AbstractArchiveUnzipStrategy {

    @Override
    protected boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format) {
        return isTarFormat(format);
    }

    @Override
    protected String getTempFileExtension() {
        return ".tar";
    }

    @Override
    protected IInArchive openArchive(RandomAccessFile randomAccessFile) throws IOException {
        throw new UnsupportedOperationException("TAR格式不支持通过7-Zip-JBinding处理");
    }

    @Override
    public Map<FileInfo, byte[]> unzip(byte[] data) throws IOException {
        Map<FileInfo, byte[]> result = new HashMap<>();
        
        // 检测压缩格式
        CompressionFormatDetector.CompressionFormat format = CompressionFormatDetector.detect(data);
        
        // 验证是否为TAR相关格式
        if (!isTarFormat(format)) {
            throw new IllegalArgumentException("不支持的文件格式: " + format);
        }
        
        // 创建TAR输入流
        try (TarArchiveInputStream tarStream = new TarArchiveInputStream(
                CompressionFormatDetector.createDecompressor(data, format))) {
            
            TarArchiveEntry entry;
            while ((entry = tarStream.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                
                // 读取文件内容
                byte[] content = readEntryContent(tarStream, entry.getSize());
                
                // 创建文件信息
                FileInfo fileInfo = FileInfo.builder()
                    .fileName(entry.getName())
                    .size(entry.getSize())
                    .lastModified(entry.getModTime().getTime())
                    .path(entry.getName())
                    .build();
                
                result.put(fileInfo, content);
            }
        }
        return result;
    }

    @Override
    public CompressionFormatDetector.CompressionFormat[] getSupportedFormats() {
        return new CompressionFormatDetector.CompressionFormat[]{
            CompressionFormatDetector.CompressionFormat.TAR,
            CompressionFormatDetector.CompressionFormat.TAR_GZ,
            CompressionFormatDetector.CompressionFormat.TAR_BZ2,
            CompressionFormatDetector.CompressionFormat.TAR_XZ
        };
    }
    
    /**
     * 检查是否为TAR相关格式
     */
    private boolean isTarFormat(CompressionFormatDetector.CompressionFormat format) {
        return format == CompressionFormatDetector.CompressionFormat.TAR ||
               format == CompressionFormatDetector.CompressionFormat.TAR_GZ ||
               format == CompressionFormatDetector.CompressionFormat.TAR_BZ2 ||
               format == CompressionFormatDetector.CompressionFormat.TAR_XZ;
    }

    /**
     * 读取TAR条目内容
     */
    private byte[] readEntryContent(InputStream inputStream, long size) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            long remainingBytes = size;
            
            while (remainingBytes > 0 && (bytesRead = inputStream.read(buffer, 0, 
                    (int) Math.min(buffer.length, remainingBytes))) != -1) {
                bos.write(buffer, 0, bytesRead);
                remainingBytes -= bytesRead;
            }
            
            return bos.toByteArray();
        }
    }
} 