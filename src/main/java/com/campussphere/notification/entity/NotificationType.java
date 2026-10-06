package com.campussphere.notification.entity;

/**
 * Category of a notification event. Only CONTENT_MODERATED is
 * actually triggered in this phase (see NotificationService/AdminService)
 * - the other values exist so future phases (an Interaction/messaging
 * system) can extend the Notification Center without a schema change,
 * consistent with how this project's enums are generally sized for
 * near-term real use rather than a speculative full set.
 */
public enum NotificationType {
    CONTENT_MODERATED,
    GENERAL
}
