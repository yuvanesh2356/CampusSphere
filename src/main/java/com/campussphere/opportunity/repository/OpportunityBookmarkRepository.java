package com.campussphere.opportunity.repository;

import com.campussphere.opportunity.entity.OpportunityBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpportunityBookmarkRepository extends JpaRepository<OpportunityBookmark, Long> {

    Optional<OpportunityBookmark> findByUserIdAndOpportunityId(Long userId, Long opportunityId);

    boolean existsByUserIdAndOpportunityId(Long userId, Long opportunityId);

    List<OpportunityBookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
}
