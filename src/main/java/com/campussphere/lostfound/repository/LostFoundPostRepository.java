package com.campussphere.lostfound.repository;

import com.campussphere.lostfound.entity.ItemCategory;
import com.campussphere.lostfound.entity.LostFoundPost;
import com.campussphere.lostfound.entity.PostStatus;
import com.campussphere.lostfound.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LostFoundPostRepository extends JpaRepository<LostFoundPost, Long> {

    /**
     * Powers the public browse/search page, combining every filter
     * the module requires (postType, category, status, keyword) into
     * one flexible query rather than several overlapping methods.
     *
     * Unlike the OPEN-only / AVAILABLE-only / PUBLISHED-only pattern
     * the other three modules use for public browsing, Lost & Found
     * lets a status be explicitly requested: if the status parameter
     * is null, only OPEN posts are shown (the sensible default, since
     * those are the ones still needing attention); if a status is
     * explicitly passed, posts with that exact status are shown
     * instead (e.g. so a student can check whether an item was
     * already CLAIMED). This is a deliberate difference from the
     * other modules, not an inconsistency - browsing "what's already
     * resolved" has genuine value here that it doesn't for a sold
     * marketplace item or a hidden guidance post.
     */
    @Query("SELECT l FROM LostFoundPost l " +
           "WHERE ((:status IS NULL AND l.status = com.campussphere.lostfound.entity.PostStatus.OPEN) " +
           "       OR (:status IS NOT NULL AND l.status = :status)) " +
           "AND (:postType IS NULL OR l.postType = :postType) " +
           "AND (:category IS NULL OR l.category = :category) " +
           "AND (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY l.createdAt DESC")
    List<LostFoundPost> searchPosts(@Param("postType") PostType postType,
                                     @Param("category") ItemCategory category,
                                     @Param("status") PostStatus status,
                                     @Param("keyword") String keyword);

    /**
     * Powers "My Posts" - every post owned by the given user
     * regardless of status, so they can manage their full reporting
     * history, not just currently-open posts.
     */
    List<LostFoundPost> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
