package com.yuxie.common.compress.strategy;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.util.CompressionCompositeInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 解压策略接口
 * <p>
 * 定义了不同压缩格式的解压策略，采用策略模式实现。
 * 每种压缩格式（如ZIP、RAR、7Z等）都需要实现此接口，提供相应的解压实现。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>支持多种解压方式：普通解压、带密码解压、带进度回调解压</li>
 *   <li>支持复合输入流：可以处理已封装的复合输入流</li>
 *   <li>格式支持检查：可以检查是否支持特定的压缩格式</li>
 *   <li>资源管理：实现了AutoCloseable接口，支持资源的自动关闭</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建解压策略
 * UnzipStrategy strategy = new ZipUnzipStrategy();
 * 
 * // 使用策略解压文件
 * try (InputStream inputStream = new FileInputStream("archive.zip")) {
 *     Map<FileInfo, byte[]> result = strategy.unzip(inputStream);
 *     // 处理解压结果
 * }
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see CompressionFormat
 * @see FileInfo
 * @see UnzipProgressCallback
 * @see CompressionCompositeInputStream
 */
public interface UnzipStrategy extends AutoCloseable {
    /**
     * 解压文件
     * <p>
     * 将压缩文件解压为文件信息及其内容的映射。
     * 文件信息包含文件名、大小、修改时间等元数据。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream) throws UnzipException;

    /**
     * 解压文件（带进度回调）
     * <p>
     * 在解压过程中通过回调接口报告进度。
     * 可以用于显示进度条、记录日志等场景。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream, UnzipProgressCallback callback) throws UnzipException;

    /**
     * 解压文件（带密码）
     * <p>
     * 使用密码解压加密的压缩文件。
     * 如果压缩文件未加密，密码参数将被忽略。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream, String password) throws UnzipException;

    /**
     * 解压文件（带密码和进度回调）
     * <p>
     * 使用密码解压加密的压缩文件，并通过回调接口报告进度。
     * 这是最完整的解压方法，支持所有功能。
     * </p>
     *
     * @param inputStream 输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzip(InputStream inputStream, String password, UnzipProgressCallback callback) throws UnzipException;

    /**
     * 解压文件（使用已封装的复合输入流）
     * <p>
     * 使用已封装的复合输入流进行解压。
     * 复合输入流可以包含多个需要关闭的资源，解压完成后会自动关闭。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream) throws UnzipException;

    /**
     * 解压文件（使用已封装的复合输入流，带进度回调）
     * <p>
     * 使用已封装的复合输入流进行解压，并通过回调接口报告进度。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, UnzipProgressCallback callback) throws UnzipException;

    /**
     * 解压文件（使用已封装的复合输入流，带密码）
     * <p>
     * 使用已封装的复合输入流和密码进行解压。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, String password) throws UnzipException;

    /**
     * 解压文件（使用已封装的复合输入流，带密码和进度回调）
     * <p>
     * 使用已封装的复合输入流、密码进行解压，并通过回调接口报告进度。
     * 这是最完整的复合流解压方法，支持所有功能。
     * </p>
     *
     * @param compositeInputStream 已封装的复合输入流，不能为null
     * @param password 解压密码，如果文件未加密可以为null
     * @param callback 进度回调接口，可以为null
     * @return 解压后的文件信息及其内容的映射，key为文件信息，value为文件内容
     * @throws UnzipException 当解压过程中发生错误时抛出异常
     */
    Map<FileInfo, byte[]> unzipWithCompositeStream(CompressionCompositeInputStream compositeInputStream, String password, UnzipProgressCallback callback) throws UnzipException;

    /**
     * 检查是否支持指定的压缩格式
     * <p>
     * 判断当前策略是否支持解压指定格式的压缩文件。
     * </p>
     *
     * @param format 要检查的压缩格式，不能为null
     * @return 如果支持该格式则返回true，否则返回false
     */
    boolean isSupportedFormat(CompressionFormat format);

    /**
     * 获取支持的压缩格式列表
     * <p>
     * 返回当前策略支持的所有压缩格式。
     * </p>
     *
     * @return 支持的压缩格式数组，不会返回null
     */
    CompressionFormat[] getSupportedFormats();

    /**
     * 关闭资源
     * <p>
     * 释放策略使用的所有资源。
     * 在不再使用策略时应该调用此方法。
     * </p>
     *
     * @throws IOException 当关闭资源时发生IO错误
     */
    void close() throws IOException;
} 