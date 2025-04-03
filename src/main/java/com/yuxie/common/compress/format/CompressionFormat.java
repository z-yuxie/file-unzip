package com.yuxie.common.compress.format;

import lombok.Getter;

/**
 * 压缩格式枚举
 * <p>
 * 定义了支持的压缩文件格式。
 * 每种格式都有其特定的文件扩展名和MIME类型。
 * </p>
 * <p>
 * 支持的格式：
 * <ul>
 *   <li>ZIP - 最常用的压缩格式，支持密码保护</li>
 *   <li>RAR - 专有格式，支持密码保护和恢复记录</li>
 *   <li>7Z - 高压缩比格式，支持密码保护和加密</li>
 *   <li>TAR - 归档格式，通常与其他压缩格式组合使用</li>
 *   <li>GZIP - 单文件压缩格式，常用于网络传输</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 检查文件格式
 * String fileName = "archive.zip";
 * CompressionFormat format = CompressionFormat.fromFileName(fileName);
 * if (format == CompressionFormat.ZIP) {
 *     // 使用ZIP解压策略
 *     UnzipStrategy strategy = new ZipUnzipStrategy();
 *     // 解压文件
 *     Map<FileInfo, byte[]> result = strategy.unzip(inputStream);
 * }
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipStrategy
 * @see FileInfo
 */
@Getter
public enum CompressionFormat {
    /**
     * ZIP格式
     * <p>
     * 最常用的压缩格式，具有以下特点：
     * <ul>
     *   <li>支持密码保护</li>
     *   <li>支持多文件压缩</li>
     *   <li>支持目录结构</li>
     *   <li>广泛的操作系统支持</li>
     * </ul>
     * </p>
     */
    ZIP(".zip", "application/zip"),
    
    /**
     * RAR格式
     * <p>
     * 专有压缩格式，具有以下特点：
     * <ul>
     *   <li>支持密码保护</li>
     *   <li>支持恢复记录</li>
     *   <li>支持分卷压缩</li>
     *   <li>高压缩比</li>
     * </ul>
     * </p>
     */
    RAR(".rar", "application/x-rar-compressed"),
    
    /**
     * 7Z格式
     * <p>
     * 高压缩比格式，具有以下特点：
     * <ul>
     *   <li>支持密码保护和加密</li>
     *   <li>支持多种压缩算法</li>
     *   <li>支持分卷压缩</li>
     *   <li>开源格式</li>
     * </ul>
     * </p>
     */
    SEVEN_ZIP(".7z", "application/x-7z-compressed"),
    
    /**
     * TAR格式
     * <p>
     * 归档格式，具有以下特点：
     * <ul>
     *   <li>不进行压缩，仅打包</li>
     *   <li>保留文件权限和时间戳</li>
     *   <li>通常与其他压缩格式组合使用</li>
     *   <li>常用于Unix/Linux系统</li>
     * </ul>
     * </p>
     */
    TAR(".tar", "application/x-tar"),
    
    /**
     * GZIP格式
     * <p>
     * 单文件压缩格式，具有以下特点：
     * <ul>
     *   <li>仅支持单文件压缩</li>
     *   <li>高压缩比</li>
     *   <li>快速压缩和解压</li>
     *   <li>常用于网络传输</li>
     * </ul>
     * </p>
     */
    GZIP(".gz", "application/gzip"),
    
    /**
     * BZIP2格式
     */
    BZIP2("bz2"),
    
    /**
     * XZ格式
     */
    XZ("xz"),
    
    /**
     * LZMA格式
     */
    LZMA("lzma"),
    
    /**
     * SNAPPY格式
     */
    SNAPPY("snappy"),
    
    /**
     * LZ4格式
     */
    LZ4("lz4"),
    
    /**
     * 未知格式
     */
    UNKNOWN(null);
    
    private final String extension;
    private final String mimeType;
    
    CompressionFormat(String extension) {
        this.extension = extension;
        this.mimeType = null;
    }
    
    CompressionFormat(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }
    
    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static CompressionFormat fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return UNKNOWN;
        }
        String ext = extension.toLowerCase();
        for (CompressionFormat format : values()) {
            if (ext.equals(format.extension)) {
                return format;
            }
        }
        return UNKNOWN;
    }

    public static CompressionFormat fromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lowerFileName = fileName.toLowerCase();
        for (CompressionFormat format : values()) {
            if (lowerFileName.endsWith(format.extension)) {
                return format;
            }
        }
        return null;
    }

    public static CompressionFormat fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        String lowerMimeType = mimeType.toLowerCase();
        for (CompressionFormat format : values()) {
            if (format.mimeType != null && format.mimeType.equals(lowerMimeType)) {
                return format;
            }
        }
        return null;
    }
} 