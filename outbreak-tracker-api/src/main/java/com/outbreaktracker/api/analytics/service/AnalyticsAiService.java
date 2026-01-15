package com.outbreaktracker.api.analytics.service;

import com.outbreaktracker.api.analytics.model.AnalyticsInsightsResponse;

/**
 * Service interface for AI-powered analytics insights
 * Generates intelligent system performance analysis using OpenAI
 */
public interface AnalyticsAiService {
    
    /**
     * Generate AI-powered system performance insights
     * Analyzes current system metrics and provides recommendations
     * 
     * @return AnalyticsInsightsResponse with AI analysis and recommendations
     */
    AnalyticsInsightsResponse getSystemInsights();
}
