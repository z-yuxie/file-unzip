package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.config.SecurityConfig;
import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.exception.UnzipErrorCode;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.strategy.UnzipStrategy;
import com.example.fileunzip.util.CompressionFormatDetector;
import com.example.fileunzip.util.UnzipUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
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
    
    // 添加线程池配置
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 1000;
    
    protected AbstractArchiveUnzipStrategy(UnzipConfig unzipConfig, SecurityConfig securityConfig) {
        this.unzipConfig = unzipConfig;
        this.securityConfig = securityConfig;
        if (unzipConfig.isEnableConcurrentUnzip()) {
            // 使用 ThreadPoolExecutor 替代 FixedThreadPool，提供更好的控制
            this.executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "unzip-worker-" + threadNumber.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
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
            tempFile = UnzipUtils.createTempFile(inputStream, unzipConfig.getTempDirectory(), getTempFileExtension(), securityConfig);
            
            // 打开压缩文件
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r")) {
                archive = openArchive(new RandomAccessFileInStream(randomAccessFile));
                
                // 获取文件总大小和数量
                long totalSize = UnzipUtils.calculateTotalSize(archive);
                int totalFiles;
                try {
                    totalFiles = archive.getNumberOfItems();
                } catch (SevenZipException e) {
                    throw new UnzipException(UnzipErrorCode.UNZIP_ERROR, "获取压缩文件数量失败", e);
                }
                
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
    
    private Map<FileInfo, byte[]> concurrentUnzip(IInArchive archive, String password, 
            UnzipProgressCallback callback) throws UnzipException {
        Map<FileInfo, byte[]> result = new ConcurrentHashMap<>();
        AtomicInteger completedFiles = new AtomicInteger(0);
        AtomicLong processedSize = new AtomicLong(0);
        
        try {
            int numberOfItems;
            try {
                numberOfItems = archive.getNumberOfItems();
            } catch (SevenZipException e) {
                throw new UnzipException(UnzipErrorCode.UNZIP_ERROR, "获取压缩文件数量失败", e);
            }
            CountDownLatch latch = new CountDownLatch(numberOfItems);
            
            for (int i = 0; i < numberOfItems; i++) {
                final int index = i;
                executorService.submit(() -> {
                    try {
                        String path = archive.getStringProperty(index, PropID.PATH);
                        if (path == null || path.isEmpty()) {
                            return;
                        }
                        
                        Boolean isFolder = (Boolean) archive.getProperty(index, PropID.IS_FOLDER);
                        if (isFolder != null && isFolder) {
                            return;
                        }
                        
                        // 安全检查
                        UnzipUtils.validatePath(path, securityConfig);
                        UnzipUtils.validateFileType(path, securityConfig);
                        
                        // 提取文件
                        byte[] content = UnzipUtils.extractFile(archive, index, password);
                        
                        // 安全检查
                        UnzipUtils.validateFileSize(content.length, securityConfig);
                        
                        // 创建文件信息
                        FileInfo fileInfo = FileInfo.builder()
                            .fileName(FilenameUtils.getName(path))
                            .path(path)
                            .size(content.length)
                            .lastModified(UnzipUtils.getLastModifiedTime(archive, index))
                            .build();
                        
                        result.put(fileInfo, content);
                        
                        // 更新进度
                        processedSize.addAndGet(content.length);
                        completedFiles.incrementAndGet();
                        
                        if (callback != null) {
                            try {
                                Long size = (Long) archive.getProperty(index, PropID.SIZE);
                                callback.onProgress(path, processedSize.get(), 
                                    size != null ? size : 0L,
                                    completedFiles.get(), numberOfItems);
                            } catch (SevenZipException e) {
                                log.warn("获取文件大小失败: {}", e.getMessage());
                                callback.onProgress(path, processedSize.get(), 0L,
                                    completedFiles.get(), numberOfItems);
                            }
                        }
                    } catch (Exception e) {
                        log.error("解压文件失败: {}", e.getMessage(), e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // 等待所有任务完成
            if (!latch.await(unzipConfig.getUnzipTimeout(), TimeUnit.MILLISECONDS)) {
                throw new UnzipException(UnzipErrorCode.TIMEOUT_ERROR, "解压超时");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnzipException(UnzipErrorCode.INTERRUPTED_ERROR, "解压被中断");
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
                UnzipUtils.validatePath(path, securityConfig);
                UnzipUtils.validateFileType(path, securityConfig);
                
                // 提取文件
                byte[] content = UnzipUtils.extractFile(archive, i, password);
                
                // 安全检查
                UnzipUtils.validateFileSize(content.length, securityConfig);
                
                // 创建文件信息
                FileInfo fileInfo = FileInfo.builder()
                    .fileName(FilenameUtils.getName(path))
                    .path(path)
                    .size(content.length)
                    .lastModified(UnzipUtils.getLastModifiedTime(archive, i))
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