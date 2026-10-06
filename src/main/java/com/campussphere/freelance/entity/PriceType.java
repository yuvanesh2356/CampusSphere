package com.campussphere.freelance.entity;

/**
 * Distinguishes a flat, non-negotiable price from a "starting from"
 * price that may vary with scope (e.g. a resume redesign priced
 * higher than a simple record write-up). Kept as a small enum next to
 * the price field, rather than two separate price columns, so the UI
 * can label the price correctly without extra branching logic.
 */
public enum PriceType {
    FIXED,
    STARTING_FROM
}
