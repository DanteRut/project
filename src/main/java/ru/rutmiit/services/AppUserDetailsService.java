package ru.rutmiit.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.rutmiit.repositories.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Попытка загрузки пользователя: {}", username);

        return userRepository.findByUsername(username)
                .map(u -> {
                    log.info("Пользователь найден в БД: {}", u.getUsername());
                    log.info("Хеш пароля в БД: {}", u.getPassword());
                    log.info("Роль пользователя: {}", u.getRole());

                    return new User(
                            u.getUsername(),
                            u.getPassword(),
                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
                    );
                }).orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", username);
                    return new UsernameNotFoundException(username + " was not found!");
                });
    }
}