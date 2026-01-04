package com.covidtracker.api.analytics.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for analytics operations
 * Provides aggregated metrics from MongoDB for dashboard visualization
 * 
 * Design: Service layer abstracts repository operations
 * - Repository handles data access (findAll)
 * - Service handles business logic (aggregations, calculations)
 * - Methods use findAll() internally, then process data in-memory
 * 
 * Why no findAll() here? Service provides business operations, not raw data access.
 * Demonstrates layered architecture: Controller -> Service -> Repository
 */
public interface AnalyticsService {

    /**
     * Returns request counts per endpoint
     * @return Map: endpoint -> request count
     */
    Map<String, Long> getEndpointStats();

    /**
     * Returns average response times per endpoint
     * @return Map: endpoint -> average response time (ms)
     */
    Map<String, Double> getResponseTimeStats();

    /**
     * Returns success/error counts per endpoint
     * Success = 2xx, Error = 4xx/5xx status codes
     * @return Map: endpoint -> {success: count, error: count}
     */
    Map<String, Map<String, Long>> getSuccessErrorRates();

    /**
     * Returns recent request timeline (last 100 requests)
     * Sorted by timestamp descending (most recent first)
     * @return List of request metrics with timestamp, endpoint, method, status code, response time
     */
    List<Map<String, Object>> getTimeline();
}

