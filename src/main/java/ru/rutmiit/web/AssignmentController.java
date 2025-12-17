package ru.rutmiit.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rutmiit.dto.CreateAssignmentDto;
import ru.rutmiit.dto.ShowAssignmentDto;
import ru.rutmiit.models.entities.*;
import ru.rutmiit.services.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService;
//    private final GroupService groupService;
    private final SubjectService subjectService;

    @GetMapping("/add")
    public String showAddAssignmentForm(Model model, Principal principal) {
        log.debug("Отображение формы создания задания");

        User teacher = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

        if (!teacher.isTeacher()) {
            return "redirect:/access-denied";
        }

        List<String> groups = userService.findGroups();
        List<Subject> subjects = subjectService.getAllSubjects();

        model.addAttribute("groups", groups);
        model.addAttribute("subjects", subjects);
        model.addAttribute("assignmentDto", new CreateAssignmentDto());

        return "assignment-add";
    }

    @PostMapping("/add")
    public String addAssignment(@Valid @ModelAttribute("assignmentDto") CreateAssignmentDto dto,
                                BindingResult bindingResult,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        log.debug("Обработка создания задания");

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.assignmentDto", bindingResult);
            redirectAttributes.addFlashAttribute("assignmentDto", dto);
            return "redirect:/assignments/add";
        }

        try {
            Assignment assignment = assignmentService.createAssignment(dto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Задание '%s' успешно создано для группы %s",
                            assignment.getTitle(),
                            assignment.getGroup()));
            return "redirect:/assignments/my";
        } catch (Exception e) {
            log.error("Ошибка при создании задания", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании задания: " + e.getMessage());
            return "redirect:/assignments/add";
        }
    }

    @GetMapping("/my")
    public String showMyAssignments(Principal principal, Model model) {
        log.debug("Отображение заданий пользователя: {}", principal.getName());

        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        List<ShowAssignmentDto> assignments;

        if (user.isStudent()) {
            List<Assignment> studentAssignments = assignmentService.getAssignmentsForStudent(user);
            assignments = studentAssignments.stream()
                    .map(assignmentService::convertToShowDto)
                    .toList();
            model.addAttribute("isStudent", true);
        } else if (user.isTeacher()) {
            List<Assignment> teacherAssignments = assignmentService.getAssignmentsForTeacher(user);
            assignments = teacherAssignments.stream()
                    .map(assignmentService::convertToShowDto)
                    .toList();
            model.addAttribute("isTeacher", true);
        } else {
            return "redirect:/access-denied";
        }

        model.addAttribute("assignments", assignments);
        return "assignment-my";
    }

    @GetMapping("/details/{id}")
    public String assignmentDetails(@PathVariable String id, Principal principal, Model model) {
        log.debug("Детали задания: {}", id);

        try {
            Assignment assignment = assignmentService.getAssignmentById(id);
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            // Проверяем доступ
            if (user.isStudent() && !assignment.getGroup().equals(user.getGroup())) {
                return "redirect:/access-denied";
            }
            if (user.isTeacher() && !assignment.getTeacher().getId().equals(user.getId())) {
                return "redirect:/access-denied";
            }

            List<Submission> submissions = assignment.getSubmissions();
            long gradedCount = submissions != null ?
                    submissions.stream().filter(s -> s.getScore() != null).count() : 0;
            long lateCount = submissions != null ?
                    submissions.stream().filter(s -> Boolean.TRUE.equals(s.getIsLate())).count() : 0;
            double averageScore = submissions != null && gradedCount > 0 ?
                    submissions.stream()
                            .filter(s -> s.getScore() != null)
                            .mapToInt(Submission::getScore)
                            .average()
                            .orElse(0.0) : 0.0;

            model.addAttribute("now", LocalDateTime.now());
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissions", assignment.getSubmissions());
            model.addAttribute("gradedCount", gradedCount);
            model.addAttribute("lateCount", lateCount);
            model.addAttribute("averageScore", String.format("%.1f", averageScore));
            return "assignment-details";
        } catch (Exception e) {
            log.error("Ошибка при получении задания", e);
            model.addAttribute("errorMessage", "Задание не найдено");
            return "redirect:/assignments/my";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteAssignment(@PathVariable String id,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        log.debug("Удаление задания: {}", id);

        try {
            Assignment assignment = assignmentService.getAssignmentById(id);
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            // Проверяем права
            if (!user.isTeacher() || !assignment.getTeacher().getId().equals(user.getId())) {
                return "redirect:/access-denied";
            }

            assignmentService.deleteAssignment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Задание успешно удалено");
        } catch (Exception e) {
            log.error("Ошибка при удалении задания", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении задания");
        }

        return "redirect:/assignments/my";
    }
}