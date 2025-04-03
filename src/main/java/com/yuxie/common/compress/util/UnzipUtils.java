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
 * <p>
 * 提供文件解压相关的通用工具方法，包括文件路径验证、类型检查、大小验证、内存监控等功能。
 * 该类中的所有方法都是静态的，可以直接通过类名调用。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>文件路径验证：检查路径合法性，防止路径遍历攻击</li>
 *   <li>文件类型检查：验证文件扩展名是否在允许列表中</li>
 *   <li>文件大小验证：确保文件大小在配置的限制范围内</li>
 *   <li>内存使用监控：检查系统内存使用情况，防止内存溢出</li>
 *   <li>临时文件管理：创建和清理临时文件</li>
 *   <li>路径处理：提供路径规范化、文件名提取等功能</li>
 *   <li>压缩文件操作：提取文件内容、计算总大小等</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 验证文件路径
 * UnzipUtils.validatePath(filePath, config);
 * 
 * // 检查文件类型
 * UnzipUtils.validateFileType(filePath, config);
 * 
 * // 创建临时文件
 * File tempFile = UnzipUtils.createTempFile(data, "prefix", ".tmp");
 * try {
 *     // 使用临时文件
 * } finally {
 *     UnzipUtils.safeDelete(tempFile);
 * }
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipConfig
 * @see UnzipException
 */
@Slf4j
public class UnzipUtils {
    
    /**
     * 允许的文件扩展名列表
     * 包含常见的文本文件、源代码文件、配置文件等扩展名
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".txt", ".log", ".json", ".xml", ".csv", ".md", ".properties",
        ".java", ".py", ".js", ".html", ".css", ".sql", ".sh", ".bat"
    );
    
    /**
     * 路径遍历检测正则表达式
     * 用于检测路径中是否包含 "../" 等试图访问上级目录的字符串
     */
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(".*\\.\\./.*");
    
    /**
     * 内存使用率阈值
     * 当系统内存使用率超过此阈值时，将抛出 UnzipException 异常
     * 默认值为 0.8，表示 80% 的内存使用率
     */
    private static final double MEMORY_THRESHOLD = 0.8;
    
    /**
     * 验证文件路径
     * <p>
     * 检查文件路径的合法性，包括：
     * <ul>
     *   <li>路径不能为空</li>
     *   <li>不能包含路径遍历攻击</li>
     *   <li>路径长度不能超过配置的限制</li>
     * </ul>
     * </p>
     *
     * @param path 要验证的文件路径
     * @param config 解压配置对象
     * @throws UnzipException 当路径验证失败时抛出异常
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
     * <p>
     * 检查文件扩展名是否在允许列表中。
     * 如果文件没有扩展名或扩展名为空，则视为合法。
     * </p>
     *
     * @param path 文件路径
     * @param config 解压配置对象
     * @throws UnzipException 当文件类型不在允许列表中时抛出异常
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
     * <p>
     * 检查文件大小是否超过配置的最大限制。
     * </p>
     *
     * @param size 文件大小（字节）
     * @param config 解压配置对象
     * @throws UnzipException 当文件大小超过限制时抛出异常
     */
    public static void validateFileSize(long size, UnzipConfig config) throws UnzipException {
        if (size > config.getMaxFileSize()) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT,
                String.format("文件大小超过限制: %d > %d", size, config.getMaxFileSize()));
        }
    }
    
    /**
     * 获取文件扩展名
     * <p>
     * 从文件路径中提取文件扩展名，包括点号。
     * 如果文件没有扩展名，返回null。
     * </p>
     *
     * @param path 文件路径
     * @return 文件扩展名（包含点号），如果没有扩展名则返回null
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
     * <p>
     * 使用指定的数据创建临时文件，并写入数据。
     * 临时文件会在系统临时目录中创建。
     * </p>
     *
     * @param data 要写入的数据
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @return 创建的临时文件
     * @throws IOException 当创建或写入文件失败时抛出异常
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
     * <p>
     * 尝试删除文件，如果删除失败则记录警告日志。
     * </p>
     *
     * @param file 要删除的文件
     */
    public static void safeDelete(File file) {
        if (file != null && file.exists()) {
            if (!file.delete()) {
                log.warn("无法删除临时文件: {}", file.getAbsolutePath());
            }
        }
    }
    
    /**
     * 安全关闭资源
     * <p>
     * 尝试关闭实现了AutoCloseable接口的资源，如果关闭失败则记录警告日志。
     * </p>
     *
     * @param closeable 要关闭的资源
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
     * <p>
     * 从文件路径中提取文件名（不包含路径）。
     * 支持Windows和Unix风格的路径分隔符。
     * </p>
     *
     * @param path 文件路径
     * @return 文件名，如果路径为空则返回空字符串
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
     * <p>
     * 从文件路径中提取目录路径（不包含文件名）。
     * 支持Windows和Unix风格的路径分隔符。
     * </p>
     *
     * @param path 文件路径
     * @return 目录路径，如果路径为空则返回空字符串
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
     * <p>
     * 将路径转换为标准格式：
     * <ul>
     *   <li>将Windows路径分隔符转换为Unix风格</li>
     *   <li>移除开头的斜杠</li>
     * </ul>
     * </p>
     *
     * @param path 要规范化的路径
     * @return 规范化后的路径，如果路径为空则返回空字符串
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
     * <p>
     * 从压缩文件条目中获取文件的最后修改时间。
     * </p>
     *
     * @param entry 压缩文件条目
     * @return 最后修改时间（毫秒）
     */
    public static long getLastModifiedTime(ArchiveEntry entry) {
        return entry.getLastModifiedDate().getTime();
    }
    
    /**
     * 从压缩文件中提取文件内容
     * <p>
     * 读取压缩文件条目对应的内容，并将其转换为字节数组。
     * 使用8KB的缓冲区进行读取，以提高性能。
     * </p>
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
     * <p>
     * 遍历压缩文件中的所有条目，累加其大小。
     * 注意：此操作会消耗输入流，调用后需要重新打开流才能继续使用。
     * </p>
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
     * <p>
     * 计算当前JVM的内存使用率，如果超过阈值则抛出异常。
     * 用于防止在解压大文件时发生内存溢出。
     * </p>
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
     * <p>
     * 从输入流中读取数据并创建临时文件。
     * 在创建过程中会进行内存使用检查，以防止内存溢出。
     * </p>
     *
     * @param inputStream 输入流
     * @param tempDirectory 临时文件目录
     * @param extension 文件扩展名
     * @param config 解压配置对象
     * @return 创建的临时文件
     * @throws UnzipException 当创建失败或内存不足时抛出异常
     */
    public static File createTempFile(InputStream inputStream, String tempDirectory, 
            String extension, UnzipConfig config) throws UnzipException {
        // 检查内存使用情况
        checkMemoryUsage();
        
        try {
            // 创建临时文件
            File tempFile = File.createTempFile("unzip_", extension, new File(tempDirectory));
            
            // 写入数据
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            return tempFile;
        } catch (IOException e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "创建临时文件失败", e);
        }
    }
} 