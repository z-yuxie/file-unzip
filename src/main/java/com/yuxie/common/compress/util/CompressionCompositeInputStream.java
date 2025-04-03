package com.yuxie.common.compress.util;

import com.yuxie.common.compress.exception.CompressionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 压缩格式复合输入流
 * 用于管理压缩操作中的多个输入流的关闭操作
 */
public class CompressionCompositeInputStream extends InputStream {
    private final InputStream delegate;
    private final List<AutoCloseable> resources;
    
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
    }
    
    @Override
    public int read() throws IOException {
        return delegate.read();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }
    
    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }
    
    @Override
    public int available() throws IOException {
        return delegate.available();
    }
    
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
    
    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }
    
    @Override
    public void reset() throws IOException {
        delegate.reset();
    }
    
    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }
} 