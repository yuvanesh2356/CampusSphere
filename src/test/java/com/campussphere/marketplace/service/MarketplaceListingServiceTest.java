package com.campussphere.marketplace.service;

import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.marketplace.dto.MarketplaceListingCreateDTO;
import com.campussphere.marketplace.dto.MarketplaceListingResponseDTO;
import com.campussphere.marketplace.dto.MarketplaceListingUpdateDTO;
import com.campussphere.marketplace.entity.*;
import com.campussphere.marketplace.repository.MarketplaceListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Marketplace module's core business rule: only the
 * seller who owns a listing may edit or delete it. Also covers listing
 * creation and browsing at a basic level. Run against mocked
 * dependencies - no database or Spring context required.
 */
class MarketplaceListingServiceTest {

    @Mock
    private MarketplaceListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    private MarketplaceListingService listingService;

    private User seller;
    private User otherStudent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listingService = new MarketplaceListingService(listingRepository, userRepository, fileStorageService);

        seller = new User("Asha Rao", "asha.rao@campus.edu.in", "hashed", "CSE", 2);
        seller.setId(1L);
        seller.setRole(Role.STUDENT);

        otherStudent = new User("Ravi Kumar", "ravi.kumar@campus.edu.in", "hashed", "ECE", 3);
        otherStudent.setId(2L);
        otherStudent.setRole(Role.STUDENT);
    }

    @Test
    void createListing_savesWithSellerAndDefaultAvailableStatus() {
        MarketplaceListingCreateDTO request = new MarketplaceListingCreateDTO();
        request.setTitle("Data Structures Textbook");
        request.setDescription("Barely used, 3rd edition");
        request.setCategory(ListingCategory.BOOKS);
        request.setCondition(ListingCondition.GOOD);
        request.setPrice(new BigDecimal("450.00"));
        request.setContactInfo("asha.rao@campus.edu.in");

        when(userRepository.findByEmail(seller.getEmail())).thenReturn(Optional.of(seller));
        when(fileStorageService.store(any(), eq("marketplace"))).thenReturn(null);
        when(listingRepository.save(any(MarketplaceListing.class))).thenAnswer(invocation -> {
            MarketplaceListing l = invocation.getArgument(0);
            l.setId(10L);
            return l;
        });

        MarketplaceListingResponseDTO result = listingService.createListing(seller.getEmail(), request);

        assertEquals("Data Structures Textbook", result.getTitle());
        assertEquals(ListingStatus.AVAILABLE, result.getStatus());
        assertTrue(result.isOwnedByCurrentUser());
        verify(listingRepository, times(1)).save(any(MarketplaceListing.class));
    }

    @Test
    void updateListing_throwsWhenCurrentUserIsNotTheSeller() {
        MarketplaceListing listing = buildListing(seller);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        MarketplaceListingUpdateDTO request = new MarketplaceListingUpdateDTO();
        request.setTitle("Attempted takeover edit");
        request.setDescription("desc");
        request.setCategory(ListingCategory.BOOKS);
        request.setCondition(ListingCondition.GOOD);
        request.setPrice(new BigDecimal("100.00"));
        request.setContactInfo("ravi.kumar@campus.edu.in");
        request.setStatus(ListingStatus.AVAILABLE);

        assertThrows(UnauthorizedActionException.class,
                () -> listingService.updateListing(10L, otherStudent.getEmail(), request));

        // Ownership check must fail before any save is attempted.
        verify(listingRepository, never()).save(any(MarketplaceListing.class));
    }

    @Test
    void deleteListing_throwsWhenCurrentUserIsNotTheSeller() {
        MarketplaceListing listing = buildListing(seller);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThrows(UnauthorizedActionException.class,
                () -> listingService.deleteListing(10L, otherStudent.getEmail()));

        verify(listingRepository, never()).delete(any(MarketplaceListing.class));
    }

    @Test
    void deleteListing_succeedsForTheActualOwner() {
        MarketplaceListing listing = buildListing(seller);
        listing.setImagePath("marketplace/some-file.jpg");

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        listingService.deleteListing(10L, seller.getEmail());

        verify(fileStorageService, times(1)).delete("marketplace/some-file.jpg");
        verify(listingRepository, times(1)).delete(listing);
    }

    @Test
    void getListingById_throwsResourceNotFoundWhenMissing() {
        when(listingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> listingService.getListingById(99L, seller.getEmail()));
    }

    @Test
    void browseListings_marksOwnershipCorrectlyForEachResult() {
        MarketplaceListing ownListing = buildListing(seller);
        MarketplaceListing othersListing = buildListing(otherStudent);

        when(listingRepository.searchAvailable(null, null)).thenReturn(List.of(ownListing, othersListing));

        List<MarketplaceListingResponseDTO> results = listingService.browseListings(null, null, seller.getEmail());

        assertEquals(2, results.size());
        assertTrue(results.get(0).isOwnedByCurrentUser());
        assertFalse(results.get(1).isOwnedByCurrentUser());
    }

    private MarketplaceListing buildListing(User owner) {
        MarketplaceListing listing = new MarketplaceListing();
        listing.setId(10L);
        listing.setSeller(owner);
        listing.setTitle("Sample Listing");
        listing.setDescription("Sample description");
        listing.setCategory(ListingCategory.BOOKS);
        listing.setCondition(ListingCondition.GOOD);
        listing.setPrice(new BigDecimal("100.00"));
        listing.setContactInfo(owner.getEmail());
        listing.setStatus(ListingStatus.AVAILABLE);
        return listing;
    }
}
