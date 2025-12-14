package ru.rutmiit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.AssignmentStatus;
import ru.rutmiit.models.enums.UserRole;
import ru.rutmiit.repositories.AssignmentRepository;
import ru.rutmiit.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class Init implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;
    private final AssignmentRepository assignmentRepository;

    public Init(UserRepository userRepository,
                PasswordEncoder passwordEncoder,
                @Value("${app.default.password}") String defaultPassword, AssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
        this.assignmentRepository = assignmentRepository;
        log.info("Init компонент инициализирован");
        log.info("Пароль по умолчанию: {}", defaultPassword);
    }

    @Override
    public void run(String... args) {
        log.info("Запуск инициализации начальных данных");
        initUsers();
        initializeAssignments();
        log.info("Инициализация начальных данных завершена");
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("Создание пользователей по умолчанию...");
            initAdmin();
            initTeachers();
            initStudents();
            log.info("Пользователи по умолчанию созданы");

            // Проверка паролей
            checkPasswords();
        } else {
            log.debug("Пользователи уже существуют, пропуск инициализации");
        }
    }

    private void initAdmin() {
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        log.info("Хеш пароля для admin: {}", encodedPassword);

        var adminUser = User.builder()
                .username("admin")
                .password(encodedPassword)
                .email("admin@rutmiit.ru")
                .fullName("Admin Adminovich")
                .age(30)
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(adminUser);
        log.info("Создан администратор: admin");
    }

    private void initTeachers() {
        List<Object[]> teachers = List.of(
                new Object[]{"ivanov", "Иванов Иван Иванович", "ivanov@rutmiit.ru", 40},
                new Object[]{"petrova", "Петрова Мария Сергеевна", "petrova@rutmiit.ru", 38},
                new Object[]{"sidorov", "Сидоров Алексей Петрович", "sidorov@rutmiit.ru", 42}
        );

        teachers.forEach(teacherData -> {
            String encodedPassword = passwordEncoder.encode(defaultPassword);
            log.info("Хеш пароля для {}: {}", teacherData[0], encodedPassword);

            User teacher = User.builder()
                    .username((String) teacherData[0])
                    .password(encodedPassword)
                    .email((String) teacherData[2])
                    .fullName((String) teacherData[1])
                    .age((Integer) teacherData[3])
                    .role(UserRole.TEACHER)
                    .build();
            userRepository.save(teacher);
            log.info("Создан преподаватель: {}", teacherData[0]);
        });
    }

    private void initStudents() {
        List<Object[]> students = List.of(
                new Object[]{"student1", "Студентов Андрей Владимирович", "student1@rutmiit.ru", 22},
                new Object[]{"student2", "Ученикова Екатерина Дмитриевна", "student2@rutmiit.ru", 21},
                new Object[]{"student3", "Обученко Максим Олегович", "student3@rutmiit.ru", 23},
                new Object[]{"student4", "Лабораторная Анна Сергеевна", "student4@rutmiit.ru", 20},
                new Object[]{"student5", "Зачетов Денис Игоревич", "student5@rutmiit.ru", 24}
        );

        students.forEach(studentData -> {
            String encodedPassword = passwordEncoder.encode(defaultPassword);
            log.info("Хеш пароля для {}: {}", studentData[0], encodedPassword);

            User student = User.builder()
                    .username((String) studentData[0])
                    .password(encodedPassword)
                    .email((String) studentData[2])
                    .fullName((String) studentData[1])
                    .age((Integer) studentData[3])
                    .role(UserRole.STUDENT)
                    .build();
            userRepository.save(student);
            log.info("Создан студент: {}", studentData[0]);
        });
    }

    private void checkPasswords() {
        log.info("=== ПРОВЕРКА ПАРОЛЕЙ ===");
        log.info("Пароль для проверки: {}", defaultPassword);

        userRepository.findAll().forEach(user -> {
            boolean matches = passwordEncoder.matches(defaultPassword, user.getPassword());
            log.info("Пользователь: {}, пароль совпадает: {}", user.getUsername(), matches);
        });
    }

    private void initializeAssignments() {
        log.info("Создание тестовых заданий...");

        // Получаем преподавателей и студентов
        List<User> teachers = userRepository.findByRole(UserRole.TEACHER);
        List<User> students = userRepository.findByRole(UserRole.STUDENT);

        if (teachers.isEmpty() || students.isEmpty()) {
            log.warn("Нет преподавателей или студентов для создания заданий");
            return;
        }

        User teacher1 = teachers.get(0);
        User teacher2 = teachers.size() > 1 ? teachers.get(1) : teachers.get(0);

        // Задание 1: Математика
        Assignment mathAssignment = Assignment.builder()
                .title("Домашнее задание по математике")
                .description("Решить задачи по дифференциальным уравнениям. Главы 1-3 учебника.")
                .criteria("""
                        1. Корректность решения (0-2 балла)
                        2. Полнота ответа (0-2 балла)
                        3. Аккуратность оформления (0-1 балл)
                        """)
                .deadline(LocalDateTime.now().plusDays(7)) // Через 7 дней
                .status(AssignmentStatus.ACTIVE)
                .teacher(teacher1)
                .assignedStudents(Arrays.asList(students.get(0), students.get(1), students.get(2)))
                .build();

        // Задание 2: Программирование
        Assignment programmingAssignment = Assignment.builder()
                .title("Лабораторная работа по Java")
                .description("Разработать REST API для системы управления заданиями. Использовать Spring Boot.")
                .criteria("""
                        1. Корректность архитектуры (0-3 балла)
                        2. Качество кода (0-2 балла)
                        3. Полнота реализации (0-3 балла)
                        4. Тестирование (0-2 балла)
                        """)
                .deadline(LocalDateTime.now().plusDays(14)) // Через 14 дней
                .status(AssignmentStatus.ACTIVE)
                .teacher(teacher2)
                .assignedStudents(Arrays.asList(students.get(1), students.get(2), students.get(3)))
                .build();

        // Задание 3: Базы данных
        Assignment dbAssignment = Assignment.builder()
                .title("Проектирование базы данных")
                .description("Спроектировать схему БД для интернет-магазина. Написать SQL-запросы.")
                .criteria("""
                        1. Нормализация БД (0-3 балла)
                        2. Оптимальность запросов (0-2 балла)
                        3. Документация (0-2 балла)
                        """)
                .deadline(LocalDateTime.now().plusDays(10)) // Через 10 дней
                .status(AssignmentStatus.ACTIVE)
                .teacher(teacher1)
                .assignedStudents(students) // Все студенты
                .build();

        // Задание 4: Просроченное задание
        Assignment expiredAssignment = Assignment.builder()
                .title("Эссе по философии")
                .description("Написать эссе на тему 'Этика в современном мире'")
                .criteria("""
                        1. Глубина раскрытия темы (0-3 балла)
                        2. Структура работы (0-2 балла)
                        3. Оригинальность мысли (0-3 балла)
                        """)
                .deadline(LocalDateTime.now().minusDays(5)) // Просрочено на 5 дней
                .status(AssignmentStatus.EXPIRED)
                .teacher(teacher2)
                .assignedStudents(Arrays.asList(students.get(0), students.get(3)))
                .build();

        // Задание 5: Завершенное задание
        Assignment completedAssignment = Assignment.builder()
                .title("Презентация по маркетингу")
                .description("Подготовить презентацию маркетинговой стратегии для стартапа")
                .criteria("""
                        1. Качество слайдов (0-2 балла)
                        2. Содержательность (0-3 балла)
                        3. Ораторское мастерство (0-2 балла)
                        """)
                .deadline(LocalDateTime.now().minusDays(2)) // Дедлайн 2 дня назад
                .status(AssignmentStatus.COMPLETED)
                .teacher(teacher1)
                .assignedStudents(Arrays.asList(students.get(1), students.get(2)))
                .build();

        // Задание 6: Срочное задание
        Assignment urgentAssignment = Assignment.builder()
                .title("Тест по английскому языку")
                .description("Пройдите онлайн-тест по грамматике английского языка")
                .criteria("""
                        1. Правильность ответов (1 балл за каждый правильный)
                        2. Скорость выполнения (0-5 баллов)
                        """)
                .deadline(LocalDateTime.now().plusHours(24)) // Через 24 часа
                .status(AssignmentStatus.ACTIVE)
                .teacher(teacher2)
                .assignedStudents(Arrays.asList(students.get(0), students.get(1)))
                .build();

        List<Assignment> assignments = Arrays.asList(
                mathAssignment,
                programmingAssignment,
                dbAssignment,
                expiredAssignment,
                completedAssignment,
                urgentAssignment
        );

        assignmentRepository.saveAll(assignments);

        log.info("Создано {} тестовых заданий", assignments.size());
        log.info("Статистика:");
        log.info("  - Активных заданий: {}", assignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE).count());
        log.info("  - Просроченных: {}", assignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.EXPIRED).count());
        log.info("  - Завершенных: {}", assignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED).count());
    }
}