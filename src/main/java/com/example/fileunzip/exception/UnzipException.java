package com.example.fileunzip.exception;

/**
 * 解压异常类
 */
public class UnzipException extends Exception {
    private final UnzipErrorCode errorCode;
    private final String details;

    public UnzipException(UnzipErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public UnzipException(UnzipErrorCode errorCode, String details) {
        super(errorCode.getMessage() + ": " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    public UnzipException(UnzipErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public UnzipException(UnzipErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + ": " + details, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public UnzipErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
} 