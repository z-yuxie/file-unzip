package com.yuxie.common.compress.config;

/**
 * 解压配置变更监听器接口
 * <p>
 * 该接口用于监听解压配置的变更事件，当配置发生变化时，实现此接口的类将收到通知。
 * 主要用于以下场景：
 * 1. 动态更新解压配置
 * 2. 记录配置变更历史
 * 3. 在配置变更时执行相应的操作
 * </p>
 * <p>
 * 实现此接口的类需要注册到{@link UnzipConfigManager}才能接收到配置变更通知。
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipConfigManager
 * @see UnzipConfig
 */
public interface UnzipConfigChangeListener {
    /**
     * 配置变更时的回调方法
     * <p>
     * 当解压配置发生变更时，此方法会被调用。
     * 实现类可以在此方法中：
     * 1. 比较新旧配置的差异
     * 2. 根据配置变更执行相应的操作
     * 3. 记录配置变更日志
     * </p>
     *
     * @param oldConfig 变更前的配置对象
     * @param newConfig 变更后的配置对象
     */
    void onConfigChanged(UnzipConfig oldConfig, UnzipConfig newConfig);
} 