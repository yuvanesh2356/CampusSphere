package com.campussphere.marketplace.service;

import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.marketplace.dto.MarketplaceListingCreateDTO;
import com.campussphere.marketplace.dto.MarketplaceListingResponseDTO;
import com.campussphere.marketplace.dto.MarketplaceListingUpdateDTO;
import com.campussphere.marketplace.entity.ListingCategory;
import com.campussphere.marketplace.entity.MarketplaceListing;
import com.campussphere.marketplace.repository.MarketplaceListingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Buy & Sell Marketplace module. Owns every rule
 * that matters here: only the seller may edit/delete their own listing,
 * images are stored via the shared FileStorageService, and browsing
 * only ever surfaces AVAILABLE listings.
 *
 * Reuses UserRepository directly (read-only lookups) rather than going
 * through UserService, since UserService's responsibility is
 * registration/profile - not resolving "who is the currently
 * authenticated user" for another module. This keeps auth and
 * marketplace loosely coupled while still reusing the existing User entity.
 */
@Service
public class MarketplaceListingService {

    private static final String UPLOAD_SUBDIRECTORY = "marketplace";

    private final MarketplaceListingRepository listingRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public MarketplaceListingService(MarketplaceListingRepository listingRepository,
                                      UserRepository userRepository,
                                      FileStorageService fileStorageService) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public MarketplaceListingResponseDTO createListing(String sellerEmail, MarketplaceListingCreateDTO request) {
        User seller = getUserByEmail(sellerEmail);

        MarketplaceListing listing = new MarketplaceListing();
        listing.setSeller(seller);
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory());
        listing.setCondition(request.getCondition());
        listing.setPrice(request.getPrice());
        listing.setContactInfo(request.getContactInfo());

        String storedImagePath = fileStorageService.store(request.getImage(), UPLOAD_SUBDIRECTORY);
        listing.setImagePath(storedImagePath);

        MarketplaceListing saved = listingRepository.save(listing);
        return MarketplaceListingResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public MarketplaceListingResponseDTO updateListing(Long listingId, String currentUserEmail, MarketplaceListingUpdateDTO request) {
        MarketplaceListing listing = getListingOrThrow(listingId);
        assertOwnership(listing, currentUserEmail);

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory());
        listing.setCondition(request.getCondition());
        listing.setPrice(request.getPrice());
        listing.setContactInfo(request.getContactInfo());
        listing.setStatus(request.getStatus());

        // Only replace the image if a new one was actually uploaded -
        // otherwise the existing photo is left untouched.
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            fileStorageService.delete(listing.getImagePath());
            String newImagePath = fileStorageService.store(request.getImage(), UPLOAD_SUBDIRECTORY);
            listing.setImagePath(newImagePath);
        }

        MarketplaceListing saved = listingRepository.save(listing);
        return MarketplaceListingResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public void deleteListing(Long listingId, String currentUserEmail) {
        MarketplaceListing listing = getListingOrThrow(listingId);
        assertOwnership(listing, currentUserEmail);

        fileStorageService.delete(listing.getImagePath());
        listingRepository.delete(listing);
    }

    public MarketplaceListingResponseDTO getListingById(Long listingId, String currentUserEmail) {
        MarketplaceListing listing = getListingOrThrow(listingId);
        boolean owned = listing.getSeller().getEmail().equalsIgnoreCase(currentUserEmail);
        return MarketplaceListingResponseDTO.fromEntity(listing, owned);
    }

    /**
     * Public browse/search. category and keyword are both optional -
     * pass null for either to skip that filter. Only AVAILABLE listings
     * are returned (enforced in the repository query).
     */
    public List<MarketplaceListingResponseDTO> browseListings(ListingCategory category, String keyword, String currentUserEmail) {
        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return listingRepository.searchAvailable(category, trimmedKeyword).stream()
                .map(listing -> MarketplaceListingResponseDTO.fromEntity(
                        listing, listing.getSeller().getEmail().equalsIgnoreCase(currentUserEmail)))
                .collect(Collectors.toList());
    }

    /**
     * "My Listings" - every listing owned by the current user,
     * regardless of status, so they can manage their full history.
     */
    public List<MarketplaceListingResponseDTO> getMyListings(String currentUserEmail) {
        User seller = getUserByEmail(currentUserEmail);
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId()).stream()
                .map(listing -> MarketplaceListingResponseDTO.fromEntity(listing, true))
                .collect(Collectors.toList());
    }

    /**
     * Full moderation list for the Admin module - every listing
     * regardless of status, unlike the public browseListings() method
     * which only ever shows AVAILABLE ones.
     */
    public List<MarketplaceListingResponseDTO> getAllForAdmin() {
        return listingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(listing -> MarketplaceListingResponseDTO.fromEntity(listing, false))
                .collect(Collectors.toList());
    }

    /**
     * Admin-only delete - bypasses the ownership check in
     * assertOwnership() entirely, since an administrator moderating
     * content is not the listing's seller. This is safe only because
     * the method is exclusively reachable from AdminController, which
     * SecurityConfig already restricts to ROLE_ADMIN - it must never
     * be exposed through any student-facing endpoint.
     */
    @Transactional
    public void adminDelete(Long listingId) {
        MarketplaceListing listing = getListingOrThrow(listingId);
        fileStorageService.delete(listing.getImagePath());
        listingRepository.delete(listing);
    }

    // ---------- Internal helpers ----------

    private MarketplaceListing getListingOrThrow(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }

    private void assertOwnership(MarketplaceListing listing, String currentUserEmail) {
        if (!listing.getSeller().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedActionException("You can only edit or delete your own listings");
        }
    }
}
