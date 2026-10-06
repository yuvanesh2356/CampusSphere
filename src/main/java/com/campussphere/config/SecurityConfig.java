package com.campussphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central security configuration for CampusSphere.
 *
 * Uses session-based form login rather than JWT/stateless tokens - the
 * simpler, well-supported choice for a single server-rendered monolith
 * of this scope (see project blueprint, Section 5: Architecture Summary).
 *
 * @EnableMethodSecurity is turned on now so that later phases (Admin
 * Module) can use @PreAuthorize("hasRole('ADMIN')") directly on service
 * or controller methods without any further security configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * BCrypt is the industry-standard adaptive hashing algorithm for
     * passwords - used both when registering (UserService) and when
     * Spring Security verifies a submitted password at login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection stays ON by default for browser form submissions
            // (e.g. POST /login, POST /logout), which correctly receive a
            // Thymeleaf-injected token automatically.
            //
            // It is explicitly disabled only for the /api/** JSON layer.
            // These endpoints are called via fetch()/AJAX (see auth.js),
            // which does not carry the CSRF token cookie/header pair unless
            // manually wired in on every request. Without this exclusion,
            // every AJAX POST (e.g. registration) is rejected with 403
            // before it reaches the controller. Scoping the exclusion to
            // /api/** - rather than disabling CSRF globally - keeps the
            // protection intact for every session-based form in the app.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                // Public: landing page, registration, login page, static assets, registration API
                // /uploads/** added in Phase 2 - uploaded listing images are served as
                // static content, same treatment as /css/** and /js/**. Marketplace
                // pages themselves are NOT in this list, so they still fall through to
                // anyRequest().authenticated() below and require a logged-in session.
                .requestMatchers(
                        "/", "/register", "/login",
                        "/api/auth/register",
                        "/css/**", "/js/**", "/images/**", "/uploads/**"
                ).permitAll()
                // Reserved now, enforced from Phase 5 onward when the Admin Module is built
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                // Everything else requires an authenticated session
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
