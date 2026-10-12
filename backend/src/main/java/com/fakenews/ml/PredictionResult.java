package com.fakenews.ml;

import java.util.List;

/**
 * PredictionResult contains the classification output, explainability signals,
 * uncertainty indicators, and verification guidelines.
 */
public class PredictionResult {

    private String label; // "Likely Genuine", "Likely Fake", "Uncertain / Needs Verification"
    private double score; // Model score (0.0 to 1.0)
    private String modelVersion;
    private String explanation;
    private List<FeatureContribution> topFakeFeatures;
    private List<FeatureContribution> topGenuineFeatures;
    private List<String> limitations;
    private List<String> verificationActions;
    private int characterCount;
    private int wordCount;

    public PredictionResult() {
    }

    public PredictionResult(String label, double score, String modelVersion, String explanation,
                            List<FeatureContribution> topFakeFeatures,
                            List<FeatureContribution> topGenuineFeatures,
                            List<String> limitations, List<String> verificationActions,
                            int characterCount, int wordCount) {
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
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
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

    public List<FeatureContribution> getTopFakeFeatures() {
        return topFakeFeatures;
    }

    public void setTopFakeFeatures(List<FeatureContribution> topFakeFeatures) {
        this.topFakeFeatures = topFakeFeatures;
    }

    public List<FeatureContribution> getTopGenuineFeatures() {
        return topGenuineFeatures;
    }

    public void setTopGenuineFeatures(List<FeatureContribution> topGenuineFeatures) {
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

    public static class FeatureContribution {
        private String token;
        private double weight;
        private double contribution;

        public FeatureContribution() {
        }

        public FeatureContribution(String token, double weight, double contribution) {
            this.token = token;
            this.weight = weight;
            this.contribution = contribution;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public double getContribution() {
            return contribution;
        }

        public void setContribution(double contribution) {
            this.contribution = contribution;
        }
    }
}
