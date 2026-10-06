package com.campussphere.admin.service;

import com.campussphere.admin.dto.AdminDashboardStatsDTO;
import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.freelance.service.FreelanceServiceManager;
import com.campussphere.guidance.service.GuidanceServiceManager;
import com.campussphere.lostfound.service.LostFoundServiceManager;
import com.campussphere.marketplace.dto.MarketplaceListingResponseDTO;
import com.campussphere.marketplace.entity.ListingCategory;
import com.campussphere.marketplace.entity.ListingCondition;
import com.campussphere.marketplace.entity.ListingStatus;
import com.campussphere.marketplace.service.MarketplaceListingService;
import com.campussphere.notification.entity.NotificationType;
import com.campussphere.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminService, run against mocked dependencies.
 * Focused on the two things that matter most here: moderation delete
 * correctly notifies the content owner (not the admin), and dashboard
 * stats correctly aggregate counts across every module.
 */
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MarketplaceListingService marketplaceListingService;

    @Mock
    private FreelanceServiceManager freelanceServiceManager;

    @Mock
    private GuidanceServiceManager guidanceServiceManager;

    @Mock
    private LostFoundServiceManager lostFoundServiceManager;

    @Mock
    private NotificationService notificationService;

    private AdminService adminService;

    private User seller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminService = new AdminService(userRepository, marketplaceListingService, freelanceServiceManager,
                guidanceServiceManager, lostFoundServiceManager, notificationService);

        seller = new User("Divya Menon", "divya.menon@campus.edu.in", "hashed", "CSE", 2);
        seller.setId(7L);
        seller.setRole(Role.STUDENT);
    }

    @Test
    void deleteMarketplaceListing_removesListingAndNotifiesTheOwner() {
        MarketplaceListingResponseDTO listing = new MarketplaceListingResponseDTO();
        listing.setId(1L);
        listing.setTitle("Used Calculator");
        listing.setSellerId(7L);
        listing.setCategory(ListingCategory.CALCULATORS);
        listing.setCondition(ListingCondition.GOOD);
        listing.setPrice(new BigDecimal("500.00"));
        listing.setStatus(ListingStatus.AVAILABLE);

        when(marketplaceListingService.getListingById(1L, "admin@system.local")).thenReturn(listing);
        when(userRepository.findById(7L)).thenReturn(Optional.of(seller));

        adminService.deleteMarketplaceListing(1L);

        verify(marketplaceListingService, times(1)).adminDelete(1L);
        verify(notificationService, times(1)).notify(
                eq(seller), eq(NotificationType.CONTENT_MODERATED),
                contains("Used Calculator"), eq("/marketplace/my-listings"));
    }

    @Test
    void getDashboardStats_aggregatesCountsFromEveryModule() {
        when(userRepository.count()).thenReturn(42L);
        when(marketplaceListingService.getAllForAdmin()).thenReturn(List.of(new MarketplaceListingResponseDTO()));
        when(freelanceServiceManager.getAllForAdmin()).thenReturn(Collections.emptyList());
        when(guidanceServiceManager.getAllForAdmin()).thenReturn(Collections.emptyList());
        when(lostFoundServiceManager.getAllForAdmin()).thenReturn(Collections.emptyList());

        AdminDashboardStatsDTO stats = adminService.getDashboardStats();

        assertEquals(42L, stats.getTotalUsers());
        assertEquals(1, stats.getTotalMarketplaceListings());
        assertEquals(0, stats.getTotalFreelanceServices());
    }
}
