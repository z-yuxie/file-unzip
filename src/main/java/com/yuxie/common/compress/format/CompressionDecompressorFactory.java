package com.yuxie.common.compress.format;

import com.yuxie.common.compress.exception.CompressionException;
import com.yuxie.common.compress.util.CompressionCompositeInputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 压缩格式解压器工厂类
 * 用于创建各种压缩格式的解压输入流
 * 注意：此工厂类只处理需要通用解压器的格式，如GZIP、BZIP2等
 * 对于RAR、7Z等特殊格式，请使用对应的专门策略类
 */
public class CompressionDecompressorFactory {
    
    private static final ArchiveStreamFactory archiveFactory = new ArchiveStreamFactory();
    private static final CompressorStreamFactory compressorFactory = new CompressorStreamFactory();
    
    /**
     * 创建解压输入流
     *
     * @param data 压缩文件数据
     * @param format 压缩格式
     * @return 解压输入流
     * @throws CompressionException 压缩操作异常
     */
    public static InputStream createDecompressor(byte[] data, CompressionFormat format) throws CompressionException {
        if (data == null || data.length == 0) {
            throw new CompressionException(format, "创建解压流", "压缩数据不能为空");
        }
        if (format == null) {
            throw new CompressionException(null, "创建解压流", "压缩格式不能为空");
        }
        
        // 检查是否支持该格式
        if (!isSupportedFormat(format)) {
            throw new CompressionException(format, "创建解压流", 
                "格式不支持通过通用解压器处理，请使用对应的专门策略类");
        }
        
        // 创建基础输入流
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        
        try {
            // 首先处理单一压缩格式
            InputStream compressorStream = createCompressorStream(bis, format);
            if (compressorStream != null) {
                return new CompressionCompositeInputStream(compressorStream, bis);
            }
            
            // 然后处理归档格式
            return createArchiveStream(bis, format);
            
        } catch (Exception e) {
            throw new CompressionException(format, "创建解压流", e);
        }
    }
    
    /**
     * 检查是否支持通过通用解压器处理该格式
     */
    private static boolean isSupportedFormat(CompressionFormat format) {
        switch (format) {
            case GZIP:
            case BZIP2:
            case XZ:
            case LZMA:
            case SNAPPY:
            case LZ4:
            case TAR:
            case TAR_GZ:
            case TAR_BZ2:
            case TAR_XZ:
                return true;
            case ZIP:
            case RAR:
            case SEVEN_ZIP:
            case COMPOUND:
            case UNKNOWN:
            default:
                return false;
        }
    }
    
    /**
     * 创建压缩格式输入流
     */
    private static InputStream createCompressorStream(InputStream input, CompressionFormat format) throws CompressorException {
        switch (format) {
            case GZIP:
                return compressorFactory.createCompressorInputStream(CompressorStreamFactory.GZIP, input);
            case BZIP2:
                return compressorFactory.createCompressorInputStream(CompressorStreamFactory.BZIP2, input);
            case XZ:
                return compressorFactory.createCompressorInputStream(CompressorStreamFactory.XZ, input);
            case LZMA:
                return compressorFactory.createCompressorInputStream(CompressorStreamFactory.LZMA, input);
            case SNAPPY:
                return compressorFactory.createCompressorInputStream(CompressorStreamFactory.SNAPPY_FRAMED, input);
            case LZ4:
                return compressorFactory.createCompressorInputStream(CompressorStreamFactory.LZ4_BLOCK, input);
            default:
                return null;
        }
    }
    
    /**
     * 创建归档格式输入流
     */
    private static InputStream createArchiveStream(InputStream input, CompressionFormat format) throws IOException {
        try {
            InputStream result;
            switch (format) {
                case TAR:
                    result = new CompressionCompositeInputStream(
                        archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, input),
                        input
                    );
                    break;
                case TAR_GZ:
                    InputStream gzipStream = compressorFactory.createCompressorInputStream(CompressorStreamFactory.GZIP, input);
                    result = new CompressionCompositeInputStream(
                        archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, gzipStream),
                        gzipStream, input
                    );
                    break;
                case TAR_BZ2:
                    InputStream bzip2Stream = compressorFactory.createCompressorInputStream(CompressorStreamFactory.BZIP2, input);
                    result = new CompressionCompositeInputStream(
                        archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, bzip2Stream),
                        bzip2Stream, input
                    );
                    break;
                case TAR_XZ:
                    InputStream xzStream = compressorFactory.createCompressorInputStream(CompressorStreamFactory.XZ, input);
                    result = new CompressionCompositeInputStream(
                        archiveFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, xzStream),
                        xzStream, input
                    );
                    break;
                default:
                    throw new CompressionException(format, "创建归档流", "不支持的压缩格式");
            }
            return result;
        } catch (ArchiveException | CompressorException e) {
            throw new CompressionException(format, "创建归档流", e);
        }
    }
} 