package com.yuxie.common.compress.model;

import lombok.Data;
import lombok.Builder;

/**
 * 文件信息类，用于存储解压后文件的元数据信息
 */
@Data
@Builder
public class FileInfo {
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private long size;
    
    /**
     * 最后修改时间（毫秒时间戳）
     */
    private long lastModified;
    
    /**
     * 文件在压缩包中的相对路径
     */
    private String path;
} 