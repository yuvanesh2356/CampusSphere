package com.campussphere.guidance.repository;

import com.campussphere.guidance.entity.GuidanceCategory;
import com.campussphere.guidance.entity.GuidancePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuidancePostRepository extends JpaRepository<GuidancePost, Long> {

    /**
     * Powers the public browse/search page. Both filters are optional.
     * Only PUBLISHED posts are ever returned here - HIDDEN posts should
     * not appear in public browsing, the same rule
     * MarketplaceListingRepository.searchAvailable() and
     * FreelanceServiceRepository.searchAvailable() apply.
     */
    @Query("SELECT g FROM GuidancePost g " +
           "WHERE g.status = com.campussphere.guidance.entity.VisibilityStatus.PUBLISHED " +
           "AND (:category IS NULL OR g.category = :category) " +
           "AND (:keyword IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY g.createdAt DESC")
    List<GuidancePost> searchPublished(@Param("category") GuidanceCategory category,
                                        @Param("keyword") String keyword);

    /**
     * Powers "My Guidance Posts" - every post owned by the given
     * author regardless of status, so they can manage their full
     * posting history, not just currently-published posts.
     */
    List<GuidancePost> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
