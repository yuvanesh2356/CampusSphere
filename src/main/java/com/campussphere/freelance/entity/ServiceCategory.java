package com.campussphere.freelance.entity;

/**
 * Category classification for a freelance service offering. Mirrors
 * ListingCategory's design in the marketplace package - a fixed enum
 * rather than a lookup table, since the category set is small and
 * doesn't need to be user-managed.
 */
public enum ServiceCategory {
    RECORD_WRITING,
    PPT_CREATION,
    POSTER_DESIGN,
    RESUME_DESIGN,
    CODING_HELP,
    ASSIGNMENT_HELP,
    VIDEO_EDITING,
    GRAPHIC_DESIGN,
    UI_UX_DESIGN,
    OTHER
}
