package com.yuxie.common.compress.exception;

import com.yuxie.common.compress.format.CompressionFormat;
import lombok.Getter;

/**
 * 压缩操作异常
 */
@Getter
public class CompressionException extends RuntimeException {
    private final CompressionFormat format;
    private final String operation;
    
    public CompressionException(CompressionFormat format, String operation, String message) {
        super(String.format("%s格式%s操作失败: %s", format, operation, message));
        this.format = format;
        this.operation = operation;
    }
    
    public CompressionException(CompressionFormat format, String operation, Throwable cause) {
        super(String.format("%s格式%s操作失败: %s", format, operation, cause.getMessage()), cause);
        this.format = format;
        this.operation = operation;
    }
    
    public CompressionException(CompressionFormat format, String operation, String message, Throwable cause) {
        super(String.format("%s格式%s操作失败: %s", format, operation, message), cause);
        this.format = format;
        this.operation = operation;
    }
} 