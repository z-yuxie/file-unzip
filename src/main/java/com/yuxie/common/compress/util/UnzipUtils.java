package com.yuxie.common.compress.util;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipException;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;

import java.io.*;

/**
 * 解压工具类
 * 提供文件解压相关的通用工具方法，包括：
 * 1. 文件路径验证
 * 2. 文件类型检查
 * 3. 文件大小验证
 * 4. 内存使用监控
 * 5. 临时文件管理
 */
@Slf4j
public class UnzipUtils {
    
    /**
     * 内存使用率阈值
     * 当系统内存使用率超过此阈值时，将抛出 UnzipException 异常
     * 默认值为 0.8，表示 80% 的内存使用率
     */
    private static final double MEMORY_THRESHOLD = 0.8;
    
    /**
     * 验证文件路径安全性
     *
     * @param path 文件路径
     * @param config 解压配置
     * @throws UnzipException 当路径不安全时抛出异常
     */
    public static void validatePath(String path, UnzipConfig config) throws UnzipException {
        if (config.isEnablePathTraversalCheck() && !isPathSafe(path)) {
            throw new UnzipException("检测到不安全的文件路径: " + path);
        }
    }
    
    /**
     * 验证文件类型是否允许
     *
     * @param path 文件路径
     * @param config 解压配置
     * @throws UnzipException 当文件类型不允许时抛出异常
     */
    public static void validateFileType(String path, UnzipConfig config) throws UnzipException {
        if (config.isEnableFileTypeCheck() && !isFileTypeAllowed(path, config)) {
            throw new UnzipException("不支持的文件类型: " + path);
        }
    }
    
    /**
     * 验证文件大小是否超过限制
     *
     * @param size 文件大小（字节）
     * @param config 解压配置
     * @throws UnzipException 当文件大小超过限制时抛出异常
     */
    public static void validateFileSize(long size, UnzipConfig config) throws UnzipException {
        if (config.isEnableFileSizeCheck() && size > config.getMaxFileSize()) {
            throw new UnzipException(
                String.format("文件大小超过限制: %d > %d", size, config.getMaxFileSize()));
        }
    }
    
    /**
     * 检查路径是否安全，防止路径遍历攻击
     *
     * @param path 文件路径
     * @return 如果路径安全返回true，否则返回false
     */
    public static boolean isPathSafe(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        // 检查是否包含路径遍历字符
        String normalizedPath = path.replace('\\', '/');
        return !normalizedPath.contains("../") && !normalizedPath.contains("..\\");
    }
    
    /**
     * 检查文件类型是否在允许列表中
     *
     * @param path 文件路径
     * @param config 解压配置
     * @return 如果文件类型允许返回true，否则返回false
     */
    public static boolean isFileTypeAllowed(String path, UnzipConfig config) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = path.substring(dotIndex + 1).toLowerCase();
            return config.getAllowedFileTypes().contains(extension);
        }
        return false;
    }
    
    /**
     * 获取文件的最后修改时间
     *
     * @param archive 压缩文件
     * @param index 文件索引
     * @return 最后修改时间（毫秒）
     * @throws SevenZipException 当获取时间失败时抛出异常
     */
    public static long getLastModifiedTime(IInArchive archive, int index) throws SevenZipException {
        Object property = archive.getProperty(index, PropID.LAST_MODIFICATION_TIME);
        if (property instanceof Long) {
            return (Long) property;
        }
        return System.currentTimeMillis();
    }
    
    /**
     * 从压缩文件中提取文件内容
     *
     * @param archive 压缩文件
     * @param index 文件索引
     * @param password 解压密码（如果有）
     * @return 文件内容字节数组
     * @throws SevenZipException 当提取失败时抛出异常
     */
    public static byte[] extractFile(IInArchive archive, int index, String password) throws SevenZipException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        archive.extractSlow(index, new ISequentialOutStream() {
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
        
        return outputStream.toByteArray();
    }
    
    /**
     * 计算压缩文件中所有文件的总大小
     *
     * @param archive 压缩文件
     * @return 总大小（字节）
     * @throws SevenZipException 当计算失败时抛出异常
     */
    public static long calculateTotalSize(IInArchive archive) throws SevenZipException {
        long totalSize = 0;
        int numberOfItems = archive.getNumberOfItems();
        for (int i = 0; i < numberOfItems; i++) {
            Object property = archive.getProperty(i, PropID.SIZE);
            if (property instanceof Long) {
                totalSize += (Long) property;
            }
        }
        return totalSize;
    }
    
    /**
     * 检查当前内存使用情况
     *
     * @throws UnzipException 当内存使用率超过阈值时抛出异常
     */
    public static void checkMemoryUsage() throws UnzipException {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsage = (double) usedMemory / maxMemory;
        
        if (memoryUsage > MEMORY_THRESHOLD) {
            throw new UnzipException("内存使用率过高: " + (memoryUsage * 100) + "%");
        }
    }
    
    /**
     * 创建临时文件并写入输入流的内容
     *
     * @param inputStream 输入流
     * @param tempDirectory 临时目录
     * @param extension 文件扩展名
     * @param config 解压配置
     * @return 创建的临时文件
     * @throws UnzipException 当创建或写入失败时抛出异常
     */
    public static File createTempFile(InputStream inputStream, String tempDirectory, 
            String extension, UnzipConfig config) throws UnzipException {
        try {
            File tempFile = File.createTempFile("unzip_", extension, new File(tempDirectory));
            tempFile.deleteOnExit();
            
            byte[] buffer = new byte[config.getBufferSize()];
            int bytesRead;
            long totalBytesRead = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                validateFileSize(totalBytesRead, config);
                
                // 写入文件
                try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                    raf.seek(raf.length());
                    raf.write(buffer, 0, bytesRead);
                }
            }
            
            return tempFile;
        } catch (IOException e) {
            throw new UnzipException("创建临时文件失败", e);
        }
    }
} 