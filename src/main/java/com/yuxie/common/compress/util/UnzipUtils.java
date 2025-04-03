package com.yuxie.common.compress.util;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipErrorCode;
import com.yuxie.common.compress.exception.UnzipException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
    
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".txt", ".log", ".json", ".xml", ".csv", ".md", ".properties",
        ".java", ".py", ".js", ".html", ".css", ".sql", ".sh", ".bat"
    );
    
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(".*\\.\\./.*");
    
    /**
     * 内存使用率阈值
     * 当系统内存使用率超过此阈值时，将抛出 UnzipException 异常
     * 默认值为 0.8，表示 80% 的内存使用率
     */
    private static final double MEMORY_THRESHOLD = 0.8;
    
    /**
     * 验证文件路径
     */
    public static void validatePath(String path, UnzipConfig config) throws UnzipException {
        if (path == null || path.trim().isEmpty()) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "文件路径不能为空");
        }
        
        // 检查路径遍历
        if (PATH_TRAVERSAL_PATTERN.matcher(path).matches()) {
            throw new UnzipException(UnzipErrorCode.SECURITY_ERROR, "检测到路径遍历攻击: " + path);
        }
        
        // 检查路径长度
        if (path.length() > config.getMaxPathLength()) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT,
                String.format("文件路径超过长度限制: %d > %d", path.length(), config.getMaxPathLength()));
        }
    }
    
    /**
     * 验证文件类型
     */
    public static void validateFileType(String path, UnzipConfig config) throws UnzipException {
        if (path == null || path.trim().isEmpty()) {
            return;
        }
        
        String extension = getFileExtension(path);
        if (extension == null || extension.isEmpty()) {
            return;
        }
        
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new UnzipException(UnzipErrorCode.SECURITY_ERROR, "不支持的文件类型: " + extension);
        }
    }
    
    /**
     * 验证文件大小
     */
    public static void validateFileSize(long size, UnzipConfig config) throws UnzipException {
        if (size > config.getMaxFileSize()) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT,
                String.format("文件大小超过限制: %d > %d", size, config.getMaxFileSize()));
        }
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        
        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1) {
            return null;
        }
        
        return path.substring(lastDot);
    }
    
    /**
     * 创建临时文件
     */
    public static File createTempFile(byte[] data, String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(data);
        }
        return tempFile;
    }
    
    /**
     * 安全删除文件
     */
    public static void safeDelete(File file) {
        if (file != null && file.exists()) {
            if (!file.delete()) {
                log.warn("无法删除临时文件: {}", file.getAbsolutePath());
            }
        }
    }
    
    /**
     * 安全关闭流
     */
    public static void safeClose(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("关闭资源失败", e);
            }
        }
    }
    
    /**
     * 获取文件名
     */
    public static String getFileName(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSeparator == -1 ? path : path.substring(lastSeparator + 1);
    }
    
    /**
     * 获取文件路径
     */
    public static String getFilePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSeparator == -1 ? "" : path.substring(0, lastSeparator);
    }
    
    /**
     * 规范化路径
     */
    public static String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        
        // 替换Windows路径分隔符
        path = path.replace('\\', '/');
        
        // 移除开头的斜杠
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        return path;
    }
    
    /**
     * 获取文件的最后修改时间
     *
     * @param entry 压缩文件条目
     * @return 最后修改时间（毫秒）
     */
    public static long getLastModifiedTime(ArchiveEntry entry) {
        return entry.getLastModifiedDate().getTime();
    }
    
    /**
     * 从压缩文件中提取文件内容
     *
     * @param archiveInputStream 压缩文件输入流
     * @param entry 压缩文件条目
     * @return 文件内容字节数组
     * @throws IOException 当提取失败时抛出异常
     */
    public static byte[] extractFile(ArchiveInputStream archiveInputStream, ArchiveEntry entry) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = archiveInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        return outputStream.toByteArray();
    }
    
    /**
     * 计算压缩文件中所有文件的总大小
     *
     * @param archiveInputStream 压缩文件输入流
     * @return 总大小（字节）
     * @throws IOException 当计算失败时抛出异常
     */
    public static long calculateTotalSize(ArchiveInputStream archiveInputStream) throws IOException {
        long totalSize = 0;
        ArchiveEntry entry;
        while ((entry = archiveInputStream.getNextEntry()) != null) {
            totalSize += entry.getSize();
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
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory;
        
        if (memoryUsage > MEMORY_THRESHOLD) {
            throw new UnzipException(UnzipErrorCode.MEMORY_ERROR,
                String.format("内存使用率过高: %.2f%% > %.2f%%", memoryUsage * 100, MEMORY_THRESHOLD * 100));
        }
    }
    
    /**
     * 创建临时文件
     *
     * @param inputStream 输入流
     * @param tempDirectory 临时目录
     * @param extension 文件扩展名
     * @param config 解压配置
     * @return 临时文件
     * @throws UnzipException 当创建失败时抛出异常
     */
    public static File createTempFile(InputStream inputStream, String tempDirectory, 
            String extension, UnzipConfig config) throws UnzipException {
        try {
            // 创建临时文件
            File tempFile = File.createTempFile("unzip_", extension, new File(tempDirectory));
            
            // 写入数据
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[config.getBufferSize()];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    // 检查文件大小
                    if (config.isEnableFileSizeCheck() && totalBytesRead > config.getMaxFileSize()) {
                        throw new UnzipException(UnzipErrorCode.FILE_TOO_LARGE,
                            String.format("文件大小超过限制: %d > %d", totalBytesRead, config.getMaxFileSize()));
                    }
                }
            }
            
            return tempFile;
        } catch (IOException e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "创建临时文件失败: " + e.getMessage(), e);
        }
    }
} 