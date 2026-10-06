package com.campussphere.auth.controller;

import com.campussphere.auth.dto.UserProfileDTO;
import com.campussphere.auth.dto.UserRegisterDTO;
import com.campussphere.auth.service.UserService;
import com.campussphere.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for the Authentication module.
 * Login itself is not handled here - it goes through Spring Security's
 * form-login filter chain (see SecurityConfig), which intercepts POST
 * /login automatically. This controller only exposes registration and
 * a convenience endpoint for retrieving the currently logged-in user.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileDTO>> register(@Valid @RequestBody UserRegisterDTO request) {
        UserProfileDTO created = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", created));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> currentUser(Authentication authentication) {
        UserProfileDTO profile = userService.getProfileByEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved", profile));
    }
}
