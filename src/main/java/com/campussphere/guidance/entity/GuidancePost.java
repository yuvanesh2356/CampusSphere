package com.campussphere.guidance.entity;

import com.campussphere.auth.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A single piece of guidance (internship advice, placement prep,
 * subject help, etc.) posted by a student for others to discover.
 * Structurally close to MarketplaceListing/FreelanceService - same
 * relationship to User, same created/updated lifecycle fields - but
 * has no price/contact fields, since guidance is shared freely rather
 * than transacted. relevantYear/relevantDepartment are optional
 * targeting metadata (e.g. "this is for 3rd-year CSE students"),
 * distinct from the author's own year/department on their User profile.
 */
@Entity
@Table(name = "guidance_posts")
public class GuidancePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GuidanceCategory category;

    @Column(nullable = false, length = 3000)
    private String description;

    /**
     * Optional targeting metadata - which year this guidance is most
     * relevant to. Nullable: guidance can be posted as generally
     * applicable to all years.
     */
    @Column
    private Integer relevantYear;

    /**
     * Optional targeting metadata - which department this guidance is
     * most relevant to. Nullable: guidance can be posted as generally
     * applicable to all departments.
     */
    @Column(length = 100)
    private String relevantDepartment;

    /**
     * Relative path under the configured upload directory, e.g.
     * "guidance/&lt;uuid&gt;.jpg". Nullable - a post can be created
     * without an attachment. Same convention MarketplaceListing and
     * FreelanceService use, resolved by FileStorageService.
     */
    @Column(length = 255)
    private String attachmentPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisibilityStatus status = VisibilityStatus.PUBLISHED;

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

    public GuidancePost() {
    }

    // ---------- Getters and Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GuidanceCategory getCategory() {
        return category;
    }

    public void setCategory(GuidanceCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRelevantYear() {
        return relevantYear;
    }

    public void setRelevantYear(Integer relevantYear) {
        this.relevantYear = relevantYear;
    }

    public String getRelevantDepartment() {
        return relevantDepartment;
    }

    public void setRelevantDepartment(String relevantDepartment) {
        this.relevantDepartment = relevantDepartment;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public VisibilityStatus getStatus() {
        return status;
    }

    public void setStatus(VisibilityStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
