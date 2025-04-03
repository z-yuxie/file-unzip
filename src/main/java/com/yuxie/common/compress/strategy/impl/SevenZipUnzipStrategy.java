package com.yuxie.common.compress.strategy.impl;

import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.format.CompressionFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * 7Z格式解压策略实现
 * <p>
 * 该类继承自 {@link AbstractSevenZipStrategy}，专门用于处理7Z格式的压缩文件解压。
 * 使用7-Zip-JBinding库进行解压，支持多种压缩算法和加密方式。
 * </p>
 * <p>
 * 主要特点：
 * <ul>
 *   <li>支持多种压缩算法：LZMA、LZMA2、PPMd、BZip2等</li>
 *   <li>支持加密7Z：可以处理带密码的7Z文件</li>
 *   <li>支持分卷7Z：可以处理分卷压缩的7Z文件</li>
 *   <li>支持进度回调：可以报告解压进度</li>
 *   <li>资源自动管理：自动关闭输入流和输出流</li>
 * </ul>
 * </p>
 * <p>
 * 注意：使用此策略需要确保系统中已安装7-Zip，并且Java程序能够访问到7-Zip的DLL文件。
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see AbstractSevenZipStrategy
 * @see UnzipConfig
 * @see net.sf.sevenzipjbinding
 */
@Slf4j
public class SevenZipUnzipStrategy extends AbstractSevenZipStrategy {
    
    /**
     * 构造函数
     * <p>
     * 初始化解压策略，设置解压配置。
     * </p>
     *
     * @param unzipConfig 解压配置，不能为null
     * @throws IllegalArgumentException 当unzipConfig为null时抛出
     */
    public SevenZipUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }
    
    /**
     * 检查是否支持指定的压缩格式
     * <p>
     * 判断当前策略是否支持解压指定格式的压缩文件。
     * </p>
     *
     * @param format 要检查的压缩格式
     * @return 如果是7Z格式则返回true，否则返回false
     */
    @Override
    public boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.SEVEN_ZIP;
    }
    
    /**
     * 获取支持的压缩格式列表
     * <p>
     * 返回当前策略支持的所有压缩格式。
     * </p>
     *
     * @return 包含7Z格式的数组
     */
    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.SEVEN_ZIP};
    }
} 