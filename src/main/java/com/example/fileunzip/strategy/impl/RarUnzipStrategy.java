package com.example.fileunzip.strategy.impl;

import com.example.fileunzip.config.UnzipConfig;
import com.example.fileunzip.format.CompressionFormat;
import com.example.fileunzip.format.CompressionFormatDetector;
import net.sf.sevenzipjbinding.*;

/**
 * RAR格式解压策略实现类
 * 使用7-Zip-JBinding库实现RAR格式的解压功能
 * 支持标准RAR压缩格式文件
 * 
 * 实现说明：
 * 1. 使用静态初始化块加载7-Zip-JBinding库
 * 2. 通过自动检测压缩格式的方式处理字节数组输入
 * 3. 使用RandomAccessFile和IInArchive接口进行文件解压
 * 4. 支持提取文件元数据（路径、大小等）
 * 5. 自动清理临时文件
 */
public class RarUnzipStrategy extends AbstractArchiveUnzipStrategy {
    
    /**
     * 构造函数
     *
     * @param unzipConfig 解压配置
     */
    public RarUnzipStrategy(UnzipConfig unzipConfig) {
        super(unzipConfig);
    }

    @Override
    protected boolean isSupportedFormat(CompressionFormat format) {
        return format == CompressionFormat.RAR;
    }

    @Override
    protected String getTempFileExtension() {
        return ".rar";
    }

    @Override
    protected IInArchive openArchive(IInStream inStream) throws SevenZipException {
        return SevenZip.openInArchive(ArchiveFormat.RAR, inStream);
    }

    @Override
    public CompressionFormat[] getSupportedFormats() {
        return new CompressionFormat[]{CompressionFormat.RAR};
    }
} 