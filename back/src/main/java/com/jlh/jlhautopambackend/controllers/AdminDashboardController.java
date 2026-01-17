package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.AdminDashboardStatsDto;
import com.jlh.jlhautopambackend.services.AdminDashboardStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard-stats")
public class AdminDashboardController {

    private final AdminDashboardStatsService statsService;

    public AdminDashboardController(AdminDashboardStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardStatsDto> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}
