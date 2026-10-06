package com.campussphere.guidance.dto;

import com.campussphere.guidance.entity.GuidanceCategory;
import com.campussphere.guidance.entity.GuidancePost;
import com.campussphere.guidance.entity.VisibilityStatus;

import java.time.LocalDateTime;

/**
 * Outgoing representation of a guidance post, used by both the
 * browse/search page and the detail page. Mirrors
 * FreelanceServiceResponseDTO's design: carries author info directly
 * so templates never reach into the User entity, and an
 * ownedByCurrentUser flag so templates can conditionally show
 * Edit/Delete buttons without repeating ownership logic in HTML.
 */
public class GuidancePostResponseDTO {

    private Long id;
    private String title;
    private GuidanceCategory category;
    private String description;
    private Integer relevantYear;
    private String relevantDepartment;
    private String attachmentUrl;
    private VisibilityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long authorId;
    private String authorName;
    private String authorDepartment;
    private Integer authorYearOfStudy;

    private boolean ownedByCurrentUser;

    public GuidancePostResponseDTO() {
    }

    /**
     * Converts an entity into its safe, external-facing representation.
     * attachmentUrl is resolved here (not left as a raw file path) so
     * templates can bind directly to it in an <img>/<a> href.
     */
    public static GuidancePostResponseDTO fromEntity(GuidancePost post, boolean ownedByCurrentUser) {
        GuidancePostResponseDTO dto = new GuidancePostResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setCategory(post.getCategory());
        dto.setDescription(post.getDescription());
        dto.setRelevantYear(post.getRelevantYear());
        dto.setRelevantDepartment(post.getRelevantDepartment());
        dto.setAttachmentUrl(post.getAttachmentPath() != null ? "/uploads/" + post.getAttachmentPath() : null);
        dto.setStatus(post.getStatus());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorName(post.getAuthor().getFullName());
        dto.setAuthorDepartment(post.getAuthor().getDepartment());
        dto.setAuthorYearOfStudy(post.getAuthor().getYearOfStudy());
        dto.setOwnedByCurrentUser(ownedByCurrentUser);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public GuidanceCategory getCategory() { return category; }
    public void setCategory(GuidanceCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRelevantYear() { return relevantYear; }
    public void setRelevantYear(Integer relevantYear) { this.relevantYear = relevantYear; }

    public String getRelevantDepartment() { return relevantDepartment; }
    public void setRelevantDepartment(String relevantDepartment) { this.relevantDepartment = relevantDepartment; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public VisibilityStatus getStatus() { return status; }
    public void setStatus(VisibilityStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorDepartment() { return authorDepartment; }
    public void setAuthorDepartment(String authorDepartment) { this.authorDepartment = authorDepartment; }

    public Integer getAuthorYearOfStudy() { return authorYearOfStudy; }
    public void setAuthorYearOfStudy(Integer authorYearOfStudy) { this.authorYearOfStudy = authorYearOfStudy; }

    public boolean isOwnedByCurrentUser() { return ownedByCurrentUser; }
    public void setOwnedByCurrentUser(boolean ownedByCurrentUser) { this.ownedByCurrentUser = ownedByCurrentUser; }
}
