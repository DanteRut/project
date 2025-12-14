package ru.rutmiit.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rutmiit.dto.GradeSubmissionDto;
import ru.rutmiit.dto.SubmitAssignmentDto;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.Submission;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.SubmissionStatus;
import ru.rutmiit.models.exceptions.AssignmentNotFoundException;
import ru.rutmiit.repositories.AssignmentRepository;
import ru.rutmiit.repositories.SubmissionRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserService userService;
    
    @Transactional
    public void submitAssignment(String assignmentId, String studentUsername, SubmitAssignmentDto submissionDto) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Задание не найдено"));
        
        User student = userService.findByUsername(studentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        
        // Проверяем, назначено ли задание студенту
        if (!assignment.getAssignedStudents().contains(student)) {
            throw new IllegalArgumentException("Это задание не назначено вам");
        }
        
        // Проверяем дедлайн
        SubmissionStatus status = LocalDateTime.now().isAfter(assignment.getDeadline())
            ? SubmissionStatus.LATE 
            : SubmissionStatus.SUBMITTED;
        
        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .solutionText(submissionDto.getSolutionText())
                .comment(submissionDto.getComment())
                .attachmentUrl(submissionDto.getAttachmentUrl())
                .status(status)
                .build();
        
        submissionRepository.save(submission);
        log.info("Студент {} сдал задание: {}", studentUsername, assignment.getTitle());
    }
    
    @Transactional
    public void gradeSubmission(String submissionId, GradeSubmissionDto gradeDto) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Сдача не найдена"));
        
        submission.setGrade(gradeDto.getGrade());
        submission.setTeacherComment(gradeDto.getTeacherComment());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        
        submissionRepository.save(submission);
        log.info("Оценено задание ID: {}, оценка: {}", submissionId, gradeDto.getGrade());
    }
    
    public Object getAssignmentForSubmission(String assignmentId, String studentUsername) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Задание не найдено"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", assignment.getId());
        result.put("title", assignment.getTitle());
        result.put("description", assignment.getDescription());
        result.put("criteria", assignment.getCriteria());
        result.put("deadline", assignment.getDeadline());
        
        return result;
    }
    
    public Object getStudentGrades(String studentUsername) {
        User student = userService.findByUsername(studentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        
        List<Submission> submissions = submissionRepository.findByStudent(student);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalAssignments", submissions.size());
        
        long completed = submissions.stream()
                .filter(s -> s.getGrade() != null)
                .count();
        result.put("completedAssignments", completed);
        
        double average = submissions.stream()
                .filter(s -> s.getGrade() != null)
                .mapToInt(Submission::getGrade)
                .average()
                .orElse(0.0);
        result.put("averageGrade", String.format("%.1f", average));
        
        result.put("submissions", submissions);
        
        return result;
    }
}