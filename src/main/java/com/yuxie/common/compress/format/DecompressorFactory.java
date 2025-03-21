package com.yuxie.common.compress.format;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 解压器工厂类
 * 用于创建各种压缩格式的解压输入流
 * 注意：此工厂类只处理需要通用解压器的格式，如GZIP、BZIP2等
 * 对于RAR、7Z等特殊格式，请使用对应的专门策略类
 */
public class DecompressorFactory {
    
    private static final ArchiveStreamFactory archiveFactory = new ArchiveStreamFactory();
    private static final CompressorStreamFactory compressorFactory = new CompressorStreamFactory();
    
    /**
     * 创建解压输入流
     *
     * @param data 压缩文件数据
     * @param format 压缩格式
     * @return 解压输入流
     * @throws IOException IO异常
     */
    public static InputStream createDecompressor(byte[] data, CompressionFormat format) throws IOException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("压缩数据不能为空");
        }
        if (format == null) {
            throw new IllegalArgumentException("压缩格式不能为空");
        }
        
        // 检查是否支持该格式
        if (!isSupportedFormat(format)) {
            throw new UnsupportedOperationException(
                String.format("格式 %s 不支持通过通用解压器处理，请使用对应的专门策略类", format));
        }
        
        // 创建基础输入流
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        
        try {
            // 首先处理单一压缩格式
            InputStream compressorStream = createCompressorStream(bis, format);
            if (compressorStream != null) {
                return new CloseShieldInputStream(compressorStream, bis);
            }
            
            // 然后处理归档格式
            return createArchiveStream(bis, format);
            
        } catch (Exception e) {
            // 发生异常时关闭基础流
            IOUtils.closeQuietly(bis);
            
            // 转换并抛出适当的异常
            throw convertException(e, format);
        }
    }
    
    /**
     * 检查是否支持通过通用解压器处理该格式
     */
    private static boolean isSupportedFormat(CompressionFormat format) {
        return switch (format) {
            case GZIP, BZIP2, XZ, LZMA, SNAPPY, LZ4,
                 TAR, TAR_GZ, TAR_BZ2, TAR_XZ -> true;
            case ZIP, RAR, SEVEN_ZIP -> false;
            default -> false;
        };
    }
    
    /**
     * 创建压缩格式输入流
     */
    private static InputStream createCompressorStream(InputStream input, CompressionFormat format) throws CompressorException {
        return switch (format) {
            case GZIP -> compressorFactory.createCompressorInputStream(CompressorStreamFactory.GZIP, input);
            case BZIP2 -> compressorFactory.createCompressorInputStream(CompressorStreamFactory.BZIP2, input);
            case XZ -> compressorFactory.createCompressorInputStream(CompressorStreamFactory.XZ, input);
            case LZMA -> compressorFactory.createCompressorInputStream(CompressorStreamFactory.LZMA, input);
            case SNAPPY -> compressorFactory.createCompressorInputStream(CompressorStreamFactory.SNAPPY_FRAMED, input);
            case LZ4 -> compressorFactory.createCompressorInputStream(CompressorStreamFactory.LZ4_BLOCK, input);
            default -> null;
        };
    }
    
    /**
     * 创建归档格式输入流
     */
    private static InputStream createArchiveStream(InputStream input, CompressionFormat format) throws IOException {
        try {
            return switch (format) {
                case TAR -> new CloseShieldInputStream(
                    archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, input),
                    input
                );
                case TAR_GZ -> {
                    InputStream gzipStream = compressorFactory.createCompressorInputStream(CompressorStreamFactory.GZIP, input);
                    yield new CloseShieldInputStream(
                        archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, gzipStream),
                        gzipStream, input
                    );
                }
                case TAR_BZ2 -> {
                    InputStream bzip2Stream = compressorFactory.createCompressorInputStream(CompressorStreamFactory.BZIP2, input);
                    yield new CloseShieldInputStream(
                        archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, bzip2Stream),
                        bzip2Stream, input
                    );
                }
                case TAR_XZ -> {
                    InputStream xzStream = compressorFactory.createCompressorInputStream(CompressorStreamFactory.XZ, input);
                    yield new CloseShieldInputStream(
                        archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, xzStream),
                        xzStream, input
                    );
                }
                default -> throw new IllegalArgumentException("不支持的压缩格式: " + format);
            };
        } catch (ArchiveException | CompressorException e) {
            throw new IOException("创建" + format + "格式解压流失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换异常
     */
    private static IOException convertException(Exception e, CompressionFormat format) {
        String message = String.format("创建%s格式解压流失败", format);
        if (e instanceof ArchiveException || e instanceof CompressorException) {
            return new IOException(message + ": " + e.getMessage(), e);
        } else if (e instanceof IOException) {
            return (IOException) e;
        } else if (e instanceof UnsupportedOperationException) {
            return new IOException(e.getMessage(), e);
        } else {
            return new IOException(message + ": 未知错误", e);
        }
    }
    
    /**
     * 包装输入流，确保正确关闭所有资源
     */
    private static class CloseShieldInputStream extends InputStream {
        private final InputStream delegate;
        private final List<InputStream> resourcesToClose;
        
        public CloseShieldInputStream(InputStream delegate, InputStream... resourcesToClose) {
            this.delegate = delegate;
            this.resourcesToClose = new ArrayList<>();
            if (resourcesToClose != null) {
                for (InputStream resource : resourcesToClose) {
                    if (resource != null) {
                        this.resourcesToClose.add(resource);
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
            try {
                delegate.close();
            } finally {
                // 确保所有资源都被关闭
                for (InputStream resource : resourcesToClose) {
                    IOUtils.closeQuietly(resource);
                }
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
} 