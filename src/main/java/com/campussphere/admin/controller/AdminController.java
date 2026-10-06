package com.campussphere.admin.controller;

import com.campussphere.admin.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Serves the Admin Dashboard, user management, and cross-module
 * content moderation pages. SecurityConfig already restricts every
 * /admin/** route to ROLE_ADMIN at the filter-chain level; the
 * @PreAuthorize annotations here are a deliberate belt-and-suspenders
 * addition (defense in depth), not a replacement for that check -
 * @EnableMethodSecurity was already turned on in SecurityConfig back
 * in Phase 1 specifically so this would be available without any
 * further security configuration.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    /**
     * A single tabbed content-moderation page rather than four separate
     * pages - all four modules' data is loaded together (the dataset
     * size for a college project makes this trivial), and the
     * "module" query param controls which tab is shown active,
     * without needing four near-identical controller methods/templates.
     */
    @GetMapping("/content")
    public String content(@RequestParam(defaultValue = "marketplace") String module, Model model) {
        model.addAttribute("activeModule", module);
        model.addAttribute("marketplaceListings", adminService.getAllMarketplaceListings());
        model.addAttribute("freelanceServices", adminService.getAllFreelanceServices());
        model.addAttribute("guidancePosts", adminService.getAllGuidancePosts());
        model.addAttribute("lostFoundPosts", adminService.getAllLostFoundPosts());
        return "admin/content";
    }

    @PostMapping("/content/marketplace/{id}/delete")
    public String deleteMarketplaceListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deleteMarketplaceListing(id);
        redirectAttributes.addFlashAttribute("successMessage", "Marketplace listing removed");
        return "redirect:/admin/content?module=marketplace";
    }

    @PostMapping("/content/freelance/{id}/delete")
    public String deleteFreelanceService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deleteFreelanceService(id);
        redirectAttributes.addFlashAttribute("successMessage", "Freelance service removed");
        return "redirect:/admin/content?module=freelance";
    }

    @PostMapping("/content/guidance/{id}/delete")
    public String deleteGuidancePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deleteGuidancePost(id);
        redirectAttributes.addFlashAttribute("successMessage", "Guidance post removed");
        return "redirect:/admin/content?module=guidance";
    }

    @PostMapping("/content/lostfound/{id}/delete")
    public String deleteLostFoundPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deleteLostFoundPost(id);
        redirectAttributes.addFlashAttribute("successMessage", "Lost & Found post removed");
        return "redirect:/admin/content?module=lostfound";
    }
}
