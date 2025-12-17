// AssignmentController.java (исправленная версия)
package ru.rutmiit.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rutmiit.dto.AssignmentStatisticsDto;
import ru.rutmiit.dto.CreateAssignmentDto;
import ru.rutmiit.dto.ShowAssignmentDto;
import ru.rutmiit.models.entities.*;
import ru.rutmiit.services.AssignmentService;
import ru.rutmiit.services.SubjectService;
import ru.rutmiit.services.UserService;

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
    private final SubjectService subjectService;

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

            // Используем сервис для получения статистики
            AssignmentStatisticsDto statistics = assignmentService.getAssignmentStatistics(id);

            model.addAttribute("now", LocalDateTime.now());
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissions", assignment.getSubmissions());
            model.addAttribute("gradedCount", statistics.getGradedCount());
            model.addAttribute("lateCount", statistics.getLateCount());
            model.addAttribute("averageScore", String.format("%.1f", statistics.getAverageScore()));
            model.addAttribute("completionRate", String.format("%.1f%%", statistics.getCompletionRate()));

            return "assignment-details";
        } catch (Exception e) {
            log.error("Ошибка при получении задания", e);
            model.addAttribute("errorMessage", "Задание не найдено");
            return "redirect:/assignments/my";
        }
    }

    // остальные методы без изменений
}