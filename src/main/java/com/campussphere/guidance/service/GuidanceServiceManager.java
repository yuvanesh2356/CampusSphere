package com.campussphere.guidance.service;

import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.guidance.dto.GuidancePostCreateDTO;
import com.campussphere.guidance.dto.GuidancePostResponseDTO;
import com.campussphere.guidance.dto.GuidancePostUpdateDTO;
import com.campussphere.guidance.entity.GuidanceCategory;
import com.campussphere.guidance.entity.GuidancePost;
import com.campussphere.guidance.repository.GuidancePostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Senior Guidance Hub module. Named
 * GuidanceServiceManager to stay consistent with
 * FreelanceServiceManager's naming - both modules use "Manager" as the
 * business-logic-layer suffix rather than "[Entity]Service", since
 * "Service" already appears prominently in this project's domain
 * vocabulary (freelance services) and consistent naming across
 * sibling modules is more valuable here than a literal per-module
 * naming derivation. This class fills the exact same architectural
 * role Service classes do everywhere else in the project.
 *
 * Structure otherwise mirrors FreelanceServiceManager and
 * MarketplaceListingService exactly: only the author may edit/delete
 * their own post, attachments go through the shared FileStorageService,
 * and browsing only ever surfaces PUBLISHED posts.
 */
@Service
public class GuidanceServiceManager {

    private static final String UPLOAD_SUBDIRECTORY = "guidance";

    private final GuidancePostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public GuidanceServiceManager(GuidancePostRepository postRepository,
                                   UserRepository userRepository,
                                   FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public GuidancePostResponseDTO createPost(String authorEmail, GuidancePostCreateDTO request) {
        User author = getUserByEmail(authorEmail);

        GuidancePost post = new GuidancePost();
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setCategory(request.getCategory());
        post.setDescription(request.getDescription());
        post.setRelevantYear(request.getRelevantYear());
        post.setRelevantDepartment(request.getRelevantDepartment());

        String storedAttachmentPath = fileStorageService.store(request.getAttachment(), UPLOAD_SUBDIRECTORY);
        post.setAttachmentPath(storedAttachmentPath);

        GuidancePost saved = postRepository.save(post);
        return GuidancePostResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public GuidancePostResponseDTO updatePost(Long postId, String currentUserEmail, GuidancePostUpdateDTO request) {
        GuidancePost post = getPostOrThrow(postId);
        assertOwnership(post, currentUserEmail);

        post.setTitle(request.getTitle());
        post.setCategory(request.getCategory());
        post.setDescription(request.getDescription());
        post.setRelevantYear(request.getRelevantYear());
        post.setRelevantDepartment(request.getRelevantDepartment());
        post.setStatus(request.getStatus());

        if (request.getAttachment() != null && !request.getAttachment().isEmpty()) {
            fileStorageService.delete(post.getAttachmentPath());
            String newAttachmentPath = fileStorageService.store(request.getAttachment(), UPLOAD_SUBDIRECTORY);
            post.setAttachmentPath(newAttachmentPath);
        }

        GuidancePost saved = postRepository.save(post);
        return GuidancePostResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public void deletePost(Long postId, String currentUserEmail) {
        GuidancePost post = getPostOrThrow(postId);
        assertOwnership(post, currentUserEmail);

        fileStorageService.delete(post.getAttachmentPath());
        postRepository.delete(post);
    }

    public GuidancePostResponseDTO getPostById(Long postId, String currentUserEmail) {
        GuidancePost post = getPostOrThrow(postId);
        boolean owned = post.getAuthor().getEmail().equalsIgnoreCase(currentUserEmail);
        return GuidancePostResponseDTO.fromEntity(post, owned);
    }

    /**
     * Public browse/search. category and keyword are both optional -
     * pass null for either to skip that filter. Only PUBLISHED posts
     * are returned (enforced in the repository query).
     */
    public List<GuidancePostResponseDTO> browsePosts(GuidanceCategory category, String keyword, String currentUserEmail) {
        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return postRepository.searchPublished(category, trimmedKeyword).stream()
                .map(post -> GuidancePostResponseDTO.fromEntity(
                        post, post.getAuthor().getEmail().equalsIgnoreCase(currentUserEmail)))
                .collect(Collectors.toList());
    }

    /**
     * "My Guidance Posts" - every post owned by the current user,
     * regardless of status, so they can manage their full history.
     */
    public List<GuidancePostResponseDTO> getMyPosts(String currentUserEmail) {
        User author = getUserByEmail(currentUserEmail);
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId()).stream()
                .map(post -> GuidancePostResponseDTO.fromEntity(post, true))
                .collect(Collectors.toList());
    }

    /**
     * Full moderation list for the Admin module - every post
     * regardless of status. Mirrors MarketplaceListingService.getAllForAdmin().
     */
    public List<GuidancePostResponseDTO> getAllForAdmin() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(post -> GuidancePostResponseDTO.fromEntity(post, false))
                .collect(Collectors.toList());
    }

    /**
     * Admin-only delete - bypasses the ownership check, safe only
     * because it is exclusively reachable from the role-gated
     * AdminController. Mirrors MarketplaceListingService.adminDelete().
     */
    @Transactional
    public void adminDelete(Long postId) {
        GuidancePost post = getPostOrThrow(postId);
        fileStorageService.delete(post.getAttachmentPath());
        postRepository.delete(post);
    }

    // ---------- Internal helpers ----------

    private GuidancePost getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Guidance post not found with id: " + postId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }

    private void assertOwnership(GuidancePost post, String currentUserEmail) {
        if (!post.getAuthor().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedActionException("You can only edit or delete your own guidance posts");
        }
    }
}
