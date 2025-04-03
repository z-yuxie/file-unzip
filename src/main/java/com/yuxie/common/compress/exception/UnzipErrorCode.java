package com.yuxie.common.compress.exception;

import lombok.Getter;

/**
 * 解压错误码枚举
 * <p>
 * 定义了在解压过程中可能遇到的各种错误类型。
 * 每个错误码都包含了一个唯一的标识符和详细的描述信息。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * try {
 *     // 执行解压操作
 * } catch (UnzipException e) {
 *     if (e.getErrorCode() == UnzipErrorCode.PASSWORD_REQUIRED) {
 *         // 处理需要密码的情况
 *     }
 * }
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipException
 */
@Getter
public enum UnzipErrorCode {
    /** 
     * IO 操作错误
     * 在文件读写过程中发生的错误，如文件不存在、权限不足等
     */
    IO_ERROR("IO_ERROR"),
    
    /** 
     * 安全检查错误
     * 在安全验证过程中发生的错误，如文件类型检查失败、病毒扫描失败等
     */
    SECURITY_ERROR("SECURITY_ERROR"),
    
    /** 
     * 文件大小超出限制
     * 当文件大小超过配置的最大限制时，用于防止内存溢出
     */
    FILE_TOO_LARGE("FILE_TOO_LARGE"),
    
    /** 
     * 内存不足错误
     * 当系统内存不足时，无法完成解压操作
     */
    MEMORY_ERROR("MEMORY_ERROR"),
    
    /** 
     * 压缩格式无效
     * 当无法识别或解析压缩文件格式时，如文件损坏或格式错误
     */
    INVALID_FORMAT("INVALID_FORMAT"),
    
    /** 
     * 需要密码
     * 当压缩文件需要密码但未提供时，提示用户输入密码
     */
    PASSWORD_REQUIRED("PASSWORD_REQUIRED"),
    
    /** 
     * 密码错误
     * 当提供的密码不正确时，需要用户重新输入正确的密码
     */
    PASSWORD_INCORRECT("PASSWORD_INCORRECT"),
    
    /** 
     * 不支持的压缩格式
     * 当遇到系统不支持的压缩格式时，如特殊的加密算法或压缩方式
     */
    UNSUPPORTED_FORMAT("UNSUPPORTED_FORMAT"),
    
    /** 
     * 超出文件数量限制
     * 当压缩包中的文件数量超过配置的限制时，用于防止解压过多文件
     */
    FILE_COUNT_LIMIT_EXCEEDED("FILE_COUNT_LIMIT_EXCEEDED"),
    
    /** 
     * 检测到路径遍历攻击
     * 当检测到压缩包中包含不安全的路径时，如 "../" 等试图访问上级目录的路径
     */
    PATH_TRAVERSAL_DETECTED("PATH_TRAVERSAL_DETECTED"),
    
    /** 
     * 解压过程错误
     * 在解压过程中发生的其他错误，如解压算法错误、数据损坏等
     */
    UNZIP_ERROR("UNZIP_ERROR"),
    
    /** 
     * 未知错误
     * 当发生未预期的错误时，用于捕获所有未明确分类的错误情况
     */
    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    
    /** 
     * 解压超时
     * 当解压操作超过配置的时间限制时，用于防止长时间运行的操作
     */
    TIMEOUT_ERROR("TIMEOUT_ERROR"),
    
    /** 
     * 解压被中断
     * 当解压操作被外部中断时，如用户取消操作或系统关闭
     */
    INTERRUPTED_ERROR("INTERRUPTED_ERROR");

    /**
     * 错误码的字符串表示
     * 用于在日志记录和错误报告中标识具体的错误类型
     */
    private final String code;
    
    /**
     * 构造函数
     *
     * @param code 错误码的字符串表示
     */
    UnzipErrorCode(String code) {
        this.code = code;
    }
} 