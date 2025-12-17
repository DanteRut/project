package ru.rutmiit.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rutmiit.dto.GradeSubmissionDto;
import ru.rutmiit.dto.SubmitAssignmentDto;
import ru.rutmiit.models.entities.Assignment;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.services.AssignmentService;
import ru.rutmiit.services.SubmissionService;
import ru.rutmiit.services.UserService;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final UserService userService;

    @GetMapping("/submit/{assignmentId}")
    public String showSubmitForm(@PathVariable String assignmentId,
                                 Principal principal,
                                 Model model) {
        log.debug("Форма сдачи задания: {}", assignmentId);

        try {
            Assignment assignment = assignmentService.getAssignmentById(assignmentId);
            User student = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

            // Проверяем доступ
            if (!student.isStudent() || !assignment.getGroup().equals(student.getGroup())) {
                return "redirect:/access-denied";
            }

            model.addAttribute("assignment", assignment);
            model.addAttribute("submissionModel", new SubmitAssignmentDto());
            return "submission-submit";
        } catch (Exception e) {
            log.error("Ошибка при отображении формы сдачи", e);
            model.addAttribute("errorMessage", "Задание не найдено");
            return "redirect:/assignments/my";
        }
    }

    @PostMapping("/submit/{assignmentId}")
    public String submitAssignment(@PathVariable String assignmentId,
                                   @Valid @ModelAttribute("submissionModel") SubmitAssignmentDto dto,
                                   BindingResult bindingResult,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        log.debug("Сдача задания: {}", assignmentId);

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при сдаче задания");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.submissionDto", bindingResult);
            redirectAttributes.addFlashAttribute("submissionDto", dto);
            return "redirect:/submissions/submit/" + assignmentId;
        }

        try {
            submissionService.submitAssignment(assignmentId, principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Задание успешно сдано!");
            return "redirect:/assignments/my";
        } catch (Exception e) {
            log.error("Ошибка при сдаче задания", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сдаче: " + e.getMessage());
            return "redirect:/submissions/submit/" + assignmentId;
        }
    }

    @GetMapping("/grade/{submissionId}")
    public String showGradeForm(@PathVariable String submissionId,
                                Principal principal,
                                Model model) {
        log.debug("Форма оценки: {}", submissionId);

        try {
            ru.rutmiit.models.entities.Submission submission = submissionService.getSubmissionById(submissionId);
            User teacher = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден"));

            // Проверяем права
            if (!teacher.isTeacher() || !submission.getAssignment().getTeacher().getId().equals(teacher.getId())) {
                return "redirect:/access-denied";
            }

            model.addAttribute("submission", submission);
            model.addAttribute("gradeDto", new GradeSubmissionDto());
            return "submission-grade";
        } catch (Exception e) {
            log.error("Ошибка при отображении формы оценки", e);
            model.addAttribute("errorMessage", "Сдача не найдена");
            return "redirect:/assignments/my";
        }
    }

    @PostMapping("/grade/{submissionId}")
    public String gradeSubmission(@PathVariable String submissionId,
                                  @Valid @ModelAttribute("gradeDto") GradeSubmissionDto dto,
                                  BindingResult bindingResult,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        log.debug("Оценка сдачи: {}", submissionId);

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при оценке");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.gradeDto", bindingResult);
            redirectAttributes.addFlashAttribute("gradeDto", dto);
            return "redirect:/submissions/grade/" + submissionId;
        }

        try {
            submissionService.gradeSubmission(submissionId, principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Оценка выставлена!");
            return "redirect:/assignments/details/" +
                    submissionService.getSubmissionById(submissionId).getAssignment().getId();
        } catch (Exception e) {
            log.error("Ошибка при оценке", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при оценке: " + e.getMessage());
            return "redirect:/submissions/grade/" + submissionId;
        }
    }

    @GetMapping("/my")
    public String showMySubmissions(Principal principal, Model model) {
        log.debug("Мои сдачи: {}", principal.getName());

        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            if (user.isStudent()) {
                model.addAttribute("submissions", submissionService.getStudentSubmissions(user.getUsername()));
                model.addAttribute("isStudent", true);
            } else if (user.isTeacher()) {
                model.addAttribute("submissions", submissionService.getTeacherSubmissions(user.getUsername()));
                model.addAttribute("isTeacher", true);
            }

            return "submission-list";
        } catch (Exception e) {
            log.error("Ошибка при получении списка сдач", e);
            model.addAttribute("errorMessage", "Ошибка при загрузке данных");
            return "redirect:/";
        }
    }
}