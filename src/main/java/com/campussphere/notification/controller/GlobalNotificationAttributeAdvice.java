package com.campussphere.notification.controller;

import com.campussphere.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes the current user's unread notification count available as
 * "unreadNotificationCount" in every server-rendered page's model,
 * without every controller needing to fetch it manually. This is what
 * powers the notification bell badge in the sidebar/top bar on every
 * page.
 *
 * Applying this globally (no basePackages/assignableTypes restriction)
 * is safe for the existing @RestController JSON endpoints too
 * (AuthController) - @ModelAttribute methods populate the Model
 * object, but a @RestController's JSON response is built directly
 * from its return value via HttpMessageConverter, never from the
 * Model, so this has no effect on any JSON API response.
 */
@ControllerAdvice
public class GlobalNotificationAttributeAdvice {

    private final NotificationService notificationService;

    public GlobalNotificationAttributeAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return 0L;
        }
        try {
            return notificationService.getUnreadCount(authentication.getName());
        } catch (RuntimeException ex) {
            // This @ModelAttribute now runs on every single authenticated
            // page load. A failure here (e.g. the rare edge case of a
            // stale session whose backing User row no longer exists)
            // must never take down page rendering for an unrelated
            // module - worst case, the notification badge is just
            // missing for that one request, which is a graceful
            // degradation rather than a broken page.
            return 0L;
        }
    }
}
