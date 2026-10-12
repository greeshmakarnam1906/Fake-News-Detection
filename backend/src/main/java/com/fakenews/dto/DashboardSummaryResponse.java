package com.fakenews.dto;

import java.util.List;

public class DashboardSummaryResponse {
    private long totalAnalyses;
    private long likelyFakeCount;
    private long likelyGenuineCount;
    private long uncertainCount;
    private List<AnalysisHistoryItemResponse> recentAnalyses;
    private String disclaimer = "All figures reflect statistical text-classification model outputs and should not be interpreted as definitive fact-checking or verified truth.";

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long totalAnalyses, long likelyFakeCount,
                                    long likelyGenuineCount, long uncertainCount,
                                    List<AnalysisHistoryItemResponse> recentAnalyses) {
        this.totalAnalyses = totalAnalyses;
        this.likelyFakeCount = likelyFakeCount;
        this.likelyGenuineCount = likelyGenuineCount;
        this.uncertainCount = uncertainCount;
        this.recentAnalyses = recentAnalyses;
    }

    public long getTotalAnalyses() {
        return totalAnalyses;
    }

    public void setTotalAnalyses(long totalAnalyses) {
        this.totalAnalyses = totalAnalyses;
    }

    public long getLikelyFakeCount() {
        return likelyFakeCount;
    }

    public void setLikelyFakeCount(long likelyFakeCount) {
        this.likelyFakeCount = likelyFakeCount;
    }

    public long getLikelyGenuineCount() {
        return likelyGenuineCount;
    }

    public void setLikelyGenuineCount(long likelyGenuineCount) {
        this.likelyGenuineCount = likelyGenuineCount;
    }

    public long getUncertainCount() {
        return uncertainCount;
    }

    public void setUncertainCount(long uncertainCount) {
        this.uncertainCount = uncertainCount;
    }

    public List<AnalysisHistoryItemResponse> getRecentAnalyses() {
        return recentAnalyses;
    }

    public void setRecentAnalyses(List<AnalysisHistoryItemResponse> recentAnalyses) {
        this.recentAnalyses = recentAnalyses;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
