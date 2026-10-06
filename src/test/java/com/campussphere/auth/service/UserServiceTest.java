package com.campussphere.auth.service;

import com.campussphere.auth.dto.ProfileUpdateDTO;
import com.campussphere.auth.dto.UserProfileDTO;
import com.campussphere.auth.dto.UserRegisterDTO;
import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.DuplicateResourceException;
import com.campussphere.common.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService's business logic, run against mocked
 * dependencies (no database or Spring context required, so these
 * run fast and don't need a live MySQL connection). Covers
 * registration (Phase 1) and profile updates (Final Phase).
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileStorageService fileStorageService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder, fileStorageService);
    }

    @Test
    void registerUser_savesHashedPasswordWithDefaultRoleAndVerifiedTrue() {
        UserRegisterDTO request = new UserRegisterDTO();
        request.setFullName("Asha Rao");
        request.setEmail("asha.rao@campus.edu.in");
        request.setPassword("plainPassword123");
        request.setDepartment("CSE");
        request.setYearOfStudy(2);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserProfileDTO result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("asha.rao@campus.edu.in", result.getEmail());
        assertEquals(Role.STUDENT, result.getRole());
        assertTrue(result.isVerified());

        verify(passwordEncoder, times(1)).encode("plainPassword123");
        verify(userRepository, times(1)).save(argThat(u -> "hashed-password".equals(u.getPassword())));
    }

    @Test
    void registerUser_throwsWhenEmailAlreadyExists() {
        UserRegisterDTO request = new UserRegisterDTO();
        request.setFullName("Duplicate Student");
        request.setEmail("existing@campus.edu.in");
        request.setPassword("plainPassword123");
        request.setDepartment("ECE");
        request.setYearOfStudy(3);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(request));

        // Must fail fast - no password hashing or save attempt for a duplicate email.
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getProfileByEmail_returnsProfileWhenUserExists() {
        User existing = new User("Rahul Dev", "rahul.dev@campus.edu.in", "hashed", "IT", 1);
        existing.setId(5L);

        when(userRepository.findByEmail("rahul.dev@campus.edu.in")).thenReturn(Optional.of(existing));

        UserProfileDTO result = userService.getProfileByEmail("rahul.dev@campus.edu.in");

        assertEquals(5L, result.getId());
        assertEquals("Rahul Dev", result.getFullName());
    }

    @Test
    void updateProfile_updatesEditableFieldsAndLeavesEmailUnchanged() {
        User existing = new User("Rahul Dev", "rahul.dev@campus.edu.in", "hashed", "IT", 1);
        existing.setId(5L);

        ProfileUpdateDTO request = new ProfileUpdateDTO();
        request.setFullName("Rahul D. Verma");
        request.setDepartment("CSE");
        request.setYearOfStudy(2);
        request.setSkills("Java, Spring Boot");
        request.setAboutMe("Aspiring backend developer");
        request.setContactDetails("+91-9000000000");

        when(userRepository.findByEmail("rahul.dev@campus.edu.in")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileDTO result = userService.updateProfile("rahul.dev@campus.edu.in", request);

        assertEquals("Rahul D. Verma", result.getFullName());
        assertEquals("CSE", result.getDepartment());
        assertEquals("Java, Spring Boot", result.getSkills());
        assertEquals("rahul.dev@campus.edu.in", result.getEmail());
        // No file was uploaded in this request, so FileStorageService must not be touched.
        verify(fileStorageService, never()).store(any(), anyString());
        verify(fileStorageService, never()).delete(anyString());
    }
}
