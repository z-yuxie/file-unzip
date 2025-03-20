package com.example.fileunzip.callback;

/**
 * 解压进度回调接口
 */
public interface UnzipProgressCallback {
    /**
     * 开始解压
     *
     * @param totalSize 总大小（字节）
     * @param totalFiles 总文件数
     */
    void onStart(long totalSize, int totalFiles);

    /**
     * 文件解压进度
     *
     * @param fileName 当前解压的文件名
     * @param bytesProcessed 已处理的字节数
     * @param totalBytes 总字节数
     * @param fileIndex 当前文件索引
     * @param totalFiles 总文件数
     */
    void onProgress(String fileName, long bytesProcessed, long totalBytes, int fileIndex, int totalFiles);

    /**
     * 文件解压完成
     *
     * @param fileName 解压完成的文件名
     * @param fileIndex 文件索引
     * @param totalFiles 总文件数
     */
    void onFileComplete(String fileName, int fileIndex, int totalFiles);

    /**
     * 解压完成
     */
    void onComplete();

    /**
     * 解压失败
     *
     * @param error 错误信息
     */
    void onError(String error);
} 