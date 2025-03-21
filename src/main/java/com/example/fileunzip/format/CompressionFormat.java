package com.example.fileunzip.format;

import lombok.Getter;

/**
 * 压缩文件格式枚举
 * 定义了系统支持的所有压缩文件格式
 */
@Getter
public enum CompressionFormat {
    ZIP("zip"),
    RAR("rar"),
    SEVEN_ZIP("7z"),
    TAR("tar"),
    TAR_GZ("tar.gz"),
    TAR_BZ2("tar.bz2"),
    TAR_XZ("tar.xz"),
    GZIP("gz"),
    BZIP2("bz2"),
    XZ("xz"),
    LZMA("lzma"),
    SNAPPY("snappy"),
    LZ4("lz4"),
    UNKNOWN("unknown");
    
    private final String extension;
    
    CompressionFormat(String extension) {
        this.extension = extension;
    }
} 