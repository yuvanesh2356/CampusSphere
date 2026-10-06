package com.campussphere.lostfound.controller;

import com.campussphere.common.exception.InvalidFileException;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.lostfound.dto.LostFoundCreateDTO;
import com.campussphere.lostfound.dto.LostFoundResponseDTO;
import com.campussphere.lostfound.dto.LostFoundUpdateDTO;
import com.campussphere.lostfound.entity.ItemCategory;
import com.campussphere.lostfound.entity.PostStatus;
import com.campussphere.lostfound.entity.PostType;
import com.campussphere.lostfound.service.LostFoundServiceManager;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Serves every Lost & Found page and handles every form submission
 * (create, edit, delete). Structured identically to
 * GuidanceController/FreelanceController/MarketplaceController: a
 * single server-rendered MVC controller, with business exceptions
 * caught locally and converted into a redirect + flash message, since
 * GlobalExceptionHandler is a JSON-only @RestControllerAdvice scoped
 * to the /api/** layer.
 */
@Controller
@RequestMapping("/lostfound")
public class LostFoundController {

    private final LostFoundServiceManager lostFoundServiceManager;

    public LostFoundController(LostFoundServiceManager lostFoundServiceManager) {
        this.lostFoundServiceManager = lostFoundServiceManager;
    }

    /**
     * Lost & Found home: browse, search, and filter by post type,
     * category, and status. All four query params are optional.
     */
    @GetMapping
    public String browse(@RequestParam(required = false) PostType postType,
                          @RequestParam(required = false) ItemCategory category,
                          @RequestParam(required = false) PostStatus status,
                          @RequestParam(required = false) String keyword,
                          Authentication authentication,
                          Model model) {
        List<LostFoundResponseDTO> posts =
                lostFoundServiceManager.browsePosts(postType, category, status, keyword, authentication.getName());

        model.addAttribute("posts", posts);
        model.addAttribute("postTypes", PostType.values());
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("statuses", PostStatus.values());
        model.addAttribute("selectedPostType", postType);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "lostfound/index";
    }

    @GetMapping("/my-posts")
    public String myPosts(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("posts", lostFoundServiceManager.getMyPosts(authentication.getName()));
            return "lostfound/my-posts";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/lostfound";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("post", lostFoundServiceManager.getPostById(id, authentication.getName()));
            return "lostfound/view";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/lostfound";
        }
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("postForm", new LostFoundCreateDTO());
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("postTypes", PostType.values());
        return "lostfound/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("postForm") LostFoundCreateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("postTypes", PostType.values());
            return "lostfound/form";
        }

        try {
            LostFoundResponseDTO created = lostFoundServiceManager.createPost(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Post created successfully");
            return "redirect:/lostfound/" + created.getId();
        } catch (InvalidFileException | ResourceNotFoundException ex) {
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("postTypes", PostType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "lostfound/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            LostFoundResponseDTO post = lostFoundServiceManager.getPostById(id, authentication.getName());
            if (!post.isOwnedByCurrentUser()) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own posts");
                return "redirect:/lostfound/" + id;
            }

            LostFoundUpdateDTO form = new LostFoundUpdateDTO();
            form.setTitle(post.getTitle());
            form.setDescription(post.getDescription());
            form.setCategory(post.getCategory());
            form.setPostType(post.getPostType());
            form.setLocation(post.getLocation());
            form.setDateLostOrFound(post.getDateLostOrFound());
            form.setContactInformation(post.getContactInformation());
            form.setStatus(post.getStatus());

            model.addAttribute("postForm", form);
            model.addAttribute("postId", id);
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("postTypes", PostType.values());
            model.addAttribute("statuses", PostStatus.values());
            return "lostfound/form";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/lostfound";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("postForm") LostFoundUpdateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("postTypes", PostType.values());
            model.addAttribute("statuses", PostStatus.values());
            return "lostfound/form";
        }

        try {
            lostFoundServiceManager.updatePost(id, authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Post updated successfully");
            return "redirect:/lostfound/" + id;
        } catch (UnauthorizedActionException | ResourceNotFoundException | InvalidFileException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/lostfound/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            lostFoundServiceManager.deletePost(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Post deleted successfully");
            return "redirect:/lostfound/my-posts";
        } catch (UnauthorizedActionException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/lostfound/" + id;
        }
    }
}
