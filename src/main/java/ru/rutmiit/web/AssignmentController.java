package ru.rutmiit.web;

import ru.rutmiit.dto.CreateAssignmentDto;
import ru.rutmiit.dto.ShowAssignmentDto;
import ru.rutmiit.models.exceptions.AssignmentNotFoundException;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.services.AssignmentService;
import ru.rutmiit.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService;

    // Список всех заданий с пагинацией и поиском
    @GetMapping("/all")
    public String showAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(required = false) String search,
            Model model) {

        log.debug("Отображение списка заданий: страница={}, размер={}, поиск={}", page, size, search);

        if (search != null && !search.trim().isEmpty()) {
            // Режим поиска
            List<ShowAssignmentDto> assignments = assignmentService.searchAssignments(search);
            model.addAttribute("assignments", assignments);
            model.addAttribute("search", search);
        } else {
            // Режим пагинации
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            Page<ShowAssignmentDto> assignmentPage = assignmentService.getAllAssignmentsPaginated(pageable);

            model.addAttribute("assignments", assignmentPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", assignmentPage.getTotalPages());
            model.addAttribute("totalItems", assignmentPage.getTotalElements());
        }

        return "assignment-list";
    }

    // Детальная информация о задании
    @GetMapping("/details/{id}")
    public String assignmentDetails(@PathVariable("id") String id, Model model) {
        log.debug("Запрос деталей задания: {}", id);
        try {
            Object assignmentDetails = assignmentService.getAssignmentDetails(id);
            model.addAttribute("assignment", assignmentDetails);
            return "assignment-details";
        } catch (AssignmentNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/assignments/all";
        }
    }

    // Форма добавления задания
    @GetMapping("/add")
    public String showAddAssignmentForm(Model model, Principal principal) {
        log.debug("Отображение формы добавления задания");

        // Загружаем список студентов для выбора
        List<User> students = userService.getAllStudents();
        model.addAttribute("students", students);

        // ВАЖНО: Добавить пустой объект DTO в модель
        model.addAttribute("assignmentModel", new CreateAssignmentDto());

        return "assignment-add";
    }

    // Обработка формы добавления задания
    @PostMapping("/add")
    public String addAssignment(@Valid @ModelAttribute("assignmentModel") CreateAssignmentDto assignmentDto,
                                BindingResult bindingResult,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        log.debug("Обработка POST запроса на добавление задания");

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("assignmentModel", assignmentDto);
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.assignmentModel",
                    bindingResult);
            return "redirect:/assignments/add";
        }

        try {
            assignmentService.createAssignment(assignmentDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Задание '" + assignmentDto.getTitle() + "' успешно создано!");
        } catch (Exception e) {
            log.error("Ошибка при создании задания", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании задания: " + e.getMessage());
            return "redirect:/assignments/add";
        }

        return "redirect:/assignments/all";
    }

    // Удаление задания
    @GetMapping("/delete/{id}")
    public String deleteAssignment(@PathVariable("id") String id,
                                   RedirectAttributes redirectAttributes) {
        log.debug("Запрос на удаление задания: {}", id);

        try {
            assignmentService.deleteAssignment(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Задание успешно удалено!");
        } catch (AssignmentNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/assignments/all";
    }

    // Страница для студентов - мои задания
    @GetMapping("/my")
    public String showMyAssignments(Principal principal, Model model,
                                    @RequestParam(defaultValue = "0") int page) {
        log.debug("Отображение заданий студента: {}", principal.getName());

        Pageable pageable = PageRequest.of(page, 10);
        Page<ShowAssignmentDto> myAssignments = assignmentService.getAssignmentsForStudent(principal.getName(), pageable);

        model.addAttribute("assignments", myAssignments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", myAssignments.getTotalPages());

        return "assignment-my";
    }
}