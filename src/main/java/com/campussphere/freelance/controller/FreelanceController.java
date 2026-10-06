package com.campussphere.freelance.controller;

import com.campussphere.common.exception.InvalidFileException;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.freelance.dto.FreelanceServiceCreateDTO;
import com.campussphere.freelance.dto.FreelanceServiceResponseDTO;
import com.campussphere.freelance.dto.FreelanceServiceUpdateDTO;
import com.campussphere.freelance.entity.AvailabilityStatus;
import com.campussphere.freelance.entity.ServiceCategory;
import com.campussphere.freelance.service.FreelanceServiceManager;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Serves every Freelance Hub page and handles every form submission
 * (create, edit, delete). Structured identically to MarketplaceController:
 * a single server-rendered MVC controller (no separate JSON API layer
 * in this phase), with business exceptions caught locally and
 * converted into a redirect + flash message, since GlobalExceptionHandler
 * is a JSON-only @RestControllerAdvice scoped to the /api/** layer.
 */
@Controller
@RequestMapping("/freelance")
public class FreelanceController {

    private final FreelanceServiceManager serviceManager;

    public FreelanceController(FreelanceServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /**
     * Freelance Hub home: browse, search, and filter by category.
     * Both query params are optional.
     */
    @GetMapping
    public String browse(@RequestParam(required = false) ServiceCategory category,
                          @RequestParam(required = false) String keyword,
                          Authentication authentication,
                          Model model) {
        List<FreelanceServiceResponseDTO> services =
                serviceManager.browseServices(category, keyword, authentication.getName());

        model.addAttribute("services", services);
        model.addAttribute("categories", ServiceCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        return "freelance/index";
    }

    @GetMapping("/my-services")
    public String myServices(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("services", serviceManager.getMyServices(authentication.getName()));
            return "freelance/my-services";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/freelance";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("service", serviceManager.getServiceById(id, authentication.getName()));
            return "freelance/view";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/freelance";
        }
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("serviceForm", new FreelanceServiceCreateDTO());
        model.addAttribute("categories", ServiceCategory.values());
        return "freelance/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("serviceForm") FreelanceServiceCreateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", ServiceCategory.values());
            return "freelance/form";
        }

        try {
            FreelanceServiceResponseDTO created = serviceManager.createService(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Service posted successfully");
            return "redirect:/freelance/" + created.getId();
        } catch (InvalidFileException | ResourceNotFoundException ex) {
            model.addAttribute("categories", ServiceCategory.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "freelance/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            FreelanceServiceResponseDTO service = serviceManager.getServiceById(id, authentication.getName());
            if (!service.isOwnedByCurrentUser()) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own services");
                return "redirect:/freelance/" + id;
            }

            FreelanceServiceUpdateDTO form = new FreelanceServiceUpdateDTO();
            form.setTitle(service.getTitle());
            form.setCategory(service.getCategory());
            form.setDescription(service.getDescription());
            form.setPrice(service.getPrice());
            form.setPriceType(service.getPriceType());
            form.setContactInfo(service.getContactInfo());
            form.setStatus(service.getStatus());

            model.addAttribute("serviceForm", form);
            model.addAttribute("serviceId", id);
            model.addAttribute("categories", ServiceCategory.values());
            model.addAttribute("statuses", AvailabilityStatus.values());
            return "freelance/form";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/freelance";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("serviceForm") FreelanceServiceUpdateDTO request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("serviceId", id);
            model.addAttribute("categories", ServiceCategory.values());
            model.addAttribute("statuses", AvailabilityStatus.values());
            return "freelance/form";
        }

        try {
            serviceManager.updateService(id, authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Service updated successfully");
            return "redirect:/freelance/" + id;
        } catch (UnauthorizedActionException | ResourceNotFoundException | InvalidFileException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/freelance/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            serviceManager.deleteService(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Service deleted successfully");
            return "redirect:/freelance/my-services";
        } catch (UnauthorizedActionException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/freelance/" + id;
        }
    }
}
