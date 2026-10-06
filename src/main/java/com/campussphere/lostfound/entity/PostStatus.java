package com.campussphere.lostfound.entity;

/**
 * Resolution status of a lost/found post, set by its owner.
 * OPEN: still active/unresolved - this is what public browsing shows
 * by default, the same convention ListingStatus/AvailabilityStatus/
 * VisibilityStatus follow in the earlier modules.
 * CLAIMED: the item has been matched with its owner but the post is
 * kept visible for transparency (e.g. so others don't duplicate
 * contact attempts) rather than hidden outright.
 * CLOSED: fully resolved and no longer relevant.
 * Unlike the earlier modules, Lost & Found intentionally lets browsing
 * filter to CLAIMED/CLOSED explicitly (not just OPEN) since seeing
 * "this was already claimed" has real value here.
 */
public enum PostStatus {
    OPEN,
    CLAIMED,
    CLOSED
}
