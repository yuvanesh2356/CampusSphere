package com.campussphere.marketplace.repository;

import com.campussphere.marketplace.entity.ListingCategory;
import com.campussphere.marketplace.entity.MarketplaceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListing, Long> {

    /**
     * Powers the public browse/search page. Both filters are optional -
     * passing null for either parameter simply skips that condition.
     * Only AVAILABLE listings are ever returned here, since RESERVED and
     * SOLD items should not appear in public browsing.
     */
    @Query("SELECT l FROM MarketplaceListing l " +
           "WHERE l.status = com.campussphere.marketplace.entity.ListingStatus.AVAILABLE " +
           "AND (:category IS NULL OR l.category = :category) " +
           "AND (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY l.createdAt DESC")
    List<MarketplaceListing> searchAvailable(@Param("category") ListingCategory category,
                                              @Param("keyword") String keyword);

    /**
     * Powers "My Listings" - returns every listing owned by the given
     * seller regardless of status, so a student can see and manage their
     * full posting history, not just currently-available items.
     */
    List<MarketplaceListing> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
