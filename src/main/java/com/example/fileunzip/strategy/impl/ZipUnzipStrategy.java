package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.util.CompressionFormatDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ZIP格式解压策略实现类
 * 使用Java内置的ZIP库实现ZIP格式的解压功能
 * 支持标准ZIP格式文件
 */
public class ZipUnzipStrategy implements UnzipStrategy {

    @Override
    public Map<FileInfo, byte[]> unzip(byte[] data) throws IOException {
        Map<FileInfo, byte[]> result = new HashMap<>();
        
        // 检测压缩格式
        CompressionFormatDetector.CompressionFormat format = CompressionFormatDetector.detect(data);
        
        // 验证是否为ZIP格式
        if (format != CompressionFormatDetector.CompressionFormat.ZIP) {
            throw new IllegalArgumentException("不支持的文件格式: " + format);
        }
        
        // 创建ZIP输入流
        try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                
                // 读取文件内容
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = zipStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                
                // 创建文件信息
                FileInfo fileInfo = FileInfo.builder()
                    .fileName(entry.getName())
                    .size(entry.getSize())
                    .lastModified(entry.getTime())
                    .path(entry.getName())
                    .build();
                
                result.put(fileInfo, bos.toByteArray());
            }
        }
        return result;
    }

    @Override
    public CompressionFormatDetector.CompressionFormat[] getSupportedFormats() {
        return new CompressionFormatDetector.CompressionFormat[]{
            CompressionFormatDetector.CompressionFormat.ZIP
        };
    }
} 