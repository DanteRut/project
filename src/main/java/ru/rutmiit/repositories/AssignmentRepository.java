package ru.rutmiit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.Subject;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.AssignmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    List<Assignment> findBygroup(String group);

    List<Assignment> findByTeacher(User teacher);

    List<Assignment> findBySubject(Subject subject);

    Page<Assignment> findByGroup(String group, Pageable pageable);

    Page<Assignment> findByTeacher(User teacher, Pageable pageable);

    List<Assignment> findByGroupAndStatus(String group, AssignmentStatus status);

    List<Assignment> findByTeacherAndStatus(User teacher, AssignmentStatus status);

    @Query("SELECT a FROM Assignment a WHERE a.group = :group AND a.deadline > :now AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsForGroup(@Param("group") String group, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Assignment a WHERE a.deadline < :now AND a.status = 'ACTIVE'")
    List<Assignment> findExpiredAssignments(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.teacher = :teacher")
    long countByTeacher(@Param("teacher") User teacher);

    @Query("SELECT a FROM Assignment a WHERE a.group = :group AND a.deadline BETWEEN :start AND :end")
    List<Assignment> findByGroupAndDeadlineBetween(
            @Param("group") String group,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}