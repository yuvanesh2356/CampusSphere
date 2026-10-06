package com.campussphere.auth.repository;

import com.campussphere.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for the User entity. Spring Data JPA generates the
 * implementation at runtime from these method signatures - no
 * manual SQL or implementation class required.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
