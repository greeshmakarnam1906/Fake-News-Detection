package com.fakenews.dto;

import com.fakenews.ml.PredictionResult;

import java.time.Instant;
import java.util.List;

public class AnalyzeResponse {
    private Long id;
    private String headline;
    private String text;
    private String sourceUrl;
    private String label;
    private Double score;
    private String modelVersion;
    private String explanation;
    private List<PredictionResult.FeatureContribution> topFakeFeatures;
    private List<PredictionResult.FeatureContribution> topGenuineFeatures;
    private List<String> limitations;
    private List<String> verificationActions;
    private int characterCount;
    private int wordCount;
    private Instant createdAt;

    public AnalyzeResponse() {
    }

    public AnalyzeResponse(Long id, String headline, String text, String sourceUrl,
                           String label, Double score, String modelVersion, String explanation,
                           List<PredictionResult.FeatureContribution> topFakeFeatures,
                           List<PredictionResult.FeatureContribution> topGenuineFeatures,
                           List<String> limitations, List<String> verificationActions,
                           int characterCount, int wordCount, Instant createdAt) {
        this.id = id;
        this.headline = headline;
        this.text = text;
        this.sourceUrl = sourceUrl;
        this.label = label;
        this.score = score;
        this.modelVersion = modelVersion;
        this.explanation = explanation;
        this.topFakeFeatures = topFakeFeatures;
        this.topGenuineFeatures = topGenuineFeatures;
        this.limitations = limitations;
        this.verificationActions = verificationActions;
        this.characterCount = characterCount;
        this.wordCount = wordCount;
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<PredictionResult.FeatureContribution> getTopFakeFeatures() {
        return topFakeFeatures;
    }

    public void setTopFakeFeatures(List<PredictionResult.FeatureContribution> topFakeFeatures) {
        this.topFakeFeatures = topFakeFeatures;
    }

    public List<PredictionResult.FeatureContribution> getTopGenuineFeatures() {
        return topGenuineFeatures;
    }

    public void setTopGenuineFeatures(List<PredictionResult.FeatureContribution> topGenuineFeatures) {
        this.topGenuineFeatures = topGenuineFeatures;
    }

    public List<String> getLimitations() {
        return limitations;
    }

    public void setLimitations(List<String> limitations) {
        this.limitations = limitations;
    }

    public List<String> getVerificationActions() {
        return verificationActions;
    }

    public void setVerificationActions(List<String> verificationActions) {
        this.verificationActions = verificationActions;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
