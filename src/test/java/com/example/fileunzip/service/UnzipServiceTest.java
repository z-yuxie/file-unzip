package com.example.fileunzip.service;

import com.example.fileunzip.model.FileInfo;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * UnzipService的单元测试类
 * 
 * 测试内容：
 * 1. 基本功能测试
 *    - ZIP文件解压测试
 *    - 7Z文件解压测试
 *    - TAR文件解压测试
 *    - RAR文件解压测试
 *    - BZIP2文件解压测试
 *    - XZ文件解压测试
 *    - LZMA文件解压测试
 * 
 * 2. 边界条件测试
 *    - 空数据测试
 *    - null数据测试
 *    - 空扩展名测试
 *    - null扩展名测试
 *    - 不支持的扩展名测试
 * 
 * 3. 结果验证
 *    - 解压结果非空验证
 *    - 文件信息完整性验证
 *    - 文件内容完整性验证
 */
public class UnzipServiceTest {
    
    @Test
    public void testUnzipZipFile() throws Exception {
        // 准备测试数据
        String testFilePath = "src/test/resources/test.zip"; // 测试文件路径
        Path path = Paths.get(testFilePath);
        
        // 确保测试文件存在
        assertTrue("测试文件不存在: " + testFilePath, Files.exists(path));
        
        // 读取测试文件
        byte[] testData = Files.readAllBytes(path);
        assertNotNull("测试文件数据不能为空", testData);
        assertTrue("测试文件数据长度必须大于0", testData.length > 0);
        
        // 获取文件扩展名
        String fileExtension = getFileExtension(testFilePath);
        assertNotNull("文件扩展名不能为空", fileExtension);
        
        // 执行测试
        UnzipService service = new UnzipService();
        Map<FileInfo, byte[]> result = service.unzip(testData, fileExtension);
        
        // 验证结果
        assertNotNull("解压结果不能为空", result);
        assertFalse("解压结果不能为空映射", result.isEmpty());
        
        // 验证解压出的文件
        for (Map.Entry<FileInfo, byte[]> entry : result.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            byte[] content = entry.getValue();
            
            assertNotNull("文件信息不能为空", fileInfo);
            assertNotNull("文件名不能为空", fileInfo.getFileName());
            assertNotNull("文件路径不能为空", fileInfo.getPath());
            assertTrue("文件大小必须大于等于0", fileInfo.getSize() >= 0);
            assertTrue("文件修改时间必须大于0", fileInfo.getLastModified() > 0);
            
            assertNotNull("文件内容不能为空", content);
            assertTrue("文件内容长度必须大于0", content.length > 0);
        }
    }
    
    @Test
    public void testUnzip7zFile() throws Exception {
        // 准备测试数据
        String testFilePath = "src/test/resources/test.7z"; // 测试文件路径
        Path path = Paths.get(testFilePath);
        
        // 确保测试文件存在
        assertTrue("测试文件不存在: " + testFilePath, Files.exists(path));
        
        // 读取测试文件
        byte[] testData = Files.readAllBytes(path);
        assertNotNull("测试文件数据不能为空", testData);
        assertTrue("测试文件数据长度必须大于0", testData.length > 0);
        
        // 获取文件扩展名
        String fileExtension = getFileExtension(testFilePath);
        assertNotNull("文件扩展名不能为空", fileExtension);
        
        // 执行测试
        UnzipService service = new UnzipService();
        Map<FileInfo, byte[]> result = service.unzip(testData, fileExtension);
        
        // 验证结果
        assertNotNull("解压结果不能为空", result);
        assertFalse("解压结果不能为空映射", result.isEmpty());
        
        // 验证解压出的文件
        for (Map.Entry<FileInfo, byte[]> entry : result.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            byte[] content = entry.getValue();
            
            assertNotNull("文件信息不能为空", fileInfo);
            assertNotNull("文件名不能为空", fileInfo.getFileName());
            assertNotNull("文件路径不能为空", fileInfo.getPath());
            assertTrue("文件大小必须大于等于0", fileInfo.getSize() >= 0);
            assertTrue("文件修改时间必须大于0", fileInfo.getLastModified() > 0);
            
            assertNotNull("文件内容不能为空", content);
            assertTrue("文件内容长度必须大于0", content.length > 0);
        }
    }
    
    @Test
    public void testUnzipTarFile() throws Exception {
        // 准备测试数据
        String testFilePath = "src/test/resources/test.tar"; // 测试文件路径
        Path path = Paths.get(testFilePath);
        
        // 确保测试文件存在
        assertTrue("测试文件不存在: " + testFilePath, Files.exists(path));
        
        // 读取测试文件
        byte[] testData = Files.readAllBytes(path);
        assertNotNull("测试文件数据不能为空", testData);
        assertTrue("测试文件数据长度必须大于0", testData.length > 0);
        
        // 获取文件扩展名
        String fileExtension = getFileExtension(testFilePath);
        assertNotNull("文件扩展名不能为空", fileExtension);
        
        // 执行测试
        UnzipService service = new UnzipService();
        Map<FileInfo, byte[]> result = service.unzip(testData, fileExtension);
        
        // 验证结果
        assertNotNull("解压结果不能为空", result);
        assertFalse("解压结果不能为空映射", result.isEmpty());
        
        // 验证解压出的文件
        for (Map.Entry<FileInfo, byte[]> entry : result.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            byte[] content = entry.getValue();
            
            assertNotNull("文件信息不能为空", fileInfo);
            assertNotNull("文件名不能为空", fileInfo.getFileName());
            assertNotNull("文件路径不能为空", fileInfo.getPath());
            assertTrue("文件大小必须大于等于0", fileInfo.getSize() >= 0);
            assertTrue("文件修改时间必须大于0", fileInfo.getLastModified() > 0);
            
            assertNotNull("文件内容不能为空", content);
            assertTrue("文件内容长度必须大于0", content.length > 0);
        }
    }
    
