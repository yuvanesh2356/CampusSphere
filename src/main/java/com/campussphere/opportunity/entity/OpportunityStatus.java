package com.campussphere.opportunity.entity;

/**
 * Lifecycle status of an opportunity listing, set by whoever curates
 * it (Admin). UPCOMING/OPEN both appear in the public discovery feed;
 * CLOSED is excluded from the default feed but remains viewable via
 * direct link and in "My Saved" if a student had already bookmarked
 * it, so a bookmark never silently disappears once an opportunity closes.
 */
public enum OpportunityStatus {
    UPCOMING,
    OPEN,
    CLOSED
}
