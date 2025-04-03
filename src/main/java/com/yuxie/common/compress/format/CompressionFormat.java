package com.yuxie.common.compress.format;

import lombok.Getter;

/**
 * 压缩格式枚举
 * 支持以下格式：
 * 1. ZIP (.zip)
 * 2. RAR (.rar)
 * 3. SEVEN_ZIP (.7z)
 * 4. TAR (.tar)
 * 5. GZIP (.gz)
 * 6. BZIP2 (.bz2)
 * 7. XZ (.xz)
 * 8. LZMA (.lzma)
 * 9. SNAPPY (.snappy)
 * 10. LZ4 (.lz4)
 */
@Getter
public enum CompressionFormat {
    /**
     * ZIP格式
     */
    ZIP("zip"),
    
    /**
     * RAR格式
     */
    RAR("rar"),
    
    /**
     * SEVEN_ZIP格式
     */
    SEVEN_ZIP("7z"),
    
    /**
     * TAR格式
     */
    TAR("tar"),
    
    /**
     * GZIP格式
     */
    GZIP("gz"),
    
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
    
    CompressionFormat(String extension) {
        this.extension = extension;
    }
    
    public String getExtension() {
        return extension;
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
} 