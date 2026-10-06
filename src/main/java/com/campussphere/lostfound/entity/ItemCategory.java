package com.campussphere.lostfound.entity;

/**
 * Category classification for a lost/found item. Mirrors
 * GuidanceCategory/ServiceCategory/ListingCategory's design - a fixed
 * enum rather than a lookup table, since the category set is small
 * and doesn't need to be user-managed.
 */
public enum ItemCategory {
    ID_CARD,
    BOOK,
    BAG,
    MOBILE,
    LAPTOP,
    CALCULATOR,
    KEYS,
    WALLET,
    ACCESSORY,
    OTHER
}
