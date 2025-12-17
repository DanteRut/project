package ru.rutmiit.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rutmiit.dto.UserRegistrationDto;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.services.AuthService;

import java.security.Principal;

@Slf4j
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
        log.info("AuthController инициализирован");
    }

    @ModelAttribute("userRegistrationDto")
    public UserRegistrationDto initForm() {
        return new UserRegistrationDto();
    }

    @GetMapping("/register")
    public String register() {
        log.debug("Отображение страницы регистрации");
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid UserRegistrationDto userRegistrationDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        log.debug("Обработка регистрации пользователя: {}", userRegistrationDto.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при регистрации: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("userRegistrationDto", userRegistrationDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegistrationDto", bindingResult);

            return "redirect:/register";
        }

        // Раскомментируйте если AuthService.register реализован
        this.authService.register(userRegistrationDto);
        log.info("Пользователь успешно зарегистрирован: {}", userRegistrationDto.getEmail());

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        log.debug("Отображение страницы входа");
        return "login";
    }

    @PostMapping("/login-error")
    public String onFailedLogin(
            @ModelAttribute(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY) String email,
            RedirectAttributes redirectAttributes) {

        log.warn("Неудачная попытка входа для пользователя: {}", email);
        redirectAttributes.addFlashAttribute(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY, email);
        redirectAttributes.addFlashAttribute("badCredentials", true);

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        String email = principal.getName();
        log.debug("Отображение профиля пользователя: {}", email);

        User user = authService.getUser(email);

        // Используем createdAt из BaseEntity
        ru.rutmiit.views.UserProfileView userProfileView = new ru.rutmiit.views.UserProfileView(
                email,
                user.getEmail(),
                user.getFullName(),
                user.getRole().getDisplayName(),  // Используем displayName
                user.getCreatedAt(),  // Поле из BaseEntity
                user.getGroup()
        );

        model.addAttribute("user", userProfileView);
//        model.getAttribute("role", )

        return "profile";
    }
}