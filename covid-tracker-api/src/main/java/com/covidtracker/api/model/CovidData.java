package com.covidtracker.api.model;

import java.util.Objects;

/**
 * Model representing COVID-19 statistics for a single country
 * Maps directly to CSV data columns
 */
public class CovidData {
    
    private String country;
    private Long totalCases;
    private Long newCases;
    private Long totalDeaths;
    private Long newDeaths;
    private Long totalRecovered;
    private Long activeCases;
    private Long criticalCases;
    private String lastUpdated;

    public CovidData() {
    }

    public CovidData(String country, Long totalCases, Long newCases, Long totalDeaths, 
                     Long newDeaths, Long totalRecovered, Long activeCases, 
                     Long criticalCases, String lastUpdated) {
        this.country = country;
        this.totalCases = totalCases;
        this.newCases = newCases;
        this.totalDeaths = totalDeaths;
        this.newDeaths = newDeaths;
        this.totalRecovered = totalRecovered;
        this.activeCases = activeCases;
        this.criticalCases = criticalCases;
        this.lastUpdated = lastUpdated;
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

    public Long getNewDeaths() {
        return newDeaths;
    }

    public void setNewDeaths(Long newDeaths) {
        this.newDeaths = newDeaths;
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

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CovidData that = (CovidData) o;
        return Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country);
    }

    @Override
    public String toString() {
        return "CovidData{" +
                "country='" + country + '\'' +
                ", totalCases=" + totalCases +
                ", newCases=" + newCases +
                ", totalDeaths=" + totalDeaths +
                ", newDeaths=" + newDeaths +
                ", totalRecovered=" + totalRecovered +
                ", activeCases=" + activeCases +
                ", criticalCases=" + criticalCases +
                ", lastUpdated='" + lastUpdated + '\'' +
                '}';
    }
}

