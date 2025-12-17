// AssignmentStatisticsDto.java
package ru.rutmiit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentStatisticsDto {
    private long totalSubmissions;
    private long gradedCount;
    private long lateCount;
    private double averageScore;
    private double completionRate;
}