package com.campussphere.opportunity.controller;

import com.campussphere.common.exception.InvalidFileException;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.opportunity.dto.OpportunityCreateDTO;
import com.campussphere.opportunity.dto.OpportunityResponseDTO;
import com.campussphere.opportunity.dto.OpportunityUpdateDTO;
import com.campussphere.opportunity.entity.OpportunityCategory;
import com.campussphere.opportunity.entity.OpportunityMode;
import com.campussphere.opportunity.entity.OpportunityStatus;
import com.campussphere.opportunity.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Serves the Global Opportunities discovery feed, saved list, and
 * detail page for every authenticated student, plus admin-only
 * curation (create/edit/delete) gated with @PreAuthorize - the same
 * belt-and-suspenders pattern AdminController uses on top of
 * SecurityConfig's filter-chain authentication requirement. Every
 * /global/** route already falls under SecurityConfig's existing
 * anyRequest().authenticated() catch-all, so no SecurityConfig change
 * was needed for this module.
 */
@Controller
@RequestMapping("/global")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public String browse(@RequestParam(required = false) OpportunityCategory category,
                          @RequestParam(required = false) OpportunityMode mode,
                          @RequestParam(required = false) String keyword,
                          Authentication authentication,
                          Model model) {
        List<OpportunityResponseDTO> opportunities =
                opportunityService.browseOpportunities(category, mode, keyword, authentication.getName());

        model.addAttribute("opportunities", opportunities);
        model.addAttribute("categories", OpportunityCategory.values());
        model.addAttribute("modes", OpportunityMode.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedMode", mode);
        model.addAttribute("keyword", keyword);
        return "opportunity/index";
    }

    @GetMapping("/saved")
    public String saved(Authentication authentication, Model model) {
        model.addAttribute("opportunities", opportunityService.getSavedOpportunities(authentication.getName()));
        return "opportunity/saved";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("opportunity", opportunityService.getOpportunityById(id, authentication.getName()));
            return "opportunity/view";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/global";
        }
    }

    /**
     * Toggles bookmark state and redirects back to wherever the
     * request came from (feed, saved list, or detail page), via a
     * hidden "redirectTo" field the form supplies - keeps a student
     * browsing the feed without being bounced to the detail page just
     * to save something.
     */
    @PostMapping("/{id}/bookmark")
    public String toggleBookmark(@PathVariable Long id,
                                  @RequestParam(defaultValue = "/global") String redirectTo,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            boolean nowBookmarked = opportunityService.toggleBookmark(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    nowBookmarked ? "Saved to your bookmarks" : "Removed from your bookmarks");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + redirectTo;
    }

    // ---------- Admin-only curation ----------

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("opportunityForm", new OpportunityCreateDTO());
        model.addAttribute("categories", OpportunityCategory.values());
        model.addAttribute("modes", OpportunityMode.values());
        return "opportunity/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("opportunityForm") OpportunityCreateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", OpportunityCategory.values());
            model.addAttribute("modes", OpportunityMode.values());
            return "opportunity/form";
        }

        try {
            OpportunityResponseDTO created = opportunityService.createOpportunity(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity posted successfully");
            return "redirect:/global/" + created.getId();
        } catch (InvalidFileException | ResourceNotFoundException ex) {
            model.addAttribute("categories", OpportunityCategory.values());
            model.addAttribute("modes", OpportunityMode.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "opportunity/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            OpportunityResponseDTO opportunity = opportunityService.getOpportunityById(id, authentication.getName());

            OpportunityUpdateDTO form = new OpportunityUpdateDTO();
            form.setTitle(opportunity.getTitle());
            form.setOrganization(opportunity.getOrganization());
            form.setDescription(opportunity.getDescription());
            form.setCategory(opportunity.getCategory());
            form.setEventDate(opportunity.getEventDate());
            form.setRegistrationDeadline(opportunity.getRegistrationDeadline());
            form.setMode(opportunity.getMode());
            form.setLocation(opportunity.getLocation());
            form.setPrizeBenefits(opportunity.getPrizeBenefits());
            form.setRegistrationUrl(opportunity.getRegistrationUrl());
            form.setStatus(opportunity.getStatus());

            model.addAttribute("opportunityForm", form);
            model.addAttribute("opportunityId", id);
            model.addAttribute("categories", OpportunityCategory.values());
            model.addAttribute("modes", OpportunityMode.values());
            model.addAttribute("statuses", OpportunityStatus.values());
            return "opportunity/form";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/global";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("opportunityForm") OpportunityUpdateDTO request,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("opportunityId", id);
            model.addAttribute("categories", OpportunityCategory.values());
            model.addAttribute("modes", OpportunityMode.values());
            model.addAttribute("statuses", OpportunityStatus.values());
            return "opportunity/form";
        }

        try {
            opportunityService.updateOpportunity(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity updated successfully");
            return "redirect:/global/" + id;
        } catch (ResourceNotFoundException | InvalidFileException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/global/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            opportunityService.deleteOpportunity(id);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity removed");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/global";
    }
}
