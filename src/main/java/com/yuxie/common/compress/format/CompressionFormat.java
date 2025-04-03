package com.yuxie.common.compress.format;

import lombok.Getter;

/**
 * 压缩文件格式枚举
 * 定义了系统支持的所有压缩文件格式
 */
@Getter
public enum CompressionFormat {
    /**
     * ZIP格式
     */
    ZIP(".zip", false),
    
    /**
     * TAR格式
     */
    TAR(".tar", false),
    
    /**
     * GZIP格式
     */
    GZIP(".gz", false),
    
    /**
     * BZIP2格式
     */
    BZIP2(".bz2", false),
    
    /**
     * XZ格式
     */
    XZ(".xz", false),
    
    /**
     * RAR格式
     */
    RAR(".rar", false),
    
    /**
     * 7Z格式
     */
    SEVEN_ZIP(".7z", false),
    
    /**
     * TAR.GZ格式
     */
    TAR_GZ(".tar.gz", true),
    
    /**
     * TAR.BZ2格式
     */
    TAR_BZ2(".tar.bz2", true),
    
    /**
     * TAR.XZ格式
     */
    TAR_XZ(".tar.xz", true),
    
    /**
     * LZMA格式
     */
    LZMA(".lzma", false),
    
    /**
     * Snappy格式
     */
    SNAPPY(".snappy", false),
    
    /**
     * LZ4格式
     */
    LZ4(".lz4", false),
    
    /**
     * 通用复合格式（用于检测阶段）
     */
    COMPOUND(null, true),
    
    /**
     * 未知格式
     */
    UNKNOWN(null, false);
    
    private final String extension;
    private final boolean isCompound;
    
    CompressionFormat(String extension, boolean isCompound) {
        this.extension = extension;
        this.isCompound = isCompound;
    }
    
    /**
     * 检查格式是否有文件扩展名
     */
    public boolean hasExtension() {
        return extension != null;
    }
    
    /**
     * 检查是否为复合格式
     */
    public boolean isCompound() {
        return isCompound;
    }
} 