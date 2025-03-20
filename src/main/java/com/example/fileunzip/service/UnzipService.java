package com.example.fileunzip.service;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.exception.UnzipErrorCode;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.monitor.UnzipMetrics;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.strategy.UnzipStrategyFactory;
import com.example.fileunzip.util.CompressionFormatDetector;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 文件解压服务类
 * 负责管理和协调不同压缩格式的解压策略
 * 支持以下格式：
 * 1. ZIP (.zip)
 * 2. RAR (.rar)
 * 3. 7Z (.7z)
 * 4. TAR (.tar)
 * 5. TAR.GZ (.tar.gz, .tgz)
 * 6. TAR.BZ2 (.tar.bz2)
 * 7. TAR.XZ (.tar.xz)
 * 8. GZIP (.gz)
 * 9. BZIP2 (.bz2)
 * 10. XZ (.xz)
 * 11. LZMA (.lzma)
 */
@Slf4j
public class UnzipService {
    
    private final UnzipStrategyFactory strategyFactory;
    private final SecurityConfig securityConfig;
    private final UnzipConfig unzipConfig;
    private final UnzipMetrics metrics;
    
    public UnzipService(UnzipStrategyFactory strategyFactory,
                       SecurityConfig securityConfig,
                       UnzipConfig unzipConfig,
                       UnzipMetrics metrics) {
        this.strategyFactory = strategyFactory;
        this.securityConfig = securityConfig;
        this.unzipConfig = unzipConfig;
        this.metrics = metrics;
    }
    
    /**
     * 解压文件
     *
     * @param data 压缩文件数据
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    public Map<FileInfo, byte[]> unzip(byte[] data) throws UnzipException {
        if (data == null || data.length == 0) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "压缩文件数据不能为空");
        }
        
        // 安全检查
        validateSecurity(data);
        
        log.info("开始解压文件，数据大小: {} 字节", data.length);
        long startTime = System.currentTimeMillis();
        
        try {
            // 检测压缩格式
            CompressionFormatDetector.CompressionFormat format = CompressionFormatDetector.detect(data);
            if (format == CompressionFormatDetector.CompressionFormat.UNKNOWN) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "无法识别的压缩格式");
            }
            
            // 获取对应的解压策略
            UnzipStrategy strategy = strategyFactory.getStrategy(format);
            
            // 创建输入流
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                // 执行解压
                Map<FileInfo, byte[]> result = strategy.unzip(inputStream);
                
                // 记录指标
                long endTime = System.currentTimeMillis();
                metrics.recordUnzipTime(endTime - startTime);
                metrics.recordUnzipSize(data.length);
                metrics.recordFileCount(result.size());
                metrics.recordSuccess();
                
                log.info("文件解压完成，共解压 {} 个文件", result.size());
                return result;
            }
        } catch (Exception e) {
            metrics.recordError(UnzipErrorCode.IO_ERROR);
            log.error("文件解压失败", e);
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "文件解压失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 带进度回调的解压
     *
     * @param data 压缩文件数据
     * @param callback 进度回调
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    public Map<FileInfo, byte[]> unzip(byte[] data, UnzipProgressCallback callback) throws UnzipException {
        if (!unzipConfig.isEnableProgressCallback()) {
            return unzip(data);
        }
        
        if (data == null || data.length == 0) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "压缩文件数据不能为空");
        }
        
        // 安全检查
        validateSecurity(data);
        
        log.info("开始解压文件，数据大小: {} 字节", data.length);
        long startTime = System.currentTimeMillis();
        
        try {
            // 检测压缩格式
            CompressionFormatDetector.CompressionFormat format = CompressionFormatDetector.detect(data);
            if (format == CompressionFormatDetector.CompressionFormat.UNKNOWN) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "无法识别的压缩格式");
            }
            
            // 获取对应的解压策略
            UnzipStrategy strategy = strategyFactory.getStrategy(format);
            
            // 创建输入流
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                // 执行解压
                Map<FileInfo, byte[]> result = strategy.unzip(inputStream, callback);
                
                // 记录指标
                long endTime = System.currentTimeMillis();
                metrics.recordUnzipTime(endTime - startTime);
                metrics.recordUnzipSize(data.length);
                metrics.recordFileCount(result.size());
                metrics.recordSuccess();
                
                log.info("文件解压完成，共解压 {} 个文件", result.size());
                return result;
            }
        } catch (Exception e) {
            metrics.recordError(UnzipErrorCode.IO_ERROR);
            log.error("文件解压失败", e);
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "文件解压失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 安全检查
     */
    private void validateSecurity(byte[] data) throws UnzipException {
        // 文件大小检查
        if (securityConfig.isFileSizeCheckEnabled() && data.length > securityConfig.getMaxFileSize()) {
            throw new UnzipException(UnzipErrorCode.FILE_TOO_LARGE, 
                String.format("文件大小超过限制: %d > %d", data.length, securityConfig.getMaxFileSize()));
        }
        
        // 文件大小检查
        if (data.length > unzipConfig.getMaxFileSize()) {
            throw new UnzipException(UnzipErrorCode.FILE_TOO_LARGE,
                String.format("文件大小超过限制: %d > %d", data.length, unzipConfig.getMaxFileSize()));
        }
    }
} 