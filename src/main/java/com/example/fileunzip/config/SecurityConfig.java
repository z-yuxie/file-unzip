package com.example.fileunzip.config;

import java.util.Set;

/**
 * 安全配置接口
 */
public interface SecurityConfig {
    /**
     * 获取最大文件大小限制（字节）
     */
    long getMaxFileSize();

    /**
     * 获取允许的文件类型集合
     */
    Set<String> getAllowedFileTypes();

    /**
     * 获取最大文件数量限制
     */
    int getMaxFileCount();

    /**
     * 获取是否启用路径遍历检查
     */
    boolean isPathTraversalCheckEnabled();

    /**
     * 获取是否启用文件类型检查
     */
    boolean isFileTypeCheckEnabled();

    /**
     * 获取是否启用文件大小检查
     */
    boolean isFileSizeCheckEnabled();

    /**
     * 获取是否启用文件数量检查
     */
    boolean isFileCountCheckEnabled();
} 