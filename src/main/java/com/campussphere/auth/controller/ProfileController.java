package com.campussphere.auth.controller;

import com.campussphere.auth.dto.ProfileUpdateDTO;
import com.campussphere.auth.dto.UserProfileDTO;
import com.campussphere.auth.service.UserService;
import com.campussphere.common.exception.InvalidFileException;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Serves the View Profile and Edit Profile pages, and handles the
 * profile update form submission. Structured the same way as every
 * content module's controller: a server-rendered MVC controller with
 * business exceptions caught locally and converted into a redirect +
 * flash message, since GlobalExceptionHandler is scoped to the
 * JSON-only /api/** layer.
 *
 * Kept separate from AuthController (registration/login API) and
 * PageController (landing/dashboard pages), since profile viewing and
 * editing is its own distinct concern with its own form-processing logic.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        model.addAttribute("profile", userService.getProfileByEmail(authentication.getName()));
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editForm(Authentication authentication, Model model) {
        UserProfileDTO profile = userService.getProfileByEmail(authentication.getName());

        ProfileUpdateDTO form = new ProfileUpdateDTO();
        form.setFullName(profile.getFullName());
        form.setDepartment(profile.getDepartment());
        form.setYearOfStudy(profile.getYearOfStudy());
        form.setSkills(profile.getSkills());
        form.setAboutMe(profile.getAboutMe());
        form.setContactDetails(profile.getContactDetails());

        model.addAttribute("profileForm", form);
        model.addAttribute("currentPictureUrl", profile.getProfilePictureUrl());
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String update(@Valid @ModelAttribute("profileForm") ProfileUpdateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        try {
            userService.updateProfile(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/profile";
        } catch (InvalidFileException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "profile/edit";
        }
    }
}
