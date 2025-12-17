package ru.rutmiit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.rutmiit.models.entities.User;
import ru.rutmiit.models.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    List<User> findByGroup(String group);


    @Query("SELECT DISTINCT u.group FROM User u WHERE u.group IS NOT NULL")
    List<String> findDistinctGroups();

    List<User> findByGroupAndRole(String group, UserRole role);
}