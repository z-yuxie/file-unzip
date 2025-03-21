package com.example.fileunzip.model;

/**
 * 压缩文件格式枚举
 * 定义了系统支持的压缩文件格式
 */
public enum CompressionFormat {
    /** ZIP格式 */
    ZIP,
    
    /** RAR格式 */
    RAR,
    
    /** 7Z格式 */
    SEVEN_ZIP,
    
    /** TAR格式 */
    TAR,
    
    /** 未知格式 */
    UNKNOWN;
    
    /**
     * 获取格式的默认文件扩展名
     *
     * @return 文件扩展名（不含点号）
     */
    public String getDefaultExtension() {
        switch (this) {
            case ZIP:
                return "zip";
            case RAR:
                return "rar";
            case SEVEN_ZIP:
                return "7z";
            case TAR:
                return "tar";
            default:
                return "";
        }
    }
    
    /**
     * 根据文件扩展名获取压缩格式
     *
     * @param extension 文件扩展名（不含点号）
     * @return 压缩格式枚举
     */
    public static CompressionFormat fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return UNKNOWN;
        }
        
        String ext = extension.toLowerCase();
        switch (ext) {
            case "zip":
                return ZIP;
            case "rar":
                return RAR;
            case "7z":
                return SEVEN_ZIP;
            case "tar":
                return TAR;
            default:
                return UNKNOWN;
        }
    }
} 