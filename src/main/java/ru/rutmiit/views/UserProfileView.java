package ru.rutmiit.views;

import lombok.Getter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class UserProfileView {
    private final String username;
    private final String email;
    private final String fullName;
    private final String role;
    private final String registrationDate;

    public UserProfileView(String username, String email, String fullName, String role, LocalDateTime registrationDate) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.registrationDate = registrationDate != null
                ? registrationDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                : "";
    }
}