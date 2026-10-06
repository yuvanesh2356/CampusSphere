package com.campussphere.opportunity.entity;

import com.campussphere.auth.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Records that a student has bookmarked/saved an opportunity. A
 * unique constraint on (user, opportunity) prevents the same student
 * from bookmarking the same opportunity twice - enforced at the
 * database level via the composite unique constraint below, and
 * checked at the service level before inserting (see
 * OpportunityService.toggleBookmark()) so a duplicate attempt is
 * handled gracefully rather than surfacing as a raw constraint
 * violation.
 */
@Entity
@Table(name = "opportunity_bookmarks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "opportunity_id"}))
public class OpportunityBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public OpportunityBookmark() {
    }

    public OpportunityBookmark(User user, Opportunity opportunity) {
        this.user = user;
        this.opportunity = opportunity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
