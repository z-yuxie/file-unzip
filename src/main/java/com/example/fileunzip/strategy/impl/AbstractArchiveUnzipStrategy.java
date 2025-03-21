package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.config.UnzipConfig;
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
    
    /**
     * 构造函数
     *
     * @param unzipConfig 解压配置
     */
    protected AbstractArchiveUnzipStrategy(UnzipConfig unzipConfig) {
        if (unzipConfig == null) {
            throw new IllegalArgumentException("解压配置不能为空");
        }
        this.unzipConfig = unzipConfig;
    }
    
    /**
     * 检查是否为支持的压缩格式
     *
     * @param format 压缩格式
     * @return 如果支持返回true，否则返回false
     */
    protected abstract boolean isSupportedFormat(CompressionFormatDetector.CompressionFormat format);
    
    /**
     * 获取临时文件扩展名
     *
     * @return 临时文件扩展名
     */
    protected abstract String getTempFileExtension();
    
    /**
     * 打开压缩文件
     *
     * @param inStream 输入流
     * @return 压缩文件对象
     * @throws SevenZipException 当打开失败时抛出异常
     */
    protected abstract IInArchive openArchive(IInStream inStream) throws SevenZipException;
    
    /**
     * 获取支持的压缩格式列表
     *
     * @return 支持的压缩格式数组
     */
    @Override
    public abstract CompressionFormatDetector.CompressionFormat[] getSupportedFormats();
    
    /**
     * 解压文件
     *
     * @param inputStream 输入流
     * @return 解压结果
     * @throws UnzipException 当解压失败时抛出异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException {
        return unzip(inputStream, null, null);
    }
    
    /**
     * 解压文件
     *
     * @param inputStream 输入流
     * @param password 解压密码
     * @return 解压结果
     * @throws UnzipException 当解压失败时抛出异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException {
        return unzip(inputStream, password, null);
    }
    
    /**
     * 解压文件
     *
     * @param inputStream 输入流
     * @param callback 进度回调
     * @return 解压结果
     * @throws UnzipException 当解压失败时抛出异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException {
        return unzip(inputStream, null, callback);
    }
    
    /**
     * 解压文件
     *
     * @param inputStream 输入流
     * @param password 解压密码
     * @param callback 进度回调
     * @return 解压结果
     * @throws UnzipException 当解压失败时抛出异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        try {
            // 创建临时文件
            File tempFile = UnzipUtils.createTempFile(inputStream, unzipConfig.getTempDirectory(), getTempFileExtension(), unzipConfig);
            
            // 打开压缩文件
            IInStream inStream = new RandomAccessFileInStream(new RandomAccessFile(tempFile, "r"));
            IInArchive archive = openArchive(inStream);
            
            try {
                // 获取文件数量
                int numberOfItems = archive.getNumberOfItems();
                if (numberOfItems <= 0) {
                    throw new UnzipException("压缩文件为空");
                }
                
                // 检查文件数量限制
                if (unzipConfig.isEnableFileCountCheck() && numberOfItems > unzipConfig.getMaxFileCount()) {
                    throw new UnzipException(
                        String.format("文件数量超过限制: %d > %d", numberOfItems, unzipConfig.getMaxFileCount()));
                }
                
                // 解压文件
                Map<FileInfo, byte[]> result = new HashMap<>();
                for (int i = 0; i < numberOfItems; i++) {
                    // 获取文件路径
                    String path = (String) archive.getProperty(i, PropID.PATH);
                    if (path == null || path.isEmpty()) {
                        continue;
                    }
                    
                    // 安全检查
                    UnzipUtils.validatePath(path, unzipConfig);
                    UnzipUtils.validateFileType(path, unzipConfig);
                    
                    // 提取文件内容
                    byte[] content = UnzipUtils.extractFile(archive, i, password);
                    
                    // 检查文件大小
                    UnzipUtils.validateFileSize(content.length, unzipConfig);
                    
                    // 创建文件信息
                    FileInfo fileInfo = FileInfo.builder()
                        .fileName(path)
                        .path(path)
                        .size(content.length)
                        .lastModified(UnzipUtils.getLastModifiedTime(archive, i))
                        .build();
                    
                    result.put(fileInfo, content);
                    
                    // 通知进度
                    if (callback != null) {
                        callback.onProgress(path, content.length, content.length, i + 1, numberOfItems);
                    }
                }
                
                // 通知完成
                if (callback != null) {
                    callback.onComplete();
                }
                
                return result;
            } finally {
                archive.close();
                inStream.close();
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException("解压文件失败", e);
        }
    }
    
    /**
     * 并发解压文件
     *
     * @param inputStream 输入流
     * @param password 解压密码
     * @param callback 进度回调
     * @return 解压结果
     * @throws UnzipException 当解压失败时抛出异常
     */
    protected Map<FileInfo, byte[]> concurrentUnzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        try {
            // 创建临时文件
            File tempFile = UnzipUtils.createTempFile(inputStream, unzipConfig.getTempDirectory(), getTempFileExtension(), unzipConfig);
            
            // 打开压缩文件
            IInStream inStream = new RandomAccessFileInStream(new RandomAccessFile(tempFile, "r"));
            IInArchive archive = openArchive(inStream);
            
            try {
                // 获取文件数量
                int numberOfItems = archive.getNumberOfItems();
                if (numberOfItems <= 0) {
                    throw new UnzipException("压缩文件为空");
                }
                
                // 检查文件数量限制
                if (unzipConfig.isEnableFileCountCheck() && numberOfItems > unzipConfig.getMaxFileCount()) {
                    throw new UnzipException(
                        String.format("文件数量超过限制: %d > %d", numberOfItems, unzipConfig.getMaxFileCount()));
                }
                
                // 创建线程池
                ExecutorService executorService = Executors.newFixedThreadPool(unzipConfig.getConcurrentThreads());
                
                try {
                    // 创建任务列表
                    List<Future<Map.Entry<FileInfo, byte[]>>> futures = new ArrayList<>();
                    
                    // 提交任务
                    for (int i = 0; i < numberOfItems; i++) {
                        final int index = i;
                        futures.add(executorService.submit(() -> {
                            // 获取文件路径
                            String path = (String) archive.getProperty(index, PropID.PATH);
                            if (path == null || path.isEmpty()) {
                                return null;
                            }
                            
                            // 安全检查
                            UnzipUtils.validatePath(path, unzipConfig);
                            UnzipUtils.validateFileType(path, unzipConfig);
                            
                            // 提取文件内容
                            byte[] content = UnzipUtils.extractFile(archive, index, password);
                            
                            // 检查文件大小
                            UnzipUtils.validateFileSize(content.length, unzipConfig);
                            
                            // 创建文件信息
                            FileInfo fileInfo = FileInfo.builder()
                                .fileName(path)
                                .path(path)
                                .size(content.length)
                                .lastModified(UnzipUtils.getLastModifiedTime(archive, index))
                                .build();
                            
                            return new AbstractMap.SimpleEntry<>(fileInfo, content);
                        }));
                    }
                    
                    // 收集结果
                    Map<FileInfo, byte[]> result = new HashMap<>();
                    for (int i = 0; i < futures.size(); i++) {
                        Future<Map.Entry<FileInfo, byte[]>> future = futures.get(i);
                        try {
                            Map.Entry<FileInfo, byte[]> entry = future.get(unzipConfig.getUnzipTimeout(), TimeUnit.MILLISECONDS);
                            if (entry != null) {
                                result.put(entry.getKey(), entry.getValue());
                                
                                // 通知进度
                                if (callback != null) {
                                    callback.onProgress(entry.getKey().getPath(), entry.getValue().length, entry.getValue().length, i + 1, numberOfItems);
                                }
                            }
                        } catch (Exception e) {
                            throw new UnzipException("解压文件失败", e);
                        }
                    }
                    
                    // 通知完成
                    if (callback != null) {
                        callback.onComplete();
                    }
                    
                    return result;
                } finally {
                    executorService.shutdown();
                }
            } finally {
                archive.close();
                inStream.close();
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException("解压文件失败", e);
        }
    }

    @Override
    public void close() throws IOException {
        // 不需要额外的清理工作
    }
} 