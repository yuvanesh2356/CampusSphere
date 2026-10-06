package com.campussphere.freelance.repository;

import com.campussphere.freelance.entity.FreelanceService;
import com.campussphere.freelance.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FreelanceServiceRepository extends JpaRepository<FreelanceService, Long> {

    /**
     * Powers the public browse/search page. Both filters are optional.
     * Only AVAILABLE services are ever returned here - BUSY and
     * NOT_ACCEPTING should not appear in public browsing, the same
     * rule MarketplaceListingRepository.searchAvailable() applies.
     */
    @Query("SELECT f FROM FreelanceService f " +
           "WHERE f.status = com.campussphere.freelance.entity.AvailabilityStatus.AVAILABLE " +
           "AND (:category IS NULL OR f.category = :category) " +
           "AND (:keyword IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY f.createdAt DESC")
    List<FreelanceService> searchAvailable(@Param("category") ServiceCategory category,
                                            @Param("keyword") String keyword);

    /**
     * Powers "My Services" - every service owned by the given seller
     * regardless of status, so they can manage their full posting
     * history, not just currently-available offerings.
     */
    List<FreelanceService> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
