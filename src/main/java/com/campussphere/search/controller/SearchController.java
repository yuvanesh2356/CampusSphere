package com.campussphere.search.controller;

import com.campussphere.freelance.dto.FreelanceServiceResponseDTO;
import com.campussphere.freelance.service.FreelanceServiceManager;
import com.campussphere.guidance.dto.GuidancePostResponseDTO;
import com.campussphere.guidance.service.GuidanceServiceManager;
import com.campussphere.lostfound.dto.LostFoundResponseDTO;
import com.campussphere.lostfound.service.LostFoundServiceManager;
import com.campussphere.marketplace.dto.MarketplaceListingResponseDTO;
import com.campussphere.marketplace.service.MarketplaceListingService;
import com.campussphere.search.dto.SearchResultDTO;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serves the Global Search page, which searches across all four
 * content modules at once by reusing each module's existing
 * browseXxx()/searchXxx() method - the same keyword-matching logic
 * every module's own browse page already uses, rather than
 * duplicating query logic here. This class only composes and
 * normalizes results into SearchResultDTO for unified rendering, plus
 * applies the newest/oldest sort.
 */
@Controller
@RequestMapping("/search")
public class SearchController {

    private final MarketplaceListingService marketplaceListingService;
    private final FreelanceServiceManager freelanceServiceManager;
    private final GuidanceServiceManager guidanceServiceManager;
    private final LostFoundServiceManager lostFoundServiceManager;

    public SearchController(MarketplaceListingService marketplaceListingService,
                             FreelanceServiceManager freelanceServiceManager,
                             GuidanceServiceManager guidanceServiceManager,
                             LostFoundServiceManager lostFoundServiceManager) {
        this.marketplaceListingService = marketplaceListingService;
        this.freelanceServiceManager = freelanceServiceManager;
        this.guidanceServiceManager = guidanceServiceManager;
        this.lostFoundServiceManager = lostFoundServiceManager;
    }

    @GetMapping
    public String search(@RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "newest") String sort,
                          Authentication authentication,
                          Model model) {
        List<SearchResultDTO> results = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String email = authentication.getName();

            for (MarketplaceListingResponseDTO r : marketplaceListingService.browseListings(null, keyword, email)) {
                results.add(new SearchResultDTO("Marketplace", r.getTitle(), r.getCategory().toString(),
                        "/marketplace/" + r.getId(), r.getCreatedAt()));
            }
            for (FreelanceServiceResponseDTO r : freelanceServiceManager.browseServices(null, keyword, email)) {
                results.add(new SearchResultDTO("Freelance Hub", r.getTitle(), r.getCategory().toString(),
                        "/freelance/" + r.getId(), r.getCreatedAt()));
            }
            for (GuidancePostResponseDTO r : guidanceServiceManager.browsePosts(null, keyword, email)) {
                results.add(new SearchResultDTO("Senior Guidance", r.getTitle(), r.getCategory().toString(),
                        "/guidance/" + r.getId(), r.getCreatedAt()));
            }
            for (LostFoundResponseDTO r : lostFoundServiceManager.browsePosts(null, null, null, keyword, email)) {
                results.add(new SearchResultDTO("Lost & Found", r.getTitle(), r.getCategory().toString(),
                        "/lostfound/" + r.getId(), r.getCreatedAt()));
            }

            Comparator<SearchResultDTO> byDate = Comparator.comparing(SearchResultDTO::getCreatedAt);
            results.sort("oldest".equals(sort) ? byDate : byDate.reversed());
        }

        model.addAttribute("results", results);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "search/index";
    }
}
