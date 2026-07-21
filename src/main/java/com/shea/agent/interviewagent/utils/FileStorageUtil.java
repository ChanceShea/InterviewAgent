package com.shea.agent.interviewagent.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author : Shea.
 * @since : 2026/7/20 18:20
 */
@Slf4j
public class FileStorageUtil {

    public static final String FILE_DIR;

    static {
        String fileDir1;
        try {
            // 获取 resources 目录下的 tmp/resume 文件夹
            ClassPathResource resource = new ClassPathResource("tmp/resume");
            File file = resource.getFile();
            fileDir1 = file.getAbsolutePath();
        } catch (IOException e) {
            // 如果文件夹不存在，使用相对路径
            fileDir1 = System.getProperty("user.dir") + File.separator + "src/main/resources/tmp/resume";
        }
        FILE_DIR = fileDir1;
    }

    public static String saveTempFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID() + extension;
        // 创建完整路径
        Path tempFilePath = Paths.get(FILE_DIR, filename);
        Files.createDirectories(tempFilePath.getParent());
        file.transferTo(tempFilePath.toFile());
        String absolutePath = tempFilePath.toAbsolutePath().toString();
        log.info("文件已保存到临时路径: {}, 原始文件名: {}, 大小: {} bytes",
                absolutePath, originalFilename, file.getSize());
        return absolutePath;
    }

    public static boolean deleteTempFile(String fileName) {
        String filePath = FILE_DIR + File.separator + fileName;
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
