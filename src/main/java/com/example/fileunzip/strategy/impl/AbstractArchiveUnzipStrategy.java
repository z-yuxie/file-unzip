package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.config.UnzipConfigManager;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.util.CompressionFormatDetector;
import net.sf.sevenzipjbinding.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * 归档文件解压策略的抽象基类
 * 提供通用的7-Zip-JBinding实现逻辑
 */
public abstract class AbstractArchiveUnzipStrategy implements UnzipStrategy {
    
    protected final UnzipConfig config;
    
    static {
        try {
            SevenZip.initSevenZipFromPlatformJAR();
        } catch (Exception e) {
            throw new RuntimeException("初始化7-Zip-JBinding失败", e);
        }
    }
    
    protected AbstractArchiveUnzipStrategy() {
        this.config = UnzipConfigManager.getInstance().getConfig();
    }

    @Override
    public Map<FileInfo, byte[]> unzip(byte[] data) throws IOException {
        Map<FileInfo, byte[]> result = new HashMap<>();
        
        // 检查输入数据大小
        if (data.length > config.getMaxFileSize()) {
            throw new UnzipException("文件大小超过限制: " + data.length + " > " + config.getMaxFileSize());
        }
        
        // 检测压缩格式
        CompressionFormatDetector.CompressionFormat format = CompressionFormatDetector.detect(data);
        
        // 验证是否为支持的格式
        if (!isSupportedFormat(format)) {
            throw new UnzipException("不支持的文件格式: " + format);
        }
        
        // 创建临时文件
        String timestamp = String.valueOf(System.currentTimeMillis());
        File tempFile = File.createTempFile("temp_archive_" + timestamp + "_", getTempFileExtension(), new File(config.getTempDirectory()));
        try {
            // 写入数据到临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(data);
            }
            
            // 打开归档文件
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r")) {
                IInArchive archive = openArchive(randomAccessFile);
                
                try {
                    // 解压所有文件
                    archive.extract(null, false, createExtractCallback(archive, result));
                } finally {
                    archive.close();
                }
            }
        } finally {
            // 删除临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
        
        return result;
    }

    /**
     * 检查是否为支持的格式
     */
    protected abstract boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format);

    /**
     * 获取临时文件扩展名
     */
    protected abstract String getTempFileExtension();

    /**
     * 打开归档文件
     */
    protected abstract IInArchive openArchive(RandomAccessFile randomAccessFile) throws IOException;

    /**
     * 创建解压回调
     */
    protected IArchiveExtractCallback createExtractCallback(IInArchive archive, Map<FileInfo, byte[]> result) {
        return new IArchiveExtractCallback() {
            private ByteArrayOutputStream currentStream;
            private int currentIndex;
            
            @Override
            public void setTotal(long total) {}
            
            @Override
            public void setCompleted(long complete) {}
            
            @Override
            public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
                if (extractAskMode != ExtractAskMode.EXTRACT) {
                    return null;
                }
                
                if ((Boolean) archive.getProperty(index, PropID.IS_FOLDER)) {
                    return null;
                }
                
                // 检查文件大小
                Long size = (Long) archive.getProperty(index, PropID.SIZE);
                if (size != null && size > config.getMaxFileSize()) {
                    throw new UnzipException("文件大小超过限制: " + size + " > " + config.getMaxFileSize());
                }
                
                // 检查文件路径
                if (config.isEnablePathSecurityCheck()) {
                    String path = (String) archive.getProperty(index, PropID.PATH);
                    if (path != null && !isValidPath(path)) {
                        throw new UnzipException("非法的文件路径: " + path);
                    }
                }
                
                currentIndex = index;
                currentStream = new ByteArrayOutputStream();
                return new ISequentialOutStream() {
                    @Override
                    public int write(byte[] data) throws SevenZipException {
                        currentStream.write(data, 0, data.length);
                        return data.length;
                    }
                };
            }
            
            @Override
            public void prepareOperation(ExtractAskMode extractAskMode) {}
            
            @Override
            public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
                if (currentStream != null) {
                    try {
                        currentStream.close();
                        String path = (String) archive.getProperty(currentIndex, PropID.PATH);
                        Long size = (Long) archive.getProperty(currentIndex, PropID.SIZE);
                        Long modificationTime = (Long) archive.getProperty(currentIndex, PropID.LAST_MODIFICATION_TIME);
                        
                        FileInfo fileInfo = FileInfo.builder()
                            .fileName(path)
                            .size(size)
                            .lastModified(modificationTime != null ? modificationTime : System.currentTimeMillis())
                            .path(path)
                            .build();
                        result.put(fileInfo, currentStream.toByteArray());
                    } catch (IOException e) {
                        throw new SevenZipException("关闭输出流失败", e);
                    }
                }
            }
        };
    }
    
    /**
     * 检查文件路径是否合法
     */
    private boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        // 检查路径是否包含危险字符
        String[] dangerousChars = {"..", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerousChar : dangerousChars) {
            if (path.contains(dangerousChar)) {
                return false;
            }
        }
        
        // 检查路径长度
        if (path.length() > 255) {
            return false;
        }
        
        return true;
    }
} 