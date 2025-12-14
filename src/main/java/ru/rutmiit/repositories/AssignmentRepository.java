package ru.rutmiit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    
    List<Assignment> findByTitleContainingIgnoreCase(String title);
    
    Page<Assignment> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    List<Assignment> findByTeacher(User teacher);
    
    Page<Assignment> findByTeacher(User teacher, Pageable pageable);
    
    List<Assignment> findByAssignedStudentsContaining(User student);
    
    Page<Assignment> findByAssignedStudentsContaining(User student, Pageable pageable);
    
    @Query("SELECT a FROM Assignment a WHERE a.deadline < :now AND a.status = 'ACTIVE'")
    List<Assignment> findExpiredAssignments(@Param("now") LocalDateTime now);
    
    List<Assignment> findByStatus(String status);
    
    @Query("SELECT a FROM Assignment a WHERE a.teacher = :teacher AND a.deadline < :now")
    List<Assignment> findOverdueAssignmentsByTeacher(@Param("teacher") User teacher, 
                                                    @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.teacher = :teacher")
    long countByTeacher(@Param("teacher") User teacher);
    
    @Query("SELECT a FROM Assignment a JOIN a.assignedStudents s WHERE s.id = :studentId")
    List<Assignment> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT a FROM Assignment a WHERE a.status = 'ACTIVE' ORDER BY a.deadline ASC")
    List<Assignment> findActiveAssignmentsOrderByDeadline();
    
    @Query("SELECT a FROM Assignment a WHERE " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "a.status = 'ACTIVE'")
    List<Assignment> searchActiveAssignments(@Param("search") String search);
    
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT a FROM Assignment a WHERE a.deadline BETWEEN :startDate AND :endDate")
    List<Assignment> findAssignmentsByDeadlineRange(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Assignment a WHERE a.teacher.id = :teacherId AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT a FROM Assignment a " +
           "LEFT JOIN a.submissions s " +
           "WHERE s IS NULL OR s.status != 'GRADED' " +
           "GROUP BY a " +
           "ORDER BY COUNT(s) ASC")
    List<Assignment> findAssignmentsWithUngradedSubmissions();
}