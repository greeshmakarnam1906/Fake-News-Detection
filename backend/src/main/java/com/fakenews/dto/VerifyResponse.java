package com.fakenews.dto;

import java.util.List;

public record VerifyResponse(
        String verdict,
        String summary,
        String claimChecked,
        List<FactCheckItem> factChecks,
        List<NewsItem> newsSources,
        AiAssessment aiAssessment,
        List<String> notes) {

    public record FactCheckItem(String claim, String publisher, String rating,
                                String title, String url, String reviewDate) {}

    public record NewsItem(String title, String source, String url,
                           String publishedAt, boolean trusted, String snippet) {}

    public record AiAssessment(String stance, String explanation) {}
}
