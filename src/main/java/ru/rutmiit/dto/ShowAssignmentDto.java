package ru.rutmiit.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ShowAssignmentDto {
    private String id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private String subject;
    private String teacherName;
    private String group;
    private int studentCount;
    
    public String getFormattedDeadline() {
        if (deadline == null) return "";
        return deadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}