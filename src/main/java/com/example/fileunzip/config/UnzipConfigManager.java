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

@Slf4j
public class UnzipConfigManager {
    private static volatile UnzipConfigManager instance;
    private UnzipConfig config;
    private final ReadWriteLock configLock;
    private final String configFile;
    private final List<UnzipConfigChangeListener> listeners;
    
    private UnzipConfigManager() {
        this.config = UnzipConfig.getDefaultConfig();
        this.configLock = new ReentrantReadWriteLock();
        this.configFile = "unzip-config.properties";
        this.listeners = new ArrayList<>();
        loadConfig();
    }
    
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
    
    public UnzipConfig getConfig() {
        configLock.readLock().lock();
        try {
            return config;
        } finally {
            configLock.readLock().unlock();
        }
    }
    
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
    
    public void addListener(UnzipConfigChangeListener listener) {
        configLock.writeLock().lock();
        try {
            listeners.add(listener);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    public void removeListener(UnzipConfigChangeListener listener) {
        configLock.writeLock().lock();
        try {
            listeners.remove(listener);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    private void notifyListeners(UnzipConfig oldConfig, UnzipConfig newConfig) {
        configLock.readLock().lock();
        try {
            for (UnzipConfigChangeListener listener : listeners) {
                try {
                    listener.onConfigChanged(oldConfig, newConfig);
                } catch (Exception e) {
                    log.error("通知配置变更监听器失败", e);
                }
            }
        } finally {
            configLock.readLock().unlock();
        }
    }
    
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
                .enableFileTypeValidation(Boolean.parseBoolean(props.getProperty("enableFileTypeValidation", String.valueOf(config.isEnableFileTypeValidation()))))
                .enablePathSecurityCheck(Boolean.parseBoolean(props.getProperty("enablePathSecurityCheck", String.valueOf(config.isEnablePathSecurityCheck()))))
                .enableChecksumValidation(Boolean.parseBoolean(props.getProperty("enableChecksumValidation", String.valueOf(config.isEnableChecksumValidation()))))
                .enableVirusScan(Boolean.parseBoolean(props.getProperty("enableVirusScan", String.valueOf(config.isEnableVirusScan()))))
                .enableConcurrentUnzip(Boolean.parseBoolean(props.getProperty("enableConcurrentUnzip", String.valueOf(config.isEnableConcurrentUnzip()))))
                .concurrentThreads(Integer.parseInt(props.getProperty("concurrentThreads", String.valueOf(config.getConcurrentThreads()))))
                .unzipTimeout(Long.parseLong(props.getProperty("unzipTimeout", String.valueOf(config.getUnzipTimeout()))))
                .enableProgressCallback(Boolean.parseBoolean(props.getProperty("enableProgressCallback", String.valueOf(config.isEnableProgressCallback()))))
                .build();
            
            updateConfig(newConfig);
        } catch (IOException e) {
            log.error("加载配置文件失败", e);
        }
    }
    
    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("maxFileSize", String.valueOf(config.getMaxFileSize()));
        props.setProperty("bufferSize", String.valueOf(config.getBufferSize()));
        props.setProperty("tempDirectory", config.getTempDirectory());
        props.setProperty("enableFileTypeValidation", String.valueOf(config.isEnableFileTypeValidation()));
        props.setProperty("enablePathSecurityCheck", String.valueOf(config.isEnablePathSecurityCheck()));
        props.setProperty("enableChecksumValidation", String.valueOf(config.isEnableChecksumValidation()));
        props.setProperty("enableVirusScan", String.valueOf(config.isEnableVirusScan()));
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