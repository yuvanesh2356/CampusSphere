package com.campussphere.admin.service;

import com.campussphere.admin.dto.AdminDashboardStatsDTO;
import com.campussphere.auth.dto.UserProfileDTO;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.freelance.dto.FreelanceServiceResponseDTO;
import com.campussphere.freelance.service.FreelanceServiceManager;
import com.campussphere.guidance.dto.GuidancePostResponseDTO;
import com.campussphere.guidance.service.GuidanceServiceManager;
import com.campussphere.lostfound.dto.LostFoundResponseDTO;
import com.campussphere.lostfound.service.LostFoundServiceManager;
import com.campussphere.marketplace.dto.MarketplaceListingResponseDTO;
import com.campussphere.marketplace.service.MarketplaceListingService;
import com.campussphere.notification.entity.NotificationType;
import com.campussphere.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Admin module. Deliberately does not go
 * through a generic/abstracted "content" concept - it composes the
 * existing per-module services (MarketplaceListingService,
 * FreelanceServiceManager, GuidanceServiceManager,
 * LostFoundServiceManager) exactly the way any other consumer of
 * those services would, calling their getAllForAdmin()/adminDelete()
 * methods added specifically to support this module. This keeps each
 * module's own service as the single source of truth for its data,
 * rather than duplicating query logic here.
 *
 * Every public method here is only ever reachable through
 * AdminController, which SecurityConfig already restricts to
 * ROLE_ADMIN - this class does not re-check the role itself, the same
 * way MarketplaceListingService doesn't re-check "is this user logged
 * in" (that's the filter chain's job, not the service layer's).
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final MarketplaceListingService marketplaceListingService;
    private final FreelanceServiceManager freelanceServiceManager;
    private final GuidanceServiceManager guidanceServiceManager;
    private final LostFoundServiceManager lostFoundServiceManager;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository,
                         MarketplaceListingService marketplaceListingService,
                         FreelanceServiceManager freelanceServiceManager,
                         GuidanceServiceManager guidanceServiceManager,
                         LostFoundServiceManager lostFoundServiceManager,
                         NotificationService notificationService) {
        this.userRepository = userRepository;
        this.marketplaceListingService = marketplaceListingService;
        this.freelanceServiceManager = freelanceServiceManager;
        this.guidanceServiceManager = guidanceServiceManager;
        this.lostFoundServiceManager = lostFoundServiceManager;
        this.notificationService = notificationService;
    }

    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public AdminDashboardStatsDTO getDashboardStats() {
        return new AdminDashboardStatsDTO(
                userRepository.count(),
                marketplaceListingService.getAllForAdmin().size(),
                freelanceServiceManager.getAllForAdmin().size(),
                guidanceServiceManager.getAllForAdmin().size(),
                lostFoundServiceManager.getAllForAdmin().size()
        );
    }

    public List<MarketplaceListingResponseDTO> getAllMarketplaceListings() {
        return marketplaceListingService.getAllForAdmin();
    }

    public List<FreelanceServiceResponseDTO> getAllFreelanceServices() {
        return freelanceServiceManager.getAllForAdmin();
    }

    public List<GuidancePostResponseDTO> getAllGuidancePosts() {
        return guidanceServiceManager.getAllForAdmin();
    }

    public List<LostFoundResponseDTO> getAllLostFoundPosts() {
        return lostFoundServiceManager.getAllForAdmin();
    }

    @Transactional
    public void deleteMarketplaceListing(Long id) {
        MarketplaceListingResponseDTO listing = marketplaceListingService.getListingById(id, "admin@system.local");
        marketplaceListingService.adminDelete(id);
        notifyOwner(listing.getSellerId(),
                "Your marketplace listing \"" + listing.getTitle() + "\" was removed by an administrator.",
                "/marketplace/my-listings");
    }

    @Transactional
    public void deleteFreelanceService(Long id) {
        FreelanceServiceResponseDTO service = freelanceServiceManager.getServiceById(id, "admin@system.local");
        freelanceServiceManager.adminDelete(id);
        notifyOwner(service.getSellerId(),
                "Your freelance service \"" + service.getTitle() + "\" was removed by an administrator.",
                "/freelance/my-services");
    }

    @Transactional
    public void deleteGuidancePost(Long id) {
        GuidancePostResponseDTO post = guidanceServiceManager.getPostById(id, "admin@system.local");
        guidanceServiceManager.adminDelete(id);
        notifyOwner(post.getAuthorId(),
                "Your guidance post \"" + post.getTitle() + "\" was removed by an administrator.",
                "/guidance/my-guidance");
    }

    @Transactional
    public void deleteLostFoundPost(Long id) {
        LostFoundResponseDTO post = lostFoundServiceManager.getPostById(id, "admin@system.local");
        lostFoundServiceManager.adminDelete(id);
        notifyOwner(post.getOwnerId(),
                "Your Lost & Found post \"" + post.getTitle() + "\" was removed by an administrator.",
                "/lostfound/my-posts");
    }

    private void notifyOwner(Long ownerId, String message, String link) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ownerId));
        notificationService.notify(owner, NotificationType.CONTENT_MODERATED, message, link);
    }
}
