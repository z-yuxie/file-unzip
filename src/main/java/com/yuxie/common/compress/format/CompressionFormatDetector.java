package com.yuxie.common.compress.format;

import com.yuxie.common.compress.exception.UnzipException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 压缩格式检测器
 * 通过文件头魔数检测压缩格式
 */
public class CompressionFormatDetector {
    
    private static final byte[] ZIP_MAGIC = {(byte)0x50, (byte)0x4B, 0x03, 0x04};
    private static final byte[] RAR_MAGIC = {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07};
    private static final byte[] SEVEN_ZIP_MAGIC = {(byte)0x37, 0x7A, (byte)0xBC, (byte)0xAF, 0x27, 0x1C};
    private static final byte[] TAR_MAGIC = {0x75, 0x73, 0x74, 0x61, 0x72};
    private static final byte[] GZIP_MAGIC = {0x1F, (byte)0x8B};
    private static final byte[] BZIP2_MAGIC = {0x42, 0x5A, 0x68};
    private static final byte[] XZ_MAGIC = {(byte)0xFD, 0x37, 0x7A, 0x58, 0x5A, 0x00};
    private static final byte[] LZMA_MAGIC = {0x5D, 0x00, 0x00, (byte)0x80, 0x00};
    private static final byte[] SNAPPY_MAGIC = {0x28, (byte)0xB5, 0x2F, (byte)0xFD};
    private static final byte[] LZ4_MAGIC = {0x04, 0x22, 0x4D, 0x18};
    
    /**
     * 检测压缩格式
     */
    public static CompressionFormat detectFormat(InputStream inputStream) throws UnzipException {
        if (inputStream == null) {
            throw new UnzipException("输入流不能为空");
        }

        try {
            // 读取文件头
            byte[] header = new byte[8];
            int bytesRead = inputStream.read(header);
            if (bytesRead < 4) {
                throw new UnzipException("文件头数据不足");
            }

            // 检测压缩格式
            CompressionFormat format = detectByMagic(header);
            if (format != CompressionFormat.UNKNOWN) {
                return format;
            }

            // 尝试使用Commons Compress的工厂类检测
            format = detectByCommonsCompress(inputStream);
            return format;
        } catch (IOException e) {
            throw new UnzipException("检测压缩格式失败", e);
        }
    }
    
    /**
     * 通过魔数检测压缩格式
     */
    private static CompressionFormat detectByMagic(byte[] header) {
        if (startsWith(header, ZIP_MAGIC)) {
            return CompressionFormat.ZIP;
        } else if (startsWith(header, RAR_MAGIC)) {
            return CompressionFormat.RAR;
        } else if (startsWith(header, SEVEN_ZIP_MAGIC)) {
            return CompressionFormat.SEVEN_ZIP;
        } else if (startsWith(header, TAR_MAGIC)) {
            return CompressionFormat.TAR;
        } else if (startsWith(header, GZIP_MAGIC)) {
            return CompressionFormat.GZIP;
        } else if (startsWith(header, BZIP2_MAGIC)) {
            return CompressionFormat.BZIP2;
        } else if (startsWith(header, XZ_MAGIC)) {
            return CompressionFormat.XZ;
        } else if (startsWith(header, LZMA_MAGIC)) {
            return CompressionFormat.LZMA;
        } else if (startsWith(header, SNAPPY_MAGIC)) {
            return CompressionFormat.SNAPPY;
        } else if (startsWith(header, LZ4_MAGIC)) {
            return CompressionFormat.LZ4;
        }
        return CompressionFormat.UNKNOWN;
    }
    
    /**
     * 通过Commons Compress检测压缩格式
     */
    private static CompressionFormat detectByCommonsCompress(InputStream inputStream) {
        try {
            ArchiveStreamFactory archiveFactory = new ArchiveStreamFactory();
            archiveFactory.createArchiveInputStream(inputStream);
            return CompressionFormat.TAR;
        } catch (Exception ignored) {
            // 不是归档格式
        }

        try {
            CompressorStreamFactory compressorFactory = new CompressorStreamFactory();
            compressorFactory.createCompressorInputStream(inputStream);
            return CompressionFormat.GZIP; // 默认返回GZIP
        } catch (Exception ignored) {
            // 不是压缩格式
        }

        return CompressionFormat.UNKNOWN;
    }
    
    /**
     * 检查字节数组是否以指定前缀开头
     */
    private static boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
} 