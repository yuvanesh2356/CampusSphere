package com.campussphere.freelance.entity;

/**
 * Availability status of a freelance service offering, set by the
 * seller. AVAILABLE services appear in public browsing/search; BUSY
 * and NOT_ACCEPTING are excluded from public browsing but remain
 * visible to the owner on their own "My Services" page - the same
 * pattern ListingStatus follows in the marketplace module.
 */
public enum AvailabilityStatus {
    AVAILABLE,
    BUSY,
    NOT_ACCEPTING
}
