package com.campussphere.opportunity.dto;

import com.campussphere.opportunity.entity.Opportunity;
import com.campussphere.opportunity.entity.OpportunityCategory;
import com.campussphere.opportunity.entity.OpportunityMode;
import com.campussphere.opportunity.entity.OpportunityStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outgoing representation of an opportunity. Carries a
 * bookmarkedByCurrentUser flag (computed by the service, not stored on
 * the entity) so templates can show a filled/outline bookmark icon
 * without any bookmark-lookup logic in HTML - the same pattern
 * ownedByCurrentUser follows on every other module's response DTO.
 */
public class OpportunityResponseDTO {

    private Long id;
    private String title;
    private String organization;
    private String description;
    private OpportunityCategory category;
    private LocalDate eventDate;
    private LocalDate registrationDeadline;
    private OpportunityMode mode;
    private String location;
    private String prizeBenefits;
    private String bannerImageUrl;
    private String registrationUrl;
    private OpportunityStatus status;
    private LocalDateTime createdAt;

    private boolean bookmarkedByCurrentUser;

    public OpportunityResponseDTO() {
    }

    public static OpportunityResponseDTO fromEntity(Opportunity opportunity, boolean bookmarkedByCurrentUser) {
        OpportunityResponseDTO dto = new OpportunityResponseDTO();
        dto.setId(opportunity.getId());
        dto.setTitle(opportunity.getTitle());
        dto.setOrganization(opportunity.getOrganization());
        dto.setDescription(opportunity.getDescription());
        dto.setCategory(opportunity.getCategory());
        dto.setEventDate(opportunity.getEventDate());
        dto.setRegistrationDeadline(opportunity.getRegistrationDeadline());
        dto.setMode(opportunity.getMode());
        dto.setLocation(opportunity.getLocation());
        dto.setPrizeBenefits(opportunity.getPrizeBenefits());
        dto.setBannerImageUrl(opportunity.getBannerImagePath() != null ? "/uploads/" + opportunity.getBannerImagePath() : null);
        dto.setRegistrationUrl(opportunity.getRegistrationUrl());
        dto.setStatus(opportunity.getStatus());
        dto.setCreatedAt(opportunity.getCreatedAt());
        dto.setBookmarkedByCurrentUser(bookmarkedByCurrentUser);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OpportunityCategory getCategory() { return category; }
    public void setCategory(OpportunityCategory category) { this.category = category; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalDate getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDate registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public OpportunityMode getMode() { return mode; }
    public void setMode(OpportunityMode mode) { this.mode = mode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPrizeBenefits() { return prizeBenefits; }
    public void setPrizeBenefits(String prizeBenefits) { this.prizeBenefits = prizeBenefits; }

    public String getBannerImageUrl() { return bannerImageUrl; }
    public void setBannerImageUrl(String bannerImageUrl) { this.bannerImageUrl = bannerImageUrl; }

    public String getRegistrationUrl() { return registrationUrl; }
    public void setRegistrationUrl(String registrationUrl) { this.registrationUrl = registrationUrl; }

    public OpportunityStatus getStatus() { return status; }
    public void setStatus(OpportunityStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isBookmarkedByCurrentUser() { return bookmarkedByCurrentUser; }
    public void setBookmarkedByCurrentUser(boolean bookmarkedByCurrentUser) { this.bookmarkedByCurrentUser = bookmarkedByCurrentUser; }
}
