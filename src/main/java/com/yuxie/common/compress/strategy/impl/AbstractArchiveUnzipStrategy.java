package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipErrorCode;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import com.yuxie.common.compress.util.UnzipUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;

import java.io.*;
import java.util.*;

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
    protected abstract boolean isSupportedFormat(CompressionFormat format);
    
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
    protected abstract IInArchive openArchive(IInStream inStream) throws Exception;
    
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

        // 创建 IInStream
        IInStream inStream = new MemoryInStream(data);

        try {
            // 打开压缩文件
            IInArchive archive = openArchive(inStream);
            if (archive == null) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "无法打开压缩文件");
            }

            // 获取文件数量
            int fileCount = archive.getNumberOfItems();
            if (fileCount <= 0) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT, "压缩文件为空");
            }

            // 检查文件数量限制
            if (unzipConfig.isEnableFileCountCheck() && fileCount > unzipConfig.getMaxFileCount()) {
                throw new UnzipException(UnzipErrorCode.INVALID_FORMAT,
                    String.format("文件数量超过限制: %d > %d", fileCount, unzipConfig.getMaxFileCount()));
            }

            // 通知开始解压
            if (callback != null) {
                callback.onStart(data.length, fileCount);
            }

            // 解压文件
            Map<FileInfo, byte[]> result = new HashMap<>();
            int currentFile = 0;
            long totalBytesRead = 0;

            for (int i = 0; i < fileCount; i++) {
                String path = (String) archive.getProperty(i, PropID.PATH);
                if (path == null || path.trim().isEmpty()) {
                    continue;
                }

                // 安全检查
                UnzipUtils.validatePath(path, unzipConfig);
                UnzipUtils.validateFileType(path, unzipConfig);

                // 提取文件内容
                byte[] content = UnzipUtils.extractFile(archive, i, password);
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
                    .lastModified(UnzipUtils.getLastModifiedTime(archive, i))
                    .build();

                // 添加到结果集
                result.put(fileInfo, content);

                // 更新进度
                currentFile++;
                totalBytesRead += content.length;
                if (callback != null) {
                    callback.onProgress(fileInfo.getFileName(), totalBytesRead, data.length, currentFile, fileCount);
                }
            }

            // 通知解压完成
            if (callback != null) {
                callback.onComplete();
            }

            return result;

        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
            throw new UnzipException(UnzipErrorCode.IO_ERROR, "解压失败: " + e.getMessage(), e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.warn("关闭输入流失败: {}", e.getMessage());
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
     * 内存输入流实现
     */
    private static class MemoryInStream implements IInStream {
        private final byte[] data;
        private long position;

        public MemoryInStream(byte[] data) {
            this.data = data;
            this.position = 0;
        }

        @Override
        public long seek(long offset, int seekOrigin) throws SevenZipException {
            switch (seekOrigin) {
                case SEEK_SET:
                    position = offset;
                    break;
                case SEEK_CUR:
                    position += offset;
                    break;
                case SEEK_END:
                    position = data.length + offset;
                    break;
                default:
                    throw new SevenZipException("Invalid seek origin");
            }
            return position;
        }

        @Override
        public int read(byte[] data) throws SevenZipException {
            if (position >= this.data.length) {
                return -1;
            }
            int bytesToRead = Math.min(data.length, (int) (this.data.length - position));
            System.arraycopy(this.data, (int) position, data, 0, bytesToRead);
            position += bytesToRead;
            return bytesToRead;
        }

        @Override
        public void close() throws IOException {
            // 不需要清理资源
        }
    }

    @Override
    public void close() throws IOException {
        // 不需要额外的清理工作
    }
} 