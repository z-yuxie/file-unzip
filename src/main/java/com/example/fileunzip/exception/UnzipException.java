package com.example.fileunzip.exception;

/**
 * 解压异常类
 * 用于表示解压过程中发生的异常
 */
public class UnzipException extends Exception {
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * 构造一个新的解压异常
     *
     * @param message 错误信息
     */
    public UnzipException(String message) {
        super(message);
        this.errorCode = "UNZIP_ERROR";
    }
    
    /**
     * 构造一个新的解压异常
     *
     * @param message 错误信息
     * @param cause 导致此异常的原始异常
     */
    public UnzipException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNZIP_ERROR";
    }
    
    /**
     * 构造一个新的解压异常
     *
     * @param message 错误信息
     * @param errorCode 错误码
     */
    public UnzipException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造一个新的解压异常
     *
     * @param message 错误信息
     * @param cause 导致此异常的原始异常
     * @param errorCode 错误码
     */
    public UnzipException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造一个新的解压异常
     *
     * @param errorCode 错误码枚举
     * @param message 错误信息
     */
    public UnzipException(UnzipErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode();
    }
    
    /**
     * 构造一个新的解压异常
     *
     * @param errorCode 错误码枚举
     * @param message 错误信息
     * @param cause 导致此异常的原始异常
     */
    public UnzipException(UnzipErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }
    
    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
} 