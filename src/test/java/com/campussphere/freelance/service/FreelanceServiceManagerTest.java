package com.campussphere.freelance.service;

import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.freelance.dto.FreelanceServiceCreateDTO;
import com.campussphere.freelance.dto.FreelanceServiceResponseDTO;
import com.campussphere.freelance.dto.FreelanceServiceUpdateDTO;
import com.campussphere.freelance.entity.*;
import com.campussphere.freelance.repository.FreelanceServiceRepository;
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
 * Unit tests for the Freelance Hub module's core business rule: only
 * the seller who owns a service may edit or delete it. Also covers
 * service creation and browsing. Structured identically to
 * MarketplaceListingServiceTest - run against mocked dependencies, no
 * database or Spring context required.
 */
class FreelanceServiceManagerTest {

    @Mock
    private FreelanceServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    private FreelanceServiceManager serviceManager;

    private User seller;
    private User otherStudent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceManager = new FreelanceServiceManager(serviceRepository, userRepository, fileStorageService);

        seller = new User("Priya Nair", "priya.nair@campus.edu.in", "hashed", "CSE", 3);
        seller.setId(1L);
        seller.setRole(Role.STUDENT);

        otherStudent = new User("Karan Mehta", "karan.mehta@campus.edu.in", "hashed", "IT", 2);
        otherStudent.setId(2L);
        otherStudent.setRole(Role.STUDENT);
    }

    @Test
    void createService_savesWithSellerAndDefaultAvailableStatus() {
        FreelanceServiceCreateDTO request = new FreelanceServiceCreateDTO();
        request.setTitle("Resume Design & Formatting");
        request.setCategory(ServiceCategory.RESUME_DESIGN);
        request.setDescription("Clean, ATS-friendly resume redesign within 24 hours");
        request.setPrice(new BigDecimal("250.00"));
        request.setPriceType(PriceType.STARTING_FROM);
        request.setContactInfo("priya.nair@campus.edu.in");

        when(userRepository.findByEmail(seller.getEmail())).thenReturn(Optional.of(seller));
        when(fileStorageService.store(any(), eq("freelance"))).thenReturn(null);
        when(serviceRepository.save(any(FreelanceService.class))).thenAnswer(invocation -> {
            FreelanceService s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        FreelanceServiceResponseDTO result = serviceManager.createService(seller.getEmail(), request);

        assertEquals("Resume Design & Formatting", result.getTitle());
        assertEquals(AvailabilityStatus.AVAILABLE, result.getStatus());
        assertTrue(result.isOwnedByCurrentUser());
        verify(serviceRepository, times(1)).save(any(FreelanceService.class));
    }

    @Test
    void updateService_throwsWhenCurrentUserIsNotTheSeller() {
        FreelanceService service = buildService(seller);

        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));

        FreelanceServiceUpdateDTO request = new FreelanceServiceUpdateDTO();
        request.setTitle("Attempted takeover edit");
        request.setCategory(ServiceCategory.CODING_HELP);
        request.setDescription("desc");
        request.setPrice(new BigDecimal("100.00"));
        request.setPriceType(PriceType.FIXED);
        request.setContactInfo("karan.mehta@campus.edu.in");
        request.setStatus(AvailabilityStatus.AVAILABLE);

        assertThrows(UnauthorizedActionException.class,
                () -> serviceManager.updateService(10L, otherStudent.getEmail(), request));

        // Ownership check must fail before any save is attempted.
        verify(serviceRepository, never()).save(any(FreelanceService.class));
    }

    @Test
    void deleteService_throwsWhenCurrentUserIsNotTheSeller() {
        FreelanceService service = buildService(seller);

        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));

        assertThrows(UnauthorizedActionException.class,
                () -> serviceManager.deleteService(10L, otherStudent.getEmail()));

        verify(serviceRepository, never()).delete(any(FreelanceService.class));
    }

    @Test
    void deleteService_succeedsForTheActualOwner() {
        FreelanceService service = buildService(seller);
        service.setSampleImagePath("freelance/some-file.jpg");

        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));

        serviceManager.deleteService(10L, seller.getEmail());

        verify(fileStorageService, times(1)).delete("freelance/some-file.jpg");
        verify(serviceRepository, times(1)).delete(service);
    }

    @Test
    void getServiceById_throwsResourceNotFoundWhenMissing() {
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceManager.getServiceById(99L, seller.getEmail()));
    }

    @Test
    void browseServices_marksOwnershipCorrectlyForEachResult() {
        FreelanceService ownService = buildService(seller);
        FreelanceService othersService = buildService(otherStudent);

        when(serviceRepository.searchAvailable(null, null)).thenReturn(List.of(ownService, othersService));

        List<FreelanceServiceResponseDTO> results = serviceManager.browseServices(null, null, seller.getEmail());

        assertEquals(2, results.size());
        assertTrue(results.get(0).isOwnedByCurrentUser());
        assertFalse(results.get(1).isOwnedByCurrentUser());
    }

    private FreelanceService buildService(User owner) {
        FreelanceService service = new FreelanceService();
        service.setId(10L);
        service.setSeller(owner);
        service.setTitle("Sample Service");
        service.setCategory(ServiceCategory.CODING_HELP);
        service.setDescription("Sample description");
        service.setPrice(new BigDecimal("100.00"));
        service.setPriceType(PriceType.FIXED);
        service.setContactInfo(owner.getEmail());
        service.setStatus(AvailabilityStatus.AVAILABLE);
        return service;
    }
}
