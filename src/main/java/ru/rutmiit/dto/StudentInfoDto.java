package ru.rutmiit.dto;

import lombok.Data;

@Data
public class StudentInfoDto {
    private String id;
    private String fullName;
    private String username;
    private String submissionStatus;
    private Integer grade;
}