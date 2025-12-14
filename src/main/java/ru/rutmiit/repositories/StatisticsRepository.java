package ru.rutmiit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rutmiit.models.entities.Assignment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface StatisticsRepository extends JpaRepository<Assignment, String> {
    
    @Query("SELECT a.status as status, COUNT(a) as count FROM Assignment a GROUP BY a.status")
    List<Map<String, Object>> countAssignmentsByStatus();
    
    @Query("SELECT FUNCTION('DATE', a.createdAt) as date, COUNT(a) as count " +
           "FROM Assignment a " +
           "WHERE a.createdAt >= :startDate " +
           "GROUP BY FUNCTION('DATE', a.createdAt) " +
           "ORDER BY date")
    List<Map<String, Object>> countAssignmentsByDate(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a.teacher.fullName as teacher, COUNT(a) as assignmentCount " +
           "FROM Assignment a " +
           "GROUP BY a.teacher " +
           "ORDER BY assignmentCount DESC")
    List<Map<String, Object>> countAssignmentsByTeacher();
    
    @Query("SELECT s.student.fullName as student, AVG(s.grade) as avgGrade " +
           "FROM Submission s " +
           "WHERE s.grade IS NOT NULL " +
           "GROUP BY s.student " +
           "ORDER BY avgGrade DESC")
    List<Map<String, Object>> getAverageGradesByStudent();
    
    @Query("SELECT a.title as assignment, AVG(s.grade) as avgGrade, COUNT(s) as submissionCount " +
           "FROM Submission s JOIN s.assignment a " +
           "WHERE s.grade IS NOT NULL " +
           "GROUP BY a " +
           "ORDER BY avgGrade DESC")
    List<Map<String, Object>> getAverageGradesByAssignment();
    
    @Query("SELECT MONTH(s.submittedAt) as month, COUNT(s) as submissionCount " +
           "FROM Submission s " +
           "WHERE YEAR(s.submittedAt) = YEAR(CURRENT_DATE) " +
           "GROUP BY MONTH(s.submittedAt) " +
           "ORDER BY month")
    List<Map<String, Object>> getSubmissionsByMonth();
    
    @Query("SELECT " +
           "SUM(CASE WHEN s.status = 'GRADED' THEN 1 ELSE 0 END) as graded, " +
           "SUM(CASE WHEN s.status = 'SUBMITTED' THEN 1 ELSE 0 END) as submitted, " +
           "SUM(CASE WHEN s.status = 'LATE' THEN 1 ELSE 0 END) as late, " +
           "SUM(CASE WHEN s.status = 'REJECTED' THEN 1 ELSE 0 END) as rejected " +
           "FROM Submission s")
    Map<String, Long> getSubmissionStatistics();
    
    @Query("SELECT " +
           "SUM(CASE WHEN a.status = 'ACTIVE' THEN 1 ELSE 0 END) as active, " +
           "SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
           "SUM(CASE WHEN a.status = 'EXPIRED' THEN 1 ELSE 0 END) as expired " +
           "FROM Assignment a")
    Map<String, Long> getAssignmentStatistics();
}