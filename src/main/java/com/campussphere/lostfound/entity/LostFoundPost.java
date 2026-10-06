package com.campussphere.lostfound.entity;

import com.campussphere.auth.entity.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A single lost-item report or found-item report posted by a student.
 * Structurally close to the other three content modules - same
 * relationship to User, same created/updated lifecycle fields - with
 * fields specific to this domain: postType distinguishes a lost report
 * from a found report, location and dateLostOrFound narrow down where
 * and when the item was lost/found, and status tracks resolution
 * rather than a sale/availability lifecycle.
 */
@Entity
@Table(name = "lostfound_posts")
public class LostFoundPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PostType postType;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(nullable = false)
    private LocalDate dateLostOrFound;

    @Column(nullable = false, length = 100)
    private String contactInformation;

    /**
     * Relative path under the configured upload directory, e.g.
     * "lostfound/&lt;uuid&gt;.jpg". Nullable - a post can be created
     * without a photo. Same convention every other module uses,
     * resolved by FileStorageService.
     */
    @Column(length = 255)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.OPEN;

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

    public LostFoundPost() {
    }

    // ---------- Getters and Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDateLostOrFound() {
        return dateLostOrFound;
    }

    public void setDateLostOrFound(LocalDate dateLostOrFound) {
        this.dateLostOrFound = dateLostOrFound;
    }

    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
