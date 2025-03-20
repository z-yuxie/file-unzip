package com.example.fileunzip.exception;

/**
 * 解压错误码枚举
 */
public enum UnzipErrorCode {
    INVALID_FORMAT("无效的压缩格式"),
    PASSWORD_REQUIRED("需要密码"),
    PASSWORD_INCORRECT("密码错误"),
    FILE_TOO_LARGE("文件过大"),
    UNSUPPORTED_FORMAT("不支持的压缩格式"),
    IO_ERROR("IO错误"),
    SECURITY_ERROR("安全错误"),
    MEMORY_LIMIT_EXCEEDED("超出内存限制"),
    FILE_COUNT_LIMIT_EXCEEDED("超出文件数量限制"),
    PATH_TRAVERSAL_DETECTED("检测到路径遍历攻击");

    private final String message;

    UnzipErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
} 