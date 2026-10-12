package com.fakenews.service;

import com.fakenews.dto.AnalysisHistoryItemResponse;
import com.fakenews.dto.AnalyzeRequest;
import com.fakenews.dto.AnalyzeResponse;
import com.fakenews.dto.PagedResponse;
import com.fakenews.exception.ResourceNotFoundException;
import com.fakenews.ml.ModelPredictor;
import com.fakenews.ml.PredictionResult;
import com.fakenews.model.Analysis;
import com.fakenews.model.User;
import com.fakenews.repository.AnalysisRepository;
import com.fakenews.repository.UserRepository;
import com.fakenews.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    private final ModelPredictor modelPredictor;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final RateLimiterService rateLimiterService;

    public AnalysisService(ModelPredictor modelPredictor,
                           AnalysisRepository analysisRepository,
                           UserRepository userRepository,
                           RateLimiterService rateLimiterService) {
        this.modelPredictor = modelPredictor;
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.rateLimiterService = rateLimiterService;
    }

    @Transactional
    public AnalyzeResponse analyze(AnalyzeRequest request, UserPrincipal currentUser, String clientIp) {
        // Enforce rate limiting
        String rateLimitKey = (currentUser != null) ? "user-" + currentUser.getId() : "ip-" + clientIp;
        if (!rateLimiterService.tryAcquire(rateLimitKey)) {
            logger.warn("Rate limit exceeded for key: {}", rateLimitKey);
            throw new RateLimitExceededException("Too many analysis requests. Please wait a moment before trying again.");
        }

        // Run ML inference
        PredictionResult prediction = modelPredictor.predict(request.getHeadline(), request.getText());

        Long analysisId = null;
        Instant createdAt = Instant.now();

        // If authenticated user, persist to database
        if (currentUser != null) {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Analysis analysis = new Analysis(
                    user,
                    request.getHeadline(),
                    request.getText(),
                    request.getSourceUrl(),
                    prediction.getLabel(),
                    prediction.getScore(),
                    prediction.getModelVersion()
            );

            Analysis saved = analysisRepository.save(analysis);
            analysisId = saved.getId();
            createdAt = saved.getCreatedAt();

            // Safe privacy logging: Never log the full submitted content
            logger.info("Analysis persisted: id={}, user_id={}, label={}, score={}",
                    saved.getId(), user.getId(), saved.getLabel(), saved.getScore());
        } else {
            logger.info("Anonymous analysis completed: label={}, score={}",
                    prediction.getLabel(), prediction.getScore());
        }

        return new AnalyzeResponse(
                analysisId,
                request.getHeadline(),
                request.getText(),
                request.getSourceUrl(),
                prediction.getLabel(),
                prediction.getScore(),
                prediction.getModelVersion(),
                prediction.getExplanation(),
                prediction.getTopFakeFeatures(),
                prediction.getTopGenuineFeatures(),
                prediction.getLimitations(),
                prediction.getVerificationActions(),
                prediction.getCharacterCount(),
                prediction.getWordCount(),
                createdAt
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnalysisHistoryItemResponse> getUserAnalyses(UserPrincipal currentUser, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Analysis> analysisPage = analysisRepository.findByUserId(currentUser.getId(), pageable);

        List<AnalysisHistoryItemResponse> content = analysisPage.getContent().stream()
                .map(this::mapToHistoryItem)
                .toList();

        return new PagedResponse<>(
                content,
                analysisPage.getNumber(),
                analysisPage.getSize(),
                analysisPage.getTotalElements(),
                analysisPage.getTotalPages(),
                analysisPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public AnalyzeResponse getAnalysisById(Long id, UserPrincipal currentUser) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + id));

        // Recompute feature explanations on the stored text
        PredictionResult prediction = modelPredictor.predict(analysis.getHeadline(), analysis.getText());

        return new AnalyzeResponse(
                analysis.getId(),
                analysis.getHeadline(),
                analysis.getText(),
                analysis.getSourceUrl(),
                analysis.getLabel(),
                analysis.getScore(),
                analysis.getModelVersion(),
                prediction.getExplanation(),
                prediction.getTopFakeFeatures(),
                prediction.getTopGenuineFeatures(),
                prediction.getLimitations(),
                prediction.getVerificationActions(),
                prediction.getCharacterCount(),
                prediction.getWordCount(),
                analysis.getCreatedAt()
        );
    }

    @Transactional
    public void deleteAnalysis(Long id, UserPrincipal currentUser) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + id));

        analysisRepository.delete(analysis);
        logger.info("Analysis deleted: id={}, user_id={}", id, currentUser.getId());
    }

    private AnalysisHistoryItemResponse mapToHistoryItem(Analysis a) {
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
    }
}
