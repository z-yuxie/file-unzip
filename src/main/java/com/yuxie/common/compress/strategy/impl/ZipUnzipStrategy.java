package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.InputStream;

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
        return new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
    }
} 