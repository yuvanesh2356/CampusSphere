package com.campussphere.marketplace.entity;

/**
 * Category classification for a marketplace listing.
 * Kept as a simple enum rather than a separate lookup table - the set
 * of categories is small, fixed, and does not need to be user-managed
 * at this scale, so a table + admin CRUD for categories would be
 * over-engineering for Phase 2.
 */
public enum ListingCategory {
    BOOKS,
    ELECTRONICS,
    CALCULATORS,
    ACADEMIC_MATERIALS,
    OTHER
}
