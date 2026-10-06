package com.campussphere.guidance.controller;

import com.campussphere.common.exception.InvalidFileException;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.guidance.dto.GuidancePostCreateDTO;
import com.campussphere.guidance.dto.GuidancePostResponseDTO;
import com.campussphere.guidance.dto.GuidancePostUpdateDTO;
import com.campussphere.guidance.entity.GuidanceCategory;
import com.campussphere.guidance.entity.VisibilityStatus;
import com.campussphere.guidance.service.GuidanceServiceManager;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Serves every Senior Guidance Hub page and handles every form
 * submission (create, edit, delete). Structured identically to
 * FreelanceController and MarketplaceController: a single
 * server-rendered MVC controller, with business exceptions caught
 * locally and converted into a redirect + flash message, since
 * GlobalExceptionHandler is a JSON-only @RestControllerAdvice scoped
 * to the /api/** layer.
 */
@Controller
@RequestMapping("/guidance")
public class GuidanceController {

    private final GuidanceServiceManager guidanceServiceManager;

    public GuidanceController(GuidanceServiceManager guidanceServiceManager) {
        this.guidanceServiceManager = guidanceServiceManager;
    }

    /**
     * Guidance Hub home: browse, search, and filter by category.
     * Both query params are optional.
     */
    @GetMapping
    public String browse(@RequestParam(required = false) GuidanceCategory category,
                          @RequestParam(required = false) String keyword,
                          Authentication authentication,
                          Model model) {
        List<GuidancePostResponseDTO> posts =
                guidanceServiceManager.browsePosts(category, keyword, authentication.getName());

        model.addAttribute("posts", posts);
        model.addAttribute("categories", GuidanceCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        return "guidance/index";
    }

    @GetMapping("/my-guidance")
    public String myGuidance(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("posts", guidanceServiceManager.getMyPosts(authentication.getName()));
            return "guidance/my-guidance";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/guidance";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("post", guidanceServiceManager.getPostById(id, authentication.getName()));
            return "guidance/view";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/guidance";
        }
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("guidanceForm", new GuidancePostCreateDTO());
        model.addAttribute("categories", GuidanceCategory.values());
        return "guidance/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("guidanceForm") GuidancePostCreateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", GuidanceCategory.values());
            return "guidance/form";
        }

        try {
            GuidancePostResponseDTO created = guidanceServiceManager.createPost(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Guidance posted successfully");
            return "redirect:/guidance/" + created.getId();
        } catch (InvalidFileException | ResourceNotFoundException ex) {
            model.addAttribute("categories", GuidanceCategory.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "guidance/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            GuidancePostResponseDTO post = guidanceServiceManager.getPostById(id, authentication.getName());
            if (!post.isOwnedByCurrentUser()) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own guidance posts");
                return "redirect:/guidance/" + id;
            }

            GuidancePostUpdateDTO form = new GuidancePostUpdateDTO();
            form.setTitle(post.getTitle());
            form.setCategory(post.getCategory());
            form.setDescription(post.getDescription());
            form.setRelevantYear(post.getRelevantYear());
            form.setRelevantDepartment(post.getRelevantDepartment());
            form.setStatus(post.getStatus());

            model.addAttribute("guidanceForm", form);
            model.addAttribute("postId", id);
            model.addAttribute("categories", GuidanceCategory.values());
            model.addAttribute("statuses", VisibilityStatus.values());
            return "guidance/form";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/guidance";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("guidanceForm") GuidancePostUpdateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("categories", GuidanceCategory.values());
            model.addAttribute("statuses", VisibilityStatus.values());
            return "guidance/form";
        }

        try {
            guidanceServiceManager.updatePost(id, authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Guidance post updated successfully");
            return "redirect:/guidance/" + id;
        } catch (UnauthorizedActionException | ResourceNotFoundException | InvalidFileException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/guidance/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            guidanceServiceManager.deletePost(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Guidance post deleted successfully");
            return "redirect:/guidance/my-guidance";
        } catch (UnauthorizedActionException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/guidance/" + id;
        }
    }
}
