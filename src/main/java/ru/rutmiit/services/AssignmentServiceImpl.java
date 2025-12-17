package ru.rutmiit.services;

import jakarta.persistence.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.rutmiit.dto.AssignmentStatisticsDto;
import ru.rutmiit.dto.CreateAssignmentDto;
import ru.rutmiit.dto.ShowAssignmentDto;
import ru.rutmiit.models.entities.*;
import ru.rutmiit.models.enums.AssignmentStatus;
import ru.rutmiit.models.exceptions.AssignmentNotFoundException;
import ru.rutmiit.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService{

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
//    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final FileStorageService fileStorageService;

    @CacheEvict(value = "assignments", key = "#result.id")
    @Override
    @Transactional
    public Assignment createAssignment(CreateAssignmentDto dto, String teacherUsername) {
        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException("Только преподаватели могут создавать задания");
        }

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));

//        String group = assignmentRepository.findByGroup(dto.getGroup())
//                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));

        Assignment assignment = Assignment.builder()
                .subject(subject)
                .teacher(teacher)
                .group(dto.getGroup())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .createdAt(LocalDateTime.now())
                .deadline(dto.getDeadline())
                .maxScore(dto.getMaxScore())
                .status(AssignmentStatus.ACTIVE)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Создано задание: '{}' для группы {}", dto.getTitle(), dto.getGroup());

        // Сохраняем файлы задания
        if (dto.getFiles() != null) {
            for (MultipartFile file : dto.getFiles()) {
                if (!file.isEmpty()) {
                    try {
                        String storedFileName = fileStorageService.storeFile(file);

                        AssignmentFile assignmentFile = AssignmentFile.builder()
                                .assignment(savedAssignment)
                                .filePath(storedFileName)
                                .fileName(file.getOriginalFilename())
                                .build();

                        assignmentFileRepository.save(assignmentFile);
                        log.info("Файл задания сохранен: {}", file.getOriginalFilename());
                    } catch (Exception e) {
                        log.error("Ошибка при сохранении файла задания: {}", file.getOriginalFilename(), e);
                    }
                }
            }
        }

        return savedAssignment;
    }

//    @Cacheable(value = "assignments", key = "#id")
    @Override
    public Assignment getAssignmentById(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Задание не найдено"));

        // Инициализируем ленивые коллекции
        Hibernate.initialize(assignment.getAssignmentFiles());
        Hibernate.initialize(assignment.getSubmissions());

        // Если нужно инициализировать вложенные объекты
        if (assignment.getSubmissions() != null) {
            for (Submission submission : assignment.getSubmissions()) {
                Hibernate.initialize(submission.getStudent());
            }
        }

        return assignment;
//        return assignmentRepository.findById(id)
//                .orElseThrow(() -> new AssignmentNotFoundException("Задание не найдено"));
    }

    public List<Assignment> getAssignmentsForGroup(String group) {
//        String group = groupRepository.findById(group)
//                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена"));
//        return assignmentRepository.findByGroup(group);
        return assignmentRepository.findBygroup(group);
    }

    public List<Assignment> getAssignmentsForStudent(User student) {
        if (student.getGroup() == null) {
            throw new IllegalArgumentException("Студент не принадлежит к группе");
        }
        return assignmentRepository.findBygroup(student.getGroup());
    }

    public List<Assignment> getAssignmentsForTeacher(User teacher) {
        return assignmentRepository.findByTeacher(teacher);
    }

    @CacheEvict(value = "assignments", key = "#id")
    @Override
    @Transactional
    public void deleteAssignment(String id) {
        Assignment assignment = getAssignmentById(id);

        // Удаляем файлы задания
        for (AssignmentFile file : assignment.getAssignmentFiles()) {
            fileStorageService.deleteFile(file.getFilePath());
        }

        assignmentRepository.delete(assignment);
        log.info("Удалено задание: {}", assignment.getTitle());
    }

    public List<Assignment> findExpiredAssignments() {
        return assignmentRepository.findExpiredAssignments(LocalDateTime.now());
    }

    @Transactional
    public void markAssignmentAsExpired(String id) {
        Assignment assignment = getAssignmentById(id);
        if (assignment.getStatus() == AssignmentStatus.ACTIVE &&
                assignment.getDeadline().isBefore(LocalDateTime.now())) {
            assignment.setStatus(AssignmentStatus.EXPIRED);
            assignmentRepository.save(assignment);
        }
    }

    public Page<Assignment> getAllAssignmentsPaginated(Pageable pageable) {
        return assignmentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public ShowAssignmentDto convertToShowDto(Assignment assignment) {
        ShowAssignmentDto dto = new ShowAssignmentDto();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDeadline(assignment.getDeadline());
        dto.setStatus(assignment.getStatus().toString());
        dto.setTeacherName(assignment.getTeacher().getFullName());

        // УБРАТЬ или закомментировать строку ниже
        // dto.setStudentCount(assignment.getGroup().getStudents().size());

        // Вместо этого можно просто поставить 0 или получить через отдельный запрос
        dto.setStudentCount(0);

        return dto;
    }

    // В AssignmentServiceImpl.java добавляем методы:
    @Override
    public AssignmentStatisticsDto getAssignmentStatistics(String assignmentId) {
        Assignment assignment = getAssignmentById(assignmentId);
        List<Submission> submissions = assignment.getSubmissions();

        if (submissions == null || submissions.isEmpty()) {
            return AssignmentStatisticsDto.builder()
                    .totalSubmissions(0)
                    .gradedCount(0)
                    .lateCount(0)
                    .averageScore(0.0)
                    .completionRate(0.0)
                    .build();
        }

        long total = submissions.size();
        long graded = submissions.stream().filter(s -> s.getScore() != null).count();
        long late = submissions.stream().filter(s -> Boolean.TRUE.equals(s.getIsLate())).count();

        double average = graded > 0 ?
                submissions.stream()
                        .filter(s -> s.getScore() != null)
                        .mapToInt(Submission::getScore)
                        .average()
                        .orElse(0.0) : 0.0;

        double completionRate = total > 0 ? (graded * 100.0 / total) : 0.0;

        return AssignmentStatisticsDto.builder()
                .totalSubmissions(total)
                .gradedCount(graded)
                .lateCount(late)
                .averageScore(average)
                .completionRate(completionRate)
                .build();
    }

//    @Override
//    public AssignmentViewDto convertToViewDto(Assignment assignment) {
//        AssignmentViewDto dto = new AssignmentViewDto();
//        dto.setId(assignment.getId());
//        dto.setTitle(assignment.getTitle());
//        dto.setDescription(assignment.getDescription());
//        dto.setTeacherName(assignment.getTeacher().getFullName());
//        dto.setTeacherEmail(assignment.getTeacher().getEmail());
//        dto.setGroup(assignment.getGroup());
//        dto.setStatus(assignment.getStatus().toString());
//        dto.setDeadline(assignment.getDeadline());
//        dto.setCreatedAt(assignment.getCreatedAt());
//        dto.setMaxScore(assignment.getMaxScore());
//        dto.setSubjectName(assignment.getSubject().getName());
//        return dto;
//    }
}