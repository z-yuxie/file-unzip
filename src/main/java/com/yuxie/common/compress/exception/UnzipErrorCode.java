package com.yuxie.common.compress.exception;

import lombok.Getter;

/**
 * 解压错误码枚举
 */
@Getter
public enum UnzipErrorCode {
    /** 
     * IO 操作错误
     * 在文件读写过程中发生的错误
     */
    IO_ERROR("IO_ERROR"),
    
    /** 
     * 安全检查错误
     * 在安全验证过程中发生的错误
     */
    SECURITY_ERROR("SECURITY_ERROR"),
    
    /** 
     * 文件大小超出限制
     * 当文件大小超过配置的最大限制时
     */
    FILE_TOO_LARGE("FILE_TOO_LARGE"),
    
    /** 
     * 内存不足错误
     * 当系统内存不足时
     */
    MEMORY_ERROR("MEMORY_ERROR"),
    
    /** 
     * 压缩格式无效
     * 当无法识别或解析压缩文件格式时
     */
    INVALID_FORMAT("INVALID_FORMAT"),
    
    /** 
     * 需要密码
     * 当压缩文件需要密码但未提供时
     */
    PASSWORD_REQUIRED("PASSWORD_REQUIRED"),
    
    /** 
     * 密码错误
     * 当提供的密码不正确时
     */
    PASSWORD_INCORRECT("PASSWORD_INCORRECT"),
    
    /** 
     * 不支持的压缩格式
     * 当遇到系统不支持的压缩格式时
     */
    UNSUPPORTED_FORMAT("UNSUPPORTED_FORMAT"),
    
    /** 
     * 超出文件数量限制
     * 当压缩包中的文件数量超过配置的限制时
     */
    FILE_COUNT_LIMIT_EXCEEDED("FILE_COUNT_LIMIT_EXCEEDED"),
    
    /** 
     * 检测到路径遍历攻击
     * 当检测到压缩包中包含不安全的路径时
     */
    PATH_TRAVERSAL_DETECTED("PATH_TRAVERSAL_DETECTED"),
    
    /** 
     * 解压过程错误
     * 在解压过程中发生的其他错误
     */
    UNZIP_ERROR("UNZIP_ERROR"),
    
    /** 
     * 未知错误
     * 当发生未预期的错误时
     */
    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    
    /** 
     * 解压超时
     * 当解压操作超过配置的时间限制时
     */
    TIMEOUT_ERROR("TIMEOUT_ERROR"),
    
    /** 
     * 解压被中断
     * 当解压操作被外部中断时
     */
    INTERRUPTED_ERROR("INTERRUPTED_ERROR");

    /**
     * -- GETTER --
     *  获取错误码
     */
    private final String code;
    
    UnzipErrorCode(String code) {
        this.code = code;
    }

} 