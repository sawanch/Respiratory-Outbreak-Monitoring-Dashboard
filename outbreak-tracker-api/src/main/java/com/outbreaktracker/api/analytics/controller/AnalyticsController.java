package com.outbreaktracker.api.analytics.controller;

import com.outbreaktracker.api.analytics.model.AnalyticsInsightsResponse;
import com.outbreaktracker.api.analytics.service.AnalyticsService;
import com.outbreaktracker.api.analytics.service.AnalyticsAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * REST Controller for analytics endpoints
 * Provides metrics data for dashboard visualization
 * All MongoDB analytics code is kept in analytics package for easy explanation
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;
    private final AnalyticsAiService analyticsAiService;

    public AnalyticsController(AnalyticsService analyticsService, AnalyticsAiService analyticsAiService) {
        this.analyticsService = analyticsService;
        this.analyticsAiService = analyticsAiService;
    }

    /**
     * GET /api/analytics/summary - Returns aggregated analytics summary
     * Includes: endpoint counts, average response times, success/error rates
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        logger.info("GET /api/analytics/summary - Fetching analytics summary");
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get all aggregated stats
        summary.put("endpointStats", analyticsService.getEndpointStats());
        summary.put("responseTimeStats", analyticsService.getResponseTimeStats());
        summary.put("successErrorRates", analyticsService.getSuccessErrorRates());
        
        // Calculate total requests
        long totalRequests = 0;
        for (Long count : analyticsService.getEndpointStats().values()) {
            totalRequests += count;
        }
        summary.put("totalRequests", totalRequests);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/analytics/timeline - Returns recent request timeline
     * Returns last 100 requests with timestamp, endpoint, method, status code
     */
    @GetMapping("/timeline")
    public ResponseEntity<List<Map<String, Object>>> getTimeline() {
        logger.info("GET /api/analytics/timeline - Fetching request timeline");
        return ResponseEntity.ok(analyticsService.getTimeline());
    }

    /**
     * GET /api/analytics/ai-insights - Returns AI-powered system analysis
     * Provides intelligent performance analysis and recommendations using OpenAI
     */
    @GetMapping("/ai-insights")
    public ResponseEntity<AnalyticsInsightsResponse> getSystemInsights() {
        logger.info("GET /api/analytics/ai-insights - Fetching AI system insights");
        
        AnalyticsInsightsResponse insights = analyticsAiService.getSystemInsights();
        return ResponseEntity.ok(insights);
    }
}

