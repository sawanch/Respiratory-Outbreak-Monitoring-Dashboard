package com.outbreaktracker.api.analytics.model;

import java.util.List;

/**
 * Response model for AI-powered analytics insights
 * Contains system performance metrics and AI-generated recommendations
 */
public class AnalyticsInsightsResponse {
    
    private Integer totalRequests;
    private String slowestEndpoint;
    private Double avgResponseTime;
    private Double errorRate;
    private String overallAssessment;
    private List<AnalyticsInsightCard> recommendations;
    private String timestamp;
    private String model;
    private Double temperature;
    private Integer tokensUsed;
    
    // Constructors
    public AnalyticsInsightsResponse() {}

    // Getters and Setters
    public Integer getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Integer totalRequests) {
        this.totalRequests = totalRequests;
    }

    public String getSlowestEndpoint() {
        return slowestEndpoint;
    }

    public void setSlowestEndpoint(String slowestEndpoint) {
        this.slowestEndpoint = slowestEndpoint;
    }

    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public String getOverallAssessment() {
        return overallAssessment;
    }

    public void setOverallAssessment(String overallAssessment) {
        this.overallAssessment = overallAssessment;
    }

    public List<AnalyticsInsightCard> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<AnalyticsInsightCard> recommendations) {
        this.recommendations = recommendations;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
}
