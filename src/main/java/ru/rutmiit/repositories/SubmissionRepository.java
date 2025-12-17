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

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String> {

    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);

    List<Submission> findByAssignment(Assignment assignment);

    List<Submission> findByStudent(User student);

    Page<Submission> findByStudent(User student, Pageable pageable);

    List<Submission> findByAssignmentAndScoreIsNotNull(Assignment assignment);

    @Query("SELECT s FROM Submission s WHERE s.assignment.teacher = :teacher")
    List<Submission> findByTeacher(@Param("teacher") User teacher);

    @Query("SELECT s FROM Submission s WHERE s.assignment.teacher = :teacher AND s.score IS NULL")
    List<Submission> findUncheckedByTeacher(@Param("teacher") User teacher);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.student = :student AND s.score IS NOT NULL")
    Double findAverageScoreByStudent(@Param("student") User student);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment = :assignment AND s.isLate = true")
    long countLateSubmissionsByAssignment(@Param("assignment") Assignment assignment);
}