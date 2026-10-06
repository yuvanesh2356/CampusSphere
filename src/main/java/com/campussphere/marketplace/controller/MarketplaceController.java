package com.campussphere.marketplace.controller;

import com.campussphere.common.exception.InvalidFileException;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.marketplace.dto.MarketplaceListingCreateDTO;
import com.campussphere.marketplace.dto.MarketplaceListingResponseDTO;
import com.campussphere.marketplace.dto.MarketplaceListingUpdateDTO;
import com.campussphere.marketplace.entity.ListingCategory;
import com.campussphere.marketplace.entity.ListingCondition;
import com.campussphere.marketplace.entity.ListingStatus;
import com.campussphere.marketplace.service.MarketplaceListingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Serves every Marketplace page and handles every Marketplace form
 * submission (create, edit, delete). Kept as a single controller,
 * unlike Auth's Page/API split - Marketplace has no separate JSON API
 * consumer in this phase (no JS-driven AJAX calls for its core
 * operations, only server-rendered forms), so a page-serving and
 * form-processing split isn't warranted here. A REST API layer can be
 * added later without touching this class if one is ever needed.
 *
 * Business exceptions (ResourceNotFoundException, UnauthorizedActionException,
 * InvalidFileException) are caught locally and converted into a redirect
 * with a flash error message, since this controller returns HTML views,
 * not JSON - GlobalExceptionHandler is a @RestControllerAdvice scoped to
 * the /api/** JSON layer and is intentionally not involved here.
 */
@Controller
@RequestMapping("/marketplace")
public class MarketplaceController {

    private final MarketplaceListingService listingService;

    public MarketplaceController(MarketplaceListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * Marketplace home: browse, search, and filter by category.
     * Both query params are optional.
     */
    @GetMapping
    public String browse(@RequestParam(required = false) ListingCategory category,
                          @RequestParam(required = false) String keyword,
                          Authentication authentication,
                          Model model) {
        List<MarketplaceListingResponseDTO> listings =
                listingService.browseListings(category, keyword, authentication.getName());

        model.addAttribute("listings", listings);
        model.addAttribute("categories", ListingCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        return "marketplace/index";
    }

    @GetMapping("/my-listings")
    public String myListings(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("listings", listingService.getMyListings(authentication.getName()));
            return "marketplace/my-listings";
        } catch (ResourceNotFoundException ex) {
            // Narrow edge case: an authenticated session whose backing User
            // record no longer exists (e.g. deleted mid-session). Caught here
            // rather than left to propagate, since GlobalExceptionHandler is
            // a JSON-only @RestControllerAdvice and would render raw JSON to
            // this HTML page's request otherwise.
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/marketplace";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("listing", listingService.getListingById(id, authentication.getName()));
            return "marketplace/view";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/marketplace";
        }
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("listingForm", new MarketplaceListingCreateDTO());
        model.addAttribute("categories", ListingCategory.values());
        model.addAttribute("conditions", ListingCondition.values());
        return "marketplace/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("listingForm") MarketplaceListingCreateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", ListingCategory.values());
            model.addAttribute("conditions", ListingCondition.values());
            return "marketplace/form";
        }

        try {
            MarketplaceListingResponseDTO created = listingService.createListing(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Listing created successfully");
            return "redirect:/marketplace/" + created.getId();
        } catch (InvalidFileException | ResourceNotFoundException ex) {
            model.addAttribute("categories", ListingCategory.values());
            model.addAttribute("conditions", ListingCondition.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "marketplace/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            MarketplaceListingResponseDTO listing = listingService.getListingById(id, authentication.getName());
            if (!listing.isOwnedByCurrentUser()) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own listings");
                return "redirect:/marketplace/" + id;
            }

            MarketplaceListingUpdateDTO form = new MarketplaceListingUpdateDTO();
            form.setTitle(listing.getTitle());
            form.setDescription(listing.getDescription());
            form.setCategory(listing.getCategory());
            form.setCondition(listing.getCondition());
            form.setPrice(listing.getPrice());
            form.setContactInfo(listing.getContactInfo());
            form.setStatus(listing.getStatus());

            model.addAttribute("listingForm", form);
            model.addAttribute("listingId", id);
            model.addAttribute("categories", ListingCategory.values());
            model.addAttribute("conditions", ListingCondition.values());
            model.addAttribute("statuses", ListingStatus.values());
            return "marketplace/form";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/marketplace";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("listingForm") MarketplaceListingUpdateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("listingId", id);
            model.addAttribute("categories", ListingCategory.values());
            model.addAttribute("conditions", ListingCondition.values());
            model.addAttribute("statuses", ListingStatus.values());
            return "marketplace/form";
        }

        try {
            listingService.updateListing(id, authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Listing updated successfully");
            return "redirect:/marketplace/" + id;
        } catch (UnauthorizedActionException | ResourceNotFoundException | InvalidFileException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/marketplace/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            listingService.deleteListing(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Listing deleted successfully");
            return "redirect:/marketplace/my-listings";
        } catch (UnauthorizedActionException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/marketplace/" + id;
        }
    }
}
