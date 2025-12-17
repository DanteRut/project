package ru.rutmiit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SubmitAssignmentDto {

    @NotBlank(message = "Комментарий не может быть пустым!")
    private String comment;

    @NotNull(message = "Файл обязателен!")
    private MultipartFile file;

}