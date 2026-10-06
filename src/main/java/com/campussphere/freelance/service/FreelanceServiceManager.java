package com.campussphere.freelance.service;

import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.freelance.dto.FreelanceServiceCreateDTO;
import com.campussphere.freelance.dto.FreelanceServiceResponseDTO;
import com.campussphere.freelance.dto.FreelanceServiceUpdateDTO;
import com.campussphere.freelance.entity.FreelanceService;
import com.campussphere.freelance.entity.ServiceCategory;
import com.campussphere.freelance.repository.FreelanceServiceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Freelance Hub module. Named
 * FreelanceServiceManager rather than following the strict
 * "[Feature]Service" naming convention used elsewhere (e.g.
 * MarketplaceListingService for MarketplaceListing) - the entity here
 * is itself named FreelanceService, so a literal application of that
 * convention would produce "FreelanceServiceService", which reads
 * ambiguously next to Spring's own @Service stereotype. "Manager" is
 * used purely as a naming clarity choice; this class fills the exact
 * same architectural role Service classes do everywhere else in the
 * project (business rules, orchestration, called only from the
 * controller, never called directly by the repository).
 *
 * Structure otherwise mirrors MarketplaceListingService exactly: only
 * the owner may edit/delete their own service, images go through the
 * shared FileStorageService, and browsing only ever surfaces AVAILABLE
 * services.
 */
@Service
public class FreelanceServiceManager {

    private static final String UPLOAD_SUBDIRECTORY = "freelance";

    private final FreelanceServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public FreelanceServiceManager(FreelanceServiceRepository serviceRepository,
                                    UserRepository userRepository,
                                    FileStorageService fileStorageService) {
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public FreelanceServiceResponseDTO createService(String sellerEmail, FreelanceServiceCreateDTO request) {
        User seller = getUserByEmail(sellerEmail);

        FreelanceService service = new FreelanceService();
        service.setSeller(seller);
        service.setTitle(request.getTitle());
        service.setCategory(request.getCategory());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setPriceType(request.getPriceType());
        service.setContactInfo(request.getContactInfo());

        String storedImagePath = fileStorageService.store(request.getSampleImage(), UPLOAD_SUBDIRECTORY);
        service.setSampleImagePath(storedImagePath);

        FreelanceService saved = serviceRepository.save(service);
        return FreelanceServiceResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public FreelanceServiceResponseDTO updateService(Long serviceId, String currentUserEmail, FreelanceServiceUpdateDTO request) {
        FreelanceService service = getServiceOrThrow(serviceId);
        assertOwnership(service, currentUserEmail);

        service.setTitle(request.getTitle());
        service.setCategory(request.getCategory());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setPriceType(request.getPriceType());
        service.setContactInfo(request.getContactInfo());
        service.setStatus(request.getStatus());

        if (request.getSampleImage() != null && !request.getSampleImage().isEmpty()) {
            fileStorageService.delete(service.getSampleImagePath());
            String newImagePath = fileStorageService.store(request.getSampleImage(), UPLOAD_SUBDIRECTORY);
            service.setSampleImagePath(newImagePath);
        }

        FreelanceService saved = serviceRepository.save(service);
        return FreelanceServiceResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public void deleteService(Long serviceId, String currentUserEmail) {
        FreelanceService service = getServiceOrThrow(serviceId);
        assertOwnership(service, currentUserEmail);

        fileStorageService.delete(service.getSampleImagePath());
        serviceRepository.delete(service);
    }

    public FreelanceServiceResponseDTO getServiceById(Long serviceId, String currentUserEmail) {
        FreelanceService service = getServiceOrThrow(serviceId);
        boolean owned = service.getSeller().getEmail().equalsIgnoreCase(currentUserEmail);
        return FreelanceServiceResponseDTO.fromEntity(service, owned);
    }

    /**
     * Public browse/search. category and keyword are both optional -
     * pass null for either to skip that filter. Only AVAILABLE services
     * are returned (enforced in the repository query).
     */
    public List<FreelanceServiceResponseDTO> browseServices(ServiceCategory category, String keyword, String currentUserEmail) {
        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return serviceRepository.searchAvailable(category, trimmedKeyword).stream()
                .map(service -> FreelanceServiceResponseDTO.fromEntity(
                        service, service.getSeller().getEmail().equalsIgnoreCase(currentUserEmail)))
                .collect(Collectors.toList());
    }

    /**
     * "My Services" - every service owned by the current user,
     * regardless of status, so they can manage their full history.
     */
    public List<FreelanceServiceResponseDTO> getMyServices(String currentUserEmail) {
        User seller = getUserByEmail(currentUserEmail);
        return serviceRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId()).stream()
                .map(service -> FreelanceServiceResponseDTO.fromEntity(service, true))
                .collect(Collectors.toList());
    }

    /**
     * Full moderation list for the Admin module - every service
     * regardless of status. Mirrors MarketplaceListingService.getAllForAdmin().
     */
    public List<FreelanceServiceResponseDTO> getAllForAdmin() {
        return serviceRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(service -> FreelanceServiceResponseDTO.fromEntity(service, false))
                .collect(Collectors.toList());
    }

    /**
     * Admin-only delete - bypasses the ownership check, safe only
     * because it is exclusively reachable from the role-gated
     * AdminController. Mirrors MarketplaceListingService.adminDelete().
     */
    @Transactional
    public void adminDelete(Long serviceId) {
        FreelanceService service = getServiceOrThrow(serviceId);
        fileStorageService.delete(service.getSampleImagePath());
        serviceRepository.delete(service);
    }

    // ---------- Internal helpers ----------

    private FreelanceService getServiceOrThrow(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }

    private void assertOwnership(FreelanceService service, String currentUserEmail) {
        if (!service.getSeller().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedActionException("You can only edit or delete your own services");
        }
    }
}
