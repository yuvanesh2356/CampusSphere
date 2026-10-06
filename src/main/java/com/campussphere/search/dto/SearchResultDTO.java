package com.campussphere.search.dto;

import java.time.LocalDateTime;

/**
 * A single search result, normalized across all four content modules
 * so the results page can render one consistent list rather than four
 * differently-shaped sections. Built by SearchController from each
 * module's existing browse method output - no new query logic is
 * introduced here, this is purely a presentation-layer aggregation.
 */
public class SearchResultDTO {

    private String module;
    private String title;
    private String subtitle;
    private String url;
    private LocalDateTime createdAt;

    public SearchResultDTO() {
    }

    public SearchResultDTO(String module, String title, String subtitle, String url, LocalDateTime createdAt) {
        this.module = module;
        this.title = title;
        this.subtitle = subtitle;
        this.url = url;
        this.createdAt = createdAt;
    }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
