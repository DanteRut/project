package ru.rutmiit.models.entities;

import jakarta.persistence.*;
import lombok.*;
import ru.rutmiit.models.enums.UserRole;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Assignment> createdAssignments = new ArrayList<>();

    @ManyToMany(mappedBy = "assignedStudents")
    @Builder.Default
    private List<Assignment> assignedAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();

    // Дополнительные методы для удобства
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isTeacher() {
        return role == UserRole.TEACHER;
    }

    public boolean isStudent() {
        return role == UserRole.STUDENT;
    }

    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : "";
    }

    // Метод для получения роли с префиксом ROLE_
    public String getRoleWithPrefix() {
        return role != null ? "ROLE_" + role.name() : "";
    }
}