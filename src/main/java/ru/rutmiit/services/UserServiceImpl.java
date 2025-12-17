package ru.rutmiit.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.UserRole;
import ru.rutmiit.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllStudents() {
        return userRepository.findByRole(UserRole.STUDENT);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ИЗМЕНЕНО: String вместо Long
    public List<User> findAllByIds(List<String> ids) {
        return userRepository.findAllById(ids);
    }

    @Transactional
    public User registerUser(String username, String password, String email, String fullName, UserRole role, String group) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName)
                .role(role)
                .group(group)
                .build();

        return userRepository.save(user);
    }

    public List<User> getAllTeachers() {
        return userRepository.findByRole(UserRole.TEACHER);
    }

    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    public List<String> findGroups(){
        return userRepository.findDistinctGroups();
    }
}