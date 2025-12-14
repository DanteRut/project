package ru.rutmiit.models.entities;

import jakarta.persistence.*;
import lombok.*;
import ru.rutmiit.models.enums.SubmissionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Submission extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(length = 5000)
    private String solutionText;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(length = 1000)
    private String comment;

    private Integer grade;

    @Column(name = "teacher_comment", length = 1000)
    private String teacherComment;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "submitted_at")
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
}