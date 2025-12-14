package ru.rutmiit.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rutmiit.dto.UserRegistrationDto;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.UserRole;
import ru.rutmiit.repositories.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(UserRegistrationDto registrationDTO) {
        // Проверка совпадения паролей
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new RuntimeException("Пароли не совпадают");
        }

        // Проверка уникальности email
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email уже используется");
        }

        // Проверка уникальности username
        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Имя пользователя уже используется");
        }

        // Создание пользователя с ролью STUDENT по умолчанию
        User user = User.builder()
                .username(registrationDTO.getUsername())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .email(registrationDTO.getEmail())
                .fullName(registrationDTO.getFullname())
                .age(registrationDTO.getAge())
                .role(UserRole.STUDENT) // По умолчанию STUDENT
                .build();

        userRepository.save(user);
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " was not found!"));
    }
}