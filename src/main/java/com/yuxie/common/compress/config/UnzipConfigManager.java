package com.yuxie.common.compress.config;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 解压配置管理器
 * <p>
 * 该类负责管理解压配置的加载、保存和更新，采用单例模式实现。
 * 主要功能包括：
 * </p>
 * <ul>
 * <li>配置的持久化存储和加载</li>
 * <li>配置的实时更新</li>
 * <li>配置变更通知机制</li>
 * <li>线程安全的配置访问</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 获取配置管理器实例
 * UnzipConfigManager manager = UnzipConfigManager.getInstance();
 * 
 * // 获取当前配置
 * UnzipConfig config = manager.getConfig();
 * 
 * // 更新配置
 * UnzipConfig newConfig = UnzipConfig.builder()
 *     .maxFileSize(1024L * 1024L * 200L)
 *     .build();
 * manager.updateConfig(newConfig);
 * 
 * // 添加配置变更监听器
 * manager.addConfigChangeListener(new UnzipConfigChangeListener() {
 *     @Override
 *     public void onConfigChanged(UnzipConfig oldConfig, UnzipConfig newConfig) {
 *         // 处理配置变更
 *     }
 * });
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipConfig
 * @see UnzipConfigChangeListener
 */
@Slf4j
public class UnzipConfigManager {
    /** 
     * 单例实例
     * <p>
     * 使用volatile关键字确保多线程环境下的可见性
     * </p>
     */
    private static volatile UnzipConfigManager instance;
    
    /** 
     * 当前配置
     * <p>
     * 存储当前使用的解压配置
     * </p>
     */
    private UnzipConfig config;
    
    /** 
     * 配置读写锁
     * <p>
     * 用于确保配置访问的线程安全性
     * </p>
     */
    private final ReadWriteLock configLock;
    
    /** 
     * 配置文件路径
     * <p>
     * 配置文件的存储位置
     * </p>
     */
    private final String configFile;
    
    /** 
     * 配置变更监听器列表
     * <p>
     * 存储所有注册的配置变更监听器
     * </p>
     */
    private final List<UnzipConfigChangeListener> listeners;
    
    /**
     * 私有构造函数，防止外部实例化
     * <p>
     * 初始化配置管理器，加载默认配置。
     * 如果存在配置文件，则从配置文件加载配置。
     * </p>
     */
    private UnzipConfigManager() {
        this.config = UnzipConfig.getDefaultConfig();
        this.configLock = new ReentrantReadWriteLock();
        this.configFile = "unzip-config.properties";
        this.listeners = new ArrayList<>();
        loadConfig();
    }
    
