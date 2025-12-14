package ru.rutmiit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateAssignmentDto {
    @NotBlank(message = "Название задания не может быть пустым!")
    @Size(min = 5, max = 100, message = "Название должно быть от 5 до 100 символов!")
    private String title;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов!")
    private String description;

    @Size(max = 500, message = "Критерии оценки не должны превышать 500 символов!")
    private String criteria;

    @Future(message = "Дедлайн должен быть в будущем!")
    @NotNull(message = "Укажите дедлайн задания!")
    private LocalDateTime deadline;

    @NotEmpty(message = "Выберите хотя бы одного студента!")
    private List<String> studentIds;  // ИЗМЕНЕНО: String вместо Long/UUID
}