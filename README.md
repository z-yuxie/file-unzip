# File Unzip

一个功能强大的文件解压工具，支持多种压缩格式，提供灵活的解压策略和进度回调机制。

## 功能特性

- 支持多种压缩格式：
  - ZIP (.zip)
  - RAR (.rar)
  - 7Z (.7z)
  - TAR (.tar)
  - TAR.GZ (.tar.gz, .tgz)
  - TAR.BZ2 (.tar.bz2)
  - TAR.XZ (.tar.xz)
  - GZIP (.gz)
  - BZIP2 (.bz2)
  - XZ (.xz)
  - LZMA (.lzma)

- 安全性保障：
  - 文件大小限制
  - 文件类型检查
  - 路径遍历防护
  - 文件数量限制

- 性能优化：
  - 支持并发解压
  - 可配置缓冲区大小
  - 内存优化处理
  - 大文件支持

- 监控和回调：
  - 解压进度回调
  - 性能指标收集
  - 错误处理和日志

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.yuxie.common.compress</groupId>
    <artifactId>file-unzip</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 基础使用

```java
// 创建配置
UnzipConfig config = UnzipConfig.builder()
    .maxFileSize(100 * 1024 * 1024)  // 100MB
    .bufferSize(8192)                 // 8KB buffer
    .enableProgressCallback(true)     // 启用进度回调
    .build();

// 创建服务
UnzipService unzipService = new UnzipService(
    new DefaultUnzipStrategyFactory(),
    config,
    new UnzipMetrics()
);

// 创建进度回调
UnzipProgressCallback callback = new UnzipProgressCallback() {
    @Override
    public void onStart(long totalSize, int totalFiles) {
        System.out.println("开始解压，总大小：" + totalSize + "字节，文件数：" + totalFiles);
    }

    @Override
    public void onProgress(String fileName, long bytesRead, long totalSize, int currentFile, int totalFiles) {
        System.out.printf("正在解压：%s (%d/%d) - %.2f%%\n", 
            fileName, currentFile, totalFiles, 
            (bytesRead * 100.0) / totalSize);
    }

    @Override
    public void onComplete() {
        System.out.println("解压完成");
    }

    @Override
    public void onError(String message) {
        System.err.println("解压错误：" + message);
    }
};

// 解压文件
try {
    // 从字节数组解压
    byte[] data = Files.readAllBytes(Paths.get("test.zip"));
    Map<FileInfo, byte[]> result = unzipService.unzip(data, callback);

    // 从输入流解压到指定目录
    try (InputStream inputStream = Files.newInputStream(Paths.get("test.zip"))) {
        Map<FileInfo, byte[]> files = unzipService.unzip(
            inputStream,
            "password",  // 可选密码
            callback,    // 可选回调
            "output"     // 输出目录
        );
    }
} catch (UnzipException e) {
    System.err.println("解压失败：" + e.getMessage());
}
```

### 高级配置

```java
// 创建自定义配置
UnzipConfig config = UnzipConfig.builder()
    // 文件限制
    .maxFileSize(200 * 1024 * 1024)    // 200MB
    .maxFileCount(5000)                 // 最多5000个文件
    .allowedFileTypes(Set.of("txt", "pdf", "doc", "docx"))  // 允许的文件类型
    
    // 性能配置
    .bufferSize(16384)                  // 16KB buffer
    .enableConcurrentUnzip(true)        // 启用并发解压
    .concurrentThreads(4)               // 4个并发线程
    .unzipTimeout(300000)               // 5分钟超时
    
    // 安全检查
    .enablePathTraversalCheck(true)     // 启用路径遍历检查
    .enableFileTypeCheck(true)          // 启用文件类型检查
    .enableFileSizeCheck(true)          // 启用文件大小检查
    .enableFileCountCheck(true)         // 启用文件数量检查
    
    // 其他功能
    .enableProgressCallback(true)       // 启用进度回调
    .enableChecksumValidation(true)     // 启用校验和验证
    .enableVirusScan(true)              // 启用病毒扫描
    .build();
```

## 项目结构

```
src/main/java/com/yuxie/common/compress/
├── callback/          # 进度回调接口
├── config/           # 配置类
├── exception/        # 异常类
├── format/          # 压缩格式检测
├── model/           # 数据模型
├── monitor/         # 监控指标
├── service/         # 核心服务
├── strategy/        # 解压策略
└── util/            # 工具类
```

## 注意事项

1. 内存使用：
   - 默认使用内存解压，适合中小文件
   - 大文件建议使用流式解压或指定输出目录

2. 安全性：
   - 建议启用所有安全检查
   - 注意设置合理的文件大小和数量限制
   - 谨慎处理用户输入的文件路径

3. 性能：
   - 根据实际需求调整缓冲区大小
   - 大文件处理时注意内存使用
   - 并发解压可以提高性能

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 许可证

本项目采用 Apache 2.0 许可证。详见 [LICENSE](LICENSE) 文件。

## 版权声明

Copyright 2024 Yuxie

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 