    /**
     * 获取配置管理器实例
     * <p>
     * 使用双重检查锁定确保线程安全。
     * 第一次调用时创建实例，后续调用返回已创建的实例。
     * </p>
     *
     * @return 配置管理器实例
     */
    public static UnzipConfigManager getInstance() {
        if (instance == null) {
            synchronized (UnzipConfigManager.class) {
                if (instance == null) {
                    instance = new UnzipConfigManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取当前配置
     * <p>
     * 使用读锁确保线程安全。
     * 多个线程可以同时读取配置。
     * </p>
     *
     * @return 当前配置实例
     */
    public UnzipConfig getConfig() {
        configLock.readLock().lock();
        try {
            return config;
        } finally {
            configLock.readLock().unlock();
        }
    }
    
    /**
     * 更新配置
     * <p>
     * 使用写锁确保线程安全，并通知所有监听器。
     * 更新配置时会自动保存到配置文件。
     * </p>
     *
     * @param newConfig 新的配置实例
     */
    public void updateConfig(UnzipConfig newConfig) {
        configLock.writeLock().lock();
        try {
            UnzipConfig oldConfig = this.config;
            this.config = newConfig;
            saveConfig();
            notifyListeners(oldConfig, newConfig);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 通知所有配置变更监听器
     * <p>
     * 当配置发生变更时，通知所有注册的监听器。
     * 如果监听器抛出异常，会被记录但不会影响其他监听器。
     * </p>
     *
     * @param oldConfig 旧的配置实例
     * @param newConfig 新的配置实例
     */
    private void notifyListeners(UnzipConfig oldConfig, UnzipConfig newConfig) {
        for (UnzipConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChanged(oldConfig, newConfig);
            } catch (Exception e) {
                log.error("通知配置变更监听器失败", e);
            }
        }
    }
    
    /**
     * 添加配置变更监听器
     * <p>
     * 注册一个新的配置变更监听器。
     * 如果监听器为null，则不会添加。
     * </p>
     *
     * @param listener 监听器实例
     */
    public void addConfigChangeListener(UnzipConfigChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    /**
     * 移除配置变更监听器
     * <p>
     * 从监听器列表中移除指定的监听器。
     * 如果监听器不存在，则不会有任何操作。
     * </p>
     *
     * @param listener 监听器实例
     */
    public void removeConfigChangeListener(UnzipConfigChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 从配置文件加载配置
     * <p>
     * 如果配置文件不存在，则创建默认配置文件。
     * 如果加载失败，会记录错误日志但不会影响程序运行。
     * </p>
     */
    private void loadConfig() {
        File file = new File(configFile);
        if (!file.exists()) {
            saveConfig();
            return;
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            
            UnzipConfig newConfig = UnzipConfig.builder()
                .maxFileSize(Long.parseLong(props.getProperty("maxFileSize", String.valueOf(config.getMaxFileSize()))))
                .bufferSize(Integer.parseInt(props.getProperty("bufferSize", String.valueOf(config.getBufferSize()))))
                .tempDirectory(props.getProperty("tempDirectory", config.getTempDirectory()))
                .maxFileCount(Integer.parseInt(props.getProperty("maxFileCount", String.valueOf(config.getMaxFileCount()))))
                .enablePathTraversalCheck(Boolean.parseBoolean(props.getProperty("enablePathTraversalCheck", String.valueOf(config.isEnablePathTraversalCheck()))))
                .enableFileTypeCheck(Boolean.parseBoolean(props.getProperty("enableFileTypeCheck", String.valueOf(config.isEnableFileTypeCheck()))))
                .enableFileSizeCheck(Boolean.parseBoolean(props.getProperty("enableFileSizeCheck", String.valueOf(config.isEnableFileSizeCheck()))))
                .enableFileCountCheck(Boolean.parseBoolean(props.getProperty("enableFileCountCheck", String.valueOf(config.isEnableFileCountCheck()))))
                .enableConcurrentUnzip(Boolean.parseBoolean(props.getProperty("enableConcurrentUnzip", String.valueOf(config.isEnableConcurrentUnzip()))))
                .concurrentThreads(Integer.parseInt(props.getProperty("concurrentThreads", String.valueOf(config.getConcurrentThreads()))))
                .unzipTimeout(Long.parseLong(props.getProperty("unzipTimeout", String.valueOf(config.getUnzipTimeout()))))
                .enableProgressCallback(Boolean.parseBoolean(props.getProperty("enableProgressCallback", String.valueOf(config.isEnableProgressCallback()))))
                .enableChecksumValidation(Boolean.parseBoolean(props.getProperty("enableChecksumValidation", String.valueOf(config.isEnableChecksumValidation()))))
                .enableVirusScan(Boolean.parseBoolean(props.getProperty("enableVirusScan", String.valueOf(config.isEnableVirusScan()))))
                .build();
            
            updateConfig(newConfig);
        } catch (IOException e) {
            log.error("加载配置文件失败", e);
        }
    }
    
    /**
     * 保存配置到配置文件
     * <p>
     * 将当前配置的所有属性保存到properties文件中。
     * 如果保存失败，会记录错误日志但不会影响程序运行。
     * </p>
     */
    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("maxFileSize", String.valueOf(config.getMaxFileSize()));
        props.setProperty("bufferSize", String.valueOf(config.getBufferSize()));
        props.setProperty("tempDirectory", config.getTempDirectory());
        props.setProperty("maxFileCount", String.valueOf(config.getMaxFileCount()));
        props.setProperty("enablePathTraversalCheck", String.valueOf(config.isEnablePathTraversalCheck()));
        props.setProperty("enableFileTypeCheck", String.valueOf(config.isEnableFileTypeCheck()));
        props.setProperty("enableFileSizeCheck", String.valueOf(config.isEnableFileSizeCheck()));
        props.setProperty("enableFileCountCheck", String.valueOf(config.isEnableFileCountCheck()));
        props.setProperty("enableConcurrentUnzip", String.valueOf(config.isEnableConcurrentUnzip()));
        props.setProperty("concurrentThreads", String.valueOf(config.getConcurrentThreads()));
        props.setProperty("unzipTimeout", String.valueOf(config.getUnzipTimeout()));
        props.setProperty("enableProgressCallback", String.valueOf(config.isEnableProgressCallback()));
        
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "Unzip Configuration");
        } catch (IOException e) {
            log.error("保存配置文件失败", e);
        }
    }
} 