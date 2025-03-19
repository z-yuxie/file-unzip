package com.example.fileunzip.service;

import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.strategy.impl.CompressedFileUnzipStrategy;
import com.example.fileunzip.strategy.impl.RarUnzipStrategy;
import com.example.fileunzip.strategy.impl.SevenZipUnzipStrategy;
import com.example.fileunzip.strategy.impl.TarUnzipStrategy;
import com.example.fileunzip.strategy.impl.ZipUnzipStrategy;
import com.example.fileunzip.util.CompressionFormatDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
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
@Service
public class UnzipService {
    
    private final Map<CompressionFormatDetector.CompressionFormat, UnzipStrategy> strategies;
    
    /**
     * 构造函数
     * 初始化所有支持的解压策略
     */
    public UnzipService() {
        strategies = new HashMap<>();
        // 注册所有支持的解压策略
        strategies.put(CompressionFormatDetector.CompressionFormat.ZIP, new ZipUnzipStrategy());
        strategies.put(CompressionFormatDetector.CompressionFormat.RAR, new RarUnzipStrategy());
        strategies.put(CompressionFormatDetector.CompressionFormat.SEVEN_ZIP, new SevenZipUnzipStrategy());
        strategies.put(CompressionFormatDetector.CompressionFormat.TAR, new TarUnzipStrategy());
        strategies.put(CompressionFormatDetector.CompressionFormat.TAR_GZ, new TarUnzipStrategy());
        strategies.put(CompressionFormatDetector.CompressionFormat.TAR_BZ2, new TarUnzipStrategy());
        strategies.put(CompressionFormatDetector.CompressionFormat.TAR_XZ, new TarUnzipStrategy());
        strategies.put(CompressionFormatDetector.CompressionFormat.GZIP, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.GZIP));
        strategies.put(CompressionFormatDetector.CompressionFormat.BZIP2, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.BZIP2));
        strategies.put(CompressionFormatDetector.CompressionFormat.XZ, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.XZ));
        strategies.put(CompressionFormatDetector.CompressionFormat.LZMA, new CompressedFileUnzipStrategy(CompressionFormatDetector.CompressionFormat.LZMA));
    }
    
    /**
     * 解压文件
     *
     * @param data 压缩文件数据
     * @return 解压后的文件信息及其内容
     * @throws IOException IO异常
     */
    public Map<FileInfo, byte[]> unzip(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("压缩文件数据不能为空");
        }
        
        log.info("开始解压文件，数据大小: {} 字节", data.length);
        
        try {
            // 检测压缩格式
            CompressionFormatDetector.CompressionFormat format = CompressionFormatDetector.detect(data);
            if (format == CompressionFormatDetector.CompressionFormat.UNKNOWN) {
                throw new IllegalArgumentException("无法识别的压缩格式");
            }
            
            // 获取对应的解压策略
            UnzipStrategy strategy = strategies.get(format);
            if (strategy == null) {
                throw new IllegalArgumentException("不支持的压缩格式: " + format);
            }
            
            // 执行解压
            Map<FileInfo, byte[]> result = strategy.unzip(data);
            log.info("文件解压完成，共解压 {} 个文件", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("文件解压失败", e);
            throw e;
        }
    }
} 