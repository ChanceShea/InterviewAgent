package com.shea.agent.interviewagent.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * @author : Shea.
 * @since : 2026/7/20 18:20
 */
@Slf4j
public class FileStorageUtil {

    public static final Path FILE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "interview-agent","uploads");
    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024; // 限制最大上传文件大小为10MB
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf");

    public static String saveTempFile(
            MultipartFile file,
            Set<String> allowedTypes,
            Set<String> allowedExtensions,
            long maxSize) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            allowedTypes = ALLOWED_TYPES;
        }
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            allowedExtensions = ALLOWED_EXTENSIONS;
        }
        if (maxSize <= 0) {
            maxSize = DEFAULT_MAX_SIZE;
        }
        validate(file,allowedExtensions,allowedTypes,maxSize);
        String filename = UUID.randomUUID() + getFileExtension(file.getOriginalFilename()).toLowerCase();
        Files.createDirectories(FILE_DIR);
        Path target = FILE_DIR.resolve(filename);
        file.transferTo(target.toFile());
        log.info("文件已保存到临时路径: {}, 原始文件名: {}, 大小: {} bytes",
                target, file.getOriginalFilename(), file.getSize());
        return target.toString();
    }

    private static void validate(
            MultipartFile file,
            Set<String> allowedExtensions,
            Set<String> allowedTypes,
            long maxSize
    ) throws IOException {
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制：" + (maxSize / 1024 / 1024) + "MB");
        }
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型：" + extension);
        }
        String contentType = file.getContentType();
        if (contentType != null && !allowedTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型：" + contentType);
        }
        // 校验文件魔数，防止伪装扩展名
        validatePdfMagic(file);
    }

    private static void validatePdfMagic(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        // 检查前 5 字节是否为 %PDF-（宽容：大小写不敏感）
        if (bytes.length < 5
                || bytes[0] != '%'
                || !isPdfPrefix(bytes)) {
            throw new IllegalArgumentException("文件内容不是有效的PDF格式，实际前几字节: "
                    + bytesToHex(bytes));
        }
    }

    private static boolean isPdfPrefix(byte[] bytes) {
        // %PDF-  |  %pdf-  （规范要求大写，但部分工具输出小写/混合）
        byte[] header = Arrays.copyOf(bytes,5);
        return new String(header, StandardCharsets.ISO_8859_1)
                .toLowerCase().startsWith("%pdf");
    }

    private static String bytesToHex(byte[] bytes) {
        int n = Math.min(8, bytes.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }


    public static boolean deleteTempFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("临时文件已删除：{}",filePath);
            } else {
                log.info("临时文件不存在：{}",filePath);
            }
            return deleted;
        }catch (IOException e) {
            log.warn("删除临时文件失败: {}, 错误: {}", filePath, e.getMessage());
            return false;
        }
    }

    private static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf > 0) {
            return filename.substring(lastIndexOf);
        }
        return "";
    }
}
