package com.outbreaktracker.api.aiinsights.model;

import java.util.List;

/**
 * Model representing AI-generated respiratory outbreak insights for a country
 * Contains country statistics and AI-generated recommendations
 */
public class CovidInsightsResponse {
    
    private String country;
    private Long totalCases;
    private Long newCases;
    private Long totalDeaths;
    private Long totalRecovered;
    private Long activeCases;
    private String overallAssessment;
    private List<InsightCard> recommendations;
    private List<PrecautionGroup> targetedPrecautions;
    private String generatedAt;
    
    public CovidInsightsResponse() {
    }
    
    // Getters and Setters
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public Long getTotalCases() {
        return totalCases;
    }
    
    public void setTotalCases(Long totalCases) {
        this.totalCases = totalCases;
    }
    
    public Long getNewCases() {
        return newCases;
    }
    
    public void setNewCases(Long newCases) {
        this.newCases = newCases;
    }
    
    public Long getTotalDeaths() {
        return totalDeaths;
    }
    
    public void setTotalDeaths(Long totalDeaths) {
        this.totalDeaths = totalDeaths;
    }
    
    public Long getTotalRecovered() {
        return totalRecovered;
    }
    
    public void setTotalRecovered(Long totalRecovered) {
        this.totalRecovered = totalRecovered;
    }
    
    public Long getActiveCases() {
        return activeCases;
    }
    
    public void setActiveCases(Long activeCases) {
        this.activeCases = activeCases;
    }
    
    public String getOverallAssessment() {
        return overallAssessment;
    }
    
    public void setOverallAssessment(String overallAssessment) {
        this.overallAssessment = overallAssessment;
    }
    
    public List<InsightCard> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<InsightCard> recommendations) {
        this.recommendations = recommendations;
    }
    
    public List<PrecautionGroup> getTargetedPrecautions() {
        return targetedPrecautions;
    }
    
    public void setTargetedPrecautions(List<PrecautionGroup> targetedPrecautions) {
        this.targetedPrecautions = targetedPrecautions;
    }
    
    public String getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    @Override
    public String toString() {
        return "CovidInsightsResponse{" +
                "country='" + country + '\'' +
                ", totalCases=" + totalCases +
                ", newCases=" + newCases +
                ", totalDeaths=" + totalDeaths +
                ", totalRecovered=" + totalRecovered +
                ", activeCases=" + activeCases +
                ", overallAssessment='" + overallAssessment + '\'' +
                ", recommendations=" + recommendations +
                ", targetedPrecautions=" + targetedPrecautions +
                ", generatedAt='" + generatedAt + '\'' +
                '}';
    }
}
