package com.example.fileunzip.detector;

import com.example.fileunzip.exception.UnzipException;
import com.example.fileunzip.model.CompressionFormat;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 压缩文件格式检测器
 * 提供多种检测方法来识别压缩文件的格式，包括：
 * 1. 文件扩展名检测
 * 2. 文件魔数检测
 * 3. 文件内容特征检测
 */
@Slf4j
public class CompressionFormatDetector {
    
    /**
     * 检测压缩文件格式
     *
     * @param file 压缩文件
     * @return 压缩格式枚举
     * @throws UnzipException 当检测失败时抛出异常
     */
    public CompressionFormat detect(File file) throws UnzipException {
        if (file == null || !file.exists()) {
            throw new UnzipException("文件不存在");
        }
        
        // 首先尝试通过扩展名检测
        CompressionFormat format = detectByExtension(file);
        if (format != CompressionFormat.UNKNOWN) {
            return format;
        }
        
        // 然后尝试通过魔数检测
        format = detectByMagicNumber(file);
        if (format != CompressionFormat.UNKNOWN) {
            return format;
        }
        
        // 最后尝试通过内容特征检测
        return detectByContent(file);
    }
    
    /**
     * 通过文件扩展名检测压缩格式
     *
     * @param file 压缩文件
     * @return 压缩格式枚举，如果无法通过扩展名确定则返回UNKNOWN
     */
    private CompressionFormat detectByExtension(File file) {
        String fileName = file.getName().toLowerCase();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = fileName.substring(dotIndex + 1);
            return CompressionFormat.fromExtension(extension);
        }
        return CompressionFormat.UNKNOWN;
    }
    
    /**
     * 通过文件魔数检测压缩格式
     *
     * @param file 压缩文件
     * @return 压缩格式枚举，如果无法通过魔数确定则返回UNKNOWN
     * @throws UnzipException 当读取文件失败时抛出异常
     */
    private CompressionFormat detectByMagicNumber(File file) throws UnzipException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] header = new byte[8];
            int bytesRead = raf.read(header);
            
            if (bytesRead < 4) {
                return CompressionFormat.UNKNOWN;
            }
            
            // ZIP文件魔数：PK\x03\x04
            if (header[0] == 0x50 && header[1] == 0x4B && 
                header[2] == 0x03 && header[3] == 0x04) {
                return CompressionFormat.ZIP;
            }
            
            // RAR文件魔数：Rar!\x1A\x07\x00
            if (header[0] == 0x52 && header[1] == 0x61 && 
                header[2] == 0x72 && header[3] == 0x21) {
                return CompressionFormat.RAR;
            }
            
            // 7Z文件魔数：7z\xBC\xAF\x27\x1C
            if (header[0] == 0x37 && header[1] == 0x7A && 
                header[2] == (byte)0xBC && header[3] == (byte)0xAF) {
                return CompressionFormat.SEVEN_ZIP;
            }
            
            // TAR文件魔数：ustar
            if (bytesRead >= 8 && header[257] == 0x75 && 
                header[258] == 0x73 && header[259] == 0x74 && 
                header[260] == 0x61 && header[261] == 0x72) {
                return CompressionFormat.TAR;
            }
            
        } catch (IOException e) {
            log.error("读取文件魔数失败", e);
            throw new UnzipException("读取文件魔数失败", e);
        }
        
        return CompressionFormat.UNKNOWN;
    }
    
    /**
     * 通过文件内容特征检测压缩格式
     *
     * @param file 压缩文件
     * @return 压缩格式枚举，如果无法通过内容特征确定则返回UNKNOWN
     * @throws UnzipException 当读取文件失败时抛出异常
     */
    private CompressionFormat detectByContent(File file) throws UnzipException {
        // 由于魔数检测已经足够准确，这里暂时返回UNKNOWN
        // 如果将来需要更复杂的检测，可以在这里实现
        return CompressionFormat.UNKNOWN;
    }
    
    /**
     * 检查文件是否为ZIP格式
     *
     * @param file 压缩文件
     * @return 如果是ZIP格式返回true，否则返回false
     * @throws UnzipException 当检查失败时抛出异常
     */
    private boolean isZipFile(File file) throws UnzipException {
        return detectByMagicNumber(file) == CompressionFormat.ZIP;
    }
    
    /**
     * 检查文件是否为RAR格式
     *
     * @param file 压缩文件
     * @return 如果是RAR格式返回true，否则返回false
     * @throws UnzipException 当检查失败时抛出异常
     */
    private boolean isRarFile(File file) throws UnzipException {
        return detectByMagicNumber(file) == CompressionFormat.RAR;
    }
    
    /**
     * 检查文件是否为7Z格式
     *
     * @param file 压缩文件
     * @return 如果是7Z格式返回true，否则返回false
     * @throws UnzipException 当检查失败时抛出异常
     */
    private boolean is7zFile(File file) throws UnzipException {
        return detectByMagicNumber(file) == CompressionFormat.SEVEN_ZIP;
    }
    
    /**
     * 检查文件是否为TAR格式
     *
     * @param file 压缩文件
     * @return 如果是TAR格式返回true，否则返回false
     * @throws UnzipException 当检查失败时抛出异常
     */
    private boolean isTarFile(File file) throws UnzipException {
        return detectByMagicNumber(file) == CompressionFormat.TAR;
    }
} 