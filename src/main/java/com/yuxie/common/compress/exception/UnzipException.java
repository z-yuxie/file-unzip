package com.yuxie.common.compress.exception;

import lombok.Getter;

/**
 * 解压异常类
 * <p>
 * 表示在解压过程中发生的异常。
 * 此异常包含了详细的错误信息，可以帮助诊断解压失败的原因。
 * </p>
 * <p>
 * 常见的异常原因：
 * <ul>
 *   <li>文件格式不支持</li>
 *   <li>文件损坏或格式错误</li>
 *   <li>密码错误（对于加密文件）</li>
 *   <li>IO错误（如磁盘空间不足）</li>
 *   <li>内存不足</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * try {
 *     // 尝试解压文件
 *     Map<FileInfo, byte[]> result = strategy.unzip(inputStream);
 * } catch (UnzipException e) {
 *     // 处理解压异常
 *     System.err.println("解压失败: " + e.getMessage());
 *     if (e.getCause() != null) {
 *         e.getCause().printStackTrace();
 *     }
 * }
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipStrategy
 * @see FileInfo
 */
@Getter
public class UnzipException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     * 用于标识具体的错误类型，便于异常处理和日志记录
     */
    private final String errorCode;
    
    /**
     * 创建解压异常
     * <p>
     * 使用指定的错误消息创建异常。
     * </p>
     *
     * @param message 错误消息
     */
    public UnzipException(String message) {
        super(message);
        this.errorCode = "UNZIP_ERROR";
    }
    
    /**
     * 创建解压异常
     * <p>
     * 使用指定的错误消息和原因创建异常。
     * </p>
     *
     * @param message 错误消息
     * @param cause 异常原因
     */
    public UnzipException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNZIP_ERROR";
    }
    
    /**
     * 创建解压异常
     * <p>
     * 使用指定的原因创建异常。
     * </p>
     *
     * @param cause 异常原因
     */
    public UnzipException(Throwable cause) {
        super(cause);
        this.errorCode = "UNZIP_ERROR";
    }
    
    /**
     * 创建解压异常
     * <p>
     * 使用指定的错误消息、原因和其他参数创建异常。
     * </p>
     *
     * @param message 错误消息
     * @param cause 异常原因
     * @param enableSuppression 是否启用异常抑制
     * @param writableStackTrace 是否生成堆栈跟踪
     */
    protected UnzipException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = "UNZIP_ERROR";
    }
    
    /**
     * 构造一个新的解压异常，使用错误码枚举
     *
     * @param errorCode 错误码枚举，提供标准化的错误类型
     * @param message 错误信息，描述异常的具体原因
     */
    public UnzipException(UnzipErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode();
    }
    
    /**
     * 构造一个新的解压异常，使用错误码枚举和原始异常
     *
     * @param errorCode 错误码枚举，提供标准化的错误类型
     * @param message 错误信息，描述异常的具体原因
     * @param cause 导致此异常的原始异常，用于异常链追踪
     */
    public UnzipException(UnzipErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }
} 