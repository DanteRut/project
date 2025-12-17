package ru.rutmiit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GradeSubmissionDto {
    @NotNull(message = "Оценка обязательна!")
    @Min(value = 0, message = "Оценка должна быть не меньше 0")
    private Integer score;

    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String feedback;
}