package ru.rutmiit.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rutmiit.dto.*;
import ru.rutmiit.models.exceptions.AssignmentNotFoundException;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.repositories.AssignmentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Добавьте эту аннотацию
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public List<ShowAssignmentDto> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::convertToShowDto)
                .collect(Collectors.toList());
    }

    public Page<ShowAssignmentDto> getAllAssignmentsPaginated(Pageable pageable) {
        return assignmentRepository.findAll(pageable)
                .map(this::convertToShowDto);
    }

    public List<ShowAssignmentDto> searchAssignments(String searchTerm) {
        return assignmentRepository.findByTitleContainingIgnoreCase(searchTerm).stream()
                .map(this::convertToShowDto)
                .collect(Collectors.toList());
    }

    @Transactional  // Для методов, изменяющих данные
    public AssignmentDetailsDto getAssignmentDetails(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(
                        "Задание с ID '" + id + "' не найдено"));

        return convertToDetailsDto(assignment);
    }

    @Transactional
    public void createAssignment(CreateAssignmentDto assignmentDto, String teacherUsername) {
        User teacher = userService.findByUsername(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

        Assignment assignment = modelMapper.map(assignmentDto, Assignment.class);
        assignment.setTeacher(teacher);

        // Назначаем студентов
        List<User> students = userService.findAllByIds(assignmentDto.getStudentIds());
        assignment.setAssignedStudents(students);

        assignmentRepository.save(assignment);
        log.info("Создано новое задание: '{}' преподавателем: {}",
                assignment.getTitle(), teacher.getFullName());
    }

    @Transactional
    public void deleteAssignment(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(
                        "Задание с ID '" + id + "' не найдено"));

        assignmentRepository.delete(assignment);
        log.info("Удалено задание: '{}'", assignment.getTitle());
    }

    public Page<ShowAssignmentDto> getAssignmentsForStudent(String studentUsername, Pageable pageable) {
        User student = userService.findByUsername(studentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

        return assignmentRepository.findByAssignedStudentsContaining(student, pageable)
                .map(this::convertToShowDto);
    }

    private ShowAssignmentDto convertToShowDto(Assignment assignment) {
        ShowAssignmentDto dto = modelMapper.map(assignment, ShowAssignmentDto.class);
        dto.setTeacherName(assignment.getTeacher().getFullName());
        dto.setStudentCount(assignment.getAssignedStudents().size());  // Тут нужна открытая сессия!
        return dto;
    }

    private AssignmentDetailsDto convertToDetailsDto(Assignment assignment) {
        AssignmentDetailsDto dto = modelMapper.map(assignment, AssignmentDetailsDto.class);
        dto.setTeacherName(assignment.getTeacher().getFullName());

        // Конвертируем студентов
        List<StudentInfoDto> studentDtos = assignment.getAssignedStudents().stream()
                .map(student -> {
                    StudentInfoDto studentDto = new StudentInfoDto();
                    studentDto.setId(student.getId());
                    studentDto.setFullName(student.getFullName());
                    studentDto.setUsername(student.getUsername());
                    return studentDto;
                })
                .collect(Collectors.toList());

        dto.setAssignedStudents(studentDtos);
        return dto;
    }
}