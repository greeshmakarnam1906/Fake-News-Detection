package com.fakenews.controller;

import com.fakenews.dto.AnalysisHistoryItemResponse;
import com.fakenews.dto.AnalyzeRequest;
import com.fakenews.dto.AnalyzeResponse;
import com.fakenews.dto.PagedResponse;
import com.fakenews.security.UserPrincipal;
import com.fakenews.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses")
@Tag(name = "News Analysis", description = "Endpoints for content classification, prediction history, and individual analysis retrieval")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    @Operation(summary = "Submit a news article or social media post for machine-learning analysis")
    public ResponseEntity<AnalyzeResponse> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest servletRequest) {

        String clientIp = extractClientIp(servletRequest);
        AnalyzeResponse response = analysisService.analyze(request, principal, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated analysis history for the authenticated user")
    public ResponseEntity<PagedResponse<AnalysisHistoryItemResponse>> getUserAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        PagedResponse<AnalysisHistoryItemResponse> response = analysisService.getUserAnalyses(principal, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of a specific analysis record owned by the authenticated user")
    public ResponseEntity<AnalyzeResponse> getAnalysisById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        AnalyzeResponse response = analysisService.getAnalysisById(id, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an analysis record owned by the authenticated user")
    public ResponseEntity<Void> deleteAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        analysisService.deleteAnalysis(id, principal);
        return ResponseEntity.noContent().build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return (xfHeader != null && !xfHeader.isEmpty()) ? xfHeader : request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
