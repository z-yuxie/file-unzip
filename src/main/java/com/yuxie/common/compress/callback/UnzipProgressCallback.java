package com.yuxie.common.compress.callback;

/**
 * 解压进度回调接口
 */
public interface UnzipProgressCallback {
    
    /**
     * 解压开始时的回调
     *
     * @param totalSize 压缩文件总大小（字节）
     * @param totalFiles 压缩文件中的文件总数
     */
    default void onStart(long totalSize, int totalFiles) {
        // 默认空实现
    }
    
    /**
     * 解压进度回调
     *
     * @param fileName 当前正在解压的文件名
     * @param bytesRead 已读取的字节数
     * @param totalBytes 文件总字节数
     * @param currentFile 当前处理的文件序号
     * @param totalFiles 总文件数
     */
    void onProgress(String fileName, long bytesRead, long totalBytes, int currentFile, int totalFiles);
    
    /**
     * 解压完成时的回调
     */
    void onComplete();
    
    /**
     * 解压出错时的回调
     *
     * @param message 错误信息
     */
    void onError(String message);
} 