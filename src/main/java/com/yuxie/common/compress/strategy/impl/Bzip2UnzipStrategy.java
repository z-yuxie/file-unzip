package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * BZIP2格式解压策略实现
 * <p>
 * 该类继承自 {@link AbstractCompressedUnzipStrategy}，专门用于处理BZIP2格式的压缩文件解压。
 * 使用Apache Commons Compress库进行解压，支持标准的BZIP2格式。
 * </p>
 * <p>
 * 主要特点：
 * <ul>
 *   <li>支持标准BZIP2格式：兼容bzip2 1.0.2规范</li>
 *   <li>支持自动检测：可以自动识别BZIP2格式</li>
 *   <li>支持进度回调：可以报告解压进度</li>
 *   <li>资源自动管理：自动关闭输入流和输出流</li>
 *   <li>支持TAR检测：自动检测解压后的数据是否为TAR格式</li>
 * </ul>
 * </p>
 * <p>
 * 注意：BZIP2格式通常用于压缩单个文件，如果解压后的数据是TAR格式，
 * 会自动切换到TAR解压策略进行处理。
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see AbstractCompressedUnzipStrategy
 * @see UnzipConfig
 * @see org.apache.commons.compress.compressors.bzip2
 */
@Slf4j
public class Bzip2UnzipStrategy extends AbstractCompressedUnzipStrategy {
    
    /**
     * 构造函数
     * <p>
     * 初始化解压策略，设置解压配置和压缩格式。
     * </p>
     *
     * @param unzipConfig 解压配置，不能为null
     * @throws IllegalArgumentException 当unzipConfig为null时抛出
     */
    public Bzip2UnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig, CompressionFormat.BZIP2);
    }
    
    /**
     * 创建压缩输入流
     * <p>
     * 使用Apache Commons Compress库创建BZIP2解压缩输入流。
     * </p>
     *
     * @param inputStream 原始输入流
     * @return BZIP2解压缩输入流
     * @throws IOException 当创建输入流失败时抛出
     */
    @Override
    protected CompressorInputStream createCompressorInputStream(InputStream inputStream) throws IOException {
        return new BZip2CompressorInputStream(inputStream);
    }
} 