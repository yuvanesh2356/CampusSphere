package com.campussphere.opportunity.service;

import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.opportunity.dto.OpportunityCreateDTO;
import com.campussphere.opportunity.dto.OpportunityResponseDTO;
import com.campussphere.opportunity.entity.*;
import com.campussphere.opportunity.repository.OpportunityBookmarkRepository;
import com.campussphere.opportunity.repository.OpportunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OpportunityService, run against mocked dependencies.
 * Covers opportunity creation and the bookmark toggle's add/remove
 * behavior - the one piece of business logic in this module that
 * isn't just a straightforward mirror of the other four modules'
 * create/update/delete pattern.
 */
class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private OpportunityBookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    private OpportunityService opportunityService;

    private User admin;
    private User student;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        opportunityService = new OpportunityService(opportunityRepository, bookmarkRepository, userRepository, fileStorageService);

        admin = new User("Platform Admin", "admin@campus.edu.in", "hashed", "Administration", 4);
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        student = new User("Neha Kapoor", "neha.kapoor@campus.edu.in", "hashed", "CSE", 2);
        student.setId(2L);
        student.setRole(Role.STUDENT);
    }

    @Test
    void createOpportunity_savesWithDefaultOpenStatus() {
        OpportunityCreateDTO request = new OpportunityCreateDTO();
        request.setTitle("Global AI Hackathon 2026");
        request.setOrganization("DevGlobal");
        request.setDescription("48-hour hackathon for student teams");
        request.setCategory(OpportunityCategory.HACKATHON);
        request.setEventDate(LocalDate.of(2026, 11, 1));
        request.setRegistrationDeadline(LocalDate.of(2026, 10, 20));
        request.setMode(OpportunityMode.ONLINE);
        request.setRegistrationUrl("https://example.com/register");

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(fileStorageService.store(any(), eq("opportunity"))).thenReturn(null);
        when(opportunityRepository.save(any(Opportunity.class))).thenAnswer(inv -> {
            Opportunity o = inv.getArgument(0);
            o.setId(5L);
            return o;
        });

        OpportunityResponseDTO result = opportunityService.createOpportunity(admin.getEmail(), request);

        assertEquals("Global AI Hackathon 2026", result.getTitle());
        assertEquals(OpportunityStatus.OPEN, result.getStatus());
        verify(opportunityRepository, times(1)).save(any(Opportunity.class));
    }

    @Test
    void toggleBookmark_addsBookmarkWhenNoneExists() {
        Opportunity opportunity = buildOpportunity();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(opportunityRepository.findById(5L)).thenReturn(Optional.of(opportunity));
        when(bookmarkRepository.findByUserIdAndOpportunityId(2L, 5L)).thenReturn(Optional.empty());

        boolean result = opportunityService.toggleBookmark(5L, student.getEmail());

        assertTrue(result);
        verify(bookmarkRepository, times(1)).save(any(OpportunityBookmark.class));
        verify(bookmarkRepository, never()).delete(any(OpportunityBookmark.class));
    }

    @Test
    void toggleBookmark_removesBookmarkWhenAlreadySaved() {
        Opportunity opportunity = buildOpportunity();
        OpportunityBookmark existing = new OpportunityBookmark(student, opportunity);
        existing.setId(9L);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(opportunityRepository.findById(5L)).thenReturn(Optional.of(opportunity));
        when(bookmarkRepository.findByUserIdAndOpportunityId(2L, 5L)).thenReturn(Optional.of(existing));

        boolean result = opportunityService.toggleBookmark(5L, student.getEmail());

        assertFalse(result);
        verify(bookmarkRepository, times(1)).delete(existing);
        verify(bookmarkRepository, never()).save(any(OpportunityBookmark.class));
    }

    @Test
    void getSavedOpportunities_returnsBookmarkedOpportunitiesForTheUser() {
        Opportunity opportunity = buildOpportunity();
        OpportunityBookmark bookmark = new OpportunityBookmark(student, opportunity);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(bookmarkRepository.findByUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(bookmark));

        List<OpportunityResponseDTO> results = opportunityService.getSavedOpportunities(student.getEmail());

        assertEquals(1, results.size());
        assertTrue(results.get(0).isBookmarkedByCurrentUser());
    }

    @Test
    void toggleBookmark_throwsResourceNotFoundWhenOpportunityMissing() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(opportunityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> opportunityService.toggleBookmark(99L, student.getEmail()));
    }

    private Opportunity buildOpportunity() {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(5L);
        opportunity.setPostedBy(admin);
        opportunity.setTitle("Sample Opportunity");
        opportunity.setOrganization("Sample Org");
        opportunity.setDescription("Sample description");
        opportunity.setCategory(OpportunityCategory.WORKSHOP);
        opportunity.setEventDate(LocalDate.now().plusDays(30));
        opportunity.setRegistrationDeadline(LocalDate.now().plusDays(20));
        opportunity.setMode(OpportunityMode.ONLINE);
        opportunity.setRegistrationUrl("https://example.com");
        opportunity.setStatus(OpportunityStatus.OPEN);
        return opportunity;
    }
}
