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

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "group_name", length = 7)
    private String group;

    // Связи только в одну сторону для избежания циклических зависимостей
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Assignment> createdAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "gradedBy", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Submission> gradedSubmissions = new ArrayList<>();

    // Дополнительные методы
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
}