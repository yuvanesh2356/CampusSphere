package com.campussphere.freelance.dto;

import com.campussphere.freelance.entity.PriceType;
import com.campussphere.freelance.entity.ServiceCategory;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * Incoming payload for the "Create Service" form. Bound via
 * @ModelAttribute, same multipart/form-data pattern
 * MarketplaceListingCreateDTO uses.
 */
public class FreelanceServiceCreateDTO {

    @NotBlank(message = "Service title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotNull(message = "Category is required")
    private ServiceCategory category;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must be a valid amount")
    private BigDecimal price;

    @NotNull(message = "Price type is required")
    private PriceType priceType;

    @NotBlank(message = "Contact information is required")
    @Size(max = 100, message = "Contact information must not exceed 100 characters")
    private String contactInfo;

    /** Optional - a service can be posted without a sample image. */
    private MultipartFile sampleImage;

    public FreelanceServiceCreateDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
        this.priceType = priceType;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public MultipartFile getSampleImage() {
        return sampleImage;
    }

    public void setSampleImage(MultipartFile sampleImage) {
        this.sampleImage = sampleImage;
    }
}
