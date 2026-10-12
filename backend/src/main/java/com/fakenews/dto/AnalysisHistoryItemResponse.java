package com.fakenews.dto;

import java.time.Instant;

public class AnalysisHistoryItemResponse {
    private Long id;
    private String headline;
    private String truncatedText;
    private String sourceUrl;
    private String label;
    private Double score;
    private String modelVersion;
    private Instant createdAt;

    public AnalysisHistoryItemResponse() {
    }

    public AnalysisHistoryItemResponse(Long id, String headline, String truncatedText, String sourceUrl,
                                       String label, Double score, String modelVersion, Instant createdAt) {
        this.id = id;
        this.headline = headline;
        this.truncatedText = truncatedText;
        this.sourceUrl = sourceUrl;
        this.label = label;
        this.score = score;
        this.modelVersion = modelVersion;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getTruncatedText() {
        return truncatedText;
    }

    public void setTruncatedText(String truncatedText) {
        this.truncatedText = truncatedText;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
