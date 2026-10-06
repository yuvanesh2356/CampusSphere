package com.campussphere.marketplace.entity;

/**
 * Availability status of a marketplace listing.
 * AVAILABLE listings appear in public browsing/search. RESERVED and SOLD
 * are excluded from public browsing but remain visible to the seller on
 * their own "My Listings" page, preserving their listing history.
 */
public enum ListingStatus {
    AVAILABLE,
    RESERVED,
    SOLD
}
