package com.campussphere.notification.service;

import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.notification.entity.Notification;
import com.campussphere.notification.entity.NotificationType;
import com.campussphere.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService, run against mocked dependencies.
 * Covers notification creation, unread counting, and the ownership
 * check on mark-as-read (a user must not be able to mark someone
 * else's notification as read).
 */
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    private User recipient;
    private User otherStudent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationService(notificationRepository, userRepository);

        recipient = new User("Anita Joseph", "anita.joseph@campus.edu.in", "hashed", "CSE", 3);
        recipient.setId(1L);
        recipient.setRole(Role.STUDENT);

        otherStudent = new User("Farhan Ali", "farhan.ali@campus.edu.in", "hashed", "ECE", 2);
        otherStudent.setId(2L);
        otherStudent.setRole(Role.STUDENT);
    }

    @Test
    void notify_savesANewNotificationForTheRecipient() {
        notificationService.notify(recipient, NotificationType.CONTENT_MODERATED,
                "Your listing was removed by an administrator", "/marketplace");

        verify(notificationRepository, times(1)).save(argThat(n ->
                n.getRecipient().equals(recipient)
                        && n.getType() == NotificationType.CONTENT_MODERATED
                        && n.getMessage().contains("removed by an administrator")));
    }

    @Test
    void getUnreadCount_returnsCountFromRepository() {
        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(3L);

        long count = notificationService.getUnreadCount(recipient.getEmail());

        assertEquals(3L, count);
    }

    @Test
    void markAsRead_throwsWhenCurrentUserIsNotTheRecipient() {
        Notification notification = new Notification(recipient, NotificationType.GENERAL, "Test message", null);
        notification.setId(5L);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedActionException.class,
                () -> notificationService.markAsRead(5L, otherStudent.getEmail()));

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_succeedsForTheActualRecipient() {
        Notification notification = new Notification(recipient, NotificationType.GENERAL, "Test message", null);
        notification.setId(5L);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(5L, recipient.getEmail());

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markAsRead_throwsResourceNotFoundWhenMissing() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAsRead(99L, recipient.getEmail()));
    }
}
