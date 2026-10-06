package com.campussphere.lostfound.service;

import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.lostfound.dto.LostFoundCreateDTO;
import com.campussphere.lostfound.dto.LostFoundResponseDTO;
import com.campussphere.lostfound.dto.LostFoundUpdateDTO;
import com.campussphere.lostfound.entity.*;
import com.campussphere.lostfound.repository.LostFoundPostRepository;
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
 * Unit tests for the Lost & Found Portal module's core business rule:
 * only the owner who created a post may edit or delete it. Also
 * covers post creation and browsing. Structured identically to
 * MarketplaceListingServiceTest, FreelanceServiceManagerTest, and
 * GuidanceServiceManagerTest - run against mocked dependencies, no
 * database or Spring context required.
 */
class LostFoundServiceManagerTest {

    @Mock
    private LostFoundPostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    private LostFoundServiceManager lostFoundServiceManager;

    private User owner;
    private User otherStudent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lostFoundServiceManager = new LostFoundServiceManager(postRepository, userRepository, fileStorageService);

        owner = new User("Meera Pillai", "meera.pillai@campus.edu.in", "hashed", "CSE", 2);
        owner.setId(1L);
        owner.setRole(Role.STUDENT);

        otherStudent = new User("Vikram Rao", "vikram.rao@campus.edu.in", "hashed", "IT", 3);
        otherStudent.setId(2L);
        otherStudent.setRole(Role.STUDENT);
    }

    @Test
    void createPost_savesWithOwnerAndDefaultOpenStatus() {
        LostFoundCreateDTO request = new LostFoundCreateDTO();
        request.setTitle("Blue Backpack Near Library");
        request.setDescription("Navy blue backpack with a laptop sleeve, left near the entrance");
        request.setCategory(ItemCategory.BAG);
        request.setPostType(PostType.FOUND);
        request.setLocation("Main Library");
        request.setDateLostOrFound(LocalDate.of(2026, 7, 20));
        request.setContactInformation("meera.pillai@campus.edu.in");

        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(fileStorageService.store(any(), eq("lostfound"))).thenReturn(null);
        when(postRepository.save(any(LostFoundPost.class))).thenAnswer(invocation -> {
            LostFoundPost p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        LostFoundResponseDTO result = lostFoundServiceManager.createPost(owner.getEmail(), request);

        assertEquals("Blue Backpack Near Library", result.getTitle());
        assertEquals(PostStatus.OPEN, result.getStatus());
        assertTrue(result.isOwnedByCurrentUser());
        verify(postRepository, times(1)).save(any(LostFoundPost.class));
    }

    @Test
    void updatePost_throwsWhenCurrentUserIsNotTheOwner() {
        LostFoundPost post = buildPost(owner);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        LostFoundUpdateDTO request = new LostFoundUpdateDTO();
        request.setTitle("Attempted takeover edit");
        request.setDescription("desc");
        request.setCategory(ItemCategory.WALLET);
        request.setPostType(PostType.LOST);
        request.setLocation("Somewhere");
        request.setDateLostOrFound(LocalDate.now());
        request.setContactInformation("vikram.rao@campus.edu.in");
        request.setStatus(PostStatus.OPEN);

        assertThrows(UnauthorizedActionException.class,
                () -> lostFoundServiceManager.updatePost(10L, otherStudent.getEmail(), request));

        // Ownership check must fail before any save is attempted.
        verify(postRepository, never()).save(any(LostFoundPost.class));
    }

    @Test
    void deletePost_throwsWhenCurrentUserIsNotTheOwner() {
        LostFoundPost post = buildPost(owner);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThrows(UnauthorizedActionException.class,
                () -> lostFoundServiceManager.deletePost(10L, otherStudent.getEmail()));

        verify(postRepository, never()).delete(any(LostFoundPost.class));
    }

    @Test
    void deletePost_succeedsForTheActualOwner() {
        LostFoundPost post = buildPost(owner);
        post.setImagePath("lostfound/some-file.jpg");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        lostFoundServiceManager.deletePost(10L, owner.getEmail());

        verify(fileStorageService, times(1)).delete("lostfound/some-file.jpg");
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void getPostById_throwsResourceNotFoundWhenMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> lostFoundServiceManager.getPostById(99L, owner.getEmail()));
    }

    @Test
    void browsePosts_marksOwnershipCorrectlyForEachResult() {
        LostFoundPost ownPost = buildPost(owner);
        LostFoundPost othersPost = buildPost(otherStudent);

        when(postRepository.searchPosts(null, null, null, null)).thenReturn(List.of(ownPost, othersPost));

        List<LostFoundResponseDTO> results =
                lostFoundServiceManager.browsePosts(null, null, null, null, owner.getEmail());

        assertEquals(2, results.size());
        assertTrue(results.get(0).isOwnedByCurrentUser());
        assertFalse(results.get(1).isOwnedByCurrentUser());
    }

    private LostFoundPost buildPost(User postOwner) {
        LostFoundPost post = new LostFoundPost();
        post.setId(10L);
        post.setOwner(postOwner);
        post.setTitle("Sample Post");
        post.setDescription("Sample description");
        post.setCategory(ItemCategory.KEYS);
        post.setPostType(PostType.LOST);
        post.setLocation("Sample Location");
        post.setDateLostOrFound(LocalDate.now());
        post.setContactInformation(postOwner.getEmail());
        post.setStatus(PostStatus.OPEN);
        return post;
    }
}
