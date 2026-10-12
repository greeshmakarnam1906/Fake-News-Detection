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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisService Unit & Ownership Tests")
class AnalysisServiceTest {

    @Mock
    private ModelPredictor modelPredictor;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RateLimiterService rateLimiterService;

    private AnalysisService analysisService;

    private User testUserA;
    private UserPrincipal principalA;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(modelPredictor, analysisRepository, userRepository, rateLimiterService);

        testUserA = new User("Alice", "alice@example.com", "hash", "ROLE_USER");
        testUserA.setId(1L);
        principalA = UserPrincipal.create(testUserA);
    }

    @Test
    @DisplayName("Should successfully analyze content and persist for authenticated user")
    void testAnalyzeAuthenticatedUser() {
        AnalyzeRequest req = new AnalyzeRequest("Headline", "This is verified news article text.", null, "Politics");

        PredictionResult pred = new PredictionResult(
                "Likely Genuine", 0.25, "1.0.0", "Valid news style",
                Collections.emptyList(), Collections.emptyList(),
                List.of("Limitation 1"), List.of("Verification 1"),
                40, 6
        );

        when(rateLimiterService.tryAcquire(anyString())).thenReturn(true);
        when(modelPredictor.predict(any(), any())).thenReturn(pred);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(invocation -> {
            Analysis a = invocation.getArgument(0);
            a.setId(99L);
            return a;
        });

        AnalyzeResponse res = analysisService.analyze(req, principalA, "127.0.0.1");

        assertNotNull(res);
        assertEquals(99L, res.getId());
        assertEquals("Likely Genuine", res.getLabel());
        assertEquals(0.25, res.getScore());
        verify(analysisRepository).save(any(Analysis.class));
    }

    @Test
    @DisplayName("Should enforce ownership: User A cannot read User B's analysis")
    void testUserCannotReadAnotherUsersAnalysis() {
        // Mock that analysis 500 is NOT found for User A (user_id = 1)
        when(analysisRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                analysisService.getAnalysisById(500L, principalA)
        );
    }

    @Test
    @DisplayName("Should enforce ownership: User A cannot delete User B's analysis")
    void testUserCannotDeleteAnotherUsersAnalysis() {
        when(analysisRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                analysisService.deleteAnalysis(500L, principalA)
        );

        verify(analysisRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throttle when rate limit exceeded")
    void testRateLimitThrottle() {
        AnalyzeRequest req = new AnalyzeRequest("Headline", "Sample news text", null, null);
        when(rateLimiterService.tryAcquire(anyString())).thenReturn(false);

        assertThrows(RateLimitExceededException.class, () ->
                analysisService.analyze(req, principalA, "127.0.0.1")
        );

        verify(modelPredictor, never()).predict(any(), any());
    }

    @Test
    @DisplayName("Should return paginated user analyses")
    void testGetPaginatedUserAnalyses() {
        Analysis analysis = new Analysis(testUserA, "Headline", "Some long body text...", null, "Likely Fake", 0.85, "1.0.0");
        analysis.setId(10L);

        when(analysisRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(analysis)));

        PagedResponse<AnalysisHistoryItemResponse> result = analysisService.getUserAnalyses(principalA, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals("Likely Fake", result.getContent().get(0).getLabel());
    }
}
