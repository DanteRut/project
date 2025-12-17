// SubmissionStatisticsDto.java
package ru.rutmiit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatisticsDto {
    private long totalSubmissions;
    private long pendingCount;
    private long gradedCount;
    private long lateCount;
    private double averageScore;
}