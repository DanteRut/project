package ru.rutmiit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.Submission;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.SubmissionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String> {

    List<Submission> findByAssignment(Assignment assignment);

    Page<Submission> findByAssignment(Assignment assignment, Pageable pageable);

    List<Submission> findByStudent(User student);

    Page<Submission> findByStudent(User student, Pageable pageable);

    List<Submission> findByAssignmentAndStudent(Assignment assignment, User student);

    // ИСПРАВЛЕНО: Используем правильные имена полей
    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.student.id = :studentId")
    Optional<Submission> findByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId,
                                                        @Param("studentId") Long studentId);

    List<Submission> findByStatus(SubmissionStatus status);

    @Query("SELECT s FROM Submission s WHERE s.assignment.teacher = :teacher")
    List<Submission> findByTeacher(@Param("teacher") User teacher);

    @Query("SELECT s FROM Submission s WHERE s.assignment.teacher = :teacher AND s.status = :status")
    List<Submission> findByTeacherAndStatus(@Param("teacher") User teacher,
                                            @Param("status") SubmissionStatus status);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment = :assignment AND s.status = 'GRADED'")
    long countGradedSubmissionsByAssignment(@Param("assignment") Assignment assignment);

    @Query("SELECT s FROM Submission s WHERE s.grade IS NULL AND s.status = 'SUBMITTED'")
    List<Submission> findUngradedSubmissions();

    @Query("SELECT s FROM Submission s WHERE s.grade IS NOT NULL AND s.student = :student")
    List<Submission> findGradedSubmissionsByStudent(@Param("student") User student);

    @Query("SELECT AVG(s.grade) FROM Submission s WHERE s.student = :student AND s.grade IS NOT NULL")
    Double findAverageGradeByStudent(@Param("student") User student);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment = :assignment")
    long countSubmissionsByAssignment(@Param("assignment") Assignment assignment);

    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId ORDER BY s.submittedAt DESC")
    List<Submission> findByAssignmentIdOrderByDate(@Param("assignmentId") Long assignmentId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.student = :student AND s.status = 'LATE'")
    long countLateSubmissionsByStudent(@Param("student") User student);

    @Query("SELECT s FROM Submission s WHERE s.assignment.status = 'ACTIVE' AND s.status = 'SUBMITTED'")
    List<Submission> findActiveSubmittedSubmissions();

    @Query("SELECT s FROM Submission s WHERE s.grade >= :minGrade AND s.grade <= :maxGrade")
    List<Submission> findSubmissionsByGradeRange(@Param("minGrade") Integer minGrade,
                                                 @Param("maxGrade") Integer maxGrade);
}