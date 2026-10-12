package com.fakenews.service;

import com.fakenews.dto.AnalysisHistoryItemResponse;
import com.fakenews.dto.DashboardSummaryResponse;
import com.fakenews.model.Analysis;
import com.fakenews.repository.AnalysisRepository;
import com.fakenews.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final AnalysisRepository analysisRepository;

    public DashboardService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(UserPrincipal currentUser) {
        long total = analysisRepository.countByUserId(currentUser.getId());
        long fakeCount = analysisRepository.countByUserIdAndLabel(currentUser.getId(), "Likely Fake");
        long genuineCount = analysisRepository.countByUserIdAndLabel(currentUser.getId(), "Likely Genuine");
        long uncertainCount = analysisRepository.countByUserIdAndLabel(currentUser.getId(), "Uncertain / Needs Verification");

        List<Analysis> recentEntities = analysisRepository.findTop5ByUserIdOrderByCreatedAtDesc(currentUser.getId());
        List<AnalysisHistoryItemResponse> recent = recentEntities.stream()
                .map(a -> {
                    String text = a.getText() != null ? a.getText() : "";
                    String truncated = text.length() > 140 ? text.substring(0, 140).trim() + "..." : text;
                    return new AnalysisHistoryItemResponse(
                            a.getId(),
                            a.getHeadline(),
                            truncated,
                            a.getSourceUrl(),
                            a.getLabel(),
                            a.getScore(),
                            a.getModelVersion(),
                            a.getCreatedAt()
                    );
                })
                .toList();

        return new DashboardSummaryResponse(total, fakeCount, genuineCount, uncertainCount, recent);
    }
}
