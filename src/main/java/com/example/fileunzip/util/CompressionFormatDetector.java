package com.example.fileunzip.util;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 压缩格式检测工具类
 * 用于自动检测压缩文件的格式
 */
public class CompressionFormatDetector {
    
    /**
     * 压缩格式枚举
     */
    public enum CompressionFormat {
        ZIP("zip"),
        RAR("rar"),
        SEVEN_ZIP("7z"),
        TAR("tar"),
        TAR_GZ("tar.gz"),
        TAR_BZ2("tar.bz2"),
        TAR_XZ("tar.xz"),
        GZIP("gz"),
        BZIP2("bz2"),
        XZ("xz"),
        LZMA("lzma"),
        SNAPPY("snappy"),
        LZ4("lz4"),
        UNKNOWN("unknown");
        
        private final String extension;
        
        CompressionFormat(String extension) {
            this.extension = extension;
        }
        
        public String getExtension() {
            return extension;
        }
    }
    
    /**
     * 检测压缩文件格式
     *
     * @param data 压缩文件数据
     * @return 压缩格式
     */
    public static CompressionFormat detect(byte[] data) {
        if (data == null || data.length < 4) {
            return CompressionFormat.UNKNOWN;
        }
        
        // 检查文件魔数
        if (isZip(data)) {
            return CompressionFormat.ZIP;
        }
        if (isRar(data)) {
            return CompressionFormat.RAR;
        }
        if (isSevenZip(data)) {
            return CompressionFormat.SEVEN_ZIP;
        }
        if (isTar(data)) {
            return CompressionFormat.TAR;
        }
        if (isGzip(data)) {
            return CompressionFormat.GZIP;
        }
        if (isBzip2(data)) {
            return CompressionFormat.BZIP2;
        }
        if (isXz(data)) {
            return CompressionFormat.XZ;
        }
        if (isLzma(data)) {
            return CompressionFormat.LZMA;
        }
        if (isSnappy(data)) {
            return CompressionFormat.SNAPPY;
        }
        if (isLz4(data)) {
            return CompressionFormat.LZ4;
        }
        
        return CompressionFormat.UNKNOWN;
    }
    
    /**
     * 创建解压输入流
     *
     * @param data 压缩文件数据
     * @param format 压缩格式
     * @return 解压输入流
     * @throws IOException IO异常
     */
    public static InputStream createDecompressor(byte[] data, CompressionFormat format) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ArchiveStreamFactory archiveFactory = new ArchiveStreamFactory();
        CompressorStreamFactory compressorFactory = new CompressorStreamFactory();
        
        try {
            switch (format) {
                case ZIP:
                    return archiveFactory.createArchiveInputStream(ArchiveStreamFactory.ZIP, bis);
                case TAR:
                    return archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, bis);
                case TAR_GZ:
                    return archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR,
                        compressorFactory.createCompressorInputStream(CompressorStreamFactory.GZIP, bis));
                case TAR_BZ2:
                    return archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR,
                        compressorFactory.createCompressorInputStream(CompressorStreamFactory.BZIP2, bis));
                case TAR_XZ:
                    return archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR,
                        compressorFactory.createCompressorInputStream(CompressorStreamFactory.XZ, bis));
                case GZIP:
                    return new GzipCompressorInputStream(bis);
                case BZIP2:
                    return new BZip2CompressorInputStream(bis);
                case XZ:
                    return new XZCompressorInputStream(bis);
                case LZMA:
                    return new LZMACompressorInputStream(bis);
                case SNAPPY:
                    return new SnappyCompressorInputStream(bis);
                case LZ4:
                    return new BlockLZ4CompressorInputStream(bis);
                default:
                    throw new IllegalArgumentException("不支持的压缩格式: " + format);
            }
        } catch (ArchiveException e) {
            throw new IOException("创建归档输入流失败", e);
        } catch (CompressorException e) {
            throw new IOException("创建压缩输入流失败", e);
        }
    }
    
    /**
     * 检查是否为ZIP格式
     */
    private static boolean isZip(byte[] data) {
        return data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x03 && data[3] == 0x04;
    }
    
    /**
     * 检查是否为RAR格式
     */
    private static boolean isRar(byte[] data) {
        return data[0] == 0x52 && data[1] == 0x61 && data[2] == 0x72 && data[3] == 0x21;
    }
    
    /**
     * 检查是否为7Z格式
     */
    private static boolean isSevenZip(byte[] data) {
        return data[0] == 0x37 && data[1] == 0x7A && data[2] == 0xBC && data[3] == 0xAF;
    }
    
    /**
     * 检查是否为TAR格式
     */
    private static boolean isTar(byte[] data) {
        // TAR文件头以文件名开始，以null字节结束
        return data[99] == 0 && data[100] == 0 && data[101] == 0;
    }
    
    /**
     * 检查是否为GZIP格式
     */
    private static boolean isGzip(byte[] data) {
        return data[0] == 0x1F && data[1] == (byte)0x8B;
    }
    
    /**
     * 检查是否为BZIP2格式
     */
    private static boolean isBzip2(byte[] data) {
        return data[0] == 0x42 && data[1] == 0x5A && data[2] == 0x68;
    }
    
    /**
     * 检查是否为XZ格式
     */
    private static boolean isXz(byte[] data) {
        return data[0] == (byte)0xFD && data[1] == 0x37 && data[2] == 0x7A && data[3] == 0x58;
    }
    
    /**
     * 检查是否为LZMA格式
     */
    private static boolean isLzma(byte[] data) {
        return data[0] == 0x5D && data[1] == 0x00 && data[2] == 0x00;
    }
    
    private static boolean isSnappy(byte[] data) {
        return data.length >= 4 && data[0] == (byte) 0x82 && data[1] == 'S' && data[2] == 'N' && data[3] == 'A';
    }
    
    private static boolean isLz4(byte[] data) {
        return data.length >= 4 && data[0] == 0x04 && data[1] == 0x22 && data[2] == 0x4D && data[3] == 0x18;
    }
} 