package com.example.fileunzip.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnzipConfigManagerTest {
    @TempDir
    File tempDir;
    
    private UnzipConfigManager configManager;
    private UnzipConfigChangeListener mockListener;
    
    @BeforeEach
    void setUp() {
        configManager = UnzipConfigManager.getInstance();
        mockListener = mock(UnzipConfigChangeListener.class);
    }
    
    @Test
    void testGetInstance() {
        UnzipConfigManager instance1 = UnzipConfigManager.getInstance();
        UnzipConfigManager instance2 = UnzipConfigManager.getInstance();
        assertSame(instance1, instance2, "应该返回相同的实例");
    }
    
    @Test
    void testGetDefaultConfig() {
        UnzipConfig config = configManager.getConfig();
        assertNotNull(config, "配置不应为空");
        assertEquals(100 * 1024 * 1024, config.getMaxFileSize(), "默认文件大小限制应为100MB");
        assertEquals(8 * 1024, config.getBufferSize(), "默认缓冲区大小应为8KB");
    }
    
    @Test
    void testUpdateConfig() {
        UnzipConfig oldConfig = configManager.getConfig();
        UnzipConfig newConfig = UnzipConfig.builder()
            .maxFileSize(200 * 1024 * 1024)
            .bufferSize(16 * 1024)
            .tempDirectory(tempDir.getAbsolutePath())
            .enableFileTypeValidation(true)
            .enablePathSecurityCheck(true)
            .enableChecksumValidation(false)
            .enableVirusScan(false)
            .enableConcurrentUnzip(false)
            .concurrentThreads(4)
            .unzipTimeout(600000)
            .enableProgressCallback(true)
            .build();
        
        configManager.addListener(mockListener);
        configManager.updateConfig(newConfig);
        
        verify(mockListener).onConfigChanged(oldConfig, newConfig);
        assertEquals(200 * 1024 * 1024, configManager.getConfig().getMaxFileSize());
        assertEquals(16 * 1024, configManager.getConfig().getBufferSize());
    }
    
    @Test
    void testConfigPersistence() throws IOException {
        // 更新配置
        UnzipConfig newConfig = UnzipConfig.builder()
            .maxFileSize(200 * 1024 * 1024)
            .bufferSize(16 * 1024)
            .tempDirectory(tempDir.getAbsolutePath())
            .enableFileTypeValidation(true)
            .enablePathSecurityCheck(true)
            .enableChecksumValidation(false)
            .enableVirusScan(false)
            .enableConcurrentUnzip(false)
            .concurrentThreads(4)
            .unzipTimeout(600000)
            .enableProgressCallback(true)
            .build();
        
        configManager.updateConfig(newConfig);
        
        // 验证配置文件
        File configFile = new File("unzip-config.properties");
        assertTrue(configFile.exists(), "配置文件应该存在");
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        }
        
        assertEquals("200000000", props.getProperty("maxFileSize"));
        assertEquals("16384", props.getProperty("bufferSize"));
        assertEquals(tempDir.getAbsolutePath(), props.getProperty("tempDirectory"));
        assertEquals("true", props.getProperty("enableFileTypeValidation"));
        assertEquals("true", props.getProperty("enablePathSecurityCheck"));
        assertEquals("false", props.getProperty("enableChecksumValidation"));
        assertEquals("false", props.getProperty("enableVirusScan"));
        assertEquals("false", props.getProperty("enableConcurrentUnzip"));
        assertEquals("4", props.getProperty("concurrentThreads"));
        assertEquals("600000", props.getProperty("unzipTimeout"));
        assertEquals("true", props.getProperty("enableProgressCallback"));
    }
    
    @Test
    void testListenerManagement() {
        configManager.addListener(mockListener);
        configManager.removeListener(mockListener);
        
        UnzipConfig newConfig = UnzipConfig.builder()
            .maxFileSize(200 * 1024 * 1024)
            .bufferSize(16 * 1024)
            .tempDirectory(tempDir.getAbsolutePath())
            .enableFileTypeValidation(true)
            .enablePathSecurityCheck(true)
            .enableChecksumValidation(false)
            .enableVirusScan(false)
            .enableConcurrentUnzip(false)
            .concurrentThreads(4)
            .unzipTimeout(600000)
            .enableProgressCallback(true)
            .build();
        
        configManager.updateConfig(newConfig);
        verify(mockListener, never()).onConfigChanged(any(), any());
    }
} 