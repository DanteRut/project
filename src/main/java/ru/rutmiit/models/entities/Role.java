package ru.rutmiit.models.entities;

import jakarta.persistence.*;
import lombok.*;
import ru.rutmiit.models.enums.UserRole;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, unique = true)
    private UserRole userRole; // Поле называется userRole, не role

    @Column(length = 500)
    private String description;

    // Если нужно получить строковое представление
    public String getRoleName() {
        return this.userRole.name();
    }
}