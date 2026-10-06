package com.campussphere.guidance.entity;

/**
 * Visibility status of a guidance post, set by its author. PUBLISHED
 * posts appear in public browsing/search; HIDDEN posts are excluded
 * from public browsing but remain visible to the author on their own
 * "My Guidance Posts" page - the same pattern ListingStatus and
 * AvailabilityStatus follow in the earlier modules. Unlike those two,
 * this is a simple two-state toggle rather than a multi-state
 * lifecycle, since a guidance post doesn't have a "transaction" to
 * track (no Reserved/Sold or Busy/Not Accepting equivalent) - an
 * author either wants a post visible or wants to withdraw it from view.
 */
public enum VisibilityStatus {
    PUBLISHED,
    HIDDEN
}
