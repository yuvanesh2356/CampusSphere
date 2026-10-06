package com.campussphere.guidance.service;

import com.campussphere.auth.entity.Role;
import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.guidance.dto.GuidancePostCreateDTO;
import com.campussphere.guidance.dto.GuidancePostResponseDTO;
import com.campussphere.guidance.dto.GuidancePostUpdateDTO;
import com.campussphere.guidance.entity.*;
import com.campussphere.guidance.repository.GuidancePostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Senior Guidance Hub module's core business rule:
 * only the author who owns a guidance post may edit or delete it.
 * Also covers post creation and browsing. Structured identically to
 * MarketplaceListingServiceTest and FreelanceServiceManagerTest - run
 * against mocked dependencies, no database or Spring context required.
 */
class GuidanceServiceManagerTest {

    @Mock
    private GuidancePostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    private GuidanceServiceManager guidanceServiceManager;

    private User author;
    private User otherStudent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        guidanceServiceManager = new GuidanceServiceManager(postRepository, userRepository, fileStorageService);

        author = new User("Sanjana Iyer", "sanjana.iyer@campus.edu.in", "hashed", "CSE", 4);
        author.setId(1L);
        author.setRole(Role.STUDENT);

        otherStudent = new User("Arjun Das", "arjun.das@campus.edu.in", "hashed", "ECE", 2);
        otherStudent.setId(2L);
        otherStudent.setRole(Role.STUDENT);
    }

    @Test
    void createPost_savesWithAuthorAndDefaultPublishedStatus() {
        GuidancePostCreateDTO request = new GuidancePostCreateDTO();
        request.setTitle("How I Cracked My Product-Based Internship");
        request.setCategory(GuidanceCategory.INTERNSHIP_GUIDANCE);
        request.setDescription("A walkthrough of my prep timeline, resources, and interview rounds");
        request.setRelevantYear(3);
        request.setRelevantDepartment("CSE");

        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(fileStorageService.store(any(), eq("guidance"))).thenReturn(null);
        when(postRepository.save(any(GuidancePost.class))).thenAnswer(invocation -> {
            GuidancePost p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        GuidancePostResponseDTO result = guidanceServiceManager.createPost(author.getEmail(), request);

        assertEquals("How I Cracked My Product-Based Internship", result.getTitle());
        assertEquals(VisibilityStatus.PUBLISHED, result.getStatus());
        assertTrue(result.isOwnedByCurrentUser());
        verify(postRepository, times(1)).save(any(GuidancePost.class));
    }

    @Test
    void updatePost_throwsWhenCurrentUserIsNotTheAuthor() {
        GuidancePost post = buildPost(author);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        GuidancePostUpdateDTO request = new GuidancePostUpdateDTO();
        request.setTitle("Attempted takeover edit");
        request.setCategory(GuidanceCategory.CAREER_ADVICE);
        request.setDescription("desc");
        request.setStatus(VisibilityStatus.PUBLISHED);

        assertThrows(UnauthorizedActionException.class,
                () -> guidanceServiceManager.updatePost(10L, otherStudent.getEmail(), request));

        // Ownership check must fail before any save is attempted.
        verify(postRepository, never()).save(any(GuidancePost.class));
    }

    @Test
    void deletePost_throwsWhenCurrentUserIsNotTheAuthor() {
        GuidancePost post = buildPost(author);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThrows(UnauthorizedActionException.class,
                () -> guidanceServiceManager.deletePost(10L, otherStudent.getEmail()));

        verify(postRepository, never()).delete(any(GuidancePost.class));
    }

    @Test
    void deletePost_succeedsForTheActualAuthor() {
        GuidancePost post = buildPost(author);
        post.setAttachmentPath("guidance/some-file.jpg");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        guidanceServiceManager.deletePost(10L, author.getEmail());

        verify(fileStorageService, times(1)).delete("guidance/some-file.jpg");
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void getPostById_throwsResourceNotFoundWhenMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> guidanceServiceManager.getPostById(99L, author.getEmail()));
    }

    @Test
    void browsePosts_marksOwnershipCorrectlyForEachResult() {
        GuidancePost ownPost = buildPost(author);
        GuidancePost othersPost = buildPost(otherStudent);

        when(postRepository.searchPublished(null, null)).thenReturn(List.of(ownPost, othersPost));

        List<GuidancePostResponseDTO> results = guidanceServiceManager.browsePosts(null, null, author.getEmail());

        assertEquals(2, results.size());
        assertTrue(results.get(0).isOwnedByCurrentUser());
        assertFalse(results.get(1).isOwnedByCurrentUser());
    }

    private GuidancePost buildPost(User owner) {
        GuidancePost post = new GuidancePost();
        post.setId(10L);
        post.setAuthor(owner);
        post.setTitle("Sample Guidance Post");
        post.setCategory(GuidanceCategory.SUBJECT_GUIDANCE);
        post.setDescription("Sample description");
        post.setStatus(VisibilityStatus.PUBLISHED);
        return post;
    }
}
