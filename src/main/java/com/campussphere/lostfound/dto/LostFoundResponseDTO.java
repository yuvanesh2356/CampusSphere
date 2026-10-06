package com.campussphere.lostfound.dto;

import com.campussphere.lostfound.entity.ItemCategory;
import com.campussphere.lostfound.entity.LostFoundPost;
import com.campussphere.lostfound.entity.PostStatus;
import com.campussphere.lostfound.entity.PostType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outgoing representation of a lost/found post, used by both the
 * browse/search page and the detail page. Mirrors the Response DTO
 * pattern used across every other module: carries owner info directly
 * so templates never reach into the User entity, and an
 * ownedByCurrentUser flag so templates can conditionally show
 * Edit/Delete buttons without repeating ownership logic in HTML.
 */
public class LostFoundResponseDTO {

    private Long id;
    private String title;
    private String description;
    private ItemCategory category;
    private PostType postType;
    private String location;
    private LocalDate dateLostOrFound;
    private String contactInformation;
    private String imageUrl;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long ownerId;
    private String ownerName;
    private String ownerDepartment;

    private boolean ownedByCurrentUser;

    public LostFoundResponseDTO() {
    }

    /**
     * Converts an entity into its safe, external-facing representation.
     * imageUrl is resolved here (not left as a raw file path) so
     * templates can bind directly to it in an <img> src attribute.
     */
    public static LostFoundResponseDTO fromEntity(LostFoundPost post, boolean ownedByCurrentUser) {
        LostFoundResponseDTO dto = new LostFoundResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setCategory(post.getCategory());
        dto.setPostType(post.getPostType());
        dto.setLocation(post.getLocation());
        dto.setDateLostOrFound(post.getDateLostOrFound());
        dto.setContactInformation(post.getContactInformation());
        dto.setImageUrl(post.getImagePath() != null ? "/uploads/" + post.getImagePath() : null);
        dto.setStatus(post.getStatus());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setOwnerId(post.getOwner().getId());
        dto.setOwnerName(post.getOwner().getFullName());
        dto.setOwnerDepartment(post.getOwner().getDepartment());
        dto.setOwnedByCurrentUser(ownedByCurrentUser);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemCategory getCategory() { return category; }
    public void setCategory(ItemCategory category) { this.category = category; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDateLostOrFound() { return dateLostOrFound; }
    public void setDateLostOrFound(LocalDate dateLostOrFound) { this.dateLostOrFound = dateLostOrFound; }

    public String getContactInformation() { return contactInformation; }
    public void setContactInformation(String contactInformation) { this.contactInformation = contactInformation; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerDepartment() { return ownerDepartment; }
    public void setOwnerDepartment(String ownerDepartment) { this.ownerDepartment = ownerDepartment; }

    public boolean isOwnedByCurrentUser() { return ownedByCurrentUser; }
    public void setOwnedByCurrentUser(boolean ownedByCurrentUser) { this.ownedByCurrentUser = ownedByCurrentUser; }
}
