package com.example.fileunzip.exception;

/**
 * 解压错误码枚举
 */
public enum UnzipErrorCode {
    /** IO错误 */
    IO_ERROR("IO_ERROR"),
    
    /** 安全检查错误 */
    SECURITY_ERROR("SECURITY_ERROR"),
    
    /** 文件过大 */
    FILE_TOO_LARGE("FILE_TOO_LARGE"),
    
    /** 内存不足 */
    MEMORY_ERROR("MEMORY_ERROR"),
    
    /** 格式无效 */
    INVALID_FORMAT("INVALID_FORMAT"),
    
    /** 需要密码 */
    PASSWORD_REQUIRED("PASSWORD_REQUIRED"),
    
    /** 密码错误 */
    PASSWORD_INCORRECT("PASSWORD_INCORRECT"),
    
    /** 不支持的压缩格式 */
    UNSUPPORTED_FORMAT("UNSUPPORTED_FORMAT"),
    
    /** 超出文件数量限制 */
    FILE_COUNT_LIMIT_EXCEEDED("FILE_COUNT_LIMIT_EXCEEDED"),
    
    /** 检测到路径遍历攻击 */
    PATH_TRAVERSAL_DETECTED("PATH_TRAVERSAL_DETECTED"),
    
    /** 解压错误 */
    UNZIP_ERROR("UNZIP_ERROR"),
    
    /** 未知错误 */
    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    
    /** 解压超时 */
    TIMEOUT_ERROR("TIMEOUT_ERROR"),
    
    /** 解压被中断 */
    INTERRUPTED_ERROR("INTERRUPTED_ERROR");
    
    private final String code;
    
    UnzipErrorCode(String code) {
        this.code = code;
    }
    
    /**
     * 获取错误码
     *
     * @return 错误码字符串
     */
    public String getCode() {
        return code;
    }
} 