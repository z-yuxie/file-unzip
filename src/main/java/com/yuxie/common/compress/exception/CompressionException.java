package com.yuxie.common.compress.exception;

import com.yuxie.common.compress.format.CompressionFormat;
import lombok.Getter;

/**
 * 压缩操作异常
 * <p>
 * 该类表示在压缩或解压操作过程中发生的异常。
 * 包含了异常发生时的压缩格式、操作类型等详细信息。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * try {
 *     // 执行压缩操作
 * } catch (IOException e) {
 *     throw new CompressionException(CompressionFormat.ZIP, "压缩", "文件写入失败", e);
 * }
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see CompressionFormat
 */
@Getter
public class CompressionException extends RuntimeException {
    /**
     * 发生异常时的压缩格式
     */
    private final CompressionFormat format;
    
    /**
     * 发生异常时的操作类型（如：压缩、解压等）
     */
    private final String operation;
    
    /**
     * 创建一个新的压缩异常
     *
     * @param format 压缩格式
     * @param operation 操作类型
     * @param message 异常信息
     */
    public CompressionException(CompressionFormat format, String operation, String message) {
        super(String.format("%s格式%s操作失败: %s", format, operation, message));
        this.format = format;
        this.operation = operation;
    }
    
    /**
     * 创建一个新的压缩异常，包含原始异常
     *
     * @param format 压缩格式
     * @param operation 操作类型
     * @param cause 原始异常
     */
    public CompressionException(CompressionFormat format, String operation, Throwable cause) {
        super(String.format("%s格式%s操作失败: %s", format, operation, cause.getMessage()), cause);
        this.format = format;
        this.operation = operation;
    }
    
    /**
     * 创建一个新的压缩异常，包含异常信息和原始异常
     *
     * @param format 压缩格式
     * @param operation 操作类型
     * @param message 异常信息
     * @param cause 原始异常
     */
    public CompressionException(CompressionFormat format, String operation, String message, Throwable cause) {
        super(String.format("%s格式%s操作失败: %s", format, operation, message), cause);
        this.format = format;
        this.operation = operation;
    }
} 