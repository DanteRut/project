package ru.rutmiit.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        // Используем абсолютный путь из описания
        this.fileStorageLocation = Paths.get("C:\\Users\\Hiper\\Documents\\4 курс\\Web\\project\\src\\main\\resources\\static\\files")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Директория для файлов создана: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось создать директорию для файлов", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Недопустимое имя файла: " + originalFileName);
            }

            // Генерируем уникальное имя файла
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Файл сохранен: {} -> {}", originalFileName, targetLocation);
            return uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Не удалось сохранить файл: " + originalFileName, ex);
        }
    }

    public Path getFilePath(String fileName) {
        return fileStorageLocation.resolve(fileName);
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            Files.deleteIfExists(filePath);
            log.info("Файл удален: {}", filePath);
        } catch (IOException ex) {
            log.error("Не удалось удалить файл: {}", fileName, ex);
        }
    }
}