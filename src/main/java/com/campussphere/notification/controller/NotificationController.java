package com.campussphere.notification.controller;

import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Serves the Notification Center page and handles marking a
 * notification as read. Structured the same way as every other
 * module's controller: server-rendered MVC, business exceptions
 * caught locally.
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        model.addAttribute("notifications", notificationService.getNotificationsForUser(authentication.getName()));
        return "notification/index";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id, authentication.getName());
        } catch (UnauthorizedActionException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/notifications";
    }
}
