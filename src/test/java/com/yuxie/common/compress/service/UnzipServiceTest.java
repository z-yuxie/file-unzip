package com.yuxie.common.compress.service;

import com.yuxie.common.compress.callback.UnzipProgressCallback;
import com.yuxie.common.compress.config.UnzipConfig;
import com.yuxie.common.compress.exception.UnzipErrorCode;
import com.yuxie.common.compress.exception.UnzipException;
import com.yuxie.common.compress.format.CompressionFormat;
import com.yuxie.common.compress.model.FileInfo;
import com.yuxie.common.compress.monitor.DefaultUnzipMetrics;
import com.yuxie.common.compress.monitor.UnzipMetrics;
import com.yuxie.common.compress.strategy.UnzipStrategy;
import com.yuxie.common.compress.strategy.UnzipStrategyFactory;
import com.yuxie.common.compress.strategy.impl.DefaultUnzipStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UnzipServiceTest {

    @Mock
    private UnzipStrategyFactory strategyFactory;

    @Mock
    private UnzipStrategy mockStrategy;

    @Mock
    private UnzipMetrics metrics;

    @Mock
    private UnzipProgressCallback progressCallback;

    private UnzipConfig unzipConfig;
    private UnzipService unzipService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unzipConfig = UnzipConfig.builder().build();
        strategyFactory = new DefaultUnzipStrategyFactory(unzipConfig);
        metrics = new DefaultUnzipMetrics();
        unzipService = new UnzipService(strategyFactory, unzipConfig, metrics);
    }

    /**
     * 根据文件路径获取文件的字节数组
     *
     * @param filePath 文件路径
     * @return 文件的字节数组
     * @throws IOException 如果读取文件失败
     */
    private static byte[] getFileBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    /**
     * 根据文件路径获取文件的字节数组，支持大文件
     *
     * @param filePath 文件路径
     * @param bufferSize 缓冲区大小，默认为8192字节
     * @return 文件的字节数组
     * @throws IOException 如果读取文件失败
     */
    private static byte[] getFileBytesWithBuffer(String filePath, int bufferSize) throws IOException {
        File file = new File(filePath);
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    @Test
    void testUnzipBasic() throws UnzipException {
        // 准备测试数据
        byte[] testData = "test data".getBytes();
        Map<FileInfo, byte[]> expectedResult = new HashMap<>();
        FileInfo fileInfo = FileInfo.builder()
                .fileName("test.txt")
                .path("test.txt")
                .size(testData.length)
                .build();
        expectedResult.put(fileInfo, testData);

        // 设置mock行为
        when(strategyFactory.getStrategy(any(CompressionFormat.class))).thenReturn(mockStrategy);
        when(mockStrategy.unzip(any(InputStream.class))).thenReturn(expectedResult);

        // 执行测试
        Map<FileInfo, byte[]> result = unzipService.unzip(testData);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertArrayEquals(testData, result.get(fileInfo));
        verify(metrics).recordSuccess();
    }

    @Test
    void testUnzipWithProgressCallback() throws UnzipException, IOException {
        unzipConfig.setEnableFileTypeCheck(false);
        // 准备测试数据
        byte[] testData = "test data".getBytes();
        Map<FileInfo, byte[]> expectedResult = new HashMap<>();
        FileInfo fileInfo = FileInfo.builder()
                .fileName("test.txt")
                .path("test.txt")
                .size(testData.length)
                .build();
        expectedResult.put(fileInfo, testData);

        // 设置mock行为
        when(strategyFactory.getStrategy(any(CompressionFormat.class))).thenReturn(mockStrategy);
        when(mockStrategy.unzip(any(InputStream.class), any(UnzipProgressCallback.class)))
                .thenReturn(expectedResult);

        // 执行测试
        Map<FileInfo, byte[]> result = unzipService.unzip(testData, progressCallback);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(progressCallback).onStart(eq((long) testData.length), eq(1));
        verify(progressCallback).onProgress(eq("test.txt"), anyLong(), anyLong(), eq(1), eq(1));
        verify(progressCallback).onComplete();
    }

    @Test
    void testUnzipWithInvalidData() {
        // 测试空数据
        assertThrows(UnzipException.class, () -> unzipService.unzip(null));
        assertThrows(UnzipException.class, () -> unzipService.unzip(new byte[0]));
    }

    @Test
    void testUnzipWithFileTooLarge() {
        // 设置最大文件大小限制
        unzipConfig = UnzipConfig.builder()
                .maxFileSize(100L)
                .build();
        unzipService = new UnzipService(strategyFactory, unzipConfig, metrics);
        byte[] largeData = new byte[101];

        // 测试超过大小限制
        UnzipException exception = assertThrows(UnzipException.class, 
            () -> unzipService.unzip(largeData));
        assertEquals(UnzipErrorCode.FILE_TOO_LARGE, exception.getErrorCode());
        verify(metrics).recordError(UnzipErrorCode.FILE_TOO_LARGE);
    }

    @Test
    void testUnzipToFile(@TempDir Path tempDir) throws UnzipException, IOException {
        // 准备测试数据
        byte[] testData = "test data".getBytes();
        Map<FileInfo, byte[]> expectedResult = new HashMap<>();
        FileInfo fileInfo = FileInfo.builder()
                .fileName("test.txt")
                .path("test.txt")
                .size(testData.length)
                .build();
        expectedResult.put(fileInfo, testData);

        // 设置mock行为
        when(strategyFactory.getStrategy(any(CompressionFormat.class))).thenReturn(mockStrategy);
        when(mockStrategy.unzip(any(InputStream.class))).thenReturn(expectedResult);

        // 执行测试
        Map<FileInfo, byte[]> result = unzipService.unzip(
            new ByteArrayInputStream(testData), 
            null, 
            null, 
            tempDir.toString()
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // 验证文件是否写入
        File outputFile = tempDir.resolve("test.txt").toFile();
        assertTrue(outputFile.exists());
        assertEquals(testData.length, outputFile.length());
    }

    @Test
    void testUnzipWithError() throws UnzipException {
        // 准备测试数据
        byte[] testData = "test data".getBytes();

        // 设置mock行为
        when(strategyFactory.getStrategy(any(CompressionFormat.class))).thenReturn(mockStrategy);
        when(mockStrategy.unzip(any(InputStream.class)))
                .thenThrow(new UnzipException(UnzipErrorCode.IO_ERROR, "Test error"));

        // 执行测试
        UnzipException exception = assertThrows(UnzipException.class, 
            () -> unzipService.unzip(testData));
        
        // 验证结果
        assertEquals(UnzipErrorCode.IO_ERROR, exception.getErrorCode());
        verify(metrics).recordError(UnzipErrorCode.IO_ERROR);
    }

    @Test
    void testUnzipWithStrategyCloseError() throws UnzipException, IOException {
        // 准备测试数据
        byte[] testData = "test data".getBytes();
        Map<FileInfo, byte[]> expectedResult = new HashMap<>();
        FileInfo fileInfo = FileInfo.builder()
                .fileName("test.txt")
                .path("test.txt")
                .size(testData.length)
                .build();
        expectedResult.put(fileInfo, testData);

        // 设置mock行为
        when(strategyFactory.getStrategy(any(CompressionFormat.class))).thenReturn(mockStrategy);
        when(mockStrategy.unzip(any(InputStream.class))).thenReturn(expectedResult);
        doThrow(new IOException("Close error")).when(mockStrategy).close();

        // 执行测试
        Map<FileInfo, byte[]> result = unzipService.unzip(testData);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockStrategy).close();
    }
} 