package com.yuxie.common.compress.service;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipErrorCode;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.monitor.UnzipMetrics;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import com.yuxie.common.compress.strategy.UnzipStrategyFactory;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.format.CompressionFormatDetector;
import com.yuxie.common.compress.format.DecompressorFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final UnzipConfig unzipConfig;
    private final UnzipMetrics metrics;
    
    public UnzipService(UnzipStrategyFactory strategyFactory,
                       UnzipConfig unzipConfig,
                       UnzipMetrics metrics) {
        this.strategyFactory = strategyFactory;
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
        return unzipInternal(data, null);
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
        return unzipInternal(data, callback);
    }
    
    /**
     * 内部解压方法，处理公共的解压逻辑
     *
     * @param data 压缩文件数据
     * @param callback 进度回调（可选）
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    private Map<FileInfo, byte[]> unzipInternal(byte[] data, UnzipProgressCallback callback) throws UnzipException {
        if (data == null || data.length == 0) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "压缩文件数据不能为空");
        }
        
        // 安全检查
        validateSecurity(data);
        
        log.info("开始解压文件，数据大小: {} 字节", data.length);
        long startTime = System.currentTimeMillis();
        
        try {
            // 检测压缩格式
            CompressionFormat format = CompressionFormatDetector.detect(data);
            if (format == CompressionFormat.UNKNOWN) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "无法识别的压缩格式");
            }
            
            // 获取对应的解压策略
            UnzipStrategy strategy = strategyFactory.getStrategy(format);
            
            // 创建输入流并执行解压
            Map<FileInfo, byte[]> result;
            try (InputStream inputStream = createInputStream(data, format)) {
                result = callback != null ? 
                    strategy.unzip(inputStream, callback) : 
                    strategy.unzip(inputStream);
            }
            
            // 记录指标
            recordMetrics(startTime, data.length, result.size());
            
            log.info("文件解压完成，共解压 {} 个文件", result.size());
            return result;
            
        } catch (Exception e) {
            handleError(e);
            if (e instanceof UnzipException) {
                throw (UnzipException) e;
            }
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "文件解压失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建输入流
     */
    private InputStream createInputStream(byte[] data, CompressionFormat format) throws IOException {
        if (format == CompressionFormat.RAR || format == CompressionFormat.SEVEN_ZIP) {
            return new ByteArrayInputStream(data);
        }
        return DecompressorFactory.createDecompressor(data, format);
    }
    
    private void recordMetrics(long startTime, long dataSize, int fileCount) {
        long endTime = System.currentTimeMillis();
        metrics.recordUnzipTime(endTime - startTime);
        metrics.recordUnzipSize(dataSize);
        metrics.recordFileCount(fileCount);
        metrics.recordSuccess();
    }
    
    private void handleError(Exception e) {
        metrics.recordError(UnzipErrorCode.IO_ERROR);
        log.error("文件解压失败", e);
    }
    
    /**
     * 安全检查
     */
    private void validateSecurity(byte[] data) throws UnzipException {
        // 文件大小检查
        if (data.length > unzipConfig.getMaxFileSize()) {
            throw new UnzipException(UnzipErrorCode.FILE_TOO_LARGE,
                String.format("文件大小超过限制: %d > %d", data.length, unzipConfig.getMaxFileSize()));
        }
    }
    
    /**
     * 解压文件到内存
     */
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        return unzipInternal(inputStream, password, callback, null);
    }
    
    /**
     * 解压文件到指定目录
     */
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback, String targetPath) throws UnzipException {
        return unzipInternal(inputStream, password, callback, targetPath);
    }
    
    /**
     * 内部解压方法，处理共同的解压逻辑
     */
    private Map<FileInfo, byte[]> unzipInternal(InputStream inputStream, String password, UnzipProgressCallback callback, String targetPath) throws UnzipException {
        // 参数验证
        if (inputStream == null) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "输入流不能为空");
        }
        
        // 读取输入流数据
        byte[] data;
        try {
            data = readInputStream(inputStream);
        } catch (IOException e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "读取输入流失败", e);
        }
        
        // 检测压缩格式
        CompressionFormat format = CompressionFormatDetector.detect(data);
        if (format == null) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "无法识别的压缩格式");
        }
        
        // 获取解压策略
        UnzipStrategy strategy = strategyFactory.getStrategy(format);
        if (strategy == null) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "不支持的压缩格式: " + format);
        }
        
        try {
            // 执行解压
            Map<FileInfo, byte[]> result = strategy.unzip(inputStream, password, callback);
            
            // 如果指定了目标路径，将文件写入磁盘
            if (targetPath != null && !targetPath.trim().isEmpty()) {
                writeFilesToDisk(result, targetPath);
            }
            
            return result;
        } catch (Exception e) {
            log.error("解压失败: {}", e.getMessage(), e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "解压失败: " + e.getMessage(), e);
        } finally {
            try {
                strategy.close();
            } catch (IOException e) {
                log.warn("关闭解压策略失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 读取输入流数据
     */
    private byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[unzipConfig.getBufferSize()];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        return outputStream.toByteArray();
    }
    
    /**
     * 将解压后的文件写入磁盘
     */
    private void writeFilesToDisk(Map<FileInfo, byte[]> files, String targetPath) throws UnzipException {
        try {
            // 创建目标目录
            Path targetDir = Paths.get(targetPath);
            Files.createDirectories(targetDir);
            
            // 写入文件
            for (Map.Entry<FileInfo, byte[]> entry : files.entrySet()) {
                FileInfo fileInfo = entry.getKey();
                byte[] content = entry.getValue();
                
                // 构建目标文件路径
                Path targetFile = targetDir.resolve(fileInfo.getPath());
                
                // 创建父目录
                Files.createDirectories(targetFile.getParent());
                
                // 写入文件内容
                try (FileOutputStream fos = new FileOutputStream(targetFile.toFile())) {
                    fos.write(content);
                }
            }
        } catch (IOException e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "写入文件失败: " + e.getMessage(), e);
        }
    }
} 