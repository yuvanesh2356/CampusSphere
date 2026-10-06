package com.campussphere.opportunity.service;

import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.opportunity.dto.OpportunityCreateDTO;
import com.campussphere.opportunity.dto.OpportunityResponseDTO;
import com.campussphere.opportunity.dto.OpportunityUpdateDTO;
import com.campussphere.opportunity.entity.Opportunity;
import com.campussphere.opportunity.entity.OpportunityBookmark;
import com.campussphere.opportunity.entity.OpportunityCategory;
import com.campussphere.opportunity.entity.OpportunityMode;
import com.campussphere.opportunity.repository.OpportunityBookmarkRepository;
import com.campussphere.opportunity.repository.OpportunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for Global Opportunities. No naming collision concern
 * here (entity is Opportunity, not OpportunityService), so the
 * standard [Feature]Service convention applies cleanly, same as
 * NotificationService.
 *
 * Unlike the four content modules, there is no ownership-based
 * edit/delete check - curation is admin-only, enforced entirely at
 * the controller level via @PreAuthorize("hasRole('ADMIN')"), the
 * same pattern AdminController already uses. This service does not
 * re-check the role itself, consistent with how every other service
 * in this project leaves authentication/authorization to the
 * filter chain and controller layer.
 */
@Service
public class OpportunityService {

    private static final String UPLOAD_SUBDIRECTORY = "opportunity";

    private final OpportunityRepository opportunityRepository;
    private final OpportunityBookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public OpportunityService(OpportunityRepository opportunityRepository,
                               OpportunityBookmarkRepository bookmarkRepository,
                               UserRepository userRepository,
                               FileStorageService fileStorageService) {
        this.opportunityRepository = opportunityRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public OpportunityResponseDTO createOpportunity(String adminEmail, OpportunityCreateDTO request) {
        User admin = getUserByEmail(adminEmail);

        Opportunity opportunity = new Opportunity();
        opportunity.setPostedBy(admin);
        opportunity.setTitle(request.getTitle());
        opportunity.setOrganization(request.getOrganization());
        opportunity.setDescription(request.getDescription());
        opportunity.setCategory(request.getCategory());
        opportunity.setEventDate(request.getEventDate());
        opportunity.setRegistrationDeadline(request.getRegistrationDeadline());
        opportunity.setMode(request.getMode());
        opportunity.setLocation(request.getLocation());
        opportunity.setPrizeBenefits(request.getPrizeBenefits());
        opportunity.setRegistrationUrl(request.getRegistrationUrl());

        String storedImagePath = fileStorageService.store(request.getBannerImage(), UPLOAD_SUBDIRECTORY);
        opportunity.setBannerImagePath(storedImagePath);

        Opportunity saved = opportunityRepository.save(opportunity);
        return OpportunityResponseDTO.fromEntity(saved, false);
    }

    @Transactional
    public OpportunityResponseDTO updateOpportunity(Long opportunityId, OpportunityUpdateDTO request) {
        Opportunity opportunity = getOpportunityOrThrow(opportunityId);

        opportunity.setTitle(request.getTitle());
        opportunity.setOrganization(request.getOrganization());
        opportunity.setDescription(request.getDescription());
        opportunity.setCategory(request.getCategory());
        opportunity.setEventDate(request.getEventDate());
        opportunity.setRegistrationDeadline(request.getRegistrationDeadline());
        opportunity.setMode(request.getMode());
        opportunity.setLocation(request.getLocation());
        opportunity.setPrizeBenefits(request.getPrizeBenefits());
        opportunity.setRegistrationUrl(request.getRegistrationUrl());
        opportunity.setStatus(request.getStatus());

        if (request.getBannerImage() != null && !request.getBannerImage().isEmpty()) {
            fileStorageService.delete(opportunity.getBannerImagePath());
            String newImagePath = fileStorageService.store(request.getBannerImage(), UPLOAD_SUBDIRECTORY);
            opportunity.setBannerImagePath(newImagePath);
        }

        Opportunity saved = opportunityRepository.save(opportunity);
        return OpportunityResponseDTO.fromEntity(saved, false);
    }

    @Transactional
    public void deleteOpportunity(Long opportunityId) {
        Opportunity opportunity = getOpportunityOrThrow(opportunityId);
        fileStorageService.delete(opportunity.getBannerImagePath());
        opportunityRepository.delete(opportunity);
    }

    public OpportunityResponseDTO getOpportunityById(Long opportunityId, String currentUserEmail) {
        Opportunity opportunity = getOpportunityOrThrow(opportunityId);
        User user = getUserByEmail(currentUserEmail);
        boolean bookmarked = bookmarkRepository.existsByUserIdAndOpportunityId(user.getId(), opportunityId);
        return OpportunityResponseDTO.fromEntity(opportunity, bookmarked);
    }

    /**
     * Public discovery feed. category, mode, and keyword are all
     * optional. Excludes CLOSED opportunities (enforced in the
     * repository query).
     */
    public List<OpportunityResponseDTO> browseOpportunities(OpportunityCategory category, OpportunityMode mode,
                                                              String keyword, String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return opportunityRepository.searchActive(category, mode, trimmedKeyword).stream()
                .map(o -> OpportunityResponseDTO.fromEntity(
                        o, bookmarkRepository.existsByUserIdAndOpportunityId(user.getId(), o.getId())))
                .collect(Collectors.toList());
    }

    /**
     * "My Saved" - every opportunity the current user has bookmarked,
     * regardless of status, so a saved item never silently disappears
     * from a student's own list even after it closes.
     */
    public List<OpportunityResponseDTO> getSavedOpportunities(String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(bookmark -> OpportunityResponseDTO.fromEntity(bookmark.getOpportunity(), true))
                .collect(Collectors.toList());
    }

    /**
     * Toggles the bookmark for the given opportunity: adds it if not
     * already saved, removes it if already saved. Returns the new
     * bookmarked state so the controller can report it back without a
     * second lookup.
     */
    @Transactional
    public boolean toggleBookmark(Long opportunityId, String currentUserEmail) {
        User user = getUserByEmail(currentUserEmail);
        Opportunity opportunity = getOpportunityOrThrow(opportunityId);

        return bookmarkRepository.findByUserIdAndOpportunityId(user.getId(), opportunityId)
                .map(existing -> {
                    bookmarkRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    bookmarkRepository.save(new OpportunityBookmark(user, opportunity));
                    return true;
                });
    }

    // ---------- Internal helpers ----------

    private Opportunity getOpportunityOrThrow(Long opportunityId) {
        return opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }
}
