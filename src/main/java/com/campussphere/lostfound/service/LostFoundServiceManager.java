package com.campussphere.lostfound.service;

import com.campussphere.auth.entity.User;
import com.campussphere.auth.repository.UserRepository;
import com.campussphere.common.exception.ResourceNotFoundException;
import com.campussphere.common.exception.UnauthorizedActionException;
import com.campussphere.common.service.FileStorageService;
import com.campussphere.lostfound.dto.LostFoundCreateDTO;
import com.campussphere.lostfound.dto.LostFoundResponseDTO;
import com.campussphere.lostfound.dto.LostFoundUpdateDTO;
import com.campussphere.lostfound.entity.ItemCategory;
import com.campussphere.lostfound.entity.LostFoundPost;
import com.campussphere.lostfound.entity.PostStatus;
import com.campussphere.lostfound.entity.PostType;
import com.campussphere.lostfound.repository.LostFoundPostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Lost & Found Portal module. Named
 * LostFoundServiceManager to stay consistent with
 * FreelanceServiceManager/GuidanceServiceManager's naming across
 * sibling modules. Structure mirrors those two exactly: only the
 * owner may edit/delete their own post, images go through the shared
 * FileStorageService, and browsing defaults to OPEN posts unless a
 * specific status is explicitly requested (see
 * LostFoundPostRepository.searchPosts for why this module's browsing
 * behaves slightly differently from the other three).
 */
@Service
public class LostFoundServiceManager {

    private static final String UPLOAD_SUBDIRECTORY = "lostfound";

    private final LostFoundPostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public LostFoundServiceManager(LostFoundPostRepository postRepository,
                                    UserRepository userRepository,
                                    FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public LostFoundResponseDTO createPost(String ownerEmail, LostFoundCreateDTO request) {
        User owner = getUserByEmail(ownerEmail);

        LostFoundPost post = new LostFoundPost();
        post.setOwner(owner);
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setCategory(request.getCategory());
        post.setPostType(request.getPostType());
        post.setLocation(request.getLocation());
        post.setDateLostOrFound(request.getDateLostOrFound());
        post.setContactInformation(request.getContactInformation());

        String storedImagePath = fileStorageService.store(request.getImage(), UPLOAD_SUBDIRECTORY);
        post.setImagePath(storedImagePath);

        LostFoundPost saved = postRepository.save(post);
        return LostFoundResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public LostFoundResponseDTO updatePost(Long postId, String currentUserEmail, LostFoundUpdateDTO request) {
        LostFoundPost post = getPostOrThrow(postId);
        assertOwnership(post, currentUserEmail);

        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setCategory(request.getCategory());
        post.setPostType(request.getPostType());
        post.setLocation(request.getLocation());
        post.setDateLostOrFound(request.getDateLostOrFound());
        post.setContactInformation(request.getContactInformation());
        post.setStatus(request.getStatus());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            fileStorageService.delete(post.getImagePath());
            String newImagePath = fileStorageService.store(request.getImage(), UPLOAD_SUBDIRECTORY);
            post.setImagePath(newImagePath);
        }

        LostFoundPost saved = postRepository.save(post);
        return LostFoundResponseDTO.fromEntity(saved, true);
    }

    @Transactional
    public void deletePost(Long postId, String currentUserEmail) {
        LostFoundPost post = getPostOrThrow(postId);
        assertOwnership(post, currentUserEmail);

        fileStorageService.delete(post.getImagePath());
        postRepository.delete(post);
    }

    public LostFoundResponseDTO getPostById(Long postId, String currentUserEmail) {
        LostFoundPost post = getPostOrThrow(postId);
        boolean owned = post.getOwner().getEmail().equalsIgnoreCase(currentUserEmail);
        return LostFoundResponseDTO.fromEntity(post, owned);
    }

    /**
     * Public browse/search. postType, category, status, and keyword
     * are all optional - pass null to skip a given filter. When status
     * is null, only OPEN posts are returned (see the repository query
     * for the reasoning behind allowing an explicit status override).
     */
    public List<LostFoundResponseDTO> browsePosts(PostType postType, ItemCategory category, PostStatus status,
                                                    String keyword, String currentUserEmail) {
        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return postRepository.searchPosts(postType, category, status, trimmedKeyword).stream()
                .map(post -> LostFoundResponseDTO.fromEntity(
                        post, post.getOwner().getEmail().equalsIgnoreCase(currentUserEmail)))
                .collect(Collectors.toList());
    }

    /**
     * "My Posts" - every post owned by the current user, regardless
     * of status, so they can manage their full reporting history.
     */
    public List<LostFoundResponseDTO> getMyPosts(String currentUserEmail) {
        User owner = getUserByEmail(currentUserEmail);
        return postRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId()).stream()
                .map(post -> LostFoundResponseDTO.fromEntity(post, true))
                .collect(Collectors.toList());
    }

    /**
     * Full moderation list for the Admin module - every post
     * regardless of status. Mirrors MarketplaceListingService.getAllForAdmin().
     */
    public List<LostFoundResponseDTO> getAllForAdmin() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(post -> LostFoundResponseDTO.fromEntity(post, false))
                .collect(Collectors.toList());
    }

    /**
     * Admin-only delete - bypasses the ownership check, safe only
     * because it is exclusively reachable from the role-gated
     * AdminController. Mirrors MarketplaceListingService.adminDelete().
     */
    @Transactional
    public void adminDelete(Long postId) {
        LostFoundPost post = getPostOrThrow(postId);
        fileStorageService.delete(post.getImagePath());
        postRepository.delete(post);
    }

    // ---------- Internal helpers ----------

    private LostFoundPost getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }

    private void assertOwnership(LostFoundPost post, String currentUserEmail) {
        if (!post.getOwner().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedActionException("You can only edit or delete your own posts");
        }
    }
}
