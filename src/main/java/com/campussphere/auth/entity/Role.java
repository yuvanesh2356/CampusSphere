package com.campussphere.auth.entity;

/**
 * Access levels within CampusSphere.
 * STUDENT: default role for every registered user - can use all
 *          content modules (Marketplace, Freelance, Guidance, Lost&Found).
 * ADMIN:   elevated role with access to the Admin Module (Phase 5) for
 *          user management, listing moderation, and report resolution.
 */
public enum Role {
    STUDENT,
    ADMIN
}
