package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.AdminDashboardAnalyticsDto;
import com.jlh.jlhautopambackend.dto.AdminDashboardStatsDto;
import com.jlh.jlhautopambackend.services.AdminDashboardStatsService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admin/dashboard-stats")
public class AdminDashboardController {

    private final AdminDashboardStatsService statsService;

    public AdminDashboardController(AdminDashboardStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AdminDashboardStatsDto> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AdminDashboardAnalyticsDto> getAnalytics(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) List<String> statuts,
            @RequestParam(required = false) List<Integer> serviceIds,
            @RequestParam(required = false, defaultValue = "true") boolean includeForecast
    ) {
        var filter = new AdminDashboardStatsService.AdminDashboardAnalyticsFilter(
                from,
                to,
                types,
                statuts,
                serviceIds,
                includeForecast
        );
        return ResponseEntity.ok(statsService.getAnalytics(filter));
    }
}
