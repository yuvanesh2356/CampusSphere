package com.campussphere.guidance.entity;

/**
 * Category classification for a senior guidance post. Mirrors
 * ServiceCategory/ListingCategory's design - a fixed enum rather than
 * a lookup table, since the category set is small and doesn't need to
 * be user-managed.
 */
public enum GuidanceCategory {
    INTERNSHIP_GUIDANCE,
    PLACEMENT_PREPARATION,
    SUBJECT_GUIDANCE,
    HACKATHON_GUIDANCE,
    CAREER_ADVICE,
    CERTIFICATION_GUIDANCE,
    HIGHER_STUDIES,
    OTHER
}
