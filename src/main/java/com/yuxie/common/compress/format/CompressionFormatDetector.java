package com.yuxie.common.compress.format;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 压缩格式检测器
 * 用于自动检测压缩文件的格式
 */
public class CompressionFormatDetector {
    
    private static final Map<CompressionFormat, FormatDetector> detectors = new HashMap<>();
    
    static {
        // 注册单一格式检测器
        detectors.put(CompressionFormat.ZIP, data -> isZip(data));
        detectors.put(CompressionFormat.RAR, data -> isRar(data));
        detectors.put(CompressionFormat.SEVEN_ZIP, data -> isSevenZip(data));
        detectors.put(CompressionFormat.TAR, data -> isTar(data));
        detectors.put(CompressionFormat.GZIP, data -> isGzip(data));
        detectors.put(CompressionFormat.BZIP2, data -> isBzip2(data));
        detectors.put(CompressionFormat.XZ, data -> isXz(data));
        detectors.put(CompressionFormat.LZMA, data -> isLzma(data));
        detectors.put(CompressionFormat.SNAPPY, data -> isSnappy(data));
        detectors.put(CompressionFormat.LZ4, data -> isLz4(data));
        
        // 注册复合格式检测器
        detectors.put(CompressionFormat.TAR_GZ, data -> isTarGz(data));
        detectors.put(CompressionFormat.TAR_BZ2, data -> isTarBz2(data));
        detectors.put(CompressionFormat.TAR_XZ, data -> isTarXz(data));
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
        
        // 先检查单一格式
        for (Map.Entry<CompressionFormat, FormatDetector> entry : detectors.entrySet()) {
            if (!entry.getKey().isCompound() && entry.getValue().detect(data)) {
                return entry.getKey();
            }
        }
        
        // 再检查复合格式
        for (Map.Entry<CompressionFormat, FormatDetector> entry : detectors.entrySet()) {
            if (entry.getKey().isCompound() && entry.getValue().detect(data)) {
                return entry.getKey();
            }
        }
        
        return CompressionFormat.UNKNOWN;
    }
    
    /**
     * 格式检测器接口
     */
    private interface FormatDetector {
        boolean detect(byte[] data);
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
    
    /**
     * 检查是否为Snappy格式
     */
    private static boolean isSnappy(byte[] data) {
        return data.length >= 4 && data[0] == (byte) 0x82 && data[1] == 'S' && data[2] == 'N' && data[3] == 'A';
    }
    
    /**
     * 检查是否为LZ4格式
     */
    private static boolean isLz4(byte[] data) {
        return data.length >= 4 && data[0] == 0x04 && data[1] == 0x22 && data[2] == 0x4D && data[3] == 0x18;
    }
    
    /**
     * 检查是否为TAR.GZ格式
     */
    private static boolean isTarGz(byte[] data) {
        if (!isGzip(data)) {
            return false;
        }
        
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new ByteArrayInputStream(data))) {
            byte[] tarHeader = new byte[512];
            int read = gzipIn.read(tarHeader);
            return read == 512 && isTar(tarHeader);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 检查是否为TAR.BZ2格式
     */
    private static boolean isTarBz2(byte[] data) {
        if (!isBzip2(data)) {
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
     * 检查是否为TAR.XZ格式
     */
    private static boolean isTarXz(byte[] data) {
        if (!isXz(data)) {
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