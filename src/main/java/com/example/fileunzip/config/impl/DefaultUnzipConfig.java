package com.example.fileunzip.config.impl;

import com.example.fileunzip.config.UnzipConfig;
import lombok.Data;

/**
 * 默认解压配置实现
 */
@Data
public class DefaultUnzipConfig {
    
    /**
     * 创建默认配置
     */
    public static UnzipConfig createDefault() {
        return UnzipConfig.getDefaultConfig();
    }
} 