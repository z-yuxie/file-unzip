package com.example.fileunzip.exception;

/**
 * 解压过程中的异常类
 */
public class UnzipException extends RuntimeException {
    
    public UnzipException(String message) {
        super(message);
    }
    
    public UnzipException(String message, Throwable cause) {
        super(message, cause);
    }
} 