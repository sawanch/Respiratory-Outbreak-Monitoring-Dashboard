package com.covidtracker.api.model;

/**
 * Model representing aggregated worldwide COVID-19 statistics
 * Calculated by summing data from all countries
 */
public class GlobalStats {
    
    private Long totalCases;
    private Long totalDeaths;
    private Long totalRecovered;
    private Long activeCases;
    private Long criticalCases;
    private int affectedCountries;
    private String lastUpdated;

    public GlobalStats() {
    }

    public GlobalStats(Long totalCases, Long totalDeaths, Long totalRecovered, 
                       Long activeCases, Long criticalCases, int affectedCountries, 
                       String lastUpdated) {
        this.totalCases = totalCases;
        this.totalDeaths = totalDeaths;
        this.totalRecovered = totalRecovered;
        this.activeCases = activeCases;
        this.criticalCases = criticalCases;
        this.affectedCountries = affectedCountries;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public Long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(Long totalCases) {
        this.totalCases = totalCases;
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

    public Long getCriticalCases() {
        return criticalCases;
    }

    public void setCriticalCases(Long criticalCases) {
        this.criticalCases = criticalCases;
    }

    public int getAffectedCountries() {
        return affectedCountries;
    }

    public void setAffectedCountries(int affectedCountries) {
        this.affectedCountries = affectedCountries;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "GlobalStats{" +
                "totalCases=" + totalCases +
                ", totalDeaths=" + totalDeaths +
                ", totalRecovered=" + totalRecovered +
                ", activeCases=" + activeCases +
                ", criticalCases=" + criticalCases +
                ", affectedCountries=" + affectedCountries +
                ", lastUpdated='" + lastUpdated + '\'' +
                '}';
    }
}

