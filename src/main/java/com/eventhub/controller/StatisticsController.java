package com.eventhub.controller;

import com.eventhub.dto.response.AdminStatisticsResponse;
import com.eventhub.dto.response.OrganizerStatisticsResponse;
import com.eventhub.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Organizer and platform statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "Get organizer statistics", description = "Returns statistics for current organizer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizer statistics returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organizer role required")})
    @GetMapping("/organizer/statistics")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<OrganizerStatisticsResponse> getOrganizerStatistics() {
        return ResponseEntity.ok(statisticsService.getOrganizerStatistics());
    }

    @Operation(summary = "Get platform statistics", description = "Returns platform statistics for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin statistics returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin role required")})
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatisticsResponse> getAdminStatistics() {
        return ResponseEntity.ok(statisticsService.getAdminStatistics());
    }
}