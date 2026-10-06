package com.campussphere.notification.entity;

import com.campussphere.auth.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A single in-app notification for a recipient. Deliberately scoped
 * to what this project can honestly generate right now: there is no
 * buyer/seller interaction system yet (see project blueprint, Future
 * Scope), so notifications are not triggered by "someone messaged
 * you" - they are triggered when an administrator moderates a piece
 * of the recipient's own content. This is a real, defensible use case
 * given the current architecture, and the schema/service here is
 * built so additional trigger sources can be wired in later without
 * any structural change - NotificationService.notify() already
 * accepts an arbitrary message/link/type combination.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    /**
     * Optional relative link (e.g. "/marketplace/12") the notification
     * should take the user to when clicked. Nullable for purely
     * informational notifications with nothing to link to.
     */
    @Column(length = 255)
    private String link;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Notification() {
    }

    public Notification(User recipient, NotificationType type, String message, String link) {
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.link = link;
    }

    // ---------- Getters and Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
