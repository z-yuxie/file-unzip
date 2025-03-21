package com.example.fileunzip.config;

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
 * 负责管理解压配置的加载、保存和更新，采用单例模式实现
 * 支持配置变更通知机制，可以注册监听器来响应配置变更
 */
@Slf4j
public class UnzipConfigManager {
    /** 单例实例 */
    private static volatile UnzipConfigManager instance;
    
    /** 当前配置 */
    private UnzipConfig config;
    
    /** 配置读写锁 */
    private final ReadWriteLock configLock;
    
    /** 配置文件路径 */
    private final String configFile;
    
    /** 配置变更监听器列表 */
    private final List<UnzipConfigChangeListener> listeners;
    
    /**
     * 私有构造函数，防止外部实例化
     * 初始化配置管理器，加载默认配置
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
     * 使用双重检查锁定确保线程安全
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
     * 使用读锁确保线程安全
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
     * 使用写锁确保线程安全，并通知所有监听器
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
     *
     * @param listener 监听器实例
     */
    public void removeConfigChangeListener(UnzipConfigChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 从配置文件加载配置
     * 如果配置文件不存在，则创建默认配置文件
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
     * 将当前配置的所有属性保存到 properties 文件中
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