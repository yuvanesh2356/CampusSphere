package com.campussphere.notification.service;

import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.notification.dto.NotificationResponseDTO;
import com.campussphere.notification.entity.Notification;
import com.campussphere.notification.entity.NotificationType;
import com.campussphere.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Notification Center. No naming collision
 * concern here (unlike Freelance/Guidance/LostFound's "Manager"
 * classes) since the entity is named Notification, not
 * NotificationService, so the standard [Feature]Service convention
 * applies cleanly.
 *
 * notify() is the single entry point every other part of the
 * application uses to create a notification - see AdminService for
 * the current trigger (content moderation). Kept intentionally
 * generic (recipient/type/message/link) so a future Interaction or
 * messaging system can call it too without any change here.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notify(User recipient, NotificationType type, String message, String link) {
        Notification notification = new Notification(recipient, type, message, link);
        notificationRepository.save(notification);
    }

    public List<NotificationResponseDTO> getNotificationsForUser(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(NotificationResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, String currentUserEmail) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedActionException("You can only manage your own notifications");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }
}
