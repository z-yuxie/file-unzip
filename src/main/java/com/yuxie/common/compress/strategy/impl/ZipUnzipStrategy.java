package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.nio.charset.Charset;

/**
 * ZIP格式解压策略实现
 * <p>
 * 该类继承自 {@link AbstractCommonsCompressStrategy}，专门用于处理ZIP格式的压缩文件解压。
 * 使用Apache Commons Compress库的 {@link ArchiveStreamFactory} 创建ZIP格式的归档输入流。
 * </p>
 * <p>
 * 主要特点：
 * <ul>
 *   <li>支持标准ZIP格式：兼容大多数ZIP压缩文件</li>
 *   <li>支持加密ZIP：可以处理带密码的ZIP文件</li>
 *   <li>支持进度回调：可以报告解压进度</li>
 *   <li>资源自动管理：自动关闭输入流和输出流</li>
 * </ul>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see AbstractCommonsCompressStrategy
 * @see UnzipConfig
 * @see ArchiveStreamFactory
 */
public class ZipUnzipStrategy extends AbstractCommonsCompressStrategy {
    
    /**
     * 构造函数
     * <p>
     * 初始化解压策略，设置解压配置。
     * </p>
     *
     * @param unzipConfig 解压配置，不能为null
     * @throws IllegalArgumentException 当unzipConfig为null时抛出
     */
    public ZipUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    /**
     * 检查是否支持指定的压缩格式
     * <p>
     * 判断当前策略是否支持解压指定格式的压缩文件。
     * </p>
     *
     * @param format 要检查的压缩格式
     * @return 如果是ZIP格式则返回true，否则返回false
     */
    @Override
    public boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.ZIP;
    }
    
    /**
     * 获取支持的压缩格式列表
     * <p>
     * 返回当前策略支持的所有压缩格式。
     * </p>
     *
     * @return 包含ZIP格式的数组
     */
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.ZIP};
    }
    
    /**
     * 创建归档输入流
     * <p>
     * 使用Apache Commons Compress库创建ZIP格式的归档输入流。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @return ZIP格式的归档输入流
     * @throws Exception 当创建输入流失败时抛出
     */
    @Override
    protected ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws Exception {
        // 定义可能的编码列表，按优先级排序
        String[] encodings = new String[]{
            "UTF-8",      // 现代ZIP文件的标准编码
            "GBK",        // 中文Windows系统
            "CP437",      // ZIP默认编码，英文Windows系统
            "GB2312",     // 较旧的中文系统
            "BIG5",       // 繁体中文系统
            "SHIFT-JIS",  // 日文系统
            "EUC-KR",     // 韩文系统
            "ISO-8859-1", // 西欧语言
            "ISO-8859-2", // 中欧语言
            "ISO-8859-5"  // 西里尔字母
        };
        
        // 确保输入流支持mark/reset
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        
        // 尝试不同的编码
        for (String encoding : encodings) {
            try {
                bufferedInputStream.mark(8192); // 标记流的开始位置
                ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(bufferedInputStream, encoding, true, true);
                
                // 检查第一个条目的文件名是否正常
                ZipArchiveEntry entry = zipInputStream.getNextZipEntry();
                if (entry != null) {
                    String entryName = entry.getName();
                    if (!isGarbled(entryName)) {
                        // 如果文件名正常，说明编码正确
                        return zipInputStream;
                    }
                }
                
                // 如果出现乱码，重置流并尝试下一个编码
                bufferedInputStream.reset();
            } catch (Exception e) {
                // 如果当前编码失败，重置流并继续尝试下一个
                bufferedInputStream.reset();
                continue;
            }
        }
        
        // 如果所有编码都失败，默认使用GBK（因为中文Windows最常见）
        return new ZipArchiveInputStream(bufferedInputStream, "GBK", true, true);
    }
    
    /**
     * 检查字符串是否出现乱码
     * <p>
     * 通过检查字符串中是否包含无法解码的字符来判断是否出现乱码。
     * 乱码通常表现为：
     * 1. 包含替换字符或无效的Unicode字符
     * 2. 包含不完整的UTF-8序列
     * 3. 包含连续的问号字符（通常表示编码转换失败）
     * </p>
     *
     * @param str 要检查的字符串
     * @return 如果出现乱码则返回true，否则返回false
     */
    private boolean isGarbled(String str) {
        // 空值检查
        if (str == null) {
            return true;  // null值视为异常情况
        }
        if (str.isEmpty()) {
            return false; // 空字符串不是乱码
        }
        
        // 检查是否包含替换字符或无效的Unicode字符
        if (str.matches(".*[\uFFFD\uFFFE\uFFFF].*")) {
            return true;
        }
        
        // 检查是否包含不完整的UTF-8序列
        if (str.matches(".*[\\uD800-\\uDBFF](?!\\uDC00-\\uDFFF).*")) {
            return true;
        }
        if (str.matches(".*(?<![\\uD800-\\uDBFF])[\\uDC00-\\uDFFF].*")) {
            return true;
        }
        
        // 检查是否包含连续的问号字符（通常表示编码转换失败）
        if (str.matches(".*\\?{2,}.*")) {
            return true;
        }
        
        return false;
    }
} 