package com.fakenews.controller;

import com.fakenews.dto.VerifyRequest;
import com.fakenews.dto.VerifyResponse;
import com.fakenews.service.FactCheckService;
import com.fakenews.service.RateLimitExceededException;
import com.fakenews.service.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verify")
@Tag(name = "Fact Checking", description = "Checks a claim against fact-check databases and credible news sources")
public class VerifyController {

    private final FactCheckService factCheckService;
    private final RateLimiterService rateLimiterService;

    public VerifyController(FactCheckService factCheckService, RateLimiterService rateLimiterService) {
        this.factCheckService = factCheckService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping
    @Operation(summary = "Check a news claim against fact-checkers and credible news outlets")
    public ResponseEntity<VerifyResponse> verify(@Valid @RequestBody VerifyRequest request,
                                                 HttpServletRequest servletRequest) {
        String key = "verify-ip-" + extractClientIp(servletRequest);
        if (!rateLimiterService.tryAcquire(key)) {
            throw new RateLimitExceededException("Too many fact-check requests. Please wait a moment and try again.");
        }
        return ResponseEntity.ok(factCheckService.verify(request.getHeadline(), request.getText()));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf == null || xf.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xf.contains(",") ? xf.split(",")[0].trim() : xf.trim();
    }
}
