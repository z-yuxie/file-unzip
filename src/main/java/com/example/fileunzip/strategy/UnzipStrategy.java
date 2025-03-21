package com.example.fileunzip.strategy;

import com.example.fileunzip.callback.UnzipProgressCallback;
import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.format.CompressionFormat;
import com.example.fileunzip.format.CompressionFormatDetector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 解压策略接口
 */
public interface UnzipStrategy {
    /**
     * 解压压缩文件
     *
     * @param inputStream 压缩文件输入流
     * @return 解压后的文件信息与内容的映射
     * @throws UnzipException 解压过程中可能发生的异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException;

    /**
     * 解压加密的压缩文件
     *
     * @param inputStream 压缩文件输入流
     * @param password 密码
     * @return 解压后的文件信息与内容的映射
     * @throws UnzipException 解压过程中可能发生的异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException;

    /**
     * 带进度回调的解压
     *
     * @param inputStream 压缩文件输入流
     * @param callback 进度回调
     * @return 解压后的文件信息与内容的映射
     * @throws UnzipException 解压过程中可能发生的异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException;

    /**
     * 带密码和进度回调的解压
     *
     * @param inputStream 压缩文件输入流
     * @param password 密码
     * @param callback 进度回调
     * @return 解压后的文件信息与内容的映射
     * @throws UnzipException 解压过程中可能发生的异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException;

    /**
     * 获取策略支持的压缩格式
     *
     * @return 支持的压缩格式列表
     */
    CompressionFormat[] getSupportedFormats();

    /**
     * 关闭策略，释放资源
     *
     * @throws IOException 关闭过程中可能发生的IO异常
     */
    void close() throws IOException;
} 