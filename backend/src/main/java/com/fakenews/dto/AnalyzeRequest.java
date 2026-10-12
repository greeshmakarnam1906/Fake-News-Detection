package com.fakenews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AnalyzeRequest {

    @Size(max = 500, message = "Headline cannot exceed 500 characters")
    private String headline;

    @NotBlank(message = "News text or social media post is required")
    @Size(min = 10, max = 10000, message = "Submitted text must be between 10 and 10,000 characters")
    private String text;

    @Pattern(
            regexp = "^(https?://[a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,})(?::\\d+)?(?:/\\S*)?)?$",
            message = "Source URL must be a valid HTTP or HTTPS URL (or left blank)"
    )
    @Size(max = 1000, message = "Source URL cannot exceed 1000 characters")
    private String sourceUrl;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    public AnalyzeRequest() {
    }

    public AnalyzeRequest(String headline, String text, String sourceUrl, String category) {
        this.headline = headline;
        this.text = text;
        this.sourceUrl = sourceUrl;
        this.category = category;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
