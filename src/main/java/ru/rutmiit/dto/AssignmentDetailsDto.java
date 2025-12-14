package ru.rutmiit.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignmentDetailsDto {
    private String id;
    private String title;
    private String description;
    private String criteria;
    private LocalDateTime deadline;
    private String status;
    private String teacherName;
    private List<StudentInfoDto> assignedStudents;
    private LocalDateTime createdAt;
}