package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipErrorCode;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import com.yuxie.common.compress.util.CompressionCompositeInputStream;
import com.yuxie.common.compress.util.UnzipUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import java.io.*;
import java.util.*;

/**
 * 基于7-Zip-JBinding的压缩文件解压抽象基类
 * <p>
 * 该类提供了使用7-Zip-JBinding库解压7Z、RAR等格式文件的通用实现。
 * 子类只需要实现{@link #isSupportedFormat(CompressionFormat)}和{@link #getSupportedFormats()}方法
 * 来指定支持的压缩格式。
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractSevenZipStrategy implements UnzipStrategy {
    
    /**
     * 解压配置
     */
    protected final UnzipConfig unzipConfig;
    
    /**
     * 构造函数
     *
     * @param unzipConfig 解压配置，不能为空
     * @throws IllegalArgumentException 当unzipConfig为null时抛出
     */
    protected AbstractSevenZipStrategy(UnzipConfig unzipConfig) {
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
    public abstract boolean isSupportedFormat(CompressionFormat format);
    
    /**
     * 获取支持的压缩格式列表
     *
     * @return 支持的压缩格式数组
     */
    @Override
    public abstract CompressionFormat[] getSupportedFormats();
    
    /**
     * 解压文件
     *
     * @param inputStream 输入流
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException {
        return unzip(inputStream, null, null);
    }
    
    /**
     * 解压文件（带密码）
     *
     * @param inputStream 输入流
     * @param password 密码
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException {
        return unzip(inputStream, password, null);
    }
    
    /**
     * 解压文件（带进度回调）
     *
     * @param inputStream 输入流
     * @param callback 进度回调
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException {
        return unzip(inputStream, null, callback);
    }
    
    /**
     * 解压文件（带密码和进度回调）
     *
     * @param inputStream 输入流
     * @param password 密码
     * @param callback 进度回调
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        if (inputStream == null) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "输入流不能为空");
        }

        // 直接调用内部解压方法，不再创建额外的CompressionCompositeInputStream
        return unzipInternal(inputStream, password, callback);
    }

    /**
     * 解压文件（使用已封装的复合输入流）
     *
     * @param compositeInputStream 已封装的复合输入流
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream) throws UnzipException {
        return unzipWithCompositeStream(compositeInputStream, null, null);
    }

    /**
     * 解压文件（使用已封装的复合输入流，带进度回调）
     *
     * @param compositeInputStream 已封装的复合输入流
     * @param callback 进度回调
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, UnzipProgressCallback callback) throws UnzipException {
        return unzipWithCompositeStream(compositeInputStream, null, callback);
    }

    /**
     * 解压文件（使用已封装的复合输入流，带密码）
     *
     * @param compositeInputStream 已封装的复合输入流
     * @param password 密码
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, String password) throws UnzipException {
        return unzipWithCompositeStream(compositeInputStream, password, null);
    }

    /**
     * 解压文件（使用已封装的复合输入流，带密码和进度回调）
     *
     * @param compositeInputStream 已封装的复合输入流
     * @param password 密码
     * @param callback 进度回调
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    @Override
    public Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        if (compositeInputStream == null) {
            throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "输入流不能为空");
        }

        // 直接调用内部解压方法
        return unzipInternal(compositeInputStream, password, callback);
    }
    
    /**
     * 内部解压方法，处理实际的解压逻辑
     * <p>
     * 该方法实现了使用7-Zip-JBinding库解压文件的核心逻辑，包括：
     * 1. 读取输入流数据
     * 2. 创建临时文件
     * 3. 初始化7-Zip-JBinding
     * 4. 打开压缩包
     * 5. 提取文件内容
     * 6. 处理进度回调
     * 7. 资源清理
     * </p>
     *
     * @param inputStream 输入流
     * @param password 密码
     * @param callback 进度回调
     * @return 解压后的文件信息及其内容
     * @throws UnzipException 解压异常
     */
    private Map<FileInfo, byte[]> unzipInternal(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException {
        // 读取输入流数据
        byte[] data;
        try {
            data = readInputStream(inputStream);
        } catch (IOException e) {
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "读取输入流失败", e);
        }

        // 创建临时文件
        File tempFile = null;
        try {
            tempFile = createTempFile(data);
            
            // 初始化7-Zip-JBinding
            SevenZip.initSevenZipFromPlatformJAR();
            
            // 打开压缩包
            RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r");
            IInArchive archive = SevenZip.openInArchive(null, 
                new RandomAccessFileInStream(randomAccessFile));
            
            try {
                // 获取所有条目
                int itemCount = archive.getNumberOfItems();
                
                // 检查文件数量限制
                if (unzipConfig.isEnableFileCountCheck() && itemCount > unzipConfig.getMaxFileCount()) {
                    throw new UnzipException(UnzipErrorCode.INVALID_FORMAT,
                        String.format("文件数量超过限制: %d > %d", itemCount, unzipConfig.getMaxFileCount()));
                }
                
                // 通知开始解压
                if (callback != null) {
                    callback.onStart(data.length, itemCount);
                }
                
                // 解压文件
                Map<FileInfo, byte[]> result = new HashMap<>();
                int currentFile = 0;
                long totalBytesRead = 0;
                
                // 使用Simple接口简化操作
                ISimpleInArchive simpleArchive = archive.getSimpleInterface();
                
                for (ISimpleInArchiveItem item : simpleArchive.getArchiveItems()) {
                    String path = item.getPath();
                    
                    if (path == null || path.trim().isEmpty() || item.isFolder()) {
                        continue;
                    }
                    
                    // 安全检查
                    UnzipUtils.validatePath(path, unzipConfig);
                    UnzipUtils.validateFileType(path, unzipConfig);
                    
                    // 提取文件内容
                    byte[] content = extractItem(item, password);
                    if (content == null) {
                        continue;
                    }
                    
                    // 检查文件大小
                    UnzipUtils.validateFileSize(content.length, unzipConfig);
                    
                    // 创建文件信息
                    FileInfo fileInfo = FileInfo.builder()
                        .fileName(new File(path).getName())
                        .path(path)
                        .size(content.length)
                        .lastModified(item.getLastWriteTime() != null ? item.getLastWriteTime().getTime() : new Date().getTime())
                        .build();
                    
                    // 添加到结果集
                    result.put(fileInfo, content);
                    
                    // 更新进度
                    currentFile++;
                    totalBytesRead += content.length;
                    if (callback != null) {
                        callback.onProgress(fileInfo.getFileName(), totalBytesRead, data.length, currentFile, itemCount);
                    }
                }
                
                // 通知解压完成
                if (callback != null) {
                    callback.onComplete();
                }
                
                return result;
                
            } finally {
                archive.close();
                randomAccessFile.close();
            }
            
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "解压失败: " + e.getMessage(), e);
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * 创建临时文件
     * <p>
     * 将输入流数据写入临时文件，用于7-Zip-JBinding库处理
     * </p>
     *
     * @param data 要写入的数据
     * @return 创建的临时文件
     * @throws IOException 创建或写入文件失败时抛出
     */
    private File createTempFile(byte[] data) throws IOException {
        File tempFile = File.createTempFile("unzip_", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(data);
        }
        return tempFile;
    }
    
    /**
     * 提取压缩包中的条目内容
     * <p>
     * 使用7-Zip-JBinding库的Simple接口提取压缩包中的条目内容
     * </p>
     *
     * @param item 压缩包中的条目
     * @param password 密码
     * @return 提取的内容
     * @throws Exception 提取失败时抛出
     */
    private byte[] extractItem(ISimpleInArchiveItem item, String password) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        item.extractSlow(new ISequentialOutStream() {
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
        
        return outputStream.toByteArray();
    }
    
    /**
     * 读取输入流数据
     * <p>
     * 将输入流中的数据读取到字节数组中
     * </p>
     *
     * @param inputStream 输入流
     * @return 读取的数据
     * @throws IOException 读取失败时抛出
     */
    protected byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[unzipConfig.getBufferSize()];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        return outputStream.toByteArray();
    }
    
    /**
     * 关闭资源
     * <p>
     * 该方法不需要额外清理资源，因为所有资源都在使用后立即关闭
     * </p>
     *
     * @throws IOException 关闭资源时可能发生的异常
     */
    @Override
    public void close() throws IOException {
        // 不需要额外清理资源
    }
} 