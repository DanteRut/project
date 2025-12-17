package ru.rutmiit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.rutmiit.models.entities.*;
import ru.rutmiit.models.enums.AssignmentStatus;
import ru.rutmiit.models.enums.UserRole;
import ru.rutmiit.repositories.*;

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
//    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final SubmissionRepository submissionRepository;

    public Init(UserRepository userRepository,
                PasswordEncoder passwordEncoder,
                @Value("${app.default.password}") String defaultPassword,
                AssignmentRepository assignmentRepository,
//                GroupRepository groupRepository,
                SubjectRepository subjectRepository,
                SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
        this.assignmentRepository = assignmentRepository;
//        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.submissionRepository = submissionRepository;
        log.info("Init компонент инициализирован");
        log.info("Пароль по умолчанию: {}", defaultPassword);
    }

    @Override
    public void run(String... args) {
        log.info("Запуск инициализации начальных данных");
//        initGroups();
        initSubjects();
        initUsers();
        initAssignments();
        log.info("Инициализация начальных данных завершена");
    }

//    private void initGroups() {
//        if (groupRepository.count() == 0) {
//            log.info("Создание групп...");
//
//            List<String> groups = Arrays.asList(
//                    "УВП-312", "УВП-313", "УВП-314",
//                    "ИТ-411", "ИТ-412",
//                    "КН-211", "КН-212"
//            );
//
//            for (String group : groups) {
//
//                groupRepository.save(group);
//                log.info("Создана группа: {}", group);
//            }
//        } else {
//            log.debug("Группы уже существуют, пропуск инициализации");
//        }
//    }

    private void initSubjects() {
        if (subjectRepository.count() == 0) {
            log.info("Создание предметов...");

            List<String> subjectNames = Arrays.asList(
                    "Математика",
                    "Программирование",
                    "Базы данных",
                    "Веб-технологии",
                    "Алгоритмы и структуры данных",
                    "Операционные системы",
                    "Компьютерные сети"
            );

            for (String subjectName : subjectNames) {
                Subject subject = Subject.builder()
                        .name(subjectName)
                        .build();
                subjectRepository.save(subject);
                log.info("Создан предмет: {}", subjectName);
            }
        } else {
            log.debug("Предметы уже существуют, пропуск инициализации");
        }
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("Создание пользователей по умолчанию...");

            // Получаем группы для студентов
            String group1 = "УВП-312";
            String group2 = "УВП-313";

            initAdmin();
            initTeachers();
            initStudents(group1, group2);
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
                .username("admin@rutmiit.ru")
                .email("admin@rutmiit.ru")
                .password(encodedPassword)
                .fullName("Admin Adminovich")
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(adminUser);
        log.info("Создан администратор: admin@rutmiit.ru");
    }

    private void initTeachers() {
        List<Object[]> teachers = Arrays.asList(
                new Object[]{"ivanov@rutmiit.ru", "Иванов Иван Иванович", UserRole.TEACHER},
                new Object[]{"petrova@rutmiit.ru", "Петрова Мария Сергеевна", UserRole.TEACHER},
                new Object[]{"sidorov@rutmiit.ru", "Сидоров Алексей Петрович", UserRole.TEACHER}
        );

        teachers.forEach(teacherData -> {
            String encodedPassword = passwordEncoder.encode(defaultPassword);
            log.info("Хеш пароля для {}: {}", teacherData[0], encodedPassword);

            User teacher = User.builder()
                    .username((String) teacherData[0])
                    .email((String) teacherData[0])
                    .password(encodedPassword)
                    .fullName((String) teacherData[1])
                    .role((UserRole) teacherData[2])
                    .build();
            userRepository.save(teacher);
            log.info("Создан преподаватель: {}", teacherData[0]);
        });
    }

    private void initStudents(String group1, String group2) {
        List<Object[]> students = Arrays.asList(
                new Object[]{"student1@rutmiit.ru", "Студентов Андрей Владимирович", group1},
                new Object[]{"student2@rutmiit.ru", "Ученикова Екатерина Дмитриевна", group1},
                new Object[]{"student3@rutmiit.ru", "Обученко Максим Олегович", group1},
                new Object[]{"student4@rutmiit.ru", "Лабораторная Анна Сергеевна", group2},
                new Object[]{"student5@rutmiit.ru", "Зачетов Денис Игоревич", group2},
                new Object[]{"student6@rutmiit.ru", "Экзаменова Ольга Петровна", group2}
        );

        students.forEach(studentData -> {
            String encodedPassword = passwordEncoder.encode(defaultPassword);
            log.info("Хеш пароля для {}: {}", studentData[0], encodedPassword);

            User student = User.builder()
                    .username((String) studentData[0])
                    .email((String) studentData[0])
                    .password(encodedPassword)
                    .fullName((String) studentData[1])
                    .role(UserRole.STUDENT)
                    .group((String) studentData[2])
                    .build();
            userRepository.save(student);
            log.info("Создан студент: {}", studentData[0]);
        });
    }

    private void initAssignments() {
        if (assignmentRepository.count() == 0) {
            log.info("Создание тестовых заданий...");

            // Получаем необходимые данные
            Subject mathSubject = subjectRepository.findByName("Математика").orElseThrow();
            Subject programmingSubject = subjectRepository.findByName("Программирование").orElseThrow();
            Subject dbSubject = subjectRepository.findByName("Базы данных").orElseThrow();

            String group1 = "УВП-312";
            String group2 = "УВП-313";

            User teacher1 = userRepository.findByEmail("ivanov@rutmiit.ru").orElseThrow();
            User teacher2 = userRepository.findByEmail("petrova@rutmiit.ru").orElseThrow();

            // Задание 1: Математика
            Assignment mathAssignment = Assignment.builder()
                    .subject(mathSubject)
                    .teacher(teacher1)
                    .group(group1)
                    .title("Домашнее задание по математике")
                    .description("Решить задачи по дифференциальным уравнениям. Главы 1-3 учебника.")
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .deadline(LocalDateTime.now().plusDays(7))
                    .maxScore(100)
                    .status(AssignmentStatus.ACTIVE)
                    .build();

            // Задание 2: Программирование
            Assignment programmingAssignment = Assignment.builder()
                    .subject(programmingSubject)
                    .teacher(teacher2)
                    .group(group1)
                    .title("Лабораторная работа по Java")
                    .description("Разработать REST API для системы управления заданиями. Использовать Spring Boot.")
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .deadline(LocalDateTime.now().plusDays(14))
                    .maxScore(100)
                    .status(AssignmentStatus.ACTIVE)
                    .build();

            // Задание 3: Базы данных
            Assignment dbAssignment = Assignment.builder()
                    .subject(dbSubject)
                    .teacher(teacher1)
                    .group(group2)
                    .title("Проектирование базы данных")
                    .description("Спроектировать схему БД для интернет-магазина. Написать SQL-запросы.")
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .deadline(LocalDateTime.now().plusDays(10))
                    .maxScore(100)
                    .status(AssignmentStatus.ACTIVE)
                    .build();

            // Задание 4: Просроченное задание
            Assignment expiredAssignment = Assignment.builder()
                    .subject(mathSubject)
                    .teacher(teacher2)
                    .group(group2)
                    .title("Эссе по философии")
                    .description("Написать эссе на тему 'Этика в современном мире'")
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .deadline(LocalDateTime.now().minusDays(5))
                    .maxScore(50)
                    .status(AssignmentStatus.EXPIRED)
                    .build();

            // Задание 5: Веб-технологии
            Subject webSubject = subjectRepository.findByName("Веб-технологии").orElseThrow();
            Assignment webAssignment = Assignment.builder()
                    .subject(webSubject)
                    .teacher(teacher1)
                    .group(group1)
                    .title("Создание веб-приложения")
                    .description("Разработать полноценное веб-приложение с использованием Spring MVC и Thymeleaf")
                    .createdAt(LocalDateTime.now())
                    .deadline(LocalDateTime.now().plusDays(21))
                    .maxScore(150)
                    .status(AssignmentStatus.ACTIVE)
                    .build();

            List<Assignment> assignments = Arrays.asList(
                    mathAssignment,
                    programmingAssignment,
                    dbAssignment,
                    expiredAssignment,
                    webAssignment
            );

            assignmentRepository.saveAll(assignments);

            // Создаем тестовые сдачи заданий
            createTestSubmissions(assignments, group1, group2);

            log.info("Создано {} тестовых заданий", assignments.size());
            log.info("Статистика:");
            log.info("  - Активных заданий: {}", assignments.stream()
                    .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE).count());
            log.info("  - Просроченных: {}", assignments.stream()
                    .filter(a -> a.getStatus() == AssignmentStatus.EXPIRED).count());
        } else {
            log.debug("Задания уже существуют, пропуск инициализации");
        }
    }

    private void createTestSubmissions(List<Assignment> assignments, String group1, String group2) {
        log.info("Создание тестовых сдач заданий...");

        // Получаем студентов из групп
        List<User> studentsGroup1 = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT && u.getGroup() != null && u.getGroup().equals(group1))
                .toList();

        List<User> studentsGroup2 = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT && u.getGroup() != null && u.getGroup().equals(group2))
                .toList();

        // Создаем сдачи для каждого задания
        for (Assignment assignment : assignments) {
            List<User> students;
            if (assignment.getGroup().equals(group1)) {
                students = studentsGroup1;
            } else {
                students = studentsGroup2;
            }

            // Для каждого студента создаем сдачу (кроме просроченного задания)
            for (User student : students) {
                if (assignment.getStatus() != AssignmentStatus.EXPIRED && Math.random() > 0.3) {
                    Submission submission = Submission.builder()
                            .assignment(assignment)
                            .student(student)
                            .filePath("test_file.txt")
                            .submittedAt(LocalDateTime.now().minusDays((int)(Math.random() * 3)))
                            .isLate(assignment.getDeadline().isBefore(LocalDateTime.now()))
                            .feedback("Тестовый комментарий студента")
                            .build();

                    // Для некоторых сдач добавляем оценку
                    if (Math.random() > 0.5) {
                        submission.setScore((int)(Math.random() * assignment.getMaxScore()));
                        submission.setFeedback("Хорошая работа, но есть замечания");
                        submission.setGradedBy(assignment.getTeacher());
                        submission.setGradedAt(LocalDateTime.now());
                    }

                    submissionRepository.save(submission);
                    log.debug("Создана сдача для студента {} по заданию {}", student.getEmail(), assignment.getTitle());
                }
            }
        }

        log.info("Тестовые сдачи созданы");
    }

    private void checkPasswords() {
        log.info("=== ПРОВЕРКА ПАРОЛЕЙ ===");
        log.info("Пароль для проверки: {}", defaultPassword);

        userRepository.findAll().forEach(user -> {
            boolean matches = passwordEncoder.matches(defaultPassword, user.getPassword());
            log.info("Пользователь: {}, пароль совпадает: {}", user.getEmail(), matches);
        });
    }
}