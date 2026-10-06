package com.campussphere.opportunity.entity;

import com.campussphere.auth.entity.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A single external opportunity (hackathon, coding competition,
 * workshop, global challenge, etc.) curated for student discovery.
 *
 * Unlike the four content modules (Marketplace, Freelance, Guidance,
 * Lost & Found), opportunities are not student-authored - there is no
 * "My Opportunities" or student-facing create/edit/delete here. This
 * is a curated feed: an administrator posts and maintains it (see
 * OpportunityController's @PreAuthorize-gated admin endpoints), and
 * students browse, search, filter, and bookmark. postedBy is still
 * tracked (which admin curated it), consistent with every other
 * entity in this project always recording an owning User, but it does
 * not drive an ownership-based edit/delete check the way the other
 * four modules' owner fields do.
 */
@Entity
@Table(name = "opportunities")
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_id", nullable = false)
    private User postedBy;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 150)
    private String organization;

    @Column(nullable = false, length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OpportunityCategory category;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private LocalDate registrationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OpportunityMode mode;

    /**
     * Physical location - relevant for OFFLINE/HYBRID. Nullable since
     * a purely ONLINE opportunity has no location.
     */
    @Column(length = 200)
    private String location;

    @Column(length = 500)
    private String prizeBenefits;

    /**
     * Relative path under the configured upload directory, e.g.
     * "opportunity/&lt;uuid&gt;.jpg". Nullable - a banner is optional.
     * Same convention every other module uses, resolved by FileStorageService.
     */
    @Column(length = 255)
    private String bannerImagePath;

    @Column(nullable = false, length = 500)
    private String registrationUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpportunityStatus status = OpportunityStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Opportunity() {
    }

    // ---------- Getters and Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(User postedBy) {
        this.postedBy = postedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OpportunityCategory getCategory() {
        return category;
    }

    public void setCategory(OpportunityCategory category) {
        this.category = category;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalDate getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDate registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public OpportunityMode getMode() {
        return mode;
    }

    public void setMode(OpportunityMode mode) {
        this.mode = mode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrizeBenefits() {
        return prizeBenefits;
    }

    public void setPrizeBenefits(String prizeBenefits) {
        this.prizeBenefits = prizeBenefits;
    }

    public String getBannerImagePath() {
        return bannerImagePath;
    }

    public void setBannerImagePath(String bannerImagePath) {
        this.bannerImagePath = bannerImagePath;
    }

    public String getRegistrationUrl() {
        return registrationUrl;
    }

    public void setRegistrationUrl(String registrationUrl) {
        this.registrationUrl = registrationUrl;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
