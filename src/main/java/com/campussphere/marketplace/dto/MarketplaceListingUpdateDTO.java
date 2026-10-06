package com.campussphere.marketplace.dto;

import com.campussphere.marketplace.entity.ListingCategory;
import com.campussphere.marketplace.entity.ListingCondition;
import com.campussphere.marketplace.entity.ListingStatus;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * Incoming payload for the "Edit Listing" form. Similar shape to
 * MarketplaceListingCreateDTO, but also allows the seller to change
 * the listing's status (e.g. mark as Reserved or Sold), and the image
 * is optional - if omitted, the existing image is left untouched.
 */
public class MarketplaceListingUpdateDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private ListingCategory category;

    @NotNull(message = "Condition is required")
    private ListingCondition condition;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must be a valid amount")
    private BigDecimal price;

    @NotBlank(message = "Contact information is required")
    @Size(max = 100, message = "Contact information must not exceed 100 characters")
    private String contactInfo;

    @NotNull(message = "Status is required")
    private ListingStatus status;

    /**
     * Optional replacement image. Null/empty means "keep the current image".
     */
    private MultipartFile image;

    public MarketplaceListingUpdateDTO() {
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

    public ListingCategory getCategory() {
        return category;
    }

    public void setCategory(ListingCategory category) {
        this.category = category;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public void setCondition(ListingCondition condition) {
        this.condition = condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
