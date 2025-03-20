package com.example.fileunzip.util;

import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.exception.UnzipErrorCode;
import com.example.fileunzip.exception.UnzipException;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.Date;

/**
 * 解压工具类
 */
@Slf4j
public class UnzipUtils {
    
    private static final double MEMORY_THRESHOLD = 0.8; // 内存使用率阈值
    
    /**
     * 验证文件路径安全性
     */
    public static void validatePath(String path, SecurityConfig securityConfig) throws UnzipException {
        if (securityConfig.isPathTraversalCheckEnabled() && !isPathSafe(path)) {
            throw new UnzipException(UnzipErrorCode.SECURITY_ERROR, "检测到路径遍历攻击: " + path);
        }
    }
    
    /**
     * 验证文件类型
     */
    public static void validateFileType(String path, SecurityConfig securityConfig) throws UnzipException {
        if (securityConfig.isFileTypeCheckEnabled() && !isFileTypeAllowed(path, securityConfig)) {
            throw new UnzipException(UnzipErrorCode.SECURITY_ERROR, "不允许的文件类型: " + path);
        }
    }
    
    /**
     * 验证文件大小
     */
    public static void validateFileSize(long size, SecurityConfig securityConfig) throws UnzipException {
        if (securityConfig.isFileSizeCheckEnabled() && size > securityConfig.getMaxFileSize()) {
            throw new UnzipException(UnzipErrorCode.FILE_TOO_LARGE, 
                String.format("文件大小超过限制: %d > %d", size, securityConfig.getMaxFileSize()));
        }
    }
    
    /**
     * 检查路径是否安全
     */
    public static boolean isPathSafe(String path) {
        if (path == null) {
            return false;
        }
        
        String normalizedPath = FilenameUtils.normalize(path);
        return normalizedPath != null && !normalizedPath.startsWith("/") && !normalizedPath.startsWith("..");
    }
    
    /**
     * 检查文件类型是否允许
     */
    public static boolean isFileTypeAllowed(String path, SecurityConfig securityConfig) {
        if (path == null) {
            return false;
        }
        
        String extension = FilenameUtils.getExtension(path).toLowerCase();
        return securityConfig.getAllowedFileTypes().contains(extension);
    }
    
    /**
     * 获取最后修改时间
     */
    public static long getLastModifiedTime(IInArchive archive, int index) throws SevenZipException {
        Object time = archive.getProperty(index, PropID.LAST_MODIFICATION_TIME);
        return time instanceof Date ? ((Date) time).getTime() : System.currentTimeMillis();
    }
    
    /**
     * 提取文件内容
     */
    public static byte[] extractFile(IInArchive archive, int index, String password) throws SevenZipException {
        ExtractOperationResult result;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        result = archive.extractSlow(index, new ISequentialOutStream() {
            @Override
            public int write(byte[] data) throws SevenZipException {
                try {
                    outputStream.write(data);
                    return data.length;
                } catch (IOException e) {
                    throw new SevenZipException("写入数据失败", e);
                }
            }
        }, password);
        
        if (result != ExtractOperationResult.OK) {
            throw new SevenZipException("提取文件失败: " + result);
        }
        
        return outputStream.toByteArray();
    }
    
    /**
     * 计算总大小
     */
    public static long calculateTotalSize(IInArchive archive) throws SevenZipException {
        long totalSize = 0;
        for (int i = 0; i < archive.getNumberOfItems(); i++) {
            Long size = (Long) archive.getProperty(i, PropID.SIZE);
            if (size != null) {
                totalSize += size;
            }
        }
        return totalSize;
    }
    
    /**
     * 检查内存使用情况
     */
    public static void checkMemoryUsage() throws UnzipException {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsage = (double) usedMemory / runtime.maxMemory();
        
        if (memoryUsage > MEMORY_THRESHOLD) {
            throw new UnzipException(UnzipErrorCode.MEMORY_ERROR, 
                String.format("内存使用率过高: %.2f%%", memoryUsage * 100));
        }
    }
    
    /**
     * 创建临时文件（优化版本）
     */
    public static File createTempFile(InputStream inputStream, String tempDirectory, 
            String extension, SecurityConfig securityConfig) throws UnzipException {
        try {
            File tempFile = File.createTempFile("unzip_", extension, new File(tempDirectory));
            // 使用缓冲流提高性能
            try (BufferedInputStream bis = new BufferedInputStream(inputStream);
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = bis.read(buffer)) != -1) {
                    totalBytesRead += bytesRead;
                    validateFileSize(totalBytesRead, securityConfig);
                    checkMemoryUsage(); // 检查内存使用
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush();
            }
            return tempFile;
        } catch (IOException e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "创建临时文件失败", e);
        }
    }
} 