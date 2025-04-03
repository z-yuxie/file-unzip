package com.yuxie.common.compress.callback;

/**
 * 解压进度回调接口
 * <p>
 * 用于在解压过程中报告进度信息。
 * 可以实现此接口来监控解压进度，例如显示进度条、记录日志等。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>进度报告：报告当前解压的进度</li>
 *   <li>文件信息：提供当前正在解压的文件信息</li>
 *   <li>状态更新：通知解压过程中的状态变化</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建进度回调实现
 * UnzipProgressCallback callback = new UnzipProgressCallback() {
 *     @Override
 *     public void onProgress(String fileName, long bytesProcessed, long totalBytes, 
 *                          int filesProcessed, int totalFiles) {
 *         // 计算进度百分比
 *         double progress = (double) bytesProcessed / totalBytes * 100;
 *         System.out.printf("正在解压: %s (%.2f%%)\n", fileName, progress);
 *     }
 * };
 * 
 * // 使用带进度回调的解压方法
 * strategy.unzip(inputStream, callback);
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see UnzipStrategy
 */
public interface UnzipProgressCallback {
    
    /**
     * 解压开始时的回调
     * <p>
     * 在开始解压文件之前调用此方法，用于初始化进度显示或记录开始时间等。
     * 此方法提供了默认实现，子类可以根据需要重写。
     * </p>
     *
     * @param totalSize 压缩文件总大小（字节）
     * @param totalFiles 压缩文件中的文件总数
     */
    default void onStart(long totalSize, int totalFiles) {
        // 默认空实现
    }
    
    /**
     * 进度回调方法
     * <p>
     * 在解压过程中定期调用此方法报告进度。
     * 可以用于更新进度条、记录日志等。
     * </p>
     *
     * @param fileName 当前正在解压的文件名，如果无法获取则为null
     * @param bytesProcessed 已处理的字节数
     * @param totalBytes 需要处理的总字节数
     * @param filesProcessed 已处理的文件数
     * @param totalFiles 需要处理的总文件数
     */
    void onProgress(String fileName, long bytesProcessed, long totalBytes, int filesProcessed, int totalFiles);
    
    /**
     * 解压完成时的回调
     * <p>
     * 在所有文件解压完成后调用此方法，用于清理资源或更新最终状态。
     * 此方法必须由实现类实现，不能使用默认实现。
     * </p>
     */
    void onComplete();
    
    /**
     * 解压出错时的回调
     * <p>
     * 在解压过程中发生错误时调用此方法，用于记录错误信息或通知用户。
     * 此方法必须由实现类实现，不能使用默认实现。
     * </p>
     *
     * @param message 错误信息，描述具体的错误原因
     */
    void onError(String message);
} 