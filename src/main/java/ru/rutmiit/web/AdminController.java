package ru.rutmiit.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    // Панель администратора
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        log.debug("Отображение панели администратора");
        return "admin-dashboard";
    }
    
    // Управление пользователями
    @GetMapping("/users")
    public String manageUsers(Model model) {
        log.debug("Управление пользователями");
        return "admin-users";
    }
    
    // Статистика системы
    @GetMapping("/statistics")
    public String systemStatistics(Model model) {
        log.debug("Статистика системы");
        return "admin-statistics";
    }
}