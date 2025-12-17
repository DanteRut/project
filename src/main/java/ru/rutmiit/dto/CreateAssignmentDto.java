package ru.rutmiit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateAssignmentDto {
    @NotBlank(message = "Название задания не может быть пустым!")
    @Size(min = 5, max = 100, message = "Название должно быть от 5 до 100 символов!")
    private String title;

    @NotBlank(message = "Выберите предмет!")
    private String subjectId;

    @NotBlank(message = "Выберите группу!")
    private String group;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов!")
    private String description;

    @Future(message = "Дедлайн должен быть в будущем!")
    @NotNull(message = "Укажите дедлайн задания!")
    private LocalDateTime deadline;

    @Min(value = 1, message = "Максимальный балл должен быть не менее 1")
    @Max(value = 100, message = "Максимальный балл должен быть не более 100")
    private Integer maxScore = 100;

    private List<MultipartFile> files;
}