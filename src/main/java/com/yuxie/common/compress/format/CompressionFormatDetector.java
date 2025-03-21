package com.yuxie.common.compress.format;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 压缩格式检测器
 * 用于自动检测压缩文件的格式
 */
public class CompressionFormatDetector {
    
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
        
        // 检查复合格式
        if (isGzippedTar(data)) {
            return CompressionFormat.TAR_GZ;
        }
        if (isBzip2Tar(data)) {
            return CompressionFormat.TAR_BZ2;
        }
        if (isXzTar(data)) {
            return CompressionFormat.TAR_XZ;
        }
        
        // 检查单一格式
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
     * 7Z文件魔数：7z\xBC\xAF\x27\x1C
     */
    private static boolean isSevenZip(byte[] data) {
        return data.length >= 6 &&
               data[0] == 0x37 && // '7'
               data[1] == 0x7A && // 'z'
               data[2] == (byte)0xBC && 
               data[3] == (byte)0xAF &&
               data[4] == 0x27 &&
               data[5] == 0x1C;
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
    
    /**
     * 检查是否为GZIP压缩的TAR文件
     */
    private static boolean isGzippedTar(byte[] data) {
        // GZIP 魔数: 1f 8b
        if (data[0] != 0x1F || data[1] != (byte)0x8B) {
            return false;
        }
        
        // 尝试解压前512字节检查是否为TAR格式
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new ByteArrayInputStream(data))) {
            byte[] tarHeader = new byte[512];
            int read = gzipIn.read(tarHeader);
            return read == 512 && isTar(tarHeader);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 检查是否为BZIP2压缩的TAR文件
     */
    private static boolean isBzip2Tar(byte[] data) {
        // BZip2 魔数: 42 5a 68 ('BZh')
        if (data[0] != 0x42 || data[1] != 0x5A || data[2] != 0x68) {
            return false;
        }
        
        try (BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(new ByteArrayInputStream(data))) {
            byte[] tarHeader = new byte[512];
            int read = bzipIn.read(tarHeader);
            return read == 512 && isTar(tarHeader);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 检查是否为XZ压缩的TAR文件
     */
    private static boolean isXzTar(byte[] data) {
        // XZ 魔数: fd 37 7a 58 5a 00
        if (data[0] != (byte)0xFD || data[1] != 0x37 || data[2] != 0x7A || 
            data[3] != 0x58 || data[4] != 0x5A || data[5] != 0x00) {
            return false;
        }
        
        try (XZCompressorInputStream xzIn = new XZCompressorInputStream(new ByteArrayInputStream(data))) {
            byte[] tarHeader = new byte[512];
            int read = xzIn.read(tarHeader);
            return read == 512 && isTar(tarHeader);
        } catch (IOException e) {
            return false;
        }
    }
} 