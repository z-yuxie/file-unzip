package com.example.fileunzip.config;

/**
 * 配置变更监听器接口
 */
public interface UnzipConfigChangeListener {
    /**
     * 配置变更时的回调方法
     *
     * @param oldConfig 旧配置
     * @param newConfig 新配置
     */
    void onConfigChanged(UnzipConfig oldConfig, UnzipConfig newConfig);
} 