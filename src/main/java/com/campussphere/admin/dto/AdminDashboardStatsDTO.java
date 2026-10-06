package com.campussphere.admin.dto;

/**
 * Simple aggregate counts for the Admin Dashboard's stat cards.
 * Deliberately a plain data holder (no behavior) - AdminService is
 * responsible for computing these values.
 */
public class AdminDashboardStatsDTO {

    private long totalUsers;
    private long totalMarketplaceListings;
    private long totalFreelanceServices;
    private long totalGuidancePosts;
    private long totalLostFoundPosts;

    public AdminDashboardStatsDTO() {
    }

    public AdminDashboardStatsDTO(long totalUsers, long totalMarketplaceListings, long totalFreelanceServices,
                                   long totalGuidancePosts, long totalLostFoundPosts) {
        this.totalUsers = totalUsers;
        this.totalMarketplaceListings = totalMarketplaceListings;
        this.totalFreelanceServices = totalFreelanceServices;
        this.totalGuidancePosts = totalGuidancePosts;
        this.totalLostFoundPosts = totalLostFoundPosts;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalMarketplaceListings() { return totalMarketplaceListings; }
    public void setTotalMarketplaceListings(long totalMarketplaceListings) { this.totalMarketplaceListings = totalMarketplaceListings; }

    public long getTotalFreelanceServices() { return totalFreelanceServices; }
    public void setTotalFreelanceServices(long totalFreelanceServices) { this.totalFreelanceServices = totalFreelanceServices; }

    public long getTotalGuidancePosts() { return totalGuidancePosts; }
    public void setTotalGuidancePosts(long totalGuidancePosts) { this.totalGuidancePosts = totalGuidancePosts; }

    public long getTotalLostFoundPosts() { return totalLostFoundPosts; }
    public void setTotalLostFoundPosts(long totalLostFoundPosts) { this.totalLostFoundPosts = totalLostFoundPosts; }
}
