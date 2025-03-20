package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.exception.UnzipErrorCode;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.util.CompressionFormatDetector;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 压缩文件解压抽象基类
 */
@Slf4j
public abstract class AbstractArchiveUnzipStrategy implements UnzipStrategy {
    
    protected final UnzipConfig unzipConfig;
    protected final SecurityConfig securityConfig;
    protected File tempFile;
    protected IInArchive archive;
    protected ExecutorService executorService;
    
    protected AbstractArchiveUnzipStrategy(UnzipConfig unzipConfig, SecurityConfig securityConfig) {
        this.unzipConfig = unzipConfig;
        this.securityConfig = securityConfig;
        if (unzipConfig.isEnableConcurrentUnzip()) {
            this.executorService = Executors.newFixedThreadPool(unzipConfig.getConcurrentThreads());
        }
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException {
        return unzip(inputStream, null, null);
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException {
        return unzip(inputStream, password, null);
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException {
        return unzip(inputStream, null, callback);
    }
    
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        try {
            // 创建临时文件
            createTempFile(inputStream);
            
            // 打开压缩文件
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r")) {
                archive = openArchive(new RandomAccessFileInStream(randomAccessFile));
                
                // 获取文件总大小和数量
                long totalSize = calculateTotalSize(archive);
                int totalFiles = archive.getNumberOfItems();
                
                // 通知开始解压
                if (callback != null) {
                    callback.onStart(totalSize, totalFiles);
                }
                
                Map<FileInfo, byte[]> result = new ConcurrentHashMap<>();
                
                if (unzipConfig.isEnableConcurrentUnzip()) {
                    // 并发解压
                    result = concurrentUnzip(archive, password, callback);
                } else {
                    // 顺序解压
                    result = sequentialUnzip(archive, password, callback);
                }
                
                // 通知完成
                if (callback != null) {
                    callback.onComplete();
                }
                
                return result;
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw translateException(e);
        } finally {
            cleanup();
        }
    }
    
    private Map<FileInfo, byte[]> concurrentUnzip(IInArchive archive, String password, UnzipProgressCallback callback) throws UnzipException {
        Map<FileInfo, byte[]> result = new ConcurrentHashMap<>();
        AtomicInteger completedFiles = new AtomicInteger(0);
        AtomicLong processedSize = new AtomicLong(0);
        List<Future<?>> futures = new ArrayList<>();
        
        try {
            int numberOfItems = archive.getNumberOfItems();
            for (int i = 0; i < numberOfItems; i++) {
                final int index = i;
                futures.add(executorService.submit(() -> {
                    String currentPath = null;
                    try {
                        currentPath = archive.getStringProperty(index, PropID.PATH);
                        if (currentPath == null || currentPath.isEmpty()) {
                            return;
                        }
                        
                        Boolean isFolder = (Boolean) archive.getProperty(index, PropID.IS_FOLDER);
                        if (isFolder != null && isFolder) {
                            return;
                        }
                        
                        // 安全检查
                        validatePath(currentPath);
                        validateFileType(currentPath);
                        
                        // 提取文件
                        byte[] content = extractFile(archive, index, password);
                        
                        // 安全检查
                        validateFileSize(content.length);
                        
                        // 创建文件信息
                        FileInfo fileInfo = FileInfo.builder()
                            .fileName(FilenameUtils.getName(currentPath))
                            .path(currentPath)
                            .size(content.length)
                            .lastModified(getLastModifiedTime(archive, index))
                            .build();
                        
                        result.put(fileInfo, content);
                        
                        // 更新进度
                        completedFiles.incrementAndGet();
                        processedSize.addAndGet(content.length);
                        
                        if (callback != null) {
                            callback.onFileComplete(currentPath, completedFiles.get(), numberOfItems);
                        }
                    } catch (Exception e) {
                        log.error("解压文件失败: {}", currentPath, e);
                        throw new CompletionException(e);
                    }
                }));
            }
        } catch (SevenZipException e) {
            throw new UnzipException(UnzipErrorCode.UNZIP_ERROR, "获取压缩文件信息失败", e);
        }
        
        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get(unzipConfig.getUnzipTimeout(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new UnzipException(UnzipErrorCode.UNZIP_ERROR, "并发解压失败", e);
            }
        }
        
        return result;
    }
    
    private Map<FileInfo, byte[]> sequentialUnzip(IInArchive archive, String password, UnzipProgressCallback callback) throws UnzipException {
        Map<FileInfo, byte[]> result = new HashMap<>();
        
        try {
            for (int i = 0; i < archive.getNumberOfItems(); i++) {
                String path = archive.getStringProperty(i, PropID.PATH);
                if (path == null || path.isEmpty()) {
                    continue;
                }
                
                Boolean isFolder = (Boolean) archive.getProperty(i, PropID.IS_FOLDER);
                if (isFolder != null && isFolder) {
                    continue;
                }
                
                // 安全检查
                validatePath(path);
                validateFileType(path);
                
                // 提取文件
                byte[] content = extractFile(archive, i, password);
                
                // 安全检查
                validateFileSize(content.length);
                
                // 创建文件信息
                FileInfo fileInfo = FileInfo.builder()
                    .fileName(FilenameUtils.getName(path))
                    .path(path)
                    .size(content.length)
                    .lastModified(getLastModifiedTime(archive, i))
                    .build();
                
                result.put(fileInfo, content);
                
                // 通知进度
                if (callback != null) {
                    callback.onFileComplete(path, i + 1, archive.getNumberOfItems());
                }
            }
        } catch (SevenZipException e) {
            throw new UnzipException(UnzipErrorCode.UNZIP_ERROR, "解压失败: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    @Override
    public void close() throws IOException {
        cleanup();
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    protected abstract IInArchive openArchive(IInStream inStream) throws SevenZipException;
    
    protected abstract boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format);
    
    protected abstract String getTempFileExtension();
    
    private void createTempFile(InputStream inputStream) throws UnzipException {
        try {
            tempFile = File.createTempFile("unzip_", getTempFileExtension(), new File(unzipConfig.getTempDirectory()));
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[unzipConfig.getBufferSize()];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    totalBytesRead += bytesRead;
                    validateFileSize(totalBytesRead);
                    fos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "创建临时文件失败", e);
        }
    }
    
    private void validatePath(String path) throws UnzipException {
        if (securityConfig.isPathTraversalCheckEnabled() && !isPathSafe(path)) {
            throw new UnzipException(UnzipErrorCode.SECURITY_ERROR, "检测到路径遍历攻击: " + path);
        }
    }
    
    private void validateFileType(String path) throws UnzipException {
        if (securityConfig.isFileTypeCheckEnabled() && !isFileTypeAllowed(path)) {
            throw new UnzipException(UnzipErrorCode.SECURITY_ERROR, "不允许的文件类型: " + path);
        }
    }
    
    protected void validateFileSize(long size) throws UnzipException {
        if (securityConfig.isFileSizeCheckEnabled() && size > securityConfig.getMaxFileSize()) {
            throw new UnzipException(UnzipErrorCode.FILE_TOO_LARGE, 
                String.format("文件大小超过限制: %d > %d", size, securityConfig.getMaxFileSize()));
        }
    }
    
    private long calculateTotalSize(IInArchive archive) throws SevenZipException {
        long totalSize = 0;
        for (int i = 0; i < archive.getNumberOfItems(); i++) {
            Long size = (Long) archive.getProperty(i, PropID.SIZE);
            if (size != null) {
                totalSize += size;
            }
        }
        return totalSize;
    }
    
    private long getLastModifiedTime(IInArchive archive, int index) throws SevenZipException {
        Object time = archive.getProperty(index, PropID.LAST_MODIFICATION_TIME);
        return time instanceof Date ? ((Date) time).getTime() : System.currentTimeMillis();
    }
    
    private byte[] extractFile(IInArchive archive, int index, String password) throws SevenZipException {
        ExtractOperationResult result;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        result = archive.extractSlow(index, new ISequentialOutStream() {
            @Override
            public int write(byte[] data) throws SevenZipException {
                try {
                    outputStream.write(data);
                    return data.length;
                } catch (IOException e) {
                    throw new SevenZipException("写入数据失败", e);
                }
            }
        }, password);
        
        if (result != ExtractOperationResult.OK) {
            throw new SevenZipException("提取文件失败: " + result);
        }
        
        return outputStream.toByteArray();
    }
    
    private void cleanup() {
        if (archive != null) {
            try {
                archive.close();
            } catch (SevenZipException e) {
                log.warn("关闭压缩文件失败", e);
            }
            archive = null;
        }
        
        if (tempFile != null && tempFile.exists()) {
            if (!tempFile.delete()) {
                tempFile.deleteOnExit();
            }
            tempFile = null;
        }
    }
    
    protected boolean isPathSafe(String path) {
        if (path == null) {
            return false;
        }
        
        String normalizedPath = FilenameUtils.normalize(path);
        return normalizedPath != null && !normalizedPath.startsWith("/") && !normalizedPath.startsWith("..");
    }
    
    protected boolean isFileTypeAllowed(String path) {
        if (path == null) {
            return false;
        }
        
        String extension = FilenameUtils.getExtension(path).toLowerCase();
        return securityConfig.getAllowedFileTypes().contains(extension);
    }
    
    protected UnzipException translateException(Exception e) {
        if (e instanceof UnzipException) {
            return (UnzipException) e;
        }
        
        if (e instanceof SevenZipException) {
            return new UnzipException(UnzipErrorCode.UNZIP_ERROR, "解压失败: " + e.getMessage(), e);
        }
        
        if (e instanceof IOException) {
            return new UnzipException(UnzipErrorCode.IO_ERROR, "IO错误: " + e.getMessage(), e);
        }
        
        return new UnzipException(UnzipErrorCode.UNKNOWN_ERROR, "未知错误: " + e.getMessage(), e);
    }
} 