package ru.rutmiit.services;

import jakarta.persistence.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.rutmiit.dto.GradeSubmissionDto;
import ru.rutmiit.dto.SubmissionStatisticsDto;
import ru.rutmiit.dto.SubmitAssignmentDto;
import ru.rutmiit.models.entities.*;
import ru.rutmiit.models.exceptions.AssignmentNotFoundException;
import ru.rutmiit.repositories.AssignmentRepository;
import ru.rutmiit.repositories.SubmissionRepository;
import ru.rutmiit.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Submission submitAssignment(String assignmentId, String studentUsername, SubmitAssignmentDto dto) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Задание не найдено"));

        User student = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

        // Проверяем, что студент в нужной группе
        if (!assignment.getGroup().equals(student.getGroup())) {
            throw new IllegalArgumentException("Это задание не для вашей группы");
        }

        // Проверяем, что задание еще не сдано
        submissionRepository.findByAssignmentAndStudent(assignment, student)
                .ifPresent(s -> {
                    throw new IllegalArgumentException("Вы уже сдали это задание");
                });

        // Проверяем дедлайн
        boolean isLate = LocalDateTime.now().isAfter(assignment.getDeadline());

        // Сохраняем файл
        MultipartFile file = dto.getFile();
        String storedFileName = fileStorageService.storeFile(file);

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .filePath(storedFileName)
                .submittedAt(LocalDateTime.now())
                .isLate(isLate)
                .feedback(dto.getComment())
                .build();

        Submission savedSubmission = submissionRepository.save(submission);
        log.info("Студент {} сдал задание: {}", studentUsername, assignment.getTitle());

        return savedSubmission;
    }

    @CacheEvict(value = {"submissions", "assignments"}, allEntries = true)
    @Override
    @Transactional
    public Submission gradeSubmission(String submissionId, String teacherUsername, GradeSubmissionDto dto) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Сдача не найдена"));

        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

        // Проверяем, что преподаватель является автором задания
        if (!submission.getAssignment().getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Вы не можете оценивать это задание");
        }

        // Проверяем, что оценка не превышает максимальный балл
        if (dto.getScore() > submission.getAssignment().getMaxScore()) {
            throw new IllegalArgumentException(
                    String.format("Оценка не может превышать %d баллов",
                            submission.getAssignment().getMaxScore())
            );
        }

        submission.setScore(dto.getScore());
        submission.setFeedback(dto.getFeedback());
        submission.setGradedBy(teacher);
        submission.setGradedAt(LocalDateTime.now());

        Submission savedSubmission = submissionRepository.save(submission);
        log.info("Оценено задание ID: {}, оценка: {}", submissionId, dto.getScore());

        return savedSubmission;
    }

    public List<Submission> getSubmissionsForAssignment(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Задание не найдено"));
        return submissionRepository.findByAssignment(assignment);
    }

    public List<Submission> getStudentSubmissions(String studentUsername) {
        User student = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        return submissionRepository.findByStudent(student);
    }

    public List<Submission> getTeacherSubmissions(String teacherUsername) {
        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));
        return submissionRepository.findByTeacher(teacher);
    }

    public List<Submission> getUncheckedSubmissionsForTeacher(String teacherUsername) {
        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));
        return submissionRepository.findUncheckedByTeacher(teacher);
    }


//    @Cacheable(value = "submissions", key = "#id")
    @Override
    public Submission getSubmissionById(String id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Сдача не найдена"));
    }

    @Override
    public SubmissionStatisticsDto getSubmissionStatistics(String assignmentId) {
        return null;
    }
}