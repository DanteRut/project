package ru.rutmiit.models.enums;

public enum UserRole {
    ADMIN("Администратор"),
    TEACHER("Преподаватель"),
    STUDENT("Студент");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoleName() {
        return "ROLE_" + this.name(); // Добавляем префикс ROLE_
    }
}