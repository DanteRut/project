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

    // УДАЛЕН неработающий метод getAverageGradesByAssignment()
    // ВАЖНО: В текущей сущности Submission нет поля grade, есть score.

    @Query("SELECT s.student.fullName as student, AVG(s.score) as avgScore " +
            "FROM Submission s " +
            "WHERE s.score IS NOT NULL " +
            "GROUP BY s.student " +
            "ORDER BY avgScore DESC")
    List<Map<String, Object>> getAverageScoresByStudent();

    // УДАЛЕН неработающий метод getSubmissionStatistics()
    // Нет сущности Submission для агрегации по статусам в этой форме.

    @Query("SELECT " +
            "SUM(CASE WHEN a.status = 'ACTIVE' THEN 1 ELSE 0 END) as active, " +
            "SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN a.status = 'EXPIRED' THEN 1 ELSE 0 END) as expired " +
            "FROM Assignment a")
    Map<String, Long> getAssignmentStatistics();
}