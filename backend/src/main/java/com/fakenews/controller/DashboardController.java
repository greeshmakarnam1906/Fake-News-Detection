package com.fakenews.controller;

import com.fakenews.dto.DashboardSummaryResponse;
import com.fakenews.security.UserPrincipal;
import com.fakenews.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for analysis statistics and aggregate user metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get aggregate statistics of previous analyses for the current authenticated user")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        DashboardSummaryResponse response = dashboardService.getSummary(principal);
        return ResponseEntity.ok(response);
    }
}
