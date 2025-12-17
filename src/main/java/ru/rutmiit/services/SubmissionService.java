// SubmissionService.java (interface)
package ru.rutmiit.services;

import ru.rutmiit.dto.GradeSubmissionDto;
import ru.rutmiit.dto.SubmissionStatisticsDto;
import ru.rutmiit.dto.SubmitAssignmentDto;
import ru.rutmiit.models.entities.Submission;

import java.util.List;

public interface SubmissionService {
    Submission submitAssignment(String assignmentId, String studentUsername, SubmitAssignmentDto dto);
    Submission gradeSubmission(String submissionId, String teacherUsername, GradeSubmissionDto dto);
    List<Submission> getSubmissionsForAssignment(String assignmentId);
    List<Submission> getStudentSubmissions(String studentUsername);
    List<Submission> getTeacherSubmissions(String teacherUsername);
    List<Submission> getUncheckedSubmissionsForTeacher(String teacherUsername);
    Submission getSubmissionById(String id);
    SubmissionStatisticsDto getSubmissionStatistics(String assignmentId);
}