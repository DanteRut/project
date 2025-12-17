package ru.rutmiit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rutmiit.models.entities.AssignmentFile;

import java.util.List;

@Repository
public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, String> {
    List<AssignmentFile> findByAssignmentId(String assignmentId);
    void deleteByAssignmentId(String assignmentId);
}