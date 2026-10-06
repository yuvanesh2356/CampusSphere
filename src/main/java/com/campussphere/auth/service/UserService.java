package com.campussphere.auth.service;

import com.campussphere.auth.dto.ProfileUpdateDTO;
import com.campussphere.auth.dto.UserProfileDTO;
import com.campussphere.auth.dto.UserRegisterDTO;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.DuplicateResourceException;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.service.FileStorageService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for the Authentication & Student Profile module.
 * Controllers never talk to UserRepository directly - all rules
 * (duplicate-email checks, password hashing, default role assignment)
 * live here, in one place, per the Controller -> Service -> Repository
 * pattern followed throughout the project.
 */
@Service
public class UserService {

    private static final String UPLOAD_SUBDIRECTORY = "profile";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Registers a new student account.
     * - Rejects the request if the email is already registered.
     * - Hashes the raw password with BCrypt before persisting.
     * - Assigns the default STUDENT role.
     * - Marks the account verified=true immediately, since domain-based
     *   college email validation has already run at the DTO level
     *   (@ValidCollegeEmail). A confirmation-link flow can replace this
     *   in a later phase without changing this method's contract.
     */
    @Transactional
    public UserProfileDTO registerUser(UserRegisterDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getDepartment(),
                request.getYearOfStudy()
        );
        // Role is not set here - User.role already defaults to STUDENT.
        // This keeps the default owned by a single source of truth (the
        // entity), rather than duplicated in both the entity and this
        // service, which is important once Phase 5's Admin creation
        // logic needs to reason about role assignment correctly.
        user.setVerified(true);

        User saved = userRepository.save(user);
        return UserProfileDTO.fromEntity(saved);
    }

    public UserProfileDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
        return UserProfileDTO.fromEntity(user);
    }

    /**
     * Updates the editable profile fields for the currently authenticated
     * user. Email, password, and role are intentionally untouched here -
     * this method only ever operates on the account matching the given
     * email, so there is no cross-user ownership check to perform (unlike
     * the content modules, a profile can only ever be "your own").
     */
    @Transactional
    public UserProfileDTO updateProfile(String email, ProfileUpdateDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));

        user.setFullName(request.getFullName());
        user.setDepartment(request.getDepartment());
        user.setYearOfStudy(request.getYearOfStudy());
        user.setSkills(request.getSkills());
        user.setAboutMe(request.getAboutMe());
        user.setContactDetails(request.getContactDetails());

        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            fileStorageService.delete(user.getProfilePicturePath());
            String newPicturePath = fileStorageService.store(request.getProfilePicture(), UPLOAD_SUBDIRECTORY);
            user.setProfilePicturePath(newPicturePath);
        }

        User saved = userRepository.save(user);
        return UserProfileDTO.fromEntity(saved);
    }
}
