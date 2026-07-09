package com.eventhub.controller;

import com.eventhub.dto.response.AdminStatisticsResponse;
import com.eventhub.dto.response.OrganizerStatisticsResponse;
import com.eventhub.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/organizer/statistics")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<OrganizerStatisticsResponse> getOrganizerStatistics() {
        return ResponseEntity.ok(statisticsService.getOrganizerStatistics());
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatisticsResponse> getAdminStatistics() {
        return ResponseEntity.ok(statisticsService.getAdminStatistics());
    }
}