package com.eventhub.service;

import com.eventhub.dto.response.AdminStatisticsResponse;
import com.eventhub.dto.response.OrganizerStatisticsResponse;

public interface StatisticsService {

    OrganizerStatisticsResponse getOrganizerStatistics();

    AdminStatisticsResponse getAdminStatistics();
}