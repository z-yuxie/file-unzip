package com.yuxie.common.compress.util;

import com.yuxie.common.compress.exception.CompressionException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 复合输入流包装器
 * <p>
 * 用于包装和管理多个需要关闭的资源（如InputStream、RandomAccessFile等）。
 * 当关闭此输入流时，会自动关闭所有被包装的资源。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>资源管理：统一管理多个需要关闭的资源</li>
 *   <li>自动关闭：在关闭输入流时自动关闭所有资源</li>
 *   <li>异常处理：确保所有资源都能被正确关闭</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建需要管理的资源
 * InputStream inputStream = new FileInputStream("file.txt");
 * RandomAccessFile randomAccessFile = new RandomAccessFile("file.txt", "r");
 * 
 * // 创建复合输入流
 * CompressionCompositeInputStream compositeStream = new CompressionCompositeInputStream(
 *     inputStream, randomAccessFile);
 * 
 * // 使用复合输入流
 * try {
 *     // 读取数据
 *     byte[] data = new byte[1024];
 *     compositeStream.read(data);
 * } finally {
 *     // 关闭复合输入流（会自动关闭所有资源）
 *     compositeStream.close();
 * }
 * </pre>
 * </p>
 *
 * @author yuxie
 * @since 1.0.0
 * @see InputStream
 * @see Closeable
 */
public class CompressionCompositeInputStream extends InputStream {
    /**
     * 委托的输入流，实际的读取操作由此流完成
     */
    private final InputStream delegate;
    
    /**
     * 需要关闭的资源列表
     */
    private final List<AutoCloseable> resources;
    
    /**
     * 用于缓冲读取的字节数组
     */
    private final byte[] buffer;
    
    /**
     * 当前缓冲区中的读取位置
     */
    private int position;
    
    /**
     * 当前缓冲区中的有效数据长度
     */
    private int count;
    
    /**
     * 缓冲区大小，默认8KB
     */
    private static final int BUFFER_SIZE = 8192;
    
    /**
     * 创建一个新的复合输入流
     *
     * @param delegate 主要的输入流，不能为null
     * @param resources 需要随输入流一起关闭的资源，可以为null
     */
    public CompressionCompositeInputStream(InputStream delegate, AutoCloseable... resources) {
        this.delegate = delegate;
        this.resources = new ArrayList<>();
        if (resources != null) {
            for (AutoCloseable resource : resources) {
                if (resource != null) {
                    this.resources.add(resource);
                }
            }
        }
        this.buffer = new byte[BUFFER_SIZE];
        this.position = 0;
        this.count = 0;
    }
    
    /**
     * 读取单个字节
     * <p>
     * 如果缓冲区中没有数据，会尝试从委托流中读取新的数据。
     * </p>
     *
     * @return 读取到的字节，如果到达流末尾则返回-1
     * @throws IOException 如果读取过程中发生IO错误
     */
    @Override
    public int read() throws IOException {
        if (position >= count) {
            fill();
            if (position >= count) {
                return -1;
            }
        }
        return buffer[position++] & 0xff;
    }
    
    /**
     * 读取多个字节到指定的数组中
     * <p>
     * 如果缓冲区中没有足够的数据，会尝试从委托流中读取新的数据。
     * </p>
     *
     * @param b 目标字节数组
     * @param off 起始偏移量
     * @param len 要读取的最大字节数
     * @return 实际读取的字节数，如果到达流末尾则返回-1
     * @throws IOException 如果读取过程中发生IO错误
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (position >= count) {
            fill();
            if (position >= count) {
                return -1;
            }
        }
        int available = count - position;
        int toRead = Math.min(available, len);
        System.arraycopy(buffer, position, b, off, toRead);
        position += toRead;
        return toRead;
    }
    
    /**
     * 从委托流中填充缓冲区
     *
     * @throws IOException 如果读取过程中发生IO错误
     */
    private void fill() throws IOException {
        position = 0;
        count = delegate.read(buffer);
    }
    
    /**
     * 跳过指定数量的字节
     *
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数
     * @throws IOException 如果跳过过程中发生IO错误
     */
    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }
    
    /**
     * 获取可读取的字节数
     *
     * @return 可读取的字节数
     * @throws IOException 如果获取过程中发生IO错误
     */
    @Override
    public int available() throws IOException {
        return delegate.available();
    }
    
    /**
     * 关闭输入流和所有关联的资源
     * <p>
     * 该方法会尝试关闭所有资源，即使某个资源关闭失败也会继续关闭其他资源。
     * 如果关闭过程中发生任何异常，将抛出第一个遇到的异常。
     * </p>
     *
     * @throws IOException 如果关闭过程中发生IO错误
     * @throws CompressionException 如果关闭资源时发生其他错误
     */
    @Override
    public void close() throws IOException {
        List<Exception> exceptions = new ArrayList<>();
        
        // 关闭委托的输入流
        try {
            delegate.close();
        } catch (Exception e) {
            exceptions.add(e);
        }
        
        // 关闭其他资源
        for (AutoCloseable resource : resources) {
            try {
                resource.close();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        
        // 如果有异常，抛出第一个异常
        if (!exceptions.isEmpty()) {
            Exception firstException = exceptions.get(0);
            throw new CompressionException(
                null, // 格式未知
                "关闭资源",
                "关闭资源时发生错误: " + firstException.getMessage(),
                firstException
            );
        }
    }
    
    /**
     * 在当前位置设置标记
     *
     * @param readlimit 在标记失效前可以读取的最大字节数
     */
    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }
    
    /**
     * 重置输入流位置到标记的位置
     * <p>
     * 如果底层流不支持标记和重置操作，将抛出IOException。
     * </p>
     *
     * @throws IOException 如果重置操作失败或底层流不支持重置
     */
    public void reset() throws IOException {
        if (delegate.markSupported()) {
            delegate.reset();
        } else {
            throw new IOException("输入流不支持重置操作");
        }
        position = 0;
        count = 0;
    }
    
    /**
     * 检查输入流是否支持标记和重置操作
     *
     * @return 如果支持标记和重置操作则返回true
     */
    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }
} 