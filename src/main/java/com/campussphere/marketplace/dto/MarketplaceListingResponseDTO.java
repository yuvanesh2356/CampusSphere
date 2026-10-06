package com.campussphere.marketplace.dto;

import com.campussphere.marketplace.entity.ListingCategory;
import com.campussphere.marketplace.entity.ListingCondition;
import com.campussphere.marketplace.entity.ListingStatus;
import com.campussphere.marketplace.entity.MarketplaceListing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outgoing representation of a listing, used by the browse/search page,
 * the detail page, and "My Listings". Carries seller info (name,
 * department) so templates never need to reach into the User entity
 * directly, and an ownedByCurrentUser flag so templates can
 * conditionally show Edit/Delete buttons without repeating ownership
 * logic in HTML.
 */
public class MarketplaceListingResponseDTO {

    private Long id;
    private String title;
    private String description;
    private ListingCategory category;
    private ListingCondition condition;
    private BigDecimal price;
    private String contactInfo;
    private String imageUrl;
    private ListingStatus status;
    private LocalDateTime createdAt;

    private Long sellerId;
    private String sellerName;
    private String sellerDepartment;

    private boolean ownedByCurrentUser;

    public MarketplaceListingResponseDTO() {
    }

    /**
     * Converts an entity into its safe, external-facing representation.
     * imageUrl is resolved here (not left as a raw file path) so
     * templates can bind directly to it in an <img> src attribute.
     */
    public static MarketplaceListingResponseDTO fromEntity(MarketplaceListing listing, boolean ownedByCurrentUser) {
        MarketplaceListingResponseDTO dto = new MarketplaceListingResponseDTO();
        dto.setId(listing.getId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setCategory(listing.getCategory());
        dto.setCondition(listing.getCondition());
        dto.setPrice(listing.getPrice());
        dto.setContactInfo(listing.getContactInfo());
        dto.setImageUrl(listing.getImagePath() != null ? "/uploads/" + listing.getImagePath() : null);
        dto.setStatus(listing.getStatus());
        dto.setCreatedAt(listing.getCreatedAt());
        dto.setSellerId(listing.getSeller().getId());
        dto.setSellerName(listing.getSeller().getFullName());
        dto.setSellerDepartment(listing.getSeller().getDepartment());
        dto.setOwnedByCurrentUser(ownedByCurrentUser);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerDepartment() {
        return sellerDepartment;
    }

    public void setSellerDepartment(String sellerDepartment) {
        this.sellerDepartment = sellerDepartment;
    }

    public boolean isOwnedByCurrentUser() {
        return ownedByCurrentUser;
    }

    public void setOwnedByCurrentUser(boolean ownedByCurrentUser) {
        this.ownedByCurrentUser = ownedByCurrentUser;
    }
}
