package com.example.fileunzip.strategy;

import com.example.fileunzip.model.FileInfo;
import com.example.fileunzip.util.CompressionFormatDetector;
import java.io.IOException;
import java.util.Map;

/**
 * 解压策略接口，用于实现不同压缩格式的解压功能
 */
public interface UnzipStrategy {
    /**
     * 解压压缩文件
     *
     * @param data 压缩文件的字节数组
     * @return 解压后的文件信息与内容的映射
     * @throws IOException 解压过程中可能发生的IO异常
     */
    Map<FileInfo, byte[]> unzip(byte[] data) throws IOException;

    /**
     * 获取策略支持的压缩格式
     *
     * @return 支持的压缩格式列表
     */
    CompressionFormatDetector.CompressionFormat[] getSupportedFormats();
} 