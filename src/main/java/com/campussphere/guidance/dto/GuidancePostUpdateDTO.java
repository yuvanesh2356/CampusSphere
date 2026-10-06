package com.campussphere.guidance.dto;

import com.campussphere.guidance.entity.GuidanceCategory;
import com.campussphere.guidance.entity.VisibilityStatus;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Incoming payload for the "Edit Guidance" form. Same shape as
 * GuidancePostCreateDTO, but also allows the author to change
 * visibility status, and the attachment is optional - if omitted, the
 * existing attachment is left untouched. Mirrors
 * FreelanceServiceUpdateDTO's design exactly.
 */
public class GuidancePostUpdateDTO {

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

    @NotNull(message = "Visibility status is required")
    private VisibilityStatus status;

    /** Optional replacement attachment. Null/empty means "keep the current one". */
    private MultipartFile attachment;

    public GuidancePostUpdateDTO() {
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

    public VisibilityStatus getStatus() {
        return status;
    }

    public void setStatus(VisibilityStatus status) {
        this.status = status;
    }

    public MultipartFile getAttachment() {
        return attachment;
    }

    public void setAttachment(MultipartFile attachment) {
        this.attachment = attachment;
    }
}
