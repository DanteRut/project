// UserService.java (interface)
package ru.rutmiit.services;

import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllStudents();
    Optional<User> findByUsername(String username);
    List<User> findAllByIds(List<String> ids);
    User registerUser(String username, String password, String email, String fullName, UserRole role, String group);
    List<User> getAllTeachers();
    long countByRole(UserRole role);
    List<String> findGroups();
}