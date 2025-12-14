package ru.rutmiit.web;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import ru.rutmiit.dto.SubmitAssignmentDto;
import ru.rutmiit.services.FileStorageService;
import ru.rutmiit.services.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rutmiit.dto.SubmitAssignmentDto;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    
    private final SubmissionService submissionService;
    private final FileStorageService fileStorageService;
    
    // Форма сдачи задания
    @GetMapping("/submit/{assignmentId}")
    public String showSubmitForm(@PathVariable("assignmentId") String assignmentId,
                                Model model,
                                Principal principal) {
        log.debug("Отображение формы сдачи задания: {}", assignmentId);
        
        try {
            Object assignmentInfo = submissionService.getAssignmentForSubmission(assignmentId, principal.getName());
            model.addAttribute("assignment", assignmentInfo);
            model.addAttribute("submissionModel", new SubmitAssignmentDto());
            return "submission-submit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/assignments/my";
        }
    }
    
    // Обработка сдачи задания
    @PostMapping("/submit/{assignmentId}")
    public String submitAssignment(
            @PathVariable String assignmentId,
            @Valid SubmitAssignmentDto submitDto,
            BindingResult bindingResult,
            @RequestParam(value = "attachment", required = false) MultipartFile file, // Добавьте этот параметр
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            log.error("Ошибки валидации при сдаче задания: {}", bindingResult.getAllErrors());
            bindingResult.getAllErrors().forEach(error ->
                    log.error("Поле: {}, Ошибка: {}",
                            ((FieldError) error).getField(), error.getDefaultMessage()));
            return "redirect:/submissions/submit/" + assignmentId;
        }

        // Сохраните файл
        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.storeFile(file);
            submitDto.setAttachmentUrl(fileUrl);
        }
        try {
            submissionService.submitAssignment(assignmentId, principal.getName(), submitDto);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Задание успешно сдано!");
        } catch (Exception e) {
            log.error("Ошибка при сдаче задания", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Ошибка при сдаче задания: " + e.getMessage());
        }
        
        return "redirect:/assignments/my";
    }
    
    // Просмотр оценок
    @GetMapping("/grades")
    public String viewGrades(Principal principal, Model model) {
        log.debug("Отображение оценок студента: {}", principal.getName());
        
        try {
            Object grades = submissionService.getStudentGrades(principal.getName());
            model.addAttribute("grades", grades);
            return "submission-grades";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при загрузке оценок");
            return "redirect:/assignments/my";
        }
    }
}