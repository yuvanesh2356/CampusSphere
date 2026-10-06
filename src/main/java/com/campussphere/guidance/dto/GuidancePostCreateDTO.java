package com.campussphere.guidance.dto;

import com.campussphere.guidance.entity.GuidanceCategory;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Incoming payload for the "Create Guidance" form. Bound via
 * @ModelAttribute, same multipart/form-data pattern
 * MarketplaceListingCreateDTO and FreelanceServiceCreateDTO use.
 * relevantYear and relevantDepartment are intentionally not annotated
 * with @NotNull/@NotBlank - both are optional targeting metadata per
 * the module requirements.
 */
public class GuidancePostCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotNull(message = "Category is required")
    private GuidanceCategory category;

    @NotBlank(message = "Description is required")
    @Size(max = 3000, message = "Description must not exceed 3000 characters")
    private String description;

    @Min(value = 1, message = "Relevant year must be between 1 and 4")
    @Max(value = 4, message = "Relevant year must be between 1 and 4")
    private Integer relevantYear;

    @Size(max = 100, message = "Relevant department must not exceed 100 characters")
    private String relevantDepartment;

    /** Optional - a post can be created without an attachment. */
    private MultipartFile attachment;

    public GuidancePostCreateDTO() {
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

    public MultipartFile getAttachment() {
        return attachment;
    }

    public void setAttachment(MultipartFile attachment) {
        this.attachment = attachment;
    }
}
