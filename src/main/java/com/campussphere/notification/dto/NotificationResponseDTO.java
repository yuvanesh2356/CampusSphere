package com.campussphere.notification.dto;

import com.campussphere.notification.entity.Notification;
import com.campussphere.notification.entity.NotificationType;

import java.time.LocalDateTime;

/**
 * Outgoing representation of a Notification. No entity is ever
 * exposed directly to a template, matching every other module's
 * pattern in this project.
 */
public class NotificationResponseDTO {

    private Long id;
    private NotificationType type;
    private String message;
    private String link;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponseDTO() {
    }

    public static NotificationResponseDTO fromEntity(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setLink(notification.getLink());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
