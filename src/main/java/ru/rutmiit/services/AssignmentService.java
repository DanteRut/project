// AssignmentService.java (interface)
package ru.rutmiit.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.rutmiit.dto.AssignmentStatisticsDto;
import ru.rutmiit.dto.CreateAssignmentDto;
import ru.rutmiit.dto.ShowAssignmentDto;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.Submission;
import ru.rutmiit.models.entities.User;

import java.util.List;

public interface AssignmentService {
    Assignment createAssignment(CreateAssignmentDto dto, String teacherUsername);
    Assignment getAssignmentById(String id);
    List<Assignment> getAssignmentsForGroup(String group);
    List<Assignment> getAssignmentsForStudent(User student);
    List<Assignment> getAssignmentsForTeacher(User teacher);
    void deleteAssignment(String id);
    List<Assignment> findExpiredAssignments();
    void markAssignmentAsExpired(String id);
    Page<Assignment> getAllAssignmentsPaginated(Pageable pageable);
    ShowAssignmentDto convertToShowDto(Assignment assignment);
    AssignmentStatisticsDto getAssignmentStatistics(String assignmentId);
}