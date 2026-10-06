package com.campussphere.opportunity.repository;

import com.campussphere.opportunity.entity.Opportunity;
import com.campussphere.opportunity.entity.OpportunityCategory;
import com.campussphere.opportunity.entity.OpportunityMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /**
     * Powers the public discovery feed. category, mode, and keyword
     * are all optional. Only UPCOMING/OPEN opportunities are shown by
     * default - CLOSED ones are excluded from browsing (but remain
     * reachable directly, e.g. from a student's Saved list).
     */
    @Query("SELECT o FROM Opportunity o " +
           "WHERE o.status <> com.campussphere.opportunity.entity.OpportunityStatus.CLOSED " +
           "AND (:category IS NULL OR o.category = :category) " +
           "AND (:mode IS NULL OR o.mode = :mode) " +
           "AND (:keyword IS NULL OR LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(o.organization) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY o.registrationDeadline ASC")
    List<Opportunity> searchActive(@Param("category") OpportunityCategory category,
                                    @Param("mode") OpportunityMode mode,
                                    @Param("keyword") String keyword);
}
