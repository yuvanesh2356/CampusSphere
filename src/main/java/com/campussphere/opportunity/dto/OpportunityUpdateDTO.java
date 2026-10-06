package com.campussphere.opportunity.dto;

import com.campussphere.opportunity.entity.OpportunityCategory;
import com.campussphere.opportunity.entity.OpportunityMode;
import com.campussphere.opportunity.entity.OpportunityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Incoming payload for editing an existing opportunity (admin-only).
 * Same shape as OpportunityCreateDTO plus status, and the banner image
 * is optional - if omitted, the existing image is left untouched.
 */
public class OpportunityUpdateDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Organization is required")
    @Size(max = 150, message = "Organization must not exceed 150 characters")
    private String organization;

    @NotBlank(message = "Description is required")
    @Size(max = 3000, message = "Description must not exceed 3000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private OpportunityCategory category;

    @NotNull(message = "Event date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @NotNull(message = "Registration deadline is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationDeadline;

    @NotNull(message = "Mode is required")
    private OpportunityMode mode;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 500, message = "Prize/benefits must not exceed 500 characters")
    private String prizeBenefits;

    @NotBlank(message = "Registration URL is required")
    @Size(max = 500, message = "Registration URL must not exceed 500 characters")
    private String registrationUrl;

    @NotNull(message = "Status is required")
    private OpportunityStatus status;

    /** Optional replacement banner. Null/empty means "keep the current one". */
    private MultipartFile bannerImage;

    public OpportunityUpdateDTO() {
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

    public MultipartFile getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(MultipartFile bannerImage) {
        this.bannerImage = bannerImage;
    }
}
