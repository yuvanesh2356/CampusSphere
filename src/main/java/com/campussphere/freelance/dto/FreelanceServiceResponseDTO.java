package com.campussphere.freelance.dto;

import com.campussphere.freelance.entity.AvailabilityStatus;
import com.campussphere.freelance.entity.FreelanceService;
import com.campussphere.freelance.entity.PriceType;
import com.campussphere.freelance.entity.ServiceCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outgoing representation of a freelance service, used by both the
 * browse/search page and the detail page. Mirrors
 * MarketplaceListingResponseDTO's design: carries seller info directly
 * so templates never reach into the User entity, and an
 * ownedByCurrentUser flag so templates can conditionally show
 * Edit/Delete buttons without repeating ownership logic in HTML.
 */
public class FreelanceServiceResponseDTO {

    private Long id;
    private String title;
    private ServiceCategory category;
    private String description;
    private BigDecimal price;
    private PriceType priceType;
    private String contactInfo;
    private String sampleImageUrl;
    private AvailabilityStatus status;
    private LocalDateTime createdAt;

    private Long sellerId;
    private String sellerName;
    private String sellerDepartment;

    private boolean ownedByCurrentUser;

    public FreelanceServiceResponseDTO() {
    }

    /**
     * Converts an entity into its safe, external-facing representation.
     * sampleImageUrl is resolved here (not left as a raw file path) so
     * templates can bind directly to it in an <img> src attribute.
     */
    public static FreelanceServiceResponseDTO fromEntity(FreelanceService service, boolean ownedByCurrentUser) {
        FreelanceServiceResponseDTO dto = new FreelanceServiceResponseDTO();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setCategory(service.getCategory());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setPriceType(service.getPriceType());
        dto.setContactInfo(service.getContactInfo());
        dto.setSampleImageUrl(service.getSampleImagePath() != null ? "/uploads/" + service.getSampleImagePath() : null);
        dto.setStatus(service.getStatus());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setSellerId(service.getSeller().getId());
        dto.setSellerName(service.getSeller().getFullName());
        dto.setSellerDepartment(service.getSeller().getDepartment());
        dto.setOwnedByCurrentUser(ownedByCurrentUser);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public PriceType getPriceType() { return priceType; }
    public void setPriceType(PriceType priceType) { this.priceType = priceType; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getSampleImageUrl() { return sampleImageUrl; }
    public void setSampleImageUrl(String sampleImageUrl) { this.sampleImageUrl = sampleImageUrl; }

    public AvailabilityStatus getStatus() { return status; }
    public void setStatus(AvailabilityStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerDepartment() { return sellerDepartment; }
    public void setSellerDepartment(String sellerDepartment) { this.sellerDepartment = sellerDepartment; }

    public boolean isOwnedByCurrentUser() { return ownedByCurrentUser; }
    public void setOwnedByCurrentUser(boolean ownedByCurrentUser) { this.ownedByCurrentUser = ownedByCurrentUser; }
}