    @Test
    public void testUnzipRarFile() throws Exception {
        // 准备测试数据
        String testFilePath = "src/test/resources/test.rar"; // 测试文件路径
        Path path = Paths.get(testFilePath);
        
        // 确保测试文件存在
        assertTrue("测试文件不存在: " + testFilePath, Files.exists(path));
        
        // 读取测试文件
        byte[] testData = Files.readAllBytes(path);
        assertNotNull("测试文件数据不能为空", testData);
        assertTrue("测试文件数据长度必须大于0", testData.length > 0);
        
        // 获取文件扩展名
        String fileExtension = getFileExtension(testFilePath);
        assertNotNull("文件扩展名不能为空", fileExtension);
        
        // 执行测试
        UnzipService service = new UnzipService();
        Map<FileInfo, byte[]> result = service.unzip(testData, fileExtension);
        
        // 验证结果
        assertNotNull("解压结果不能为空", result);
        assertFalse("解压结果不能为空映射", result.isEmpty());
        
        // 验证解压出的文件
        for (Map.Entry<FileInfo, byte[]> entry : result.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            byte[] content = entry.getValue();
            
            assertNotNull("文件信息不能为空", fileInfo);
            assertNotNull("文件名不能为空", fileInfo.getFileName());
            assertNotNull("文件路径不能为空", fileInfo.getPath());
            assertTrue("文件大小必须大于等于0", fileInfo.getSize() >= 0);
            assertTrue("文件修改时间必须大于0", fileInfo.getLastModified() > 0);
            
            assertNotNull("文件内容不能为空", content);
            assertTrue("文件内容长度必须大于0", content.length > 0);
        }
    }
    
    @Test
    public void testUnzipBzip2File() throws Exception {
        testCompressedFile("test.bz2", "bzip2");
    }
    
    @Test
    public void testUnzipXzFile() throws Exception {
        testCompressedFile("test.xz", "xz");
    }
    
    @Test
    public void testUnzipLzmaFile() throws Exception {
        testCompressedFile("test.lzma", "lzma");
    }
    
    /**
     * 测试压缩文件解压的通用方法
     *
     * @param testFileName 测试文件名
     * @param format 压缩格式
     * @throws Exception 测试过程中可能发生的异常
     */
    private void testCompressedFile(String testFileName, String format) throws Exception {
        // 准备测试数据
        String testFilePath = "src/test/resources/" + testFileName;
        Path path = Paths.get(testFilePath);
        
        // 确保测试文件存在
        assertTrue("测试文件不存在: " + testFilePath, Files.exists(path));
        
        // 读取测试文件
        byte[] testData = Files.readAllBytes(path);
        assertNotNull("测试文件数据不能为空", testData);
        assertTrue("测试文件数据长度必须大于0", testData.length > 0);
        
        // 执行测试
        UnzipService service = new UnzipService();
        Map<FileInfo, byte[]> result = service.unzip(testData, format);
        
        // 验证结果
        assertNotNull("解压结果不能为空", result);
        assertFalse("解压结果不能为空映射", result.isEmpty());
        assertEquals("解压结果应该只包含一个文件", 1, result.size());
        
        // 验证解压出的文件
        Map.Entry<FileInfo, byte[]> entry = result.entrySet().iterator().next();
        FileInfo fileInfo = entry.getKey();
        byte[] content = entry.getValue();
        
        assertNotNull("文件信息不能为空", fileInfo);
        assertNotNull("文件名不能为空", fileInfo.getFileName());
        assertTrue("文件名应该包含正确的扩展名", fileInfo.getFileName().endsWith("." + format));
        assertTrue("文件大小必须大于等于0", fileInfo.getSize() >= 0);
        assertTrue("文件修改时间必须大于0", fileInfo.getLastModified() > 0);
        
        assertNotNull("文件内容不能为空", content);
        assertTrue("文件内容长度必须大于0", content.length > 0);
    }
    
    /**
     * 获取文件扩展名（不包含点号）
     *
     * @param filePath 文件路径
     * @return 文件扩展名，如果没有扩展名则返回空字符串
     */
    private String getFileExtension(String filePath) {
        String fileName = Paths.get(filePath).getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnzipWithEmptyData() throws IOException {
        UnzipService service = new UnzipService();
        service.unzip(new byte[0], "zip");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnzipWithNullData() throws IOException {
        UnzipService service = new UnzipService();
        service.unzip(null, "zip");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnzipWithEmptyExtension() throws IOException {
        UnzipService service = new UnzipService();
        service.unzip(new byte[]{1, 2, 3}, "");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnzipWithNullExtension() throws IOException {
        UnzipService service = new UnzipService();
        service.unzip(new byte[]{1, 2, 3}, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnzipWithUnsupportedExtension() throws IOException {
        UnzipService service = new UnzipService();
        service.unzip(new byte[]{1, 2, 3}, "unsupported");
    }
} 