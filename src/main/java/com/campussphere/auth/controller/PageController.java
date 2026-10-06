package com.campussphere.auth.controller;

import com.campussphere.freelance.service.FreelanceServiceManager;
import com.campussphere.guidance.service.GuidanceServiceManager;
import com.campussphere.lostfound.service.LostFoundServiceManager;
import com.campussphere.marketplace.service.MarketplaceListingService;
import com.campussphere.opportunity.service.OpportunityService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Serves the server-rendered HTML views: the public landing page, the
 * registration page, Spring Security's login page, and the post-login
 * dashboard.
 *
 * Kept separate from AuthController (which handles JSON API calls) so
 * that view-serving and API logic are not mixed in the same class.
 */
@Controller
public class PageController {

    private static final int RECENT_ITEM_LIMIT = 3;

    private final MarketplaceListingService marketplaceListingService;
    private final FreelanceServiceManager freelanceServiceManager;
    private final GuidanceServiceManager guidanceServiceManager;
    private final LostFoundServiceManager lostFoundServiceManager;
    private final OpportunityService opportunityService;

    public PageController(MarketplaceListingService marketplaceListingService,
                           FreelanceServiceManager freelanceServiceManager,
                           GuidanceServiceManager guidanceServiceManager,
                           LostFoundServiceManager lostFoundServiceManager,
                           OpportunityService opportunityService) {
        this.marketplaceListingService = marketplaceListingService;
        this.freelanceServiceManager = freelanceServiceManager;
        this.guidanceServiceManager = guidanceServiceManager;
        this.lostFoundServiceManager = lostFoundServiceManager;
        this.opportunityService = opportunityService;
    }

    /**
     * Public landing page. Accessible without authentication.
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Registration page. The form on this page submits via JavaScript
     * (auth.js) to POST /api/auth/register.
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * Custom login page, referenced from SecurityConfig via
     * .loginPage("/login"). Spring Security handles the actual
     * POST /login submission automatically.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Post-login dashboard. Shows live stats and recent activity
     * across all five content areas (the four original modules plus
     * Global Opportunities, added in this improvement pass). Reuses
     * each module's existing public browseXxx() method rather than
     * adding new repository/service methods just for this page - the
     * first three results of an already-sorted list are the "recent
     * activity" feed, and the full list's size is the stat count.
     * Global Opportunities is sorted by soonest registration deadline
     * (see OpportunityRepository.searchActive), which is arguably more
     * useful on a dashboard widget than newest-posted would be - it
     * surfaces what a student needs to act on soonest.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();

        var marketplaceListings = marketplaceListingService.browseListings(null, null, email);
        var freelanceServices = freelanceServiceManager.browseServices(null, null, email);
        var guidancePosts = guidanceServiceManager.browsePosts(null, null, email);
        var lostFoundPosts = lostFoundServiceManager.browsePosts(null, null, null, null, email);
        var opportunities = opportunityService.browseOpportunities(null, null, null, email);

        model.addAttribute("marketplaceCount", marketplaceListings.size());
        model.addAttribute("freelanceCount", freelanceServices.size());
        model.addAttribute("guidanceCount", guidancePosts.size());
        model.addAttribute("lostFoundCount", lostFoundPosts.size());
        model.addAttribute("opportunityCount", opportunities.size());

        model.addAttribute("recentMarketplace", firstN(marketplaceListings, RECENT_ITEM_LIMIT));
        model.addAttribute("recentFreelance", firstN(freelanceServices, RECENT_ITEM_LIMIT));
        model.addAttribute("recentGuidance", firstN(guidancePosts, RECENT_ITEM_LIMIT));
        model.addAttribute("recentLostFound", firstN(lostFoundPosts, RECENT_ITEM_LIMIT));
        model.addAttribute("recentOpportunities", firstN(opportunities, RECENT_ITEM_LIMIT));

        return "dashboard";
    }

    private <T> List<T> firstN(List<T> list, int n) {
        return list.size() <= n ? list : list.subList(0, n);
    }
}
